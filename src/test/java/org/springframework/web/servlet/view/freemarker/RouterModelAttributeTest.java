package org.springframework.web.servlet.view.freemarker;

import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.router.Router;

import static org.junit.Assert.*;

public class RouterModelAttributeTest {
  @Test
  public void testSetAttributeName() throws Exception {
    String name = "anotherName";

    RouterModelAttribute rma = new RouterModelAttribute();
    rma.setAttributeName(name);

    ModelAndView mav = new ModelAndView();
    rma.postHandle(null, null, null, mav);
    Object router = mav.getModel().get(name);

    assertTrue(router instanceof Router);
    assertNull(mav.getModel().get("route"));
  }

  @Test
  public void testPostHandle() throws Exception {
    RouterModelAttribute rma = new RouterModelAttribute();
    ModelAndView mav = new ModelAndView();
    rma.postHandle(null, null, null, mav);
    Object router = mav.getModel().get("route");

    assertTrue(router instanceof Router);
  }
}
