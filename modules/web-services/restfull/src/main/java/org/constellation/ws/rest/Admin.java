package org.constellation.ws.rest;

import org.apache.sis.util.logging.Logging;
import org.constellation.ServiceDef.Specification;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.Instance;
import org.constellation.configuration.InstanceReport;
import org.constellation.configuration.NotRunningServiceException;
import org.constellation.configuration.ServiceConfigurer;
import org.constellation.configuration.ServiceReport;
import org.constellation.configuration.ws.rs.ConfigurationUtilities;
import org.constellation.dto.Configuration;
import org.constellation.ogc.configuration.OGCConfigurer;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.WSEngine;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Restfull main configuration service
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 */
@Path("/1/admin/")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class Admin {

    private static final Logger LOGGER = Logging.getLogger(Admin.class);

    /**
     * service to return available service list
     *
     * @return
     */
    @GET
    @Path("serviceType/")
    public Response serviceType() {
        final ServiceReport response = new ServiceReport(WSEngine.getRegisteredServices());
        return Response.ok(response).build();
    }

    /**
     *
     * @return configuration path on a {@link Response}
     * @throws CstlServiceException
     */
    @GET
    @Path("configurationLocation")
    public Response configurationPath() throws CstlServiceException {
        return Response.ok(ConfigurationUtilities.getConfigPath()).build();
    }

    /**
     * Reset configuration path
     * @param configuration contain new path
     * @return an {@link AcknowlegementType} on {@link Response} to know operation state
     * @throws CstlServiceException
     */
    @POST
    @Path("configurationLocation")
    public Response configurationPath(final Configuration configuration) throws CstlServiceException {
        return Response.ok(ConfigurationUtilities.setConfigPath(configuration.getPath())).build();
    }

    /**
     *
     * @return
     */
    @GET
    @Path("instances")
    public Response listInstances(){
        final List<Instance> instances = new ArrayList<>();
        final Set<String> services = WSEngine.getRegisteredServices().keySet();
        for (final String service : services) {
            try {
                final Specification spec = Specification.fromShortName(service);
                final OGCConfigurer configurer = (OGCConfigurer) ServiceConfigurer.newInstance(spec);
                instances.addAll(configurer.getInstances());
            } catch (NotRunningServiceException ignore) {
            }
        }
        return Response.ok(new InstanceReport(instances)).build();
    }
}
