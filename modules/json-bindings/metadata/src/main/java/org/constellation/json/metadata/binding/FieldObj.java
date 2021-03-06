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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import java.io.Serializable;

/**
 * Pojo class used for Jackson that represents the binding for field object
 * in metadata template json.
 *
 * @author Mehdi Sidhoum (Geomatys).
 * @since 0.9
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class FieldObj extends ComponentObj implements Serializable {

    private Field field;

    public FieldObj(){

    }
    
    public FieldObj(final FieldObj fieldO){
        if (fieldO != null) {
            this.field = new Field(fieldO.field);
        }
    }
    
    public FieldObj(final Field field){
        this.field = field;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }
    
    @Override
    public String getPath() {
        return field.getPath();
    }
    
    public static boolean diff(FieldObj original, FieldObj modified) {
        return Field.diff(original.field, modified.field);
    }
    
    @Override
    public String toString() {
        return "[FieldObj]\nfield:" + field;
    }

    @Override
    public void updatePath(final String oldPrefix, final String newPrefix) {
        field.updatePath(oldPrefix, newPrefix);
    }
}
