package org.resthub.web.springmvc.router.javaconfig;

import org.resthub.web.springmvc.router.RouterConfigurationSupport;
import org.resthub.web.springmvc.router.RouterHandlerMapping;
import org.resthub.web.springmvc.router.support.TeapotHandlerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ComponentScan(basePackages = "org.resthub.web.springmvc.router.controllers")
public class WebAppConfig extends RouterConfigurationSupport {

    @Override
    protected void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(new TeapotHandlerInterceptor());
    }

    @Override
    public List<String> listRouteFiles() {

        List<String> routeFiles = new ArrayList<String>();
        routeFiles.add("mappingroutes.conf");

        return routeFiles;
    }
}
