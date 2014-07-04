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

package org.constellation.gui;

import juzu.Action;
import juzu.Path;
import juzu.RequestScoped;
import juzu.Resource;
import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.plugin.ajax.Ajax;
import juzu.template.Template;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.configuration.StyleBrief;
import org.constellation.configuration.StyleReport;
import org.constellation.dto.BandDescription;
import org.constellation.dto.CoverageDataDescription;
import org.constellation.dto.DataDescription;
import org.constellation.dto.StyleListBrief;
import org.constellation.gui.binding.ColorMap;
import org.constellation.gui.binding.Interpolate;
import org.constellation.gui.binding.InterpolationPoint;
import org.constellation.gui.binding.Style;
import org.constellation.gui.service.ConstellationService;
import org.constellation.gui.service.ProviderManager;
import org.constellation.gui.service.StyleService;
import org.constellation.gui.util.StyleBeanComparator;
import org.geotoolkit.style.interval.DefaultIntervalPalette;
import org.geotoolkit.style.interval.IntervalPalette;

import javax.inject.Inject;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;
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
    private ConstellationService cstl;

    @Inject
    private StyleService service;

    @Inject
    @Path("style_dashboard.gtmpl")
    Template dashboard;

    @Inject
    @Path("style_edition.gtmpl")
    Template edition;

    @Inject
    @Path("style_list.gtmpl")
    Template list;

    @Inject
    @Path("style_selected.gtmpl")
    Template selected;


    /**
     * View for the style dashboard.
     *
     * @return the {@link juzu.Response} view
     */
    @View
    @Route("style/dashboard")
    public Response dashboard(String category) throws IOException {
        category = (category==null)?"all":category;
        category = (category.equalsIgnoreCase("raster"))?"coverage":category;

        final StyleListBrief listBean = service.getStyleList(category);
        final int nbResults = listBean.getStyles().size();

        // Truncate the list.
        final List<StyleBrief> styles;
        if (!listBean.getStyles().isEmpty()) {
            final int endIndex = Math.min(listBean.getStyles().size(), 10);
            styles = listBean.getStyles().subList(0, endIndex);
        } else {
            styles = new ArrayList<>(0);
        }

        final Map<String, Object> parameters = new HashMap<>(0);
        parameters.put("category",   category);
        parameters.put("styles",     styles);
        parameters.put("nbResults",  nbResults);
        parameters.put("startIndex", 0);
        parameters.put("nbPerPage",  10);
        parameters.put("selected",   null);
        return dashboard.ok(parameters).withMimeType("text/html");
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
                            final String styleName,
                            final String returnURL) {

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
            parameters.put("returnURL",       returnURL);
            parameters.put("portrayUrl",      cstl.getUrlWithEndSlash() + "api/1/portrayal/portray");
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
            return StyleController_.dashboard(null);
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
    public Response create(final String styleName, final String styleJson, final String returnURL) {
        try {
            // Read edited JSON body.
            final Style style = readJson(styleJson, Style.class);

            String stylePath = ConfigDirectory.getStyleDirectory().getPath();
            provider.createProvider("sld", "sld", stylePath, "sld", null, null);

            // Create the style.
            service.createStyle(DEFAULT_PROVIDER_ID, styleName, style);

            // Return to required Page.
            return Response.redirect(returnURL);
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

    @Ajax
    @Resource
    @Route("style/select")
    public Response selectStyle(final String name, final String providerId) throws IOException {
        // Acquire the style details.
        final StyleReport report = cstl.openClient().providers.getStyleReport(providerId, name);

        // Go to view with appropriate parameters.
        final Map<String, Object> parameters = new HashMap<>(0);
        parameters.put("selected", report);
        return selected.ok(parameters).withMimeType("text/html");
    }

    @Ajax
    @Resource
    @Route("style/filter")
    public Response styleList(final String start, final String count, final String filter, final String orderBy, final String direction, final String dataTypes) throws IOException {
        final StyleListBrief listBean = service.getStyleList(dataTypes);

        // Search style by name.
        if (!isBlank(filter)) {
            final List<StyleBrief> toRemove = new ArrayList<>();
            for (final StyleBrief bean : listBean.getStyles()) {
                if (!containsIgnoreCase(bean.getName(), filter)) {
                    toRemove.add(bean);
                }
            }
            listBean.getStyles().removeAll(toRemove);
        }
        final int nbResults = listBean.getStyles().size();

        // Sort style by criteria.
        if (!isBlank(orderBy) && !isBlank(direction)) {
            Collections.sort(listBean.getStyles(), new StyleBeanComparator(orderBy, direction));
        }

        // Truncate the list.
        final List<StyleBrief> styles;
        final int intStart = Integer.parseInt(start);
        final int intCount = Integer.parseInt(count);
        if (!listBean.getStyles().isEmpty() && intStart < listBean.getStyles().size()) {
            final int endIndex = Math.min(listBean.getStyles().size(), intStart + intCount);
            styles = listBean.getStyles().subList(intStart, endIndex);
        } else {
            styles = new ArrayList<>(0);
        }

        final Map<String, Object> parameters = new HashMap<>(0);
        parameters.put("styles",     styles);
        parameters.put("nbResults",  nbResults);
        parameters.put("startIndex", intStart);
        parameters.put("nbPerPage",  intCount);
        return list.ok(parameters).withMimeType("text/html");
    }

    @Ajax
    @Resource
    @Route("style/linkData")
    public Response linkStyleToData(final String styleProvider, final String styleName, final String dataProvider, final String dataName, final String namespace) {
        try {
            cstl.openClient().providers.linkStyleToData(styleProvider, styleName, dataProvider, dataName, namespace);
            return Response.ok();
        } catch (IOException ex) {
            return Response.error(ex.getLocalizedMessage());
        }
    }

    @Ajax
    @Resource
    @Route("style/unlinkData")
    public Response unlinkStyleFromData(final String styleProvider, final String styleName, final String dataProvider, final String dataName, String namespace) {
        try {
            namespace = (namespace!=null &&namespace.equalsIgnoreCase("null"))? "":namespace;
            cstl.openClient().providers.unlinkStyleFromData(styleProvider, styleName, dataProvider, dataName, namespace);
            return Response.ok();
        } catch (IOException ex) {
            return Response.error(ex.getLocalizedMessage());
        }
    }

    @Ajax
    @Resource
    @Route("style/delete")
    public Response deleteStyle(final String providerId, final String styleName) {
        try {
            cstl.openClient().providers.deleteStyle(providerId, styleName);
            return Response.ok();
        } catch (IOException ex) {
            return Response.error(ex.getLocalizedMessage());
        }
    }
}