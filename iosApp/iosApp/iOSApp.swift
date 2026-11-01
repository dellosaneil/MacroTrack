import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate

    init() {
        TelemetrySetup.start()
        // Before launch finishes, so a tapped reminder that opens the app isn't missed.
        Notifications_nativeKt.installNotificationDelegate()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

/// Hands the app icon's quick actions and widget links to Kotlin, which opens their screens.
final class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        configurationForConnecting connectingSceneSession: UISceneSession,
        options: UIScene.ConnectionOptions
    ) -> UISceneConfiguration {
        // A quick action that launches the app comes with its scene; its screen waits until the app has started.
        if let shortcutItem = options.shortcutItem {
            _ = AppShortcuts_nativeKt.openAppShortcut(type: shortcutItem.type)
        }
        // Likewise a widget tapped while the app was closed.
        if let url = options.urlContexts.first?.url {
            _ = HomeWidgets_iosKt.openWidgetUrl(url: url.absoluteString)
        }
        let configuration = UISceneConfiguration(name: nil, sessionRole: connectingSceneSession.role)
        configuration.delegateClass = SceneDelegate.self
        return configuration
    }
}

/// A quick action or widget tapped while the app is already running.
final class SceneDelegate: NSObject, UIWindowSceneDelegate {
    func windowScene(
        _ windowScene: UIWindowScene,
        performActionFor shortcutItem: UIApplicationShortcutItem,
        completionHandler: @escaping (Bool) -> Void
    ) {
        completionHandler(AppShortcuts_nativeKt.openAppShortcut(type: shortcutItem.type))
    }

    func scene(_ scene: UIScene, openURLContexts URLContexts: Set<UIOpenURLContext>) {
        guard let url = URLContexts.first?.url else { return }
        _ = HomeWidgets_iosKt.openWidgetUrl(url: url.absoluteString)
    }

    /// Logs water added from a widget while the app was away, and brings the widgets up to date.
    func sceneDidBecomeActive(_ scene: UIScene) {
        HomeWidgets_iosKt.refreshHomeWidgets()
    }
}
