import SwiftUI
import WidgetKit

@main
struct FitMetricWidgets: WidgetBundle {
    var body: some Widget {
        TodayWidget()
        WaterWidget()
        WeightWidget()
        StreakWidget()
    }
}

struct DayEntry: TimelineEntry {
    let date: Date
    let day: WidgetDay
    let isLockNumbersShown: Bool
    var setup = WidgetSetup()
    /// The water button's last tap while it's still showing.
    var waterTap: WaterTap?
}

/// What a widget was set up to show, from Edit Widget.
struct WidgetSetup {
    var bigNumber: BigNumber = .calories
    var details: [WidgetDetailKind] = [.protein, .carbs, .fat]
    var buttons: [WidgetButtonKind] = [.food, .water, .scan]
    var waterMl: Int = 250
}

extension WidgetSetup {
    init(_ intent: TodayConfigurationIntent) {
        self.init(bigNumber: intent.bigNumber, details: Array(intent.details.uniqued().prefix(3)), buttons: intent.buttons.uniqued(), waterMl: intent.waterAmount.ml)
    }

    init(_ intent: WaterConfigurationIntent) {
        self.init(waterMl: intent.waterAmount.ml)
    }
}

private extension Array where Element: Hashable {
    /// Each one once, kept where it was first picked.
    func uniqued() -> [Element] {
        var seen = Set<Element>()
        return filter { seen.insert($0).inserted }
    }
}

/// Today now, then the next day from midnight, so the widgets start the day fresh even if the app stays closed.
/// Just after the water button is tapped, it shows the tap until it ends, then goes back to how it was.
private func dayTimeline(setup: WidgetSetup = WidgetSetup()) -> Timeline<DayEntry> {
    let data = WidgetStore.load() ?? .sample
    let now = Date()
    let midnight = Calendar.current.startOfDay(for: now.addingTimeInterval(24 * 60 * 60))
    let tap = WidgetStore.waterTap(at: now)
    var dates = [now, midnight]
    if let tap, tap.until < midnight { dates.insert(tap.until, at: 1) }
    let entries = dates.map { date in
        DayEntry(
            date: date,
            day: data.day(at: date),
            isLockNumbersShown: data.isLockNumbersShown,
            setup: setup,
            waterTap: date == now ? tap : nil
        )
    }
    // After the next day starts there's nothing new to show until the app writes again, which reloads the widgets.
    return Timeline(entries: entries, policy: .never)
}

private func sampleEntry(setup: WidgetSetup = WidgetSetup()) -> DayEntry {
    DayEntry(date: Date(), day: WidgetData.sample.today, isLockNumbersShown: true, setup: setup)
}

struct DayProvider: TimelineProvider {
    func placeholder(in context: Context) -> DayEntry { sampleEntry() }

    func getSnapshot(in context: Context, completion: @escaping (DayEntry) -> Void) {
        completion(context.isPreview ? sampleEntry() : (dayTimeline().entries.first ?? sampleEntry()))
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<DayEntry>) -> Void) {
        completion(dayTimeline())
    }
}

struct TodayProvider: AppIntentTimelineProvider {
    func placeholder(in context: Context) -> DayEntry { sampleEntry() }

    func snapshot(for configuration: TodayConfigurationIntent, in context: Context) async -> DayEntry {
        let setup = WidgetSetup(configuration)
        return context.isPreview ? sampleEntry(setup: setup) : (dayTimeline(setup: setup).entries.first ?? sampleEntry(setup: setup))
    }

    func timeline(for configuration: TodayConfigurationIntent, in context: Context) async -> Timeline<DayEntry> {
        dayTimeline(setup: WidgetSetup(configuration))
    }
}

struct WaterProvider: AppIntentTimelineProvider {
    func placeholder(in context: Context) -> DayEntry { sampleEntry() }

    func snapshot(for configuration: WaterConfigurationIntent, in context: Context) async -> DayEntry {
        let setup = WidgetSetup(configuration)
        return context.isPreview ? sampleEntry(setup: setup) : (dayTimeline(setup: setup).entries.first ?? sampleEntry(setup: setup))
    }

    func timeline(for configuration: WaterConfigurationIntent, in context: Context) async -> Timeline<DayEntry> {
        dayTimeline(setup: WidgetSetup(configuration))
    }
}

struct TodayWidget: Widget {
    var body: some WidgetConfiguration {
        AppIntentConfiguration(kind: "Today", intent: TodayConfigurationIntent.self, provider: TodayProvider()) { entry in
            TodayView(entry: entry)
        }
        .configurationDisplayName("Today")
        .description("Calories left, the details you choose, and buttons to log. The large size lists each meal.")
        .supportedFamilies([.systemSmall, .systemMedium, .systemLarge, .accessoryCircular, .accessoryRectangular, .accessoryInline])
    }
}

struct WaterWidget: Widget {
    var body: some WidgetConfiguration {
        AppIntentConfiguration(kind: "Water", intent: WaterConfigurationIntent.self, provider: WaterProvider()) { entry in
            WaterView(entry: entry)
        }
        .configurationDisplayName("Water")
        .description("How much you've had, and a button to add water.")
        .supportedFamilies([.systemSmall])
    }
}

struct WeightWidget: Widget {
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: "Weight", provider: DayProvider()) { entry in
            WeightView(day: entry.day)
        }
        .configurationDisplayName("Weight")
        .description("Your trend weight and how it moved this week.")
        .supportedFamilies([.systemSmall])
    }
}

struct StreakWidget: Widget {
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: "Streak", provider: DayProvider()) { entry in
            StreakView(day: entry.day)
        }
        .configurationDisplayName("Streak")
        .description("Days in a row with food logged.")
        .supportedFamilies([.systemSmall])
    }
}

#Preview(as: .systemMedium) {
    TodayWidget()
} timeline: {
    sampleEntry()
}

#Preview(as: .systemLarge) {
    TodayWidget()
} timeline: {
    sampleEntry()
}

#Preview(as: .accessoryRectangular) {
    TodayWidget()
} timeline: {
    sampleEntry()
}
