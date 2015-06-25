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
package org.constellation.wps.ws;

import org.apache.sis.util.logging.Logging;
import org.constellation.ws.CstlServiceException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.geotoolkit.ows.xml.OWSExceptionCode.NO_APPLICABLE_CODE;
import org.geotoolkit.processing.quartz.ProcessJobDetail;
/**
 * 
 * @author Quentin Boileau (Geomatys)
 */
public class WPSScheduler {

    private static final Logger LOGGER  = Logging.getLogger(WPSScheduler.class);
    
    public static WPSScheduler INSTANCE;
    
    private Scheduler quartzScheduler;
    
    private WPSScheduler() throws CstlServiceException {
        final SchedulerFactory schedFact = new StdSchedulerFactory();
        try {
            quartzScheduler = schedFact.getScheduler();
            quartzScheduler.start();
        } catch (SchedulerException ex) {
            throw new CstlServiceException(" Failed to start WPS quartz scheduler", ex, NO_APPLICABLE_CODE);
        }
    }
    
    public static synchronized WPSScheduler getInstance() throws CstlServiceException{
        if(INSTANCE == null){
            INSTANCE = new WPSScheduler();
        }
        return INSTANCE;
    }
    
    public void addProcessJob(final org.geotoolkit.process.Process process) throws CstlServiceException{
        try {
            final ProcessJobDetail job = new ProcessJobDetail(process);
            final Trigger trigger = TriggerBuilder.newTrigger().forJob(job).startNow().build();
            quartzScheduler.scheduleJob(job, trigger);
        }catch(SchedulerException ex){
            throw new CstlServiceException(ex);
        }
    }
    
    public void stop() {        
        try {
            quartzScheduler.shutdown();
        } catch (SchedulerException ex) {
            LOGGER.log(Level.SEVERE, "Failed to stop WPS quartz scheduler");
            return;
        }
        LOGGER.log(Level.WARNING, "WPS Scheduler sucessfully stopped");    
    }
}
    