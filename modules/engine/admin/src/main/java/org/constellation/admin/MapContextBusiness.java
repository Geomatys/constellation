package org.constellation.admin;

import org.constellation.engine.register.MapcontextStyledLayer;
import org.constellation.engine.register.repository.MapContextRepository;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

@Component
public class MapContextBusiness {

    @Inject
    private MapContextRepository mapContextRepository;

    public void addMapItems(final int contextId, final List<MapcontextStyledLayer> layers) {
        mapContextRepository.setLinkedLayers(contextId, layers);
    }
}
