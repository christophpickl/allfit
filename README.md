# AllFit

A desktop OneFit client with metadata.

## ToDos

* rework Dbos: make them only reference foreign key (not whole object)
    - domain has two versions, e.g. Categories and CategoriesWithPartners; Partner (only with category names flattened)
* make domain objects things var which are changeable (e.g. rating, favorited, ignored, visits, notes, ...)
* UI has data storage in different flavors: filterable has resettable has cacheable
* UI has on top right corner (above partners) current reservations
* UI any clickable element with blue rounded background color
* UI filter panel use widgets (not plain text), with enable-sliding-checkbox