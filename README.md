# AllFit

A desktop OneFit client with metadata.

## ToDos

### Sync / Persistence

* download image during sync
* for more workout data (e.g. description), need to parse HTML
  * e.g. from: https://one.fit/en-nl/workouts/11002448/vondelgym-zuid-fitness-vegym-training
* ? for partner: primaryCategory vs additionalCategories?

### Domain

* make domain objects things var which are changeable (e.g. rating, favorited, ignored, visits, notes, ...)

### UI related

* UI has data storage in different flavors: filterable has resettable has cacheable
* UI has on top right corner (above partners) current reservations
* UI any clickable element with blue rounded background color
* UI filter panel use widgets (not plain text), with enable-sliding-checkbox