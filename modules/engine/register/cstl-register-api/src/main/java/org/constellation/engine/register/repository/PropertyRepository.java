/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.engine.register.repository;

import java.util.List;

import org.constellation.engine.register.jooq.tables.pojos.Property;

public interface PropertyRepository {

    Property findOne(String key);
    
    List<? extends Property> findIn(List<String> keys);
    
    void save(Property prop);
	
	List<? extends Property> startWith(String string);

	void delete(Property property);

    List<? extends Property> findAll();

    String getValue(String key, String defaultValue);

	
}
