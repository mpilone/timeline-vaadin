package org.mpilone.vaadin;

import java.util.Calendar;

import org.mpilone.vaadin.timeline.*;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.*;
import com.vaadin.ui.components.calendar.ContainerEventProvider;

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

    Timeline t = new Timeline(new ContainerEventProvider(container));
    t.setWidth(StyleConstants.FULL_WIDTH);
    addComponent(t);

    // Control options
    addComponent(buildControls(t));
  }

  private Component buildControls(final Timeline timeline) {
    HorizontalLayout controlLayout = new HorizontalLayout();
    controlLayout.setSpacing(true);
    controlLayout.setWidth("100%");
    addComponent(controlLayout);

    Button btn = new Button("Show Now", new Button.ClickListener() {
      @Override
      public void buttonClick(Button.ClickEvent event) {
        timeline.setVisibleChartRangeNow();
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
        timeline.setVisibleChartRange(startDt.getValue(), endDt.getValue());
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

    chk = new CheckBox("Moveable");
    chk.setValue(timeline.isMoveable());
    chk.addValueChangeListener(new Property.ValueChangeListener() {
      @Override
      public void valueChange(Property.ValueChangeEvent event) {
        timeline.setMoveable((Boolean) event.getProperty().getValue());
      }
    });
    controlLayout.addComponent(chk);

    chk = new CheckBox("Zoomable");
    chk.setValue(timeline.isZoomable());
    chk.addValueChangeListener(new Property.ValueChangeListener() {
      @Override
      public void valueChange(Property.ValueChangeEvent event) {
        timeline.setZoomable((Boolean) event.getProperty().getValue());
      }
    });
    controlLayout.addComponent(chk);

    return controlLayout;
  }

}
