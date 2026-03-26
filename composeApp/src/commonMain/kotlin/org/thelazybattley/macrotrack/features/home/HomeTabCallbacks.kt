package org.thelazybattley.macrotrack.features.home

import org.thelazybattley.macrotrack.ui.navigation.MacroTrackDestination

interface HomeTabCallbacks {

    fun onNavigation(destination: MacroTrackDestination)
    fun resetNavigation()

    companion object {
        fun default() = object : HomeTabCallbacks {
            override fun onNavigation(destination: MacroTrackDestination) {
                TODO("Not yet implemented")
            }

            override fun resetNavigation() {
                TODO("Not yet implemented")
            }
        }
    }
}
