/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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

package org.constellation.map.configuration;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.stream.XMLStreamException;
import org.constellation.configuration.AbstractConfigurer;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.provider.LayerProvider;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.LayerProviderService;
import org.constellation.provider.StyleProviderProxy;
import org.constellation.provider.configuration.ProviderParameters;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.xml.parameter.ParameterValueReader;
import org.opengis.parameter.ParameterValueGroup;

import static org.geotoolkit.parameter.Parameters.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class DefaultMapConfigurer extends AbstractConfigurer {

    private Map<String, LayerProviderService> services = new HashMap<String, LayerProviderService>();
    
    public DefaultMapConfigurer() {
        final Collection<LayerProviderService> services = LayerProviderProxy.getInstance().getServices();
        for (LayerProviderService service: services) {
            this.services.put(service.getName(), service);
        }
    }
    
    @Override
    public Object treatRequest(String request, MultivaluedMap<String, String> parameters, final Object objectRequest) throws CstlServiceException {
        if ("addSource".equalsIgnoreCase(request)) {
            final String serviceName = getParameter("serviceName", true, parameters);
            final LayerProviderService service = this.services.get(serviceName);
            if (service != null) {
                
                final ParameterValueReader reader = new ParameterValueReader(service.getDescriptor());
                try {
                    // we read the soruce parameter to add
                    reader.setInput(objectRequest);
                    ParameterValueGroup sourceToAdd = (ParameterValueGroup) reader.read();
                    reader.dispose();
                    LayerProviderProxy.getInstance().createProvider(service, sourceToAdd);
                    
                    return new AcknowlegementType("Success", "The source has been added");
                } catch (XMLStreamException ex) {
                    throw new CstlServiceException(ex);
                } catch (IOException ex) {
                    throw new CstlServiceException(ex);
                }
            } else {
                throw new CstlServiceException("No provider service for: " + serviceName + " has been found");
            }
            
        } else if ("modifySource".equalsIgnoreCase(request)) {
            final String serviceName = getParameter("serviceName", true, parameters);
            final LayerProviderService service = services.get(serviceName);
            if (service != null) {
                
                final ParameterValueReader reader = new ParameterValueReader(service.getDescriptor());
                try {
                    // we read the soruce parameter to add
                    reader.setInput(objectRequest);
                    ParameterValueGroup sourceToModify = (ParameterValueGroup) reader.read();
                    reader.dispose();
                    final String currentId = stringValue(ProviderParameters.SOURCE_ID_DESCRIPTOR,sourceToModify);
                    
                    if (currentId != null) {
                        Collection<LayerProvider> providers = LayerProviderProxy.getInstance().getProviders();
                        for (LayerProvider p : providers) {
                            if (p.getId().equals(currentId)) {
                                p.updateSource(sourceToModify);
                                return new AcknowlegementType("Success", "The source has been modified");
                            }
                        }
                        return new AcknowlegementType("Failure", "Unable to find a source named:" + currentId);
                    } else {
                        throw new CstlServiceException("there is no ID on the source");
                    }
                    
                } catch (XMLStreamException ex) {
                    throw new CstlServiceException(ex);
                } catch (IOException ex) {
                    throw new CstlServiceException(ex);
                }
            } else {
                throw new CstlServiceException("No descriptor for: " + serviceName + " has been found");
            }
        } else if ("removeSource".equalsIgnoreCase(request)) {
            
            final String sourceId = getParameter("id", true, parameters);
            Collection<LayerProvider> providers = LayerProviderProxy.getInstance().getProviders();
            for (LayerProvider p : providers) {
                if (p.getId().equals(sourceId)) {
                     LayerProviderProxy.getInstance().removeProvider(p);
                    return new AcknowlegementType("Success", "The source has been deleted");
                }
            }
            return new AcknowlegementType("Failure", "Unable to find a source named:" + sourceId);
            
        } else if ("addLayer".equalsIgnoreCase(request)) {
            final String sourceId = getParameter("id", true, parameters);
            
                final ParameterValueReader reader = new ParameterValueReader(ProviderParameters.LAYER_DESCRIPTOR);
                try {
                    // we read the soruce parameter to add
                    reader.setInput(objectRequest);
                    ParameterValueGroup newLayer = (ParameterValueGroup) reader.read();
                    reader.dispose();
                    
                    Collection<LayerProvider> providers = LayerProviderProxy.getInstance().getProviders();
                    for (LayerProvider p : providers) {
                        if (p.getId().equals(sourceId)) {
                            p.getSource().values().add(newLayer);
                            p.updateSource(p.getSource());
                            return new AcknowlegementType("Success", "The layer has been added");
                        }
                    }
                    return new AcknowlegementType("Failure", "Unable to find a source named:" + sourceId);
                    
                    
                } catch (XMLStreamException ex) {
                    throw new CstlServiceException(ex);
                } catch (IOException ex) {
                    throw new CstlServiceException(ex);
                }
        }
        
        return null;
    }
    
    @Override
    public void beforeRestart() {
        StyleProviderProxy.getInstance().dispose();
        LayerProviderProxy.getInstance().dispose();
    }
    
}
