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

import java.util.Collection;
import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import org.constellation.catalog.Entry;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.maintenance.MaintenanceFrequency;
import org.opengis.metadata.maintenance.MaintenanceInformation;
import org.opengis.metadata.maintenance.ScopeCode;
import org.opengis.metadata.maintenance.ScopeDescription;
import org.opengis.temporal.PeriodDuration;
import org.opengis.util.InternationalString;

/**
 *
 * @author legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MaintenanceInformation")
public class MaintenanceInformationEntry extends Entry implements MaintenanceInformation{

    public MaintenanceFrequency getMaintenanceAndUpdateFrequency() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Date getDateOfNextUpdate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PeriodDuration getUserDefinedMaintenanceFrequency() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ScopeCode getUpdateScope() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ScopeDescription getUpdateScopeDescription() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<ScopeCode> getUpdateScopes() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<? extends ScopeDescription> getUpdateScopeDescriptions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public InternationalString getMaintenanceNote() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<InternationalString> getMaintenanceNotes() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<? extends ResponsibleParty> getContacts() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
