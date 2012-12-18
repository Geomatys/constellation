package org.constellation.webdav;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import org.constellation.configuration.ConfigDirectory;

import org.geotoolkit.util.logging.Logging;

/**
 * A resource factory which provides access to files in a file system.
 *
 * Using this with milton is equivalent to using the dav servlet in tomcat
 *
 */
public final class WebdavService implements ResourceFactory {

    private static final Logger LOGGER = Logging.getLogger(WebdavService.class);

    private final Map<String, WebdavWorker> workersMap;
            
    private final String contextPath;
    
    public WebdavService() {
        workersMap = buildWorkerMap();
        // all worker MUST have the same contextPath
        if (!workersMap.isEmpty()) {
            contextPath = workersMap.values().iterator().next().getContextPath();
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
        final WebdavWorker currentWorker = workersMap.get(instanceName);
        if (currentWorker != null) {
            return currentWorker.getResource(host, url);
        } else {
            LOGGER.log(Level.WARNING, "Unexisting webdav instance : {0}", instanceName);
        }
        return null;
    }


    private Map<String, WebdavWorker> buildWorkerMap() {
        final Map<String, WebdavWorker> workersMap = new HashMap<String, WebdavWorker>();
        final File serviceDirectory = getServiceDirectory();
        if (serviceDirectory != null && serviceDirectory.isDirectory()) {
            for (File instanceDirectory : serviceDirectory.listFiles()) {
                /*
                 * For each sub-directory we build a new Worker.
                 */
                if (instanceDirectory.isDirectory() && !instanceDirectory.getName().startsWith(".")) {
                    final WebdavWorker newWorker = new WebdavWorker(instanceDirectory.getName(), instanceDirectory);
                    if (newWorker != null) {
                        workersMap.put(instanceDirectory.getName(), newWorker);
                    }
                }
            }
        } else {
            LOGGER.log(Level.WARNING, "no webdav directory.");
        }
        return workersMap;
    }
    
    protected File getServiceDirectory() {
        final File configDirectory   = ConfigDirectory.getConfigDirectory();
        if (configDirectory != null && configDirectory.isDirectory()) {
            final File serviceDirectory = new File(configDirectory, "webdav");
            if (serviceDirectory.isDirectory()) {
                return serviceDirectory;
            } else {
                LOGGER.log(Level.INFO, "The service configuration directory: {0} does not exist or is not a directory, creating new one.", serviceDirectory.getPath());
                if (!serviceDirectory.mkdir()) {
                    LOGGER.log(Level.SEVERE, "The service was unable to create the directory.{0}", serviceDirectory.getPath());
                } else {
                    return serviceDirectory;
                }
            }
        } else {
            if (configDirectory == null) {
                LOGGER.severe("The service was unable to find a config directory.");
            } else {
                LOGGER.log(Level.SEVERE, "The configuration directory: {0} does not exist or is not a directory.", configDirectory.getPath());
            }
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
