Feature: Handler mapping support
  As a developer coding a application
  I want HTTP requests to be mapped to controllers
  In order to implement Controller behaviour accordingly


  Scenario: No route defined for a request
    Given I have a web application with the config locations "/simpleTestContext.xml"
    When I send the HTTP request "GET" "/noroute"
    Then no handler should be found

  Scenario: Mapping a simple request
    Given I have a web application with the config locations "/simpleTestContext.xml"
    When I send the HTTP request "GET" "/simpleaction"
    Then the request should be handled by "myTestController.simpleAction"

  Scenario: Mapping a request with a default param
    Given I have a web application with the config locations "/simpleTestContext.xml"
    When I send the HTTP request "GET" "/param"
    Then the request should be handled by "myTestController.paramAction"

  Scenario: Mapping a request with a given param
    Given I have a web application with the config locations "/simpleTestContext.xml"
    When I send the HTTP request "GET" "/param/myparam"
    Then the request should be handled by "myTestController.paramAction"

  Scenario: Mapping a GET request
    Given I have a web application with the config locations "/simpleTestContext.xml"
    When I send the HTTP request "GET" "/http"
    Then the request should be handled by "myTestController.httpAction"

  Scenario: Mapping a PUT request
    Given I have a web application with the config locations "/simpleTestContext.xml"
    When I send the HTTP request "PUT" "/http"
    Then the request should be handled by "myTestController.httpAction"

  Scenario: Mapping a POST request
    Given I have a web application with the config locations "/simpleTestContext.xml"
    When I send the HTTP request "POST" "/http"
    Then the request should be handled by "myTestController.httpAction"

  Scenario: Mapping a DELETE request
    Given I have a web application with the config locations "/simpleTestContext.xml"
    When I send the HTTP request "DELETE" "/http"
    Then the request should be handled by "myTestController.httpAction"

  Scenario: Mapping a PATCH request
    Given I have a web application with the config locations "/simpleTestContext.xml"
    When I send the HTTP request "PATCH" "/http"
    Then the request should be handled by "myTestController.httpAction"

  Scenario: Mapping a HEAD request
    Given I have a web application with the config locations "/simpleTestContext.xml"
    When I send the HTTP request "HEAD" "/http"
    Then the request should be handled by "myTestController.httpAction"

  Scenario: Mapping a request overriden by its HTTP Header
    Given I have a web application with the config locations "/simpleTestContext.xml"
    When I send the HTTP request "GET" "/overridemethod" with headers:
      | name                   | value  |
      | x-http-method-override | PUT    |
    Then the request should be handled by "myTestController.overrideMethod"

  Scenario: Mapping a request with a number regexp
    Given I have a web application with the config locations "/simpleTestContext.xml"
    When I send the HTTP request "GET" "/regex/42"
    Then the request should be handled by "myTestController.regexNumberAction"

  Scenario: Mapping a request with a string regexp
    Given I have a web application with the config locations "/simpleTestContext.xml"
    When I send the HTTP request "GET" "/regex/marvin"
    Then the request should be handled by "myTestController.regexStringAction"

  Scenario: Mapping a request with a string regexp
    Given I have a web application with the config locations "/simpleTestContext.xml"
    When I send the HTTP request "GET" "/caseinsensitive"
    Then the request should be handled by "myTestController.caseInsensitive"

  Scenario: Mapping a request with a route defined in an another configured file
    Given I have a web application with the config locations "/multiplefilesTestContext.xml"
    When I send the HTTP request "GET" "/additionalroute"
    Then the request should be handled by "myTestController.additionalRouteFile"

  Scenario: Mapping a request with a route defined in a wildcard-configured file
    Given I have a web application with the config locations "/multiplefilesTestContext.xml"
    When I send the HTTP request "GET" "/wildcard-b"
    Then the request should be handled by "myTestController.wildcardB"

  Scenario: Mapping a simple request with a servlet path and a context path
    Given I have a web application configured locations "/simpleTestContext.xml" servletPath "/servlet" contextPath "/context"
    When I send the HTTP request "GET" "/context/servlet/simpleaction"
    Then the request should be handled by "myTestController.simpleAction"

  # Testing issue https://github.com/resthub/springmvc-router/issues/41
  Scenario: Mapping a request to the index with a null pathInfo
    Given I have a web application configured locations "/simpleTestContext.xml" servletPath "/" contextPath "/context"
    When I send the HTTP request "GET" "/context/simpleaction" with a null pathInfo
    Then the request should be handled by "myTestController.simpleAction"