package app.apartment.mobile.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.time.LocalDate

/** CMP-007 on Android (FR-027, FR-028): the temporary file to share, and reading a file the landlord picks. */
object BackupFiles {
    /** A backup of a few tenants is a few kilobytes; anything much bigger is not one of ours. */
    const val MAX_BYTES = 5_000_000

    private fun dir(context: Context) = File(context.cacheDir, "backups")

    fun fileName(date: LocalDate) = "apartment-backup-$date.json"

    /** The file name carries no personal data (SEC-002). The file appears under its final name only once complete. */
    fun write(context: Context, text: String, date: LocalDate): Uri {
        cleanOld(context)
        val folder = dir(context).apply { mkdirs() }
        val partial = File(folder, fileName(date) + ".part")
        partial.writeText(text, Charsets.UTF_8)
        val file = File(folder, fileName(date))
        check(partial.renameTo(file)) { "could not finish the backup file" }
        return FileProvider.getUriForFile(context, "${context.packageName}.files", file)
    }

    fun shareIntent(uri: Uri, title: String): Intent = Intent.createChooser(
        Intent(Intent.ACTION_SEND).setType("application/json").putExtra(Intent.EXTRA_STREAM, uri)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),
        title,
    )

    /** Removes earlier temporary files (SEC-002): called before each export and when the app starts. */
    fun cleanOld(context: Context) {
        dir(context).listFiles()?.forEach { it.delete() }
    }

    /** The text of the picked file, or null when it cannot be read or is too big to be a backup. */
    fun readText(context: Context, uri: Uri): String? = try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            val bytes = input.readNBytes(MAX_BYTES + 1)
            if (bytes.size > MAX_BYTES) null else String(bytes, Charsets.UTF_8)
        }
    } catch (_: IOException) {
        null
    } catch (_: SecurityException) {
        null
    }
}
