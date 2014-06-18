package org.mpilone.vaadin;

import java.util.*;
import java.util.Calendar;

import org.mpilone.vaadin.timeline.*;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.*;
import com.vaadin.ui.components.calendar.ContainerEventProvider;
import com.vaadin.ui.components.calendar.event.CalendarEvent;

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
    setSizeFull();
    setMargin(true);
    setSpacing(true);

    Calendar cal = Calendar.getInstance();

    BeanItemContainer container =
        new BeanItemContainer(BasicTimelineEvent.class);

    for (int i = 0; i < 50; ++i) {
      BasicTimelineEvent evt = new BasicTimelineEvent();
      evt.setStart(cal.getTime());
      cal.add(Calendar.MINUTE, (int) (10 + Math.random() * 50));
      evt.setEnd(cal.getTime());
      evt.setCaption(PROGRAMS[(int) (Math.random() * 6)]);
      evt.setGroup(GROUPS[(int) (Math.random() * 6)]);
      container.addBean(evt);

      cal.setTime(evt.getStart());
      cal.add(Calendar.MINUTE, 10);
    }

    List<TimelineGroup> groups = new ArrayList<>(GROUPS.length);
    for (String groupId : GROUPS) {
      groups.add(new BasicTimelineGroup(groupId, groupId, null));
    }

    Timeline t = new Timeline(new ContainerEventProvider(container));
    t.setGroups(groups);
    t.setWidth(StyleConstants.FULL_WIDTH);
    t.addEventSelectListener(new TimelineComponentEvents.EventSelectListener() {
      @Override
      public void eventSelect(TimelineComponentEvents.EventSelect event) {
        CalendarEvent calEvent = event.getCalendarEvent();
        System.out.println("Event select: " + (calEvent == null ?
            "<no selection>" : calEvent.getCaption()));
      }
    });
    t.addVisibleRangeChangeListener(
        new TimelineComponentEvents.VisibleRangeChangeListener() {
          @Override
          public void visibleRangeChange(
              TimelineComponentEvents.VisibleRangeChange event) {
            System.out.println("Visible range change: " + event.
                getStartDate()
                + " to " + event.getEndDate());
              }
        });
    addComponent(t);

    // Control options
    addComponent(buildControls(t));
  }

  private Component buildControls(final Timeline timeline) {
    HorizontalLayout controlLayout = new HorizontalLayout();
    controlLayout.setSpacing(true);
    controlLayout.setWidth("100%");
    addComponent(controlLayout);

    Button btn = new Button("Go to Now", new Button.ClickListener() {
      @Override
      public void buttonClick(Button.ClickEvent event) {
        Calendar cal = Calendar.getInstance();
        Date start = cal.getTime();
        cal.add(Calendar.HOUR, 3);
        Date end = cal.getTime();
        timeline.setWindow(start, end);
      }
    });
    controlLayout.addComponent(btn);

    controlLayout.addComponent(buildTimeControl(timeline));
    controlLayout.addComponent(buildToggleControls(timeline));
    controlLayout.addComponent(buildZoomControl(timeline));

    return controlLayout;
  }

  private Component buildTimeControl(final Timeline timeline) {

    VerticalLayout controlLayout = new VerticalLayout();
    controlLayout.setSpacing(true);

    final DateField startDt = new DateField();
    startDt.setResolution(Resolution.SECOND);
    startDt.setValue(timeline.getStartDate());
    controlLayout.addComponent(startDt);

    final DateField endDt = new DateField();
    endDt.setResolution(Resolution.SECOND);
    endDt.setValue(timeline.getEndDate());
    controlLayout.addComponent(endDt);

    Button btn = new Button("Apply Visible Range", new Button.ClickListener() {
      @Override
      public void buttonClick(Button.ClickEvent event) {
        timeline.setWindow(startDt.getValue(), endDt.getValue());
      }
    });
    controlLayout.addComponent(btn);

    return controlLayout;
  }

  private Component buildZoomControl(final Timeline timeline) {

    VerticalLayout controlLayout = new VerticalLayout();
    controlLayout.setSpacing(true);

    final TextField zoomMin = new TextField();
    zoomMin.setValue(String.valueOf(timeline.getZoomMin() / 1000));
    controlLayout.addComponent(zoomMin);

    final TextField zoomMax = new TextField();
    zoomMax.setValue(String.valueOf(timeline.getZoomMax() / 1000));
    controlLayout.addComponent(zoomMax);

    Button btn = new Button("Apply Zoom Min/Max", new Button.ClickListener() {
      @Override
      public void buttonClick(Button.ClickEvent event) {
        timeline.setZoomMin(Integer.parseInt(zoomMin.getValue()) * 1000);
        timeline.setZoomMax(Integer.parseInt(zoomMax.getValue()) * 1000);
      }
    });
    controlLayout.addComponent(btn);

    return controlLayout;
  }

  private Component buildToggleControls(final Timeline timeline) {
    VerticalLayout controlLayout = new VerticalLayout();
    controlLayout.setSpacing(true);

    CheckBox chk = new CheckBox("Show Current Time");
    chk.setValue(timeline.isShowCurrentTime());
    chk.addValueChangeListener(new Property.ValueChangeListener() {
      @Override
      public void valueChange(Property.ValueChangeEvent event) {
        timeline.setShowCurrentTime((Boolean) event.getProperty().getValue());
      }
    });
    controlLayout.addComponent(chk);

     chk = new CheckBox("Show Custom Time");
    chk.setValue(timeline.isShowCustomTime());
    chk.addValueChangeListener(new Property.ValueChangeListener() {
      @Override
      public void valueChange(Property.ValueChangeEvent event) {
        timeline.setShowCustomTime((Boolean) event.getProperty().getValue());
      }
    });
    controlLayout.addComponent(chk);

    chk = new CheckBox("Read-only");
    chk.setValue(timeline.isReadOnly());
    chk.addValueChangeListener(new Property.ValueChangeListener() {
      @Override
      public void valueChange(Property.ValueChangeEvent event) {
        timeline.setReadOnly((Boolean) event.getProperty().getValue());
      }
    });
    controlLayout.addComponent(chk);

    chk = new CheckBox("Selectable");
    chk.setValue(timeline.isSelectable());
    chk.addValueChangeListener(new Property.ValueChangeListener() {
      @Override
      public void valueChange(Property.ValueChangeEvent event) {
        timeline.setSelectable((Boolean) event.getProperty().getValue());
      }
    });
    controlLayout.addComponent(chk);

    chk = new CheckBox("Update Time");
    chk.setValue(timeline.isUpdateTime());
    chk.addValueChangeListener(new Property.ValueChangeListener() {
      @Override
      public void valueChange(Property.ValueChangeEvent event) {
        timeline.setUpdateTime((Boolean) event.getProperty().getValue());
      }
    });
    controlLayout.addComponent(chk);

    chk = new CheckBox("Update Group");
    chk.setValue(timeline.isUpdateGroup());
    chk.addValueChangeListener(new Property.ValueChangeListener() {
      @Override
      public void valueChange(Property.ValueChangeEvent event) {
        timeline.setUpdateGroup((Boolean) event.getProperty().getValue());
      }
    });
    controlLayout.addComponent(chk);

    return controlLayout;
  }

}
