package org.constellation.gui.service;

import org.constellation.admin.service.ConstellationServer;
import org.constellation.dto.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author bgarcia
 * @since 27/05/13
 */
public class ServicesManager {

    private static final Logger LOGGER = Logger.getLogger(ServicesManager.class.getName());

    public ServicesManager() {
    }

    public void createServices(Service createdService, String service) {
        if (createdService != null) {
            LOGGER.log(Level.INFO, "service will be created : " + createdService.getName());
            URL serverUrl = null;
            try {
                serverUrl = new URL("http://localhost:8090/constellation/WS/");
                ConstellationServer cs = new ConstellationServer(serverUrl, "admin", "admin");
                cs.services.newInstance(service, createdService);
            } catch (MalformedURLException e) {
                LOGGER.log(Level.WARNING, "error on url", e);
            }
        }
    }
}
