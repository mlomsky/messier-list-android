# Messier List Android

An Android app that tells you what's worth looking at tonight. It takes your
phone's location (or a location you set manually), and for every Messier
object and visible planet, calculates when it's above the horizon during the
current viewing session — a port of the logic from the
[`Astronomy`](../../Astronomy) Python desktop tool to a native Android app.

## Status

First implementation is complete and ready to open in Android Studio (see
[Building](#building)). It hasn't been compiled or run yet — I don't have a
JDK/Android SDK in this environment — so treat it as needing a first-build
pass to shake out any typos.

## Core features

- **Location**: uses GPS by default, falling back to New York City if
  location is unavailable. A "Set Location" button allows searching by
  address/city name instead.
- **Night mode**: a toggle that switches all text to red, to preserve night
  vision during observing sessions.
- **Header**: shows current location, date, and time, plus moonrise,
  moonset, sunrise, and sunset for that location.
- **Object list**: every Messier object and the naked-eye/telescope planets,
  sortable by:
  - Name
  - Max elevation during the session
  - Start or end time (when the object rises/sets above the horizon)
- **Viewing session window**: one observing session runs from 6pm local time
  to 6am the next day. The app picks the correct window based on when it's
  opened:
  - If it's 6pm or later, the session is tonight 6pm → tomorrow 6am.
  - If it's midnight–6am, the session is the tail end of the session that
    started the previous evening (today's midnight → 6am).

## Architecture decisions

| Decision | Choice | Why |
|---|---|---|
| UI toolkit | Jetpack Compose | Declarative UI fits the live clock, sortable list, and night-mode theme swap better than XML/View boilerplate. |
| Astronomy math | Self-contained Kotlin port of Meeus low-precision algorithms | Fully offline, no external astronomy dependency; arcminute-level accuracy is more than enough for "is this above the horizon." Covers Julian date, sidereal time, RA/Dec → Alt/Az, and sun/moon/planet position formulas. |
| Manual location entry | Android `Geocoder` address search | Mirrors the Python app's Nominatim-based address search UX; requires network only when the user explicitly sets a location. |
| Location persistence | Single remembered custom location (DataStore) | GPS is the default path; one overwritable "custom location" slot is enough, unlike the Python app's multi-preset `user_data_folder`. |

## Data reused from the Python project

The Messier catalog (110 objects) reuses the J2000 ICRS RA/Dec coordinates
and object-type/difficulty metadata already vetted in
[`Astronomy/Messier/Messier.py`](../../Astronomy/Messier/Messier.py), so
results stay consistent between the desktop tool and this app.

## Project structure

```
app/src/main/java/com/mlomsky/messierviewer/
  astro/        JulianDate, SiderealTime, RA/Dec<->AltAz (Coordinates.kt),
                 SunPosition, MoonPosition, PlanetPositions + PlanetElements,
                 AltitudeSampler (generic rise/set/max-altitude finder)
  data/         MessierCatalog (110 objects, ported from the Python project),
                 LocationRepository (GPS + saved custom location via
                 DataStore), GeocodingRepository (address search),
                 PreferencesRepository (night mode)
  model/        CatalogTarget, ObjectVisibility, ViewingSession (6pm/6am rule)
  ui/           MainScreen, LocationSearchDialog, theme (incl. night-mode red)
  viewmodel/    MainViewModel
  MainActivity.kt
```

### Astronomy engine notes

- **Sun**: Meeus's standard low-precision solar formula — high confidence,
  this is the same algorithm behind most "sunrise/sunset calculator" tools.
- **Moon**: Meeus's reduced-accuracy lunar series (~10' longitude accuracy) —
  good enough for rise/set/altitude, not for precision pointing.
- **Planets**: mean Keplerian orbital elements (Standish/JPL, valid
  1800–2050) run through a Kepler-equation solver, no light-time/aberration
  correction. This is the least-verified part of the engine (no compiler or
  reference ephemeris was available while writing it) — worth spot-checking
  a planet's rise/set time against Stellarium or timeanddate.com once the
  app is running.
- Messier RA/Dec are J2000 with no precession correction applied; the drift
  by 2026 is well under the accuracy this app needs.

## Building

This was written without access to a JDK, Android SDK, or Gradle in the
dev environment, so **it hasn't been compiled yet** — there may be a typo or
two to fix on first build. To build it:

1. Install [Android Studio](https://developer.android.com/studio) (bundles
   the JDK, Kotlin compiler, and Gradle support — no separate Kotlin
   install needed).
2. Open this folder (`messier-list-android/`) in Android Studio.
3. On first open, Android Studio will notice the Gradle wrapper jar is
   missing and offer to generate it — accept that (or run `gradle wrapper`
   yourself if you have a system Gradle install).
4. Let Gradle sync, then Run on a device/emulator.

If sync or build turns up errors, paste them back and they can be fixed
directly — this first pass optimized for a complete, coherent app over
guaranteeing zero-typo compilation.
