package org.resthub.web.springmvc.router;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.resthub.web.springmvc.router.exceptions.NoHandlerFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;

public class ReverseRoutingStepdefs {

    private Router.ActionDefinition resolvedAction;

    private Exception thrownException;

    @Given("^an empty Router$")
    public void an_empty_Router() throws Throwable {
        Router.clear();
    }

    @Given("^I have a route with method \"([^\"]*)\" path \"([^\"]*)\" action \"([^\"]*)\"$")
    public void I_have_a_route_with_method_url_action(String method, String path, String action) throws Throwable {
        Router.prependRoute(method, path, action);
    }

    @Given("^I have routes:$")
    public void I_have_routes(List<RouteItem> routes) throws Throwable {
        for(RouteItem item : routes) {
            Router.addRoute(item.method,item.path,item.action,item.params,null);
        }
    }

    @When("^I try to reverse route \"([^\"]*)\" with params:$")
    public void I_try_to_reverse_route_with_params(String path, List<ParamItem> params) throws Throwable {
        Map<String,Object> routeParams = new HashMap<String, Object>();
        for(ParamItem param : params) {
              routeParams.put(param.key,param.value);
        }
        try {
            resolvedAction = Router.reverse(path,routeParams);
        } catch(Exception exc) {
            this.thrownException = exc;
        }
    }

    @When("^I try to reverse route \"([^\"]*)\"$")
    public void I_try_to_reverse_route(String action) throws Throwable {
        resolvedAction = Router.reverse(action);
    }

    @Then("^I should get an action with path \"([^\"]*)\"$")
    public void I_should_get_an_action_with_URL(String path) throws Throwable {
         assertThat(path).isEqualTo(resolvedAction.url);
    }

    @Then("^I should get an action with path \"([^\"]*)\" and host \"([^\"]*)\"$")
    public void I_should_get_an_action_with_path_and_host(String path, String host) throws Throwable {
        assertThat(path).isEqualTo(resolvedAction.url);
        assertThat(host).isEqualTo(resolvedAction.host);
    }

    @Then("^no action should match$")
    public void no_action_should_match() throws Throwable {
        assertThat(this.thrownException).isNotNull().isInstanceOf(NoHandlerFoundException.class);
    }

    public static class RouteItem {
        public String method;
        public String path;
        public String action;
        public String params;
    }

    public static class ParamItem {
        public String key;
        public String value;
    }
}
