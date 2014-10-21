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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
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
import org.apache.sis.util.collection.Cache;
import org.apache.sis.util.logging.Logging;
import org.constellation.business.IDatasetBusiness;
import org.constellation.business.IProviderBusiness;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.ProviderConfiguration;
import org.constellation.dto.DataDescription;
import org.constellation.dto.ParameterValues;
import org.constellation.dto.SimpleValue;
import org.constellation.provider.Data;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviders;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.rs.LayerProviders;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.coverage.GridSampleDimension;
import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.io.wkt.PrjFiles;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.storage.DataFileStore;
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
    private IProviderBusiness providerBusiness;

    @Inject
    private IDatasetBusiness datasetBusiness;

    @Inject
    private LayerProviders layerProviders;

    /**
     * @FIXME remove this cache used for demo and add more generic cache management (Spring).
     */
    private static final Cache<String,DataDescription> CACHE_DATA_DESC = new Cache<>(10, 10, true);

    @POST
    @Path("/{id}/test")
    public Response test( final @PathParam("domainId") int domainId, final @PathParam("id") String providerIdentifier, final ProviderConfiguration configuration) {
        try {
            final Set<Name> names = providerBusiness.test(providerIdentifier, configuration);
            if (names.isEmpty()){
                LOGGER.warning("non data found for provider: " + providerIdentifier);
                return Response.status(500).build();
            }
        } catch (DataStoreException e) {
            LOGGER.log(Level.WARNING, "Cannot open provider "+providerIdentifier+" for domain "+domainId, e);
            return Response.status(500).build();
        }
        return Response.ok().type(MediaType.TEXT_PLAIN_TYPE).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(final @PathParam("domainId") int domainId, final @PathParam("id") String id, final ProviderConfiguration config) {
        providerBusiness.update(domainId, id, config);
        return Response.ok().type(MediaType.TEXT_PLAIN_TYPE).build();
    }

    /**
     * Create a new provider from the given configuration.
     */
    @POST
    @Path("/{id}")
    public Response create(final @PathParam("domainId") int domainId, final @PathParam("id") String id, final ProviderConfiguration config) {
        try {
            providerBusiness.create(domainId, id, config);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            return Response.status(500).entity(ex.getLocalizedMessage()).build();
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
            LOGGER.log(Level.WARNING, "Cannot get CRS for provider "+providerIdentifier+" for domain "+domainId, e);
            return Response.status(500).build();
        }
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
    @POST
    @Path("dataDescription")
    public Response dataDescription(final ParameterValues values) {
        try {
            final String id = values.getValues().get("providerId");
            final String layerName = values.getValues().get("dataId");
            final String cacheKey = id+"_"+layerName;

            return Response.ok(
                    CACHE_DATA_DESC.getOrCreate(cacheKey, new Callable<DataDescription>() {
                        @Override
                        public DataDescription call() throws Exception {
                            return layerProviders.getDataDescription(id, layerName);
                        }
                    })
            ).build();

        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, null, ex);
            return Response.status(500).entity(new AcknowlegementType("Failure", ex.getLocalizedMessage())).build();
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
            LOGGER.log(Level.WARNING, "Cannot retrieve information for layer "+layerName+" for domain "+domainId, ex);
            return Response.status(500).entity(new AcknowlegementType("Failure", ex.getLocalizedMessage())).build();
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
            LOGGER.log(Level.WARNING, "Cannot retrieve information for layer "+layerName+" for domain "+domainId, ex);
            return Response.status(500).entity(new AcknowlegementType("Failure", ex.getLocalizedMessage())).build();
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
        try {
            return Response.ok(providerBusiness.listPyramids(id, layerName)).build();
        } catch (DataStoreException ex) {
            LOGGER.log(Level.WARNING, "Cannot retrieve information for layer "+layerName+" for domain "+domainId, ex);
            return Response.status(500).entity(new AcknowlegementType("Failure", ex.getLocalizedMessage())).build();
        }
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
            LOGGER.log(Level.WARNING, "Cannot retrieve information for layer "+layerName+" for domain "+domainId, ex);
            return Response.status(500).entity(new AcknowlegementType("Failure", ex.getLocalizedMessage())).build();
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
    public Response getMetadata(final @PathParam("domainId") int domainId, final @PathParam("providerId") String providerId) {
        // for now assume that providerID == datasetID
        try {
            return Response.ok(datasetBusiness.getMetadata(providerId,domainId)).build();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Cannot retrieve metadata for provider "+providerId+" for domain "+domainId, e);
            return Response.status(500).entity(new AcknowlegementType("Failure", e.getLocalizedMessage())).build();
        }
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
    public Response setMetadata(final @PathParam("domainId") int domainId, final @PathParam("providerId") String providerId, final DefaultMetadata metadata) {
        // for now assume that providerID == datasetID
        try {
            datasetBusiness.updateMetadata(providerId, domainId, metadata);
            return Response.ok().type(MediaType.TEXT_PLAIN_TYPE).build();
        } catch (ConfigurationException e) {
            LOGGER.log(Level.WARNING, "Cannot update metadata for provider "+providerId+" for domain "+domainId, e);
            return Response.status(500).entity(new AcknowlegementType("Failure", e.getLocalizedMessage())).build();
        }
    }
}
