package org.resthub.web.springmvc.router.test;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.resthub.web.springmvc.router.HTTPRequestAdapter;
import org.resthub.web.springmvc.router.RouterHandlerMapping;
import org.resthub.web.springmvc.router.hateoas.RouterLinkBuilder;
import org.resthub.web.springmvc.router.support.RouterHandler;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.support.AbstractRefreshableWebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.*;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

public class HandlersStepdefs {

    private AbstractRefreshableWebApplicationContext wac;
    private HandlerMapping hm;
    private HandlerAdapter ha;

    private RouterLinkBuilder linkBuilder;

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

        this.hm = this.wac.getBean(RouterHandlerMapping.class);
        this.ha = this.wac.getBean(RequestMappingHandlerAdapter.class);
    }

    @Given("^I have a web application with javaconfig in package \"([^\"]*)\"$")
    public void I_have_a_web_application_with_javaconfig_in_package(String scanPackage) throws Throwable {
        MockServletContext sc = new MockServletContext("");
        AnnotationConfigWebApplicationContext appContext = new AnnotationConfigWebApplicationContext();
        appContext.scan(scanPackage);
        appContext.setServletContext(sc);
        appContext.refresh();

        this.wac = appContext;

        this.hm = appContext.getBean(RouterHandlerMapping.class);
        this.ha = appContext.getBean(RequestMappingHandlerAdapter.class);
    }

    @Given("^a current request \"([^\"]*)\" \"([^\"]*)\" with servlet path \"([^\"]*)\" and context path \"([^\"]*)\"$")
    public void a_current_request_with_servlet_path_and_context_path(String method, String url, String servletPath, String contextPath) throws Throwable {

        MockServletContext sc = new MockServletContext();
        sc.setContextPath(contextPath);

        int pathLength = 0;
        if(contextPath.length() > 0) {
            pathLength += contextPath.length();
        }

        if(servletPath.length() > 0) {
            pathLength += servletPath.length();
        }

        request = new MockHttpServletRequest(sc, method, url);
        request.setContextPath(contextPath);
        request.setServletPath(servletPath);
        request.addHeader("host", host);

        request.setPathInfo(url.substring(pathLength));

	    ServletRequestAttributes requestAttributes = new ServletRequestAttributes(request);
	    RequestContextHolder.setRequestAttributes(requestAttributes);

        HTTPRequestAdapter.parseRequest(request);
    }



    @When("^I send the HTTP request \"([^\"]*)\" \"([^\"]*)\"$")
    public void I_send_the_HTTP_request(String method, String url) throws Throwable {

        int pathLength = 0;
        if(this.contextPath.length() > 0) {
            pathLength += this.contextPath.length();
        }

        if(this.servletPath.length() > 0) {
            pathLength += this.servletPath.length();
        }

        request = new MockHttpServletRequest(this.wac.getServletContext(),method, url);
        request.setContextPath(this.contextPath);
        request.setServletPath(this.servletPath);
        request.addHeader("host", host);

	    ServletRequestAttributes requestAttributes = new ServletRequestAttributes(request);
	    RequestContextHolder.setRequestAttributes(requestAttributes);

        for (HTTPHeader header : headers) {
            request.addHeader(header.name, header.value);
        }

        for (HTTPParam param : queryParams) {
            request.addParameter(param.name, param.value);
        }

        request.setPathInfo(url.substring(pathLength));
        chain = this.hm.getHandler(request);
    }

    @When("^I send the HTTP request \"([^\"]*)\" \"([^\"]*)\" with a null pathInfo$")
    public void I_send_the_HTTP_request_with_a_null_pathInfo(String method, String url) throws Throwable {

        request = new MockHttpServletRequest(this.wac.getServletContext());
        request.setMethod(method);
        request.setContextPath(this.contextPath);
        request.setServletPath(url.replaceFirst(this.contextPath,""));
        request.addHeader("host", host);

        for (HTTPHeader header : headers) {
            request.addHeader(header.name, header.value);
        }

        for (HTTPParam param : queryParams) {
            request.addParameter(param.name, param.value);
        }

        request.setPathInfo(null);

	    ServletRequestAttributes requestAttributes = new ServletRequestAttributes(request);
	    RequestContextHolder.setRequestAttributes(requestAttributes);

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

    @When("^I build a link for controller \"([^\"]*)\" and action \"([^\"]*)\"$")
    public void I_build_a_link_for_controller_and_action(String controller, String action) throws Throwable {

         linkBuilder = RouterLinkBuilder.linkTo(controller,action);
    }

    @When("^I add an argument named \"([^\"]*)\" with value \"([^\"]*)\"$")
    public void I_add_a_argument_named_with_value(String name, String value) throws Throwable {
        linkBuilder = linkBuilder.slash(name, value);
    }

    @When("^I add an argument \"([^\"]*)\"$")
    public void I_add_a_argument(String argument) throws Throwable {
        linkBuilder = linkBuilder.slash(argument);
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

    @Then("^the server should send an HTTP response with status \"([^\"]*)\"$")
    public void the_server_should_send_an_HTTP_response_with_status(int status) throws Throwable {

        RouterHandler handler = null;
        MockHttpServletResponse response = new MockHttpServletResponse();

        if(chain != null) {
            handler = (RouterHandler) chain.getHandler();
        }

        HandlerInterceptor[] interceptors = chain.getInterceptors();

        for(HandlerInterceptor interceptor : Arrays.asList(interceptors)) {
            interceptor.preHandle(request,response, handler);
        }

        ha.handle(request, response, handler);
        assertThat(response.getStatus()).isEqualTo(status);
    }

    @Then("^the raw link should be \"([^\"]*)\"$")
    public void the_raw_link_should_be(String link) throws Throwable {

        assertThat(linkBuilder.toString()).isEqualTo(link);
    }

    @Then("^the self rel link should be \"(.*)\"$")
    public void the_self_rel_link_should_be(String link) throws Throwable {

        assertThat(linkBuilder.withSelfRel().toString()).isEqualTo(link);
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
