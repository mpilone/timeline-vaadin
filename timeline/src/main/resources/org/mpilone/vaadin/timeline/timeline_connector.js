
/*
 * The entry point into the connector from the Vaadin framework.
 */
org_mpilone_vaadin_timeline_Timeline = function () {

  /*
   *  The root HTML element that represents this component. 
   */
  var element = this.getElement();

  /*
   * The RPC proxy to the server side implementation.
   */
  var rpcProxy = this.getRpcProxy();

  /*
   * The unique ID of the connector.
   */
  var connectorId = this.getConnectorId();

  /**
   * The timeline being displayed.
   * 
   * @type links.Timeline
   */
  var timeline;

  /**
   * The ID of the timer currently running to inform the server side of 
   * a range change.
   * 
   * @type Number
   */
  var rangeChangeTimerId = -1;

  /**
   * The data set containing the groups in the timeline.
   * 
   * @type vis.DataSet
   */
  var groupsDataSet = new vis.DataSet([], {"queue": true});

  /**
   * The data set containing the items in the timeline.
   * 
   * @type vis.DataSet
   */
  var itemsDataSet = new vis.DataSet([], {"queue": true});

  /*
   * Simple method for logging to the JS console if one is available.
   */
  function console_log(msg) {
    if (window.console) {
      console.log(msg);
    }
  }

  /**
   * 
   * @param {type} time
   * @returns {undefined}
   */
  this.setCurrentTime = function (time) {
    timeline.setCurrentTime(new Date(time));
  };

  /**
   * 
   * @param {type} id
   * @returns {undefined}
   */
  this.removeCustomTime = function (id) {
    timeline.removeCustomTime(id);
  };

  /**
   * 
   * @param {type} time
   * @returns {undefined}
   */
  this.addCustomTime = function (time, id) {
    timeline.addCustomTime(new Date(time), id);
  };

  /**
   * 
   * @param {type} time
   * @returns {undefined}
   */
  this.setCustomTime = function (time, id) {
    timeline.setCustomTime(new Date(time), id);
  };

  /**
   * Sets the visible range (zoom) to the specified range.
   * 
   * @param {Number} start the start time in millis
   * @param {Number} end the end time in millis
   * @param {Object} the method options
   * @returns {undefined}
   */
  this.setWindow = function (start, end, options) {
    timeline.setWindow(new Date(start), new Date(end), options);
  };

  /**
   * Move the window such that given time is centered on screen.
   * 
   * @param {Number} time the time to center on
   * @param {Object} the method options
   * @returns {undefined}
   */
  this.moveTo = function (time, options) {
    timeline.moveTo(new Date(time), options);
  };

  /**
   * Adjust the visible window such that the selected item (or multiple 
   * items) are centered on screen.
   * 
   * @param {Number} the ids to focus on
   * @param {Object} the method options
   * @returns {undefined}
   */
  this.focus = function (ids, options) {
    timeline.focus(ids, options);
  };

  /**
   * Adjust the visible window such that it fits all items.
   * 
   * @param {Object} the method options
   * @returns {undefined}
   */
  this.fit = function (options) {
    timeline.fit(options);
  };

  /**
   * Select one or multiple items by their id. The currently selected 
   * items will be unselected. 
   * 
   * @param {Number} the ids to focus on
   * @param {Object} the method options
   * @returns {undefined}
   */
  this.setSelection = function (ids, options) {
    timeline.setSelection(ids, options);

    // The timeline doesn't generate a selection event when explictly 
    // setting the selection so we fake one.
    rpcProxy.select(timeline.getSelection());
  };

  this.setItems = function (items) {
    // Remove the items no longer in the set and update the remaining ones.
    var newIds = [];
    for (var i = 0; i < items.length; i++) {
      newIds.push(items[i].id);
    }
    oldIds = itemsDataSet.getIds();
    for (var i = 0; i < oldIds.length; i++) {
      if (newIds.indexOf(oldIds[i]) < 0) {
        itemsDataSet.remove(oldIds[i]);
      }
    }
    
    // We loop through the items and clone them because Timeline uses 
    // 'instanceof' which fails for GWT created objects. My best guess 
    // is that GWT is creating the objects in a different frame or context 
    // so instanceof doesn't work.
    // See https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/instanceof
    // See https://github.com/almende/vis/issues/1528
    for (var i = 0; i < items.length; i++) {
      itemsDataSet.update(this.shallowClone(items[i]));
    }
    
    itemsDataSet.flush();
  };

  this.setGroups = function (groups) {
    // Remove the groups no longer in the set and update the remaining ones.
    var newIds = [];
    for (var i = 0; i < groups.length; i++) {
      newIds.push(groups[i].id);
    }
    var oldIds = groupsDataSet.getIds();
    for (var i = 0; i < oldIds.length; i++) {
      if (newIds.indexOf(oldIds[i]) < 0) {
        groupsDataSet.remove(oldIds[i]);
      }
    }
    
    // We loop through the items and clone them because Timeline uses 
    // 'instanceof' which fails for GWT created objects. See the same loop in
    // setItems for more details.
    for (var i = 0; i < groups.length; i++) {
      groupsDataSet.update(this.shallowClone(groups[i]));
    }
    groupsDataSet.flush();
  };

  /**
   * Called when the component is removed from the UI.
   * 
   * @returns {undefined}
   */
  this.onUnregister = function () {
    try {
      if (rangeChangeTimerId !== -1) {
        window.clearTimeout(rangeChangeTimerId);
      }

      timeline.destroy();
    }
    catch (ex) {
      // There appears to be a bug in timeline 3.3.0 that causes an exception 
      // to be raised due to the onMoving method.
    }
    rangeChangeTimerId = -1;
    timeline = null;
  };

  /*
   * Called when the state on the server side changes.
   */
  this.onStateChange = function () {

    var state = this.getState();

    console_log("State change!");

    var options = state.options;
    
    // Convert the JS string into a real function.
    if (options.moment) {
      options.moment = Function('return ' + options.moment)();
    }
    
    timeline.setOptions(options);
  };

/**
 * Performs a shallow clone on an object and returns the new object.
 * 
 * @param {Object} obj the object to clone
 * @returns {Object} the new clone
 */
  this.shallowClone = function(obj) {
    var copy = {};
    for (var attr in obj) {
        if (obj.hasOwnProperty(attr)) copy[attr] = obj[attr];
    }
    return copy;  
  };

  // -----------------------
  // Init component
  this.registerRpc("org.mpilone.vaadin.timeline.shared.TimelineClientRpc", this);

  console_log("Creating timeline.");

  timeline = new vis.Timeline(element, itemsDataSet, groupsDataSet, {});

  var that = this;
  timeline.on('rangechanged', function (evt) {
    if (rangeChangeTimerId !== -1) {
      window.clearTimeout(rangeChangeTimerId);
    }
    rangeChangeTimerId = -1;

    if (that.getState().options.zoomable) {
      // Delay the call to the server because we only care about the last 
      // update, not all the changes in between. This is similar to the 
      // @Delay annotation on the ServerRpc interface but that isn't supported 
      // in a JavaScript component.
      rangeChangeTimerId = window.setTimeout(function () {
        console_log("Range changed. Notifying the server.");
        rpcProxy.rangeChanged(evt.start.getTime(), evt.end.getTime(), evt.byUser);
        rangeChangeTimerId = -1;
      }, 250);
    }
    else {
      console_log("Range changed. Notifying the server.");
      rpcProxy.rangeChanged(evt.start.getTime(), evt.end.getTime(), evt.byUser);
    }
  });

  timeline.on('select', function (evt) {
    rpcProxy.select(evt.items);
  });

  timeline.on('click', function (props) {
    // Clear the original event or we get a circular reference error
    // during stringify.
    props.event = null;

    // Vaadin appears to want to map a long to a Date (rather than a JS 
    // or moment.js Date).
    props.time = props.time.valueOf();
    props.snappedTime = props.snappedTime.valueOf();

    rpcProxy.click(props);
  });

  timeline.on('doubleClick', function (props) {
    // Clear the original event or we get a circular reference error
    // during stringify.
    props.event = null;

    // Vaadin appears to want to map a long to a Date (rather than a JS 
    // or moment.js Date).
    props.time = props.time.valueOf();
    props.snappedTime = props.snappedTime.valueOf();

    rpcProxy.doubleClick(props);
  });

  timeline.on('contextmenu', function (props) {
    // Need to make this a property based on if there is a 
    // listener or something.
    props.event.preventDefault();

    // Clear the original event or we get a circular reference error
    // during stringify.
    props.event = null;

    // Vaadin appears to want to map a long to a Date (rather than a JS 
    // or moment.js Date).
    props.time = props.time.valueOf();
    props.snappedTime = props.snappedTime.valueOf();

    rpcProxy.contextmenu(props);
  });
};