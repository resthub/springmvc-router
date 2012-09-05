package org.resthub.web.springmvc.view.freemarker;

import static org.fest.assertions.api.Assertions.*;
import org.resthub.web.springmvc.router.Router;
import org.springframework.web.servlet.ModelAndView;
import org.testng.annotations.Test;


public class RouterModelAttributeTest {
  @Test
  public void testSetAttributeName() throws Exception {
    String name = "anotherName";

    RouterModelAttribute rma = new RouterModelAttribute();
    rma.setAttributeName(name);

    ModelAndView mav = new ModelAndView();
    rma.postHandle(null, null, null, mav);
    Object router = mav.getModel().get(name);

    assertThat(router).isInstanceOf(Router.class);
    assertThat(mav.getModel().get("route")).isNull();
  }

  @Test
  public void testPostHandle() throws Exception {
    RouterModelAttribute rma = new RouterModelAttribute();
    ModelAndView mav = new ModelAndView();
    rma.postHandle(null, null, null, mav);
    Object router = mav.getModel().get("route");

    assertThat(router).isInstanceOf(Router.class);
  }
}
