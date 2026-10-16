package org.thelazybattley.macrotrack.features.deletedata

interface DeleteDataCallbacks {

    fun onTyped(text: String)

    fun onDelete()

    companion object {
        fun default() = object : DeleteDataCallbacks {
            override fun onTyped(text: String) = Unit
            override fun onDelete() = Unit
        }
    }
}
