package app.apartment.mobile

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Document
import org.w3c.dom.Element

/** TC-026 (NFR-002, SEC-005, SEC-007, OI-042): no Internet, only the permissions reminders need, backup blocked. */
class ManifestPolicyTest {
    private val android = "http://schemas.android.com/apk/res/android"

    private fun parse(path: String): Document =
        DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
            .newDocumentBuilder().parse(File(path))

    private val manifest get() = parse("src/main/AndroidManifest.xml")

    private fun permissions(): List<String> {
        val perms = manifest.getElementsByTagName("uses-permission")
        return (0 until perms.length).map { perms.item(it).attributes.getNamedItemNS(android, "name").nodeValue }
    }

    @Test
    fun internetPermissionIsNeverDeclared() {
        assertFalse("INTERNET must not be declared", permissions().contains("android.permission.INTERNET"))
    }

    @Test
    fun onlyTheReminderPermissionsAreDeclared() {
        assertEquals(
            setOf(
                "android.permission.POST_NOTIFICATIONS",
                "android.permission.SCHEDULE_EXACT_ALARM",
                "android.permission.RECEIVE_BOOT_COMPLETED",
            ),
            permissions().toSet(),
        )
    }

    @Test
    fun alarmReceiverIsPrivateAndSystemReceiverHandlesOnlyTwoBroadcasts() {
        val receivers = manifest.getElementsByTagName("receiver")
        val byName = (0 until receivers.length).map { receivers.item(it) as Element }
            .associateBy { it.getAttributeNS(android, "name") }
        assertEquals("false", byName.getValue(".reminders.AlarmReceiver").getAttributeNS(android, "exported"))
        val system = byName.getValue(".reminders.SystemEventReceiver")
        assertEquals("true", system.getAttributeNS(android, "exported"))
        val actions = system.getElementsByTagName("action")
        assertEquals(
            setOf(
                "android.intent.action.BOOT_COMPLETED",
                "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED",
            ),
            (0 until actions.length).map { (actions.item(it) as Element).getAttributeNS(android, "name") }.toSet(),
        )
    }

    @Test
    fun androidBackupIsDisabled() {
        val app = manifest.getElementsByTagName("application").item(0)
        assertEquals("false", app.attributes.getNamedItemNS(android, "allowBackup").nodeValue)
        assertTrue(app.attributes.getNamedItemNS(android, "dataExtractionRules") != null)
        assertTrue(app.attributes.getNamedItemNS(android, "fullBackupContent") != null)
    }

    @Test
    fun cloudBackupAndDeviceTransferExcludeEverything() {
        val rules = parse("src/main/res/xml/data_extraction_rules.xml")
        for (section in listOf("cloud-backup", "device-transfer")) {
            val node = rules.getElementsByTagName(section).item(0)
            assertTrue("$section missing", node != null)
            val excluded = (0 until node.childNodes.length).map { node.childNodes.item(it) }
                .filter { it.nodeName == "exclude" }
                .map { it.attributes.getNamedItem("domain").nodeValue }
            assertEquals(listOf("root", "file", "database", "sharedpref", "external"), excluded)
            assertFalse("$section must not include anything", node.textContent.contains("<include"))
        }
    }
}
