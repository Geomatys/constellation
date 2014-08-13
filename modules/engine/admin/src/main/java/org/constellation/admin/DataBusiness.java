package org.constellation.admin;

import com.google.common.base.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SimpleFSLockFactory;
import org.apache.lucene.util.Version;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.util.logging.Logging;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.ServiceDef;
import org.constellation.admin.exception.ConstellationException;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.configuration.CstlConfigurationRuntimeException;
import org.constellation.configuration.DataBrief;
import org.constellation.configuration.ServiceProtocol;
import org.constellation.configuration.StyleBrief;
import org.constellation.dto.CoverageMetadataBean;
import org.constellation.engine.register.CstlUser;
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
import org.constellation.engine.register.repository.UserRepository;
import org.constellation.utils.ISOMarshallerPool;
import org.constellation.utils.MetadataFeeder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
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
    private UserRepository userRepository;

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

    private  IndexWriter indexWriter;

    private IndexSearcher indexSearcher;

    @PostConstruct
    private void init() throws IOException {
        createIndex();
        initIndexSearcher();

    }


    private void createIndex() throws IOException {
        final File dataIndexDirectory = ConfigDirectory.getDataIndexDirectory();
        final Directory directory = FSDirectory.open(dataIndexDirectory, new SimpleFSLockFactory());
        StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_46, analyzer);
        indexWriter = new IndexWriter(directory, config);
        indexWriter.commit();
    }



    private void initIndexSearcher() throws IOException {

        DirectoryReader directoryReader = DirectoryReader.open(indexWriter.getDirectory());
        indexSearcher = new IndexSearcher(directoryReader);
    }

    private void addMetadataToIndex(DefaultMetadata metadata, Integer dataId)  {
        final MetadataFeeder metadataFeeder = new MetadataFeeder(metadata);
        try {

            Document doc = new Document();
            doc.add(new IntField("dataId",dataId, Field.Store.YES));
            final String keywords = StringUtils.join(metadataFeeder.getKeywordsNoType(), " ");
            if (keywords!=null && keywords.length()>0) {
                doc.add(new TextField("keywords", keywords, Field.Store.NO));
            }
            final String title = metadataFeeder.getTitle();
            if (title != null && title.length()>0){
                doc.add(new TextField("title", title, Field.Store.NO));
            }
            final String abstractField = metadataFeeder.getAbstract();
            if (abstractField != null && abstractField.length()>0) {
                doc.add(new TextField("abstract", abstractField, Field.Store.NO));
            }
            final List<String> topicCategories = metadataFeeder.getAllTopicCategory();
            if (topicCategories != null && topicCategories.size()>0) {
                doc.add(new TextField("topic", StringUtils.join(topicCategories, ' '), Field.Store.NO));
            }
            final List<String> sequenceIdentifiers = metadataFeeder.getAllSequenceIdentifier();
            if (sequenceIdentifiers != null && sequenceIdentifiers.size()>0){
                doc.add(new TextField("data",StringUtils.join(sequenceIdentifiers, ' '),Field.Store.NO));
            }
            final String processingLevel = metadataFeeder.getProcessingLevel();
            if (processingLevel != null && processingLevel.length()>0){
                doc.add(new TextField("level",processingLevel,Field.Store.NO));
            }
            final List<String> geographicIdentifiers = metadataFeeder.getAllGeographicIdentifier();
            if (geographicIdentifiers != null && geographicIdentifiers.size()>0){
                doc.add(new TextField("area",StringUtils.join(sequenceIdentifiers, ' '),Field.Store.NO));
            }
            indexWriter.addDocument(doc);
            indexWriter.commit();

        } catch (IOException e) {
            throw new ConstellationException(e);
        }
    }

    @PreDestroy
    private void destroy() throws IOException {
        LOGGER.info("closing metadata index");
        indexWriter.close();
    }

    public DefaultMetadata loadIsoDataMetadata(String providerId, QName name) {

        DefaultMetadata metadata = null;
        Data data = dataRepository.findByNameAndNamespaceAndProviderIdentifier(name.getLocalPart(), name.getNamespaceURI(), providerId);
        MarshallerPool pool = ISOMarshallerPool.getInstance();
        try {
            if (data != null && data.getIsoMetadata() != null) {
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

    public List<Data> searchOnMetadata(String queryString) throws ParseException, IOException {
        List<Data> result = new ArrayList<>();
        initIndexSearcher();
        TopScoreDocCollector collector = TopScoreDocCollector.create(5, true);
        final MultiFieldQueryParser queryParser = new MultiFieldQueryParser(Version.LUCENE_46, new String[]{
                "title", "abstract", "keywords", "topic", "data", "level", "area" }, new StandardAnalyzer(Version.LUCENE_46));
        queryParser.setDefaultOperator(QueryParser.Operator.OR);
        final Query q = queryParser.parse(queryString);
//        final Query q = new QueryParser(Version.LUCENE_46, "keywords", new StandardAnalyzer(Version.LUCENE_46)).parse(queryString);
        indexSearcher.search(q, collector);
        final ScoreDoc[] hits = collector.topDocs().scoreDocs;
        for (ScoreDoc scoreDoc : hits){
            Document doc = indexSearcher.doc(scoreDoc.doc);
            final Integer dataId = Integer.valueOf(doc.get("dataId"));
            result.add(dataRepository.findById(dataId));
        }
        return result;
    }

    public void saveMetadata(String providerId, QName name, DefaultMetadata metadata) {
        final StringWriter sw = new StringWriter();
        try {
            final Marshaller marshaller = ISOMarshallerPool.getInstance().acquireMarshaller();
            marshaller.marshal(metadata, sw);
        } catch (JAXBException ex) {
            throw new ConstellationException(ex);
        }
        Data data = dataRepository.findByNameAndNamespaceAndProviderIdentifier(name.getLocalPart(), name.getNamespaceURI(), providerId);
        data.setIsoMetadata(sw.toString());
        data.setMetadataId(metadata.getFileIdentifier());
        dataRepository.update(data);
        addMetadataToIndex(metadata, data.getId());
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
    public CoverageMetadataBean loadDataMetadata(final String providerIdentifier, final QName name, final MarshallerPool pool) {
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
        } catch (JAXBException ex) {
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
        if (dataBriefs != null && dataBriefs.size() == 1) {
            return dataBriefs.get(0);
        }
        throw new ConstellationException(new Exception("Problem : DataBrief Construction is null or multiple"));
    }

    public DataBrief getDataBrief(QName fullName, String providerIdentifier) {
        Data data = dataRepository.findByNameAndNamespaceAndProviderIdentifier(fullName.getLocalPart(), fullName.getNamespaceURI(), providerIdentifier);
        List<Data> datas = new ArrayList<>();
        datas.add(data);
        List<DataBrief> dataBriefs = getDataBriefFrom(datas);
        if (dataBriefs != null && dataBriefs.size() == 1) {
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
            final Optional<CstlUser> user = userRepository.findById(data.getOwner());
            if (user.isPresent()) {
                db.setOwner(user.get().getLogin());
            }
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

    public void create(final QName name, final String providerIdentifier, final String type, final boolean sensorable,
            final boolean visible, final String subType, final String metadata) {
        final Provider provider = providerRepository.findByIdentifier(providerIdentifier);
        if (provider != null) {
            final Data data = new Data();
            data.setDate(new Date().getTime());
            data.setName(name.getLocalPart());
            data.setNamespace(name.getNamespaceURI());
            Optional<CstlUser> user = userRepository.findOne(securityManager.getCurrentUserLogin());
            if (user.isPresent()) {
                data.setOwner(user.get().getId());
            }
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
        final Data data = dataRepository.findByNameAndNamespaceAndProviderIdentifier(name.getLocalPart(), name.getNamespaceURI(),
                providerIdentifier);
        data.setVisible(visibility);
        dataRepository.update(data);
    }

    public void addDataToDomain(int dataId, int domainId) {
        domainRepository.addDataToDomain(dataId, domainId);
    }

    @Transactional("txManager")
    public synchronized void removeDataFromDomain(int dataId, int domainId) {
        List<Domain> findByLinkedService = domainRepository.findByLinkedData(dataId);
        if (findByLinkedService.size() == 1) {
            throw new CstlConfigurationRuntimeException("Could not unlink last domain from a data").withErrorCode("error.data.lastdomain");
        }
        domainRepository.removeDataFromDomain(dataId, domainId);
    }

    @Transactional("txManager")
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
