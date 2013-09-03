/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2013, Geomatys
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

/**
 * Provider type list :
 * <ul>
 *     <li>{@linkplain #VECTOR}</li>
 *     <li>{@linkplain #RASTER}</li>
 *     <li>{@linkplain #SENSOR}</li>
 *     <li>{@linkplain #PYRAMID_DATA}</li>
 *     <li>{@linkplain #VIRTUAL_SENSOR}</li>
 *     <li>{@linkplain #STYLE}</li>
 * </ul>
 *
 * @author Benjamin Garcia (Geomatys)
 * @since 0.9
 * @version 0.9
 */
public enum ProviderType {
    /**
     * vector provider
     */
    VECTOR,

    /**
     * raster provider
     */
    RASTER,

    /**
     * sensor provider
     */
    SENSOR,

    /**
     * raster pyramid provider
     */
    PYRAMID_DATA,

    /**
     * virtual sensor provider
     */
    VIRTUAL_SENSOR,

    /**
     * style provider
     */
    STYLE
}
