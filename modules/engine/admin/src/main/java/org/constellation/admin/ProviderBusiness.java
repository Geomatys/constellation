package org.constellation.admin;

import static org.geotoolkit.parameter.Parameters.getOrCreate;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.xml.namespace.QName;

import org.apache.sis.geometry.GeneralDirectPosition;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.logging.Logging;
import org.constellation.admin.exception.ConstellationException;
import org.constellation.admin.util.IOUtilities;
import org.constellation.api.ProviderType;
import org.constellation.business.IDataBusiness;
import org.constellation.business.IProcessBusiness;
import org.constellation.business.IProviderBusiness;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.CstlConfigurationRuntimeException;
import org.constellation.configuration.DataBrief;
import org.constellation.configuration.ProviderConfiguration;
import org.constellation.dto.ProviderPyramidChoiceList;
import org.constellation.engine.register.jooq.tables.pojos.CstlUser;
import org.constellation.engine.register.jooq.tables.pojos.Provider;
import org.constellation.engine.register.jooq.tables.pojos.Style;
import org.constellation.engine.register.jooq.tables.pojos.TaskParameter;
import org.constellation.engine.register.repository.ProviderRepository;
import org.constellation.engine.register.repository.UserRepository;
import org.constellation.provider.CoverageData;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviderFactory;
import org.constellation.provider.DataProviders;
import org.constellation.provider.configuration.ProviderParameters;
import org.constellation.util.ParamUtilities;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.coverage.CoverageStoreFactory;
import org.geotoolkit.coverage.CoverageStoreFinder;
import org.geotoolkit.coverage.CoverageUtilities;
import org.geotoolkit.coverage.GridSampleDimension;
import org.geotoolkit.coverage.Pyramid;
import org.geotoolkit.coverage.PyramidalCoverageReference;
import org.geotoolkit.coverage.grid.GeneralGridGeometry;
import org.geotoolkit.coverage.grid.ViewType;
import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.coverage.xmlstore.XMLCoverageReference;
import org.geotoolkit.coverage.xmlstore.XMLCoverageStore;
import org.geotoolkit.coverage.xmlstore.XMLCoverageStoreFactory;
import org.geotoolkit.data.FeatureStoreFactory;
import org.geotoolkit.data.FeatureStoreFinder;
import org.geotoolkit.data.FileFeatureStoreFactory;
import org.geotoolkit.feature.type.DefaultName;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.image.interpolation.InterpolationCase;
import org.geotoolkit.observation.ObservationStoreFactory;
import org.geotoolkit.parameter.Parameters;
import org.geotoolkit.parameter.ParametersExt;
import org.geotoolkit.process.Process;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.storage.DataStoreFactory;
import org.geotoolkit.util.FileUtilities;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.Identifier;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.NoSuchIdentifierException;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;

import com.google.common.base.Optional;

@Component("providerBusiness")
@Primary
public class ProviderBusiness implements IProviderBusiness {
    private static final Logger LOGGER = Logging.getLogger(ProviderBusiness.class);

    /**
     * Identifier of the possible provider types.
     * @deprecated Used for current provider management mechanism. Removed when providers will be simplified in
     * DataStoreSource.
     */
    static enum SPI_NAMES {
        COVERAGE_SPI_NAME("coverage-store"),
        FEATURE_SPI_NAME("feature-store"),
        OBSERVATION_SPI_NAME("observation-store");

        public final String name;
        private SPI_NAMES(final String providerSPIName) {
            name = providerSPIName;
        }
    }

    private static final String CONFORM_PREFIX = "conform_";

    @Inject
    private UserRepository userRepository;

    @Inject
    private ProviderRepository providerRepository;

    @Inject
    private org.constellation.security.SecurityManager securityManager;

    @Inject
    private IDataBusiness dataBusiness;

    @Inject
    private IProcessBusiness processBusiness;

    @Override
    public List<Provider> getProviders() {
        return providerRepository.findAll();
    }

    @Override
    public Provider getProvider(final String identifier) {
        return providerRepository.findByIdentifier(identifier);
    }

    @Override
    public Provider getProvider(String providerIdentifier, int domainId) {
        return providerRepository.findByIdentifierAndDomainId(providerIdentifier, domainId);
    }

    @Override
    public Provider getProvider(final int id) {
        return providerRepository.findOne(id);
    }

    @Override
    public List<String> getProviderIds() {
        final List<String> ids = new ArrayList<>();
        final List<Provider> providers = providerRepository.findAll();
        for (Provider p : providers) {
            ids.add(p.getIdentifier());
        }
        return ids;
    }

    @Override
    @Transactional
    public void removeProvider(final String identifier) {
        providerRepository.deleteByIdentifier(identifier);
    }

    @Override
    @Transactional
    public void removeAll() {
        final List<Provider> providers = providerRepository.findAll();
        for (Provider p : providers) {
            final DataProvider dp = DataProviders.getInstance().getProvider(p.getIdentifier());
            try{
                DataProviders.getInstance().removeProvider(dp);
            }catch(ConfigurationException ex){
                LOGGER.log(Level.WARNING,ex.getLocalizedMessage(),ex);
            }
            providerRepository.delete(p.getId());
            final File provDir = ConfigDirectory.getDataIntegratedDirectory(p.getIdentifier());
            FileUtilities.deleteDirectory(provDir);
        }
    }

    @Override
    public List<Provider> getProviderChildren(final String identifier) {
        return providerRepository.findChildren(identifier);
    }


    @Override
    public List<org.constellation.engine.register.jooq.tables.pojos.Data> getDatasFromProviderId(Integer id) {
        return providerRepository.findDatasByProviderId(id);
    }

    @Override
    @Transactional
    public void updateParent(String providerIdentifier, String newParentIdentifier) {
        final Provider provider = getProvider(providerIdentifier);
        provider.setParent(newParentIdentifier);
        providerRepository.update(provider);
    }

    @Override
    public List<Style> getStylesFromProviderId(Integer providerId) {
        return providerRepository.findStylesByProviderId(providerId);
    }


    @Override
    @Transactional
    public Provider storeProvider(final String identifier, final String parent, final ProviderType type, final String serviceName,
                                  final GeneralParameterValue config) throws IOException {
        Provider provider = new Provider();
        Optional<CstlUser> user = userRepository.findOne(securityManager.getCurrentUserLogin());
        if (user.isPresent()) {
            provider.setOwner(user.get().getId());
        }
        provider.setParent(parent);
        provider.setType(type.name());
        provider.setConfig(IOUtilities.writeParameter(config));
        provider.setIdentifier(identifier);
        // TODO very strange !!!!
        provider.setImpl(serviceName);
        return providerRepository.insert(provider);

    }

    @Override
    public Set<Name> test(final String providerIdentifier, final ProviderConfiguration configuration) throws DataStoreException, ConfigurationException {
        final String type = configuration.getType();
        final String subType = configuration.getSubType();
        final Map<String, String> inParams = configuration.getParameters();

        final DataProviderFactory providerService = DataProviders.getInstance().getFactory(type);
        final ParameterDescriptorGroup sourceDesc = providerService.getProviderDescriptor();
        ParameterValueGroup sources = sourceDesc.createValue();
        sources.parameter("id").setValue(providerIdentifier);
        sources.parameter("providerType").setValue(type);
        sources = fillProviderParameter(type, subType, inParams, sources);
        return DataProviders.getInstance().testProvider(providerIdentifier, providerService, sources);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Provider create(final String id, final DataStoreFactory spi, ParameterValueGroup spiConfiguration) throws ConfigurationException {
        if (getProvider(id) != null) {
            throw new ConfigurationException("A provider already exists for name "+id);
        }

        final String providerType;
        if (spi instanceof CoverageStoreFactory) {
            providerType = SPI_NAMES.COVERAGE_SPI_NAME.name;
        } else if (spi instanceof FeatureStoreFactory) {
            providerType = SPI_NAMES.FEATURE_SPI_NAME.name;
        } else if (spi instanceof ObservationStoreFactory) {
            providerType = SPI_NAMES.OBSERVATION_SPI_NAME.name;
        } else {
            throw new ConfigurationException("No provider can be created for following factory and parameters : " +
                    spi.getDisplayName() + "\n"+spiConfiguration);
        }
        final DataProviderFactory pFactory = DataProviders.getInstance().getFactory(providerType);
        final ParameterValueGroup providerConfig = pFactory.getProviderDescriptor().createValue();

        providerConfig.parameter("id").setValue(id);
        providerConfig.parameter("providerType").setValue(providerType);
        final ParameterValueGroup choice =
                providerConfig.groups("choice").get(0).addGroup(spiConfiguration.getDescriptor().getName().getCode());
        Parameters.copy(spiConfiguration, choice);

        return create(id, pFactory.getName(), providerConfig);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Provider create(final String id, final ProviderConfiguration config) throws ConfigurationException {
        final String type = config.getType();
        final String subType = config.getSubType();
        final Map<String,String> inParams = config.getParameters();

        final DataProviderFactory providerService = DataProviders.getInstance().getFactory(type);
        final ParameterDescriptorGroup sourceDesc = providerService.getProviderDescriptor();
        ParameterValueGroup sources = sourceDesc.createValue();
        sources.parameter("id").setValue(id);
        sources.parameter("providerType").setValue(type);
        sources = fillProviderParameter(type, subType, inParams, sources);
        return create(id, providerService.getName(), sources);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Provider create(final String id, final String providerSPIName, final ParameterValueGroup providerConfig) throws ConfigurationException {
        final DataProviderFactory providerSPI = DataProviders.getInstance().getFactory(providerSPIName);
        /////
        // WARNING : createProvider() will create provider, data list and dataset records in repositories.
        /////
        DataProviders.getInstance().createProvider(id, providerSPI, providerConfig, null);
        return getProvider(id);
    }

    @Override
    @Transactional
    public void update(final String id, final ProviderConfiguration config) throws ConfigurationException {
        final String type = config.getType();
        final String subType = config.getSubType();
        final Map<String, String> inParams = config.getParameters();

        final DataProviderFactory providerService = DataProviders.getInstance().getFactory(type);
        final ParameterDescriptorGroup sourceDesc = providerService.getProviderDescriptor();
        ParameterValueGroup sources = sourceDesc.createValue();
        sources.parameter("id").setValue(id);
        sources.parameter("providerType").setValue(type);

        sources = fillProviderParameter(type, subType, inParams, sources);

        final DataProvider old = DataProviders.getInstance().getProvider(id);
        if (old != null) {
            // Provider already exists, update config
            old.updateSource(sources);
        }
    }

    protected ParameterValueGroup fillProviderParameter(String type, String subType,
                                                        Map<String, String> inParams,
                                                        ParameterValueGroup sources)throws ConfigurationException {
        switch (type) {
            case "sld":
                final String sldPath = inParams.get("path");
                String folderPath = sldPath.substring(0, sldPath.lastIndexOf('/'));
                sources.groups("sldFolder").get(0).parameter("path").setValue(folderPath);
                break;

            case "feature-store":
                boolean foundProvider = false;
                try {
                    
                    final Path filePath;
                    if (inParams.get("path") != null) {
                        filePath = Paths.get(inParams.get("path"));
                    } else {
                        filePath = null;
                    }
                    URL url = null;
                    if (filePath != null) {
                        url = filePath.toUri().toURL();
                        final File file = filePath.toFile();
                        final File[] candidates;
                        if (file.isDirectory()) {
                            candidates = file.listFiles();
                        } else {
                            candidates = new File[]{file};
                        }

                        search:
                        for (File candidate : candidates) {
                            final String candidateName = candidate.getName().toLowerCase();

                            //loop on features file factories
                            final Iterator<FeatureStoreFactory> ite = FeatureStoreFinder.getAllFactories(null).iterator();
                            while (ite.hasNext()) {
                                final FeatureStoreFactory factory = ite.next();
                                if (factory instanceof FileFeatureStoreFactory) {
                                    final FileFeatureStoreFactory fileFactory = (FileFeatureStoreFactory) factory;
                                    for (String tempExtension : fileFactory.getFileExtensions()) {
                                        if (candidateName.endsWith(tempExtension) && !tempExtension.endsWith("dbf")) {
                                            //found a factory which can handle it
                                            final ParameterValueGroup params = sources.groups("choice").get(0).addGroup(
                                            factory.getParametersDescriptor().getName().getCode());
                                            if(candidates.length>1 && file.isDirectory()){
                                                url = new URL(url.toString()+candidateName);
                                            }
                                            params.parameter("url").setValue(url);
                                            params.parameter("namespace").setValue("no namespace");
                                            foundProvider = true;
                                            break search;
                                        }
                                    }
                                } else {
                                    final ParameterValueGroup testParams = factory.getParametersDescriptor().createValue();
                                    try {
                                        testParams.parameter("namespace").setValue("no namespace");
                                        final ParameterValue pv = ParametersExt.getOrCreateValue(testParams, "url");
                                        pv.setValue(url);

                                        if (factory.canProcess(testParams)) {
                                            final ParameterValueGroup params = sources.groups("choice").get(0).addGroup(
                                                    factory.getParametersDescriptor().getName().getCode());
                                            params.parameter("url").setValue(url);
                                            params.parameter("namespace").setValue("no namespace");
                                            foundProvider = true;
                                            break search;
                                        }
                                    } catch (Exception ex) {
                                        //parameter might not exist
                                    }
                                }
                            }
                        }
                    }


                    if (subType!=null && !subType.isEmpty()) {
                        if (url != null) {
                            inParams.put("url",url.toString());
                        }
                        final FeatureStoreFactory featureFactory = FeatureStoreFinder.getFactoryById(subType);
                        final ParameterValueGroup cvgConfig = Parameters.toParameter(inParams, featureFactory.getParametersDescriptor(), true);
                        final ParameterValueGroup choice = ParametersExt.getOrCreateGroup(sources.groups("choice").get(0),cvgConfig.getDescriptor().getName().getCode());
                        Parameters.copy(cvgConfig, choice);
                        foundProvider = true;
                    }
                    
                    if(!foundProvider) {
                        throw new ConfigurationException("No provider found to resolve the data!");
                    }
                    
                } catch (MalformedURLException e) {
                    LOGGER.log(Level.WARNING, "unable to create url from path", e);
                }
                break;

            case "coverage-store":
                // TODO : remove this crappy hack after provider system refactoring.
                final String filePath = inParams.get("path");
                if (filePath != null ) {
                    
                    try {
                        inParams.put("path", Paths.get(filePath).toUri().toURL().toString());
                    } catch (MalformedURLException e) {
                     throw new CstlConfigurationRuntimeException(e);
                    }
                }
                final CoverageStoreFactory cvgFactory = CoverageStoreFinder.getFactoryById(subType);
                final ParameterValueGroup cvgConfig = Parameters.toParameter(inParams, cvgFactory.getParametersDescriptor(), true);
                final ParameterValueGroup choice =
                        sources.groups("choice").get(0).addGroup(cvgConfig.getDescriptor().getName().getCode());
                Parameters.copy(cvgConfig, choice);
                break;

            case "observation-store":

                switch (subType) {
                    // TODO : Remove this hacky switch / case when input map will have the right identifier for url parameter.
                    case "observation-file":
                        final ParameterValueGroup ncObsParams = sources.groups("choice").get(0).addGroup("ObservationFileParameters");
                        ncObsParams.parameter("identifier").setValue("observationFile");
                        ncObsParams.parameter("namespace").setValue("no namespace");
                        ncObsParams.parameter("url").setValue(new File(inParams.get("path")));
                        break;

                    case "observation-xml":

                        final ParameterValueGroup xmlObsParams = sources.groups("choice").get(0).addGroup("ObservationXmlFileParameters");
                        xmlObsParams.parameter("identifier").setValue("observationXmlFile");
                        xmlObsParams.parameter("namespace").setValue("no namespace");
                        xmlObsParams.parameter("url").setValue(new File(inParams.get("path")));
                        break;

                    default:
                        LOGGER.log(Level.WARNING, "error on subtype definition");
                }
                break;
            default:
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.log(Level.FINER, "Provider type not known");
                }
        }
        return sources;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProviderPyramidChoiceList listPyramids(final String id, final String layerName) throws DataStoreException {
        final ProviderPyramidChoiceList choices = new ProviderPyramidChoiceList();

        final List<Provider> childrenRecs = getProviderChildren(id);

        for(Provider childRec : childrenRecs){
            final DataProvider provider = DataProviders.getInstance().getProvider(childRec.getIdentifier());
            final CoverageData cacheData = (CoverageData) provider.get(layerName);
            if(cacheData!=null){
                final PyramidalCoverageReference cacheRef = (PyramidalCoverageReference) cacheData.getOrigin();
                final Collection<Pyramid> pyramids = cacheRef.getPyramidSet().getPyramids();
                if(pyramids.isEmpty()) continue;
                //TODO what do we do if there are more then one pyramid ?
                //it the current state of constellation there is only one pyramid
                final Pyramid pyramid = pyramids.iterator().next();
                final Identifier crsid = pyramid.getCoordinateReferenceSystem().getIdentifiers().iterator().next();

                final ProviderPyramidChoiceList.CachePyramid cache = new ProviderPyramidChoiceList.CachePyramid();
                cache.setCrs(crsid.getCode());
                cache.setScales(pyramid.getScales());
                cache.setProviderId(provider.getId());
                cache.setDataId(layerName);
                cache.setConform(childRec.getIdentifier().startsWith("conform_"));

                choices.getPyramids().add(cache);
            }
        }
        return choices;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataBrief createPyramidConform(final String providerId, final String dataName, final String namespace,
                                          final int userOwnerId) throws ConstellationException {
        final QName qName = new QName(namespace, dataName);
        final DataBrief inData = dataBusiness.getDataBrief(qName, providerId);
        if (inData != null){
            // Execute in transaction to ensure that taskParameter is in Database before run
            // process in Quartz scheduler.
            Map<String, Object> result = SpringHelper.executeInTransaction(new TransactionCallback<Map<String, Object>>() {
                @Override
                public Map<String, Object> doInTransaction(TransactionStatus transactionStatus) {
                    return preparePyramidConform(inData, userOwnerId);
                }
            });

            final DataBrief outData = (DataBrief) result.get("outData");
            final Process process = (Process) result.get("process");
            final Integer taskParameterId = (Integer) result.get("taskParameterId");
            final String taskName = (String) result.get("taskName");

            //run pyramid process in Quartz
            processBusiness.runProcess(taskName, process, taskParameterId, userOwnerId);
            return outData;
        }
        throw new ConstellationException("Data "+qName+" not found in provider identifier "+providerId);
    }

    /**
     * Prepare pyramid conform for a data.
     * This method should not be called alone, but before
     * {@link org.constellation.business.IProcessBusiness#runProcess(String, org.geotoolkit.process.Process, Integer, Integer)}.
     *
     * This method need to be called in a Transaction
     *
     * @param inData Data to pyramid
     * @param userOwnerId owner of the pyramid
     * @return a Map with :
     *      <ul>
     *          <li>"ouData" : new pyramid DataBrief</li>
     *          <li>"process" : pyramid process to execute</li>
     *          <li>"taskParameterId" : task parameter id linked to process</li>
     *          <li>"taskName" : name of the task</li>
     *      </ul>
     * @throws ConstellationException
     */
    private Map<String, Object> preparePyramidConform(final DataBrief inData, final int userOwnerId) throws ConstellationException {
        final String dataName = inData.getName();
        final String namespace = inData.getNamespace();
        final String providerId = inData.getProvider();
        final Integer datasetID = inData.getDatasetId();
        Name name = new DefaultName(namespace,dataName);

        //get data
        final DataProvider inProvider = DataProviders.getInstance().getProvider(providerId);
        if (inProvider == null) {
            throw new ConstellationException("Provider " + providerId + " does not exist");
        }
        final org.constellation.provider.Data providerData = inProvider.get(name);
        if (providerData == null) {
            throw new ConstellationException("Data " + dataName + " does not exist in provider " + providerId);
        }
        Envelope dataEnv;
        try {
            //use data crs
            dataEnv = providerData.getEnvelope();
        } catch (DataStoreException ex) {
            throw new ConstellationException("Failed to extract envelope for data " + dataName + ". " + ex.getMessage(),ex);
        }

        final Object origin = providerData.getOrigin();

        if(!(origin instanceof CoverageReference)) {
            throw new ConstellationException("Cannot create pyramid conform for no raster data, it is not supported yet!");
        }

        //init coverage reference and grid geometry
        final CoverageReference inRef = (CoverageReference) origin;
        final GeneralGridGeometry gg;
        try {
            final GridCoverageReader reader = inRef.acquireReader();
            gg = reader.getGridGeometry(inRef.getImageIndex());
            inRef.recycle(reader);
        } catch (CoverageStoreException ex) {
            throw new ConstellationException("Failed to extract grid geometry for data " + dataName + ". " + ex.getMessage(),ex);
        }

        //create the output folder for pyramid
        PyramidalCoverageReference outRef;
        final String pyramidProviderId = CONFORM_PREFIX + UUID.randomUUID().toString();
        //create the output provider
        final DataProvider outProvider;
        final DataBrief pyramidDataBrief;
        try {
            //create the output folder for pyramid
            final File providerDirectory = ConfigDirectory.getDataIntegratedDirectory(providerId);
            final File pyramidDirectory = new File(providerDirectory, pyramidProviderId);
            pyramidDirectory.mkdirs();

            final String namespaceToSend = namespace != null && ! namespace.isEmpty() ? namespace:"no namespace";

            //create output store
            final ParameterValueGroup storeParams = XMLCoverageStoreFactory.PARAMETERS_DESCRIPTOR.createValue();
            getOrCreate(XMLCoverageStoreFactory.NAMESPACE, storeParams).setValue(namespaceToSend);
            getOrCreate(XMLCoverageStoreFactory.PATH, storeParams).setValue(pyramidDirectory.toURI().toURL());
            getOrCreate(XMLCoverageStoreFactory.CACHE_TILE_STATE, storeParams).setValue(true);

            XMLCoverageStore outStore = (XMLCoverageStore) CoverageStoreFinder.open(storeParams);
            if (outStore == null) {
                throw new ConstellationException("Failed to create pyramid layer ");
            }
            XMLCoverageReference covRef = (XMLCoverageReference)outStore.create(name, ViewType.GEOPHYSICS, "TIFF");

            // create provider
            final DataProviderFactory factory = DataProviders.getInstance().getFactory("coverage-store");
            final ParameterValueGroup pparams = factory.getProviderDescriptor().createValue();
            ParametersExt.getOrCreateValue(pparams, ProviderParameters.SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue(pyramidProviderId);
            ParametersExt.getOrCreateValue(pparams, ProviderParameters.SOURCE_TYPE_DESCRIPTOR.getName().getCode()).setValue("coverage-store");
            final ParameterValueGroup choiceparams = ParametersExt.getOrCreateGroup(pparams, factory.getStoreDescriptor().getName().getCode());
            final ParameterValueGroup xmlpyramidparams = ParametersExt.getOrCreateGroup(choiceparams, XMLCoverageStoreFactory.PARAMETERS_DESCRIPTOR.getName().getCode());
            ParametersExt.getOrCreateValue(xmlpyramidparams, XMLCoverageStoreFactory.PATH.getName().getCode()).setValue(pyramidDirectory.toURI().toURL());
            ParametersExt.getOrCreateValue(xmlpyramidparams, XMLCoverageStoreFactory.NAMESPACE.getName().getCode()).setValue(namespaceToSend);
            outProvider = DataProviders.getInstance().createProvider(pyramidProviderId, factory, pparams, datasetID);

            name = covRef.getName();
            outStore = (XMLCoverageStore) outProvider.getMainStore();
            outRef = (XMLCoverageReference) outStore.getCoverageReference(name);

            // Update the parent attribute of the created provider
            updateParent(outProvider.getId(), providerId);

            final QName qName = new QName(name.getNamespaceURI(), name.getLocalPart());
            //set rendered attribute to false to indicates that this pyramid can have stats.
            dataBusiness.updateDataRendered(qName, outProvider.getId(), false);

            //set hidden value to true for the pyramid conform data
            pyramidDataBrief = dataBusiness.getDataBrief(qName, pyramidProviderId);
            dataBusiness.updateHidden(pyramidDataBrief.getId(),true);

        } catch (Exception ex) {
            throw new ConstellationException("Failed to create pyramid provider " + ex.getMessage(),ex);
        }

        //get the fill value for no data
        try {
            final GridCoverageReader reader = inRef.acquireReader();
            final List<GridSampleDimension> sampleDimensions = reader.getSampleDimensions(inRef.getImageIndex());
            inRef.recycle(reader);
            if (sampleDimensions != null) {
                final int nbBand = sampleDimensions.size();
                double[] fillValue = new double[nbBand];
                Arrays.fill(fillValue, Double.NaN);
                for (int i = 0; i < nbBand; i++) {
                    final double[] nodata = sampleDimensions.get(i).geophysics(true).getNoDataValues();
                    if (nodata != null && nodata.length > 0) {
                        fillValue[i] = nodata[0];
                    }
                }
            }
        } catch (CoverageStoreException ex) {
            throw new ConstellationException("Failed to extract no-data values for resampling " + ex.getMessage(),ex);
        }

        //calculate scales
        final Map<Envelope, double[]> resolutionPerEnvelope = new HashMap<>();
        final double geospanX = dataEnv.getSpan(0);
        final double baseScale = geospanX / gg.getExtent().getSpan(0);
        final int tileSize = 256;
        double scale = geospanX / tileSize;
        final GeneralDirectPosition ul = new GeneralDirectPosition(dataEnv.getCoordinateReferenceSystem());
        ul.setOrdinate(0, dataEnv.getMinimum(0));
        ul.setOrdinate(1, dataEnv.getMaximum(1));
        final List<Double> scalesList = new ArrayList<>();
        while (true) {
            if (scale <= baseScale) {
                //fit to exact match to preserve base quality.
                scale = baseScale;
            }
            scalesList.add(scale);
            if (scale <= baseScale) {
                break;
            }
            scale = scale / 2;
        }
        final double[] scales = new double[scalesList.size()];
        for (int i = 0; i < scales.length; i++) scales[i] = scalesList.get(i);
        resolutionPerEnvelope.put(dataEnv, scales);

        //Prepare pyramid's mosaics.
        final Dimension tileDim = new Dimension(tileSize, tileSize);
        try {
            CoverageUtilities.getOrCreatePyramid(outRef, dataEnv, tileDim, scales);
        } catch (Exception ex) {
            throw new ConstellationException("Failed to create pyramid and mosaics in store " + ex.getMessage(),ex);
        }

        //prepare process
        final ProcessDescriptor desc;
        try {
            desc = ProcessFinder.getProcessDescriptor("coverage", "coveragepyramid");
        } catch (NoSuchIdentifierException ex) {
            throw new ConstellationException("Process coverage.coveragepyramid not found " + ex.getMessage(),ex);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("outData", pyramidDataBrief);

        //add task in scheduler
        try {
            final ParameterValueGroup input = desc.getInputDescriptor().createValue();
            input.parameter("coverageref").setValue(inRef);
            input.parameter("in_coverage_store").setValue(outRef.getStore());
            input.parameter("tile_size").setValue(new Dimension(tileSize, tileSize));
            input.parameter("pyramid_name").setValue(outRef.getName().getLocalPart());
            input.parameter("interpolation_type").setValue(InterpolationCase.NEIGHBOR);
            input.parameter("resolution_per_envelope").setValue(resolutionPerEnvelope);
            final org.geotoolkit.process.Process p = desc.createProcess(input);
            result.put("process", p);

            String taskName = "conform_pyramid_" + providerId + ":" + dataName + "_" + System.currentTimeMillis();
            TaskParameter taskParameter = new TaskParameter();
            taskParameter.setProcessAuthority(desc.getIdentifier().getAuthority().toString());
            taskParameter.setProcessCode(desc.getIdentifier().getCode());
            taskParameter.setDate(System.currentTimeMillis());
            taskParameter.setInputs(ParamUtilities.writeParameterJSON(input));
            taskParameter.setOwner(userOwnerId);
            taskParameter.setName(taskName);
            taskParameter.setType("INTERNAL");
            taskParameter = processBusiness.addTaskParameter(taskParameter);
            result.put("taskParameterId", taskParameter.getId());
            result.put("taskName", taskName);

        } catch ( IOException ex) {
            throw new ConstellationException("Unable to run pyramid process on scheduler",ex);
        }
        return result;
    }

	@Override
	public List<Integer> getProviderIdsAsInt() {
	        final List<Integer> ids = new ArrayList<>();
	        final List<Provider> providers = providerRepository.findAll();
	        for (Provider p : providers) {
	            ids.add(p.getId());
	        }
	        return ids;
	    
	}
}
