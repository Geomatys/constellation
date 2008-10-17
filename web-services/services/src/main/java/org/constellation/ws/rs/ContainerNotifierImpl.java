/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.constellation.ws.rs;

import com.sun.jersey.spi.container.ContainerListener;
import com.sun.jersey.spi.container.ContainerNotifier;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author guilhem
 */
public class ContainerNotifierImpl implements ContainerNotifier {
    
    private List<ContainerListener> cls;

    public ContainerNotifierImpl() {
        cls = new ArrayList<ContainerListener>();
    }
    
    public void addListener(ContainerListener arg0) {
        cls.add(arg0);
    }
    
    public void reload() {
        for ( ContainerListener cl : cls) {
            cl.onReload();
        }
    }

}
