package org.springframework.web.servlet.mvc.router;

import javax.inject.Inject;
import javax.inject.Named;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.web.servlet.mvc.router.Router.Route;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:routerTestContext.xml" })
public class RouterHandlerMappingTest {

	private String LOCATION = "/routerTestContext.xml";
	
	private XmlWebApplicationContext wac;
	
	@Inject
	@Named("myTestController")
	private MyTestController testController;
	
	private String handlerName = "myTestController";
	
	private HandlerMapping hm;
	
	/**
	 * Setup a MockServletContext configured by routerTestContext.xml
	 */
	@Before
	public void setUp() {

		MockServletContext sc = new MockServletContext("");
		this.wac = new XmlWebApplicationContext();
		this.wac.setServletContext(sc);
		this.wac.setConfigLocations(new String[] {LOCATION});
		this.wac.refresh();
		
		this.hm = (HandlerMapping) this.wac.getBean("handlerMapping");
	}
	
	/**
	 * Test route:
	 * GET     /simpleaction    myTestController.simpleAction
	 * @throws Exception
	 */
	@Test
	public void testSimpleRoute() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/simpleaction");
		request.addHeader("host", "mairie-test.fr");
		HandlerExecutionChain chain = this.hm.getHandler(request);
		
		RouterHandler handler = (RouterHandler)chain.getHandler();
		Assert.assertNotNull(handler);
		
		Route route = handler.getRoute();
		Assert.assertNotNull(route);
		Assert.assertEquals(this.handlerName+".simpleAction", route.action);
	}
	
	/**
	 * Test route:
	 * GET      /param     myTestController.paramAction(param:'default')
	 * @throws Exception
	 */
	@Test
	public void testDefaultParamAction() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/param");
		request.addHeader("host", "mairie-test.fr");
		HandlerExecutionChain chain = this.hm.getHandler(request);
		
		RouterHandler handler = (RouterHandler)chain.getHandler();
		Assert.assertNotNull(handler);
		
		Route route = handler.getRoute();
		Assert.assertNotNull(route);
		Assert.assertEquals(this.handlerName+".paramAction", route.action);
		
		HTTPRequestAdapter req = handler.getRequest();
		Assert.assertNotNull(req);
		Assert.assertEquals(req.routeArgs.get("param"), "default");
	}
	
	/**
	 * Test route:
	 * GET     /param/{param}      myTestController.paramAction
	 * @throws Exception
	 */
	@Test
	public void testParamAction() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/param/myparam");
		request.addHeader("host", "mairie-test.fr");
		HandlerExecutionChain chain = this.hm.getHandler(request);
		
		RouterHandler handler = (RouterHandler)chain.getHandler();
		Assert.assertNotNull(handler);
		
		Route route = handler.getRoute();
		Assert.assertNotNull(route);
		Assert.assertEquals(this.handlerName+".paramAction", route.action);
		
		HTTPRequestAdapter req = handler.getRequest();
		Assert.assertNotNull(req);
		Assert.assertEquals(req.routeArgs.get("param"), "myparam");
	}	

	/**
	 * Test route:
	 * GET     /http       myTestController.httpAction(type:'GET')
	 * @throws Exception
	 */
	@Test
	public void testGETHTTPAction() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/http");
		request.addHeader("host", "mairie-test.fr");
		HandlerExecutionChain chain = this.hm.getHandler(request);
		
		RouterHandler handler = (RouterHandler)chain.getHandler();
		Assert.assertNotNull(handler);
		
		Route route = handler.getRoute();
		Assert.assertNotNull(route);
		Assert.assertEquals(this.handlerName+".httpAction", route.action);
		
		HTTPRequestAdapter req = handler.getRequest();
		Assert.assertNotNull(req);
		Assert.assertEquals(req.routeArgs.get("type"), "GET");
	}

	/**
	 * Test route:
	 * PUT     /http       myTestController.httpAction(type:'PUT')
	 * @throws Exception
	 */
	@Test
	public void testPUTHTTPAction() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/http");
		request.addHeader("host", "mairie-test.fr");
		HandlerExecutionChain chain = this.hm.getHandler(request);
		
		RouterHandler handler = (RouterHandler)chain.getHandler();
		Assert.assertNotNull(handler);
		
		Route route = handler.getRoute();
		Assert.assertNotNull(route);
		Assert.assertEquals(this.handlerName+".httpAction", route.action);
		
		HTTPRequestAdapter req = handler.getRequest();
		Assert.assertNotNull(req);
		Assert.assertEquals(req.routeArgs.get("type"), "PUT");
	}
	
	
	/**
	 * Test route:
	 * POST     /http       myTestController.httpAction(type:'POST')
	 * @throws Exception
	 */
	@Test
	public void testPOSTHTTPAction() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/http");
		request.addHeader("host", "mairie-test.fr");
		HandlerExecutionChain chain = this.hm.getHandler(request);
		
		RouterHandler handler = (RouterHandler)chain.getHandler();
		Assert.assertNotNull(handler);
		
		Route route = handler.getRoute();
		Assert.assertNotNull(route);
		Assert.assertEquals(this.handlerName+".httpAction", route.action);
		
		HTTPRequestAdapter req = handler.getRequest();
		Assert.assertNotNull(req);
		Assert.assertEquals(req.routeArgs.get("type"), "POST");
	}

	
	/**
	 * Test route:
	 * DELETE     /http       myTestController.httpAction(type:'DELETE')
	 * @throws Exception
	 */
	@Test
	public void testDELETEHTTPAction() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/http");
		request.addHeader("host", "mairie-test.fr");
		HandlerExecutionChain chain = this.hm.getHandler(request);
		
		RouterHandler handler = (RouterHandler)chain.getHandler();
		Assert.assertNotNull(handler);
		
		Route route = handler.getRoute();
		Assert.assertNotNull(route);
		Assert.assertEquals(this.handlerName+".httpAction", route.action);
		
		HTTPRequestAdapter req = handler.getRequest();
		Assert.assertNotNull(req);
		Assert.assertEquals(req.routeArgs.get("type"), "DELETE");
	}

	
	/**
	 * Test route:
	 * GET     /regex/{<[0-9]+>number}          myTestController.regexNumberAction
	 * @throws Exception
	 */
	@Test
	public void testregexNumberAction() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/regex/42");
		request.addHeader("host", "mairie-test.fr");
		HandlerExecutionChain chain = this.hm.getHandler(request);
		
		RouterHandler handler = (RouterHandler)chain.getHandler();
		Assert.assertNotNull(handler);
		
		Route route = handler.getRoute();
		Assert.assertNotNull(route);
		Assert.assertEquals(this.handlerName+".regexNumberAction", route.action);
		
		HTTPRequestAdapter req = handler.getRequest();
		Assert.assertNotNull(req);
		Assert.assertEquals(req.routeArgs.get("number"), "42");
	}
	
	/**
	 * Test route:
	 * GET     /regex/{<[a-z]+>string}      myTestController.regexStringAction
	 * @throws Exception
	 */
	@Test
	public void testregexStringAction() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/regex/marvin");
		request.addHeader("host", "mairie-test.fr");
		HandlerExecutionChain chain = this.hm.getHandler(request);
		
		RouterHandler handler = (RouterHandler)chain.getHandler();
		Assert.assertNotNull(handler);
		
		Route route = handler.getRoute();
		Assert.assertNotNull(route);
		Assert.assertEquals(this.handlerName+".regexStringAction", route.action);
		
		HTTPRequestAdapter req = handler.getRequest();
		Assert.assertNotNull(req);
		Assert.assertEquals(req.routeArgs.get("string"), "marvin");
	}
	
	
	/**
	 * Test route:
	 * GET     /host          myTestController.hostAction
	 * @throws Exception
	 */
	@Test
	public void testhostAction() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/host");
		request.addHeader("host", "mairie-test.fr");
		HandlerExecutionChain chain = this.hm.getHandler(request);
		
		RouterHandler handler = (RouterHandler)chain.getHandler();
		Assert.assertNotNull(handler);
		
		Route route = handler.getRoute();
		Assert.assertNotNull(route);
		Assert.assertEquals(this.handlerName+".hostAction", route.action);
		
		HTTPRequestAdapter req = handler.getRequest();
		Assert.assertNotNull(req);
		Assert.assertEquals(req.host, "mairie-test.fr");
	}

}
