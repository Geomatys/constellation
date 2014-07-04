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

import org.constellation.util.SimplyMetadataTreeNode;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

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
