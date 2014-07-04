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

import org.geotoolkit.feature.type.FeatureType;
import org.geotoolkit.filter.DefaultLiteral;
import org.geotoolkit.filter.binaryspatial.LooseBBox;
import org.geotoolkit.filter.binaryspatial.UnreprojectedLooseBBox;
import org.geotoolkit.filter.visitor.DuplicatingFilterVisitor;
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
