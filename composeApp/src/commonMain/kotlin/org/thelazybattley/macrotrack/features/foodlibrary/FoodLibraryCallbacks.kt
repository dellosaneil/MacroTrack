package org.thelazybattley.macrotrack.features.foodlibrary

import org.thelazybattley.macrotrack.domain.model.LibraryCategory

interface FoodLibraryCallbacks {
    fun onQueryChanged(query: String)
    fun onCategorySelected(category: LibraryCategory?)
    fun onToggleFood(name: String)
    fun onOpenFood(name: String)
    fun onCloseFood()
    fun onAddOpenFood()
    fun onMakeOwnVersion()
    fun onCreateFood()
    fun onScan()
    fun onAddSelection()
    fun resetNavigateScreen()

    companion object {
        fun default() = object : FoodLibraryCallbacks {
            override fun onQueryChanged(query: String) {}
            override fun onCategorySelected(category: LibraryCategory?) {}
            override fun onToggleFood(name: String) {}
            override fun onOpenFood(name: String) {}
            override fun onCloseFood() {}
            override fun onAddOpenFood() {}
            override fun onMakeOwnVersion() {}
            override fun onCreateFood() {}
            override fun onScan() {}
            override fun onAddSelection() {}
            override fun resetNavigateScreen() {}
        }
    }
}
