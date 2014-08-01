/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.constellation.converter;

import org.apache.sis.util.UnconvertibleObjectException;
import org.geotoolkit.util.converter.SimpleConverter;

/**
 *
 * @author guilhem
 */
public class StyleReferenceConverter2 extends SimpleConverter<org.constellation.provider.coveragesgroup.xml.StyleReference, org.constellation.util.StyleReference> {

    @Override
    public Class<org.constellation.util.StyleReference> getTargetClass() {
        return org.constellation.util.StyleReference.class;
    }

    @Override
    public Class<org.constellation.provider.coveragesgroup.xml.StyleReference> getSourceClass() {
        return org.constellation.provider.coveragesgroup.xml.StyleReference.class;
    }

    @Override
    public org.constellation.util.StyleReference apply(org.constellation.provider.coveragesgroup.xml.StyleReference object) throws UnconvertibleObjectException {
        if (object != null) {
            return new org.constellation.util.StyleReference(object.getValue());
        }
        return null;
    }
    
}
