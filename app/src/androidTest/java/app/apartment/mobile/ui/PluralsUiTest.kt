package app.apartment.mobile.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import app.apartment.mobile.data.PropertyEntity
import app.apartment.mobile.data.TenantEntity
import app.apartment.mobile.domain.Rental
import app.apartment.mobile.ui.theme.ApartmentTheme
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test

/** NFR-007 (Spanish text) and AC-FR-006-1: counts read correctly in singular and plural. */
class PluralsUiTest {
    @get:Rule val rule = createComposeRule()

    private val today = LocalDate.of(2026, 11, 4)
    private val tenants = listOf(TenantEntity(1, "María Rojas"))
    private val properties = listOf(PropertyEntity(1, "Apto 3"))

    private fun rental(id: Long, end: LocalDate?) = Rental(id, 1, 1, 250_000, 3, LocalDate.of(2026, 1, 1), end)

    @Test
    fun theRentalSummaryUsesTheSingularForOne() {
        rule.setContent {
            ApartmentTheme { RentalsScreen(listOf(rental(1, null)), tenants, properties, today, {}, {}, {}) }
        }
        rule.onNodeWithText("1 activo · 0 terminados").assertExists()
    }

    @Test
    fun theRentalSummaryUsesThePluralOtherwise() {
        val ended = LocalDate.of(2026, 9, 30)
        rule.setContent {
            ApartmentTheme { RentalsScreen(listOf(rental(1, ended), rental(2, ended)), tenants, properties, today, {}, {}, {}) }
        }
        rule.onNodeWithText("0 activos · 2 terminados").assertExists()
    }

    private fun showDeleteDialog(rentalCount: Int) {
        rule.setContent {
            ApartmentTheme {
                PeopleScreen(
                    tenants, properties, emptyList(), today, onBack = {}, onSaveTenant = { _, _, _, _ -> },
                    onSaveProperty = { _, _, _, _ -> }, onRentalCount = { _, done -> done(rentalCount) }, onDeleteTenant = {},
                )
            }
        }
        rule.onNodeWithContentDescription("Eliminar").performClick()
    }

    @Test
    fun deletingATenantWithOneRentalSaysOneRental() {
        showDeleteDialog(1)
        rule.onNodeWithText("Se eliminará también su alquiler. La propiedad se conserva. No se puede deshacer.").assertExists()
        rule.onNodeWithText("Eliminar inquilino y 1 alquiler").assertExists()
    }

    @Test
    fun deletingATenantWithTwoRentalsStatesTheCount() {
        showDeleteDialog(2)
        rule.onNodeWithText("Se eliminarán también sus 2 alquileres. Las propiedades se conservan. No se puede deshacer.").assertExists()
        rule.onNodeWithText("Eliminar inquilino y 2 alquileres").assertExists()
    }
}
