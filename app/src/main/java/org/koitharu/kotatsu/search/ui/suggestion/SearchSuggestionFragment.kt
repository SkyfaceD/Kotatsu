package org.koitharu.kotatsu.search.ui.suggestion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.Insets
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.ItemTouchHelper
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.base.ui.BaseFragment
import org.koitharu.kotatsu.databinding.FragmentSearchSuggestionBinding
import org.koitharu.kotatsu.main.ui.AppBarOwner
import org.koitharu.kotatsu.search.ui.suggestion.adapter.SearchSuggestionAdapter
import org.koitharu.kotatsu.utils.ext.measureHeight

class SearchSuggestionFragment :
	BaseFragment<FragmentSearchSuggestionBinding>(),
	SearchSuggestionItemCallback.SuggestionItemListener {

	private val viewModel by sharedViewModel<SearchSuggestionViewModel>()

	override fun onInflateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
	) = FragmentSearchSuggestionBinding.inflate(inflater, container, false)

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		val adapter = SearchSuggestionAdapter(
			coil = get(),
			lifecycleOwner = viewLifecycleOwner,
			listener = requireActivity() as SearchSuggestionListener,
		)
		binding.root.adapter = adapter
		viewModel.suggestion.observe(viewLifecycleOwner) {
			adapter.items = it
		}
		ItemTouchHelper(SearchSuggestionItemCallback(this))
			.attachToRecyclerView(binding.root)
	}

	override fun onWindowInsetsChanged(insets: Insets) {
		val headerHeight = (activity as? AppBarOwner)?.appBar?.measureHeight() ?: insets.top
		val extraPadding = resources.getDimensionPixelOffset(R.dimen.list_spacing)
		binding.root.updatePadding(
			top = headerHeight + extraPadding,
			bottom = insets.bottom + extraPadding,
		)
	}

	override fun onRemoveQuery(query: String) {
		viewModel.deleteQuery(query)
	}

	companion object {

		fun newInstance() = SearchSuggestionFragment()
	}
}