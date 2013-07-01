package org.constellation.wfs.ws.rs;


import org.constellation.wfs.ws.WFSWorker;
import org.constellation.ws.rs.GridServiceConfiguration;

/**
 * WFS {@link org.constellation.ws.rs.GridServiceConfiguration} extention
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 */
public class WFSServiceConfiguration extends GridServiceConfiguration{

    @Override
    public Class getWorkerClass() {
        return WFSWorker.class;
    }
}
