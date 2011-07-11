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

package org.constellation.sos.io;

import java.sql.Timestamp;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class ObservationResult {

    public String resultID;
    public Timestamp beginTime;
    public Timestamp endTime;

    public ObservationResult(String resultID, Timestamp beginTime, Timestamp endTime) {
        this.beginTime = beginTime;
        this.endTime   = endTime;
        this.resultID  = resultID;
    }
}
