package org.constellation.map.ws.rs;

import org.constellation.map.ws.DefaultWMSWorker;
import org.constellation.ws.rs.GridServiceConfiguration;

import java.util.logging.Logger;

/**
 * WMS {@link org.constellation.ws.rs.GridServiceConfiguration} extention
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 */
public class WMSServiceConfiguration extends GridServiceConfiguration {


    @Override
    public Class getWorkerClass() {
        return DefaultWMSWorker.class;
    }


}
