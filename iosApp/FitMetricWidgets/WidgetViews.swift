import AppIntents
import SwiftUI
import WidgetKit

/// The app's widget colours, light and dark.
enum Palette {
    static let background = dynamic(0xFFFFFF, 0x1C1D20)
    static let ink = dynamic(0x15171A, 0xEEEDE8)
    static let muted = dynamic(0x6B6E75, 0xA3A6AD)
    static let accent = dynamic(0x0E6E52, 0x3FBF8F)
    static let onAccent = dynamic(0xFFFFFF, 0x08241A)
    static let tonal = dynamic(0xDDEFE7, 0x15322A)
    static let tonalInk = dynamic(0x0E6E52, 0x7FD8B5)
    static let track = dynamic(0xECEAE3, 0x2E3136)
    static let protein = dynamic(0x2A5BD7, 0x7AA2F7)
    static let carbs = dynamic(0xE0A12B, 0xF0B44C)
    static let fat = dynamic(0xD4603F, 0xF08A6C)
    static let water = dynamic(0x2468B8, 0x6FAFEF)
    static let waterSoft = dynamic(0xDCEAF8, 0x16283B)
    static let warm = dynamic(0xB07A12, 0xF0B44C)
    static let macros = [protein, carbs, fat]

    private static func dynamic(_ light: UInt32, _ dark: UInt32) -> Color {
        Color(uiColor: UIColor { traits in rgb(traits.userInterfaceStyle == .dark ? dark : light) })
    }

    private static func rgb(_ hex: UInt32) -> UIColor {
        UIColor(red: CGFloat((hex >> 16) & 0xFF) / 255, green: CGFloat((hex >> 8) & 0xFF) / 255, blue: CGFloat(hex & 0xFF) / 255, alpha: 1)
    }
}

private enum Links {
    static let open = URL(string: "fitmetric://open")!
    static let logFood = URL(string: "fitmetric://shortcut/LOG_FOOD")!
    static let scan = URL(string: "fitmetric://shortcut/SCAN_BARCODE")!
    static let logWeight = URL(string: "fitmetric://shortcut/LOG_WEIGHT")!
}

private extension Font {
    static func number(_ size: CGFloat) -> Font { .system(size: size, weight: .heavy, design: .rounded) }
}

// MARK: - Pieces

struct WidgetTitle: View {
    let title: String
    var trailing: String?

    var body: some View {
        HStack(spacing: 6) {
            Image(systemName: "chart.line.uptrend.xyaxis")
                .font(.system(size: 8, weight: .heavy))
                .foregroundStyle(Palette.onAccent)
                .frame(width: 16, height: 16)
                .background(Palette.accent, in: RoundedRectangle(cornerRadius: 5))
            Text(title).font(.system(size: 12, weight: .bold)).foregroundStyle(Palette.ink)
            Spacer(minLength: 0)
            if let trailing {
                Text(trailing).font(.system(size: 12, weight: .semibold)).foregroundStyle(Palette.muted)
            }
        }
    }
}

struct Ring: View {
    let progress: Double
    let number: String
    let label: String
    var lineWidth: CGFloat = 10

    var body: some View {
        ZStack {
            Circle().stroke(Palette.track, lineWidth: lineWidth)
            Circle()
                .trim(from: 0, to: progress)
                .stroke(Palette.accent, style: StrokeStyle(lineWidth: lineWidth, lineCap: .round))
                .rotationEffect(.degrees(-90))
                .widgetAccentable()
            VStack(spacing: 0) {
                Text(number).font(.number(22)).minimumScaleFactor(0.6).foregroundStyle(Palette.ink)
                Text(label).font(.system(size: 10, weight: .semibold)).foregroundStyle(Palette.muted)
            }
            .padding(lineWidth)
        }
    }
}

struct MacroBars: View {
    let macros: [WidgetMacro]

    var body: some View {
        VStack(spacing: 7) {
            ForEach(Array(macros.enumerated()), id: \.offset) { index, macro in
                VStack(spacing: 3) {
                    HStack {
                        Text(macro.name).font(.system(size: 11, weight: .bold)).foregroundStyle(Palette.ink)
                        Spacer(minLength: 4)
                        Text(macro.value).font(.system(size: 11, weight: .semibold)).foregroundStyle(Palette.muted)
                    }
                    Bar(progress: macro.progress, color: Palette.macros[index % Palette.macros.count])
                }
            }
        }
    }
}

struct Bar: View {
    let progress: Double
    let color: Color
    var height: CGFloat = 5

    var body: some View {
        GeometryReader { proxy in
            ZStack(alignment: .leading) {
                Capsule().fill(Palette.track)
                Capsule().fill(color).frame(width: proxy.size.width * progress)
            }
        }
        .frame(height: height)
    }
}

/// A rounded button with an icon and a label.
private struct Pill: View {
    let label: String
    let systemImage: String
    let background: Color
    let foreground: Color

    var body: some View {
        Label(label, systemImage: systemImage)
            .font(.system(size: 12, weight: .bold))
            .labelStyle(PillLabelStyle())
            .lineLimit(1)
            .minimumScaleFactor(0.8)
            .foregroundStyle(foreground)
            .frame(maxWidth: .infinity, minHeight: 30)
            .background(background, in: Capsule())
    }
}

private struct PillLabelStyle: LabelStyle {
    func makeBody(configuration: Configuration) -> some View {
        HStack(spacing: 4) {
            configuration.icon.font(.system(size: 10, weight: .heavy))
            configuration.title
        }
    }
}

private struct FoodButton: View {
    let day: WidgetDay

    var body: some View {
        Link(destination: Links.logFood) {
            Pill(label: day.food, systemImage: "plus", background: Palette.accent, foreground: Palette.onAccent)
        }
    }
}

private struct ScanButton: View {
    let day: WidgetDay

    var body: some View {
        Link(destination: Links.scan) {
            Pill(label: day.scan, systemImage: "barcode", background: Palette.tonal, foreground: Palette.tonalInk)
        }
    }
}

private struct WaterButton: View {
    let day: WidgetDay

    var body: some View {
        Button(intent: AddWaterIntent()) {
            Pill(label: day.addWater, systemImage: "drop.fill", background: Palette.waterSoft, foreground: Palette.water)
        }
        .buttonStyle(.plain)
    }
}

private struct SetUpView: View {
    let day: WidgetDay

    var body: some View {
        VStack(alignment: .leading) {
            WidgetTitle(title: day.title)
            Spacer()
            Text(day.setUp).font(.system(size: 12, weight: .medium)).foregroundStyle(Palette.muted)
        }
    }
}

private extension View {
    func widgetCard() -> some View {
        containerBackground(for: .widget) { Palette.background }
    }
}

// MARK: - Today

struct TodayView: View {
    let entry: DayEntry
    @Environment(\.widgetFamily) private var family

    private var day: WidgetDay { entry.day }
    private var isProtein: Bool { entry.bigNumber == .protein }
    private var number: String { isProtein ? day.proteinNumber : day.caloriesNumber }
    private var label: String { isProtein ? day.proteinLabel : day.caloriesLabel }
    private var progress: Double { isProtein ? day.proteinProgress : day.caloriesProgress }

    var body: some View {
        switch family {
        case .accessoryCircular: circular
        case .accessoryRectangular: rectangular
        case .accessoryInline: inline
        default:
            Group {
                if !day.isSetUp {
                    SetUpView(day: day)
                } else if family == .systemLarge {
                    large
                } else if family == .systemMedium {
                    medium
                } else {
                    small
                }
            }
            .widgetURL(Links.open)
            .widgetCard()
        }
    }

    private var small: some View {
        VStack(spacing: 6) {
            WidgetTitle(title: day.title)
            Ring(progress: progress, number: number, label: label, lineWidth: 11)
            HStack {
                Text(day.proteinLeft)
                Spacer(minLength: 4)
                Text(day.eaten)
            }
            .font(.system(size: 10, weight: .semibold))
            .foregroundStyle(Palette.muted)
            .lineLimit(1)
        }
    }

    private var medium: some View {
        HStack(spacing: 14) {
            VStack(spacing: 6) {
                WidgetTitle(title: day.title)
                Ring(progress: progress, number: number, label: label)
            }
            .frame(width: 112)
            VStack(spacing: 8) {
                MacroBars(macros: day.macros)
                Spacer(minLength: 0)
                HStack(spacing: 6) {
                    FoodButton(day: day)
                    WaterButton(day: day)
                }
            }
        }
    }

    private var large: some View {
        VStack(spacing: 10) {
            WidgetTitle(title: day.title, trailing: day.dateText)
            HStack(spacing: 14) {
                Ring(progress: progress, number: number, label: label).frame(width: 96, height: 96)
                MacroBars(macros: day.macros)
            }
            VStack(spacing: 0) {
                Divider().overlay(Palette.track)
                ForEach(day.meals, id: \.self) { meal in
                    Link(destination: URL(string: meal.url) ?? Links.open) {
                        HStack(spacing: 10) {
                            VStack(alignment: .leading, spacing: 1) {
                                Text(meal.name).font(.system(size: 13, weight: .bold)).foregroundStyle(Palette.ink)
                                Text(meal.detail).font(.system(size: 11)).foregroundStyle(Palette.muted).lineLimit(1)
                            }
                            Spacer(minLength: 4)
                            if let calories = meal.calories {
                                Text(calories).font(.system(size: 13, weight: .bold)).foregroundStyle(Palette.muted)
                            } else {
                                Image(systemName: "plus")
                                    .font(.system(size: 13, weight: .bold))
                                    .foregroundStyle(Palette.onAccent)
                                    .frame(width: 26, height: 26)
                                    .background(Palette.accent, in: Circle())
                            }
                        }
                        .frame(maxHeight: .infinity)
                    }
                    Divider().overlay(Palette.track)
                }
            }
            HStack(spacing: 6) {
                FoodButton(day: day)
                ScanButton(day: day)
                WaterButton(day: day)
            }
        }
    }

    private var circular: some View {
        Gauge(value: progress) {
            Text(isProtein ? "PROT" : "KCAL")
        } currentValueLabel: {
            Text(number).privacySensitive(!entry.isLockNumbersShown)
        }
        .gaugeStyle(.accessoryCircular)
        .widgetURL(Links.open)
        .containerBackground(for: .widget) { AccessoryWidgetBackground() }
    }

    private var rectangular: some View {
        VStack(alignment: .leading, spacing: 3) {
            Text("\(day.caloriesNumber) \(day.caloriesLabel)").font(.system(size: 13, weight: .bold))
            ForEach(day.macros, id: \.self) { macro in
                HStack(spacing: 5) {
                    Text(macro.short).font(.system(size: 10, weight: .bold)).frame(width: 10, alignment: .leading)
                    Bar(progress: macro.progress, color: .primary, height: 4).widgetAccentable()
                    Text(macro.left).font(.system(size: 10, weight: .semibold)).foregroundStyle(.secondary)
                        .frame(width: 54, alignment: .trailing)
                }
            }
        }
        .privacySensitive(!entry.isLockNumbersShown)
        .widgetURL(Links.open)
        .containerBackground(for: .widget) { Color.clear }
    }

    private var inline: some View {
        Label {
            Text("\(number) \(label)").privacySensitive(!entry.isLockNumbersShown)
        } icon: {
            Image(systemName: "chart.line.uptrend.xyaxis")
        }
        .widgetURL(Links.open)
        .containerBackground(for: .widget) { Color.clear }
    }
}

// MARK: - Water, weight, streak

struct WaterView: View {
    let day: WidgetDay

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Label(day.waterTitle, systemImage: "drop.fill")
                .font(.system(size: 12, weight: .bold))
                .foregroundStyle(Palette.water)
            HStack(alignment: .bottom, spacing: 10) {
                GeometryReader { proxy in
                    ZStack(alignment: .bottom) {
                        RoundedRectangle(cornerRadius: 9).fill(Palette.waterSoft)
                        RoundedRectangle(cornerRadius: 9).fill(Palette.water).frame(height: proxy.size.height * day.waterProgress)
                    }
                }
                .frame(width: 30)
                .frame(maxHeight: 66)
                VStack(alignment: .leading, spacing: 2) {
                    Text(day.waterText).font(.number(24)).foregroundStyle(Palette.ink)
                    Text(day.waterOf).font(.system(size: 11, weight: .semibold)).foregroundStyle(Palette.muted)
                }
                Spacer(minLength: 0)
            }
            WaterButton(day: day)
        }
        .widgetURL(Links.open)
        .widgetCard()
    }
}

struct WeightView: View {
    let day: WidgetDay

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            WidgetTitle(title: day.weightTitle)
            if day.hasWeight {
                Text(day.weightNumber).font(.number(26)).foregroundStyle(Palette.ink).padding(.top, 2)
                Text(day.weightChange).font(.system(size: 11, weight: .bold)).foregroundStyle(Palette.accent)
                Sparkline(values: day.weightLine).frame(maxHeight: .infinity)
                Text(day.weightCaption).font(.system(size: 10, weight: .semibold)).foregroundStyle(Palette.muted)
            } else {
                Spacer()
                Text(day.weightEmpty).font(.system(size: 12, weight: .medium)).foregroundStyle(Palette.muted)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .widgetURL(Links.logWeight)
        .widgetCard()
    }
}

struct Sparkline: View {
    let values: [Double]

    var body: some View {
        GeometryReader { proxy in
            if values.count >= 2, let low = values.min(), let high = values.max() {
                let range = max(high - low, 0.0001)
                let points = values.enumerated().map { index, value in
                    CGPoint(
                        x: proxy.size.width * CGFloat(index) / CGFloat(values.count - 1),
                        y: 3 + (proxy.size.height - 6) * CGFloat(1 - (value - low) / range)
                    )
                }
                let line = Path { path in path.addLines(points) }
                Path { path in
                    path.addLines(points)
                    path.addLine(to: CGPoint(x: proxy.size.width, y: proxy.size.height))
                    path.addLine(to: CGPoint(x: 0, y: proxy.size.height))
                    path.closeSubpath()
                }
                .fill(Palette.tonal)
                line.stroke(Palette.accent, style: StrokeStyle(lineWidth: 2.5, lineCap: .round, lineJoin: .round))
            }
        }
    }
}

struct StreakView: View {
    let day: WidgetDay

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Label(day.streakTitle, systemImage: "flame.fill")
                .font(.system(size: 12, weight: .bold))
                .foregroundStyle(Palette.warm)
            HStack(alignment: .firstTextBaseline, spacing: 6) {
                Text(day.streakDays).font(.number(38)).foregroundStyle(Palette.ink)
                Text(day.streakUnit).font(.system(size: 12, weight: .bold)).foregroundStyle(Palette.muted)
            }
            Spacer(minLength: 0)
            HStack(spacing: 3) {
                ForEach(Array(zip(day.streakLetters, day.streakStates).enumerated()), id: \.offset) { _, pair in
                    VStack(spacing: 3) {
                        dot(state: pair.1)
                        Text(pair.0).font(.system(size: 9, weight: .bold)).foregroundStyle(Palette.muted)
                    }
                    .frame(maxWidth: .infinity)
                }
            }
            Text(day.streakNote).font(.system(size: 10, weight: .semibold)).foregroundStyle(Palette.muted).lineLimit(1)
        }
        .widgetURL(Links.open)
        .widgetCard()
    }

    @ViewBuilder
    private func dot(state: String) -> some View {
        switch state {
        case "LOGGED": Circle().fill(Palette.warm).frame(width: 12, height: 12)
        case "OPEN": Circle().strokeBorder(Palette.warm, lineWidth: 2).frame(width: 12, height: 12)
        default: Circle().fill(Palette.track).frame(width: 12, height: 12)
        }
    }
}
