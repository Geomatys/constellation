package org.constellation.gui.Mock;

import org.constellation.ServiceDef;
import org.constellation.configuration.Layer;
import org.constellation.configuration.LayerList;
import org.constellation.gui.service.MapManager;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Just WMS Mock
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 *
 */
public class MapManagerMock extends MapManager {

    private static final Logger LOGGER = Logger.getLogger(MapManagerMock.class.getName());


    /**
     *
     * @param serviceName
     * @param specification
     * @return
     */
    @Override
    public LayerList getLayers(String serviceName, final ServiceDef.Specification specification) {
        return new LayerList(new ArrayList<Layer>(0));
    }
}
