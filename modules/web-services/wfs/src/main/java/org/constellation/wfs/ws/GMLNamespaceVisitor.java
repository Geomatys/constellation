/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.constellation.wfs.ws;

import org.geotoolkit.filter.visitor.DuplicatingFilterVisitor;
import org.opengis.filter.expression.PropertyName;

/**
 * temporary hack for GML 3.2
 *                
 * @author Guilhem Legal (Geomatys)
 */
public class GMLNamespaceVisitor extends DuplicatingFilterVisitor{

    @Override
    public Object visit(final PropertyName expression, final Object extraData) {
        if (expression.getPropertyName().indexOf("http://www.opengis.net/gml/3.2")   != -1 ||
            expression.getPropertyName().indexOf("http://www.opengis.net/gml/3.2.1") != -1) {
            String newPropertyName = expression.getPropertyName().replace("http://www.opengis.net/gml/3.2", "http://www.opengis.net/gml");
            newPropertyName = newPropertyName.replace("http://www.opengis.net/gml/3.2.1", "http://www.opengis.net/gml");
            return getFactory(extraData).property(newPropertyName);
        }
        return super.visit(expression, extraData);
    }
}
