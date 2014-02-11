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
import org.apache.sis.geometry.GeneralDirectPosition;
import org.apache.sis.util.ArraysExt;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.coverage.GridSampleDimension;
import org.geotoolkit.display2d.canvas.RenderingContext2D;
import org.geotoolkit.display2d.primitive.ProjectedCoverage;
import org.geotoolkit.display2d.primitive.ProjectedFeature;
import org.geotoolkit.display2d.primitive.SearchAreaJ2D;
import org.geotoolkit.map.FeatureMapLayer;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.type.Name;

import javax.measure.unit.Unit;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.logging.Level;
import javax.measure.converter.ConversionException;
import javax.measure.unit.NonSI;
import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.coverage.io.GridCoverageReadParam;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.display2d.canvas.AbstractGraphicVisitor;
import org.geotoolkit.map.CoverageMapLayer;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.crs.DefaultCompoundCRS;
import  org.apache.sis.util.logging.Logging;
import org.opengis.coverage.CannotEvaluateException;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.TemporalCRS;
import org.opengis.referencing.operation.TransformException;

/**
 * @author Quentin Boileau (Geomatys)
 */
public abstract class AbstractTextFeatureInfoFormat extends AbstractFeatureInfoFormat {

    /**
     * Contains the values for all coverage layers requested.
     */
    protected final Map<String, List<String>> coverages = new HashMap<String, List<String>>();


    protected final Map<String, List<String>> features = new HashMap<String, List<String>>();

    /**
     * {@inheritDoc}
     */
    @Override
    protected void nextProjectedCoverage(ProjectedCoverage graphic, RenderingContext2D context, SearchAreaJ2D queryArea) {
        final List<Map.Entry<GridSampleDimension,Object>> results =
                FeatureInfoUtilities.getCoverageValues(graphic, context, queryArea);

        if (results == null) {
            return;
        }
        final CoverageReference ref = graphic.getLayer().getCoverageReference();
        final String layerName = ref.getName().getLocalPart();
        List<String> strs = coverages.get(layerName);
        if (strs == null) {
            strs = new ArrayList<String>();
            coverages.put(layerName, strs);
        }

        final StringBuilder builder = new StringBuilder();
        for (final Map.Entry<GridSampleDimension,Object> entry : results) {
            final Object value = entry.getValue();
            if (value == null) {
                continue;
            }
            builder.append(value);
            final Unit unit = entry.getKey().getUnits();
            if (unit != null) {
                builder.append(" ").append(unit.toString());
            }
        }

        final String result = builder.toString();
        strs.add(result.substring(0, result.length() - 2));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void nextProjectedFeature(ProjectedFeature graphic, RenderingContext2D context, SearchAreaJ2D queryArea) {
        final StringBuilder builder = new StringBuilder();
        final FeatureMapLayer layer = graphic.getLayer();
        final Feature feature = graphic.getCandidate();

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
            List<String> strs = features.get(layerName);
            if (strs == null) {
                strs = new ArrayList<String>();
                features.put(layerName, strs);
            }
            strs.add(result.substring(0, result.length() - 1));
        }
    }
    
}
