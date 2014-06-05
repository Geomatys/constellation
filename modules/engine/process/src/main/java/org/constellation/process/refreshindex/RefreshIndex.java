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
package org.constellation.process.refreshindex;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.constellation.ServiceDef;
import org.constellation.admin.service.ConstellationClient;
import org.constellation.admin.service.ConstellationServerFactory;
import org.constellation.process.AbstractCstlProcess;
import static org.constellation.process.refreshindex.RefreshIndexDescriptor.*;
import static org.geotoolkit.io.X364.*;
import org.geotoolkit.process.ProcessDescriptor;
import org.opengis.parameter.ParameterValueGroup;

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
    
    private ConstellationClient createConstellationClient() throws MalformedURLException {
        final URL url = (URL) CSTL_CONFIG.parameter(ConstellationServerFactory.URL.getName().getCode()).getValue();
        final String version = "1";
        return new ConstellationClient(url.toString(), version);
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
        try {
            final String login  = CSTL_CONFIG.parameter(ConstellationServerFactory.USER.getName().getCode()).stringValue();
            final String passw  = CSTL_CONFIG.parameter(ConstellationServerFactory.PASSWORD.getName().getCode()).stringValue();
            final ConstellationClient cstlClient = createConstellationClient().auth(login, passw);
            return cstlClient != null;
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.WARNING, "Error while login to cstl server", ex);
            return false;
        }
    }
    
    
    private void refreshIndex() {
        try {
            final ConstellationClient cstl = createConstellationClient();
            cstl.csw.refreshIndex(CSW_INSTANCE, ASYNCHRONOUS, false);
            cstl.services.restart(ServiceDef.Specification.CSW, CSW_INSTANCE, false);

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
