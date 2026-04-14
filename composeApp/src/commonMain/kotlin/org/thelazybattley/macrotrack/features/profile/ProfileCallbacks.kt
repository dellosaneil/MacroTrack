package org.thelazybattley.macrotrack.features.profile

interface ProfileCallbacks {

    fun onWeightInput(weight: String)

    fun onSaveWeight()

    companion object {
        fun default() = object : ProfileCallbacks {
            override fun onWeightInput(weight: String) {
                TODO("Not yet implemented")
            }

            override fun onSaveWeight() {
                TODO("Not yet implemented")
            }
        }
    }
}
