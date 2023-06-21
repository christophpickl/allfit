# AllFit

A desktop client for OneFit with additional metadata (notes, personal rating, visibility...).

## ToDos

### High Prio

* filter workouts: future/past/both (radio buttons)
* filter for partners table: only checkouts > 0

### Medium

* remember window size/position after next startup
* make black font for dates in table white (as other cells) when selected
* make address selectable text (PLUS: clickable link to google maps, for now)
* get sure, sync also updates checkins/reservations; besides updating spots left (necessary for registration)
* BUG? when click on "visited workouts", no details shown?! (also check if exception is displayed!)
* search-filter for rating
* adjust column sizes in table

### Low

* disable closing app while still syncing. display info dialog.
* tornadofx UI tests
* ad workout details (right hand side): use up full vertical space (make some parts grow; notes)
* UI any clickable element with blue rounded background color
* render distance in km to my place (calculate via google during sync)
* make application start-able also when there is no internet connection
* PROD to DEV copier: delete DEV files, copy over relevant PROD files, change dates for all workouts (add necessary days to all existing data)
* partners window, filter capability
* make work parallel fetch delay time dependent on amount of items (e.g. when 10k workouts, then every 5 secs, when 1k workouts, every 2 secs)

### Nope

* improve sync dialog: how many partners/workouts (left, report on interval); show progress with progress bar as total amount is known
* improve sync dialog: show report at the end: X foo inserted, Y bar deleted

### Future

* display location on map
* display more (all) pictures
* sync available spots
* reservation button
* model dirty check/commit tutorial: https://docs.tornadofx.io/0_subsection/11_editing_models_and_validation

### Going Public

* ad credentials loader: encrypt password (using local username?); prompt dialog if file doesn't exist
* release on tag, deploy JAR file

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