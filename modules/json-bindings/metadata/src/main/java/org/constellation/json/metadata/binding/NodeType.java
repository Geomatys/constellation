
package org.constellation.json.metadata.binding;

import java.io.Serializable;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class NodeType implements Serializable {
    
    private String path;
    private String type;

    public NodeType() {
        
    }
    
    public NodeType(String path, String type) {
        this.path = path;
        this.type = type;
    }

    public NodeType(NodeType nt) {
        this.path = nt.path;
        this.type = nt.type;
    }
    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }
    
}
