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
import org.constellation.configuration.ConfigDirectory;
import org.constellation.dto.Service;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.util.Util;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class ConfigurationEngine {

    /**
     * The service metadata file name.
     */
    private static final String METADATA_FILE_NAME = "serviceMetadata.xml";
    

    public static Object getConfiguration(final File configurationDirectory, final String fileName) throws JAXBException, FileNotFoundException {
        final File confFile = new File(configurationDirectory, fileName);
        if (confFile.exists()) {
            final Unmarshaller unmarshaller = GenericDatabaseMarshallerPool.getInstance().acquireUnmarshaller();
            final Object obj = unmarshaller.unmarshal(confFile);
            GenericDatabaseMarshallerPool.getInstance().recycle(unmarshaller);
            return obj;
        }
        throw new FileNotFoundException("The configuration file " + fileName + " has not been found.");
    }

    public static void storeConfiguration(final File configurationDirectory, final String fileName, final Object obj) throws JAXBException {
        final File confFile = new File(configurationDirectory, fileName);
        final Marshaller marshaller = GenericDatabaseMarshallerPool.getInstance().acquireMarshaller();
        marshaller.marshal(obj, confFile);
        GenericDatabaseMarshallerPool.getInstance().recycle(marshaller);
    }

    public static void writeMetadata(final File directory, final Service metadata) throws IOException {
        ensureNonNull("directory", directory);
        ensureNonNull("metadata", metadata);

        try {
            final Marshaller m = GenericDatabaseMarshallerPool.getInstance().acquireMarshaller();
            final File metadataFile = new File(directory, METADATA_FILE_NAME);
            m.marshal(metadata, metadataFile);
            GenericDatabaseMarshallerPool.getInstance().recycle(m);
        } catch (JAXBException ex) {
            throw new IOException("Metadata marshalling has failed.", ex);
        }
    }

    /**
     * Writes the service metadata file into the service instance directory.
     *
     * @param directory the service instance directory
     * @throws IOException if failed to read the service metadata for any reason
     */
    public static Service readMetadata(final String identifier, final String serviceType) throws IOException {
        ensureNonNull("identifier",  identifier);
        ensureNonNull("serviceType", serviceType);

        final File directory = ConfigDirectory.getInstanceDirectory(identifier, serviceType);
        ensureNonNull("directory", directory);

        final File metadataFile = new File(directory, METADATA_FILE_NAME);
        if (metadataFile.exists() && !metadataFile.isDirectory()) {
            try {
                final Unmarshaller um = GenericDatabaseMarshallerPool.getInstance().acquireUnmarshaller();
                final Object metadata = um.unmarshal(metadataFile);
                GenericDatabaseMarshallerPool.getInstance().recycle(um);
                if (metadata instanceof Service) {
                    return (Service) metadata;
                }
                throw new IOException("Unexpected metadata object: " + metadata.getClass());
            } catch (JAXBException ex) {
                throw new IOException("Metadata unmarshalling has failed.", ex);
            }
        }
        return null;
    }

    @Deprecated
    public static Service getOldStaticCapabilitiesObject(final File configDirectory, final String fileName) throws IOException, JAXBException {
        final Service response;
        final File f;
        if (configDirectory != null && configDirectory.exists()) {
            f = new File(configDirectory, fileName);
        } else {
            f = null;
        }
        final Unmarshaller unmarshaller = GenericDatabaseMarshallerPool.getInstance().acquireUnmarshaller();
        // If the file is not present in the configuration directory, take the one in resource.
        if (f == null || !f.exists()) {
            final InputStream in = Util.getResourceAsStream("org/constellation/xml/" + fileName);
            if (in != null) {
                response = (Service) unmarshaller.unmarshal(in);
                in.close();
            } else {
                throw new IOException("Unable to find the capabilities skeleton from resource:" + fileName);
            }
        } else {
            response = (Service) unmarshaller.unmarshal(f);
        }
        GenericDatabaseMarshallerPool.getInstance().recycle(unmarshaller);
        return response;
    }
}
