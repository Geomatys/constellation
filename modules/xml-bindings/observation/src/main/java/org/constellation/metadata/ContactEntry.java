/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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
package org.constellation.metadata;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import org.constellation.catalog.Entry;
import org.opengis.metadata.citation.Address;
import org.opengis.metadata.citation.Contact;
import org.opengis.metadata.citation.OnLineResource;
import org.opengis.metadata.citation.Telephone;
import org.opengis.util.InternationalString;

/**
 *
 * @author legal
 */
@XmlAccessorType(value = XmlAccessType.FIELD)
@XmlType(name = "Contact")
public class ContactEntry extends Entry implements Contact {

    private Telephone phone;
    private Address address;
    private OnLineResource onLineResource;
    private InternationalStringEntry hoursOfService;
    private InternationalStringEntry contactInstructions;

    public Telephone getPhone() {
        return phone;
    }

    public Address getAddress() {
        return address;
    }

    public OnLineResource getOnLineResource() {
        return onLineResource;
    }

    public InternationalString getHoursOfService() {
        return hoursOfService;
    }

    public InternationalString getContactInstructions() {
        return contactInstructions;
    }
}
