@file:OptIn(ExperimentalMaterial3Api::class)

package app.apartment.mobile.ui

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.apartment.mobile.R
import app.apartment.mobile.domain.Format
import app.apartment.mobile.reminders.ReminderPermissions

/** CMP-001. Four bottom destinations (OI-044 e). */
@Composable
fun AppRoot(vm: AppViewModel = viewModel()) {
    val context = LocalContext.current
    val tenants by vm.tenants.collectAsStateWithLifecycle()
    val properties by vm.properties.collectAsStateWithLifecycle()
    val rentals by vm.rentals.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val today = vm.today()
    var pendingMessage by remember { mutableStateOf<String?>(null) }
    val savedText = stringResource(R.string.rental_saved, "%s")

    // FR-023 and FR-024: ask for what is missing; when the system dialog cannot be shown, go to the settings screen.
    var fromBanner by remember { mutableStateOf(false) }
    fun openExactAlarmSettings() {
        if (!vm.permissions.exactAlarmsAllowed) context.startActivity(ReminderPermissions.exactAlarmSettings(context))
    }
    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        vm.refreshPermissions()
        if (!granted && fromBanner) {
            context.startActivity(ReminderPermissions.notificationSettings(context))
        } else {
            openExactAlarmSettings()
        }
    }
    fun fixPermissions(banner: Boolean) {
        fromBanner = banner
        vm.refreshPermissions()
        if (!vm.permissions.notificationsEnabled) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            openExactAlarmSettings()
        }
    }

    BackHandler(enabled = vm.screen != Screen.Tabs) { vm.back() }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { vm.resetHome() }
    // FR-021: opening the app rebuilds the reminders; coming back from a settings screen refreshes the warning.
    LifecycleEventEffect(Lifecycle.Event.ON_START) { vm.refreshOnOpen() }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.refreshPermissions() }
    LaunchedEffect(pendingMessage) {
        pendingMessage?.let { snackbar.showSnackbar(it); pendingMessage = null }
    }

    when (val screen = vm.screen) {
        Screen.Tabs -> Scaffold(
            snackbarHost = { SnackbarHost(snackbar) },
            bottomBar = {
                Column {
                    // The warning stays visible on every tab and never blocks the app (FR-024). It matters once
                    // there is a rental to remind about.
                    if (rentals.isNotEmpty()) PermissionBanner(vm.permissions) { fixPermissions(banner = true) }
                    NavigationBar {
                        tabs.forEach { (tab, icon, label) ->
                            NavigationBarItem(
                                selected = vm.tab == tab, onClick = { vm.selectTab(tab) },
                                icon = { Icon(icon, contentDescription = null) }, label = { Text(stringResource(label)) },
                            )
                        }
                    }
                }
            },
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
                when (vm.tab) {
                    Tab.WEEK -> WeekScreen(rentals, tenants, properties, today, vm.weekOffset, vm::showWeek)
                    Tab.CALENDAR -> CalendarScreen(rentals, tenants, properties, today)
                    Tab.RENTALS -> RentalsScreen(
                        rentals, tenants, properties, today,
                        onNew = { vm.open(Screen.RentalForm(null)) },
                        onOpen = { vm.open(Screen.RentalDetail(it)) },
                        onPeople = { vm.open(Screen.People) },
                    )
                    Tab.SETTINGS -> SettingsScreen(settings, onChange = { vm.saveSettings(it) })
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
                if (screen.rentalId == null) vm.askPermissionsIfFirstRental()
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

    if (vm.showPermissionExplanation) {
        AlertDialog(
            onDismissRequest = vm::dismissPermissionExplanation,
            title = { Text(stringResource(R.string.perm_explain_title)) },
            text = { Text(stringResource(R.string.perm_explain_body)) },
            confirmButton = {
                TextButton(onClick = { vm.dismissPermissionExplanation(); fixPermissions(banner = false) }) {
                    Text(stringResource(R.string.perm_explain_continue))
                }
            },
            dismissButton = {
                TextButton(onClick = vm::dismissPermissionExplanation) { Text(stringResource(R.string.perm_explain_later)) }
            },
        )
    }
}

private data class TabItem(val tab: Tab, val icon: ImageVector, val label: Int)

private val tabs = listOf(
    TabItem(Tab.WEEK, Icons.Default.List, R.string.nav_week),
    TabItem(Tab.CALENDAR, Icons.Default.DateRange, R.string.nav_calendar),
    TabItem(Tab.RENTALS, Icons.Default.Home, R.string.nav_rentals),
    TabItem(Tab.SETTINGS, Icons.Default.Settings, R.string.nav_settings),
)
