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
package org.constellation.provider;

import org.apache.sis.measure.MeasurementRange;
import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.storage.DataStoreException;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.coverage.grid.GeneralGridGeometry;
import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.coverage.io.GridCoverageReadParam;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.display.PortrayalException;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.image.io.metadata.SpatialMetadata;
import org.geotoolkit.map.DefaultCoverageMapLayer;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.cs.DiscreteCoordinateSystemAxis;
import org.geotoolkit.style.DefaultStyleFactory;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.StyleConstants;
import org.opengis.filter.Filter;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.TemporalCRS;
import org.opengis.referencing.crs.VerticalCRS;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

import java.awt.*;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;


/**
 * Regroups information about a {@linkplain org.constellation.provider.LayerDetails layer}.
 *
 * @author Johann Sorel (Geomatys)
 */
public class DefaultCoverageData extends AbstractData implements CoverageData {

    private static final MutableStyle DEFAULT =
            new DefaultStyleFactory().style(StyleConstants.DEFAULT_RASTER_SYMBOLIZER);

    private final CoverageReference ref;

    public DefaultCoverageData(final Name name, final CoverageReference ref){
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
            return (GridCoverage2D) reader.read(ref.getImageIndex(), param);
        } catch (CancellationException ex) {
            throw new IOException(ex.getMessage(),ex);
        }finally{
            ref.recycle(reader);
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
                        final Object extra = extras.get(key);
                        String cqlFilter = null;
                        if (extra instanceof List) {
                            cqlFilter = ((List) extra).get(0).toString();
                        } else if (extra instanceof String){
                            cqlFilter = (String)extra;
                        }
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

        final TemporalCRS temporalCRS = org.apache.sis.referencing.CRS.getTemporalComponent(crs);
        if (temporalCRS != null) {
            try {
                final CoordinateSystem cs = temporalCRS.getCoordinateSystem();
                if (cs.getDimension() != 1) {
                    throw new DataStoreException("Invalid temporal CRS : "+temporalCRS);
                }

                final CoordinateSystemAxis axis = cs.getAxis(0);

                double[] temporalArray;
                if (axis instanceof DiscreteCoordinateSystemAxis) {
                    final DiscreteCoordinateSystemAxis discretAxis = (DiscreteCoordinateSystemAxis) axis;
                    final int nbOrdinate = discretAxis.length();
                    temporalArray = new double[nbOrdinate];

                    for (int i = 0; i < nbOrdinate; i++) {
                        final Comparable c = discretAxis.getOrdinateAt(i);
                        if (c instanceof Date) {
                            dates.add((Date) c);
                        } else {
                            temporalArray[i] = ((Number)c).doubleValue();
                        }
                    }
                } else {
                    temporalArray = new double[] {
                            axis.getMinimumValue(),
                            axis.getMaximumValue()
                    };
                }

                // transformation needed.
                int coordLength = temporalArray.length;
                if (coordLength > 0) {
                    //find transform from data temporal CRS to default temporal CRS
                    final MathTransform transform = CRS.findMathTransform(temporalCRS, CommonCRS.Temporal.JAVA.crs());
                    transform.transform(temporalArray, 0, temporalArray, 0, coordLength);

                    for (int i = 0; i < coordLength; i++) {
                        dates.add(new Date(Double.valueOf(temporalArray[i]).longValue()));
                    }
                }

            } catch (FactoryException e) {
                throw new DataStoreException(e.getMessage(), e);
            } catch (TransformException e) {
                throw new DataStoreException(e.getMessage(), e);
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

        final VerticalCRS verticalCRS = org.apache.sis.referencing.CRS.getVerticalComponent(crs, true);
        if (verticalCRS != null) {

            try {
                final CoordinateSystem cs = verticalCRS.getCoordinateSystem();
                if (cs.getDimension() != 1) {
                    throw new DataStoreException("Invalid vertical CRS : "+verticalCRS);
                }

                //find transform from data vertical CRS to default vertical CRS
                final MathTransform transform = CRS.findMathTransform(verticalCRS, CommonCRS.Vertical.ELLIPSOIDAL.crs());

                final CoordinateSystemAxis axis = cs.getAxis(0);
                double[] elevationOrd;
                if (axis instanceof DiscreteCoordinateSystemAxis) {
                    final DiscreteCoordinateSystemAxis discretAxis =(DiscreteCoordinateSystemAxis) axis;
                    final int nbOrdinate = discretAxis.length();

                    elevationOrd = new double[nbOrdinate];
                    for (int i = 0; i < nbOrdinate; i++) {
                        elevationOrd[i] = ((Number)discretAxis.getOrdinateAt(i)).doubleValue();
                    }

                } else {
                    elevationOrd = new double[] {
                            axis.getMinimumValue(),
                            axis.getMaximumValue()
                    };
                }

                int coordLength = elevationOrd.length;
                if (coordLength > 0) {
                    transform.transform(elevationOrd, 0, elevationOrd, 0, coordLength);
                    for (int i = 0; i < coordLength; i++) {
                        elevations.add(elevationOrd[i]);
                    }
                }

            } catch (FactoryException e) {
                throw new DataStoreException(e.getMessage(), e);
            } catch (TransformException e) {
                throw new DataStoreException(e.getMessage(), e);
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
            if (generalGridGeom == null || generalGridGeom.getEnvelope() == null) {
                LOGGER.log(Level.INFO, "The layer \"{0}\" does not contain a grid geometry information.", name);
                return null;
            }

            return generalGridGeom.getEnvelope();
        } catch (CancellationException ex) {
            throw new DataStoreException(ex);
        } finally {
            ref.recycle(reader);
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
            ref.recycle(reader);
        }
    }

}
