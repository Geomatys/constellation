package org.constellation.rest.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.constellation.admin.DataBusiness;
import org.constellation.engine.register.Domain;
import org.constellation.engine.register.PermissionConstants;
import org.constellation.engine.register.repository.DataRepository;
import org.constellation.engine.register.repository.DomainRepository;

@Path("/1/dataXdomain")
public class DataXDomainRest {

    @Inject
    private DataBusiness business;

    @Inject
    private DomainRepository domainRepository;

    @Inject
    private DataRepository dataRepository;

    @POST
    @Path("/{dataId}/domain/{domainId}")
    public Response post(@PathParam("dataId") int dataId, @PathParam("domainId") int domainId) {
        business.addDataToDomain(dataId, domainId);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{dataId}/domain/{domainId}")
    public Response delete(@PathParam("dataId") int dataId, @PathParam("domainId") int domainId) {
        business.removeDataFromDomain(dataId, domainId);
        return Response.noContent().build();
    }

    @GET
    @Path("/{dataId}/user/{userId}/domain")
    public Response linkedDomains(@PathParam("dataId") int dataId, @PathParam("userId") int userId) {
        Set<Integer> domainsById = domainRepository.findUserDomainIdsWithPermission(userId, PermissionConstants.DATA_CREATION);
        List<LinkedDomain> result = new ArrayList<>();
        for (Entry<Domain, Boolean> e : dataRepository.getLinkedDomains(dataId).entrySet()) {
            Domain domain = e.getKey();
            result.add(new LinkedDomain(domain, e.getValue(), domainsById.contains(domain.getId())));
        }
        return Response.ok(result).build();
    }

}
