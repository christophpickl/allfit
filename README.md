# AllFit

A desktop client for OneFit with additional metadata (notes, personal rating, visibility...).

## ToDos

### High Prio

* make sure all persisted fields are displayed in UI (compare entities with view models)
* sync in progress dialog (mockup with delay; only update message string)
* dry run with prod data

### Medium

* isHidden, right click, for partner only!
  * plus: in menu bar, enable "is hidden visible YES/NO"; with right click in table, can be made hidden/unhidden (plus
    make filterable) ... or a custom dialog for it?! in menu bar, "Manage hidden partners"
* adjust column sizes in table
* bigger images-pictures in detail view
* log file writer for PROD env
* make domain objects things var which are changeable (e.g. rating, favorited, ignored, visits, notes, ...)
* UI any clickable element with blue rounded background color

### Future

* sync available spots
* display how many credits in total/left (date range until; how many days left)
* reservation button

### Going public

* ad credentials loader: encrypt password (using local username); prompt dialog if file doesn't exist
* release on tag, deploy JAR file