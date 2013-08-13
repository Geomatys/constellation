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
import org.constellation.configuration.ConfigDirectory;
import org.constellation.dto.BandDescription;
import org.constellation.dto.CoverageDataDescription;
import org.constellation.dto.DataDescription;
import org.constellation.gui.binding.ColorMap;
import org.constellation.gui.binding.Interpolate;
import org.constellation.gui.binding.InterpolationPoint;
import org.constellation.gui.binding.Style;
import org.constellation.gui.service.ProviderManager;
import org.constellation.gui.service.StyleService;
import org.geotoolkit.style.interval.DefaultIntervalPalette;
import org.geotoolkit.style.interval.IntervalPalette;

import javax.inject.Inject;
import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.constellation.gui.util.StyleUtilities.createDefaultStyle;
import static org.constellation.gui.util.StyleUtilities.readJson;
import static org.constellation.gui.util.StyleUtilities.toHex;
import static org.constellation.gui.util.StyleUtilities.writeJson;

/**
 * StyledLayerDescriptor controller to manage edition edition.
 *
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
@RequestScoped
public final class StyleController {

    private static final String DEFAULT_PROVIDER_ID = "sld";

    @Inject
    private ProviderManager provider;

    @Inject
    private StyleService service;

    @Inject
    @Path("style_dashboard.gtmpl")
    Template dashboard;

    @Inject
    @Path("style_edition.gtmpl")
    Template edition;


    /**
     * View for the style dashboard.
     *
     * @return the {@link juzu.Response} view
     */
    @View
    @Route("style/dashboard")
    public Response dashboard() {
        return dashboard.ok().withMimeType("text/html");
    }

    /**
     * View for the style edition.
     *
     * @param layerProvider the layer provider id
     * @param layerName     the layer name
     * @param styleProvider the style provider id
     * @param styleName     the style name
     * @return the {@link juzu.Response} view
     */
    @View
    @Route("style/edition")
    public Response edition(final String layerProvider,
                            final String layerName,
                            final String styleProvider,
                            final String styleName) {
        try {
            final DataDescription dataDescription;
            if (layerProvider != null && layerName != null) {
                // Existing data, load its description.
                dataDescription = service.getLayerDataDescription(layerProvider, layerName);
            } else {
                // No data, free edition.
                dataDescription = null;
            }

            final Style styleBody;
            if (styleProvider != null && styleName != null) {
                // Existing style, load it.
                styleBody = service.getStyle(styleProvider, styleName);
            } else {
                // New style, create a default.
                styleBody = createDefaultStyle(dataDescription);
            }

            // Go to view with appropriate input parameters.
            final Map<String, Object> parameters = new HashMap<>(0);
            parameters.put("layerProvider",   layerProvider);
            parameters.put("layerName",       layerName);
            parameters.put("styleProvider",   styleProvider);
            parameters.put("styleName",       styleName);
            parameters.put("dataDescription", dataDescription);
            parameters.put("styleBody",       writeJson(styleBody));
            parameters.put("portrayUrl",      service.getConstellationUrl() + "portrayal/portray");
            return edition.ok(parameters).withMimeType("text/html");
        } catch (IOException ex) {
            return Response.error(ex);
        }
    }

    /**
     * Action for style update.
     *
     * @param styleProvider the style provider id
     * @param styleName     the style name
     * @param styleJson     the style json
     * @return a status {@link juzu.Response}
     */
    @Action
    @Route("style/update")
    public Response update(final String styleProvider, final String styleName, final String styleJson) {
        try {
            // Read edited JSON body.
            final Style style = readJson(styleJson, Style.class);

            // Update the style constellation side.
            service.updateStyle(styleProvider, styleName, style);

            // Return to dashboard.
            return StyleController_.dashboard();
        } catch (IOException ex) {
            return Response.error(ex);
        }
    }

    /**
     * Action for style creation.
     *
     * @param styleName  the style name
     * @param styleJson     the style json
     * @return a status {@link juzu.Response}
     */
    @Action
    @Route("style/create")
    public Response create(final String styleName, final String styleJson) {
        try {
            // Read edited JSON body.
            final Style style = readJson(styleJson, Style.class);

            String stylePath = ConfigDirectory.getDataDirectory().getAbsolutePath()+"/styles/";
            provider.createProvider("sld", "sld", stylePath);

            // Create the style.
            service.createStyle(DEFAULT_PROVIDER_ID, styleName, style);

            // Return to dashboard.
            return StyleController_.dashboard();
        } catch (IOException ex) {
            return Response.error(ex);
        }
    }

    @Ajax
    @Resource
    @Route("style/raster/classification")
    public Response rasterClassification(final String providerId, final String layerName, final String bandIndex, final String nbIntervals) {
        try {
            final CoverageDataDescription coverageDesc = (CoverageDataDescription) service.getLayerDataDescription(providerId, layerName);
            final int index = Integer.parseInt(bandIndex);
            final BandDescription band = coverageDesc.getBands().get(index);
            final int intervals = Integer.parseInt(nbIntervals);


            final Color[] colors = new Color[]{Color.decode("#ffff00"), Color.decode("#ff0000")};
            final IntervalPalette palette = new DefaultIntervalPalette(colors);

            final ColorMap colorMap = new ColorMap();
            final Interpolate function = new Interpolate();
            final double step = (band.getMaxValue() - band.getMinValue()) / intervals;
            for (int i = 0; i <= intervals; i++) {
                final InterpolationPoint point = new InterpolationPoint();
                point.setData(band.getMinValue() + (i * step));
                if (i == 0) {
                    point.setColor(toHex(palette.interpolate(0)));
                } else {
                    point.setColor(toHex(palette.interpolate(1 / ((double) intervals / (double) i))));
                }
                function.getPoints().add(point);
            }
            colorMap.setFunction(function);
            return Response.content(200, writeJson(colorMap).getBytes()).withMimeType("text/html");
        } catch (IOException ex) {
            return Response.error(ex);
        }
    }
}