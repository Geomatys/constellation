/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2015 Geomatys.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Used for dynamic featureStore/CoverageStore creation panel.
 * 
 * @author Johann Sorel (Geomatys)
 * @version 0.9
 * @since 0.9
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DataCustomConfiguration {

    @XmlElementWrapper(name="types")
    @XmlElement(name="Type")
    private List<Type> types;

    public synchronized List<Type> getTypes() {
        if(types==null) types = new ArrayList<>();
        return types;
    }

    public synchronized void setTypes(List<Type> types) {
        this.types = types;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class Type{

        @XmlElement(name="id")
        private String id;
        @XmlElement(name="title")
        private String title;
        @XmlElement(name="description")
        private String description;
        @XmlElement(name="category")
        private String category;
        @XmlElement(name="selected")
        private boolean selected = false;
        @XmlElement(name="property")
        private Property property;
        
        public Type() {
        }

        public Type(String name) {
            this.title = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public boolean isSelected() {
            return selected;
        }

        public Property getProperty() {
            return property;
        }

        public void setProperty(Property property) {
            this.property = property;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class Property{
        @XmlElement(name="id")
        private String id;
        @XmlElement(name="description")
        private String description;
        @XmlElement(name="type")
        private String type;
        @XmlElement(name="value")
        private Object value;
        @XmlElement(name="optional")
        private boolean optional;
        @XmlElementWrapper(name="Properties")
        @XmlElement(name="Property")
        private List<Property> properties;

        public Property() {
        }

        public Property(String name, String description, String type, Object value, boolean optional) {
            this.id = name;
            this.description = description;
            this.type = type;
            this.value = value;
            this.optional = optional;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public boolean isOptional() {
            return optional;
        }

        public void setOptional(boolean optional) {
            this.optional = optional;
        }

        public synchronized List<Property> getProperties() {
            if(properties==null) properties = new ArrayList<>();
            return properties;
        }

        public synchronized void setProperties(List<Property> properties) {
            this.properties = properties;
        }

        /**
         * Copy property values in given map
         * @param map
         */
        public void toMap(Map map){
            for(Property p : getProperties()){
                if(p.type!=null){
                    map.put(p.id, p.value);
                }else{
                    final HashMap subs = new HashMap();
                    p.toMap(subs);
                    map.put(p.id, subs);
                }
            }
        }

    }

}
