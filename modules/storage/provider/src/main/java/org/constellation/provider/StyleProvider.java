/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
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
package org.constellation.provider;

import org.geotoolkit.style.MutableStyle;

/**
 *
 *
 * @author Johann Sorel (Geomatys)
 */
public interface StyleProvider extends Provider<String,MutableStyle> {

    /**
     * Create or replace an existing style.
     *
     * @param key : key used for this style
     * @param style : the style definition
     */
    void set(final String key, final MutableStyle style);

    /**
     * Change name of a Style.
     */
    void rename(final String key, final String newName);

}
