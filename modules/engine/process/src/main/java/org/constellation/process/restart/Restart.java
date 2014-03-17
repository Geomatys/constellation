/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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
package org.constellation.process.restart;


import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import org.constellation.admin.service.ConstellationServer;
import org.constellation.process.AbstractCstlProcess;

import org.opengis.parameter.ParameterValueGroup;

import static org.geotoolkit.io.X364.*;
import static org.constellation.process.restart.RestartDescriptor.*;
import org.geotoolkit.process.ProcessDescriptor;

/**
 * @author Guilhem Legal (Geomatys)
 */
public class Restart extends AbstractCstlProcess {

    
    private static final AtomicBoolean ACTIVE = new AtomicBoolean(false);
    private final ParameterValueGroup CSTL_CONFIG;
    private final String WS_INSTANCE;
    private final String WS_TYPE;
    

    /**
     * Default Constructor
     */
    public Restart(final ProcessDescriptor desc, final ParameterValueGroup parameter) {
        super(desc, parameter);
        
        CSTL_CONFIG   = inputParameters.groups(CSTL_DESCRIPTOR_GROUP.getName().getCode()).get(0);
        WS_INSTANCE   = CSTL_CONFIG.parameter(CSTL_WS_INSTANCE.getName().getCode()).stringValue();
        WS_TYPE       = CSTL_CONFIG.parameter(CSTL_WS_TYPE.getName().getCode()).stringValue();
        
    }

    //// parametres ////////////////////////////////////////////////////////////
    
    private ConstellationServer createConstellationClient() throws MalformedURLException {
        return new ConstellationServer(CSTL_CONFIG);
    }

    // traitement //////////////////////////////////////////////////////////////
    
    @Override
    public void execute() {
        
        //jobs can be called concurently, so they may overlaps each other
        //we avoid it with this flag
        if (!ACTIVE.compareAndSet(false, true)) {
            //we skip this run
            return;
        }

        //verify if constellation is runing
        if (!constellationRunning()) {
            console(FOREGROUND_RED,"constellation unreachable FAILED",FOREGROUND_DEFAULT,"\n");
            ACTIVE.set(false);
            return;
        }
        
        try{
            console("\n",BOLD,"Restart service(",new Date(),")",RESET,"\n");
            restartService();
        } finally {
            console(BOLD, "Restart performed", RESET, "\n");
            ACTIVE.set(false);
        }

        console("\n");
        return;
    }

    /**
     * Verify constellation accesibility
     */
    private boolean constellationRunning() {
        final ConstellationServer cstlServer = ConstellationServer.login(CSTL_CONFIG);
        return cstlServer != null;
    }
    
    
    private void restartService() {
        try {
            final ConstellationServer cstl = createConstellationClient();
            cstl.services.restartInstance(WS_TYPE, WS_INSTANCE);

            console(FOREGROUND_GREEN, "OK", FOREGROUND_DEFAULT, "\n");
        } catch (MalformedURLException ex) {
            console(FOREGROUND_RED, "FAILED(Invalid URL)", FOREGROUND_DEFAULT, "\n");
            log(ex, "Restart ...... FAILED(Invalid URL)");
        } catch (IOException ex) {
            console(FOREGROUND_RED, "FAILED", FOREGROUND_DEFAULT, "\n");
            log(ex, "Restart ...... FAILED");
        }
    }
        
}
