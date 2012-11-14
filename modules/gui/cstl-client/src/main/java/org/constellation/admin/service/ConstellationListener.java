
package org.constellation.admin.service;

import java.util.EventListener;
import org.opengis.parameter.ParameterValueGroup;


/**
 *
 * @author Cédric Briançon (Geomatys)
 */
public interface ConstellationListener extends EventListener{
    void providerCreated(final String serviceName, final ParameterValueGroup config);
    void providerDeleted(final String id);
    void providerUpdated(final String serviceName, final String id, final ParameterValueGroup config);
}
