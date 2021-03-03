# Timeline for Vaadin History

## 2021-03-02, v3.0.1

- Updated moment timezone data.

## 2016-06-13, v2.5.0

- Added support for item orientation separate from axis orientation. (API breaking change)
- Added support for "none" and "both" axis orientations.
- Modified the editable option to be more consistent with other nested options. (API breaking change)
- Updated the demo to include item and axis orientation selection.

## 2016-06-09, v2.4.2

- Added support for the AUTO item alignment and changed the default to match the new Timeline default.

## 2016-06-03, v2.4.1

- Fixed an issue where the timeline could render with the default window range (Jan 1, 1970 - Jan 1, 1970) if setWindow wsn't called before the initial response to the client.
- Groups, items, and window is now only sent once in the response to the client rather than each time the setter is called on the server.
- Removed vis.map from the published files on the component because it is only needed for debugging.

## 2016-05-10, v2.4.0

- Updated to vis.js 4.16.1
- Added support for the 'moment' configuration option to set a moment constructor function.
- Added TimelineTimeZone extension to support time zones in the moment configuration option.
- Added a time zone selector to the demo.

## 2016-02-02, v2.3.0

- Updated to vis.js 4.13.0.
- Added support for order, style, subgroupOrder, and title properties on a group. (API breaking change)
- Added support for style and subgroupId properties on an item. (API breaking change)
- Fixed a bug where the window range would reset to the default if the timeline visibility was toggled.

## 2016-01-22, v2.2.0

- Updated to vis.js 4.12.0.
- Fixed a NPE if groups were not set before the initial send to the client.
- Removed the getWindowStart/getWindowEnd methods and added getWindow to be consistent with the Timeline API. (API breaking change)
- Changed the behavior to only set the window state on the Timeline after the range change event is received from the client. This ensures a range change event whenever setWindow is called and the window range is modified even on the initial setup of the timeline.

## 2015-10-09, v2.1.2

- Modified the margin options to support null margin value.
- Fixed a bug where replacing the groups would only add groups and never remove the existing ones.

## 2015-08-24, v2.1.1

- Fixed a bug where toggling the visibility of the timeline caused the groups and items to disappear.

## 2015-08-21, v2.1.0

- Updated to vis.js 4.7.0.
- Added support for the click, doubleClick, and contextmenu events.
- Renamed the windowRangeChange and selectionChange events to be consistent with the Timeline API.
- Added new editable property to the TimelineItem.
- Added support for the multiselect configuration option.
- Added support for the fit, focus, and moveTo operations. Added method options support to setWindow.
- Implemented server side selection.
- More optimizations on item and group updates.
- Reintroduced the delay in sending the rangechanged event to the server but only if zoomable is enabled. Simple user drag events should therefore be immediate but when zooming is enabled the delay is used to prevent a flood of server side events.
- Switched to the Valo theme in the demo and updated the layout to make it a little more organized.

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