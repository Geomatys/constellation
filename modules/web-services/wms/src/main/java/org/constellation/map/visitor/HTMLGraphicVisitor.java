/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2010, Geomatys
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
package org.constellation.map.visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.measure.unit.Unit;

import org.constellation.provider.LayerDetails;
import org.constellation.provider.LayerDetails.TYPE;
import org.constellation.ws.MimeType;
import org.geotoolkit.coverage.GridSampleDimension;

import org.geotoolkit.display2d.canvas.RenderingContext2D;
import org.geotoolkit.display2d.primitive.ProjectedCoverage;
import org.geotoolkit.display2d.primitive.ProjectedFeature;
import org.geotoolkit.display2d.primitive.SearchAreaJ2D;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.wms.xml.GetFeatureInfo;

import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.type.PropertyDescriptor;


/**
 * Visit results of a GetFeatureInfo request, and format the output into HTML.
 *
 * @author Johann Sorel (Geomatys)
 * @author Cédric Briançon (Geomatys)
 */
public final class HTMLGraphicVisitor extends TextGraphicVisitor implements GetFeatureInfoVisitor{
    /**
     * Contains the values for all coverage layers requested.
     */
    private final Map<String, List<String>> coverages = new HashMap<String, List<String>>();

    /**
     * Contains all features that cover the point requested, for feature layers.
     */
    private final Map<String, List<Feature>> features = new HashMap<String, List<Feature>>();

    private int index = 0;

    public HTMLGraphicVisitor(final GetFeatureInfo gfi, List<LayerDetails> layerDetails) {
        super(gfi);

        for (LayerDetails layerDetail : layerDetails) {
            final String name = layerDetail.getName().getLocalPart();
            if (layerDetail.getType().equals(TYPE.COVERAGE)) {
                coverages.put(name, new ArrayList<String>());
            } else {
                features.put(name, new ArrayList<Feature>());
            }
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean isStopRequested() {
        final Integer count = gfi.getFeatureCount();
        if (count != null) {
            return (index == count);
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void visit(ProjectedFeature graphic, RenderingContext2D context, SearchAreaJ2D queryArea) {
        index++;
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
     * {@inheritDoc }
     */
    @Override
    public void visit(ProjectedCoverage coverage, RenderingContext2D context, SearchAreaJ2D queryArea) {
        index++;
        final List<Entry<GridSampleDimension,Object>> results = getCoverageValues(coverage, context, queryArea);

        if (results == null) {
            return;
        }

        final String layerName = coverage.getLayer().getCoverageName().getLocalPart();
        List<String> strs = coverages.get(layerName);
        if (strs == null) {
            strs = new ArrayList<String>();
            coverages.put(layerName, strs);
        }

        final StringBuilder builder = new StringBuilder();
        for (final Entry<GridSampleDimension,Object> entry : results) {
            final Object value = entry.getValue();
            if (value == null) {
                continue;
            }
            builder.append(value);
            final Unit unit = entry.getKey().getUnits();
            if (unit != null) {
                builder.append(" ").append(unit.toString());
            }
            //builder.append(" [").append(i).append(']').append(';');
        }

        final String result = builder.toString();
        strs.add(result.substring(0, result.length() - 2));

    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getResult() {
        final StringBuilder response = new StringBuilder();
        response.append("<html>\n")
                .append("    <head>\n")
                .append("        <title>GetFeatureInfo HTML output</title>\n")
                .append("    </head>\n")
                .append("    <style>\n")
                .append("       .trLayerName th {\n")
                .append("           text-decoration:underline;\n")
                .append("       }\n")
                .append("    </style>\n")
                .append("    <body>\n");

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
                        response.append("           <td>")
                                .append(((Property)it.next()).getValue().toString())
                                .append("           </td>\n");
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

    @Override
    public String getMimeType() {
        return MimeType.TEXT_HTML;
    }

}
