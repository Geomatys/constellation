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
package org.constellation.configuration;

import java.io.InputStream;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBException;

import org.constellation.ws.CstlServiceException;
import static org.constellation.ws.ExceptionCode.*;

import org.apache.sis.util.logging.Logging;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public abstract class AbstractConfigurer {

    protected static final Logger LOGGER = Logging.getLogger("org.constellation.configuration.ws.rs");

    /**
     * Extracts the value, for a parameter specified, from a query.
     *
     * @param parameterName The name of the parameter.
     *
     * @return the parameter, or {@code null} if not specified.
     */
    private List<String> getParameter(final String parameterName, final MultivaluedMap<String,String> parameters) {
        List<String> values = parameters.get(parameterName);

        //maybe the parameterName is case sensitive.
        if (values == null) {
            for(final Entry<String, List<String>> key : parameters.entrySet()){
                if(key.getKey().equalsIgnoreCase(parameterName)){
                    values = key.getValue();
                    break;
                }
            }
        }
        return values;
    }

    /**
     * Extracts the value, for a parameter specified, from a query.
     * If it is a mandatory one, and if it is {@code null}, it throws an exception.
     * Otherwise returns {@code null} in the case of an optional parameter not found.
     * The parameter is then parsed as boolean.
     *
     * @param parameterName The name of the parameter.
     * @param mandatory true if this parameter is mandatory, false if its optional.
      *
     * @return the parameter, or {@code null} if not specified and not mandatory.
     * @throw CstlServiceException
     */
    protected boolean getBooleanParameter(final String parameterName, final boolean mandatory, final MultivaluedMap<String,String> parameters) throws CstlServiceException {
        return Boolean.parseBoolean(getParameter(parameterName, mandatory, parameters));
    }

    /**
     * Extracts the value, for a parameter specified, from a query.
     * If it is a mandatory one, and if it is {@code null}, it throws an exception.
     * Otherwise returns {@code null} in the case of an optional parameter not found.
     *
     * @param parameterName The name of the parameter.
     * @param mandatory true if this parameter is mandatory, false if its optional.
      *
     * @return the parameter, or {@code null} if not specified and not mandatory.
     * @throw CstlServiceException
     */
    protected String getParameter(final String parameterName, final boolean mandatory, final MultivaluedMap<String,String> parameters) throws CstlServiceException {

        final List<String> values = getParameter(parameterName, parameters);
        if (values == null) {
            if (mandatory) {
                throw new CstlServiceException("The parameter " + parameterName + " must be specified",
                        MISSING_PARAMETER_VALUE, parameterName.toLowerCase());
            }
            return null;
        } else {
            final String value = values.get(0);
            if ((value == null || value.isEmpty()) && mandatory) {
                throw new CstlServiceException("The parameter " + parameterName + " should have a value",
                        MISSING_PARAMETER_VALUE, parameterName.toLowerCase());
            } else {
                return value;
            }
        }
    }

    /**
     * Check if the configurer can handle the given request.
     *
     * @param request : request name
     * @return true if the request is supported
     */
    public boolean needCustomUnmarshall(final String request, final MultivaluedMap<String,String> parameters){
        return false;
    }

    public Object unmarshall(final String request, final MultivaluedMap<String,String> parameters, InputStream stream)
            throws JAXBException, CstlServiceException{
        return null;
    }

    public abstract Object treatRequest(final String request, final MultivaluedMap<String,String> parameters, final Object objectRequest) throws CstlServiceException;

    /**
     * Return true if the restart must be refused.
     */
    public boolean isLock() {
        return false;
    }

    /**
     * destroy all the resource and close the connections.
     */
    public void destroy() {
       // do nothing must be overriden if needed
    }

    /**
     * Stop operation going on because the service is going to be restarted.
     */
    public void closeForced() {
        // do nothing must be overriden if needed
    }

    /**
     * Do specific work before a service restart
     */
    public void beforeRestart() {
        // do nothing must be overriden if needed
    }
}
