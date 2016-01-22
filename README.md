# Timeline for Vaadin

A [Vaadin](https://vaadin.com/home) component for the 
[vis.js Timeline](http://visjs.org/) visualization component.

The timeline is easy to use and follows similar patterns to Vaadin's
Calendar component.

    // Create item groups (if using groups).
    List<TimelineGroup> groups = new ArrayList<>();
    // ...

    // Create the item provider. A basic item provider can be used with a 
    // static list of items or a more advanced provider could be implemented for 
    // direct DB access of timeline events.
    BasicItemProvider provider = new BasicItemProvider();

    // Create the timeline.
    Timeline t = new Timeline();
    t.getOptions().setOrientation(TimelineOptions.TimeAxisOrientation.TOP);
    t.getOptions().setType(TimelineOptions.ItemType.RANGEOVERFLOW);
    t.setItemProvider(provider);
    t.setGroups(groups);

    // Add it to the UI.
    layout.addComponent(t);

## Demo

The demo can be executed with the following commands:

  - git clone repo_url
  - cd timeline-vaadin
  - mvn clean install
  - mvn -pl :timeline-vaadin-demo jetty:run

You can then view the demo by going to 
`http://localhost:8090/timeline-vaadin-demo/app/`.

### Demo Screenshot

![Demo Screenshot](/docs/Timeline_for_Vaadin_Demo.png?raw=true "Demo Screenshot")

## Not Implemented Yet

  - Events on item move, update, add, remove
