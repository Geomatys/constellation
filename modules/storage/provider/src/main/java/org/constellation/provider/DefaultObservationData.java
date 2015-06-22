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
import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.display.PortrayalException;
import org.geotoolkit.map.MapItem;
import org.geotoolkit.style.MutableStyle;
import org.opengis.geometry.Envelope;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.opengis.util.GenericName;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class DefaultObservationData extends AbstractData implements ObservationData {

    public DefaultObservationData(final GenericName name) {
        super(name, new ArrayList<String>());
    }
    
    @Override
    public SortedSet<Date> getAvailableTimes() throws DataStoreException {
        return new TreeSet<>();
    }

    @Override
    public SortedSet<Number> getAvailableElevations() throws DataStoreException {
        return new TreeSet<>();
    }

    @Override
    public GridCoverage2D getCoverage(Envelope envelope, Dimension dimension, Double elevation, Date time) throws DataStoreException, IOException {
        return null;
    }

    @Override
    public Envelope getEnvelope() throws DataStoreException {
        return null;
    }

    @Override
    public MapItem getMapLayer(MutableStyle style, Map<String, Object> params) throws PortrayalException {
        return null;
    }

    @Override
    public MeasurementRange<?>[] getSampleValueRanges() {
        return new MeasurementRange<?>[0];
    }

    @Override
    public TYPE getType() {
        return TYPE.OBSERVATION;
    }
    
}
