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
package net.seagis.catalog;

import java.util.UUID;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlTransient;
import org.geotools.util.Utilities;


/**
 * Base class of {@linkplain Element element} created from a record in a
 * {@linkplain Table table}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Guilhem Legal
 */
@XmlTransient
public class Entry implements Element, Serializable {
    /**
     * For cross-platform compatibility.
     */
    private static final long serialVersionUID = -7119518186999674633L;

    /**
     * Name of this entry. In some case this name may be created on the fly when first needed.
     *
     * @see #createName
     */
    @XmlTransient
    private String name;

    /**
     * Remarks for this entry, of {@code null}.
     */
    @XmlTransient
    private final String remarks;

    /**
     * Empty constructor used by JAXB.
     */
    protected Entry() {
        remarks = null;
    }

    /**
     * Creates an entry for the specified name without remarks. If a {@code null} name
     * is specified, then a name will be {@linkplain #createName generated} on the fly
     * when first needed.
     *
     * @param name The element name, or {@code null} if none.
     */
    protected Entry(final String name) {
        this(name, null);
    }

    /**
     * Creates an entry for the specified name and remarks. If a {@code null} name
     * is specified, then a name will be {@linkplain #createName generated} on the
     * fly when first needed.
     *
     * @param name The element name, or {@code null} if none.
     * @param remarks The remarks, or {@code null} if none.
     */
    protected Entry(final String name, final String remarks) {
        this.name = (name!=null) ? name.trim() : null;
        this.remarks = remarks;
    }

    /**
     * Creates a name of the fly. This method is invoked when first needed if the entry
     * has been created with a {@code null} name.
     *
     * @return A name generated on the fly.
     */
    protected String createName() {
        return UUID.randomUUID().toString();
    }

    /**
     * Returns the name for this entry.
     *
     * @return The name for this entry. Should never be {@code null}.
     */
    public String getName() {
        if (name == null) {
            name = createName();
        }
        return name;
    }

    /**
     * Returns the remarks for this entry, or {@code null} if none.
     *
     * @return Remarks for this entry, or {@code null}.
     */
    public String getRemarks() {
        return remarks;
    }

    /**
     * Returns a string representation of the entry. This string may be used in graphical user
     * interface, for example a <cite>Swing</cite> {@link javax.swing.JTree}. The default
     * implementation returns the entry {@linkplain #getName name}.
     */
    @Override
    public String toString() {
        return String.valueOf(getName());
    }

    /**
     * Returns a hash code value for this entry. The default implementation computes the hash
     * code from the {@linkplain #getName nom}. Because entry name should be unique, this is
     * often suffisient.
     */
    @Override
    public int hashCode() {
        return getName().hashCode() ^ (int)serialVersionUID;
    }

    /**
     * Compares this entry with the specified object for equality. The default implementation
     * compares the {@linkplain #getClass class}, {@linkplain #getName name} and {@linkplain
     * #getRemarks remarks}. If should be suffisient when every entries have a unique name, for
     * example when the name is the primary key in a database table. Subclasses should compare
     * other attributes as a safety when affordable, but should avoid any comparaison that may
     * force the loading of a large amount of data.
     *
     * @param  object The object to compare with this entry.
     * @return {@code true} if the given object is equals to this entry.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final Entry that = (Entry) object;
            return Utilities.equals(this.getName(),    that.getName()) &&
                   Utilities.equals(this.getRemarks(), that.getRemarks());
        }
        return false;
    }
}
