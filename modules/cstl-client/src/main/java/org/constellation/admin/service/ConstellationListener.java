
package org.constellation.admin.service;

import org.opengis.parameter.ParameterValueGroup;

import java.util.EventListener;


/**
 *
 * @author Cédric Briançon (Geomatys)
 */
public interface ConstellationListener extends EventListener{
    void providerCreated(final String serviceName, final ParameterValueGroup config);
    void providerDeleted(final String id);
    void providerUpdated(final String serviceName, final String id, final ParameterValueGroup config);
}
