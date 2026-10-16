package org.thelazybattley.macrotrack.domain.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeleteDataStepTest {

    @Test
    fun theWordConfirmsWhateverItsCase() {
        assertTrue(isDeleteConfirmed(typed = "DELETE"))
        assertTrue(isDeleteConfirmed(typed = "delete"))
        assertTrue(isDeleteConfirmed(typed = "  Delete "))
    }

    @Test
    fun anythingElseDoesNot() {
        assertFalse(isDeleteConfirmed(typed = ""))
        assertFalse(isDeleteConfirmed(typed = "DELET"))
        assertFalse(isDeleteConfirmed(typed = "DELETE ALL"))
    }
}
