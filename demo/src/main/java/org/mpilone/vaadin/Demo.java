package org.mpilone.vaadin;

import static org.mpilone.vaadin.StyleConstants.FULL_WIDTH;

import com.vaadin.annotations.*;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.ui.*;

/**
 * Demo for the various Vaadin components by Mike Pilone.
 *
 * @author mpilone
 */
@Theme("valo")
@Push(value = PushMode.DISABLED)
public class Demo extends UI {

  @Override
  protected void init(VaadinRequest request) {

    //setPollInterval(3000);
    setWidth(FULL_WIDTH);
    setContent(new DefaultDemo());
  }
}
