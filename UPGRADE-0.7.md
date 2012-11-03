UPGRADE FROM 0.6 to 0.7
=======================

### Spring backwards compatibility

This project no longer supports Spring MVC 3.0.x . Spring MVC 3.1.0++ is now a requirement. 

[@RequestMapping support changed in Spring MVC 3.1](http://static.springsource.org/spring/docs/current/spring-framework-reference/html/mvc.html#mvc-ann-requestmapping-31-vs-30), and springmvc-router follows that direction.

### Spring MVC configuration

The HandlerAdapter part of springmvc-router is no longer needed, because we rely on Spring MVC default implementation `RequestMappingHandlerAdapter`.
So you should delete this part from *-servlet.xml your configuration:

    <!--
      HandlerAdapter
      RouterHandlerAdapter use routes defined by RouterHandlerMapping.
      Still gets @RequestParam, @SessionAttributes, @CookieValue ... annotations
    -->
    <bean id="handlerAdapter"
      class="org.resthub.web.springmvc.router.RouterHandlerAdapter" />

RouterHandlerMapping declaration has changed.
It now supports multiple configuration files. Even if your application uses only one configuration file, you have to update your RouterHandlerMapping declaration for the property "routeFiles".

    <bean id="handlerMapping"
      class="org.resthub.web.springmvc.router.RouterHandlerMapping">
      <property name="routeFile" value="routes.conf" />
      <property name="routeFiles">
      <list>
        <value>routes.conf</value>
        <!--
        Router will *append* routes declared in additional files
        <value>addroutes.conf</value>
        -->
      </list>
      </property>
    </bean>

