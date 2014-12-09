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

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import org.apache.sis.util.logging.Logging;
import org.constellation.ServiceDef.Specification;
import org.constellation.ws.WSEngine;
import org.constellation.ws.Worker;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A resource factory which provides access to files in a file system.
 *
 * Using this with milton is equivalent to using the dav servlet in tomcat
 *
 */
public final class WebdavService implements ResourceFactory {

    private static final Logger LOGGER = Logging.getLogger(WebdavService.class);

    private final String contextPath;
    
    public WebdavService() {
        final Map<String, Worker> workersMap = WSEngine.getWorkersMap(Specification.WEBDAV.name());
        
        // all worker MUST have the same contextPath
        if (!workersMap.isEmpty()) {
            contextPath = ((WebdavWorker)workersMap.values().iterator().next()).getContextPath();
        } else {
            contextPath = null;
        }
        LOGGER.log(Level.INFO, "Webdav REST service running ({0} instances)", workersMap.size());
    }

    @Override
    public Resource getResource(final String host, final String url) {
        LOGGER.log(Level.INFO, "getResource host={0} url={1}", new Object[]{host, url});
        final Map<String, Worker> workersMap = WSEngine.getWorkersMap(Specification.WEBDAV.name());
        if (workersMap.isEmpty()) {
            return null;
        }
        final String instanceName = getInstanceName(url);
        if (instanceName == null) {
            return null; 
        }
        final WebdavWorker currentWorker = (WebdavWorker) workersMap.get(instanceName);
        if (currentWorker != null) {
            return currentWorker.getResource(host, url);
        } else {
            LOGGER.log(Level.WARNING, "Unexisting webdav instance : {0}", instanceName);
        }
        return null;
    }

    private String getInstanceName(final String url) {
        final int i = url.indexOf(contextPath);
        if (url.endsWith(contextPath) || url.endsWith(contextPath+"/")) {
            return null;
        }
        String s = url.substring(i + contextPath.length() + 1);
        if (s.isEmpty()) {
            return null;
        } else if (s.indexOf('/') != -1) {
            return s.substring(0, s.indexOf('/'));
        } else {
            return s;
        }
    }
}
