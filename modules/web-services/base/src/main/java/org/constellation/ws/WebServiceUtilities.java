/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010, Geomatys
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

package org.constellation.ws;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import org.constellation.provider.configuration.ConfigDirectory;
import org.geotoolkit.util.logging.Logging;

/**
 * 
 * @author Guilhem Legal (Geomatys)
 */
public class WebServiceUtilities {

    private static final Logger LOGGER = Logging.getLogger(WebServiceUtilities.class);

    private WebServiceUtilities(){}

    @Deprecated
    public static boolean getUpdateCapabilitiesFlag() {
        final Properties p = new Properties();

        // if the flag file is present we load the properties
        final File changeFile = getChangeFile();
        if (changeFile != null && changeFile.exists()) {
            try {
                final FileInputStream in = new FileInputStream(changeFile);
                p.load(in);
                in.close();
            } catch (IOException ex) {
                LOGGER.warning("Unable to read the change.properties file");
                return false;
            }
            
        } else {
            p.put("update", "false");
        }
        return  p.getProperty("update").equals("true");
    }

    @Deprecated
    public static void storeUpdateCapabilitiesFlag() {
        final Properties p = new Properties();
        final File changeFile = getChangeFile();
        p.put("update", "false");

        // if the flag file is present we store the properties
        if (changeFile != null && changeFile.exists()) {
            try {
                final FileOutputStream out = new FileOutputStream(changeFile);
                p.store(out, "updated from WebService");
                out.close();
            } catch (IOException ex) {
                LOGGER.warning("Unable to write the change.properties file");
            }
        }
    }

    /**
     * Return a the change file named "change.properties" located in the constellation directory.
     *
     * @return The specified file.
     */
    private static File getChangeFile() {
         File path = ConfigDirectory.getConfigDirectory();
         if (path != null && path.exists() && path.isDirectory()) {
            return new File(path, "change.properties");
         }
         return null;
    }

    /**
     * Extract The mapping between namespace and prefix in a namespace parameter of a GET request.
     *
     * @param namespace a String with the pattern: xmlns(ns1=http://my_ns1.com),xmlns(ns2=http://my_ns2.com),xmlns(ns3=http://my_ns3.com)
     * @return a Map of @{<prefix, namespace>}.
     * @throws CstlServiceException if the parameter namespace is malformed.
     */
    public static Map<String,String> extractNamespace(String namespace) throws CstlServiceException {
        final Map<String, String> namespaces = new HashMap<String, String>();
        if (namespace != null) {
            final StringTokenizer tokens = new StringTokenizer(namespace, ",;");
            while (tokens.hasMoreTokens()) {
                String token = tokens.nextToken().trim();
                if (token.startsWith("xmlns(") && token.endsWith(")")) {
                    token = token.substring(6, token.length() -1);
                    if (token.indexOf('=') != -1) {
                        final String prefix = token.substring(0, token.indexOf('='));
                        final String url    = token.substring(token.indexOf('=') + 1);
                        namespaces.put(prefix, url);
                    } else {
                         throw new CstlServiceException("The namespace parameter is malformed : [" + token + "] the good pattern is xmlns(ns1=http://my_ns1.com)",
                                                  ExceptionCode.INVALID_PARAMETER_VALUE, "namespace");
                    }
                } else {
                    throw new CstlServiceException("The namespace attribute is malformed: good pattern is \"xmlns(ns1=http://namespace1),xmlns(ns2=http://namespace2)\"",
                                                       ExceptionCode.INVALID_PARAMETER_VALUE, "namespace");
                }
            }
        }
        return namespaces;
    }

}
