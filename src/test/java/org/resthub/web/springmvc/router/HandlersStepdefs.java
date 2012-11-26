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

import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

public class HandlersStepdefs {

    private XmlWebApplicationContext wac;
    private HandlerMapping hm;
    private HandlerAdapter ha;

    private String servletPath = "";
    private String contextPath = "";
    private List<HTTPParam> queryParams = new ArrayList<HTTPParam>();
    private List<HTTPHeader> headers = new ArrayList<HTTPHeader>();

    private MockHttpServletRequest request;

    private String host = "example.org";

    private HandlerExecutionChain chain;

    @Given("^I have a web application with the config locations \"([^\"]*)\"$")
    public void I_have_a_web_applications_with_the_config_locations(String locations) throws Throwable {
        I_have_a_web_application_configured_locations_servletPath_contextPath(locations,"","");
    }


    @Given("^I have a web application configured locations \"([^\"]*)\" servletPath \"([^\"]*)\" contextPath \"([^\"]*)\"$")
    public void I_have_a_web_application_configured_locations_servletPath_contextPath(String locations, String servletPath, String contextPath) throws Throwable {

        this.servletPath = servletPath;
        this.contextPath = contextPath;

        MockServletContext sc = new MockServletContext();
        sc.setContextPath(contextPath);

        this.wac = new XmlWebApplicationContext();
        this.wac.setServletContext(sc);
        this.wac.setConfigLocations(locations.split(","));
        this.wac.refresh();

        this.hm = (HandlerMapping) this.wac.getBean("handlerMapping");
        this.ha = (HandlerAdapter) this.wac.getBean("handlerAdapter");
    }



    @When("^I send the HTTP request \"([^\"]*)\" \"([^\"]*)\"$")
    public void I_send_the_HTTP_request(String method, String url) throws Throwable {

        int pathLength = 0;
        if(this.contextPath.length() > 0) {
            pathLength += this.contextPath.length() + 1;
        }

        if(this.servletPath.length() > 0) {
            pathLength += this.servletPath.length() + 1;
        }

        request = new MockHttpServletRequest(this.wac.getServletContext(),method, url);
        request.setContextPath(this.contextPath);
        request.setServletPath(this.servletPath);
        request.addHeader("host", host);

        for (HTTPHeader header : headers) {
            request.addHeader(header.name, header.value);
        }

        for (HTTPParam param : queryParams) {
            request.addParameter(param.name, param.value);
        }

        request.setPathInfo(url.substring(pathLength));
        chain = this.hm.getHandler(request);
    }

    @When("^I send the HTTP request \"([^\"]*)\" \"([^\"]*)\" to host \"([^\"]*)\"$")
    public void I_send_the_HTTP_request_to_host(String method, String url, String host) throws Throwable {

        this.host = host;
        I_send_the_HTTP_request(method,url);
    }

    @When("^I send the HTTP request \"([^\"]*)\" \"([^\"]*)\" with query params:$")
    public void I_send_the_HTTP_request_with_query_params(String method, String url, List<HTTPParam> queryParams) throws Throwable {

        this.queryParams = queryParams;
        I_send_the_HTTP_request(method,url);
    }

    @When("^I send the HTTP request \"([^\"]*)\" \"([^\"]*)\" with headers:$")
    public void I_send_the_HTTP_request_with_headers(String method, String url, List<HTTPHeader> headers) throws Throwable {

        this.headers = headers;
        I_send_the_HTTP_request(method,url);
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
