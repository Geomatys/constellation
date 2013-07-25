/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2012, Geomatys
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

package org.constellation.gui;

import com.fasterxml.jackson.databind.ObjectMapper;
import juzu.Action;
import juzu.Path;
import juzu.RequestScoped;
import juzu.Resource;
import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.impl.request.Request;
import juzu.plugin.ajax.Ajax;
import juzu.template.Template;
import org.constellation.gui.binding.Style;
import org.constellation.gui.service.StyleManager;
import org.geotoolkit.filter.DefaultFilterFactory2;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * StyledLayerDescriptor controller to manage style edition.
 *
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
@RequestScoped
public final class StyleEdition {

    @Inject
    private StyleManager api;

    @Inject
    @Path("style/style.gtmpl")
    Template style;

    @Inject
    @Path("style/rule_manual.gtmpl")
    Template rule_manual;

    @Inject
    @Path("style/rule_ai.gtmpl")
    Template rule_ai;

    @Inject
    @Path("style/rule_auv.gtmpl")
    Template rule_auv;

    @Inject
    @Path("style/symbol_point.gtmpl")
    Template symbol_point;

    @Inject
    @Path("style/symbol_line.gtmpl")
    Template symbol_line;

    @Inject
    @Path("style/symbol_polygon.gtmpl")
    Template symbol_polygon;

    @Inject
    @Path("style/symbol_text.gtmpl")
    Template symbol_text;

    @Inject
    @Path("style/symbol_raster.gtmpl")
    Template symbol_raster;

    @View
    @Route("edit/style")
    public Response style(final String providerId, final String styleName, final String load) throws IOException {
        // Prepare input parameters.
        final Map<String, Object> parameters = new HashMap<String, Object>(0);
        parameters.put("providerId", providerId);
        parameters.put("styleName",  styleName);

        // Put the complete style JSON as input parameter if needed.
        if (Boolean.parseBoolean(load)) {
            parameters.put("style", api.getStyleJSON(providerId, styleName));
        } else {
            parameters.put("style", null);
        }

        // Go to view.
        return style.ok(parameters).withMimeType("text/html");
    }

    @View
    @Route("edit/rule")
    public Response rule(final String providerId, final String styleName, final String method, final String ruleIndex) {
        // Prepare input parameters.
        final Map<String, Object> parameters = new HashMap<String, Object>(0);
        parameters.put("providerId", providerId);
        parameters.put("styleName",  styleName);

        // Go to view according the specified edition method.
        if ("m".equals(method)) {
            parameters.put("ruleIndex", ruleIndex);
            return rule_manual.ok(parameters).withMimeType("text/html");
        } else if ("ai".equals(method)) {
            return rule_ai.ok(parameters).withMimeType("text/html");
        } else if ("auv".equals(method)) {
            return rule_auv.ok(parameters).withMimeType("text/html");
        }

        // Not found.
        return Response.notFound();
    }

    @View
    @Route("edit/symbol")
    public Response symbol(final String providerId, final String styleName, final String ruleIndex, final String type, final String symbolIndex) {
        // Prepare input parameters.
        final Map<String, Object> parameters = new HashMap<String, Object>(0);
        parameters.put("providerId",  providerId);
        parameters.put("styleName",   styleName);
        parameters.put("ruleIndex",   ruleIndex);
        parameters.put("symbolIndex", symbolIndex);

        // Go to view according the specified symbolizer type.
        if ("point".equals(type)) {
            return symbol_point.ok(parameters).withMimeType("text/html");
        } else if ("line".equals(type)) {
            return symbol_line.ok(parameters).withMimeType("text/html");
        } else if ("polygon".equals(type)) {
            return symbol_polygon.ok(parameters).withMimeType("text/html");
        } else if ("text".equals(type)) {
            return symbol_text.ok(parameters).withMimeType("text/html");
        } else if ("raster".equals(type)) {
            return symbol_raster.ok(parameters).withMimeType("text/html");
        }

        // Not found.
        return Response.notFound();
    }

    @Ajax
    @Resource
    @Route("edit/update")
    public Response update(final String providerId, final String styleName) {
        try {
            // Get style json body.
            final String json = Request.getCurrent().getParameters().get("style").getValue();

            // Update the style.
            api.updateStyleJSON(providerId, styleName, json);

            // Prepare input parameters.
            final Map<String, Object> parameters = new HashMap<String, Object>(0);
            parameters.put("providerId", providerId);
            parameters.put("styleName",  styleName);

            // Return status
            return Response.status(200);
        } catch (Exception ex) {
            return Response.error(ex);
        }
    }
}
