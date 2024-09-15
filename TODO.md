# ToDos

## Version History

### Version 7

* ...

## Backlog

### High Prio

* BUGFIX: the app.zip should only contain the app file (not the build/artifacts folders)
* BUGFIX: wrong workout reservation icon when scrolling.
* BUGFIX: wrong workout background row color when scrolling.
* define text search targets (partner/workout, title/description/notes, teacher, ...)

### Medium Prio

* when looking at a workout, for whom i already have a reservation with the same partner, then highlight that!
* ability to group/link partners together
* harden retry logic (use ktor-retry plugin)
* include in available counter: how many workouts reserved in this period for this particular partner (not only past
  checkins)
* how to get bold font via CSS working?!
* UI BUG: go to partners tab, select partner, select workout, update partner; workout should stay, but is "removed"?!
* use GitHub actions (to verify; maybe even release in the future?!)
* only show tooltip if width is > maxWidth and text is cut off
* could catch exception in SyncController (custom dialog?!)
* listen to shortcut for tab selection good enough, yet the key listener could be even more global (e.g. when in
  textfield "stuck")
* play sound when sync is done (or say something funny like "sync is done master")
* TESTS: OneFitHttpClient, WorkoutInserterImpl, ReservationsSyncerIntegrationTest
* presync all; except workout meta and partner images 2+
* display location on map
* ad search views: when disabled, make everything transparent; when enabled, make it opaque
* STYLE: padding within htmlviews remove (especially left/right)
* real time sync (?); while syncing, update DB and UI (no restart needed!)
* sync available spots (need to sync ALL workouts; yet gladly without metadata)
* Given table row selected with wishlist/favorite, When table loses focus => Then change bg color (like default
  behavior)

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
* PROD to DEV copier: delete DEV files, copy over relevant PROD files, change dates for all workouts (add necessary days
  to all existing data)
* make work parallel fetch delay time dependent on amount of items (e.g. when 10k workouts, then every 5 secs, when 1k
  workouts, every 2 secs)
* use ktor client retry plugin: https://ktor.io/docs/client-retry.html
* disable closing app while still syncing. display info dialog.
* model dirty check/commit tutorial: https://docs.tornadofx.io/0_subsection/11_editing_models_and_validation

### Nope

* ? disable workout meta fetching (move syncer all in pre-syncer!)
* improve sync dialog: how many partners/workouts (left, report on interval); show progress with progress bar as total
  amount is known
* improve sync dialog: show report at the end: X foo inserted, Y bar deleted
