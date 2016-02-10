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
package org.constellation.admin.process;


import org.constellation.process.AbstractCstlProcess;
import org.geotoolkit.process.ProcessDescriptor;
import org.opengis.parameter.ParameterValueGroup;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import org.constellation.ServiceDef;
import org.constellation.client.ConstellationClient;
import org.geotoolkit.client.AbstractClientFactory;

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
    private final String wsInstance;
    private final String wsType;
    

    /**
     * Default Constructor
     */
    public Restart(final ProcessDescriptor desc, final ParameterValueGroup parameter) {
        super(desc, parameter);
        
        CSTL_CONFIG   = inputParameters.groups(RestartDescriptor.CSTL_DESCRIPTOR_GROUP.getName().getCode()).get(0);
        wsInstance   = CSTL_CONFIG.parameter(RestartDescriptor.CSTL_WS_INSTANCE.getName().getCode()).stringValue();
        wsType       = CSTL_CONFIG.parameter(RestartDescriptor.CSTL_WS_TYPE.getName().getCode()).stringValue();
        
    }

    private ConstellationClient createConstellationClient() throws MalformedURLException, IOException {
        final URL url = (URL) CSTL_CONFIG.parameter(AbstractClientFactory.URL.getName().getCode()).getValue();
        final String login  = CSTL_CONFIG.parameter(RefreshIndexDescriptor.USER.getName().getCode()).stringValue();
        final String passw  = CSTL_CONFIG.parameter(RefreshIndexDescriptor.PASSWORD.getName().getCode()).stringValue();
        final String version = "1";
        return new ConstellationClient(url.toString(), version).authenticate(login, passw);
    }

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
     * Verify constellation accessibility
     */
    private boolean constellationRunning() {
        try {
            final ConstellationClient cstlClient = createConstellationClient();
            return cstlClient != null;
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Error while login to cstl server", ex);
            return false;
        }
    }
    
    private void restartService() {
        try {
            final ConstellationClient cstl = createConstellationClient();
            cstl.servicesApi.restart(ServiceDef.Specification.valueOf(wsType), wsInstance, true);

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
