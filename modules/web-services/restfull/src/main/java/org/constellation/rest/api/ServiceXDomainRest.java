package org.constellation.rest.api;

import org.constellation.business.IServiceBusiness;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path("/1/serviceXdomain")
public class ServiceXDomainRest {

        
    @Inject
    private IServiceBusiness serviceBusiness;

    @POST
    @Path("/{domainId}/service/{serviceId}")
    public Response post(@PathParam("serviceId") int serviceId,@PathParam("domainId") int domainId) {
        

        serviceBusiness.addServiceToDomain(serviceId, domainId);
        
        return Response.noContent().type(MediaType.TEXT_PLAIN_TYPE).build();
    }
    

    @DELETE
    @Path("/{domainId}/service/{serviceId}")
    public Response delete(@PathParam("serviceId") int serviceId, @PathParam("domainId") int domainId) {
        
        serviceBusiness.removeServiceFromDomain(serviceId, domainId);
        
        return Response.noContent().type(MediaType.TEXT_PLAIN_TYPE).build();
    }

}
