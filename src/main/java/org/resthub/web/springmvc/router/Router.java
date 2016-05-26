package org.resthub.web.springmvc.router;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

import jregex.Matcher;
import jregex.Pattern;
import jregex.REFlags;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.resthub.web.springmvc.router.exceptions.NoHandlerFoundException;
import org.resthub.web.springmvc.router.exceptions.NoRouteFoundException;
import org.resthub.web.springmvc.router.exceptions.RouteFileParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

/**
 * <p>The router matches HTTP requests to action invocations.
 * <p>Courtesy of Play! Framework Router
 *
 * @author Play! Framework developers
 * @author Brian Clozel
 * @see org.resthub.web.springmvc.router.RouterHandlerMapping
 */
public class Router {

    static Pattern routePattern = new Pattern("^({method}GET|POST|PUT|DELETE|PATCH|OPTIONS|HEAD|\\*)[(]?({headers}[^)]*)(\\))?\\s+({path}.*/[^\\s]*)\\s+({action}[^\\s(]+)({params}.+)?(\\s*)$");
    /**
     * Pattern used to locate a method override instruction
     */
    static Pattern methodOverride = new Pattern("^.*x-http-method-override=({method}GET|PUT|POST|DELETE|PATCH).*$");

    /**
     * Timestamp the routes file was last loaded at.
     */
    public static long lastLoading = -1;
    private static final Logger logger = LoggerFactory.getLogger(Router.class);

    public static void clear() {
        routes.clear();
    }

    /**
     * Parse the routes file. This is called at startup.
     *
     */
    public static void load(List<Resource> fileResources) throws IOException {
        routes.clear();
        for (Resource res : fileResources) {
            parse(res);
        }

        lastLoading = System.currentTimeMillis();
    }

    /**
     * This one can be called to add new route. Last added is first in the route
     * list.
     */
    public static void prependRoute(String method, String path, String action, String headers) {
        prependRoute(method, path, action, null, headers);
    }

    /**
     * This one can be called to add new route. Last added is first in the route
     * list.
     */
    public static void prependRoute(String method, String path, String action) {
        prependRoute(method, path, action, null, null);
    }

    /**
     * Add a route at the given position
     */
    public static void addRoute(int position, String method, String path, String action, String params, String headers) {
        if (position > routes.size()) {
            position = routes.size();
        }
        routes.add(position, getRoute(method, path, action, params, headers));
    }

    /**
     * Add a route at the given position
     */
    public static void addRoute(int position, String method, String path, String headers) {
        addRoute(position, method, path, null, null, headers);
    }

    /**
     * Add a route at the given position
     */
    public static void addRoute(int position, String method, String path, String action, String headers) {
        addRoute(position, method, path, action, null, headers);
    }

    /**
     * Add a new route. Will be first in the route list
     */
    public static void addRoute(String method, String path, String action) {
        prependRoute(method, path, action);
    }

    /**
     * Add a route at the given position
     */
    public static void addRoute(String method, String path, String action, String headers) {
        addRoute(method, path, action, null, headers);
    }

    /**
     * Add a route
     */
    public static void addRoute(String method, String path, String action, String params, String headers) {
        appendRoute(method, path, action, params, headers, null, 0);
    }

    /**
     * This is used internally when reading the route file. The order the routes
     * are added matters and we want the method to append the routes to the
     * list.
     */
    public static void appendRoute(String method, String path, String action, String params, String headers, String sourceFile, int line) {
        routes.add(getRoute(method, path, action, params, headers, sourceFile, line));
    }

    public static Route getRoute(String method, String path, String action, String params, String headers) {
        return getRoute(method, path, action, params, headers, null, 0);
    }

    public static Route getRoute(String method, String path, String action, String params, String headers, String sourceFile, int line) {
        Route route = new Route();
        route.method = method;
        route.path = path.replace("//", "/");
        route.action = action;
        route.routesFile = sourceFile;
        route.routesFileLine = line;
        route.addFormat(headers);
        route.addParams(params);
        route.compute();
        if (logger.isDebugEnabled()) {
            logger.debug("Adding [" + route.toString() + "] with params [" + params + "] and headers [" + headers + "]");
        }

        return route;
    }

    /**
     * Add a new route at the beginning of the route list
     */
    public static void prependRoute(String method, String path, String action, String params, String headers) {
        routes.add(0, getRoute(method, path, action, params, headers));
    }

    /**
     * Parse a route file.
     *
     * @param fileResource
     * @throws IOException
     */
    static void parse(Resource fileResource) throws IOException {

        String fileAbsolutePath = fileResource.getURL().getPath();
        String content = IOUtils.toString(fileResource.getInputStream());

        parse(content, fileAbsolutePath);
    }

    static void parse(String content, String fileAbsolutePath) throws IOException {
        int lineNumber = 0;
        for (String line : content.split("\n")) {
            lineNumber++;
            line = line.trim().replaceAll("\\s+", " ");
            if (line.length() == 0 || line.startsWith("#")) {
                continue;
            }
            Matcher matcher = routePattern.matcher(line);
            if (matcher.matches()) {

                String action = matcher.group("action");
                String method = matcher.group("method");
                String path = matcher.group("path");
                String params = matcher.group("params");
                String headers = matcher.group("headers");
                appendRoute(method, path, action, params, headers, fileAbsolutePath, lineNumber);
            } else {
                logger.error("Invalid route definition : " + line);
            }
        }
    }

    public static void detectChanges(List<Resource> fileResources) throws IOException {

        boolean hasChanged = false;

        for (Resource res : fileResources) {
            if (FileUtils.isFileNewer(res.getFile(), lastLoading)) {
                hasChanged = true;
                break;
            }
        }

        if (hasChanged) {
            load(fileResources);
        }
    }

    public static List<Route> routes = new ArrayList<Route>(500);

    public static Route route(HTTPRequestAdapter request) {
        if (logger.isTraceEnabled()) {
            logger.trace("Route: " + request.path + " - " + request.querystring);
        }
        // request method may be overriden if a x-http-method-override parameter is given
        if (request.querystring != null && methodOverride.matches(request.querystring)) {
            Matcher matcher = methodOverride.matcher(request.querystring);
            if (matcher.matches()) {
                if (logger.isTraceEnabled()) {
                    logger.trace("request method %s overriden to %s ", request.method, matcher.group("method"));
                }
                request.method = matcher.group("method");
            }
        }

        for (Route route : routes) {
            String format = request.format;
            String host = request.host;
            Map<String, String> args = route.matches(request.method, request.path, format, host);

            if (args != null) {
                request.routeArgs = args;
                request.action = route.action;
                if (args.containsKey("format")) {
                    request.setFormat(args.get("format"));
                }
                if (request.action.indexOf("{") > -1) { // more optimization ?
                    for (String arg : request.routeArgs.keySet()) {
                        request.action = request.action.replace("{" + arg + "}", request.routeArgs.get(arg));
                    }
                }
                return route;
            }
        }
        // Not found - if the request was a HEAD, let's see if we can find a corresponding GET
        if (request.method.equalsIgnoreCase("head")) {
            request.method = "GET";
            Route route = route(request);
            request.method = "HEAD";
            if (route != null) {
                return route;
            }
        }
        throw new NoRouteFoundException(request.method, request.path);
    }

    public static Map<String, String> route(String method, String path) {
        return route(method, path, null, null);
    }

    public static Map<String, String> route(String method, String path, String headers) {
        return route(method, path, headers, null);
    }

    public static Map<String, String> route(String method, String path, String headers, String host) {
        for (Route route : routes) {
            Map<String, String> args = route.matches(method, path, headers, host);
            if (args != null) {
                args.put("action", route.action);
                return args;
            }
        }
        return new HashMap<String, String>(16);
    }

    public static ActionDefinition reverse(String action) {
        // Note the map is not <code>Collections.EMPTY_MAP</code> because it will be copied and changed.
        return reverse(action, new HashMap<String, Object>(16));
    }

    public static String getFullUrl(String action, Map<String, Object> args) {
        return HTTPRequestAdapter.getCurrent().getBase() + reverse(action, args);
    }

    public static String getFullUrl(String action) {
        // Note the map is not <code>Collections.EMPTY_MAP</code> because it will be copied and changed.
        return getFullUrl(action, new HashMap<String, Object>(16));
    }

    public static Collection<Route> resolveActions(String action) {

        List<Route> candidateRoutes = new ArrayList<Route>(3);

        for (Route route : routes) {
            if (route.actionPattern != null) {
                Matcher matcher = route.actionPattern.matcher(action);
                if (matcher.matches()) {
                    candidateRoutes.add(route);
                }
            }
        }

        return candidateRoutes;
    }

    public static ActionDefinition reverse(String action, Map<String, Object> args) {

	    HTTPRequestAdapter currentRequest = HTTPRequestAdapter.getCurrent();

        Map<String, Object> argsbackup = new HashMap<String, Object>(args);
        for (Route route : routes) {
            if (route.actionPattern != null) {
                Matcher matcher = route.actionPattern.matcher(action);
                if (matcher.matches()) {
                    for (String group : route.actionArgs) {
                        String v = matcher.group(group);
                        if (v == null) {
                            continue;
                        }
                        args.put(group, v.toLowerCase());
                    }
                    List<String> inPathArgs = new ArrayList<String>(16);
                    boolean allRequiredArgsAreHere = true;
                    // les noms de parametres matchent ils ?
                    for (Route.Arg arg : route.args) {
                        inPathArgs.add(arg.name);
                        Object value = args.get(arg.name);
                        if (value == null) {
                            // This is a hack for reverting on hostname that are a regex expression.
                            // See [#344] for more into. This is not optimal and should retough. However,
                            // it allows us to do things like {(.*)}.domain.com
                            String host = route.host.replaceAll("\\{", "").replaceAll("\\}", "");
                            if (host.equals(arg.name) || host.matches(arg.name)) {
                                args.put(arg.name, "");
                                value = "";
                            } else {
                                allRequiredArgsAreHere = false;
                                break;
                            }
                        } else {
                            if (value instanceof List<?>) {
                                @SuppressWarnings("unchecked")
                                List<Object> l = (List<Object>) value;
                                value = l.get(0);
                            }
                            if (!value.toString().startsWith(":") && !arg.constraint.matches(value.toString())) {
                                allRequiredArgsAreHere = false;
                                break;
                            }
                        }
                    }
                    // les parametres codes en dur dans la route matchent-ils ?
                    for (String staticKey : route.staticArgs.keySet()) {
                        if (staticKey.equals("format")) {
                            if (!currentRequest.format.equals(route.staticArgs.get("format"))) {
                                allRequiredArgsAreHere = false;
                                break;
                            }
                            continue; // format is a special key
                        }
                        if (!args.containsKey(staticKey) || (args.get(staticKey) == null)
                                || !args.get(staticKey).toString().equals(route.staticArgs.get(staticKey))) {
                            allRequiredArgsAreHere = false;
                            break;
                        }
                    }
                    if (allRequiredArgsAreHere) {
                        StringBuilder queryString = new StringBuilder();
                        String path = route.path;
                        //add contextPath and servletPath if set in the current request
                        if( currentRequest != null) {

                            if(!currentRequest.servletPath.isEmpty() && !currentRequest.servletPath.equals("/")) {
                            	String servletPath = currentRequest.servletPath;
                                path = (servletPath.startsWith("/") ?  servletPath : "/" + servletPath) + path;
                            }
                            if(!currentRequest.contextPath.isEmpty() && !currentRequest.contextPath.equals("/")) {
                            	String contextPath = currentRequest.contextPath; 
                                path = (contextPath.startsWith("/") ? contextPath : "/" + contextPath) + path;
                            }
                        }
                        String host = route.host;
                        if (path.endsWith("/?")) {
                            path = path.substring(0, path.length() - 2);
                        }
                        for (Map.Entry<String, Object> entry : args.entrySet()) {
                            String key = entry.getKey();
                            Object value = entry.getValue();
                            if (inPathArgs.contains(key) && value != null) {
                                if (List.class.isAssignableFrom(value.getClass())) {
                                    @SuppressWarnings("unchecked")
                                    List<Object> vals = (List<Object>) value;
                                    try {
                                        path = path.replaceAll("\\{(<[^>]+>)?" + key + "\\}", URLEncoder.encode(vals.get(0).toString().replace("$", "\\$"), "utf-8"));
                                    } catch (UnsupportedEncodingException e) {
                                        throw new RouteFileParsingException("RouteFile encoding exception", e);
                                    }
                                } else {
                                    try {
                                        path = path.replaceAll("\\{(<[^>]+>)?" + key + "\\}", URLEncoder.encode(value.toString().replace("$", "\\$"), "utf-8"));
                                        host = host.replaceAll("\\{(<[^>]+>)?" + key + "\\}", URLEncoder.encode(value.toString().replace("$", "\\$"), "utf-8"));
                                    } catch (UnsupportedEncodingException e) {
                                        throw new RouteFileParsingException("RouteFile encoding exception", e);
                                    }
                                }
                            } else if (route.staticArgs.containsKey(key)) {
                                // Do nothing -> The key is static
                            } else if (value != null) {
                                if (List.class.isAssignableFrom(value.getClass())) {
                                    @SuppressWarnings("unchecked")
                                    List<Object> vals = (List<Object>) value;
                                    for (Object object : vals) {
                                        try {
                                            queryString.append(URLEncoder.encode(key, "utf-8"));
                                            queryString.append("=");
                                            if (object.toString().startsWith(":")) {
                                                queryString.append(object.toString());
                                            } else {
                                                queryString.append(URLEncoder.encode(object.toString() + "", "utf-8"));
                                            }
                                            queryString.append("&");
                                        } catch (UnsupportedEncodingException ex) {
                                        }
                                    }
//                                } else if (value.getClass().equals(Default.class)) {
//                                    // Skip defaults in queryString
                                } else {
                                    try {
                                        queryString.append(URLEncoder.encode(key, "utf-8"));
                                        queryString.append("=");
                                        if (value.toString().startsWith(":")) {
                                            queryString.append(value.toString());
                                        } else {
                                            queryString.append(URLEncoder.encode(value.toString() + "", "utf-8"));
                                        }
                                        queryString.append("&");
                                    } catch (UnsupportedEncodingException ex) {
                                    }
                                }
                            }
                        }
                        String qs = queryString.toString();
                        if (qs.endsWith("&")) {
                            qs = qs.substring(0, qs.length() - 1);
                        }
                        ActionDefinition actionDefinition = new ActionDefinition();
                        actionDefinition.url = qs.length() == 0 ? path : path + "?" + qs;
                        actionDefinition.method = route.method == null || route.method.equals("*") ? "GET" : route.method.toUpperCase();
                        actionDefinition.star = "*".equals(route.method);
                        actionDefinition.action = action;
                        actionDefinition.args = argsbackup;
                        actionDefinition.host = host;
                        return actionDefinition;
                    }
                }
            }
        }
        throw new NoHandlerFoundException(action, args);
    }

    private static void addToQuerystring(StringBuilder queryString, String key, Object value) {
        if (List.class.isAssignableFrom(value.getClass())) {
            @SuppressWarnings("unchecked")
            List<Object> vals = (List<Object>) value;
            for (Object object : vals) {
                try {
                    queryString.append(URLEncoder.encode(key, "utf-8"));
                    queryString.append("=");
                    if (object.toString().startsWith(":")) {
                        queryString.append(object.toString());
                    } else {
                        queryString.append(URLEncoder.encode(object.toString() + "", "utf-8"));
                    }
                    queryString.append("&");
                } catch (UnsupportedEncodingException ex) {
                }
            }
//        } else if (value.getClass().equals(Default.class)) {
//            // Skip defaults in queryString
        } else {
            try {
                queryString.append(URLEncoder.encode(key, "utf-8"));
                queryString.append("=");
                if (value.toString().startsWith(":")) {
                    queryString.append(value.toString());
                } else {
                    queryString.append(URLEncoder.encode(value.toString() + "", "utf-8"));
                }
                queryString.append("&");
            } catch (UnsupportedEncodingException ex) {
            }
        }
    }

    public static class ActionDefinition {

        /**
         * The domain/host name.
         */
        public String host;
        /**
         * The HTTP method, e.g. "GET".
         */
        public String method;
        /**
         * @todo - what is this? does it include the domain?
         */
        public String url;
        /**
         * Whether the route contains an astericks *.
         */
        public boolean star;
        /**
         * @todo - what is this? does it include the class and package?
         */
        public String action;
        /**
         * @todo - are these the required args in the routing file, or the query
         * string in a request?
         */
        public Map<String, Object> args;

        public ActionDefinition add(String key, Object value) {
            args.put(key, value);
            return reverse(action, args);
        }

        public ActionDefinition remove(String key) {
            args.remove(key);
            return reverse(action, args);
        }

        public ActionDefinition addRef(String fragment) {
            url += "#" + fragment;
            return this;
        }

        @Override
        public String toString() {
            return url;
        }

        public void absolute() {
	        HTTPRequestAdapter currentRequest = HTTPRequestAdapter.getCurrent();
            if (!url.startsWith("http")) {
                if (host == null || host.isEmpty()) {
                    url = currentRequest.getBase() + url;
                } else {
                    url = (currentRequest.secure ? "https://" : "http://") + host + url;
                }
            }
        }

        public ActionDefinition secure() {
            if (!url.contains("http://") && !url.contains("https://")) {
                absolute();
            }
            url = url.replace("http:", "https:");
            return this;
        }
    }

    public static class Route {

        public String getAction() {
            return action;
        }

        public String getHost() {
            return host;
        }

        public String getMethod() {
            return method;
        }

        public String getPath() {
            return path;
        }

        public List<Arg> getArgs() {
            return args;
        }

        public Map<String, String> getStaticArgs() {
            return staticArgs;
        }



        /**
         * HTTP method, e.g. "GET".
         */
        public String method;
        public String path;
        public String action;
        Pattern actionPattern;
        List<String> actionArgs = new ArrayList<String>(3);
        Pattern pattern;
        Pattern hostPattern;
        List<Arg> args = new ArrayList<Arg>(3);
        Map<String, String> staticArgs = new HashMap<String, String>(3);
        List<String> formats = new ArrayList<String>(1);
        String host;
        Arg hostArg = null;
        public int routesFileLine;
        public String routesFile;
        static Pattern customRegexPattern = new Pattern("\\{([a-zA-Z_0-9]+)\\}");
        static Pattern argsPattern = new Pattern("\\{<([^>]+)>([a-zA-Z_0-9]+)\\}");
        static Pattern paramPattern = new Pattern("\\s*([a-zA-Z_0-9]+)\\s*:\\s*'(.*)'\\s*");

        public void compute() {
            this.host = "";
            this.hostPattern = new Pattern(".*");


            // URL pattern
            // Is there is a host argument, append it.
            if (!path.startsWith("/")) {
                String p = this.path;
                this.path = p.substring(p.indexOf("/"));
                this.host = p.substring(0, p.indexOf("/"));
                String pattern = host.replaceAll("\\.", "\\\\.").replaceAll("\\{.*\\}", "(.*)");

                if (logger.isTraceEnabled()) {
                    logger.trace("pattern [" + pattern + "]");
                    logger.trace("host [" + host + "]");
                }

                Matcher m = new Pattern(pattern).matcher(host);
                this.hostPattern = new Pattern(pattern);

                if (m.matches()) {
                    if (this.host.contains("{")) {
                        String name = m.group(1).replace("{", "").replace("}", "");
                        hostArg = new Arg();
                        hostArg.name = name;
                        if (logger.isTraceEnabled()) {
                            logger.trace("hostArg name [" + name + "]");
                        }
                        // The default value contains the route version of the host ie {client}.bla.com
                        // It is temporary and it indicates it is an url route.
                        // TODO Check that default value is actually used for other cases.
                        hostArg.defaultValue = host;
                        hostArg.constraint = new Pattern(".*");

                        if (logger.isTraceEnabled()) {
                            logger.trace("adding hostArg [" + hostArg + "]");
                        }

                        args.add(hostArg);
                    }
                }
            }
            String patternString = path;
            patternString = customRegexPattern.replacer("\\{<[^/]+>$1\\}").replace(patternString);
            Matcher matcher = argsPattern.matcher(patternString);
            while (matcher.find()) {
                Arg arg = new Arg();
                arg.name = matcher.group(2);
                arg.constraint = new Pattern(matcher.group(1));
                args.add(arg);
            }

            patternString = argsPattern.replacer("({$2}$1)").replace(patternString);
            this.pattern = new Pattern(patternString);
            // Action pattern
            patternString = action;
            patternString = patternString.replace(".", "[.]");
            for (Arg arg : args) {
                if (patternString.contains("{" + arg.name + "}")) {
                    patternString = patternString.replace("{" + arg.name + "}", "({" + arg.name + "}" + arg.constraint.toString() + ")");
                    actionArgs.add(arg.name);
                }
            }
            actionPattern = new Pattern(patternString, REFlags.IGNORE_CASE);
        }

        public void addParams(String params) {
            if (params == null || params.length() < 1) {
                return;
            }
            params = params.substring(1, params.length() - 1);
            for (String param : params.split(",")) {
                Matcher matcher = paramPattern.matcher(param);
                if (matcher.matches()) {
                    staticArgs.put(matcher.group(1), matcher.group(2));
                } else {
                    logger.warn("Ignoring " + param + " (static params must be specified as key:'value',...)");
                }
            }
        }

        // TODO: Add args names
        public void addFormat(String params) {
            if (params == null || params.length() < 1) {
                return;
            }
            params = params.trim();
            formats.addAll(Arrays.asList(params.split(",")));
        }

        private boolean contains(String accept) {
            boolean contains = (accept == null);
            if (accept != null) {
                if (this.formats.isEmpty()) {
                    return true;
                }
                for (String format : this.formats) {
                    contains = format.startsWith(accept);
                    if (contains) {
                        break;
                    }
                }
            }
            return contains;
        }

        public Map<String, String> matches(String method, String path) {
            return matches(method, path, null, null);
        }

        public Map<String, String> matches(String method, String path, String accept) {
            return matches(method, path, accept, null);
        }

        /**
         * Check if the parts of a HTTP request equal this Route.
         *
         * @param method GET/POST/etc.
         * @param path   Part after domain and before query-string. Starts with a
         *               "/".
         * @param accept Format, e.g. html.
         * @param domain the domain.
         * @return ???
         */
        public Map<String, String> matches(String method, String path, String accept, String domain) {
            // If method is HEAD and we have a GET
            if (method == null || this.method.equals("*") || method.equalsIgnoreCase(this.method) || (method.equalsIgnoreCase("head") && ("get").equalsIgnoreCase(this.method))) {

                Matcher matcher = pattern.matcher(path);

                boolean hostMatches = (domain == null);
                if (domain != null) {
                    Matcher hostMatcher = hostPattern.matcher(domain);
                    hostMatches = hostMatcher.matches();
                }
                // Extract the host variable
                if (matcher.matches() && contains(accept) && hostMatches) {

                    Map<String, String> localArgs = new HashMap<String, String>();
                    for (Arg arg : args) {
                        // FIXME: Careful with the arguments that are not matching as they are part of the hostname
                        // Defaultvalue indicates it is a one of these urls. This is a trick and should be changed.
                        if (arg.defaultValue == null) {
                            localArgs.put(arg.name, matcher.group(arg.name));
                        }
                    }
                    if (hostArg != null && domain != null) {
                        // Parse the hostname and get only the part we are interested in
                        String routeValue = hostArg.defaultValue.replaceAll("\\{.*}", "");
                        domain = domain.replace(routeValue, "");
                        localArgs.put(hostArg.name, domain);
                    }
                    localArgs.putAll(staticArgs);
                    return localArgs;
                }
            }
            return null;
        }

        public static class Arg {

            String name;
            Pattern constraint;
            String defaultValue;
            Boolean optional = false;

            public String getName() {
                return name;
            }

            public String getDefaultValue() {
                return defaultValue;
            }
        }

        @Override
        public String toString() {
            return method + " " + path + " -> " + action;
        }
    }

}
