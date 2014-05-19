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

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import org.constellation.util.SimplyMetadataTreeNode;

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
