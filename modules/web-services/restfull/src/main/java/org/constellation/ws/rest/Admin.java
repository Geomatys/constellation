package org.constellation.ws.rest;

import org.apache.sis.util.logging.Logging;
import org.constellation.configuration.AbstractConfigurer;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.ServiceReport;
import org.constellation.configuration.ws.rs.ConfigurationUtilities;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.WSEngine;
import org.constellation.ws.rest.post.Configuration;
import org.constellation.ws.rest.post.Restart;
import org.constellation.ws.rs.ContainerNotifierImpl;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Restfull main configuration service
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 */
@Path("/admin/")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class Admin {

    protected static final Logger LOGGER = Logging.getLogger(Admin.class);

    /**
     * A container notifier allowing to dynamically reload all the active service.
     */
    @Context
    protected volatile ContainerNotifierImpl cn;

    /**
     * The implementation specific configurers.
     */
    private final List<AbstractConfigurer> configurers = new ArrayList<AbstractConfigurer>();

    /**
     * service to return available service list
     *
     * @return
     */
    @GET
    @Path("availablesServices/")
    public Response AvailablesServices() {
        final ServiceReport response = new ServiceReport(WSEngine.getRegisteredServices());
        return Response.ok(response).build();
    }

    /**
     * Restart all services
     * @param restart {@link Restart} with {@link Boolean} to know if restart is forced
     * @return an {@link AcknowlegementType} on {@link Response} to know operation state
     */
    @POST
    @Path("operation/restart")
    public Response restart(final Restart restart) {
        return Response.ok(ConfigurationUtilities.restartService(restart.isForced(), configurers, cn)).build();
    }

    /**
     *
     * @return configuration path on a {@link Response}
     * @throws CstlServiceException
     */
    @GET
    @Path("configurationPath")
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
    @Path("configurationPath")
    public Response configurationPath(final Configuration configuration) throws CstlServiceException {
        return Response.ok(ConfigurationUtilities.setConfigPath(configuration.getPath())).build();
    }

}
