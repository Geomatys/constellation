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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.util.logging.Logging;
import org.opengis.feature.type.Name;

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
	
    private static final Logger LOGGER = Logging.getLogger("org.constellation.util");
    
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
                LOGGER.log(Level.SEVERE, "The distant service does not respond correctly: unable to unmarshall response document.\ncause: {0}", ex.getMessage());
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
            LOGGER.log(Level.SEVERE, "Exception throw in the invokated getter value() \nCause: {0}", ex.getMessage());
        } catch (NoSuchMethodException ex) {
           LOGGER.log(Level.SEVERE, "no such method value() in {0}", enumeration.getClass().getSimpleName());
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
     * Parse a String to instantiate a named Layer (namespace : name).
     * @param layerName
     * @return
     */
    public static Name parseLayerName(final String layerName) {
        final Name name;
        if (layerName != null && layerName.lastIndexOf(':') != -1) {
            final String namespace = layerName.substring(0, layerName.lastIndexOf(':'));
            final String localPart = layerName.substring(layerName.lastIndexOf(':') + 1);
            name = new DefaultName(namespace, localPart);
        } else {
            name = new DefaultName(layerName);
        }
        return name;
    }
    
    /**
     * Parse a String to instantiate a named Layer ({namespace}name).
     * @param layerName
     * @return
     */
    public static QName parseLayerQName(final String layerName) {
        final QName name;
        if (layerName != null && layerName.lastIndexOf('}') != -1) {
            final String namespace = layerName.substring(1, layerName.lastIndexOf('}'));
            final String localPart = layerName.substring(layerName.lastIndexOf('}') + 1);
            name = new QName(namespace, localPart);
        } else {
            name = new QName(layerName);
        }
        return name;
    }
    
    /**
     * decoding an UTF8/URL encoded strings
     * this is the reverse of {@link encodeToUTF8URL} method.
     * 
     * @param s
     * @return 
     */
    public static String decodeUTF8URL(String s) {
        StringBuilder sbuf = new StringBuilder();
        int l = s.length();
        int ch = -1;
        int b, sumb = 0;
        for (int i = 0, more = -1; i < l; i++) {
            /* Get next byte b from URL segment s */
            switch (ch = s.charAt(i)) {
                case '%':
                    ch = s.charAt(++i);
                    int hb = (Character.isDigit((char) ch)
                            ? ch - '0'
                            : 10 + Character.toLowerCase((char) ch) - 'a') & 0xF;
                    ch = s.charAt(++i);
                    int lb = (Character.isDigit((char) ch)
                            ? ch - '0'
                            : 10 + Character.toLowerCase((char) ch) - 'a') & 0xF;
                    b = (hb << 4) | lb;
                    break;
                case '+':
                    b = ' ';
                    break;
                default:
                    b = ch;
            }
            /* Decode byte b as UTF-8, sumb collects incomplete chars */
            if ((b & 0xc0) == 0x80) {			// 10xxxxxx (continuation byte)
                sumb = (sumb << 6) | (b & 0x3f);	// Add 6 bits to sumb
                if (--more == 0) {
                    sbuf.append((char) sumb); // Add char to sbuf
                }
            } else if ((b & 0x80) == 0x00) {		// 0xxxxxxx (yields 7 bits)
                sbuf.append((char) b);			// Store in sbuf
            } else if ((b & 0xe0) == 0xc0) {		// 110xxxxx (yields 5 bits)
                sumb = b & 0x1f;
                more = 1;				// Expect 1 more byte
            } else if ((b & 0xf0) == 0xe0) {		// 1110xxxx (yields 4 bits)
                sumb = b & 0x0f;
                more = 2;				// Expect 2 more bytes
            } else if ((b & 0xf8) == 0xf0) {		// 11110xxx (yields 3 bits)
                sumb = b & 0x07;
                more = 3;				// Expect 3 more bytes
            } else if ((b & 0xfc) == 0xf8) {		// 111110xx (yields 2 bits)
                sumb = b & 0x03;
                more = 4;				// Expect 4 more bytes
            } else /*if ((b & 0xfe) == 0xfc)*/ {	// 1111110x (yields 1 bit)
                sumb = b & 0x01;
                more = 5;				// Expect 5 more bytes
            }
            /* No need to test if the UTF-8 encoding is well-formed */
        }
        return sbuf.toString();
    }
}
