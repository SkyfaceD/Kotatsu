package org.koitharu.kotatsu.widget.recent

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import coil.ImageLoader
import coil.executeBlocking
import coil.request.ImageRequest
import kotlinx.coroutines.runBlocking
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.base.domain.MangaIntent
import org.koitharu.kotatsu.history.domain.HistoryRepository
import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.utils.ext.requireBitmap
import java.io.IOException

class RecentListFactory(
	private val context: Context,
	private val historyRepository: HistoryRepository,
	private val coil: ImageLoader
) : RemoteViewsService.RemoteViewsFactory {

	private val dataSet = ArrayList<Manga>()

	override fun onCreate() {
	}

	override fun getLoadingView() = null

	override fun getItemId(position: Int) = dataSet[position].id

	override fun onDataSetChanged() {
		dataSet.clear()
		val data = runBlocking { historyRepository.getList(0, 10) }
		dataSet.addAll(data)
	}

	override fun hasStableIds() = true

	override fun getViewAt(position: Int): RemoteViews {
		val views = RemoteViews(context.packageName, R.layout.item_recent)
		val item = dataSet[position]
		try {
			val cover = coil.executeBlocking(
				ImageRequest.Builder(context)
					.data(item.coverUrl)
					.build()
			).requireBitmap()
			views.setImageViewBitmap(R.id.imageView_cover, cover)
		} catch (e: IOException) {
			views.setImageViewResource(R.id.imageView_cover, R.drawable.ic_placeholder)
		}
		val intent = Intent()
		intent.putExtra(MangaIntent.KEY_ID, item.id)
		views.setOnClickFillInIntent(R.id.imageView_cover, intent)
		return views
	}

	override fun getCount() = dataSet.size

	override fun getViewTypeCount() = 1

	override fun onDestroy() {
	}
}