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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.sis.util.ArgumentChecks;
import org.constellation.admin.service.ConstellationServer.Csws;
import org.constellation.admin.service.ConstellationServer.Providers;
import org.constellation.admin.service.ConstellationServer.Services;
import org.constellation.admin.service.ConstellationServer.Tasks;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.ExceptionReport;
import org.constellation.configuration.InstanceReport;
import org.constellation.configuration.LayerList;
import org.constellation.configuration.ObjectFactory;
import org.constellation.configuration.ProvidersReport;
import org.constellation.configuration.ServiceReport;
import org.constellation.configuration.StringList;
import org.constellation.configuration.StringTreeNode;
import org.constellation.dto.Service;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.geotoolkit.client.AbstractRequest;
import org.geotoolkit.client.AbstractServer;
import org.geotoolkit.client.ServerFactory;
import org.geotoolkit.parameter.Parameters;
import org.geotoolkit.security.BasicAuthenticationSecurity;
import org.geotoolkit.sld.xml.Specification.StyledLayerDescriptor;
import org.geotoolkit.sld.xml.Specification.SymbologyEncoding;
import org.geotoolkit.sld.xml.StyleXmlIO;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.util.StringUtilities;
import org.geotoolkit.util.logging.Logging;
import org.geotoolkit.xml.MarshallerPool;
import org.geotoolkit.xml.parameter.ParameterDescriptorReader;
import org.geotoolkit.xml.parameter.ParameterValueReader;
import org.geotoolkit.xml.parameter.ParameterValueWriter;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.style.Style;
import org.opengis.util.FactoryException;

import javax.swing.event.EventListenerList;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.constellation.api.QueryConstants.REQUEST_ACCESS;
import static org.constellation.api.QueryConstants.REQUEST_ADD_TO_INDEX;
import static org.constellation.api.QueryConstants.REQUEST_AVAILABLE_SOURCE_TYPE;
import static org.constellation.api.QueryConstants.REQUEST_CLEAR_CACHE;
import static org.constellation.api.QueryConstants.REQUEST_CREATE_LAYER;
import static org.constellation.api.QueryConstants.REQUEST_CREATE_PROVIDER;
import static org.constellation.api.QueryConstants.REQUEST_CREATE_STYLE;
import static org.constellation.api.QueryConstants.REQUEST_CREATE_TASK;
import static org.constellation.api.QueryConstants.REQUEST_DELETE_LAYER;
import static org.constellation.api.QueryConstants.REQUEST_DELETE_PROVIDER;
import static org.constellation.api.QueryConstants.REQUEST_DELETE_RECORDS;
import static org.constellation.api.QueryConstants.REQUEST_DELETE_STYLE;
import static org.constellation.api.QueryConstants.REQUEST_DELETE_TASK;
import static org.constellation.api.QueryConstants.REQUEST_DOWNLOAD_STYLE;
import static org.constellation.api.QueryConstants.REQUEST_FULL_RESTART;
import static org.constellation.api.QueryConstants.REQUEST_GET_CONFIG_PATH;
import static org.constellation.api.QueryConstants.REQUEST_GET_PROCESS_DESC;
import static org.constellation.api.QueryConstants.REQUEST_GET_PROVIDER_CONFIG;
import static org.constellation.api.QueryConstants.REQUEST_GET_SERVICE_DESCRIPTOR;
import static org.constellation.api.QueryConstants.REQUEST_GET_SOURCE_DESCRIPTOR;
import static org.constellation.api.QueryConstants.REQUEST_GET_TASK_PARAMS;
import static org.constellation.api.QueryConstants.REQUEST_IMPORT_RECORDS;
import static org.constellation.api.QueryConstants.REQUEST_LIST_PROCESS;
import static org.constellation.api.QueryConstants.REQUEST_LIST_SERVICE;
import static org.constellation.api.QueryConstants.REQUEST_LIST_SERVICES;
import static org.constellation.api.QueryConstants.REQUEST_LIST_TASKS;
import static org.constellation.api.QueryConstants.REQUEST_METADATA_EXIST;
import static org.constellation.api.QueryConstants.REQUEST_REFRESH_INDEX;
import static org.constellation.api.QueryConstants.REQUEST_REMOVE_FROM_INDEX;
import static org.constellation.api.QueryConstants.REQUEST_RESTART_ALL_LAYER_PROVIDERS;
import static org.constellation.api.QueryConstants.REQUEST_RESTART_ALL_STYLE_PROVIDERS;
import static org.constellation.api.QueryConstants.REQUEST_RESTART_PROVIDER;
import static org.constellation.api.QueryConstants.REQUEST_SET_CONFIG_PATH;
import static org.constellation.api.QueryConstants.REQUEST_UPDATE_CAPABILITIES;
import static org.constellation.api.QueryConstants.REQUEST_UPDATE_LAYER;
import static org.constellation.api.QueryConstants.REQUEST_UPDATE_PROVIDER;
import static org.constellation.api.QueryConstants.REQUEST_UPDATE_STYLE;
import static org.constellation.api.QueryConstants.REQUEST_UPDATE_TASK;

/**
 * convenient class to perform actions on constellation web services.
 *
 * @author Guilhem Legal (Geomatys)
 * @author Johann Sorel (Geomatys)
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 */
public class ConstellationServer<S extends Services, P extends Providers, C extends Csws, T extends Tasks> extends AbstractServer {

    protected static final Logger LOGGER = Logging.getLogger("org.constellation.admin.service");
    private static final MarshallerPool POOL = GenericDatabaseMarshallerPool.getInstance();

    private final static String XML_POST = "XML";
    private final static String JSON_POST = "JSON";

    private final EventListenerList listeners = new EventListenerList();

    public final S services;
    public final P providers;
    public final C csws;
    public final T tasks;

    public final String currentUser;

    public ConstellationServer(final URL server, final String user, final String password) {
        super(create(ConstellationServerFactory.PARAMETERS, server, null));
        this.services = createServiceManager();
        this.providers = createProviderManager();
        this.csws = createCswManager();
        this.tasks = createTaskManager();
        Parameters.getOrCreate(ConstellationServerFactory.USER, parameters).setValue(user);
        Parameters.getOrCreate(ConstellationServerFactory.PASSWORD, parameters).setValue(password);
        Parameters.getOrCreate(ConstellationServerFactory.SECURITY, parameters).setValue(new BasicAuthenticationSecurity(user, password));
        this.currentUser = Parameters.value(ConstellationServerFactory.USER, parameters);
    }

    public ConstellationServer(ParameterValueGroup params) {
        super(params);
        this.services = createServiceManager();
        this.providers = createProviderManager();
        this.csws = createCswManager();
        this.tasks = createTaskManager();
        this.currentUser = Parameters.value(ConstellationServerFactory.USER, parameters);
        Parameters.getOrCreate(ConstellationServerFactory.SECURITY, parameters)
                .setValue(new BasicAuthenticationSecurity(
                        Parameters.value(ConstellationServerFactory.USER, params),
                        Parameters.value(ConstellationServerFactory.PASSWORD, params)));
    }

    @Override
    public ServerFactory getFactory() {
        return new ConstellationServerFactory();
    }

    protected S createServiceManager() {
        return (S) new Services();
    }

    protected T createTaskManager() {
        return (T) new Tasks();
    }

    protected C createCswManager() {
        return (C) new Csws();
    }

    protected P createProviderManager() {
        return (P) new Providers();
    }

    public static ConstellationServer login(final ParameterValueGroup value) {
        return login(Parameters.value(ConstellationServerFactory.URL, value),
                Parameters.stringValue(ConstellationServerFactory.USER, value),
                Parameters.stringValue(ConstellationServerFactory.PASSWORD, value));
    }

    public static ConstellationServer login(final String serviceURL,
                                            final String login, final String password) {
        final URL url;
        try {
            url = new URL(serviceURL);
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            return null;
        }
        return login(url, login, password);
    }

    public static ConstellationServer login(final URL serviceURL,
                                            final String login, final String password) {
        ArgumentChecks.ensureNonNull("server url", serviceURL);
        ArgumentChecks.ensureNonNull("user", login);
        ArgumentChecks.ensureNonNull("password", password);

        ConstellationServer serviceAdmin = new ConstellationServer(serviceURL, login, password);

        //check if the service and logins are valid
        if (!serviceAdmin.authenticate()) {
            //unvalid configuration
            serviceAdmin = null;
        }

        return serviceAdmin;
    }

    /**
     * Set the basic authentication for HTTP request.
     *
     * @return true if login/password are valid
     */
    protected boolean authenticate() {
        final String str = this.getURLWithEndSlash() + "configuration?request=" + REQUEST_ACCESS;
        InputStream stream = null;
        HttpURLConnection cnx = null;
        try {
            final URL url = new URL(str);
            cnx = (HttpURLConnection) url.openConnection();
            getClientSecurity().secure(cnx);
            stream = AbstractRequest.openRichException(cnx, getClientSecurity());
        } catch (Exception ex) {
            LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
            return false;
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
                }
            }
            if (cnx != null) {
                cnx.disconnect();
            }
        }
        return true;
    }

    /**
     * @return configuration path used by constellation, null if failed to get path.
     */
    public String getConfigurationPath() {
        try {
            final String url = getURLWithEndSlash() + "configuration?request=" + REQUEST_GET_CONFIG_PATH;
            final Object response = sendRequest(url, null);
            if (response instanceof AcknowlegementType) {
                final AcknowlegementType ak = (AcknowlegementType) response;
                if ("Success".equalsIgnoreCase(ak.getStatus())) {
                    return ak.getMessage();
                } else {
                    return null;
                }
            } else if (response instanceof ExceptionReport) {
                LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                return null;
            } else {
                LOGGER.warning("The service respond uncorrectly");
                return null;
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
        }
        return null;
    }

    /**
     * @param path
     * @return true if succeed
     */
    public boolean setConfigurationPath(final String path) {
        try {

            final String url = getURLWithEndSlash() + "configuration?request=" + REQUEST_SET_CONFIG_PATH + "&path=" + URLEncoder.encode(path);
            final Object response = sendRequest(url, null);
            if (response instanceof AcknowlegementType) {
                return "Success".equals(((AcknowlegementType) response).getStatus());
            } else if (response instanceof ExceptionReport) {
                LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                return false;
            } else {
                LOGGER.warning("The service respond uncorrectly");
                return false;
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
        }
        return false;
    }

    public boolean deleteUser(final String userName) {
        try {
            final String url = getURLWithEndSlash() + "configuration?request=deleteUser&username=" + userName;
            return sendRequestAck(url, null);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
        }
        return false;
    }

    public boolean updateUser(final String userName, final String password, final String oldLogin) {
        if (oldLogin == null) {
            LOGGER.warning("you must specify the old login to change it");
            return false;
        }
        try {
            final String url = getURLWithEndSlash() + "configuration?request=updateUser&username=" + userName + "&password=" + password + "&oldLogin=" + oldLogin;
            return sendRequestAck(url, null);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
        }
        return false;
    }

    public String getUserName() {
        try {
            final String url = getURLWithEndSlash() + "configuration?request=getUserName";
            final Object response = sendRequest(url, null);
            if (response instanceof AcknowlegementType) {
                final AcknowlegementType ak = (AcknowlegementType) response;
                if ("Success".equalsIgnoreCase(ak.getStatus())) {
                    return ak.getMessage();
                }
            } else if (response instanceof ExceptionReport) {
                LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                return null;
            } else {
                LOGGER.warning("The service respond uncorrectly");
                return null;
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
        }
        return null;
    }

    /**
     * Adds a {@linkplain ConstellationListener listener}.
     *
     * @param listener Listener to add
     */
    public void addListener(final ConstellationListener listener) {
        listeners.add(ConstellationListener.class, listener);
    }

    /**
     * Remove a {@linkplain ConstellationListener listener}.
     *
     * @param listener Listener to remove
     */
    public void removeListener(final ConstellationListener listener) {
        listeners.remove(ConstellationListener.class, listener);
    }

    /**
     * Fire event that a provider has been created.
     *
     * @param serviceName Service name for this provider.
     * @param config      Configuration of the provider.
     */
    private void fireProviderCreated(final String serviceName, final ParameterValueGroup config) {
        for (ConstellationListener listener : listeners.getListeners(ConstellationListener.class)) {
            listener.providerCreated(serviceName, config);
        }
    }

    /**
     * Fire event that a provider has been deleted.
     *
     * @param id Identifier of the provider to delete.
     */
    private void fireProviderDeleted(final String id) {
        for (ConstellationListener listener : listeners.getListeners(ConstellationListener.class)) {
            listener.providerDeleted(id);
        }
    }

    /**
     * Fire event that a provider has been updated.
     *
     * @param serviceName Service name for this provider.
     * @param id          Identifier of the provider to delete.
     * @param config      Configuration of the provider.
     */
    private void fireProviderUpdated(final String serviceName, final String id, final ParameterValueGroup config) {
        for (ConstellationListener listener : listeners.getListeners(ConstellationListener.class)) {
            listener.providerUpdated(serviceName, id, config);
        }
    }

    /**
     * Configuration methods for services
     */
    public final class Services {

        public Map<String, List<String>> getAvailableService() {
            try {
                final String url = getURLWithEndSlash() + "configuration?request=" + REQUEST_LIST_SERVICE;
                final Object response = sendRequest(url, null);
                if (response instanceof ServiceReport) {
                    return ((ServiceReport) response).getAvailableServices();
                } else if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                    return new HashMap<String, List<String>>();
                } else {
                    LOGGER.warning("The service respond uncorrectly");
                    return new HashMap<String, List<String>>();
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return new HashMap<String, List<String>>();
        }

        /**
         * Restart all the web-service (wms, wfs, csw,...)
         *
         * @return true if the operation succeed
         */
        public boolean restartAll() {
            try {
                final String url = getURLWithEndSlash() + "configuration?request=" + REQUEST_FULL_RESTART;
                return sendRequestAck(url, null);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return false;
        }

        /**
         * Restart all the instance of a specific web-service (wms, wfs, csw,...)
         *
         * @param service The service name to restart (wms, wfs, csw,...).
         * @return true if the operation succeed
         */
        public boolean restartAllInstance(final String service) {
            try {
                final String url = getURLWithEndSlash() + service.toLowerCase() + "/admin?request=restart";
                return sendRequestAck(url, null);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return false;
        }

        /**
         * Restart all the instance of a specific web-service (wms, wfs, csw,...)
         *
         * @param service The service name to restart (wms, wfs, csw,...).
         * @return true if the operation succeed
         */
        public boolean renameInstance(final String service, final String instanceId, final String newName) {
            try {
                final String url = getURLWithEndSlash() + service.toLowerCase() + "/admin?request=renameInstance&id=" + instanceId + "&newName=" + newName;
                return sendRequestAck(url, null);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return false;
        }

        /**
         * Restart a unique instance for the specified service  (wms, wfs, csw,...) and instance identifier.
         *
         * @param service    The service name to restart (wms, wfs, csw,...).
         * @param instanceId The instance identifier to restart.
         * @return true if the operation succeed
         */
        public boolean restartInstance(final String service, final String instanceId) {
            try {
                final String url = getURLWithEndSlash() + service.toLowerCase() + "/admin?request=restart&id=" + instanceId;
                return sendRequestAck(url, null);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return false;
        }

        /**
         * Create a new instance for the specified service  (wms, wfs, csw,...) with the specified identifier.
         *
         * @param service    The service name to restart (wms, wfs, csw,...).
         * @param instanceId The instance identifier to create.
         * @return true if the operation succeed
         */
        public boolean newInstance(String service, String instanceId) {
            try {
                final String url = getURLWithEndSlash() + service.toLowerCase() + "/admin?request=newInstance&id=" + instanceId;
                return sendRequestAck(url, null);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return false;

        }

        /**
         * Create a new instance for the specified service  (wms, wfs, csw,...) with the specified identifier.
         *
         * @param serviceType : The service type (wms, wfs, csw,...).
         * @param service     : {@link Service} DTO object to post JSON
         * @return true if the operation succeed
         */
        public boolean newInstance(String serviceType, Service service) {
            try {
                final String url = getURLWithEndSlash() + serviceType.toUpperCase() + "/metadata";

                // transform object to JSON
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(service);

                //send request
                return sendRequestAck(url, json);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "error", ex);
            }
            return false;
        }

        /**
         * Start a new instance for the specified service  (wms, wfs, csw,...) with the specified identifier.
         *
         * @param service    The service name to restart (wms, wfs, csw,...).
         * @param instanceId The instance identifier to create.
         * @return true if the operation succeed.
         */
        public boolean startInstance(final String service, final String instanceId) {
            try {
                final String url = getURLWithEndSlash() + service.toLowerCase() + "/admin?request=start&id=" + instanceId;
                return sendRequestAck(url, null);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return false;
        }

        /**
         * Stop a new instance for the specified service  (wms, wfs, csw,...) with the specified identifier.
         *
         * @param service    The service name to restart (wms, wfs, csw,...).
         * @param instanceId The instance identifier to create.
         * @return true if the operation succeed.
         */
        public boolean stopInstance(final String service, final String instanceId) {
            try {
                final String url = getURLWithEndSlash() + service.toLowerCase() + "/admin?request=stop&id=" + instanceId;
                return sendRequestAck(url, null);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return false;
        }

        /**
         * Delete a new instance for the specified service  (wms, wfs, csw,...) with the specified identifier.
         *
         * @param service    The service name to start (wms, wfs, csw,...).
         * @param instanceId The instance identifier to create.
         * @return true if the operation succeed.
         */
        public boolean deleteInstance(final String service, final String instanceId) {
            try {
                final String url = getURLWithEndSlash() + service.toLowerCase() + "/admin?request=delete&id=" + instanceId;
                return sendRequestAck(url, null);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return false;
        }

        /**
         * Ask for a report about the instances for the specified service  (wms, wfs, csw,...).
         *
         * @param service The service name to restart (wms, wfs, csw,...).
         * @return A {@link InstanceReport} about the specified service.
         */
        public InstanceReport listInstance(final String service) {
            try {
                final String url = getURLWithEndSlash() + service.toLowerCase() + "/admin?request=listInstance";
                final Object response = sendRequest(url, null);
                if (response instanceof InstanceReport) {
                    return (InstanceReport) response;
                } else if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                    return null;
                } else {
                    LOGGER.warning("The service respond uncorrectly");
                    return null;
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return null;
        }

        /**
         * Ask for a report about the instances for all service.
         *
         * @return A {@link InstanceReport} about all service.
         */
        public InstanceReport listInstance() {
            try {
                final String url = getURLWithEndSlash() + "admin/status";
                final Object response = sendRequest(url, null);
                if (response instanceof InstanceReport) {
                    return (InstanceReport) response;
                } else if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                    return null;
                } else {
                    LOGGER.warning("The service respond uncorrectly");
                    return null;
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return null;
        }

        /**
         * Send a configuration object to the specified service and instance.
         *
         * @param service       The service name to restart (wms, wfs, csw,...).
         * @param instanceId    The instance identifier to configure.
         * @param configuration A configuration object depending on the service type (for example WxS service take LayerContext object).
         * @return true if the operation succeed.
         */
        public boolean configureInstance(final String service, final String instanceId, final Object configuration) {
            try {
                final String url = getURLWithEndSlash() + service.toLowerCase() + "/admin?request=configure&id=" + instanceId;
                return sendRequestAck(url, configuration);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return false;
        }

        /**
         * Return the current configuration object for the specified service and instance.
         *
         * @param service    The service name to restart (wms, wfs, csw,...).
         * @param instanceId The instance identifier to configure.
         * @return A configuration object depending on the service type (for example WxS service return LayerContext object).
         */
        public Object getInstanceconfiguration(final String service, final String instanceId) {
            try {
                final String url = getURLWithEndSlash() + service.toLowerCase() + "/admin?request=getConfiguration&id=" + instanceId;
                final Object response = sendRequest(url, null);
                if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                    return null;
                } else if (response != null) {
                    return response;
                } else {
                    LOGGER.warning("The service respond uncorrectly");
                    return null;
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return false;
        }

        public boolean updateCapabilities(final String service, final String instanceId, final File importFile, final String fileName) {
            try {
                final String url = getURLWithEndSlash() + service.toLowerCase() + "/admin?request=" + REQUEST_UPDATE_CAPABILITIES + "&id=" + instanceId + "&filename=" + fileName;
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
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return false;
        }


        /**
         * Return a complete URL for the specified service  (wms, wfs, csw,...) and instance identifier.
         *
         * @param service    The service name (wms, wfs, csw,...).
         * @param instanceId The instance identifier.
         * @return A complete URL for the specified service.
         */
        public String getInstanceURL(final String service, final String instanceId) {
            return getURLWithEndSlash() + service.toLowerCase() + '/' + instanceId;
        }

        /**
         *
         * @param serviceType
         * @param identifier
         * @return
         */
        public Service getMetadata(final String serviceType, final String identifier){
            final String url = getURLWithEndSlash()+serviceType.toUpperCase()+"/"+identifier+"/metadata";
            try {
                Object response = sendRequest(url, null);
                if(response instanceof Service){
                    return (Service)response;
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error when send request to server", e);
            }
            return null;
        }

        /**
         *
         * @param serviceType
         * @param identifier
         * @return
         */
        public LayerList getLayers(final String serviceType, final String identifier){
            final String url = getURLWithEndSlash()+serviceType.toUpperCase()+"/"+identifier+"/layers";
            try {
                Object response = sendRequest(url, null);
                if(response instanceof LayerList){
                    return (LayerList)response;
                }
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error when send request to server", e);
            }
            return null;
        }

    }

    /**
     * Configuration methods for providers
     */
    public final class Providers {

        /**
         * Restart all layer providers.
         */
        public boolean restartAllLayerProviders() {
            try {
                final String url = getURLWithEndSlash() + "configuration?request=" + REQUEST_RESTART_ALL_LAYER_PROVIDERS;
                return sendRequestAck(url, null);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return false;
        }

        /**
         * Restart all layer providers.
         */
        public boolean restartAllStyleProviders() {
            try {
                final String url = getURLWithEndSlash() + "configuration?request=" + REQUEST_RESTART_ALL_STYLE_PROVIDERS;
                return sendRequestAck(url, null);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return false;
        }

        /**
         * Add a new source provider to the service.
         *
         * @param serviceName The provider service name (shapefile, coverage-sql, ...)
         * @param config      The configuration Object to add to the specific provider file.
         * @return
         */
        public AcknowlegementType createProvider(final String serviceName, final ParameterValueGroup config) {
            ArgumentChecks.ensureNonNull("service name", serviceName);
            ArgumentChecks.ensureNonNull("config", config);
            try {
                final String url = getURLWithEndSlash() + "configuration?request=" + REQUEST_CREATE_PROVIDER + "&serviceName=" + serviceName;
                Object response = sendRequest(url, config);
                if (response instanceof AcknowlegementType) {
                    final AcknowlegementType ack = (AcknowlegementType) response;
                    if ("Success".equals(ack.getStatus())) {
                        fireProviderCreated(serviceName, config);
                        return null;
                    } else {
                        return ack;
                    }
                } else if (response instanceof ExceptionReport) {
                    return new AcknowlegementType("Failure", ((ExceptionReport) response).getMessage());
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
                return new AcknowlegementType("Failure", ex.getMessage());
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
                final String url = getURLWithEndSlash() + "configuration?request=" + REQUEST_GET_PROVIDER_CONFIG + "&id=" + id;
                Object response = sendRequest(url, null, descriptor, null, false);
                if (response instanceof GeneralParameterValue) {
                    return (GeneralParameterValue) response;
                } else if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
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
            return deleteProvider(id, false);
        }

        /**
         * Remove a source provider in the service and eventually delete data.
         *
         * @param id The identifier of the source
         * @param deleteData {@code True} to delete the data.
         * @return
         */
        public boolean deleteProvider(final String id, final boolean deleteData) {
            try {
                final StringBuilder url = new StringBuilder();
                url.append(getURLWithEndSlash()).append("configuration?request=").append(REQUEST_DELETE_PROVIDER)
                   .append("&id=").append(id);
                if (deleteData) {
                    url.append("&deleteData=").append(deleteData);
                }
                Object response = sendRequest(url.toString(), null);
                if (response instanceof AcknowlegementType) {
                    final AcknowlegementType ack = (AcknowlegementType) response;
                    if ("Success".equals(ack.getStatus())) {
                        fireProviderDeleted(id);
                        return true;
                    } else {
                        LOGGER.log(Level.INFO, "Failure:{0}", ack.getMessage());
                    }
                } else if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return false;
        }

        /**
         * Modify a source provider in the service.
         *
         * @param serviceName The provider type (shapefile, coverage-sql, ...)
         * @param id          The identifier of the source to update.
         * @param config      The configuration Object to modify on the specific provider file.
         * @return
         */
        public boolean updateProvider(final String serviceName, final String id, final ParameterValueGroup config) {
            try {
                final String url = getURLWithEndSlash() + "configuration?request=" + REQUEST_UPDATE_PROVIDER + "&serviceName=" + serviceName + "&id=" + id;
                final Object response = sendRequest(url, config);
                if (response instanceof AcknowlegementType) {
                    final AcknowlegementType ack = (AcknowlegementType) response;
                    if ("Success".equals(ack.getStatus())) {
                        fireProviderUpdated(serviceName, id, config);
                        return true;
                    } else {
                        LOGGER.log(Level.INFO, "Failure:{0}", ack.getMessage());
                    }
                } else if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return false;
        }

        /**
         * Reload a source provider in the service.
         *
         * @param id The identifier of the source
         * @return
         */
        public boolean restartProvider(final String id) {
            try {
                final String url = getURLWithEndSlash() + "configuration?request=" + REQUEST_RESTART_PROVIDER + "&id=" + id;
                return sendRequestAck(url, null);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return false;
        }


        // LAYER PROVIDERS ACTIONS /////////////////////////////////////////////

        /**
         * Add a new layer to a source provider in the service.
         *
         * @param id The identifier of the provider
         * @return
         */
        public boolean createLayer(final String id, final ParameterValueGroup config) {
            ArgumentChecks.ensureNonNull("id", id);
            ArgumentChecks.ensureNonNull("config", config);
            try {
                final String url = getURLWithEndSlash() + "configuration?request=" + REQUEST_CREATE_LAYER + "&id=" + id;
                return sendRequestAck(url, config);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
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
                final String url = getURLWithEndSlash() + "configuration?request=" + REQUEST_DELETE_LAYER + "&id=" + id + "&layerName=" + layerName;
                return sendRequestAck(url, null);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return false;
        }

        /**
         * Add a new layer to a source provider in the service.
         *
         * @param id The identifier of the provider
         * @return
         */
        public boolean updateLayer(final String id, final String layerName, final ParameterValueGroup layer) {
            try {
                final String url = getURLWithEndSlash() + "configuration?request=" + REQUEST_UPDATE_LAYER + "&id=" + id + "&layerName=" + layerName;
                return sendRequestAck(url, layer);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return false;
        }

        // STYLE PROVIDERS ACTIONS /////////////////////////////////////////////

        public MutableStyle downloadStyle(final String id, final String styleName) {
            ArgumentChecks.ensureNonNull("id", id);
            ArgumentChecks.ensureNonNull("styleName", styleName);

            try {
                final String url = getURLWithEndSlash() + "configuration?request=" + REQUEST_DOWNLOAD_STYLE + "&id=" + id + "&styleName=" + styleName;
                Object response = sendRequest(url, null, null, StyleXmlIO.getJaxbContext110(), false);

                if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                } else {
                    final StyleXmlIO utils = new StyleXmlIO();
                    return utils.readStyle(response, SymbologyEncoding.V_1_1_0);
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            } catch (JAXBException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            } catch (FactoryException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return null;
        }

        /**
         * @param id
         * @param style : SLD or other
         * @return null if successful, AcknowlegementType if failed
         */
        public boolean createStyle(final String id, final String styleName, final Object style) {
            ArgumentChecks.ensureNonNull("id", id);
            ArgumentChecks.ensureNonNull("styleName", styleName);
            ArgumentChecks.ensureNonNull("style", style);
            try {
                final String url = getURLWithEndSlash() + "configuration?request=" + REQUEST_CREATE_STYLE + "&id=" + id + "&styleName=" + styleName;
                return sendRequestAck(url, style);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return false;
        }

        /**
         * @param id        : provider id
         * @param styleName : style id
         * @return true if successful
         */
        public boolean deleteStyle(final String id, final String styleName) {
            ArgumentChecks.ensureNonNull("id", id);
            ArgumentChecks.ensureNonNull("styleName", styleName);

            try {
                final String url = getURLWithEndSlash() + "configuration?request=" + REQUEST_DELETE_STYLE + "&id=" + id + "&styleName=" + styleName;
                return sendRequestAck(url, null);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return false;
        }

        /**
         * Update a style
         *
         * @param id        The identifier of the provider
         * @param styleName The identifier of the style
         * @param style     The new style definition
         * @return
         */
        public boolean updateStyle(final String id, final String styleName, final Object style) {
            ArgumentChecks.ensureNonNull("id", id);
            ArgumentChecks.ensureNonNull("styleName", styleName);
            ArgumentChecks.ensureNonNull("style", style);
            try {
                final String url = getURLWithEndSlash() + "configuration?request=" + REQUEST_UPDATE_STYLE + "&id=" + id + "&styleName=" + styleName;
                return sendRequestAck(url, style);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return false;
        }

        /**
         * Get the provider service configuration description.
         *
         * @return
         */
        public GeneralParameterDescriptor getServiceDescriptor(final String serviceName) {
            try {
                final String url = getURLWithEndSlash() + "configuration?request=" + REQUEST_GET_SERVICE_DESCRIPTOR + "&serviceName=" + serviceName;
                Object response = sendDescriptorRequest(url, null);
                if (response instanceof GeneralParameterDescriptor) {
                    return (GeneralParameterDescriptor) response;
                } else if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                } else {
                    LOGGER.log(Level.WARNING, "Unexpected response type :{0}", response);
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return null;
        }

        /**
         * Get the provider service source configuration description.
         *
         * @return
         */
        public GeneralParameterDescriptor getSourceDescriptor(final String serviceName) {
            try {
                final String url = getURLWithEndSlash() + "configuration?request=" + REQUEST_GET_SOURCE_DESCRIPTOR + "&serviceName=" + serviceName;
                Object response = sendDescriptorRequest(url, null);
                if (response instanceof GeneralParameterDescriptor) {
                    return (GeneralParameterDescriptor) response;
                } else if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                } else {
                    LOGGER.log(Level.WARNING, "Unexpected response type :{0}", response);
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return null;
        }

        public ProvidersReport listProviders() {
            try {
                final String url = getURLWithEndSlash() + "configuration?request=" + REQUEST_LIST_SERVICES;
                final Object response = sendRequest(url, null);
                if (response instanceof ProvidersReport) {
                    return (ProvidersReport) response;
                } else if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                    return null;
                } else {
                    LOGGER.warning("The service respond uncorrectly");
                    return null;
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return null;
        }

    }

    /**
     * Configuration methods for task scheduler.
     */
    public final class Tasks {

        /**
         * Ask for a list of all available process.
         */
        public StringList listProcess() {
            try {
                final String url = getURLWithEndSlash() + "configuration?request=" + REQUEST_LIST_PROCESS;
                final Object response = sendRequest(url, null);
                if (response instanceof StringList) {
                    return (StringList) response;
                } else if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                    return null;
                } else {
                    LOGGER.warning("The service respond uncorrectly");
                    return null;
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return null;
        }

        /**
         * Ask for a list of all tasks.
         */
        public StringTreeNode listTasks() {
            try {
                final String url = getURLWithEndSlash() + "configuration?request=" + REQUEST_LIST_TASKS;
                final Object response = sendRequest(url, null);
                if (response instanceof StringTreeNode) {
                    return (StringTreeNode) response;
                } else if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                    return null;
                } else {
                    LOGGER.warning("The service respond uncorrectly");
                    return null;
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return null;
        }

        /**
         * Get the parameters description for the given process.
         */
        public GeneralParameterDescriptor getProcessDescriptor(final String authority, final String code) {
            try {
                final String url = getURLWithEndSlash() + "configuration?request=" + REQUEST_GET_PROCESS_DESC + "&authority=" + authority + "&code=" + code;
                Object response = sendDescriptorRequest(url, null);
                if (response instanceof GeneralParameterDescriptor) {
                    return (GeneralParameterDescriptor) response;
                } else if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                } else {
                    LOGGER.log(Level.WARNING, "Unexpected response type :{0}", response);
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return null;
        }

        /**
         * Get the parameters for the given task
         *
         * @param id
         * @return
         */
        public GeneralParameterValue getTaskParameters(final String id, ParameterDescriptorGroup desc) {
            try {
                final String url = getURLWithEndSlash() + "configuration?request=" + REQUEST_GET_TASK_PARAMS + "&id=" + id;
                Object response = sendRequest(url, null, desc, null, false);
                if (response instanceof GeneralParameterValue) {
                    return (GeneralParameterValue) response;
                } else if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                } else {
                    LOGGER.log(Level.WARNING, "Unexpected response type :{0}", response);
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
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
                                  final String title, final int step, final GeneralParameterValue parameters) {
            ArgumentChecks.ensureNonNull("authority", authority);
            ArgumentChecks.ensureNonNull("code", code);
            ArgumentChecks.ensureNonNull("id", id);
            ArgumentChecks.ensureNonNull("title", title);
            ArgumentChecks.ensureNonNull("step", step);
            ArgumentChecks.ensureNonNull("parameters", parameters);
            try {
                final String url = getURLWithEndSlash() + "configuration?request=" + REQUEST_CREATE_TASK
                        + "&authority=" + authority
                        + "&code=" + code
                        + "&id=" + id
                        + "&title=" + URLEncoder.encode(title, "UTF-8")
                        + "&step=" + step;
                return sendRequestAck(url, parameters);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
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
                                  final String title, final int step, final GeneralParameterValue parameters) {
            ArgumentChecks.ensureNonNull("authority", authority);
            ArgumentChecks.ensureNonNull("code", code);
            ArgumentChecks.ensureNonNull("id", id);
            ArgumentChecks.ensureNonNull("title", title);
            ArgumentChecks.ensureNonNull("step", step);
            ArgumentChecks.ensureNonNull("parameters", parameters);
            try {
                final String url = getURLWithEndSlash() + "configuration?request=" + REQUEST_UPDATE_TASK
                        + "&authority=" + authority
                        + "&code=" + code
                        + "&id=" + id
                        + "&title=" + title
                        + "&step=" + step;
                return sendRequestAck(url, parameters);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return false;
        }


        /**
         * Delete an existing task.
         */
        public boolean deleteTask(final String id) {
            ArgumentChecks.ensureNonNull("id", id);
            try {
                final String url = getURLWithEndSlash().toString() + "configuration?request=" + REQUEST_DELETE_TASK + "&id=" + id;
                return sendRequestAck(url, null);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
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
                final String url = getURLWithEndSlash() + "configuration?request=" + REQUEST_REFRESH_INDEX + "&id=" + id + "&asynchrone=" + asynchrone;
                return sendRequestAck(url, null);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return false;
        }

        public boolean addToIndex(final String id, final List<String> identifiers) {
            try {
                final String idList = StringUtilities.toCommaSeparatedValues(identifiers);
                final String url = getURLWithEndSlash() + "configuration?request=" + REQUEST_ADD_TO_INDEX + "&id=" + id + "&identifiers=" + idList;
                return sendRequestAck(url, null);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return false;
        }

        public boolean removeFromIndex(final String id, final List<String> identifiers) {
            try {
                final String idList = StringUtilities.toCommaSeparatedValues(identifiers);
                final String url = getURLWithEndSlash() + "configuration?request=" + REQUEST_REMOVE_FROM_INDEX + "&id=" + id + "&identifiers=" + idList;
                return sendRequestAck(url, null);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return false;
        }

        public boolean clearCache(final String id) {
            try {
                final String url = getURLWithEndSlash() + "csw/admin?request=" + REQUEST_CLEAR_CACHE + "&id=" + id;
                return sendRequestAck(url, null);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return false;
        }

        public boolean importFile(final String id, final File importFile, final String fileName) {
            try {
                final String url = getURLWithEndSlash() + "configuration?request=" + REQUEST_IMPORT_RECORDS + "&id=" + id + "&filename=" + fileName;
                return sendRequestAck(url, importFile, null, null, true);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return false;
        }

        public boolean metadataExist(final String id, final String metadataName) {
            try {
                final String url = getURLWithEndSlash() + "configuration?request=" + REQUEST_METADATA_EXIST + "&id=" + id + "&metadata=" + metadataName;
                Object response = sendRequest(url, null);
                if (response instanceof AcknowlegementType) {
                    final AcknowlegementType ack = (AcknowlegementType) response;
                    if ("Exist".equals(ack.getStatus())) {
                        return true;
                    } else if ("Not Exist".equals(ack.getStatus())) {
                        return false;
                    } else {
                        LOGGER.log(Level.WARNING, "Failure:{0}", ack.getMessage());
                    }
                } else if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return false;
        }

        public boolean deleteMetadata(final String id, final String metadataName) {
            try {
                final String url = getURLWithEndSlash() + "configuration?request=" + REQUEST_DELETE_RECORDS + "&id=" + id + "&metadata=" + metadataName;
                return sendRequestAck(url, null);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return false;
        }

        public Collection<String> getAvailableDataSourceType() {
            try {
                final String url = getURLWithEndSlash() + "configuration?request=" + REQUEST_AVAILABLE_SOURCE_TYPE;
                Object response = sendRequest(url, null);
                if (response instanceof StringList) {
                    final StringList sl = (StringList) response;
                    return sl.getList();
                } else if (response instanceof ExceptionReport) {
                    LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            return new ArrayList<String>();
        }
    }


    // convinient methods //////////////////////////////////////////////////////

    protected boolean sendRequestAck(String sourceURL, Object request, ParameterDescriptorGroup descriptor,
                                     MarshallerPool unmarshallerPool, boolean put) throws MalformedURLException, IOException {
        final Object response = sendRequest(sourceURL, request, descriptor, unmarshallerPool, put);
        if (response instanceof AcknowlegementType) {
            final AcknowlegementType ak = (AcknowlegementType) response;
            if ("Success".equalsIgnoreCase(ak.getStatus())) {
                return true;
            } else {
                LOGGER.log(Level.WARNING, "Failure:{0}", ak.getMessage());
            }
        } else if (response instanceof ExceptionReport) {
            LOGGER.log(Level.WARNING, "The service return an exception:{0}", ((ExceptionReport) response).getMessage());
        } else {
            LOGGER.warning("The service respond uncorrectly");
        }
        return false;
    }

    protected boolean sendRequestAck(String sourceURL, Object request) throws MalformedURLException, IOException {
        return sendRequestAck(sourceURL, request, null, null, false);
    }

    protected Object sendRequest(String sourceURL, Object request) throws MalformedURLException, IOException {
        return sendRequest(sourceURL, request, null, null, false);
    }

    /**
     * Send a request to another service.
     *
     * @param sourceURL the URL of the distant web-service
     * @param request   The XML object to send in POST mode (if null the request is GET)
     * @return The object corresponding to the XML response of the distant web-service
     * @throws java.net.MalformedURLException
     * @throws java.io.IOException
     */
    protected Object sendRequest(String sourceURL, Object request, ParameterDescriptorGroup descriptor,
                                 MarshallerPool unmarshallerPool, boolean put) throws MalformedURLException, IOException {

        //fix possible not correctly encoded url parameters.
        final int index = sourceURL.indexOf('?');
        if (index > 0) {
            String params = sourceURL.substring(index + 1);
            final StringBuilder sb = new StringBuilder();
            final String[] parts = params.split("&");
            for (int i = 0; i < parts.length; i++) {
                String part = parts[i];
                final int sepi = part.indexOf('=');
                if (sepi > 0) {
                    sb.append(URLEncoder.encode(part.substring(0, sepi), "UTF-8"));
                    sb.append('=');
                    sb.append(URLEncoder.encode(part.substring(sepi + 1), "UTF-8"));

                } else {
                    part = URLEncoder.encode(part, "UTF-8");
                    sb.append(part);
                }

                if (i < parts.length - 1) {
                    sb.append('&');
                }
            }
            params = sb.toString();
            sourceURL = sourceURL.substring(0, index + 1) + params;
        }

        final URL source = new URL(sourceURL);
        final HttpURLConnection conec = (HttpURLConnection) source.openConnection();
        conec.setRequestProperty("Accept", "application/xml");
        getClientSecurity().secure(conec);
        applySessionId(conec);

        try {

            // for a POST request
            if (request != null) {

                conec.setDoOutput(true);
                if (put) {
                    conec.setRequestMethod("PUT");
                }

                //if request is String, it's a json part
                if (request instanceof String) {
                    conec.setRequestProperty("Accept", "application/json");
                    String sRequest = (String) request;
                    doJsonPost(sRequest, conec);

                } else {
                    doXMLPost(request, conec);

                }
            }
            
            return readResponse(descriptor, unmarshallerPool, conec);

        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "The Distant service have made an error", ex);
            return null;
        }
    }

    /**
     * Unmarshall server response.
     *
     * @param descriptor used to transform XML
     * @param unmarshallerPool Pool to load unmarshaller
     * @param conec {@link HttpURLConnection} to access to constellation server side
     * @return an object which contains service state (create or not for example)
     * @throws IOException
     */
    private Object readResponse(ParameterDescriptorGroup descriptor, MarshallerPool unmarshallerPool, HttpURLConnection conec) throws IOException {
        if (unmarshallerPool == null) {
            //use default pool
            unmarshallerPool = POOL;
        }

        Object response = null;
        readSessionId(conec);
        Unmarshaller unmarshaller = null;
        try {
            unmarshaller = unmarshallerPool.acquireUnmarshaller();
            final InputStream responseStream = AbstractRequest.openRichException(conec, getClientSecurity());
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
        } catch (IllegalAccessError ex) {
            LOGGER.log(Level.WARNING, "The distant service does not respond correctly: unable to unmarshall response document.\ncause: {0}", ex.getMessage());
        } finally {
            if (unmarshaller != null) {
                unmarshallerPool.release(unmarshaller);
            }
        }
        return response;
    }

    /**
     * Send request with inner JSON
     * @param request JSON object which define object to send
     * @param conec {@link HttpURLConnection} to access to constellation server side
     */
    private void doJsonPost(String request, HttpURLConnection conec) {
        try {
            //Define request
            conec.setRequestMethod("POST");
            conec.setRequestProperty("Content-Type", "application/json");

            //Send request
            OutputStream os = conec.getOutputStream();
            os.write(request.getBytes());
            os.flush();
            if (conec.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conec.getResponseCode());
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "", e);
        }
    }


    /**
     * Send request with inner XML
     *
     * @param request object which will be send as XML
     * @param conec {@link HttpURLConnection} to access to constellation server side
     * @throws IOException
     */
    private void doXMLPost(Object request, HttpURLConnection conec) throws IOException {
        conec.setRequestProperty("Content-Type", "text/xml");

        if (request instanceof GeneralParameterValue) {
            final ParameterValueWriter writer = new ParameterValueWriter();
            try {
                writer.setOutput(conec.getOutputStream());
                writer.write((GeneralParameterValue) request);
            } catch (XMLStreamException ex) {
                LOGGER.log(Level.WARNING, "unable to marshall the request", ex);
            }
        } else if (request instanceof Style) {
            final StyleXmlIO util = new StyleXmlIO();
            try {
                util.writeStyle(conec.getOutputStream(), (Style) request, StyledLayerDescriptor.V_1_1_0);
            } catch (JAXBException ex) {
                LOGGER.log(Level.WARNING, "unable to marshall the request", ex);
            }
        } else if (request instanceof org.opengis.sld.StyledLayerDescriptor) {
            final StyleXmlIO util = new StyleXmlIO();
            try {
                util.writeSLD(conec.getOutputStream(),
                        (org.opengis.sld.StyledLayerDescriptor) request, StyledLayerDescriptor.V_1_1_0);
            } catch (JAXBException ex) {
                LOGGER.log(Level.WARNING, "unable to marshall the request", ex);
            }
        } else if (request instanceof File) {
            final FileInputStream in = new FileInputStream((File) request);
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

    /**
     * Send a request to another service.
     *
     * @param sourceURL the URL of the distant web-service
     * @param request   The XML object to send in POST mode (if null the request is GET)
     * @return The object corresponding to the XML response of the distant web-service
     * @throws java.net.MalformedURLException
     * @throws java.io.IOException
     */
    protected Object sendDescriptorRequest(String sourceURL, Object request) throws MalformedURLException, IOException {

        final URL source = new URL(sourceURL);
        final URLConnection conec = source.openConnection();
        getClientSecurity().secure(conec);
        Object response = null;
        applySessionId(conec);

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
                readSessionId(conec);
                final ParameterDescriptorReader reader = new ParameterDescriptorReader();
                final InputStream responseStream = AbstractRequest.openRichException(conec, getClientSecurity());
                reader.setInput(responseStream);
                reader.read();
                response = reader.getDescriptorsRoot();

            } catch (ClassNotFoundException ex) {
                LOGGER.log(Level.WARNING, "The distant service does not respond correctly: unable to read response document.\ncause: {0}", ex.getMessage());
            } catch (XMLStreamException ex) {
                LOGGER.log(Level.WARNING, "The distant service does not respond correctly: unable to read general parameter descriptor in response document.\ncause: {0}", ex.getMessage());
            } catch (IllegalAccessError ex) {
                LOGGER.log(Level.WARNING, "The distant service does not respond correctly: unable to unmarshall response document.\ncause: {0}", ex.getMessage());
            }
        } catch (IOException ex) {
            LOGGER.severe("The Distant service have made an error");
            return null;
        }
        return response;
    }

    protected String getURLWithEndSlash() {
        return (serverURL.toString().endsWith("/") ? serverURL.toString() : serverURL.toString() + '/');
    }
}
