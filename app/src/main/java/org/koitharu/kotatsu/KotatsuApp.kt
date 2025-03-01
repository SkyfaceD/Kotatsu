package org.koitharu.kotatsu

import android.app.Application
import android.os.StrictMode
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.strictmode.FragmentStrictMode
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koitharu.kotatsu.base.ui.util.ActivityRecreationHandle
import org.koitharu.kotatsu.bookmarks.bookmarksModule
import org.koitharu.kotatsu.core.db.databaseModule
import org.koitharu.kotatsu.core.github.githubModule
import org.koitharu.kotatsu.core.network.networkModule
import org.koitharu.kotatsu.core.prefs.AppSettings
import org.koitharu.kotatsu.core.ui.AppCrashHandler
import org.koitharu.kotatsu.core.ui.uiModule
import org.koitharu.kotatsu.details.detailsModule
import org.koitharu.kotatsu.favourites.favouritesModule
import org.koitharu.kotatsu.history.historyModule
import org.koitharu.kotatsu.local.data.PagesCache
import org.koitharu.kotatsu.local.domain.LocalMangaRepository
import org.koitharu.kotatsu.local.localModule
import org.koitharu.kotatsu.main.mainModule
import org.koitharu.kotatsu.main.ui.protect.AppProtectHelper
import org.koitharu.kotatsu.parsers.MangaLoaderContext
import org.koitharu.kotatsu.reader.readerModule
import org.koitharu.kotatsu.remotelist.remoteListModule
import org.koitharu.kotatsu.search.searchModule
import org.koitharu.kotatsu.settings.settingsModule
import org.koitharu.kotatsu.suggestions.suggestionsModule
import org.koitharu.kotatsu.tracker.trackerModule
import org.koitharu.kotatsu.widget.WidgetUpdater
import org.koitharu.kotatsu.widget.appWidgetModule

class KotatsuApp : Application() {

	override fun onCreate() {
		super.onCreate()
		if (BuildConfig.DEBUG) {
			enableStrictMode()
		}
		initKoin()
		Thread.setDefaultUncaughtExceptionHandler(AppCrashHandler(applicationContext))
		AppCompatDelegate.setDefaultNightMode(get<AppSettings>().theme)
		registerActivityLifecycleCallbacks(get<AppProtectHelper>())
		registerActivityLifecycleCallbacks(get<ActivityRecreationHandle>())
		val widgetUpdater = WidgetUpdater(applicationContext)
		widgetUpdater.subscribeToFavourites(get())
		widgetUpdater.subscribeToHistory(get())
	}

	private fun initKoin() {
		startKoin {
			androidContext(this@KotatsuApp)
			modules(
				networkModule,
				databaseModule,
				githubModule,
				uiModule,
				mainModule,
				searchModule,
				localModule,
				favouritesModule,
				historyModule,
				remoteListModule,
				detailsModule,
				trackerModule,
				settingsModule,
				readerModule,
				appWidgetModule,
				suggestionsModule,
				bookmarksModule,
			)
		}
	}

	private fun enableStrictMode() {
		StrictMode.setThreadPolicy(
			StrictMode.ThreadPolicy.Builder()
				.detectAll()
				.penaltyLog()
				.build()
		)
		StrictMode.setVmPolicy(
			StrictMode.VmPolicy.Builder()
				.detectAll()
				.setClassInstanceLimit(LocalMangaRepository::class.java, 1)
				.setClassInstanceLimit(PagesCache::class.java, 1)
				.setClassInstanceLimit(MangaLoaderContext::class.java, 1)
				.penaltyLog()
				.build()
		)
		FragmentStrictMode.defaultPolicy = FragmentStrictMode.Policy.Builder()
			.penaltyDeath()
			.detectFragmentReuse()
			.detectWrongFragmentContainer()
			.detectRetainInstanceUsage()
			.detectSetUserVisibleHint()
			.detectFragmentTagUsage()
			.build()
	}
}