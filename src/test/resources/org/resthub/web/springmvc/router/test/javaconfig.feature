Feature: java config support
  As a developer coding a application
  I want to configure beans using java config
  In order to setup my application

  Scenario: Adding HandlerInterceptors using javaconfig
    Given I have a web application with javaconfig in package "org.resthub.web.springmvc.router.javaconfig"
    When I send the HTTP request "GET" "/simpleaction" with query params:
      | name         | value           |
      | teapot       | true            |
    Then the server should send an HTTP response with status "418"