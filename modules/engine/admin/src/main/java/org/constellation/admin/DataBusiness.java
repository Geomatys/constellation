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
import org.apache.sis.util.logging.Logging;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.ServiceDef;
import org.constellation.admin.dto.LayerDTO;
import org.constellation.admin.exception.ConstellationException;
import org.constellation.configuration.DataBrief;
import org.constellation.configuration.ServiceProtocol;
import org.constellation.configuration.StyleBrief;
import org.constellation.dto.CoverageMetadataBean;
import org.constellation.engine.register.Data;
import org.constellation.engine.register.Layer;

import org.constellation.engine.register.Service;
import org.constellation.engine.register.Style;

import org.constellation.engine.register.Provider;
import org.constellation.engine.register.repository.*;

import org.constellation.utils.ISOMarshallerPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class DataBusiness {


    private static final Logger LOGGER = Logging.getLogger(DataBusiness.class);

    @Autowired
    private DataRepository dataRepository;

    @Autowired
    private LayerRepository layerRepository;

    @Autowired
    private org.constellation.security.SecurityManager securityManager;

    @Autowired
    private StyleRepository styleRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private SensorRepository sensorRepository;


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
		Data data = dataRepository.findByNameAndNamespaceAndProviderIdentifier(name.getLocalPart(), name.getNamespaceURI(), providerId);
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


    public void saveMetadata(String providerId, QName name, DefaultMetadata metadata) {
        final StringWriter sw = new StringWriter();
        try {
            final Marshaller marshaller = ISOMarshallerPool.getInstance().acquireMarshaller();
            marshaller.marshal(metadata, sw);
        } catch (JAXBException ex){
            throw new ConstellationException(ex);
        }
        Data data = dataRepository.findByNameAndNamespaceAndProviderIdentifier(name.getLocalPart(), name.getNamespaceURI(), providerId);
        data.setIsoMetadata(sw.toString());
        dataRepository.save(data);
    }


    /**
     * Load a metadata for a provider.
     *
     *
     * @param providerIdentifier
     * @param pool
     * @param name
     * @return
     */
    public CoverageMetadataBean loadDataMetadata(final String providerIdentifier, final QName name,
                                                        final MarshallerPool pool) {
        CoverageMetadataBean metadata = null;
        try {
            Data data = dataRepository.findByNameAndNamespaceAndProviderIdentifier(name.getLocalPart(), name.getNamespaceURI(), providerIdentifier);
            if (data != null) {
                final InputStream sr = new ByteArrayInputStream(data.getMetadata().getBytes());
                final Unmarshaller m = pool.acquireUnmarshaller();
                if (sr != null) {
                    metadata = (CoverageMetadataBean) m.unmarshal(sr);
                }
                pool.recycle(m);
                return metadata;
            }
        } catch ( JAXBException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while updating service database", ex);
            throw new ConstellationException(ex);
        }
        return null;
    }
    public DataBrief getDataBrief(QName fullName,Integer providerId){
        Data data = dataRepository.findByNameAndNamespaceAndProviderId(fullName.getLocalPart(),fullName.getNamespaceURI(), providerId);
        List<Data> datas = new ArrayList<Data>();
        datas.add(data);
        List<DataBrief> dataBriefs = getDataBriefFrom(datas);
        if (dataBriefs !=null && dataBriefs.size()==0){
            return dataBriefs.get(0);
        }
        throw new ConstellationException(new Exception("Problem : DataBrief Construction is null or multiple"));
    }

    public DataBrief getDataBrief(QName fullName, String providerIdentifier) {
        Data data = dataRepository.findByNameAndNamespaceAndProviderIdentifier(fullName.getLocalPart(), fullName.getNamespaceURI(), providerIdentifier);
        List<Data> datas = new ArrayList<Data>();
        datas.add(data);
        List<DataBrief> dataBriefs = getDataBriefFrom(datas);
        if (dataBriefs !=null && dataBriefs.size()==0){
            return dataBriefs.get(0);
        }
        throw new ConstellationException(new Exception("Problem : DataBrief Construction is null or multiple"));
    }

    public List<DataBrief> getDataBriefsFromMetadataId(final String metadataId) {
        List<Data> datas = findByMetadataId(metadataId);
        return getDataBriefFrom(datas);
    }

    public DataBrief getDataLayer(final String layerAlias, final String dataProviderIdentifier) {
        Data data = layerRepository.findDatasFromLayerAlias(layerAlias,dataProviderIdentifier);
        List<Data> datas = new ArrayList<Data>();
        datas.add(data);
        List<DataBrief> dataBriefs = getDataBriefFrom(datas);
        if (dataBriefs !=null && dataBriefs.size()==0){
            return dataBriefs.get(0);
        }
        throw new ConstellationException(new Exception("Problem : DataBrief Construction is null or multiple"));

    }

    private  List<Data> findByMetadataId(String metadataId) {
        List<Data> dataResult = new ArrayList<Data>();
        Provider provider = providerRepository.findByMetadataId(metadataId);
        Data data = dataRepository.findByMetadataId(metadataId);
        Service service = serviceRepository.findByMetadataId(metadataId);
        if (provider!=null){
            dataResult = dataRepository.findByProviderId(provider.getId());
        } else if (service!=null) {
            dataResult = serviceRepository.findDataByServiceId(service.getId());
        } else {
            dataResult.add(data);
        }

        return dataResult;

    }

    private List<DataBrief> getDataBriefFrom(List<Data> datas) {
        List<DataBrief> dataBriefs = new ArrayList<DataBrief>();
        for (Data data : datas) {
            List<Style> styles = styleRepository.findByData(data);
            List<Service> services = serviceRepository.findByDataId(data.getId());


            final DataBrief db = new DataBrief();
            db.setOwner(data.getOwner());
            db.setName(data.getName());
            db.setNamespace(data.getNamespace());
            db.setDate(new Date(data.getDate()));
            db.setProvider(getProviderIdentifier(data.getProvider()));
            db.setType(data.getType());
            db.setSubtype(data.getSubtype());
            db.setSensorable(data.isSensorable());
            db.setTargetSensor(sensorRepository.getLinkedSensors(data));

            final List<StyleBrief> styleBriefs = new ArrayList<>(0);
            for (Style style : styles) {
                final StyleBrief sb = new StyleBrief();
                sb.setType(style.getType().toString());
                sb.setProvider(getProviderIdentifier(style.getProvider()));
                sb.setDate(new Date(style.getDate()));
                sb.setName(style.getName());
                sb.setOwner(style.getOwner());
                styleBriefs.add(sb);
            }
            db.setTargetStyle(styleBriefs);

            final List<ServiceProtocol> serviceProtocols = new ArrayList<>(0);
            for (Service service : services) {
                final List<String> protocol = new ArrayList<>(0);
                protocol.add(ServiceDef.Specification.valueOf(service.getType()).name());
                protocol.add(ServiceDef.Specification.valueOf(service.getType()).fullName);
                final ServiceProtocol sp = new ServiceProtocol(service.getIdentifier(), protocol);
                serviceProtocols.add(sp);
            }
            db.setTargetService(serviceProtocols);
            dataBriefs.add(db);
        }

        return dataBriefs;
    }

    private String getProviderIdentifier(int providerId) {
        return providerRepository.findOne(providerId).getIdentifier();

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
