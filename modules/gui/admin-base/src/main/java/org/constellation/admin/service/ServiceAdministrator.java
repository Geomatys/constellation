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

import java.io.IOException;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.ExceptionReport;
import org.constellation.configuration.InstanceReport;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.geotoolkit.util.logging.Logging;
import org.geotoolkit.xml.MarshallerPool;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class ServiceAdministrator {

    private static final Logger LOGGER = Logging.getLogger("org.constellation.admin.service");

    private static final MarshallerPool POOL = GenericDatabaseMarshallerPool.getInstance();

    /**
     * Set the basic authentication for HTTP request.
     *
     * @param userName The login of the user.
     * @param password The password of the user
     */
    public static void authenticate(final String userName, final String password) {
        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userName, password.toCharArray());
            }
        });
    }

    /**
     * Return the base URL of the web-services.
     */
    public static String getServiceURL() {
        final HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String result = null;
        try {
            final String pathUrl = request.getRequestURL().toString();
            final URL url = new URL(pathUrl);
            result = url.getProtocol() + "://" + url.getAuthority() + request.getContextPath() + "/WS/";
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        return result;
    }

    /**
     * Return a complete URL for the specified service  (wms, wfs, csw,...) and instance identifier.
     *
     * @param service The service name (wms, wfs, csw,...).
     * @param instanceId The instance identifier.
     *
     * @return A complete URL for the specified service.
     */
    public static String getInstanceURL(final String service, final String instanceId) {
        return getServiceURL() + service.toLowerCase() + '/' + instanceId;
    }

    /**
     * Restart all the web-service (wms, wfs, csw,...)
     *
     * @return true if the operation succeed
     */
    public static boolean restartAll() {
        try {
            final String url = getServiceURL() + "configuration?request=restart";
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
    public static boolean restartAllInstance(final String service) {
        try {
            final String url = getServiceURL() + service.toLowerCase() + "/admin?request=restart";
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
    public static boolean restartInstance(final String service, final String instanceId) {
        try {
            String url = getServiceURL() + service.toLowerCase() + "/admin?request=restart&id=" + instanceId;
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
    public static boolean newInstance(String service, String instanceId) {
        try {
            String url = getServiceURL() + service.toLowerCase() + "/admin?request=newInstance&id=" + instanceId;
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
    public static boolean startInstance(final String service, final String instanceId) {
        try {
            String url = getServiceURL() + service.toLowerCase() + "/admin?request=start&id=" + instanceId;
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
    public static boolean stopInstance(final String service, final String instanceId) {
        try {
            String url = getServiceURL() + service.toLowerCase() + "/admin?request=stop&id=" + instanceId;
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
    public static boolean deleteInstance(final String service, final String instanceId) {
        try {
            String url = getServiceURL() + service.toLowerCase() + "/admin?request=delete&id=" + instanceId;
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
    public static InstanceReport listInstance(final String service) {
        try {
            String url = getServiceURL() + service.toLowerCase() + "/admin?request=listInstance";
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
    public static boolean configureInstance(final String service, final String instanceId, final Object configuration) {
        try {
            String url = getServiceURL() + service.toLowerCase() + "/admin?request=configure&id=" + instanceId;
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
    public static Object getInstanceconfiguration(final String service, final String instanceId) {
        try {
            String url = getServiceURL() + service.toLowerCase() + "/admin?request=getConfiguration&id=" + instanceId;
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
    private static Object sendRequest(String sourceURL, Object request) throws MalformedURLException, IOException {

        final URL source          = new URL(sourceURL);
        final URLConnection conec = source.openConnection();
        Object response    = null;

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
            Unmarshaller unmarshaller = null;
            try {
                unmarshaller = POOL.acquireUnmarshaller();
                response = unmarshaller.unmarshal(conec.getInputStream());
                if (response instanceof JAXBElement) {
                    response = ((JAXBElement) response).getValue();
                }
            } catch (JAXBException ex) {
                LOGGER.log(Level.WARNING, "The distant service does not respond correctly: unable to unmarshall response document.\ncause: {0}", ex.getMessage());
            }  catch (IllegalAccessError ex) {
                LOGGER.log(Level.WARNING, "The distant service does not respond correctly: unable to unmarshall response document.\ncause: {0}", ex.getMessage());
            } finally {
                if (unmarshaller != null) {
                    POOL.release(unmarshaller);
                }
            }
        } catch (IOException ex) {
            LOGGER.severe("The Distant service have made an error");
            return null;
        }
        return response;
    }


}
