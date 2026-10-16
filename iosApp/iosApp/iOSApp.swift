import SwiftUI

@main
struct iOSApp: App {
    init() {
        TelemetrySetup.start()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}