package org.thelazybattley.macrotrack.core.platform

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.checklist_action_done
import macrotrack.composeapp.generated.resources.checklist_action_skip
import macrotrack.composeapp.generated.resources.checklist_action_snooze
import org.jetbrains.compose.resources.getString
import org.koin.mp.KoinPlatform
import org.thelazybattley.macrotrack.domain.usecase.checklist.HandleChecklistReminderActionUseCase
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import platform.Foundation.NSDateComponents
import platform.Foundation.NSUserDefaults
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationAction
import platform.UserNotifications.UNNotificationActionOptionNone
import platform.UserNotifications.UNNotificationCategory
import platform.UserNotifications.UNNotificationCategoryOptionNone
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter

private const val ID_PREFIX = "checklist-reminder-"
private const val CATEGORY_ID = "checklist"
private const val ITEM_ID_KEY = "checklistItemId"
private const val DATE_KEY = "checklistDate"
private const val PENDING_ACTIONS_KEY = "checklist_pending_actions"

/** iOS caps pending notifications at 64, shared with the other reminders. */
private const val MAX_REMINDERS = 32

/**
 * iOS can't check what's ticked when a reminder is due, so each is written ahead and they're all set again as
 * items are ticked while the app is open: ticking one takes back its reminder.
 */
class IosChecklistReminderScheduler : ChecklistReminderScheduler {

    override val days: Int = 2

    override suspend fun schedule(upcoming: List<ChecklistReminder>) {
        UNUserNotificationCenter.currentNotificationCenter().setNotificationCategories(categories = setOf(category()))
        val requests = upcoming.take(n = MAX_REMINDERS).map { reminder ->
            val content = UNMutableNotificationContent().apply {
                setTitle(reminder.message.title)
                setBody(reminder.message.body)
                setSound(UNNotificationSound.defaultSound)
                setCategoryIdentifier(CATEGORY_ID)
                setUserInfo(
                    mapOf(
                        NOTIFICATION_ROUTE_KEY to AppDestinations.Root.Checklist.route,
                        ITEM_ID_KEY to reminder.itemId.toString(),
                        DATE_KEY to reminder.date.toString()
                    )
                )
            }
            val components = NSDateComponents().apply {
                year = reminder.at.year.toLong()
                month = reminder.at.month.ordinal + 1L
                day = reminder.at.day.toLong()
                hour = reminder.at.hour.toLong()
                minute = reminder.at.minute.toLong()
            }
            UNNotificationRequest.requestWithIdentifier(
                identifier = "$ID_PREFIX${reminder.itemId}-${reminder.at}",
                content = content,
                trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(dateComponents = components, repeats = false)
            )
        }
        replacePendingNotifications(prefix = ID_PREFIX, requests = requests)
    }

    private suspend fun category(): UNNotificationCategory = UNNotificationCategory.categoryWithIdentifier(
        identifier = CATEGORY_ID,
        actions = listOf(
            ChecklistReminderAction.DONE to Res.string.checklist_action_done,
            ChecklistReminderAction.SNOOZE to Res.string.checklist_action_snooze,
            ChecklistReminderAction.SKIP to Res.string.checklist_action_skip
        ).map { (action, label) ->
            UNNotificationAction.actionWithIdentifier(identifier = action.name, title = getString(resource = label), options = UNNotificationActionOptionNone)
        },
        intentIdentifiers = emptyList<String>(),
        options = UNNotificationCategoryOptionNone
    )
}

/** A reminder button pressed while the app wasn't running, kept until it starts. */
@Serializable
private data class PendingChecklistAction(val itemId: Long, val date: String, val action: String)

private val json = Json { ignoreUnknownKeys = true }

/**
 * Handles a button on a checklist reminder, returning false when [actionIdentifier] isn't one. Done or skipped
 * is saved straight away when the app is running in the background; otherwise it waits for the app to start
 * (see [deliverPendingChecklistActions]). [onFinished] is called once iOS can suspend the app again.
 */
internal fun handleChecklistAction(actionIdentifier: String, userInfo: Map<Any?, *>, onFinished: () -> Unit): Boolean {
    val action = ChecklistReminderAction.entries.find { it.name == actionIdentifier } ?: return false
    val itemId = (userInfo[ITEM_ID_KEY] as? String)?.toLongOrNull()
    val date = userInfo[DATE_KEY] as? String
    if (itemId == null || date == null) {
        onFinished()
        return true
    }
    val handle = KoinPlatform.getKoinOrNull()?.getOrNull<HandleChecklistReminderActionUseCase>()
    if (handle == null) {
        savePending(actions = loadPending() + PendingChecklistAction(itemId = itemId, date = date, action = action.name))
        onFinished()
        return true
    }
    MainScope().launch {
        try {
            handle(itemId = itemId, date = LocalDate.parse(input = date), action = action)
        } finally {
            onFinished()
        }
    }
    return true
}

/** Saves the reminder buttons pressed while the app was closed, once it has started. */
fun deliverPendingChecklistActions() {
    val pending = loadPending()
    if (pending.isEmpty()) return
    val handle = KoinPlatform.getKoinOrNull()?.getOrNull<HandleChecklistReminderActionUseCase>() ?: return
    savePending(actions = emptyList())
    MainScope().launch {
        pending.forEach { action ->
            runCatching {
                handle(itemId = action.itemId, date = LocalDate.parse(input = action.date), action = ChecklistReminderAction.valueOf(value = action.action))
            }
        }
    }
}

private fun loadPending(): List<PendingChecklistAction> =
    NSUserDefaults.standardUserDefaults.stringForKey(defaultName = PENDING_ACTIONS_KEY)
        ?.let { runCatching { json.decodeFromString<List<PendingChecklistAction>>(string = it) }.getOrNull() }
        .orEmpty()

private fun savePending(actions: List<PendingChecklistAction>) {
    NSUserDefaults.standardUserDefaults.setObject(value = json.encodeToString(value = actions), forKey = PENDING_ACTIONS_KEY)
}
