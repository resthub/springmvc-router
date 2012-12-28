package org.resthub.web.springmvc.router.hateoas;

import org.resthub.web.springmvc.router.Router;
import org.springframework.hateoas.Identifiable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkBuilder;
import org.springframework.util.Assert;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * SpringMVC-Router's own take on Spring-HATEOAS LinkBuilder.
 * The RouterLinkBuilder helps you build links to Controller+Action+Arguments
 * when using Spring HATEOAS.
 *
 * This class implements the LinkBuilder interface, but you should use methods with
 * action names and/or controller names for better performance and determinism.
 *
 * @author Brian Clozel
 * @see <a href="https://github.com/springSource/spring-hateoas">Spring HATEOAS</a>
 * @see LinkBuilder
 */
public class RouterLinkBuilder implements LinkBuilder {

    private String controllerName;
    private String actionName;
    private List<Object> unresolvedArgs = new ArrayList<Object>(3);
    private Map<String, Object> resolvedArgs = new HashMap<String, Object>(3);

    public RouterLinkBuilder(String controllerAction) {

        Assert.hasLength(controllerAction, "Controller.action should not be empty");
        String[] names = controllerAction.split("\\.");

        this.controllerName = names[0];
        if (names.length == 2) {
            this.actionName = names[1];
        }
    }

    public static RouterLinkBuilder linkTo(String controllerAction) {

        return new RouterLinkBuilder(controllerAction);
    }

    public static RouterLinkBuilder linkTo(String controllerName, String actionName) {

        return new RouterLinkBuilder(controllerName + "." + actionName);
    }

    public static RouterLinkBuilder linkTo(Class<?> controller) {

        return new RouterLinkBuilder(controller.getSimpleName());
    }

    public RouterLinkBuilder action(String actionName) {
        this.actionName = actionName;
        return this;
    }

    @Override
    public RouterLinkBuilder slash(Object object) {

        if (object == null) {
            return this;
        }

        if (object instanceof Identifiable) {
            return slash((Identifiable<?>) object);
        }

        this.unresolvedArgs.add(object);
        return this;
    }

    public RouterLinkBuilder slash(String name, Object object) {

        if (object == null) {
            return this;
        }

        if (object instanceof Identifiable) {
            return slash(name, (Identifiable<?>) object);
        }

        this.resolvedArgs.put(name, object);
        return this;
    }

    @Override
    public RouterLinkBuilder slash(Identifiable<?> identifiable) {
        return slash(identifiable.getId());
    }

    public RouterLinkBuilder slash(String name, Identifiable<?> identifiable) {
        return slash(name, identifiable.getId());
    }

    @Override
    public URI toUri() {

        try {
            return new URI(toString());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Could not reverse controller.action to an URI", e);
        }
    }

    @Override
    public String toString() {
        return Router.getFullUrl(controllerName + "." + actionName,resolveArgs());
    }

    @Override
    public Link withRel(String rel) {
        return new Link(this.toString(), rel);
    }

    @Override
    public Link withSelfRel() {
        return new Link(this.toString());
    }

    private Map<String, Object> resolveArgs() {

        if (unresolvedArgs.size() > 0) {

            Collection<Router.Route> routes = Router.resolveActions(controllerName + "." + actionName);
            int argsCount = unresolvedArgs.size() + resolvedArgs.size();

            for (Router.Route route : routes) {

                int routeArgsCount = route.getArgs().size();

                // if args number doesn't match, check next route
                if (argsCount != routeArgsCount) {
                    continue;
                }

                List<String> argNames = new ArrayList<String>();
                for (Router.Route.Arg arg : route.getArgs()) {
                    argNames.add(arg.getName());
                }

                // if all resolved args aren't in this route, check next route
                if(!argNames.containsAll(resolvedArgs.keySet())) {
                    continue;
                }

                // resolve missing arguments
                for(String argName : argNames) {
                    //this is an unresolved arg
                    if(!resolvedArgs.containsKey(argName)) {
                        resolvedArgs.put(argName,unresolvedArgs.remove(0));
                    }

                }

            }
        }
        return resolvedArgs;
    }

}