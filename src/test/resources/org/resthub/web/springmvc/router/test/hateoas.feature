Feature: HATEOAS support
  As a developer coding a application
  I want to use a LinkBuilder
  In order to build HATEOAS links

  Scenario: Reverse routing a simple link using the linkbuilder
    Given I have a web application with the config locations "/simpleTestContext.xml"
    When I build a link for controller "myTestController" and action "simpleAction"
    Then the raw link should be "http://example.org/context/servlet/simpleaction"

  Scenario: Build a self rel link to a simple route
    Given I have a web application with the config locations "/simpleTestContext.xml"
    When I build a link for controller "myTestController" and action "simpleAction"
    Then the self rel link should be "{ rel : self, href : http://example.org/context/servlet/simpleaction }"

  Scenario: Build a self rel link to a route with a named argument
    Given I have a web application with the config locations "/simpleTestContext.xml"
    When I build a link for controller "myTestController" and action "regexStringAction"
    And I add an argument named "string" with value "testlinkbuilder"
    Then the raw link should be "http://example.org/context/servlet/regex/testlinkbuilder"

  Scenario: Build a self rel link to a route with a named argument
    Given I have a web application with the config locations "/simpleTestContext.xml"
    When I build a link for controller "myTestController" and action "regexStringAction"
    And I add an argument "testlinkbuilder"
    Then the raw link should be "http://example.org/context/servlet/regex/testlinkbuilder"
