/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.constellation.menu.system;

import org.constellation.bean.AbstractMenuItem;


/**
 *
 * @author jsorel
 */
public class JVMItem extends AbstractMenuItem{

    public JVMItem() {
        super(null,
            new Path(SYSTEMS_PATH,"JVM", "/org/constellation/menu/system/jvm.xhtml", null)
            );
    }

}
