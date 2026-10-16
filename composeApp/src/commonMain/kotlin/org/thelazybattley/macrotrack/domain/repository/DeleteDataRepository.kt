package org.thelazybattley.macrotrack.domain.repository

import org.thelazybattley.macrotrack.domain.model.DeleteDataStep

interface DeleteDataRepository {

    suspend fun delete(step: DeleteDataStep)
}
