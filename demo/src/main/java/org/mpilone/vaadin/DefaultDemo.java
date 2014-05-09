
package org.mpilone.vaadin;

import java.util.Calendar;

import org.mpilone.vaadin.timeline.*;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.components.calendar.ContainerEventProvider;

/**
 *
 * @author mpilone
 */
public class DefaultDemo extends VerticalLayout {

  private String[] groups = new String[]{"S001", "S002", "S003", "S004", "S005",
    "S006"};
  private String[] programs =
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
      evt.setCaption(programs[(int) (Math.random() * 6)]);
      evt.setGroup(groups[(int) (Math.random() * 6)]);
      container.addBean(evt);

      cal.setTime(evt.getStart());
      cal.add(Calendar.MINUTE, 10);
    }

    Timeline t = new Timeline(new ContainerEventProvider(container));
    t.setShowCurrentTime(true);
    t.setWidth(StyleConstants.FULL_WIDTH);

    addComponent(t);
  }

}
