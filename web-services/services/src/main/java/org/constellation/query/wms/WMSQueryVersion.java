/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
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
package org.constellation.query.wms;

import org.constellation.query.QueryVersion;


/**
 * Stores the version number available for this webservice.
 *
 * @version $Id$
 * @author Cédric Briançon
 */
public class WMSQueryVersion extends QueryVersion {
    /**
     * Key for the WMS service in version {@code 1.1.1}.
     */
    public static final WMSQueryVersion WMS_1_1_1 = new WMSQueryVersion("1.1.1");

    /**
     * Key for the WMS service in version {@code 1.3.0}.
     */
    public static final WMSQueryVersion WMS_1_3_0 = new WMSQueryVersion("1.3.0");
    
    private WMSQueryVersion(final String key) {
        super(key);
    }
}
