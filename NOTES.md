# Session Notes

Running log for picking this project back up after a break. See `README.md`
for the actual architecture/build docs — this file is "where we left off"
context that doesn't belong there.

## Where things stand

App builds and runs cleanly. Tested on the emulator and on a physical Galaxy
S24+ (sideloaded via a debug APK — see "Deploying to a phone" below). Core
feature set from the README is implemented, plus everything in the log below.

## What's been added since the first commit

1. **Fixed first compile error**: `GeocodingRepository` referenced
   `Address.hasAltitude()`/`altitude`, which don't exist on
   `android.location.Address` (only on `Location`). Elevation there was
   `0.0` until item 5 below gave it a real source.
2. **Name sort**: was lexicographic ("M1, M10, M100…"). Added a natural-sort
   comparator (`MainViewModel.naturalCompare`) so digit runs compare
   numerically.
3. **Rise/Set time sort**: the combined chip was wired to always select
   `START_TIME`, so `END_TIME` was unreachable. Fixed the toggle; labels are
   "Rise Time" / "Set Time".
4. **Location header**: added lat/long (degrees-minutes-seconds) and
   elevation under the location name.
5. **Real elevation data**: `Address` has no altitude field, and GPS
   altitude fixes are often unavailable (esp. on emulators). Added
   `ElevationRepository`. Originally used the Open-Elevation API — its SSL
   cert was found **expired** (verified with `curl -v`, `SEC_E_CERT_EXPIRED`)
   — switched to **Open Topo Data** (`api.opentopodata.org`, `aster30m`
   dataset), which works reliably. Used both for address search and as a GPS
   fallback. Requires the `INTERNET` permission (added to the manifest).
   Elevation is displayed in feet.
6. **Night mode chip contrast**: the selected sort chip stayed grey/white in
   night mode because Material3's `FilterChip` selected state pulls from
   `secondaryContainer`, which the night theme never set. Gave the chip an
   explicit night-mode-only color override rather than touching the global
   theme (so nothing else changed).
7. **"Now" sort**: 4th sort mode — filters to objects currently above the
   horizon, sorted by current altitude descending. Computes
   `currentAltitudeDeg`/`currentAzimuthDeg` per object at `Instant.now()`,
   independent of the 6pm–6am session window (works at any time of day).
   Along the way, fixed a state bug where switching sort modes re-sorted
   whatever was currently *displayed* (i.e. the "Now"-filtered subset)
   instead of the full catalog — `MainUiState.allObjects` now holds the
   unfiltered source of truth.
8. **Sun/moon times outside 6pm–6am**: were previously bounded to the
   session window, so an early winter sunset or late sunrise showed "--".
   Widened the search to a **noon-to-noon 24h window** (the standard
   "observing night" convention) — this reliably captures exactly one
   sunrise + one sunset. Moonrise/moonset can still legitimately be missing
   some nights (the Moon's ~24h50m cycle vs. a 24h calendar day) — that
   matches real published moon tables, not a bug.
9. **Lunar illumination %** — added next to Moonrise/Moonset, via Meeus's
   simplified geocentric-elongation method.
10. **Alt/Az per object** — current altitude/azimuth + 8-point cardinal
    direction (e.g. `42°/315° NW`) shown next to each object's name.
11. Coordinate seconds rounded to whole numbers (was showing decimals).
12. Current time added next to the "Tonight's Sky" title in the top bar.
13. **Themed splash screen** (`androidx.core.splashscreen`) using the
    existing dark-navy/crescent-moon launcher branding instead of the
    generic Android 12+ default.
14. **Personalization**: `WelcomeScreen.kt` (shown once per app session
    during the first data load — "Created by Michael Lomsky") and
    `AboutDialog.kt` (ⓘ icon in the top bar — app icon, version pulled live
    from `BuildConfig.VERSION_NAME`, creator credit, description).

## Version 2 session

Full detail in [CHANGELOG.md](CHANGELOG.md); short version:

15. **NGC catalog added** (~4,000 objects scraped from Wikipedia, bundled as
    `assets/ngc_catalog.json`). Planets/Messier/NGC are now independently
    toggleable buttons under the title bar. A "Display Filters" dialog (tune
    icon) sets an NGC magnitude limit — applied *before* rise/set sampling,
    which is what keeps turning NGC on from being slow — plus a
    hide-below-horizon checkbox that applies to all catalogs.
16. **Search dialog** (magnifying glass): name/type/direction/min-altitude,
    combinable, instant (filters already-computed data, no re-sampling).
17. **Favorites**: star button on every object (any catalog), persisted via
    DataStore; a ★ sort-mode button filters to just favorites (tap again to
    turn off). This resolves the "favorites/pinned-objects list" idea from
    the "Open for next session" list below.
18. **Nebula filter button (F)** now works on every object, not just
    Messier — same UHC/OIII/H-Beta/Light Pollution/custom checkbox dialog.
19. **Magnitude everywhere**: Messier (from Sky at Night's reference table)
    and NGC (from the scrape) already had it; planets now show a mean
    apparent magnitude from Wikipedia's "Apparent magnitude" page.
20. Fixed a bug where long NGC names squished the altitude/azimuth text off
    the row, and a bug where the night-mode "F" button picked up Material's
    default purple/white dark-theme colors instead of the app's red/black
    night theme.
21. Help dialog rewritten to cover all of the above, with a down-arrow
    indicator when there's more to scroll to. About dialog and the splash
    screen now say "Version 2"; About also lists a "What's new in v2" bullet
    summary. `versionCode`/`versionName` bumped to 2 / "2.0".

## Version 2.1 session

Explored on a throwaway `image-popup` branch first (idea the user wasn't
sure they'd keep), then merged in once it proved out. Full detail in
[CHANGELOG.md](CHANGELOG.md); short version:

22. **Elevation chart popup**: tap any object's name (any catalog) for a
    chart of its altitude across tonight's session, below-horizon portion
    shaded. Built on a new `AltitudeSampler.series()` helper (the existing
    rise/set sampler was refactored to reuse it, not duplicated).
23. **Sun & Moon chart popup**: tap Sunset/Sunrise/Moonrise/Moonset/
    Illumination for a combined Sun+Moon altitude chart, with the
    background shaded by the Sun's own altitude — daylight clear, then
    progressively darker bands for civil/nautical twilight, astronomical
    twilight (-12° to -18°), and full night (below -18°). Chart lines stay
    in the red family in Night Mode to preserve dark adaptation; removed
    the per-hour vertical gridlines after the first pass looked too busy,
    keeping just the hour labels, horizontal gridlines, and shaded bands.
24. **NGC catalog cleanup**: removed 18 entries whose scraped type was
    "Nonexistent", "Doesn't exist", or bare "Unknown" (4,034 → 4,016
    entries) — see the "NGC data quality" item below, now partially
    addressed.
25. NGC rows with no published magnitude now show "Mag --" instead of
    silently dropping the magnitude segment, matching Messier's format.
    ~425 of 4,016 NGC entries (10.6%) still have no magnitude in the
    Wikipedia source data — mostly open clusters, double/plain stars, and
    some galaxies — that's a data-source gap, not a display bug.
26. `versionCode`/`versionName` bumped to 3 / "2.1"; About/splash updated
    with a "What's new in v2.1" summary alongside the existing v2 one.

## Deploying to a phone (Galaxy S24+, no Play Store)

- Developer options: **Settings → About phone → Software information → tap
  Build number 7x**.
- **Fastest while actively iterating**: enable USB debugging, plug in,
  select the phone in Android Studio's run-target dropdown, hit ▶ Run.
- **No cable needed after transfer**: `Build → Generate APKs` (this Android
  Studio version's name for the old "Build APK(s)" — **not** "Generate
  Signed App Bundle/APK", that's the Play Store signing path). Copy
  `app-debug.apk` to the phone (USB file transfer or Drive), open it from
  Files, allow install from that source once, install.

## Play Store plan (decided, not yet started)

- Renamed the in-app title text to "Messier Tonight" everywhere (top bar,
  welcome screen, About dialog) to match `strings.xml`'s `app_name` — they
  were inconsistent before.
- Strategy: **soft-launch via the user's astronomy club** before touching
  Play Console. Built `TESTER_INSTALL_GUIDE.md` (plain-language sideload
  instructions for non-developers) and zipped it with the current
  `app-debug.apk` into `MessierTonight-Beta.zip`, posted to the club's
  Discord for feedback.
- Branding decision: **keeping personal name/credit** ("Created by Michael
  Lomsky") for now rather than branding it under the club's name — may
  revisit after talking to the club more.
- Play Store account plan, when they get there: likely a **new, separate
  Google account** dedicated to the developer identity (not their primary
  account) — organizational separation, not anonymity (Google still
  requires real ID verification regardless). The public "developer name"
  on the store listing doesn't have to be a real name, so this doesn't
  block the personal-branding decision above.
- Google Play requires new personal developer accounts to run a **closed
  testing track with 12+ testers enrolled continuously for 14 days**
  before unlocking production publishing — the club is the natural source
  for those 12 testers when the time comes.
- Ads (AdMob): **deferred to a possible future update**, not part of
  initial release. User is skeptical they'll bother, leaning instead
  toward the app just being a free community tool/goodwill gesture for the
  club. If revisited: needs AdMob account + SDK integration + Data Safety
  form update + privacy policy update — none of that is done yet.
- Still need before any Play Store submission: signing keystore + `.aab`
  release build, store listing assets (icon, screenshots, feature
  graphic), a hosted privacy policy page (required due to location
  permission use), Data Safety form, content rating questionnaire.

## Open for next session

- **Waiting on club feedback** from the Discord beta drop — check whether
  anything's come back before planning next features.
- **More personalization**: favorites/pinned-objects is now done (v2). Still
  just brainstorming fodder, not yet requested: time-of-day-aware greeting, a
  user-chosen accent color, home-location presets beyond the single saved
  custom slot.
- **NGC data quality**: the scrape strips known junk (duplicates, nonexistent
  entries, "(Located in...)" annotations, footnote markers) as it's found,
  but with ~4,000 entries there may be more odd ones lurking — flag any that
  turn up while browsing the list.
- Debug APK is unsigned/unoptimized — fine for personal/club testing. The
  Play Store path above is decided in outline but not yet started.
- Planet positions are the least-verified part of the astronomy engine (no
  compiler/reference ephemeris was available when first written) — worth
  spot-checking against Stellarium or timeanddate.com if precision ever
  matters.
- `ElevationRepository.kt` is the single place elevation comes from — if
  Open Topo Data ever becomes unreliable (it's a free, keyless public API),
  swap the provider there.
