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

//import juzu.Mapped;

import org.constellation.util.SimplyMetadataTreeNode;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
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
