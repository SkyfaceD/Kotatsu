package org.koitharu.kotatsu.utils.ext

import android.content.res.Resources
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.core.exceptions.CloudFlareProtectedException
import org.koitharu.kotatsu.core.exceptions.EmptyHistoryException
import org.koitharu.kotatsu.core.exceptions.UnsupportedFileException
import org.koitharu.kotatsu.core.exceptions.WrongPasswordException
import org.koitharu.kotatsu.parsers.exception.AuthRequiredException
import java.io.FileNotFoundException
import java.net.SocketTimeoutException

fun Throwable.getDisplayMessage(resources: Resources) = when (this) {
	is AuthRequiredException -> resources.getString(R.string.auth_required)
	is CloudFlareProtectedException -> resources.getString(R.string.captcha_required)
	is UnsupportedOperationException -> resources.getString(R.string.operation_not_supported)
	is UnsupportedFileException -> resources.getString(R.string.text_file_not_supported)
	is FileNotFoundException -> resources.getString(R.string.file_not_found)
	is EmptyHistoryException -> resources.getString(R.string.history_is_empty)
	is SocketTimeoutException -> resources.getString(R.string.network_error)
	is WrongPasswordException -> resources.getString(R.string.wrong_password)
	else -> localizedMessage ?: resources.getString(R.string.error_occurred)
}