package org.constellation.wmts.ws.rs;

import org.constellation.wmts.ws.WMTSWorker;
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
public class WMTSServiceConfiguration extends GridServiceConfiguration {

    @Override
    public Class getWorkerClass() {
        return WMTSWorker.class;
    }

}
