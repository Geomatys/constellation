/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2014, Geomatys
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
package org.constellation.map.featureinfo;

import org.constellation.ws.MimeType;
import org.geotoolkit.display.PortrayalException;
import org.geotoolkit.display2d.canvas.RenderingContext2D;
import org.geotoolkit.display2d.primitive.ProjectedFeature;
import org.geotoolkit.display2d.primitive.SearchAreaJ2D;
import org.geotoolkit.display2d.service.CanvasDef;
import org.geotoolkit.display2d.service.SceneDef;
import org.geotoolkit.display2d.service.ViewDef;

import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.ows.xml.GetFeatureInfo;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.type.PropertyDescriptor;

import java.awt.Rectangle;
import java.util.*;

/**
 * A generic FeatureInfoFormat that produce HTML output for Features and Coverages.
 * Supported mimeTypes are :
 * <ul>
 *     <li>text/html</li>
 * </ul>
 *
 * @author Quentin Boileau (Geomatys)
 */
public class HTMLFeatureInfoFormat extends AbstractTextFeatureInfoFormat {

    /**
     * Contains all features that cover the point requested, for feature layers.
     */
    private final Map<String, List<Feature>> features = new HashMap<String, List<Feature>>();

    public HTMLFeatureInfoFormat() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getSupportedMimeTypes() {
        return Collections.singletonList(MimeType.TEXT_HTML);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void nextProjectedFeature(ProjectedFeature graphic, RenderingContext2D context, SearchAreaJ2D queryArea) {
        final FeatureMapLayer layer = graphic.getLayer();
        final Feature feature = graphic.getCandidate();
        final String layerName = layer.getName();
        List<Feature> feat = features.get(layerName);
        if (feat == null) {
            feat = new ArrayList<Feature>();
            features.put(layerName, feat);
        }
        feat.add(feature);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getFeatureInfo(final SceneDef sdef, final ViewDef vdef, final CanvasDef cdef, final Rectangle searchArea,
                                 final GetFeatureInfo getFI) throws PortrayalException {

        //fill coverages and features maps
        getCandidates(sdef, vdef, cdef, searchArea, -1);

        final StringBuilder response = new StringBuilder();
        response.append("<!DOCTYPE html>\n")
                .append("<html>\n")
                .append("    <head>\n")
                .append("        <title>GetFeatureInfo HTML output</title>\n")
                .append("    </head>\n")
                .append("    <style>\n")
                .append("       table { border-collapse:collapse; }\n")
                .append("       .trLayerName th { text-decoration:underline; }\n")
                .append("       .tableCoverageGFI, th, td { border: 1px solid black; }\n")
                .append("    </style>\n")
                .append("    <body>\n");

        //TODO support layer alias
        for (String layer : coverages.keySet()) {
            response.append("    <table class=\"tableCoverageGFI\">\n")
                    .append("       <tr class=\"trLayerName\">\n")
                    .append("           <th>").append(layer).append("</th>\n")
                    .append("       </tr>\n");
            final List<String> record = coverages.get(layer);

            if (record.isEmpty()) {
                response.append("       <tr>\n")
                        .append("           <td>")
                        .append("               No data for this point.")
                        .append("           </td>\n")
                        .append("       </tr>\n");
            } else {
                for (String value : record) {
                    response.append("       <tr class=\"trValue\">\n")
                            .append("           <td>Value: </td>")
                            .append("           <td>")
                            .append(value)
                            .append("           </td>\n")
                            .append("       </tr>\n");
                }
            }
            response.append("    </table>\n")
                    .append("<br/>\n");
        }
        for (String featureId : features.keySet()) {
            response.append("    <table class=\"tableFeatureGFI\">\n")
                    .append("       <tr class=\"trLayerName\">\n")
                    .append("           <th>").append(featureId).append("</th>\n")
                    .append("       </tr>\n");
            final List<Feature> record = features.get(featureId);

            if (record.isEmpty()) {
                response.append("       <tr>\n")
                        .append("           <td>")
                        .append("               No feature covers the requested point.")
                        .append("           </td>\n")
                        .append("       </tr>\n");
            } else {

                final Feature firstFeature = record.get(0);
                response.append("       <tr class=\"trColumnName\">\n");
                for (final Iterator it = firstFeature.getType().getDescriptors().iterator(); it.hasNext();) {
                    response.append("           <th>")
                            .append(((PropertyDescriptor)it.next()).getName().getLocalPart())
                            .append("           </th>\n");
                }
                response.append("       </tr>\n");
                for (Feature feature : record) {
                    response.append("       <tr class=\"trValue\">\n");
                    for (final Iterator it = feature.getProperties().iterator(); it.hasNext();) {
                        response.append("           <td>");
                        response.append(((Property)it.next()).getValue());
                        response.append("           </td>\n");
                    }
                    response.append("       </tr>\n");
                }
            }
            response.append("    </table>\n")
                    .append("<br/>\n");
        }
        response.append("    </body>\n")
                .append("</html>");

        coverages.clear();
        return response.toString();
    }
}
