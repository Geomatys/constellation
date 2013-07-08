package org.constellation.gui.service;


import org.apache.sis.util.logging.Logging;
import org.constellation.admin.service.ConstellationServer;
import org.constellation.configuration.LayerList;
import org.constellation.dto.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Benjamin Garcia (Geomatys)
 */
public class WMSManager {

    private static final Logger LOGGER = Logging.getLogger(WMSManager.class.getName());

    /**
     * constellation server URL
     */
    private String constellationUrl;

    /**
     * constellation server user login
     */
    private String login;

    /**
     * constellation server user password
     */
    private String password;

    public void setConstellationUrl(String constellationUrl) {
        this.constellationUrl = constellationUrl;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     *
     * @param serviceName
     * @param serviceType
     * @return
     */
    public Service getServiceMetadata(final String serviceName, final String serviceType){
        Service service = new Service();
        service.setName(serviceName);
        try{
            URL serverUrl = new URL(constellationUrl);
            ConstellationServer cs = new ConstellationServer(serverUrl, login, password);
            service = cs.services.getMetadata(serviceType, serviceName);
        }catch (MalformedURLException e){
            LOGGER.log(Level.WARNING, "error on url", e);
        }
        return service;
    }

    /**
     *
     * @param serviceName
     * @param serviceType
     * @return
     */
    public LayerList getLayers(final String serviceName, final String serviceType){
        LayerList layers = new LayerList();
        try {
            URL serverUrl = new URL(constellationUrl);
            ConstellationServer cs = new ConstellationServer(serverUrl, login, password);
            layers = cs.services.getLayers(serviceType, serviceName);
        } catch (MalformedURLException e) {
            LOGGER.log(Level.WARNING, "", e);
        }
        return layers;
    }

}
