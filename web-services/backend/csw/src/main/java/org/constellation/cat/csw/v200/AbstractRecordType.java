/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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
package net.seagis.cat.csw.v200;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import net.seagis.cat.csw.AbstractRecord;


/**
 * <p>Java class for AbstractRecordType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AbstractRecordType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractRecordType")
@XmlSeeAlso({
    BriefRecordType.class,
    SummaryRecordType.class,
    DCMIRecordType.class
})
public abstract class AbstractRecordType implements AbstractRecord {
    
    @XmlTransient
    protected static net.seagis.ows.v100.ObjectFactory owsFactory = new net.seagis.ows.v100.ObjectFactory();
    
    @XmlTransient
    protected static net.seagis.dublincore.v1.elements.ObjectFactory dublinFactory = new net.seagis.dublincore.v1.elements.ObjectFactory();
    
    @XmlTransient
    protected static net.seagis.dublincore.v1.terms.ObjectFactory dublinTermFactory = new net.seagis.dublincore.v1.terms.ObjectFactory();


}
