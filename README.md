# AllFit

A desktop client for OneFit with additional metadata (notes, personal rating, visibility...).

## ToDos

### High Prio

* bug: check whether "visited workouts" works properly
* sync while application is running (don't block startup); keep data in memory, and reconnect database to insert
* new window: list of partner (filters applicable, e.g. yet visited)
* isHidden, right click, for partner only! plus: in menu bar, enable "is hidden visible YES/NO"; with right click in table, can be made hidden/unhidden (plus make filterable) ... or a custom dialog for it?! in menu bar, "Manage hidden partners"

### Medium

* make work parallel fetch delay time dependent on amount of items (e.g. when 10k workouts, then every 5 secs, when 1k workouts, every 2 secs)
* adjust column sizes in table
* UI any clickable element with blue rounded background color
* improve sync dialog: how many partners/workouts (left, report on interval); show progress with progress bar as total amount is known
* PROD to DEV copier: delete DEV files, copy over relevant PROD files, change dates for all workouts (add necessary days to all existing data)
* fetch usage: https://api.one.fit/v2/en-nl/members/usage; display how many credits in total/left (date range until; how many days left)
* in sync dialog, show report at the end: X foo inserted, Y bar deleted
* get sure, sync also updates checkins/reservations; besides updating spots left (necessary for registration)

### Future

* sync available spots
* reservation button
* model dirty check/commit tutorial: https://docs.tornadofx.io/0_subsection/11_editing_models_and_validation

### Going public

* ad credentials loader: encrypt password (using local username); prompt dialog if file doesn't exist
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