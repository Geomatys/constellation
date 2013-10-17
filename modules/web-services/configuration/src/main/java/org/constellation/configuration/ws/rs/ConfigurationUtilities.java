package org.constellation.configuration.ws.rs;

import org.constellation.admin.dao.Session;
import org.constellation.admin.EmbeddedDatabase;
import org.constellation.admin.dao.UserRecord;
import org.constellation.configuration.AbstractConfigurer;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.WSEngine;
import static org.constellation.api.CommonConstants.*;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.constellation.ws.Worker;

/**
 * @author Benjamin Garcia (Geomatys)
 */
public class ConfigurationUtilities {

    private static final Logger LOGGER = Logger.getLogger(ConfigurationUtilities.class.getName());

    public static AcknowlegementType getUserName() {
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            final List<UserRecord> users = session.readUsers();
            String userName = null;
            if (users != null && !users.isEmpty()) {
                userName = users.get(0).getLogin();
            }
            return new AcknowlegementType(SUCCESS, userName);
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Error while reading users", ex);
        } finally {
            if (session != null) session.close();
        }
        return new AcknowlegementType("Failure", "An error occurs");
    }

    @Deprecated
    public static AcknowlegementType deleteUser(final String userName) {
//        Session session = null;
//        try {
//            session = EmbeddedDatabase.createSession();
//            session.deleteUser(userName);
//            return new AcknowlegementType(SUCCESS, "The user has been deleted");
//        } catch (SQLException ex) {
//            LOGGER.log(Level.WARNING, "Error while deleting user", ex);
//        } finally {
//            if (session != null) session.close();
//        }
//        return new AcknowlegementType("Failure", "An error occurs");

        return new AcknowlegementType("Failure", "Operation no longer supported");
    }

    @Deprecated
    public static AcknowlegementType updateUser(final String userName, final String password, final String oldLogin) {
//        Session session = null;
//        try {
//            session = EmbeddedDatabase.createSession();
//            session.updateUser(oldLogin, userName, password, "Default Constellation Administrator", Arrays.asList("cstl-admin"));
//            return new AcknowlegementType(SUCCESS, "The user has been changed");
//        } catch (SQLException ex) {
//            LOGGER.log(Level.WARNING, "Error while deleting user", ex);
//        } finally {
//            if (session != null) session.close();
//        }
//        return new AcknowlegementType("Failure", "An error occurs");

        return new AcknowlegementType("Failure", "Operation no longer supported");
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

        return new AcknowlegementType(SUCCESS, path);
    }

    public static AcknowlegementType getConfigPath() throws CstlServiceException {
        final String path = ConfigDirectory.getConfigDirectory().getPath();
        return new AcknowlegementType(SUCCESS, path);
    }

    /**
     * Restart all the web-services, reload the providers.
     * If some services are currently indexing, the service will not restart
     * unless you specified the flag "forced".
     *
     * @return an Acknowledgment if the restart succeed.
     */
    public static AcknowlegementType restartService(final boolean forced, final List<AbstractConfigurer> configurers) {
        LOGGER.info("\n restart requested \n");

        for (String serviceType : WSEngine.getRegisteredServices().keySet()) {
            final Map<String, Worker> workersMap = new HashMap<>();
            for (String instanceID : WSEngine.getInstanceNames(serviceType)) {
                try {
                    final Worker worker = WSEngine.buildWorker(serviceType, instanceID);
                    if (worker != null) {
                        workersMap.put(instanceID, worker);
                    } else {
                        LOGGER.log(Level.WARNING, "The instance {0} can be started, maybe there is no configuration directory with this name.", instanceID);
                    }
                } catch (IllegalArgumentException ex) {
                    LOGGER.log(Level.WARNING, ex.getMessage(), ex);
                }
            }
            WSEngine.setServiceInstances(serviceType, workersMap);
        }
        return new AcknowlegementType(SUCCESS, "services succefully restarted");
    }

    public static boolean configurerLock(final AbstractConfigurer[] configurers) {
        for (AbstractConfigurer configurer : configurers) {
            if (configurer.isLock()) return true;
        }
        return false;
    }
}
