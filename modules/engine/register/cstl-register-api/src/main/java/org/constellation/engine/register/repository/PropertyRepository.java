package org.constellation.engine.register.repository;

import org.constellation.engine.register.Property;

public interface PropertyRepository {

    Property findOne(String key);

}
