package org.koitharu.kotatsu.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.annotation.MainThread
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.get
import org.koitharu.kotatsu.BuildConfig
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.core.github.AppVersion
import org.koitharu.kotatsu.core.github.GithubRepository
import org.koitharu.kotatsu.core.github.VersionId
import org.koitharu.kotatsu.core.prefs.AppSettings
import org.koitharu.kotatsu.parsers.util.byte2HexFormatted
import org.koitharu.kotatsu.utils.FileSize
import org.koitharu.kotatsu.utils.ext.printStackTraceDebug
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateEncodingException
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit

class AppUpdateChecker(private val activity: ComponentActivity) {

	private val settings = activity.get<AppSettings>()
	private val repo = activity.get<GithubRepository>()

	suspend fun checkIfNeeded(): Boolean? = if (
		settings.isUpdateCheckingEnabled &&
		settings.lastUpdateCheckTimestamp + PERIOD < System.currentTimeMillis()
	) {
		checkNow()
	} else {
		null
	}

	suspend fun checkNow() = runCatching {
		val version = repo.getLatestVersion()
		val newVersionId = VersionId(version.name)
		val currentVersionId = VersionId(BuildConfig.VERSION_NAME)
		val result = newVersionId > currentVersionId
		if (result) {
			withContext(Dispatchers.Main) {
				showUpdateDialog(version)
			}
		}
		settings.lastUpdateCheckTimestamp = System.currentTimeMillis()
		result
	}.onFailure {
		it.printStackTraceDebug()
	}.getOrNull()

	@MainThread
	private fun showUpdateDialog(version: AppVersion) {
		val message = buildString {
			append(activity.getString(R.string.new_version_s, version.name))
			appendLine()
			append(activity.getString(R.string.size_s, FileSize.BYTES.format(activity, version.apkSize)))
			appendLine()
			appendLine()
			append(version.description)
		}
		MaterialAlertDialogBuilder(activity)
			.setTitle(R.string.app_update_available)
			.setMessage(message)
			.setPositiveButton(R.string.download) { _, _ ->
				activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(version.apkUrl)))
			}
			.setNegativeButton(R.string.close, null)
			.setCancelable(false)
			.create()
			.show()
	}

	companion object {

		private const val CERT_SHA1 = "2C:19:C7:E8:07:61:2B:8E:94:51:1B:FD:72:67:07:64:5D:C2:58:AE"
		private val PERIOD = TimeUnit.HOURS.toMillis(6)

		fun isUpdateSupported(context: Context): Boolean {
			return getCertificateSHA1Fingerprint(context) == CERT_SHA1
		}

		@Suppress("DEPRECATION")
		@SuppressLint("PackageManagerGetSignatures")
		private fun getCertificateSHA1Fingerprint(context: Context): String? {
			val packageInfo = try {
				context.packageManager.getPackageInfo(
					context.packageName,
					PackageManager.GET_SIGNATURES
				)
			} catch (e: PackageManager.NameNotFoundException) {
				e.printStackTraceDebug()
				return null
			}
			val signatures = packageInfo?.signatures
			val cert: ByteArray = signatures?.firstOrNull()?.toByteArray() ?: return null
			val input: InputStream = ByteArrayInputStream(cert)
			val c = try {
				val cf = CertificateFactory.getInstance("X509")
				cf.generateCertificate(input) as X509Certificate
			} catch (e: CertificateException) {
				e.printStackTraceDebug()
				return null
			}
			return try {
				val md: MessageDigest = MessageDigest.getInstance("SHA1")
				val publicKey: ByteArray = md.digest(c.encoded)
				publicKey.byte2HexFormatted()
			} catch (e: NoSuchAlgorithmException) {
				e.printStackTraceDebug()
				null
			} catch (e: CertificateEncodingException) {
				e.printStackTraceDebug()
				null
			}
		}
	}
}