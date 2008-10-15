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
package org.constellation.catalog;

import java.io.Serializable;
import org.geotools.util.Utilities;


/**
 * Base class for {@linkplain Element element} implementations that delegate their work to an
 * other element.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class Proxy implements Element, Serializable {
    /**
     * For cross version compatibility.
     */
    private static final long serialVersionUID = 6984331646382641188L;

    /**
     * Creates a new proxy.
     */
    public Proxy() {
    }

    /**
     * Returns the wrapped element.
     */
    public abstract Element getBackingElement();

    /**
     * Returns the name of this element. The default implementation delegates to
     * the {@linkplain #getBackingElement backing element}.
     */
    public String getName() {
        return getBackingElement().getName();
    }

    /**
     * Returns remarks about this element, or {@code null} if none. The default
     * implementation delegates to the {@linkplain #getBackingElement backing element}.
     */
    public String getRemarks() {
        return getBackingElement().getRemarks();
    }

    /**
     * Returns a string representation of this element. The default implementation
     * returns the {@linkplain #getName name}.
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * Returns a hash code value for this element. The default implementation derives a
     * code from the one computed by the {@linkplain #getBackingElement backing element}.
     */
    @Override
    public int hashCode() {
        return getBackingElement().hashCode() ^ (int)serialVersionUID;
    }

    /**
     * Compares this element with the specified object for equality. The default implementation
     * returns {@code true} if the specified object is the same {@code Proxy} subclass and uses
     * an equals {@linkplain #getBackingElement backing element}.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final Proxy that = (Proxy) object;
            return Utilities.equals(this.getBackingElement(), that.getBackingElement());
        }
        return false;
    }
}
