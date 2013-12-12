package org.resthub.web.springmvc.view.jsp;


import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.DynamicAttributes;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.resthub.web.springmvc.router.Router;
import org.resthub.web.springmvc.router.Router.ActionDefinition;

public class URLRouteTag extends SimpleTagSupport implements DynamicAttributes {
    
    private String action;
    
    /**
     * Stores key/value pairs to pass as parameters to the URL.
     */
    private Map<String, Object> attrMap = new HashMap<String, Object>();
        
    public void doTag() throws JspException, IOException, IllegalArgumentException {
        JspWriter out = getJspContext().getOut();

        ActionDefinition urlAction = Router.reverse(action, attrMap);
        
        out.println(urlAction);
    }
    
    @Override
    public void setDynamicAttribute(String uri, String name, Object value) throws JspException {
        attrMap.put(name, value);
    }

    public void setAction(String action) {
        this.action = action;
    }
}
