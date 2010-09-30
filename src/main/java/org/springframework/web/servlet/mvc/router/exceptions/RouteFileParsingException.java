package org.springframework.web.servlet.mvc.router.exceptions;

import org.springframework.beans.BeansException;

/**
 * Exception: Error while parsing route file
 * @author Brian Clozel
 * @see org.springframework.web.servlet.mvc.router.Router
 */
public class RouteFileParsingException extends BeansException {

	public RouteFileParsingException(String msg) {
		super(msg);
	}

	public RouteFileParsingException(String msg, Throwable e) {
		super(msg, e);
	}
}
