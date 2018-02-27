package com.example.jetty;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import com.example.jetty.filters.JettyResponseFilter;
import com.example.jetty.resources.UserResource;
import com.example.jetty.resources.VideoResource;

public class MainApplication extends Application {
	private Set<Object> singletons = new HashSet<>();
	private Set<Class<?>> empty = new HashSet<>();

	public MainApplication() {
		// add all resource api
		this.singletons.add(new VideoResource());
		this.singletons.add(new UserResource());

		// add a filter request and response
		this.singletons.add(new JettyResponseFilter());
	}

	public Set<Class<?>> getClasses() {
		return this.empty;
	}

	public Set<Object> getSingletons() {
		return this.singletons;
	}

}
