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
package org.constellation.admin.service;

import java.util.List;
import java.io.OutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;

import org.constellation.admin.service.ConstellationServer.Tasks;
import org.constellation.admin.service.ConstellationServer.Services;
import org.constellation.admin.service.ConstellationServer.Providers;
import org.constellation.admin.service.ConstellationServer.Csws;
import org.constellation.configuration.StringTreeNode;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.ExceptionReport;
import org.constellation.configuration.InstanceReport;
import org.constellation.configuration.ObjectFactory;
import org.constellation.configuration.ProvidersReport;
import org.constellation.configuration.ServiceReport;
import org.constellation.configuration.StringList;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;

import org.geotoolkit.client.AbstractRequest;
import org.geotoolkit.client.AbstractServer;
import org.geotoolkit.security.BasicAuthenticationSecurity;
import org.geotoolkit.util.ArgumentChecks;
import org.geotoolkit.util.logging.Logging;
import org.geotoolkit.parameter.Parameters;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;
import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.geotoolkit.xml.MarshallerPool;
import org.geotoolkit.xml.parameter.ParameterDescriptorReader;
import org.geotoolkit.xml.parameter.ParameterValueReader;
import org.geotoolkit.xml.parameter.ParameterValueWriter;
import org.geotoolkit.sld.xml.Specification.StyledLayerDescriptor;
import org.geotoolkit.sld.xml.Specification.SymbologyEncoding;
import org.geotoolkit.sld.xml.XMLUtilities;
import org.geotoolkit.style.MutableStyle;

import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.style.Style;
import org.opengis.util.FactoryException;

import static org.constellation.api.QueryConstants.*;

/**
 * convenient class to perform actions on constellation web services.
 * 
 * @author Guilhem Legal (Geomatys)
 * @author Johann Sorel (Geomatys)
 */
public class ConstellationServer<S extends Services, P extends Providers, C extends Csws, T extends Tasks> extends AbstractServer{
    
    public static final ParameterDescriptor<String> URL_PARAMETER = new DefaultParameterDescriptor("Url",
            "Server URL",String.class,null,true);
    public static final ParameterDescriptor<String> USER_PARAMETER = new DefaultParameterDescriptor("User",
            "User login",String.class,null,true);
    public static final ParameterDescriptor<String> PASSWORD_PARAMETER = new DefaultParameterDescriptor("Password",
            "User password",String.class,null,true);
    public static final ParameterDescriptorGroup CSTL_DESCRIPTOR_GROUP =
            new DefaultParameterDescriptorGroup("Constellation",URL_PARAMETER,USER_PARAMETER,PASSWORD_PARAMETER);
    
    protected static final Logger LOGGER = Logging.getLogger("org.constellation.admin.service");
    private static final MarshallerPool POOL = GenericDatabaseMarshallerPool.getInstance();

    public final S services;
    public final P providers;
    public final C csws;
    public final T tasks;
    
    public final String currentUser;
    
    public ConstellationServer(final ParameterValueGroup value) throws MalformedURLException {
        this(new URL(Parameters.value(URL_PARAMETER, value)),
             Parameters.stringValue(USER_PARAMETER, value),
             Parameters.stringValue(PASSWORD_PARAMETER, value));
    }
    
    
    public ConstellationServer(final URL server, final String user, final String password) {
        super(server,new BasicAuthenticationSecurity(user, password));
        this.services    = createServiceManager();
        this.providers   = createProviderManager();
        this.csws        = createCswManager();
        this.tasks       = createTaskManager();
        this.currentUser = user;
    }
    
    protected S createServiceManager(){
        return (S) new Services();
    }
    
    protected T createTaskManager(){
        return (T) new Tasks();
    }
    
    protected C createCswManager(){
        return (C) new Csws();
    }
    
    protected P createProviderManager(){
        return (P) new Providers();
    }
    
    public static ConstellationServer login(final ParameterValueGroup value) {
        return login(Parameters.value(URL_PARAMETER, value),
             Parameters.stringValue(USER_PARAMETER, value),
             Parameters.stringValue(PASSWORD_PARAMETER, value));
    }
    
    public static ConstellationServer login(final String serviceURL, 
            final String login, final String password) {
        ArgumentChecks.ensureNonNull("server url", serviceURL);
        ArgumentChecks.ensureNonNull("user", login);
        ArgumentChecks.ensureNonNull("password", password);
        
        final URL url;
        try {
            url = new URL(serviceURL);
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            return null;
        }
        
        ConstellationServer serviceAdmin = new ConstellationServer(url, login, password);
        
        //check if the service and logins are valid
        if(!serviceAdmin.authenticate()){
            //unvalid configuration
            serviceAdmin = null;
        }
        
        return serviceAdmin;
    }
    
    /**
     * Set the basic authentication for HTTP request.
     * @return true if login/password are valid
     */
    protected boolean authenticate() {
        final String str = this.serverURL.toString() + "configuration?request=access";
        InputStream stream = null;
        HttpURLConnection cnx = null;
        try {
            final URL url = new URL(str);
            cnx = (HttpURLConnection) url.openConnection();
            getClientSecurity().secure(cnx);
            stream = AbstractRequest.openRichException(cnx, securityManager);            
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, ex.getLocalizedMessage());
            return false;
        }finally{
            if(stream != null){
                try {
                    stream.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
                }
            }
            if(cnx != null){
                cnx.disconnect();
            }
        }
        return true;
    }

    /**
     * @return configuration path used by constellation, null if failed to get path.
     */
    public String getConfigurationPath(){
        try {
            final String url = getURL() + "configuration?request=" + REQUEST_GET_CONFIG_PATH;
            final Object response = sendRequest(url, null);
            if (response instanceof AcknowlegementType) {
                final AcknowlegementType ak = (AcknowlegementType) response;
                if("Success".equalsIgnoreCase(ak.getStatus())){
                    return ak.getMessage();
                }else{
                    return null;
                }
            } else if (response instanceof ExceptionReport){
                LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                return null;
            } else {
                LOGGER.warning("The service respond uncorrectly");
                return null;
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        return null;
    }
    
    /**
     * 
     * @param path
     * @return true if succeed
     */
    public boolean setConfigurationPath(final String path){
        try {
            
            final String url = getURL() + "configuration?request=" + REQUEST_SET_CONFIG_PATH + "&path=" + URLEncoder.encode(path);
            final Object response = sendRequest(url, null);
            if (response instanceof AcknowlegementType) {
                return "Success".equals(((AcknowlegementType)response).getStatus());
            } else if (response instanceof ExceptionReport){
                LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                return false;
            } else {
                LOGGER.warning("The service respond uncorrectly");
                return false;
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        return false;
    }
    
    public boolean deleteUser(final String userName){
        try {
            final String url = getURL() + "configuration?request=deleteUser&username=" + userName;
            final Object response = sendRequest(url, null);
            if (response instanceof AcknowlegementType) {
                final AcknowlegementType ak = (AcknowlegementType) response;
                if ("Success".equalsIgnoreCase(ak.getStatus())) {
                    return true;
                }
            } else if (response instanceof ExceptionReport){
                LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                return false;
            } else {
                LOGGER.warning("The service respond uncorrectly");
                return false;
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        return false;
    }
    
    public boolean updateUser(final String userName, final String password, final String oldLogin){
        try {
            final String url = getURL() + "configuration?request=updateUser&username=" + userName + "&password=" + password + "&oldLogin=" + oldLogin;
            final Object response = sendRequest(url, null);
            if (response instanceof AcknowlegementType) {
                final AcknowlegementType ak = (AcknowlegementType) response;
                if ("Success".equalsIgnoreCase(ak.getStatus())) {
                    return true;
                }
            } else if (response instanceof ExceptionReport){
                LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                return false;
            } else {
                LOGGER.warning("The service respond uncorrectly");
                return false;
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        return false;
    }
    
    public String getUserName(){
        try {
            final String url = getURL() + "configuration?request=getUserName";
            final Object response = sendRequest(url, null);
            if (response instanceof AcknowlegementType) {
                final AcknowlegementType ak = (AcknowlegementType) response;
                if ("Success".equalsIgnoreCase(ak.getStatus())) {
                    return ak.getMessage();
                }
            } else if (response instanceof ExceptionReport){
                LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                return null;
            } else {
                LOGGER.warning("The service respond uncorrectly");
                return null;
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        return null;
    }
    
    /**
     * Configuration methods for services
     */
    public final class Services{
        
        public List<String> getAvailableService() {
            try {
                final String url = getURL() + "configuration?request=" + REQUEST_LIST_SERVICE;
                final Object response = sendRequest(url, null);
                if (response instanceof ServiceReport) {
                    return ((ServiceReport)response).getAvailableServices();
                } else if (response instanceof ExceptionReport){
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                    return new ArrayList<String>();
                } else {
                    LOGGER.warning("The service respond uncorrectly");
                    return new ArrayList<String>();
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return new ArrayList<String>();
        }
        
        /**
         * Restart all the web-service (wms, wfs, csw,...)
         *
         * @return true if the operation succeed
         */
        public boolean restartAll() {
            try {
                final String url = getURL() + "configuration?request=" + REQUEST_FULL_RESTART;
                final Object response = sendRequest(url, null);
                if (response instanceof AcknowlegementType) {
                    return "Success".equals(((AcknowlegementType)response).getStatus());
                } else if (response instanceof ExceptionReport){
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                    return false;
                } else {
                    LOGGER.warning("The service respond uncorrectly");
                    return false;
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return false;
        }

        /**
         * Restart all the instance of a specific web-service (wms, wfs, csw,...)
         * 
         * @param service The service name to restart (wms, wfs, csw,...).
         * 
         * @return true if the operation succeed
         */
        public boolean restartAllInstance(final String service) {
            try {
                final String url = getURL() + service.toLowerCase() + "/admin?request=restart";
                final Object response = sendRequest(url, null);
                if (response instanceof AcknowlegementType) {
                    return "Success".equals(((AcknowlegementType)response).getStatus());
                } else if (response instanceof ExceptionReport){
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                    return false;
                } else {
                    LOGGER.warning("The service respond uncorrectly");
                    return false;
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return false;
        }
        
        /**
         * Restart all the instance of a specific web-service (wms, wfs, csw,...)
         * 
         * @param service The service name to restart (wms, wfs, csw,...).
         * 
         * @return true if the operation succeed
         */
        public boolean renameInstance(final String service, final String instanceId, final String newName) {
            try {
                final String url = getURL() + service.toLowerCase() + "/admin?request=renameInstance&id=" + instanceId + "&newName=" + newName;
                final Object response = sendRequest(url, null);
                if (response instanceof AcknowlegementType) {
                    return "Success".equals(((AcknowlegementType)response).getStatus());
                } else if (response instanceof ExceptionReport){
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                    return false;
                } else {
                    LOGGER.warning("The service respond uncorrectly");
                    return false;
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return false;
        }

        /**
         * Restart a unique instance for the specified service  (wms, wfs, csw,...) and instance identifier.
         * 
         * @param service The service name to restart (wms, wfs, csw,...).
         * @param instanceId The instance identifier to restart.
         * 
         * @return true if the operation succeed
         */
        public boolean restartInstance(final String service, final String instanceId) {
            try {
                final String url = getURL() + service.toLowerCase() + "/admin?request=restart&id=" + instanceId;
                final Object response = sendRequest(url, null);
                if (response instanceof AcknowlegementType) {
                    return "Success".equals(((AcknowlegementType)response).getStatus());
                } else if (response instanceof ExceptionReport){
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                    return false;
                } else {
                    LOGGER.warning("The service respond uncorrectly");
                    return false;
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return false;
        }

        /**
         * Create a new instance for the specified service  (wms, wfs, csw,...) with the specified identifier.
         *
         * @param service The service name to restart (wms, wfs, csw,...).
         * @param instanceId The instance identifier to create.
         * 
         * @return true if the operation succeed
         */
        public boolean newInstance(String service, String instanceId) {
            try {
                final String url = getURL() + service.toLowerCase() + "/admin?request=newInstance&id=" + instanceId;
                final Object response = sendRequest(url, null);
                if (response instanceof AcknowlegementType) {
                    return "Success".equals(((AcknowlegementType)response).getStatus());
                } else if (response instanceof ExceptionReport){
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                    return false;
                } else {
                    LOGGER.warning("The service respond uncorrectly");
                    return false;
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return false;
        }

        /**
         * Start a new instance for the specified service  (wms, wfs, csw,...) with the specified identifier.
         * 
         * @param service The service name to restart (wms, wfs, csw,...).
         * @param instanceId The instance identifier to create.
         *
         * @return true if the operation succeed.
         */
        public boolean startInstance(final String service, final String instanceId) {
            try {
                final String url = getURL() + service.toLowerCase() + "/admin?request=start&id=" + instanceId;
                final Object response = sendRequest(url, null);
                if (response instanceof AcknowlegementType) {
                    return "Success".equals(((AcknowlegementType)response).getStatus());
                } else if (response instanceof ExceptionReport){
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                    return false;
                } else {
                    LOGGER.warning("The service respond uncorrectly");
                    return false;
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return false;
        }

        /**
         * Stop a new instance for the specified service  (wms, wfs, csw,...) with the specified identifier.
         *
         * @param service The service name to restart (wms, wfs, csw,...).
         * @param instanceId The instance identifier to create.
         *
         * @return true if the operation succeed.
         */
        public boolean stopInstance(final String service, final String instanceId) {
            try {
                final String url = getURL() + service.toLowerCase() + "/admin?request=stop&id=" + instanceId;
                final Object response = sendRequest(url, null);
                if (response instanceof AcknowlegementType) {
                    return "Success".equals(((AcknowlegementType)response).getStatus());
                } else if (response instanceof ExceptionReport){
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                    return false;
                } else {
                    LOGGER.warning("The service respond uncorrectly");
                    return false;
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return false;
        }

        /**
         * Delete a new instance for the specified service  (wms, wfs, csw,...) with the specified identifier.
         *
         * @param service The service name to start (wms, wfs, csw,...).
         * @param instanceId The instance identifier to create.
         *
         * @return true if the operation succeed.
         */
        public boolean deleteInstance(final String service, final String instanceId) {
            try {
                final String url = getURL() + service.toLowerCase() + "/admin?request=delete&id=" + instanceId;
                final Object response = sendRequest(url, null);
                if (response instanceof AcknowlegementType) {
                    return "Success".equals(((AcknowlegementType)response).getStatus());
                } else if (response instanceof ExceptionReport){
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                    return false;
                } else {
                    LOGGER.warning("The service respond uncorrectly");
                    return false;
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return false;
        }

        /**
         * Ask for a report about the instances for the specified service  (wms, wfs, csw,...).
         *
         * @param service The service name to restart (wms, wfs, csw,...).
         *
         * @return A {@link InstanceReport} about the specified service.
         */
        public InstanceReport listInstance(final String service) {
            try {
                final String url = getURL() + service.toLowerCase() + "/admin?request=listInstance";
                final Object response = sendRequest(url, null);
                if (response instanceof InstanceReport) {
                    return (InstanceReport) response;
                } else if (response instanceof ExceptionReport){
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                    return null;
                } else {
                    LOGGER.warning("The service respond uncorrectly");
                    return null;
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return null;
        }

        /**
         * Send a configuration object to the specified service and instance.
         * 
         * @param service The service name to restart (wms, wfs, csw,...).
         * @param instanceId The instance identifier to configure.
         * @param configuration A configuration object depending on the service type (for example WxS service take LayerContext object).
         *
         * @return true if the operation succeed.
         */
        public boolean configureInstance(final String service, final String instanceId, final Object configuration) {
            try {
                final String url = getURL() + service.toLowerCase() + "/admin?request=configure&id=" + instanceId;
                final Object response = sendRequest(url, configuration);
                if (response instanceof AcknowlegementType) {
                    return "Success".equals(((AcknowlegementType)response).getStatus());
                } else if (response instanceof ExceptionReport){
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                    return false;
                } else {
                    LOGGER.warning("The service respond uncorrectly");
                    return false;
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return false;
        }

        /**
         * Return the current configuration object for the specified service and instance.
         *
         * @param service The service name to restart (wms, wfs, csw,...).
         * @param instanceId The instance identifier to configure.
         *
         * @return  A configuration object depending on the service type (for example WxS service return LayerContext object).
         */
        public Object getInstanceconfiguration(final String service, final String instanceId) {
            try {
                final String url = getURL() + service.toLowerCase() + "/admin?request=getConfiguration&id=" + instanceId;
                final Object response = sendRequest(url, null);
                if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                    return null;
                } else if (response != null){
                    return response;
                } else {
                    LOGGER.warning("The service respond uncorrectly");
                    return null;
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return false;
        }

        public boolean updateCapabilities(final String service, final String instanceId, final File importFile, final String fileName) {
            try {
                final String url = getURL() + service.toLowerCase() + "/admin?request=" + REQUEST_UPDATE_CAPABILITIES + "&id=" + instanceId + "&filename=" + fileName;
                Object response = sendRequest(url, importFile, null, null, true);
                if (response instanceof AcknowlegementType) {
                    final AcknowlegementType ack = (AcknowlegementType) response;
                    if ("Success".equals(ack.getStatus())) {
                        return true;
                    } else {
                        LOGGER.log(Level.INFO, "Failure:{0}", ack.getMessage());
                    }
                } else if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return false;
        }
        
        /**
         * Return a complete URL for the specified service  (wms, wfs, csw,...) and instance identifier.
         *
         * @param service The service name (wms, wfs, csw,...).
         * @param instanceId The instance identifier.
         *
         * @return A complete URL for the specified service.
         */
        public String getInstanceURL(final String service, final String instanceId) {
            return getURL() + service.toLowerCase() + '/' + instanceId;
        }
        
    }
    
    /**
     * Configuration methods for providers
     */
    public final class Providers{
                
        /**
         * Restart all layer providers.
         */
        public boolean restartAllLayerProviders() {
            try {
                final String url = getURL() + "configuration?request="+REQUEST_RESTART_ALL_LAYER_PROVIDERS;
                Object response = sendRequest(url, null);
                if (response instanceof AcknowlegementType) {
                    final AcknowlegementType ack = (AcknowlegementType) response;
                    if ("Success".equals(ack.getStatus())) {
                        return true;
                    } else {
                        LOGGER.log(Level.INFO, "Failure:{0}", ack.getMessage());
                    }
                } else if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return false;
        }
        
        /**
         * Restart all layer providers.
         */
        public boolean restartAllStyleProviders() {
            try {
                final String url = getURL() + "configuration?request="+REQUEST_RESTART_ALL_STYLE_PROVIDERS;
                Object response = sendRequest(url, null);
                if (response instanceof AcknowlegementType) {
                    final AcknowlegementType ack = (AcknowlegementType) response;
                    if ("Success".equals(ack.getStatus())) {
                        return true;
                    } else {
                        LOGGER.log(Level.INFO, "Failure:{0}", ack.getMessage());
                    }
                } else if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return false;
        }
        
        /**
         * Add a new source provider to the service.
         * 
         * @param serviceName The provider service name (shapefile, coverage-sql, ...)
         * @param config The configuration Object to add to the specific provider file.
         * @return 
         */
        public AcknowlegementType createProvider(final String serviceName, final ParameterValueGroup config) {
            ArgumentChecks.ensureNonNull("service name", serviceName);
            ArgumentChecks.ensureNonNull("config", config);
            try {
                final String url = getURL() + "configuration?request="+REQUEST_CREATE_PROVIDER+"&serviceName=" + serviceName;
                Object response = sendRequest(url, config);
                if (response instanceof AcknowlegementType) {
                    final AcknowlegementType ack = (AcknowlegementType) response;
                    if ("Success".equals(ack.getStatus())) {
                        return null;
                    } else {
                        return ack;
                    }
                } else if (response instanceof ExceptionReport) {
                    return new AcknowlegementType("Failure", ((ExceptionReport)response).getMessage() );
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
                return new AcknowlegementType("Failure", ex.getMessage() );
            }
            return null;
        }

        /**
         * Get the source provider configuration.
         * 
         * @param id The identifier of the source
         * @return 
         */
        public GeneralParameterValue getProviderConfiguration(final String id, final ParameterDescriptorGroup descriptor) {
            try {
                final String url = getURL() + "configuration?request="+REQUEST_GET_PROVIDER_CONFIG+"&id=" + id;
                Object response = sendRequest(url, null, descriptor, null, false);
                if (response instanceof GeneralParameterValue) {
                    return (GeneralParameterValue) response;
                } else if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return null;
        }

        /**
         * Remove a source provider in the service.
         * 
         * @param id The identifier of the source
         * @return 
         */
        public boolean deleteProvider(final String id) {
            try {
                final String url = getURL() + "configuration?request="+REQUEST_DELETE_PROVIDER+"&id=" + id;
                Object response = sendRequest(url, null);
                if (response instanceof AcknowlegementType) {
                    final AcknowlegementType ack = (AcknowlegementType) response;
                    if ("Success".equals(ack.getStatus())) {
                        return true;
                    } else {
                        LOGGER.log(Level.INFO, "Failure:{0}", ack.getMessage());
                    }
                } else if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return false;
        }

        /**
         * Modify a source provider in the service.
         * 
         * @param serviceName The provider type (shapefile, coverage-sql, ...)
         * @param id The identifier of the source to update.
         * @param config The configuration Object to modify on the specific provider file.
         * @return 
         */
        public boolean updateProvider(final String serviceName, final String id, final ParameterValueGroup config) {
            try {
                final String url = getURL() + "configuration?request="+REQUEST_UPDATE_PROVIDER+"&serviceName=" + serviceName + "&id=" + id;
                final Object response = sendRequest(url, config);
                if (response instanceof AcknowlegementType) {
                    final AcknowlegementType ack = (AcknowlegementType) response;
                    if ("Success".equals(ack.getStatus())) {
                        return true;
                    } else {
                        LOGGER.log(Level.INFO, "Failure:{0}", ack.getMessage());
                    }
                } else if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return false;
        }

        /**
         * Reload a source provider in the service.
         * 
         * @param id The identifier of the source
         * @return 
         */
        public boolean restartProvider(final String id){
            try {
                final String url = getURL() + "configuration?request="+REQUEST_RESTART_PROVIDER+"&id=" + id;
                Object response = sendRequest(url, null);
                if (response instanceof AcknowlegementType) {
                    final AcknowlegementType ack = (AcknowlegementType) response;
                    if ("Success".equals(ack.getStatus())) {
                        return true;
                    } else {
                        LOGGER.log(Level.INFO, "Failure:{0}", ack.getMessage());
                    }
                } else if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return false;
        }
        
        
        // LAYER PROVIDERS ACTIONS /////////////////////////////////////////////
        
        /**
         *Add a new layer to a source provider in the service.
         * 
         * @param id The identifier of the provider
         * @return 
         */
        public boolean createLayer(final String id, final ParameterValueGroup config) {
            ArgumentChecks.ensureNonNull("id", id);
            ArgumentChecks.ensureNonNull("config", config);
            try {
                final String url = getURL() + "configuration?request="+REQUEST_CREATE_LAYER+"&id=" + id;
                Object response = sendRequest(url, config);
                if (response instanceof AcknowlegementType) {
                    final AcknowlegementType ack = (AcknowlegementType) response;
                    if ("Success".equals(ack.getStatus())) {
                        return true;
                    } else {
                        LOGGER.log(Level.INFO, "Failure:{0}", ack.getMessage());
                    }
                } else if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return false;
        }

        /**
         * Remove a source provider in the service.
         * 
         * @param id The identifier of the provider
         * @return 
         */
        public boolean deleteLayer(final String id, final String layerName) {
            try {
                final String url = getURL() + "configuration?request="+REQUEST_DELETE_LAYER+"&id=" + id + "&layerName=" + layerName;
                Object response = sendRequest(url, null);
                if (response instanceof AcknowlegementType) {
                    final AcknowlegementType ack = (AcknowlegementType) response;
                    if ("Success".equals(ack.getStatus())) {
                        return true;
                    } else {
                        LOGGER.log(Level.INFO, "Failure:{0}", ack.getMessage());
                    }
                } else if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return false;
        }

        /**
         *Add a new layer to a source provider in the service.
         * 
         * @param id The identifier of the provider
         * @return 
         */
        public boolean updateLayer(final String id, final String layerName, final ParameterValueGroup layer) {
            try {
                final String url = getURL() + "configuration?request="+REQUEST_UPDATE_LAYER+"&id=" + id + "&layerName=" + layerName;
                Object response = sendRequest(url, layer);
                if (response instanceof AcknowlegementType) {
                    final AcknowlegementType ack = (AcknowlegementType) response;
                    if ("Success".equals(ack.getStatus())) {
                        return true;
                    } else {
                        LOGGER.log(Level.INFO, "Failure:{0}", ack.getMessage());
                    }
                } else if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return false;
        }

        // STYLE PROVIDERS ACTIONS /////////////////////////////////////////////
        
        public MutableStyle downloadStyle(final String id, final String styleName){
            ArgumentChecks.ensureNonNull("id", id);
            ArgumentChecks.ensureNonNull("styleName", styleName);
            
            try {
                final String url = getURL() + "configuration?request="+REQUEST_DOWNLOAD_STYLE+"&id=" + id + "&styleName=" + styleName;
                Object response = sendRequest(url, null, null, XMLUtilities.getJaxbContext110(), false);
                
                if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                } else {
                    final XMLUtilities utils = new XMLUtilities();
                    return utils.readStyle(response, SymbologyEncoding.V_1_1_0);
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            } catch (JAXBException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            } catch (FactoryException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return null;
        }
        
        /**
         * 
         * @param id
         * @param style : SLD or other
         * @return null if successful, AcknowlegementType if failed
         */
        public boolean createStyle(final String id, final String styleName, final Object style){
            ArgumentChecks.ensureNonNull("id", id);
            ArgumentChecks.ensureNonNull("styleName", styleName);
            ArgumentChecks.ensureNonNull("style", style);
            try {
                final String url = getURL() + "configuration?request="+REQUEST_CREATE_STYLE+"&id=" + id + "&styleName=" + styleName;
                Object response = sendRequest(url, style);
                if (response instanceof AcknowlegementType) {
                    final AcknowlegementType ack = (AcknowlegementType) response;
                    if ("Success".equals(ack.getStatus())) {
                        return true;
                    } else {
                        LOGGER.log(Level.INFO, "Failure:{0}", ack.getMessage());
                    }
                } else if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return false;
        }
        
        /**
         * 
         * @param id : provider id
         * @param styleName : style id
         * @return true if successful
         */
        public boolean deleteStyle(final String id, final String styleName){
            ArgumentChecks.ensureNonNull("id", id);
            ArgumentChecks.ensureNonNull("styleName", styleName);
            
            try {
                final String url = getURL() + "configuration?request="+REQUEST_DELETE_STYLE+"&id=" + id + "&styleName=" + styleName;
                Object response = sendRequest(url, null);
                if (response instanceof AcknowlegementType) {
                    final AcknowlegementType ack = (AcknowlegementType) response;
                    if ("Success".equals(ack.getStatus())) {
                        return true;
                    } else {
                        LOGGER.log(Level.INFO, "Failure:{0}", ack.getMessage());
                    }
                } else if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return false;
        }
        
        /**
         * Update a style
         * 
         * @param id The identifier of the provider
         * @param styleName The identifier of the style
         * @param style The new style definition
         * @return 
         */
        public boolean updateStyle(final String id, final String styleName, final Object style) {
            ArgumentChecks.ensureNonNull("id", id);
            ArgumentChecks.ensureNonNull("styleName", styleName);
            ArgumentChecks.ensureNonNull("style", style);
            try {
                final String url = getURL() + "configuration?request="+REQUEST_UPDATE_STYLE+"&id=" + id + "&styleName=" + styleName;
                Object response = sendRequest(url, style);
                if (response instanceof AcknowlegementType) {
                    final AcknowlegementType ack = (AcknowlegementType) response;
                    if ("Success".equals(ack.getStatus())) {
                        return true;
                    } else {
                        LOGGER.log(Level.INFO, "Failure:{0}", ack.getMessage());
                    }
                } else if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return false;
        }
        
        /**
         * Get the provider service configuration description.
         * 
         * @param id The identifier of the service
         * @return 
         */
        public GeneralParameterDescriptor getServiceDescriptor(final String serviceName) {
            try {
                final String url = getURL() + "configuration?request="+REQUEST_GET_SERVICE_DESCRIPTOR+"&serviceName=" + serviceName;
                Object response = sendDescriptorRequest(url, null);
                if (response instanceof GeneralParameterDescriptor) {
                    return (GeneralParameterDescriptor) response;
                } else if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                } else {
                    LOGGER.log(Level.WARNING, "Unexpected response type :{0}", response);
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return null;
        }
        
        /**
         * Get the provider service source configuration description.
         * 
         * @param id The identifier of the service
         * @return 
         */
        public GeneralParameterDescriptor getSourceDescriptor(final String serviceName) {
            try {
                final String url = getURL() + "configuration?request="+REQUEST_GET_SOURCE_DESCRIPTOR+"&serviceName=" + serviceName;
                Object response = sendDescriptorRequest(url, null);
                if (response instanceof GeneralParameterDescriptor) {
                    return (GeneralParameterDescriptor) response;
                } else if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                } else {
                    LOGGER.log(Level.WARNING, "Unexpected response type :{0}", response);
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return null;
        }

        public ProvidersReport listProviders() {
            try {
                final String url = getURL() + "configuration?request="+REQUEST_LIST_SERVICES;
                final Object response = sendRequest(url, null);
                if (response instanceof ProvidersReport) {
                    return (ProvidersReport) response;
                } else if (response instanceof ExceptionReport){
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                    return null;
                } else {
                    LOGGER.warning("The service respond uncorrectly");
                    return null;
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return null;
        }

    }
    
    /**
     * Configuration methods for task scheduler.
     */
    public final class Tasks{
        
        /**
         * Ask for a list of all available process.
         */
        public StringList listProcess() {
            try {
                final String url = getURL() + "configuration?request="+REQUEST_LIST_PROCESS;
                final Object response = sendRequest(url, null);
                if (response instanceof StringList) {
                    return (StringList) response;
                } else if (response instanceof ExceptionReport){
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                    return null;
                } else {
                    LOGGER.warning("The service respond uncorrectly");
                    return null;
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return null;
        }
        
        /**
         * Ask for a list of all tasks.
         */
        public StringTreeNode listTasks(){
            try {
                final String url = getURL() + "configuration?request="+REQUEST_LIST_TASKS;
                final Object response = sendRequest(url, null);
                if (response instanceof StringTreeNode) {
                    return (StringTreeNode) response;
                } else if (response instanceof ExceptionReport){
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                    return null;
                } else {
                    LOGGER.warning("The service respond uncorrectly");
                    return null;
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return null;
        }
        
        /**
         * Get the parameters description for the given process.
         */
        public GeneralParameterDescriptor getProcessDescriptor(final String authority, final String code) {
            try {
                final String url = getURL() + "configuration?request="+REQUEST_GET_PROCESS_DESC+"&authority="+authority+"&code="+code;
                Object response = sendDescriptorRequest(url, null);
                if (response instanceof GeneralParameterDescriptor) {
                    return (GeneralParameterDescriptor) response;
                } else if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                } else {
                    LOGGER.log(Level.WARNING, "Unexpected response type :{0}", response);
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return null;
        }
        
        /**
         * Get the parameters for the given task
         * @param id
         * @return 
         */
        public GeneralParameterValue getTaskParameters(final String id, ParameterDescriptorGroup desc){
            try {
                final String url = getURL() + "configuration?request="+REQUEST_GET_TASK_PARAMS+"&id="+id;
                Object response = sendRequest(url, null, desc, null, false);
                if (response instanceof GeneralParameterValue) {
                    return (GeneralParameterValue) response;
                } else if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                } else {
                    LOGGER.log(Level.WARNING, "Unexpected response type :{0}", response);
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return null;
        }
        
        /**
         * Create a new task.
         * 
         * @param authority
         * @param code
         * @param title
         * @param step
         * @param parameters
         * @return 
         */
        public boolean createTask(final String authority, final String code, final String id, 
                final String title, final int step, final GeneralParameterValue parameters){
            ArgumentChecks.ensureNonNull("authority", authority);
            ArgumentChecks.ensureNonNull("code", code);
            ArgumentChecks.ensureNonNull("id", id);
            ArgumentChecks.ensureNonNull("title", title);
            ArgumentChecks.ensureNonNull("step", step);
            ArgumentChecks.ensureNonNull("parameters", parameters);
            try {
                final String url = getURL() + "configuration?request="+REQUEST_CREATE_TASK
                        +"&authority=" + authority 
                        +"&code=" + code
                        +"&id=" + id
                        +"&title=" + title 
                        +"&step=" + step;
                Object response = sendRequest(url, parameters);
                if (response instanceof AcknowlegementType) {
                    final AcknowlegementType ack = (AcknowlegementType) response;
                    if ("Success".equals(ack.getStatus())) {
                        return true;
                    } else {
                        LOGGER.log(Level.INFO, "Failure:{0}", ack.getMessage());
                    }
                } else if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return false;
        }
        
        
        /**
         * Update a task.
         * 
         * @param authority
         * @param code
         * @param title
         * @param step
         * @param parameters
         * @return 
         */
        public boolean updateTask(final String authority, final String code, final String id, 
                final String title, final int step, final GeneralParameterValue parameters){
            ArgumentChecks.ensureNonNull("authority", authority);
            ArgumentChecks.ensureNonNull("code", code);
            ArgumentChecks.ensureNonNull("id", id);
            ArgumentChecks.ensureNonNull("title", title);
            ArgumentChecks.ensureNonNull("step", step);
            ArgumentChecks.ensureNonNull("parameters", parameters);
            try {
                final String url = getURL() + "configuration?request="+REQUEST_UPDATE_TASK
                        +"&authority=" + authority 
                        +"&code=" + code
                        +"&id=" + id
                        +"&title=" + title 
                        +"&step=" + step;
                final Object response = sendRequest(url, parameters);
                if (response instanceof AcknowlegementType) {
                    final AcknowlegementType ack = (AcknowlegementType) response;
                    if ("Success".equals(ack.getStatus())) {
                        return true;
                    } else {
                        LOGGER.log(Level.INFO, "Failure:{0}", ack.getMessage());
                    }
                } else if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return false;
        }
        
        
        
        /**
         * Delete an existing task.
         */
        public boolean deleteTask(final String id){
            ArgumentChecks.ensureNonNull("id", id);
            try {
                final String url = getURL().toString() + "configuration?request="+REQUEST_DELETE_TASK+"&id="+id;
                Object response = sendRequest(url,null);
                if (response instanceof AcknowlegementType) {
                    final AcknowlegementType ack = (AcknowlegementType) response;
                    if ("Success".equals(ack.getStatus())) {
                        return true;
                    } else {
                        LOGGER.log(Level.INFO, "Failure:{0}", ack.getMessage());
                    }
                } else if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return false;
        }
        
    }
    
    /**
     * Configuration methods for csw
     */
    public class Csws {
        
        public boolean refreshIndex(final String id, final boolean asynchrone) {
            try {
                final String url = getURL() + "configuration?request=" + REQUEST_REFRESH_INDEX + "&id=" + id + "&asynchrone=" + asynchrone;
                Object response = sendRequest(url, null);
                if (response instanceof AcknowlegementType) {
                    final AcknowlegementType ack = (AcknowlegementType) response;
                    if ("Success".equals(ack.getStatus())) {
                        return true;
                    } else {
                        LOGGER.log(Level.INFO, "Failure:{0}", ack.getMessage());
                    }
                } else if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return false;
        }
    
        public boolean importFile(final String id, final File importFile, final String fileName) {
            try {
                final String url = getURL() + "configuration?request=" + REQUEST_IMPORT_RECORDS + "&id=" + id + "&filename=" + fileName;
                Object response = sendRequest(url, importFile, null, null, true);
                if (response instanceof AcknowlegementType) {
                    final AcknowlegementType ack = (AcknowlegementType) response;
                    if ("Success".equals(ack.getStatus())) {
                        return true;
                    } else {
                        LOGGER.log(Level.INFO, "Failure:{0}", ack.getMessage());
                    }
                } else if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return false;
        }
        
        public boolean metadataExist(final String id, final String metadataName) {
            try {
                final String url = getURL() + "configuration?request=" + REQUEST_METADATA_EXIST + "&id=" + id + "&metadata=" + metadataName;
                Object response = sendRequest(url, null);
                if (response instanceof AcknowlegementType) {
                    final AcknowlegementType ack = (AcknowlegementType) response;
                    if ("Exist".equals(ack.getStatus())) {
                        return true;
                    } else if ("Not Exist".equals(ack.getStatus())){
                        return false;
                    } else {
                        LOGGER.log(Level.WARNING, "Failure:{0}", ack.getMessage());
                    }
                } else if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return false;
        }
        
        public List<String> getAvailableDataSourceType() {
            try {
                final String url = getURL() + "configuration?request=" + REQUEST_AVAILABLE_SOURCE_TYPE;
                Object response = sendRequest(url, null);
                if (response instanceof StringList) {
                    final StringList sl = (StringList) response;
                    return sl.getList();
                } else if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
            return null;
        }
    }
    
    
    // convinient methods //////////////////////////////////////////////////////
    
    protected Object sendRequest(String sourceURL, Object request) throws MalformedURLException, IOException {
         return sendRequest(sourceURL, request, null, null, false);
    }
    
    /**
     * Send a request to another service.
     *
     * @param sourceURL the URL of the distant web-service
     * @param request The XML object to send in POST mode (if null the request is GET)
     *
     * @return The object corresponding to the XML response of the distant web-service
     *
     * @throws java.net.MalformedURLException
     * @throws java.io.IOException
     * @throws org.constellation.coverage.web.CstlServiceException
     */
    protected Object sendRequest(String sourceURL, Object request, ParameterDescriptorGroup descriptor, 
            MarshallerPool unmarshallerPool, boolean put) throws MalformedURLException, IOException {

        final URL source = new URL(sourceURL);
        final HttpURLConnection conec = (HttpURLConnection) source.openConnection();
        getClientSecurity().secure(conec);
        Object response = null;

        try {

            // for a POST request
            if (request != null) {

                conec.setDoOutput(true);
                conec.setRequestProperty("Content-Type", "text/xml");
                if (put) {
                    conec.setRequestMethod("PUT");
                }

                if (request instanceof GeneralParameterValue) {
                    final ParameterValueWriter writer = new ParameterValueWriter();
                    try {
                        writer.setOutput(conec.getOutputStream());
                        writer.write((GeneralParameterValue)request);
                    } catch (XMLStreamException ex) {
                        LOGGER.log(Level.WARNING, "unable to marshall the request", ex);
                    }
                } else if (request instanceof Style) {
                    final XMLUtilities util = new XMLUtilities();
                    try {
                        util.writeStyle(conec.getOutputStream(), (Style)request, StyledLayerDescriptor.V_1_1_0);
                    } catch (JAXBException ex) {
                        LOGGER.log(Level.WARNING, "unable to marshall the request", ex);
                    }
                } else if (request instanceof org.opengis.sld.StyledLayerDescriptor) {
                    final XMLUtilities util = new XMLUtilities();
                    try {
                        util.writeSLD(conec.getOutputStream(), 
                                (org.opengis.sld.StyledLayerDescriptor)request, StyledLayerDescriptor.V_1_1_0);
                    } catch (JAXBException ex) {
                        LOGGER.log(Level.WARNING, "unable to marshall the request", ex);
                    }
                } else if (request instanceof File) {
                    final FileInputStream in = new FileInputStream((File)request);
                    final OutputStream out = conec.getOutputStream();
                    try {
                        final byte[] buffer = new byte[4096];
                        int bytesRead;

                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead); // write
                        }
                    } finally {
                        out.close();
                        in.close();
                    }
                } else {
                    Marshaller marshaller = null;
                    try {
                        marshaller = POOL.acquireMarshaller();
                        marshaller.marshal(request, conec.getOutputStream());
                    } catch (JAXBException ex) {
                        LOGGER.log(Level.WARNING, "unable to marshall the request", ex);
                    } finally {
                        if (marshaller != null) {
                            POOL.release(marshaller);
                        }
                    }
                }
            }
            
            if(unmarshallerPool == null){
                //use default pool
                unmarshallerPool = POOL;
            }
            
            Unmarshaller unmarshaller = null;
            try {
                unmarshaller = unmarshallerPool.acquireUnmarshaller();
                final InputStream responseStream = AbstractRequest.openRichException(conec, securityManager);  
                response = unmarshaller.unmarshal(responseStream);
                if (response instanceof JAXBElement) {
                    JAXBElement element = (JAXBElement) response;
                    if (element.getName().equals(ObjectFactory.SOURCE_QNAME) 
                     || element.getName().equals(ObjectFactory.LAYER_QNAME)
                     || element.getName().equals(ObjectFactory.INPUT_QNAME)) {
                        final ParameterValueReader reader = new ParameterValueReader(descriptor);
                        reader.setInput(element.getValue());
                        response = reader.read();
                    } else {
                        response = ((JAXBElement) response).getValue();
                    }
                }
            } catch (JAXBException ex) {
                LOGGER.log(Level.WARNING, "The distant service does not respond correctly: unable to unmarshall response document.\ncause: {0}", ex.getMessage());
            } catch (XMLStreamException ex) {
                LOGGER.log(Level.WARNING, "The distant service does not respond correctly: unable to read xml response document.\ncause: {0}", ex.getMessage());
            }  catch (IllegalAccessError ex) {
                LOGGER.log(Level.WARNING, "The distant service does not respond correctly: unable to unmarshall response document.\ncause: {0}", ex.getMessage());
            } finally {
                if (unmarshaller != null) {
                    unmarshallerPool.release(unmarshaller);
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "The Distant service have made an error", ex);
            return null;
        }
        return response;
    }
    
    /**
     * Send a request to another service.
     *
     * @param sourceURL the URL of the distant web-service
     * @param request The XML object to send in POST mode (if null the request is GET)
     *
     * @return The object corresponding to the XML response of the distant web-service
     *
     * @throws java.net.MalformedURLException
     * @throws java.io.IOException
     * @throws org.constellation.coverage.web.CstlServiceException
     */
    protected Object sendDescriptorRequest(String sourceURL, Object request) throws MalformedURLException, IOException {

        final URL source = new URL(sourceURL);
        final URLConnection conec = source.openConnection();
        getClientSecurity().secure(conec);
        Object response = null;

        try {

            // for a POST request
            if (request != null) {

                conec.setDoOutput(true);
                conec.setRequestProperty("Content-Type", "text/xml");
                Marshaller marshaller = null;
                try {
                    marshaller = POOL.acquireMarshaller();
                    marshaller.marshal(request, conec.getOutputStream());
                } catch (JAXBException ex) {
                    LOGGER.log(Level.WARNING, "unable to marshall the request", ex);
                } finally {
                    if (marshaller != null) {
                        POOL.release(marshaller);
                    }
                }
                
            }
            try {
                final ParameterDescriptorReader reader = new ParameterDescriptorReader();
                final InputStream responseStream = AbstractRequest.openRichException(conec, securityManager);  
                reader.setInput(responseStream);
                reader.read();
                response = reader.getDescriptorsRoot();
                
            } catch (ClassNotFoundException ex) {
                LOGGER.log(Level.WARNING, "The distant service does not respond correctly: unable to read response document.\ncause: {0}", ex.getMessage());
            } catch (XMLStreamException ex) {
                LOGGER.log(Level.WARNING, "The distant service does not respond correctly: unable to read general parameter descriptor in response document.\ncause: {0}", ex.getMessage());
            }  catch (IllegalAccessError ex) {
                LOGGER.log(Level.WARNING, "The distant service does not respond correctly: unable to unmarshall response document.\ncause: {0}", ex.getMessage());
            }
        } catch (IOException ex) {
            LOGGER.severe("The Distant service have made an error");
            return null;
        }
        return response;
    }

    
}
