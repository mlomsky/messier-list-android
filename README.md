# Messier List Android

An Android app that tells you what's worth looking at tonight. It takes your
phone's location (or a location you set manually), and for every Messier
object and visible planet, calculates when it's above the horizon during the
current viewing session — a port of the logic from the
[`Astronomy`](../../Astronomy) Python desktop tool to a native Android app.

## Status

Design/scaffolding stage — architecture decisions below are locked in;
implementation is in progress.

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

## Planned project structure

```
app/src/main/java/com/mlomsky/messierviewer/
  astro/        Julian date, sidereal time, RA/Dec<->AltAz, sun/moon/planet
                 position formulas, rise/set/transit calculations
  data/         Location repository (GPS + saved custom location via
                 DataStore), preferences (night mode)
  model/        SkyObject, ViewingSession (6pm/6am window logic)
  ui/           Compose screens, theme (including night-mode red theme)
  viewmodel/    MainViewModel
```

## Building

Gradle wrapper and full source are being added as implementation proceeds.
Open the project folder in Android Studio once scaffolding is complete.
