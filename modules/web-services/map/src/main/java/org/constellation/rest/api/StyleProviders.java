/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package org.constellation.rest.api;

import org.constellation.configuration.AcknowlegementType;
import org.constellation.dto.StyleListBean;
import org.constellation.map.configuration.StyleProviderConfig;
import org.geotoolkit.style.MutableStyle;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.constellation.utils.RESTfulUtilities.ok;

/**
 * RESTful API for style providers configuration.
 *
 * @author Bernard Fabien (Geomatys)
 * @version 0.9
 * @since 0.9
 */
@Path("/1/SP")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public final class StyleProviders {

    /**
     * @see StyleProviderConfig#createStyle(String, MutableStyle)
     */
    @PUT
    @Path("{id}/style")
    public Response createStyle(final @PathParam("id") String id, final MutableStyle style) throws Exception {
        StyleProviderConfig.createStyle(id, style);
        return ok(AcknowlegementType.success("Style \"" + style.getName() + "\" successfully added to provider (" + id + ")."));
    }

    /**
     * @see StyleProviderConfig#getStyle(String, String)
     */
    @GET
    @Path("{id}/style/{styleId}")
    public Response getStyle(final @PathParam("id") String id, final @PathParam("styleId") String styleId) throws Exception {
        return ok(StyleProviderConfig.getStyle(id, styleId));
    }

    /**
     * @see StyleProviderConfig#getAvailableStyles(String)
     */
    @GET
    @Path("{id}/style/available")
    public Response getAvailableStyles(final @PathParam("id") String id) throws Exception {
        return ok(new StyleListBean(StyleProviderConfig.getAvailableStyles(id)));
    }

    /**
     * @see StyleProviderConfig#getAvailableStyles()
     */
    @GET
    @Path("all/style/available")
    public Response getAvailableStyles() throws Exception {
        return ok(new StyleListBean(StyleProviderConfig.getAvailableStyles()));
    }

    /**
     * @see StyleProviderConfig#setStyle(String, String, MutableStyle)
     */
    @POST
    @Path("{id}/style/{styleId}")
    public Response updateStyle(final @PathParam("id") String id, final @PathParam("styleId") String styleId, final MutableStyle style) throws Exception {
        StyleProviderConfig.setStyle(id, styleId, style);
        return ok(AcknowlegementType.success("Style \"" + styleId + "\" from provider \"" + id + "\" successfully updated."));
    }

    /**
     * @see StyleProviderConfig#deleteStyle(String, String)
     */
    @DELETE
    @Path("{id}/style/{styleId}")
    public Response deleteStyle(final @PathParam("id") String id, final @PathParam("styleId") String styleId) throws Exception {
        StyleProviderConfig.deleteStyle(id, styleId);
        return ok(AcknowlegementType.success("Style \"" + styleId + "\" successfully removed from provider \"" + id + "\"."));
    }
}
