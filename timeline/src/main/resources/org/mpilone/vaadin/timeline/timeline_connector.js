
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

    rpcProxy.ackSetCurrentTime();
  };

  /**
   * Sets the visible range (zoom) to the specified range.
   * 
   * @param {Number} start the start time in millis or -1
   * @param {Number} end the end time in millis or -1
   * @returns {undefined}
   */
  this.setVisibleChartRange = function(start, end) {
    var startDate = start === -1 ? null : new Date(start);
    var endDate = end === -1 ? null : new Date(end);
    
    timeline.setVisibleChartRange(startDate, endDate);
  };
  
  /**
   * Move the visible range such that the current time is located in the 
   * center of the timeline. 
   * 
   * @returns {undefined}
   */
  this.setVisibleChartRangeNow = function() {
    timeline.setVisibleChartRangeNow();
    
    var range = timeline.getVisibleChartRange();
    
    // Notify the server that the range changed. We have to do that here 
    // because the range is calculated dynamically on the client side.
    rpcProxy.rangeChanged(range.start.getTime(), range.end.getTime());
  };

  /*
   * Called when the state on the server side changes.
   */
  this.onStateChange = function() {

    var state = this.getState();

    console_log("State change! Events: " + state.events.length);

    var options = {
      "axisOnTop": true,
      "editable": !state.readOnly,
      "groupsOrder": true,
      "height": "auto",
      "moveable": state.moveable,
      "selectable": state.selectable,
      "showCurrentTime": state.showCurrentTime,
      "style": "box",
      "width": "100%",
      "zoomable": state.zoomable,
      "zoomMax": state.zoomMax,
      "zoomMin": state.zoomMin
    };

    timeline.draw(state.events, options);
  };

  // -----------------------
  // Init component
  this.registerRpc("org.mpilone.vaadin.timeline.shared.TimelineClientRpc", this);

  console_log("Creating timeline.");

  timeline = new links.Timeline(element);
  
  links.events.addListener(timeline, 'rangechanged', function(evt) {
    rpcProxy.rangeChanged(evt.start.getTime(), evt.end.getTime());
  });
  
  links.events.addListener(timeline, 'select', function() {
    var selected = timeline.getSelection();
    if (selected && selected.length >= 1) {
      rpcProxy.select(selected[0].row);
    }
    else {
      rpcProxy.select(-1);
    }
  });
  
  links.events.addListener(timeline, 'add', function() {
    // Add is not currently supported.
    timeline.cancelAdd();
  });
};