## Guides

### Make a Release

1. run all tests to ensure build will go through
2. make a new local release and smoke test it: `bin/local_release.sh`
3. execute the `bin/prod_release.sh` script
4. create a [new release in github](https://github.com/christophpickl/allfit/releases/new).
   1. select the latest tag
   2. define title as `AllFit Version X`
   3. release notes: copy feature list from todo list
   4. upload both the macOS app (ZIP) and the shadow JAR
   5. hit publish release
5. update the website in the `docs/` folder:
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
