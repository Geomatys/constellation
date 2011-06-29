/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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

package org.constellation.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @since 0.8
 */
@XmlRootElement(name="StringTreeNode")
@XmlAccessorType(XmlAccessType.FIELD)
public class StringTreeNode {

    private HashMap<String,String> properties;
    
    @XmlElement(name="Child")
    private List<StringTreeNode> children;

    public HashMap<String, String> getProperties() {
        if(properties == null){
            properties = new HashMap<String, String>();
        }
        return properties;
    }

    public void setProperties(HashMap<String, String> properties) {
        this.properties = properties;
    }
    
    public List<StringTreeNode> getChildren() {
        if(children == null){
            children = new ArrayList<StringTreeNode>();
        }
        return children;
    }

    public void setChildren(List<StringTreeNode> children) {
        this.children = children;
    }

}
