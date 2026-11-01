import Foundation
import WidgetKit

/// The App Group the app and its widgets share. The app writes what to show; the widgets read it, and leave
/// glasses of water added from the water button for the app to log when it next opens.
enum WidgetStore {
    static let appGroup = "group.org.thelazybattley.macrotrack"
    private static let dataKey = "widget_data"
    private static let pendingWaterKey = "pending_water"

    private static var defaults: UserDefaults? { UserDefaults(suiteName: appGroup) }

    static func load() -> WidgetData? {
        guard let json = defaults?.string(forKey: dataKey), let data = json.data(using: .utf8) else { return nil }
        return try? JSONDecoder().decode(WidgetData.self, from: data)
    }

    static func pendingWater() -> [PendingWater] {
        guard let json = defaults?.string(forKey: pendingWaterKey), let data = json.data(using: .utf8) else { return [] }
        return (try? JSONDecoder().decode([PendingWater].self, from: data)) ?? []
    }

    static func addPendingWater(ml: Int) {
        let glass = PendingWater(ml: ml, atMillis: Int64(Date().timeIntervalSince1970 * 1000))
        guard let data = try? JSONEncoder().encode(pendingWater() + [glass]) else { return }
        defaults?.set(String(decoding: data, as: UTF8.self), forKey: pendingWaterKey)
    }
}

/// A glass added from a widget, as the app's Kotlin side reads it.
struct PendingWater: Codable {
    let ml: Int
    let atMillis: Int64

    var date: Date { Date(timeIntervalSince1970: TimeInterval(atMillis) / 1000) }
}

/// Written by the app's Kotlin side, IosWidgetData. Every word is already in the user's language and units.
struct WidgetData: Codable {
    let today: WidgetDay
    let next: WidgetDay
    let isLockNumbersShown: Bool

    /// Today's widgets, or the next day's once midnight has passed and the app hasn't written a new day yet.
    func day(at date: Date) -> WidgetDay {
        let key = WidgetDay.key(for: date)
        return next.date == key ? next : today
    }
}

struct WidgetMacro: Codable, Hashable {
    let name: String
    let short: String
    let value: String
    let left: String
    let progress: Double
}

struct WidgetMeal: Codable, Hashable {
    let url: String
    let name: String
    let detail: String
    let calories: String?
}

struct WidgetDay: Codable {
    let date: String
    let isSetUp: Bool
    let title: String
    let dateText: String
    let setUp: String
    let caloriesNumber: String
    let caloriesLabel: String
    let caloriesProgress: Double
    let proteinNumber: String
    let proteinLabel: String
    let proteinProgress: Double
    let proteinLeft: String
    let eaten: String
    let macros: [WidgetMacro]
    let meals: [WidgetMeal]
    let food: String
    let scan: String
    let addWater: String
    let waterTitle: String
    let waterMl: Int
    let waterGoalMl: Int
    let glassMl: Int
    let isImperial: Bool
    let waterOf: String
    let weightTitle: String
    let hasWeight: Bool
    let weightNumber: String
    let weightChange: String
    let weightCaption: String
    let weightEmpty: String
    let weightLine: [Double]
    let streakTitle: String
    let streakDays: String
    let streakUnit: String
    let streakNote: String
    let streakLetters: [String]
    let streakStates: [String]

    /// "2026-10-08", as Kotlin writes dates.
    static func key(for date: Date) -> String {
        let parts = Calendar.current.dateComponents([.year, .month, .day], from: date)
        return String(format: "%04d-%02d-%02d", parts.year ?? 0, parts.month ?? 0, parts.day ?? 0)
    }

    /// Water drunk this day, with glasses added from a widget that the app hasn't logged yet.
    var waterWithPendingMl: Int {
        waterMl + WidgetStore.pendingWater().filter { WidgetDay.key(for: $0.date) == date }.reduce(0) { $0 + $1.ml }
    }

    var waterProgress: Double {
        guard waterGoalMl > 0 else { return 0 }
        return min(1, Double(waterWithPendingMl) / Double(waterGoalMl))
    }

    /// "1,250", or whole fl oz in imperial.
    var waterText: String {
        let shown = isImperial ? Int((Double(waterWithPendingMl) / 29.5735295625).rounded()) : waterWithPendingMl
        return shown.formatted(.number.grouping(.automatic))
    }
}

extension WidgetData {
    /// A made-up day for the widget gallery, before the app has written anything.
    static let sample: WidgetData = {
        let day = WidgetDay(
            date: WidgetDay.key(for: Date()),
            isSetUp: true,
            title: "Today",
            dateText: Date().formatted(.dateTime.weekday(.abbreviated).month(.abbreviated).day()),
            setUp: "Finish setting up FitMetric to see your day",
            caloriesNumber: "1,050",
            caloriesLabel: "kcal left",
            caloriesProgress: 0.51,
            proteinNumber: "86g",
            proteinLabel: "protein left",
            proteinProgress: 0.46,
            proteinLeft: "P 86g left",
            eaten: "1,100 eaten",
            macros: [
                WidgetMacro(name: "Protein", short: "P", value: "74 / 160g", left: "86g left", progress: 0.46),
                WidgetMacro(name: "Carbs", short: "C", value: "140 / 220g", left: "80g left", progress: 0.64),
                WidgetMacro(name: "Fat", short: "F", value: "30 / 70g", left: "40g left", progress: 0.43)
            ],
            meals: [
                WidgetMeal(url: "fitmetric://add/BREAKFAST", name: "Breakfast", detail: "3 items · Greek yogurt, Oats, Berries", calories: "460"),
                WidgetMeal(url: "fitmetric://add/LUNCH", name: "Lunch", detail: "2 items · Chicken rice bowl, Iced latte", calories: "640"),
                WidgetMeal(url: "fitmetric://add/DINNER", name: "Dinner", detail: "Not logged yet", calories: nil),
                WidgetMeal(url: "fitmetric://add/SNACK", name: "Snack", detail: "Not logged yet", calories: nil)
            ],
            food: "Food",
            scan: "Scan",
            addWater: "+ 250 ml",
            waterTitle: "Water",
            waterMl: 1_250,
            waterGoalMl: 2_600,
            glassMl: 250,
            isImperial: false,
            waterOf: "of 2,600 ml",
            weightTitle: "Weight",
            hasWeight: true,
            weightNumber: "74.2",
            weightChange: "−0.7 kg this week",
            weightCaption: "7-day trend",
            weightEmpty: "Log a weigh-in to see your trend",
            weightLine: [74.9, 74.8, 74.6, 74.6, 74.4, 74.3, 74.2],
            streakTitle: "Streak",
            streakDays: "3",
            streakUnit: "days",
            streakNote: "Log today to make it 4",
            streakLetters: ["M", "T", "W", "T", "F", "S", "S"],
            streakStates: ["LOGGED", "LOGGED", "LOGGED", "OPEN", "LATER", "LATER", "LATER"]
        )
        return WidgetData(today: day, next: day, isLockNumbersShown: true)
    }()
}
