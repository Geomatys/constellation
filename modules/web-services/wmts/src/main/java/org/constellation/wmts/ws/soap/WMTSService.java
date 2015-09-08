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
package org.constellation.wmts.ws.soap;

import org.constellation.ServiceDef.Specification;
import org.constellation.map.configuration.MapConfigurer;
import org.constellation.wmts.ws.DefaultWMTSWorker;
import org.constellation.wmts.ws.WMTSWorker;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.ExceptionCode;
import org.constellation.ws.ServiceConfigurer;
import org.constellation.ws.soap.OGCWebService;
import org.geotoolkit.wmts.xml.v100.BinaryPayload;
import org.geotoolkit.wmts.xml.v100.Capabilities;
import org.geotoolkit.wmts.xml.v100.GetCapabilities;
import org.geotoolkit.wmts.xml.v100.GetFeatureInfo;
import org.geotoolkit.wmts.xml.v100.GetTile;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.BindingType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;

/**
 * The SOAP facade to an OGC Web Map Tile Service, implementing the 1.0.0 version.
 * <p>
 * This SOAP service is not runned in that implementation because the working part is
 * not written.
 * </p>
 *
 * @version $Id$
 *
 * @author Cédric Briançon (Geomatys)
 */
@WebService(name = "WMTSService")
@XmlSeeAlso({org.apache.sis.internal.jaxb.geometry.ObjectFactory.class})
@SOAPBinding(parameterStyle = ParameterStyle.BARE)
@BindingType(value="http://java.sun.com/xml/ns/jaxws/2003/05/soap/bindings/HTTP/")
public class WMTSService extends OGCWebService<WMTSWorker>{

    /**
     * Creates a WMTS SOAP service.
     */
    public WMTSService() {
       super(Specification.WMTS);
       LOGGER.log(Level.INFO, "WMTS SOAP service running ({0} instances)", getWorkerMapSize());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected WMTSWorker createWorker(final String id) {
        return new DefaultWMTSWorker(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class getWorkerClass() {
        return DefaultWMTSWorker.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<? extends ServiceConfigurer> getConfigurerClass() {
        return MapConfigurer.class;
    }

    /**
     * Web service operation describing the service and its capabilities.
     *
     * @param requestCapabilities A document specifying the section you would obtain like :
     *      ServiceIdentification, ServiceProvider, Contents, operationMetadata.
     *
     * @throws SOAPServiceException
     */
    @WebMethod(action="getCapabilities")
    @WebResult(name="Capabilities", targetNamespace="http://www.opengis.net/wmts/1.0.0")
    public Capabilities getCapabilities(@WebParam(name = "GetCapabilities") GetCapabilities requestCapabilities)
                                                                                     throws SOAPServiceException
    {
        try {
            LOGGER.info("received SOAP getCapabilities request");
            final WMTSWorker worker = getCurrentWorker();
            worker.setServiceUrl(getServiceURL());
            return worker.getCapabilities(requestCapabilities);

        } catch (CstlServiceException ex) {
            throw new SOAPServiceException(ex.getMessage(), ex.getExceptionCode().name(),
                                           requestCapabilities.getVersion().toString());
        }
    }

    /**
     * Web service operation giving the value of a precise point in an image.
     *
     * @param requestFeatureInfo The request to execute.
     *
     * @throws SOAPServiceException
     */
    @WebMethod(action="getFeatureInfo")
    public String getFeatureInfo(@WebParam(name = "GetFeatureInfo") GetFeatureInfo requestFeatureInfo)
                                                                           throws SOAPServiceException
    {
        try {
            LOGGER.info("received SOAP getFeatureInfo request");
            final WMTSWorker worker = getCurrentWorker();
            worker.setServiceUrl(getServiceURL());
            final Map.Entry<String, Object> result = worker.getFeatureInfo(requestFeatureInfo);
            if (result != null && result.getValue() != null) {
                return result.getValue().toString();
            } else {
                LOGGER.warning("Empty FeatureInfo for request request"+requestFeatureInfo);
                return null;
            }

        } catch (CstlServiceException ex) {
            throw new SOAPServiceException(ex.getMessage(), ex.getExceptionCode().name(),
                                           requestFeatureInfo.getVersion());
        }
    }

    /**
     * Web service operation returning the image chosen.
     *
     * @param requestTile The request to execute.
     *
     * @throws SOAPServiceException
     */
    @WebMethod(action="getTile")
    public BinaryPayload getTile(@WebParam(name = "GetTile") GetTile requestTile)
                                                      throws SOAPServiceException
    {
        LOGGER.info("received SOAP getTile request");
        try {
            final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            /*
            final RenderedImage buffered = worker.getTile(requestTile);
            final String mimeType = ImageIOUtilities.fileExtensionToMimeType(requestTile.getFormat());
            ImageIOUtilities.writeImage(buffered, mimeType, byteOut);*/
            final BinaryPayload binaryPayLoad = new BinaryPayload();
            binaryPayLoad.setBinaryContent(byteOut.toByteArray());
            binaryPayLoad.setFormat(requestTile.getFormat());
            byteOut.close();
            return binaryPayLoad;
        /*} catch (CstlServiceException ex) {
            throw new SOAPServiceException(ex.getMessage(), ex.getExceptionCode().name(),
                                           requestTile.getVersion());*/
        } catch (IOException ex) {
            throw new SOAPServiceException(ex.getMessage(), ExceptionCode.NO_APPLICABLE_CODE.name(), requestTile.getVersion());
        }
    }

    @Override
    protected Object treatIncomingRequest(Object objectRequest, WMTSWorker worker) throws CstlServiceException {
        throw new UnsupportedOperationException("TODO."); 
    }

    @Override
    protected SOAPMessage processExceptionResponse(String message, String code, String locator) {
        throw new UnsupportedOperationException("TODO."); 
    }
}
