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

package org.constellation.admin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javanet.staxutils.IndentingXMLStreamWriter;

import org.constellation.configuration.ConfigDirectory;
import org.constellation.dto.Service;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.util.Util;

import org.geotoolkit.util.FileUtilities;
import org.geotoolkit.xml.parameter.ParameterValueReader;
import org.geotoolkit.xml.parameter.ParameterValueWriter;

import org.apache.sis.util.logging.Logging;
import org.apache.sis.xml.MarshallerPool;
import static org.apache.sis.util.ArgumentChecks.ensureNonNull;

import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class ConfigurationEngine {

    private static final Logger LOGGER = Logging.getLogger(ConfigurationEngine.class);
    
    
    public static ParameterValueGroup getProviderConfiguration(final String serviceName, final ParameterDescriptorGroup desc) {

        final String fileName = serviceName + ".xml";
        final File configFile = ConfigDirectory.getProviderConfigFile(fileName);

        if (configFile == null || !configFile.exists()) {
            //return an empty configuration
            return desc.createValue();
        }

        //parse the configuration
        ParameterValueGroup config = null;
        try {
            final ParameterValueReader reader = new ParameterValueReader(desc);
            reader.setInput(configFile);
            config = (ParameterValueGroup) reader.read();
        } catch (XMLStreamException | IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return config;
    }

    public static void storePoviderConfiguration(final String serviceName, final ParameterValueGroup params) {
        final String fileName = serviceName + ".xml";
        final File configFile = ConfigDirectory.getProviderConfigFile(fileName);

        if (configFile.exists()) {
            //make a backup
            configFile.delete();
        }

        //write the configuration
        try {
            final ParameterValueWriter writer = new ParameterValueWriter();
            writer.setOutput(toWriter(configFile));
            writer.write(params);
        } catch (XMLStreamException | IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    public static Object getConfiguration(final String serviceType, final String serviceID, final String fileName) throws JAXBException, FileNotFoundException {
        return getConfiguration(serviceType, serviceID, fileName, GenericDatabaseMarshallerPool.getInstance());
    }

    public static Object getConfiguration(final String serviceType, final String serviceID, final String fileName, final MarshallerPool pool) throws JAXBException, FileNotFoundException {
        final File configurationDirectory = ConfigDirectory.getInstanceDirectory(serviceType, serviceID);
        final File confFile = new File(configurationDirectory, fileName);
        if (confFile.exists()) {
            final Unmarshaller unmarshaller = pool.acquireUnmarshaller();
            final Object obj = unmarshaller.unmarshal(confFile);
            pool.recycle(unmarshaller);
            return obj;
        }
        throw new FileNotFoundException("The configuration file " + fileName + " has not been found.");
    }

    public static void storeConfiguration(final String serviceType, final String serviceID, final String fileName, final Object obj) throws JAXBException {
        storeConfiguration(serviceType, serviceID, fileName, obj,  GenericDatabaseMarshallerPool.getInstance());
    }

    public static void storeConfiguration(final String serviceType, final String serviceID, final String fileName, final Object obj, final MarshallerPool pool) throws JAXBException {
        final File configurationDirectory = ConfigDirectory.getInstanceDirectory(serviceType, serviceID);
        final File confFile = new File(configurationDirectory, fileName);
        final Marshaller marshaller = pool.acquireMarshaller();
        marshaller.marshal(obj, confFile);
        pool.recycle(marshaller);
    }

    public static void createConfiguration(final String serviceType, final String serviceID, final String fileName, final Object obj) throws JAXBException {
        final File instanceDirectory = ConfigDirectory.getInstanceDirectory(serviceType, serviceID);
        if (!instanceDirectory.isDirectory()) {
            instanceDirectory.mkdir();
        }
        storeConfiguration(serviceType, serviceID, fileName, obj,  GenericDatabaseMarshallerPool.getInstance());
    }

    public static void writeMetadata(final String identifier, final String serviceType, final Service metadata, final String language) throws IOException {
        ensureNonNull("metadata", metadata);

        final File directory = ConfigDirectory.getInstanceDirectory(serviceType, identifier);
        ensureNonNull("directory", directory);
        
        final String fileName;
        if (language == null) {
            fileName = serviceType + "Capabilities.xml";
        } else {
            fileName = serviceType + "Capabilities-" + language + ".xml";
        }
        try {
            final Marshaller m = GenericDatabaseMarshallerPool.getInstance().acquireMarshaller();
            final File metadataFile = new File(directory, fileName);
            m.marshal(metadata, metadataFile);
            GenericDatabaseMarshallerPool.getInstance().recycle(m);
        } catch (JAXBException ex) {
            throw new IOException("Metadata marshalling has failed.", ex);
        }
    }

    public static Service readMetadata(final String identifier, final String serviceType, final String language) throws IOException {
        ensureNonNull("identifier",  identifier);
        ensureNonNull("serviceType", serviceType);

        final File directory = ConfigDirectory.getInstanceDirectory(serviceType, identifier);
        ensureNonNull("directory", directory);

        final String fileName;
        if (language == null) {
            fileName = serviceType + "Capabilities.xml";
        } else {
            fileName = serviceType + "Capabilities-" + language + ".xml";
        }

        final File metadataFile = new File(directory, fileName);
        try {
            final Unmarshaller um = GenericDatabaseMarshallerPool.getInstance().acquireUnmarshaller();
            final Object metadata;
            if (metadataFile.exists() && !metadataFile.isDirectory()) {
                metadata = um.unmarshal(metadataFile);
                GenericDatabaseMarshallerPool.getInstance().recycle(um);
            } else {
                final InputStream in = Util.getResourceAsStream("org/constellation/xml/" + fileName);
                if (in != null) {
                    metadata = um.unmarshal(in);
                    GenericDatabaseMarshallerPool.getInstance().recycle(um);
                    in.close();
                } else {
                    throw new IOException("Unable to find the capabilities skeleton from resource:" + fileName);
                }
            }
            if (metadata instanceof Service) {
                final Service serv = (Service) metadata;
                // override identifier
                serv.setIdentifier(identifier);
                return serv;
            } else {
                throw new IOException("Unexpected metadata object: " + metadata.getClass());
            }
        } catch (JAXBException ex) {
            throw new IOException("Metadata unmarshalling has failed.", ex);
        }
    }

    public static List<String> getServiceConfigurationIds(final String serviceType) {
        final List<String> results = new ArrayList<>();
        final File serviceDir = ConfigDirectory.getServiceDirectory(serviceType);
        for (File instanceDir : serviceDir.listFiles()) {
            if (instanceDir.isDirectory()) {
                final String instanceID = instanceDir.getName();
                if (!instanceID.startsWith(".")) {
                   results.add(instanceID);
                }
            }
        }
        return results;
    }

    public static boolean deleteConfiguration(final String serviceType, final String identifier) {
        final File directory = ConfigDirectory.getInstanceDirectory(serviceType, identifier);
        if (directory.exists()) {
            return FileUtilities.deleteDirectory(directory);
        }
        return false;
    }

    public static boolean renameConfiguration(final String serviceType, final String identifier, final String newID) {
        final File serviceDirectory  = ConfigDirectory.getServiceDirectory(serviceType);
        final File instanceDirectory = ConfigDirectory.getInstanceDirectory(serviceType, identifier);
        final File newDirectory      = new File(serviceDirectory, newID);
        return instanceDirectory.renameTo(newDirectory);
    }

    /**
     * Create writer with indentation.
     */
    private static XMLStreamWriter toWriter(final Object output)
            throws XMLStreamException{
        final XMLOutputFactory XMLfactory = XMLOutputFactory.newInstance();
        XMLfactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);

        XMLStreamWriter writer;
        if(output instanceof File){
            try {
                writer = XMLfactory.createXMLStreamWriter(new FileOutputStream((File) output));
            } catch (FileNotFoundException ex) {
                throw new XMLStreamException(ex.getLocalizedMessage(), ex);
            }
        }else if(output instanceof OutputStream){
            writer = XMLfactory.createXMLStreamWriter((OutputStream)output);
        }else if(output instanceof Result){
            writer = XMLfactory.createXMLStreamWriter((Result)output);
        }else if(output instanceof Writer){
            writer = XMLfactory.createXMLStreamWriter((Writer)output);
        }else{
            throw new XMLStreamException("Output type is not supported : "+ output);
        }

        return new IndentingXMLStreamWriter(writer);
    }
}
