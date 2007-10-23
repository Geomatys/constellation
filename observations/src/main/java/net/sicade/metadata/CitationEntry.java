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

package net.sicade.metadata;



import java.util.Collection;
import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import net.sicade.catalog.Entry;
import org.opengis.metadata.Identifier;
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.citation.CitationDate;
import org.opengis.metadata.citation.PresentationForm;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.citation.Series;
import org.opengis.util.InternationalString;
/**
 *
 * @author legal
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Citation")
public class CitationEntry extends Entry implements Citation{

    @Override
    public InternationalString getTitle() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<? extends InternationalString> getAlternateTitles() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<? extends CitationDate> getDates() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public InternationalString getEdition() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Date getEditionDate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<? extends Identifier> getIdentifiers() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<String> getIdentifierTypes() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<? extends ResponsibleParty> getCitedResponsibleParties() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<PresentationForm> getPresentationForm() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Series getSeries() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public InternationalString getOtherCitationDetails() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public InternationalString getCollectiveTitle() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getISBN() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getISSN() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
