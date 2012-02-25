package org.resthub.web.springmvc.router.exceptions;

/**
 * Exception: No route found (during reverse routing)
 * @author Brian Clozel
 * @see org.resthub.web.springmvc.router.Router
 */
public class NoRouteFoundException extends RuntimeException {

    public String method;
    public String path;

    public NoRouteFoundException(String method, String path) {
        super("No route found");
        this.method = method;
        this.path = path;
    }

    public String toString() {

        return this.getMessage() + " method[" + this.method + "] path[" + this.path + "]";
    }
}
