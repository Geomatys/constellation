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
import org.geotoolkit.style.DefaultStyleFactory;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.StyleConstants;
import org.opengis.filter.Filter;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.TemporalCRS;
import org.opengis.referencing.crs.VerticalCRS;

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
import org.apache.sis.measure.NumberRange;
import org.geotoolkit.coverage.combineIterator.GridCombineIterator;
import org.geotoolkit.internal.referencing.CRSUtilities;
import org.geotoolkit.coverage.GridSampleDimension;
import org.geotoolkit.coverage.filestore.FileCoverageReference;

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

        GridCoverageReadParam param = new GridCoverageReadParam();
        if (envelope != null) {
            param.setEnvelope(envelope);
        }
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
//        final TemporalCRS temporalCRS = org.apache.sis.referencing.CRS.getTemporalComponent(crs);
        final int tempIndex = CRSUtilities.getDimensionOf(crs, TemporalCRS.class);
        if (tempIndex != -1) {
            final GridCoverageReader reader = ref.acquireReader();
            final NumberRange[] tempValues = GridCombineIterator.extractAxisRanges(reader.getGridGeometry(ref.getImageIndex()), tempIndex);
            ref.recycle(reader);
            for (NumberRange nR : tempValues) {
                dates.add(new Date(Double.valueOf(nR.getMinDouble()).longValue()));
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

        //final VerticalCRS verticalCRS = org.apache.sis.referencing.CRS.getVerticalComponent(crs, true);
        final int tempIndex = CRSUtilities.getDimensionOf(crs, VerticalCRS.class);
        if (tempIndex != -1) {
            
            final GridCoverageReader reader = ref.acquireReader();
            final NumberRange[] tempValues = GridCombineIterator.extractAxisRanges(reader.getGridGeometry(ref.getImageIndex()), tempIndex);
            ref.recycle(reader);
            
            for (NumberRange nR : tempValues) {
                elevations.add(nR.getMinDouble());
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
            final GeneralGridGeometry generalGridGeom = reader.getGridGeometry(ref.getImageIndex());
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
         if (ref instanceof FileCoverageReference) {
            FileCoverageReference fref = (FileCoverageReference) ref;
            if (fref.getSpi() != null && 
                fref.getSpi().getMIMETypes() != null &&
                fref.getSpi().getMIMETypes().length > 0) {
                return fref.getSpi().getMIMETypes()[0];
            }
        }
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
    
    @Override
    public List<GridSampleDimension> getSampleDimensions() throws DataStoreException {
        final GridCoverageReader reader = ref.acquireReader();

        try {
            return reader.getSampleDimensions(ref.getImageIndex());
        } catch (CancellationException ex) {
            throw new DataStoreException(ex);
        } finally {
            ref.recycle(reader);
        }
    }
}
