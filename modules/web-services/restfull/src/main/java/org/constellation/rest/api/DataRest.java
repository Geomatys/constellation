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

import com.google.common.base.Optional;
import org.apache.sis.geometry.Envelopes;
import org.apache.sis.geometry.GeneralDirectPosition;
import org.apache.sis.geometry.GeneralEnvelope;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.storage.DataStore;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.logging.Logging;
import org.constellation.admin.dto.MapContextLayersDTO;
import org.constellation.admin.dto.MapContextStyledLayerDTO;
import org.constellation.admin.exception.ConstellationException;
import org.constellation.business.IDataBusiness;
import org.constellation.business.IDatasetBusiness;
import org.constellation.business.IMapContextBusiness;
import org.constellation.business.IMetadataBusiness;
import org.constellation.business.IProcessBusiness;
import org.constellation.business.IProviderBusiness;
import org.constellation.business.ISensorBusiness;
import org.constellation.business.IStyleBusiness;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.DataBrief;
import org.constellation.configuration.DataCustomConfiguration;
import org.constellation.configuration.DataSetBrief;
import org.constellation.configuration.ProviderConfiguration;
import org.constellation.configuration.StringList;
import org.constellation.configuration.StyleBrief;
import org.constellation.configuration.TargetNotFoundException;
import org.constellation.dto.FileBean;
import org.constellation.dto.ImportedData;
import org.constellation.dto.MetadataLists;
import org.constellation.dto.ParameterValues;
import org.constellation.dto.ProviderData;
import org.constellation.dto.SimpleValue;
import org.constellation.engine.register.jooq.tables.pojos.CstlUser;
import org.constellation.engine.register.jooq.tables.pojos.Dataset;
import org.constellation.engine.register.jooq.tables.pojos.Mapcontext;
import org.constellation.engine.register.jooq.tables.pojos.MapcontextStyledLayer;
import org.constellation.engine.register.jooq.tables.pojos.Provider;
import org.constellation.engine.register.jooq.tables.pojos.Sensor;
import org.constellation.engine.register.jooq.tables.pojos.TaskParameter;
import org.constellation.engine.register.repository.SensorRepository;
import org.constellation.engine.register.repository.ServiceRepository;
import org.constellation.engine.register.repository.StyleRepository;
import org.constellation.engine.register.repository.UserRepository;
import org.constellation.engine.security.WorkspaceService;
import org.constellation.json.metadata.binding.RootObj;
import org.constellation.json.metadata.v2.Template;
import org.constellation.model.SelectedExtension;
import org.constellation.provider.Data;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviderFactory;
import org.constellation.provider.DataProviders;
import org.constellation.provider.Providers;
import org.constellation.provider.configuration.ProviderParameters;
import org.constellation.util.ParamUtilities;
import org.constellation.util.Util;
import org.constellation.utils.GeotoolkitFileExtensionAvailable;
import org.constellation.utils.MetadataFeeder;
import org.geotoolkit.coverage.grid.GeneralGridGeometry;
import org.geotoolkit.coverage.grid.ViewType;
import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.coverage.xmlstore.XMLCoverageReference;
import org.geotoolkit.coverage.xmlstore.XMLCoverageStore;
import org.geotoolkit.coverage.xmlstore.XMLCoverageStoreFactory;
import org.geotoolkit.data.FeatureStoreFactory;
import org.geotoolkit.data.FeatureStoreFinder;
import org.geotoolkit.data.memory.ExtendedFeatureStore;
import org.geotoolkit.display.PortrayalException;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.sos.netcdf.NetCDFExtractor;
import org.geotoolkit.storage.DataFileStore;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.util.FileUtilities;
import org.geotoolkit.util.StringUtilities;
import org.geotoolkit.utility.parameter.ParametersExt;
import org.geotoolkit.wms.WebMapClient;
import org.geotoolkit.wms.map.WMSMapLayer;
import org.geotoolkit.wms.xml.WMSVersion;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.ImageCRS;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import org.opengis.util.NoSuchIdentifierException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;

import static org.constellation.utils.RESTfulUtilities.ok;
import org.geotoolkit.feature.type.NamesExt;
import org.geotoolkit.storage.coverage.CoverageReference;
import org.geotoolkit.storage.coverage.CoverageStore;
import org.geotoolkit.storage.coverage.CoverageStoreFactory;
import org.geotoolkit.storage.coverage.CoverageStoreFinder;
import org.geotoolkit.storage.coverage.CoverageUtilities;
import org.geotoolkit.storage.coverage.PyramidalCoverageReference;
import org.opengis.util.GenericName;

/**
 * Manage data sending
 *
 * @author Benjamin Garcia (Geomatys)
 * @author Christophe Mourette (Geomatys)
 */
@Component
@Path("/1/domain/{domainId}/data/")
public class DataRest {

    private final XMLInputFactory xif = XMLInputFactory.newFactory();

    private static final Logger LOGGER = Logging.getLogger(DataRest.class);

    private static final String RENDERED_PREFIX = "rendered_";

    @Inject
    private UserRepository userRepository;

    @Inject
    private StyleRepository styleRepository;

    @Inject
    private ServiceRepository serviceRepository;

    @Inject
    private SensorRepository sensorRepository;

    @Inject
    private IStyleBusiness styleBusiness;

    @Inject
    private IDataBusiness dataBusiness;

    @Inject
    private IDatasetBusiness datasetBusiness;

    @Inject
    private IProviderBusiness providerBusiness;

    @Inject
    private ISensorBusiness sensorBusiness;

    @Inject
    private IProcessBusiness processBusiness;

    @Inject
    private WorkspaceService workspaceService;

    @Inject
    private IMapContextBusiness mapContextBusiness;

    @Inject
    private IMetadataBusiness metadataBusiness;

    /**
     * Give metadata CodeLists (example {@link org.opengis.metadata.citation.Role} codes
     *
     * @return a {@link javax.ws.rs.core.Response} which contain codelists
     */
    @GET
    @Path("metadataCodeLists")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getMetadataCodeLists() {
        final MetadataLists mdList = metadataBusiness.getMetadataCodeLists();
        return Response.ok().entity(mdList).build();
    }


    /**
     * Give subfolder list of data from a server file path
     *
     * @param path server file path
     * @param filtered {@code True} if we want to keep only known files.
     * @return a {@link javax.ws.rs.core.Response} which contain file list
     */
    @POST
    @Path("datapath/{filtered}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getDataFolder(@PathParam("filtered") final Boolean filtered, final String path) {
        try {
            final List<FileBean> listBean = dataBusiness.getFilesFromPath(path, filtered, false);
            return Response.status(200).entity(listBean).build();
        }catch(Exception ex) {
            Map<String,String> hashMap = new HashMap<>();
            hashMap.put("msg", ex.getMessage());
            return Response.status(500).entity(hashMap).build();
        }
    }

    /**
     * Give subfolder list of metadata xml from a server file path
     *
     * @param path server file path
     * @param filtered {@code True} if we want to keep only known files.
     * @return a {@link javax.ws.rs.core.Response} which contain file list
     */
    @POST
    @Path("metadatapath/{filtered}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getMetaDataFolder(@PathParam("filtered") final Boolean filtered, final String path) {
        try {
            final List<FileBean> listBean = dataBusiness.getFilesFromPath(path, filtered, true);
            return Response.status(200).entity(listBean).build();
        }catch(Exception ex) {
            Map<String,String> hashMap = new HashMap<>();
            hashMap.put("msg", ex.getMessage());
            return Response.status(500).entity(hashMap).build();
        }
    }

    /**
     * Receive a {@link MultiPart} which contain a file need to be save on server to create data on provider
     * @return A {@link Response} with 200 code if upload work, 500 if not work.
     */
    @POST
    @Path("upload/data")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response uploadData(@FormDataParam("data") InputStream fileIs,
                               @FormDataParam("data") FormDataContentDisposition fileDetail,
                               @FormDataParam("metadata") InputStream fileMetaIs,
                               @FormDataParam("metadata") FormDataContentDisposition fileMetaDetail,
                               @Context HttpServletRequest request) {
        final File uploadDirectory = workspaceService.getUploadDirectory();
        HashMap<String,String> hashMap = new HashMap<>();
        String dataName = fileDetail.getFileName();
        final File newFileData = new File(uploadDirectory, dataName);
        try {
            if (fileIs != null) {
                if (!uploadDirectory.exists()) {
                    uploadDirectory.mkdir();
                }
                Files.copy(fileIs, newFileData.toPath(), StandardCopyOption.REPLACE_EXISTING);
                //fileIs.close();
                hashMap.put("dataPath", newFileData.getAbsolutePath());
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            return Response.status(500).entity(ex.getLocalizedMessage()).build();
        }
        return Response.ok(hashMap).build();
    }

    /**
     * Receive a {@link MultiPart} which contain a file need to be save on server to create data on provider
     * @return A {@link Response} with 200 code if upload work, 500 if not work.
     */
    @POST
    @Path("upload/metadata")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response uploadMetadata(@FormDataParam("metadata") InputStream mdFileIs,
                                   @FormDataParam("metadata") FormDataContentDisposition fileMetaDetail,
                                   @FormDataParam("identifier") String identifier,
                                   @FormDataParam("serverMetadataPath") String serverMetadataPath,
                                   @Context HttpServletRequest request) {

        final File uploadDirectory = workspaceService.getUploadDirectory();
        Map<String,String> hashMap = new HashMap<>();
        if (identifier != null && ! identifier.isEmpty()){
            hashMap.put("dataName", identifier);
        } else {
            if (serverMetadataPath !=null && serverMetadataPath.length()>0){
                hashMap.put("metadataPath", serverMetadataPath);
                try {
                    extractIdentifierFromMetadataFile(hashMap,new File(serverMetadataPath));
                }catch (ConstellationException ex){
                    hashMap.put("msg", ex.getLocalizedMessage());
                    return Response.status(500).entity(hashMap).build();
                }
            } else  if (! fileMetaDetail.getFileName().isEmpty()) {
                final File newFileMetaData = new File(uploadDirectory, fileMetaDetail.getFileName());
                try {
                    if (mdFileIs != null) {
                        if (!uploadDirectory.exists()) {
                            uploadDirectory.mkdir();
                        }
                        Files.copy(mdFileIs, newFileMetaData.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        //mdFileIs.close();

                        try {
                            extractIdentifierFromMetadataFile(hashMap, newFileMetaData);
                        }catch (ConstellationException ex){
                            hashMap.put("msg", ex.getLocalizedMessage());
                            return Response.status(500).entity(hashMap).build();
                        }
                    }
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
                    return Response.status(500).entity(ex.getLocalizedMessage()).build();
                }
            }
        }
        //verify uniqueness of data identifier
        final Provider dataName = providerBusiness.getProvider(hashMap.get("dataName"));
        if (dataName!=null){
            hashMap.put("msg", "dataName or identifier of metadata is already used");
            return Response.status(500).entity(hashMap).build();
        }
        return Response.status(200).entity(hashMap).build();
    }

    private void extractIdentifierFromMetadataFile(final Map<String,String> hashMap,
                                                   final File newFileMetaData) throws ConstellationException{
        Object obj;
        if (metadataBusiness.isSpecialMetadataFormat(newFileMetaData)) {
            try {
                obj = metadataBusiness.getMetadataFromSpecialFormat(newFileMetaData);
            } catch (ConfigurationException ex) {
                LOGGER.log(Level.WARNING, "Error when trying to read dimap metadata", ex);
                throw new ConstellationException("Dimap metadata file is incorrect");
            }
        } else {
            try {
                obj = metadataBusiness.unmarshallMetadata(newFileMetaData);
            } catch (ConfigurationException ex) {
                LOGGER.log(Level.WARNING, "Error when trying to unmarshal metadata", ex);
                throw new ConstellationException("metadata file is incorrect");
            }
        }
        if (!(obj instanceof DefaultMetadata)) {
            throw new ConstellationException("metadata file is incorrect");
        }
        final DefaultMetadata metadata = (DefaultMetadata)obj;
        final String metaIdentifier = metadata.getFileIdentifier();
        if (metaIdentifier!=null && ! metaIdentifier.isEmpty()) {
            hashMap.put("dataName", metaIdentifier);
        }else {
            throw new ConstellationException("metadata does not contains any identifier," +
                    " please check the fileIdentifier in your metadata.");
        }
        hashMap.put("metadataPath",newFileMetaData.getAbsolutePath());
        final MetadataFeeder mdPojo = new MetadataFeeder(metadata);
        final String title = mdPojo.getTitle();
        hashMap.put("metatitle",title);
        hashMap.put("metaIdentifier", metaIdentifier);
    }

    /**
     * Import data from upload Directory to integrated directory
     * - change file location from upload to integrated
     * this method do all chain: init provider and metadata.
     *
     * @param domainId given domain identifier.
     * @param values {@link org.constellation.dto.ParameterValues} containing file path & data type
     * @return a {@link javax.ws.rs.core.Response}
     */
    @POST
    @Path("import/full")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response proceedToImport(final @PathParam("domainId") int domainId,
                                    final ParameterValues values) {
        String filePath = values.getValues().get("dataPath");
        final String metadataFilePath = values.getValues().get("metadataFilePath");
        final String dataType = values.getValues().get("dataType");
        String dataName= values.getValues().get("dataName");
        final String fileExtension = values.getValues().get("extension");
        final String fsServer = values.getValues().get("fsServer");
        final ImportedData importedDataReport = new ImportedData();
        try{
            final File dataIntegratedDirectory = ConfigDirectory.getDataIntegratedDirectory();
            final File uploadFolder = ConfigDirectory.getDataUploads().toFile();

            if (metadataFilePath != null) {
                if (metadataFilePath.startsWith(uploadFolder.getAbsolutePath())) {
                    final File destMd = new File(dataIntegratedDirectory.getAbsolutePath() + File.separator + new File(metadataFilePath).getName());
                    Files.move(Paths.get(metadataFilePath), Paths.get(destMd.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
                    importedDataReport.setMetadataFile(destMd.getAbsolutePath());
                } else {
                    importedDataReport.setMetadataFile(metadataFilePath);
                }
            }

            if (fsServer != null && fsServer.equalsIgnoreCase("true")) {
                importedDataReport.setDataFile(filePath);
            } else {
                if (filePath != null) {
                    filePath = renameDataFile(dataName, filePath);
                    final java.nio.file.Path intDirPath = dataIntegratedDirectory.toPath();
                    final File providerDir = intDirPath.resolve(Paths.get(dataName, dataName)).toFile();
                    if (providerDir.exists()) {
                        FileUtilities.deleteDirectory(providerDir);
                    }
                    providerDir.mkdirs();
                    if (filePath.toLowerCase().endsWith(".zip")) {
                        final File zipFile = new File(filePath);
                        FileUtilities.unzip(zipFile, providerDir, new CRC32());
                        filePath = providerDir.getAbsolutePath();
                    }
                    if (filePath.startsWith(uploadFolder.getAbsolutePath())) {
                        final File destFile = new File(providerDir.getAbsolutePath() + File.separator + new File(filePath).getName());
                        Files.move(Paths.get(filePath), Paths.get(destFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
                        importedDataReport.setDataFile(destFile.getAbsolutePath());
                    } else {
                        importedDataReport.setDataFile(filePath);
                    }
                }
            }

            final String dataFile = importedDataReport.getDataFile();
            final String importedMetaData = importedDataReport.getMetadataFile();
            final SelectedExtension selectedExtension = findDataType(dataFile,fileExtension,dataType);
            final String uploadType = selectedExtension.getDataType();
            importedDataReport.setDataType(uploadType);
            final String providerIdentifier = dataName;

            if("vector".equalsIgnoreCase(uploadType)) {
                final String subType;
                if ("shp".equalsIgnoreCase(fileExtension)) {
                    subType = "shapefile";
                } else if("mif".equalsIgnoreCase(fileExtension)){
                    subType = "MIF-MID";
                } else if("gml".equalsIgnoreCase(fileExtension)){
                    subType = "gml";
                } else {
                    subType = null;
                }
                //create provider
                final ProviderConfiguration config = new ProviderConfiguration();
                config.setType("feature-store");
                config.setSubType(subType);
                config.getParameters().put("path",dataFile);
                providerBusiness.create(providerIdentifier, config);

                //set up user metadata
                if (importedMetaData != null && !importedMetaData.isEmpty()) {
                    proceedToSaveUploadedMetadata(providerIdentifier, importedMetaData);
                }

                //verify CRS
                try {
                    final Map<GenericName, CoordinateReferenceSystem> nameCoordinateReferenceSystemHashMap = DataProviders.getInstance().getCRS(providerIdentifier);
                    for(final CoordinateReferenceSystem crs : nameCoordinateReferenceSystemHashMap.values()){
                        if (crs == null || crs instanceof ImageCRS) {
                            throw new DataStoreException("CRS is null or is instance of ImageCRS");
                        }
                    }
                    importedDataReport.setVerifyCRS("success");
                } catch (DataStoreException e) {
                    importedDataReport.setVerifyCRS("error");
                    LOGGER.log(Level.INFO, "Cannot get CRS for provider "+providerIdentifier);
                    //get a list of EPSG codes
                    importedDataReport.setCodes(getAllEpsgCodes());
                }
            }else if("raster".equalsIgnoreCase(uploadType)) {
                //create provider
                final ProviderConfiguration config = new ProviderConfiguration();
                config.setType("coverage-store");
                config.setSubType("coverage-file");
                config.getParameters().put("path",dataFile);
                final Provider finalProvider = providerBusiness.create(providerIdentifier, config);

                //set up user metadata
                if (importedMetaData != null && !importedMetaData.isEmpty()) {
                    proceedToSaveUploadedMetadata(providerIdentifier, importedMetaData);
                }

                //verify CRS
                try {
                    final Map<GenericName, CoordinateReferenceSystem> nameCoordinateReferenceSystemHashMap = DataProviders.getInstance().getCRS(providerIdentifier);
                    for(final CoordinateReferenceSystem crs : nameCoordinateReferenceSystemHashMap.values()){
                        if (crs == null || crs instanceof ImageCRS) {
                            throw new DataStoreException("CRS is null or is instance of ImageCRS");
                        }
                    }
                    importedDataReport.setVerifyCRS("success");
                } catch (DataStoreException e) {
                    importedDataReport.setVerifyCRS("error");
                    LOGGER.log(Level.INFO, "Cannot get CRS for provider "+providerIdentifier);
                    //get a list of EPSG codes
                    importedDataReport.setCodes(getAllEpsgCodes());
                }

                /**
                 * For each data created in provider, we need to pyramid conform each raster.
                 */
                final List<org.constellation.engine.register.jooq.tables.pojos.Data> dataList =  providerBusiness.getDatasFromProviderId(finalProvider.getId());
                for(final org.constellation.engine.register.jooq.tables.pojos.Data d : dataList) {
                    try {
                        final DataBrief db = providerBusiness.createPyramidConform(providerIdentifier, d.getName(),d.getNamespace(), d.getOwner());
                        // link original data with the tiled data.
                        dataBusiness.linkDataToData(d.getId(),db.getId());
                    }catch(ConstellationException ex) {
                        LOGGER.log(Level.WARNING,ex.getLocalizedMessage(),ex);
                    }
                }

            }else if("observation".equalsIgnoreCase(uploadType) && "xml".equalsIgnoreCase(fileExtension)) {
                //create provider
                final ProviderConfiguration config = new ProviderConfiguration();
                config.setType("observation-store");
                config.setSubType("observation-xml");
                config.getParameters().put("path",dataFile);
                providerBusiness.create(providerIdentifier, config);

                //set up user metadata
                if (importedMetaData != null && !importedMetaData.isEmpty()) {
                    proceedToSaveUploadedMetadata(providerIdentifier, importedMetaData);
                }
            } else if("observation".equals(uploadType)){
                //create provider
                final ProviderConfiguration config = new ProviderConfiguration();
                config.setType("observation-store");
                config.setSubType("observation-file");
                config.getParameters().put("path",dataFile);
                providerBusiness.create(providerIdentifier, config);

                //set up user metadata
                if (importedMetaData != null && !importedMetaData.isEmpty()) {
                    proceedToSaveUploadedMetadata(providerIdentifier, importedMetaData);
                }
            }else {
                //not supported
                throw new UnsupportedOperationException("The uploaded file is not recognized or not supported by the application. file:"+uploadType);
            }
            return Response.ok(importedDataReport).build();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
            return Response.status(500).entity(e.getLocalizedMessage()).build();
        }
    }

    /**
     * List all FeatureStore and CoverageStore factories and there parameters.
     *
     * @return Response {@link DataCustomConfiguration}
     */
    @GET
    @Path("listStoreConfigurations")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getAllDataStoreConfigurations(){

        final DataCustomConfiguration all = new DataCustomConfiguration();

        //list feature store factories
        for(FeatureStoreFactory factory : FeatureStoreFinder.getAllFactories(null)){
            final DataCustomConfiguration.Type type = new DataCustomConfiguration.Type();
            type.setId(factory.getIdentification().getCitation().getIdentifiers().iterator().next().getCode());
            type.setTitle(String.valueOf(factory.getDisplayName()));
            type.setCategory("feature-store");
            if(factory.getDescription()!=null) type.setDescription(String.valueOf(factory.getDescription()));
            final DataCustomConfiguration.Property prop = toDataStorePojo(factory.getParametersDescriptor());
            type.setProperty(prop);

            if(all.getTypes().isEmpty()){
                //select the first type found
                type.setSelected(true);
            }
            all.getTypes().add(type);
        }

        //list coverage store factories
        for(CoverageStoreFactory factory : CoverageStoreFinder.getAllFactories(null)){
            final DataCustomConfiguration.Type type = new DataCustomConfiguration.Type();
            type.setId(factory.getIdentification().getCitation().getIdentifiers().iterator().next().getCode());
            type.setTitle(String.valueOf(factory.getDisplayName()));
            type.setCategory("coverage-store");
            if(factory.getDescription()!=null) type.setDescription(String.valueOf(factory.getDescription()));
            final DataCustomConfiguration.Property prop = toDataStorePojo(factory.getParametersDescriptor());
            type.setProperty(prop);

            if(all.getTypes().isEmpty()){
                //select the first type found
                type.setSelected(true);
            }
            all.getTypes().add(type);
        }

        return Response.ok(all).build();
    }

    /**
     * Proceed to create new provider for given type.
     *
     * @param selected given selected type
     * @return {@code Response}
     */
    @POST
    @Path("putStoreConfigurations")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response putDataStoreConfiguration(final DataCustomConfiguration.Type selected) {
        final ImportedData importedDataReport = new ImportedData();
        try{
            //create provider
            final ProviderConfiguration provConfig = new ProviderConfiguration();
            provConfig.setType(selected.getCategory());
            provConfig.setSubType(selected.getId());

            final DataCustomConfiguration.Property props = selected.getProperty();
            props.toMap(provConfig.getParameters());

            final String provId = selected.getId()+UUID.randomUUID().toString();
            providerBusiness.create(provId, provConfig);


            return Response.ok(importedDataReport).build();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
            return Response.status(500).entity(e.getLocalizedMessage()).build();
        }
    }

    private static final Set<Class> MARSHALLABLE = new HashSet<>();
    static {
        MARSHALLABLE.add(boolean.class);
        MARSHALLABLE.add(byte.class);
        MARSHALLABLE.add(short.class);
        MARSHALLABLE.add(int.class);
        MARSHALLABLE.add(long.class);
        MARSHALLABLE.add(float.class);
        MARSHALLABLE.add(double.class);
        MARSHALLABLE.add(Boolean.class);
        MARSHALLABLE.add(Byte.class);
        MARSHALLABLE.add(Short.class);
        MARSHALLABLE.add(Integer.class);
        MARSHALLABLE.add(Long.class);
        MARSHALLABLE.add(Float.class);
        MARSHALLABLE.add(Double.class);
        MARSHALLABLE.add(String.class);
        MARSHALLABLE.add(Date.class);
    }
    private static DataCustomConfiguration.Property toDataStorePojo(GeneralParameterDescriptor desc){
        final DataCustomConfiguration.Property prop = new DataCustomConfiguration.Property();
        prop.setId(desc.getName().getCode());
        if(desc.getDescription()!=null) prop.setDescription(String.valueOf(desc.getDescription()));
        prop.setOptional(desc.getMinimumOccurs()==0);

        if(desc instanceof ParameterDescriptorGroup){
            final ParameterDescriptorGroup d = (ParameterDescriptorGroup)desc;
            for(GeneralParameterDescriptor child : d.descriptors()){
                prop.getProperties().add(toDataStorePojo(child));
            }
        }else if(desc instanceof ParameterDescriptor){
            final ParameterDescriptor d = (ParameterDescriptor)desc;
            final Object defaut = d.getDefaultValue();
            if(defaut!=null && MARSHALLABLE.contains(defaut.getClass())){
                prop.setValue(defaut);
            }
            prop.setType(d.getValueClass().getSimpleName());
        }

        return prop;
    }

    private List<String> getAllEpsgCodes() {
        final List<String> codes = new ArrayList<>();
        try{
            final CRSAuthorityFactory factory = CRS.getAuthorityFactory(Boolean.FALSE);
            final Set<String> authorityCodes = factory.getAuthorityCodes(CoordinateReferenceSystem.class);
            for (String code : authorityCodes){
                code += " - " + factory.getDescriptionText(code).toString();
                codes.add(code);
            }
        }catch(FactoryException ex){
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(),ex);
        }
        return codes;
    }

    private String renameDataFile(String dataName, String filePath) throws IOException {
        final File file = new File(filePath);
        final String parent = file.getParentFile().getCanonicalPath();
        final String fileName = file.getName();
        final String fileExt;
        if(fileName.contains(".")){
            fileExt = file.getName().substring(file.getName().lastIndexOf("."), file.getName().length());
        }else {
            fileExt = "";
        }
        final java.nio.file.Path newPath = Paths.get(parent + File.separator + dataName + fileExt);
        Files.move(Paths.get(file.getAbsolutePath()), newPath,StandardCopyOption.REPLACE_EXISTING);
        return newPath.toString();
    }

    @DELETE
    @Path("pyramid/folder/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response deletePyramidFolder(@PathParam("id") final String providerId) {
        final Map<String,Object> map = new HashMap<>();
        final DataStore ds = DataProviders.getInstance().getProvider(providerId).getMainStore();
        if (!(ds instanceof XMLCoverageStore)) {
            map.put("isPyramid",false);
            return Response.ok(map).build();
        }
        map.put("isPyramid",true);
        final XMLCoverageStore xmlCoverageStore = (XMLCoverageStore)ds;
        final ParameterValue paramVal = ParametersExt.getValue(xmlCoverageStore.getConfiguration(), XMLCoverageStoreFactory.PATH.getName().getCode());
        if (paramVal.getValue() instanceof URL) {
            try {
                final File dataFolder = new File(((URL)paramVal.getValue()).toURI());
                FileUtilities.deleteDirectory(dataFolder);
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Unable to delete folder "+ paramVal.getValue(), ex);
                return Response.status(500).entity(ex.getLocalizedMessage()).build();
            }
        }
        return Response.ok(map).build();
    }


    /*
     * Save metadata.
     * @return {@link javax.ws.rs.core.Response} with code 200.
     */
    @POST
    @Path("metadata/upload")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response saveUploadedMetadata(final ParameterValues values) {
        final String providerId         = values.getValues().get("providerId");
        final String mdPath             = values.getValues().get("mdPath");
        try{
            proceedToSaveUploadedMetadata(providerId, mdPath);
        }catch(Exception ex){
            LOGGER.log(Level.WARNING, "Error when saving uploaded metadata", ex);
            return Response.status(500).entity(ex.getLocalizedMessage()).build();
        }
        return Response.ok().type(MediaType.TEXT_PLAIN_TYPE).build();
    }

    private void proceedToSaveUploadedMetadata(final String providerId, final String mdPath) throws ConstellationException {
        if (mdPath != null && !mdPath.isEmpty()) {
            final File f = new File(mdPath);
            try {
                final DefaultMetadata metadata;
                if (metadataBusiness.isSpecialMetadataFormat(f)){
                    metadata = metadataBusiness.getMetadataFromSpecialFormat(f);
                } else {
                    metadata = (DefaultMetadata) metadataBusiness.unmarshallMetadata(f);
                }
                if (metadata == null) {
                    throw new ConstellationException("Cannot save uploaded metadata because it is not recognized as a valid file!");
                }
                // for now we assume datasetID == providerID
                datasetBusiness.updateMetadata(providerId, metadata);
            } catch (ConfigurationException ex) {
                throw new ConstellationException("Error while saving dataset metadata, " + ex.getMessage());
            }
        }
    }

    /**
     * Used to open metadata editor form.
     * the metadata.prune() should never be called in this method.
     * Returns json result of template writer to apply a given template to metadata object.
     * The path of each fields/blocks will be numerated.
     * the returned json object will be used directly in html metadata editor.
     *
     * @param values given parameters.
     * @return {@code Response}
     */
    @POST
    @Path("metadata/dataset")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getDatasetMetadata(final ParameterValues values) {
        final String identifier         = values.getValues().get("identifier");
        final String dataType           = values.getValues().get("type");
        final StringWriter buffer       = new StringWriter();
        try{
            final DefaultMetadata metadata  =  datasetBusiness.getMetadata(identifier);
            if (metadata != null) {
                //get template name
                final String templateName = metadataBusiness.getDatasetTemplate(identifier, dataType);
                final Template template   = Template.getInstance(templateName);
                template.write(metadata, buffer, false, false);
            }
        }catch(Exception ex){
            LOGGER.log(Level.WARNING, "error cannot get dataset Metadata.", ex);
        }
        return Response.ok(buffer.toString()).build();
    }

    /**
     * Used to open metadata editor form.
     * the metadata.prune() should never be called in this method.
     * Returns json result of template writer to apply a given template to metadata object.
     *
     * @param values given parameters to resolve the metadata object from database.
     * @return {@code Response}
     * @throws ConfigurationException
     */
    @POST
    @Path("metadata/data")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getDataMetadata(final ParameterValues values) throws ConfigurationException {
        final String providerId         = values.getValues().get("provider");
        final String identifier         = values.getValues().get("identifier");
        final String dataType           = values.getValues().get("type");
        final StringWriter buffer       = new StringWriter();
        try{
            final QName dataName = Util.parseQName(identifier);
            DefaultMetadata metadata = dataBusiness.loadIsoDataMetadata(providerId, dataName);
            String datasetId = null;
            if(metadata == null){
                //try to get dataset metadata.
                final Dataset dataset = dataBusiness.getDatasetForData(providerId, dataName);
                if (dataset != null) {
                    metadata = datasetBusiness.getMetadata(dataset.getIdentifier());
                    datasetId = dataset.getIdentifier();
                }
            }
            if (metadata != null) {
                //get template name
                final String templateName;
                if (datasetId != null) {
                    templateName = metadataBusiness.getDatasetTemplate(datasetId, dataType);
                } else {
                    templateName = metadataBusiness.getDataTemplate(dataName, dataType);
                }
                final Template template = Template.getInstance(templateName);
                template.write(metadata, buffer, false, false);
            }
        }catch(Exception ex){
            LOGGER.log(Level.WARNING, "error cannot get dataset Metadata.", ex);
        }
        return Response.ok(buffer.toString()).build();
    }

    /**
     * Called always with prune=true for display purposes of metadata.
     * metadata.prune() is called to clean empty nodes.
     * For data, returns applied template for metadata for read mode only like metadata viewer.
     * for reference (consult) purposes only.
     *
     * @param providerId given dataset identifier which is provider identifier.
     * @param dataId data identifier
     * @param type data type
     * @param prune flag that indicates if template result will clean empty children/block.
     * @return {@code Response}
     */
    @GET
    @Path("metadataJson/iso/{providerId}/{dataId}/{type}/{prune}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getIsoMetadataJson(final @PathParam("providerId") String providerId,
                                       final @PathParam("dataId") String dataId,
                                       final @PathParam("type") String type,
                                       final @PathParam("prune") boolean prune) {
        final StringWriter buffer = new StringWriter();
        try{
            String dataIdStr = dataId;
            if(dataIdStr != null) {
                dataIdStr = dataIdStr.replaceAll(" ","%20");
            }
            final QName dataName     = Util.parseQName(dataIdStr);
            DefaultMetadata metadata = dataBusiness.loadIsoDataMetadata(providerId, dataName);
            String datasetId         = null;
            if (metadata == null) {
                //try to get dataset metadata.
                final Dataset dataset = dataBusiness.getDatasetForData(providerId, dataName);
                if (dataset != null) {
                    metadata = datasetBusiness.getMetadata(dataset.getIdentifier());
                    datasetId = dataset.getIdentifier();
                }
            }
            if (metadata != null) {
                metadata.prune();
                //get template name
                final String templateName;
                if (datasetId != null) {
                    templateName = metadataBusiness.getDatasetTemplate(datasetId, type);
                } else {
                    templateName = metadataBusiness.getDataTemplate(dataName, type);
                }
                final Template template = Template.getInstance(templateName);
                template.write(metadata, buffer, prune, false);
            }
        }catch(Exception ex){
            LOGGER.log(Level.WARNING, "error while writing metadata json.", ex);
        }
        return Response.ok(buffer.toString()).build();
    }

    /**
     * Called always with prune=true for display purposes of metadata.
     * metadata.prune() is called to clean empty nodes.
     * for Dataset, returns applied template for metadata for read mode only like metadata viewer.
     * for reference (consult) purposes only.
     *
     * @param datasetIdentifier given dataset identifier.
     * @param type data type
     * @param prune flag that indicates if template result will clean empty children/block.
     * @return {@code Response}
     */
    @GET
    @Path("metadataJson/dataset/iso/{datasetIdentifier}/{type}/{prune}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getIsoMetadataJsonDS(final @PathParam("datasetIdentifier") String datasetIdentifier,
                                         final @PathParam("type") String type,
                                         final @PathParam("prune") boolean prune) {
        final StringWriter buffer = new StringWriter();
        try{
            final DefaultMetadata metadata = datasetBusiness.getMetadata(datasetIdentifier);
            if (metadata != null) {
                metadata.prune();
                //get template name
                final String templateName = metadataBusiness.getDatasetTemplate(datasetIdentifier, type);
                final Template template   = Template.getInstance(templateName);
                template.write(metadata, buffer, false, prune);
            }
        }catch(Exception ex){
            LOGGER.log(Level.WARNING, "error while writing metadata json.", ex);
        }
        return Response.ok(buffer.toString()).build();
    }

    /**
     * Proceed to merge saved metadata with given values from metadata editor.
     *
     * @param provider the data provider identifier.
     * @param identifier the resource identifier.
     * @param type the data type.
     * @param metadataValues the values of metadata editor.
     * @return {@code Response}
     */
    @POST
    @Path("metadata/merge/{provider}/{identifier}/{type}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response mergeMetadata(@PathParam("provider") final String provider,
                                  @PathParam("identifier") final String identifier,
                                  @PathParam("type") final String type,
                                  final RootObj metadataValues) {
        try {
            // Get previously saved metadata
            boolean dataset = false;
            final QName dataName = Util.parseQName(identifier);
            final Dataset ds = dataBusiness.getDatasetForData(provider, dataName);
            String datasetId         = null;
            DefaultMetadata metadata = dataBusiness.loadIsoDataMetadata(provider, dataName);
            if(metadata == null){
                //try to get dataset metadata.
                metadata = datasetBusiness.getMetadata(ds.getIdentifier());
                dataset = true;
                datasetId = ds.getIdentifier();
            }
            if(metadata != null) {
                //get template name
                final String templateName;
                if (datasetId != null) {
                    templateName = metadataBusiness.getDatasetTemplate(datasetId, type);
                } else {
                    templateName = metadataBusiness.getDataTemplate(dataName, type);
                }
                final Template template = Template.getInstance(templateName);

                try{
                    template.read(metadataValues,metadata,false);
                }catch(IOException ex){
                    LOGGER.log(Level.WARNING, "error while saving metadata.", ex);
                    return Response.status(500).entity(ex.getLocalizedMessage()).build();
                }

                //update dateStamp for metadata
                metadata.setDateStamp(new Date());

                //Save metadata
                if (dataset) {
                    datasetBusiness.updateMetadata(ds.getIdentifier(), metadata);
                } else {
                    dataBusiness.updateMetadata(provider, dataName, metadata);
                }
            }
        } catch (ConfigurationException ex) {
            LOGGER.warning("Error while saving dataset metadata");
            throw new ConstellationException(ex);
        }
        return Response.ok().type(MediaType.TEXT_PLAIN_TYPE).build();
    }

    @POST
    @Path("metadata/dataset/merge/{identifier}/{type}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response mergeMetadataDS(@PathParam("identifier") final String identifier,
                                    @PathParam("type") final String type,
                                    final RootObj metadataValues) {
        try {
            // Get previously saved metadata
            final DefaultMetadata metadata = datasetBusiness.getMetadata(identifier);
            if(metadata != null) {
                //get template name
                final String templateName = metadataBusiness.getDatasetTemplate(identifier, type);
                final Template template = Template.getInstance(templateName);

                try{
                    template.read(metadataValues,metadata,false);
                }catch(IOException ex){
                    LOGGER.log(Level.WARNING, "error while saving metadata.", ex);
                    return Response.status(500).entity(ex.getLocalizedMessage()).build();
                }

                //update dateStamp for metadata
                metadata.setDateStamp(new Date());

                //Save metadata
                datasetBusiness.updateMetadata(identifier, metadata);

            }
        } catch (ConfigurationException ex) {
            LOGGER.warning("Error while saving dataset metadata");
            throw new ConstellationException(ex);
        }
        return Response.ok().type(MediaType.TEXT_PLAIN_TYPE).build();
    }

    /**
     * Proceed to search data for query.
     * @param values given parameters
     * @return {code Response} that contains all data that matches the lucene query.
     */
    @POST
    @Path("metadata/find")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findMetadata(@PathParam("domainId") final int domainId, final ParameterValues values) {
        final String search = values.getValues().get("search");
        List<DataBrief> briefs = new ArrayList<>();
        final List<org.constellation.engine.register.jooq.tables.pojos.Data> datas;
        try {
            datas = dataBusiness.searchOnMetadata(search);
            if(datas != null && !datas.isEmpty()){
                for (org.constellation.engine.register.jooq.tables.pojos.Data data : datas) {
                    final QName name = new QName(data.getNamespace(), data.getName());
                    final DataBrief db = dataBusiness.getDataBrief(name, data.getProvider());
                    briefs.add(db);
                }
            }else {
                //@TODO remove this else when we will add data metadata in cstl db after SIS implementation.
                final List<DataSetBrief> datasetBriefs = new ArrayList<>();
                final List<Dataset> datasetList = datasetBusiness.searchOnMetadata(search);
                for (final Dataset ds : datasetList) {
                    final DataSetBrief dsb = buildDatsetBrief(ds,null);
                    datasetBriefs.add(dsb);
                }
                for(final DataSetBrief dsb : datasetBriefs){
                    briefs.addAll(dsb.getChildren());
                }
            }
            return Response.ok(briefs).build();
        } catch (ConstellationException | IOException ex) {
            return Response.ok("Failed to parse query : "+ex.getMessage()).status(500).build();
        }
    }

    /**
     * Build {@link DataSetBrief} instance from {@link Dataset}
     * @param dataset given dataset object.
     * @return {@link DataSetBrief} built from the given dataset.
     */
    private DataSetBrief buildDatsetBrief(final Dataset dataset, List<DataBrief> children){
        final Integer dataSetId = dataset.getId();
        if (children == null) {
            children = dataBusiness.getDataBriefsFromDatasetId(dataSetId);
        }
        final DataSetBrief dsb = datasetBusiness.getDatasetBrief(dataSetId, children);
        return dsb;
    }

    /**
     * Init metadata for imported data.
     * It is the first save called after import phase.
     * if user send its own metadata he can decide if its
     * metadata will be merged with reader metadata by passing parameter flag mergeWithUploadedMD.
     *
     * @param values params that contains providerId, dataType and flag
     * @return {@link javax.ws.rs.core.Response}
     * @throws ConfigurationException
     */
    @POST
    @Path("metadata")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response initMetadataFromReader(final ParameterValues values) throws ConfigurationException {
        final String providerId          = values.getValues().get("providerId");
        final String dataType            = values.getValues().get("dataType");
        final String mergeWithUploadedMD = values.getValues().get("mergeWithUploadedMD");
        DefaultMetadata uploadedMetadata;
        try{
            uploadedMetadata = datasetBusiness.getMetadata(providerId);
        }catch(Exception ex){
            uploadedMetadata = null;
        }
        if(uploadedMetadata!=null && (mergeWithUploadedMD == null || mergeWithUploadedMD.equalsIgnoreCase("false"))){
            //skip if there is uploaded metadata and user want to keep this original metadata.
            return Response.ok().type(MediaType.TEXT_PLAIN_TYPE).build();
        }
        datasetBusiness.saveMetadata(providerId, dataType);
        return Response.ok().type(MediaType.TEXT_PLAIN_TYPE).build();
    }

    /**
     * Generates a pyramid on a data in the given provider, create and return this new provider.
     *
     * N.B : Generated pyramid contains coverage real values, it's not styled for rendering.
     *
     * @param providerId Provider identifier of the data to tile.
     * @param dataName the given data name.
     * @return
     */
    @POST
    @Path("pyramid/createconform/{providerId}/{dataName}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response createTiledProviderConform(@PathParam("providerId") final String providerId,
                                               @PathParam("dataName") final String dataName,
                                               @Context HttpServletRequest req) {

        final Optional<CstlUser> cstlUser = userRepository.findOne(req.getUserPrincipal().getName());
        if (!cstlUser.isPresent()) {
            return Response.status(500).entity("operation not allowed without login").build();
        }
        try {
            final DataBrief db = providerBusiness.createPyramidConform(providerId, dataName, null, cstlUser.get().getId());
            final ProviderData ref = new ProviderData(db.getProvider(), dataName);
            return Response.ok(ref).build();
        }catch(ConstellationException ex) {
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            return Response.status(500).entity(ex.getLocalizedMessage()).build();
        }
    }

    @GET
    @Path("pyramid/mapcontext/{contextId}/{crs}/{layerName}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response pyramidMapContext(@PathParam("crs") final String crs,
                                      @PathParam("layerName") final String layerName,
                                      @PathParam("contextId") final Integer contextId,
                                      @Context HttpServletRequest req) throws ConstellationException{
        final Optional<CstlUser> cstlUser = userRepository.findOne(req.getUserPrincipal().getName());
        if (!cstlUser.isPresent()) {
            throw new ConstellationException("operation not allowed without login");
        }

        if(crs == null || crs.isEmpty()){
            return Response.status(500).entity("CRS code is null!").build();
        }

        if(layerName == null || layerName.isEmpty()){
            return Response.status(500).entity("layerName is null!").build();
        }

        if(contextId == null){
            return Response.status(500).entity("contextId is null!").build();
        }

        final MapContextLayersDTO mc = mapContextBusiness.findMapContextLayers(contextId);

        if(mc == null || mc.getLayers() == null || mc.getLayers().isEmpty()) {
            return Response.ok("The given mapcontext to pyramid is empty.").build();
        }

        final CoordinateReferenceSystem crsOutput;
        try {
            crsOutput = CRS.decode(crs,true);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Invalid CRS code : "+crs, ex);
            return Response.status(500).entity("Invalid CRS code : " + crs).build();
        }

        final CoordinateReferenceSystem crsObj;
        final String mapCtxtCrs = mc.getCrs();
        if(mapCtxtCrs != null) {
            try {
                crsObj = CRS.decode(mapCtxtCrs,true);
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Invalid mapcontext CRS code : "+mapCtxtCrs, ex);
                return Response.status(500).entity("Invalid mapcontext CRS code : " + mapCtxtCrs).build();
            }
        } else {
            return Response.status(500).entity("mapcontext CRS code is null!").build();
        }
        final GeneralEnvelope env = new GeneralEnvelope(crsObj);
        env.setRange(0,mc.getWest(),mc.getEast());
        env.setRange(1,mc.getSouth(),mc.getNorth());
        GeneralEnvelope globalEnv;
        try {
            globalEnv = new GeneralEnvelope(CRS.transform(env,crsOutput));
        }catch (TransformException ex){
            globalEnv=null;
        }

        if(globalEnv == null || globalEnv.isEmpty()){
            globalEnv = new GeneralEnvelope(CRS.getEnvelope(crsOutput));
        }

        if(containsInfinity(globalEnv)){
            globalEnv.intersect(CRS.getEnvelope(crsOutput));
        }

        final double geospanX = globalEnv.getSpan(0);
        final int tileSize = 256;
        final double[] scales = new double[8];
        scales[0] = geospanX / tileSize;
        for(int i=1;i<scales.length;i++){
            scales[i] = scales[i-1] / 2.0;
        }

        final String tileFormat = "PNG";
        final MapContext context = MapBuilder.createContext();

        for(final MapContextStyledLayerDTO layer : mc.getLayers()){
            final String providerIdentifier = layer.getProvider();
            final String dataName = layer.getName();
            if(providerIdentifier == null){
                URL serviceUrl;
                try{
                    serviceUrl = new URL(layer.getExternalServiceUrl());
                }catch(Exception ex){
                    LOGGER.log(Level.WARNING,"An external wms layer in mapcontext have invalid service url! "+layer.getName());
                    continue;
                }
                //it is a wms layer
                final String serviceVersion = layer.getExternalServiceVersion() != null ? layer.getExternalServiceVersion() : "1.3.0";
                final WebMapClient wmsServer = new WebMapClient(serviceUrl, WMSVersion.getVersion(serviceVersion));
                final WMSMapLayer wmsLayer = new WMSMapLayer(wmsServer, dataName);
                context.items().add(wmsLayer);
                continue;
            }
            //get data
            final DataProvider inProvider = DataProviders.getInstance().getProvider(providerIdentifier);
            if(inProvider==null){
                LOGGER.log(Level.WARNING,"Provider "+providerIdentifier+" does not exist");
                continue;
            }
            final Data inData = inProvider.get(NamesExt.create(dataName));
            if(inData==null){
                LOGGER.log(Level.WARNING,"Data "+dataName+" does not exist in provider "+providerIdentifier);
                continue;
            }

            MutableStyle style = null;
            try {
                final List<StyleBrief> styles = layer.getTargetStyle();
                if(styles != null && ! styles.isEmpty()){
                    final String styleName = styles.get(0).getName();
                    style = styleBusiness.getStyle("sld",styleName);
                }
            }catch(Exception ex){
                LOGGER.log(Level.WARNING,ex.getLocalizedMessage(),ex);
            }

            try {
                //if style is null, a default style will be used in maplayer.
                context.items().add(inData.getMapLayer(style, null));
            } catch (PortrayalException ex) {
                LOGGER.log(Level.WARNING, "Failed to create map context item for data " + ex.getMessage(), ex);
            }
        }

        final String uuid = UUID.randomUUID().toString();
        final String providerId = uuid;
        final String dataName = layerName;

        //create the output folder for pyramid
        PyramidalCoverageReference outRef;
        String pyramidProviderId = RENDERED_PREFIX + uuid;
        final File providerDirectory = ConfigDirectory.getDataIntegratedDirectory(providerId);
        final File pyramidDirectory = new File(providerDirectory, pyramidProviderId);
        pyramidDirectory.mkdirs();

        //create the output provider
        final DataProvider outProvider;
        try {
            final DataProviderFactory factory = DataProviders.getInstance().getFactory("coverage-store");
            final ParameterValueGroup pparams = factory.getProviderDescriptor().createValue();
            ParametersExt.getOrCreateValue(pparams, ProviderParameters.SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue(pyramidProviderId);
            ParametersExt.getOrCreateValue(pparams, ProviderParameters.SOURCE_TYPE_DESCRIPTOR.getName().getCode()).setValue("coverage-store");
            final ParameterValueGroup choiceparams = ParametersExt.getOrCreateGroup(pparams, factory.getStoreDescriptor().getName().getCode());
            final ParameterValueGroup xmlpyramidparams = ParametersExt.getOrCreateGroup(choiceparams, XMLCoverageStoreFactory.PARAMETERS_DESCRIPTOR.getName().getCode());
            ParametersExt.getOrCreateValue(xmlpyramidparams, XMLCoverageStoreFactory.PATH.getName().getCode()).setValue(pyramidDirectory.toURI().toURL());
            ParametersExt.getOrCreateValue(xmlpyramidparams, XMLCoverageStoreFactory.NAMESPACE.getName().getCode()).setValue("no namespace");
            outProvider = DataProviders.getInstance().createProvider(pyramidProviderId, factory, pparams,null,false);
        } catch (Exception ex) {
            Providers.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            return Response.status(500).entity("Failed to create pyramid provider " + ex.getMessage()).build();
        }

        try {
            //create the output pyramid coverage reference
            CoverageStore pyramidStore = (CoverageStore) outProvider.getMainStore();
            outRef = (XMLCoverageReference) pyramidStore.create(NamesExt.create(dataName));
            outRef.setPackMode(ViewType.RENDERED);
            ((XMLCoverageReference) outRef).setPreferredFormat(tileFormat);
            //this produces an update event which will create the DataRecord
            outProvider.reload();

            pyramidStore = (CoverageStore) outProvider.getMainStore();
            outRef = (XMLCoverageReference) pyramidStore.getCoverageReference(outRef.getName());

            //set data as RENDERED
            final QName outDataQName = new QName(NamesExt.getNamespace(outRef.getName()), outRef.getName().tip().toString());
            dataBusiness.updateDataRendered(outDataQName, outProvider.getId(), true);

            //set hidden value to true for the pyramid styled map
            final DataBrief pyramidDataBrief = dataBusiness.getDataBrief(outDataQName, outProvider.getId());
            dataBusiness.updateHidden(pyramidDataBrief.getId(),true);

        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            return Response.status(500).entity("Failed to create pyramid layer " + ex.getMessage()).build();
        }

        //prepare the pyramid and mosaics
        final Dimension tileDim = new Dimension(tileSize, tileSize);
        try {
            CoverageUtilities.getOrCreatePyramid(outRef, globalEnv, tileDim, scales);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            return Response.status(500).entity("Failed to initialize output pyramid. Cause : " + ex.getMessage()).build();
        }

        final ProcessDescriptor desc;
        try {
            desc = ProcessFinder.getProcessDescriptor("engine2d", "mapcontextpyramid");

            final ParameterValueGroup input = desc.getInputDescriptor().createValue();
            input.parameter("context").setValue(context);
            input.parameter("extent").setValue(globalEnv);
            input.parameter("tilesize").setValue(tileDim);
            input.parameter("scales").setValue(scales);
            input.parameter("container").setValue(outRef);
            final org.geotoolkit.process.Process p = desc.createProcess(input);

            //add task in scheduler
            TaskParameter taskParameter = new TaskParameter();
            taskParameter.setProcessAuthority(desc.getIdentifier().getAuthority().toString());
            taskParameter.setProcessCode(desc.getIdentifier().getCode());
            taskParameter.setDate(System.currentTimeMillis());
            taskParameter.setInputs(ParamUtilities.writeParameter(input));
            taskParameter.setOwner(cstlUser.get().getId());
            taskParameter.setName("Styled pyramid " + crs + " for " + providerId + ":" + dataName+" | "+System.currentTimeMillis());
            taskParameter.setType("INTERNAL");
            taskParameter = processBusiness.addTaskParameter(taskParameter);
            //add task in scheduler
            processBusiness.runProcess("Create pyramid " + crs + " for " + providerId + ":" + dataName, p, taskParameter.getId(), cstlUser.get().getId());

        } catch (NoSuchIdentifierException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            return Response.status(500).entity("Process engine2d.mapcontextpyramid not found " + ex.getMessage()).build();
        } catch (ConstellationException e) {
            LOGGER.log(Level.WARNING, "Unable to run pyramid process on scheduler");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            return Response.status(500).entity("Data cannot be tiled. " + ex.getMessage()).build();
        }
        final ProviderData ref = new ProviderData(pyramidProviderId, dataName);
        return Response.ok(ref).build();
    }

    private static boolean containsInfinity(final GeneralEnvelope env){
        return Double.isInfinite(env.getLower(0)) || Double.isInfinite(env.getUpper(0)) ||
                Double.isInfinite(env.getLower(1)) || Double.isInfinite(env.getUpper(1));
    }

    /**
     * Generates a pyramid on a list of data and create and return this new provider.
     * Creates btw a mapcontext that contains internal data.
     * N.B : It creates a styled pyramid, which can be used for display purposes, but not for analysis.
     */
    @POST
    @Path("pyramid/create/{crs}/{layerName}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response pyramidData(@PathParam("crs") final String crs,
                                @PathParam("layerName") final String layerName,
                                final List<DataBrief> briefs,
                                @Context HttpServletRequest req) throws ConstellationException{

        final Optional<CstlUser> cstlUser = userRepository.findOne(req.getUserPrincipal().getName());
        if (!cstlUser.isPresent()) {
            throw new ConstellationException("operation not allowed without login");
        }
        final CoordinateReferenceSystem coordsys;
        if(crs != null) {
            try {
                coordsys = CRS.decode(crs,true);
            } catch (FactoryException ex) {
                LOGGER.log(Level.WARNING, "Invalid CRS code : "+crs, ex);
                return Response.status(500).entity("Invalid CRS code : " + crs).build();
            }
        } else {
            coordsys = null;
        }

        if(briefs != null && !briefs.isEmpty()){
            /**
             * 1) calculate best scales array.
             *    loop on each data and determine the largest scales
             *    that wrap all data.
             */
            final List<Double> mergedScales = new LinkedList<>();
            for(final DataBrief db : briefs){
                final String providerIdentifier = db.getProvider();
                final String dataName = db.getName();
                final Double[] scales;
                try {
                    scales = computeScales(providerIdentifier, dataName,crs);
                }catch(Exception ex) {
                    LOGGER.log(Level.WARNING, ex.getMessage(), ex);
                    continue;
                }
                if(mergedScales.isEmpty()){
                    mergedScales.addAll(Arrays.asList(scales));
                }else {
                    Double max = Math.max(mergedScales.get(0),scales[0]);
                    Double min = Math.min(mergedScales.get(mergedScales.size()-1),scales[scales.length-1]);
                    final List<Double> scalesList = new ArrayList<>();
                    Double scale = max;
                    while (true) {
                        if (scale <= min) {
                            scale = min;
                        }
                        scalesList.add(scale);
                        if (scale <= min) {
                            break;
                        }
                        scale = scale / 2;
                    }
                    mergedScales.clear();
                    mergedScales.addAll(scalesList);
                }
            }

            /**
             * 2) creates the styled pyramid that contains all selected layers
             *    we need to loop on data and creates a mapcontext
             *    to send to pyramid process
             */
            double[] scales = new double[mergedScales.size()];
            for (int i = 0; i < scales.length; i++) {
                scales[i] = mergedScales.get(i);
            }
            final String tileFormat = "PNG";
            String firstDataProv=null;
            String firstDataName=null;
            GeneralEnvelope globalEnv = null;
            final MapContext context = MapBuilder.createContext();
            for(final DataBrief db : briefs){
                final String providerIdentifier = db.getProvider();
                final String dataName = db.getName();
                if(firstDataProv==null){
                    firstDataProv = providerIdentifier;
                    firstDataName = dataName;
                }
                //get data
                final DataProvider inProvider = DataProviders.getInstance().getProvider(providerIdentifier);
                if(inProvider==null){
                    LOGGER.log(Level.WARNING,"Provider "+providerIdentifier+" does not exist");
                    continue;
                }
                final Data inData = inProvider.get(NamesExt.create(dataName));
                if(inData==null){
                    LOGGER.log(Level.WARNING,"Data "+dataName+" does not exist in provider "+providerIdentifier);
                    continue;
                }

                Envelope dataEnv;
                try {
                    dataEnv = inData.getEnvelope();
                } catch (DataStoreException ex) {
                    LOGGER.log(Level.WARNING,"Failed to extract envelope for data "+dataName);
                    continue;
                }
                MutableStyle style = null;
                try {
                    final List<StyleBrief> styles = db.getTargetStyle();
                    if(styles != null && !styles.isEmpty()){
                        final String styleName = styles.get(0).getName();
                        style = styleBusiness.getStyle("sld",styleName);
                    }
                }catch(Exception ex){
                    LOGGER.log(Level.WARNING,ex.getLocalizedMessage(),ex);
                }

                try {
                    //if style is null, a default style will be used in maplayer.
                    context.items().add(inData.getMapLayer(style, null));
                } catch (PortrayalException ex) {
                    LOGGER.log(Level.WARNING, "Failed to create map context layer for data " + ex.getMessage(), ex);
                    continue;
                }
                if(coordsys != null) {
                    try {
                        //reproject data envelope
                        dataEnv = CRS.transform(dataEnv, coordsys);
                    } catch (TransformException ex) {
                        LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
                        return Response.status(500).entity("Could not transform data envelope to crs "+crs).build();
                    }
                }
                if(globalEnv == null) {
                    globalEnv = (GeneralEnvelope) dataEnv;
                }else {
                    globalEnv.add(dataEnv);
                }
            }

            globalEnv.intersect(CRS.getEnvelope(coordsys));

            final String uuid = UUID.randomUUID().toString();
            final String providerId = briefs.size()==1?firstDataProv:uuid;
            final String dataName = layerName;

            //create the output folder for pyramid
            PyramidalCoverageReference outRef;
            String pyramidProviderId = RENDERED_PREFIX + uuid;
            final File providerDirectory = ConfigDirectory.getDataIntegratedDirectory(providerId);
            final File pyramidDirectory = new File(providerDirectory, pyramidProviderId);
            pyramidDirectory.mkdirs();

            //create the output provider
            final DataProvider outProvider;
            try {
                final DataProviderFactory factory = DataProviders.getInstance().getFactory("coverage-store");
                final ParameterValueGroup pparams = factory.getProviderDescriptor().createValue();
                ParametersExt.getOrCreateValue(pparams, ProviderParameters.SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue(pyramidProviderId);
                ParametersExt.getOrCreateValue(pparams, ProviderParameters.SOURCE_TYPE_DESCRIPTOR.getName().getCode()).setValue("coverage-store");
                final ParameterValueGroup choiceparams = ParametersExt.getOrCreateGroup(pparams, factory.getStoreDescriptor().getName().getCode());
                final ParameterValueGroup xmlpyramidparams = ParametersExt.getOrCreateGroup(choiceparams, XMLCoverageStoreFactory.PARAMETERS_DESCRIPTOR.getName().getCode());
                ParametersExt.getOrCreateValue(xmlpyramidparams, XMLCoverageStoreFactory.PATH.getName().getCode()).setValue(pyramidDirectory.toURI().toURL());
                ParametersExt.getOrCreateValue(xmlpyramidparams, XMLCoverageStoreFactory.NAMESPACE.getName().getCode()).setValue("no namespace");
                outProvider = DataProviders.getInstance().createProvider(pyramidProviderId, factory, pparams,null,false);
                // Update the parent attribute of the created provider
                if(briefs.size()==1){
                    providerBusiness.updateParent(outProvider.getId(), providerId);
                }
            } catch (Exception ex) {
                Providers.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
                return Response.status(500).entity("Failed to create pyramid provider " + ex.getMessage()).build();
            }

            try {
                //create the output pyramid coverage reference
                CoverageStore pyramidStore = (CoverageStore) outProvider.getMainStore();
                outRef = (XMLCoverageReference) pyramidStore.create(NamesExt.create(dataName));
                outRef.setPackMode(ViewType.RENDERED);
                ((XMLCoverageReference) outRef).setPreferredFormat(tileFormat);
                //this produces an update event which will create the DataRecord
                outProvider.reload();

                pyramidStore = (CoverageStore) outProvider.getMainStore();
                outRef = (XMLCoverageReference) pyramidStore.getCoverageReference(outRef.getName());

                //set data as RENDERED
                final QName outDataQName = new QName(NamesExt.getNamespace(outRef.getName()), outRef.getName().tip().toString());
                dataBusiness.updateDataRendered(outDataQName, outProvider.getId(), true);

                //set hidden value to true for the pyramid styled map
                final DataBrief pyramidDataBrief = dataBusiness.getDataBrief(outDataQName, outProvider.getId());
                dataBusiness.updateHidden(pyramidDataBrief.getId(),true);

                //link pyramid data to original data
                for(final DataBrief db : briefs){
                    dataBusiness.linkDataToData(db.getId(),pyramidDataBrief.getId());
                }
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, ex.getMessage(), ex);
                return Response.status(500).entity("Failed to create pyramid layer " + ex.getMessage()).build();
            }

            try {
                //insert a mapcontext for this wmts pyramid of data
                final MapContextLayersDTO mapContext = new MapContextLayersDTO();
                mapContext.setOwner(cstlUser.get().getId());
                mapContext.setCrs(crs);
                mapContext.setKeywords("");
                mapContext.setWest(globalEnv.getLower(0));
                mapContext.setSouth(globalEnv.getLower(1));
                mapContext.setEast(globalEnv.getUpper(0));
                mapContext.setNorth(globalEnv.getUpper(1));
                mapContext.setName(dataName+" (wmts context)");
                final Mapcontext mapContextCreated = mapContextBusiness.create(mapContext);
                final List<MapcontextStyledLayer> mapcontextlayers = new ArrayList<>();
                final Integer mapcontextId = mapContextCreated.getId();
                for(final DataBrief db : briefs) {
                    final MapcontextStyledLayer mcStyledLayer = new MapcontextStyledLayer();
                    mcStyledLayer.setDataId(db.getId());
                    final List<StyleBrief> styles = db.getTargetStyle();
                    if(styles != null && !styles.isEmpty()){
                        final String styleName = styles.get(0).getName();
                        mcStyledLayer.setExternalStyle(styleName);
                        mcStyledLayer.setStyleId(styles.get(0).getId());
                    }
                    mcStyledLayer.setIswms(false);
                    mcStyledLayer.setLayerId(null);
                    mcStyledLayer.setLayerOpacity(100);
                    mcStyledLayer.setLayerOrder(briefs.indexOf(db));
                    mcStyledLayer.setLayerVisible(true);
                    mcStyledLayer.setMapcontextId(mapcontextId);
                    mapcontextlayers.add(mcStyledLayer);
                }
                mapContextBusiness.setMapItems(mapcontextId, mapcontextlayers);
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Can not create mapcontext for WMTS layer", ex);
            }

            //prepare the pyramid and mosaics
            final int tileSize = 256;
            final Dimension tileDim = new Dimension(tileSize, tileSize);
            try {
                CoverageUtilities.getOrCreatePyramid(outRef, globalEnv, tileDim, scales);
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, ex.getMessage(), ex);
                return Response.status(500).entity("Failed to initialize output pyramid. Cause : " + ex.getMessage()).build();
            }

            final ProcessDescriptor desc;
            try {
                desc = ProcessFinder.getProcessDescriptor("engine2d", "mapcontextpyramid");

                final ParameterValueGroup input = desc.getInputDescriptor().createValue();
                input.parameter("context").setValue(context);
                input.parameter("extent").setValue(globalEnv);
                input.parameter("tilesize").setValue(tileDim);
                input.parameter("scales").setValue(scales);
                input.parameter("container").setValue(outRef);
                final org.geotoolkit.process.Process p = desc.createProcess(input);

                //add task in scheduler
                TaskParameter taskParameter = new TaskParameter();
                taskParameter.setProcessAuthority(desc.getIdentifier().getAuthority().toString());
                taskParameter.setProcessCode(desc.getIdentifier().getCode());
                taskParameter.setDate(System.currentTimeMillis());
                taskParameter.setInputs(ParamUtilities.writeParameter(input));
                taskParameter.setOwner(cstlUser.get().getId());
                taskParameter.setName("Styled pyramid " + crs + " for " + providerId + ":" + dataName+" | "+System.currentTimeMillis());
                taskParameter.setType("INTERNAL");
                taskParameter = processBusiness.addTaskParameter(taskParameter);
                //add task in scheduler
                processBusiness.runProcess("Create pyramid " + crs + " for " + providerId + ":" + dataName, p, taskParameter.getId(), cstlUser.get().getId());

            } catch (NoSuchIdentifierException ex) {
                LOGGER.log(Level.WARNING, ex.getMessage(), ex);
                return Response.status(500).entity("Process engine2d.mapcontextpyramid not found " + ex.getMessage()).build();
            } catch (ConstellationException e) {
                LOGGER.log(Level.WARNING, "Unable to run pyramid process on scheduler");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, ex.getMessage(), ex);
                return Response.status(500).entity("Data cannot be tiled. " + ex.getMessage()).build();
            }
            final ProviderData ref = new ProviderData(pyramidProviderId, dataName);
            return Response.ok(ref).build();
        }
        return Response.ok("The given list of data to pyramid is empty.").build();
    }

    /**
     * Returns scales array for data. (for wmts scales)
     * @throws ConstellationException
     */
    private Double[] computeScales(final String providerId, final String dataId, final String crs) throws ConstellationException {
        //get data
        final DataProvider inProvider = DataProviders.getInstance().getProvider(providerId);
        if(inProvider==null){
            throw new ConstellationException("Provider "+providerId+" does not exist");
        }
        final Data inData = inProvider.get(NamesExt.create(dataId));
        if(inData==null){
            throw new ConstellationException("Data "+dataId+" does not exist in provider "+providerId);
        }
        Envelope dataEnv;
        try {
            //use data crs
            dataEnv = inData.getEnvelope();
        } catch (DataStoreException ex) {
            throw new ConstellationException("Failed to extract envelope for data "+dataId, ex);
        }
        final Object origin = inData.getOrigin();
        final Double[] scales;
        Envelope env;
        if(crs == null || crs.isEmpty()){
            env = dataEnv;
        }else {
            try{
                env = Envelopes.transform(dataEnv, CRS.decode(crs));
            }catch(Exception ex) {
                env = dataEnv;
            }
        }
        if(origin instanceof CoverageReference){
            //calculate pyramid scale levels
            final CoverageReference inRef = (CoverageReference) origin;
            final GeneralGridGeometry gg;
            try{
                final GridCoverageReader reader = inRef.acquireReader();
                gg = reader.getGridGeometry(inRef.getImageIndex());
                inRef.recycle(reader);
            } catch(CoverageStoreException ex) {
                throw new ConstellationException("Failed to extract grid geometry for data "+dataId+". ",ex);
            }
            final double geospanX = env.getSpan(0);
            final double baseScale = geospanX / gg.getExtent().getSpan(0);
            final int tileSize = 256;
            double scale = geospanX / tileSize;
            final GeneralDirectPosition ul = new GeneralDirectPosition(env.getCoordinateReferenceSystem());
            ul.setOrdinate(0, env.getMinimum(0));
            ul.setOrdinate(1, env.getMaximum(1));
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
            scales = new Double[scalesList.size()];
            for(int i=0;i<scales.length;i++){
                scales[i] = scalesList.get(i);
            }
        }else{
            //featurecollection or anything else, scales can not be defined accurately.
            //vectors have virtually an unlimited resolution
            //we build scales, to obtain 8 levels, this should be enough for a default case
            final double geospanX = env.getSpan(0);
            final int tileSize = 256;
            scales = new Double[8];
            scales[0] = geospanX / tileSize;
            for(int i=1;i<scales.length;i++){
                scales[i] = scales[i-1] / 2.0;
            }
        }
        return scales;
    }

    @GET
    @Path("pyramid/bestscales/{providerId}/{dataId}/{crs}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response findBestScales(@PathParam("providerId") final String providerId,
                                   @PathParam("dataId") final String dataId,
                                   @PathParam("crs") final String crs){
        try {
            final Double[] scales = computeScales(providerId,dataId,crs);
            final String scalesStr = StringUtilities.toCommaSeparatedValues(scales);
            return Response.ok(new StringList(Collections.singleton(scalesStr))).build();
        }catch(Exception ex) {
            LOGGER.log(Level.WARNING,ex.getLocalizedMessage(),ex);
            return Response.status(500).entity(ex.getLocalizedMessage()).build();
        }
    }

    /**
     * Send an ArrayList which contains coverage list from a file
     * @return an {@link java.util.ArrayList}
     */
    @POST
    @Path("coverage/list/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getCoverageList(final SimpleValue value) {
        final DataProvider provider = DataProviders.getInstance().getProvider(value.getValue());
        final Set<GenericName> nameSet = provider.getKeys();
        final List<String> names = new ArrayList<>();
        for (GenericName n : nameSet) {
            names.add(n.tip().toString());
        }
        Collections.sort(names);

        //Search on Metadata to found description
        final Map<String, String> coveragesDescription = new HashMap<>(0);
        for (int i=0; i<names.size(); i++) {
            coveragesDescription.put(String.valueOf(i), names.get(i));
        }

        //Send String Map via REST
        ParameterValues pv = new ParameterValues(coveragesDescription);
        return Response.ok(pv).build();
    }

    @PUT
    @Path("summary")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getDataSummary(final ParameterValues pv) {
        final String namespace = pv.get("namespace");
        final String name = pv.get("name");
        final String providerId = pv.get("providerId");
        final QName fullName = new QName(namespace, name);
        final DataBrief db = dataBusiness.getDataBrief(fullName, providerId);
        return Response.ok(db).build();
    }

    @GET
    @Path("list")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response getDataList() {
        return getDataList(null);
    }

    @GET
    @Path("list/{type}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response getDataList(@PathParam("type") String type) {
        final List<DataBrief> briefs = new ArrayList<>();
        final List<String> providerIds = providerBusiness.getProviderIds();
        for (final String providerId : providerIds) {
            final Provider provider = providerBusiness.getProvider(providerId);
            final List<org.constellation.engine.register.jooq.tables.pojos.Data> datas = providerBusiness.getDatasFromProviderId(provider.getId());
            for (final org.constellation.engine.register.jooq.tables.pojos.Data data : datas) {
                if (type != null && !type.equalsIgnoreCase(data.getType())) {
                    continue;
                }
                if (data.getIncluded() && !data.getHidden()) {
                    final QName name = new QName(data.getNamespace(), data.getName());
                    final DataBrief db = dataBusiness.getDataBrief(name, providerId);
                    briefs.add(db);
                }
            }
        }
        return Response.ok(briefs).build();
    }

    @GET
    @Path("list/provider")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response getDataListsForProviders() {
        final Map<String, List<DataBrief>> all = new HashMap<>();
        final List<String> providerIds = providerBusiness.getProviderIds();
        for (final String providerId : providerIds) {
            final List<DataBrief> briefs = new ArrayList<>();
            final Provider provider = providerBusiness.getProvider(providerId);
            final List<org.constellation.engine.register.jooq.tables.pojos.Data> datas = providerBusiness.getDatasFromProviderId(provider.getId());
            for (final org.constellation.engine.register.jooq.tables.pojos.Data data : datas) {
                if (data.getIncluded() && !data.getHidden()) {
                    final QName name = new QName(data.getNamespace(), data.getName());
                    final DataBrief db = dataBusiness.getDataBrief(name, providerId);
                    briefs.add(db);
                }
            }
            all.put(provider.getIdentifier(), briefs);
        }
        return Response.ok(all).build();
    }

    @GET
    @Path("list/provider/{providerId}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response getDataListsForProviders(@PathParam("providerId") final String providerId) {
        final Provider prov = providerBusiness.getProvider(providerId);
        final List<DataBrief> briefs = new ArrayList<>();
        final List<org.constellation.engine.register.jooq.tables.pojos.Data> datas;
        datas = providerBusiness.getDatasFromProviderId(prov.getId());
        for (final org.constellation.engine.register.jooq.tables.pojos.Data data : datas) {
            if (data.getIncluded() && !data.getHidden()) {
                final QName name = new QName(data.getNamespace(), data.getName());
                final DataBrief db = dataBusiness.getDataBrief(name, providerId);
                briefs.add(db);
            }
        }
        return Response.ok(briefs).build();
    }

    @GET
    @Path("count/all")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response getCountAll() {
        final Map<String,Integer> map = new HashMap<>();
        final int count = dataBusiness.getCountAll(false);
        map.put("count", count);
        return Response.ok(map).build();
    }

    @GET
    @Path("list/top")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response getTopDataList(@PathParam("domainId") int domainId) {
        return getTopDataList(domainId, null);
    }

    @GET
    @Path("list/top/{type}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response getTopDataList(@PathParam("domainId") int domainId, @PathParam("type") String type) {
        final List<DataBrief> briefs = new ArrayList<>();
        final List<Integer> providerIds = providerBusiness.getProviderIdsAsInt();
        for (final Integer providerId : providerIds) {
            final Provider provider = providerBusiness.getProvider(providerId);
            final String parent = provider.getParent();
            if (parent != null && !parent.isEmpty()) {
                // Remove all providers that have a parent
                continue;
            }
            final List<org.constellation.engine.register.jooq.tables.pojos.Data> datas;
            datas = providerBusiness.getDatasFromProviderId(provider.getId());
            for (final org.constellation.engine.register.jooq.tables.pojos.Data data : datas) {
                if (type != null && !data.getType().equals(type)) {
                    continue;
                }
                if (data.getIncluded() && !data.getHidden()) {
                    final QName name = new QName(data.getNamespace(), data.getName());
                    final DataBrief db = dataBusiness.getDataBrief(name, providerId);
                    briefs.add(db);
                }
            }
        }
        return Response.ok(briefs).build();
    }

    /**
     * List all Datasets without sub-datas
     * @param domainId
     * @return
     */
    @GET
    @Path("list/dataset")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response getDatasetList(@PathParam("domainId") int domainId) {
        final List<DataSetBrief> datasetBriefs = new ArrayList<>();
        final List<Dataset> datasets = datasetBusiness.getAllDataset();
        if(datasets!=null){
            for(final Dataset ds : datasets){
                final List<DataBrief> briefs = new ArrayList<>();
                final DataSetBrief dsb = buildDatsetBrief(ds,briefs);
                datasetBriefs.add(dsb);
            }
        }
        return Response.ok(datasetBriefs).build();
    }

    @GET
    @Path("list/published/{published}/data")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response getPublishedDataList(@PathParam("domainId") int domainId, @PathParam("published") boolean published) {
        final List<DataBrief> briefs = new ArrayList<>();
        final List<Integer> providerIds = providerBusiness.getProviderIdsAsInt();
        for (final Integer providerId : providerIds) {
            final Provider provider = providerBusiness.getProvider(providerId);
            final String parent = provider.getParent();
            if (parent != null && !parent.isEmpty()) {
                // Skip all providers that have a parent ie : skip for tiled data to avoid duplication
                continue;
            }
            final List<org.constellation.engine.register.jooq.tables.pojos.Data> datas;
            datas = providerBusiness.getDatasFromProviderId(provider.getId());
            for (final org.constellation.engine.register.jooq.tables.pojos.Data data : datas) {
                if (data.getIncluded() && !data.getHidden()) {
                    final QName name = new QName(data.getNamespace(), data.getName());
                    final DataBrief db = dataBusiness.getDataBrief(name, providerId);
                    if ((published && (db.getTargetService() == null || db.getTargetService().size() == 0)) ||
                            (!published && db.getTargetService() != null && db.getTargetService().size() > 0)) {
                        continue;
                    }
                    briefs.add(db);
                }
            }
        }
        return Response.ok(briefs).build();
    }

    @GET
    @Path("list/published/{published}/dataset")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response getPublishedDatasetList(@PathParam("domainId") int domainId, @PathParam("published") boolean published) {
        final List<DataSetBrief> datasetBriefs = new ArrayList<>();
        final List<Dataset> datasets = datasetBusiness.getAllDataset();
        if(datasets!=null){
            for(final Dataset ds : datasets){
                final List<org.constellation.engine.register.jooq.tables.pojos.Data> dataList = dataBusiness.findByDatasetId(ds.getId());
                final List<DataBrief> briefs = new ArrayList<>();
                for (final org.constellation.engine.register.jooq.tables.pojos.Data data : dataList) {
                    if (data.getIncluded() && !data.getHidden()) {
                        final QName name = new QName(data.getNamespace(), data.getName());
                        final DataBrief db = dataBusiness.getDataBrief(name, data.getProvider());
                        if ((published  && (db.getTargetService() == null ||  db.getTargetService().isEmpty())) ||
                                (!published && db.getTargetService() != null && !db.getTargetService().isEmpty())) {
                            continue;
                        }
                        briefs.add(db);
                    }
                }
                if(briefs.isEmpty()){
                    continue;
                }
                final DataSetBrief dsb = buildDatsetBrief(ds,briefs);
                datasetBriefs.add(dsb);
            }
        }
        return Response.ok(datasetBriefs).build();
    }

    @GET
    @Path("list/observation/{sensorable}/data")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response getSensorableDataList(@PathParam("domainId") int domainId, @PathParam("sensorable") boolean sensorable) {
        final List<DataBrief> briefs = new ArrayList<>();
        final List<Integer> providerIds = providerBusiness.getProviderIdsAsInt();
        for (final Integer providerId : providerIds) {
            final Provider provider = providerBusiness.getProvider(providerId);
            final String parent = provider.getParent();
            if (parent != null && !parent.isEmpty()) {
                // Remove all providers that have a parent
                continue;
            }
            final List<org.constellation.engine.register.jooq.tables.pojos.Data> datas;
            datas = providerBusiness.getDatasFromProviderId(provider.getId());
            for (final org.constellation.engine.register.jooq.tables.pojos.Data data : datas) {

                if (data.getIncluded() && !data.getHidden()) {
                    final QName name = new QName(data.getNamespace(), data.getName());
                    final DataBrief db = dataBusiness.getDataBrief(name, providerId);
                    if ((sensorable && (db.getTargetSensor() == null || db.getTargetSensor().size() == 0)) ||
                            (!sensorable && db.getTargetSensor() != null && db.getTargetSensor().size() > 0)) {
                        continue;
                    }
                    briefs.add(db);
                }
            }
        }
        return Response.ok(briefs).build();
    }

    @GET
    @Path("list/observation/{sensorable}/dataset")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response getSensorableDatasetList(@PathParam("domainId") int domainId, @PathParam("sensorable") boolean sensorable) {
        final List<DataSetBrief> datasetBriefs = new ArrayList<>();
        final List<Dataset> datasets = datasetBusiness.getAllDataset();
        if(datasets!=null){
            for(final Dataset ds : datasets){
                final List<org.constellation.engine.register.jooq.tables.pojos.Data> dataList = dataBusiness.findByDatasetId(ds.getId());
                final List<DataBrief> briefs = new ArrayList<>();
                for (final org.constellation.engine.register.jooq.tables.pojos.Data data : dataList) {
                    if (data.getIncluded() && !data.getHidden()) {
                        final QName name = new QName(data.getNamespace(), data.getName());
                        final DataBrief db = dataBusiness.getDataBrief(name, data.getProvider());
                        if ((sensorable && (db.getTargetSensor() == null ||  db.getTargetSensor().isEmpty())) ||
                                (!sensorable && db.getTargetSensor() != null && !db.getTargetSensor().isEmpty())) {
                            continue;
                        }
                        briefs.add(db);
                    }
                }
                if(briefs.isEmpty()){
                    continue;
                }
                final DataSetBrief dsb = buildDatsetBrief(ds,briefs);
                datasetBriefs.add(dsb);
            }
        }
        return Response.ok(datasetBriefs).build();
    }

    @POST
    @Path("include/{dataId}")
    public Response includeData(final @PathParam("dataId") int dataId) {
        try {
            dataBusiness.updateDataIncluded(dataId, true);
            return Response.ok().entity("data included successfully!").build();
        }catch(Exception ex){
            LOGGER.log(Level.WARNING,ex.getLocalizedMessage(),ex);
            return Response.status(500).entity("Failed to include data, cause : "+ex.getMessage()).build();
        }
    }

    @DELETE
    @Path("{dataId}")
    public Response removeData(final @PathParam("dataId") int dataId) {
        try {
            dataBusiness.removeData(dataId);
            return Response.ok().type(MediaType.TEXT_PLAIN_TYPE).build();
        }catch(Exception ex){
            LOGGER.log(Level.WARNING,ex.getLocalizedMessage(),ex);
            return Response.status(500).entity("Failed to remove data, cause : "+ex.getMessage()).build();
        }
    }

    @GET
    @Path("layer/summary/{providerid}/{layerAlias}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getLayerSummary(@PathParam("providerid") String providerid, @PathParam("layerAlias") String layerAlias) {
        final DataBrief db = dataBusiness.getDataLayer(layerAlias, providerid);
        return Response.ok(db).build();
    }

    @GET
    @Path("metadata/iso/{providerId}/{dataId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getIsoMetadata(final @PathParam("providerId") String providerId, final @PathParam("dataId") String dataId) throws ConfigurationException {
        final DefaultMetadata metadata = dataBusiness.loadIsoDataMetadata(providerId, Util.parseQName(dataId));
        if (metadata != null) {
            metadata.prune();
        }
        return Response.ok(metadata).build();
    }

    /**
     * Return as an attachment file the metadata of data in xml format.
     * @param providerId given data provider identifier.
     * @param dataId data Id.
     * @return the xml file
     */
    @GET
    @Path("metadata/iso/download/{providerId}/{dataId}")
    @Produces(MediaType.APPLICATION_XML)
    public Response downloadMetadataForData(final @PathParam("providerId") String providerId,
                                            final @PathParam("dataId") int dataId) {
        try {
            DefaultMetadata metadata = metadataBusiness.getIsoMetadataForData(dataId);
            if (metadata == null) {
                //try to get dataset metadata.
                final Dataset dataset = dataBusiness.getDatasetForData(dataId);
                if (dataset != null) {
                    metadata = datasetBusiness.getMetadata(dataset.getIdentifier());
                }
            }
            if (metadata != null) {
                metadata.prune();
                final String xmlStr = metadataBusiness.marshallMetadata(metadata);
                return Response.ok(xmlStr, MediaType.APPLICATION_XML_TYPE)
                        .header("Content-Disposition", "attachment; filename=\"" + providerId + ".xml\"").build();
            }
        }catch(Exception ex){
            LOGGER.log(Level.WARNING, "Failed to get xml metadata for data with provider identifier "+providerId+" dataId = "+dataId,ex);
        }
        return Response.ok("<empty></empty>", MediaType.APPLICATION_XML_TYPE)
                .header("Content-Disposition", "attachment; filename=\"" + providerId + ".xml\"").build();
    }

    @POST
    @Path("metadata/associated")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getAssociatedData(final String[] params) {
        final Map<String, List<DataBrief>> mapping = new HashMap<>();
        for (final String id : params) {
            if (id != null) {
                final List<DataBrief> dataBriefs = dataBusiness.getDataBriefsFromMetadataId(id);
                mapping.put(id, dataBriefs);
            }
        }
        return Response.ok(mapping).build();
    }

    @POST
    @Path("testextension")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public SelectedExtension testExtension(final SimpleValue extension) {
        final Map<String, String> extensions = GeotoolkitFileExtensionAvailable.getAvailableFileExtension();
        final String type = extensions.get(extension.getValue().toLowerCase());
        final SelectedExtension validate = new SelectedExtension();
        validate.setExtension(extension.getValue());

        if (type != null) {
            validate.setDataType(type);
        } else {
            validate.setDataType("");
        }
        return validate;
    }

    private SelectedExtension findDataType(final String filePath,final String extension,final String selectedType) {
        final SelectedExtension result = new SelectedExtension();
        result.setExtension(extension);
        result.setDataType(selectedType);
        // look for observation netcdf
        if ("nc".equals(extension) && NetCDFExtractor.isObservationFile(filePath)) {
            result.setDataType("observation");
        }
        // look for SML file (available for data import ?)
        if ("xml".equals(extension)) {
            try {
                String rootMark = getXmlDocumentRoot(filePath);
                if (rootMark.equals("SensorML")) {
                    result.setDataType("observation");
                }
            } catch (IOException | XMLStreamException ex) {
                LOGGER.log(Level.WARNING, "error while reading xml file", ex);
            }
        }
        return result;
    }

    @GET
    @Path("export/{providerId}/{dataId}")
    public Response exportData(final @PathParam("providerId") String providerId, final @PathParam("dataId") String dataId) {
        final DataProvider provider = DataProviders.getInstance().getProvider(providerId);
        DataStore store = provider.getMainStore();
        if (store instanceof ExtendedFeatureStore) {
            store = ((ExtendedFeatureStore)store).getWrapped();
        }

        if (!(store instanceof DataFileStore)) {
            LOGGER.info("No files for this data to export!");
            return Response.status(500).entity("failed").build();
        }

        final DataFileStore fileStore = (DataFileStore)store;
        final File[] filesToSend;
        try {
            filesToSend = fileStore.getDataFiles();
        } catch (DataStoreException ex) {
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            return Response.status(500).entity(ex.getLocalizedMessage()).build();
        }

        if (filesToSend.length == 0) {
            LOGGER.info("No files for this data to export!");
            return Response.status(500).entity("No files for this data to export!").build();
        }

        if (filesToSend.length == 1 && !filesToSend[0].isDirectory()) {
            final File f = filesToSend[0];
            return Response.ok(f).header("content-disposition", "attachment; filename="+ f.getName()).build();
        }

        final File zip = new File(System.getProperty("java.io.tmpdir"), dataId +".zip");
        if (zip.exists()) {
            zip.delete();
        }

        final Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        try {
            URI uri = URI.create("jar:file:" + zip.getAbsolutePath());
            try (FileSystem zipfs = FileSystems.newFileSystem(uri, env)) {
                for (final File fileToSend : filesToSend) {
                    File[] files = fileToSend.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        File fileAddToZip = files[i];
                        java.nio.file.Path pathFile = fileAddToZip.toPath();
                        java.nio.file.Path pathInZipFile = zipfs.getPath(fileAddToZip.getName());
                        Files.copy(pathFile, pathInZipFile, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        } catch (IOException ex) {
            LOGGER.info("Error while zipping data");
            return Response.status(500).entity(ex.getLocalizedMessage()).build();
        }
        return Response.ok(zip).header("content-disposition", "attachment; filename=" + zip.getName()).build();
    }

    protected String getXmlDocumentRoot(final String filePath) throws IOException, XMLStreamException {
        final FileInputStream stream = new FileInputStream(new File(filePath));
        final XMLStreamReader xsr = xif.createXMLStreamReader(stream);
        xsr.nextTag();
        final String rootName = xsr.getLocalName();
        xsr.close();
        stream.close();
        return rootName;
    }

    @POST
    @Path("link/sensor/{providerId}/{dataId}/{sensorId}")
    public Response linkDataToSensor(final @PathParam("providerId") String providerId,
                                     final @PathParam("dataId") String dataId,
                                     final @PathParam("sensorId") String sensorId,
                                     final SimpleValue value) {
        final String namespace = value.getValue();
        final QName name = new QName(namespace, dataId);
        sensorBusiness.linkDataToSensor(name, providerId, sensorId);
        return Response.ok().type(MediaType.TEXT_PLAIN_TYPE).build();
    }

    @POST
    @Path("unlink/sensor/{providerId}/{dataId}/{sensorId}")
    public Response unlinkDataToSensor(final @PathParam("providerId") String providerId,
                                       final @PathParam("dataId") String dataId,
                                       final @PathParam("sensorId") String sensorId,
                                       final SimpleValue value) {
        final String namespace = value.getValue();
        final QName name = new QName(namespace, dataId);
        try{
            sensorBusiness.unlinkDataToSensor(name, providerId, sensorId);
        }catch(TargetNotFoundException ex){
            LOGGER.log(Level.WARNING,ex.getMessage(),ex);
            return Response.status(500).entity(ex.getLocalizedMessage()).build();
        }
        return Response.ok().type(MediaType.TEXT_PLAIN_TYPE).build();
    }

    /**
     * Get the properties (columns) names for a vector data.
     *
     * @param id Vector data identifier
     * @throws DataStoreException
     */
    @GET
    @Path("{id}/vector/columns")
    public Response getVectorDataColumns(final @PathParam("id") int id) throws DataStoreException {
        return Response.ok(dataBusiness.getVectorDataColumns(id)).build();
    }


    @GET
    @Path("{dataId}/associations")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getAssociations(@PathParam("dataId") int dataId) {
        if (dataBusiness.existsById(dataId)) {
            Map<String, Object> entity = new HashMap<>();
            entity.put("styles", styleRepository.fetchByDataId(dataId));
            entity.put("services", serviceRepository.fetchByDataId(dataId));
            entity.put("sensors", sensorRepository.fetchByDataId(dataId));
            return ok(entity);
        }
        return Response.status(404).build();
    }

    @DELETE
    @Path("{dataId}/associations/styles/{styleId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Transactional
    public Response deleteStyleAssociation(@PathParam("dataId") int dataId, @PathParam("styleId") int styleId) {
        if (dataBusiness.existsById(dataId) && styleRepository.existsById(styleId)) {
            styleRepository.unlinkStyleToData(styleId, dataId);
            return Response.noContent().build();
        }
        return Response.status(404).build();
    }

    @DELETE
    @Path("{dataId}/associations/sensors/{sensorId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Transactional
    public Response deleteSensorAssociation(@PathParam("dataId") int dataId, @PathParam("sensorId") String sensorIdentifier) {
        Sensor sensor = sensorRepository.findByIdentifier(sensorIdentifier);
        if (sensor != null && dataBusiness.existsById(dataId)) {
            sensorRepository.unlinkDataToSensor(dataId, sensor.getId());
            return Response.noContent().build();
        }
        return Response.status(404).build();
    }
}
