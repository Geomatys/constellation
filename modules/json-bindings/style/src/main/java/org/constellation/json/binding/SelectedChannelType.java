/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2012, Geomatys
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

package org.constellation.json.binding;

import org.opengis.style.ContrastEnhancement;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import static org.constellation.json.util.StyleFactories.SF;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class SelectedChannelType implements StyleElement<org.opengis.style.SelectedChannelType> {

    private String name = "";

    public SelectedChannelType() {
    }

    public SelectedChannelType(final org.opengis.style.SelectedChannelType selectedChannelType) {
        ensureNonNull("selectedChannelType", selectedChannelType);
        this.name = selectedChannelType.getChannelName();
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public org.opengis.style.SelectedChannelType toType() {
        return SF.selectedChannelType(name, (ContrastEnhancement) null);
    }
}
