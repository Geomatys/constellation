package org.constellation.dto;

import java.util.Date;

/**
 * Just bean to acces to style information
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 */
public class StyleBean {

    /**
     * Style name.
     */
    private String name;

    /**
     * Provider id.
     */
    private String providerId;

    /**
     * Creation date.
     */
    private Date date;

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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
