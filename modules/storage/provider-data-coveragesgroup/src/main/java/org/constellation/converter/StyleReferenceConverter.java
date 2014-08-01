/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.converter;

import org.apache.sis.util.UnconvertibleObjectException;
import org.geotoolkit.util.converter.SimpleConverter;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class StyleReferenceConverter extends SimpleConverter<org.constellation.util.StyleReference, org.constellation.provider.coveragesgroup.xml.StyleReference> {

    @Override
    public Class<org.constellation.util.StyleReference> getSourceClass() {
        return org.constellation.util.StyleReference.class;
    }

    @Override
    public Class<org.constellation.provider.coveragesgroup.xml.StyleReference> getTargetClass() {
        return org.constellation.provider.coveragesgroup.xml.StyleReference.class;
    }

    @Override
    public org.constellation.provider.coveragesgroup.xml.StyleReference apply(org.constellation.util.StyleReference object) throws UnconvertibleObjectException {
        if (object != null) {
            return new org.constellation.provider.coveragesgroup.xml.StyleReference(object.toString());
        }
        return null;
    }
    
}
