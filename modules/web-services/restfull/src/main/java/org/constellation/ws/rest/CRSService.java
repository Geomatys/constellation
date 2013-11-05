package org.constellation.ws.rest;


import org.apache.sis.util.logging.Logging;
import org.constellation.dto.ParameterValues;
import org.geotoolkit.factory.AuthorityFactoryFinder;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.FactoryException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.HashMap;
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
    @Path("all")
    public Response getAll(){
        final CRSAuthorityFactory factory = AuthorityFactoryFinder.getCRSAuthorityFactory("EPSG", null);
        HashMap<String,String> allCodes = new HashMap<>(0);
        try {
            allCodes = toWKTMap(factory, factory.getAuthorityCodes(CoordinateReferenceSystem.class));
        } catch (FactoryException e) {
            LOGGER.log(Level.WARNING, "Error when search codes CRS", e);
        }
        ParameterValues pv = new ParameterValues(allCodes);
        return Response.ok(pv).build();
    }

    private static HashMap<String,String> toWKTMap(final CRSAuthorityFactory factory, final Collection<String> codes){
        final HashMap<String,String> map = new HashMap<>(0);

        for(final String code : codes){

            try{
                final IdentifiedObject obj = factory.createObject(code);
                final String wkt = obj.getName().toString();
                map.put(code, wkt);
            }catch(Exception ex){
                //some objects can not be expressed in WKT, we skip them
                if(LOGGER.isLoggable(Level.FINEST)){
                    LOGGER.log(Level.FINEST, "not available in WKT : " + code);
                }
            }
        }
        return map;
    }
}
