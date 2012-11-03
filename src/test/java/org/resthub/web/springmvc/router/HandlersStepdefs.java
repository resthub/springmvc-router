package org.resthub.web.springmvc.router;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.resthub.web.springmvc.router.support.RouterHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

public class HandlersStepdefs {

    private XmlWebApplicationContext wac;
    private HandlerMapping hm;
    private HandlerAdapter ha;

    private MockHttpServletRequest request;

    private String defaultHost = "example.org";

    private HandlerExecutionChain chain;

    private Logger logger = LoggerFactory.getLogger(HandlersStepdefs.class);

    @Given("^I have a web application with the config locations \"([^\"]*)\"$")
    public void I_have_a_web_applications_with_the_config_locations(String locations) throws Throwable {
        MockServletContext sc = new MockServletContext("");
        this.wac = new XmlWebApplicationContext();
        this.wac.setServletContext(sc);
        this.wac.setConfigLocations(locations.split(","));
        this.wac.refresh();

        this.hm = (HandlerMapping) this.wac.getBean("handlerMapping");
        this.ha = (HandlerAdapter) this.wac.getBean("handlerAdapter");
    }

    @When("^I send the HTTP request \"([^\"]*)\" \"([^\"]*)\"$")
    public void I_send_the_HTTP_request(String method, String url) throws Throwable {

        request = new MockHttpServletRequest(method, url);
        request.addHeader("host", defaultHost);

        chain = this.hm.getHandler(request);
    }

    @When("^I send the HTTP request \"([^\"]*)\" \"([^\"]*)\" to host \"([^\"]*)\"$")
    public void I_send_the_HTTP_request_to_host(String method, String url, String host) throws Throwable {
        request = new MockHttpServletRequest(method, url);
        request.addHeader("host", host);

        chain = this.hm.getHandler(request);
    }

    @When("^I send the HTTP request \"([^\"]*)\" \"([^\"]*)\" with query params:$")
    public void I_send_the_HTTP_request_with_query_params(String method, String url, List<HTTPParam> queryParams) throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest(method, url);

        for (HTTPParam param : queryParams) {
            request.addParameter(param.name, param.value);
        }

        request.addHeader("host", defaultHost);
        chain = this.hm.getHandler(request);
    }

    @When("^I send the HTTP request \"([^\"]*)\" \"([^\"]*)\" with headers:$")
    public void I_send_the_HTTP_request_with_headers(String method, String url, List<HTTPHeader> headers) throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest(method, url);

        for (HTTPHeader header : headers) {
            request.addHeader(header.name, header.value);
        }

        if (request.getHeader("host") == null) {
            request.addHeader("host", defaultHost);
        }

        chain = this.hm.getHandler(request);
    }

    @Then("^no handler should be found$")
    public void no_handler_should_be_found() throws Throwable {

        assertThat(chain).isNull();
    }

    @Then("^the request should be handled by \"([^\"]*)\"$")
    public void the_request_should_be_handled_by(String controllerAction) throws Throwable {

        assertThat(chain).isNotNull();
        RouterHandler handler = (RouterHandler) chain.getHandler();

        assertThat(handler).isNotNull();
        assertThat(handler.getRoute()).isNotNull();
        assertThat(handler.getRoute().action).isNotNull().isEqualToIgnoringCase(controllerAction);
    }


    @Then("^the handler should raise a security exception$")
    public void the_handler_should_raise_a_security_exception() throws Throwable {

        assertThat(chain).isNotNull();
        RouterHandler handler = (RouterHandler) chain.getHandler();

        Exception securityException = null;

        try {
            ha.handle(request, new MockHttpServletResponse(), handler);
        } catch(Exception exc) {
            securityException = exc;
        }

        assertThat(securityException).isNotNull().isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    @Then("^the controller should respond with a ModelAndView containing:$")
    public void the_controller_should_respond_with_a_ModelAndView_containing(List<MaVParams> mavparams) throws Throwable {

        assertThat(chain).isNotNull();
        RouterHandler handler = (RouterHandler) chain.getHandler();

        ModelAndView mv = ha.handle(request, new MockHttpServletResponse(), handler);

        for (MaVParams param : mavparams) {
            assertThat(param.value).isEqualTo(mv.getModel().get(param.key).toString());
        }
    }

    public static class HTTPHeader {
        public String name;
        public String value;
    }

    public static class HTTPParam {
        public String name;
        public String value;
    }

    public static class MaVParams {
        public String key;
        public String value;
    }

}
