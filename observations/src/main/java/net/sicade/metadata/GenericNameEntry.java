/*
 * Sicade - Systemes integrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le D�veloppement
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

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import net.sicade.catalog.Entry;
import org.opengis.util.GenericName;
import org.opengis.util.InternationalString;
import org.opengis.util.LocalName;
import org.opengis.util.NameSpace;
import org.opengis.util.ScopedName;

/**
 *
 * @author legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GenericName")
public class GenericNameEntry extends Entry implements GenericName {

    @Override
    public NameSpace scope() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public GenericName getScope() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int depth() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<LocalName> getParsedNames() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public LocalName asLocalName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public LocalName name() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ScopedName asScopedName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public GenericName toFullyQualifiedName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ScopedName push(GenericName arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public InternationalString toInternationalString() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int compareTo(Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
