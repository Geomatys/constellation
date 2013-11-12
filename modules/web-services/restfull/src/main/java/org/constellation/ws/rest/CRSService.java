package org.constellation.ws.rest;


import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.logging.Logging;
import org.constellation.configuration.StringList;
import org.constellation.dto.CRSCoverageList;
import org.constellation.dto.ParameterValues;
import org.constellation.dto.SimpleValue;
import org.constellation.utils.CRSUtilities;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.rs.LayerProviders;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * REST API to access to EPSG
 *
 * @author bgarcia
 * @version 0.9
 * @since 0.9
 *
 */
@Path("/1/crs")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class CRSService {

    private static final Logger LOGGER = Logging.getLogger(CRSService.class);

    /**
     * @return All EPSG CRS
     */
    @GET
    @Path("all/{start}/{nbByPage}/{filter}")
    public Response getAll(@PathParam("start") int start, @PathParam("nbByPage") int nbByPage, @PathParam("filter") String filter){
        final CRSCoverageList coverageList = CRSUtilities.pagingAndFilterCode(start, nbByPage, filter);
        return Response.ok(coverageList).build();
    }



    /**
     * return crs list string for a layer
     * @param providerId provider identifier which contain layer
     * @param LayerId layer identifier on provider
     * @return crs {@link String} {@link java.util.List}
     */
    @GET
    @Path("{id}/{layer}")
    public Response getCrsList(@PathParam("id") final String providerId, @PathParam("layer") final String LayerId){
        List<String> crs;
        try {
            crs = LayerProviders.getCrs(providerId, LayerId);
        } catch (CstlServiceException | IOException | DataStoreException e) {
            LOGGER.log(Level.WARNING, "error when search CRS", e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        final StringList sl = new StringList(crs);
        return Response.ok(sl).build();
    }

}
