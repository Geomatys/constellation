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

import org.geotoolkit.filter.visitor.DuplicatingFilterVisitor;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.expression.PropertyName;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class UnprefixerFilterVisitor extends DuplicatingFilterVisitor{

    private final FeatureType ft;

    public UnprefixerFilterVisitor(final FeatureType ft) {
        this.ft = ft;
    }

    @Override
    public Object visit(final PropertyName expression, final Object extraData) {
        final String prefix = ft.getName().toString();
        if (expression.getPropertyName().startsWith(prefix)) {
            final String newPropertyName = expression.getPropertyName().substring(prefix.length());
            return getFactory(extraData).property(newPropertyName);
        }
        return super.visit(expression, extraData);
    }
}
