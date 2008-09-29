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
package org.constellation.worker;

import java.io.File;

import org.constellation.coverage.web.WebServiceException;
import org.constellation.portrayal.CSTLPortrayalService;
import org.constellation.query.wms.GetMap;

import org.geotools.display.exception.PortrayalException;


/**
 *
 * @version $Id$
 * @author Johann Sorel (Geomatys)
 */
public class WMSWorker{

    private final CSTLPortrayalService service = new CSTLPortrayalService();
    private final GetMap query;
    private final File output;


    public WMSWorker(GetMap query, File output){
        if(query == null || output == null){
            throw new NullPointerException("Query and outpur file can not be null");
        }
        this.query = query;
        this.output = output;
    }

    public File getMap() throws PortrayalException, WebServiceException {
        service.portray(query, output);
        return output;
    }
}
