package org.koitharu.kotatsu.settings.backup

import android.content.Context
import androidx.lifecycle.MutableLiveData
import org.koitharu.kotatsu.base.ui.BaseViewModel
import org.koitharu.kotatsu.core.backup.BackupRepository
import org.koitharu.kotatsu.core.backup.BackupZipOutput
import org.koitharu.kotatsu.utils.SingleLiveEvent
import org.koitharu.kotatsu.utils.progress.Progress
import java.io.File

class BackupViewModel(
	private val repository: BackupRepository,
	context: Context
) : BaseViewModel() {

	val progress = MutableLiveData<Progress?>(null)
	val onBackupDone = SingleLiveEvent<File>()

	init {
		launchLoadingJob {
			val file = BackupZipOutput(context).use { backup ->
				backup.put(repository.createIndex())

				progress.value = Progress(0, 3)
				backup.put(repository.dumpHistory())

				progress.value = Progress(1, 3)
				backup.put(repository.dumpCategories())

				progress.value = Progress(2, 3)
				backup.put(repository.dumpFavourites())

				progress.value = Progress(3, 3)
				backup.finish()
				progress.value = null
				backup.close()
				backup.file
			}
			onBackupDone.call(file)
		}
	}
}