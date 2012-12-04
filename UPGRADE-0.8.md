UPGRADE FROM 0.7 to 0.8
=======================

### Spring MVC configuration

RouterHandlerMapping declaration has changed.
"servletPrefix" configuration is now useless and so you should remove that part from your *-servlet.xml.

    <property name="servletPrefix" value="foobar" />

This is now done automatically for you at runtime, for the context path and the servlet path.

### Javaconfig support

If you want full Javaconfig support - for example, adding interceptors by implementing [WebMvcConfigurer](http://static.springsource.org/spring/docs/current/javadoc-api/org/springframework/web/servlet/config/annotation/WebMvcConfigurer.html) methods:

* you SHOULD NOT use the [@EnableWebMvc](http://static.springsource.org/spring/docs/current/javadoc-api/org/springframework/web/servlet/config/annotation/EnableWebMvc.html) annotation
* you SHOULD make your javaconfig class extend RouterConfigurationSupport
* no need to declare springmvc-router related beans in XMLs :-D