/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
package org.constellation.provider.coveragesgroup;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.constellation.ServiceDef.Query;
import org.constellation.provider.AbstractLayerDetails;
import org.constellation.provider.coveragesgroup.util.ConvertersJaxbToGeotk;
import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.display.exception.PortrayalException;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.metadata.iso.extent.DefaultGeographicBoundingBox;
import org.geotoolkit.storage.DataStoreException;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.util.MeasurementRange;
import org.geotoolkit.util.logging.Logging;
import org.geotoolkit.xml.MarshallerPool;
import org.opengis.feature.type.Name;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.operation.TransformException;

/**
 *
 * @author Cédric Briançon
 */
public class CoveragesGroupLayerDetails extends AbstractLayerDetails {
    private static final Logger LOGGER = Logging.getLogger(CoveragesGroupLayerDetails.class);

    private MapContext ctxt;

    private MarshallerPool pool;
    private Unmarshaller unmarshaller;

    public CoveragesGroupLayerDetails(final Name name, final File file) {
        super(name, Collections.EMPTY_LIST);

        // Parsing ctxt : MapBuilder.createContext
        try {
            ctxt = createMapContextForFile(file);
        } catch (JAXBException e) {
            LOGGER.log(Level.INFO, "Unable to convert map context file into a valid object", e);
        }
    }

    private MapContext createMapContextForFile(final File file) throws JAXBException {
        pool = new MarshallerPool(org.geotoolkit.providers.xml.MapContext.class);
        unmarshaller = pool.acquireUnmarshaller();
        final Object result = unmarshaller.unmarshal(file);
        if (!(result instanceof org.geotoolkit.providers.xml.MapContext)) {
            throw new JAXBException("Wrong response for the unmarshalling");
        }
        final org.geotoolkit.providers.xml.MapContext mapContext = (org.geotoolkit.providers.xml.MapContext)result;
        return ConvertersJaxbToGeotk.convertsMapContext(mapContext);
    }

    @Override
    protected MutableStyle getDefaultStyle() {
        return null;
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
    public GridCoverage2D getCoverage(Envelope envelope, Dimension dimension, Double elevation, Date time) throws DataStoreException, IOException {
        return null;
    }

    @Override
    public GeographicBoundingBox getGeographicBoundingBox() throws DataStoreException {
        try {
            return new DefaultGeographicBoundingBox(ctxt.getBounds());
        } catch (TransformException e) {
            throw new DataStoreException(e);
        } catch (IOException e) {
            throw new DataStoreException(e);
        }
    }

    @Override
    public MapItem getMapLayer(MutableStyle style, Map<String, Object> params) throws PortrayalException {
        return ctxt;
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
