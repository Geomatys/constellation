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
package org.constellation.webdav;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;
import java.io.File;
import java.util.logging.Level;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.ServiceDef;
import org.constellation.configuration.WebdavContext;
import org.constellation.configuration.ConfigurationException;
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
            Object obj = serviceBusiness.getConfiguration("webdav", id);
            if (obj instanceof WebdavContext) {
                candidate = (WebdavContext) obj;
                isStarted = true;
            } else {
                startError = "The webdav context File does not contain a WebdavContext object";
                isStarted = false;
                LOGGER.log(Level.WARNING, startError);
            }
        } catch (ConfigurationException ex) {
            startError = "ConfigurationException while unmarshalling the webdav context File";
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
