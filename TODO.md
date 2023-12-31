# ToDos

## Version Hisotry

### Version 3

* Bugfix: Usage counter now only respecting reservations within current period.
* Bugfix: Hardening metadata fetching (recover from any 5xx API response).
* Bugfix: Visited icon is now only shown where it should be shown.
* Define version for macOs native app bundle.
* Minor improvement of synchronisation progress message.
* Minor improvement of partner update form using icons.

## Backlog

### High Prio

* sync available spots
* BUG? when click on "visited workouts", no details shown?! (also check if exception is displayed!)

### Medium Prio

* ability to group/link partners together
* harden retry logic (use ktor-retry plugin)
* include in available counter: how many workouts reserved in this period for this particular partner (not only past checkins)
* UI BUG: go to partners tab, select partner, select workout, update partner; workout should stay, but is "removed"?!
* define text search targets (partner/workout, title/description/notes, teacher, ...)
* improve progress tracking, when 100% force a message, don't just stay with 0% and then next step
* BUG: when skip through date, period-indicator grows horizontally! but... there is a "specific unknown condition"!
* parse "bijzonderheden" (they are shown in the app, and sometimes important!)
* use GitHub actions (to verify; maybe even release in the future?!)
* how to get bold font via CSS working?!
* when looking at a workout, for whom i already have a reservation with the same partner, then highlight that!
* only show tooltip if width is > maxWidth and text is cut off
* could catch exception in SyncController (custom dialog?!)
* listen to shortcut for tab selection good enough, yet the key listener could be even more global (e.g. when in textfield "stuck")
* TESTS: OneFitHttpClient, WorkoutInserterImpl, ReservationsSyncerIntegrationTest
* presync all; except workout meta and partner images 2+
* real time sync (?); while syncing, update DB and UI (no restart needed!)
* display location on map
* ad search views: when disabled, make everything transparent; when enabled, make it opaque
* play sound when sync is done (or say something funny like "sync is done master")
* STYLE: padding within htmlviews remove (especially left/right)

### Low Prio

* support offline mode
* tornadofx UI tests
* view replace transition: https://courses.bekwam.net/public_tutorials/bkcourse_tornadofx_replacewithapp.html
* download more partner images (think of migration step)
* sync with google calendar plugin
* reservation button (BIIIIG feature)
* disable quitting during syncing (?)
* luxury: auto release and auto upload (one-click website release)
* ad search views: instead checkbox, make "enable/disable slider a la iphone"
* render distance in km to my place (calculate via google during sync)
* make application start-able also when there is no internet connection
* PROD to DEV copier: delete DEV files, copy over relevant PROD files, change dates for all workouts (add necessary days to all existing data)
* make work parallel fetch delay time dependent on amount of items (e.g. when 10k workouts, then every 5 secs, when 1k workouts, every 2 secs)
* use ktor client retry plugin: https://ktor.io/docs/client-retry.html
* disable closing app while still syncing. display info dialog.
* model dirty check/commit tutorial: https://docs.tornadofx.io/0_subsection/11_editing_models_and_validation

### Nope

* ? disable workout meta fetching (move syncer all in pre-syncer!)
* improve sync dialog: how many partners/workouts (left, report on interval); show progress with progress bar as total amount is known
* improve sync dialog: show report at the end: X foo inserted, Y bar deleted
