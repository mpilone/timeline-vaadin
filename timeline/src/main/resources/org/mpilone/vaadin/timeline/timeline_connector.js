
/*
 * The entry point into the connector from the Vaadin framework.
 */
org_mpilone_vaadin_timeline_Timeline = function() {

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
  
  var groupsDataSet = new vis.DataSet([], { "queue": true });
  var itemsDataSet = new vis.DataSet([], { "queue": true } );

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
  this.setCurrentTime = function(time) {
    timeline.setCurrentTime(new Date(time));
  };

/**
   * 
   * @param {type} id
   * @returns {undefined}
   */
  this.removeCustomTime = function(id) {
    timeline.removeCustomTime(id);
  };

  /**
   * 
   * @param {type} time
   * @returns {undefined}
   */
  this.addCustomTime = function(time, id) {
    timeline.addCustomTime(new Date(time), id);
  };
  
  /**
   * 
   * @param {type} time
   * @returns {undefined}
   */
  this.setCustomTime = function(time, id) {
    timeline.setCustomTime(new Date(time), id);
  };

  /**
   * Sets the visible range (zoom) to the specified range.
   * 
   * @param {Number} start the start time in millis
   * @param {Number} end the end time in millis
   * @returns {undefined}
   */
  this.setWindow = function(start, end) {
    timeline.setWindow(new Date(start), new Date(end));
  };
  
  /**
   * Called when the component is removed from the UI.
   * 
   * @returns {undefined}
   */
  this.onUnregister = function() {
    try {
      timeline.destroy();
    }
    catch (ex) {
      // There appears to be a bug in timeline 3.3.0 that causes an exception 
      // to be raised due to the onMoving method.
    }
    timeline = null;
  };

  /*
   * Called when the state on the server side changes.
   */
  this.onStateChange = function() {

    var state = this.getState();

    console_log("State change! Items: " + state.items.length);

    var items = state.items; 
    var groups = state.groups;
    var options = state.options;

    // We should probably be smarter about updating the groups and items 
    // already in the timeline rather than blindly replacing all the items.
    timeline.setOptions(options);
    
    // Remove the groups no longer in the set and update the remaining ones.
    var newIds = [];
    for (var i = 0; i < groups.length; i++) {
      newIds.push(groups[i].id);
    }
    var oldIds = groupsDataSet.getIds();
    for (var i = 0; i < oldIds; i++) {
      if (newIds.indexOf(oldIds[i]) < 0) {
        groupsDataSet.remove(oldIds[i]);
      }
    }
    groupsDataSet.update(groups);
    groupsDataSet.flush();
    
    // Remove the items no longer in the set and update the remaining ones.
    newIds = [];
    for (var i = 0; i < items.length; i++) {
      newIds.push(items[i].id);
    }
    oldIds = itemsDataSet.getIds();
    for (var i = 0; i < oldIds.length; i++) {
      if (newIds.indexOf(oldIds[i]) < 0) {
        itemsDataSet.remove(oldIds[i]);
      }
    }
    itemsDataSet.update(items);
    itemsDataSet.flush();
  };

  // -----------------------
  // Init component
  this.registerRpc("org.mpilone.vaadin.timeline.shared.TimelineClientRpc", this);

  console_log("Creating timeline.");

  timeline = new vis.Timeline(element, itemsDataSet, groupsDataSet, {});

  timeline.on('rangechanged', function(evt) {
    console_log("Range changed. Notifying the server.");
      rpcProxy.rangeChanged(evt.start.getTime(), evt.end.getTime(), evt.byUser);
  });

  timeline.on('select', function(evt) {
    rpcProxy.select(evt.items);
  });
  
  timeline.on('click', function(props) {
    // Clear the original event or we get a circular reference error
    // during stringify.
    props.event = null;
    
    rpcProxy.click(props);
  });
  
  timeline.on('doubleClick', function(props) {
    // Clear the original event or we get a circular reference error
    // during stringify.
    props.event = null;
    
    rpcProxy.doubleClick(props);
  });
  
  timeline.on('contextmenu', function(props) {
    // Need to make this a property based on if there is a 
    // listener or something.
    props.event.preventDefault();
    
    // Clear the original event or we get a circular reference error
    // during stringify.
    props.event = null;
    
    rpcProxy.contextmenu(props);
  });
};