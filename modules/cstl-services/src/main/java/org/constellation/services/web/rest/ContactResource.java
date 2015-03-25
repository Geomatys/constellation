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
package org.constellation.services.web.rest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.constellation.engine.register.jooq.tables.pojos.Property;
import org.constellation.engine.register.repository.PropertyRepository;
import org.constellation.utils.JSonUtils;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ContactResource {

	@Inject
	private PropertyRepository propertyRepository;
	
	
	@RequestMapping(value = "/admin/contact", method = RequestMethod.GET, produces = "application/json")
	public Map<String, Object> get() {
		List<? extends Property> properties = propertyRepository.startWith("contact.%");
		Properties javaProperties = new Properties();
		for (Property property : properties)
			javaProperties.put(property.getName(), property.getValue());

		
		
		
		return JSonUtils.toJSon(javaProperties);
	}

	@RequestMapping(value = "/admin/contact", method = RequestMethod.PUT)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Transactional
	public void put(@RequestBody HashMap<String, Object> contact) {

		Properties properties = JSonUtils.toProperties(contact);
		
		List<? extends Property> propertiesDB = propertyRepository.startWith("contact.%");
		for (Iterator iterator = propertiesDB.iterator(); iterator.hasNext();) {
			Property property = (Property) iterator.next();
			String posted = properties.getProperty(property.getName());
			if(StringUtils.isNotBlank(posted)) {
				property.setValue(posted);
				properties.remove(property.getName());
			}else {
				propertyRepository.delete(property);
			}
		}
		
		for (Entry<Object, Object> entry : properties.entrySet()) {
			Property prop = new Property((String)entry.getKey(), (String)entry.getValue());
			propertyRepository.save(prop);
		}
	}

}
