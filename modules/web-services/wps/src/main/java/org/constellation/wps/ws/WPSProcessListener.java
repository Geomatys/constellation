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

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.constellation.wps.utils.WPSUtils;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.ows.xml.v110.ExceptionReport;
import org.geotoolkit.process.ProcessEvent;
import org.geotoolkit.process.ProcessListener;
import org.geotoolkit.util.logging.Logging;
import org.geotoolkit.wps.xml.v100.*;
import org.opengis.parameter.GeneralParameterDescriptor;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public class WPSProcessListener implements ProcessListener{

    private static final Logger LOGGER = Logging.getLogger(WPSProcessListener.class);
    
    private Execute request;
    private ExecuteResponse responseDoc;
    private String fileName;
    private StatusType status;
    boolean useStatus;
    
    public WPSProcessListener(final Execute request, final ExecuteResponse responseDoc, final String fileName) {
        this.request = request;
        this.responseDoc = responseDoc;
        this.fileName = fileName;
        this.status = responseDoc.getStatus();
        this.useStatus = request.getResponseForm().getResponseDocument().isStatus();
    }
    
    @Override
    public void started(ProcessEvent event) {
        LOGGER.log(Level.INFO, "Process {0} is started.", WPSUtils.buildProcessIdentifier(event.getSource().getDescriptor()));
        final ProcessStartedType started = new ProcessStartedType();
        started.setValue("Process " + request.getIdentifier().getValue() + " is started");
        started.setPercentCompleted(0);
        status.setProcessStarted(started);
        updateDocument();
    }

    @Override
    public void progressing(ProcessEvent event) {
        LOGGER.log(Level.INFO, "Process {0} is progressing : " + event.getProgress() + ".", WPSUtils.buildProcessIdentifier(event.getSource().getDescriptor()));
        status.getProcessStarted().setPercentCompleted((int) event.getProgress());
        updateDocument();
    }

    @Override
    public void completed(ProcessEvent event) {
        LOGGER.log(Level.INFO, "Process {0} is finished.", WPSUtils.buildProcessIdentifier(event.getSource().getDescriptor()));
        try {
            final List<GeneralParameterDescriptor> processOutputDesc = event.getSource().getDescriptor().getOutputDescriptor().descriptors();
            final ExecuteResponse.ProcessOutputs outputs = new ExecuteResponse.ProcessOutputs();
            WPSWorker.fillOutputsFromProcessResult(outputs, request.getResponseForm().getResponseDocument().getOutput(), processOutputDesc, event.getOutput());
            status.setProcessSucceeded("Process complet.");
            
            updateDocument();
        } catch (CstlServiceException ex) {
            Logger.getLogger(WPSProcessListener.class.getName()).log(Level.SEVERE, null, ex);
            writeException(ex);
            //TODO handle exception
        }
               
    }

    @Override
    public void failed(ProcessEvent event) {
        LOGGER.log(Level.INFO, "Process {0} has failed.", WPSUtils.buildProcessIdentifier(event.getSource().getDescriptor()));
        final ProcessFailedType processFT = new ProcessFailedType();
        processFT.setExceptionReport(new ExceptionReport(event.getException().getMessage(), null, null, null));
         status.setProcessFailed(processFT);
         updateDocument();
    }
    
    
    private void updateDocument(){
        WPSUtils.storeResponseDocument(responseDoc, fileName);
        
    }
    
    private void writeException(Exception ex){
        
    }
}
