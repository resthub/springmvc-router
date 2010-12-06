package org.springframework.web.servlet.mvc.router;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.web.servlet.mvc.router.Router.ActionDefinition;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.router.exceptions.NoHandlerFoundException;

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
        
}
