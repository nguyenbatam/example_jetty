package com.example.jetty.filters;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;

@Provider
public class JettyResponseFilter implements ContainerResponseFilter {

	@Context
	HttpServletRequest req;

	@Context
	HttpServletResponse res;

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
		if (responseContext.getStatus() != 200) {
			Logger.getRootLogger()	
					.error("Error Reqest HTTP code = " + responseContext.getStatus() + " uri="
							+ requestContext.getUriInfo().getRequestUri());
			res.setStatus(Response.Status.OK.getStatusCode());
		}
	}

}

