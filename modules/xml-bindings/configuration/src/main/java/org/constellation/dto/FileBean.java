package org.constellation.dto;

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

    private String subPath;

    public FileBean() {
    }

    public FileBean(final String name, final Boolean folder, final String subPath) {
        this.name = name;
        this.folder = folder;
        this.subPath = subPath;
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

    public String getSubPath() {
        return subPath;
    }

    public void setSubPath(final String subPath) {
        this.subPath = subPath;
    }
}
