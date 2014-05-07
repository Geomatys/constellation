package org.constellation.util;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Reprensent more simply as possible a node on metadata tree
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 *
 */
public class SimplyMetadataTreeNode {

    private String name;
    private String value;
    private boolean childrenExist;
    private String nameNoWhiteSpace;
    private int depthSpan;
    private String parentName;

    public SimplyMetadataTreeNode() {
        
    }
    
    public SimplyMetadataTreeNode(final String name, final boolean childrenExist, final String nameNoWhiteSpace, 
            final int depthSpan, final String parentName) {
        this.name             = name;
        this.childrenExist    = childrenExist;
        this.nameNoWhiteSpace = nameNoWhiteSpace;
        this.depthSpan        = depthSpan;
        this.parentName       = parentName;
        
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isChildrenExist() {
        return childrenExist;
    }

    public void setChildrenExist(boolean childrenExist) {
        this.childrenExist = childrenExist;
    }

    public String getNameNoWhiteSpace() {
        return nameNoWhiteSpace;
    }

    public void setNameNoWhiteSpace(String nameNoWhiteSpace) {
        this.nameNoWhiteSpace = nameNoWhiteSpace;
    }

    public int getDepthSpan() {
        return depthSpan;
    }

    public void setDepthSpan(int depthSpan) {
        this.depthSpan = depthSpan;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    /**
     * /!\ Important : generate JSON. Used on javascript
     * @return object as JSON
     */
    @Override
    public String toString() {
        final String jsonValue  = StringEscapeUtils.escapeEcmaScript(value);
        return "{" +
                "\"name\":\"" + name + '\"' +
                ", \"value\":\"" + jsonValue + '\"' +
                ", \"childrenExist\":" + childrenExist +
                ", \"nameNoWhiteSpace\":\"" + nameNoWhiteSpace + '\"' +
                ", \"depthSpan\":" + depthSpan +
                ", \"parentName\":\"" + parentName + '\"' +
                '}';
    }
}
