/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.constellation.rest.api;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.logging.Logging;
import org.constellation.admin.DatasetBusiness;
import org.constellation.admin.ProviderBusiness;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.ProviderConfiguration;
import org.constellation.dto.ProviderPyramidChoiceList;
import org.constellation.dto.SimpleValue;
import org.constellation.engine.register.Dataset;
import org.constellation.engine.register.Provider;
import org.constellation.engine.register.repository.DomainRepository;
import org.constellation.provider.CoverageData;
import org.constellation.provider.Data;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviderFactory;
import org.constellation.provider.DataProviders;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.rs.LayerProviders;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.coverage.GridSampleDimension;
import org.geotoolkit.coverage.Pyramid;
import org.geotoolkit.coverage.PyramidalCoverageReference;
import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.data.FeatureStoreFactory;
import org.geotoolkit.data.FeatureStoreFinder;
import org.geotoolkit.data.FileFeatureStoreFactory;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.io.wkt.PrjFiles;
import org.geotoolkit.parameter.ParametersExt;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.storage.DataFileStore;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.ImageCRS;
import org.opengis.util.FactoryException;

/**
 * RestFull API for provider management/operations.
 *
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
@Path("/1/domain/{domainId}/provider")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public final class ProviderRest {

    private static final Logger LOGGER = Logging.getLogger(ProviderRest.class);

    
    @Inject
    private DomainRepository domainRepository;
    
    @Inject
    private ProviderBusiness providerBusiness;
    
    @Inject
    private DatasetBusiness datasetBusiness;
    

    @POST
    @Path("/{id}/test")
    public Response test( final @PathParam("domainId") int domainId, final @PathParam("id") String providerIdentifier, final ProviderConfiguration configuration) {
        try {
            final String type = configuration.getType();
            final String subType = configuration.getSubType();
            final Map<String, String> inParams = configuration.getParameters();

            final DataProviderFactory providerService = DataProviders.getInstance().getFactory(type);
            final ParameterDescriptorGroup sourceDesc = providerService.getProviderDescriptor();
            ParameterValueGroup sources = sourceDesc.createValue();
            sources.parameter("id").setValue(providerIdentifier);
            sources.parameter("providerType").setValue(type);
            sources = fillProviderParameter(type, subType, inParams, sources);
            final Set<Name> names = DataProviders.getInstance().testProvider(providerIdentifier, providerService, sources);
            if (names.isEmpty()){
                LOGGER.warning("non data found for provider: " + providerIdentifier);
                return Response.status(500).build();
            }

        } catch (DataStoreException e) {
            return Response.status(500).build();
        }
        return Response.ok().type(MediaType.TEXT_PLAIN_TYPE).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(final @PathParam("domainId") int domainId, final @PathParam("id") String id, final ProviderConfiguration config) {
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
        return Response.ok().type(MediaType.TEXT_PLAIN_TYPE).build();
    }

    /**
     * Create a new provider from the given configuration.
     */
    @POST
    @Path("/{id}")
    public Response create(final @PathParam("domainId") int domainId, final @PathParam("id") String id, final ProviderConfiguration config) {
        final String type = config.getType();
        final String subType = config.getSubType();
        final Map<String,String> inParams = config.getParameters();

        final DataProviderFactory providerService = DataProviders.getInstance().getFactory(type);
        final ParameterDescriptorGroup sourceDesc = providerService.getProviderDescriptor();
        ParameterValueGroup sources = sourceDesc.createValue();
        sources.parameter("id").setValue(id);
        sources.parameter("providerType").setValue(type);

        sources = fillProviderParameter(type, subType, inParams, sources);

        try {
            DataProvider dataProvider = DataProviders.getInstance().createProvider(id, providerService, sources);
            final int count = domainRepository.addProviderDataToDomain(id, domainId );
            final int provId = providerBusiness.getProvider(id).getId();
            // for now we assume provider == dataset
            final Dataset dataset = datasetBusiness.createDataset(id, provId, null, null);
            
            // link to dataset
            datasetBusiness.linkDataTodataset(dataset, providerBusiness.getDatasFromProviderId(provId));
                    
            LOGGER.info("Added " + count + " data to domain " + domainId);
        } catch (ConfigurationException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            return Response.status(500).build();
        }

        return Response.ok().type(MediaType.TEXT_PLAIN_TYPE).build();
    }

    @GET
    @Path("/{id}/epsgCode")
    public Response getAllEpsgCode(final @PathParam("domainId") int domainId, final @PathParam("id") String providerIdentifier) throws FactoryException {
        final CRSAuthorityFactory factory = CRS.getAuthorityFactory(Boolean.FALSE);
        final Set<String> authorityCodes = factory.getAuthorityCodes(CoordinateReferenceSystem.class);
        List<String> codes = new ArrayList<>();
        for (String code : authorityCodes){
            code += " - " + factory.getDescriptionText(code).toString();
            codes.add(code);
        }
        return Response.ok().entity(codes).build();
    }



    @POST
    @Path("/{id}/createprj")
    public Response createPrj(final @PathParam("domainId") int domainId, final @PathParam("id") String providerIdentifier, Map<String,String> epsgCode) throws DataStoreException, FactoryException, IOException {
        final DataProvider provider = DataProviders.getInstance().getProvider(providerIdentifier);
        if (provider.getMainStore() instanceof DataFileStore){
            File[] dataFiles = ((DataFileStore) provider.getMainStore()).getDataFiles();
            if (dataFiles.length == 1 && dataFiles[0].isDirectory()){
                dataFiles = dataFiles[0].listFiles();
            }
            final String fileNameWithoutExtention = dataFiles[0].getName().substring(0, dataFiles[0].getName().indexOf('.'));
            final String parentPath = dataFiles[0].getParentFile().getAbsolutePath();
            final CoordinateReferenceSystem coordinateReferenceSystem = CRS.decode(epsgCode.get("codeEpsg"));
            PrjFiles.write(coordinateReferenceSystem, new File(parentPath+File.separator+fileNameWithoutExtention+".prj"));
            provider.reload();
            return Response.ok().type(MediaType.TEXT_PLAIN_TYPE).build();
        }
        return Response.status(500).build();
    }

    /**
     * Create a new provider from the given configuration.
     */
    @GET
    @Path("/{id}/crs")
    public Response verifyCRS(final @PathParam("domainId") int domainId, final @PathParam("id") String providerIdentifier){
        try {


            final HashMap<Name, CoordinateReferenceSystem> nameCoordinateReferenceSystemHashMap = DataProviders.getInstance().getCRS(providerIdentifier);
            for( CoordinateReferenceSystem crs : nameCoordinateReferenceSystemHashMap.values()){
                if (crs == null || crs instanceof ImageCRS){
                    return Response.status(500).build();
                }
            }
            return Response.ok(true).build();

        } catch (DataStoreException e) {
            return Response.status(500).build();
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

    /**
     * Delete a provider with the given id.
     * @param id
     */
    @DELETE
    @Path("{id}")
    public Response delete(final @PathParam("id") String id) {
        final DataProvider old = DataProviders.getInstance().getProvider(id);
        try {
            DataProviders.getInstance().removeProvider(old);
        } catch (ConfigurationException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            return Response.status(500).build();
        }
        return Response.ok().type(MediaType.TEXT_PLAIN_TYPE).build();
    }

    /**
     * @see LayerProviders#getDataDescription(String, String)
     */
    @GET
    @Path("{id}/{layerName}/dataDescription")
    public Response dataDescription(final @PathParam("domainId") int domainId, final @PathParam("id") String id,
                                    final @PathParam("layerName") String layerName) {
        try {
            return Response.ok(LayerProviders.getDataDescription(id, layerName)).build();
        } catch (CstlServiceException ex) {
            return Response.ok(new AcknowlegementType("Failure", ex.getLocalizedMessage())).build();
        }
    }

    /**
     * @see LayerProviders#getPropertyValues(String, String, String)
     */
    @GET
    @Path("{id}/{layerName}/{property}/propertyValues")
    public Response propertyValues(final @PathParam("domainId") int domainId, final @PathParam("id") String id,
                                   final @PathParam("layerName") String layerName,
                                   final @PathParam("property") String property) {
        try {
            return Response.ok(LayerProviders.getPropertyValues(id, layerName, property)).build();
        } catch (CstlServiceException ex) {
            return Response.ok(new AcknowlegementType("Failure", ex.getLocalizedMessage())).build();
        }
    }

    /**
     * Indicate if given provider contains a geophysic data.
     */
    @GET
    @Path("{id}/{layerName}/isGeophysic")
    public Response isGeophysic(final @PathParam("domainId") int domainId, final @PathParam("id") String id,
                                final @PathParam("layerName") String layerName) {
        
        boolean isGeophysic = false;
        try {
            final Data data = LayerProviders.getLayer(id, layerName);
            if(data!=null && data.getOrigin() instanceof CoverageReference){
                final CoverageReference ref = (CoverageReference) data.getOrigin();
                final GridCoverageReader reader = ref.acquireReader();
                final List<GridSampleDimension> dims = reader.getSampleDimensions(ref.getImageIndex());
                if(dims!=null && !dims.isEmpty()){
                    isGeophysic = true;
                }                
                ref.recycle(reader);
            }
        } catch (CstlServiceException|CoverageStoreException ex) {
            return Response.ok(new AcknowlegementType("Failure", ex.getLocalizedMessage())).build();
        }
                
       return Response.ok(new SimpleValue(isGeophysic)).build();
    }
        
    /**
     * List the available pyramids for this layer
     */
    @GET
    @Path("{id}/{layerName}/listPyramidChoice")
    public Response listPyramids(final @PathParam("domainId") int domainId, final @PathParam("id") String id,
                                final @PathParam("layerName") String layerName) {
        
        final ProviderPyramidChoiceList choices = new ProviderPyramidChoiceList();
        
        final List<Provider> childrenRecs = providerBusiness.getProviderChildren(id);
        
        for(Provider childRec : childrenRecs){
            final DataProvider provider = DataProviders.getInstance().getProvider(childRec.getIdentifier());
            final CoverageData cacheData = (CoverageData) provider.get(layerName);
            if(cacheData!=null){
                final PyramidalCoverageReference cacheRef = (PyramidalCoverageReference) cacheData.getOrigin();
                try {
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
                    
                } catch (DataStoreException ex) {
                    return Response.ok(new AcknowlegementType("Failure", ex.getLocalizedMessage())).build();
                }
            }
        }
        
        return Response.ok(choices).build();
    }
    
    /**
     * @see LayerProviders#getBandValues(String, String, int)
     */
    @GET
    @Path("{id}/{layerName}/{bandIndex}/bandValues")
    public Response bandValues(final @PathParam("domainId") int domainId, final @PathParam("id") String id,
                               final @PathParam("layerName") String layerName,
                               final @PathParam("bandIndex") int bandIndex) {
        try {
            return Response.ok(LayerProviders.getBandValues(id, layerName, bandIndex)).build();
        } catch (CstlServiceException ex) {
            return Response.ok(new AcknowlegementType("Failure", ex.getLocalizedMessage())).build();
        }
    }

    /**
     *  
     * Is this method still used ??
     * 
     * No longer metadata for provider but for dataset
     */
    @GET
    @Path("metadata/{providerId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getMetadata(final @PathParam("domainId") int domainId, final @PathParam("providerId") String providerId) throws ConfigurationException {
        // for now assume that providerID == datasetID
        DefaultMetadata metadata = datasetBusiness.getMetadata(providerId,domainId);
        return Response.ok(metadata).build();
    }

    /**
     *  
     * Is this method still used ??
     * 
     * No longer metadata for provider but for dataset
     */
    @POST
    @Path("metadata/{providerId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response setMetadata(final @PathParam("domainId") int domainId, final @PathParam("providerId") String providerId, final DefaultMetadata metadata) throws ConfigurationException {
        // for now assume that providerID == datasetID
        datasetBusiness.updateMetadata(providerId, domainId, metadata);
        return Response.ok().type(MediaType.TEXT_PLAIN_TYPE).build();
    }
}
