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

package org.constellation.json.metadata.binding;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Pojo class used for Jackson that represents the binding for root object
 * in metadata template json.
 *
 * @author Mehdi Sidhoum (Geomatys).
 * @since 0.9
 */
public class RootObj implements Serializable {
    private RootBlock root;
    
    private List<NodeType> nodeTypes;

    public RootObj(){
    }
    
    public RootObj(RootObj rootObj){
        this.root = new RootBlock(rootObj.root);
        if (rootObj.nodeTypes != null) {
            this.nodeTypes = new ArrayList<>();
            for (NodeType nd : rootObj.nodeTypes) {
                this.nodeTypes.add(new NodeType(nd));
            }
        }
    }

    public RootBlock getRoot() {
        return root;
    }

    public void setRoot(RootBlock root) {
        this.root = root;
    }

    /**
     * @return the nodeTypes
     */
    public List<NodeType> getNodeTypes() {
        return nodeTypes;
    }

    /**
     * @param nodeTypes the nodeTypes to set
     */
    public void setNodeTypes(List<NodeType> nodeTypes) {
        this.nodeTypes = nodeTypes;
    }
    
    public String getTypeForPath(final String path) {
        if (nodeTypes != null) {
            for (NodeType nt : nodeTypes) {
                if (nt.getPath().equals(path)) {
                    return nt.getType();
                }
            }
        }
        return null;
    }
}
