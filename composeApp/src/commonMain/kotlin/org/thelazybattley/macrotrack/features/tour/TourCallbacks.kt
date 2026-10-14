package org.thelazybattley.macrotrack.features.tour

interface TourCallbacks {

    fun onNextClicked()

    fun onBackClicked()

    fun onSkipClicked()

    fun onReplayClicked()

    fun onOutcomeDismissed()

    companion object {
        fun default() = run {
            object : TourCallbacks {
                override fun onNextClicked() {
                    TODO("Not yet implemented")
                }

                override fun onBackClicked() {
                    TODO("Not yet implemented")
                }

                override fun onSkipClicked() {
                    TODO("Not yet implemented")
                }

                override fun onReplayClicked() {
                    TODO("Not yet implemented")
                }

                override fun onOutcomeDismissed() {
                    TODO("Not yet implemented")
                }
            }
        }
    }
}
