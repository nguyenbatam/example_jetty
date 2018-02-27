package com.example.jetty;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;

import com.example.eventbus.publish.InteractPublisher;
import com.example.jetty.gzip.GzipBasicHandler;
import com.example.services.Cache.CacheManager;
import com.example.utils.ConfigUtils;

public class Main {

	public static void main(String[] args) throws Exception {

		// initiation single thread publish event through rabbitMQ 
		InteractPublisher.initThreadPublisher();
		// initiation single thread check change information in config file
		ConfigUtils.initcheckingThread();
		
		int http_port = 8888;
		try {
			http_port = Integer.valueOf(System.getProperty("http_port"));
		} catch (Exception e) {

		}
		// initiation a subscriber guava cache to update data
		CacheManager.start("jetty-example-" + http_port);
		System.out.println("http_port = " + http_port);

		// Start Jetty Server
		Server server = new Server(http_port);
		WebAppContext context = new WebAppContext();
		context.setDescriptor("Jetty");
		context.setResourceBase("");
		context.setContextPath("/example/api/v1.0/"); // base prefix uri

		ServletHolder h = new ServletHolder(new HttpServletDispatcher());
		
		// Add all resource api
		h.setInitParameter("javax.ws.rs.Application", MainApplication.class.getCanonicalName());

		context.addServlet(h, "/*");
		
		context.setParentLoaderPriority(true);
		// add gzip handler
		GzipBasicHandler gzipHandler = new GzipBasicHandler();
		context.setGzipHandler(gzipHandler);
		
		server.setHandler(context);
		server.start();
		server.join();
	}
}
