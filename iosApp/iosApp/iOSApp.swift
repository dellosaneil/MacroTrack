import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
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