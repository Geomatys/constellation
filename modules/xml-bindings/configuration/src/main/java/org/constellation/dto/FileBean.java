/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.dto;

import java.io.Serializable;

/**
 * Bean for file information : name and boolean to define folder
 *
 * @author Benjamin Garcia (Geomatys)
 * @author Cédric Briançon (Geomatys)
 * @version 0.9
 * @since 0.9
 */
public class FileBean implements Serializable,Comparable<FileBean> {

    private String name;

    private Boolean folder;

    private String path;

    private String parentPath;

    public FileBean() {
    }

    public FileBean(final String name, final Boolean folder, final String path, final String parentPath) {
        this.name = name;
        this.folder = folder;
        this.path = path;
        this.parentPath = parentPath;
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

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public String getParentPath() {
        return parentPath;
    }

    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    @Override
    public int compareTo(FileBean o) {
        return name.compareTo(o.getName());
    }
}
