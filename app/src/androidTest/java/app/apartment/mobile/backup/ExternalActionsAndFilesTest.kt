package app.apartment.mobile.backup

import android.content.Context
import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import app.apartment.mobile.contact.ContactAction
import app.apartment.mobile.contact.ContactResult
import app.apartment.mobile.contact.ExternalActions
import java.io.File
import java.time.LocalDate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** FR-025, FR-026 (intents), FR-027 (temporary file, share), FR-028 (reading the picked file), SEC-002. On a device. */
class ExternalActionsAndFilesTest {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @After
    fun clean() = BackupFiles.cleanOld(context)

    // --- contact
    @Test
    fun theDialerIntentOpensTheDialerWithTheNumber() {
        val intent = ExternalActions.dialIntent("+506 8888-1111")!!
        assertEquals(Intent.ACTION_DIAL, intent.action)
        assertEquals("tel", intent.data!!.scheme)
        assertEquals("+50688881111", intent.data!!.schemeSpecificPart)
        assertNull(ExternalActions.dialIntent(null))
    }

    @Test
    fun theWhatsAppIntentPointsAtTheChatOfTheTenantInTheWhatsAppApp() {
        val intent = ExternalActions.whatsAppIntent("8888 1111", "com.whatsapp")!!
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals("https://wa.me/50688881111", intent.data.toString())
        assertEquals("com.whatsapp", intent.`package`)
    }

    @Test
    fun withoutANumberNothingOpens() {
        assertEquals(ContactResult.NO_NUMBER, ExternalActions.open(context, null, ContactAction.WHATSAPP))
        assertEquals(ContactResult.NO_NUMBER, ExternalActions.open(context, "  ", ContactAction.CALL))
    }

    @Test
    fun whenWhatsAppIsNotInstalledTheLandlordIsToldInsteadOfOpeningAnything() {
        // The emulator image has no WhatsApp. If a device has it, this check does not apply.
        if (ExternalActions.whatsAppPackage(context) == null) {
            assertEquals(ContactResult.WHATSAPP_MISSING, ExternalActions.open(context, "8888 1111", ContactAction.WHATSAPP))
        }
    }

    // --- backup files
    @Test
    fun theExportFileIsWrittenUnderTheDateNameAndOfferedThroughAFileProvider() {
        val uri = BackupFiles.write(context, """{"format":"apartment-backup"}""", LocalDate.of(2026, 11, 4))
        assertEquals("${context.packageName}.files", uri.authority)
        assertTrue(uri.lastPathSegment!!.endsWith("apartment-backup-2026-11-04.json"))
        assertEquals("""{"format":"apartment-backup"}""", BackupFiles.readText(context, uri))
        val share = BackupFiles.shareIntent(uri, "Compartir")
        assertEquals(Intent.ACTION_CHOOSER, share.action)
        val inner = share.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)!!
        assertEquals(Intent.ACTION_SEND, inner.action)
        assertEquals("application/json", inner.type)
        assertTrue(inner.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
    }

    @Test
    fun noPartialFileIsLeftAndOldFilesAreRemoved() {
        val folder = File(context.cacheDir, "backups")
        BackupFiles.write(context, "one", LocalDate.of(2026, 11, 3))
        BackupFiles.write(context, "two", LocalDate.of(2026, 11, 4))
        assertEquals(listOf("apartment-backup-2026-11-04.json"), folder.list()!!.toList())
        BackupFiles.cleanOld(context)
        assertTrue(folder.list().isNullOrEmpty())
    }

    @Test
    fun aFileTooBigToBeABackupIsNotRead() {
        val uri = BackupFiles.write(context, "x".repeat(BackupFiles.MAX_BYTES + 10), LocalDate.of(2026, 11, 4))
        assertNull(BackupFiles.readText(context, uri))
    }

    @Test
    fun aMissingFileReadsAsNull() {
        val uri = android.net.Uri.parse("content://${context.packageName}.files/backups/none.json")
        assertNull(BackupFiles.readText(context, uri))
        assertNotNull(BackupFiles.fileName(LocalDate.of(2026, 1, 2)))
        assertFalse(BackupFiles.fileName(LocalDate.of(2026, 1, 2)).contains("María"))
    }
}
