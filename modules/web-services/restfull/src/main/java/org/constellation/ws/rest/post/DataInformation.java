package org.constellation.ws.rest.post;

import juzu.Mapped;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.geotoolkit.coverage.grid.GeneralGridGeometry;
import org.geotoolkit.image.io.metadata.SpatialMetadata;
import org.opengis.metadata.Metadata;
import org.opengis.util.GenericName;

import javax.xml.bind.annotation.XmlRootElement;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Logger;

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

    private static final Logger LOGGER = Logger.getLogger(DataInformation.class.getName());

    private String name;

    private String path;

    private String dataType;

    private Map<String, String> coveragesMetadata;

    private DefaultMetadata fileMetadata;

    public DataInformation() {
    }

    public DataInformation(String path, String dataType, DefaultMetadata fileMetadata) {
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

    public Map<String, String> getCoveragesMetadata() {
        return coveragesMetadata;
    }

    public void setCoveragesMetadata(Map<String, String> coveragesMetadata) {
        this.coveragesMetadata = coveragesMetadata;
    }

    public DefaultMetadata getFileMetadata() {
        return fileMetadata;
    }

    public void setFileMetadata(DefaultMetadata fileMetadata) {
        this.fileMetadata = fileMetadata;
    }
}
