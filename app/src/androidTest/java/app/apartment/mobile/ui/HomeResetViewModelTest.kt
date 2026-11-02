package app.apartment.mobile.ui

import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test

/** TC-035 (AC-FR-016-1, AC-FR-016-3, CHG-003) on the real view model, with the time passed in. */
class HomeResetViewModelTest {
    private val minute = 60_000L

    private fun viewModel() =
        AppViewModel(InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application)

    @Test
    fun comingBackWithinFiveMinutesKeepsTheTabAndTheWeek() {
        val vm = viewModel()
        vm.selectTab(Tab.RENTALS)
        vm.showWeek(1)
        vm.appLeft(now = 1_000)
        vm.appReturned(now = 1_000 + 2 * minute)
        assertEquals(Tab.RENTALS, vm.tab)
        assertEquals(1, vm.weekOffset)
    }

    @Test
    fun comingBackAfterMoreThanFiveMinutesShowsTheCurrentWeek() {
        val vm = viewModel()
        vm.selectTab(Tab.RENTALS)
        vm.showWeek(1)
        vm.appLeft(now = 1_000)
        vm.appReturned(now = 1_000 + 6 * minute)
        assertEquals(Tab.WEEK, vm.tab)
        assertEquals(0, vm.weekOffset)
    }

    @Test
    fun anOpenFormIsNotClosedByAnAbsence() {
        val vm = viewModel()
        vm.open(Screen.RentalForm(null))
        vm.showWeek(-1)
        vm.appLeft(now = 0)
        vm.appReturned(now = 20 * minute)
        assertEquals(Screen.RentalForm(null), vm.screen)
        assertEquals(0, vm.weekOffset)
    }

    @Test
    fun aTappedReminderAlwaysOpensTheCurrentWeek() {
        val vm = viewModel()
        vm.selectTab(Tab.SETTINGS)
        vm.showWeek(1)
        vm.showCurrentWeek()
        assertEquals(Tab.WEEK, vm.tab)
        assertEquals(0, vm.weekOffset)
    }
}
