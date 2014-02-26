package org.constellation.ws.rest;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * RestFull user configuration service
 * 
 * @author Olivier NOUGUIER (Geomatys)
 * @version 0.9
 * @since 0.9
 */
@Named
@Path("/1/session")
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public class SessionService {

	@GET
	@Path("/logout")
	public Response findOne(@PathParam("id") String login, @Context HttpServletRequest req) {
		HttpSession session = req.getSession(false);
		if(session!=null)
			session.invalidate();
		return Response.ok("OK").build();
	}


}
