package org.constellation.services.web.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.inject.Inject;

import org.constellation.engine.register.DTOMapper;
import org.constellation.engine.register.Property;
import org.constellation.engine.register.repository.PropertyRepository;
import org.constellation.utils.JSonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;

@RestController
public class ContactResource {

	@Inject
	private PropertyRepository propertyRepository;
	
	@Inject
	private DTOMapper dtoMapper;

	

	@RequestMapping(value = "/admin/contact", method = RequestMethod.GET, produces = "application/json")
	@Timed
	public Map<String, Object> get() {
			Map<String, String> contactDTO = new HashMap<String, String>();

		List<? extends Property> properties = propertyRepository.startWith("contact.%");
		Properties javaProperties = new Properties();
		for (Property property : properties)
			javaProperties.put(property.getKey(), property.getValue());

		
		
		
		return JSonUtils.toJSon(javaProperties);
	}

	@RequestMapping(value = "/admin/contact", method = RequestMethod.PUT)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Timed
	@Transactional
	public void put(@RequestBody HashMap<String, Object> contact) {

		Properties properties = JSonUtils.toProperties(contact);
		
		for (Entry<Object, Object> entry : properties.entrySet()) {
			Property prop = dtoMapper.propertyEntity((String)entry.getKey(), (String)entry.getValue());
			propertyRepository.save(prop);
		}
	}

}
