package org.koitharu.kotatsu.browser

import android.graphics.Bitmap
import android.webkit.WebView
import org.koin.core.component.KoinComponent
import org.koitharu.kotatsu.core.network.WebViewClientCompat

class BrowserClient(private val callback: BrowserCallback) : WebViewClientCompat(), KoinComponent {

	override fun onPageFinished(webView: WebView, url: String) {
		super.onPageFinished(webView, url)
		callback.onLoadingStateChanged(isLoading = false)
	}

	override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
		super.onPageStarted(view, url, favicon)
		callback.onLoadingStateChanged(isLoading = true)
	}

	override fun onPageCommitVisible(view: WebView, url: String?) {
		super.onPageCommitVisible(view, url)
		callback.onTitleChanged(view.title.orEmpty(), url)
	}
}