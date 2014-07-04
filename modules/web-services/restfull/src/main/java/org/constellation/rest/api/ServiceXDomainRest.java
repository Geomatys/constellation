package org.constellation.rest.api;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.constellation.admin.ServiceBusiness;

@Path("/1/serviceXdomain")
public class ServiceXDomainRest {

        
    @Inject
    private ServiceBusiness serviceBusiness;

    @POST
    @Path("/{domainId}/service/{serviceId}")
    public Response post(@PathParam("serviceId") int serviceId,@PathParam("domainId") int domainId) {
        

        serviceBusiness.addServiceToDomain(serviceId, domainId);
        
        return Response.noContent().build();
    }
    

    @DELETE
    @Path("/{domainId}/service/{serviceId}")
    public Response delete(@PathParam("serviceId") int serviceId, @PathParam("domainId") int domainId) {
        
        serviceBusiness.removeServiceFromDomain(serviceId, domainId);
        
        return Response.noContent().build();
    }

}
