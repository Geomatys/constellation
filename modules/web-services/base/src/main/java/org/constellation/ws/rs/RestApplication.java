package org.constellation.ws.rs;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class RestApplication extends ResourceConfig {
    public RestApplication() {
         super(JacksonFeature.class);
    }
}
