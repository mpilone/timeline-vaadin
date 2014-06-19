
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

  /**
   * The ID of the timer currently running to inform the server side of 
   * a range change.
   * 
   * @type Number
   */
  var rangeChangeTimerId = -1;

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
  this.setCustomTime = function(time) {
    timeline.setCustomTime(new Date(time));

//    rpcProxy.ackSetCurrentTime();
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

  /*
   * Called when the state on the server side changes.
   */
  this.onStateChange = function() {

    var state = this.getState();

    console_log("State change! Items: " + state.items.length);

    // We have to copy the values into a new array because Timeline uses 
    // instanceof to check for an array and it doesn't work for the GWT 
    // created arrays. Stupid JavaScript and broken operations. This should 
    // be fixed in the next version of Timeline (1.2?).
    var items = [];
    for (var i = 0; i < state.items.length; ++i) {
      items.push(state.items[i]);
    }

    var groups = [];
    for (var i = 0; i < state.groups.length; ++i) {
      groups.push(state.groups[i]);
    }

    // We should probably be smarter about updating the groups and items 
    // already in the timeline rather than blindly replacing all the items.
    timeline.setGroups(groups);
    timeline.setItems(items);
    timeline.setOptions(state.options);
  };

  // -----------------------
  // Init component
  this.registerRpc("org.mpilone.vaadin.timeline.shared.TimelineClientRpc", this);

  console_log("Creating timeline.");

  timeline = new vis.Timeline(element, [], {});

  timeline.on('rangechanged', function(evt) {
    if (rangeChangeTimerId !== -1) {
      window.clearTimeout(rangeChangeTimerId);
    }
    rangeChangeTimerId = -1;

    // Delay the call to the server because we only care about the last 
    // update, not all the changes in between. This is similar to the 
    // @Delay annotation on the ServerRpc interface but that isn't supported 
    // in a JavaScript component.
    rangeChangeTimerId = window.setTimeout(function() {
      rpcProxy.rangeChanged(evt.start.getTime(), evt.end.getTime());
      rangeChangeTimerId = -1;
    }, 1000);
  });

  timeline.on('select', function(evt) {
    rpcProxy.select(evt.items);
  });
};