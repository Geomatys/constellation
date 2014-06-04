package org.constellation.admin;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;
import org.constellation.admin.dto.Service;
import org.constellation.engine.register.repository.LayerRepository;
import org.constellation.engine.register.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServiceAdmin {
	@Autowired
    private ServiceRepository serviceRepository;
	
	@Autowired
    private LayerRepository layerRepository;
	
	Service getService(int id) throws IllegalAccessException, InvocationTargetException{
		Service returnService = new Service();
		org.constellation.engine.register.Service service = serviceRepository.findById(id);
		BeanUtils.copyProperties(returnService, service);
		return returnService;
	}
	
	
	
	

}
