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
import java.io.FileNotFoundException;
import java.util.logging.Level;
import javax.xml.bind.JAXBException;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.ServiceDef;
import org.constellation.configuration.WebdavContext;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.ws.AbstractWorker;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class WebdavWorker extends AbstractWorker {
 
    private final WebdavContext context;
    
    public WebdavWorker(final String id) {
        super(id, ServiceDef.Specification.WEBDAV);
        WebdavContext candidate = null;
        try {
            Object obj = ConfigurationEngine.getConfiguration("webdav", id);
            if (obj instanceof WebdavContext) {
                candidate = (WebdavContext) obj;
                isStarted = true;
            } else {
                startError = "The webdav context File does not contain a WebdavContext object";
                isStarted = false;
                LOGGER.log(Level.WARNING, startError);
            }
        } catch (FileNotFoundException | JAXBException ex) {
            startError = "JAXBExeception while unmarshalling the webdav context File";
            isStarted = false;
            LOGGER.log(Level.WARNING, startError, ex);
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
            url = url.replaceFirst('/' + getId(), "");
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

    @Override
    protected String getProperty(String propertyName) {
        return null; // not available in webDav
    }

    @Override
    protected MarshallerPool getMarshallerPool() {
        return null; // not available in webDav
    }
}
