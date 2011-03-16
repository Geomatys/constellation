/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.constellation.bean;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.faces.context.FacesContext;
import javax.imageio.spi.ServiceRegistry;
import javax.servlet.ServletContext;
import org.apache.commons.io.IOUtils;
import org.constellation.bean.MenuItem.Path;
import org.geotoolkit.util.collection.UnmodifiableArrayList;

/**
 *
 * @author jsorel
 */
public final class MenuItems {

    private static final List<MenuItem> PAGES;

    static {

        //get the root path of the application
        final Object obj = FacesContext.getCurrentInstance().getExternalContext().getContext();
        final ServletContext ctx = (ServletContext) obj;
        final String webapp = ctx.getRealPath("/");
        final File webappFolder = new File(webapp);


        final Iterator<MenuItem> ite = ServiceRegistry.lookupProviders(MenuItem.class);
        final List<MenuItem> lst = new ArrayList<MenuItem>();
        while(ite.hasNext()){
            final MenuItem page = ite.next();
            lst.add(page);

            // copy each file in the web app folder preserving the path
            for(Path p : page.getPaths()){
                if(p.linkedPage != null){
                    final String[] parts = p.linkedPage.split("/");
                    File parent = webappFolder;
                    for(int i=0;i<parts.length-1;i++){
                        parent = new File(parent,parts[i]);
                        //ensure it is removed when application stops
                        parent.deleteOnExit();
                        parent.mkdirs();
                    }

                    final File target = new File(parent,parts[parts.length-1]);
                    copy(MenuItems.class.getResourceAsStream(p.linkedPage), target);
                }
            }
        }

        PAGES = UnmodifiableArrayList.wrap(lst.toArray(new MenuItem[lst.size()]));
    }

    private MenuItems() {}

    public static List<MenuItem> getPages() {
        return PAGES;
    }

    private static void copy(final InputStream in, File f){
        OutputStream out = null;
        try {
            out = new FileOutputStream(f);
            IOUtils.copy(in, out);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }finally{
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
