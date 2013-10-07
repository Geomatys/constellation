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
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class ConfigurationEngine {

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

}
