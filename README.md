SpringMVC Router
================

[![Build Status](https://secure.travis-ci.org/resthub/springmvc-router.png?branch=master)](http://travis-ci.org/resthub/springmvc-router)

Developers mailing list: resthub-dev@googlegroups.com

Route mapping with SpringMVC Router
-----------------------------------

Spring MVC 3.2 [handles requests mapping](http://static.springsource.org/spring/docs/3.2.x/spring-framework-reference/html/mvc.html) with `RequestMappingHandlerMapping` and `RequestMappingHandlerAdapter` beans (that's the "out-of-the-box" configuration that comes with your springmvc application).

But you may want to use a request Router for your application:

* Route Configuration is centralized in one place (you don't need to look into your controllers anymore) 
* URL Refactoring is easier
* Many web frameworks  use that system (Rails, PlayFramework and many others)
* Handles routes priority 


Define your application routes like this!

    GET     /user/?                 userController.listAll
    GET     /user/{<[0-9]+>id}      userController.showUser
    DELETE  /user/{<[0-9]+>id}      userController.deleteUser
    POST    /user/add/?             userController.createUser


Configuring the SpringMVC Router for your project
-------------------------------------------------

### Add the dependency to your maven pom.xml

Warning: **this project is currently tested on Spring 3.2.x**, and is not compatible with Spring 3.0.x - your project needs these dependencies.
  

    <dependencies>
    ...
      <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-aop</artifactId>
        <version>${spring-version}</version>
      </dependency>
      <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-beans</artifactId>
        <version>${spring-version}</version>
      </dependency>
      <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-webmvc</artifactId>
        <version>${spring-version}</version>
      </dependency>
    ...
      <dependency>
        <groupId>org.resthub</groupId>
        <artifactId>springmvc-router</artifactId>
        <version>1.1.1</version>
      </dependency>
    ...
    </dependencies>

If you want to use SNAPSHOTs, add oss.sonatype.org as a repository.

    <repositories>
      <repository>
        <id>sonatype.oss.snapshots</id>
          <name>Sonatype OSS Snapshot Repository</name>
          <url>http://oss.sonatype.org/content/repositories/snapshots</url>
          <releases>
            <enabled>false</enabled>
          </releases>
          <snapshots>
            <enabled>true</enabled>
          </snapshots>
      </repository> 
    </repositories>


### Add the Router to your Spring MVC configuration

In your *-servlet.xml file, add the following beans:


     <?xml version="1.0" encoding="UTF-8"?>
     <beans  xmlns="http://www.springframework.org/schema/beans"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xmlns:context="http://www.springframework.org/schema/context"
         xsi:schemaLocation="http://www.springframework.org/schema/beans
             http://www.springframework.org/schema/beans/spring-beans-3.1.xsd
             http://www.springframework.org/schema/context
             http://www.springframework.org/schema/context/spring-context-3.1.xsd">
    
        <!--
          Enable bean declaration by annotations, update base package according to your project
        -->
        <context:annotation-config/>
    
    	<!--
    		Package to scan for Controllers.
    		All Controllers with @Controller annotation are loaded as such.
    	-->
    	<context:component-scan base-package="com.example.yourproject.controllers" />
    
    	<!-- 
    		Choose HandlerMapping.
    		RouterHandlerMapping loads routes configuration from a file.
    		Router adapted from Play! Framework.
    		
    		@see http://www.playframework.org/documentation/1.2.4/routes#syntax
    		for route configuration syntax.
    		Example:
    		GET    /home          PageController.showPage(id:'home')
    		GET    /page/{id}     PageController.showPage
    	-->		 
    	<bean id="handlerMapping"
              class="org.resthub.web.springmvc.router.RouterHandlerMapping">
                <property name="routeFiles">
                    <list>
                        <value>routes.conf</value>
                    <!--
                        Router will *append* routes declared in additional files
                        <value>addroutes.conf</value>
                    -->
                    </list>
                </property>

                <!-- 
                    Uncomment the following configuration line
                    if you want routes to be dynamically reloaded when
                    route files are modified.
                    Can be a good idea in dev mode, not so much in production!
                -->
                <!-- <property name="autoReloadEnabled" value="true" /> -->
        </bean>
    
    </beans>

Or you can achieve the same thing with a Javaconfig class like this:

    @Configuration
    @ComponentScan(basePackages = "com.example.yourproject.controllers")
    // You should not use the @EnableWebMvc annotation
    public class WebAppConfig extends RouterConfigurationSupport {

      @Override
      public List<String> listRouteFiles() {

        List<String> routeFiles = new ArrayList<String>();
        routeFiles.add("routes.conf");
        return routeFiles;
      }
    }    

### Create your route configuration file


The example above will load the configuration file using Spring ResourceLoader - so create a new file in your project `src/main/resources/routes.conf`.

Routes configuration
--------------------

The router maps HTTP request to a specific action (i.e. a public method of a Controller class handling requests).

### Get your first Controller ready!

Controllers can use [Spring MVC annotations and conventions](http://static.springsource.org/spring/docs/3.1.x/spring-framework-reference/html/mvc.html) - only the `@RequestMapping` annotation is useless.


    @Controller
    public class HelloController {
      public void simpleAction() {
      
      }
    	
      public @ResponseBody String sayHelloTo(@PathVariable(value = "name") String name) {
        return "Hello "+name+" !";	  
      }
    }



### Edit your route configuration file

**Warning: in the route configuration file, Controller names are case sensitive, and should always start with a lower case letter.**


    # this is a comment
    
    GET     /simpleaction                  helloController.simpleAction
    GET     /hello/{<[a-zA-Z]+>name}       helloController.sayHelloTo


For more details on routes syntax, [check out the PlayFramework documentation](http://www.playframework.org/documentation/1.2.4/routes).


View Integration
----------------

Routing requests to actions is one thing. But refactoring routes can be a real pain if all your URLs are hard coded in your template views. Reverse routing is the solution.


### Reverse Routing

Example route file:

    GET     /user/?                 userController.listAll
    GET     /user/{<[0-9]+>id}      userController.showUser
    DELETE  /user/{<[0-9]+>id}      userController.deleteUser
    POST    /user/add/?             userController.createUser


Reverse routing in your Java class:

    import org.resthub.web.springmvc.router.Router;
    
    public class MyClass {
      public void myMethod() {
        
        ActionDefinition action = Router.reverse("userController.listAll");
        // logs "/user/"
        logger.info(action.url);
    
        HashMap<String, Object> args = new HashMap<String, Object>();
        args.put("id",42L);
        ActionDefinition otherAction = Router.reverse("userController.showUser", args);
        // logs "/user/42"
        logger.info(otherAction.url);
      }
    }


### Integrating with Velocity

First, add the RouteDirective to your Velocity Engine configuration:

    <!--
      Configure your Velocity Template engine.
      Add the custom directive to the engine.  
    -->
    <bean id="velocityConfig"
      class="org.springframework.web.servlet.view.velocity.VelocityConfigurer">
      <property name="resourceLoaderPath" value="classpath:velocity" />
      <property name="preferFileSystemAccess" value="false"/>
      <property name="velocityProperties">
        <props>
          <prop key="userdirective">org.resthub.web.springmvc.view.velocity.RouteDirective</prop>
        </props>
      </property>
    </bean>

Then use the #route directive within your .vm file:

    <a href="#route("userController.listAll")">List all users</a>
    <a href="#route("userController.showUser(id:'42')")">Show user 42</a>

### Integrating with FreeMarker

In your Spring MVC context add the following:

    <mvc:interceptors>
        <bean class="org.resthub.web.springmvc.view.freemarker.RouterModelAttribute"/>
    </mvc:interceptors>

This will inject a model attribute called "route" to every model. The attribute name can be modified by setting the
property "attributeName".

    <mvc:interceptors>
        <bean class="org.resthub.web.springmvc.view.freemarker.RouterModelAttribute">
            <property name="attributeName" value="myAttributeName"/>
        </bean>
    </mvc:interceptors>

Then use the Router instance within your .ftl files:

    <a href="${route.reverse('userController.listAll')}">List all users</a>

    <#assign params = {"id":42}/>
    <a href="${route.reverse('userController.showUser', params)}">Show user 42</a>

Spring HATEOAS support
----------------------

SpringMVC Router has its own [LinkBuilder implementation](https://github.com/resthub/springmvc-router/blob/master/src/main/java/org/resthub/web/springmvc/router/hateoas/RouterLinkBuilder.java) to work with [Spring HATEOAS](https://github.com/SpringSource/spring-hateoas).


Tools
-----

### Autocomplete reverse routing in your IDE

[springmvc-router-ide](https://github.com/bradhouse/springmvc-router-ide) is a Maven plugin to generate template files that assist IDEs in autocompleting reverse routing with this project.

### RESThub framework

This project can be used as an addon to [RESThub framework](http://resthub.org/2).
