package org.constellation.ws.rest.post;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Jersey POJO used on {@link org.constellation.ws.rest.Admin} service
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 *
 */
@XmlRootElement
public class Configuration {

    /**
     * Constellation configuration path
     */
    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
