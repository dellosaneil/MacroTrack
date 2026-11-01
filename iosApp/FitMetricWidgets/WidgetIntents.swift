import AppIntents
import WidgetKit

/// The water button: adds a glass without opening the app. The app logs it next time it opens.
struct AddWaterIntent: AppIntent {
    static var title: LocalizedStringResource = "Add a glass of water"
    static var description = IntentDescription("Adds a glass of water to today.")

    func perform() async throws -> some IntentResult {
        let glassMl = WidgetStore.load()?.day(at: Date()).glassMl ?? 250
        WidgetStore.addPendingWater(ml: glassMl)
        return .result()
    }
}

enum BigNumber: String, AppEnum {
    case calories
    case protein

    static var typeDisplayRepresentation: TypeDisplayRepresentation = "Big number"
    static var caseDisplayRepresentations: [BigNumber: DisplayRepresentation] = [
        .calories: DisplayRepresentation(title: "Calories left", subtitle: "What's left of today's calories"),
        .protein: DisplayRepresentation(title: "Protein left", subtitle: "What's left of today's protein")
    ]
}

/// Touch and hold › Edit Widget on the Today widget.
struct TodayConfigurationIntent: WidgetConfigurationIntent {
    static var title: LocalizedStringResource = "Today"
    static var description = IntentDescription("Choose what the ring shows.")

    @Parameter(title: "Big number in the ring", default: .calories)
    var bigNumber: BigNumber
}
