/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2011, Geomatys
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
import java.util.Arrays;
import java.io.FileInputStream;
import java.util.Properties;
import org.mdweb.io.auth.AuthenticationReader;
import org.constellation.configuration.ConfigDirectory;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Context;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

// Jersey dependencies
import javax.annotation.PreDestroy;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.sun.jersey.spi.resource.Singleton;

// JAXB dependencies
import java.lang.ref.WeakReference;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

// Constellation dependencies
import javax.xml.bind.Unmarshaller;
import org.constellation.ws.rs.WebService;
import org.constellation.configuration.AbstractConfigurer;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.ServiceReport;
import org.constellation.configuration.factory.AbstractConfigurerFactory;
import org.constellation.configuration.filter.ConfigurerFilter;
import org.constellation.configuration.ExceptionReport;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.MimeType;
import org.constellation.ws.rs.ContainerNotifierImpl;
import org.constellation.generic.database.BDD;
        
// Geotoolkit dependencies
import org.constellation.ws.ExceptionCode;
import org.geotoolkit.factory.FactoryRegistry;
import org.geotoolkit.factory.FactoryNotFoundException;
import org.geotoolkit.util.StringUtilities;
import org.geotoolkit.xml.MarshallerPool;
import org.geotoolkit.ows.xml.OWSExceptionCode;
import org.geotoolkit.util.FileUtilities;
import org.geotoolkit.internal.sql.DefaultDataSource;

import static org.constellation.api.QueryConstants.*;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

// Mdweb metamodel auth dependencies
import org.mdweb.model.auth.AuthenticationException;
import org.mdweb.io.auth.sql.DataSourceAuthenticationReader;
import org.mdweb.model.auth.UserAuthnInfo;

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

    private static WeakReference<ConfigurationService> INSTANCE = null;
    
    /**
     * A container notifier allowing to dynamically reload all the active service.
     */
    @Context
    protected volatile ContainerNotifierImpl cn;
    
    /**
     * The implementation specific configurers.
     */
    private final List<AbstractConfigurer> configurers = new ArrayList<AbstractConfigurer>();

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
            setXMLContext(GenericDatabaseMarshallerPool.getInstance());
            final Iterator<AbstractConfigurerFactory> ite = factory.getServiceProviders(AbstractConfigurerFactory.class, new ConfigurerFilter(), null, null);
            while (ite.hasNext()) {
                AbstractConfigurerFactory currentfactory = ite.next();
                try {
                    AbstractConfigurer ac = currentfactory.getConfigurer(cn);
                    LOGGER.log(Level.INFO, "Found a configurer:{0}", ac.getClass().getName());
                    configurers.add(ac);
                } catch (ConfigurationException ex) {
                    LOGGER.log(Level.WARNING, "Specific operation will not be available.\nCause:{0}", ex.getMessage());
                }

            }
            
        } catch (FactoryNotFoundException ex) {
            LOGGER.warning("Factory not found for Configurer, specific operation will not be available.");
            
        }
        LOGGER.info("Configuration service runing");
        
        if(INSTANCE == null || INSTANCE.get() == null){
            INSTANCE = new WeakReference<ConfigurationService>(this);
        }
        
    }
    
    private AuthenticationReader getAuthReader() {
        final File authProperties = ConfigDirectory.getAuthConfigFile();
        final Properties prop = new Properties();
        try {
            final FileInputStream fis = new FileInputStream(authProperties);
            prop.load(fis);
            final String url = prop.getProperty("cstl_authdb_host");
            final DefaultDataSource ds = new DefaultDataSource(url.replace('\\', '/') + ";");
            return new DataSourceAuthenticationReader(ds);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "IOException while loading cstl auth properties file", ex);
        }
        return null;
    }
    
    /**
     * Handle the various types of requests made to the service.
     */
    @Override
    public Response treatIncomingRequest(final Object objectRequest) {
        Marshaller marshaller = null;
        try {
            marshaller = getMarshallerPool().acquireMarshaller();
            final String request = (String) getParameter("REQUEST", true);

            for (AbstractConfigurer configurer: configurers) {
                configurer.setContainerNotifier(cn);
            }
            
            if (REQUEST_FULL_RESTART.equalsIgnoreCase(request)) {
                final boolean force = Boolean.parseBoolean(getParameter("FORCED", false));
                return Response.ok(restartService(force), MimeType.TEXT_XML).build();
            }
            
            else if (REQUEST_DOWNLOAD.equalsIgnoreCase(request)) {    
                final File f = downloadFile();
                return Response.ok(f, MediaType.MULTIPART_FORM_DATA_TYPE).build(); 
            }
                    
            else if (REQUEST_LIST_SERVICE.equalsIgnoreCase(request)) {    
                final ServiceReport response = new ServiceReport(REGISTERED_SERVICE);
                return Response.ok(response, MediaType.TEXT_XML).build(); 
            } 
            
            else if (REQUEST_GET_CONFIG_PATH.equalsIgnoreCase(request)) {
                final AcknowlegementType response = getConfigPath();
                return Response.ok(response, MediaType.TEXT_XML).build(); 
            } 
            
            else if (REQUEST_SET_CONFIG_PATH.equalsIgnoreCase(request)) {
                final String path = getParameter("path", false);
                final AcknowlegementType response = setConfigPath(path);
                return Response.ok(response, MediaType.TEXT_XML).build(); 
            }
            
            else if ("updateUser".equalsIgnoreCase(request)) {
                final String userName = getParameter("userName", true);
                final String password = getParameter("password", true);
                final String oldLogin = getParameter("oldLogin", true);
                final AcknowlegementType response = updateUser(userName, password, oldLogin);
                return Response.ok(response, MediaType.TEXT_XML).build(); 
            }
            
            else if ("deleteUser".equalsIgnoreCase(request)) {
                final String userName = getParameter("userName", true);
                final AcknowlegementType response = deleteUser(userName);
                return Response.ok(response, MediaType.TEXT_XML).build(); 
            }
            
            else if ("getUserName".equalsIgnoreCase(request)) {
                final AcknowlegementType response = getUserName();
                return Response.ok(response, MediaType.TEXT_XML).build(); 
            }
            
            /* specific operations */
            
            else {
                for (AbstractConfigurer configurer : configurers) {
                    final Object response = configurer.treatRequest(request, getUriContext().getQueryParameters(), objectRequest);
                    if (response != null) {
                        return Response.ok(response, MimeType.TEXT_XML).build();
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
            final ExceptionReport report = new ExceptionReport(ex.getMessage(), code);
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
        final ExceptionReport report = new ExceptionReport(message, code.name());
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
        for (AbstractConfigurer configurer : configurers) {
            configurer.beforeRestart();
        }
        
        if (cn != null) {
            if (!configurerLock()) {
                BDD.clearConnectionPool();
                cn.reload();
                return new AcknowlegementType(Parameters.SUCCESS, "services succefully restarted");
            } else if (!forced) {
                return new AcknowlegementType("failed", "There is an indexation running use the parameter FORCED=true to bypass it.");
            } else {
                for (AbstractConfigurer configurer : configurers) {
                    configurer.closeForced();
                }
                BDD.clearConnectionPool();
                cn.reload();
                return new AcknowlegementType(Parameters.SUCCESS, "services succefully restarted (previous indexation was stopped)");
            }
        } else {
            return new AcknowlegementType("failed", "The services can not be restarted (ContainerNotifier is null)");
        }
        
    }
    
    private boolean configurerLock() {
        for (AbstractConfigurer configurer : configurers) {
            if (configurer.isLock()) return true;
        }
        return false;
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
    public Response uploadFile(final InputStream in) {
        LOGGER.info("uploading");
        try  {
            final File tmp = File.createTempFile("cstl-", null);
            final File uploadedFile = FileUtilities.buildFileFromStream(in, tmp);
            in.close();
            return treatIncomingRequest(uploadedFile);
            
        } catch (IOException ex) {
            LOGGER.severe("IO exception while uploading file");
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
        return Response.ok("error while uploading the file", MimeType.TEXT_PLAIN).build();
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
    
    private AcknowlegementType getConfigPath() throws CstlServiceException{
        final String path = ConfigDirectory.getConfigDirectory().getPath();
        return new AcknowlegementType("Success", path);
    }
    
    private AcknowlegementType setConfigPath(final String path) throws CstlServiceException{
        // Set the new user directory
        if (path != null && !path.isEmpty()) {
            final File userDirectory = new File(path);
            if (!userDirectory.isDirectory()) {
                userDirectory.mkdir();
            }
            ConfigDirectory.setConfigDirectory(userDirectory);
        }

        return new AcknowlegementType("Success", path);
    }

    private AcknowlegementType updateUser(final String userName, final String password, final String oldLogin) {
        try {
            final AuthenticationReader authReader = getAuthReader();
            authReader.writeUser(userName, password, "Default Constellation Administrator", Arrays.asList("cstl-admin"), oldLogin);
            authReader.destroy();
            return new AcknowlegementType("Success", "The user has been changed");
        } catch (AuthenticationException ex) {
            LOGGER.log(Level.WARNING, "Error while updating user", ex);
        }
        return new AcknowlegementType("Failure", "An error occurs");
    }
    
    private AcknowlegementType deleteUser(final String userName) {
        try {
            final AuthenticationReader authReader = getAuthReader();
            authReader.deleteUser(userName);
            authReader.destroy();
            return new AcknowlegementType("Success", "The user has been deleted");
        } catch (AuthenticationException ex) {
            LOGGER.log(Level.WARNING, "Error while deleting user", ex);
        }
        return new AcknowlegementType("Failure", "An error occurs");
    }
    
    private AcknowlegementType getUserName() {
        try {
            final AuthenticationReader authReader = getAuthReader();
            final List<UserAuthnInfo> users =  authReader.listAllUsers();
            String userName = null;
            if (users != null && !users.isEmpty()) {
                userName = users.get(0).getLogin();
            }
            authReader.destroy();
            return new AcknowlegementType("Success", userName);
        } catch (AuthenticationException ex) {
            LOGGER.log(Level.WARNING, "Error while updating user", ex);
        }
        return new AcknowlegementType("Failure", "An error occurs");
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
        for (AbstractConfigurer configurer : configurers) {
            configurer.destroy();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected MarshallerPool getConfigurationPool() {
        return GenericDatabaseMarshallerPool.getInstance();
    }

    @Override
    protected Object unmarshallRequest(final Unmarshaller unmarshaller, final InputStream is) throws JAXBException, CstlServiceException {
        
        final String request = (String) getParameter("REQUEST", true);
        final MultivaluedMap<String,String> parameters = getParameters();
        for (AbstractConfigurer configurer: configurers) {
            if (configurer.needCustomUnmarshall(request,parameters)) {
                return configurer.unmarshall(request,parameters, is);
            }
        }
        return super.unmarshallRequest(unmarshaller, is);
    }
    
}
