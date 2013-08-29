package org.constellation.dto;

import juzu.Mapped;
import org.constellation.utils.SimplyMetadataTreeNode;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Pojo which contains all metadata for a specific data and his path and type (vector, raster, sensor)
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 */
@XmlRootElement
@Mapped
public class DataInformation {

    private String name;

    private String path;

    private String dataType;

    private HashMap<String, CoverageMetadataBean> coveragesMetadata;

    private ArrayList<SimplyMetadataTreeNode> fileMetadata;

    public DataInformation() {
    }

    public DataInformation(String path, String dataType, ArrayList<SimplyMetadataTreeNode> fileMetadata) {
        this.path = path;
        this.dataType = dataType;
        this.fileMetadata = fileMetadata;
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

    public HashMap<String, CoverageMetadataBean> getCoveragesMetadata() {
        return coveragesMetadata;
    }

    public void setCoveragesMetadata(HashMap<String,CoverageMetadataBean> coveragesMetadata) {
        this.coveragesMetadata = coveragesMetadata;
    }

    public ArrayList<SimplyMetadataTreeNode> getFileMetadata() {
        return fileMetadata;
    }

    public void setFileMetadata(ArrayList<SimplyMetadataTreeNode> fileMetadata) {
        this.fileMetadata = fileMetadata;
    }
}
