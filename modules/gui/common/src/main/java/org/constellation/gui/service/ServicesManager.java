package org.constellation.gui.service;

import org.constellation.gui.model.Service;

import java.util.logging.Logger;

/**
 * @author bgarcia
 * @since 27/05/13
 */
public class ServicesManager {

    private static final Logger LOGGER = Logger.getLogger(ServicesManager.class.getName());

    public ServicesManager() {
    }

    public void createServices(Service createdService){
        System.out.println("test currentService : "+createdService.getName());

    }
}
