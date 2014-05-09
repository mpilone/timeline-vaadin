package org.mpilone.vaadin;

import static org.mpilone.vaadin.StyleConstants.FULL_WIDTH;

import com.vaadin.annotations.*;
import com.vaadin.data.Property;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.ui.*;

/**
 * Demo for the various Vaadin components by Mike Pilone.
 *
 * @author mpilone
 */
@Theme("runo")
@Push(value = PushMode.DISABLED)
public class Demo extends UI {

  private VerticalLayout contentLayout;
  private Component demoComponent;

  @Override
  protected void init(VaadinRequest request) {

    //setPollInterval(3000);
    setWidth(FULL_WIDTH);

    contentLayout = new VerticalLayout();
    contentLayout.setMargin(true);
    contentLayout.setSpacing(true);
    contentLayout.setWidth(FULL_WIDTH);
    setContent(contentLayout);

    ComboBox cmb = new ComboBox("Select Demo");
    cmb.addItem("Default");
    cmb.setValue("Default");
    cmb.setNullSelectionAllowed(false);
    cmb.setImmediate(true);
    cmb.addValueChangeListener(new Property.ValueChangeListener() {
      @Override
      public void valueChange(Property.ValueChangeEvent event) {
        onDemoChange((String) event.getProperty().getValue());
      }
    });
    contentLayout.addComponent(cmb);

    onDemoChange((String) cmb.getValue());
  }

  /**
   * Called when the selected demo is changed. The new demo component is swapped
   * in.
   *
   * @param value the name of the new demo to display
   */
  private void onDemoChange(String value) {
    Component c = null;

    switch (value) {
      case "Default":
        c = new DefaultDemo();
        break;
    }

    if (demoComponent != null) {
      contentLayout.replaceComponent(demoComponent, c);
    }
    else {
      contentLayout.addComponent(c);
    }

    demoComponent = c;
  }
}
