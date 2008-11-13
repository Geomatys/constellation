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
package org.constellation.security.ws.soap;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import org.constellation.security.PEPWorker;


/**
 *
 * @version $Id$
 * @author Cédric Briançon (Geomatys)
 */
@WebService(name = "PEP")
@SOAPBinding(parameterStyle = ParameterStyle.BARE)
public class PEPService {
    /**
     * The PEP worker.
     */
    private final PEPWorker worker;

    public PEPService() {
        worker = new PEPWorker();
    }

    @WebMethod(action="getCapabilities")
    public void getCapabilities() {
        try {
            worker.getCapabilities();
        } catch (IOException ex) {
            Logger.getLogger(PEPService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
