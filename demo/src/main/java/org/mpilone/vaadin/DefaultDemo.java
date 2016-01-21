package org.mpilone.vaadin;

import java.util.*;
import java.util.Calendar;

import org.mpilone.vaadin.timeline.*;

import com.vaadin.data.Property;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.datefield.Resolution;
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
    t.getOptions().setOrientation(TimelineOptions.TimeAxisOrientation.TOP);
    t.getOptions().setType(TimelineOptions.ItemType.RANGE);
    t.setGroups(groups);
    t.setWidth(StyleConstants.FULL_WIDTH);

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
    controlLayout.addComponent(buildZoomControls(timeline));
    controlLayout.addComponent(buildDataControls(timeline));

    return controlLayout;
  }

  private Component buildDataControls(final Timeline timeline) {
    Panel panel = new Panel("Data Controls");

    VerticalLayout controlLayout = new VerticalLayout();
    controlLayout.setSpacing(true);
    controlLayout.setMargin(true);
    panel.setContent(controlLayout);

    // Item type
    final ComboBox typeCmb = new ComboBox("Item Type");
    typeCmb.setNullSelectionAllowed(false);
    typeCmb.setImmediate(true);
    typeCmb.setFilteringMode(FilteringMode.OFF);
    for (TimelineOptions.ItemType itemType : TimelineOptions.ItemType.values()) {
      typeCmb.addItem(itemType);
    }
    typeCmb.setValue(timeline.getOptions().getType());
    typeCmb.addValueChangeListener(new Property.ValueChangeListener() {
      @Override
      public void valueChange(Property.ValueChangeEvent event) {
        timeline.getOptions().setType((TimelineOptions.ItemType) event.
            getProperty().getValue());
      }
    });
    controlLayout.addComponent(typeCmb);

    // Item alignment
    final ComboBox alignCmb = new ComboBox("Item Alignment");
    alignCmb.setNullSelectionAllowed(false);
    alignCmb.setImmediate(true);
    alignCmb.setFilteringMode(FilteringMode.OFF);
    for (TimelineOptions.ItemAlignment a : TimelineOptions.ItemAlignment.
        values()) {
      alignCmb.addItem(a);
    }
    alignCmb.setValue(timeline.getOptions().getAlign());
    alignCmb.addValueChangeListener(new Property.ValueChangeListener() {
      @Override
      public void valueChange(Property.ValueChangeEvent event) {
        timeline.getOptions().setAlign((TimelineOptions.ItemAlignment) event.
            getProperty().getValue());
      }
    });
    controlLayout.addComponent(alignCmb);

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
          cal.add(Calendar.MINUTE, (int) (10 + Math.random() * 50));
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

    btn = new Button("Toggle Visible", new Button.ClickListener() {
      @Override
      public void buttonClick(Button.ClickEvent event) {
        timeline.setVisible(!timeline.isVisible());
      }
    });
    controlLayout.addComponent(btn);

    return panel;
  }

  private Component buildTimeControls(final Timeline timeline) {
    Panel panel = new Panel("Time Controls");

    Calendar cal = Calendar.getInstance();

    VerticalLayout controlLayout = new VerticalLayout();
    controlLayout.setSpacing(true);
    controlLayout.setMargin(true);
    panel.setContent(controlLayout);

    final DateField startDt = new DateField();
    startDt.setResolution(Resolution.SECOND);
    startDt.setValue(cal.getTime());
    controlLayout.addComponent(startDt);

    cal.add(Calendar.HOUR, 8);
    final DateField endDt = new DateField();
    endDt.setResolution(Resolution.SECOND);
    endDt.setValue(cal.getTime());
    controlLayout.addComponent(endDt);

    Button btn = new Button("Set Window", new Button.ClickListener() {
      @Override
      public void buttonClick(Button.ClickEvent event) {
        timeline.setWindow(startDt.getValue(), endDt.getValue(),
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

    final DateField currentTimeDt = new DateField();
    currentTimeDt.setResolution(Resolution.SECOND);
    currentTimeDt.setValue(new Date());
    controlLayout.addComponent(currentTimeDt);
    
    btn = new Button("Set Current Time", new Button.ClickListener() {
      @Override
      public void buttonClick(Button.ClickEvent event) {
        timeline.setCurrentTime(currentTimeDt.getValue());
      }
    });
    controlLayout.addComponent(btn);

    btn = new Button("Add Custom Time", new Button.ClickListener() {
      @Override
      public void buttonClick(Button.ClickEvent event) {
        timeline.addCustomTime(new Date(), UUID.randomUUID().toString());
      }
    });
    controlLayout.addComponent(btn);

    final DateField moveToDt = new DateField();
    moveToDt.setResolution(Resolution.SECOND);
    moveToDt.setValue(new Date());
    controlLayout.addComponent(moveToDt);

    btn = new Button("Move To", new Button.ClickListener() {
      @Override
      public void buttonClick(Button.ClickEvent event) {
        timeline.moveTo(moveToDt.getValue(), new TimelineMethodOptions.MoveTo(
            new Animation(1000, Animation.EasingFunction.easeInOutQuad)));
      }
    });
    controlLayout.addComponent(btn);

    btn = new Button("Fit", new Button.ClickListener() {
      @Override
      public void buttonClick(Button.ClickEvent event) {
        timeline.fit(new TimelineMethodOptions.Fit(new Animation(750,
            Animation.EasingFunction.easeInOutQuint)));
      }
    });
    controlLayout.addComponent(btn);

    return panel;
  }

  private Component buildZoomControls(final Timeline timeline) {

    Panel panel = new Panel("Bounds Controls");
    Calendar cal = Calendar.getInstance();

    VerticalLayout controlLayout = new VerticalLayout();
    controlLayout.setSpacing(true);
    controlLayout.setMargin(true);
    panel.setContent(controlLayout);

    // ZoomMin, ZoomMax
    final TextField zoomMin = new TextField();
    zoomMin.setValue(String.valueOf(timeline.getOptions().getZoomMin() / 1000));
    controlLayout.addComponent(zoomMin);

    final TextField zoomMax = new TextField();
    zoomMax.setValue(String.valueOf(timeline.getOptions().getZoomMax() / 1000));
    controlLayout.addComponent(zoomMax);

    Button btn = new Button("Apply Zoom Min/Max", new Button.ClickListener() {
      @Override
      public void buttonClick(Button.ClickEvent event) {
        TimelineOptions options = timeline.getOptions();
        options.setZoomMin(Integer.parseInt(zoomMin.getValue()) * 1000);
        options.setZoomMax(Integer.parseInt(zoomMax.getValue()) * 1000);
      }
    });
    controlLayout.addComponent(btn);

    // Min/max
    final DateField minDt = new DateField();
    minDt.setResolution(Resolution.SECOND);
    minDt.setValue(cal.getTime());
    controlLayout.addComponent(minDt);

    cal.add(Calendar.HOUR, 8);
    final DateField maxDt = new DateField();
    maxDt.setResolution(Resolution.SECOND);
    maxDt.setValue(cal.getTime());
    controlLayout.addComponent(maxDt);

    btn = new Button("Apply Min/Max", new Button.ClickListener() {
      @Override
      public void buttonClick(Button.ClickEvent event) {
        TimelineOptions options = timeline.getOptions();
        options.setMin(minDt.getValue());
        options.setMax(maxDt.getValue());
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
    chk.setValue(timeline.isReadOnly());
    chk.addValueChangeListener(new Property.ValueChangeListener() {
      @Override
      public void valueChange(Property.ValueChangeEvent event) {
        timeline.setReadOnly((Boolean) event.getProperty().getValue());
      }
    });
    controlLayout.addComponent(chk);

    chk = new CheckBox("Edit Update Time");
    chk.setValue(timeline.getOptions().isEditUpdateTime());
    chk.addValueChangeListener(new Property.ValueChangeListener() {
      @Override
      public void valueChange(Property.ValueChangeEvent event) {
        TimelineOptions options = timeline.getOptions();
        options.setEditUpdateTime((Boolean) event.getProperty().getValue());
      }
    });
    controlLayout.addComponent(chk);

    chk = new CheckBox("Edit Update Group");
    chk.setValue(timeline.getOptions().isEditUpdateGroup());
    chk.addValueChangeListener(new Property.ValueChangeListener() {
      @Override
      public void valueChange(Property.ValueChangeEvent event) {
        TimelineOptions options = timeline.getOptions();
        options.setEditUpdateGroup((Boolean) event.getProperty().getValue());
      }
    });
    controlLayout.addComponent(chk);

    chk = new CheckBox("Edit Add");
    chk.setValue(timeline.getOptions().isEditAdd());
    chk.addValueChangeListener(new Property.ValueChangeListener() {
      @Override
      public void valueChange(Property.ValueChangeEvent event) {
        TimelineOptions options = timeline.getOptions();
        options.setEditAdd((Boolean) event.getProperty().getValue());
      }
    });
    controlLayout.addComponent(chk);

    chk = new CheckBox("Edit Remove");
    chk.setValue(timeline.getOptions().isEditRemove());
    chk.addValueChangeListener(new Property.ValueChangeListener() {
      @Override
      public void valueChange(Property.ValueChangeEvent event) {
        TimelineOptions options = timeline.getOptions();
        options.setEditRemove((Boolean) event.getProperty().getValue());
      }
    });
    controlLayout.addComponent(chk);

    chk = new CheckBox("Selectable");
    chk.setValue(timeline.getOptions().isSelectable());
    chk.addValueChangeListener(new Property.ValueChangeListener() {
      @Override
      public void valueChange(Property.ValueChangeEvent event) {
        TimelineOptions options = timeline.getOptions();
        options.setSelectable((Boolean) event.getProperty().getValue());
      }
    });
    controlLayout.addComponent(chk);

    chk = new CheckBox("Multiselect");
    chk.setValue(timeline.getOptions().isMultiselect());
    chk.addValueChangeListener(new Property.ValueChangeListener() {
      @Override
      public void valueChange(Property.ValueChangeEvent event) {
        TimelineOptions options = timeline.getOptions();
        options.setMultiselect((Boolean) event.getProperty().getValue());
      }
    });
    controlLayout.addComponent(chk);

    chk = new CheckBox("Show Current Time");
    chk.setValue(timeline.getOptions().isShowCurrentTime());
    chk.addValueChangeListener(new Property.ValueChangeListener() {
      @Override
      public void valueChange(Property.ValueChangeEvent event) {
        TimelineOptions options = timeline.getOptions();
        options.setShowCurrentTime((Boolean) event.getProperty().getValue());
      }
    });
    controlLayout.addComponent(chk);

    chk = new CheckBox("Movable");
    chk.setValue(timeline.getOptions().isMoveable());
    chk.addValueChangeListener(new Property.ValueChangeListener() {
      @Override
      public void valueChange(Property.ValueChangeEvent event) {
        TimelineOptions options = timeline.getOptions();
        options.setMoveable((Boolean) event.getProperty().getValue());
      }
    });
    controlLayout.addComponent(chk);

    chk = new CheckBox("Zoomable");
    chk.setValue(timeline.getOptions().isZoomable());
    chk.addValueChangeListener(new Property.ValueChangeListener() {
      @Override
      public void valueChange(Property.ValueChangeEvent event) {
        TimelineOptions options = timeline.getOptions();
        options.setZoomable((Boolean) event.getProperty().getValue());
      }
    });
    controlLayout.addComponent(chk);

    chk = new CheckBox("Click to Use");
    chk.setValue(timeline.getOptions().isClickToUse());
    chk.addValueChangeListener(new Property.ValueChangeListener() {
      @Override
      public void valueChange(Property.ValueChangeEvent event) {
        TimelineOptions options = timeline.getOptions();
        options.setClickToUse((Boolean) event.getProperty().getValue());
      }
    });
    controlLayout.addComponent(chk);

    chk = new CheckBox("Show Major Labels");
    chk.setValue(timeline.getOptions().isClickToUse());
    chk.addValueChangeListener(new Property.ValueChangeListener() {
      @Override
      public void valueChange(Property.ValueChangeEvent event) {
        TimelineOptions options = timeline.getOptions();
        options.setShowMajorLabels((Boolean) event.getProperty().getValue());
      }
    });
    controlLayout.addComponent(chk);

    chk = new CheckBox("Show Minor Labels");
    chk.setValue(timeline.getOptions().isClickToUse());
    chk.addValueChangeListener(new Property.ValueChangeListener() {
      @Override
      public void valueChange(Property.ValueChangeEvent event) {
        TimelineOptions options = timeline.getOptions();
        options.setShowMinorLabels((Boolean) event.getProperty().getValue());
      }
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
