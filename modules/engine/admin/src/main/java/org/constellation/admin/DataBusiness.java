package org.constellation.admin;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.admin.dto.LayerDTO;
import org.constellation.admin.exception.ConstellationException;
import org.constellation.engine.register.Data;
import org.constellation.engine.register.Layer;
import org.constellation.engine.register.repository.DataRepository;
import org.constellation.engine.register.repository.LayerRepository;
import org.constellation.utils.ISOMarshallerPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataBusiness {

	@Autowired
	private DataRepository dataRepository;

	@Autowired
	private LayerRepository layerRepository;

	public List<LayerDTO> getLayers(int serviceId) {
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

	public DefaultMetadata loadIsoDataMetadata(String providerId, QName name) {

		DefaultMetadata metadata = null;
		Data data = dataRepository.findByNameAndNamespaceAndProviderId(name.getLocalPart(), name.getNamespaceURI(), providerId);
		MarshallerPool pool = ISOMarshallerPool.getInstance();
		InputStream sr;
        try {
	        sr = new ByteArrayInputStream(data.getIso_metadata().getBytes("UTF-8"));
	        final Unmarshaller m = pool.acquireUnmarshaller();
			if (sr != null) {
				metadata = (DefaultMetadata) m.unmarshal(sr);
			}
			pool.recycle(m);
        } catch (UnsupportedEncodingException | JAXBException e) {
        	throw new ConstellationException(e);
        }
		return metadata;
	}

}
