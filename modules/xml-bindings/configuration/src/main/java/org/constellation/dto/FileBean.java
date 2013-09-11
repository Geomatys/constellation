package org.constellation.dto;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Bean for file information : name and boolean to define folder
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 */
public class FileBean {

    private String name;

    private Boolean folder;

    public FileBean() {
    }

    public FileBean(final String name, final Boolean folder) {
        this.name = name;
        this.folder = folder;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Boolean isFolder() {
        return folder;
    }

    public void setFolder(final Boolean folder) {
        this.folder = folder;
    }
}
