package org.constellation.admin;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
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
import org.constellation.engine.register.Provider;
import org.constellation.engine.register.repository.DataRepository;
import org.constellation.engine.register.repository.LayerRepository;
import org.constellation.engine.register.repository.ProviderRepository;
import org.constellation.utils.ISOMarshallerPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataBusiness {

    @Autowired
    private DataRepository dataRepository;

    @Autowired
    private LayerRepository layerRepository;
    
    @Autowired
    private ProviderRepository providerRepository;
    
    @Autowired
    private org.constellation.security.SecurityManager securityManager;

    public List<LayerDTO> getLayers(int serviceId) {
        List<LayerDTO> returnlist = new ArrayList<>();
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
            sr = new ByteArrayInputStream(data.getIsoMetadata().getBytes("UTF-8"));
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

    public void deleteData(final QName name, final String providerIdentifier) {
        final Provider provider = providerRepository.findByIdentifier(providerIdentifier);
        if (provider != null) {
            dataRepository.delete(name.getNamespaceURI(), name.getLocalPart(), provider.getId());
        }
    }
    
    public void write(final QName name, final String providerIdentifier, final String type, final boolean sensorable, final boolean visible,
            final String subType, final String metadata) {
        final Provider provider = providerRepository.findByIdentifier(providerIdentifier);
        if (provider != null) {
            final Data data = new Data();
            data.setDate(new Date().getTime());
            data.setName(name.getLocalPart());
            data.setNamespace(name.getNamespaceURI());
            data.setOwner(securityManager.getCurrentUserLogin());
            data.setProvider(provider.getId());
            data.setSensorable(sensorable);
            data.setType(type);
            data.setSubtype(subType);
            data.setVisible(visible);
            data.setMetadata(metadata);
            dataRepository.save(data);
        }
    }
}
