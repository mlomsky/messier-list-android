# Changelog

## Version 2 (2026-10-04)

### New catalogs
- **NGC catalog** added: ~4,000 deep-sky objects scraped from Wikipedia's "List
  of NGC objects" series (number, type, RA/Dec, magnitude where published,
  common name), bundled as `assets/ngc_catalog.json` and loaded via
  `NgcCatalog.kt`. Entries flagged as duplicates, nonexistent, or pure
  cross-reference annotations ("Located in...", footnote markers) were cleaned
  out or stripped during import.
- **Planets, Messier, and NGC can each be toggled on/off independently**
  (`CatalogSource`), persisted via DataStore. NGC is off by default given its
  size.
- **NGC magnitude limit**: a "Display Filters" dialog (tune icon) lets you cap
  the dimmest NGC magnitude included, applied *before* the expensive rise/set
  sampling so turning NGC on doesn't force thousands of calculations.

### Finding things
- **Search** (magnifying-glass icon): filter the current list by name
  contains, type contains (e.g. "Galaxy"), compass direction, and/or a
  minimum current altitude — all optional, combinable, and instant (no
  re-sampling).
- **Hide objects below the horizon**: a checkbox in the same Display Filters
  dialog, applied across all catalogs.
- **Favorites**: a star button next to every object (any catalog) toggles a
  favorite; a matching ★ sort-mode button filters the list down to just your
  favorites (tap again to turn it off).

### Nebula filter tracking
- The "F" filter-recommendation button (UHC / OIII / H-Beta / Light Pollution
  / custom write-in) now works on **every object**, not just Messier. Filter
  text shows as a "Filter: ..." line under the object when set.
- A handful of Messier objects still ship with starting recommendations
  (`DefaultFilters.kt`); nothing is pre-filled for NGC/planets.

### Magnitude everywhere
- Messier and NGC objects show their apparent magnitude (NGC from the scrape;
  Messier from Sky at Night's reference table).
- Planets now show a representative **mean** apparent magnitude (from
  Wikipedia's "Apparent magnitude" reference table), since their real
  brightness varies continuously with position.

### UI polish and fixes
- Long NGC names no longer squish the altitude/azimuth text off the row —
  the name now wraps within its own bounded space instead of claiming
  unbounded width.
- Altitude/azimuth/compass text turns red (day) or grey (night mode) once an
  object drops to or below the horizon.
- Fixed the night-mode "F" filter button pulling in Material's default
  purple/white dark-theme colors instead of the app's red/black night theme;
  empty filter buttons now get a visible red outline in night mode.
- Help (?) dialog rewritten to cover every feature above, with a down-arrow
  indicator that appears whenever there's more content to scroll to.
- About dialog and the loading splash now say "Version 2" (with today's date
  on the About screen), plus a "What's new in v2" bullet summary on the About
  screen. Top app bar shows the version next to the title.
- `versionCode`/`versionName` bumped to 2 / "2.0".

## Version 1

Initial release: Messier catalog (110 objects) + naked-eye/telescope planets,
GPS or manual location, night mode, sun/moon/illumination data, and sortable
object list (Name / Max Elevation / Rise-Set Time / Now). See `NOTES.md` for
the detailed session-by-session history of this release.
