package org.resthub.web.springmvc.router.exceptions;


public class ActionNotFoundException extends Exception {

	    private String action;
	   
	    public ActionNotFoundException(String action, Throwable cause) {
	        super(String.format("Action %s not found", action), cause);
	        this.action = action;
	    }

	    public ActionNotFoundException(String action, String message) {
	    	super(String.format("Action %s not found", action));
	        this.action = action;
	    }
	    
	    public String getAction() {
	        return action;
	    }
	    
	    public String getErrorDescription() {
	        return String.format(
	                "Action <strong>%s</strong> could not be found. Error raised is <strong>%s</strong>", 
	                action, 
	                getCause() instanceof ClassNotFoundException ? "ClassNotFound: "+getCause().getMessage() : getCause().getMessage()
	        );
	    }
	    
}
