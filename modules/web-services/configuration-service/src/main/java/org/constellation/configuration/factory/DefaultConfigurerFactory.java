/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.constellation.configuration.factory;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.constellation.configuration.exception.ConfigurationException;
import org.constellation.configuration.ws.rs.AbstractCSWConfigurer;
import org.constellation.configuration.ws.rs.CSWconfigurer;
import org.constellation.ws.rs.ContainerNotifierImpl;

/**
 *
 * @author Guilhem Legal
 */
public class DefaultConfigurerFactory extends AbstractConfigurerFactory {

    @Override
    public AbstractCSWConfigurer getCSWConfigurer(Marshaller marshaller, Unmarshaller unmarshaller, ContainerNotifierImpl cn) throws ConfigurationException {
        return new CSWconfigurer(marshaller, unmarshaller, cn);
    }

}
