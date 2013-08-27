package org.constellation.ws.rest;

import org.apache.sis.util.logging.Logging;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.InstanceReport;
import org.constellation.configuration.ServiceReport;
import org.constellation.configuration.ws.rs.ConfigurationUtilities;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.WSEngine;
import org.constellation.dto.Configuration;
import org.constellation.ws.rs.OGCServiceConfiguration;
import org.constellation.ws.rs.ServiceConfiguration;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import java.util.logging.Level;
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

    @GET
    @Path("status")
    public Response listintances(){
        OGCServiceConfiguration sc = new OGCServiceConfiguration();
        InstanceReport report = sc.listInstance();
        return Response.ok(report).build();
    }
}
