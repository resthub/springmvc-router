Feature: Reverse routing support
  As a developer coding a Controller
  I want to do reverse routing
  In order to generate URLs within my application

  Background:
    Given an empty Router

  Scenario: Reverse routing a simple URL
    Given I have a route with method "GET" path "/simpleaction" action "myTestController.simpleAction"
    When I try to reverse route "myTestController.simpleAction"
    Then I should get an action with path "/simpleaction"

  Scenario: Reverse routing an URL with params
    Given I have routes:
      | method | path             | action                       | params          |
      | GET    | /param           | myTestController.paramAction |                 |
      | GET    | /param/{param}   | myTestController.paramAction | param:'default' |
    When I try to reverse route "myTestController.paramAction" with params:
      | key   | value     |
      | param | testparam |
    Then I should get an action with path "/param?param=testparam"