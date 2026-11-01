import ComposeApp
import Foundation
import WidgetKit

/// Hands Kotlin the bridge to the widgets' App Group. Without the App Group entitlement the bridge stays nil and
/// Profile leaves out the widgets screen.
enum HomeWidgetSetup {
    /// Must match WidgetStore in the widget extension.
    private static let appGroup = "group.org.thelazybattley.macrotrack"
    private static let dataKey = "widget_data"
    private static let pendingWaterKey = "pending_water"

    static let bridge: WidgetBridge? = {
        guard FileManager.default.containerURL(forSecurityApplicationGroupIdentifier: appGroup) != nil,
              let defaults = UserDefaults(suiteName: appGroup) else { return nil }
        return AppGroupWidgetBridge(defaults: defaults)
    }()

    private final class AppGroupWidgetBridge: WidgetBridge {
        private let defaults: UserDefaults

        init(defaults: UserDefaults) {
            self.defaults = defaults
        }

        func save(json: String) {
            defaults.set(json, forKey: dataKey)
            WidgetCenter.shared.reloadAllTimelines()
        }

        func takePendingWater() -> String {
            let pending = defaults.string(forKey: pendingWaterKey) ?? "[]"
            defaults.removeObject(forKey: pendingWaterKey)
            return pending
        }
    }
}
