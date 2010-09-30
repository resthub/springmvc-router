package org.springframework.web.servlet.mvc.router;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Adapter class for HTTP class defined in Play! Framework
 * Maps HTTPServletRequest to HTTP.Request and HTTP.Header
 * 
 * @author Brian Clozel
 * @see org.springframework.web.servlet.mvc.router.Router
 */
public class HTTPRequestAdapter {

	private static final Log logger = LogFactory
			.getLog(HTTPRequestAdapter.class);

	
    /**
     * Server host
     */
    public String host;
    /**
     * Request path
     */
    public String path;
    /**
     * QueryString
     */
    public String querystring;
    /**
     * Full url
     */
    public String url;
    /**
     * HTTP method
     */
    public String method;
    /**
     * Server domain
     */
    public String domain;
    /**
     * Client address
     */
    public String remoteAddress;
    /**
     * Request content-type
     */
    public String contentType;
    /**
     * Controller to invoke
     */
    public String controller;
    /**
     * Action method name
     */
    public String actionMethod;
    /**
     * HTTP port
     */
    public Integer port;
    /**
     * HTTP Headers
     */
    public Map<String, HTTPRequestAdapter.Header> headers = new HashMap<String, HTTPRequestAdapter.Header>();
    /**
     * Additinal HTTP params extracted from route
     */
    public Map<String, String> routeArgs;
    /**
     * Format (html,xml,json,text)
     */
    public String format = null;
    /**
     * Full action (ex: Application.index)
     */
    public String action;
    /**
     * The really invoker Java methid
     */
    public transient Method invokedMethod;
    /**
     * The invoked controller class
     */
    public transient Class controllerClass;
    /**
     * Free space to store your request specific data
     */
    public Map<String, Object> args = new HashMap<String, Object>();
    /**
     * When the request has been received
     */
    public Date date = new Date();
    /**
     * is HTTPS ?
     */
    public Boolean secure = false;
	/**
	 * Bind to thread
	 */
	public static ThreadLocal<HTTPRequestAdapter> current = new ThreadLocal<HTTPRequestAdapter>();

	
	public HTTPRequestAdapter(HttpServletRequest _request) {

		this.headers = new HashMap<String, Header>();
	}

	public void setFormat(String _format) {

		this.format = _format;
	}

	public boolean isSecure() {
		return secure;
	}

	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getQueryString() {
		return querystring;
	}

	public void setQueryString(String queryString) {
		this.querystring = queryString;
	}

	/**
	 * Get the request base (ex: http://localhost:9000
	 * 
	 * @return the request base of the url (protocol, host and port)
	 */
	public String getBase() {
		if (port == 80 || port == 443) {
			return String.format("%s://%s", secure ? "https" : "http", domain)
					.intern();
		}
		return String.format("%s://%s:%s", secure ? "https" : "http", domain,
				port).intern();
	}

	public static HTTPRequestAdapter parseRequest(
			HttpServletRequest httpServletRequest) throws Exception {
		HTTPRequestAdapter request = new HTTPRequestAdapter(httpServletRequest);

		HTTPRequestAdapter.current.set(request);

		URI uri = new URI(httpServletRequest.getRequestURI());
		request.method = httpServletRequest.getMethod().intern();
		request.path = uri.getPath();
		request.setQueryString(httpServletRequest.getQueryString() == null ? ""
				: httpServletRequest.getQueryString());

		logger.trace("httpServletRequest.getContextPath(): "
				+ httpServletRequest.getContextPath());
		logger.trace("request.path: " + request.path
				+ ", request.querystring: " + request.getQueryString());

		if (httpServletRequest.getHeader("Content-Type") != null) {
			request.contentType = httpServletRequest.getHeader("Content-Type")
					.split(";")[0].trim().toLowerCase().intern();
		} else {
			request.contentType = "text/html".intern();
		}

		if (httpServletRequest.getHeader("X-HTTP-Method-Override") != null) {
			request.method = httpServletRequest.getHeader(
					"X-HTTP-Method-Override").intern();
		}

		request.setSecure(httpServletRequest.isSecure());

		request.url = uri.toString();
		request.host = httpServletRequest.getHeader("host");
		if (request.host.contains(":")) {
			request.port = Integer.parseInt(request.host.split(":")[1]);
			request.domain = request.host.split(":")[0];
		} else {
			request.port = 80;
			request.domain = request.host;
		}

		request.remoteAddress = httpServletRequest.getRemoteAddr();

		Enumeration headersNames = httpServletRequest.getHeaderNames();
		while (headersNames.hasMoreElements()) {
			HTTPRequestAdapter.Header hd = request.new Header();
			hd.name = (String) headersNames.nextElement();
			hd.values = new ArrayList<String>();
			Enumeration enumValues = httpServletRequest.getHeaders(hd.name);
			while (enumValues.hasMoreElements()) {
				String value = (String) enumValues.nextElement();
				hd.values.add(value);
			}
			request.headers.put(hd.name.toLowerCase(), hd);
		}

		request.resolveFormat();

		return request;
	}

	/**
	 * Automatically resolve request format from the Accept header (in this
	 * order : html > xml > json > text)
	 */
	public void resolveFormat() {

		if (format != null) {
			return;
		}

		if (headers.get("accept") == null) {
			format = "html".intern();
			return;
		}

		String accept = headers.get("accept").value();

		if (accept.indexOf("application/xhtml") != -1
				|| accept.indexOf("text/html") != -1
				|| accept.startsWith("*/*")) {
			format = "html".intern();
			return;
		}

		if (accept.indexOf("application/xml") != -1
				|| accept.indexOf("text/xml") != -1) {
			format = "xml".intern();
			return;
		}

		if (accept.indexOf("text/plain") != -1) {
			format = "txt".intern();
			return;
		}

		if (accept.indexOf("application/json") != -1
				|| accept.indexOf("text/javascript") != -1) {
			format = "json".intern();
			return;
		}

		if (accept.endsWith("*/*")) {
			format = "html".intern();
			return;
		}
	}

	public class Header {

		public String name;

		public List<String> values;

		public Header() {

		}

		/**
		 * First value
		 * 
		 * @return The first value
		 */
		public String value() {
			return values.get(0);
		}

	}

}
