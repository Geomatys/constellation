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
import java.io.IOException;
import java.io.InputStream;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.dto.Service;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.util.Util;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class ConfigurationEngine {

    public static Object getConfiguration(final String serviceType, final String serviceID, final String fileName) throws JAXBException, FileNotFoundException {
        final File dir = ConfigDirectory.getInstanceDirectory(serviceType, serviceID);
        return getConfiguration(dir, fileName);
    }

    @Deprecated
    public static Object getConfiguration(final File configurationDirectory, final String fileName) throws JAXBException, FileNotFoundException {
        return getConfiguration(configurationDirectory, fileName, GenericDatabaseMarshallerPool.getInstance());
    }

    public static Object getConfiguration(final String serviceType, final String serviceID, final String fileName, final MarshallerPool pool) throws JAXBException, FileNotFoundException {
        final File dir = ConfigDirectory.getInstanceDirectory(serviceType, serviceID);
        return getConfiguration(dir, fileName, pool);
    }

    @Deprecated
    public static Object getConfiguration(final File configurationDirectory, final String fileName, final MarshallerPool pool) throws JAXBException, FileNotFoundException {
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
        final File dir = ConfigDirectory.getInstanceDirectory(serviceType, serviceID);
        storeConfiguration(dir, fileName, obj);
    }

    @Deprecated
    public static void storeConfiguration(final File configurationDirectory, final String fileName, final Object obj) throws JAXBException {
        storeConfiguration(configurationDirectory, fileName, obj, GenericDatabaseMarshallerPool.getInstance());
    }

    public static void storeConfiguration(final String serviceType, final String serviceID, final String fileName, final Object obj, final MarshallerPool pool) throws JAXBException {
        final File dir = ConfigDirectory.getInstanceDirectory(serviceType, serviceID);
        storeConfiguration(dir, fileName, obj, pool);
    }

    @Deprecated
    public static void storeConfiguration(final File configurationDirectory, final String fileName, final Object obj, final MarshallerPool pool) throws JAXBException {
        final File confFile = new File(configurationDirectory, fileName);
        final Marshaller marshaller = pool.acquireMarshaller();
        marshaller.marshal(obj, confFile);
        pool.recycle(marshaller);
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
}
