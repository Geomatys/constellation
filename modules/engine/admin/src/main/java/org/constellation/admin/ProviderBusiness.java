package org.constellation.admin;

import com.google.common.base.Optional;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
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
import org.constellation.configuration.AcknowlegementType;
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
import org.geotoolkit.coverage.Pyramid;
import org.geotoolkit.coverage.PyramidalCoverageReference;
import org.geotoolkit.data.FeatureStoreFactory;
import org.geotoolkit.data.FeatureStoreFinder;
import org.geotoolkit.data.FileFeatureStoreFactory;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.parameter.ParametersExt;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.ReferenceIdentifier;
import org.springframework.stereotype.Component;

@Component
public class ProviderBusiness implements IProviderBusiness {
    private static final Logger LOGGER = Logging.getLogger(ProviderBusiness.class);

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

    public List<Provider> getProviders() {
        return providerRepository.findAll();
    }

    public Provider getProvider(final String identifier) {
        return providerRepository.findByIdentifier(identifier);
    }

    public Provider getProvider(String providerIdentifier, int domainId) {
        return providerRepository.findByIdentifierAndDomainId(providerIdentifier, domainId);
    }

    public Provider getProvider(final int id) {
        return providerRepository.findOne(id);
    }

    public List<String> getProviderIds() {
        final List<String> ids = new ArrayList<>();
        final List<Provider> providers = providerRepository.findAll();
        for (Provider p : providers) {
            ids.add(p.getIdentifier());
        }
        return ids;
    }

    public void removeProvider(final String identifier) {
        datasetRepository.removeForProvider(identifier);
        providerRepository.deleteByIdentifier(identifier);
    }

    public void removeAll() {
        final List<Provider> providers = providerRepository.findAll();
        for (Provider p : providers) {
            datasetRepository.removeForProvider(p.getIdentifier());
            providerRepository.delete(p.getId());
        }
    }

    public List<Provider> getProviderChildren(final String identifier) {
        return providerRepository.findChildren(identifier);
    }

    public List<Integer> getProviderIdsForDomain(int domainId) {
        return providerRepository.getProviderIdsForDomain(domainId);
    }

    public List<Data> getDatasFromProviderId(Integer id) {
        return providerRepository.findDatasByProviderId(id);
    }

    public void updateParent(String providerIdentifier, String newParentIdentifier) {
        final Provider provider = getProvider(providerIdentifier);
        provider.setParent(newParentIdentifier);
        providerRepository.update(provider);
    }

    public List<Style> getStylesFromProviderId(Integer providerId) {
        return providerRepository.findStylesByProviderId(providerId);
    }


    public Provider createProvider(final String identifier, final String parent, final ProviderType type, final String serviceName,
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

    public Provider create(final int domainId, final String id, final ProviderConfiguration config) throws ConfigurationException {
        final String type = config.getType();
        final String subType = config.getSubType();
        final Map<String,String> inParams = config.getParameters();

        final DataProviderFactory providerService = DataProviders.getInstance().getFactory(type);
        final ParameterDescriptorGroup sourceDesc = providerService.getProviderDescriptor();
        ParameterValueGroup sources = sourceDesc.createValue();
        sources.parameter("id").setValue(id);
        sources.parameter("providerType").setValue(type);

        sources = fillProviderParameter(type, subType, inParams, sources);

        DataProvider dataProvider = DataProviders.getInstance().createProvider(id, providerService, sources);
        final int count = domainRepository.addProviderDataToDomain(id, domainId);
        final Provider provider = getProvider(id);
        final int provId = provider.getId();
        // for now we assume provider == dataset
        final Dataset dataset = datasetBusiness.createDataset(id, provId, null, null);

        // link to dataset
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

    private ParameterValueGroup fillProviderParameter(String type, String subType, Map<String, String> inParams, ParameterValueGroup sources) {
        switch (type) {
            case "sld":
                final String sldPath = inParams.get("path");
                String folderPath = sldPath.substring(0, sldPath.lastIndexOf('/'));
                sources.groups("sldFolder").get(0).parameter("path").setValue(folderPath);
                break;
            case "feature-store":

                boolean foundProvider = false;
                try {
                    final String filePath = inParams.get("path");
                    if (filePath != null && !filePath.isEmpty()) {
                        final URL url = new URL("file:" + filePath);
                        final File folder = new File(filePath);
                        final File[] candidates;
                        if(folder.isDirectory()){
                            candidates = folder.listFiles();
                        }else{
                            candidates = new File[]{folder};
                        }

                        search:
                        for(File candidate : candidates) {
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
                                            if (!tempExtension.endsWith("shp") && !tempExtension.endsWith("dbf") && candidates.length>1) {
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

                if(!foundProvider){
                    switch (subType) {
                        case "shapefile":
                            try {
                                final String shpPath = inParams.get("path");
                                final URL url = new URL("file:" + shpPath);
                                final ParameterValueGroup shpFolderParams = sources.groups("choice").get(0).addGroup("ShapeFileParametersFolder");
                                shpFolderParams.parameter("url").setValue(url);
                                shpFolderParams.parameter("namespace").setValue("no namespace");
                            } catch (MalformedURLException e) {
                                LOGGER.log(Level.WARNING, "unable to create url from path", e);
                            }
                            break;
                        case "om2":
                            final ParameterValueGroup omParams = sources.groups("choice").get(0).addGroup("OM2Parameters");
                            omParams.parameter("host").setValue(inParams.get("host"));
                            omParams.parameter("port").setValue(Integer.parseInt(inParams.get("port")));
                            omParams.parameter("database").setValue(inParams.get("database"));
                            omParams.parameter("user").setValue(inParams.get("user"));
                            omParams.parameter("password").setValue(inParams.get("password"));
                            omParams.parameter("sgbdtype").setValue(inParams.get("sgbdtype"));
                            omParams.parameter("namespace").setValue("no namespace");
                            break;
                        case "postgresql":
                            final ParameterValueGroup pgParams = sources.groups("choice").get(0).addGroup("PostgresParameters");
                            final int port = Integer.parseInt(inParams.get("port"));
                            pgParams.parameter("identifier").setValue("postgresql");
                            pgParams.parameter("host").setValue(inParams.get("host"));
                            pgParams.parameter("port").setValue(port);
                            pgParams.parameter("user").setValue(inParams.get("user"));
                            pgParams.parameter("password").setValue(inParams.get("password"));
                            pgParams.parameter("database").setValue(inParams.get("database"));
                            pgParams.parameter("namespace").setValue("no namespace");
                            pgParams.parameter("simple types").setValue(true);
                            break;
                        default:
                            final ParameterValueGroup defParams = sources.groups("choice").get(0).addGroup("PostgresParameters");
                            final int defPort = Integer.parseInt(inParams.get("port"));
                            defParams.parameter("identifier").setValue("postgresql");
                            defParams.parameter("host").setValue(inParams.get("host"));
                            defParams.parameter("port").setValue(defPort);
                            defParams.parameter("user").setValue(inParams.get("user"));
                            defParams.parameter("password").setValue(inParams.get("password"));
                            defParams.parameter("database").setValue(inParams.get("database"));
                            defParams.parameter("namespace").setValue("no namespace");
                            defParams.parameter("simple types").setValue(true);
                            break;
                    }
                }
                break;
            case "coverage-store":
                URL fileUrl = null;

                switch (subType) {
                    case "coverage-xml-pyramid":
                        try {
                            final String pyramidPath = inParams.get("path");
                            fileUrl = URI.create(pyramidPath).toURL();
                        } catch (MalformedURLException e) {
                            LOGGER.log(Level.WARNING, "unable to create url from path", e);
                        }
                        final ParameterValueGroup xmlCovParams = sources.groups("choice").get(0).addGroup("XMLCoverageStoreParameters");
                        xmlCovParams.parameter("identifier").setValue("coverage-xml-pyramid");
                        xmlCovParams.parameter("path").setValue(fileUrl);
                        xmlCovParams.parameter("type").setValue("AUTO");
                        break;
                    case "coverage-file":
                        try {
                            final String covPath = inParams.get("path");
                            fileUrl = URI.create("file:"+covPath).toURL();
                        } catch (MalformedURLException e) {
                            LOGGER.log(Level.WARNING, "unable to create url from path", e);
                        }

                        final ParameterValueGroup fileCovParams = sources.groups("choice").get(0).addGroup("FileCoverageStoreParameters");
                        fileCovParams.parameter("identifier").setValue("coverage-file");
                        fileCovParams.parameter("path").setValue(fileUrl);
                        fileCovParams.parameter("type").setValue("AUTO");
                        fileCovParams.parameter("namespace").setValue("no namespace");
                        break;
                    case "pgraster":
                        final ParameterValueGroup pgRasterParams = sources.groups("choice").get(0).addGroup("PGRasterParameters");
                        final int port = Integer.parseInt(inParams.get("port"));
                        pgRasterParams.parameter("identifier").setValue("postgresql");
                        pgRasterParams.parameter("host").setValue(inParams.get("host"));
                        pgRasterParams.parameter("port").setValue(port);
                        pgRasterParams.parameter("user").setValue(inParams.get("user"));
                        pgRasterParams.parameter("password").setValue(inParams.get("password"));
                        pgRasterParams.parameter("database").setValue(inParams.get("database"));
                        pgRasterParams.parameter("simple types").setValue(true);
                        break;
                    default:
                        LOGGER.log(Level.WARNING, "error on subtype definition");
                }
                break;
            case "observation-store":

                switch (subType) {
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
                final ReferenceIdentifier crsid = pyramid.getCoordinateReferenceSystem().getIdentifiers().iterator().next();

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
