package org.resthub.web.springmvc.router;

public class RouterHandler {

	Router.Route route;
	
	HTTPRequestAdapter request;

	public Router.Route getRoute() {
		return route;
	}

	public void setRoute(Router.Route route) {
		this.route = route;
	}

	public HTTPRequestAdapter getRequest() {
		return request;
	}

	public void setRequest(HTTPRequestAdapter request) {
		this.request = request;
	}
	
}
