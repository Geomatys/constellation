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
public class JAIItem extends AbstractMenuItem{

    public JAIItem() {
        super(null,
            new Path(SYSTEMS_PATH,"JAI", "/org/constellation/menu/system/jai.xhtml", null)
            );
    }

}
