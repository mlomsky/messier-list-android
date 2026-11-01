# iOS port — handoff notes

This file exists to brief a *new* Claude Code session (running on a Mac, in a
separate repo) that's building a SwiftUI port of this Android app. Paste this
whole file in as context at the start of that session.

## The plan

1. Build a **feature-complete** SwiftUI port of this app — not a trimmed-down
   MVP. Michael is willing to spend multiple nights/weeks on this.
2. Test locally (simulator + Michael's own device) until it's solid.
3. Hand it to 5-10 fellow Westchester Amateur Astronomers (WAA) club members
   for real-world review. Likely distribution path: TestFlight **internal
   testing** under a personal/throwaway Apple Developer account (skips Apple's
   review, builds show up within minutes) — see "Distribution" below.
4. Incorporate their feedback, tweak.
5. Submit to the App Store for real, as the **official WAA club app**, under a
   developer account enrolled in WAA's own name. WAA is a registered
   nonprofit, so it should qualify for Apple's Developer Program fee waiver
   (free apps only, no in-app purchases/paid downloads — confirmed against
   Apple's current official page as of this writing). The account holder
   would need to enroll as the organization, not as Michael individually, and
   supply proof of nonprofit status.

This is now officially the club's app, not Michael's personal project — same
spirit as the Android version's v2.2 rebrand (see CHANGELOG.md) to "WAA
Presents Viewing Tonight."

## What this app does (feature list to match)

Source of truth: this repo's `README.md` "Core features" section, plus
`CHANGELOG.md` for the full version-by-version history. Summary:

- **Three catalogs**, independently toggleable: Messier (110 objects), NGC
  (~4,000 objects), and the naked-eye/telescope planets **plus the Moon**
  (added in v2.2). A magnitude-limit filter applies across all three.
- **Location**: GPS by default, falls back to a default location if
  unavailable; manual address/city search as an alternative.
- **Search**: filter by name, object type, compass direction, minimum
  current altitude.
- **Favorites**: star any object in any catalog; a sort mode shows just
  favorites.
- **Nebula filter tracking**: an "F" button per object records which filter
  (UHC/OIII/H-Beta/Light Pollution/custom) works best for it; some Messier
  objects ship with starting recommendations.
- **Night mode**: switches all text/UI to red to preserve night vision.
- **Header**: location, date, time, moonrise/moonset/sunrise/sunset.
- **Object list**: apparent magnitude per object (live-computed for the Moon
  based on current phase; a representative mean for planets; catalog value
  for Messier/NGC), altitude/azimuth/compass direction that dims once below
  horizon. Sortable by name, max elevation, start/end time, "now," favorites.
- **Viewing session window**: currently a fixed 6pm-6am local-time window
  (see `model/ViewingSession.kt`). A sunset/sunrise-aware version is under
  discussion with the club (see `README.md`'s "Possible future changes"
  section) but not decided — port the current fixed-window behavior as-is
  unless that discussion has since concluded.
- **Elevation chart**: tap any object to see its altitude plotted across the
  session, below-horizon portion shaded.
- **Sun & Moon chart**: tap sunset/sunrise/moonrise/moonset/illumination for
  a combined Sun/Moon altitude chart, shaded by twilight phase.
- **Display Filters dialog**: magnitude limit (all catalogs) + hide-below-
  horizon toggle.
- **About dialog / Help dialog**: branding, version, credits, feature guide.

## Architecture to port from

Everything lives under
`app/src/main/java/com/mlomsky/messierviewer/` in this repo:

- **`astro/`** — the actual astronomy math, in self-contained Kotlin, no
  external dependencies. This is the most mechanically portable part — each
  file is a fairly direct implementation of a named algorithm, so it should
  translate function-by-function into Swift:
  - `JulianDate.kt`, `SiderealTime.kt`, `Coordinates.kt` (RA/Dec <-> Alt/Az,
    `RiseSetThresholds`)
  - `SunPosition.kt` — Meeus low-precision solar formula
  - `MoonPosition.kt` — Meeus reduced-accuracy lunar series, plus (new in
    v2.2) a phase-based apparent-magnitude formula
  - `PlanetPositions.kt` + `PlanetElements.kt` — Standish/JPL mean Keplerian
    elements (1800-2050), Kepler equation solver
  - `AltitudeSampler.kt` — generic rise/set/max-altitude finder by sampling
    across a time window; shared by every catalog and both charts
  - `CardinalDirection.kt` — azimuth -> N/NE/E/.../NW
- **`data/`** — `MessierCatalog.kt` (110 hardcoded objects), `NgcCatalog.kt`
  (loads `assets/ngc_catalog.json`, ~4,000 entries — **this JSON file can be
  reused as-is**, just bundle it in the iOS app and parse with `Codable`),
  `DefaultFilters.kt`, `PreferencesRepository.kt` (Android DataStore — on iOS
  this maps to `UserDefaults` or `SwiftData`/`@AppStorage`), `LocationRepository`,
  `GeocodingRepository`.
- **`model/`** — `CatalogTarget` (sealed interface: Messier/Ngc/PlanetTarget/
  MoonTarget — maps naturally to a Swift `enum` with associated values),
  `ObjectVisibility`, `SortMode`, `CatalogSource`, `ViewingSession`.
- **`ui/`** — Jetpack Compose screens; conceptually map 1:1 to SwiftUI views
  (`MainScreen` -> main list view, `*Dialog.kt` -> `.sheet`/`.alert`
  presentations, `WelcomeScreen` -> SwiftUI launch screen).
- **`viewmodel/MainViewModel.kt`** — the real logic hub: recomputes all
  catalog visibility on a timer (every 5 min) and on location/filter changes;
  maps conceptually to an `ObservableObject` with `@Published` state, or a
  Swift `Observation`-framework model on newer iOS targets.

The original, even-more-canonical source this whole app was ported from is
the Python desktop tool at `../Astronomy` (sibling directory in this same
GitHub org/user) — useful as a tie-breaker reference if the Android port's
behavior is ever ambiguous, but the Android code is the more current and
more cleanly structured reference.

## Branding assets

- Club logo: `app/src/main/res/drawable/waa_logo.png` (1004x496, landscape,
  transparent/black background, white constellation + "Westchester Amateur
  Astronomers" text). Use this for in-app branding (splash/about screens).
- **iOS app icon needs a separate square asset.** The source logo is a wide
  landscape banner — it won't work directly as an iOS Home Screen icon
  (1024x1024 required for App Store, square only, no transparency). Options:
  ask the club for a square/icon-only mark, crop the constellation glyph
  alone from the existing logo, or keep using a simple icon (e.g. a crescent
  moon, matching the original Android launcher icon style at
  `app/src/main/res/drawable/ic_launcher_foreground.xml`) for the Home Screen
  icon specifically, while the full banner logo still appears inside the
  app's own screens. Flag this to Michael before finalizing — don't guess.
- App name: "WAA Presents Viewing Tonight". Credit line: "Created by Michael
  Lomsky for the Westchester Amateur Astronomers."
- Current version: 2.2 (Android `versionCode` 4) — the iOS app doesn't need
  to match this number, but keep them conceptually in sync going forward.

## Distribution (near-term, before official App Store submission)

- Local testing: simulator + Michael's own device via Xcode, no account
  needed beyond a free Apple ID (7-day "Personal Team" signing limit, fine
  for active development).
- Club beta (5-10 testers): most likely path is enrolling a personal/
  throwaway Apple Developer Program membership ($99/year, individual) and
  using TestFlight **internal testing** — skips Apple's Beta App Review
  entirely, builds appear for testers within minutes of processing, but
  testers must be added directly to the developer account's team (up to 100
  people). This account is disposable — it's separate from and doesn't
  complicate the eventual official WAA nonprofit account.
  - Alternative if avoiding the $99 entirely: AltStore or Sideloadly, where
    each tester sideloads the `.ipa` using their *own* free Apple ID — no
    developer account needed on Michael's end, but higher friction (7-day
    app expiry, testers need to install AltServer/Sideloadly themselves).
- Official release: Apple Developer Program fee waiver for WAA as a
  registered nonprofit (free apps only, no IAP — this app qualifies).
  Enrollment must be under WAA's organizational identity, not Michael's
  personal Apple ID — needs the club's EIN/nonprofit determination letter.
  See `developer.apple.com/support/fee-waiver` for the current application
  process.
