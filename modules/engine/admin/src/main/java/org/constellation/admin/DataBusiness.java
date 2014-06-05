package org.constellation.admin;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.constellation.admin.dto.LayerDTO;
import org.constellation.admin.exception.ConstellationException;
import org.constellation.engine.register.Layer;
import org.constellation.engine.register.repository.DataRepository;
import org.constellation.engine.register.repository.LayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataBusiness {

	@Autowired
	private DataRepository dataRepository;

	@Autowired
	private LayerRepository layerRepository;

	List<LayerDTO> getLayers(int serviceId) {
		List<LayerDTO> returnlist = new ArrayList<LayerDTO>();
		List<Layer> layerList = layerRepository.findByServiceId(serviceId);
		for (Layer layer : layerList) {
			LayerDTO layerDTO = new LayerDTO();
			try {
				BeanUtils.copyProperties(layerDTO, layer);
			} catch (IllegalAccessException | InvocationTargetException e) {
				throw new ConstellationException(e);
			}
			returnlist.add(layerDTO);
		}
		return returnlist;
	}

}
