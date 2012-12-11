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
