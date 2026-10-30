package org.thelazybattley.macrotrack.core.platform

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import org.thelazybattley.macrotrack.MainActivity
import org.thelazybattley.macrotrack.R

/** The extra on a shortcut's launch naming the [AppShortcut] tapped. */
const val EXTRA_APP_SHORTCUT = "app_shortcut"

/** Dynamic shortcuts, so their titles come from the app's own strings like everything else. */
class AndroidAppShortcutPublisher(private val context: Context) : AppShortcutPublisher {

    override fun publish(titles: Map<AppShortcut, String>) {
        val shortcuts = titles.entries
            .take(n = ShortcutManagerCompat.getMaxShortcutCountPerActivity(context))
            .mapIndexed { rank, (shortcut, title) ->
                ShortcutInfoCompat.Builder(context, shortcut.name)
                    .setShortLabel(title)
                    .setIcon(IconCompat.createWithResource(context, shortcut.icon))
                    .setIntent(
                        Intent(context, MainActivity::class.java)
                            .setAction(Intent.ACTION_VIEW)
                            .putExtra(EXTRA_APP_SHORTCUT, shortcut.name)
                    )
                    .setRank(rank)
                    .build()
            }
        ShortcutManagerCompat.setDynamicShortcuts(context, shortcuts)
    }
}

private val AppShortcut.icon: Int
    get() = when (this) {
        AppShortcut.SCAN_BARCODE -> R.drawable.ic_shortcut_scan_barcode
        AppShortcut.LOG_FOOD -> R.drawable.ic_shortcut_log_food
        AppShortcut.QUICK_ADD -> R.drawable.ic_shortcut_quick_add
        AppShortcut.ADD_WATER -> R.drawable.ic_shortcut_add_water
        AppShortcut.LOG_WEIGHT -> R.drawable.ic_shortcut_log_weight
    }
