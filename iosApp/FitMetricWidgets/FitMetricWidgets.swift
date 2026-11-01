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
    var bigNumber: BigNumber = .calories
}

/// Today now, then the next day from midnight, so the widgets start the day fresh even if the app stays closed.
private func dayTimeline(bigNumber: BigNumber = .calories) -> Timeline<DayEntry> {
    let data = WidgetStore.load() ?? .sample
    let now = Date()
    let midnight = Calendar.current.startOfDay(for: now.addingTimeInterval(24 * 60 * 60))
    let entries = [now, midnight].map { date in
        DayEntry(date: date, day: data.day(at: date), isLockNumbersShown: data.isLockNumbersShown, bigNumber: bigNumber)
    }
    // After the next day starts there's nothing new to show until the app writes again, which reloads the widgets.
    return Timeline(entries: entries, policy: .never)
}

private func sampleEntry(bigNumber: BigNumber = .calories) -> DayEntry {
    DayEntry(date: Date(), day: WidgetData.sample.today, isLockNumbersShown: true, bigNumber: bigNumber)
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
        context.isPreview ? sampleEntry(bigNumber: configuration.bigNumber) : (dayTimeline(bigNumber: configuration.bigNumber).entries.first ?? sampleEntry())
    }

    func timeline(for configuration: TodayConfigurationIntent, in context: Context) async -> Timeline<DayEntry> {
        dayTimeline(bigNumber: configuration.bigNumber)
    }
}

struct TodayWidget: Widget {
    var body: some WidgetConfiguration {
        AppIntentConfiguration(kind: "Today", intent: TodayConfigurationIntent.self, provider: TodayProvider()) { entry in
            TodayView(entry: entry)
        }
        .configurationDisplayName("Today")
        .description("Calories and macros left, with buttons to log. The large size lists each meal.")
        .supportedFamilies([.systemSmall, .systemMedium, .systemLarge, .accessoryCircular, .accessoryRectangular, .accessoryInline])
    }
}

struct WaterWidget: Widget {
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: "Water", provider: DayProvider()) { entry in
            WaterView(day: entry.day)
        }
        .configurationDisplayName("Water")
        .description("How much you've had, and a button to add a glass.")
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
