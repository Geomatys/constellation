/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 * (C) 2007, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package net.seagis.metadata;

import java.util.Collection;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import net.seagis.catalog.Entry;
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.constraint.Constraints;
import org.opengis.metadata.distribution.Format;
import org.opengis.metadata.identification.AggregateInformation;
import org.opengis.metadata.identification.BrowseGraphic;
import org.opengis.metadata.identification.Identification;
import org.opengis.metadata.identification.Keywords;
import org.opengis.metadata.identification.Progress;
import org.opengis.metadata.identification.Usage;
import org.opengis.metadata.maintenance.MaintenanceInformation;
import org.opengis.util.InternationalString;

/**
 *
 * @author legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Identification")
public class IdentificationEntry extends Entry implements Identification{

    public Citation getCitation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public InternationalString getAbstract() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public InternationalString getPurpose() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<String> getCredits() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<Progress> getStatus() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<? extends ResponsibleParty> getPointOfContacts() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<? extends MaintenanceInformation> getResourceMaintenance() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<? extends BrowseGraphic> getGraphicOverviews() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<? extends Format> getResourceFormat() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<? extends Keywords> getDescriptiveKeywords() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<? extends Usage> getResourceSpecificUsages() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<? extends Constraints> getResourceConstraints() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<? extends AggregateInformation> getAggregationInfo() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
