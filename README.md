# Messier List Android

An Android app that tells you what's worth looking at tonight. It takes your
phone's location (or a location you set manually), and for every Messier
object and visible planet, calculates when it's above the horizon during the
current viewing session — a port of the logic from the
[`Astronomy`](../../Astronomy) Python desktop tool to a native Android app.

## Status

Builds, runs, and has been tested on both an emulator and a physical device
(Galaxy S24+). Now at **version 2.1** — see [CHANGELOG.md](CHANGELOG.md) for
what's new, and [NOTES.md](NOTES.md) for a running session-by-session
log, known caveats, and open ideas for next time.

## Core features

- **Catalogs**: Messier (110 objects), the NGC catalog (~4,000 objects), and
  the naked-eye/telescope planets — each independently toggleable. A
  magnitude-limit dialog keeps the NGC list to a manageable size.
- **Location**: uses GPS by default, falling back to New York City if
  location is unavailable. A "Set Location" button allows searching by
  address/city name instead.
- **Search**: filter the current list by name, object type, compass
  direction, and/or minimum current altitude.
- **Favorites**: star any object, on any catalog; a dedicated sort mode
  shows just your favorites.
- **Nebula filter tracking**: an "F" button on every object records which
  filter (UHC/OIII/H-Beta/Light Pollution/custom) works best for it; several
  Messier objects ship with a starting recommendation.
- **Night mode**: a toggle that switches all text to red, to preserve night
  vision during observing sessions.
- **Header**: shows current location, date, and time, plus moonrise,
  moonset, sunrise, and sunset for that location.
- **Object list**: shows apparent magnitude for every object (a representative
  mean value for planets, since their real brightness varies with position),
  and the altitude/azimuth/compass direction, which dims out once an object
  is below the horizon. Sortable by:
  - Name
  - Max elevation during the session
  - Start or end time (when the object rises/sets above the horizon)
  - Now (only objects currently above the horizon)
  - Favorites
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

## Data sources

- **Messier catalog** (110 objects) reuses the J2000 ICRS RA/Dec coordinates
  and object-type/difficulty metadata already vetted in
  [`Astronomy/Messier/Messier.py`](../../Astronomy/Messier/Messier.py), so
  results stay consistent between the desktop tool and this app. Apparent
  magnitudes added from Sky at Night's Messier catalogue reference table.
- **NGC catalog** (~4,000 objects) scraped from Wikipedia's "List of NGC
  objects" series; bundled as `assets/ngc_catalog.json`.
- **Planet magnitudes** are mean apparent-magnitude values from Wikipedia's
  "Apparent magnitude" reference table — a representative figure, not a live
  computation, since a planet's actual brightness varies with its position.

## Project structure

```
app/src/main/assets/
  ngc_catalog.json   NGC catalog data (see "Data sources" above)

app/src/main/java/com/mlomsky/messierviewer/
  astro/        JulianDate, SiderealTime, RA/Dec<->AltAz (Coordinates.kt),
                 SunPosition, MoonPosition, PlanetPositions + PlanetElements,
                 AltitudeSampler (generic rise/set/max-altitude finder),
                 CardinalDirection (azimuth -> N/NE/E/.../NW)
  data/         MessierCatalog (110 objects), NgcCatalog (loads the bundled
                 JSON asset), DefaultFilters (starting nebula-filter
                 recommendations), FilterText (checkbox <-> stored-string
                 conversion for the filter dialog), LocationRepository
                 (GPS + saved custom location via DataStore),
                 GeocodingRepository (address search), PreferencesRepository
                 (night mode, enabled catalogs, NGC magnitude limit,
                 hide-below-horizon, favorites, per-object filter overrides)
  model/        CatalogTarget (Messier/Ngc/PlanetTarget + typeLabel),
                 ObjectVisibility, SortMode, CatalogSource,
                 ViewingSession (6pm/6am rule)
  ui/           MainScreen (catalog toggles, sort row, object list),
                 SearchDialog, DisplayFiltersDialog, FilterEditDialog,
                 HelpDialog, AboutDialog, LocationSearchDialog, WelcomeScreen,
                 theme (incl. night-mode red)
  viewmodel/    MainViewModel (ObjectSearchFilter, display-filter pipeline)
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

1. Install [Android Studio](https://developer.android.com/studio) (bundles
   the JDK, Kotlin compiler, and Gradle support — no separate Kotlin
   install needed).
2. Open this folder (`messier-list-android/`) in Android Studio and let
   Gradle sync.
3. Run on a device/emulator (▶), or `Build → Generate APKs` for a debug APK
   to sideload — see [NOTES.md](NOTES.md) for phone-deployment steps.

The dev environment this was built in has no command-line JDK/Gradle, so
building and running happens through Android Studio.
