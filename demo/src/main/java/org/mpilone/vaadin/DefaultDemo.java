package org.mpilone.vaadin;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import org.mpilone.vaadin.timeline.*;

import com.vaadin.shared.ui.datefield.DateTimeResolution;
import com.vaadin.ui.*;

/**
 *
 * @author mpilone
 */
public class DefaultDemo extends VerticalLayout {

  private final static String[] GROUPS = new String[]{"S001", "S002", "S003",
    "S004", "S005", "S006"};
  private final static String[] PROGRAMS =
      new String[]{"All Things Considered", "Diane Rehm", "Car Talk",
        "Morning Edition", "Radiolab",
        "NPR News"};

  public DefaultDemo() {
    setWidth("100%");
    setMargin(true);
    setSpacing(true);

    List<TimelineGroup> groups = new ArrayList<>(GROUPS.length);
    for (String groupId : GROUPS) {
      groups.add(new BasicTimelineGroup(groupId, groupId, null));
    }

    final Timeline t = new Timeline();
    t.getOptions().setOrientationAxis(TimelineOptions.TimeAxisOrientation.TOP);
    t.getOptions().setType(TimelineOptions.ItemType.RANGE);
    t.setGroups(groups);
    t.setWidth(StyleConstants.FULL_WIDTH);

    TimelineTimeZone.extend(t, "America/New_York");

    // Timeline
    addComponent(t);

    // Control options
    addListeners(t);
    addComponent(buildControls(t));
  }

  private Component buildControls(final Timeline timeline) {
    HorizontalLayout controlLayout = new HorizontalLayout();
    controlLayout.setSpacing(true);
    controlLayout.setWidth("100%");
    addComponent(controlLayout);

    controlLayout.addComponent(buildTimeControls(timeline));
    controlLayout.addComponent(buildToggleControls(timeline));
    controlLayout.addComponent(buildAxisItemControls(timeline));
    controlLayout.addComponent(buildDataControls(timeline));

    return controlLayout;
  }

  private Component buildDataControls(final Timeline timeline) {
    Panel panel = new Panel("Data Controls");

    VerticalLayout controlLayout = new VerticalLayout();
    controlLayout.setSpacing(true);
    controlLayout.setMargin(true);
    panel.setContent(controlLayout);

    // Build items
    final TextField numItemsTxt = new TextField();
    numItemsTxt.setValue("100");
    controlLayout.addComponent(numItemsTxt);

    Button btn = new Button("Rebuild Data", new Button.ClickListener() {
      @Override
      public void buttonClick(Button.ClickEvent event) {
        BasicItemProvider provider = new BasicItemProvider();

        Calendar cal = Calendar.getInstance();
        int numItems = Integer.parseInt(numItemsTxt.getValue());

        for (int i = 0; i < numItems; ++i) {
          BasicTimelineItem evt = new BasicTimelineItem();
          evt.setStart(cal.getTime());
          cal.add(Calendar.MINUTE, (int) (10 + Math.random() * 80));
          evt.setEnd(cal.getTime());
          evt.setContent(PROGRAMS[(int) (Math.random() * 6)]);
          evt.setGroupId(GROUPS[(int) (Math.random() * 6)]);
          evt.setTitle("Go to " + evt.getContent() + " details.");
          provider.addItem(evt);

          cal.setTime(evt.getStart());
          cal.add(Calendar.MINUTE, 10);
        }

        timeline.setItemProvider(provider);
      }
    });
    controlLayout.addComponent(btn);

    btn = new Button("Select Random", new Button.ClickListener() {
      @Override
      public void buttonClick(Button.ClickEvent event) {
        TimelineItemProvider provider = timeline.getItemProvider();
        List<TimelineItem> items = provider.getItems(timeline.getWindow().
            getStart(),
            timeline.getWindow().getEnd());

        int index = (int) (Math.random() * items.size());
        TimelineItem item = items.get(index);
        timeline.setSelection(Arrays.asList(item.getId()),
            new TimelineMethodOptions.SetSelection(true, new Animation()));
      }
    });
    controlLayout.addComponent(btn);

    btn = new Button("Deselect All", new Button.ClickListener() {
      @Override
      public void buttonClick(Button.ClickEvent event) {
        timeline.deselectAll(null);
      }
    });
    controlLayout.addComponent(btn);

    return panel;
  }

  private Component buildTimeControls(final Timeline timeline) {
    Panel panel = new Panel("Window Controls");

    Calendar cal = Calendar.getInstance();

    VerticalLayout controlLayout = new VerticalLayout();
    controlLayout.setSpacing(true);
    controlLayout.setMargin(true);
    panel.setContent(controlLayout);

    final DateTimeField startDt = new DateTimeField();
    startDt.setResolution(DateTimeResolution.SECOND);
    startDt.setValue(LocalDateTime.ofInstant(cal.toInstant(), cal.getTimeZone().toZoneId()));
    controlLayout.addComponent(startDt);

    cal.add(Calendar.HOUR, 8);
    final DateTimeField endDt = new DateTimeField();
    endDt.setResolution(DateTimeResolution.SECOND);
    endDt.setValue(LocalDateTime.ofInstant(cal.toInstant(), cal.getTimeZone().toZoneId()));
    controlLayout.addComponent(endDt);

    Button btn = new Button("Set Window", new Button.ClickListener() {
      @Override
      public void buttonClick(Button.ClickEvent event) {

        Date start = Date.from(startDt.getValue().atZone(cal.getTimeZone().toZoneId()).toInstant());
        Date end = Date.from(endDt.getValue().atZone(cal.getTimeZone().toZoneId()).toInstant());

        timeline.setWindow(start, end,
            new TimelineMethodOptions.SetWindow(new Animation(1000,
                    Animation.EasingFunction.easeInCubic)));
      }
    });
    controlLayout.addComponent(btn);

    btn = new Button("Set Window (now + 4)", new Button.ClickListener() {
      @Override
      public void buttonClick(Button.ClickEvent event) {
        Calendar cal = Calendar.getInstance();
        Date start = cal.getTime();
        cal.add(Calendar.HOUR, 4);
        Date end = cal.getTime();
        timeline.setWindow(start, end, null);
      }
    });
    controlLayout.addComponent(btn);

    final DateTimeField currentTimeDt = new DateTimeField();
    currentTimeDt.setResolution(DateTimeResolution.SECOND);
    currentTimeDt.setValue(LocalDateTime.now());
    controlLayout.addComponent(currentTimeDt);
    
    btn = new Button("Set Current Time", event -> {
          timeline.setCurrentTime(
              Date.from(currentTimeDt.getValue().atZone(ZoneId.systemDefault()).toInstant()));
        });
    controlLayout.addComponent(btn);

    btn = new Button("Add Custom Time", new Button.ClickListener() {
      @Override
      public void buttonClick(Button.ClickEvent event) {
        timeline.addCustomTime(new Date(), UUID.randomUUID().toString());
      }
    });
    controlLayout.addComponent(btn);

    final DateTimeField moveToDt = new DateTimeField();
    moveToDt.setResolution(DateTimeResolution.SECOND);
    moveToDt.setValue(LocalDateTime.now());
    controlLayout.addComponent(moveToDt);

    btn = new Button("Move To", evt -> {
      timeline.moveTo(Date.from(moveToDt.getValue().atZone(ZoneId.systemDefault()).toInstant()),
          new TimelineMethodOptions.MoveTo(
            new Animation(1000, Animation.EasingFunction.easeInOutQuad)));
    });
    controlLayout.addComponent(btn);

    btn = new Button("Fit", evt -> {
        timeline.fit(new TimelineMethodOptions.Fit(new Animation(750,
            Animation.EasingFunction.easeInOutQuint)));
    });
    controlLayout.addComponent(btn);

    // ZoomMin, ZoomMax
    final TextField zoomMin = new TextField();
    zoomMin.setValue(String.valueOf(timeline.getOptions().getZoomMin() / 1000));
    controlLayout.addComponent(zoomMin);

    final TextField zoomMax = new TextField();
    zoomMax.setValue(String.valueOf(timeline.getOptions().getZoomMax() / 1000));
    controlLayout.addComponent(zoomMax);

    btn = new Button("Apply Zoom Min/Max", event -> {
      TimelineOptions options = timeline.getOptions();
      options.setZoomMin(Integer.parseInt(zoomMin.getValue()) * 1000);
      options.setZoomMax(Integer.parseInt(zoomMax.getValue()) * 1000);
    });
    controlLayout.addComponent(btn);

    // Min/max
    final DateTimeField minDt = new DateTimeField();
    minDt.setResolution(DateTimeResolution.SECOND);
    minDt.setValue(LocalDateTime.ofInstant(cal.toInstant(), cal.getTimeZone().toZoneId()));
    controlLayout.addComponent(minDt);

    cal.add(Calendar.HOUR, 8);
    final DateTimeField maxDt = new DateTimeField();
    maxDt.setResolution(DateTimeResolution.SECOND);
    maxDt.setValue(LocalDateTime.ofInstant(cal.toInstant(), cal.getTimeZone().toZoneId()));
    controlLayout.addComponent(maxDt);

    btn = new Button("Apply Min/Max", new Button.ClickListener() {
      @Override
      public void buttonClick(Button.ClickEvent event) {
        TimelineOptions options = timeline.getOptions();
        options.setMin(Date.from(minDt.getValue().atZone(cal.getTimeZone().toZoneId()).toInstant()));
        options
            .setMax(Date.from(maxDt.getValue().atZone(cal.getTimeZone().toZoneId()).toInstant()));
      }
    });
    controlLayout.addComponent(btn);

    final ComboBox timeZoneCmb = new ComboBox();
    timeZoneCmb.setItems("Africa/Cairo", "America/New_York", "America/Chicago",
        "America/Los_Angeles", "Asia/Hong_Kong", "Asia/Seoul", "Etc/UTC", "Europe/London",
        "Europe/Paris");
    timeZoneCmb.setEmptySelectionAllowed(true);
    controlLayout.addComponent(timeZoneCmb);

    final Button tzBtn = new Button("Apply Time Zone");
    tzBtn.addClickListener(new Button.ClickListener() {
      @Override
      public void buttonClick(Button.ClickEvent event) {
        if (tzBtn.getData() != null) {
          TimelineTimeZone e = (TimelineTimeZone) tzBtn.getData();
          e.remove();
          tzBtn.setData(null);
          timeline.getOptions().setMoment(null);
        }

        if (timeZoneCmb.getValue() != null) {
          TimelineTimeZone e = TimelineTimeZone.extend(timeline,
              (String) timeZoneCmb.getValue());
          tzBtn.setData(e);
        }
      }
    });
    controlLayout.addComponent(tzBtn);

    return panel;
  }

  private Component buildAxisItemControls(final Timeline timeline) {

    Panel panel = new Panel("Axis/Item Controls");

    VerticalLayout controlLayout = new VerticalLayout();
    controlLayout.setSpacing(true);
    controlLayout.setMargin(true);
    panel.setContent(controlLayout);

    // Item type
    final ComboBox typeCmb = new ComboBox("Item Type");
    typeCmb.setEmptySelectionAllowed(false);
    typeCmb.setTextInputAllowed(false);
    typeCmb.setItems(TimelineOptions.ItemType.values());
    typeCmb.setValue(timeline.getOptions().getType());
    typeCmb.addSelectionListener(evt -> {
      timeline.getOptions().setType((TimelineOptions.ItemType) evt.getValue());
    });
    controlLayout.addComponent(typeCmb);

    // Item alignment
    final ComboBox alignCmb = new ComboBox("Item Alignment");
    alignCmb.setEmptySelectionAllowed(false);
    alignCmb.setTextInputAllowed(false);
    alignCmb.setItems(TimelineOptions.ItemAlignment.values());
    alignCmb.setValue(timeline.getOptions().getAlign());
    alignCmb.addSelectionListener(evt -> {
      timeline.getOptions().setAlign((TimelineOptions.ItemAlignment) evt.getValue());
    });
    controlLayout.addComponent(alignCmb);

    // Item orientation
    final ComboBox itemOrientCmb = new ComboBox("Item Orientation");
    itemOrientCmb.setEmptySelectionAllowed(false);
    itemOrientCmb.setTextInputAllowed(false);
    itemOrientCmb.setItems(TimelineOptions.ItemOrientation.values());
    itemOrientCmb.setValue(TimelineOptions.ItemOrientation.valueOf(
        timeline.getOptions().getOrientation().item.toUpperCase()));
    itemOrientCmb.addSelectionListener(evt -> {
        timeline.getOptions().setOrientationItem(
          (TimelineOptions.ItemOrientation) evt.getValue());
    });
    controlLayout.addComponent(itemOrientCmb);

    // Axis orientation
    final ComboBox axisOrientCmb = new ComboBox("Axis Orientation");
    axisOrientCmb.setEmptySelectionAllowed(false);
    axisOrientCmb.setTextInputAllowed(false);
    axisOrientCmb.setItems(TimelineOptions.TimeAxisOrientation.values());
    axisOrientCmb.setValue(TimelineOptions.TimeAxisOrientation.valueOf(
        timeline.getOptions().getOrientation().axis.toUpperCase()));

    axisOrientCmb.addSelectionListener(evt -> {
        timeline.getOptions().setOrientationAxis(
          (TimelineOptions.TimeAxisOrientation) evt.getValue());
    });
    controlLayout.addComponent(axisOrientCmb);

    Button btn = new Button("Toggle Visible", new Button.ClickListener() {
      @Override
      public void buttonClick(Button.ClickEvent event) {
        timeline.setVisible(!timeline.isVisible());
      }
    });
    controlLayout.addComponent(btn);

    return panel;
  }

  /**
   * Builds the toggle controls.
   *
   * @param timeline the timeline to control
   *
   * @return the container component
   */
  private Component buildToggleControls(final Timeline timeline) {
    Panel panel = new Panel("Toggle Controls");

    VerticalLayout controlLayout = new VerticalLayout();
    controlLayout.setSpacing(true);
    controlLayout.setMargin(true);
    panel.setContent(controlLayout);

    CheckBox chk = new CheckBox("Read-only");
    // chk.setValue(timeline.isReadOnly());
    // chk.addValueChangeListener(evt -> {
    // timeline.setReadOnly(evt.getValue());
    // });
    // controlLayout.addComponent(chk);

    chk = new CheckBox("Edit Update Time");
    chk.setValue(timeline.getOptions().getEditable().updateTime);
    chk.addValueChangeListener(evt -> {
        TimelineOptions options = timeline.getOptions();
      options.setEditableUpdateTime(evt.getValue());
    });
    controlLayout.addComponent(chk);

    chk = new CheckBox("Edit Update Group");
    chk.setValue(timeline.getOptions().getEditable().updateGroup);
    chk.addValueChangeListener(evt -> {
        TimelineOptions options = timeline.getOptions();
      options.setEditableUpdateGroup(evt.getValue());
    });
    controlLayout.addComponent(chk);

    chk = new CheckBox("Edit Add");
    chk.setValue(timeline.getOptions().getEditable().add);
    chk.addValueChangeListener(evt -> {
        TimelineOptions options = timeline.getOptions();
      options.setEditableAdd(evt.getValue());
    });
    controlLayout.addComponent(chk);

    chk = new CheckBox("Edit Remove");
    chk.setValue(timeline.getOptions().getEditable().remove);
    chk.addValueChangeListener(evt -> {
        TimelineOptions options = timeline.getOptions();
      options.setEditableRemove(evt.getValue());
    });
    controlLayout.addComponent(chk);

    chk = new CheckBox("Selectable");
    chk.setValue(timeline.getOptions().isSelectable());
    chk.addValueChangeListener(evt -> {
        TimelineOptions options = timeline.getOptions();
      options.setSelectable(evt.getValue());
    });
    controlLayout.addComponent(chk);

    chk = new CheckBox("Multiselect");
    chk.setValue(timeline.getOptions().isMultiselect());
    chk.addValueChangeListener(evt -> {
        TimelineOptions options = timeline.getOptions();
      options.setMultiselect(evt.getValue());
    });
    controlLayout.addComponent(chk);

    chk = new CheckBox("Show Current Time");
    chk.setValue(timeline.getOptions().isShowCurrentTime());
    chk.addValueChangeListener(evt -> {
        TimelineOptions options = timeline.getOptions();
      options.setShowCurrentTime(evt.getValue());
    });
    controlLayout.addComponent(chk);

    chk = new CheckBox("Movable");
    chk.setValue(timeline.getOptions().isMoveable());
    chk.addValueChangeListener(evt -> {
        TimelineOptions options = timeline.getOptions();
      options.setMoveable(evt.getValue());
    });
    controlLayout.addComponent(chk);

    chk = new CheckBox("Zoomable");
    chk.setValue(timeline.getOptions().isZoomable());
    chk.addValueChangeListener(evt -> {
        TimelineOptions options = timeline.getOptions();
      options.setZoomable(evt.getValue());
    });
    controlLayout.addComponent(chk);

    chk = new CheckBox("Click to Use");
    chk.setValue(timeline.getOptions().isClickToUse());
    chk.addValueChangeListener(evt -> {
        TimelineOptions options = timeline.getOptions();
      options.setClickToUse(evt.getValue());
    });
    controlLayout.addComponent(chk);

    chk = new CheckBox("Show Major Labels");
    chk.setValue(timeline.getOptions().isClickToUse());
    chk.addValueChangeListener(evt -> {
        TimelineOptions options = timeline.getOptions();
      options.setShowMajorLabels(evt.getValue());
    });
    controlLayout.addComponent(chk);

    chk = new CheckBox("Show Minor Labels");
    chk.setValue(timeline.getOptions().isClickToUse());
    chk.addValueChangeListener(evt -> {
        TimelineOptions options = timeline.getOptions();
      options.setShowMinorLabels(evt.getValue());
    });
    controlLayout.addComponent(chk);

    return panel;
  }

  /**
   * Adds a number of listeners to the timeline to display user interactions.
   *
   * @param t the timeline to add the listeners to
   */
  private void addListeners(Timeline t) {
    t.addSelectListener(new SelectListener() {
      @Override
      public void select(SelectListener.SelectEvent evt) {
        Notification n = new Notification("Select", evt.toString(),
            Notification.Type.TRAY_NOTIFICATION);
        n.setDelayMsec(4000);
        n.show(getUI().getPage());

        System.out.println("Select: " + evt.toString());
      }
    });
    t.addRangeChangedListener(new RangeChangedListener() {
      @Override
      public void rangeChanged(RangeChangedListener.RangeChangedEvent evt) {
        Notification n = new Notification("Range Changed", evt.toString(),
            Notification.Type.TRAY_NOTIFICATION);
        n.setDelayMsec(3500);
        n.show(getUI().getPage());

        System.out.println("Range Changed: " + evt.toString());
      }
    });
    t.addClickListener(new ClickListener() {
      @Override
      public void click(ClickListener.ClickEvent evt) {
        Notification n = new Notification("Click", evt.toString(),
            Notification.Type.TRAY_NOTIFICATION);
        n.setDelayMsec(3000);
        n.show(getUI().getPage());
      }
    });
    t.addDoubleClickListener(new DoubleClickListener() {
      @Override
      public void doubleClick(DoubleClickListener.DoubleClickEvent evt) {
        Notification n = new Notification("Double Click", evt.toString(),
            Notification.Type.TRAY_NOTIFICATION);
        n.setDelayMsec(2500);
        n.show(getUI().getPage());
      }
    });
    t.addContextMenuListener(new ContextMenuListener() {
      @Override
      public void contextMenu(ContextMenuListener.ContextMenuEvent evt) {
        Notification n = new Notification("Context Menu", evt.toString(),
            Notification.Type.TRAY_NOTIFICATION);
        n.setDelayMsec(2000);
        n.show(getUI().getPage());
      }
    });
  }

}
