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

package org.constellation.wfs.ws;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.namespace.QName;
import org.geotoolkit.filter.visitor.DuplicatingFilterVisitor;
import org.opengis.filter.expression.PropertyName;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class AliasFilterVisitor extends DuplicatingFilterVisitor {

    private final Map<String, QName> aliases;
    
    public AliasFilterVisitor(final Map<String, QName> aliases) {
        if (aliases != null) {
            this.aliases = aliases;
        } else {
            this.aliases = new HashMap<>();
        }
    }
    
    @Override
    public Object visit(final PropertyName expression, final Object extraData) {
        for (Entry<String, QName> entry : aliases.entrySet()) {
            if (expression.getPropertyName().startsWith(entry.getKey() + "/")) {
                final String newPropertyName = '{' + entry.getValue().getNamespaceURI() + '}' + entry.getValue().getLocalPart() + expression.getPropertyName().substring(entry.getKey().length());
                return getFactory(extraData).property(newPropertyName);
            }
        }
        return super.visit(expression, extraData);
    }
}
