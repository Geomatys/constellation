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
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class BooleanVisitor extends DuplicatingFilterVisitor {

    private final FeatureType ft;

    public BooleanVisitor(final FeatureType ft) {
        this.ft = ft;
    }

    @Override
    public Object visit(final PropertyIsEqualTo filter, final Object extraData) {
        final Expression exp1 = filter.getExpression1();
        final Expression exp2 = filter.getExpression2();
        if (exp1 instanceof PropertyName) {
            final PropertyName property = (PropertyName) exp1;
            if (exp2 instanceof Literal) {
                final Literal literal = (Literal) exp2;

                // Add a support for a filter on boolean property using integer 0 or 1
                if (ft != null) {
                    final AttributeDescriptor descriptor = (AttributeDescriptor) property.evaluate(ft);
                    if (descriptor != null) {
                        if (descriptor.getType().getBinding().equals(Boolean.class) && literal.getValue() instanceof Number) {
                            final Literal booleanLit;
                            if (literal.getValue().equals(1.0)) {
                                booleanLit = getFactory(extraData).literal(true);
                            } else {
                                booleanLit = getFactory(extraData).literal(false);
                            }
                            return getFactory(extraData).equals(exp1, booleanLit);
                        }
                    }
                }
            }
        }
        return filter;
    }


}
