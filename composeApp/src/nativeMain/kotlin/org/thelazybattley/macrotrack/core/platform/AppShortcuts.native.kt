package org.thelazybattley.macrotrack.core.platform

import org.thelazybattley.macrotrack.features.shortcuts.appShortcutRoute
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationShortcutIcon
import platform.UIKit.UIApplicationShortcutItem
import platform.UIKit.shortcutItems

/** Dynamic quick actions, so their titles come from the app's own strings; they show from the second launch on. */
class IosAppShortcutPublisher : AppShortcutPublisher {

    override fun publish(titles: Map<AppShortcut, String>) {
        UIApplication.sharedApplication.shortcutItems = titles.map { (shortcut, title) ->
            UIApplicationShortcutItem(
                type = shortcut.name,
                localizedTitle = title,
                localizedSubtitle = null,
                icon = UIApplicationShortcutIcon.iconWithSystemImageName(systemImageName = shortcut.symbol),
                userInfo = null
            )
        }
    }
}

/**
 * A quick action tapped on the app icon, by its [type]: opens its screen, worked out for the time it was tapped.
 * Returns false for a type the app doesn't know.
 */
fun openAppShortcut(type: String): Boolean {
    val shortcut = AppShortcut.of(id = type) ?: return false
    requestRoute(route = appShortcutRoute(shortcut = shortcut))
    return true
}

/** The SF Symbol beside each quick action. */
private val AppShortcut.symbol: String
    get() = when (this) {
        AppShortcut.SCAN_BARCODE -> "barcode.viewfinder"
        AppShortcut.LOG_FOOD -> "magnifyingglass"
        AppShortcut.QUICK_ADD -> "bolt.fill"
        AppShortcut.ADD_WATER -> "drop.fill"
        AppShortcut.LOG_WEIGHT -> "scalemass.fill"
    }
