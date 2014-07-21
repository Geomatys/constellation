package org.constellation.admin;

import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.util.logging.Logging;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.ServiceDef;
import org.constellation.admin.exception.ConstellationException;
import org.constellation.configuration.CstlConfigurationRuntimeException;
import org.constellation.configuration.DataBrief;
import org.constellation.configuration.ServiceProtocol;
import org.constellation.configuration.StyleBrief;
import org.constellation.dto.CoverageMetadataBean;
import org.constellation.engine.register.Data;
import org.constellation.engine.register.Domain;
import org.constellation.engine.register.Provider;
import org.constellation.engine.register.Service;
import org.constellation.engine.register.Style;
import org.constellation.engine.register.repository.DataRepository;
import org.constellation.engine.register.repository.DomainRepository;
import org.constellation.engine.register.repository.LayerRepository;
import org.constellation.engine.register.repository.ProviderRepository;
import org.constellation.engine.register.repository.SensorRepository;
import org.constellation.engine.register.repository.ServiceRepository;
import org.constellation.engine.register.repository.StyleRepository;
import org.constellation.utils.ISOMarshallerPool;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class DataBusiness {


    private static final Logger LOGGER = Logging.getLogger(DataBusiness.class);

    @Inject
    private DomainRepository domainRepository;
    
    @Inject
    private DataRepository dataRepository;

    @Inject
    private LayerRepository layerRepository;

    @Inject
    private org.constellation.security.SecurityManager securityManager;

    @Inject
    private StyleRepository styleRepository;

    @Inject
    private ServiceRepository serviceRepository;

    @Inject
    private ProviderRepository providerRepository;

    @Inject
    private SensorRepository sensorRepository;


    public DefaultMetadata loadIsoDataMetadata(String providerId, QName name) {

        DefaultMetadata metadata = null;
        Data data = dataRepository.findByNameAndNamespaceAndProviderIdentifier(name.getLocalPart(), name.getNamespaceURI(), providerId);
        MarshallerPool pool = ISOMarshallerPool.getInstance();
        try {
            if (data.getIsoMetadata() != null) {
                InputStream sr = new ByteArrayInputStream(data.getIsoMetadata().getBytes("UTF-8"));
                final Unmarshaller m = pool.acquireUnmarshaller();
                metadata = (DefaultMetadata) m.unmarshal(sr);
                pool.recycle(m);
            }
            
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
        dataRepository.update(data);
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
            if (data != null && data.getMetadata() != null) {
                final InputStream sr = new ByteArrayInputStream(data.getMetadata().getBytes());
                final Unmarshaller m = pool.acquireUnmarshaller();
                metadata = (CoverageMetadataBean) m.unmarshal(sr);
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
        List<Data> datas = new ArrayList<>();
        datas.add(data);
        List<DataBrief> dataBriefs = getDataBriefFrom(datas);
        if (dataBriefs !=null && dataBriefs.size()==1){
            return dataBriefs.get(0);
        }
        throw new ConstellationException(new Exception("Problem : DataBrief Construction is null or multiple"));
    }

    public DataBrief getDataBrief(QName fullName, String providerIdentifier) {
        Data data = dataRepository.findByNameAndNamespaceAndProviderIdentifier(fullName.getLocalPart(), fullName.getNamespaceURI(), providerIdentifier);
        List<Data> datas = new ArrayList<>();
        datas.add(data);
        List<DataBrief> dataBriefs = getDataBriefFrom(datas);
        if (dataBriefs !=null && dataBriefs.size()==1){
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
        List<Data> datas = new ArrayList<>();
        datas.add(data);
        List<DataBrief> dataBriefs = getDataBriefFrom(datas);
        if (dataBriefs != null && !dataBriefs.isEmpty()){
            return dataBriefs.get(0);
        }
        throw new ConstellationException(new Exception("Problem : DataBrief Construction is null or multiple"));

    }

    private  List<Data> findByMetadataId(String metadataId) {
        List<Data> dataResult   = new ArrayList<>();
        final Provider provider = providerRepository.findByMetadataId(metadataId);
        final Data data         = dataRepository.findByMetadataId(metadataId);
        final Service service   = serviceRepository.findByMetadataId(metadataId);
        if (provider != null){
            dataResult = dataRepository.findByProviderId(provider.getId());
        } else if (service!= null) {
            dataResult = serviceRepository.findDataByServiceId(service.getId());
        } else if (data != null) {
            dataResult.add(data);
        }
        return dataResult;

    }

    private List<DataBrief> getDataBriefFrom(List<Data> datas) {
        List<DataBrief> dataBriefs = new ArrayList<>();
        for (Data data : datas) {
            List<Style> styles = styleRepository.findByData(data);
            List<Service> services = serviceRepository.findByDataId(data.getId());


            final DataBrief db = new DataBrief();
            db.setId(data.getId());
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
                sb.setId(style.getId());
                sb.setType(style.getType());
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
                protocol.add(ServiceDef.Specification.valueOf(service.getType().toUpperCase()).name());
                protocol.add(ServiceDef.Specification.valueOf(service.getType().toUpperCase()).fullName);
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
    
    public void deleteDataForProvider(final String providerIdentifier) {
        final Provider provider = providerRepository.findByIdentifier(providerIdentifier);
        if (provider != null) {
            List<Data> datas = dataRepository.findByProviderId(provider.getId());
            for (Data data : datas) {
                dataRepository.delete(data.getId());
            }
            
        }
    }
    
    public void deleteAll() {
        final List<Data> datas = dataRepository.findAll();
        for (Data data : datas) {
            dataRepository.delete(data.getId());
        }
    }
    
    public void create(final QName name, final String providerIdentifier, final String type, final boolean sensorable, final boolean visible,
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
            dataRepository.create(data);
        }
    }

    public void updateDataVisibility(QName name, String providerIdentifier, boolean visibility) {
        final Data data = dataRepository.findByNameAndNamespaceAndProviderIdentifier(name.getLocalPart(), name.getNamespaceURI(), providerIdentifier);
        data.setVisible(visibility);
        dataRepository.update(data);
    }
    
    
    
    public void addDataToDomain(int dataId, int domainId) {
        domainRepository.addDataToDomain(dataId, domainId);
    }

    @Transactional
    public synchronized void removeDataFromDomain(int dataId, int domainId) {
        List<Domain> findByLinkedService = domainRepository.findByLinkedData(dataId);
        if (findByLinkedService.size() == 1) {
            throw new CstlConfigurationRuntimeException("Could not unlink last domain from a data")
                    .withErrorCode("error.data.lastdomain");
        }
        domainRepository.removeDataFromDomain(dataId, domainId);
    }
    
    @Transactional
    public synchronized void removeDataFromProvider(String providerID) {
        final Provider p = providerRepository.findByIdentifier(providerID);
        if (p != null) {
            final List<Data> datas = dataRepository.findByProviderId(p.getId());
            for (Data data : datas) {
                dataRepository.delete(data.getId());
            }
        } 
    }
}
