package org.thelazybattley.macrotrack.domain.model

/**
 * Deleting all data, one group of tables at a time. Goals and the user's details go last, so a delete cut short
 * still leaves an app that opens normally, and running it again finishes the job.
 */
enum class DeleteDataStep { FOOD_LOG, BODY_RECORDS, SAVED_FOODS, GOALS_AND_SETTINGS }

/** The user typed the confirmation word; case and surrounding spaces don't matter. */
fun isDeleteConfirmed(typed: String): Boolean = typed.trim().equals(other = DELETE_CONFIRMATION_WORD, ignoreCase = true)

const val DELETE_CONFIRMATION_WORD = "DELETE"
