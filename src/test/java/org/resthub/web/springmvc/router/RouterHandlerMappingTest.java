package org.resthub.web.springmvc.router;

import javax.inject.Inject;
import javax.inject.Named;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.resthub.web.springmvc.router.Router.Route;
import org.resthub.web.springmvc.router.support.RouterHandler;
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
        
        private String sampleHost = "samplehost.org";
	
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
		request.addHeader("host", sampleHost);
		HandlerExecutionChain chain = this.hm.getHandler(request);
		
		RouterHandler handler = (RouterHandler)chain.getHandler();
		Assert.assertNotNull(handler);
		
		Route route = handler.getRoute();
		Assert.assertNotNull(route);
		Assert.assertEquals(this.handlerName+".simpleAction", route.action);
	}
        
        /**
	 * Test route:
	 * GET     /additionalroute       myTestController.additionalRouteFile
	 * @throws Exception
	 */
	@Test
	public void additionalRouteFile() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/additionalroute");
		request.addHeader("host", sampleHost);
		HandlerExecutionChain chain = this.hm.getHandler(request);
		
		RouterHandler handler = (RouterHandler)chain.getHandler();
		Assert.assertNotNull(handler);
		
		Route route = handler.getRoute();
		Assert.assertNotNull(route);
		Assert.assertEquals(this.handlerName+".additionalRouteFile", route.action);
	}
        
        /**
	 * Test route:
	 * GET     /caseinsensitive            MyTestCONTROLLER.caseInsensitive
	 * @throws Exception
	 */
	@Test
	public void caseInsensitiveRoute() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/caseinsensitive");
		request.addHeader("host", sampleHost);
		HandlerExecutionChain chain = this.hm.getHandler(request);
		
		RouterHandler handler = (RouterHandler)chain.getHandler();
		Assert.assertNotNull(handler);
		
		Route route = handler.getRoute();
		Assert.assertNotNull(route);
		Assert.assertTrue((this.handlerName+".caseInsensitive").equalsIgnoreCase(route.action));
	}
        
        /**
	 * Test routes:
	 * GET     /wildcard-a       myTestController.wildcardA
         * GET     /wildcard-b       myTestController.wildcardB
	 * @throws Exception
	 */
	@Test
	public void wildcardRouteFiles() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/wildcard-a");
		request.addHeader("host", sampleHost);
		HandlerExecutionChain chain = this.hm.getHandler(request);
		
		RouterHandler handler = (RouterHandler)chain.getHandler();
		Assert.assertNotNull(handler);
		
		Route route = handler.getRoute();
		Assert.assertNotNull(route);
		Assert.assertEquals(this.handlerName+".wildcardA", route.action);
                
		request = new MockHttpServletRequest("GET", "/wildcard-b");
		request.addHeader("host", sampleHost);
		chain = this.hm.getHandler(request);
		
		handler = (RouterHandler)chain.getHandler();
		Assert.assertNotNull(handler);
		
		route = handler.getRoute();
		Assert.assertNotNull(route);
		Assert.assertEquals(this.handlerName+".wildcardB", route.action);                
	}        
        
	/**
	 * Test route with HTTP method overriding (HEAD -> GET):
	 * GET     /simpleaction    myTestController.simpleAction
	 * @throws Exception
	 */
	@Test
	public void testHeadMethod() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest("HEAD", "/simpleaction");
		request.addHeader("host", sampleHost);
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
		request.addHeader("host", sampleHost);
		HandlerExecutionChain chain = this.hm.getHandler(request);
		
		RouterHandler handler = (RouterHandler)chain.getHandler();
		Assert.assertNotNull(handler);
		
		Route route = handler.getRoute();
		Assert.assertNotNull(route);
		Assert.assertEquals(this.handlerName+".paramAction", route.action);
	}
	
	/**
	 * Test route:
	 * GET     /param/{param}      myTestController.paramAction
	 * @throws Exception
	 */
	@Test
	public void testParamAction() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/param/myparam");
		request.addHeader("host", sampleHost);
		HandlerExecutionChain chain = this.hm.getHandler(request);
		
		RouterHandler handler = (RouterHandler)chain.getHandler();
		Assert.assertNotNull(handler);
		
		Route route = handler.getRoute();
		Assert.assertNotNull(route);
		Assert.assertEquals(this.handlerName+".paramAction", route.action);
	}	

	/**
	 * Test route:
	 * GET     /http       myTestController.httpAction(type:'GET')
	 * @throws Exception
	 */
	@Test
	public void testGETHTTPAction() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/http");
		request.addHeader("host", sampleHost);
		HandlerExecutionChain chain = this.hm.getHandler(request);
		
		RouterHandler handler = (RouterHandler)chain.getHandler();
		Assert.assertNotNull(handler);
		
		Route route = handler.getRoute();
		Assert.assertNotNull(route);
		Assert.assertEquals(this.handlerName+".httpAction", route.action);
	}

	/**
	 * Test route:
	 * PUT     /http       myTestController.httpAction(type:'PUT')
	 * @throws Exception
	 */
	@Test
	public void testPUTHTTPAction() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/http");
		request.addHeader("host", sampleHost);
		HandlerExecutionChain chain = this.hm.getHandler(request);
		
		RouterHandler handler = (RouterHandler)chain.getHandler();
		Assert.assertNotNull(handler);
		
		Route route = handler.getRoute();
		Assert.assertNotNull(route);
		Assert.assertEquals(this.handlerName+".httpAction", route.action);
	}
	
	
	/**
	 * Test route:
	 * POST     /http       myTestController.httpAction(type:'POST')
	 * @throws Exception
	 */
	@Test
	public void testPOSTHTTPAction() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/http");
		request.addHeader("host", sampleHost);
		HandlerExecutionChain chain = this.hm.getHandler(request);
		
		RouterHandler handler = (RouterHandler)chain.getHandler();
		Assert.assertNotNull(handler);
		
		Route route = handler.getRoute();
		Assert.assertNotNull(route);
		Assert.assertEquals(this.handlerName+".httpAction", route.action);
	}

	
	/**
	 * Test route:
	 * DELETE     /http       myTestController.httpAction(type:'DELETE')
	 * @throws Exception
	 */
	@Test
	public void testDELETEHTTPAction() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/http");
		request.addHeader("host", sampleHost);
		HandlerExecutionChain chain = this.hm.getHandler(request);
		
		RouterHandler handler = (RouterHandler)chain.getHandler();
		Assert.assertNotNull(handler);
		
		Route route = handler.getRoute();
		Assert.assertNotNull(route);
		Assert.assertEquals(this.handlerName+".httpAction", route.action);
	}

        /**
	 * Test route:
	 * PUT     /http       myTestController.httpAction(type:'PUT')
         * with a GET request and a "x-http-method-override" header
	 * @throws Exception
	 */
	@Test
	public void testOverrideHeaderHTTPAction() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/http");
		request.addHeader("host", sampleHost);
                request.addHeader("x-http-method-override","PUT");
		HandlerExecutionChain chain = this.hm.getHandler(request);
		
		RouterHandler handler = (RouterHandler)chain.getHandler();
		Assert.assertNotNull(handler);
		
		Route route = handler.getRoute();
		Assert.assertNotNull(route);
		Assert.assertEquals(this.handlerName+".httpAction", route.action);
	}
        
        /**
	 * Test route:
	 * PUT     /http       myTestController.httpAction(type:'PUT')
         * with a GET request and a "x-http-method-override" argument in queryString
	 * @throws Exception
	 */
	@Test
	public void testOverrideQueryStringHTTPAction() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/http");
                request.setQueryString("x-http-method-override=PUT");
                request.addHeader("host", sampleHost);
		HandlerExecutionChain chain = this.hm.getHandler(request);
		
		RouterHandler handler = (RouterHandler)chain.getHandler();
		Assert.assertNotNull(handler);
		
		Route route = handler.getRoute();
		Assert.assertNotNull(route);
		Assert.assertEquals(this.handlerName+".httpAction", route.action);
	}        
        
	
	/**
	 * Test route:
	 * GET     /regex/{<[0-9]+>number}          myTestController.regexNumberAction
	 * @throws Exception
	 */
	@Test
	public void testregexNumberAction() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/regex/42");
		request.addHeader("host", sampleHost);
		HandlerExecutionChain chain = this.hm.getHandler(request);
		
		RouterHandler handler = (RouterHandler)chain.getHandler();
		Assert.assertNotNull(handler);
		
		Route route = handler.getRoute();
		Assert.assertNotNull(route);
		Assert.assertEquals(this.handlerName+".regexNumberAction", route.action);
	}
	
	/**
	 * Test route:
	 * GET     /regex/{<[a-z]+>string}      myTestController.regexStringAction
	 * @throws Exception
	 */
	@Test
	public void testregexStringAction() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/regex/marvin");
		request.addHeader("host", sampleHost);
		HandlerExecutionChain chain = this.hm.getHandler(request);
		
		RouterHandler handler = (RouterHandler)chain.getHandler();
		Assert.assertNotNull(handler);
		
		Route route = handler.getRoute();
		Assert.assertNotNull(route);
		Assert.assertEquals(this.handlerName+".regexStringAction", route.action);
	}
	
	
	/**
	 * Test route:
	 * GET     /host          myTestController.hostAction
	 * @throws Exception
	 */
	@Test
	public void testhostAction() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/host");
		request.addHeader("host", sampleHost);
		HandlerExecutionChain chain = this.hm.getHandler(request);
		
		RouterHandler handler = (RouterHandler)chain.getHandler();
		Assert.assertNotNull(handler);
		
		Route route = handler.getRoute();
		Assert.assertNotNull(route);
		Assert.assertEquals(this.handlerName+".hostAction", route.action);
	}

}
