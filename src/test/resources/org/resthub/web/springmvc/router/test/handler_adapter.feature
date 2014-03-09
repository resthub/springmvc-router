Feature: Handler adapter support
  As a developer coding a application
  I want HTTP requests to be processed by controllers
  In order to map HTTP responses back to the client

  Scenario: Binding a string within the HTTP request
    Given I have a web application with the config locations "/bindingTestContext.xml"
    When I send the HTTP request "GET" "/bind/name/test"
    Then the controller should respond with a ModelAndView containing:
      | key  | value  |
      | name | test   |

  Scenario: Binding a Long within the HTTP request
    Given I have a web application with the config locations "/bindingTestContext.xml"
    When I send the HTTP request "POST" "/bind/id/42"
    Then the controller should respond with a ModelAndView containing:
      | key | value |
      | id  | 42    |

  Scenario: Binding a regexp and a hostname within the HTTP request
    Given I have a web application with the config locations "/bindingTestContext.xml"
    When I send the HTTP request "GET" "/bind/hostslug/my-slug-number-1"
    Then the controller should respond with a ModelAndView containing:
      | key       | value             |
      | hostname  | example.org       |
      | slug      | my-slug-number-1  |

  Scenario: Binding a specific host within the HTTP request
    Given I have a web application with the config locations "/bindingTestContext.xml"
    When I send the HTTP request "GET" "/bind/specifichost" to host "myhost.com"
    Then the controller should respond with a ModelAndView containing:
      | key     | value             |
      | host    | specific          |

  Scenario: Checking routes bound to a specific host won't work with other hosts
    Given I have a web application with the config locations "/bindingTestContext.xml"
    When I send the HTTP request "GET" "/bind/specifichost" to host "myotherhost.com"
    Then no handler should be found

  Scenario: Binding a subdomain within the HTTP request
    Given I have a web application with the config locations "/bindingTestContext.xml"
    When I send the HTTP request "GET" "/bind/regexphost" to host "myhost.domain.org"
    Then the controller should respond with a ModelAndView containing:
      | key          | value      |
      | subdomain    | myhost     |

  Scenario: Binding modelattributes within the HTTP request
    Given I have a web application with the config locations "/bindingTestContext.xml"
    When I send the HTTP request "GET" "/bind/modelattribute" to host "myhost.domain.org"
    Then the controller should respond with a ModelAndView containing:
      | key                             | value      |
      | simpleModelAttributeOnMethod    | true       |
      | firstModelAttributeOnMethod     | true       |
      | secondModelAttributeOnMethod    | true       |

  Scenario: Adding the best matched pattern to the request
    Given I have a web application with the config locations "/bindingTestContext.xml"
    When I send the HTTP request "GET" "/bestpattern/55"
    Then the controller should respond with a ModelAndView containing:
      | key      | value                           |
      | pattern  | /bestpattern/({value}[0-9]+)    |
      | value    | 55                              |

  Scenario: Checking that secured routes are protected
    Given I have a web application with the config locations "/bindingTestContext.xml,/securityContext.xml"
    When I send the HTTP request "GET" "/security/test" to host "myotherhost.com"
    Then the handler should raise a security exception

  Scenario: Binding a regexp and queryParam within the HTTP request
    Given I have a web application with the config locations "/bindingTestContext.xml"
    When I send the HTTP request "DELETE" "/bind/slug/my-slug-number-1" with query params:
      | name  | value               |
      | hash  | slughash            |
    Then the controller should respond with a ModelAndView containing:
      | key   | value               |
      | slug  | my-slug-number-1    |
      | hash  | slughash            |