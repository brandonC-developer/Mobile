package app.apartment.mobile.ui

import android.app.Application
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.apartment.mobile.R
import app.apartment.mobile.backup.BackupFiles
import app.apartment.mobile.data.AppDatabase
import app.apartment.mobile.data.CreateRentalResult
import app.apartment.mobile.data.PropertyEntity
import app.apartment.mobile.data.Repository
import app.apartment.mobile.data.SettingsEntity
import app.apartment.mobile.data.TenantEntity
import app.apartment.mobile.domain.BackupCodec
import app.apartment.mobile.domain.BackupData
import app.apartment.mobile.domain.BackupError
import app.apartment.mobile.domain.BackupResult
import app.apartment.mobile.domain.HomeReset
import app.apartment.mobile.domain.Rental
import app.apartment.mobile.domain.RentalError
import app.apartment.mobile.domain.WeekNavigation
import app.apartment.mobile.reminders.ReminderPermissions
import app.apartment.mobile.reminders.ReminderScheduler
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class Tab { WEEK, CALENDAR, RENTALS, SETTINGS }

sealed interface Screen {
    data object Tabs : Screen
    data object People : Screen
    data class RentalForm(val rentalId: Long?) : Screen
    data class RentalDetail(val rentalId: Long) : Screen
}

/** One screen-state holder (CMP-001). Business rules live in the repository; reminders are rebuilt on every change. */
class AppViewModel(private val app: Application) : AndroidViewModel(app) {
    private val repo = Repository(AppDatabase.get(app), onChanged = { ReminderScheduler.rebuild(app) }) { LocalDate.now() }

    val properties = repo.properties.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val tenants = repo.tenants.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val rentals = repo.rentals.map { list -> list.map { it.toDomain() } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList<Rental>())
    val settings = repo.settings.map { it ?: SettingsEntity() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SettingsEntity())

    var screen: Screen by mutableStateOf(Screen.Tabs)
        private set
    var tab by mutableStateOf(Tab.WEEK)
        private set
    var weekOffset by mutableStateOf(0)
        private set
    var permissions by mutableStateOf(ReminderPermissions.state(app))
        private set
    var showPermissionExplanation by mutableStateOf(false)
        private set

    fun today(): LocalDate = LocalDate.now()

    fun showWeek(offset: Int) { weekOffset = WeekNavigation.clamp(offset) }

    private var leftAt: Long? = null

    /** The app left the screen (any reason). The time comes from a clock that keeps running while the phone sleeps. */
    fun appLeft(now: Long = SystemClock.elapsedRealtime()) { leftAt = now }

    /** FR-016, CHG-003: back on screen; the current week returns only after more than 5 minutes away. */
    fun appReturned(now: Long = SystemClock.elapsedRealtime()) {
        if (HomeReset.shouldReset(leftAt, now)) resetHome()
        leftAt = null
    }

    private fun resetHome() {
        weekOffset = 0
        if (screen == Screen.Tabs) tab = Tab.WEEK
    }

    /** FR-020: a tapped reminder opens the app on the current week. */
    fun showCurrentWeek() {
        weekOffset = 0
        tab = Tab.WEEK
        screen = Screen.Tabs
    }

    fun open(next: Screen) { screen = next }
    fun selectTab(next: Tab) { tab = next }
    fun back() {
        screen = when (val s = screen) {
            is Screen.RentalForm -> if (s.rentalId != null) Screen.RentalDetail(s.rentalId) else Screen.Tabs
            else -> Screen.Tabs
        }
    }

    /** FR-021: the reminders are rebuilt each time the app is opened. */
    fun refreshOnOpen() {
        permissions = ReminderPermissions.state(app)
        viewModelScope.launch { ReminderScheduler.rebuild(app) }
    }

    fun refreshPermissions() { permissions = ReminderPermissions.state(app) }

    /** FR-023: once, the first time a rental is created and something is missing. */
    fun askPermissionsIfFirstRental() {
        refreshPermissions()
        if (!permissions.allGranted && !ReminderPermissions.alreadyPrompted(app)) showPermissionExplanation = true
    }

    fun dismissPermissionExplanation() {
        ReminderPermissions.markPrompted(app)
        showPermissionExplanation = false
    }

    fun saveSettings(next: SettingsEntity) = viewModelScope.launch { repo.saveSettings(next) }

    fun saveProperty(id: Long?, name: String, description: String, done: (Boolean) -> Unit) = viewModelScope.launch {
        val ok = if (id == null) {
            repo.createProperty(name, description) != null
        } else {
            repo.updateProperty(PropertyEntity(id, name, description))
        }
        done(ok)
    }

    fun saveTenant(id: Long?, fullName: String, phone: String, done: (Boolean) -> Unit) = viewModelScope.launch {
        val ok = if (id == null) repo.createTenant(fullName, phone) != null else repo.updateTenant(TenantEntity(id, fullName, phone))
        done(ok)
    }

    fun rentalCount(tenantId: Long, done: (Int) -> Unit) = viewModelScope.launch { done(repo.rentalCount(tenantId)) }

    fun deleteTenant(tenantId: Long) = viewModelScope.launch { repo.deleteTenant(tenantId) }

    fun createRental(
        tenantId: Long, propertyId: Long, amount: Long, paymentDay: Int, endDate: LocalDate?,
        done: (CreateRentalResult) -> Unit,
    ) = viewModelScope.launch { done(repo.createRental(tenantId, propertyId, amount, paymentDay, endDate)) }

    fun updateRental(id: Long, amount: Long, paymentDay: Int, endDate: LocalDate?, done: (List<RentalError>) -> Unit) =
        viewModelScope.launch { done(repo.updateRental(id, amount, paymentDay, endDate)) }

    fun endRental(id: Long, endDate: LocalDate, done: (List<RentalError>) -> Unit) =
        viewModelScope.launch { done(repo.endRental(id, endDate)) }

    fun deleteRental(id: Long) = viewModelScope.launch { repo.deleteRental(id) }

    // --- backup (FR-027, FR-028, FR-029)
    var backupDialog: BackupDialog? by mutableStateOf(null)
        private set

    fun askExport() { backupDialog = BackupDialog.ExportWarning }
    fun dismissBackupDialog() { backupDialog = null }

    /** Builds the file and hands the share intent to [done], or null if it could not be prepared. */
    fun export(done: (android.content.Intent?) -> Unit) {
        backupDialog = null
        viewModelScope.launch {
            val intent = try {
                val text = BackupCodec.encode(repo.exportData(), java.time.Instant.now())
                val uri = withContext(Dispatchers.IO) { BackupFiles.write(app, text, LocalDate.now()) }
                BackupFiles.shareIntent(uri, app.getString(R.string.backup_share_title))
            } catch (_: Exception) {
                null
            }
            done(intent)
        }
    }

    /** Reads and validates the picked file completely; nothing is written until the landlord confirms (ADR-0004). */
    fun importFrom(uri: android.net.Uri) {
        viewModelScope.launch {
            val text = withContext(Dispatchers.IO) { BackupFiles.readText(app, uri) }
            backupDialog = when (val result = text?.let { BackupCodec.decode(it, LocalDate.now()) }) {
                null -> BackupDialog.ImportInvalid(null)
                is BackupResult.Valid -> BackupDialog.ImportConfirm(result.data)
                is BackupResult.Invalid -> BackupDialog.ImportInvalid(result.error)
            }
        }
    }

    fun confirmImport(data: BackupData) {
        viewModelScope.launch {
            backupDialog = try {
                repo.replaceAll(data)
                BackupDialog.ImportDone
            } catch (_: Exception) {
                BackupDialog.ImportInvalid(null) // rolled back: the previous data is intact (NFR-005)
            }
        }
    }

    init {
        BackupFiles.cleanOld(app) // SEC-002: no old backup file stays in the cache
    }
}

/** What the backup flow asks the landlord, one at a time. */
sealed interface BackupDialog {
    data object ExportWarning : BackupDialog
    data class ImportConfirm(val data: BackupData) : BackupDialog
    data class ImportInvalid(val error: BackupError?) : BackupDialog
    data object ImportDone : BackupDialog
}
