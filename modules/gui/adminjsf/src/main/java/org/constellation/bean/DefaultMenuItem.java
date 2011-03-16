/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.constellation.bean;

/**
 *
 * @author jsorel
 */
public class DefaultMenuItem extends AbstractMenuItem{

    public DefaultMenuItem() {
        super("/org/mdweb/edition/formsearch/bundle",
            new Path(null,"search", "/org/mdweb/edition/formsearch/page.xhtml", null)
            );
    }

}
