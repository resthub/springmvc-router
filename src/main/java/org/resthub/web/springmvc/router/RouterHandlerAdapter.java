package org.resthub.web.springmvc.router;


import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.Principal;
import java.util.*;
import javassist.CtClass;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.resthub.web.springmvc.router.exceptions.ActionNotFoundException;
import org.resthub.web.springmvc.router.support.MessageConverterHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.*;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.support.BindingAwareModelMap;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpSessionRequiredException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.support.HandlerMethodInvoker;
import org.springframework.web.bind.annotation.support.HandlerMethodResolver;
import org.springframework.web.bind.support.DefaultSessionAttributeStore;
import org.springframework.web.bind.support.SessionAttributeStore;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestScope;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.annotation.ModelAndViewResolver;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.support.WebContentGenerator;
import org.springframework.web.util.UrlPathHelper;
import org.springframework.web.util.WebUtils;

/**
 * Implementation of the {@link org.springframework.web.servlet.HandlerAdapter} interface
 * that maps HTTPServletRequests on Routes defined in route file configuration.
 * The route file name can be configured
 * handler methods based on RoutesHTTP paths, HTTP methods and request parameters
 * expressed through the {@link RequestMapping} annotation.
 *
 * <p>Supports request parameter binding through the {@link RequestParam} annotation.
 * Also supports the {@link ModelAttribute} annotation for exposing model attribute
 * values to the view, as well as {@link InitBinder} for binder initialization methods
 * and {@link SessionAttributes} for automatic session management of specific attributes.
 *
 * <p>This adapter can be customized through various bean properties.
 * A common use case is to apply shared binder initialization logic through
 * a custom {@link #setWebBindingInitializer WebBindingInitializer}.
 *
 * @author Brian Clozel
 *
 */
public class RouterHandlerAdapter extends WebContentGenerator implements
        HandlerAdapter, Ordered {

    /**
     * order value for this HandlerAdapter bean.
     * <p>Default value is <code>Ordered.LOWEST_PRECEDENCE</code>, meaning that it's non-ordered.
     * @see org.springframework.core.Ordered
     */
    private int order = Ordered.LOWEST_PRECEDENCE;
    private UrlPathHelper urlPathHelper = new UrlPathHelper();
    private WebBindingInitializer webBindingInitializer;
    private SessionAttributeStore sessionAttributeStore = new DefaultSessionAttributeStore();
    private ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();
    private WebArgumentResolver[] customArgumentResolvers;
    private ModelAndViewResolver[] customModelAndViewResolvers;
    private MessageConverterHolder messageConverterHolder;
    private ConfigurableBeanFactory beanFactory;
    private BeanExpressionContext expressionContext;
    private Map<String, Object> cachedControllers;
    private final Logger logger = LoggerFactory.getLogger(RouterHandlerAdapter.class); 

    public RouterHandlerAdapter() {
        // no restriction of HTTP methods by default
        super(false);

        this.messageConverterHolder = new MessageConverterHolder();
    }

    /**
     * Get the order value for this HandlerAdapter bean.
     * <p>Default value is <code>Ordered.LOWEST_PRECEDENCE</code>, meaning that it's non-ordered.
     * @see org.springframework.core.Ordered#getOrder()
     */
    @Override
    public int getOrder() {
        return this.order;
    }

    /**
     * Specify the order value for this HandlerAdapter bean.
     * <p>Default value is <code>Ordered.LOWEST_PRECEDENCE</code>, meaning that it's non-ordered.
     * @see org.springframework.core.Ordered#getOrder()
     */
    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * Specify a WebBindingInitializer which will apply pre-configured
     * configuration to every DataBinder that this controller uses.
     */
    public void setWebBindingInitializer(WebBindingInitializer webBindingInitializer) {
        this.webBindingInitializer = webBindingInitializer;
    }

    /**
     * Set a custom WebArgumentResolvers to use for special method parameter types.
     * <p>Such a custom WebArgumentResolver will kick in first, having a chance to resolve
     * an argument value before the standard argument handling kicks in.
     */
    public void setCustomArgumentResolver(WebArgumentResolver argumentResolver) {
        this.customArgumentResolvers = new WebArgumentResolver[]{argumentResolver};
    }

    /**
     * Set one or more custom WebArgumentResolvers to use for special method parameter types.
     * <p>Any such custom WebArgumentResolver will kick in first, having a chance to resolve
     * an argument value before the standard argument handling kicks in.
     */
    public void setCustomArgumentResolvers(WebArgumentResolver[] argumentResolvers) {
        this.customArgumentResolvers = argumentResolvers;
    }

    /**
     * Set one or more custom ModelAndViewResolvers to use for special method return types.
     * <p>Any such custom ModelAndViewResolver will kick in first, having a chance to resolve
     * a return value before the standard ModelAndView handling kicks in.
     */
    public void setCustomModelAndViewResolvers(ModelAndViewResolver[] customModelAndViewResolvers) {
        this.customModelAndViewResolvers = customModelAndViewResolvers;
    }

	/**
	 * Provide the converters to use in argument resolvers and return value 
	 * handlers that support reading and/or writing to the body of the 
	 * request and response.
	 */
	public void setMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
		this.messageConverterHolder.setMessageConverters(messageConverters);
	}

	/**
	 * Return the configured message body converters.
	 */
	public List<HttpMessageConverter<?>> getMessageConverters() {
		return this.messageConverterHolder.getMessageConverters();
	}

    /**
     * Template method for creating a new ServletRequestDataBinder instance.
     * <p>The default implementation creates a standard ServletRequestDataBinder.
     * This can be overridden for custom ServletRequestDataBinder subclasses.
     * @param request current HTTP request
     * @param target the target object to bind onto (or <code>null</code>
     * if the binder is just used to convert a plain parameter value)
     * @param objectName the objectName of the target object
     * @return the ServletRequestDataBinder instance to use
     * @throws Exception in case of invalid state or arguments
     * @see ServletRequestDataBinder#bind(javax.servlet.ServletRequest)
     * @see ServletRequestDataBinder#convertIfNecessary(Object, Class, org.springframework.core.MethodParameter)
     */
    protected ServletRequestDataBinder createBinder(HttpServletRequest request, Object target, String objectName)
            throws Exception {
        return new ServletRequestDataBinder(target, objectName);
    }

    /**
     * Template method for creating a new HttpInputMessage instance.
     * <p>The default implementation creates a standard {@link ServletServerHttpRequest}.
     * This can be overridden for custom {@code HttpInputMessage} implementations
     * @param servletRequest current HTTP request
     * @return the HttpInputMessage instance to use
     * @throws Exception in case of errors
     */
    protected HttpInputMessage createHttpInputMessage(HttpServletRequest servletRequest) throws Exception {
        return new ServletServerHttpRequest(servletRequest);
    }

    /**
     * Template method for creating a new HttpOuputMessage instance.
     * <p>The default implementation creates a standard {@link ServletServerHttpResponse}.
     * This can be overridden for custom {@code HttpOutputMessage} implementations
     * @param servletResponse current HTTP response
     * @return the HttpInputMessage instance to use
     * @throws Exception in case of errors
     */
    protected HttpOutputMessage createHttpOutputMessage(HttpServletResponse servletResponse) throws Exception {
        return new ServletServerHttpResponse(servletResponse);
    }

    public void setBeanFactory(BeanFactory beanFactory) {
        if (beanFactory instanceof ConfigurableBeanFactory) {
            this.beanFactory = (ConfigurableBeanFactory) beanFactory;
            this.expressionContext = new BeanExpressionContext(this.beanFactory, new RequestScope());
        }
    }

    @Override
    public long getLastModified(HttpServletRequest request, Object handler) {
        return -1;
    }

    /**
     * Load and cache @Controller classes at application start.
     * @throws BeansException
     */
    @Override
    protected void initApplicationContext() throws BeansException {
        super.initApplicationContext();

        //load controllers from application context
        this.cachedControllers = getApplicationContext().getBeansWithAnnotation(Controller.class);
    }

    /**
     * Says wether or not this handlerAdapter supports the given handler,
     * i.e. knows how to handle requests mapped with this handler.
     * @param handler the handler to adapt
     * @return true if the current adapter supports the handler given in
     * parameter.
     */
    @Override
    public boolean supports(Object handler) {

        return (handler instanceof RouterHandler);
    }

    /**
     * Handles a request, calling the matched controller+action.
     * @param request the HTTP request
     * @param response the HTTP response
     * @param handler the handler mapped for this request (should be an instance
     * of RouterHandler)
     * @return a ModelAndview (ModelMap + ViewName) resulting from the
     * controller.action call.
     * @throws Exception
     */
    @Override
    public ModelAndView handle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {

        // routing is done
        RouterHandler routerHandler = (RouterHandler) handler;

        HTTPRequestAdapter req = routerHandler.getRequest();
        Router.Route route = routerHandler.getRoute();

        // 1. Find the action method
        Method actionMethod = null;
        RouterMethodResolver methodResolver = new RouterMethodResolver();

        Object[] ca = methodResolver.resolveActionMethod(this.cachedControllers, req.action);
        actionMethod = (Method) ca[1];
        req.controller = ca[0].getClass().getName();
        req.controllerClass = ca[0].getClass();
        req.actionMethod = actionMethod.getName();
        req.action = req.controller + "." + req.actionMethod;
        req.invokedMethod = actionMethod;


        logger.trace("------- " + actionMethod);

        // 2. resolve args found in path and set them as attribute

        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, req.routeArgs);


        // 3. Invoke the action

        // @Before


        // Execute Action
        ModelAndView mav = null;

        RouterhandlerMethodInvoker methodInvoker = new RouterhandlerMethodInvoker(methodResolver);
        ServletWebRequest webRequest = new ServletWebRequest(request, response);
        ExtendedModelMap implicitModel = new BindingAwareModelMap();

        Object result = methodInvoker.invokeHandlerMethod(actionMethod, ca[0], webRequest, implicitModel);

        mav = methodInvoker.getModelAndView(actionMethod, req.controllerClass, result, implicitModel, webRequest);
        methodInvoker.updateModelAttributes(handler, (mav != null ? mav.getModel() : null), implicitModel, webRequest);


        // @After

        return mav;
    }

    protected void addReturnValueAsModelAttribute(Method handlerMethod, Class handlerType,
            Object returnValue, ExtendedModelMap implicitModel) {

        ModelAttribute attr = AnnotationUtils.findAnnotation(handlerMethod, ModelAttribute.class);
        String attrName = (attr != null ? attr.value() : "");
        if ("".equals(attrName)) {
            Class resolvedType = GenericTypeResolver.resolveReturnType(handlerMethod, handlerType);
            attrName = Conventions.getVariableNameForReturnType(handlerMethod, resolvedType, returnValue);
        }
        implicitModel.addAttribute(attrName, returnValue);
    }

    /**
     * Resolve Controller and Action for the given route (that contains the fullAction "controller.action")
     * @author Brian Clozel
     *
     */
    public class RouterMethodResolver extends HandlerMethodResolver {

        /**
         *
         * @param controllers
         * @param fullAction
         * @return
         * @throws ActionNotFoundException
         */
        private Object[] resolveActionMethod(Map<String, Object> controllers, String fullAction) throws ActionNotFoundException {

            Method actionMethod = null;
            Object controllerObject = null;

            String controller = fullAction.substring(0, fullAction.lastIndexOf("."));
            String action = fullAction.substring(fullAction.lastIndexOf(".") + 1);
            controllerObject = controllers.get(controller);

            if (controllerObject == null) {
                throw new ActionNotFoundException(fullAction, new Exception("Controller " + controller + " not found"));
            }

            // find actionMethod on target
            actionMethod = findActionMethod(action, controllerObject);

            if (actionMethod == null) {
                throw new ActionNotFoundException(fullAction, new Exception("No method public static void " + action + "() was found in class " + controller));
            }

            return new Object[]{controllerObject, actionMethod};
        }

        /**
         * Find the first public static method of a controller class
         * @param name The method name
         * @param controller The controller
         * @return The method or null
         */
        public Method findActionMethod(String name, Object controller) {

            //get the controller class
            //(or the corresponding target class if the current controller
            // instance is an AOP proxy
            Class clazz = AopUtils.getTargetClass(controller);
            
            while (!clazz.getName().equals("java.lang.Object")) {
                for (Method m : clazz.getDeclaredMethods()) {
                    if (m.getName().equalsIgnoreCase(name) && Modifier.isPublic(m.getModifiers())) {
                        // Check annotations?
                        //if (!m.isAnnotationPresent(Before.class) && !m.isAnnotationPresent(After.class) && !m.isAnnotationPresent(Finally.class)) {
                        //return m;
                        return BridgeMethodResolver.findBridgedMethod(m);
                        //}
                    }
                }
                clazz = clazz.getSuperclass();
            }
            return null;
        }
    }

    /**
     * Runtime part.
     */
    public static class LocalVariablesNamesTracer {

        public static Integer computeMethodHash(CtClass[] parameters) {
            String[] names = new String[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                names[i] = parameters[i].getName();
            }
            return computeMethodHash(names);
        }

        public static Integer computeMethodHash(Class[] parameters) {
            String[] names = new String[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                Class param = parameters[i];
                names[i] = "";
                if (param.isArray()) {
                    int level = 1;
                    param = param.getComponentType();
                    // Array of array
                    while (param.isArray()) {
                        level++;
                        param = param.getComponentType();
                    }
                    names[i] = param.getName();
                    for (int j = 0; j < level; j++) {
                        names[i] += "[]";
                    }
                } else {
                    names[i] = param.getName();
                }
            }
            return computeMethodHash(names);
        }

        public static Integer computeMethodHash(String[] parameters) {
            StringBuffer buffer = new StringBuffer();
            for (String param : parameters) {
                buffer.append(param);
            }
            Integer hash = buffer.toString().hashCode();
            if (hash < 0) {
                return -hash;
            }
            return hash;
        }
    }

    /**
     * Servlet-specific subclass of {@link HandlerMethodInvoker}.
     */
    private class RouterhandlerMethodInvoker extends HandlerMethodInvoker {

        private boolean responseArgumentUsed = false;

        private RouterhandlerMethodInvoker(HandlerMethodResolver resolver) {
            super(resolver, webBindingInitializer, sessionAttributeStore, parameterNameDiscoverer,
                    customArgumentResolvers, messageConverterHolder.getMessageConverters().toArray(new HttpMessageConverter[0]));
        }

        @Override
        protected void raiseMissingParameterException(String paramName, Class paramType) throws Exception {
            throw new MissingServletRequestParameterException(paramName, paramType.getSimpleName());
        }

        @Override
        protected void raiseSessionRequiredException(String message) throws Exception {
            throw new HttpSessionRequiredException(message);
        }

        @Override
        protected WebDataBinder createBinder(NativeWebRequest webRequest, Object target, String objectName)
                throws Exception {

            return RouterHandlerAdapter.this.createBinder(
                    webRequest.getNativeRequest(HttpServletRequest.class), target, objectName);
        }

        @Override
        protected void doBind(WebDataBinder binder, NativeWebRequest webRequest) throws Exception {
            ServletRequestDataBinder servletBinder = (ServletRequestDataBinder) binder;
            servletBinder.bind(webRequest.getNativeRequest(ServletRequest.class));
        }

        @Override
        protected HttpInputMessage createHttpInputMessage(NativeWebRequest webRequest) throws Exception {
            HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
            return RouterHandlerAdapter.this.createHttpInputMessage(servletRequest);
        }

        @Override
        protected HttpOutputMessage createHttpOutputMessage(NativeWebRequest webRequest) throws Exception {
            HttpServletResponse servletResponse = (HttpServletResponse) webRequest.getNativeResponse();
            return RouterHandlerAdapter.this.createHttpOutputMessage(servletResponse);
        }

        @Override
        protected Object resolveDefaultValue(String value) {
            if (beanFactory == null) {
                return value;
            }
            String placeholdersResolved = beanFactory.resolveEmbeddedValue(value);
            BeanExpressionResolver exprResolver = beanFactory.getBeanExpressionResolver();
            if (exprResolver == null) {
                return value;
            }
            return exprResolver.evaluate(placeholdersResolved, expressionContext);
        }

        @Override
        protected Object resolveCookieValue(String cookieName, Class paramType, NativeWebRequest webRequest)
                throws Exception {

            HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
            Cookie cookieValue = WebUtils.getCookie(servletRequest, cookieName);
            if (Cookie.class.isAssignableFrom(paramType)) {
                return cookieValue;
            } else if (cookieValue != null) {
                return urlPathHelper.decodeRequestString(servletRequest, cookieValue.getValue());
            } else {
                return null;
            }
        }

        @Override
        @SuppressWarnings({"unchecked"})
        protected String resolvePathVariable(String pathVarName, Class paramType, NativeWebRequest webRequest)
                throws Exception {

            HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
            Map<String, String> uriTemplateVariables =
                    (Map<String, String>) servletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

            if (uriTemplateVariables == null || !uriTemplateVariables.containsKey(pathVarName)) {
                throw new IllegalStateException(
                        "Could not find @PathVariable [" + pathVarName + "] in @RequestMapping");
            }
            return uriTemplateVariables.get(pathVarName);
        }

        @Override
        protected Object resolveStandardArgument(Class<?> parameterType, NativeWebRequest webRequest) throws Exception {
            HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
            HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);

            if (ServletRequest.class.isAssignableFrom(parameterType)
                    || MultipartRequest.class.isAssignableFrom(parameterType)) {
                Object nativeRequest = webRequest.getNativeRequest(parameterType);
                if (nativeRequest == null) {
                    throw new IllegalStateException(
                            "Current request is not of type [" + parameterType.getName() + "]: " + request);
                }
                return nativeRequest;
            } else if (ServletResponse.class.isAssignableFrom(parameterType)) {
                this.responseArgumentUsed = true;
                Object nativeResponse = webRequest.getNativeResponse(parameterType);
                if (nativeResponse == null) {
                    throw new IllegalStateException(
                            "Current response is not of type [" + parameterType.getName() + "]: " + response);
                }
                return nativeResponse;
            } else if (HttpSession.class.isAssignableFrom(parameterType)) {
                return request.getSession();
            } else if (Principal.class.isAssignableFrom(parameterType)) {
                return request.getUserPrincipal();
            } else if (Locale.class.equals(parameterType)) {
                return RequestContextUtils.getLocale(request);
            } else if (InputStream.class.isAssignableFrom(parameterType)) {
                return request.getInputStream();
            } else if (Reader.class.isAssignableFrom(parameterType)) {
                return request.getReader();
            } else if (OutputStream.class.isAssignableFrom(parameterType)) {
                this.responseArgumentUsed = true;
                return response.getOutputStream();
            } else if (Writer.class.isAssignableFrom(parameterType)) {
                this.responseArgumentUsed = true;
                return response.getWriter();
            }
            return super.resolveStandardArgument(parameterType, webRequest);
        }

        @SuppressWarnings("unchecked")
        public ModelAndView getModelAndView(Method handlerMethod, Class handlerType, Object returnValue,
                ExtendedModelMap implicitModel, ServletWebRequest webRequest) throws Exception {

            ResponseStatus responseStatusAnn = AnnotationUtils.findAnnotation(handlerMethod, ResponseStatus.class);
            if (responseStatusAnn != null) {
                HttpStatus responseStatus = responseStatusAnn.value();
                String reason = responseStatusAnn.reason();
                if (!StringUtils.hasText(reason)) {
                    webRequest.getResponse().setStatus(responseStatus.value());
                } else {
                    webRequest.getResponse().sendError(responseStatus.value(), reason);
                }

                // to be picked up by the RedirectView
                webRequest.getRequest().setAttribute(View.RESPONSE_STATUS_ATTRIBUTE, responseStatus);

                responseArgumentUsed = true;
            }

            // Invoke custom resolvers if present...
            if (customModelAndViewResolvers != null) {
                for (ModelAndViewResolver mavResolver : customModelAndViewResolvers) {
                    ModelAndView mav = mavResolver.resolveModelAndView(
                            handlerMethod, handlerType, returnValue, implicitModel, webRequest);
                    if (mav != ModelAndViewResolver.UNRESOLVED) {
                        return mav;
                    }
                }
            }

            if (returnValue instanceof HttpEntity) {
                handleHttpEntityResponse((HttpEntity<?>) returnValue, webRequest);
                return null;
            } else if (AnnotationUtils.findAnnotation(handlerMethod, ResponseBody.class) != null) {
                handleResponseBody(returnValue, webRequest);
                return null;
            } else if (returnValue instanceof ModelAndView) {
                ModelAndView mav = (ModelAndView) returnValue;
                mav.getModelMap().mergeAttributes(implicitModel);
                return mav;
            } else if (returnValue instanceof Model) {
                return new ModelAndView().addAllObjects(implicitModel).addAllObjects(((Model) returnValue).asMap());
            } else if (returnValue instanceof View) {
                return new ModelAndView((View) returnValue).addAllObjects(implicitModel);
            } else if (AnnotationUtils.findAnnotation(handlerMethod, ModelAttribute.class) != null) {
                addReturnValueAsModelAttribute(handlerMethod, handlerType, returnValue, implicitModel);
                return new ModelAndView().addAllObjects(implicitModel);
            } else if (returnValue instanceof Map) {
                return new ModelAndView().addAllObjects(implicitModel).addAllObjects((Map) returnValue);
            } else if (returnValue instanceof String) {
                return new ModelAndView((String) returnValue).addAllObjects(implicitModel);
            } else if (returnValue == null) {
                // Either returned null or was 'void' return.
                if (this.responseArgumentUsed || webRequest.isNotModified()) {
                    return null;
                } else {
                    // Assuming view name translation...
                    return new ModelAndView().addAllObjects(implicitModel);
                }
            } else if (!BeanUtils.isSimpleProperty(returnValue.getClass())) {
                // Assume a single model attribute...
                addReturnValueAsModelAttribute(handlerMethod, handlerType, returnValue, implicitModel);
                return new ModelAndView().addAllObjects(implicitModel);
            } else {
                throw new IllegalArgumentException("Invalid handler method return value: " + returnValue);
            }
        }

        private void handleResponseBody(Object returnValue, ServletWebRequest webRequest)
                throws Exception {
            if (returnValue == null) {
                return;
            }
            HttpInputMessage inputMessage = createHttpInputMessage(webRequest);
            HttpOutputMessage outputMessage = createHttpOutputMessage(webRequest);
            writeWithMessageConverters(returnValue, inputMessage, outputMessage);
        }

        private void handleHttpEntityResponse(HttpEntity<?> responseEntity, ServletWebRequest webRequest)
                throws Exception {
            if (responseEntity == null) {
                return;
            }
            HttpInputMessage inputMessage = createHttpInputMessage(webRequest);
            HttpOutputMessage outputMessage = createHttpOutputMessage(webRequest);
            if (responseEntity instanceof ResponseEntity && outputMessage instanceof ServerHttpResponse) {
                ((ServerHttpResponse) outputMessage).setStatusCode(((ResponseEntity) responseEntity).getStatusCode());
            }
            HttpHeaders entityHeaders = responseEntity.getHeaders();
            if (!entityHeaders.isEmpty()) {
                outputMessage.getHeaders().putAll(entityHeaders);
            }
            Object body = responseEntity.getBody();
            if (body != null) {
                writeWithMessageConverters(body, inputMessage, outputMessage);
            } else {
                // flush headers
                outputMessage.getBody();
            }
        }

        @SuppressWarnings("unchecked")
        private void writeWithMessageConverters(Object returnValue,
                HttpInputMessage inputMessage, HttpOutputMessage outputMessage)
                throws IOException, HttpMediaTypeNotAcceptableException {
            List<MediaType> acceptedMediaTypes = inputMessage.getHeaders().getAccept();
            if (acceptedMediaTypes.isEmpty()) {
                acceptedMediaTypes = Collections.singletonList(MediaType.ALL);
            }
            MediaType.sortByQualityValue(acceptedMediaTypes);
            Class<?> returnValueType = returnValue.getClass();
            List<MediaType> allSupportedMediaTypes = new ArrayList<MediaType>();
            if (getMessageConverters() != null) {
                for (MediaType acceptedMediaType : acceptedMediaTypes) {
                    for (HttpMessageConverter messageConverter : getMessageConverters()) {
                        if (messageConverter.canWrite(returnValueType, acceptedMediaType)) {
                            messageConverter.write(returnValue, acceptedMediaType, outputMessage);
                            if (logger.isDebugEnabled()) {
                                MediaType contentType = outputMessage.getHeaders().getContentType();
                                if (contentType == null) {
                                    contentType = acceptedMediaType;
                                }
                                logger.debug("Written [" + returnValue + "] as \"" + contentType
                                        + "\" using [" + messageConverter + "]");
                            }
                            this.responseArgumentUsed = true;
                            return;
                        }
                    }
                }
                for (HttpMessageConverter messageConverter : messageConverterHolder.getMessageConverters()) {
                    allSupportedMediaTypes.addAll(messageConverter.getSupportedMediaTypes());
                }
            }
            throw new HttpMediaTypeNotAcceptableException(allSupportedMediaTypes);
        }
    }
}
