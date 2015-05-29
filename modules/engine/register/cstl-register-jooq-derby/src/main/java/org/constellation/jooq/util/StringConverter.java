
package org.constellation.jooq.util;

import org.jooq.Converter;

/**
 *
 * @author guilhem
 */
public class StringConverter implements Converter {

    @Override
    public Object from(Object t) {
        return t;
    }

    @Override
    public Object to(Object u) {
        return u;
    }

    @Override
    public Class fromType() {
        return String.class;
    }

    @Override
    public Class toType() {
        return String.class;
    }
    
}
