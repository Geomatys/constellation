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

package org.constellation.generic.nerc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <xsd:complexType name="codeTableTypeType">
 *     <xsd:sequence>
 *             <xsd:element name="listKey" type="xsd:string" />
 *             <xsd:element name="listLongName" type="xsd:string" />
 *             <xsd:element name="listShortName" type="xsd:string" />
 *             <xsd:element name="listDefinition" type="xsd:string" />
 *             <xsd:element name="listVersion" type="xsd:int" />
 *             <xsd:element name="listLastMod" type="xsd:dateTime" />
 *     </xsd:sequence>
 * </xsd:complexType>
 *  
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "CodeTableType")
public class CodeTableType {
    
    private String listKey;
    
    private String listLongName;
    
    private String listShortName;
    
    private String listDefinition;
    
    private String listVersion;
    
    private String listLastMod;

    public String getListKey() {
        return listKey;
    }

    public void setListKey(String listKey) {
        this.listKey = listKey;
    }

    public String getListLongName() {
        return listLongName;
    }

    public void setListLongName(String listLongName) {
        this.listLongName = listLongName;
    }

    public String getListShortName() {
        return listShortName;
    }

    public void setListShortName(String listShortName) {
        this.listShortName = listShortName;
    }

    public String getListDefinition() {
        return listDefinition;
    }

    public void setListDefinition(String listDefinition) {
        this.listDefinition = listDefinition;
    }

    public String getListVersion() {
        return listVersion;
    }

    public void setListVersion(String listVersion) {
        this.listVersion = listVersion;
    }

    public String getListLastMod() {
        return listLastMod;
    }

    public void setListLastMod(String listLastMod) {
        this.listLastMod = listLastMod;
    }

}
