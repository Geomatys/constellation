/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.dto;

//import juzu.Mapped;
import org.constellation.util.SimplyMetadataTreeNode;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pojo which contains all metadata for a specific data and his path and type (vector, raster, sensor)
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 */
@XmlRootElement
//@Mapped
public class DataInformation {

    private String name;

    private String path;

    private String dataType;

    private Map<String, CoverageMetadataBean> coveragesMetadata;

    private ArrayList<SimplyMetadataTreeNode> fileMetadata;

    private String crs;

    private String errorInformation;

    public DataInformation() {
    }

    public DataInformation(final String errorInformation) {
        this.errorInformation = errorInformation;
    }

    public DataInformation(String path, String dataType, ArrayList<SimplyMetadataTreeNode> fileMetadata) {
        this.path = path;
        this.dataType = dataType;
        this.fileMetadata = fileMetadata;
    }

    public DataInformation(final String name, final String path, final String dataType, final String crs) {
        this.name = name;
        this.path = path;
        this.dataType = dataType;
        this.crs = crs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Map<String, CoverageMetadataBean> getCoveragesMetadata() {
        return coveragesMetadata;
    }

    public void setCoveragesMetadata(Map<String,CoverageMetadataBean> coveragesMetadata) {
        this.coveragesMetadata = coveragesMetadata;
    }

    public ArrayList<SimplyMetadataTreeNode> getFileMetadata() {
        return fileMetadata;
    }

    public void setFileMetadata(ArrayList<SimplyMetadataTreeNode> fileMetadata) {
        this.fileMetadata = fileMetadata;
    }

    public String getCrs() {
        return crs;
    }

    public void setCrs(final String crs) {
        this.crs = crs;
    }

    public String getErrorInformation() {
        return errorInformation;
    }

    public void setErrorInformation(final String errorInformation) {
        this.errorInformation = errorInformation;
    }
}
