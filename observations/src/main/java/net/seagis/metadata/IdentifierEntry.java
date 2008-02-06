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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import net.seagis.catalog.Entry;
import org.opengis.metadata.Identifier;
import org.opengis.metadata.citation.Citation;

/**
 *
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Identifier")
public class IdentifierEntry extends Entry implements Identifier{

    public String getCode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Citation getAuthority() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getVersion() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
