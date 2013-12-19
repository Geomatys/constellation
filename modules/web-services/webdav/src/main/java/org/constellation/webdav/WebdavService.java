package org.constellation.webdav;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;

import org.apache.sis.util.logging.Logging;
import org.constellation.ServiceDef.Specification;
import org.constellation.ws.WSEngine;
import org.constellation.ws.Worker;

/**
 * A resource factory which provides access to files in a file system.
 *
 * Using this with milton is equivalent to using the dav servlet in tomcat
 *
 */
public final class WebdavService implements ResourceFactory {

    private static final Logger LOGGER = Logging.getLogger(WebdavService.class);

    private final Map<String, Worker> workersMap;
            
    private final String contextPath;
    
    public WebdavService() {
        workersMap = WSEngine.getWorkersMap(Specification.WEBDAV.name());
        
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
