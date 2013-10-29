/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010, Geomatys
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
package org.constellation.provider;

import java.awt.Dimension;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;

import org.apache.sis.measure.MeasurementRange;
import org.apache.sis.storage.DataStoreException;
import static org.constellation.provider.AbstractLayerDetails.LOGGER;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.coverage.grid.GeneralGridGeometry;
import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.coverage.io.GridCoverageReadParam;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.display.PortrayalException;
import org.geotoolkit.image.io.metadata.SpatialMetadata;
import org.geotoolkit.map.DefaultCoverageMapLayer;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.referencing.cs.DiscreteCoordinateSystemAxis;
import org.geotoolkit.style.DefaultStyleFactory;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.StyleConstants;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;


/**
 * Regroups information about a {@linkplain org.constellation.provider.LayerDetails layer}.
 *
 * @author Johann Sorel (Geomatys)
 */
public class DefaultCoverageStoreLayerDetails extends AbstractLayerDetails implements CoverageLayerDetails {

    private static final MutableStyle DEFAULT =
            new DefaultStyleFactory().style(StyleConstants.DEFAULT_RASTER_SYMBOLIZER);

    private final CoverageReference ref;

    public DefaultCoverageStoreLayerDetails(final Name name, final CoverageReference ref){
        super(name, Collections.EMPTY_LIST);
        this.ref = ref;
    }

    @Override
    public Object getOrigin() {
        return ref;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GridCoverage2D getCoverage(final Envelope envelope, final Dimension dimension, final Double elevation,
                                      final Date time) throws DataStoreException, IOException
    {
        final GridCoverageReader reader = ref.acquireReader();

        final GridCoverageReadParam param = new GridCoverageReadParam();
        param.setEnvelope(envelope);
        try {
            return (GridCoverage2D) reader.read(0, param);
        } catch (CancellationException ex) {
            throw new IOException(ex.getMessage(),ex);
        }finally{
            reader.dispose();
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MapLayer getMapLayer(MutableStyle style, final Map<String, Object> params) throws PortrayalException {
        if(style == null){
            style = getDefaultStyle();
        }

        final MapLayer layer;

            layer = MapBuilder.createCoverageLayer(ref, style);

        // EXTRA FILTER extra parameter ////////////////////////////////////////
        if (params != null && layer instanceof DefaultCoverageMapLayer) {
            final Map<String,?> extras = (Map<String, ?>) params.get(KEY_EXTRA_PARAMETERS);
            if (extras != null) {
                Filter filter = null;
                for (String key : extras.keySet()) {
                    if (key.equalsIgnoreCase("cql_filter")) {
                        final String cqlFilter = ((List)extras.get(key)).get(0).toString();
                        if (cqlFilter != null){
                            filter = buildCQLFilter(cqlFilter, filter);
                        }
                    } else if (key.startsWith("dim_") || key.startsWith("DIM_")){
                        final String dimValue = ((List)extras.get(key)).get(0).toString();
                        final String dimName  = key.substring(4);
                        filter = buildDimFilter(dimName, dimValue, filter);
                    }
                }
                if (filter != null) {
                    final DefaultCoverageMapLayer cml = (DefaultCoverageMapLayer) layer;
                    cml.setQuery(QueryBuilder.filtered(cml.getCoverageReference().getName(), filter));
                }
            }
        }
        ////////////////////////////////////////////////////////////////////////

        return layer;
    }

    /**
     * {@inheritDoc}
     */
    private MutableStyle getDefaultStyle() {
        return DEFAULT;
    }

    /**
     * Returns an empty set.
     */
    @Override
    public SortedSet<Date> getAvailableTimes() throws DataStoreException {
        SortedSet<Date> dates = new TreeSet<>();
        final Envelope env = getEnvelope();
            
        final CoordinateReferenceSystem crs = env.getCoordinateReferenceSystem();
        final CoordinateSystem cs = crs.getCoordinateSystem();

        final int nbDim = cs.getDimension();

        for (int i = 0; i < nbDim; i++) {
            final CoordinateSystemAxis axis = cs.getAxis(i);
            final AxisDirection direction = axis.getDirection();

            //TEMPORAL AXIS
            if (direction.equals(AxisDirection.PAST) || direction.equals(AxisDirection.FUTURE)) {
                if (axis instanceof DiscreteCoordinateSystemAxis) {
                    final DiscreteCoordinateSystemAxis discretAxis =(DiscreteCoordinateSystemAxis) axis;
                    final int nbOrdinate = discretAxis.length();
                    for (int j = 0; j < nbOrdinate; j++) {
                        Object value = discretAxis.getOrdinateAt(j);
                        if(value instanceof Date){
                            dates.add((Date)value);
                        }else{                            
                            Number n = (Number) value;
                            dates.add(new Date(n.longValue()));
                        }
                    }
                } else {
                    final Double min = Double.valueOf(axis.getMinimumValue());
                    final Double max = Double.valueOf(axis.getMaximumValue());
                    final long intMin = min.longValue();
                    final long intMax = max.longValue();

                    dates.add(new Date(intMin));
                    dates.add(new Date(intMax));
                } 
            } 
        }
        return dates;
    }

    /**
     * Returns an empty set.
     */
    @Override
    public SortedSet<Number> getAvailableElevations() throws DataStoreException {
        SortedSet<Number> elevations = new TreeSet<>();
        final Envelope env = getEnvelope();
        final CoordinateReferenceSystem crs = env.getCoordinateReferenceSystem();
        final CoordinateSystem cs = crs.getCoordinateSystem();
        
        final int nbDim = cs.getDimension();
        
        for (int i = 0; i < nbDim; i++) {
            final CoordinateSystemAxis axis = cs.getAxis(i);
            final AxisDirection direction = axis.getDirection();
            
            //ELEVATION AXIS
            if (direction.equals(AxisDirection.DOWN) || direction.equals(AxisDirection.UP)) {
                if (axis instanceof DiscreteCoordinateSystemAxis) {
                    final DiscreteCoordinateSystemAxis discretAxis =(DiscreteCoordinateSystemAxis) axis;
                    final int nbOrdinate = discretAxis.length();
                    for (int j = 0; j < nbOrdinate; j++) {
                        elevations.add((Number) discretAxis.getOrdinateAt(j));
                    }
                } else {
                    elevations.add(Double.valueOf(axis.getMinimumValue()));
                    elevations.add(Double.valueOf(axis.getMaximumValue()));
                }
            }
        }
        
        return elevations;
    }

    @Override
    public MeasurementRange<?>[] getSampleValueRanges() {
        return new MeasurementRange<?>[0];
    }

    @Override
    public TYPE getType() {
        return TYPE.COVERAGE;
    }

    @Override
    public Envelope getEnvelope() throws DataStoreException {
        final GridCoverageReader reader = ref.acquireReader();

        try {
            final GeneralGridGeometry generalGridGeom = reader.getGridGeometry(0);
            if (generalGridGeom == null) {
                LOGGER.log(Level.INFO, "The layer \"{0}\" does not contain a grid geometry information.", name);
                return null;
            }

            return generalGridGeom.getEnvelope();
        } catch (CancellationException ex) {
            throw new DataStoreException(ex);
        } finally {
            reader.dispose();
        }
    }

    @Override
    public String getImageFormat() {
        return "";
    }

    @Override
    public String getRemarks() {
        return "";
    }

    @Override
    public String getThematic() {
        return "";
    }

    @Override
    public SpatialMetadata getSpatialMetadata() throws DataStoreException {
        final GridCoverageReader reader = ref.acquireReader();

        try {
            return reader.getCoverageMetadata(0);
        } catch (CancellationException ex) {
            throw new DataStoreException(ex);
        } finally {
            reader.dispose();
        }
    }

}
