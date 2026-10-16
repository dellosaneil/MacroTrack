import ComposeApp
import Foundation
#if canImport(FirebaseCore) && canImport(FirebaseAnalytics) && canImport(FirebaseCrashlytics)
import FirebaseAnalytics
import FirebaseCore
import FirebaseCrashlytics
#endif

/// Starts Firebase and hands Kotlin the bridge it sends crash reports and analytics through. Until the Firebase
/// package is added to the project and GoogleService-Info.plist is in the app, the bridge stays nil and both are off.
enum TelemetrySetup {
    private(set) static var bridge: FirebaseBridge?

    static func start() {
        #if canImport(FirebaseCore) && canImport(FirebaseAnalytics) && canImport(FirebaseCrashlytics)
        guard Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist") != nil else { return }
        if FirebaseApp.app() == nil { FirebaseApp.configure() }
        #if DEBUG
        // Debug builds never send analytics, even before Kotlin applies the in-app choice. Crash reports still go.
        Analytics.setAnalyticsCollectionEnabled(false)
        #endif
        bridge = FirebaseTelemetryBridge()
        #endif
    }
}

#if canImport(FirebaseCore) && canImport(FirebaseAnalytics) && canImport(FirebaseCrashlytics)
private final class FirebaseTelemetryBridge: FirebaseBridge {
    func logEvent(name: String, params: [String: Any]) {
        Analytics.logEvent(name, parameters: params.isEmpty ? nil : params)
    }

    func recordError(name: String, reason: String, stackTrace: [String]) {
        let model = ExceptionModel(name: name, reason: reason)
        model.stackTrace = stackTrace.map { StackFrame(symbol: $0, file: "", line: 0) }
        Crashlytics.crashlytics().record(exceptionModel: model)
    }

    func log(message: String) {
        Crashlytics.crashlytics().log(message)
    }

    func setCollectionEnabled(crashReports: Bool, analytics: Bool) {
        Analytics.setAnalyticsCollectionEnabled(analytics)
        Crashlytics.crashlytics().setCrashlyticsCollectionEnabled(crashReports)
    }
}
#endif
