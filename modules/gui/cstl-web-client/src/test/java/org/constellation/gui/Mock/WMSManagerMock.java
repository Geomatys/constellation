package org.constellation.gui.Mock;

import org.constellation.configuration.Layer;
import org.constellation.configuration.LayerList;
import org.constellation.dto.Service;
import org.constellation.gui.service.WMSManager;

import java.util.ArrayList;
import java.util.logging.Logger;
import org.constellation.ServiceDef.Specification;

/**
 * Just WMS Mock
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 *
 */
public class WMSManagerMock extends WMSManager{

    private static final Logger LOGGER = Logger.getLogger(WMSManagerMock.class.getName());

    /**
     * @param serviceName
     * @param serviceType
     * @return
     */
    @Override
    public Service getServiceMetadata(String serviceName, Specification serviceType) {
        return new Service();
    }

    /**
     * @param serviceName
     * @param serviceType
     * @return
     */
    @Override
    public LayerList getLayers(String serviceName, String serviceType) {
        return new LayerList(new ArrayList<Layer>(0));
    }
}
