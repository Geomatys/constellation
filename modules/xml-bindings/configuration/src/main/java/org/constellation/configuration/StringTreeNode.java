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

package org.constellation.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
