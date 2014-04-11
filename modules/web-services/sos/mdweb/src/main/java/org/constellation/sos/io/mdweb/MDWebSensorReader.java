/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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

package org.constellation.sos.io.mdweb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.TransformerException;
import org.apache.sis.internal.jaxb.LegacyNamespaces;

// constellation dependencies
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.metadata.io.MDWebMetadataReader;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.metadata.io.MetadataType;
import org.constellation.sos.io.SensorReader;
import org.constellation.ws.CstlServiceException;

import static org.constellation.sos.ws.SOSConstants.*;

// MDWeb dependencies
import org.mdweb.io.MD_IOException;
import org.mdweb.io.sql.AbstractReader;
import org.mdweb.model.storage.RecordSet;

// GEOTK dependencies
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.geotoolkit.util.StringUtilities;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import org.geotoolkit.sos.xml.SOSMarshallerPool;
import org.w3c.dom.Node;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class MDWebSensorReader extends MDWebMetadataReader implements SensorReader {

    private final Map<String, List<String>> acceptedSensorMLFormats = new HashMap<>();
    
    /**
     * Build a new Sensor reader for a MDweb database.
     * 
     * @param configuration
     * @param properties
     * @throws org.constellation.metadata.io.MetadataIoException
     */
    public MDWebSensorReader(final Automatic configuration, final Map<String, Object> properties) throws MetadataIoException  {
        super(configuration);
        if (configuration == null) {
            throw new MetadataIoException("The configuration object is null", NO_APPLICABLE_CODE);
        }
        // we get the database informations
        final BDD db = configuration.getBdd();
        if (db == null) {
            throw new MetadataIoException("The configuration file does not contains a BDD object", NO_APPLICABLE_CODE);
        }
        // pre warm up
        try {
            ((AbstractReader)mdReader).getAllClasses();
        } catch (MD_IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new MetadataIoException("the service has throw a MD_IO Exception:" + ex.getMessage(),
                                         NO_APPLICABLE_CODE);
        }
        final String smlFormats100 = configuration.getParameter("smlFormats100");
        if (smlFormats100 != null) {
            acceptedSensorMLFormats.put("1.0.0", StringUtilities.toStringList(smlFormats100));
        } else {
            acceptedSensorMLFormats.put("1.0.0", Arrays.asList(SENSORML_100_FORMAT_V100,
                                                               SENSORML_101_FORMAT_V100));
        }
        
        final String smlFormats200 = configuration.getParameter("smlFormats200");
        if (smlFormats200 != null) {
            acceptedSensorMLFormats.put("2.0.0", StringUtilities.toStringList(smlFormats200));
        } else {
            acceptedSensorMLFormats.put("2.0.0", Arrays.asList(SENSORML_100_FORMAT_V200,
                                                               SENSORML_101_FORMAT_V200));
        }
    }

    @Override
    public Map<String, List<String>> getAcceptedSensorMLFormats() {
        return acceptedSensorMLFormats;
    }
    
    /**
     * Return the specified sensor description from the specified ID.
     *
     * @param sensorId The identifier of the sensor.
     *
     * @return the specified sensor description from the specified ID.
     * 
     * @throws CstlServiceException If the sensor is not registered in the database,
     *                              if the specified record in the database is not a sensorML object,
     *                              or if an IO Exception occurs.
     */
    @Override
    public AbstractSensorML getSensor(final String sensorId) throws CstlServiceException {
        try {
            final Node node   = getMetadata(sensorId, MetadataType.SENSORML);
            final Object metadata;
            if (node != null) {
                metadata = unmarshallObject(node);
            } else {
                metadata = null;
            }

            if (metadata instanceof AbstractSensorML) {
               return (AbstractSensorML) metadata;
            
            } else if (metadata == null) {
                throw new CstlServiceException("this sensor is not registered in the database (id:" + sensorId + ")!",
                        INVALID_PARAMETER_VALUE, "procedure");
            } else {
                throw new CstlServiceException("The metadata record is not a sensor", NO_APPLICABLE_CODE);
            }

        } catch (MetadataIoException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw new CstlServiceException("the service has throw a Metadata IO Exception:" + ex.getMessage(),
                                         NO_APPLICABLE_CODE);
        }
    }

     private Object unmarshallObject(final Node n) throws MetadataIoException {
        try {
            final Unmarshaller um = SOSMarshallerPool.getInstance().acquireUnmarshaller();
            um.setProperty(LegacyNamespaces.APPLY_NAMESPACE_REPLACEMENTS, false);
            //final String xml = getStringFromNode(n);
            //Object obj = um.unmarshal(new StringReader(xml));
            Object obj = um.unmarshal(n);
            SOSMarshallerPool.getInstance().recycle(um);

            if (obj instanceof JAXBElement) {
                obj = ((JAXBElement)obj).getValue();
            }
            return obj;
        } catch (JAXBException ex) {
            throw new MetadataIoException(ex);
        }
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public String getInfos() {
        return "Constellation MDweb Sensor Reader 0.9";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getSensorNames() throws CstlServiceException {
        try {
            final RecordSet smlCat = mdReader.getRecordSet("SMLC");
            if (smlCat != null) {
                return mdReader.getAllIdentifiers(Arrays.asList(smlCat), false);
            }
            return new ArrayList<>();
        } catch (MD_IOException ex) {
            throw new CstlServiceException(ex);
        }
    }

    @Override
    public int getSensorCount() throws CstlServiceException {
        return getSensorNames().size();
    }
}
