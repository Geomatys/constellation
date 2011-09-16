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

package org.constellation.bean;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.imageio.spi.ServiceRegistry;
import javax.servlet.ServletContext;
import org.apache.commons.io.IOUtils;
import org.constellation.util.ReflectionUtilities;
import org.geotoolkit.util.logging.Logging;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public final class MenuItems {

    private static final Logger LOGGER = Logging.getLogger(MenuItems.class);
    
    private static final List<MenuItem> PAGES =  new ArrayList<MenuItem>();

    private MenuItems() {}
    
    public static void deployPages(final Object ctx) {

        /**
         * the method getRealPath is available in PortletContext and ServletContext
         * in order to avoid a class cast exception we use reflection to get the method
         */
        final Method m = ReflectionUtilities.getMethod("getRealPath", ctx.getClass(), String.class);
        if (m != null) {
            final String webapp = (String) ReflectionUtilities.invokeMethod(m, ctx, "/");
            final File webappFolder = new File(webapp);


            final Iterator<MenuItem> ite = ServiceRegistry.lookupProviders(MenuItem.class);
            while(ite.hasNext()){
                final MenuItem page = ite.next();
                PAGES.add(page);

                // copy each file in the web app folder preserving the path
                for(String path : page.getPages()){
                    final String[] parts = path.split("/");
                    File parent = webappFolder;
                    for(int i=0;i<parts.length-1;i++){
                        parent = new File(parent,parts[i]);
                        //ensure it is removed when application stops
                        parent.deleteOnExit();
                        parent.mkdirs();
                    }

                    final File target = new File(parent,parts[parts.length-1]);
                    copy(MenuItems.class.getResourceAsStream(path), target);
                }
            }
        } else {
           LOGGER.log(Level.WARNING, "There is no method getRealPath on: {0}", ctx.getClass().getName());
        }
    }

    

    public static List<MenuItem> getPages() {
        return PAGES;
    }

    private static void copy(final InputStream in, final File f){
        if (in != null) {
            OutputStream out = null;
            try {
                out = new FileOutputStream(f);
                IOUtils.copy(in, out);
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } finally {
                try {
                    in.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                if(out != null){
                    try {
                        out.close();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }
    }

}
