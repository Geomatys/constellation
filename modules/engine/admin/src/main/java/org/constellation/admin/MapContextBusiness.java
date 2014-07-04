package org.constellation.admin;

import org.constellation.engine.register.Mapcontext;
import org.constellation.engine.register.repository.MapContextRepository;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class MapContextBusiness {

    @Inject
    private MapContextRepository mapContextRepository;

    public void create(final String name, final String owner) {
        final Mapcontext context = new Mapcontext(name, owner);
        mapContextRepository.create(context);
    }
}
