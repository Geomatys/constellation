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
import org.constellation.ws.rs.WebService;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Arrays;
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
import org.constellation.ServiceDef;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.exception.ConfigurationException;
import org.constellation.configuration.factory.AbstractConfigurerFactory;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.StyleProviderProxy;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.MimeType;
import org.constellation.ws.rs.ContainerNotifierImpl;
import org.constellation.generic.database.BDD;
        
// Geotoolkit dependencies
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
     * The implementation specific CSW configurer.
     */
    private AbstractCSWConfigurer cswConfigurer;

    /**
     * The factory registry allowing to load the correct implementation specific CSW configurer.
     */
    private static FactoryRegistry factory = new FactoryRegistry(AbstractConfigurerFactory.class);

    /**
     * A flag indicating if a CSW configuration have been found.
     */
    private boolean cswFunctionEnabled;

    /**
     * A flag indicating if an indexation is going on.
     */
    private static boolean indexing;

    /**
     * The list of service currently indexing.
     */
    private static final List<String> SERVICE_INDEXING = new ArrayList<String>();

    /**
     * Construct the ConfigurationService and configure its context.
     */
    public ConfigurationService() {
        super();
        indexing = false;
        try {
            final MarshallerPool pool = new MarshallerPool("org.geotoolkit.ows.xml.v110:org.constellation.configuration:org.geotoolkit.skos.xml:org.geotoolkit.internal.jaxb.geometry");
            setXMLContext(pool);
            final AbstractConfigurerFactory configurerfactory = factory.getServiceProvider(AbstractConfigurerFactory.class, null, null, null);
            cswConfigurer      = configurerfactory.getCSWConfigurer(cn);
            cswFunctionEnabled = true;
        } catch (JAXBException ex) {
            LOGGER.log(Level.WARNING, "JAXBException while setting the JAXB context for configuration service: " + ex.getMessage(), ex);
            cswFunctionEnabled = false;
        } catch (ConfigurationException ex) {
            LOGGER.log(Level.WARNING, "Specific CSW operation will not be available.\nCause:{0}", ex.getMessage());
            cswFunctionEnabled = false;
        } catch (FactoryNotFoundException ex) {
            LOGGER.warning("Factory not found for CSWConfigurer, specific CSW operation will not be available.");
            cswFunctionEnabled = false;
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

            if (cswConfigurer != null) {
                cswConfigurer.setContainerNotifier(cn);
            }
            
            if (objectRequest == null) {
                request = (String) getParameter("REQUEST", true);
            }

            if ("Restart".equalsIgnoreCase(request)) {
                final boolean force = Boolean.parseBoolean(getParameter("FORCED", false));
                marshaller.marshal(restartService(force), sw);
                return Response.ok(sw.toString(), MimeType.TEXT_XML).build();
            }
            
            if ("Download".equalsIgnoreCase(request)) {    
                final File f = downloadFile();
                return Response.ok(f, MediaType.MULTIPART_FORM_DATA_TYPE).build(); 
            }
            
            
            /* CSW specific operations */
            
            if ("RefreshIndex".equalsIgnoreCase(request)) {
                if (cswFunctionEnabled) {
                    final boolean asynchrone = Boolean.parseBoolean((String) getParameter("ASYNCHRONE", false));
                    final String id          = getParameter("ID", true);
                    final boolean forced     = Boolean.parseBoolean((String) getParameter("FORCED", false));

                    if (isIndexing(id) && !forced) {
                        final AcknowlegementType refused = new AcknowlegementType("Failure",
                                "An indexation is already started for this service:" + id);
                        marshaller.marshal(refused, sw);
                        return Response.ok(sw.toString(), "text/xml").build();
                    } else if (indexing && forced) {
                        AbstractIndexer.stopIndexation(Arrays.asList(id));
                    }
                    
                    startIndexation(id);
                    AcknowlegementType ack;
                    try {
                        ack = cswConfigurer.refreshIndex(asynchrone, id);
                    } finally {
                        endIndexation(id);
                    }
                    marshaller.marshal(ack, sw);
                    return Response.ok(sw.toString(), MimeType.TEXT_XML).build();
                } else {
                     throw new CstlServiceException("This specific CSW operation " + request + " is not activated",
                                                  OPERATION_NOT_SUPPORTED, Parameters.REQUEST);
                }
            }

            if ("AddToIndex".equalsIgnoreCase(request)) {
                if (cswFunctionEnabled) {
                    final String id                = getParameter("ID", true);
                    final List<String> identifiers = new ArrayList<String>();
                    final String identifierList    = getParameter("IDENTIFIERS", true);
                    final StringTokenizer tokens   = new StringTokenizer(identifierList, ",;");
                    while (tokens.hasMoreTokens()) {
                        final String token = tokens.nextToken().trim();
                        identifiers.add(token);
                    }

                    marshaller.marshal(cswConfigurer.addToIndex(id, identifiers), sw);
                    return Response.ok(sw.toString(), MimeType.TEXT_XML).build();
                } else {
                     throw new CstlServiceException("This specific CSW operation " + request + " is not activated",
                                                  OPERATION_NOT_SUPPORTED, Parameters.REQUEST);
                }
            }

            if ("stopIndex".equalsIgnoreCase(request)) {
                if (cswFunctionEnabled) {
                    //final String service     = getParameter("SERVICE", false);
                    final String id          = getParameter("ID", false);

                    final AcknowlegementType ack= stopIndexation(id);
                    marshaller.marshal(ack, sw);
                    return Response.ok(sw.toString(), "text/xml").build();
                } else {
                     throw new CstlServiceException("This specific CSW operation " + request + " is not activated",
                                                  OPERATION_NOT_SUPPORTED, "Request");
                }
            }
            
            if ("UpdateVocabularies".equalsIgnoreCase(request)) {    
                if (cswFunctionEnabled) {
                    return Response.ok(cswConfigurer.updateVocabularies(),MimeType.TEXT_XML).build();
                } else {
                     throw new CstlServiceException("This specific CSW operation " + request + " is not activated",
                                                  OPERATION_NOT_SUPPORTED, Parameters.REQUEST);
                }
            }
            
            if ("UpdateContacts".equalsIgnoreCase(request)) {    
                if (cswFunctionEnabled) {
                    return Response.ok(cswConfigurer.updateContacts(),MimeType.TEXT_XML).build();
                } else {
                     throw new CstlServiceException("This specific CSW operation " + request + " is not activated",
                                                  OPERATION_NOT_SUPPORTED, Parameters.REQUEST);
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
            if (!ex.getExceptionCode().equals(MISSING_PARAMETER_VALUE) &&
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
     * Return true if the select service (identified by his ID) is currently indexing (CSW).
     * @param id
     * @return
     */
    private boolean isIndexing(final String id) {
        return indexing && SERVICE_INDEXING.contains(id);
    }

    /**
     * Add the specified service to the indexing service list.
     * @param id
     */
    private void startIndexation(final String id) {
        indexing  = true;
        if (id != null) {
            SERVICE_INDEXING.add(id);
        }
    }

    /**
     * remove the selected service from the indexing service list.
     * @param id
     */
    private void endIndexation(final String id) {
        indexing = false;
        if (id != null) {
            SERVICE_INDEXING.remove(id);
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
            if (!indexing) {
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
     * Stop all the indexation going on.
     *
     * @return an Acknowledgment.
     */
    private AcknowlegementType stopIndexation(final String id) {
        LOGGER.info("\n stop indexation requested \n");
        if (!isIndexing(id)) {
            return new AcknowlegementType("Success", "There is no indexation to stop");
        } else {
            AbstractIndexer.stopIndexation(Arrays.asList(id));
            return new AcknowlegementType("Success", "The indexation have been stopped");
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
        if (cswConfigurer != null) {
            cswConfigurer.destroy();
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
