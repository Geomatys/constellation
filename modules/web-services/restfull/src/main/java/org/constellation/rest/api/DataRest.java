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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
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
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.sis.geometry.GeneralDirectPosition;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.storage.DataStore;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.Locales;
import org.apache.sis.util.iso.Types;
import org.apache.sis.util.logging.Logging;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.admin.ConfigurationBusiness;
import org.constellation.admin.DataBusiness;
import org.constellation.admin.DatasetBusiness;
import org.constellation.admin.ProviderBusiness;
import org.constellation.admin.SensorBusiness;
import org.constellation.admin.exception.ConstellationException;
import org.constellation.configuration.*;
import org.constellation.coverage.PyramidCoverageHelper;
import org.constellation.coverage.PyramidCoverageProcessListener;
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
import org.constellation.engine.register.repository.DataRepository;
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
import org.constellation.scheduler.CstlScheduler;
import org.constellation.security.SecurityManagerHolder;
import org.constellation.util.SimplyMetadataTreeNode;
import org.constellation.util.Util;
import org.constellation.utils.CstlMetadatas;
import org.constellation.utils.GeotoolkitFileExtensionAvailable;
import org.constellation.utils.ISOMarshallerPool;
import org.constellation.utils.MetadataFeeder;
import org.constellation.utils.MetadataUtilities;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.coverage.CoverageStore;
import org.geotoolkit.coverage.CoverageUtilities;
import org.geotoolkit.coverage.GridSampleDimension;
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
import org.geotoolkit.image.interpolation.InterpolationCase;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.observation.ObservationStore;
import org.geotoolkit.parameter.ParametersExt;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.process.ProcessListener;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.ReferencingUtilities;
import org.geotoolkit.sos.netcdf.NetCDFExtractor;
import org.geotoolkit.storage.DataFileStore;
import org.geotoolkit.util.FileUtilities;
import org.geotoolkit.util.StringUtilities;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.opengis.feature.PropertyType;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.citation.DateType;
import org.opengis.metadata.citation.Role;
import org.opengis.metadata.constraint.Classification;
import org.opengis.metadata.identification.TopicCategory;
import org.opengis.metadata.maintenance.MaintenanceFrequency;
import org.opengis.metadata.spatial.GeometricObjectType;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import org.opengis.util.NoSuchIdentifierException;
import org.quartz.SchedulerException;

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

    @Inject
    private UserRepository userRepository;
    
    @Inject
    private DataRepository dataRepository;

    @Inject
    private DataBusiness dataBusiness;
    
    @Inject
    private DatasetBusiness datasetBusiness;

    @Inject
    private ProviderBusiness providerBusiness;

    @Inject
    private SensorBusiness sensorBusiness;


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
        final MetadataLists mdList = new MetadataLists();

        //for role codes
        final List<String> roleCodes = new LinkedList<>();
        for (final Role role : Role.values()) {
            final String standardName = Types.getStandardName(role.getClass());
            final String code = role.identifier()!=null?role.identifier():role.name();
            final String codeListName = standardName+"."+code;
            roleCodes.add(codeListName);
        }
        Collections.sort(roleCodes);
        mdList.setRoleCodes(roleCodes);

        //for locale codes
        final List<String> localeCodes = new LinkedList<>();
        for (final Locale locale : Locales.ALL.getAvailableLanguages()) {
            localeCodes.add("LanguageCode."+locale.getISO3Language());
        }
        Collections.sort(localeCodes);
        mdList.setLocaleCodes(localeCodes);

        //for topic category codes
        final List<String> topicCategoryCodes = new LinkedList<>();
        for (final TopicCategory tc : TopicCategory.values()) {
            final String standardName = Types.getStandardName(tc.getClass());
            final String code = tc.identifier()!=null? tc.identifier(): tc.name();
            final String codeListName = standardName+"."+code;
            topicCategoryCodes.add(codeListName);
        }
        Collections.sort(topicCategoryCodes);
        mdList.setTopicCategoryCodes(topicCategoryCodes);

        //for date type codes
        final List<String> dateTypeCodes = new LinkedList<>();
        for (final DateType dateType : DateType.values()) {
            final String standardName = Types.getStandardName(dateType.getClass());
            final String code = dateType.identifier()!=null? dateType.identifier(): dateType.name();
            final String codeListName = standardName+"."+code;
            dateTypeCodes.add(codeListName);
        }
        Collections.sort(dateTypeCodes);
        mdList.setDateTypeCodes(dateTypeCodes);

        //for maintenanceFrequency codes
        final List<String> maintenanceFrequencyCodes = new LinkedList<>();
        for (final MaintenanceFrequency cl : MaintenanceFrequency.values()) {
            final String standardName = Types.getStandardName(cl.getClass());
            final String code = cl.identifier()!=null? cl.identifier(): cl.name();
            final String codeListName = standardName+"."+code;
            maintenanceFrequencyCodes.add(codeListName);
        }
        Collections.sort(maintenanceFrequencyCodes);
        mdList.setMaintenanceFrequencyCodes(maintenanceFrequencyCodes);

        //for GeometricObjectType codes
        final List<String> geometricObjectTypeCodes = new LinkedList<>();
        for (final GeometricObjectType got : GeometricObjectType.values()) {
            final String standardName = Types.getStandardName(got.getClass());
            final String code = got.identifier()!=null? got.identifier(): got.name();
            final String codeListName = standardName+"."+code;
            geometricObjectTypeCodes.add(codeListName);
        }
        Collections.sort(geometricObjectTypeCodes);
        mdList.setGeometricObjectTypeCodes(geometricObjectTypeCodes);

        //for Classification codes
        final List<String> classificationCodes = new LinkedList<>();
        for (final Classification cl : Classification.values()) {
            final String standardName = Types.getStandardName(cl.getClass());
            final String code = cl.identifier()!=null? cl.identifier(): cl.name();
            final String codeListName = standardName+"."+code;
            classificationCodes.add(codeListName);
        }
        Collections.sort(classificationCodes);
        mdList.setClassificationCodes(classificationCodes);

        // for characterSet codes
        final List<String> characterSetCodes = new LinkedList<>();
        final Set<String> keys = Charset.availableCharsets().keySet();
        final List<String> keep = Arrays.asList("UTF-8","UTF-16","UTF-32",
                "ISO-8859-1","ISO-8859-13","ISO-8859-15",
                "ISO-8859-2","ISO-8859-3","ISO-8859-4",
                "ISO-8859-5","ISO-8859-6","ISO-8859-7",
                "ISO-8859-8","ISO-8859-9","Shift_JIS",
                "EUC-JP","EUC-KR","US-ASCII","Big5","GB2312");
        keep.retainAll(keys);
        for (final String c : keep) {
            characterSetCodes.add(c);
        }
        Collections.sort(characterSetCodes);
        mdList.setCharacterSetCodes(characterSetCodes);

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
            HashMap hashMap = new HashMap();
            hashMap.put("msg", "invalid path");
            return Response.status(500).entity(hashMap).build();
        }

        //loop on subfiles/folders to create bean
        if (children != null) {
            for (File child : children) {
                final FileBean bean = new FileBean(child.getName(), child.isDirectory(), child.getAbsolutePath(), child.getParentFile().getAbsolutePath());

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
            HashMap hashMap = new HashMap();
            hashMap.put("msg", "invalid path");
            return Response.status(500).entity(hashMap).build();
        }

        //loop on subfiles/folders to create bean
        if (children != null) {
            for (File child : children) {
                final FileBean bean = new FileBean(child.getName(), child.isDirectory(), child.getAbsolutePath(), child.getParentFile().getAbsolutePath());

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
                fileIs.close();
                hashMap.put("dataPath", newFileData.getAbsolutePath());
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            return Response.status(500).entity("failed").build();
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
        HashMap<String,String> hashMap = new HashMap<>();
        if (identifier!=null && identifier.length()>0){
            //dataName = identifier + addExtentionIfExist(fileDetail.getFileName());
            hashMap.put("dataName", identifier);
        } else {
            if (serverMetadataPath !=null && serverMetadataPath.length()>0){
                hashMap.put("metadataPath", serverMetadataPath);
                try {
                    hashMap = extractIdentifierFromMetadataFile(hashMap,new File(serverMetadataPath));
                }catch (ConstellationException ex){
                    return Response.status(500).entity(hashMap.put("msg", ex.getMessage())).build();
                }
            } else  if (fileMetaDetail.getFileName().length() > 0) {
                final File newFileMetaData = new File(uploadDirectory, fileMetaDetail.getFileName());
                try {
                    if (mdFileIs != null) {
                        if (!uploadDirectory.exists()) {
                            uploadDirectory.mkdir();
                        }
                        Files.copy(mdFileIs, newFileMetaData.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        mdFileIs.close();

                        try {
                            extractIdentifierFromMetadataFile(hashMap, newFileMetaData);
                        }catch (ConstellationException ex){
                            return Response.status(500).entity(hashMap.put("msg", ex.getMessage())).build();
                        }
                    }
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
                    return Response.status(500).entity("failed").build();
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

    private HashMap extractIdentifierFromMetadataFile(HashMap<String, String> hashMap, File newFileMetaData) {
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
        final String metaIdentifier = new MetadataFeeder(metadata).getIdentifier();
        if (metaIdentifier!=null && metaIdentifier.length()>0) {
            //dataName = metaIdentifier + addExtentionIfExist(fileDetail.getFileName());
            hashMap.put("dataName", metaIdentifier);
        }


        hashMap.put("metadataPath",newFileMetaData.getAbsolutePath());

        final String title = new MetadataFeeder(metadata).getTitle();
        hashMap.put("metatitle",title);
        hashMap.put("metaIdentifier", metaIdentifier);
        return hashMap;
    }


    /**
     * Import data from upload Directory to integrated directory 
     * - change file location from upload to integrated
     *
     * @param values {@link org.constellation.dto.ParameterValues} containing file path & data type
     * @param request
     * @return a {@link javax.ws.rs.core.Response}
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
            LOGGER.log(Level.SEVERE, "Bad configuration for data Integrated directory", e);
            return Response.status(500).entity("failed").build();
        }
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
            return Response.status(500).entity("failed").build();
        }

        final XMLCoverageStore xmlCoverageStore = (XMLCoverageStore)ds;
        final ParameterValue paramVal = ParametersExt.getValue(xmlCoverageStore.getConfiguration(), XMLCoverageStoreFactory.PATH.getName().getCode());
        if (paramVal.getValue() instanceof URL) {
            try {
                final File dataFolder = new File(((URL)paramVal.getValue()).toURI());
                recursiveDelete(dataFolder);
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Unable to delete folder "+ paramVal.getValue(), ex);
                return Response.status(500).entity("failed").build();
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

        if (mdPath != null && mdPath.length()>0) {
            final Object obj;
            try {
                final MarshallerPool pool = CSWMarshallerPool.getInstance();
                final Unmarshaller unmarsh = pool.acquireUnmarshaller();
                obj = unmarsh.unmarshal(new File(mdPath));
                pool.recycle(unmarsh);
            } catch (JAXBException ex) {
                LOGGER.log(Level.WARNING, "Error when trying to unmarshal metadata", ex);
                return Response.status(500).entity("failed").build();
            }

            if (!(obj instanceof DefaultMetadata)) {
                return Response.status(500).entity("failed").build();
            }

            final DefaultMetadata metadata = (DefaultMetadata) obj;

            // for now we assume datasetID == providerID
            try {
                datasetBusiness.updateMetadata(providerId, -1, metadata);
            } catch (ConfigurationException ex) {
                LOGGER.warning("Error while saving dataset metadata");
                throw new ConstellationException(ex);
            }
        }

        return Response.ok().type(MediaType.TEXT_PLAIN_TYPE).build();
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
    public Response getDatasetMetadata(final ParameterValues values) throws ConfigurationException {
        final String providerId         = values.getValues().get("providerId");
        final String dataType           = values.getValues().get("type");
        final DefaultMetadata metadata  =  datasetBusiness.getMetadata(providerId, -1);
            
        if (metadata != null) {
            //get template name
            final String templateName;
            if("vector".equalsIgnoreCase(dataType)){
                //vector template
                templateName="profile_default_vector";
            }else if("raster".equalsIgnoreCase(dataType)){
                //raster template
                templateName="profile_default_raster";
            }else {
                //default template is import
                templateName="profile_import";
            }
            final Template template = Template.getInstance(templateName);
            final StringBuilder buffer = new StringBuilder();
            try{
                template.write(metadata,buffer,false);
            }catch(IOException ex){
                LOGGER.log(Level.WARNING, "error while generating metadata json.", ex);
                return Response.status(500).entity("failed").build();
            }
            return Response.ok(buffer.toString()).build();

        } else {
            LOGGER.log(Level.WARNING, "Metadata is null for providerId:{0}", providerId);
            return Response.status(500).entity("failed").build();
        }
    }

    /**
     * Returns applied template for metadata for read mode only like metadata viewer.
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
    public Response getIsoMetadataJson(final @PathParam("providerId") String providerId, final @PathParam("dataId") String dataId,
                                       final @PathParam("type") String type, final @PathParam("prune") boolean prune) {

        //@TODO get dataset or data metadata when dashboard will be added.
        // meanwhile we returns the dataset metadata and if not exists we returns the data metadata.
        //final DefaultMetadata metadata = dataBusiness.loadIsoDataMetadata(providerId, Util.parseQName(dataId));
        DefaultMetadata metadata;
        try{
            metadata = datasetBusiness.getMetadata(providerId,-1);
        }catch(Exception ex){
            metadata = dataBusiness.loadIsoDataMetadata(providerId, Util.parseQName(dataId));
        }

        final StringBuilder buffer = new StringBuilder();
        if (metadata != null) {
            //prune the metadata
            metadata.prune();

            //for debugging purposes
                /*try{
                    System.out.println(XML.marshal(metadata));
                }catch(Exception ex){
                    LOGGER.log(Level.WARNING,ex.getLocalizedMessage(),ex);
                }*/

            //get template name
            final String templateName;
            if("vector".equalsIgnoreCase(type)){
                //vector template
                templateName="profile_default_vector";
            }else if ("vector".equalsIgnoreCase(type)){
                //raster template
                templateName="profile_default_raster";
            } else {
                //default template is import
                templateName="profile_import";
            }
            final Template template = Template.getInstance(templateName);
            try{
                template.write(metadata,buffer,prune);
            }catch(IOException ex){
                LOGGER.log(Level.WARNING, "error while writing metadata json.", ex);
                return Response.status(500).entity("failed").build();
            }
        }
        return Response.ok(buffer.toString()).build();
    }

    /**
     * Proceed to merge saved metadata with given values from metadata editor.
     *
     * @param providerId the data provider identifier
     * @param type the data type.
     * @param metadataValues the values of metadata editor.
     * @return {@code Response}
     */
    @POST
    @Path("metadata/merge/{providerId}/{type}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response mergeMetadata(@PathParam("providerId") final String providerId,
                                  @PathParam("type") final String type,
                                  final RootObj metadataValues) {
        // for now assume that providerID == datasetID
        try {

            // Get previously saved metadata for the current data
            final DefaultMetadata metadata = datasetBusiness.getMetadata(providerId, -1);

            //get template name
            final String templateName;
            if ("vector".equalsIgnoreCase(type)) {
                //vector template
                templateName = "profile_default_vector";
            } else if ("raster".equalsIgnoreCase(type)){
                //raster template
                templateName = "profile_default_raster";
            }else {
                //default template is import
                templateName = "profile_default_raster";
            }
            final Template template = Template.getInstance(templateName);

            try{
                //uncomment for debugging purposes.
                //final CharSequence[] lines = CharSequences.splitOnEOL(metadataValues);
                //template.read(Arrays.asList(lines),metadata,false);
                template.read(metadataValues,metadata,false);
            }catch(IOException ex){
                LOGGER.log(Level.WARNING, "error while saving metadata.", ex);
                return Response.status(500).entity("failed").build();
            }

            //Save metadata
            datasetBusiness.updateMetadata(providerId, -1, metadata);
        } catch (ConfigurationException ex) {
            LOGGER.warning("Error while saving dataset metadata");
            throw new ConstellationException(ex);
        }
        return Response.ok().type(MediaType.TEXT_PLAIN_TYPE).build();
    }


    @POST
    @Path("metadata/find")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response findMetadata(final ParameterValues values) {
        final String search = values.getValues().get("search");
        List<DataBrief> briefs = new ArrayList<>();
        final List<org.constellation.engine.register.Data> datas;
        try {
            datas = dataBusiness.searchOnMetadata(search);
            for (org.constellation.engine.register.Data data : datas ) {
                final QName name = new QName(data.getNamespace(), data.getName());
                final DataBrief db = dataBusiness.getDataBrief(name, data.getProvider());
                briefs.add(db);
            }
            return Response.ok(briefs).build();
        } catch (ParseException | IOException ex) {
            return Response.ok("Failed to parse query : "+ex.getMessage()).status(500).build();
        }
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
    public Response saveMetadata(final ParameterValues values) throws TransformException {
        final String providerId         = values.getValues().get("providerId");
        final String dataType           = values.getValues().get("dataType");
        final DataProvider dataProvider = DataProviders.getInstance().getProvider(providerId);
        
        DefaultMetadata extractedMetadata = null;
        switch (dataType) {
            case "raster":
                try {
                    extractedMetadata = MetadataUtilities.getRasterMetadata(dataProvider);
                } catch (DataStoreException e) {
                    LOGGER.log(Level.WARNING, "Error when trying to get coverage metadata", e);
                    extractedMetadata = new DefaultMetadata();
                }
                break;
            case "vector":
                try {                
                    extractedMetadata = MetadataUtilities.getVectorMetadata(dataProvider);
                } catch (DataStoreException e) {
                    LOGGER.log(Level.WARNING, "Error when trying to get metadata for a shape file", e);
                    extractedMetadata = new DefaultMetadata();
                }
                break;
            default:
                extractedMetadata = new DefaultMetadata();
        }
        //Update metadata
        final Properties prop = ConfigurationBusiness.getMetadataTemplateProperties();
        final String metadataID = CstlMetadatas.getMetadataIdForDataset(providerId);
        prop.put("fileId", metadataID);
        prop.put("dataTitle", metadataID);
        prop.put("dataAbstract", "");
        final DefaultMetadata templateMetadata = MetadataUtilities.getTemplateMetadata(prop);

        DefaultMetadata mergedMetadata;
        if (extractedMetadata != null) {
            mergedMetadata = new DefaultMetadata();
            try {
                mergedMetadata = MetadataUtilities.mergeTemplate(templateMetadata, extractedMetadata);
            } catch (NoSuchIdentifierException | ProcessException ex) {
                LOGGER.log(Level.WARNING, "error while merging metadata", ex);
            }
        } else {
            mergedMetadata = templateMetadata;
        }
        mergedMetadata.prune();

        try {
            //Save metadata
            datasetBusiness.updateMetadata(providerId, -1, mergedMetadata);
        } catch (ConfigurationException ex) {
            LOGGER.warning("Error while saving dataset metadata");
            throw new ConstellationException(ex);
        }
        
        return Response.ok().type(MediaType.TEXT_PLAIN_TYPE).build();
    }

    @POST
    @Path("pyramid/{id}/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response pyramidData(@PathParam("id") final String providerId, final SimpleValue path) {

        if (path.getValue() != null) {
            // create folder to save pyramid
            File dataDirectory = ConfigDirectory.getDataDirectory();
            File pyramidFolder = new File(dataDirectory, "pyramid");
            if (!pyramidFolder.exists()) {
                pyramidFolder.mkdir();
            }

            final File dataPyramidFolder = new File(pyramidFolder, providerId);
            final String pyramidPath = dataPyramidFolder.getAbsolutePath();

            String login = SecurityManagerHolder.getInstance().getCurrentUserLogin();
            CstlUser user = userRepository.findOne(login).get();
            //create listener which save information on Database
            final ProcessListener listener = new PyramidCoverageProcessListener(user.getId(), pyramidPath, providerId);

            Runnable pyramidRunnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        PyramidCoverageHelper pyramidHelper = PyramidCoverageHelper.builder(providerId).
                                inputFormat("AUTO").withDeeps(new double[]{1}).
                                fromImage(path.getValue()).toFileStore(pyramidPath).build();
                        pyramidHelper.buildPyramid(listener);
                    } catch (MalformedURLException | DataStoreException | TransformException | FactoryException exception) {
                        LOGGER.log(Level.WARNING, "Error on pyramid building", exception);
                    }
                }
            };
            final Thread pyramidalThread = new Thread(pyramidRunnable);
            pyramidalThread.start();

        }
        return Response.ok().type(MediaType.TEXT_PLAIN_TYPE).build();
    }

    /**
     * Generates a pyramid on a data in the given provider, create and return this new provider.
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
            @PathParam("providerId") final String providerId, @PathParam("dataId") final String dataId) {
        
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
            Providers.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            return Response.ok("Failed to extract envelope for data "+dataId+". "+ex.getMessage()).status(500).build();
        }
        
        //calculate pyramid scale levels
        final CoverageReference inRef = (CoverageReference) inData.getOrigin();
        final GeneralGridGeometry gg;
        try{
            final GridCoverageReader reader = inRef.acquireReader();
            gg = reader.getGridGeometry(inRef.getImageIndex());
            
        } catch(CoverageStoreException ex){
            Providers.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            return Response.ok("Failed to extract grid geometry for data "+dataId+". "+ex.getMessage()).status(500).build();
        }
                        
        //create the output folder for pyramid 
        final String pyramidProviderId = "conform_"+ UUID.randomUUID().toString();
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
            ParametersExt.getOrCreateValue(xmlpyramidparams, XMLCoverageStoreFactory.PATH.getName().getCode()).setValue(pyramidDirectory.toURL());
            ParametersExt.getOrCreateValue(xmlpyramidparams, XMLCoverageStoreFactory.NAMESPACE.getName().getCode()).setValue("no namespace");
            outProvider = DataProviders.getInstance().createProvider(pyramidProviderId, factory, pparams);
        } catch (Exception ex) {
            Providers.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            return Response.ok("Failed to create pyramid provider "+ex.getMessage()).status(500).build();
        }

        // Update the parent attribute of the created provider


        providerBusiness.updateParent(outProvider.getId(),providerId);

        //create the output pyramid coverage reference
        CoverageStore outStore = (CoverageStore) outProvider.getMainStore();
        Name name = new DefaultName(dataId);
        try{
            name = ((XMLCoverageReference) outStore.create(name)).getName();
        }catch(DataStoreException ex){
            Providers.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            return Response.ok("Failed to create pyramid layer "+ex.getMessage()).status(500).build();
        }
                
        //update the DataRecord objects
        //this produces an update event which will create the DataRecord
        outProvider.reload();
        final org.constellation.engine.register.Data outData;

        final Provider provider = providerBusiness.getProvider(pyramidProviderId);
        final List<org.constellation.engine.register.Data> datas = providerBusiness.getDatasFromProviderId(provider.getId());
        outData = datas.get(0);


        
        //get the coverage reference after reload, otherwise this won't be the same reference
        outStore = (CoverageStore) outProvider.getMainStore();
        final XMLCoverageReference outputRef;
        try{
            outputRef = (XMLCoverageReference) outStore.getCoverageReference(name);
        }catch(DataStoreException ex){
            Providers.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            return Response.ok("Failed to create pyramid layer "+ex.getMessage()).status(500).build();
        }
        
        
        //get the fill value for no data
        try{
            final GridCoverageReader reader = inRef.acquireReader();
            final List<GridSampleDimension> sampleDimensions = reader.getSampleDimensions(inRef.getImageIndex());
            if(sampleDimensions!=null){
                final int nbBand = sampleDimensions.size();
                double[] fillValue = new double[nbBand];
                Arrays.fill(fillValue, Double.NaN);
                for(int i=0;i<nbBand;i++){
                    final double[] nodata = sampleDimensions.get(i).geophysics(true).getNoDataValues();
                    if(nodata!=null && nodata.length>0){
                        fillValue[i] = nodata[0];
                    }
                }
            }
        } catch(CoverageStoreException ex) {
            Providers.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            return Response.ok("Failed to extract no-data values for resampling "+ex.getMessage()).status(500).build();
        }
        
        //calculate scales
        final Map<Envelope, double[]> resolutionPerEnvelope = new HashMap<>();
        final double geospanX = dataEnv.getSpan(0);
        final double geospanY = dataEnv.getSpan(1);
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
        for(int i=0;i<scales.length;i++) scales[i] = scalesList.get(i);
        resolutionPerEnvelope.put(dataEnv, scales);
        
        //Prepare pyramid's mosaics.
        final Dimension tileDim = new Dimension(tileSize, tileSize);
        try {
            CoverageUtilities.getOrCreatePyramid(outputRef, dataEnv, tileDim, scales);
        } catch (Exception ex) {
            Providers.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            return Response.ok("Failed to create pyramid and mosaics in store "+ex.getMessage()).status(500).build();
        }

        //update the DataRecord objects
        //this produces an update event which will create the DataRecord
        outProvider.reload();
        //we grab the store again, reload has recreate it
        outStore = (CoverageStore)outProvider.getMainStore();
        
        //prepare process
        final ProcessDescriptor desc;
        try {
            desc = ProcessFinder.getProcessDescriptor("coverage", "coveragepyramid");
        } catch (NoSuchIdentifierException ex) {
            Providers.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            return Response.ok("Process engine2d.mapcontextpyramid not found "+ex.getMessage()).status(500).build();
        }
        final ParameterValueGroup input = desc.getInputDescriptor().createValue();
        input.parameter("coverageref").setValue(inRef);
        input.parameter("in_coverage_store").setValue(outStore);
        input.parameter("tile_size").setValue(new Dimension(tileSize, tileSize));
        input.parameter("pyramid_name").setValue(outputRef.getName().getLocalPart());
        input.parameter("interpolation_type").setValue(InterpolationCase.NEIGHBOR);
        input.parameter("resolution_per_envelope").setValue(resolutionPerEnvelope);
        final org.geotoolkit.process.Process p = desc.createProcess(input);

        //add task in scheduler
        try {
            CstlScheduler.getInstance().runOnce("Create conform pyramid for "+providerId+":"+dataId, p);
        } catch (SchedulerException e) {
            LOGGER.log(Level.WARNING, "Unable to run pyramid process on scheduler");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        final ProviderData ref = new ProviderData(outProvider.getId(), outData.getName());
        return Response.ok(ref).status(202).build();
    }

    /**
     * Generates a pyramid on a data in the given provider, create and return this new provider.
     *
     * @param providerId Provider identifier of the data to tile.
     * @param dataId Data identifier
     * @param params PyramidParams
     * @return
     */
    @POST
    @Path("pyramid/create/{providerId}/{dataId}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response createTiledProvider(
            @PathParam("providerId") final String providerId, @PathParam("dataId") final String dataId, final PyramidParams params) {
        
        String tileFormat = params.getTileFormat();
        String crs = params.getCrs();
        double[] scales = params.getScales();
        Double upperCornerX = params.getUpperCornerX();
        Double upperCornerY = params.getUpperCornerY();
        
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
        
        //get tile format 
        if(tileFormat==null || tileFormat.isEmpty()){
            tileFormat = "PNG";
        }
        
        //get pyramid CRS, we force longiude first on the pyramids
        // WMTS is made for display like WMS, so longitude is expected to be on the X axis.
        // Note : this is not writen in the spec.
        final CoordinateReferenceSystem coordsys;
        if(crs == null || crs.isEmpty()){
            try {
                coordsys = ReferencingUtilities.setLongitudeFirst(dataEnv.getCoordinateReferenceSystem());
            } catch (FactoryException ex) {
                return Response.ok("Failed to invert axes (longitude first) on CRS : "+ex.getMessage()).status(500).build();
            }
            try {
                //reproject data envelope
                dataEnv = CRS.transform(dataEnv, coordsys);
            } catch (TransformException ex) {
                return Response.ok("Could not transform data envelope to crs "+crs).status(400).build();
            }
        }else{
            try {
                coordsys = CRS.decode(crs,true);
            } catch (FactoryException ex) {
                return Response.ok("Invalid CRS code : "+crs).status(400).build();
            }
            try {
                //reproject data envelope
                dataEnv = CRS.transform(dataEnv, coordsys);
            } catch (TransformException ex) {
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
        final String pyramidProviderId = UUID.randomUUID().toString();
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
            ParametersExt.getOrCreateValue(xmlpyramidparams, XMLCoverageStoreFactory.PATH.getName().getCode()).setValue(pyramidDirectory.toURL());
            ParametersExt.getOrCreateValue(xmlpyramidparams, XMLCoverageStoreFactory.NAMESPACE.getName().getCode()).setValue("no namespace");
            outProvider = DataProviders.getInstance().createProvider(pyramidProviderId, factory, pparams);
        } catch (Exception ex) {
            Providers.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            return Response.ok("Failed to create pyramid provider "+ex.getMessage()).status(500).build();
        }

        // Update the parent attribute of the created provider
        providerBusiness.updateParent(outProvider.getId(),providerId);

        //create the output pyramid coverage reference
        CoverageStore pyramidStore = (CoverageStore) outProvider.getMainStore();
        XMLCoverageReference outputRef;
        Name name = new DefaultName(dataId);
        try{
            outputRef = (XMLCoverageReference) pyramidStore.create(name);
            name = outputRef.getName();
            outputRef.setPackMode(ViewType.RENDERED);
            outputRef.setPreferredFormat(tileFormat);
        }catch(DataStoreException ex){
            Providers.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            return Response.ok("Failed to create pyramid layer "+ex.getMessage()).status(500).build();
        }
        
        //prepare the pyramid and mosaics
        final int tileSize = 256;
        final Dimension tileDim = new Dimension(tileSize, tileSize);
        try {
            CoverageUtilities.getOrCreatePyramid(outputRef, dataEnv, tileDim, scales);
        } catch (Exception ex) {
            Providers.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            return Response.ok("Failed to create pyramid and mosaics in store "+ex.getMessage()).status(500).build();
        }
        
        //update the DataRecord objects
        //this produces an update event which will create the DataRecord
        outProvider.reload();
        final org.constellation.engine.register.Data outData;
        final Provider provider = providerBusiness.getProvider(pyramidProviderId);
        final List<org.constellation.engine.register.Data> datas = providerBusiness.getDatasFromProviderId(provider.getId());
        outData = datas.get(0);
        
        //get the coverage reference after reload, otherwise this won't be the same reference
        pyramidStore = (CoverageStore) outProvider.getMainStore();
        try{
            outputRef = (XMLCoverageReference) pyramidStore.getCoverageReference(name);
        }catch(DataStoreException ex){
            Providers.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            return Response.ok("Failed to create pyramid layer "+ex.getMessage()).status(500).build();
        }
        
        //get the rendering process
        final MapContext context = MapBuilder.createContext();
        try {
            context.items().add(inData.getMapLayer(null, null));
        } catch (PortrayalException ex) {
            Providers.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            return Response.ok("Failed to create map context layer for data "+ex.getMessage()).status(500).build();
        }
        final ProcessDescriptor desc;
        try {
            desc = ProcessFinder.getProcessDescriptor("engine2d", "mapcontextpyramid");
        } catch (NoSuchIdentifierException ex) {
            Providers.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            return Response.ok("Process engine2d.mapcontextpyramid not found "+ex.getMessage()).status(500).build();
        }
        final ParameterValueGroup input = desc.getInputDescriptor().createValue();
        input.parameter("context").setValue(context);
        input.parameter("extent").setValue(dataEnv);
        input.parameter("tilesize").setValue(tileDim);
        input.parameter("scales").setValue(scales);
        input.parameter("container").setValue(outputRef);
        final org.geotoolkit.process.Process p = desc.createProcess(input);

        //add task in scheduler
        try {
            CstlScheduler.getInstance().runOnce("Create pyramid "+crs+" for "+providerId+":"+dataId, p);
        } catch (SchedulerException e) {
            LOGGER.log(Level.WARNING, "Unable to run pyramid process on scheduler");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
                                
        final ProviderData ref = new ProviderData(outProvider.getId(), outData.getName());
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
    @Path("pyramid/bestscales/{providerId}/{dataId}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response findBestScales(
            @PathParam("providerId") final String providerId, @PathParam("dataId") final String dataId){
        
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
        
        final CoordinateReferenceSystem objCRS = dataEnv.getCoordinateReferenceSystem();
        final Object origin = inData.getOrigin();
        
        final Object[] scales;
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
            scales = new Object[scalesList.size()];
            for(int i=0;i<scales.length;i++) scales[i] = scalesList.get(i);

        }else{
            //featurecollection or anything else, scales can not be defined accurately.
            //vectors have virtually an unlimited resolution
            //we build scales, to obtain 8 levels, this should be enough for a default case
            final double geospanX = dataEnv.getSpan(0);
            final int tileSize = 256;
            scales = new Object[8];
            scales[0] = geospanX / tileSize;
            for(int i=1;i<scales.length;i++){
                scales[i] = ((Double)scales[i-1]) / 2.0;
            }
            
        }
        
        final String scalesStr = StringUtilities.toCommaSeparatedValues(scales);
        
        return Response.ok(new StringList(Collections.singleton(scalesStr))).build();
        
//        /**
//         * Used in SLD/SE to calculate scale for degree CRSs.
//         * We should use this to calculate a more friendly scale value.
//         * TODO : move this algo in Javascript, we should see 2 columns : real crs scale + map scale.
//         */
//        final double SE_DEGREE_TO_METERS = 6378137.0 * 2.0 * Math.PI / 360;
//        final double DEFAULT_DPI = 90; // ~ 0.28 * 0.28mm
//        final double PIXEL_SIZE = 0.0254;
//    
//        if(objCRS instanceof GeographicCRS) {
//            return (dataEnv.getSpan(0) * SE_DEGREE_TO_METERS) / (width / DEFAULT_DPI*PIXEL_SIZE);
//        } else {
//            return dataEnv.getSpan(0) / (width / DEFAULT_DPI*PIXEL_SIZE);
//        }
        
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
    @Path("/list/published/{published}/top")
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
    @Path("/list/observation/{sensorable}/top")
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

    @DELETE
    @Path("{providerid}/{dataid}")
    public Response deleteData(@PathParam("providerid") String providerid, @PathParam("dataid") String dataid) {
        dataBusiness.deleteData(new QName("", dataid), providerid);
        return Response.ok().type(MediaType.TEXT_PLAIN_TYPE).build();
    }

    @POST
    @Path("{providerid}/{dataid}/visible")
    public Response visibleData(@PathParam("providerid") String providerid, @PathParam("dataid") String dataid) {

        dataBusiness.updateDataVisibility(new QName("", dataid), providerid, true);
        return Response.ok().type(MediaType.TEXT_PLAIN_TYPE).build();
    }

    @POST
    @Path("{providerid}/{dataid}/hidden")
    public Response hideData(@PathParam("providerid") String providerid, @PathParam("dataid") String dataid, final SimpleValue dataNmsp) {
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
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
                return Response.status(500).entity("failed").build();
            }
        } else {
            LOGGER.log(Level.INFO, "Type unknown to found metadata");
            return Response.status(500).entity("failed").build();
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
            final List<DataBrief> dataBriefs = dataBusiness.getDataBriefsFromMetadataId(id);
            mapping.put(id, dataBriefs);
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
        final SelectedExtension r = new SelectedExtension();
        r.setExtension(extension);
        r.setDataType(selectedType);
        
        // look for observation netcdf
        if ("nc".equals(extension) && NetCDFExtractor.isObservationFile(filePath)) {
            r.setDataType("observation");
        }
        // look for SML file (available for data import ?)
        if ("xml".equals(extension)) {
            try {
                String rootMark = getXmlDocumentRoot(filePath);
                if (rootMark.equals("SensorML")) {
                    r.setDataType("observation");
                }
            } catch (IOException | XMLStreamException ex) {
                LOGGER.log(Level.WARNING, "error while reading xml file", ex);
            }
            
        }
        return r;
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
            return Response.status(500).entity("failed").build();
        }

        if (filesToSend.length == 0) {
            LOGGER.info("No files for this data to export!");
            return Response.status(500).entity("failed").build();
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
            return Response.status(500).entity("failed").build();
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
            return Response.status(500).entity("failed").build();
        }
        return Response.ok().type(MediaType.TEXT_PLAIN_TYPE).build();
    }

    @GET
    @Path("{id}/vector/columns")
    public Response getVectorDataColumns(final @PathParam("id") int id) throws DataStoreException {
        final Provider provider = dataBusiness.getProvider(id);
        final DataProvider dataProvider = DataProviders.getInstance().getProvider(provider.getIdentifier());
        if (!(dataProvider.getMainStore() instanceof FeatureStore)) {
            return Response.ok("Not a feature requested").build();
        }

        final List<String> colNames = new ArrayList<>();
        final String dataName = dataRepository.findById(id).getName();
        final FeatureStore store = (FeatureStore)dataProvider.getMainStore();
        final org.opengis.feature.FeatureType ft = store.getFeatureType(dataName);
        for (final PropertyType prop : ft.getProperties(true)) {
            colNames.add(prop.getName().toString());
        }

        final ParameterValues values = new ParameterValues();
        final HashMap<String,String> mapVals = new HashMap<>();
        for (final String colName : colNames) {
            mapVals.put(colName, colName);
        }
        values.setValues(mapVals);

        return Response.ok(values).build();
    }
}


