/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.constellation.sos.ws;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Order {
    public int order();
}
