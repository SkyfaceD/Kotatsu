package org.koitharu.kotatsu.list.ui.model

import org.koitharu.kotatsu.parsers.model.Manga

data class MangaListDetailedModel(
	override val id: Long,
	val title: String,
	val subtitle: String?,
	val tags: String,
	val coverUrl: String,
	val rating: String?,
	override val manga: Manga,
	val counter: Int,
) : MangaItemModel