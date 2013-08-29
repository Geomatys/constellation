package org.constellation.ws.rest;

import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.Instance;
import org.constellation.configuration.InstanceReport;
import org.constellation.configuration.Layer;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.LayerList;
import org.constellation.dto.AddLayer;
import org.constellation.dto.Rename;
import org.constellation.dto.Restart;
import org.constellation.dto.Service;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.rs.OGCServiceConfiguration;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Restfull services administration (configuration, creation and managment)
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 */
@Path("/1/{serviceType}")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class ServiceAdmin {

    private static final Logger LOGGER = Logger.getLogger(ServiceAdmin.class.getName());

    /**
     * @see OGCServiceConfiguration
     */
    private OGCServiceConfiguration serviceConfiguration;

    public ServiceAdmin() {
        serviceConfiguration = new OGCServiceConfiguration();
    }

    /**
     * Service to get a service instance.
     *
     * @param serviceType the service type (WMS, CSW, WPS...)
     * @param id          the service identifier
     * @return a {@link Response} with service status
     */
    @GET
    @Path("{id}/instance")
    public Response getInstance(final @PathParam("serviceType") String serviceType, final @PathParam("id") String id) {
        final Instance instance = serviceConfiguration.getInstance(serviceType, id);
        return Response.ok(instance).build();
    }

    /**
     * Service to list service instance by type.
     *
     * @param serviceType a service type (WMS, WPS,...)
     * @return a {@link Response} with service listing
     */
    @GET
    @Path("instances")
    public Response listInstance(final @PathParam("serviceType") String serviceType) {
        InstanceReport report = serviceConfiguration.listInstance(serviceType);
        return Response.ok(report).build();
    }

    /**
     * Service to stop a specific service
     *
     * @param serviceType a service type (WMS, WPS,...)
     * @param id          service identifier
     * @return a {@link Response} with server state
     */
    @GET
    @Path("{id}/operation/stop")
    public Response stop(final @PathParam("serviceType") String serviceType, final @PathParam("id") String id) {
        AcknowlegementType response = serviceConfiguration.stop(serviceType, id);
        return Response.ok(response).build();
    }

    /**
     * Service to start a specific service
     *
     * @param serviceType a service type (WMS, WPS,...)
     * @param id          service identifier
     * @return a {@link Response} with server state
     */
    @GET
    @Path("{id}/operation/start")
    public Response start(final @PathParam("serviceType") String serviceType, final @PathParam("id") String id) {
        AcknowlegementType response = serviceConfiguration.start(serviceType, id);
        return Response.ok(response).build();
    }

    /**
     * Service to restart a specific service
     *
     * @param serviceType a service type (WMS, WPS,...)
     * @param id          service identifier
     * @param restart     Object to know if we close service before restart
     * @return a {@link Response} with server state
     */
    @POST
    @Path("{id}/operation/restart")
    public Response restart(final @PathParam("serviceType") String serviceType, final @PathParam("id") String id, Restart restart) {
        AcknowlegementType response = serviceConfiguration.restart(serviceType, id, restart.isCloseFirst());
        return Response.ok(response).build();
    }

    /**
     * Service to delete a specific service
     *
     * @param serviceType a service type (WMS, WPS,...)
     * @param id          service identifier
     * @return a {@link Response} with server state
     */
    @DELETE
    @Path("{id}")
    public Response delete(final @PathParam("serviceType") String serviceType, final @PathParam("id") String id) {
        AcknowlegementType response = serviceConfiguration.delete(serviceType, id);
        return Response.ok(response).build();
    }

    /**
     * Service to rename a specific service
     *
     * @param serviceType a service type (WMS, WPS,...)
     * @param id          service identifier
     * @param rename      Object which contains service new name
     * @return a {@link Response} with server state
     */
    @POST
    @Path("{id}/operation/rename")
    public Response rename(final @PathParam("serviceType") String serviceType, final @PathParam("id") String id, final Rename rename) throws CstlServiceException {
        AcknowlegementType response = serviceConfiguration.rename(serviceType, id, rename.getNewName());
        return Response.ok(response).build();
    }

    /**
     * @param serviceType a service type (WMS, WPS,...)
     * @param id          service identifier
     * @return a {@link Response} which contains service configuration
     */
    @GET
    @Path("{id}/configuration")
    public Response configuration(final @PathParam("serviceType") String serviceType, final @PathParam("id") String id) {
        LOGGER.info("sending instance configuration");
        Object response;
        try {
            response = serviceConfiguration.getConfiguration(serviceType, id);
        } catch (CstlServiceException e) {
            LOGGER.log(Level.WARNING, "", e);
            response = new AcknowlegementType("error", "unnable to access to configuration");
        }
        return Response.ok(response).build();
    }

    /**
     * Set configuration with Object sent
     *
     * @param serviceType   a service type (WMS, WPS,...)
     * @param id            service identifier
     * @param configuration unmarshalled object which contains sent data from client
     * @return a {@link Response} with server state
     * @throws CstlServiceException
     */
    @POST
    @Path("{id}/configuration")
    public Response configuration(final @PathParam("serviceType") String serviceType, final @PathParam("id") String id, final LayerContext configuration) throws CstlServiceException {
        final AcknowlegementType response = serviceConfiguration.configure(serviceType, id, configuration);
        return Response.ok(response).build();
    }

    /**
     * @see OGCServiceConfiguration#setMetadata(String, Service)
     */
    @POST
    @Path("metadata")
    public Response configure(final @PathParam("serviceType") String serviceType, final Service service) {
        try {
            serviceConfiguration.setMetadata(serviceType, service);
            return Response.ok(new AcknowlegementType("Success", "The service has been updated.")).build();
        } catch (CstlServiceException ex) {
            return Response.ok(new AcknowlegementType("Failure", ex.getLocalizedMessage())).build();
        }
    }

    /**
     * create a new service instance
     *
     * @param serviceType a service type (WMS, WPS,...)
     * @param service     unmarshalled object with new service informations
     * @return a {@link Response} with server state
     * @throws CstlServiceException
     */
    @POST
    @Path("create")
    public Response metadata(final @PathParam("serviceType") String serviceType, Service service) throws CstlServiceException {
        LOGGER.info("creating an instance");
        final AcknowlegementType response = serviceConfiguration.newInstance(serviceType, service.getIdentifier(), service);
        return Response.ok(response).build();
    }

    @GET
    @Path("{id}/metadata")
    public Response metadata(final @PathParam("serviceType") String serviceType, final @PathParam("id") String identifier) throws CstlServiceException {
        Service service = serviceConfiguration.getMetadata(serviceType, identifier);
        return Response.ok(service).build();
    }

    /**
     * Give service layer list
     * @param serviceType a service type (WMS, WPS,...)
     * @param id     service identifier
     * @return a {@link LayerList}
     */
    @GET
    @Path("{id}/layers")
    public Response layers(final @PathParam("serviceType") String serviceType, final @PathParam("id") String id){
        List<Layer> list = serviceConfiguration.getdatas(serviceType, id);
        LayerList layers = new LayerList(list);
        return Response.ok(layers).build();
    }

    @POST
    @Path("{id}/layer")
    public Response addLayer(final @PathParam("serviceType") String serviceType, final @PathParam("id") String id, final AddLayer addedLayer){
        boolean created = serviceConfiguration.addLayer(serviceType, id, addedLayer);
        if(created){
            return Response.ok("true").build();
        }else{
            return Response.ok("false").build();
        }
    }

}
