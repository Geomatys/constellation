package org.constellation.ws.rest.post;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Jersey POJO used on {@link org.constellation.ws.rest.Admin} and {@link org.constellation.ws.rest.ServiceAdmin} service
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

    /**
     * close first service before restart it
     */
    private boolean closeFirst;

    public boolean isForced() {
        return forced;
    }

    public void setForced(boolean forced) {
        this.forced = forced;
    }

    public boolean isCloseFirst() {
        return closeFirst;
    }

    public void setCloseFirst(boolean closeFirst) {
        this.closeFirst = closeFirst;
    }
}
