
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
};