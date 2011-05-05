/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2009, Geomatys
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
package org.constellation.configuration.ws.rs;

// J2SE dependencies
import javax.ws.rs.core.Context;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.logging.Level;

// Jersey dependencies
import javax.annotation.PreDestroy;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.sun.jersey.spi.resource.Singleton;

// JAXB dependencies
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

// Constellation dependencies
import org.constellation.ws.rs.WebService;
import org.constellation.configuration.AbstractConfigurer;
import org.constellation.ServiceDef;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.factory.AbstractConfigurerFactory;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.StyleProviderProxy;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.MimeType;
import org.constellation.ws.rs.ContainerNotifierImpl;
import org.constellation.generic.database.BDD;
        
// Geotoolkit dependencies
import org.constellation.ws.ExceptionCode;
import org.geotoolkit.factory.FactoryRegistry;
import org.geotoolkit.factory.FactoryNotFoundException;
import org.geotoolkit.lucene.index.AbstractIndexer;
import org.geotoolkit.util.StringUtilities;
import org.geotoolkit.xml.MarshallerPool;
import org.geotoolkit.ows.xml.OWSExceptionCode;
import org.geotoolkit.ows.xml.v110.ExceptionReport;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

/**
 * Web service for administration and configuration operations.
 * <p>
 * This web service enables basic remote management of a Constellation server. 
 * </p>
 * 
 * @author Guilhem Legal (Geomatys)
 * @since 0.1
 */
@Path("configuration")
@Singleton
public final class ConfigurationService extends WebService  {

    /**
     * A container notifier allowing to dynamically reload all the active service.
     */
    @Context
    protected volatile ContainerNotifierImpl cn;
    
    /**
     * The implementation specific configurer. TODO multiple
     */
    private AbstractConfigurer configurer;

    /**
     * The factory registry allowing to load the correct implementation specific configurer.
     */
    private static FactoryRegistry factory = new FactoryRegistry(AbstractConfigurerFactory.class);


    /**
     * Construct the ConfigurationService and configure its context.
     */
    public ConfigurationService() {
        super();
        try {
            final MarshallerPool pool = new MarshallerPool("org.geotoolkit.ows.xml.v110:org.constellation.configuration:org.geotoolkit.skos.xml:org.geotoolkit.internal.jaxb.geometry");
            setXMLContext(pool);
            final AbstractConfigurerFactory configurerfactory = factory.getServiceProvider(AbstractConfigurerFactory.class, null, null, null);
            configurer      = configurerfactory.getConfigurer(cn);
            
        } catch (JAXBException ex) {
            LOGGER.log(Level.WARNING, "JAXBException while setting the JAXB context for configuration service: " + ex.getMessage(), ex);
            
        } catch (ConfigurationException ex) {
            LOGGER.log(Level.WARNING, "Specific operation will not be available.\nCause:{0}", ex.getMessage());
            
        } catch (FactoryNotFoundException ex) {
            LOGGER.warning("Factory not found for Configurer, specific operation will not be available.");
            
        }
        LOGGER.info("Configuration service runing");
    }
    
    /**
     * Handle the various types of requests made to the service.
     */
    @Override
    public Response treatIncomingRequest(final Object objectRequest) {
        Marshaller marshaller = null;
        try {
            marshaller = getMarshallerPool().acquireMarshaller();
            String request  = "";
            final StringWriter sw = new StringWriter();

            if (configurer != null) {
                configurer.setContainerNotifier(cn);
            }
            
            if (objectRequest == null) {
                request = (String) getParameter("REQUEST", true);
            }

            if ("Restart".equalsIgnoreCase(request)) {
                final boolean force = Boolean.parseBoolean(getParameter("FORCED", false));
                marshaller.marshal(restartService(force), sw);
                return Response.ok(sw.toString(), MimeType.TEXT_XML).build();
            }
            
            else if ("Download".equalsIgnoreCase(request)) {    
                final File f = downloadFile();
                return Response.ok(f, MediaType.MULTIPART_FORM_DATA_TYPE).build(); 
            }
            
            
            /* CSW specific operations */
            
            else {
                if (configurer != null) {
                    final Object response = configurer.treatRequest(request, getUriContext().getQueryParameters());
                    if (response != null) {
                        marshaller.marshal(response, sw);
                        return Response.ok(sw.toString(), MimeType.TEXT_XML).build();
                    }
                }
            }
            
            throw new CstlServiceException("The operation " + request + " is not supported by the service",
                                                 OPERATION_NOT_SUPPORTED, Parameters.REQUEST);
            

        } catch (JAXBException ex) {
            LOGGER.log(Level.WARNING, "Error while marshalling the configuration service response", ex);
            return Response.ok("<error>JAXB Exception</error>", MimeType.TEXT_XML).build();

        } catch (CstlServiceException ex) {
            final String code = StringUtilities.transformCodeName(ex.getExceptionCode().name());
            final ExceptionReport report = new ExceptionReport(ex.getMessage(), code, ex.getLocator(),
                                                               ServiceDef.CONFIG.exceptionVersion.toString());
            if (!ex.getExceptionCode().equals(MISSING_PARAMETER_VALUE) && !ex.getExceptionCode().equals(ExceptionCode.MISSING_PARAMETER_VALUE) &&
                !ex.getExceptionCode().equals(VERSION_NEGOTIATION_FAILED) &&
                !ex.getExceptionCode().equals(OPERATION_NOT_SUPPORTED)) {
                LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            } else {
                LOGGER.info(ex.getMessage());
            }
            return Response.ok(report, MimeType.TEXT_XML).build();
            
        } finally {
            if (marshaller != null) {
                getMarshallerPool().release(marshaller);
            }
        }
        
    }

    /**
     * Build a service ExceptionReport
     *
     * @param message
     * @param codeName
     * @return
     */
    @Override
    protected Response launchException(final String message, final String codeName, final String locator) {
        final OWSExceptionCode code = OWSExceptionCode.valueOf(codeName);
        final ExceptionReport report = new ExceptionReport(message, code.name(), locator,
                                                       ServiceDef.CONFIG.exceptionVersion.toString());
        return Response.ok(report, MimeType.TEXT_XML).build();
    }

    /**
     * Restart all the web-services, reload the providers.
     * If some services are currently indexing, the service will not restart
     * unless you specified the flag "forced".
     * 
     * @return an Acknowledgment if the restart succeed.
     */
    private AcknowlegementType restartService(final boolean forced) {
        LOGGER.info("\n restart requested \n");
        // clear cache
        StyleProviderProxy.getInstance().dispose();
        LayerProviderProxy.getInstance().dispose();

        if (cn != null) {
            if (!configurer.isLock()) {
                BDD.clearConnectionPool();
                cn.reload();
                return new AcknowlegementType(Parameters.SUCCESS, "services succefully restarted");
            } else if (!forced) {
                return new AcknowlegementType("failed", "There is an indexation running use the parameter FORCED=true to bypass it.");
            } else {
                AbstractIndexer.stopIndexation();
                BDD.clearConnectionPool();
                cn.reload();
                return new AcknowlegementType(Parameters.SUCCESS, "services succefully restarted (previous indexation was stopped)");
            }
        } else {
            return new AcknowlegementType("failed", "The services can not be restarted (ContainerNotifier is null)");
        }
        
    }

    /**
     * Receive a file and write it into the static file path.
     * 
     * @param in The input stream.
     * @return an Acknowledgment indicating if the operation succeed or not.
     *
     * @todo Not implemented. This is just a placeholder where we can customize the
     *       download action for some users. Will probably be removed in a future version.
     */
    @PUT
    public AcknowlegementType uploadFile(final InputStream in) {
        LOGGER.info("uploading");
        try  {
            final String layer = getParameter("layer", false);
            LOGGER.log(Level.INFO, "LAYER= {0}", layer);
            // TODO: implement upload action here.
            in.close();
        } catch (CstlServiceException ex) {
            //must never happen in normal case
            LOGGER.severe("Webservice exception while get the layer parameter");
            return new AcknowlegementType("failed", "Webservice exception while get the layer parameter");
        } catch (IOException ex) {
            LOGGER.severe("IO exception while uploading file");
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            return new AcknowlegementType("failed", "IO exception while performing upload");
        }
        return new AcknowlegementType(Parameters.SUCCESS, "the file has been successfully uploaded");
    }
    
    /**
     * Return a static file present on the server.
     * 
     * @return a file.
     *
     * @todo Not implemented. This is just a placeholder where we can customize the
     *       download action for some users. Will probably be removed in a future version.
     */
    private File downloadFile() throws CstlServiceException {
        throw new CstlServiceException("Download operation not implemented", OPERATION_NOT_SUPPORTED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreDestroy
    public void destroy() {
        super.destroy();
        LOGGER.info("Shutting down the REST Configuration service facade. Disposing " +
                "all datastore instances.");
        if (configurer != null) {
            configurer.destroy();
        }
        /**
        try {
            final StyleProviderProxy spp = StyleProviderProxy.getInstance(false);
            if (spp != null) {
                spp.dispose();
            }
            final LayerProviderProxy lpp = LayerProviderProxy.getInstance(false);
            if (lpp != null) {
                lpp.dispose();
            }
        } catch (ExceptionInInitializerError ex) {
            // Factory Registery cannot found MutableStyleFactory instance.
            // shutdown is a bit late for looking for this factory.
            // @TODO avoid this above block if StyleProviderProxy has never been initialized.
            LOGGER.fine(ex.toString());
        }**/
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected MarshallerPool getConfigurationPool() {
        return GenericDatabaseMarshallerPool.getInstance();
    }
}
