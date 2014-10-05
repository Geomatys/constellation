package org.constellation.admin;

import com.google.common.base.Optional;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;

import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.logging.Logging;
import org.constellation.admin.util.IOUtilities;
import org.constellation.api.ProviderType;
import org.constellation.business.IDatasetBusiness;
import org.constellation.business.IProviderBusiness;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.ProviderConfiguration;
import org.constellation.dto.ProviderPyramidChoiceList;
import org.constellation.engine.register.CstlUser;
import org.constellation.engine.register.Data;
import org.constellation.engine.register.Dataset;
import org.constellation.engine.register.Provider;
import org.constellation.engine.register.Style;
import org.constellation.engine.register.repository.DatasetRepository;
import org.constellation.engine.register.repository.DomainRepository;
import org.constellation.engine.register.repository.ProviderRepository;
import org.constellation.engine.register.repository.UserRepository;
import org.constellation.provider.CoverageData;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviderFactory;
import org.constellation.provider.DataProviders;
import org.geotoolkit.coverage.CoverageStoreFactory;
import org.geotoolkit.coverage.CoverageStoreFinder;
import org.geotoolkit.coverage.Pyramid;
import org.geotoolkit.coverage.PyramidalCoverageReference;
import org.geotoolkit.data.FeatureStoreFactory;
import org.geotoolkit.data.FeatureStoreFinder;
import org.geotoolkit.data.FileFeatureStoreFactory;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.observation.ObservationStoreFactory;
import org.geotoolkit.parameter.Parameters;
import org.geotoolkit.parameter.ParametersExt;
import org.geotoolkit.storage.DataStoreFactory;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.metadata.Identifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
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

    @Inject
    private UserRepository userRepository;

    @Inject
    private DomainRepository domainRepository;

    @Inject
    private ProviderRepository providerRepository;
    
    @Inject
    private DatasetRepository datasetRepository;

    @Inject
    private IDatasetBusiness datasetBusiness;

    @Inject
    private org.constellation.security.SecurityManager securityManager;

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
    public void removeProvider(final String identifier) {
        providerRepository.deleteByIdentifier(identifier);
    }

    @Override
    public void removeAll() {
        final List<Provider> providers = providerRepository.findAll();
        for (Provider p : providers) {
            providerRepository.delete(p.getId());
        }
    }

    @Override
    public List<Provider> getProviderChildren(final String identifier) {
        return providerRepository.findChildren(identifier);
    }

    @Override
    public List<Integer> getProviderIdsForDomain(int domainId) {
        return providerRepository.getProviderIdsForDomain(domainId);
    }

    @Override
    public List<Data> getDatasFromProviderId(Integer id) {
        return providerRepository.findDatasByProviderId(id);
    }

    @Override
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
    public Set<Name> test(final String providerIdentifier, final ProviderConfiguration configuration) throws DataStoreException {
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
    public Provider create(final int domainId, final String id, final DataStoreFactory spi, ParameterValueGroup spiConfiguration) throws ConfigurationException {
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

        return create(domainId, id, pFactory.getName(), providerConfig);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Provider create(final int domainId, final String id, final ProviderConfiguration config) throws ConfigurationException {
        final String type = config.getType();
        final String subType = config.getSubType();
        final Map<String,String> inParams = config.getParameters();

        final DataProviderFactory providerService = DataProviders.getInstance().getFactory(type);
        final ParameterDescriptorGroup sourceDesc = providerService.getProviderDescriptor();
        ParameterValueGroup sources = sourceDesc.createValue();
        sources.parameter("id").setValue(id);
        sources.parameter("providerType").setValue(type);

        return create(domainId, id, providerService.getName(), fillProviderParameter(type, subType, inParams, sources));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Provider create(final int domainId, final String id, final String providerSPIName, final ParameterValueGroup providerConfig) throws ConfigurationException {
        final DataProviderFactory providerSPI = DataProviders.getInstance().getFactory(providerSPIName);
        DataProviders.getInstance().createProvider(id, providerSPI, providerConfig);
        final int count = domainRepository.addProviderDataToDomain(id, domainId);
        final Provider provider = getProvider(id);
        final int provId = provider.getId();

        // for now we assume provider == dataset, so we create a dataset bound to the new provider.
        final Dataset dataset = datasetBusiness.createDataset(id, null, null, provider.getOwner());
        datasetBusiness.linkDataTodataset(dataset, getDatasFromProviderId(provId));

        LOGGER.info("Added " + count + " data to domain " + domainId);
        return provider;
    }

    public void update(final int domainId, final String id, final ProviderConfiguration config) {
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

    protected ParameterValueGroup fillProviderParameter(String type, String subType, Map<String, String> inParams, ParameterValueGroup sources) {
        switch (type) {
            case "sld":
                final String sldPath = inParams.get("path");
                String folderPath = sldPath.substring(0, sldPath.lastIndexOf('/'));
                sources.groups("sldFolder").get(0).parameter("path").setValue(folderPath);
                break;

            case "feature-store":

                // TODO : What's going on here ? We should have received parameters matching a specific factory, not some
                // randomly named parameters.
                boolean foundProvider = false;
                try {
                    final String filePath = inParams.get("path");
                    if (filePath != null && !filePath.isEmpty()) {
                        final URL url = new URL("file:" + filePath);
                        final File folder = new File(filePath);
                        final File[] candidates;
                        if (folder.isDirectory()) {
                            candidates = folder.listFiles();
                        } else {
                            candidates = new File[]{folder};
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
                                        //we do not want shapefiles or dbf types, a folder provider will be created in those cases
                                        if (candidateName.endsWith(tempExtension)) {
                                            if (!tempExtension.endsWith("shp") && !tempExtension.endsWith("dbf") && candidates.length > 1) {
                                                //found a factory which can handle it
                                                final ParameterValueGroup params = sources.groups("choice").get(0).addGroup(
                                                        factory.getParametersDescriptor().getName().getCode());
                                                params.parameter("url").setValue(url);
                                                params.parameter("namespace").setValue("no namespace");
                                                foundProvider = true;
                                                //TODO we should add all files which define a possible feature-store
                                                //but the web interfaces do not handle that yet, so we limit to one for now.
                                                break search;
                                            }
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
                                            //TODO we should add all files which define a possible feature-store
                                            //but the web interfaces do not handle that yet, so we limit to one for now.
                                            break search;
                                        }

                                    } catch (Exception ex) {
                                        //parameter might not exist
                                    }

                                }
                            }
                        }
                    }

                } catch (MalformedURLException e) {
                    LOGGER.log(Level.WARNING, "unable to create url from path", e);
                }

                if (!foundProvider) {
                    final FeatureStoreFactory featureFactory = FeatureStoreFinder.getFactoryById(subType);
                    final ParameterValueGroup cvgConfig = Parameters.toParameter(inParams, featureFactory.getParametersDescriptor(), true);
                    final ParameterValueGroup choice =
                            sources.groups("choice").get(0).addGroup(cvgConfig.getDescriptor().getName().getCode());
                    Parameters.copy(cvgConfig, choice);
                }
                break;

            case "coverage-store":
                // TODO : remove this crappy hack after provider system refactoring.
                final String filePath = inParams.get("path");
                if (filePath != null && !filePath.toLowerCase().startsWith("file:")) {
                    inParams.put("path", "file:"+filePath);
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
}
