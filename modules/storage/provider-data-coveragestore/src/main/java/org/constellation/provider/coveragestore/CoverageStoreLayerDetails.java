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
package org.constellation.provider.coveragestore;

import java.awt.Dimension;
import java.io.IOException;
import java.util.*;
import org.constellation.ServiceDef.Query;
import org.constellation.provider.AbstractLayerDetails;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.display.exception.PortrayalException;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.storage.DataStoreException;
import org.geotoolkit.style.DefaultStyleFactory;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.StyleConstants;
import org.geotoolkit.util.MeasurementRange;
import org.opengis.feature.type.Name;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.GeographicBoundingBox;


/**
 * Regroups information about a {@linkplain Layer layer}.
 *
 * @author Johann Sorel (Geomatys)
 */
class CoverageStoreLayerDetails extends AbstractLayerDetails {

    private static final MutableStyle DEFAULT = 
            new DefaultStyleFactory().style(StyleConstants.DEFAULT_RASTER_SYMBOLIZER);
    
    private final CoverageReference ref;
    
    CoverageStoreLayerDetails(Name name, CoverageReference ref){
        super(name, Collections.EMPTY_LIST);
        this.ref = ref;
    }

    @Override
    public Object getOrigin() {
        return ref;
    }
    
    @Override
    public GridCoverage2D getCoverage(Envelope envelope, Dimension dimension, Double elevation, Date time) throws DataStoreException, IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public GeographicBoundingBox getGeographicBoundingBox() throws DataStoreException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MapLayer getMapLayer(MutableStyle style, Map<String, Object> params) throws PortrayalException {
        if(style == null){
            style = getDefaultStyle();
        }
        
        final MapLayer layer = MapBuilder.createCoverageLayer(
                    ref, 
                    style, 
                    getName().getLocalPart());
        return layer;
    }

    @Override
    protected MutableStyle getDefaultStyle() {
        return DEFAULT;
    }

    @Override
    public SortedSet<Date> getAvailableTimes() throws DataStoreException {
        return new TreeSet<Date>();
    }

    @Override
    public SortedSet<Number> getAvailableElevations() throws DataStoreException {
        return new TreeSet<Number>();
    }

    @Override
    public MeasurementRange<?>[] getSampleValueRanges() {
        return new MeasurementRange<?>[0];
    }

    @Override
    public boolean isQueryable(Query query) {
        return false;
    }

    @Override
    public TYPE getType() {
        return TYPE.COVERAGE;
    }
    
}
