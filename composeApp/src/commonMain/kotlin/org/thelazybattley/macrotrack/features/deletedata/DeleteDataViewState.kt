package org.thelazybattley.macrotrack.features.deletedata

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.model.DeleteDataStep
import org.thelazybattley.macrotrack.domain.model.isDeleteConfirmed

enum class DeleteDataStage { FORM, DELETING, DONE }

/** How much of each kind of data is about to go, for the "What will be deleted" list. */
data class DeleteDataSummary(
    val logCount: Int = 0,
    val firstLogDate: LocalDate? = null,
    val weighInCount: Int = 0,
    val measurementCount: Int = 0,
    val foodCount: Int = 0,
    val recipeCount: Int = 0,
    val goalChangeCount: Int = 0
)

data class DeleteDataViewState(
    val summary: DeleteDataSummary = DeleteDataSummary(),
    val typed: String = "",
    val stage: DeleteDataStage = DeleteDataStage.FORM,
    /** The step being deleted; the ones before it are done. */
    val currentStep: DeleteDataStep? = null,
    /** The last attempt stopped partway; deleting again finishes it. */
    val hasFailed: Boolean = false
) {
    val canDelete: Boolean get() = isDeleteConfirmed(typed = typed)
}
