package app.apartment.mobile.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import app.apartment.mobile.contact.ContactAction
import app.apartment.mobile.contact.ContactResult
import app.apartment.mobile.data.SettingsEntity
import app.apartment.mobile.domain.BackupData
import app.apartment.mobile.domain.BackupError
import app.apartment.mobile.domain.BackupProperty
import app.apartment.mobile.domain.BackupRental
import app.apartment.mobile.domain.BackupSettings
import app.apartment.mobile.domain.BackupTenant
import app.apartment.mobile.domain.Rental
import app.apartment.mobile.ui.theme.ApartmentTheme
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** TC-021 (AC-FR-025-1/2, AC-FR-026-1/2), TC-022 (AC-FR-027-1), TC-023 (AC-FR-028-1), TC-024 (AC-FR-029-1/2) on screen. */
class ContactAndBackupUiTest {
    @get:Rule val rule = createComposeRule()

    private val today = LocalDate.of(2026, 11, 4)
    private val rental = Rental(1, 1, 1, 250_000, 3, LocalDate.of(2026, 1, 1), null)

    private fun showDetail(phone: String?, result: ContactResult = ContactResult.OPENED, log: MutableList<ContactAction> = mutableListOf()) {
        rule.setContent {
            ApartmentTheme {
                RentalDetailScreen(
                    rental, "María Rojas", "Apto 3", today, onBack = {}, onEdit = {}, onEnd = { _, _ -> }, onDelete = {},
                    phone = phone, onContact = { log += it; result },
                )
            }
        }
    }

    // --- contact
    @Test
    fun withAPhoneBothActionsAreOfferedAndOpenTheirApps() {
        val log = mutableListOf<ContactAction>()
        showDetail("8888 1111", log = log)
        rule.onNodeWithTag("contact-whatsapp").performClick()
        rule.onNodeWithTag("contact-call").performClick()
        assertEquals(listOf(ContactAction.WHATSAPP, ContactAction.CALL), log)
    }

    @Test
    fun withoutAPhoneTheActionsAreNotShown() {
        showDetail(null)
        rule.onNodeWithTag("contact-whatsapp").assertDoesNotExist()
        rule.onNodeWithTag("contact-call").assertDoesNotExist()
    }

    @Test
    fun aBlankPhoneCountsAsNoPhone() {
        showDetail("   ")
        rule.onNodeWithTag("contact-whatsapp").assertDoesNotExist()
    }

    @Test
    fun aMissingWhatsAppIsExplained() {
        showDetail("8888 1111", ContactResult.WHATSAPP_MISSING)
        rule.onNodeWithTag("contact-whatsapp").performClick()
        rule.onNodeWithText("WhatsApp no está instalado en este teléfono.").assertExists()
    }

    // --- backup: export warning (FR-027)
    @Test
    fun theExportRowAsksForTheWarning() {
        var asked = 0
        rule.setContent { ApartmentTheme { SettingsScreen(SettingsEntity(), onExport = { asked++ }, onChange = {}) } }
        rule.onNodeWithTag("backup-export").performClick()
        assertEquals(1, asked)
    }

    @Test
    fun theExportWarningSaysTheFileIsNotEncryptedAndCancelExportsNothing() {
        var exported = 0
        var dismissed = 0
        rule.setContent {
            ApartmentTheme { BackupDialogs(BackupDialog.ExportWarning, onExport = { exported++ }, onConfirmImport = {}, onDismiss = { dismissed++ }) }
        }
        rule.onNodeWithText("El archivo guarda los nombres y teléfonos de tus inquilinos sin cifrar. Quien lo reciba podrá leerlo. Compártelo solo con quien confíes.").assertExists()
        rule.onNodeWithText("Cancelar").performClick()
        assertEquals(0, exported)
        assertEquals(1, dismissed)
    }

    @Test
    fun confirmingTheWarningExports() {
        var exported = 0
        rule.setContent {
            ApartmentTheme { BackupDialogs(BackupDialog.ExportWarning, onExport = { exported++ }, onConfirmImport = {}, onDismiss = {}) }
        }
        rule.onNodeWithText("Exportar").performClick()
        assertEquals(1, exported)
    }

    // --- backup: import confirmation (FR-028) and rejections (FR-029)
    private val data = BackupData(
        tenants = listOf(BackupTenant(1, "María Rojas", null)),
        properties = listOf(BackupProperty(1, "Apto 3", null), BackupProperty(2, "Apto 4", null)),
        rentals = listOf(BackupRental(1, 1, 1, 250_000, 3, "2026-01-01", null)),
        settings = BackupSettings(listOf(0), "08:00"),
    )

    @Test
    fun importingStatesWhatIsLostAndOnlyReplacesAfterConfirming() {
        var confirmed: BackupData? = null
        rule.setContent {
            ApartmentTheme { BackupDialogs(BackupDialog.ImportConfirm(data), onExport = {}, onConfirmImport = { confirmed = it }, onDismiss = {}) }
        }
        rule.onNodeWithText("Contenido de la copia: inquilinos 1, propiedades 2, alquileres 1. Tus datos actuales se perderán y no se puede deshacer.").assertExists()
        assertTrue(confirmed == null) // nothing replaced without the explicit confirmation (AC-FR-028-1)
        rule.onNodeWithText("Reemplazar todo").performClick()
        assertEquals(data, confirmed)
    }

    @Test
    fun everyRejectionSaysTheDataDidNotChange() {
        var shown by mutableStateOf<BackupError?>(BackupError.NOT_A_BACKUP)
        rule.setContent { ApartmentTheme { BackupDialogs(BackupDialog.ImportInvalid(shown), {}, {}, {}) } }
        val errors: List<BackupError?> = BackupError.entries + null
        for (error in errors) {
            rule.runOnIdle { shown = error }
            rule.onNodeWithText("No se pudo importar").assertExists()
            rule.onNodeWithText("Tus datos no cambiaron.", substring = true).assertExists()
        }
    }
}
