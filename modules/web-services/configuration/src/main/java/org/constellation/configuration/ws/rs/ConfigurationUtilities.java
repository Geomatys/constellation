package org.constellation.configuration.ws.rs;

import org.constellation.configuration.AbstractConfigurer;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.generic.database.BDD;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.WSEngine;
import org.constellation.ws.rs.ContainerNotifierImpl;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.mdweb.io.auth.AuthenticationReader;
import org.mdweb.io.auth.sql.v24.DataSourceAuthenticationReader;
import org.mdweb.model.auth.AuthenticationException;
import org.mdweb.model.auth.UserAuthnInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Benjamin Garcia (Geomatys)
 */
public class ConfigurationUtilities {

    private static final Logger LOGGER = Logger.getLogger(ConfigurationUtilities.class.getName());

    public static AcknowlegementType getUserName() {
        try {
            final AuthenticationReader authReader = getAuthReader();
            final List<UserAuthnInfo> users =  authReader.listAllUsers();
            String userName = null;
            if (users != null && !users.isEmpty()) {
                userName = users.get(0).getLogin();
            }
            authReader.destroy();
            return new AcknowlegementType("Success", userName);
        } catch (AuthenticationException ex) {
            LOGGER.log(Level.WARNING, "Error while updating user", ex);
        }
        return new AcknowlegementType("Failure", "An error occurs");
    }

    public static AcknowlegementType deleteUser(final String userName) {
        try {
            final AuthenticationReader authReader = getAuthReader();
            authReader.deleteUser(userName);
            authReader.destroy();
            return new AcknowlegementType("Success", "The user has been deleted");
        } catch (AuthenticationException ex) {
            LOGGER.log(Level.WARNING, "Error while deleting user", ex);
        }
        return new AcknowlegementType("Failure", "An error occurs");
    }


    public static AuthenticationReader getAuthReader() {
        final File authProperties = ConfigDirectory.getAuthConfigFile();
        final Properties prop = new Properties();
        try {
            final FileInputStream fis = new FileInputStream(authProperties);
            prop.load(fis);
            final String url = prop.getProperty("cstl_authdb_host");
            final DefaultDataSource ds = new DefaultDataSource(url.replace('\\', '/') + ";");
            return new DataSourceAuthenticationReader(ds);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "IOException while loading cstl auth properties file", ex);
        }
        return null;
    }

    public static AcknowlegementType updateUser(final String userName, final String password, final String oldLogin) {
        try {
            final AuthenticationReader authReader = getAuthReader();
            authReader.writeUser(userName, password, "Default Constellation Administrator", Arrays.asList("cstl-admin"), oldLogin);
            authReader.destroy();
            return new AcknowlegementType("Success", "The user has been changed");
        } catch (AuthenticationException ex) {
            LOGGER.log(Level.WARNING, "Error while updating user", ex);
        }
        return new AcknowlegementType("Failure", "An error occurs");
    }

    public static AcknowlegementType setConfigPath(final String path) throws CstlServiceException {
        // Set the new user directory
        if (path != null && !path.isEmpty()) {
            final File userDirectory = new File(path);
            if (!userDirectory.isDirectory()) {
                userDirectory.mkdir();
            }
            ConfigDirectory.setConfigDirectory(userDirectory);
        }

        return new AcknowlegementType("Success", path);
    }

    public static AcknowlegementType getConfigPath() throws CstlServiceException {
        final String path = ConfigDirectory.getConfigDirectory().getPath();
        return new AcknowlegementType("Success", path);
    }

    /**
     * Restart all the web-services, reload the providers.
     * If some services are currently indexing, the service will not restart
     * unless you specified the flag "forced".
     *
     * @return an Acknowledgment if the restart succeed.
     */
    public static AcknowlegementType restartService(final boolean forced, final List<AbstractConfigurer> configurers, ContainerNotifierImpl cn) {
        LOGGER.info("\n restart requested \n");
        // clear cache
        for (AbstractConfigurer configurer : configurers) {
            configurer.beforeRestart();
        }

        if (cn != null) {
            if (!configurerLock(new AbstractConfigurer[0])) {
                BDD.clearConnectionPool();
                WSEngine.prepareRestart();
                cn.reload();
                return new AcknowlegementType(Parameters.SUCCESS, "services succefully restarted");
            } else if (!forced) {
                return new AcknowlegementType("failed", "There is an indexation running use the parameter FORCED=true to bypass it.");
            } else {
                for (AbstractConfigurer configurer : configurers) {
                    configurer.closeForced();
                }
                BDD.clearConnectionPool();
                WSEngine.prepareRestart();
                cn.reload();
                return new AcknowlegementType(Parameters.SUCCESS, "services succefully restarted (previous indexation was stopped)");
            }
        } else {
            return new AcknowlegementType("failed", "The services can not be restarted (ContainerNotifier is null)");
        }

    }

    public static boolean configurerLock(final AbstractConfigurer[] configurers) {
        for (AbstractConfigurer configurer : configurers) {
            if (configurer.isLock()) return true;
        }
        return false;
    }
}
