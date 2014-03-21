package org.constellation.ws.rs;

import org.constellation.ws.rs.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class RestApplication extends ResourceConfig {
    public RestApplication() {
         super(JacksonFeature.class, MultiPartFeature.class);
    }
}
