package org.constellation.engine.register.repository;

import java.util.List;

import org.constellation.engine.register.Property;

public interface PropertyRepository {

    Property findOne(String key);
    
    List<? extends Property> findIn(List<String> keys);
    
	void save(Property prop);
	
	List<? extends Property> startWith(String string);

	
}
