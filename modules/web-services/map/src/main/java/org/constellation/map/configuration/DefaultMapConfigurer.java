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

package org.constellation.map.configuration;

import java.io.InputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.constellation.provider.*;

import org.constellation.configuration.AbstractConfigurer;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.provider.configuration.ProviderParameters;
import org.constellation.ws.CstlServiceException;

import org.geotoolkit.xml.parameter.ParameterValueReader;

import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import static org.constellation.api.QueryConstants.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @author Johann Sorel (Geomatys)
 */
public class DefaultMapConfigurer extends AbstractConfigurer {

    private final Map<String, ProviderFactory> services = new HashMap<>();

    private ProviderOperationListener providerListener;
    
    public DefaultMapConfigurer() {
        this(new DefaultProviderOperationListener());
    }
    
    public DefaultMapConfigurer(final ProviderOperationListener providerListener) {
        final Collection<DataProviderFactory> availableLayerServices = DataProviders.getInstance().getFactories();
        for (DataProviderFactory service: availableLayerServices) {
            this.services.put(service.getName(), service);
        }
        final Collection<StyleProviderFactory> availableStyleServices = StyleProviders.getInstance().getFactories();
        for (StyleProviderFactory service: availableStyleServices) {
            this.services.put(service.getName(), service);
        }
        this.providerListener = providerListener;
    }

    @Override
    public boolean needCustomUnmarshall(final String request, MultivaluedMap<String, String> parameters) {

       /* if ( REQUEST_CREATE_STYLE.equalsIgnoreCase(request)
          || REQUEST_UPDATE_STYLE.equalsIgnoreCase(request)) {
            return true;
        }*/

        return super.needCustomUnmarshall(request,parameters);
    }

    @Override
    public Object unmarshall(final String request, final MultivaluedMap<String, String> parameters,
            final InputStream stream) throws JAXBException, CstlServiceException {

        /*if ( REQUEST_CREATE_STYLE.equalsIgnoreCase(request)
          || REQUEST_UPDATE_STYLE.equalsIgnoreCase(request)) {

            final StyleXmlIO util = new StyleXmlIO();
            try {
                return util.readStyle(stream, SymbologyEncoding.V_1_1_0);
            } catch (FactoryException ex) {
                throw new JAXBException(ex.getMessage(),ex);
            }
        }*/

        return super.unmarshall(request, parameters, stream);
    }

    @Override
    public Object treatRequest(final String request, final MultivaluedMap<String, String> parameters, final Object objectRequest) throws CstlServiceException {

        

        return null;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void beforeRestart() {
        StyleProviders.getInstance().dispose();
        DataProviders.getInstance().dispose();
    }

}
