package org.constellation.ws.rs;

import org.constellation.configuration.Instance;
import org.constellation.configuration.Layer;
import org.constellation.dto.Service;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.Worker;

import javax.xml.bind.JAXBException;
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
    public void configureInstance(File instanceDirectory, Object configuration, Object capabilitiesConfiguration, String serviceType) throws CstlServiceException;

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
    public Object getInstanceConfiguration(File instanceDirectory, String serviceType) throws CstlServiceException;

    /**
     * create an empty configuration for the service.
     *
     * @param instanceDirectory The directory containing the instance configuration files.
     * @param capabilitiesConfiguration Define GetCapabilities service part.
     * @param serviceType instance service type
     */
    public void basicConfigure(final File instanceDirectory, Object capabilitiesConfiguration, String serviceType) throws CstlServiceException;


    /**
     * give instance abstract
     * @param instanceDirectory instance folder which contain metadata file
     * @return service abstract information
     */
    String getAbstract(File instanceDirectory);

    /**
     * Give instance layer number
     *
     * @param worker current instance worker to count data number
     * @return an <code>int</code> which is layer number configurated on instance
     */
    List<Layer> getlayersNumber(Worker worker);

}
