package org.constellation.dto;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import org.constellation.utils.SimplyMetadataTreeNode;

/**
 * Container used to pass a list on JAXB
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 *
 */
@XmlRootElement
public class CoverageMetadataBean {


    /**
     * Coverage metadata tree as list.
     */
    private List<SimplyMetadataTreeNode> coverageMetadataTree;

    public CoverageMetadataBean(List<SimplyMetadataTreeNode> coverageMetadataTree) {
        this.coverageMetadataTree = coverageMetadataTree;
    }

    public CoverageMetadataBean() {
    }

    public List<SimplyMetadataTreeNode> getCoverageMetadataTree() {
        return coverageMetadataTree;
    }

    public void setCoverageMetadataTree(List<SimplyMetadataTreeNode> coverageMetadataTree) {
        this.coverageMetadataTree = coverageMetadataTree;
    }
}
