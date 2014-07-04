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

package org.constellation.sos.configuration;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import javax.imageio.spi.ServiceRegistry;
import javax.xml.bind.JAXBException;
import org.apache.sis.storage.DataStoreException;
import org.constellation.configuration.*;
import org.constellation.dto.SensorMLTree;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.ogc.configuration.OGCConfigurer;
import org.constellation.sos.factory.OMFactory;
import org.constellation.sos.factory.SMLFactory;
import org.constellation.sos.io.SensorReader;
import org.constellation.sos.io.SensorWriter;
import org.constellation.sos.ws.SOSConstants;
import org.constellation.sos.ws.SOSUtils;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.factory.FactoryNotFoundException;
import org.geotoolkit.gml.xml.AbstractGeometry;
import org.geotoolkit.gml.xml.v321.TimeInstantType;
import org.geotoolkit.gml.xml.v321.TimePeriodType;
import org.geotoolkit.observation.ObservationFilterReader;
import org.geotoolkit.observation.ObservationReader;
import org.geotoolkit.observation.ObservationWriter;
import org.geotoolkit.observation.xml.AbstractObservation;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.geotoolkit.util.FileUtilities;
import org.opengis.observation.Observation;
import org.opengis.observation.ObservationCollection;
import org.opengis.observation.Phenomenon;
import org.opengis.referencing.operation.TransformException;
import org.opengis.temporal.Instant;
import org.opengis.temporal.Period;
import org.opengis.temporal.TemporalGeometricPrimitive;
import org.opengis.util.FactoryException;
import static org.geotoolkit.sml.xml.SensorMLUtilities.*;

/**
 * {@link org.constellation.configuration.ServiceConfigurer} implementation for SOS service.
 *
 * TODO: implement specific configuration methods
 *
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public class SOSConfigurer extends OGCConfigurer {


    @Override
    public Instance getInstance(final String spec, final String identifier) throws ConfigurationException {
        final Instance instance = super.getInstance(spec, identifier);
        try {
            instance.setLayersNumber(getSensorIds(identifier).size());
        } catch (ConfigurationException ex) {
            LOGGER.log(Level.WARNING, "Error while getting metadata count on CSW instance:" + identifier, ex);
        }
        return instance;
    }

    public AcknowlegementType importSensor(final String id, final File sensorFile, final String type) throws ConfigurationException {
        LOGGER.info("Importing sensor");
        
        final SensorWriter writer = getSensorWriter(id);
        final List<File> files;
        switch (type) {
            case "zip":
                try  {
                    final FileInputStream fis = new FileInputStream(sensorFile);
                    files = FileUtilities.unZipFileList(fis);
                    fis.close();
                } catch (IOException ex) {
                    throw new ConfigurationException(ex);
                }   break;

            case "xml":
                files = Arrays.asList(sensorFile);
                break;

            default:
                throw new ConfigurationException("Unexpected file extension, accepting zip or xml");
        }
        
        try {
            for (File importedFile: files) {
                if (importedFile != null) {
                    final AbstractSensorML sensor = SOSUtils.unmarshallSensor(importedFile);
                    final String sensorID = getSmlID(sensor);
                    writer.writeSensor(sensorID, sensor);
                } else {
                    throw new ConfigurationException("An imported file is null");
                }
            }
            return new AcknowlegementType("Success", "The specified sensor have been imported in the SOS");
        } catch (JAXBException ex) {
            LOGGER.log(Level.WARNING, "Exception while unmarshalling imported file", ex);
        } catch (DataStoreException | CstlServiceException ex) {
            throw new ConfigurationException(ex);
        }
        return new AcknowlegementType("Error", "An error occurs during the process");
    }
    
    public AcknowlegementType importSensor(final String id, final AbstractSensorML sensor, final String sensorID) throws ConfigurationException {
        LOGGER.info("Importing sensor");
        final SensorWriter writer = getSensorWriter(id);
        try {
            writer.writeSensor(sensorID, sensor);
            return new AcknowlegementType("Success", "The specified sensor have been imported in the SOS");
        } catch (CstlServiceException ex) {
            throw new ConfigurationException(ex);
        }
    }
    
    public AcknowlegementType removeSensor(final String id, final String sensorID) throws ConfigurationException {
        final SensorWriter smlWriter     = getSensorWriter(id);
        final SensorReader smlReader     = getSensorReader(id);
        final ObservationWriter omWriter = getObservationWriter(id);
        try {
            final SensorMLTree root = getSensorTree(id);
            final SensorMLTree tree = root.find(sensorID);
            
            // for a System sensor, we delete also his components
            final List<String> toRemove = new ArrayList<>();
            if (tree != null) {
                toRemove.addAll(tree.getAllChildrenIds());
            } else {
                // tree should no be null
                toRemove.add(sensorID);
            }
            for (String sid : toRemove) {
                smlWriter.deleteSensor(sid);
                omWriter.removeProcedure(sid);
            }
            
            // if the sensor has a System parent, we must update his component list
            if (tree != null && tree.getParent() != null) {
                final String parentID = tree.getParent().getId();
                if (!"root".equals(parentID)) {
                    final AbstractSensorML sml = smlReader.getSensor(parentID);
                    SOSUtils.removeComponent(sml, sensorID);
                    smlWriter.replaceSensor(parentID, sml);
                }
            }
            
            return new AcknowlegementType("Success", "The specified sensor have been removed in the SOS");
        } catch (CstlServiceException | DataStoreException ex) {
            throw new ConfigurationException(ex);
        }
    }
    
    public AcknowlegementType removeAllSensors(final String id) throws ConfigurationException {
        final SensorWriter smlWriter = getSensorWriter(id);
        final ObservationWriter omWriter = getObservationWriter(id);
        try {
            final Collection<String> sensorNames = getSensorIds(id);
            for (String sensorID : sensorNames) {
                boolean sucess = smlWriter.deleteSensor(sensorID);
                if (sucess) {
                    omWriter.removeProcedure(sensorID);
                } else {
                    return new AcknowlegementType("Error", "Unable to remove the sensor from SML datasource:" + sensorID);
                }
            }
            return new AcknowlegementType("Success", "The specified sensor have been removed in the SOS");
        } catch (CstlServiceException | DataStoreException ex) {
            throw new ConfigurationException(ex);
        }
    }
    
    public SensorMLTree getSensorTree(String id) throws ConfigurationException {
        final SensorReader reader = getSensorReader(id);
         try {
            final Collection<String> sensorNames = reader.getSensorNames();
            final List<SensorMLTree> values = new ArrayList<>();
            for (String sensorID : sensorNames) {
                final AbstractSensorML sml = reader.getSensor(sensorID);
                final String smlType       = getSensorMLType(sml);
                final String smlID         = getSmlID(sml);
                final SensorMLTree t       = new SensorMLTree(smlID, smlType);
                final List<SensorMLTree> children = SOSUtils.getChildren(sml);
                t.setChildren(children);
                values.add(t);
            }
            return SensorMLTree.buildTree(values);
        } catch (CstlServiceException ex) {
            throw new ConfigurationException(ex);
        }
    }
    
    public Object getSensor(final String id, final String sensorID) throws ConfigurationException {
        final SensorReader reader = getSensorReader(id);
        try {
            return reader.getSensor(sensorID);
        } catch (CstlServiceException ex) {
            throw new ConfigurationException(ex);
        }
    }
    
    public int getSensorCount(final String id) throws ConfigurationException {
        final SensorReader reader = getSensorReader(id);
        try {
            return reader.getSensorCount();
        } catch (CstlServiceException ex) {
            throw new ConfigurationException(ex);
        }
    }
    
    public Collection<String> getSensorIds(final String id) throws ConfigurationException {
        final ObservationReader reader = getObservationReader(id);
        try {
            return reader.getProcedureNames();
        } catch (DataStoreException ex) {
            throw new ConfigurationException(ex);
        }
    }
    
    public Collection<String> getSensorIdsForObservedProperty(final String id, final String observedProperty) throws ConfigurationException {
        final ObservationReader reader = getObservationReader(id);
        try {
            return reader.getProceduresForPhenomenon(observedProperty);
        } catch (DataStoreException ex) {
            throw new ConfigurationException(ex);
        }
    }
    
    public Collection<String> getObservedPropertiesForSensorId(final String id, final String sensorID) throws ConfigurationException {
        final ObservationReader reader = getObservationReader(id);
        try {
            final SensorMLTree root          = getSensorTree(id);
            final SensorMLTree current       = root.find(sensorID);
            return SOSUtils.getPhenomenonFromSensor(current, reader);
        } catch (DataStoreException ex) {
            throw new ConfigurationException(ex);
        }
    }
    
    public TemporalGeometricPrimitive getTimeForSensorId(final String id, final String sensorID) throws ConfigurationException {
        final ObservationReader reader = getObservationReader(id);
        try {
            return reader.getTimeForProcedure("2.0.0", sensorID);
        } catch (DataStoreException ex) {
            throw new ConfigurationException(ex);
        }
    }
    
    public AcknowlegementType importObservations(final String id, final File observationFile) throws ConfigurationException {
        final ObservationWriter writer = getObservationWriter(id);
        try {
            final Object objectFile = SOSUtils.unmarshallObservationFile(observationFile);
            if (objectFile instanceof AbstractObservation) {
                writer.writeObservation((AbstractObservation)objectFile);
            } else if (objectFile instanceof ObservationCollection) {
                importObservations(id, (ObservationCollection)objectFile);
            } else {
                return new AcknowlegementType("Failure", "Unexpected object type for observation file");
            }
            return new AcknowlegementType("Success", "The specified observation have been imported in the SOS");
        } catch (JAXBException | DataStoreException ex) {
            throw new ConfigurationException(ex);
        }
    }
    
    public AcknowlegementType importObservations(final String id, final ObservationCollection collection) throws ConfigurationException {
        final ObservationWriter writer = getObservationWriter(id);
        try {
            final long start = System.currentTimeMillis();
            writer.writeObservations(collection.getMember());
            LOGGER.log(Level.INFO, "observations imported in :{0} ms", (System.currentTimeMillis() - start));
            return new AcknowlegementType("Success", "The specified observations have been imported in the SOS");
        } catch (DataStoreException ex) {
            throw new ConfigurationException(ex);
        }
    }
    
    public AcknowlegementType importObservations(final String id, final List<Observation> observations, final List<Phenomenon> phenomenons) throws ConfigurationException {
        final ObservationWriter writer = getObservationWriter(id);
        try {
            final long start = System.currentTimeMillis();
            writer.writePhenomenons(phenomenons);
            writer.writeObservations(observations);
            LOGGER.log(Level.INFO, "observations imported in :{0} ms", (System.currentTimeMillis() - start));
            return new AcknowlegementType("Success", "The specified observations have been imported in the SOS");
        } catch (DataStoreException ex) {
            throw new ConfigurationException(ex);
        }
    }
    
    public AcknowlegementType removeSingleObservation(final String id, final String observationID) throws ConfigurationException {
        final ObservationWriter writer = getObservationWriter(id);
        try {
            writer.removeObservation(observationID);
            return new AcknowlegementType("Success", "The specified observation have been removed from the SOS");
        } catch (DataStoreException ex) {
            throw new ConfigurationException(ex);
        }
    }
    
    public AcknowlegementType removeObservationForProcedure(final String id, final String procedureID) throws ConfigurationException {
        final ObservationWriter writer = getObservationWriter(id);
        try {
            writer.removeObservationForProcedure(procedureID);
            return new AcknowlegementType("Success", "The specified observations have been removed from the SOS");
        } catch (DataStoreException ex) {
            throw new ConfigurationException(ex);
        }
    }
    
    public Collection<String> getObservedPropertiesIds(String id) throws ConfigurationException {
        final ObservationReader reader = getObservationReader(id);
        try {
            return reader.getPhenomenonNames();
        } catch (DataStoreException ex) {
            throw new ConfigurationException(ex);
        }
    }
    
    public AcknowlegementType updateSensorLocation(final String id, final String sensorID, final AbstractGeometry location) throws ConfigurationException {
        final ObservationWriter writer = getObservationWriter(id);
        try {
            writer.recordProcedureLocation(sensorID, location);
            return new AcknowlegementType("Success", "The sensor location have been updated in the SOS");
        } catch (DataStoreException ex) {
            throw new ConfigurationException(ex);
        }
    }
    
    public String getWKTSensorLocation(final String id, final String sensorID) throws ConfigurationException {
        final ObservationReader reader = getObservationReader(id);
        try {
            final SensorMLTree root          = getSensorTree(id);
            final SensorMLTree current       = root.find(sensorID);
            final List<Geometry> jtsGeometries = SOSUtils.getJTSGeometryFromSensor(current, reader);
            if (jtsGeometries.size() == 1) {
                final WKTWriter writer = new WKTWriter();
                return writer.write(jtsGeometries.get(0));
            } else if (!jtsGeometries.isEmpty()) {
                final Geometry[] geometries   = jtsGeometries.toArray(new Geometry[jtsGeometries.size()]);
                final GeometryCollection coll = new GeometryCollection(geometries, new GeometryFactory());
                final WKTWriter writer        = new WKTWriter();
                return writer.write(coll);
            }
            return "";
        } catch (DataStoreException | FactoryException | TransformException ex) {
            throw new ConfigurationException(ex);
        }
    }
    
    public String getObservationsCsv(final String id, final String sensorID, final List<String> observedProperties, final Date start, final Date end) throws ConfigurationException {
        final ObservationFilterReader filter = getObservationFilter(id);
        try {
            filter.initFilterGetResult(sensorID, SOSConstants.OBSERVATION_QNAME);
            if (observedProperties.isEmpty()) {
                observedProperties.addAll(getObservedPropertiesForSensorId(id, sensorID));
            }
            filter.setObservedProperties(observedProperties);
            filter.setResponseFormat("text/csv");
            
            if (start != null && end != null) {
                final Period period = new TimePeriodType(new Timestamp(start.getTime()), new Timestamp(end.getTime()));
                filter.setTimeDuring(period);
            } else if (start != null) {
                final Instant time = new TimeInstantType(new Timestamp(start.getTime()));
                filter.setTimeAfter(time);
            } else if (end != null) {
                final Instant time = new TimeInstantType(new Timestamp(end.getTime()));
                filter.setTimeBefore(time);
            }
            return filter.getResults();
            
        } catch (DataStoreException ex) {
            throw new ConfigurationException(ex);
        }
    }
    
    public String getDecimatedObservationsCsv(final String id, final String sensorID, final List<String> observedProperties, final Date start, final Date end, final int width) throws ConfigurationException {
        final ObservationFilterReader filter = getObservationFilter(id);
        try {
            filter.initFilterGetResult(sensorID, SOSConstants.OBSERVATION_QNAME);
            if (observedProperties.isEmpty()) {
                observedProperties.addAll(getObservedPropertiesForSensorId(id, sensorID));
            }
            filter.setObservedProperties(observedProperties);
            filter.setResponseFormat("text/csv");
            
            if (start != null && end != null) {
                final Period period = new TimePeriodType(new Timestamp(start.getTime()), new Timestamp(end.getTime()));
                filter.setTimeDuring(period);
            } else if (start != null) {
                final Instant time = new TimeInstantType(new Timestamp(start.getTime()));
                filter.setTimeAfter(time);
            } else if (end != null) {
                final Instant time = new TimeInstantType(new Timestamp(end.getTime()));
                filter.setTimeBefore(time);
            }
            return filter.getDecimatedResults(width);
            
        } catch (DataStoreException ex) {
            throw new ConfigurationException(ex);
        }
    }
    
    /**
     * Build a new Sensor writer for the specified service ID.
     *
     * @param serviceID the service identifier (form multiple SOS) default: ""
     *
     * @return A sensor Writer.
     * @throws ConfigurationException
     */
    protected SensorWriter getSensorWriter(final String serviceID) throws ConfigurationException {

        // we get the SOS configuration file
        final SOSConfiguration config = getServiceConfiguration(serviceID);
        if (config != null) {
            final SMLFactory smlfactory = getSMLFactory(config.getSMLType());
            try {
                return smlfactory.getSensorWriter(config.getSMLType(), config.getSMLConfiguration(), new HashMap<String, Object>());

            } catch (MetadataIoException ex) {
                throw new ConfigurationException("JAXBException while initializing the writer!", ex);
            }
        } else {
            throw new ConfigurationException("there is no configuration file correspounding to this ID:" + serviceID);
        }
    }
    
    /**
     * Build a new Sensor reader for the specified service ID.
     *
     * @param serviceID the service identifier (form multiple SOS) default: ""
     *
     * @return A sensor reader.
     * @throws ConfigurationException
     */
    protected SensorReader getSensorReader(final String serviceID) throws ConfigurationException {

        // we get the CSW configuration file
        final SOSConfiguration config = getServiceConfiguration(serviceID);
        if (config != null) {
            final SMLFactory smlfactory = getSMLFactory(config.getSMLType());
            try {
                return smlfactory.getSensorReader(config.getSMLType(), config.getSMLConfiguration(), new HashMap<String, Object>());

            } catch (MetadataIoException ex) {
                throw new ConfigurationException("MetadataIoException while initializing the reader:" + ex.getMessage(), ex);
            }
        } else {
            throw new ConfigurationException("there is no configuration file correspounding to this ID:" + serviceID);
        }
    }
    
    /**
     * Refresh the map of configuration object.
     *
     * @param id identifier of the CSW service.
     * @return
     * @throws ConfigurationException
     */
    protected SOSConfiguration getServiceConfiguration(final String id) throws ConfigurationException {
        try {
            // we get the SOS configuration file
            final SOSConfiguration config = (SOSConfiguration) serviceBusiness.getConfiguration("SOS", id);
            return config;

        } catch (ConfigurationException ex) {
            throw new ConfigurationException("ConfigurationException while getting the SOS configuration for:" + id, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw new ConfigurationException("IllegalArgumentException: " + ex.getMessage());
        }
    }

    /**
     * Select the good SML factory in the available ones in function of the dataSource type.
     *
     * @param type
     * @return
     */
    private SMLFactory getSMLFactory(DataSourceType type) {
        final Iterator<SMLFactory> ite = ServiceRegistry.lookupProviders(SMLFactory.class);
        while (ite.hasNext()) {
            SMLFactory currentFactory = ite.next();
            if (currentFactory.factoryMatchType(type)) {
                return currentFactory;
            }
        }
        throw new FactoryNotFoundException("No SML factory has been found for type:" + type);
    }
    
    /**
     * Select the good OM factory in the available ones in function of the dataSource type.
     *
     * @param type
     * @return
     */
    private OMFactory getOMFactory(DataSourceType type) {
        final Iterator<OMFactory> ite = ServiceRegistry.lookupProviders(OMFactory.class);
        while (ite.hasNext()) {
            OMFactory currentFactory = ite.next();
            if (currentFactory.factoryMatchType(type)) {
                return currentFactory;
            }
        }
        throw new FactoryNotFoundException("No OM factory has been found for type:" + type);
    }
    
    /**
     * Build a new Observation writer for the specified service ID.
     *
     * @param serviceID the service identifier (form multiple SOS) default: ""
     *
     * @return An observation Writer.
     * @throws ConfigurationException
     */
    protected ObservationWriter getObservationWriter(final String serviceID) throws ConfigurationException {

        // we get the SOS configuration file
        final SOSConfiguration config = getServiceConfiguration(serviceID);
        if (config != null) {
            final OMFactory omfactory = getOMFactory(config.getObservationWriterType());
            try {
                return omfactory.getObservationWriter(config.getObservationWriterType(), config.getOMConfiguration(), new HashMap<String, Object>());

            } catch (DataStoreException ex) {
                throw new ConfigurationException("JAXBException while initializing the writer!", ex);
            }
        } else {
            throw new ConfigurationException("there is no configuration file correspounding to this ID:" + serviceID);
        }
    }
    
    /**
     * Build a new Observation writer for the specified service ID.
     *
     * @param serviceID the service identifier (form multiple SOS) default: ""
     *
     * @return An observation Writer.
     * @throws ConfigurationException
     */
    protected ObservationReader getObservationReader(final String serviceID) throws ConfigurationException {

        // we get the SOS configuration file
        final SOSConfiguration config = getServiceConfiguration(serviceID);
        if (config != null) {
            final OMFactory omfactory = getOMFactory(config.getObservationWriterType());
            try {
                return omfactory.getObservationReader(config.getObservationWriterType(), config.getOMConfiguration(), new HashMap<String, Object>());

            } catch (DataStoreException ex) {
                throw new ConfigurationException("JAXBException while initializing the writer!", ex);
            }
        } else {
            throw new ConfigurationException("there is no configuration file correspounding to this ID:" + serviceID);
        }
    }
    
    /**
     * Build a new Observation writer for the specified service ID.
     *
     * @param serviceID the service identifier (form multiple SOS) default: ""
     *
     * @return An observation Writer.
     * @throws ConfigurationException
     */
    protected ObservationFilterReader getObservationFilter(final String serviceID) throws ConfigurationException {

        // we get the SOS configuration file
        final SOSConfiguration config = getServiceConfiguration(serviceID);
        if (config != null) {
            final OMFactory omfactory = getOMFactory(config.getObservationWriterType());
            try {
                return (ObservationFilterReader) omfactory.getObservationFilter(DataSourceType.OM2, config.getOMConfiguration(), new HashMap<String, Object>());

            } catch (DataStoreException ex) {
                throw new ConfigurationException("JAXBException while initializing the filter reader!", ex);
            }
        } else {
            throw new ConfigurationException("there is no configuration file correspounding to this ID:" + serviceID);
        }
    }
}
