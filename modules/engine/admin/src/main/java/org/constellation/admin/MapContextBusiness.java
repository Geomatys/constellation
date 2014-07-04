package org.constellation.admin;

import org.constellation.engine.register.repository.MapContextRepository;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class MapContextBusiness {

    @Inject
    private MapContextRepository mapContextRepository;

}
