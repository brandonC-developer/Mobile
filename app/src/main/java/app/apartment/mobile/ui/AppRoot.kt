@file:OptIn(ExperimentalMaterial3Api::class)

package app.apartment.mobile.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.apartment.mobile.R
import app.apartment.mobile.domain.Format
import kotlinx.coroutines.launch

/** CMP-001. Four bottom destinations (OI-044 e); Week, Calendar and Settings arrive in later milestones. */
@Composable
fun AppRoot(vm: AppViewModel = viewModel()) {
    val tenants by vm.tenants.collectAsStateWithLifecycle()
    val properties by vm.properties.collectAsStateWithLifecycle()
    val rentals by vm.rentals.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val today = vm.today()
    var pendingMessage by remember { mutableStateOf<String?>(null) }
    val savedText = stringResource(R.string.rental_saved, "%s")

    BackHandler(enabled = vm.screen != Screen.Tabs) { vm.back() }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { vm.resetHome() }
    LaunchedEffect(pendingMessage) {
        pendingMessage?.let { snackbar.showSnackbar(it); pendingMessage = null }
    }

    when (val screen = vm.screen) {
        Screen.Tabs -> Scaffold(
            snackbarHost = { SnackbarHost(snackbar) },
            bottomBar = {
                NavigationBar {
                    tabs.forEach { (tab, icon, label) ->
                        NavigationBarItem(
                            selected = vm.tab == tab, onClick = { vm.selectTab(tab) },
                            icon = { Icon(icon, contentDescription = null) }, label = { Text(stringResource(label)) },
                        )
                    }
                }
            },
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
                when (vm.tab) {
                    Tab.RENTALS -> RentalsScreen(
                        rentals, tenants, properties, today,
                        onNew = { vm.open(Screen.RentalForm(null)) },
                        onOpen = { vm.open(Screen.RentalDetail(it)) },
                        onPeople = { vm.open(Screen.People) },
                    )
                    Tab.WEEK -> WeekScreen(rentals, tenants, properties, today, vm.weekOffset, vm::showWeek)
                    Tab.CALENDAR -> CalendarScreen(rentals, tenants, properties, today)
                    Tab.SETTINGS -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(stringResource(R.string.soon)) }
                }
            }
        }

        Screen.People -> PeopleScreen(
            tenants, properties, rentals, today, onBack = vm::back,
            onSaveTenant = { id, name, phone, done -> vm.saveTenant(id, name, phone, done) },
            onSaveProperty = { id, name, desc, done -> vm.saveProperty(id, name, desc, done) },
            onRentalCount = { id, done -> vm.rentalCount(id, done) },
            onDeleteTenant = { vm.deleteTenant(it) },
        )

        is Screen.RentalForm -> RentalFormScreen(
            editing = rentals.firstOrNull { it.id == screen.rentalId }, rentals = rentals, tenants = tenants,
            properties = properties, today = today, onBack = vm::back,
            onGoPeople = { vm.open(Screen.People) },
            onCreate = { t, p, a, d, e, done -> vm.createRental(t, p, a, d, e, done) },
            onUpdate = { id, a, d, e, done -> vm.updateRental(id, a, d, e, done) },
            onSaved = { next ->
                if (next != null) pendingMessage = savedText.replace("%s", Format.longDate(next))
                vm.open(if (screen.rentalId != null) Screen.RentalDetail(screen.rentalId) else Screen.Tabs)
            },
        )

        is Screen.RentalDetail -> {
            val rental = rentals.firstOrNull { it.id == screen.rentalId }
            RentalDetailScreen(
                rental = rental,
                tenantName = tenants.firstOrNull { it.id == rental?.tenantId }?.fullName.orEmpty(),
                propertyName = properties.firstOrNull { it.id == rental?.propertyId }?.name.orEmpty(),
                today = today, onBack = vm::back,
                onEdit = { vm.open(Screen.RentalForm(screen.rentalId)) },
                onEnd = { date, done -> vm.endRental(screen.rentalId, date, done) },
                onDelete = { vm.deleteRental(screen.rentalId); vm.back() },
            )
        }
    }
}

private data class TabItem(val tab: Tab, val icon: ImageVector, val label: Int)

private val tabs = listOf(
    TabItem(Tab.WEEK, Icons.Default.List, R.string.nav_week),
    TabItem(Tab.CALENDAR, Icons.Default.DateRange, R.string.nav_calendar),
    TabItem(Tab.RENTALS, Icons.Default.Home, R.string.nav_rentals),
    TabItem(Tab.SETTINGS, Icons.Default.Settings, R.string.nav_settings),
)
