package org.constellation.engine.register.repository;

import java.util.List;

import org.constellation.engine.register.Property;

public interface PropertyRepository {

    Property findOne(String key);
    
    List<? extends Property> findIn(List<String> keys);
    
	void save(Property prop);
	
	List<? extends Property> startWith(String string);

	void delete(Property property);

    List<? extends Property> findAll();

    String getValue(String key, String defaultValue);

	
}
