package org.constellation.coverage.ws.rs;

import org.constellation.coverage.ws.WCSWorker;
import org.constellation.ws.rs.GridServiceConfiguration;
import org.constellation.ws.rs.ServiceConfiguration;

import java.util.logging.Logger;

/**
 * WCS {@link org.constellation.ws.rs.GridServiceConfiguration} extention
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 */
public class WCSServiceConfiguration extends GridServiceConfiguration {

    @Override
    public Class getWorkerClass() {
        return WCSWorker.class;
    }
}
