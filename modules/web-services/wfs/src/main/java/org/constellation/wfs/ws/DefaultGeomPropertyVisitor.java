/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
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

import org.geotoolkit.filter.DefaultLiteral;
import org.geotoolkit.filter.binaryspatial.LooseBBox;
import org.geotoolkit.filter.binaryspatial.UnreprojectedLooseBBox;
import org.geotoolkit.filter.visitor.DuplicatingFilterVisitor;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.spatial.BBOX;
import org.opengis.geometry.BoundingBox;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class DefaultGeomPropertyVisitor extends DuplicatingFilterVisitor{

    private final FeatureType ft;

    public DefaultGeomPropertyVisitor(final FeatureType ft) {
        this.ft = ft;
    }

    @Override
    public Object visit(BBOX filter, Object extraData) {

        Expression exp1 = visit(filter.getExpression1(),extraData);
        if (exp1 instanceof PropertyName) {
            PropertyName pname = (PropertyName) exp1;
            if (pname.getPropertyName().trim().isEmpty()) {
                exp1 = ff.property(ft.getGeometryDescriptor().getName());
            }
        }
        final Expression exp2 = filter.getExpression2();
        if(!(exp2 instanceof Literal)) {
            //this value is supposed to hold a BoundingBox
            throw new IllegalArgumentException("Illegal BBOX filter, "
                    + "second expression should have been a literal with a boundingBox value: \n" + filter);
        } else {
            final Literal l = (Literal)visit(exp2,extraData);
            final Object obj = l.getValue();
            if(obj instanceof BoundingBox){
                if (filter instanceof UnreprojectedLooseBBox) {
                    return new UnreprojectedLooseBBox((PropertyName)exp1, new DefaultLiteral<>((BoundingBox) obj));
                } else if (filter instanceof LooseBBox) {
                    return new LooseBBox((PropertyName)exp1, new DefaultLiteral<>((BoundingBox) obj));
                } else {
                    return getFactory(extraData).bbox(exp1, (BoundingBox) obj);
                }
            }else{
                throw new IllegalArgumentException("Illegal BBOX filter, "
                    + "second expression should have been a literal with a boundingBox value but value was a : \n" + obj.getClass());
            }
        }
    }

    
}
