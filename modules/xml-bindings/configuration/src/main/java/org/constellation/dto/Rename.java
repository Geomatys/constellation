package org.constellation.dto;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Jersey POJO used on {@link org.constellation.ws.rest.ServiceAdmin} service
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 *
 */
@XmlRootElement
public class Rename {


    private String newName;

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }
}
