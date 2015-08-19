# Timeline for Vaadin History

## 2015-08-xx, v2.1.0

- Updated to vis.js 4.7.0.
- Added support for the click, doubleClick, and contextmenu events.
- Renamed the windowRangeChange and selectionChange events to be consistent with the Timeline API.

## 2015-07-22, v2.0.0

- Removed the delay in sending the rangechanged event to the server. This may result in many more events server side but it improves performance after a simple user drag.
- Updated to vis.js 4.6.0. This is an API breaking change and is not backward compatible. Refer to the vis.js documentation for API changes.
- Changed the client side implementation to use vis.DataSet which may improve performance a little bit when redrawing the timeline with a lot of items.
- Added support for multiple custom time lines.

## 2015-04-21, v1.2.1

- Fixed a bug where the time axis scale was required when it should be an optional configuration parameter.

## 2015-04-09, v1.2.0

- Added support for setting format labels for major/minor labels.
- Added support for "background" item type.
- Added showMajorAxis and showMinorAxis toggles to the demo.
- Upgraded to Vaadin 7.4.3.
- Upgraded to vis.js 3.11.0.
- Added removeAllItems to the BasicItemProvider.
- Upgraded to vis.js 3.3.
- Exposed setCurrentTime method.
- Exposed margin.item.vertical, margin.item.horizontal, clickToUse options.
- Added try/catch to handle an exception thrown when trying to destroy the 
  timeline. See https://github.com/almende/vis/issues/294
- Added delete image and vis.map to the published "stylesheet" files so they 
  are accessible on the client.

## 2014-07-18, v1.1.0

- Upgraded to vis.js 3.0. 
- Added support for the new title field in a timeline item. 
- Removed duplication of the items and groups on the client side because the array bug is fixed.

## 2014-06-20, v1.0.0

- Initial release.