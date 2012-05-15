package org.constellation.webdav;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.constellation.configuration.WebdavContext;
import org.geotoolkit.util.logging.Logging;

/**
 * A resource factory which provides access to files in a file system.
 *
 * Using this with milton is equivalent to using the dav servlet in tomcat
 *
 */
public final class WebdavService implements ResourceFactory {

    private static final Logger LOGGER = Logging.getLogger(WebdavService.class);
    private final WebdavContext context;

    public WebdavService() {
        LOGGER.info("setting default configuration...");
        this.context = new WebdavContext(System.getProperty("user.home"));
        if (!context.getRootFile().exists()) {
            LOGGER.log(Level.WARNING, "Root folder does not exist: {0}", context.getRootFile().getAbsolutePath());
        }
        if (!context.getRootFile().isDirectory()) {
            LOGGER.log(Level.WARNING, "Root exists but is not a directory: {0}", context.getRootFile().getAbsolutePath());
        }
        final int i = 1; // temporary
        LOGGER.log(Level.INFO, "Webdav REST service running ({0} instances)", i);
    }

    @Override
    public Resource getResource(final String host, final String url) {
        LOGGER.log(Level.INFO, "getResource host={0} url={1}", new Object[]{host, url});
        final String strippedUrl = stripContext(url);
        final File requested = resolvePath(context.getRootFile(), strippedUrl);
        return resolveFile(host, requested);
    }


    public FsResource resolveFile(final String host, final File file) {
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

    public File resolvePath(final File root, final String url) {
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
            return url;
        } else {
             return Path.path(url).getStripFirst().toPath();
        }
    }
}
