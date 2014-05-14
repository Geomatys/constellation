/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013 - 2014, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.ws.rest;

import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Adler32;
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
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.sis.geometry.GeneralDirectPosition;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.storage.DataStore;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.iso.Types;
import org.apache.sis.util.logging.Logging;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.admin.dao.DataRecord;
import org.constellation.admin.dao.ProviderRecord;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.configuration.DataBrief;
import org.constellation.configuration.StringList;
import org.constellation.coverage.PyramidCoverageHelper;
import org.constellation.coverage.PyramidCoverageProcessListener;
import org.constellation.dto.*;
import org.constellation.engine.register.repository.ProviderRepository;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
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
import org.constellation.utils.*;
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
import org.geotoolkit.data.memory.ExtendedFeatureStore;
import org.geotoolkit.display.PortrayalException;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.feature.xml.Utils;
import org.geotoolkit.image.interpolation.InterpolationCase;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapContext;
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
import org.opengis.feature.type.Name;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.citation.DateType;
import org.opengis.metadata.citation.Role;
import org.opengis.metadata.identification.TopicCategory;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import org.opengis.util.InternationalString;
import org.opengis.util.NoSuchIdentifierException;

/**
 * Manage data sending
 *
 * @author Benjamin Garcia (Geomatys)
 */
@Path("/1/data/")
public class DataRest {

    private final XMLInputFactory xif = XMLInputFactory.newFactory();

    private static final Logger LOGGER = Logging.getLogger(DataRest.class);

    @Inject
    private SessionData sessionData;

    
    @Inject
    private ProviderRepository providerRepository;

    /**
     * Give metadata CodeList (example {@link org.opengis.metadata.citation.Role} codes
     *
     * @param pLocale locale to found right translation
     * @return a {@link javax.ws.rs.core.Response} which contain codelists
     */
    @GET
    @Path("metadataCodeLists/{locale}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getMetadataCodeLists(@PathParam("locale") final String pLocale) {

        final Locale userLocale = new Locale(pLocale);
        MetadataLists mdList = new MetadataLists();

        HashMap<String, String> roles = new HashMap<>(0);
        for (Role role : Role.values()) {
            InternationalString is = Types.getCodeTitle(role);
            roles.put(role.name(), is.toString(userLocale));
        }
        mdList.setRoles(roles);


        Comparator<String> comparator = new Comparator<String>() {
            @Override
            public int compare(final String first, final String second) {
                return first.compareTo(second);
            }
        };
        TreeMap<String, String> locales = new TreeMap<>(comparator);
        for (Locale locale : Locale.getAvailableLocales()) {
            locales.put(locale.toString(), locale.getDisplayName(userLocale));
        }
        mdList.setLocales(locales);

        HashMap<String, String> topics = new HashMap<>(0);
        for (TopicCategory topicCategory : TopicCategory.values()) {
            InternationalString is = Types.getCodeTitle(topicCategory);
            topics.put(topicCategory.name(), is.toString(userLocale));
        }
        mdList.setCategories(topics);

        HashMap<String, String> dateTypes = new HashMap<>(0);
        for (DateType dateType : DateType.values()) {
            InternationalString is = Types.getCodeTitle(dateType);
            dateTypes.put(dateType.name(), is.toString(userLocale));
        }
        mdList.setDateTypes(dateTypes);

        return Response.status(200).entity(mdList).build();
    }


    /**
     * Give subfolder list from a server file path
     *
     * @param path server file path
     * @return a {@link javax.ws.rs.core.Response} which contain file list
     */
    @POST
    @Path("datapath")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getDataFolder(String path) {
        final List<FileBean> listBean = new ArrayList<>(0);
        File[] children;
        final Set<String> extensions = GeotoolkitFileExtensionAvailable.getAvailableFileExtension().keySet();

        final File root = ConfigDirectory.getUserHomeDirectory();
        if ("root".equalsIgnoreCase(path)) {
            path = "";
            children = root.listFiles();

        } else {
            final File nextRoot = new File(root, path);
            children = nextRoot.listFiles();
        }

        //loop on subfiles/folders to create bean
        if (children != null) {
            for (File child : children) {
                
                if (child.isFile()) {
                    int lastIndexPoint = child.getName().lastIndexOf('.');
                    String extension = child.getName().substring(lastIndexPoint + 1);

                    if (extensions.contains(extension.toLowerCase())) {
                        final FileBean bean = new FileBean(child.getName(), child.isDirectory(), root.getAbsolutePath(), path + "/" + child.getName());
                        listBean.add(bean);
                    }

                } else {
                    final FileBean bean = new FileBean(child.getName(), child.isDirectory(), root.getAbsolutePath(), path + "/" + child.getName());
                    listBean.add(bean);
                }
            }
        }
        return Response.status(200).entity(listBean).build();
    }

    /**
     * Receive a {@link MultiPart} which contain a file need to be save on server to create data on provider
     *
     * @param fileIs
     * @param fileDetail
     * @param request
     * @return A {@link Response} with 200 code if upload work, 500 if not work.
     */
    @POST
    @Path("upload/data")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadData(@FormDataParam("data") InputStream fileIs,
                               @FormDataParam("data") FormDataContentDisposition fileDetail,
                               @Context HttpServletRequest request) {
    	final String sessionId = request.getSession(false).getId();
    	final File uploadDirectory = ConfigDirectory.getUploadDirectory(sessionId);
    	boolean isArchive =false;
        File newFile = new File(uploadDirectory, fileDetail.getFileName());
        File OriginalFile = new File(uploadDirectory, fileDetail.getFileName());
        try {
            if (fileIs != null) {
                Files.copy(fileIs, newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                fileIs.close();
                if (newFile.getName().endsWith(".zip")) {
                    final String fileNameWithoutExt = newFile.getName().substring(0, newFile.getName().indexOf("."));
                    final File zipDir = new File(uploadDirectory, fileNameWithoutExt);
                    FileUtilities.unzip(newFile, zipDir, new CRC32());
                    newFile = zipDir;
                    isArchive = true;
                }
            }

            
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            return Response.status(500).entity("failed").build();
        } finally {
            if (isArchive){
                OriginalFile.delete();
            }
        }

        String result = newFile.getAbsolutePath();
        return Response.ok(result).build();
    }

    /**
     * Receive a {@link MultiPart} which contain a file need to be save on server to create data on provider
     *
     * @param mdFileIs
     * @param mdFileDetail
     * @param request
     * @return A {@link Response} with 200 code if upload work, 500 if not work.
     */
    @POST
    @Path("upload/metadata")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadMetadata(@FormDataParam("metadata") InputStream mdFileIs,
                               @FormDataParam("metadata") FormDataContentDisposition mdFileDetail,
                               @Context HttpServletRequest request) {
    	final String sessionId = request.getSession(false).getId();
        final File uploadDirectory = ConfigDirectory.getUploadDirectory(sessionId);
       
        File mdFile = null;
        try {
            if (mdFileIs != null && mdFileDetail != null && !mdFileDetail.getFileName().isEmpty()) {
                final File mdFolder = new File(uploadDirectory, "metadata");
                if (mdFolder.exists() && mdFolder.isFile()) {
                    // Ensures we do not have a file named "metadata" in this folder
                    mdFolder.delete();
                }
                if (!mdFolder.exists()) {
                    mdFolder.mkdir();
                }
                mdFile = new File(mdFolder, mdFileDetail.getFileName());
                Files.copy(mdFileIs, mdFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                mdFileIs.close();
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            return Response.status(500).entity("failed").build();
        }

        if (mdFile != null) {
            String result = mdFile.getAbsolutePath();
            return Response.ok(result).build();
        }

        // Did nothing cause no metadata file was given
        return Response.status(200).build();
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
        final String filePath = values.getValues().get("filePath");
        final String metadataFilePath = values.getValues().get("metadataFilePath");
        final String dataType = values.getValues().get("dataType");

        try{
            File dataIntegratedDirectory = ConfigDirectory.getDataIntegratedDirectory();
            if (filePath!= null){
                recursiveDelete(new File(new File(dataIntegratedDirectory.getAbsolutePath() + File.separator + new File(filePath).getName()).getAbsolutePath()));
                truncateZipFolder(filePath);
                Files.move(Paths.get(filePath), Paths.get(new File(dataIntegratedDirectory.getAbsolutePath() + File.separator + new File(filePath).getName()).getAbsolutePath()),StandardCopyOption.REPLACE_EXISTING);
            }
            if (metadataFilePath!= null){
                Files.move(Paths.get(metadataFilePath), Paths.get(new File(dataIntegratedDirectory.getAbsolutePath() + File.separator + new File(metadataFilePath).getName()).getAbsolutePath()),StandardCopyOption.REPLACE_EXISTING);
            }
            ImportedData importedData = new ImportedData();
            if (filePath != null) {
                importedData.setDataFile(new File(dataIntegratedDirectory.getAbsolutePath() + File.separator + new File(filePath).getName()).getAbsolutePath());
            }
            if (metadataFilePath != null) {
                importedData.setMetadataFile(new File(dataIntegratedDirectory.getAbsolutePath() + File.separator + new File(metadataFilePath).getName()).getAbsolutePath());
            }

            return Response.ok(importedData).build();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Bad configuration for data Integrated directory", e);
            return Response.status(500).entity("failed").build();
        }
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
        return Response.status(200).build();
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
     * @param values {@link org.constellation.dto.ParameterValues} containing file path & data type
     * @return a {@link javax.ws.rs.core.Response} with a {@link org.constellation.dto.DataInformation}
     */
    @POST
    @Path("load")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response loadData(final ParameterValues values) {
        final String filePath = values.getValues().get("filePath");
        final String metadataFilePath = values.getValues().get("metadataFilePath");
        final String dataType = values.getValues().get("dataType");

        File dataIntegratedDirectory = ConfigDirectory.getDataIntegratedDirectory();
        final File choosingFile = new File(dataIntegratedDirectory, filePath);

        File choosingMetadataFile = null;
        if (metadataFilePath != null && !metadataFilePath.isEmpty()) {
            choosingMetadataFile = new File(dataIntegratedDirectory.getAbsolutePath(), metadataFilePath);
        }
        
        if (choosingFile.exists()) {
            final DataInformation information = MetadataUtilities.generateMetadatasInformation(choosingFile, choosingMetadataFile, dataType);
            final String choosingName = choosingFile.getName();
            String dataName = choosingName;
            if (!choosingFile.isDirectory()) {
                dataName = choosingName.substring(0, choosingName.lastIndexOf("."));
            }
            information.setName(dataName);
            return Response.status(200).entity(information).build();
        }
        return Response.status(418).build();
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
        final DataProvider dataProvider = DataProviders.getInstance().getProvider(providerId);

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

        final DefaultMetadata metadata = (DefaultMetadata)obj;

        for (Name dataName : dataProvider.getKeys()) {
            //Save metadata
            final QName name = Utils.getQnameFromName(dataName);
            ConfigurationEngine.saveDataMetadata(metadata, name, providerId);
        }

        return Response.status(200).build();
    }

    
    @POST
    @Path("metadata/data")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getDataMetadata(final ParameterValues values) {
        final String providerId = values.getValues().get("providerId");
        final String dataName   = values.getValues().get("dataName");
        final DefaultMetadata metadata;
        if (dataName != null) {
            final QName name = QName.valueOf(dataName);
            metadata =  ConfigurationEngine.loadIsoDataMetadata(providerId, name, ISOMarshallerPool.getInstance());
        } else {
            final DataProvider dataProvider = DataProviders.getInstance().getProvider(providerId);
            // multiple ?
            if (!dataProvider.getKeys().isEmpty()) {
                final QName name = Utils.getQnameFromName(dataProvider.getKeys().iterator().next());
                metadata =  ConfigurationEngine.loadIsoDataMetadata(providerId, name, ISOMarshallerPool.getInstance());
            } else {
                metadata = null;
            }
        }
        if (metadata != null) {
            final MetadataFeeder feeder = new MetadataFeeder(metadata);
            final DataMetadata information = feeder.extractDataMetadata();
                    
            return Response.status(200).entity(information).build();
        } else {
            return Response.status(500).build();
        }
    }
    
    /**
     * Save metadata.
     *
     * @param values
     * @return {@link javax.ws.rs.core.Response} with code 200.
     */
    @POST
    @Path("metadata")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response saveMetadata(final ParameterValues values) {
        final String providerId         = values.getValues().get("providerId");
        final String dataType           = values.getValues().get("dataType");
        final DataProvider dataProvider = DataProviders.getInstance().getProvider(providerId);
        
        for (Name dataName : dataProvider.getKeys()) {
            
            DefaultMetadata extractedMetadata = null;
            switch (dataType) {
                case "raster":
                    try {
                        extractedMetadata = MetadataUtilities.getRasterMetadata(dataProvider, dataName);
                    } catch (DataStoreException e) {
                        LOGGER.log(Level.WARNING, "Error when trying to get coverage metadata", e);
                        extractedMetadata = new DefaultMetadata();
                    }
                    break;
                case "vector":
                    try {                
                        extractedMetadata = MetadataUtilities.getVectorMetadata(dataProvider, dataName);
                    } catch (DataStoreException e) {
                        LOGGER.log(Level.WARNING, "Error when trying to get metadata for a shape file", e);
                        extractedMetadata = new DefaultMetadata();
                    }
                    break;
                default:
                    extractedMetadata = new DefaultMetadata();
            }
            //Update metadata
            final Properties prop = ConfigurationEngine.getMetadataTemplateProperties();
            final String metadataID = CstlMetadatas.getMetadataIdForData(providerId, dataName);
            prop.put("fileId", metadataID);
            prop.put("dataTitle", dataName);
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
            
            //Save metadata
            final QName name = Utils.getQnameFromName(dataName);
            ConfigurationEngine.saveDataMetadata(mergedMetadata, name, providerId);
        }
        return Response.status(200).build();
    }

    /**
     * Save metadata with merge from ISO19115 form
     *
     * @param overridenValue {@link org.constellation.dto.DataMetadata} which contains new information for metadata.
     * @return {@link javax.ws.rs.core.Response} with code 200.
     */
    @POST
    @Path("metadata/merge")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response mergeMetadata(final DataMetadata overridenValue) {
        final String providerId = overridenValue.getDataName();
        final DataProvider dataProvider = DataProviders.getInstance().getProvider(providerId);

        for (Name dataName : dataProvider.getKeys()) {
            final QName name = Utils.getQnameFromName(dataName);
            
            // Get previously saved metadata for the current data
            final DefaultMetadata previous = ConfigurationEngine.loadIsoDataMetadata(providerId, name, ISOMarshallerPool.getInstance());
            
            // Import changes from DataMetadata into the DefaultMetadata
            final MetadataFeeder feeder = new MetadataFeeder(previous);
            feeder.feed(overridenValue);
            
            //Save metadata
            ConfigurationEngine.saveDataMetadata(previous, name, providerId);
        }
        return Response.status(200).build();
    }

    private static void printXml(final DefaultMetadata meta) throws JAXBException {
        final Marshaller m = ISOMarshallerPool.getInstance().acquireMarshaller();
        m.marshal(meta, System.out);
        ISOMarshallerPool.getInstance().recycle(m);
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

            //create listener which save information on Database
            final ProcessListener listener = new PyramidCoverageProcessListener(login, pyramidPath, providerId);

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
        return Response.status(200).build();
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
        final ProviderRecord updatedProvider = ConfigurationEngine.getProvider(outProvider.getId());
        updatedProvider.setParentIdentifier(providerId);
        ConfigurationEngine.updateProvider(updatedProvider);

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
        final DataRecord outData;
        try {
            outData = ConfigurationEngine.getProvider(pyramidProviderId).getData().get(0);
        } catch (SQLException ex) {
            Providers.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            return Response.ok("Failed to get ouput data record "+ex.getMessage()).status(500).build();
        }
        
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
                Arrays.fill(fillValue,Double.NaN);
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

        new Thread(){
            @Override
            public void run() {
                try {
                    p.call();
                } catch (ProcessException ex) {
                    LOGGER.log(Level.WARNING, null, ex);
                }
            }
        }.start();
        
                        
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
            //TODO a way to work on all cases, a defualt values ?
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
        final ProviderRecord updatedProvider = ConfigurationEngine.getProvider(outProvider.getId());
        updatedProvider.setParentIdentifier(providerId);
        ConfigurationEngine.updateProvider(updatedProvider);

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
        final DataRecord outData;
        try {
            outData = ConfigurationEngine.getProvider(pyramidProviderId).getData().get(0);
        } catch (SQLException ex) {
            Providers.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
            return Response.ok("Failed to get ouput data record "+ex.getMessage()).status(500).build();
        }
        
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
        CstlScheduler.getInstance().runOnce("Create pyramid "+crs+" for "+providerId+":"+dataId, p);
                                
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
        final DataBrief db = ConfigurationEngine.getData(fullName, providerId);
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
        final List<String> providerIds = ConfigurationEngine.getProviderIds();
        for (final String providerId : providerIds) {
            final ProviderRecord provider = ConfigurationEngine.getProvider(providerId);

            final List<DataRecord> datas;
            try {
                datas = provider.getData();
            } catch (SQLException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
                return Response.status(500).entity("failed").build();
            }

            for (final DataRecord data : datas) {
                if (type != null && !data.getType().equals(DataRecord.DataType.valueOf(type))) {
                    continue;
                }

                if (data.isVisible()) {
                    final QName name = new QName(data.getNamespace(), data.getName());
                    final DataBrief db = ConfigurationEngine.getData(name, providerId);
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
        final List<String> providerIds = ConfigurationEngine.getProviderIds();
        for (final String providerId : providerIds) {
            final ProviderRecord provider = ConfigurationEngine.getProvider(providerId);

            final List<DataBrief> briefs = new ArrayList<>();
            final List<DataRecord> datas;
            try {
                datas = provider.getData();
            } catch (SQLException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
                return Response.status(500).entity("failed").build();
            }
            for (final DataRecord data : datas) {
                if (data.isVisible()) {
                    final QName name = new QName(data.getNamespace(), data.getName());
                    final DataBrief db = ConfigurationEngine.getData(name, providerId);
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
        final ProviderRecord prov = ConfigurationEngine.getProvider(providerId);
        final List<DataBrief> briefs = new ArrayList<>();
        final List<DataRecord> datas;
        try {
            datas = prov.getData();
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            return Response.status(500).entity("failed").build();
        }
        for (final DataRecord data : datas) {
            if (data.isVisible()) {
                final QName name = new QName(data.getNamespace(), data.getName());
                final DataBrief db = ConfigurationEngine.getData(name, providerId);
                briefs.add(db);
            }
        }
        return Response.ok(briefs).build();
    }

    @GET
    @Path("list/top")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response getTopDataList() {
        return getTopDataList(null);
    }

    @GET
    @Path("list/top/{type}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public Response getTopDataList(@PathParam("type") String type) {
        final List<DataBrief> briefs = new ArrayList<>();

        final List<String> providerIds = providerRepository.getProviderIds();
        for (final String providerId : providerIds) {
            final ProviderRecord provider = ConfigurationEngine.getProvider(providerId);
            final String parent = provider.getParentIdentifier();
            if (parent != null && !parent.isEmpty()) {
                // Remove all providers that have a parent
                continue;
            }
            final List<DataRecord> datas;
            try {
                datas = provider.getData();
            } catch (SQLException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
                return Response.status(500).entity("failed").build();
            }
            for (final DataRecord data : datas) {
                if (type != null && !data.getType().equals(DataRecord.DataType.valueOf(type))) {
                    continue;
                }

                if (data.isVisible()) {
                    final QName name = new QName(data.getNamespace(), data.getName());
                    final DataBrief db = ConfigurationEngine.getData(name, providerId);
                    briefs.add(db);
                }
            }
        }

        return Response.ok(briefs).build();
    }

    @DELETE
    @Path("{providerid}/{dataid}")
    public Response deleteData(@PathParam("providerid") String providerid, @PathParam("dataid") String dataid) {
        ConfigurationEngine.deleteData(new QName("", dataid), providerid);
        return Response.status(200).build();
    }

    @POST
    @Path("{providerid}/{dataid}/visible")
    public Response visibleData(@PathParam("providerid") String providerid, @PathParam("dataid") String dataid) {
        ConfigurationEngine.updateDataVisibility(new QName("", dataid), providerid, true);
        return Response.status(200).build();
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
        ConfigurationEngine.updateDataVisibility(dataName, providerid, false);
        return Response.status(200).build();
    }

    @GET
    @Path("layer/summary/{providerid}/{layerAlias}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getLayerSummary(@PathParam("providerid") String providerid, @PathParam("layerAlias") String layerAlias) {
        final DataBrief db = ConfigurationEngine.getDataLayer(layerAlias, providerid);
        return Response.ok(db).build();
    }

    @GET
    @Path("metadata/iso/{providerId}/{dataId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getIsoMetadata(final @PathParam("providerId") String providerId, final @PathParam("dataId") String dataId) {
        final DefaultMetadata metadata = ConfigurationEngine.loadIsoDataMetadata(providerId, QName.valueOf(dataId), CSWMarshallerPool.getInstance());
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

        final DefaultMetadata metadata = ConfigurationEngine.loadProviderMetadata(providerId, CSWMarshallerPool.getInstance());
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
                final CoverageMetadataBean cmb = ConfigurationEngine.loadDataMetadata(providerId, name, GenericDatabaseMarshallerPool.getInstance());
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
            final List<DataBrief> dataRecords = ConfigurationEngine.getDataRecordsForMetadata(id);
            mapping.put(id, dataRecords);
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

        final File zip = new File(System.getProperty("java.io.tmpdir"), "export_data.zip");
        if (zip.exists()) {
            zip.delete();
        }
        try {
            FileUtilities.zip(zip, new Adler32(), filesToSend);
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
        ConfigurationEngine.linkDataToSensor(name, providerId, sensorId);
        return Response.status(200).build();
    }
}


