# ToDos

## Version 2

* Days to synchronize future data made configurable via preferences.
* UI improvement: Unified table icons (reserved, wishlisted, favorited, visited) into a single column.

AFTER: user experience session with: serieke, joey

## Backlog

### High Prio

* harden retry logic (use ktor-retry plugin)
* include in available counter: how many workouts reserved in this period for this particular partner (not only past checkins)
* UI BUG: go to partners tab, select partner, select workout, update partner; workout should stay, but is "removed"?!
* define text search targets (partner/workout, title/description/notes, teacher, ...)
* somehow make partners with "open training" more visible
* right click on address, context menu to copy to clipboard
* add version number to app file manifest (macOS)
* download more partner images (think of migration step)
* improve progress tracking, when 100% force a message, don't just stay with 0% and then next step
* BUG: when skip through date, period-indicator grows horizontally! but... there is a "specific unknown condition"!
* parse "bijzonderheden" (they are shown in the app, and sometimes important!)
* use GitHub actions (to verify; maybe even release in the future?!)
* how to get bold font via CSS working?!

### Medium Prio

* view replace transition: https://courses.bekwam.net/public_tutorials/bkcourse_tornadofx_replacewithapp.html
* URL of official website from partner
* link partners together (similar...)
* only show tooltip if width is > maxWidth and text is cut off
* could catch exception in SyncController (custom dialog?!)
* listen to shortcut for tab selection good enough, yet the key listener could be even more global (e.g. when in textfield "stuck")
* TESTS: OneFitHttpClient, WorkoutInserterImpl, ReservationsSyncerIntegrationTest
* presync all; except workout meta and partner images 2+
* real time sync (?); while syncing, update DB and UI (no restart needed!)
* support offline mode
* display location on map
* tornadofx UI tests
* ad search views: when disabled, make everything transparent; when enabled, make it opaque
* play sound when sync is done (or say something funny like "sync is done master")
* STYLE: padding within htmlviews remove (especially left/right)
* BUG? when click on "visited workouts", no details shown?! (also check if exception is displayed!)
* sync available spots

### Low Prio

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

## Guides

### Make a Release

1. make a new local release and smoke test it
2. execute the `bin/prod_release.sh` script
3. create a [new release in github](https://github.com/christophpickl/allfit/releases/new).
   1. select the latest tag
   2. define title as `AllFit Version X`
   3. release notes: copy feature list from todo list
   4. upload both the macOS app (ZIP) and the shadow JAR
   5. hit publish release
4. update the website in the `docs/` folder:
   1. replace the screenshots with current ones
   2. update the version number
   3. change the download links
   4. commit, push, wait
   5. click both links verify it works

### Limit production data

1. Connect directly to H2 DB and execute the following:

```sql
delete
from WORKOUTS
where START < '2023-05-26';

delete
from CHECKINS
where WORKOUT_ID in (select WORKOUT_ID
                     from WORKOUTS
                     where START < '2023-05-26');

delete
from RESERVATIONS
where WORKOUT_ID in (select WORKOUT_ID
                     from WORKOUTS
                     where START < '2023-05-26')
```

2. In the app-config, set the proper dummy date.
