/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.process.client;


import org.constellation.admin.service.ConstellationServer;
import org.constellation.process.AbstractCstlProcess;
import org.geotoolkit.process.ProcessDescriptor;
import org.opengis.parameter.ParameterValueGroup;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.constellation.process.client.RestartDescriptor.CSTL_DESCRIPTOR_GROUP;
import static org.constellation.process.client.RestartDescriptor.CSTL_WS_INSTANCE;
import static org.constellation.process.client.RestartDescriptor.CSTL_WS_TYPE;
import static org.geotoolkit.io.X364.BOLD;
import static org.geotoolkit.io.X364.FOREGROUND_DEFAULT;
import static org.geotoolkit.io.X364.FOREGROUND_GREEN;
import static org.geotoolkit.io.X364.FOREGROUND_RED;
import static org.geotoolkit.io.X364.RESET;

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
