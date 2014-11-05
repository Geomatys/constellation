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

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
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
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.google.common.base.Optional;
import org.apache.sis.geometry.GeneralDirectPosition;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.storage.DataStore;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.logging.Logging;
import org.apache.sis.xml.MarshallerPool;
import org.apache.sis.xml.XML;
import org.constellation.admin.exception.ConstellationException;
import org.constellation.business.*;
import org.constellation.configuration.*;
import org.constellation.dto.CoverageMetadataBean;
import org.constellation.dto.DataInformation;
import org.constellation.dto.FileBean;
import org.constellation.dto.ImportedData;
import org.constellation.dto.MetadataLists;
import org.constellation.dto.ParameterValues;
import org.constellation.dto.ProviderData;
import org.constellation.dto.PyramidParams;
import org.constellation.dto.SimpleValue;
import org.constellation.engine.register.CstlUser;
import org.constellation.engine.register.Dataset;
import org.constellation.engine.register.Provider;
import org.constellation.engine.register.TaskParameter;
import org.constellation.engine.register.repository.StyleRepository;
import org.constellation.engine.register.repository.UserRepository;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.json.metadata.Template;
import org.constellation.json.metadata.binding.RootObj;
import org.constellation.model.SelectedExtension;
import org.constellation.provider.CoverageData;
import org.constellation.provider.Data;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviderFactory;
import org.constellation.provider.DataProviders;
import org.constellation.provider.FeatureData;
import org.constellation.provider.Providers;
import org.constellation.provider.configuration.ProviderParameters;
import org.constellation.util.ParamUtilities;
import org.constellation.util.SimplyMetadataTreeNode;
import org.constellation.util.Util;
import org.constellation.utils.GeotoolkitFileExtensionAvailable;
import org.constellation.utils.ISOMarshallerPool;
import org.constellation.utils.MetadataFeeder;
import org.constellation.admin.util.MetadataUtilities;
import org.geotoolkit.coverage.*;
import org.geotoolkit.coverage.grid.GeneralGridGeometry;
import org.geotoolkit.coverage.grid.ViewType;
import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.coverage.xmlstore.XMLCoverageReference;
import org.geotoolkit.coverage.xmlstore.XMLCoverageStore;
import org.geotoolkit.coverage.xmlstore.XMLCoverageStoreFactory;
import org.geotoolkit.csw.xml.CSWMarshallerPool;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.data.memory.ExtendedFeatureStore;
import org.geotoolkit.display.PortrayalException;
import org.geotoolkit.feature.type.DefaultName;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.geometry.Envelopes;
import org.geotoolkit.image.interpolation.InterpolationCase;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.observation.ObservationStore;
import org.geotoolkit.parameter.ParametersExt;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.ReferencingUtilities;
import org.geotoolkit.sos.netcdf.NetCDFExtractor;
import org.geotoolkit.storage.DataFileStore;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.util.FileUtilities;
import org.geotoolkit.util.StringUtilities;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ImageCRS;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import org.opengis.util.NoSuchIdentifierException;
import org.w3c.dom.Node;

/**
 * Manage data sending
 *
 * @author Benjamin Garcia (Geomatys)
 * @author Christophe Mourette (Geomatys)
 */
@Path("/1/domain/{domainId}/data/")
public class DataRest {

    private final XMLInputFactory xif = XMLInputFactory.newFactory();

    private static final Logger LOGGER = Logging.getLogger(DataRest.class);

    private static final String CONFORM_PREFIX = "conform_";
    private static final String RENDERED_PREFIX = "rendered_";

    @Inject
    private UserRepository userRepository;

    @Inject
    private StyleRepository styleRepository;

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
        final MetadataLists mdList = dataBusiness.getMetadataCodeLists();
        return Response.ok().entity(mdList).build();
    }


    /**
     * Give subfolder list from a server file path
     *
     * @param path server file path
     * @param filtered {@code True} if we want to keep only known files.
     * @return a {@link javax.ws.rs.core.Response} which contain file list
     */
    @POST
    @Path("datapath/{filtered}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getDataFolder(@PathParam("filtered") final Boolean filtered, String path) {
        final List<FileBean> listBean = new ArrayList<>();
        final Set<String> extensions = GeotoolkitFileExtensionAvailable.getAvailableFileExtension().keySet();

//        final File root = ConfigDirectory.getUserHomeDirectory();
        final File[] children;
        if (Paths.get(path).toFile().exists()) {
            final File nextRoot = new File(path);
            children = nextRoot.listFiles();
        }else{
            Map<String,String> hashMap = new HashMap<>();
            hashMap.put("msg", "invalid path");
            return Response.status(500).entity(hashMap).build();
        }

        //loop on subfiles/folders to create bean
        if (children != null) {
            for (File child : children) {
                final FileBean bean = new FileBean(child.getName(),
                                                   child.isDirectory(),
                                                   child.getAbsolutePath(),
                                                   child.getParentFile().getAbsolutePath());

                if (!child.isDirectory() || !filtered) {
                    final int lastIndexPoint = child.getName().lastIndexOf('.');
                    final String extension = child.getName().substring(lastIndexPoint + 1);

                    if (extensions.contains(extension.toLowerCase()) /*|| "zip".equalsIgnoreCase(extension)*/) {
                        listBean.add(bean);
                    }

                } else {
                    listBean.add(bean);
                }
            }
        }
        Collections.sort(listBean);
        return Response.status(200).entity(listBean).build();
    }

    /**
     * Give subfolder list from a server file path
     *
     * @param path server file path
     * @param filtered {@code True} if we want to keep only known files.
     * @return a {@link javax.ws.rs.core.Response} which contain file list
     */
    @POST
    @Path("metadatapath/{filtered}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getMetaDataFolder(@PathParam("filtered") final Boolean filtered, String path) {
        final List<FileBean> listBean = new ArrayList<>();

        final File[] children;
        if (Paths.get(path).toFile().exists()) {
            final File nextRoot = new File(path);
            children = nextRoot.listFiles();
        }else{
            Map<String,String> hashMap = new HashMap<>();
            hashMap.put("msg", "invalid path");
            return Response.status(500).entity(hashMap).build();
        }

        //loop on subfiles/folders to create bean
        if (children != null) {
            for (File child : children) {
                final FileBean bean = new FileBean(child.getName(),
                                                   child.isDirectory(),
                                                   child.getAbsolutePath(),
                                                   child.getParentFile().getAbsolutePath());

                if (!child.isDirectory() || !filtered) {
                    final int lastIndexPoint = child.getName().lastIndexOf('.');
                    final String extension = child.getName().substring(lastIndexPoint + 1);

                    if ("xml".equalsIgnoreCase(extension)) {
                        listBean.add(bean);
                    }

                } else {
                    listBean.add(bean);
                }
            }
        }
        Collections.sort(listBean);
        return Response.status(200).entity(listBean).build();
    }

    /**
     * Receive a {@link MultiPart} which contain a file need to be save on server to create data on provider
     *
     * @param fileIs
     * @param fileDetail
     * @param fileMetaIs
     * @param fileMetaDetail
     * @param request
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
    	final String sessionId = request.getSession(false).getId();
    	final File uploadDirectory = ConfigDirectory.getUploadDirectory(sessionId);
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
     *
     * @param mdFileIs
     * @param fileMetaDetail
     * @param identifier
     * @param serverMetadataPath
     * @param request
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
    	final String sessionId = request.getSession(false).getId();
        final File uploadDirectory = ConfigDirectory.getUploadDirectory(sessionId);
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
        try {
            final MarshallerPool pool = CSWMarshallerPool.getInstance();
            final Unmarshaller unmarsh = pool.acquireUnmarshaller();
            obj = unmarsh.unmarshal(newFileMetaData);
            pool.recycle(unmarsh);
        } catch (JAXBException ex) {
            LOGGER.log(Level.WARNING, "Error when trying to unmarshal metadata", ex);
            throw new ConstellationException("metadata file is incorrect");
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
     *
     * @param values {@link org.constellation.dto.ParameterValues} containing file path & data type
     * @param request
     * @return a {@link javax.ws.rs.core.Response}
     *
     * @Deprecated this is deprecated since we are using the method proceedToImport.
     */
    @POST
    @Path("import")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response importData(final ParameterValues values, @Context HttpServletRequest request) {
        String filePath = values.getValues().get("dataPath");
        final String metadataFilePath = values.getValues().get("metadataFilePath");
        final String dataType = values.getValues().get("dataType");
        final String dataName= values.getValues().get("dataName");
        final String fsServer = values.getValues().get("fsServer");

        try{
            final File dataIntegratedDirectory = ConfigDirectory.getDataIntegratedDirectory();
            final File uploadFolder = new File(ConfigDirectory.getDataDirectory(), "upload");
            final ImportedData importedData = new ImportedData();
            if (fsServer != null && fsServer.equalsIgnoreCase("true")) {
                importedData.setDataFile(filePath);
            } else {
                if (filePath != null) {
                    filePath = renameDataFile(dataName, filePath);
                    if (filePath.toLowerCase().endsWith(".zip")) {
                        final File zipFile = new File(filePath);
                        final String fileNameWithoutExt = zipFile.getName().substring(0, zipFile.getName().indexOf("."));
                        final File zipDir = new File(dataIntegratedDirectory, dataName);
                        if (zipDir.exists()) {
                            recursiveDelete(zipDir);
                        }
                        FileUtilities.unzip(zipFile, zipDir, new CRC32());
                        filePath = zipDir.getAbsolutePath();
                    }

                    if (filePath.startsWith(uploadFolder.getAbsolutePath())) {
                        final File destFile = new File(dataIntegratedDirectory.getAbsolutePath() + File.separator + new File(filePath).getName());
                        Files.move(Paths.get(filePath), Paths.get(destFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
                        importedData.setDataFile(destFile.getAbsolutePath());
                    } else {
                        importedData.setDataFile(filePath);
                    }
                }
            }

            if (metadataFilePath != null){
                if (metadataFilePath.startsWith(uploadFolder.getAbsolutePath())) {
                    final File destMd = new File(dataIntegratedDirectory.getAbsolutePath() + File.separator + new File(metadataFilePath).getName());
                    Files.move(Paths.get(metadataFilePath), Paths.get(destMd.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
                    importedData.setMetadataFile(destMd.getAbsolutePath());
                } else {
                    importedData.setMetadataFile(metadataFilePath);
                }
            }

            return Response.ok(importedData).build();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Bad configuration for data Integrated directory", e);
            return Response.status(500).entity(e.getLocalizedMessage()).build();
        }
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
            final File uploadFolder = new File(ConfigDirectory.getDataDirectory(), "upload");

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
                    if (filePath.toLowerCase().endsWith(".zip")) {
                        final File zipFile = new File(filePath);
                        final File zipDir = new File(dataIntegratedDirectory, dataName);
                        if (zipDir.exists()) {
                            recursiveDelete(zipDir);
                        }
                        FileUtilities.unzip(zipFile, zipDir, new CRC32());
                        filePath = zipDir.getAbsolutePath();
                    }
                    if (filePath.startsWith(uploadFolder.getAbsolutePath())) {
                        final File destFile = new File(dataIntegratedDirectory.getAbsolutePath() + File.separator + new File(filePath).getName());
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
                } else {
                    subType = null;
                }
                //create provider
                final ProviderConfiguration config = new ProviderConfiguration();
                config.setType("feature-store");
                config.setSubType(subType);
                config.getParameters().put("path",dataFile);
                providerBusiness.create(domainId, providerIdentifier, config);

                //set up user metadata
                if (importedMetaData != null && !importedMetaData.isEmpty()) {
                    proceedToSaveUploadedMetadata(providerIdentifier, importedMetaData);
                }

                //verify CRS
                try {
                    final Map<Name, CoordinateReferenceSystem> nameCoordinateReferenceSystemHashMap = DataProviders.getInstance().getCRS(providerIdentifier);
                    for(final CoordinateReferenceSystem crs : nameCoordinateReferenceSystemHashMap.values()){
                        if (crs == null || crs instanceof ImageCRS) {
                            throw new DataStoreException("CRS is null or is instance of ImageCRS");
                        }
                    }
                    importedDataReport.setVerifyCRS("success");
                } catch (DataStoreException e) {
                    importedDataReport.setVerifyCRS("error");
                    LOGGER.log(Level.INFO, "Cannot get CRS for provider "+providerIdentifier+" for domain "+domainId);
                    //get a list of EPSG codes
                    importedDataReport.setCodes(getAllEpsgCodes());
                }
            }else if("raster".equalsIgnoreCase(uploadType)) {
                //create provider
                final ProviderConfiguration config = new ProviderConfiguration();
                config.setType("coverage-store");
                config.setSubType("coverage-file");
                config.getParameters().put("path",dataFile);
                providerBusiness.create(domainId, providerIdentifier, config);

                //set up user metadata
                if (importedMetaData != null && !importedMetaData.isEmpty()) {
                    proceedToSaveUploadedMetadata(providerIdentifier, importedMetaData);
                }

                //verify CRS
                try {
                    final Map<Name, CoordinateReferenceSystem> nameCoordinateReferenceSystemHashMap = DataProviders.getInstance().getCRS(providerIdentifier);
                    for(final CoordinateReferenceSystem crs : nameCoordinateReferenceSystemHashMap.values()){
                        if (crs == null || crs instanceof ImageCRS) {
                            throw new DataStoreException("CRS is null or is instance of ImageCRS");
                        }
                    }
                    importedDataReport.setVerifyCRS("success");
                } catch (DataStoreException e) {
                    importedDataReport.setVerifyCRS("error");
                    LOGGER.log(Level.INFO, "Cannot get CRS for provider "+providerIdentifier+" for domain "+domainId);
                    //get a list of EPSG codes
                    importedDataReport.setCodes(getAllEpsgCodes());
                }
            }else if("observation".equalsIgnoreCase(uploadType) && "xml".equalsIgnoreCase(fileExtension)) {
                //create provider
                final ProviderConfiguration config = new ProviderConfiguration();
                config.setType("observation-store");
                config.setSubType("observation-xml");
                config.getParameters().put("path",dataFile);
                providerBusiness.create(domainId, providerIdentifier, config);

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
                providerBusiness.create(domainId, providerIdentifier, config);

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
        final DataStore ds = DataProviders.getInstance().getProvider(providerId).getMainStore();
        if (!(ds instanceof XMLCoverageStore)) {
            return Response.status(500).entity("Datastore is not instance of XMLCoverageStore").build();
        }

        final XMLCoverageStore xmlCoverageStore = (XMLCoverageStore)ds;
        final ParameterValue paramVal = ParametersExt.getValue(xmlCoverageStore.getConfiguration(), XMLCoverageStoreFactory.PATH.getName().getCode());
        if (paramVal.getValue() instanceof URL) {
            try {
                final File dataFolder = new File(((URL)paramVal.getValue()).toURI());
                recursiveDelete(dataFolder);
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Unable to delete folder "+ paramVal.getValue(), ex);
                return Response.status(500).entity(ex.getLocalizedMessage()).build();
            }
        }
        return Response.ok().type(MediaType.TEXT_PLAIN_TYPE).build();
    }

    private static void truncateZipFolder(String filePath) throws IOException {
        File file = new File(filePath);
        if (file.isDirectory()){
            if (file.listFiles().length==1 && file.listFiles()[0].isDirectory()){
                File directoryToDelete = file.listFiles()[0];
                File[] filesToMove = directoryToDelete.listFiles();
                for (int i = 0 ; i < filesToMove.length;i++){
                    Files.move(Paths.get(filesToMove[i].getPath()) ,Paths.get(file.getPath() + File.separator + filesToMove[i].getName()));
                }
                directoryToDelete.delete();
            }
        }
    }
    
    private static void recursiveDelete(File file) {
        //to end the recursive loop
        if (!file.exists())
            return;
         
        //if directory, go inside and call recursively
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                //call recursively
                recursiveDelete(f);
            }
        }
        //call delete to delete files and empty directory
        file.delete();
    }

    /**
     * Load data from file selected 
     
     *
     * @param domainId
     * @param values {@link org.constellation.dto.ParameterValues} containing file path & data type
     * @return a {@link javax.ws.rs.core.Response} with a {@link org.constellation.dto.DataInformation}
     * 
     * @throws javax.xml.bind.JAXBException
     * @throws org.apache.sis.storage.DataStoreException
     * @throws org.opengis.util.NoSuchIdentifierException
     * @throws org.geotoolkit.process.ProcessException
     */
    @POST
    @Path("load")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response loadData(@PathParam("domainId") int domainId, final ParameterValues values) throws JAXBException, DataStoreException, NoSuchIdentifierException, ProcessException {
        final String filePath = values.getValues().get("filePath");
        final String metadataFilePath = values.getValues().get("metadataFilePath");
        final String dataType = values.getValues().get("dataType");
        final String providerId = values.getValues().get("providerId");
        DataInformation information = null;
        final DataProvider dataProvider;
        if (providerId !=null && providerId.length()>0){
            dataProvider = DataProviders.getInstance().getProvider(providerId);
        }else{
            return Response.status(404).build();
        }

        final Provider providerDB = providerBusiness.getProvider(providerId, domainId);
        // for now assume that providerID == datasetID
        final Dataset datasetDB   = datasetBusiness.getDataset(providerId, domainId);
        final List<org.constellation.engine.register.Data> datasFromProviderId = providerBusiness.getDatasFromProviderId(providerDB.getId());
        final String metadata;
        if(datasFromProviderId.isEmpty()){
            return Response.status(500).entity("The dataset have no data").build();
        }
        if (datasFromProviderId.size()>1){
            metadata = datasetDB.getMetadataIso();
        }else{
            metadata = datasFromProviderId.get(0).getIsoMetadata();
        }
        final MarshallerPool pool = ISOMarshallerPool.getInstance();
        final Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        final DefaultMetadata defaultMetadata = (DefaultMetadata) unmarshaller.unmarshal(new ByteArrayInputStream(metadata.getBytes()));

        switch (dataType) {
            case "raster":
                final Name nameCoverage = ((CoverageStore) dataProvider.getMainStore()).getNames().iterator().next();
                final GridCoverageReader gridCoverageReader = ((CoverageStore) dataProvider.getMainStore()).getCoverageReference(nameCoverage).acquireReader();
                information = MetadataUtilities.getRasterDataInformation(gridCoverageReader, defaultMetadata, providerDB.getType());
                break;
            case "vector":
                final Name nameFeature = ((FeatureStore) dataProvider.getMainStore()).getNames().iterator().next();
                information = new DataInformation(providerId, null, providerDB.getType(), dataProvider.get(nameFeature).getEnvelope().getCoordinateReferenceSystem().getName().toString());
                information.setFileMetadata(MetadataUtilities.getVectorDataInformation(defaultMetadata));
                break;
            case "observation":
                final ObservationStore store = (ObservationStore) dataProvider.getMainStore();
                information = new DataInformation(providerId, null, dataType, null);
                information.setFileMetadata(MetadataUtilities.getSensorInformations(providerId, store));
                break;

        }
        return Response.ok(information).build();
    }

    /**
     * Save metadata.
     *
     * @param values
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
            final Object obj = unmarshallMetadata(mdPath);
            if (!(obj instanceof DefaultMetadata)) {
                throw new ConstellationException("Cannot save uploaded metadata because it is not recognized as a valid file!");
            }
            final DefaultMetadata metadata = (DefaultMetadata) obj;
            // for now we assume datasetID == providerID
            try {
                datasetBusiness.updateMetadata(providerId, -1, metadata);
            } catch (ConfigurationException ex) {
                throw new ConstellationException("Error while saving dataset metadata, "+ex.getMessage());
            }
        }
    }

    private Object unmarshallMetadata(final String mdPath) throws ConstellationException {
        final Object obj;
        try {
            final MarshallerPool pool = CSWMarshallerPool.getInstance();
            final Unmarshaller unmarsh = pool.acquireUnmarshaller();
            obj = unmarsh.unmarshal(new File(mdPath));
            pool.recycle(unmarsh);
        } catch (JAXBException ex) {
            throw new ConstellationException("Error when trying to unmarshal metadata "+ex.getMessage());
        }
        return obj;
    }

    /**
     * Returns json result of template writer to apply a given template to metadata object.
     * The path of each fields/blocks will be numerated.
     * the returned json object will be used directly in html metadata editor.
     *
     * @param values given parameters.
     * @return {@code Response}
     * @throws ConfigurationException
     */
    @POST
    @Path("metadata/dataset")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getDatasetMetadata(final ParameterValues values) {
        final String identifier         = values.getValues().get("identifier");
        final String dataType           = values.getValues().get("type");
        final StringBuilder buffer = new StringBuilder();
        try{
            final DefaultMetadata metadata  =  datasetBusiness.getMetadata(identifier, -1);
            if (metadata != null) {
                //get template name
                final String templateName = datasetBusiness.getTemplate(identifier, dataType);
                final Template template   = Template.getInstance(templateName);
                template.write(metadata,buffer,false);
            }
        }catch(Exception ex){
            LOGGER.log(Level.WARNING, "error cannot get dataset Metadata.", ex);
        }
        return Response.ok(buffer.toString()).build();
    }

    @POST
    @Path("metadata/data")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getDataMetadata(final ParameterValues values) throws ConfigurationException {
        final String providerId         = values.getValues().get("provider");
        final String identifier         = values.getValues().get("identifier");
        final String dataType           = values.getValues().get("type");
        final StringBuilder buffer = new StringBuilder();
        try{
            final QName dataName = Util.parseQName(identifier);
            DefaultMetadata metadata = dataBusiness.loadIsoDataMetadata(providerId, dataName);
            String datasetId = null;
            if(metadata == null){
                //try to get dataset metadata.
                final Dataset dataset = dataBusiness.getDatasetForData(providerId, dataName);
                if (dataset != null) {
                    metadata = datasetBusiness.getMetadata(dataset.getIdentifier(),-1);
                    datasetId = dataset.getIdentifier();
                }
            }
            if (metadata != null) {
                //get template name
                final String templateName;
                if (datasetId != null) {
                    templateName = datasetBusiness.getTemplate(datasetId, dataType);
                } else {
                    templateName = dataBusiness.getTemplate(dataName, dataType);
                }
                final Template template = Template.getInstance(templateName);
                template.write(metadata,buffer,false);
            }
        }catch(Exception ex){
            LOGGER.log(Level.WARNING, "error cannot get dataset Metadata.", ex);
        }
        return Response.ok(buffer.toString()).build();
    }

    /**
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
        final StringBuilder buffer = new StringBuilder();
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
                    metadata = datasetBusiness.getMetadata(dataset.getIdentifier(),-1);
                    datasetId = dataset.getIdentifier();
                }
            }
            if (metadata != null) {
                metadata.prune();

                //for debugging purposes
                    /*try{
                        System.out.println(XML.marshal(metadata));
                    }catch(Exception ex){
                        LOGGER.log(Level.WARNING,ex.getLocalizedMessage(),ex);
                    }*/

                //get template name
                final String templateName;
                if (datasetId != null) {
                    templateName = datasetBusiness.getTemplate(datasetId, type);
                } else {
                    templateName = dataBusiness.getTemplate(dataName, type);
                }
                final Template template = Template.getInstance(templateName);
                template.write(metadata,buffer,prune);
            }
        }catch(Exception ex){
            LOGGER.log(Level.WARNING, "error while writing metadata json.", ex);
        }
        return Response.ok(buffer.toString()).build();
    }

    /**
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
        final StringBuilder buffer = new StringBuilder();
        try{
            final DefaultMetadata metadata = datasetBusiness.getMetadata(datasetIdentifier,-1);
            if (metadata != null) {
                metadata.prune();
                //for debugging purposes
                /*try{
                    System.out.println(XML.marshal(metadata));
                }catch(Exception ex){
                    LOGGER.log(Level.WARNING,ex.getLocalizedMessage(),ex);
                }*/

                //get template name
                final String templateName = datasetBusiness.getTemplate(datasetIdentifier, type);
                final Template template   = Template.getInstance(templateName);
                template.write(metadata,buffer,prune);
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
                metadata = datasetBusiness.getMetadata(ds.getIdentifier(),-1);
                dataset = true;
                datasetId = ds.getIdentifier();
            }
            if(metadata != null) {
                //get template name
                final String templateName;
                if (datasetId != null) {
                    templateName = datasetBusiness.getTemplate(datasetId, type);
                } else {
                    templateName = dataBusiness.getTemplate(dataName, type);
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
                    datasetBusiness.updateMetadata(ds.getIdentifier(), -1, metadata);
                } else {
                    dataBusiness.updateMetadata(provider, dataName, -1, metadata);
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
            final DefaultMetadata metadata = datasetBusiness.getMetadata(identifier, -1);
            if(metadata != null) {
                //get template name
                final String templateName = datasetBusiness.getTemplate(identifier, type);
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
                datasetBusiness.updateMetadata(identifier, -1, metadata);
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
        final List<org.constellation.engine.register.Data> datas;
        try {
            datas = dataBusiness.searchOnMetadata(search);
            if(datas != null && !datas.isEmpty()){
                for (org.constellation.engine.register.Data data : datas) {
                    final QName name = new QName(data.getNamespace(), data.getName());
                    final DataBrief db = dataBusiness.getDataBrief(name, data.getProvider());
                    briefs.add(db);
                }
            }else {
                //@TODO remove this else when we will add data metadata in cstl db after SIS implementation.
                final List<DataSetBrief> datasetBriefs = new ArrayList<>();
                final List<Dataset> datasetList = datasetBusiness.searchOnMetadata(search);
                for (final Dataset ds : datasetList) {
                    final DataSetBrief dsb = buildDatsetBrief(ds,domainId,null);
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
     * Build {@link DataSetBrief} instance from {@link Dataset} and domain id.
     * @param dataset given dataset object.
     * @param domainId given domain id.
     * @return {@link DataSetBrief} built from the given dataset.
     */
    private DataSetBrief buildDatsetBrief(final Dataset dataset,final int domainId, final List<DataBrief> children){
        final Integer dataSetId = dataset.getId();
        final Optional<CstlUser> optUser = userRepository.findById(dataset.getOwner());
        String owner = null;
        if(optUser!=null && optUser.isPresent()){
            final CstlUser user = optUser.get();
            if (user != null) {
                owner = user.getLogin();
            }
        }
        final DataSetBrief dsb;
        if(children==null){
            final List<DataBrief> dataBriefList = dataBusiness.getDataBriefsFromDatasetId(dataSetId);
            String type = null;
            if(dataBriefList!=null && !dataBriefList.isEmpty()){
                type = dataBriefList.get(0).getType();
            }
            dsb = new DataSetBrief(dataset.getId(),dataset.getIdentifier(), type, owner, dataBriefList,dataset.getDate());
        }else {
            String type = null;
            if(!children.isEmpty()){
                type = children.get(0).getType();
            }
            dsb = new DataSetBrief(dataset.getId(),dataset.getIdentifier(), type, owner, children, dataset.getDate());
        }

        try{
            final Node nodeMetadata = datasetBusiness.getMetadataNode(dataset.getIdentifier(),domainId);
            if(nodeMetadata!=null){
                //@TODO fill the DataSetBrief properties for keywords, abstract and dateStamp
            }
        }catch(Exception ex){
            LOGGER.log(Level.WARNING,ex.getLocalizedMessage(),ex);
        }
        return dsb;
    }

    /**
     * Save metadata.
     *
     * @param values
     * @return {@link javax.ws.rs.core.Response}
     * @throws org.opengis.referencing.operation.TransformException with code 200.
     */
    @POST
    @Path("metadata")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response saveMetadata(final ParameterValues values) throws ConfigurationException {
        final String providerId          = values.getValues().get("providerId");
        final String dataType            = values.getValues().get("dataType");
        final String mergeWithUploadedMD = values.getValues().get("mergeWithUploadedMD");
        DefaultMetadata uploadedMetadata;
        try{
            uploadedMetadata = datasetBusiness.getMetadata(providerId,-1);
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
     * @param dataId Data identifier 
     * @return
     */
    @POST
    @Path("pyramid/createconform/{providerId}/{dataId}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response createTiledProvider(
            @PathParam("providerId") final String providerId, @PathParam("dataId") final String dataId, @Context HttpServletRequest req) {

        final Optional<CstlUser> cstlUser = userRepository.findOne(req.getUserPrincipal().getName());

        if (!cstlUser.isPresent()) {
            throw new ConstellationException("operation not allowed without login");
        }
        //get data
        final DataProvider inProvider = DataProviders.getInstance().getProvider(providerId);
        if (inProvider == null) {
            return Response.ok("Provider " + providerId + " does not exist").status(400).build();
        }
        final Data inData = inProvider.get(new DefaultName(dataId));
        if (inData == null) {
            return Response.ok("Data " + dataId + " does not exist in provider " + providerId).status(400).build();
        }

        final Object origin = inData.getOrigin();
        if(! (origin instanceof CoverageReference)){
            return Response.ok("Cannot create pyramid for no coverage data!").build();
        }

        Envelope dataEnv;
        try {
            //use data crs
            dataEnv = inData.getEnvelope();
        } catch (DataStoreException ex) {
            Providers.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            return Response.ok("Failed to extract envelope for data " + dataId + ". " + ex.getMessage()).status(500).build();
        }

        //calculate pyramid scale levels
        final CoverageReference inRef = (CoverageReference) inData.getOrigin();
        final GeneralGridGeometry gg;
        try {
            final GridCoverageReader reader = inRef.acquireReader();
            gg = reader.getGridGeometry(inRef.getImageIndex());

        } catch (CoverageStoreException ex) {
            Providers.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            return Response.ok("Failed to extract grid geometry for data " + dataId + ". " + ex.getMessage()).status(500).build();
        }

        //create the output folder for pyramid
        PyramidalCoverageReference outRef;
        final String pyramidProviderId = CONFORM_PREFIX + UUID.randomUUID().toString();
            //create the output provider
            final DataProvider outProvider;
            try {
                //create the output folder for pyramid
                final File providerDirectory = ConfigDirectory.getDataIntegratedDirectory(providerId);
                final File pyramidDirectory = new File(providerDirectory, pyramidProviderId);
                pyramidDirectory.mkdirs();

                final DataProviderFactory factory = DataProviders.getInstance().getFactory("coverage-store");
                final ParameterValueGroup pparams = factory.getProviderDescriptor().createValue();
                ParametersExt.getOrCreateValue(pparams, ProviderParameters.SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue(pyramidProviderId);
                ParametersExt.getOrCreateValue(pparams, ProviderParameters.SOURCE_TYPE_DESCRIPTOR.getName().getCode()).setValue("coverage-store");
                final ParameterValueGroup choiceparams = ParametersExt.getOrCreateGroup(pparams, factory.getStoreDescriptor().getName().getCode());
                final ParameterValueGroup xmlpyramidparams = ParametersExt.getOrCreateGroup(choiceparams, XMLCoverageStoreFactory.PARAMETERS_DESCRIPTOR.getName().getCode());
                ParametersExt.getOrCreateValue(xmlpyramidparams, XMLCoverageStoreFactory.PATH.getName().getCode()).setValue(pyramidDirectory.toURL());
                ParametersExt.getOrCreateValue(xmlpyramidparams, XMLCoverageStoreFactory.NAMESPACE.getName().getCode()).setValue("no namespace");
                outProvider = DataProviders.getInstance().createProvider(pyramidProviderId, factory, pparams);

                //create the output pyramid coverage reference
                CoverageStore outStore = (CoverageStore) outProvider.getMainStore();
                Name name = new DefaultName(dataId);
                try {
                    name = ((XMLCoverageReference) outStore.create(name)).getName();
                } catch (DataStoreException ex) {
                    Providers.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
                    return Response.ok("Failed to create pyramid layer " + ex.getMessage()).status(500).build();
                }

                // Update the parent attribute of the created provider
                providerBusiness.updateParent(outProvider.getId(), providerId);

                //update the DataRecord objects
                //this produces an update event which will create the DataRecord
                outProvider.reload();

                outStore = (CoverageStore) outProvider.getMainStore();
                outRef = (XMLCoverageReference) outStore.getCoverageReference(name);

                //set data as GEOPHYSIC
                outRef.setPackMode(ViewType.GEOPHYSICS);
                dataBusiness.updateDataRendered(new QName(name.getNamespaceURI(), name.getLocalPart()), outProvider.getId(), false);

            } catch (Exception ex) {
                Providers.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
                return Response.ok("Failed to create pyramid provider " + ex.getMessage()).status(500).build();
            }

        //get the fill value for no data
        try {
            final GridCoverageReader reader = inRef.acquireReader();
            final List<GridSampleDimension> sampleDimensions = reader.getSampleDimensions(inRef.getImageIndex());
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
            Providers.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            return Response.ok("Failed to extract no-data values for resampling " + ex.getMessage()).status(500).build();
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
            Providers.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            return Response.ok("Failed to create pyramid and mosaics in store " + ex.getMessage()).status(500).build();
        }

        //prepare process
        final ProcessDescriptor desc;
        try {
            desc = ProcessFinder.getProcessDescriptor("coverage", "coveragepyramid");
        } catch (NoSuchIdentifierException ex) {
            Providers.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            return Response.ok("Process coverage.coveragepyramid not found " + ex.getMessage()).status(500).build();
        }

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

            TaskParameter taskParameter = new TaskParameter();
            taskParameter.setProcessAuthority(desc.getIdentifier().getAuthority().toString());
            taskParameter.setProcessCode(desc.getIdentifier().getCode());
            taskParameter.setDate(System.currentTimeMillis());
            taskParameter.setInputs(ParamUtilities.writeParameter(input));
            taskParameter.setOwner(cstlUser.get().getId());
            taskParameter.setName("Create conform pyramid for " + providerId + ":" + dataId+" | "+System.currentTimeMillis());
            taskParameter.setType("INTERNAL");
            taskParameter = processBusiness.addTaskParameter(taskParameter);
            processBusiness.runProcess("Create conform pyramid for " + providerId + ":" + dataId, p, taskParameter.getId(), cstlUser.get().getId());
        } catch ( IOException e) {
            LOGGER.log(Level.WARNING, "Unable to run pyramid process on scheduler");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        final ProviderData ref = new ProviderData(pyramidProviderId, dataId);
        return Response.ok(ref).status(202).build();
    }

    /**
     * Generates a pyramid on a data in the given provider, create and return this new provider.
     *
     * N.B : It creates a styled pyramid, which can be used for display purposes, but not for analysis.
     *
     * TODO : Input pyramid parameters should contain an horizontal envelope, not an upper-left point. Upper-left point
     * is not sufficient to determine a custom pyramid zone. Unused.
     *
     * @param dataId Data id
     * @param provider Provider identifier of the data to tile.
     * @param dataName Data identifier
     * @param params PyramidParams
     * @return
     */
    @POST
    @Path("pyramid/create/{dataId}/{provider}/{dataName}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response createTiledProvider(@PathParam("dataId") final Integer dataId,
                                        @PathParam("provider") final String provider,
                                        @PathParam("dataName") final String dataName,
                                        final PyramidParams params,
                                        @Context HttpServletRequest req) {
        final Optional<CstlUser> cstlUser = userRepository.findOne(req.getUserPrincipal().getName());

        if (!cstlUser.isPresent()) {
            throw new ConstellationException("operation not allowed without login");
        }
        String tileFormat = params.getTileFormat();
        String crs = params.getCrs();
        double[] scales = params.getScales();
        Double upperCornerX = params.getUpperCornerX();
        Double upperCornerY = params.getUpperCornerY();
        
        //get data
        final DataProvider inProvider = DataProviders.getInstance().getProvider(provider);
        if(inProvider==null){
            return Response.ok("Provider "+provider+" does not exist").status(400).build();
        }
        final Data inData = inProvider.get(new DefaultName(dataName));
        if(inData==null){
            return Response.ok("Data "+dataName+" does not exist in provider "+provider).status(400).build();
        }
        
        Envelope dataEnv;
        try {
            //use data crs
            dataEnv = inData.getEnvelope();
        } catch (DataStoreException ex) {
            return Response.ok("Failed to extract envelope for data "+dataName).status(500).build();
        }

        //get tile format 
        if(tileFormat==null || tileFormat.isEmpty()) {
            tileFormat = "PNG";
        }

        MutableStyle style = null;
        try {
            final org.constellation.engine.register.Data data = dataBusiness.findById(dataId);
            if(data != null){
                List<org.constellation.engine.register.Style> list =  styleRepository.findByData(data);
                if(list != null && !list.isEmpty()){
                    final org.constellation.engine.register.Style s = list.get(0);
                    style = styleBusiness.getStyle("sld",s.getName());
                }
            }
        }catch(Exception ex){
            LOGGER.log(Level.WARNING,ex.getLocalizedMessage(),ex);
        }
        
        //get pyramid CRS, we force longiude first on the pyramids
        // WMTS is made for display like WMS, so longitude is expected to be on the X axis.
        // Note : this is not writen in the spec.
        final CoordinateReferenceSystem coordsys;
        if(crs == null || crs.isEmpty()){
            try {
                coordsys = ReferencingUtilities.setLongitudeFirst(dataEnv.getCoordinateReferenceSystem());
            } catch (FactoryException ex) {
                LOGGER.log(Level.WARNING, "Failed to invert axes (longitude first) on CRS", ex);
                return Response.ok("Failed to invert axes (longitude first) on CRS : "+ex.getMessage()).status(500).build();
            }
            try {
                //reproject data envelope
                dataEnv = CRS.transform(dataEnv, coordsys);
            } catch (TransformException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
                return Response.ok("Could not transform data envelope to crs "+crs).status(400).build();
            }
        }else{
            try {
                coordsys = CRS.decode(crs,true);
            } catch (FactoryException ex) {
                LOGGER.log(Level.WARNING, "Invalid CRS code : "+crs, ex);
                return Response.ok("Invalid CRS code : "+crs).status(400).build();
            }
            try {
                //reproject data envelope
                dataEnv = CRS.transform(dataEnv, coordsys);
            } catch (TransformException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
                return Response.ok("Could not transform data envelope to crs "+crs).status(400).build();
            }
        }
        
        //get pyramid scale levels
        if(scales==null || scales.length==0){
            //TODO a way to work on all cases, a default values ?
            return Response.ok("Scale values missing").status(400).build();
        }

        //get upper corner
        if(upperCornerX==null || upperCornerY==null){
            upperCornerX = dataEnv.getMinimum(0);
            upperCornerY = dataEnv.getMaximum(1);
        }

        //create the output folder for pyramid
        PyramidalCoverageReference outRef;
        String pyramidProviderId = RENDERED_PREFIX + UUID.randomUUID().toString();
        final File providerDirectory = ConfigDirectory.getDataIntegratedDirectory(provider);
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
            ParametersExt.getOrCreateValue(xmlpyramidparams, XMLCoverageStoreFactory.PATH.getName().getCode()).setValue(pyramidDirectory.toURL());
            ParametersExt.getOrCreateValue(xmlpyramidparams, XMLCoverageStoreFactory.NAMESPACE.getName().getCode()).setValue("no namespace");
            outProvider = DataProviders.getInstance().createProvider(pyramidProviderId, factory, pparams);
            // Update the parent attribute of the created provider
            providerBusiness.updateParent(outProvider.getId(), provider);
        } catch (Exception ex) {
            Providers.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            return Response.status(500).entity("Failed to create pyramid provider " + ex.getMessage()).build();
        }

        try {
            //create the output pyramid coverage reference
            CoverageStore pyramidStore = (CoverageStore) outProvider.getMainStore();
            outRef = (XMLCoverageReference) pyramidStore.create(new DefaultName(dataName));
            outRef.setPackMode(ViewType.RENDERED);
            ((XMLCoverageReference) outRef).setPreferredFormat(tileFormat);
            //this produces an update event which will create the DataRecord
            outProvider.reload();

            pyramidStore = (CoverageStore) outProvider.getMainStore();
            outRef = (XMLCoverageReference) pyramidStore.getCoverageReference(outRef.getName());

            //set data as RENDERED
            final QName outDataQName = new QName(outRef.getName().getNamespaceURI(), outRef.getName().getLocalPart());
            dataBusiness.updateDataRendered(outDataQName, outProvider.getId(), true);

        } catch (Exception ex) {
            Providers.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            return Response.status(500).entity("Failed to create pyramid layer " + ex.getMessage()).build();
        }


        //prepare the pyramid and mosaics
        final int tileSize = 256;
        final Dimension tileDim = new Dimension(tileSize, tileSize);
        try {
            CoverageUtilities.getOrCreatePyramid(outRef, dataEnv, tileDim, scales);
        } catch (Exception ex) {
            Providers.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            return Response.status(500).entity("Failed to initialize output pyramid. Cause : " + ex.getMessage()).build();
        }

        /**
         * TODO : Before launching pyramid update, we should ensure user has not asked for tiles which already exists.
         */
        final MapContext context = MapBuilder.createContext();
        try {
            //if style is null, a default style will be used in maplayer.
            context.items().add(inData.getMapLayer(style, null));
        } catch (PortrayalException ex) {
            Providers.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            return Response.status(500).entity("Failed to create map context layer for data " + ex.getMessage()).build();
        }
        final ProcessDescriptor desc;
        try {
            desc = ProcessFinder.getProcessDescriptor("engine2d", "mapcontextpyramid");

            final ParameterValueGroup input = desc.getInputDescriptor().createValue();
            input.parameter("context").setValue(context);
            input.parameter("extent").setValue(dataEnv);
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
            taskParameter.setName("Styled pyramid " + crs + " for " + provider + ":" + dataName+" | "+System.currentTimeMillis());
            taskParameter.setType("INTERNAL");
            taskParameter = processBusiness.addTaskParameter(taskParameter);
            //add task in scheduler
            processBusiness.runProcess("Create pyramid " + crs + " for " + provider + ":" + dataName, p, taskParameter.getId(), cstlUser.get().getId());


        } catch (NoSuchIdentifierException ex) {
            Providers.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            return Response.status(500).entity("Process engine2d.mapcontextpyramid not found " + ex.getMessage()).build();
        } catch (ConstellationException e) {
            LOGGER.log(Level.WARNING, "Unable to run pyramid process on scheduler");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (Exception ex) {
            Providers.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            return Response.status(500).entity("Data cannot be tiled. " + ex.getMessage()).build();
        }

        final ProviderData ref = new ProviderData(pyramidProviderId, dataName);
        return Response.ok(ref).status(202).build();
    }

    @POST
    @Path("crs/isgeographic/{epsgCode}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response isGeographic(@PathParam("epsgCode") final String epsgCode){
        
        CoordinateReferenceSystem crs;
        try {
            crs = CRS.decode(epsgCode);
        } catch (FactoryException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            crs = null;
        }
        return Response.ok(crs instanceof GeographicCRS).build();
    }

    @GET
    @Path("pyramid/bestscales/{providerId}/{dataId}/{crs}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response findBestScales(@PathParam("providerId") final String providerId,
                                   @PathParam("dataId") final String dataId,
                                   @PathParam("crs") final String crs){

        //get data
        final DataProvider inProvider = DataProviders.getInstance().getProvider(providerId);
        if(inProvider==null){
            return Response.ok("Provider "+providerId+" does not exist").status(400).build();
        }
        final Data inData = inProvider.get(new DefaultName(dataId));
        if(inData==null){
            return Response.ok("Data "+dataId+" does not exist in provider "+providerId).status(400).build();
        }

        Envelope dataEnv;
        try {
            //use data crs
            dataEnv = inData.getEnvelope();
        } catch (DataStoreException ex) {
            return Response.ok("Failed to extract envelope for data "+dataId).status(500).build();
        }
        final Object origin = inData.getOrigin();
        final Object[] scales;

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
            final CoverageReference inRef = (CoverageReference) inData.getOrigin();
            final GeneralGridGeometry gg;
            try{
                final GridCoverageReader reader = inRef.acquireReader();
                gg = reader.getGridGeometry(inRef.getImageIndex());

            } catch(CoverageStoreException ex) {
                Providers.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
                return Response.ok("Failed to extract grid geometry for data "+dataId+". "+ex.getMessage()).status(500).build();
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
            scales = new Object[scalesList.size()];
            for(int i=0;i<scales.length;i++) scales[i] = scalesList.get(i);

        }else{
            //featurecollection or anything else, scales can not be defined accurately.
            //vectors have virtually an unlimited resolution
            //we build scales, to obtain 8 levels, this should be enough for a default case
            final double geospanX = env.getSpan(0);
            final int tileSize = 256;
            scales = new Object[8];
            scales[0] = geospanX / tileSize;
            for(int i=1;i<scales.length;i++){
                scales[i] = ((Double)scales[i-1]) / 2.0;
            }

        }
        final String scalesStr = StringUtilities.toCommaSeparatedValues(scales);
        return Response.ok(new StringList(Collections.singleton(scalesStr))).build();
    }
    
    /**
     * Send an ArrayList which contains coverage list from a file
     *
     * @param value
     * @return an {@link java.util.ArrayList}
     */
    @POST
    @Path("coverage/list/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getCoverageList(final SimpleValue value) {
        final DataProvider provider = DataProviders.getInstance().getProvider(value.getValue());
        final Set<Name> nameSet = provider.getKeys();
        final List<String> names = new ArrayList<>();
        for (Name n : nameSet) {
            names.add(n.getLocalPart());
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
            final List<org.constellation.engine.register.Data> datas = providerBusiness.getDatasFromProviderId(provider.getId());
            for (final org.constellation.engine.register.Data data : datas) {
                if (type != null && !type.equalsIgnoreCase(data.getType())) {
                    continue;
                }

                if (data.isVisible()) {
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
            final List<org.constellation.engine.register.Data> datas = providerBusiness.getDatasFromProviderId(provider.getId());
            for (final org.constellation.engine.register.Data data : datas) {
                if (data.isVisible()) {
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
        final List<org.constellation.engine.register.Data> datas;
        datas = providerBusiness.getDatasFromProviderId(prov.getId());
        for (final org.constellation.engine.register.Data data : datas) {
            if (data.isVisible()) {
                final QName name = new QName(data.getNamespace(), data.getName());
                final DataBrief db = dataBusiness.getDataBrief(name, providerId);
                briefs.add(db);
            }
        }
        return Response.ok(briefs).build();
    }

    @GET
    @Path("/list/top")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response getTopDataList(@PathParam("domainId") int domainId) {
        return getTopDataList(domainId, null);
    }

    @GET
     @Path("/list/top/{type}")
     @Produces({MediaType.APPLICATION_JSON})
     @Consumes({MediaType.APPLICATION_JSON})
     public Response getTopDataList(@PathParam("domainId") int domainId, @PathParam("type") String type) {
        final List<DataBrief> briefs = new ArrayList<>();

        final List<Integer> providerIds = providerBusiness.getProviderIdsForDomain(domainId);
        for (final Integer providerId : providerIds) {
            final Provider provider = providerBusiness.getProvider(providerId);
            final String parent = provider.getParent();
            if (parent != null && !parent.isEmpty()) {
                // Remove all providers that have a parent
                continue;
            }
            final List<org.constellation.engine.register.Data> datas;
            datas = providerBusiness.getDatasFromProviderId(provider.getId());
            for (final org.constellation.engine.register.Data data : datas) {
                if (type != null && !data.getType().equals(type)) {
                    continue;
                }

                if (data.isVisible()) {
                    final QName name = new QName(data.getNamespace(), data.getName());
                    final DataBrief db = dataBusiness.getDataBrief(name, providerId);
                    briefs.add(db);
                }
            }
        }

        return Response.ok(briefs).build();
    }

    @GET
    @Path("/list/published/{published}/data")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response getPublishedDataList(@PathParam("domainId") int domainId, @PathParam("published") boolean published) {
        final List<DataBrief> briefs = new ArrayList<>();
        final List<Integer> providerIds = providerBusiness.getProviderIdsForDomain(domainId);
        for (final Integer providerId : providerIds) {
            final Provider provider = providerBusiness.getProvider(providerId);
            final String parent = provider.getParent();
            if (parent != null && !parent.isEmpty()) {
                // Skip all providers that have a parent ie : skip for tiled data to avoid duplication
                continue;
            }
            final List<org.constellation.engine.register.Data> datas;
            datas = providerBusiness.getDatasFromProviderId(provider.getId());
            for (final org.constellation.engine.register.Data data : datas) {
                if (data.isVisible()) {
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
    @Path("/list/published/{published}/dataset")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response getPublishedDatasetList(@PathParam("domainId") int domainId, @PathParam("published") boolean published) {
        final List<DataSetBrief> datasetBriefs = new ArrayList<>();
        final List<Dataset> datasets = datasetBusiness.getAllDataset();
        if(datasets!=null){
            for(final Dataset ds : datasets){
                final List<org.constellation.engine.register.Data> dataList = dataBusiness.findByDatasetId(ds.getId());
                final List<DataBrief> briefs = new ArrayList<>();
                for (final org.constellation.engine.register.Data data : dataList) {
                    if (data.isVisible()) {
                        final QName name = new QName(data.getNamespace(), data.getName());
                        final DataBrief db = dataBusiness.getDataBrief(name, data.getProvider());
                        if ((published && (db.getTargetService() == null || db.getTargetService().size() == 0)) ||
                                (!published && db.getTargetService() != null && db.getTargetService().size() > 0)) {
                            continue;
                        }
                        briefs.add(db);
                    }
                }
                if(briefs.isEmpty()){
                    continue;
                }
                final DataSetBrief dsb = buildDatsetBrief(ds,domainId,briefs);
                datasetBriefs.add(dsb);
            }
        }
        return Response.ok(datasetBriefs).build();
    }

    @GET
    @Path("/list/observation/{sensorable}/data")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response getSensorableDataList(@PathParam("domainId") int domainId, @PathParam("sensorable") boolean sensorable) {
        final List<DataBrief> briefs = new ArrayList<>();

        final List<Integer> providerIds = providerBusiness.getProviderIdsForDomain(domainId);
        for (final Integer providerId : providerIds) {
            final Provider provider = providerBusiness.getProvider(providerId);
            final String parent = provider.getParent();
            if (parent != null && !parent.isEmpty()) {
                // Remove all providers that have a parent
                continue;
            }
            final List<org.constellation.engine.register.Data> datas;
            datas = providerBusiness.getDatasFromProviderId(provider.getId());
            for (final org.constellation.engine.register.Data data : datas) {

                if (data.isVisible()) {
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
    @Path("/list/observation/{sensorable}/dataset")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response getSensorableDatasetList(@PathParam("domainId") int domainId, @PathParam("sensorable") boolean sensorable) {
        final List<DataSetBrief> datasetBriefs = new ArrayList<>();
        final List<Dataset> datasets = datasetBusiness.getAllDataset();
        if(datasets!=null){
            for(final Dataset ds : datasets){
                final List<org.constellation.engine.register.Data> dataList = dataBusiness.findByDatasetId(ds.getId());
                final List<DataBrief> briefs = new ArrayList<>();
                for (final org.constellation.engine.register.Data data : dataList) {
                    if (data.isVisible()) {
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
                final DataSetBrief dsb = buildDatsetBrief(ds,domainId,briefs);
                datasetBriefs.add(dsb);
            }
        }
        return Response.ok(datasetBriefs).build();
    }

    @DELETE
    @Path("{providerid}/{dataid}")
    public Response deleteData(@PathParam("providerid") String providerid, @PathParam("dataid") String dataid) {
        dataBusiness.deleteData(new QName("", dataid), providerid);
        return Response.ok().type(MediaType.TEXT_PLAIN_TYPE).build();
    }

    @POST
    @Path("{providerid}/{dataid}/visible")
    public Response visibleData(@PathParam("providerid") String providerid, @PathParam("dataid") String dataid) throws ConfigurationException {

        dataBusiness.updateDataVisibility(new QName("", dataid), providerid, true);
        return Response.ok().type(MediaType.TEXT_PLAIN_TYPE).build();
    }

    @POST
    @Path("{providerid}/{dataid}/hidden")
    public Response hideData(@PathParam("providerid") String providerid, @PathParam("dataid") String dataid, final SimpleValue dataNmsp) throws ConfigurationException {
        final QName dataName;
        if (dataNmsp != null) {
            dataName = new QName(dataNmsp.getValue(), dataid);
        } else {
            dataName = new QName(dataid);
        }
        dataBusiness.updateDataVisibility(dataName, providerid, false);
        return Response.ok().type(MediaType.TEXT_PLAIN_TYPE).build();
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
    public Response getIsoMetadata(final @PathParam("providerId") String providerId, final @PathParam("dataId") String dataId) {
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
            DefaultMetadata metadata = dataBusiness.loadIsoDataMetadata(dataId);
            if (metadata == null) {
                //try to get dataset metadata.
                final Dataset dataset = dataBusiness.getDatasetForData(dataId);
                if (dataset != null) {
                    metadata = datasetBusiness.getMetadata(dataset.getIdentifier(),-1);
                }
            }
            if (metadata != null) {
                metadata.prune();
                final String xmlStr = XML.marshal(metadata);
                return Response.ok(xmlStr, MediaType.APPLICATION_XML_TYPE)
                        .header("Content-Disposition", "attachment; filename=\"" + providerId + ".xml\"").build();
            }
        }catch(Exception ex){
            LOGGER.log(Level.WARNING, "Failed to get xml metadata for data with provider identifier "+providerId+" dataId = "+dataId,ex);
        }
        return Response.ok("<empty></empty>", MediaType.APPLICATION_XML_TYPE)
                .header("Content-Disposition", "attachment; filename=\"" + providerId + ".xml\"").build();
    }

    @GET
    @Path("metadata/{providerId}/{dataId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getMetadata(final @PathParam("providerId") String providerId, final @PathParam("dataId") String dataId) {

        //get reader
        final DataProvider provider = DataProviders.getInstance().getProvider(providerId);
        final Data layer = provider.get(new DefaultName(dataId));
        final Object origin = layer.getOrigin();
        //generate DataInformation


        DefaultMetadata metadata = null;
        // for now assume that providerID == datasetID
        final Dataset datasetFromDB = datasetBusiness.getDataset(providerId);
        final String metadataFromDB = datasetFromDB.getMetadataIso();
        final MarshallerPool pool = CSWMarshallerPool.getInstance();
        try {
            final Unmarshaller m = pool.acquireUnmarshaller();
            if (metadataFromDB != null) {
                final InputStream is = new ByteArrayInputStream(metadataFromDB.getBytes());
                metadata = (DefaultMetadata) m.unmarshal(is);
            }
            pool.recycle(m);
        } catch (JAXBException ex) {
            throw new ConstellationException(ex);
        }
        DataInformation information = new DataInformation();
        if (layer instanceof FeatureData) {
            final ArrayList<SimplyMetadataTreeNode> meta = MetadataUtilities.getVectorDataInformation(metadata);
            information.setFileMetadata(meta);
        } else if (layer instanceof CoverageData) {
            final Map<String, CoverageMetadataBean> nameSpatialMetadataMap = new HashMap<>(0);
            final CoverageReference fcr = (CoverageReference) origin;
            try {
                final GridCoverageReader reader = fcr.acquireReader();
                information = MetadataUtilities.getRasterDataInformation(reader, metadata, "COVERAGE");
                final QName name = new QName(layer.getName().getNamespaceURI(), layer.getName().getLocalPart());
                final CoverageMetadataBean cmb = dataBusiness.loadDataMetadata(providerId, name, GenericDatabaseMarshallerPool.getInstance());
                nameSpatialMetadataMap.put(dataId, cmb);
                information.setCoveragesMetadata(nameSpatialMetadataMap);
                fcr.recycle(reader);
            } catch (CoverageStoreException | NoSuchIdentifierException | ProcessException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
                return Response.status(500).entity(ex.getLocalizedMessage()).build();
            }
        } else {
            LOGGER.log(Level.INFO, "Type unknown to found metadata");
            return Response.status(500).entity("Type unknown to found metadata").build();
        }

        information.setName(dataId);
        return Response.ok(information).build();
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
    
    @POST
    @Path("findDataType")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public SelectedExtension findDataType(final ParameterValues values) {
        final String filePath     = values.getValues().get("filePath");
        final String extension    = values.getValues().get("extension");
        final String selectedType = values.getValues().get("dataType");
        final SelectedExtension result = findDataType(filePath,extension,selectedType);
        return result;
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
    public Response linkDataToSensor(final @PathParam("providerId") String providerId, final @PathParam("dataId") String dataId, final @PathParam("sensorId") String sensorId, final SimpleValue value) {
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
     * @return
     * @throws DataStoreException
     */
    @GET
    @Path("{id}/vector/columns")
    public Response getVectorDataColumns(final @PathParam("id") int id) throws DataStoreException {
        return Response.ok(dataBusiness.getVectorDataColumns(id)).build();
    }

    ///////////////////////////////////////
    //  UTILITIES
    //////////////////////////////////////

    private static PyramidalCoverageReference getPyramidLayer(final CoverageStore store, final String dataId) throws DataStoreException {
        final Set<Name> names = ((CoverageStore) store).getNames();
        if (names != null && !names.isEmpty()) {
            for (Name n : names) {
                if (n.getLocalPart().equals(dataId)) {
                    final CoverageReference tmpRef = ((CoverageStore) store).getCoverageReference(n);
                    if (tmpRef instanceof PyramidalCoverageReference) {
                        return (PyramidalCoverageReference) tmpRef;
                    }
                }
            }
        }
        return null;
    }
}


