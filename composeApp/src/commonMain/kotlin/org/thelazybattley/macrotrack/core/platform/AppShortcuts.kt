package org.thelazybattley.macrotrack.core.platform

import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.shortcut_add_water
import macrotrack.composeapp.generated.resources.shortcut_log_food
import macrotrack.composeapp.generated.resources.shortcut_log_weight
import macrotrack.composeapp.generated.resources.shortcut_quick_add
import macrotrack.composeapp.generated.resources.shortcut_scan_barcode
import org.jetbrains.compose.resources.StringResource

/** The actions on a long press of the app icon, in the order they show. iOS shows only the first four. */
enum class AppShortcut(val title: StringResource) {
    SCAN_BARCODE(title = Res.string.shortcut_scan_barcode),
    LOG_FOOD(title = Res.string.shortcut_log_food),
    QUICK_ADD(title = Res.string.shortcut_quick_add),
    ADD_WATER(title = Res.string.shortcut_add_water),
    LOG_WEIGHT(title = Res.string.shortcut_log_weight);

    companion object {
        /** The shortcut a launch names by its id, or null when it names none. */
        fun of(id: String?): AppShortcut? = entries.find { it.name == id }
    }
}

/** Sets the app icon's shortcuts on the phone, replacing any set before. */
interface AppShortcutPublisher {

    /** [titles] is every shortcut in order, with its title in the phone's language. */
    fun publish(titles: Map<AppShortcut, String>)
}
