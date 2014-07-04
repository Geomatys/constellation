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

package org.constellation.ws;

import java.util.List;
import java.util.logging.Level;
import javax.xml.validation.Schema;
import org.constellation.ServiceDef;
import org.apache.sis.util.Version;

/**
 * Generic definition of a worker.
 *
 * @author Johann Sorel (Geomatys)
 */
public interface Worker {
    /**
     * Destroy the worker and free the resources.
     */
    void destroy();

    /**
     * Initialize the serviceUrl of the service.
     */
    void setServiceUrl(final String serviceUrl);

    /**
     * Set The Logging level for all the info message in the worker
     *
     * @param logLevel
     */
    void setLogLevel(Level logLevel);
    
    void setShiroAccessible(final boolean shiroAccessible);

    /**
     * Return the identifier of the worker.
     * @return
     */
    String getId();

    /**
     * Returns {@code true} if the worker is secured, {@code false} otherwise.
     */
    boolean isSecured();

    /**
     * Returns {@code true} if the worker is working, {@code false} otherwise.
     */
    boolean isStarted();

    /**
     * Verifies if the ip or referrer is authorized to request the service or not.
     * Should delegate the verification to the Policy Desicion Point (PDP).
     *
     * @param ip The IP address of the requestor.
     * @param referer The referrer identifier of the requestor.
     * @return {@code True} if the requestor is authorized, {@code false} otherwise.
     */
    boolean isAuthorized(String ip, String referer);
    
    boolean isPostRequestLog();
    
    /**
     * If this flag is set r false the method logParameters() will write nothing in the logs
     */
    boolean isPrintRequestParameter();

    /**
     * Return the service URL.
     * @return String
     */
    String getServiceUrl();
    
    /**
     * Return a Version Object from the version number.
     * if the version number is not correct return the default version.
     *
     * @param number the version number.
     * @return
     */
    ServiceDef getVersionFromNumber(final Version number);
    
    ServiceDef getVersionFromNumber(final String number);
    
    /**
     * If the requested version number is not available we choose the best version to return.
     *
     * @param number A version number, which will be compared to the ones specified.
     *               Can be {@code null}, in this case the best version specified is just returned.
     * @return The best version (the highest one) specified for this web service.
     * 
     */
    ServiceDef getBestVersion(final String number);
    
    /**
     * if this flag is set the request received will be validated against a XSD schema
     * @return 
     */
    boolean isRequestValidationActivated();
    
    List<Schema> getRequestValidationSchema();
    
    void checkVersionSupported(final String version, final boolean getCapabilities) throws CstlServiceException;

    Object getConfiguration();
}
