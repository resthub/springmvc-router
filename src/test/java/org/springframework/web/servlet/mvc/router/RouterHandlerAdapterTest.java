package org.springframework.web.servlet.mvc.router;

import javax.inject.Inject;
import javax.inject.Named;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:routerTestContext.xml"})
public class RouterHandlerAdapterTest {

    private String ROUTERCONTEXT_LOCATION = "/routerTestContext.xml";
    private String SECURITYCONTEXT_LOCATION = "/securityContext.xml";
    private XmlWebApplicationContext wac;
    @Inject
    @Named("bindTestController")
    private BindTestController bindController;
    private String handlerName = "bindTestController";
    private HandlerMapping hm;
    private HandlerAdapter ha;
    private static final String TEST_STRING = "test";
    private static final Long TEST_LONG = 42L;
    private static final String TEST_SLUG = "my-slug-number-1";
    private static final String TEST_SLUG_HASH = "slughash";
    private static final String TEST_HOST = "example.org";

    /**
     * Setup a MockServletContext configured by routerTestContext.xml
     */
    @Before
    public void setUp() {

        MockServletContext sc = new MockServletContext("");
        this.wac = new XmlWebApplicationContext();
        this.wac.setServletContext(sc);
        this.wac.setConfigLocations(new String[]{ROUTERCONTEXT_LOCATION, SECURITYCONTEXT_LOCATION});
        this.wac.refresh();

        this.hm = (HandlerMapping) this.wac.getBean("handlerMapping");
        this.ha = (HandlerAdapter) this.wac.getBean("handlerAdapter");
    }

    private ModelAndView handleRequest(MockHttpServletRequest request) throws Exception {

        ModelAndView mv;

        // map request on handler
        HandlerExecutionChain chain = this.hm.getHandler(request);
        Assert.assertNotNull("No handler found for this request", chain);

        RouterHandler handler = (RouterHandler) chain.getHandler();

        // handle request using handleradapter
        mv = ha.handle(request, new MockHttpServletResponse(), handler);

        return mv;
    }

    /**
     * Test route handling:
     * GET     /bind/string/{myName}    bindTestController.bindStringAction
     * @throws Exception
     */
    @Test
    public void testBindName() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/bind/name/" + TEST_STRING);
        request.addHeader("host", TEST_HOST);

        ModelAndView mv = handleRequest(request);

        Assert.assertNotNull(mv);
        Assert.assertEquals(TEST_STRING, mv.getModel().get("name"));

    }

    /**
     * Test route handling:
     * POST    /bind/id/{<[0-9]+>myId}     bindTestController.bindIdAction
     * @throws Exception
     */
    @Test
    public void testBindId() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/bind/id/" + TEST_LONG);
        request.addHeader("host", TEST_HOST);

        ModelAndView mv = handleRequest(request);

        Assert.assertNotNull(mv);
        Assert.assertEquals(TEST_LONG, mv.getModel().get("id"));

    }

    /**
     * Test route handling:
     * DELETE  /bind/slug/{<[a-z0-9-]+>slug}  bindTestController.bindSlugAction
     * @throws Exception
     */
    @Test
    public void testBindSlug() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/bind/slug/" + TEST_SLUG);
        request.addParameter("hash", TEST_SLUG_HASH);
        request.addHeader("host", TEST_HOST);

        ModelAndView mv = handleRequest(request);

        Assert.assertNotNull(mv);
        Assert.assertEquals(TEST_SLUG, mv.getModel().get("slug"));
        Assert.assertEquals(TEST_SLUG_HASH, mv.getModel().get("hash"));

    }

    /**
     * Test route handling:
     * GET     /bind/host                bindTestController.bindHostAction
     * @throws Exception
     */
    @Test
    public void testBindHostSlug() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/bind/hostslug/" + TEST_SLUG);
        request.addHeader("host", TEST_HOST);

        ModelAndView mv = handleRequest(request);

        Assert.assertNotNull(mv);
        Assert.assertEquals(TEST_HOST, mv.getModel().get("hostname"));
        Assert.assertEquals(TEST_SLUG, mv.getModel().get("slug"));

    }    
    
    /**
     * Test route handling:
     * GET     /bind/host                bindTestController.bindHostAction
     * @throws Exception
     */
    @Test
    public void testBindHost() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/bind/host");
        request.addHeader("host", TEST_HOST);

        ModelAndView mv = handleRequest(request);

        Assert.assertNotNull(mv);
        Assert.assertEquals(TEST_HOST, mv.getModel().get("host"));

        request = new MockHttpServletRequest("GET", "/bind/host");
        request.addHeader("host", "www."+TEST_HOST);

        mv = handleRequest(request);

        Assert.assertNotNull(mv);
        Assert.assertEquals("www."+TEST_HOST, mv.getModel().get("host"));
        
    }

    /**
     * Test route handling:
     * GET     myhost.com/bind/specifichost     bindTestController.bindSpecificHostAction
     * @throws Exception
     */
    @Test
    public void testBindSpecificHost() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/bind/specifichost");
        request.addHeader("host", "myhost.com");

        ModelAndView mv = handleRequest(request);

        Assert.assertNotNull(mv);
        Assert.assertEquals("specific", mv.getModel().get("host"));

        request = new MockHttpServletRequest("GET", "/bind/specifichost");
        request.addHeader("host", "myotherhost.com");

        HandlerExecutionChain chain = this.hm.getHandler(request);

        Assert.assertNull("No handler should match this request", chain);
    }

    /**
     * Test route handling:
     * GET     {host}.domain.org/bind/regexphost    bindTestController.bindRegexpHostAction
     * @throws Exception
     */
    @Test
    public void testBindRegexpHost() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/bind/regexphost");
        request.addHeader("host", "myhost.domain.org");

        ModelAndView mv = handleRequest(request);

        Assert.assertNotNull(mv);
        Assert.assertEquals("myhost", mv.getModel().get("subdomain"));

    }

    /**
     * Test route:
     * GET   /security/{name}         bindTestController.securityAction
     * @throws Exception
     */
    @Test(expected = AuthenticationCredentialsNotFoundException.class)
    public void testBindSecurity() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/security/test");
        request.addHeader("host", "myhost.domain.org");

        ModelAndView mv = handleRequest(request);
    }
}
