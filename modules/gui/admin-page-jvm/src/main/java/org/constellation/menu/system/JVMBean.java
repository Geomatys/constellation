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

package org.constellation.menu.system;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.tree.TreeNode;
import org.mapfaces.renderkit.html.outline.OutlineCellStyler;

/**
 * Returns several information from the system jvm.
 *
 * @author Johann Sorel (Geomatys)
 */
public class JVMBean {

    public static final OutlineCellStyler CELL_STYLER = new OutlineCellStyler() {

        @Override
        public String getCellStyle(TreeNode node, int rowIndex, int columnIndex) {
            if(columnIndex > 0){
                return "border-left: 1px solid #A2ACB6; padding-left:20px;";
            }
            
            return "";
        }

        @Override
        public String getCellClass(TreeNode node, int rowIndex, int columnIndex) {
            return "";
        }

    };
    
    /**
     * List of most interesting system keys.
     * We don't want to display all of them.
     */
    private static final String[] SYSTEM_KEYS = new String[]{
            "java.version",
            "java.vendor",
            "java.vm.name",

            "java.home",
            "java.awt.graphicsenv",
            "java.endorsed.dirs",
            "java.library.path",
            "java.ext.dirs",
            "java.class.path",

            "user.language",
            "user.country",
            "user.timezone",
            "user.name",
            "user.dir",
            "user.home",
            "java.io.tmpdir",
            "file.encoding",

            "os.arch",
            "os.name",
            "os.version"
    };

    public Map<Object, Object> getSystemMap(){
        final Map<Object,Object> params = new HashMap<Object, Object>();
        
        final Map<Object,Object> system = System.getProperties();
        for(String key : SYSTEM_KEYS){
            params.put(key, system.get(key));
        }
        
        return params;
    }

    public void gc(){
        //better call this twice, not done right away the first time.
        Runtime.getRuntime().gc();
        Runtime.getRuntime().gc();

//        JAI.getDefaultInstance().getTileCache().getMemoryCapacity();
//
//        new FactoryDependencies().asTree();
//        hints = new Hints();

    }

    public String getMaxMemory(){
        return String.valueOf(Runtime.getRuntime().maxMemory());
    }

    public String getFreeMemory(){
        return String.valueOf(Runtime.getRuntime().freeMemory());
    }

}
