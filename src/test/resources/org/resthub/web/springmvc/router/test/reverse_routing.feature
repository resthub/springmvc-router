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

  Scenario: Reverse routing a simple URL with PATCH method
    Given I have a route with method "PATCH" path "/simpleaction" action "myTestController.simpleAction"
    When I try to reverse route "myTestController.simpleAction"
    Then I should get an action with path "/simpleaction"

  Scenario: Reverse routing an URL with params
    Given I have routes:
      | method | path             | action                       | params            |
      | GET    | /param           | myTestController.paramAction | (param:'default') |
      | GET    | /param/{param}   | myTestController.paramAction |                   |
    When I try to reverse route "myTestController.paramAction" with params:
      | key   | value     |
      | param | testparam |
    Then I should get an action with path "/param/testparam"

  Scenario: Reverse routing an URL with a regexp
    Given I have routes:
      | method | path                               | action                          | params  |
      | GET    | /bind/slug/{<[a-z0-9\-_]+>slug}    | myTestController.bindSlugAction |         |
    When I try to reverse route "myTestController.bindSlugAction" with params:
      | key   | value     |
      | slug  | slug_01   |
    Then I should get an action with path "/bind/slug/slug_01"

  Scenario: Reverse routing an URL with a regexp
    Given I have routes:
      | method | path                               | action                          | params  |
      | GET    | /bind/slug/{<[a-z0-9\-_]+>slug}    | myTestController.bindSlugAction |         |
    When I try to reverse route "myTestController.bindSlugAction" with params:
      | key   | value     |
      | slug  | slug_01   |
    Then I should get an action with path "/bind/slug/slug_01"

  Scenario: Reverse routing an URL with a regexp
    Given I have routes:
      | method | path                           | action                          | params  |
      | GET    | /bind/name/{<[a-z]+>myName}    | myTestController.bindNameAction |         |
    When I try to reverse route "myTestController.bindNameAction" with params:
      | key   | value    |
      | name  | name01   |
    Then no action should match

  Scenario: Reverse routing an URL and a given domain
    Given I have routes:
      | method | path                     | action                          | params  |
      | GET    | {host}/bind/host         | myTestController.bindHostAction |         |
    When I try to reverse route "myTestController.bindHostAction" with params:
      | key   | value         |
      | host  | example.org   |
    Then I should get an action with path "/bind/host" and host "example.org"

  Scenario: Reverse routing an URL and a specific domain
    Given I have routes:
      | method | path                            | action                                  | params  |
      | GET    | myhost.com/bind/specifichost    | myTestController.bindSpecificHostAction |         |
    When I try to reverse route "myTestController.bindSpecificHostAction"
    Then I should get an action with path "/bind/specifichost" and host "myhost.com"

  Scenario: Reverse routing an URL and a given subdomain
    Given I have routes:
      | method | path                                     | action                                | params  |
      | GET    | {subdomain}.domain.org/bind/regexphost   | myTestController.bindRegexpHostAction |         |
    When I try to reverse route "myTestController.bindRegexpHostAction" with params:
      | key        | value  |
      | subdomain  | sub    |
    Then I should get an action with path "/bind/regexphost" and host "sub.domain.org"

  Scenario: Reverse routing a simple URL with a servlet path and context path
    Given I have a route with method "GET" path "/simpleaction" action "myTestController.simpleAction"
    And the current request is processed within a context path "context" and servlet path "servlet"
    When I try to reverse route "myTestController.simpleAction"
    Then I should get an action with path "/context/servlet/simpleaction"

  Scenario: Reverse routing a simple URL with servlet and context paths prepended by slash
    Given I have a route with method "GET" path "/simpleaction" action "myTestController.simpleAction"
    And the current request is processed within a context path "/context" and servlet path "/servlet"
    When I try to reverse route "myTestController.simpleAction"
    Then I should get an action with path "/context/servlet/simpleaction"