package org.constellation.ws.rest;

import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.iso.Types;
import org.apache.sis.util.logging.Logging;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.admin.dao.DataRecord;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.configuration.DataBrief;
import org.constellation.configuration.NotRunningServiceException;
import org.constellation.configuration.ProviderConfiguration;
import org.constellation.coverage.PyramidCoverageHelper;
import org.constellation.coverage.PyramidCoverageProcessListener;
import org.constellation.dto.CoverageMetadataBean;
import org.constellation.dto.DataInformation;
import org.constellation.dto.DataMetadata;
import org.constellation.dto.FileBean;
import org.constellation.dto.FileListBean;
import org.constellation.dto.MetadataLists;
import org.constellation.dto.ParameterValues;
import org.constellation.dto.SimpleValue;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.model.SelectedExtension;
import org.constellation.provider.CoverageLayerDetails;
import org.constellation.provider.FeatureLayerDetails;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.LayerProvider;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.coveragestore.CoverageStoreProvider;
import org.constellation.security.SecurityManagerHolder;
import org.constellation.util.SimplyMetadataTreeNode;
import org.constellation.utils.GeotoolkitFileExtensionAvailable;
import org.constellation.utils.MetadataFeeder;
import org.constellation.utils.MetadataUtilities;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.csw.xml.CSWMarshallerPool;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessListener;
import org.geotoolkit.util.FileUtilities;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.opengis.feature.type.Name;
import org.opengis.metadata.citation.DateType;
import org.opengis.metadata.citation.Role;
import org.opengis.metadata.identification.TopicCategory;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import org.opengis.util.InternationalString;
import org.opengis.util.NoSuchIdentifierException;

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
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;

/**
 * Manage data sending
 *
 * @author Benjamin Garcia (Geomatys)
 */
@Path("/1/data/")
public class Data {


    private static final Logger LOGGER = Logging.getLogger(Data.class);


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
        for (int i = 0; i < Role.values().length; i++) {
            Role role = Role.values()[i];
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
        for (int i = 0; i < Locale.getAvailableLocales().length; i++) {
            Locale locale = Locale.getAvailableLocales()[i];
            locales.put(locale.toString(), locale.getDisplayName(userLocale));
        }
        mdList.setLocales(locales);

        HashMap<String, String> topics = new HashMap<>(0);
        for (int i = 0; i < TopicCategory.values().length; i++) {
            TopicCategory topicCategory = TopicCategory.values()[i];
            InternationalString is = Types.getCodeTitle(topicCategory);
            topics.put(topicCategory.name(), is.toString(userLocale));
        }
        mdList.setCategories(topics);

        HashMap<String, String> dateTypes = new HashMap<>(0);
        for (int i = 0; i < DateType.values().length; i++) {
            DateType dateType = DateType.values()[i];
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

        final File root = ConfigDirectory.getDataDirectory();
        if ("root".equalsIgnoreCase(path)) {
            path = "";
            children = root.listFiles();

        } else {
            final File nextRoot = new File(root, path);
            children = nextRoot.listFiles();
        }

        //loop on subfiles/folders to create bean
        if (children != null) {
            for (int i = 0; i < children.length; i++) {
                File child = children[i];

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
     * @return A {@link Response} with 200 code if upload work, 500 if not work.
     */
    @POST
    @Path("upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(@FormDataParam("file") InputStream fileIs,
                               @FormDataParam("file") FormDataContentDisposition fileDetail,
                               @FormDataParam("metadatafile") InputStream mdFileIs,
                               @FormDataParam("metadatafile") FormDataContentDisposition mdFileDetail) {

        final File dataDirectory = ConfigDirectory.getDataDirectory();
        File newFile = new File(dataDirectory, fileDetail.getFileName());
        File mdFile = null;
        try {
            if (fileIs != null && fileDetail != null) {
                Files.copy(fileIs, newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                fileIs.close();
                if (newFile.getName().endsWith(".zip")) {
                    final String fileNameWithoutExt = newFile.getName().substring(0, newFile.getName().indexOf("."));
                    final File zipDir = new File(dataDirectory, fileNameWithoutExt);
                    FileUtilities.unzip(newFile, zipDir, new CRC32());
                    newFile = zipDir;
                }
            }

            if (mdFileIs != null && mdFileDetail != null && !mdFileDetail.getFileName().isEmpty()) {
                final File mdFolder = new File(dataDirectory, "metadata");
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

        String result = newFile.getAbsolutePath();
        if (mdFile != null) {
            result += ","+ mdFile.getAbsolutePath();
        }
        return Response.ok(result).header("X-Frame-Options", "SAMEORIGIN").build();
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

        final File root = ConfigDirectory.getDataDirectory();
        final File choosingFile = new File(root, filePath);

        File choosingMetadataFile = null;
        if (!metadataFilePath.isEmpty()) {
            choosingMetadataFile = new File(root.getAbsolutePath() + "/metadata/" + metadataFilePath);
        }

        if (choosingFile.exists()) {
            final DataInformation information = MetadataUtilities.generateMetadatasInformation(choosingFile, choosingMetadataFile, dataType);
            final int extensionPoint = filePath.lastIndexOf('.');
            final int lastSlash = filePath.lastIndexOf("/");
            final String dataName = filePath.substring(lastSlash + 1, extensionPoint);
            information.setName(dataName);
            return Response.status(200).entity(information).build();
        }
        return Response.status(418).build();
    }

    /**
     * Save metadata with merge from ISO19115 form
     *
     * @param metadataToSave {@link org.constellation.dto.DataMetadata} which contains new information for metadata.
     * @return {@link javax.ws.rs.core.Response} with code 200.
     */
    @POST
    @Path("metadata")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response saveMetadata(final DataMetadata metadataToSave) {
        //Recover metadatapyram
        DefaultMetadata dm = new DefaultMetadata();
        switch (metadataToSave.getType()) {
            case "raster":
                try {
                    dm = MetadataUtilities.getRasterMetadata(metadataToSave);
                } catch (CoverageStoreException e) {
                    LOGGER.log(Level.WARNING, "Error when try to access to metadata from data file", e);
                }
                break;
            case "vector":
                break;
            default:
                if (LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.log(Level.INFO, "Type unknown");
                    Response.status(200).build();
                }
        }

        //Update metadata
        MetadataFeeder mf = new MetadataFeeder(dm);
        mf.feed(metadataToSave);


        //Save metadata
        ConfigurationEngine.saveMetaData(dm, metadataToSave.getDataName(), CSWMarshallerPool.getInstance());
        return Response.status(200).build();
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
        final CoverageStoreProvider provider = (CoverageStoreProvider) LayerProviderProxy.getInstance().getProvider(value.getValue());
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

        final Collection<LayerProvider> providers = LayerProviderProxy.getInstance().getProviders();
        for (final LayerProvider p : providers) {
            if (type != null && !p.getDataType().equals(DataRecord.DataType.valueOf(type))) {
                continue;
            }
            for(Name n : p.getKeys()){
                final QName name = new QName(n.getNamespaceURI(), n.getLocalPart());
                final DataBrief db = ConfigurationEngine.getData(name, p.getId());
                briefs.add(db);
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

    @GET
    @Path("layer/summary/{providerid}/{layerAlias}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getLayerSummary(@PathParam("providerid") String providerid, @PathParam("layerAlias") String layerAlias) {
        final DataBrief db = ConfigurationEngine.getDataLayer(layerAlias, providerid);
        return Response.ok(db).build();
    }

    @GET
    @Path("metadata/{providerId}/{dataId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getMetadata(final @PathParam("providerId") String providerId, final @PathParam("dataId") String dataId) {

        //get reader
        final LayerProvider provider = LayerProviderProxy.getInstance().getProvider(providerId);
        final LayerDetails layer = provider.get(new DefaultName(dataId));
        final Object origin = layer.getOrigin();
        //generate DataInformation

        final DefaultMetadata metadata = ConfigurationEngine.loadProviderMetadata(providerId, CSWMarshallerPool.getInstance());
        DataInformation information = new DataInformation();
        if (layer instanceof FeatureLayerDetails) {
            final ArrayList<SimplyMetadataTreeNode> meta = MetadataUtilities.getVectorDataInformation(metadata);
            information.setFileMetadata(meta);
        } else if (layer instanceof CoverageLayerDetails) {
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
}


