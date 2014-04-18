/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
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

package org.constellation.sos.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import javax.imageio.spi.ServiceRegistry;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.constellation.ServiceDef.Specification;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.DataSourceType;
import org.constellation.configuration.SOSConfiguration;
import org.constellation.dto.Service;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.metadata.utils.Utils;
import org.constellation.ogc.configuration.OGCConfigurer;
import org.constellation.sos.factory.OMFactory;
import org.constellation.sos.factory.SMLFactory;
import org.constellation.sos.io.ObservationReader;
import org.constellation.sos.io.ObservationWriter;
import org.constellation.sos.io.SensorReader;
import org.constellation.sos.io.SensorWriter;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.factory.FactoryNotFoundException;
import org.geotoolkit.observation.xml.AbstractObservation;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.geotoolkit.sml.xml.SensorMLMarshallerPool;
import org.geotoolkit.sos.xml.SOSMarshallerPool;
import org.geotoolkit.util.FileUtilities;
import org.opengis.observation.Observation;
import org.opengis.observation.ObservationCollection;

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

    /**
     * Create a new {@link SOSConfigurer} instance.
     */
    public SOSConfigurer() {
        super(Specification.SOS, SOSConfiguration.class, "config.xml");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createInstance(final String identifier, final Service metadata, Object configuration) throws ConfigurationException {
        if (configuration == null) {
            final SOSConfiguration baseConfig = new SOSConfiguration(new Automatic(null, new BDD()), new Automatic(null, new BDD()));
            baseConfig.setObservationReaderType(DataSourceType.FILESYSTEM);
            baseConfig.setObservationFilterType(DataSourceType.LUCENE);
            baseConfig.setObservationWriterType(DataSourceType.FILESYSTEM);
            baseConfig.setSMLType(DataSourceType.FILESYSTEM);
            configuration = baseConfig;
        }
        super.createInstance(identifier, metadata, configuration);
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
                    final AbstractSensorML sensor = unmarshallSensor(importedFile);
                    final String sensorID = Utils.findIdentifier(sensor);
                    writer.writeSensor(sensorID, sensor);
                } else {
                    throw new ConfigurationException("An imported file is null");
                }
            }
            return new AcknowlegementType("Success", "The specified sensor have been imported in the SOS");
        } catch (JAXBException ex) {
            LOGGER.log(Level.WARNING, "Exception while unmarshalling imported file", ex);
        } catch (CstlServiceException ex) {
            throw new ConfigurationException(ex);
        }
        return new AcknowlegementType("Error", "An error occurs during the process");
    }
    
    public AcknowlegementType removeSensor(final String id, final String sensorID) throws ConfigurationException {
        final SensorWriter writer = getSensorWriter(id);
        try {
            boolean sucess = writer.deleteSensor(sensorID);
            if (sucess) {
                return new AcknowlegementType("Success", "The specified sensor have been removed in the SOS");
            } else {
                return new AcknowlegementType("Error", "Unable to remove the sensor.");
            }
        } catch (CstlServiceException ex) {
            throw new ConfigurationException(ex);
        }
    }
    
    public Object getSensor(final String id, final String sensorID) throws ConfigurationException {
        final SensorReader reader = getReader(id);
        try {
            return reader.getSensor(sensorID);
        } catch (CstlServiceException ex) {
            throw new ConfigurationException(ex);
        }
    }
    
    public int getSensorCount(final String id) throws ConfigurationException {
        final SensorReader reader = getReader(id);
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
        } catch (CstlServiceException ex) {
            throw new ConfigurationException(ex);
        }
    }
    
    private static AbstractSensorML unmarshallSensor(final File f) throws JAXBException, CstlServiceException {
        final Unmarshaller um = SensorMLMarshallerPool.getInstance().acquireUnmarshaller();
        Object obj = um.unmarshal(f);
        if (obj instanceof JAXBElement) {
            obj = ((JAXBElement)obj).getValue();
        }
        if (obj instanceof AbstractSensorML) {
            return (AbstractSensorML)obj;
        }
        throw new CstlServiceException("the sensorML file does not contain a valid sensorML object");
    }
    
    private static Object unmarshallObservationFile(final File f) throws JAXBException, CstlServiceException {
        final Unmarshaller um = SOSMarshallerPool.getInstance().acquireUnmarshaller();
        Object obj = um.unmarshal(f);
        if (obj instanceof JAXBElement) {
            obj = ((JAXBElement)obj).getValue();
        }
        if (obj != null) {
            return obj;
        }
        throw new CstlServiceException("the observation file does not contain a valid O&M object");
    }
    
    public AcknowlegementType importObservations(final String id, final File observationFile) throws ConfigurationException {
        final ObservationWriter writer = getObservationWriter(id);
        try {
            final Object objectFile = unmarshallObservationFile(observationFile);
            if (objectFile instanceof AbstractObservation) {
                writer.writeObservation((AbstractObservation)objectFile);
            } else if (objectFile instanceof ObservationCollection) {
                importObservations(id, (ObservationCollection)objectFile);
            } else {
                return new AcknowlegementType("Failure", "Unexpected object type for observation file");
            }
            return new AcknowlegementType("Success", "The specified observation have been imported in the SOS");
        } catch (JAXBException | CstlServiceException ex) {
            throw new ConfigurationException(ex);
        }
    }
    
    public AcknowlegementType importObservations(final String id, final ObservationCollection collection) throws ConfigurationException {
        final ObservationWriter writer = getObservationWriter(id);
        try {
            for (Observation observation : collection.getMember()) {
                writer.writeObservation((AbstractObservation)observation);
            }
            return new AcknowlegementType("Success", "The specified observations have been imported in the SOS");
        } catch (CstlServiceException ex) {
            throw new ConfigurationException(ex);
        }
        //return new AcknowlegementType("Error", "An error occurs during the process");
    }
    
    public Object removeSingleObservation(final String id, final String observationID) throws ConfigurationException {
        final ObservationWriter writer = getObservationWriter(id);
        try {
            writer.removeObservation(observationID);
            return new AcknowlegementType("Success", "The specified observation have been removed from the SOS");
        } catch (CstlServiceException ex) {
            throw new ConfigurationException(ex);
        }
    }
    
    public Object removeObservationForProcedure(final String id, final String procedureID) throws ConfigurationException {
        final ObservationWriter writer = getObservationWriter(id);
        try {
            writer.removeObservationForProcedure(procedureID);
            return new AcknowlegementType("Success", "The specified observations have been removed from the SOS");
        } catch (CstlServiceException ex) {
            throw new ConfigurationException(ex);
        }
    }
    
    public Collection<String> getObservedPropertiesIds(String id) throws ConfigurationException {
        final ObservationReader reader = getObservationReader(id);
        try {
            return reader.getPhenomenonNames();
        } catch (CstlServiceException ex) {
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
    protected SensorReader getReader(final String serviceID) throws ConfigurationException {

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
            final SOSConfiguration config = (SOSConfiguration) ConfigurationEngine.getConfiguration("SOS", id);
            return config;

        } catch (JAXBException ex) {
            throw new ConfigurationException("JAXBexception while getting the SOS configuration for:" + id, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw new ConfigurationException("IllegalArgumentException: " + ex.getMessage());
        } catch (FileNotFoundException ex) {
            throw new ConfigurationException("Unable to find the configuration file");
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

            } catch (CstlServiceException ex) {
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

            } catch (CstlServiceException ex) {
                throw new ConfigurationException("JAXBException while initializing the writer!", ex);
            }
        } else {
            throw new ConfigurationException("there is no configuration file correspounding to this ID:" + serviceID);
        }
    }
}
