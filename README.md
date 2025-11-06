# DailyLife

<p align="center">
    <a href="https://github.com/Evening-01/DailyLife">
        <img src="https://socialify.git.ci/Evening-01/DailyLife/image?font=Source+Code+Pro&forks=1&issues=1&language=1&name=1&owner=1&pattern=Circuit+Board&pulls=1&stargazers=1&theme=Light" alt="socialify"/>
    </a>
    <a target="_blank" href="https://socialify.git.ci/Evening-01/DailyLife/README.zh-CN.md">简体中文</a>&nbsp;&nbsp;|&nbsp;&nbsp;  English&nbsp;&nbsp;
</p>

DailyLife is a Jetpack Compose personal finance app that helps you record transactions, understand spending patterns, and stay on top of daily habits. It ships with a Material 3 design system, offline-first storage, rich analytics, and productivity tools such as widget shortcuts, biometric protection, and mortgage & currency calculators.

## Table of Contents
- [Features](#features)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Module Overview](#module-overview)
- [Getting Started](#getting-started)
- [Development Tasks](#development-tasks)
- [Quality Guidelines](#quality-guidelines)
- [Internationalization](#internationalization)
- [Contributing](#contributing)
- [License](#license)

## Features
- Transaction ledger with fast add/edit flows, category presets, mood tracking, and soft-delete support.
- Home feed that summarizes the month, groups daily activity, and surfaces quick navigation targets.
- Interactive analytics with expense/income charts, category rankings, and mood correlation timelines.
- Discover hub featuring a spending type profile, AI preview section, mortgage amortization calculator, and manual currency conversion tool with rate inversion and quick swapping.
- “Me” area containing biometric lock settings, theme & typography controls, usage streak insights, and a full data backup/restore workflow.
- Glance-powered home screen widget offering at-a-glance totals, recent activity, and quick-add transaction actions.
- Room-backed offline database with state flows for live updates and background pruning of archived items.
- Dynamic themes, custom fonts, large UI scale options, and automatic adaptive icons when dynamic color is enabled.

## Architecture
DailyLife follows a modular, layered structure built around MVVM and unidirectional data flow:
- **Compose-first UI**: Screens are pure `@Composable` functions with state hoisted to view models and backed by `StateFlow`.
- **Feature segregation**: Each domain (transactions, charts, discover, mortgage, currency, profile) lives under `feature/<area>` with its own UI, view models, and internal models.
- **Shared foundation**: The `core` package houses design system primitives, dependency injection modules, Room entities/DAOs, FastKV-backed preferences, analytics caching, and utility helpers.
- **Navigation orchestration**: `app/.../navigation` defines the top-level graph and routes, while feature graphs compose into `HomeScreen`.
- **Long-lived analytics cache**: `TransactionAnalyticsRepository` aggregates transaction data once and fans out snapshots to charts, discover cards, profile stats, and widgets.
- **Safe background work**: Repository operations run on injected coroutine scopes (`@ApplicationScope`), keeping UI reactive without leaking activities.

```text
app/src/main/java/com/evening/dailylife/
├── app/                # Application entry points, navigation, widget host
├── core/               # Shared data layer, DI, design system, utilities
└── feature/            # Screen-specific flows (chart, currency, details, discover, home, me, mortgage, transaction)
```

## Tech Stack
- Kotlin 2.1 + Coroutines + Flow
- Jetpack Compose (Material 3, Navigation, Glance App Widgets)
- Hilt for dependency injection
- Room for local persistence
- FastKV for preferences & feature flags
- AndroidX Biometric for fingerprint lock
- Material Kolor & custom theming utilities
- Min SDK 23, Target/Compile SDK 35, JVM target 17

## Module Overview
| Area | Path | Highlights |
| --- | --- | --- |
| Application shell | `app/src/main/java/com/evening/dailylife/app` | `MainActivity`, Hilt `DailyLifeApplication`, navigation graph, app widget host |
| Core foundation | `app/src/main/java/com/evening/dailylife/core` | Room database, repositories, analytics cache, design tokens, biometric manager, DI modules |
| Transaction flow | `feature/transaction` | Add/edit screens, validation, mood picker, repository wiring |
| Analytics | `feature/chart` | Range builders, income/expense charts, mood trend visualization |
| Productivity hub | `feature/discover` | Type profile, AI teaser, mortgage & currency tools |
| Personalization | `feature/me` | Fingerprint lock toggle, theme/typography controls, usage stats, backup/restore |

## Getting Started
1. **Prerequisites**
   - Android Studio Ladybug (2024.2) or newer
   - JDK 17
   - Android SDK Platform 35 and build tools installed
2. **Clone**
   ```bash
   git clone https://github.com/your-org/DailyLife.git
   cd DailyLife
   ```
3. **Local configuration**
   - Ensure `local.properties` points to your Android SDK (`sdk.dir=...`).
   - Optional: define release signing variables (`SIGNING_STORE_FILE`, `SIGNING_STORE_PASSWORD`, `SIGNING_KEY_ALIAS`, `SIGNING_KEY_PASSWORD`) in `local.properties` or environment variables if you plan to build release artifacts.
4. **Sync & build**
   - Open the project in Android Studio and sync Gradle, or run `./gradlew assembleDebug` from the terminal.
5. **Run**
   - Deploy `app` to an emulator or device running Android 6.0 (API 23) or later.

## Development Tasks
- Build debug artifact: `./gradlew assembleDebug`
- JVM unit tests: `./gradlew test`
- Instrumentation & Compose UI tests: `./gradlew connectedDebugAndroidTest`
- Static analysis: `./gradlew lint`
- Clean build outputs: `./gradlew clean`
- Utility tasks for CI/distribution:
  - `./gradlew printAppName`
  - `./gradlew printVersionName`
  - `./gradlew printCommitCount`
  - `./gradlew renameReleaseBundle`

## Quality Guidelines
- Kotlin code style: 4-space indentation, no wildcard imports, trailing commas on multiline argument lists.
- Compose components must keep state hoisted; prefer pure composables plus view models injected by Hilt.
- Add unit tests beside the package under `app/src/test`; put Compose/UI automation in `app/src/androidTest`.
- Update dependency versions via `gradle/sweet-dependency/sweet-dependency-config.yaml`; avoid hardcoding versions in Gradle scripts.
- Run unit tests and `lint` before submitting a pull request; execute `connectedDebugAndroidTest` for navigation or UI-affecting changes.

## Internationalization
- English and Simplified Chinese (`values-zh-rCN`) resources ship by default.
- `LanguagePreferencesRepository` exposes language overrides if you need to add more locales.
- All user-facing strings belong in `app/src/main/res/values/strings.xml`; keep parity with translated files when adding new text.

## Contributing
1. Fork the repository and create a topic branch from `main`.
2. Make changes, keeping commits atomic and following the `{Action} {index}.` message convention (e.g., `Add 1. Implement mortgage calculator cache.`).
3. Run `./gradlew test lint` (and `connectedDebugAndroidTest` when UI changes) before opening a pull request.
4. Include a short summary, screenshots/GIFs for visual tweaks, and a list of validation commands in the PR description.

Bug reports and feature requests are welcome through GitHub Issues. Please attach logs or reproduction steps where possible.

## License
DailyLife is distributed under the [Cooperative Non-Commercial License v1.0 (CNC-1.0)](LICENSE).
- Free to use, modify, and share for non-commercial purposes.
- Derivatives must keep the same license and provide full source code with attribution to DailyLife contributors.
- Commercial use, venture-backed usage, or deployments by corporations with annual revenue over $1M USD require prior written approval.
For commercial licensing inquiries, contact the DailyLife maintainers.
