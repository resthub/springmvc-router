UPGRADE FROM 0.7 to 0.8
=======================

### Spring MVC configuration

RouterHandlerMapping declaration has changed.
"servletPrefix" configuration is now useless and so you should remove that part from your *-servlet.xml.

    <property name="servletPrefix" value="foobar" />

This is now done automatically for you at runtime, for the context path and the servlet path.