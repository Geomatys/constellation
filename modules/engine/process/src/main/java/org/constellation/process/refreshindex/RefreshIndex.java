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
package org.constellation.process.refreshindex;


import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import org.constellation.admin.service.ConstellationServer;
import org.constellation.process.AbstractCstlProcess;


import org.opengis.parameter.ParameterValueGroup;

import static org.geotoolkit.io.X364.*;
import static org.constellation.process.refreshindex.RefreshIndexDescriptor.*;
import org.geotoolkit.process.ProcessDescriptor;

/**
 * @author Guilhem Legal (Geomatys)
 */
public class RefreshIndex extends AbstractCstlProcess {

    
    private static final AtomicBoolean ACTIVE = new AtomicBoolean(false);
    private final ParameterValueGroup CSTL_CONFIG;
    private final String CSW_INSTANCE;
    private final boolean ASYNCHRONOUS;
    

    /**
     * Default Constructor
     */
    public RefreshIndex(final ProcessDescriptor desc, final ParameterValueGroup parameter) {
        super(desc, parameter);
        
        CSTL_CONFIG   = inputParameters.groups(CSTL_DESCRIPTOR_GROUP.getName().getCode()).get(0);
        CSW_INSTANCE  = CSTL_CONFIG.parameter(CSTL_CSW_INSTANCE.getName().getCode()).stringValue();
        ASYNCHRONOUS  = CSTL_CONFIG.parameter(CSTL_ASYNCHRONOUS.getName().getCode()).booleanValue();
        
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
            console("\n",BOLD,"Refresh index(",new Date(),")",RESET,"\n");
            refreshIndex();
        } finally {
            console(BOLD, "Refresh index performed", RESET, "\n");
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
    
    
    private void refreshIndex() {
        try {
            final ConstellationServer cstl = createConstellationClient();
            cstl.csws.refreshIndex(CSW_INSTANCE, ASYNCHRONOUS);
            cstl.services.restartInstance("CSW", CSW_INSTANCE);

            console(FOREGROUND_GREEN, "OK", FOREGROUND_DEFAULT, "\n");
        } catch (MalformedURLException ex) {
            console(FOREGROUND_RED, "FAILED(Invalid URL)", FOREGROUND_DEFAULT, "\n");
            log(ex, "Refresh index ...... FAILED(Invalid URL)");
        } catch (IOException ex) {
            console(FOREGROUND_RED, "FAILED", FOREGROUND_DEFAULT, "\n");
            log(ex, "Refresh index ...... FAILED");
        }
    }
        
}
