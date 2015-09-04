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

package org.constellation.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.sis.util.logging.Logging;
import org.constellation.lib.base.CstlLibBaseRuntimeException;
import org.geotoolkit.feature.type.NamesExt;
import org.opengis.util.GenericName;

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

    public static final DateFormat LUCENE_DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
    static {
        LUCENE_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private Util() {}

    /**
     * This should be a class loader from the main constellation application.
     */
    private static final ClassLoader baseClassLoader;

    //we try to load this variable at the start by reading a properties file
    static {
        baseClassLoader = Thread.currentThread().getContextClassLoader();
    }
    
    public static <T> T copy(T src, T dst) {
        try {
            BeanUtils.copyProperties(dst, src);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new CstlLibBaseRuntimeException(e);
        }
        return dst;
    }


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
    public static GenericName parseLayerName(final String layerName) {
        final GenericName name;
        if (layerName != null && layerName.lastIndexOf(':') != -1) {
            final String namespace = layerName.substring(0, layerName.lastIndexOf(':'));
            final String localPart = layerName.substring(layerName.lastIndexOf(':') + 1);
            name = NamesExt.create(namespace, localPart);
        } else {
            name = NamesExt.create(layerName);
        }
        return name;
    }

    /**
     * Parse a String to instantiate a named Layer ({namespace}name).
     * @param layerName
     * @return
     * 
     * @deprecated use parseQName()
     */
    @Deprecated
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
    
    public static QName parseQName(String name) {
        if (name != null) {
            if (name.startsWith("{}")) {
                name = name.substring(2);
            }
            return QName.valueOf(name);
        } 
        return null;
    }

    public static File getWebappDiretory() {
        final URL url = baseClassLoader.getResource("org/constellation/util/Util.class");
        String path = url.toString();
        path = path.substring(path.lastIndexOf(':') + 1); // we remove the file type
        final int separator = path.indexOf('!'); // we remove the path inside the jar
        if (separator != -1) {
            path = path.substring(0, separator);
        }
        File f = new File(path);
        f = f.getParentFile(); // lib
        f = f.getParentFile(); // WEB-INF
        f = f.getParentFile(); // webapp root
        return f;
    }

    public static File[] expandSubDirectories(File... files) {
        Set<File> addTo = new LinkedHashSet<>();
        expandSubDirectories(files, addTo);
        return addTo.toArray(new File[addTo.size()]);
    }

    private static void expandSubDirectories(File[] files, Set<File> addTo) {
        if (files != null) {
            for (File f : files) {
                addTo.add(f);
                if (f.isDirectory()) {
                    expandSubDirectories(f.listFiles(), addTo);
                }
            }
        }
    }
}
