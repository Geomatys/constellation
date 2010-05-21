/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2009, Geomatys
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

package org.constellation.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * Utility methods of general use.
 * <p>
 * TODO: this class needs review.
 *   * methods should be re-ordered for coherence
 *       -- String
 *       -- Reflection
 *       -- ...
 * </p>
 * 
 * @author Mehdi Sidhoum (Geomatys)
 * @author Legal Guilhem (Geomatys)
 * @author Adrian Custer (Geomatys)
 * 
 * @since 0.2
 */
public final class Util {
	
    private static final Logger LOGGER = Logger.getLogger("org.constellation.util");
    
    private Util() {}

    /**
     * Return an marshallable Object from an url
     */
    public static Object getUrlContent(final String url, final Unmarshaller unmarshaller) throws MalformedURLException, IOException {
        final URL source         = new URL(url);
        final URLConnection conec = source.openConnection();
        Object response = null;
        
        try {
        
            // we get the response document
            final InputStream in   = conec.getInputStream();
            final StringWriter out = new StringWriter();
            final byte[] buffer    = new byte[1024];
            int size;

            while ((size = in.read(buffer, 0, 1024)) > 0) {
                out.write(new String(buffer, 0, size));
            }

            //we convert the brut String value into UTF-8 encoding
            String brutString = out.toString();

            //we need to replace % character by "percent because they are reserved char for url encoding
            brutString = brutString.replaceAll("%", "percent");
            final String decodedString = java.net.URLDecoder.decode(brutString, "UTF-8");
            
            try {
                response = unmarshaller.unmarshal(new StringReader(decodedString));
                if (response instanceof JAXBElement) {
                    response = ((JAXBElement<?>) response).getValue();
                }
            } catch (JAXBException ex) {
                LOGGER.severe("The distant service does not respond correctly: unable to unmarshall response document." + '\n' +
                        "cause: " + ex.getMessage());
            }
        } catch (IOException ex) {
            LOGGER.severe("The Distant service have made an error");
            return null;
        }
        return response;
    }
    
    /**
     * 
     * @param enumeration
     * @return
     */
    public static String getElementNameFromEnum(final Object enumeration) {
        String value = "";
        try {
            final Method getValue = enumeration.getClass().getDeclaredMethod("value");
            value = (String) getValue.invoke(enumeration);
        } catch (IllegalAccessException ex) {
            LOGGER.severe("The class is not accessible");
        } catch (IllegalArgumentException ex) {
            LOGGER.severe("IllegalArguement exeption in value()");
        } catch (InvocationTargetException ex) {
            LOGGER.severe("Exception throw in the invokated getter value() " + '\n' +
                       "Cause: " + ex.getMessage());
        } catch (NoSuchMethodException ex) {
           LOGGER.severe("no such method value() in " + enumeration.getClass().getSimpleName());
        } catch (SecurityException ex) {
           LOGGER.severe("security Exception while getting the codelistElement in value() method");
        }
        return value;
    }
    
    /**
     * Obtain the Thread Context ClassLoader.
     */
    public static ClassLoader getContextClassLoader() {
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            @Override
            public ClassLoader run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
    }
    
    /**
     * Return an input stream of the specified resource. 
     */
    public static InputStream getResourceAsStream(final String url) {
        final ClassLoader cl = getContextClassLoader();
        return cl.getResourceAsStream(url);
    }

    /**
     * Execute a SQL script located into the resources.
     *
     * @param path the path to the name of the file example : org.constellation.sos.sql in Resources folder.
     * @param connection.
     */
    public static void executeSQLScript(String path, Connection connection) {
        executeSQLScript(path, connection, false);
    }

    /**
     * Execute a SQL script located into the resources.
     *
     * @param path the path to the name of the file example : org.constellation.sos.sql in Resources folder.
     * @param connection a connection to the SQL datasource.
     * @param a flag indicating if we have to use a derby syntax (replace true and false by 1 and 0)
     */
    public static void executeSQLScript(String path, Connection connection, boolean derbySource) {

        try {
            final BufferedReader in = new BufferedReader(new InputStreamReader(getResourceAsStream(path), "UTF-8"));
            final StringWriter sw   = new StringWriter();
            final char[] buffer     = new char[1024];
            int size;
            while ((size = in.read(buffer, 0, 1024)) > 0) {
                sw.append(new String(buffer, 0, size));
            }
            in.close();

            final Statement stmt  = connection.createStatement();
            String sqlQuery       = sw.toString();
            int end               = sqlQuery.indexOf(';');
            int nbQuery           = 0;
            while (end != -1) {
                String singleQuery = sqlQuery.substring(0, end);
                if (derbySource) {
                    singleQuery = singleQuery.replaceAll("true", "1");
                    singleQuery = singleQuery.replaceAll("false", "0");
                }
                try {
                    stmt.execute(singleQuery);
                    nbQuery++;
                } catch (SQLException ex) {
                    LOGGER.severe("SQLException while executing: " + singleQuery + '\n' + ex.getMessage() + '\n' + " in file:" + path + " instruction n° " + nbQuery);
                }
                sqlQuery = sqlQuery.substring(end + 1);
                end      = sqlQuery.indexOf(';');
            }
        } catch (IOException ex) {
            LOGGER.severe("IOException creating statement:" + '\n' + ex.getMessage());
        } catch (SQLException ex) {
            LOGGER.severe("SQLException creating statement:" + '\n' + ex.getMessage());
        }
    }
}
