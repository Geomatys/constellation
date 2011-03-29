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


package org.constellation.wfs.ws.rs;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

/**
 *
 *
 * @author Guilhem Legal (Geomatys)
 */
public class JAXBEventHandler implements ValidationEventHandler {

    public int level = -1;

    @Override
    public boolean handleEvent(final ValidationEvent ve) {
        if (ve.getSeverity() == ve.FATAL_ERROR || ve.getSeverity() == ve.ERROR) {
            if (ve.getMessage() != null && ve.getMessage().startsWith("unexpected element")) {
                level = ve.getSeverity();
                return false;
            }
        }
        return true;
    }
}
