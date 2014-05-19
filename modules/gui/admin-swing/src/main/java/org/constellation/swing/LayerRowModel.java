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

package org.constellation.swing;

import java.util.ResourceBundle;
import org.netbeans.swing.outline.RowModel;

/**
 *
 * @author Quentin Boileau (Geomatys)
 */
public class LayerRowModel implements RowModel {

    public static final ResourceBundle BUNDLE = ResourceBundle.getBundle("org/constellation/swing/Bundle");
    
    public static class EditLayer{}
    public static class DeleteLayer{}
    
    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueFor(Object o, int i) {
        //do nothing, edition are just actions
        return o;
    }

    @Override
    public Class getColumnClass(int i) {
        switch(i){
            case 0 : return EditLayer.class;
            case 1 : return DeleteLayer.class;
            default: return Object.class;
        }
    }

    @Override
    public boolean isCellEditable(Object o, int i) {
        return true;
    }

    @Override
    public void setValueFor(Object o, int i, Object o1) {
        //do nothing
    }

    @Override
    public String getColumnName(int i) {
        switch(i){
            case 0 : return BUNDLE.getString("edit");
            case 1 : return BUNDLE.getString("delete");
            default: return "";
        }
    }
    
}
