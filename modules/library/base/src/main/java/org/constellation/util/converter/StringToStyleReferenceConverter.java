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
package org.constellation.util.converter;

import org.apache.sis.util.UnconvertibleObjectException;
import org.constellation.util.StyleReference;
import org.geotoolkit.util.converter.SimpleConverter;

/**
 * @author Quentin Boileau (Geomatys)
 */
public class StringToStyleReferenceConverter extends SimpleConverter<String, StyleReference> {

    @Override
    public Class<String> getSourceClass() {
            return String.class;
    }

    @Override
    public Class<StyleReference> getTargetClass() {
            return StyleReference.class;
    }

    @Override
    public StyleReference apply(String str) throws UnconvertibleObjectException {
        try {
            return new StyleReference(str);
        } catch (IllegalArgumentException e) {
            throw new UnconvertibleObjectException(e.getMessage(), e);
        }
    }
}
