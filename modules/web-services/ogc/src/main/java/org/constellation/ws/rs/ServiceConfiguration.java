/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
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
package org.constellation.ws.rs;

import org.constellation.configuration.Layer;
import org.constellation.dto.AddLayer;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.Worker;

import java.io.File;
import java.util.List;

/**
 * Describe methods which need to be specify by an implementation to manage service (create, set configuration, etc...)
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 *
 */
public interface ServiceConfiguration {

    /**
     * Give {@link org.constellation.ws.Worker} for {@link ServiceConfiguration} implementation.
     * @see org.constellation.ws.Worker
     * @return a {@link org.constellation.ws.Worker} implementation {@link Class}
     */
    public Class getWorkerClass();

    public void setWorkerClass(final Class c);

    /**
     * Create a new File containing the specific object sent.
     *
     * @param instanceDirectory The directory containing the instance configuration files.
     * @param configuration A service specific configuration Object.
     * @param capabilitiesConfiguration an object to define capabilities. can be <code>null</code>
     * @param serviceType service which want create
     *
     * @throws CstlServiceException if they have an error on configure process
     */
    public void configureInstance(final File instanceDirectory, final Object configuration, final Object capabilitiesConfiguration, final String serviceType) throws CstlServiceException;

    /**
     * Return the configuration object of the instance.
     *
     * @param instanceDirectory The directory containing the instance configuration files.
     * @param serviceType instance service type
     *
     * @return a configuration object
     *
     * @throws CstlServiceException if they have an error on configure process
     */
    public Object getInstanceConfiguration(final File instanceDirectory, final String serviceType) throws CstlServiceException;

    /**
     * create an empty configuration for the service.
     *
     * @param instanceDirectory The directory containing the instance configuration files.
     * @param capabilitiesConfiguration Define GetCapabilities service part.
     * @param serviceType instance service type
     */
    public void basicConfigure(final File instanceDirectory, final Object capabilitiesConfiguration, final String serviceType) throws CstlServiceException;


    /**
     * give instance abstract
     * @param instanceDirectory instance folder which contain metadata file
     * @return service abstract information
     */
    public String getAbstract(final File instanceDirectory);

    /**
     * give instance version
     * @param instanceDirectory instance folder which contain metadata file
     * @return
     */
    public List<String> getVersion(final File instanceDirectory);

    /**
     * Give instance layer number
     *
     * @param worker current instance worker to count data number
     * @return an <code>int</code> which is layer number configurated on instance
     */
    public List<Layer> getlayersNumber(final Worker worker);


    /**
     * Add layer on service
     * @return <code>true</code> if layer created, <code>false</code> if layer can't be created
     * @param addLayerData
     */
    public boolean addLayer(final AddLayer addLayerData);
}
