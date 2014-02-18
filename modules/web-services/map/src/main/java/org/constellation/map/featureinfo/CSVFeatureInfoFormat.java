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

import com.vividsolutions.jts.geom.Geometry;
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
import org.opengis.feature.type.Name;

import java.awt.Rectangle;
import java.util.*;
import java.util.List;

/**
 * A generic FeatureInfoFormat that produce CSV output for Features and Coverages.
 * Supported mimeTypes are :
 * <ul>
 *     <li>text/plain</li>
 * </ul>
 *
 * @author Quentin Boileau (Geomatys)
 */
public class CSVFeatureInfoFormat extends AbstractTextFeatureInfoFormat {

    /**
     * Contains all features that cover the point requested, for feature layers.
     */
    private final Map<String, List<Feature>> features = new HashMap<String, List<Feature>>();

    public CSVFeatureInfoFormat() {
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
    public Object getFeatureInfo(SceneDef sdef, ViewDef vdef, CanvasDef cdef, Rectangle searchArea, GetFeatureInfo getFI) throws PortrayalException {

        //fill coverages and features maps
        getCandidates(sdef, vdef, cdef, searchArea, -1);

        final StringBuilder builder = new StringBuilder();

        final Map<String, List<String>> values = new HashMap<String, List<String>>();
        values.putAll(coverages);

        //features
        for (final String layerName : features.keySet()) {
            builder.append(layerName).append("\n");
            final List<Feature> feat = features.get(layerName);

            if (feat.isEmpty()) {
                builder.append("No values.").append("\n");
            } else {
                //first feature
                final Feature first = feat.get(0);
                final int size = first.getProperties().size();
                int idx = 0;
                for (Property prop : first.getProperties()) {
                    if (prop == null) {
                        continue;
                    }
                    final Name propName = prop.getName();
                    if (propName == null) {
                        continue;
                    }
                    builder.append(propName);
                    if (idx < size-1) {
                        builder.append(";");
                    }
                    idx++;
                }
                builder.append("\n");

                for (final Feature record : feat) {
                    idx = 0;
                    for (Property prop : record.getProperties()) {
                        if (prop == null) {
                            continue;
                        }

                        if (Geometry.class.isAssignableFrom(prop.getType().getBinding())) {
                            builder.append(prop.getType().getBinding().getSimpleName());
                        } else {
                            final Object value = prop.getValue();
                            builder.append(formatPropertyValue(value));
                        }
                        if (idx < size-1) {
                            builder.append(";");
                        }
                        idx++;
                    }

                    builder.append("\n");
                }
            }
        }

        //coverages
        for (final String layerName : coverages.keySet()) {
            builder.append(layerName).append("\n");
            final List<String> covs = coverages.get(layerName);

            if (covs.isEmpty()) {
                builder.append("No values.").append("\n");
            } else {
                for (final String record : covs) {
                    builder.append(record).append("\n");
                }
            }
        }

        features.clear();
        coverages.clear();

        return builder.toString();

    }

    private String formatPropertyValue(Object value) {
        String valueStr = "";
        if (value != null) {
            if (!(value instanceof Number)) {
                valueStr = "\"" +value.toString().trim()+"\"";
            } else {
                valueStr = value.toString();
            }
        }
        return valueStr;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getSupportedMimeTypes() {
        return Collections.singletonList(MimeType.TEXT_PLAIN);
    }
}
