package org.resthub.web.springmvc.router;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.Assert;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.resthub.web.springmvc.router.Router.ActionDefinition;
import org.resthub.web.springmvc.router.exceptions.NoHandlerFoundException;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.HandlerMapping;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:routerTestContext.xml" })
public class ReverseRoutingTest {
    
    	private String LOCATION = "/routerTestContext.xml";
	
	private XmlWebApplicationContext wac;
	
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
        
    @Test
    public void testSimpleReverse() {
        
        ActionDefinition action = Router.reverse("myTestController.simpleAction");
        
        Assert.assertNotNull(action);
        Assert.assertEquals("/simpleaction",action.url);
    }

    @Test
    public void testParamReverse() {
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("param","testparam");
        ActionDefinition action = Router.reverse("myTestController.paramAction",params);
        
        Assert.assertNotNull(action);
        Assert.assertEquals("/param/"+params.get("param"),action.url);
    }
    
    @Test
    public void testRegexpParamReverse() {
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("slug","slug_01");
        ActionDefinition action = Router.reverse("bindTestController.bindSlugAction",params);
        
        Assert.assertNotNull(action);
        Assert.assertEquals("/bind/slug/"+params.get("slug"),action.url);
        }       
 
        
        @Test(expected=NoHandlerFoundException.class)
        public void testFailRegexpReverse() {
            Map<String,Object> params = new HashMap<String, Object>();
            params.put("name","name01");
        ActionDefinition action = Router.reverse("bindTestController.bindNameAction",params);
        
    }
    
    @Test
    public void testHostRegexpReverse() {
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("subdomain","sub");
        ActionDefinition action = Router.reverse("bindTestController.bindRegexpHostAction",params);
        
        Assert.assertNotNull(action);
        Assert.assertEquals("/bind/regexphost",action.url);
        Assert.assertEquals("sub.domain.org",action.host);
    }

    @Test
    public void testHostSlugReverse() {
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("slug","slug1");
        ActionDefinition action = Router.reverse("bindTestController.bindHostSlugAction",params);
        
        Assert.assertNotNull(action);
        Assert.assertNotNull(action.host);
        Assert.assertEquals("/bind/hostslug/slug1",action.url);
        
    }
    
    
    //--------------------------------
    // querystring params tests
    
    @Test
    public void qsParamPresenceReverse() {
        
        // without the qsparam : should fail
        try {
            ActionDefinition action = Router.reverse("myTestController.qsParamPresence");
            Assert.assertTrue("Should have thrown a NoHandlerFoundException exception", false);
        } catch(NoHandlerFoundException ex) {
            // ok
        }
        
        // with the qsparam : should work
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("qsParamA", "");
        ActionDefinition action = Router.reverse("myTestController.qsParamPresence", params);
        
        Assert.assertNotNull(action);
        Assert.assertNotNull(action.host);
        Assert.assertEquals("/qsparampresence?qsParamA=", action.url); 
    }
    
    @Test
    public void qsParamNegatePresenceReverse() {
        
        // without the qsparam : should work
        Map<String,Object> params = new HashMap<String, Object>();
        ActionDefinition action = Router.reverse("myTestController.qsParamNegatePresence", params);
        
        Assert.assertNotNull(action);
        Assert.assertNotNull(action.host);
        Assert.assertEquals("/qsparamnegatepresence", action.url); 
        
        // with the qsparam, should fail
        params.put("qsParamA", "abc");
        
        try {
            action = Router.reverse("myTestController.qsParamNegatePresence", params);
            Assert.assertTrue("Should have thrown a NoHandlerFoundException exception", false);
        } catch(NoHandlerFoundException ex) {
            // ok
        }
    }
    
    @Test
    public void qsParamEmptyValueRequiredReverse() {
        
        // without the qsparam : should fail
        Map<String,Object> params = new HashMap<String, Object>();
        try {
            ActionDefinition action = Router.reverse("myTestController.qsParamEmptyValueRequired", params);
            Assert.assertTrue("Should have thrown a NoHandlerFoundException exception", false);
        } catch(NoHandlerFoundException ex) {
            // ok
        }
        
        // wrong qsparam's value : should fail
        params = new HashMap<String, Object>();
        params.put("qsParamA", "wrongValue");
      
        try {
            ActionDefinition action = Router.reverse("myTestController.qsParamEmptyValueRequired", params);
            Assert.assertTrue("Should have thrown a NoHandlerFoundException exception", false);
        } catch(NoHandlerFoundException ex) {
            // ok
        }
        
        //  qsparam with empty value: should work
        params = new HashMap<String, Object>();
        params.put("qsParamA", "");
        ActionDefinition action = Router.reverse("myTestController.qsParamEmptyValueRequired", params);
        
        Assert.assertNotNull(action);
        Assert.assertNotNull(action.host);
        Assert.assertEquals("/qsparamemptyvaluerequired?qsParamA=", action.url);    
    }
    
    @Test
    public void qsParamSpecificValueRequiredReverse() {
        
        // without the qsparam : should fail
        Map<String,Object> params = new HashMap<String, Object>();
        try {
            ActionDefinition action = Router.reverse("myTestController.qsParamSpecificValueRequired", params);
            Assert.assertTrue("Should have thrown a NoHandlerFoundException exception", false);
        } catch(NoHandlerFoundException ex) {
            // ok
        }
        
        // wrong qsparam's value : should fail
        params = new HashMap<String, Object>();
        params.put("qsParamA", "wrongValue");
      
        try {
            ActionDefinition action = Router.reverse("myTestController.qsParamSpecificValueRequired", params);
            Assert.assertTrue("Should have thrown a NoHandlerFoundException exception", false);
        } catch(NoHandlerFoundException ex) {
            // ok
        }
        
        // with empty : should fail
        params = new HashMap<String, Object>();
        params.put("qsParamA", "");
      
        try {
            ActionDefinition action = Router.reverse("myTestController.qsParamSpecificValueRequired", params);
            Assert.assertTrue("Should have thrown a NoHandlerFoundException exception", false);
        } catch(NoHandlerFoundException ex) {
            // ok
        }
        
        
        //  qsparam with the required value: should work
        params = new HashMap<String, Object>();
        params.put("qsParamA", "abc");
        ActionDefinition action = Router.reverse("myTestController.qsParamSpecificValueRequired", params);
        
        Assert.assertNotNull(action);
        Assert.assertNotNull(action.host);
        Assert.assertEquals("/qsparamspecificvaluerequired?qsParamA=abc", action.url);    
    }
    
    @Test
    public void qsParamNegateSpecificValueReverse() {
        
        // without the qsparam : should not work!
        Map<String,Object> params = new HashMap<String, Object>();
        try {
            ActionDefinition action = Router.reverse("myTestController.qsParamNegateSpecificValue", params);
            Assert.assertTrue("Should have thrown a NoHandlerFoundException exception", false);
        } catch(NoHandlerFoundException ex) {
            // ok
        }
        
        // with the prohibited value : should not work
        params = new HashMap<String, Object>();
        params.put("qsParamA", "abc");
        try {
            ActionDefinition action = Router.reverse("myTestController.qsParamNegateSpecificValue", params);
            Assert.assertTrue("Should have thrown a NoHandlerFoundException exception", false);
        } catch(NoHandlerFoundException ex) {
            // ok
        }
        
        // with an empty value : should work
        params = new HashMap<String, Object>();
        params.put("qsParamA", "");
        
        ActionDefinition action = Router.reverse("myTestController.qsParamNegateSpecificValue", params);
        
        Assert.assertNotNull(action);
        Assert.assertNotNull(action.host);
        Assert.assertEquals("/qsparamnegatespecificvalue?qsParamA=", action.url);   
        
        
        // with a random value : should work
        params = new HashMap<String, Object>();
        params.put("qsParamA", "def");
        
        action = Router.reverse("myTestController.qsParamNegateSpecificValue", params);
        
        Assert.assertNotNull(action);
        Assert.assertNotNull(action.host);
        Assert.assertEquals("/qsparamnegatespecificvalue?qsParamA=def", action.url);   
    }
    
    @Test
    public void qsParamTwoParamsRequiredReverse() throws Exception {
        
        // without any qsparam : should not work
        Map<String,Object> params = new HashMap<String, Object>();
        try {
            ActionDefinition action = Router.reverse("myTestController.qsParamTwoParamsRequired", params);
            Assert.assertTrue("Should have thrown a NoHandlerFoundException exception", false);
        } catch(NoHandlerFoundException ex) {
            // ok
        }
        
        // with one required param only : should not work
        params = new HashMap<String, Object>();
        params.put("qsParamA", "abc");
        try {
            ActionDefinition action = Router.reverse("myTestController.qsParamTwoParamsRequired", params);
            Assert.assertTrue("Should have thrown a NoHandlerFoundException exception", false);
        } catch(NoHandlerFoundException ex) {
            // ok
        }
        
        // with the two required params but invalid vales : should not work
        params = new HashMap<String, Object>();
        params.put("qsParamA", "wrongValue");
        params.put("qsParamB", "abc");
        try {
            ActionDefinition action = Router.reverse("myTestController.qsParamTwoParamsRequired", params);
            Assert.assertTrue("Should have thrown a NoHandlerFoundException exception", false);
        } catch(NoHandlerFoundException ex) {
            // ok
        }
        
        // with valid params : should work
        params = new HashMap<String, Object>();
        params.put("qsParamA", "abc");
        params.put("qsParamB", "anyValue");
        
        ActionDefinition action = Router.reverse("myTestController.qsParamTwoParamsRequired", params);
        
        Assert.assertNotNull(action);
        Assert.assertNotNull(action.host);
        Assert.assertTrue(action.url != null && action.url.startsWith("/qsparamtwoparamsrequired?"));  

        List<NameValuePair> queryParams = URLEncodedUtils.parse(new URI(action.url), "utf-8");
        Assert.assertTrue(queryParams != null);
        
        boolean qsParamAFound = false;
        boolean qsParamBFound = false;
        for(NameValuePair nameValuePair : queryParams) {
            if("qsParamA".equals(nameValuePair.getName())) {
                qsParamAFound = true;
                Assert.assertEquals("abc", nameValuePair.getValue());
            } else if("qsParamB".equals(nameValuePair.getName())) {
                qsParamBFound = true;
                Assert.assertEquals("anyValue", nameValuePair.getValue());    
            }    
        }
        Assert.assertTrue(qsParamAFound && qsParamBFound);
    }
    
    @Test
    public void qsParamEncodedValueAndRandomSpacesReverse() throws Exception {

        Map<String,Object> params = new HashMap<String, Object>();
        params.put("qsParamA", " ");
        params.put("qsParamB", "a b");
        params.put("qsParamC", "été");
        
        ActionDefinition action = Router.reverse("myTestController.qsParamEncodedValueAndRandomSpaces", params);
        
        Assert.assertNotNull(action);
        Assert.assertNotNull(action.host);
        Assert.assertTrue(action.url != null && action.url.startsWith("/qsparamencodedvalueandrandomspaces?"));  

        List<NameValuePair> queryParams = URLEncodedUtils.parse(new URI(action.url), "utf-8");
        Assert.assertTrue(queryParams != null);

        boolean qsParamAFound = false;
        boolean qsParamBFound = false;
        boolean qsParamCFound = false;
        for(NameValuePair nameValuePair : queryParams) {
            if("qsParamA".equals(nameValuePair.getName())) {
                qsParamAFound = true;
                Assert.assertEquals(" ", nameValuePair.getValue());
            } else if("qsParamB".equals(nameValuePair.getName())) {
                qsParamBFound = true;
                Assert.assertEquals("a b", nameValuePair.getValue());    
            } else if("qsParamC".equals(nameValuePair.getName())) {
                qsParamCFound = true;
                Assert.assertEquals("été", nameValuePair.getValue());    
            }    
        }
        Assert.assertTrue(qsParamAFound && qsParamBFound && qsParamCFound);
    }
    
    @Test
    public void qsParamPlayNiceWithOtherRoutingFeaturesReverse() throws Exception {
        
        // with the path param only : should not work
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("myName", "stromgol");
        try {
            ActionDefinition action = Router.reverse("myTestController.qsParamPlayNiceWithOtherRoutingFeatures", params);
            Assert.assertTrue("Should have thrown a NoHandlerFoundException exception", false);
        } catch(NoHandlerFoundException ex) {
            // ok
        }
        
        // with the staticArg param only : should not work
        params = new HashMap<String, Object>();
        params.put("myStaticArg", "someStaticVal");
        try {
            ActionDefinition action = Router.reverse("myTestController.qsParamPlayNiceWithOtherRoutingFeatures", params);
            Assert.assertTrue("Should have thrown a NoHandlerFoundException exception", false);
        } catch(NoHandlerFoundException ex) {
            // ok
        }
        
        // with the querystring param only : should not work
        params = new HashMap<String, Object>();
        params.put("qsParamA", "abc");
        try {
            ActionDefinition action = Router.reverse("myTestController.qsParamPlayNiceWithOtherRoutingFeatures", params);
            Assert.assertTrue("Should have thrown a NoHandlerFoundException exception", false);
        } catch(NoHandlerFoundException ex) {
            // ok
        }
        
        // with the path param + staticArg param only : should not work
        params = new HashMap<String, Object>();
        params.put("myName", "stromgol");
        params.put("myStaticArg", "someStaticVal");
        try {
            ActionDefinition action = Router.reverse("myTestController.qsParamPlayNiceWithOtherRoutingFeatures", params);
            Assert.assertTrue("Should have thrown a NoHandlerFoundException exception", false);
        } catch(NoHandlerFoundException ex) {
            // ok
        }
        
        // with the path param + querystring param only : should not work
        params = new HashMap<String, Object>();
        params.put("myName", "stromgol");
        params.put("qsParamA", "abc");
        try {
            ActionDefinition action = Router.reverse("myTestController.qsParamPlayNiceWithOtherRoutingFeatures", params);
            Assert.assertTrue("Should have thrown a NoHandlerFoundException exception", false);
        } catch(NoHandlerFoundException ex) {
            // ok
        }
        
        // with the staticArg param + querystring param only : should not work
        params = new HashMap<String, Object>();
        params.put("myStaticArg", "someStaticVal");
        params.put("qsParamA", "abc");
        try {
            ActionDefinition action = Router.reverse("myTestController.qsParamPlayNiceWithOtherRoutingFeatures", params);
            Assert.assertTrue("Should have thrown a NoHandlerFoundException exception", false);
        } catch(NoHandlerFoundException ex) {
            // ok
        }

        // With all the required params : should work
        params = new HashMap<String, Object>();
        params.put("qsParamA", "abc");
        params.put("myName", "stromgol");
        params.put("myStaticArg", "someStaticVal");
        ActionDefinition action = Router.reverse("myTestController.qsParamPlayNiceWithOtherRoutingFeatures", params);
        
        Assert.assertNotNull(action);
        Assert.assertNotNull(action.host);
        
        Assert.assertEquals("/qsparamplaynicewithotherroutingfeatures/name/stromgol?qsParamA=abc", action.url);   
    }
    
    @Test
    public void qsParamAndSameKeyStaticParamReverse() {
        
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("qsParamA", "someStaticVal");
        ActionDefinition action = Router.reverse("bindTestController.qsParamAndSameKeyStaticParam", params);
        
        Assert.assertNotNull(action);
        Assert.assertNotNull(action.host);
        Assert.assertEquals("/qsparamandsamekeystaticparam?qsParamA=abc", action.url);    
    }
    
    @Test
    public void qsParamEmptyAndSameKeyStaticParamReverse() {
        
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("qsParamA", "someStaticVal");
        ActionDefinition action = Router.reverse("bindTestController.qsParamEmptyAndSameKeyStaticParam", params);
        
        Assert.assertNotNull(action);
        Assert.assertNotNull(action.host);
        Assert.assertEquals("/qsparamemptyandsamekeystaticparam?qsParamA=", action.url);    
    }
    
    @Test
    public void qsParamNullAndSameKeyStaticParamReverse() {
        
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("qsParamA", "someStaticVal");
        ActionDefinition action = Router.reverse("bindTestController.qsParamNullAndSameKeyStaticParam", params);
        
        Assert.assertNotNull(action);
        Assert.assertNotNull(action.host);
        Assert.assertEquals("/qsparamnullandsamekeystaticparam?qsParamA=someStaticVal", action.url);    
    }
   
}
