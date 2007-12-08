
package com.sun.ws.rest.wadl.resource;

import java.io.InputStream;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.UriInfo;
import com.sun.ws.rest.impl.wadl.WadlReader;


/**
 * This class was generated.
 * 
 * It is used to retrieve a WADL description
 * of all of the other resources
 * 
 * 
 */
@ProduceMime("application/vnd.sun.wadl+xml")
@UriTemplate("/application.wadl")
public class WadlResource {

    @HttpContext
    public UriInfo uriInfo;

    @HttpMethod("GET")
    public String getWadl() {
        InputStream is = this.getClass().getResourceAsStream("application.wadl");
        String str = WadlReader.read(is, uriInfo.getBase());
        return str;
    }

}
