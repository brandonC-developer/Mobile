package app.apartment.mobile.contact

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import app.apartment.mobile.domain.PhoneNumber

enum class ContactAction { WHATSAPP, CALL }

enum class ContactResult { OPENED, NO_NUMBER, WHATSAPP_MISSING, NO_APP }

/**
 * CMP-008 (FR-025, FR-026, INT-004, INT-005): opens WhatsApp or the dialer on the tenant's number. The landlord sends the
 * message or places the call in that app; nothing leaves this app (NFR-002).
 */
object ExternalActions {
    private val WHATSAPP_PACKAGES = listOf("com.whatsapp", "com.whatsapp.w4b")

    /** The installed WhatsApp package, or null (OI-044 c). Needs the `queries` entry of the manifest. */
    fun whatsAppPackage(context: Context): String? = WHATSAPP_PACKAGES.firstOrNull { pkg ->
        try {
            context.packageManager.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(0))
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun whatsAppIntent(phone: String?, pkg: String): Intent? =
        PhoneNumber.whatsAppLink(phone)?.let { Intent(Intent.ACTION_VIEW, Uri.parse(it)).setPackage(pkg) }

    /** ACTION_DIAL only opens the dialer with the number; the landlord presses call, so no permission is needed. */
    fun dialIntent(phone: String?): Intent? =
        PhoneNumber.dialNumber(phone)?.let { Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", it, null)) }

    fun open(context: Context, phone: String?, action: ContactAction): ContactResult {
        if (!PhoneNumber.hasNumber(phone)) return ContactResult.NO_NUMBER
        val intent = when (action) {
            ContactAction.WHATSAPP -> {
                val pkg = whatsAppPackage(context) ?: return ContactResult.WHATSAPP_MISSING
                whatsAppIntent(phone, pkg)
            }
            ContactAction.CALL -> dialIntent(phone)
        } ?: return ContactResult.NO_NUMBER
        return try {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            ContactResult.OPENED
        } catch (_: ActivityNotFoundException) {
            ContactResult.NO_APP
        }
    }
}
