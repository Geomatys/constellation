package org.constellation.gui.service;


import org.apache.sis.util.logging.Logging;
import org.constellation.admin.service.ConstellationServer;
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
     * @param ServiceType
     * @return
     */
    public Service getServiceMetadata(final String serviceName, final String ServiceType){
        final Service service = new Service();
        service.setName(serviceName);
        try{
            URL serverUrl = new URL(constellationUrl);
            ConstellationServer cs = new ConstellationServer(serverUrl, login, password);


        }catch (MalformedURLException e){
            LOGGER.log(Level.WARNING, "error on url", e);
        }
        return service;
    }

}
