
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

    console_log("State change! Events: " + state.items.length);

//    var options = {
//      "editable": {
//        "add": false,
//        "updateTime": !state.readOnly && state.updateTime,
//        "updateGroup": !state.readOnly && state.updateGroup,
//        "remove": false
//      },
////      "groupOrder": "caption",
//      "orientation": "top",
//      "selectable": state.selectable,
//      "showCurrentTime": state.showCurrentTime,
//      "showCustomTime": state.showCustomTime,
//      "type": "rangeoverflow",
//      "width": "100%",
//      "zoomMax": state.zoomMax,
//      "zoomMin": state.zoomMin
//    };

    // We have to copy the values into a new array because Timeline uses 
    // instanceof to check for an array and it doesn't work for the GWT 
    // created arrays. Stupid JavaScript and broken operations.
    var items = [];
    for (var i = 0; i < state.items.length; ++i) {
      items.push(state.items[i]);
    }
    
    var groups = [];
    for (var i = 0; i < state.groups.length; ++i) {
      groups.push(state.groups[i]);
    }

try {
    timeline.setGroups(groups);
    timeline.setItems(items);
    timeline.setOptions(state.options);
  }
  catch (ex) {
    alert("Got here 2!");
  }
  };

  // -----------------------
  // Init component
  this.registerRpc("org.mpilone.vaadin.timeline.shared.TimelineClientRpc", this);

  console_log("Creating timeline.");

try {
  timeline = new vis.Timeline(element, [], {});


   timeline.on('rangechanged', function(evt) {
    rpcProxy.rangeChanged(evt.start.getTime(), evt.end.getTime());
  });
  
  timeline.on('select', function(evt) {
      rpcProxy.select(evt.items);
  });
  
  }
catch (ex) {
alert("Got here 1");  
}
};