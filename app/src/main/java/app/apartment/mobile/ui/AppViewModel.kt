package app.apartment.mobile.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.apartment.mobile.data.AppDatabase
import app.apartment.mobile.data.CreateRentalResult
import app.apartment.mobile.data.PropertyEntity
import app.apartment.mobile.data.Repository
import app.apartment.mobile.data.SettingsEntity
import app.apartment.mobile.data.TenantEntity
import app.apartment.mobile.domain.Rental
import app.apartment.mobile.domain.RentalError
import app.apartment.mobile.domain.WeekNavigation
import app.apartment.mobile.reminders.ReminderPermissions
import app.apartment.mobile.reminders.ReminderScheduler
import java.time.LocalDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

    /** FR-016: leaving the app and coming back shows the current week again. */
    fun resetHome() {
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
}
