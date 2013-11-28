/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009-2012, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
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
