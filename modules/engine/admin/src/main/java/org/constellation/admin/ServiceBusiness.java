package org.constellation.admin;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.constellation.admin.dto.LayerDTO;
import org.constellation.admin.dto.ServiceDTO;
import org.constellation.admin.exception.ConstellationException;
import org.constellation.engine.register.ConstellationPersistenceException;
import org.constellation.engine.register.Layer;
import org.constellation.engine.register.Service;
import org.constellation.engine.register.repository.LayerRepository;
import org.constellation.engine.register.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServiceBusiness {
	@Autowired
	private ServiceRepository serviceRepository;

	@Autowired
	private LayerRepository layerRepository;

	ServiceDTO getService(int id) throws IllegalAccessException,
			InvocationTargetException {
		ServiceDTO returnService = new ServiceDTO();
		org.constellation.engine.register.Service service = serviceRepository
				.findById(id);
		BeanUtils.copyProperties(returnService, service);
		return returnService;
	}

	ServiceDTO create(ServiceDTO serviceDTO) {
		Service service = new Service();
		try {
			BeanUtils.copyProperties(service, serviceDTO);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new ConstellationException(e);
		}
		int serviceId = serviceRepository.create(service);
		serviceDTO.setId(serviceId);
		return serviceDTO;
	}
	
	

}
