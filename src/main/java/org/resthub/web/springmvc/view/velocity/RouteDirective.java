package org.resthub.web.springmvc.view.velocity;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import jregex.Matcher;
import jregex.Pattern;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;
import org.resthub.web.springmvc.router.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * #route directive for the Velocity engine.
 * Computes an URL given a Controller.action(argName:'argValue',...) argument.
 * 
 * Examples:
 * <ul>
 * <li> #route("helloWorldController.sayHelloName(name:'yourname')")
 * will resolve '/hello/yourname' because the route configured is "GET /hello/{name} helloWorldController.sayHelloName"
 * <li> #route("helloWorldController.sayHello")
 * will resolve '/hello' because the route configured is "GET /hello helloWorldController.sayHello"
 * </ul>
 * 
 * @author Brian Clozel
 * @see org.resthub.web.springmvc.router.Router
 */
public class RouteDirective extends Directive {


    /**
     * Regex pattern that matches params (paramName:'paramValue')
     */
    private static Pattern paramPattern = new Pattern("([a-zA-Z_0-9]+)\\s*:\\s*'(.*)'");

    private static Logger logger = LoggerFactory.getLogger(RouteDirective.class);


    /**
     * Name of the Velocity directive to be called from the .vm views
     * #route
     */
    @Override
    public String getName() {
        return "route";
    }


    /**
     * This directive is a "LINE directive", meaning it <em>has to be
     * written on a single line</em>.
     * @return org.apache.velocity.runtime.directive.DirectiveConstants.LINE
     */
    @Override
    public int getType() {
        return LINE;
    }

    /**
     * Renders the directive
     * @param context velocity context
     * @param writer used for writing directive result in the view
     * @param node body of the directive (params, content)
     * @return true if the directive rendered ok.
     * @throws IOException
     * @throws ResourceNotFoundException
     * @throws ParseErrorException
     * @throws MethodInvocationException
     */
    @Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {

        String route = null;
        String action = null;
        String params = null;
        Map<String, Object> args = new HashMap<String, Object>();

        //reading the unique param
        if (node.jjtGetChild(0) != null) {

            route = String.valueOf(node.jjtGetChild(0).value(context));
            logger.debug("-- RouteDirective " + route);

            // check if arguments are provided with the "controller.action"
            int index = route.indexOf('(');
            if (index > 0) {
                action = route.substring(0, index);
                params = route.substring(index+1, route.length()-1);
            } else {
                action = route;
            }

            // extract arguments if params is not null
            if (params != null && params.length() > 1) {

                for (String param : params.split(",")) {
                    Matcher matcher = paramPattern.matcher(param);
                    if (matcher.matches()) {
                        // add arguments to the args map
                        args.put(matcher.group(1), matcher.group(2));
                    } else {
                        logger.warn("Ignoring " + params + " (static params must be specified as key:'value',...)");
                    }
                }

            }
        }

        // resolve URL and write it to the view
        writer.write(Router.reverse(action, args).url);

        return true;
    }
}
