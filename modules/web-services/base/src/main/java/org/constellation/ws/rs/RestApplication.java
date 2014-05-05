package org.constellation.ws.rs;

import org.constellation.ws.rs.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;

public class RestApplication extends ResourceConfig {
    public RestApplication() {
         super(JacksonFeature.class, MultiPartFeature.class);
         
         register(new Hibernate4Module());
         
    }
}
