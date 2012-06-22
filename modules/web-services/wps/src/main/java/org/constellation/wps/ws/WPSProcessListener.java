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

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.constellation.ServiceDef;
import org.constellation.wps.utils.WPSUtils;
import org.constellation.ws.CstlServiceException;

import org.geotoolkit.ows.xml.v110.ExceptionReport;
import org.geotoolkit.process.ProcessEvent;
import org.geotoolkit.process.ProcessListener;
import org.geotoolkit.util.StringUtilities;
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
    private final String folderPath;
    private String fileName;
    private ServiceDef def;
    private String serviceURL;
    private long nextTimestamp;
    private static final int TIMEOUT = 20000;
    
    public WPSProcessListener(final Execute request, final ExecuteResponse responseDoc, final String fileName, final ServiceDef def,
            final String serviceURL, final String folderPath) {
        this.request = request;
        this.responseDoc = responseDoc;
        this.fileName = fileName;
        this.def = def;
        this.serviceURL = serviceURL;
        this.nextTimestamp = System.currentTimeMillis() + TIMEOUT;
        this.folderPath = folderPath;
    }
    
    @Override
    public void started(final ProcessEvent event) {
        LOGGER.log(Level.INFO, "Process {0} is started.", WPSUtils.buildProcessIdentifier(event.getSource().getDescriptor()));
        final StatusType status = new StatusType();
        final ProcessStartedType started = new ProcessStartedType();
        status.setCreationTime(getCurrentXMLGregorianCalendar());
        started.setValue("Process " + request.getIdentifier().getValue() + " is started");
        started.setPercentCompleted(0);
        status.setProcessStarted(started);
        responseDoc.setStatus(status);
        WPSUtils.storeResponse(responseDoc, folderPath, fileName);
    }

    @Override
    public void progressing(final ProcessEvent event) {
        final long currentTimestamp = System.currentTimeMillis();
        if (currentTimestamp >= (nextTimestamp)){
            //LOGGER.log(Level.INFO, "Process {0} is progressing : {1}.", new Object[]{WPSUtils.buildProcessIdentifier(event.getSource().getDescriptor()), event.getProgress()});
            nextTimestamp += TIMEOUT;
            final StatusType status = new StatusType();
            status.setCreationTime(getCurrentXMLGregorianCalendar());
            final ProcessStartedType started = new ProcessStartedType();
            started.setValue("Process " + request.getIdentifier().getValue() + " is pending");
            started.setPercentCompleted((int) event.getProgress());
            status.setProcessStarted(started);
            responseDoc.setStatus(status);
            WPSUtils.storeResponse(responseDoc, folderPath, fileName);
        }
    }

    @Override
    public void completed(final ProcessEvent event) {
        LOGGER.log(Level.INFO, "Process {0} is finished.", WPSUtils.buildProcessIdentifier(event.getSource().getDescriptor()));
        try {
            final List<GeneralParameterDescriptor> processOutputDesc = event.getSource().getDescriptor().getOutputDescriptor().descriptors();
            final ExecuteResponse.ProcessOutputs outputs = new ExecuteResponse.ProcessOutputs();
            WPSWorker.fillOutputsFromProcessResult(outputs, request.getResponseForm().getResponseDocument().getOutput(), processOutputDesc, event.getOutput(), serviceURL, folderPath);
            final StatusType status = new StatusType();
            status.setCreationTime(getCurrentXMLGregorianCalendar());
            status.setProcessSucceeded("Process complet.");
            
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
        status.setCreationTime(getCurrentXMLGregorianCalendar());
        final ProcessFailedType processFT = new ProcessFailedType();
        processFT.setExceptionReport(new ExceptionReport(event.getException().getMessage(), null, null, null));
        status.setProcessFailed(processFT);
        responseDoc.setStatus(status);
        WPSUtils.storeResponse(responseDoc, folderPath, fileName);
    }
    
    /**
     * Write the occured exception in the response file.
     * 
     * @param ex 
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
    
    /**
     * @return the current time in an XMLGregorianCalendar.
     */
    private static XMLGregorianCalendar getCurrentXMLGregorianCalendar(){
        XMLGregorianCalendar xcal = null;
        try {
            final GregorianCalendar c = new GregorianCalendar();
            c.setTime(new Date());
            xcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        } catch (DatatypeConfigurationException ex) {
            LOGGER.log(Level.INFO, "Can't create the creation time of the status.");
        }
        return xcal;
    }
}
