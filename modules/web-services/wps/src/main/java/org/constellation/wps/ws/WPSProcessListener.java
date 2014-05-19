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

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.constellation.ServiceDef;
import org.constellation.wps.utils.WPSUtils;
import org.constellation.ws.CstlServiceException;

import org.geotoolkit.ows.xml.v110.ExceptionReport;
import org.geotoolkit.process.ProcessEvent;
import org.geotoolkit.process.ProcessListener;
import org.geotoolkit.util.Exceptions;
import org.geotoolkit.util.StringUtilities;
import org.apache.sis.util.logging.Logging;
import org.geotoolkit.wps.converters.WPSConvertersUtils;
import org.geotoolkit.wps.xml.v100.*;

import org.opengis.parameter.GeneralParameterDescriptor;
/**
 * ProcessListener implementation for WPS asynchronous process execution.
 *
 * @author Quentin Boileau (Geomatys).
 */
public class WPSProcessListener implements ProcessListener{

    private static final Logger LOGGER = Logging.getLogger(WPSProcessListener.class);
    private static final int TIMEOUT = 20000; //processing time step

    private final Execute request;
    private final ExecuteResponse responseDoc;
    private final String fileName;
    private final ServiceDef def;
    final Map<String, Object> parameters;
    private final String folderPath;
    private long nextTimestamp;
    private final boolean useStatus;

    /**
     *
     * @param request execute request
     * @param responseDoc ExecuteResponse base
     * @param fileName name of the file to update
     * @param def service def (use when exception occurs)
     */
    public WPSProcessListener(final Execute request, final ExecuteResponse responseDoc, final String fileName, final ServiceDef def,
            final Map<String, Object> parameters) {
        this.request = request;
        this.responseDoc = responseDoc;
        this.fileName = fileName;
        this.def = def;
        this.parameters = parameters;
        this.folderPath = (String) parameters.get(WPSConvertersUtils.OUT_STORAGE_DIR);
        this.nextTimestamp = System.currentTimeMillis() + TIMEOUT;
        this.useStatus = this.request.getResponseForm().getResponseDocument().isStatus();
    }
    
    @Override
    public void started(final ProcessEvent event) {
        LOGGER.log(Level.INFO, "Process {0} is started.", WPSUtils.buildProcessIdentifier(event.getSource().getDescriptor()));
        final StatusType status = new StatusType();
        final ProcessStartedType started = new ProcessStartedType();
        status.setCreationTime(WPSUtils.getCurrentXMLGregorianCalendar());
        started.setValue("Process " + request.getIdentifier().getValue() + " is started");
        started.setPercentCompleted(0);
        status.setProcessStarted(started);
        if (useStatus) {
            responseDoc.setStatus(status);
        }
        WPSUtils.storeResponse(responseDoc, folderPath, fileName);
    }

    @Override
    public void progressing(final ProcessEvent event) {
        if (useStatus) {
            final long currentTimestamp = System.currentTimeMillis();
            if (currentTimestamp >= (nextTimestamp)){
                //LOGGER.log(Level.INFO, "Process {0} is progressing : {1}.", new Object[]{WPSUtils.buildProcessIdentifier(event.getSource().getDescriptor()), event.getProgress()});
                nextTimestamp += TIMEOUT;
                final StatusType status = new StatusType();
                status.setCreationTime(WPSUtils.getCurrentXMLGregorianCalendar());
                final ProcessStartedType started = new ProcessStartedType();
                started.setValue("Process " + request.getIdentifier().getValue() + " is pending");
                started.setPercentCompleted((int) event.getProgress());
                status.setProcessStarted(started);
                responseDoc.setStatus(status);
                WPSUtils.storeResponse(responseDoc, folderPath, fileName);
            }
        }
    }

    @Override
    public void completed(final ProcessEvent event) {
        LOGGER.log(Level.INFO, "Process {0} is finished.", WPSUtils.buildProcessIdentifier(event.getSource().getDescriptor()));
        try {
            final List<GeneralParameterDescriptor> processOutputDesc = event.getSource().getDescriptor().getOutputDescriptor().descriptors();
            final ExecuteResponse.ProcessOutputs outputs = new ExecuteResponse.ProcessOutputs();
            WPSWorker.fillOutputsFromProcessResult(outputs, request.getResponseForm().getResponseDocument().getOutput(), processOutputDesc, event.getOutput(), parameters);
            final StatusType status = new StatusType();
            status.setCreationTime(WPSUtils.getCurrentXMLGregorianCalendar());
            status.setProcessSucceeded("Process completed.");
            responseDoc.setStatus(status);
            responseDoc.setProcessOutputs(outputs);
            WPSUtils.storeResponse(responseDoc, folderPath, fileName);
        } catch (CstlServiceException ex) {
            writeException(ex);
        }
               
    }

    @Override
    public void failed(final ProcessEvent event) {
        LOGGER.log(Level.WARNING, "Process "+WPSUtils.buildProcessIdentifier(event.getSource().getDescriptor())+" has failed.", event.getException());
        final StatusType status = new StatusType();
        status.setCreationTime(WPSUtils.getCurrentXMLGregorianCalendar());
        final ProcessFailedType processFT = new ProcessFailedType();
        if (event.getException() != null) {
            processFT.setExceptionReport(new ExceptionReport(Exceptions.formatStackTrace(event.getException()), null, null, this.def.exceptionVersion.toString()));
        } else {
            processFT.setExceptionReport(new ExceptionReport("Process failed for some unknown reason.", null, null, this.def.exceptionVersion.toString()));
        }
        status.setProcessFailed(processFT);
        responseDoc.setStatus(status);
        WPSUtils.storeResponse(responseDoc, folderPath, fileName);
    }

    @Override
    public void paused(final ProcessEvent event) {
        if (useStatus) {
            LOGGER.log(Level.INFO, "Process {0} is paused.", WPSUtils.buildProcessIdentifier(event.getSource().getDescriptor()));
            final StatusType status = new StatusType();
            final ProcessStartedType paused = new ProcessStartedType();
            status.setCreationTime(WPSUtils.getCurrentXMLGregorianCalendar());
            paused.setValue("Process " + request.getIdentifier().getValue() + " is paused");
            paused.setPercentCompleted((int) event.getProgress());
            status.setProcessPaused(paused);
            responseDoc.setStatus(status);
            WPSUtils.storeResponse(responseDoc, folderPath, fileName);
        }
    }

    @Override
    public void resumed(final ProcessEvent event) {
        if (useStatus) {
            LOGGER.log(Level.INFO, "Process {0} is resumed.", WPSUtils.buildProcessIdentifier(event.getSource().getDescriptor()));
            final StatusType status = new StatusType();
            final ProcessStartedType resumed = new ProcessStartedType();
            status.setCreationTime(WPSUtils.getCurrentXMLGregorianCalendar());
            resumed.setValue("Process " + request.getIdentifier().getValue() + " is resumed");
            resumed.setPercentCompleted((int) event.getProgress());
            status.setProcessPaused(resumed);
            responseDoc.setStatus(status);
            WPSUtils.storeResponse(responseDoc, folderPath, fileName);
        }
    }
    
    /**
     * Write the occurred exception in the response file.
     * 
     * @param ex exception
     */
    private void writeException(final CstlServiceException ex){

        final String codeRepresentation;
        if (ex.getExceptionCode() instanceof org.constellation.ws.ExceptionCode) {
            codeRepresentation = StringUtilities.transformCodeName(ex.getExceptionCode().name());
        } else {
            codeRepresentation = ex.getExceptionCode().name();
        }
        
        final ExceptionReport report = new ExceptionReport(ex.getMessage(), codeRepresentation, ex.getLocator(),
                                                     def.exceptionVersion.toString());
        
        WPSUtils.storeResponse(report, folderPath, fileName);
    }
    
}
