package org.resthub.web.springmvc.router.support;

import org.resthub.web.springmvc.router.HTTPRequestAdapter;
import org.resthub.web.springmvc.router.Router;
import org.resthub.web.springmvc.router.exceptions.ActionNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Resolve Controller and Action for the given route (that contains the
 * fullAction "controller.action")
 *
 * @author Brian Clozel
 */
public class RouterHandlerResolver {
    
    private Map<String, Object> cachedControllers = new LinkedHashMap<String, Object>();
    
    private final Map<String, HandlerMethod> cachedHandlers = new LinkedHashMap<String, HandlerMethod>();

    private static final Logger logger = LoggerFactory.getLogger(RouterHandlerResolver.class);
    
    public void setCachedControllers(Map<String, Object> controllers) {
        
        for(String key : controllers.keySet()) {
            this.cachedControllers.put(key.toLowerCase(), controllers.get(key));            
        }
    }

    /**
     * Returns a proper HandlerMethod given the matching Route
     * @param route the matching Route for the current request
     * @param fullAction string "controller.action"
     * @return HandlerMethod to be used by the RequestAdapter
     * @throws ActionNotFoundException
     */
    public HandlerMethod resolveHandler(Router.Route route, String fullAction, HTTPRequestAdapter req) throws ActionNotFoundException {

        HandlerMethod handlerMethod;
        
        // check if the Handler is already cached
        if(this.cachedHandlers.containsKey(fullAction)) {
            handlerMethod = this.cachedHandlers.get(fullAction);
        } else {
            handlerMethod = this.doResolveHandler(route, fullAction);
            this.cachedHandlers.put(fullAction, handlerMethod);
        }

        return handlerMethod;
    }
    
    private HandlerMethod doResolveHandler(Router.Route route, String fullAction) throws ActionNotFoundException {
        
        Method actionMethod;
        Object controllerObject;

        String controller = fullAction.substring(0, fullAction.lastIndexOf(".")).toLowerCase();
        String action = fullAction.substring(fullAction.lastIndexOf(".") + 1);
        controllerObject = cachedControllers.get(controller);

        if (controllerObject == null) {
            logger.debug("Did not find handler {} for [{} {}]", controller, route.method, route.path);
            throw new ActionNotFoundException(fullAction, new Exception("Controller " + controller + " not found"));
        }

        // find actionMethod on target
        actionMethod = findActionMethod(action, controllerObject);

        if (actionMethod == null) {
            logger.debug("Did not find handler method {}.{} for [{} {}]", controller, action, route.method, route.path);
            throw new ActionNotFoundException(fullAction, new Exception("No method public static void " + action + "() was found in class " + controller));
        }
        
        return new RouterHandler(controllerObject, actionMethod, route);
    }

    /**
     * Find the first public static method of a controller class
     *
     * @param name The method name
     * @param controller The controller
     * @return The method or null
     */
    private Method findActionMethod(String name, Object controller) {

        //get the controller class
        //(or the corresponding target class if the current controller
        // instance is an AOP proxy
        Class clazz = AopUtils.getTargetClass(controller);

        while (!clazz.getName().equals("java.lang.Object")) {
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.getName().equalsIgnoreCase(name)) {
                    return BridgeMethodResolver.findBridgedMethod(m);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }
}
