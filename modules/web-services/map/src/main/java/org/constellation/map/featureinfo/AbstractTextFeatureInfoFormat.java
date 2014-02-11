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
    
    /**
     * Returns the data values of the given coverage, or {@code null} if the
     * values can not be obtained.
     *
     * @return list : each entry contain a gridsampledimension and value associated.
     */
    protected static List<Map.Entry<GridSampleDimension,Object>> getCoverageValues(final ProjectedCoverage gra, final RenderingContext2D context, final SearchAreaJ2D queryArea){

        final CoverageMapLayer layer = gra.getLayer();
        Envelope objBounds = context.getCanvasObjectiveBounds();
        CoordinateReferenceSystem objCRS = objBounds.getCoordinateReferenceSystem();
        TemporalCRS temporalCRS = CRS.getTemporalCRS(objCRS);
        if (temporalCRS == null) {
            /*
             * If there is no temporal range, arbitrarily select the latest date.
             * This is necessary otherwise the call to reader.read(...) will scan
             * every records in the GridCoverages table for the layer.
             */
            Envelope timeRange = layer.getBounds();
            if (timeRange != null) {
                temporalCRS = CRS.getTemporalCRS(timeRange.getCoordinateReferenceSystem());
                if (temporalCRS != null) {
                    try {
                        timeRange = CRS.transform(timeRange, temporalCRS);
                    } catch (TransformException e) {
                        // Should never happen since temporalCRS is a component of layer CRS.
                        Logging.unexpectedException(AbstractGraphicVisitor.class, "getCoverageValues", e);
                        return null;
                    }
                    final double lastTime = timeRange.getMaximum(0);
                    double day;
                    try {
                        // Arbitrarily use a time range of 1 day, to be converted in units of the temporal CRS.
                        day = NonSI.DAY.getConverterToAny(temporalCRS.getCoordinateSystem().getAxis(0).getUnit()).convert(1);
                    } catch (ConversionException e) {
                        // Should never happen since TemporalCRS use time units. But if it happen
                        // anyway, use a time range of 1 of whatever units the temporal CRS use.
                        Logging.unexpectedException(AbstractGraphicVisitor.class, "getCoverageValues", e);
                        day = 1;
                    }
                    objCRS = new DefaultCompoundCRS(objCRS.getName().getCode() + " + time", objCRS, temporalCRS);
                    final org.apache.sis.geometry.GeneralEnvelope merged = new org.apache.sis.geometry.GeneralEnvelope(objCRS);
                    org.apache.sis.geometry.GeneralEnvelope subEnv = merged.subEnvelope(0, objBounds.getDimension());
                    subEnv.setEnvelope(objBounds);
                    merged.setRange(objBounds.getDimension(), lastTime - day, lastTime);
                    objBounds = merged;
                }
            }
        }
        double[] resolution = context.getResolution();
        resolution = ArraysExt.resize(resolution, objCRS.getCoordinateSystem().getDimension());

        final GridCoverageReadParam param = new GridCoverageReadParam();
        param.setEnvelope(objBounds);
        param.setResolution(resolution);

        final CoverageReference ref = layer.getCoverageReference();
        GridCoverageReader reader = null;
        final GridCoverage2D coverage;
        try {
            reader = ref.acquireReader();
            coverage = (GridCoverage2D) reader.read(ref.getImageIndex(),param);
        } catch (CoverageStoreException ex) {
            context.getMonitor().exceptionOccured(ex, Level.INFO);
            return null;
        } finally {
            if (reader!= null) {
                try {
                    reader.dispose();
                } catch (CoverageStoreException e) {
                    context.getMonitor().exceptionOccured(e, Level.INFO);
                }
            }
        }

        if (coverage == null) {
            //no coverage for this BBOX
            return null;
        }

        final GeneralDirectPosition dp = new GeneralDirectPosition(objCRS);
        final Rectangle2D bounds2D = queryArea.getObjectiveShape().getBounds2D();
        dp.setOrdinate(0, bounds2D.getCenterX());
        dp.setOrdinate(1, bounds2D.getCenterY());

        float[] values = null;
        
        try{
            values = coverage.evaluate(dp, values);
        }catch(CannotEvaluateException ex){
            context.getMonitor().exceptionOccured(ex, Level.INFO);
            values = new float[coverage.getSampleDimensions().length];
            Arrays.fill(values, Float.NaN);
        }

        final List<Map.Entry<GridSampleDimension,Object>> results = new ArrayList<Map.Entry<GridSampleDimension, Object>>();
        for (int i=0; i<values.length; i++){
            final GridSampleDimension sample = coverage.getSampleDimension(i);
            results.add(new AbstractMap.SimpleImmutableEntry<GridSampleDimension, Object>(sample, values[i]));
        }
        return results;
    }
    
}
