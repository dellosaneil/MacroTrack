import AppIntents
import WidgetKit

/// The water button: adds water without opening the app, and the widgets show it was added, with Undo, for a
/// few seconds. The app logs it next time it opens.
struct AddWaterIntent: AppIntent {
    static var title: LocalizedStringResource = "Add water"
    static var description = IntentDescription("Adds water to today.")

    @Parameter(title: "Amount in ml")
    var ml: Int

    init() {}

    init(ml: Int) {
        self.ml = ml
    }

    func perform() async throws -> some IntentResult {
        let added = WidgetStore.addPendingWater(ml: ml)
        WidgetStore.setWaterTap(WaterTap(ml: ml, atMillis: added.atMillis, isUndone: false, until: Date().addingTimeInterval(WaterTap.addedSeconds)))
        WidgetCenter.shared.reloadAllTimelines()
        return .result()
    }
}

/// Undo next to Added: takes back the water just added, and the button says so for a moment.
struct UndoWaterIntent: AppIntent {
    static var title: LocalizedStringResource = "Undo water"
    static var description = IntentDescription("Takes back the water just added from a widget.")

    func perform() async throws -> some IntentResult {
        if let tap = WidgetStore.waterTap(), !tap.isUndone {
            if WidgetStore.removePendingWater(atMillis: tap.atMillis) {
                WidgetStore.setWaterTap(WaterTap(ml: tap.ml, atMillis: tap.atMillis, isUndone: true, until: Date().addingTimeInterval(WaterTap.undoneSeconds)))
            } else {
                // The app has logged it already; it's taken back there.
                WidgetStore.setWaterTap(nil)
            }
            WidgetCenter.shared.reloadAllTimelines()
        }
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

/// A detail next to the Today ring. [id] matches the name Kotlin writes it under.
enum WidgetDetailKind: String, AppEnum {
    case protein
    case carbs
    case fat
    case water
    case eaten
    case weight
    case streak

    var id: String { rawValue.uppercased() }

    static var typeDisplayRepresentation: TypeDisplayRepresentation = "Detail"
    static var caseDisplayRepresentations: [WidgetDetailKind: DisplayRepresentation] = [
        .protein: DisplayRepresentation(title: "Protein", subtitle: "Eaten of your goal", image: .init(systemName: "p.circle")),
        .carbs: DisplayRepresentation(title: "Carbs", subtitle: "Eaten of your goal", image: .init(systemName: "leaf")),
        .fat: DisplayRepresentation(title: "Fat", subtitle: "Eaten of your goal", image: .init(systemName: "drop")),
        .water: DisplayRepresentation(title: "Water", subtitle: "Drunk of your goal", image: .init(systemName: "drop.fill")),
        .eaten: DisplayRepresentation(title: "Calories eaten", subtitle: "Eaten of your goal", image: .init(systemName: "chart.bar")),
        .weight: DisplayRepresentation(title: "Weight", subtitle: "Trend and this week's change", image: .init(systemName: "scalemass")),
        .streak: DisplayRepresentation(title: "Streak", subtitle: "Days in a row", image: .init(systemName: "flame"))
    ]
}

/// A button on the Today widget. Add water works right from the widget; the others open the app.
enum WidgetButtonKind: String, AppEnum {
    case food
    case scan
    case water
    case weight

    static var typeDisplayRepresentation: TypeDisplayRepresentation = "Button"
    static var caseDisplayRepresentations: [WidgetButtonKind: DisplayRepresentation] = [
        .food: DisplayRepresentation(title: "Add food", subtitle: "Opens Add meal", image: .init(systemName: "plus")),
        .scan: DisplayRepresentation(title: "Scan barcode", subtitle: "Opens the camera", image: .init(systemName: "barcode")),
        .water: DisplayRepresentation(title: "Add water", subtitle: "Logs it right from the widget", image: .init(systemName: "drop.fill")),
        .weight: DisplayRepresentation(title: "Log weight", subtitle: "Opens Log weight", image: .init(systemName: "scalemass"))
    ]
}

/// How much the water button adds.
enum WaterAmount: String, AppEnum {
    case ml150 = "150"
    case ml250 = "250"
    case ml330 = "330"
    case ml500 = "500"

    var ml: Int { Int(rawValue) ?? 250 }

    static var typeDisplayRepresentation: TypeDisplayRepresentation = "Water button adds"
    static var caseDisplayRepresentations: [WaterAmount: DisplayRepresentation] = [
        .ml150: "150 ml",
        .ml250: "250 ml",
        .ml330: "330 ml",
        .ml500: "500 ml"
    ]
}

/// Touch and hold › Edit Widget on the Today widget: the big number, then the details and buttons of the
/// medium and large sizes, shown in the order picked.
struct TodayConfigurationIntent: WidgetConfigurationIntent {
    static var title: LocalizedStringResource = "Today"
    static var description = IntentDescription("Choose what the ring, details and buttons show.")

    @Parameter(title: "Big number in the ring", default: .calories)
    var bigNumber: BigNumber

    // iOS caps lists by size only for entities, so the widget shows the first 3 picked.
    @Parameter(
        title: "Details",
        description: "Next to the ring, in the order you pick them. The first 3 show.",
        default: [.protein, .carbs, .fat]
    )
    var details: [WidgetDetailKind]

    // Water before Scan, so the medium size's two are Food and Water until changed.
    @Parameter(
        title: "Buttons",
        description: "The first 2 show on the medium size, 3 on the large. Add water works without opening the app; the others open FitMetric.",
        default: [.food, .water, .scan]
    )
    var buttons: [WidgetButtonKind]

    @Parameter(title: "Water button adds", default: .ml250)
    var waterAmount: WaterAmount

    static var parameterSummary: some ParameterSummary {
        When(widgetFamily: .oneOf, [.systemMedium, .systemLarge]) {
            Summary {
                \.$bigNumber
                \.$details
                \.$buttons
                \.$waterAmount
            }
        } otherwise: {
            Summary {
                \.$bigNumber
            }
        }
    }
}

/// Touch and hold › Edit Widget on the Water widget.
struct WaterConfigurationIntent: WidgetConfigurationIntent {
    static var title: LocalizedStringResource = "Water"
    static var description = IntentDescription("Choose how much the button adds.")

    @Parameter(title: "Water button adds", default: .ml250)
    var waterAmount: WaterAmount
}
