/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.seagis.ogc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import org.opengis.filter.PropertyIsNotEqualTo;

/**
 *
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PropertyIsNotEqualTo")
public class PropertyIsNotEqualToType  extends BinaryComparisonOpType implements PropertyIsNotEqualTo {

    /**
     * Empty constructor used by JAXB
     */
    PropertyIsNotEqualToType() {
        
    }
    
    /**
     * Build a new Binary comparison operator
     */
    public PropertyIsNotEqualToType(LiteralType literal, PropertyNameType propertyName, Boolean matchCase) {
        super(literal, propertyName, matchCase);
    }
}
