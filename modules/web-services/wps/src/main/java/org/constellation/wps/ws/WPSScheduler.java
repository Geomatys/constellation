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
package org.constellation.wps.ws;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.constellation.ws.CstlServiceException;
import static org.geotoolkit.ows.xml.OWSExceptionCode.NO_APPLICABLE_CODE;
import org.geotoolkit.process.quartz.ProcessJobDetail;
import org.apache.sis.util.logging.Logging;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
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
    