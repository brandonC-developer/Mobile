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
import app.apartment.mobile.data.TenantEntity
import app.apartment.mobile.domain.Rental
import app.apartment.mobile.domain.RentalError
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

/** One screen-state holder for the milestone H2 screens (CMP-001). Business rules live in the repository. */
class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repository(AppDatabase.get(app)) { LocalDate.now() }

    val properties = repo.properties.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val tenants = repo.tenants.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val rentals = repo.rentals.map { list -> list.map { it.toDomain() } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList<Rental>())

    var screen: Screen by mutableStateOf(Screen.Tabs)
        private set
    var tab by mutableStateOf(Tab.RENTALS)
        private set

    fun today(): LocalDate = LocalDate.now()

    fun open(next: Screen) { screen = next }
    fun selectTab(next: Tab) { tab = next }
    fun back() {
        screen = when (val s = screen) {
            is Screen.RentalForm -> if (s.rentalId != null) Screen.RentalDetail(s.rentalId) else Screen.Tabs
            else -> Screen.Tabs
        }
    }

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
