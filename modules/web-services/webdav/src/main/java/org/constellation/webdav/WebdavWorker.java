/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009-2010, Geomatys
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
package org.constellation.webdav;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.constellation.configuration.WebdavContext;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.geotoolkit.util.logging.Logging;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class WebdavWorker {
 
    private static final Logger LOGGER = Logging.getLogger(WebdavWorker.class);
    
    private final WebdavContext context;
    
    private boolean isStarted;
    
    private String startError;
    
    private String id;
    
    public WebdavWorker(final String id, final File configurationDirectory) {
        this.id = id;
        WebdavContext candidate = null;
        if (configurationDirectory != null) {
            final File lcFile = new File(configurationDirectory, "WebdavContext.xml");
            if (lcFile.exists()) {
                Unmarshaller unmarshaller = null;
                try {
                    unmarshaller = GenericDatabaseMarshallerPool.getInstance().acquireUnmarshaller();
                    Object obj = unmarshaller.unmarshal(lcFile);
                    if (obj instanceof WebdavContext) {
                        candidate = (WebdavContext) obj;
                        isStarted = true;
                    } else {
                        startError = "The webdav context File does not contain a WebdavContext object";
                        isStarted = false;
                        LOGGER.log(Level.WARNING, startError);
                    }
                } catch (JAXBException ex) {
                    startError = "JAXBExeception while unmarshalling the webdav context File";
                    isStarted = false;
                    LOGGER.log(Level.WARNING, startError, ex);
                } finally {
                    if (unmarshaller != null) {
                        GenericDatabaseMarshallerPool.getInstance().release(unmarshaller);
                    }
                }
            } else {
                startError = "The configuration file processContext.xml has not been found";
                isStarted = false;
                LOGGER.log(Level.WARNING, "\nThe worker ({0}) is not working!\nCause: ", id);
            }
        } else {
            startError = "The configuration directory has not been found";
            isStarted = false;
            LOGGER.log(Level.WARNING, "\nThe worker ({0}) is not working!\nCause: " + startError, id);
        }
        this.context = candidate;
        this.context.setId(id);
        if (isStarted) {
            LOGGER.log(Level.INFO, "Webdav worker {0} running", id);
        }
    }
    
    public Resource getResource(final String host, final String url) {
        LOGGER.log(Level.FINER, "getResource host={0} url={1}", new Object[]{host, url});
        final String strippedUrl = stripContext(url);
        final File requested = resolvePath(context.getRootFile(), strippedUrl);
        return resolveFile(host, requested);
    }


    private FsResource resolveFile(final String host, final File file) {
        FsResource r;
        if (!file.exists()) {
            LOGGER.log(Level.INFO, "file not found: {0}", file.getAbsolutePath());
            return null;
        } else if (file.isDirectory()) {
            r = new FsDirectoryResource(host, file,context);
        } else {
            r = new FsFileResource(host, file, context);
        }
        return r;
    }

    private File resolvePath(final File root, final String url) {
        Path path = Path.path(url);
        File f = root;
        for (String s : path.getParts()) {
            f = new File(f, s);
        }
        return f;
    }
    
    private String stripContext(String url) {
        if (context.getContextPath() != null && context.getContextPath().length() > 0) {
            url = Path.path(url).getStripFirst().toPath();
            url = url.replaceFirst('/' + context.getContextPath(), "");
            url = url.replaceFirst('/' + id, "");
            return url;
        } else {
             return Path.path(url).getStripFirst().toPath();
        }
    }
    
    public String getContextPath() {
        if (isStarted) {
            return context.getContextPath();
        }
        return null;
    }
}
