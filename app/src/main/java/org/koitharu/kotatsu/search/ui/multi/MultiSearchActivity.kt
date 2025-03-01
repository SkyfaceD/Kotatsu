package org.koitharu.kotatsu.search.ui.multi

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ActionMode
import androidx.core.graphics.Insets
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.base.ui.BaseActivity
import org.koitharu.kotatsu.base.ui.list.OnListItemClickListener
import org.koitharu.kotatsu.databinding.ActivitySearchMultiBinding
import org.koitharu.kotatsu.details.ui.DetailsActivity
import org.koitharu.kotatsu.download.ui.service.DownloadService
import org.koitharu.kotatsu.favourites.ui.categories.select.FavouriteCategoriesBottomSheet
import org.koitharu.kotatsu.list.ui.MangaSelectionDecoration
import org.koitharu.kotatsu.list.ui.adapter.MangaListListener
import org.koitharu.kotatsu.parsers.model.Manga
import org.koitharu.kotatsu.parsers.model.MangaTag
import org.koitharu.kotatsu.search.ui.SearchActivity
import org.koitharu.kotatsu.search.ui.multi.adapter.ItemSizeResolver
import org.koitharu.kotatsu.search.ui.multi.adapter.MultiSearchAdapter
import org.koitharu.kotatsu.utils.ShareHelper
import org.koitharu.kotatsu.utils.ext.findViewsByType

class MultiSearchActivity : BaseActivity<ActivitySearchMultiBinding>(), MangaListListener, ActionMode.Callback {

	private val viewModel by viewModel<MultiSearchViewModel> {
		parametersOf(intent.getStringExtra(EXTRA_QUERY).orEmpty())
	}
	private lateinit var adapter: MultiSearchAdapter
	private lateinit var selectionDecoration: MangaSelectionDecoration
	private var actionMode: ActionMode? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(ActivitySearchMultiBinding.inflate(layoutInflater))

		val itemCLickListener = object : OnListItemClickListener<MultiSearchListModel> {
			override fun onItemClick(item: MultiSearchListModel, view: View) {
				startActivity(SearchActivity.newIntent(view.context, item.source, viewModel.query.value))
			}
		}
		val sizeResolver = ItemSizeResolver(resources, get())
		selectionDecoration = MangaSelectionDecoration(this)
		adapter = MultiSearchAdapter(
			lifecycleOwner = this,
			coil = get(),
			listener = this,
			itemClickListener = itemCLickListener,
			sizeResolver = sizeResolver,
			selectionDecoration = selectionDecoration,
		)
		binding.recyclerView.adapter = adapter
		binding.recyclerView.setHasFixedSize(true)

		supportActionBar?.run {
			setDisplayHomeAsUpEnabled(true)
			setSubtitle(R.string.search_results)
		}

		viewModel.query.observe(this) { title = it }
		viewModel.list.observe(this) { adapter.items = it }
	}

	override fun onWindowInsetsChanged(insets: Insets) {
		with(binding.toolbar) {
			updatePadding(
				left = insets.left,
				right = insets.right,
			)
			updateLayoutParams<ViewGroup.MarginLayoutParams> {
				topMargin = insets.top
			}
		}
		binding.recyclerView.updatePadding(
			bottom = insets.bottom,
			left = insets.left,
			right = insets.right,
		)
	}

	override fun onItemClick(item: Manga, view: View) {
		if (selectionDecoration.checkedItemsCount != 0) {
			selectionDecoration.toggleItemChecked(item.id)
			if (selectionDecoration.checkedItemsCount == 0) {
				actionMode?.finish()
			} else {
				actionMode?.invalidate()
				invalidateItemDecorations()
			}
			return
		}
		val intent = DetailsActivity.newIntent(this, item)
		startActivity(intent)
	}

	override fun onItemLongClick(item: Manga, view: View): Boolean {
		if (actionMode == null) {
			actionMode = startSupportActionMode(this)
		}
		return actionMode?.also {
			selectionDecoration.setItemIsChecked(item.id, true)
			invalidateItemDecorations()
			it.invalidate()
		} != null
	}

	override fun onRetryClick(error: Throwable) {
		viewModel.doSearch(viewModel.query.value.orEmpty())
	}

	override fun onTagRemoveClick(tag: MangaTag) = Unit

	override fun onFilterClick() = Unit

	override fun onEmptyActionClick() = Unit

	override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
		mode.menuInflater.inflate(R.menu.mode_remote, menu)
		return true
	}

	override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
		mode.title = selectionDecoration.checkedItemsCount.toString()
		return true
	}

	override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
		return when (item.itemId) {
			R.id.action_share -> {
				ShareHelper(this).shareMangaLinks(collectSelectedItems())
				mode.finish()
				true
			}
			R.id.action_favourite -> {
				FavouriteCategoriesBottomSheet.show(supportFragmentManager, collectSelectedItems())
				mode.finish()
				true
			}
			R.id.action_save -> {
				DownloadService.confirmAndStart(this, collectSelectedItems())
				mode.finish()
				true
			}
			else -> false
		}
	}

	override fun onDestroyActionMode(mode: ActionMode) {
		selectionDecoration.clearSelection()
		invalidateItemDecorations()
		actionMode = null
	}

	private fun collectSelectedItems(): Set<Manga> {
		return viewModel.getItems(selectionDecoration.checkedItemsIds)
	}

	private fun invalidateItemDecorations() {
		binding.recyclerView.findViewsByType(RecyclerView::class.java).forEach {
			it.invalidateItemDecorations()
		}
	}

	companion object {

		private const val EXTRA_QUERY = "query"

		fun newIntent(context: Context, query: String) =
			Intent(context, MultiSearchActivity::class.java)
				.putExtra(EXTRA_QUERY, query)
	}
}