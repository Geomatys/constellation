package org.constellation.dto;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Just bean to acces to style information
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 */
@XmlRootElement
public class StyleBean {

    /**
     * style name
     */
    private String name;
    /**
     * provider id
     */
    private String providerId;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(final String providerId) {
        this.providerId = providerId;
    }
}
