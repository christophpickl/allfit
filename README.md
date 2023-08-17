# AllFit

A desktop client for OneFit with additional metadata (notes, personal rating, visibility...).

https://christophpickl.github.io/allfit/

## ToDos

### High Prio

* indicate how many workouts reserved in this period for this particular partner
* remove workout images from table; load them lazily cached; increase sync to 14 days
    * or: load workouts 14 days in advance (approx 20k) => needs lazy image loading (dynamic load, otherwise heap error!)
* define text search targets (partner/workout, title/description/notes, teacher, ...)

### Going Public

* !!! notification for new version available (like done back then with gadsu)
* !! when auth fails, allow user to rewrite credentials
* pimp release script: macos zip, shadowjar zip (maybe windows exe zip?)

### Medium

* luxury: auto release and auto upload (one-click website release)
* BUG? when click on "visited workouts", no details shown?! (also check if exception is displayed!)
* sync available spots
* reservation button
* disable quitting during syncing (?)
* tornadofx UI tests
* use GitHub actions (to verify; maybe even release in the future?!)
* display location on map

### Low

* render distance in km to my place (calculate via google during sync)
* make application start-able also when there is no internet connection
* PROD to DEV copier: delete DEV files, copy over relevant PROD files, change dates for all workouts (add necessary days to all existing data)
* make work parallel fetch delay time dependent on amount of items (e.g. when 10k workouts, then every 5 secs, when 1k workouts, every 2 secs)
* use ktor client retry plugin: https://ktor.io/docs/client-retry.html
* disable closing app while still syncing. display info dialog.

### Nope

* improve sync dialog: how many partners/workouts (left, report on interval); show progress with progress bar as total amount is known
* improve sync dialog: show report at the end: X foo inserted, Y bar deleted

### Styling

* UI any clickable element with blue rounded background color
* https://docs.oracle.com/javase/8/javafx/user-interface-tutorial/custom.htm

### Future

* display more (all) pictures
* model dirty check/commit tutorial: https://docs.tornadofx.io/0_subsection/11_editing_models_and_validation

## Guides

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

### Resources

* Layout help: https://docs.tornadofx.io/0_subsection/7_layouts_and_menus