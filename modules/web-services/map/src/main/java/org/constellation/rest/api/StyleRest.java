/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.constellation.rest.api;

import static org.constellation.utils.RESTfulUtilities.ok;

import java.util.Locale;

import javax.inject.Inject;
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
import javax.xml.namespace.QName;

import org.constellation.admin.StyleBusiness;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.dto.ParameterValues;
import org.constellation.dto.StyleListBrief;
import org.constellation.json.binding.Style;
import org.geotoolkit.style.DefaultMutableStyle;
import org.geotoolkit.style.MutableStyle;

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
public final class StyleRest {
    
    @Inject
    private StyleBusiness styleBusiness;

    /**
     * @see StyleBusiness#createStyle(String, MutableStyle)
     */
    @PUT
    @Path("{id}/style")
    public Response createStyle(final @PathParam("id") String id, final DefaultMutableStyle style) throws Exception {
        styleBusiness.createStyle(id, style);
        return ok(AcknowlegementType.success("Style named \"" + style.getName() + "\" successfully added to provider with id \"" + id + "\"."));
    }

    /**
     * @see StyleBusiness#createStyle(String, MutableStyle)
     */
    @PUT
    @Path("{id}/style/create")
    public Response createStyleJson(final @PathParam("id") String id, final Style style) throws Exception {
        styleBusiness.createStyle(id, style.toType());
        return ok(AcknowlegementType.success("Style named \"" + style.getName() + "\" successfully added to provider with id \"" + id + "\"."));
    }

    /**
     * @see StyleBusiness#getStyle(String, String)
     */
    @GET
    @Path("{id}/style/{styleId}")
    public Response getStyle(final @PathParam("id") String id, final @PathParam("styleId") String styleId) throws Exception {
    	return ok(new Style(styleBusiness.getStyle(id, styleId)));
    }

    /**
     * @see StyleBusiness#getAvailableStyles(String)
     */
    @GET
    @Path("{id}/style/available")
    public Response getAvailableStyles(final @PathParam("id") String id) throws Exception {
        return ok(new StyleListBrief(styleBusiness.getAvailableStyles(id, null)));
    }

    /**
     * @see StyleBusiness#getAvailableStyles(String)
     */
    @GET
    @Path("all/style/available")
    public Response getAvailableStyles() throws Exception {
        return ok(new StyleListBrief(styleBusiness.getAvailableStyles("ALL")));
    }

    /**
     * @see StyleBusiness#getAvailableStyles(String)
     */
    @GET
    @Path("all/style/available/{category}")
    public Response getCategoryAvailableStyles(@PathParam("category") String category) throws Exception {
        return ok(new StyleListBrief(styleBusiness.getAvailableStyles(category)));
    }

    /**
     * @see StyleBusiness#setStyle(String, String, MutableStyle)
     */
    @POST
    @Path("{id}/style/{styleId}")
    public Response updateStyle(final @PathParam("id") String id, final @PathParam("styleId") String styleId, final MutableStyle style) throws Exception {
        styleBusiness.setStyle(id, styleId, style);
        return ok(AcknowlegementType.success("Style named \"" + styleId + "\" successfully updated."));
    }

    /**
     * @see StyleBusiness#createStyle(String, MutableStyle)
     */
    @PUT
    @Path("{id}/style/{styleId}/update")
    public Response updateStyleJson(final @PathParam("id") String id, final @PathParam("styleId") String styleId, final Style style) throws Exception {
        styleBusiness.setStyle(id, styleId, style.toType());
        return ok(AcknowlegementType.success("Style named \"" + style.getName() + "\" successfully updated to provider with id \"" + id + "\"."));
    }

    /**
     * @see StyleBusiness#deleteStyle(String, String)
     */
    @DELETE
    @Path("{id}/style/{styleId}")
    public Response deleteStyle(final @PathParam("id") String id, final @PathParam("styleId") String styleId) throws Exception {
        styleBusiness.deleteStyle(id, styleId);
        return ok(AcknowlegementType.success("Style named \"" + styleId + "\" successfully removed from provider with id \"" + id + "\"."));
    }

    /**
     * @see StyleBusiness#getStyleReport(String, String,Locale)
     */
    @GET
    @Path("{id}/style/{styleId}/report")
    public Response getStyleReport(final @Context HttpServletRequest request, final @PathParam("id") String id, final @PathParam("styleId") String styleId) throws Exception {
        return ok(styleBusiness.getStyleReport(id, styleId, request.getLocale()));
    }

    /**
     * @see StyleBusiness#linkToData(String, String, String, String)
     */
    @POST
    @Path("{id}/style/{styleId}/linkData")
    public Response linkToData(final @PathParam("id") String id, final @PathParam("styleId") String styleId, final ParameterValues values) throws Exception {
        styleBusiness.linkToData(id, styleId, values.get("dataProvider"), new QName(values.get("dataNamespace"), values.get("dataId")));
        return ok(AcknowlegementType.success("Style named \"" + styleId + "\" successfully linked to data named \"" + values.get("dataId") + "\"."));
    }

    /**
     * @see StyleBusiness#unlinkFromData(String, String, String, String)
     */
    @POST
    @Path("{id}/style/{styleId}/unlinkData")
    public Response unlinkFromData(final @PathParam("id") String id, final @PathParam("styleId") String styleId, final ParameterValues values) throws Exception {
        styleBusiness.unlinkFromData(id, styleId, values.get("dataProvider"), new QName(values.get("dataNamespace"), values.get("dataId")));
        return ok(AcknowlegementType.success("Style named \"" + styleId + "\" successfully unlinked from data named \"" + values.get("dataId") + "\"."));
    }
    
    @GET
    @Path("restart")
    public Response restartStyleProviders() throws Exception {
        org.constellation.provider.StyleProviders.getInstance().reload();
        return ok(new AcknowlegementType("Success", "All style providers have been restarted."));
    }
}
