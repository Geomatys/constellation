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
import org.constellation.dto.ParameterValues;
import org.constellation.dto.StyleListBrief;
import org.constellation.json.binding.Style;
import org.constellation.json.util.StyleUtilities;
import org.constellation.map.configuration.StyleProviderConfig;
import org.geotoolkit.sld.xml.StyleXmlIO;
import org.geotoolkit.style.DefaultMutableStyle;
import org.geotoolkit.style.MutableStyle;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.Locale;

import javax.xml.namespace.QName;

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
    public Response createStyle(final @PathParam("id") String id, final DefaultMutableStyle style) throws Exception {
        StyleProviderConfig.createStyle(id, style);
        return ok(AcknowlegementType.success("Style named \"" + style.getName() + "\" successfully added to provider with id \"" + id + "\"."));
    }

    /**
     * @see StyleProviderConfig#createStyle(String, MutableStyle)
     */
    @PUT
    @Path("{id}/style/create")
    public Response createStyleJson(final @PathParam("id") String id, final Style style) throws Exception {
        StyleProviderConfig.createStyle(id, style.toType());
        return ok(AcknowlegementType.success("Style named \"" + style.getName() + "\" successfully added to provider with id \"" + id + "\"."));
    }

    /**
     * @see StyleProviderConfig#getStyle(String, String)
     */
    @GET
    @Path("{id}/style/{styleId}")
    public Response getStyle(final @PathParam("id") String id, final @PathParam("styleId") String styleId) throws Exception {
    	return ok(new Style(StyleProviderConfig.getStyle(id, styleId)));
        //return ok(StyleProviderConfig.getStyle(id, styleId));
        //return ok(new StyleXmlIO().getTransformerXMLv110().);
    }

    /**
     * @see StyleProviderConfig#getAvailableStyles(String)
     */
    @GET
    @Path("{id}/style/available")
    public Response getAvailableStyles(final @PathParam("id") String id) throws Exception {
        return ok(new StyleListBrief(StyleProviderConfig.getAvailableStyles(id)));
    }

    /**
     * @see StyleProviderConfig#getAvailableStyles(String)
     */
    @GET
    @Path("all/style/available")
    public Response getAvailableStyles() throws Exception {
        return ok(new StyleListBrief(StyleProviderConfig.getAvailableStyles("ALL")));
    }

    /**
     * @see StyleProviderConfig#getAvailableStyles(String)
     */
    @GET
    @Path("all/style/available/{category}")
    public Response getCategoryAvailableStyles(@PathParam("category") String category) throws Exception {
        return ok(new StyleListBrief(StyleProviderConfig.getAvailableStyles(category)));
    }

    /**
     * @see StyleProviderConfig#setStyle(String, String, MutableStyle)
     */
    @POST
    @Path("{id}/style/{styleId}")
    public Response updateStyle(final @PathParam("id") String id, final @PathParam("styleId") String styleId, final MutableStyle style) throws Exception {
        StyleProviderConfig.setStyle(id, styleId, style);
        return ok(AcknowlegementType.success("Style named \"" + styleId + "\" successfully updated."));
    }

    /**
     * @see StyleProviderConfig#createStyle(String, MutableStyle)
     */
    @PUT
    @Path("{id}/style/{styleId}/update")
    public Response updateStyleJson(final @PathParam("id") String id, final @PathParam("styleId") String styleId, final Style style) throws Exception {
        StyleProviderConfig.setStyle(id, styleId, style.toType());
        return ok(AcknowlegementType.success("Style named \"" + style.getName() + "\" successfully updated to provider with id \"" + id + "\"."));
    }

    /**
     * @see StyleProviderConfig#deleteStyle(String, String)
     */
    @DELETE
    @Path("{id}/style/{styleId}")
    public Response deleteStyle(final @PathParam("id") String id, final @PathParam("styleId") String styleId) throws Exception {
        StyleProviderConfig.deleteStyle(id, styleId);
        return ok(AcknowlegementType.success("Style named \"" + styleId + "\" successfully removed from provider with id \"" + id + "\"."));
    }

    /**
     * @see StyleProviderConfig#getStyleReport(String, String,Locale)
     */
    @GET
    @Path("{id}/style/{styleId}/report")
    public Response getStyleReport(final @Context HttpServletRequest request, final @PathParam("id") String id, final @PathParam("styleId") String styleId) throws Exception {
        return ok(StyleProviderConfig.getStyleReport(id, styleId, request.getLocale()));
    }

    /**
     * @see StyleProviderConfig#linkToData(String, String, String, String)
     */
    @POST
    @Path("{id}/style/{styleId}/linkData")
    public Response linkToData(final @PathParam("id") String id, final @PathParam("styleId") String styleId, final ParameterValues values) throws Exception {
        StyleProviderConfig.linkToData(id, styleId, values.get("dataProvider"), new QName(values.get("dataNamespace"), values.get("dataId")));
        return ok(AcknowlegementType.success("Style named \"" + styleId + "\" successfully linked to data named \"" + values.get("dataId") + "\"."));
    }

    /**
     * @see StyleProviderConfig#unlinkFromData(String, String, String, String)
     */
    @POST
    @Path("{id}/style/{styleId}/unlinkData")
    public Response unlinkFromData(final @PathParam("id") String id, final @PathParam("styleId") String styleId, final ParameterValues values) throws Exception {
        StyleProviderConfig.unlinkFromData(id, styleId, values.get("dataProvider"), new QName(values.get("dataNamespace"), values.get("dataId")));
        return ok(AcknowlegementType.success("Style named \"" + styleId + "\" successfully unlinked from data named \"" + values.get("dataId") + "\"."));
    }
}
