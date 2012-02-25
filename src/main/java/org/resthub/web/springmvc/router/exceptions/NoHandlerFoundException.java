package org.resthub.web.springmvc.router.exceptions;

import java.util.Map;

/**
 * Exception: No handler found (during routing)
 * @author Brian Clozel
 * @see org.resthub.web.springmvc.router.Router
 */
public class NoHandlerFoundException extends RuntimeException {

    String action;
    Map<String, Object> args;

    public NoHandlerFoundException(String action, Map<String, Object> args) {
        super("No handler found");
        this.action = action;
        this.args = args;
    } 
    
    public String getAction() {
        return action;
    }

    public Map<String, Object> getArgs() {
        return args;
    }
    
    public String toString() {
    	
    	return this.getMessage()+" action["+this.action+"] args["+this.args+"]";
    }


}
