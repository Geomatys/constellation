/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
package org.constellation.process;

import org.constellation.process.service.GetConfigMapServiceTest;
import org.constellation.wfs.ws.DefaultWFSWorker;

/**
 *
 * @author Quentin Boileau (Geomatys)
 */
public class GetConfigWFSServiceTest extends GetConfigMapServiceTest {

    public GetConfigWFSServiceTest() {
        super("WFS", DefaultWFSWorker.class);
    }
}
