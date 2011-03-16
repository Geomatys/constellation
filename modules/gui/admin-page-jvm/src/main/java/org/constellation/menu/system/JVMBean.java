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
import java.util.List;
import java.util.Map;

/**
 * Returns several information from the system jvm.
 *
 * @author Johann Sorel (Geomatys)
 */
public class JVMBean {

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

    public List<String> getSystemProperties(){
        return Arrays.asList(SYSTEM_KEYS);
    }

    public Map<Object, Object> getSystemMap(){
        return System.getProperties();
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
