package org.thelazybattley.macrotrack.features.home

import org.thelazybattley.macrotrack.ui.navigation.AppDestinations

interface HomeTabCallbacks {

    fun onNavigation(destination: AppDestinations.Root)
    fun resetNavigation()

    companion object {
        fun default() = object : HomeTabCallbacks {
            override fun onNavigation(destination: AppDestinations.Root) {
                TODO("Not yet implemented")
            }

            override fun resetNavigation() {
                TODO("Not yet implemented")
            }
        }
    }
}
