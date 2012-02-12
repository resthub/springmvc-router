package org.springframework.web.servlet.view.freemarker;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.mvc.router.Router;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * An interceptor to inject an instance of {@link Router} into the model for all Spring MVC views. By default the
 * attribute name used is "route". This can be changed using {@link #setAttributeName(String)}.
 */
public class RouterModelAttribute extends HandlerInterceptorAdapter {

  private static final String DEFAULT_ATTRIBUTE_NAME = "route";

  private String attributeName = DEFAULT_ATTRIBUTE_NAME;

  public void setAttributeName(String attributeName) {
    this.attributeName = attributeName;
  }

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
      ModelAndView mav) throws Exception {

    if (mav != null && mav.getModelMap() != null) {
      mav.getModelMap().addAttribute(attributeName, new Router());
    }
  }
  
}
