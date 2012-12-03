package org.resthub.web.springmvc.router.support;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TeapotHandlerInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if(request.getParameter("teapot").equals("true")) {
            // I'm a teapot
            response.sendError(418);
            return false;
        }
        return true;
    }
}