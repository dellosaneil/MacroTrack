package org.thelazybattley.macrotrack.features.pro

interface ProCallbacks {

    fun onUnlockClicked()

    fun onRestoreClicked()

    fun onTrialEndedNoticeDismissed()

    fun onMessageShown()

    companion object {
        fun default() = object : ProCallbacks {
            override fun onUnlockClicked() {
            }

            override fun onRestoreClicked() {
            }

            override fun onTrialEndedNoticeDismissed() {
            }

            override fun onMessageShown() {
            }
        }
    }
}
