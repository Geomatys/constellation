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

import com.vividsolutions.jts.geom.Geometry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.constellation.query.wms.GetFeatureInfo;

import org.geotoolkit.display2d.canvas.AbstractGraphicVisitor;
import org.geotoolkit.display2d.canvas.RenderingContext2D;
import org.geotoolkit.display2d.primitive.ProjectedFeature;
import org.geotoolkit.display2d.primitive.SearchAreaJ2D;
import org.geotoolkit.map.FeatureMapLayer;

import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.type.Name;


/**
 * Abstract graphic visitor designed to handle text output format.
 *
 * @author Johann Sorel (Geomatys)
 * @author Cédric Briançon (Geomatys)
 *
 * @see AbstractGraphicVisitor
 */
public abstract class TextGraphicVisitor extends AbstractGraphicVisitor {
    /**
     * The GetFeatureInfo WMS request.
     */
    protected final GetFeatureInfo gfi;

    protected final Map<String, List<String>> values = new HashMap<String, List<String>>();

    /**
     * Instanciates this abstract graphic visitor with the GetFeatureInfo request specified.
     *
     * @param gfi A GetFeatureInfo request.
     */
    protected TextGraphicVisitor(final GetFeatureInfo gfi) {
        if (gfi == null) {
            throw new IllegalArgumentException("GetFeatureInfo Object can not be null");
        }
        this.gfi = gfi;
    }

    /**
     * Method that have to be called in order to get the output result of the GetFeatureInfo request.
     *
     * @return A text representing the result, depending on the output format chosen.
     */
    public abstract String getResult();

    /**
     * {@inheritDoc }
     */
    @Override
    public void visit(ProjectedFeature graphic,  RenderingContext2D context, SearchAreaJ2D queryArea) {
        final StringBuilder builder = new StringBuilder();
        final FeatureMapLayer layer = graphic.getFeatureLayer();
        final Feature feature = graphic.getFeature();

        for (final Property prop : feature.getProperties()) {
            if (prop == null) {
                continue;
            }
            final Name propName = prop.getName();
            if (propName == null) {
                continue;
            }

            if (Geometry.class.isAssignableFrom(prop.getType().getBinding())) {
                builder.append(propName.toString()).append(':').append(prop.getType().getBinding().getSimpleName()).append(';');
            } else {
                final Object value = prop.getValue();
                builder.append(propName.toString()).append(':').append(value).append(';');
            }
        }

        final String result = builder.toString();
        if (builder.length() > 0 && result.endsWith(";")) {
            final String layerName = layer.getName();
            List<String> strs = values.get(layerName);
            if (strs == null) {
                strs = new ArrayList<String>();
                values.put(layerName, strs);
            }
            strs.add(result.substring(0, result.length() - 1));
        }

    }
}
