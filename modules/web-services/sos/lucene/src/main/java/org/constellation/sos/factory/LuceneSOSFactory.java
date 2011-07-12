/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2011, Geomatys
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

package org.constellation.sos.factory;

import java.util.Map;
import org.constellation.configuration.ObservationFilterType;
import org.constellation.configuration.ObservationReaderType;
import org.constellation.configuration.ObservationWriterType;
import org.constellation.generic.database.Automatic;
import org.constellation.sos.io.filesystem.FileObservationReader;
import org.constellation.sos.io.filesystem.FileObservationWriter;
import org.constellation.sos.io.lucene.LuceneObservationFilter;
import org.constellation.sos.io.ObservationFilter;
import org.constellation.sos.io.ObservationReader;
import org.constellation.sos.io.ObservationWriter;
import org.constellation.ws.CstlServiceException;

import static org.constellation.configuration.ObservationFilterType.*;

/**
  * A Lucene implementation of the SOS factory.
 * it provide reader / writer / filter for observations datasource.
 *
 * @since 0.8
 * @author Guilhem Legal (Geomatys)
 */
public class LuceneSOSFactory implements OMFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean factoryMatchType(Object type) {
        if (type instanceof ObservationFilterType && ((ObservationFilterType)type).equals(LUCENE) 
         || type instanceof ObservationReaderType && ((ObservationReaderType)type).equals(ObservationReaderType.FILESYSTEM)
         || type instanceof ObservationWriterType && ((ObservationWriterType)type).equals(ObservationWriterType.FILESYSTEM)) {
            return true;
        }
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ObservationFilter getObservationFilter(ObservationFilterType type, Automatic configuration, Map<String, Object> properties) throws CstlServiceException {
        return new LuceneObservationFilter(configuration, properties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObservationFilter cloneObservationFilter(ObservationFilter omFilter) throws CstlServiceException {
        return new LuceneObservationFilter((LuceneObservationFilter) omFilter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObservationReader getObservationReader(ObservationReaderType type, Automatic configuration, Map<String, Object> properties) throws CstlServiceException {
        return new FileObservationReader(configuration, properties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObservationWriter getObservationWriter(ObservationWriterType type, Automatic configuration, Map<String, Object> properties) throws CstlServiceException {
        return new FileObservationWriter(configuration, properties);
    }
}
