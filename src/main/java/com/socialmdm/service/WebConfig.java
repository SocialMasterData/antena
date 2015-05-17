package com.socialmdm.service;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

public class WebConfig extends Application {

    private Set<Object> singletons = new HashSet<Object>();
    private Set<Class<?>> empty = new HashSet<Class<?>>();

    public WebConfig() {
        // ADD YOUR RESTFUL RESOURCES HERE
        this.singletons.add(new SocialMediaService());
    }

    public Set<Class<?>> getClasses()
    {
        return this.empty;
    }

    public Set<Object> getSingletons()
    {
        return this.singletons;
    }

}
