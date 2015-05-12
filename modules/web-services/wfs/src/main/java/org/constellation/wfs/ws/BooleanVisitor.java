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

import org.geotoolkit.feature.type.AttributeDescriptor;
import org.geotoolkit.feature.type.FeatureType;
import org.geotoolkit.filter.visitor.DuplicatingFilterVisitor;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsNotEqualTo;
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
                    final Object obj = property.evaluate(ft);
                    if (obj instanceof AttributeDescriptor) {
                        final AttributeDescriptor descriptor = (AttributeDescriptor) obj;
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

    @Override
    public Object visit(final PropertyIsNotEqualTo filter, final Object extraData) {
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
