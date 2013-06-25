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
public class Restart {

    /**
     * define restart need to be forced
     */
    private boolean forced;

    public boolean isForced() {
        return forced;
    }

    public void setForced(boolean forced) {
        this.forced = forced;
    }
}
