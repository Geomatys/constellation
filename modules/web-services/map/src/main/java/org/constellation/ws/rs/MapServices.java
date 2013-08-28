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

package org.constellation.ws.rs;

import org.apache.sis.util.Static;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.configuration.ConfigurationException;
import org.constellation.dto.AccessConstraint;
import org.constellation.dto.Contact;
import org.constellation.dto.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;

/**
 * Utility class for map services management/configuration.
 *
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class MapServices extends Static {

    /**
     * Service metadata JAXB context.
     */
    private static JAXBContext METADATA_CONTEXT;
    static {
        try {
            METADATA_CONTEXT = JAXBContext.newInstance(Service.class, Contact.class, AccessConstraint.class);
        } catch (JAXBException ex) {
            throw new IllegalStateException("Unable to create JAXB context for service metadata marshalling/unmarshalling.");
        }
    }

    /**
     * The service metadata file name.
     */
    private static final String METADATA_FILE_NAME = "serviceMetadata.xml";

    /**
     * Error message for invalid directories.
     */
    private static final MessageFormat INVALID_DIRECTORY = new MessageFormat("The {0} folder does not exist or is not a directory.");


    /**
     * Gets the service instance folder from its identifier.
     *
     * @param identifier  the service identifier
     * @param serviceType the service type (WMS, WFS, WPS...)
     * @return the service instance folder
     * @throws ConfigurationException if the service instance directory doesn't exists
     */
    public static File getInstanceDirectory(final String identifier, String serviceType) throws ConfigurationException {
        ensureNonNull("identifier",  identifier);
        ensureNonNull("serviceType", serviceType);

        serviceType = serviceType.toUpperCase();
        final File cstlDirectory = ConfigDirectory.getConfigDirectory();
        if (!cstlDirectory.exists() || !cstlDirectory.isDirectory()) {
            throw new ConfigurationException(INVALID_DIRECTORY.format(".constellation"));
        }
        final File wmsDirectory = new File(cstlDirectory, serviceType);
        if (!wmsDirectory.exists() || !wmsDirectory.isDirectory()) {
            throw new ConfigurationException(INVALID_DIRECTORY.format(".constellation/" + serviceType));
        }
        final File instanceDirectory = new File(wmsDirectory,  identifier);
        if (!instanceDirectory.exists() || !instanceDirectory.isDirectory()) {
            throw new ConfigurationException(INVALID_DIRECTORY.format(".constellation/" + serviceType + "/" + identifier));
        }
        return instanceDirectory;
    }

    /**
     * Writes the service metadata file into the service instance directory.
     *
     * @param directory the service instance directory
     * @param metadata  the service metadata
     * @throws IOException if failed to write the service metadata for any reason
     */
    public static void writeMetadata(final File directory, final Service metadata) throws IOException {
        ensureNonNull("directory", directory);
        ensureNonNull("metadata", metadata);

        try {
            final File metadataFile = new File(directory, METADATA_FILE_NAME);
            METADATA_CONTEXT.createMarshaller().marshal(metadata, metadataFile);
        } catch (JAXBException ex) {
            throw new IOException("Metadata marshalling has failed.", ex);
        }
    }

    /**
     * Writes the service metadata file into the service instance directory.
     *
     * @param identifier  the service identifier
     * @param serviceType the service type (WMS, WFS, WPS...)
     * @param metadata    the service metadata
     * @throws ConfigurationException if the service instance directory doesn't exists
     * @throws IOException if failed to write the service metadata for any reason
     */
    public static void writeMetadata(final String identifier, final String serviceType, final Service metadata) throws ConfigurationException, IOException {
        ensureNonNull("identifier",  identifier);
        ensureNonNull("serviceType", serviceType);
        ensureNonNull("metadata",    metadata);

        writeMetadata(getInstanceDirectory(identifier, serviceType), metadata);
    }

    /**
     * Writes the service metadata file into the service instance directory.
     *
     * @param directory the service instance directory
     * @throws IOException if failed to read the service metadata for any reason
     */
    public static Service readMetadata(final File directory) throws IOException {
        ensureNonNull("directory", directory);

        final File metadataFile = new File(directory, METADATA_FILE_NAME);
        if (metadataFile.exists() && !metadataFile.isDirectory()) {
            try {
                final Object metadata = METADATA_CONTEXT.createUnmarshaller().unmarshal(metadataFile);
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

    /**
     * Reads the service metadata file from the service instance directory.
     *
     * @param identifier  the service identifier
     * @param serviceType the service type (WMS, WFS, WPS...)
     * @return the service metadata
     * @throws ConfigurationException if the service instance directory doesn't exists
     * @throws IOException if failed to read the service metadata for any reason
     */
    public static Service readMetadata(final String identifier, final String serviceType) throws ConfigurationException, IOException {
        ensureNonNull("identifier",  identifier);
        ensureNonNull("serviceType", serviceType);

        return readMetadata(getInstanceDirectory(identifier, serviceType));
    }
}
