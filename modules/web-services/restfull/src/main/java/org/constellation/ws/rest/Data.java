package org.constellation.ws.rest;

import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.BodyPartEntity;
import com.sun.jersey.multipart.MultiPart;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.iso.Types;
import org.apache.sis.util.logging.Logging;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.coverage.PyramidCoverageHelper;
import org.constellation.coverage.PyramidCoverageProcessListener;
import org.constellation.dto.DataInformation;
import org.constellation.dto.DataMetadata;
import org.constellation.dto.FileBean;
import org.constellation.dto.FileListBean;
import org.constellation.dto.MetadataLists;
import org.constellation.dto.ParameterValues;
import org.constellation.dto.SimpleValue;
import org.constellation.utils.GeotoolkitFileExtensionAvailable;
import org.constellation.utils.MetadataFeeder;
import org.constellation.utils.MetadataUtilities;
import org.constellation.utils.UploadUtilities;
import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.process.ProcessListener;
import org.opengis.metadata.citation.DateType;
import org.opengis.metadata.citation.Role;
import org.opengis.metadata.identification.TopicCategory;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import org.opengis.util.InternationalString;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manage data sending
 *
 * @author Benjamin Garcia (Geomatys)
 */
@Path("/1/data/")
public class Data {


    private static final Logger LOGGER = Logging.getLogger(Data.class);

    /**
     * Receive a {@link MultiPart} which contain a file need to be save on server to create data on provider
     *
     * @return A {@link Response} with 200 code if upload work, 500 if not work.
     */
    @POST
    @Path("upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(final MultiPart multi) {


        String dataType = "";
        String fileName = "";
        String metadataFileName = null;
        int extensionPoint = 0;
        InputStream uploadedInputStream = null;
        InputStream uploadedMetadataInputStream = null;

        for (BodyPart bodyPart : multi.getBodyParts()) {

            Map<String, String> cdParameter = bodyPart.getContentDisposition().getParameters();
            String name = cdParameter.get("name");
            switch (name) {
                case "file":
                    BodyPartEntity bpeData = (BodyPartEntity) bodyPart.getEntity();
                    uploadedInputStream = bpeData.getInputStream();
                    fileName = bodyPart.getContentDisposition().getFileName();
                    extensionPoint = fileName.lastIndexOf('.');
                    break;
                case "metadatafile":
                    BodyPartEntity bpeMetadata = (BodyPartEntity) bodyPart.getEntity();
                    uploadedMetadataInputStream = bpeMetadata.getInputStream();
                    metadataFileName = bodyPart.getContentDisposition().getFileName();
                    break;
                case "type":
                    dataType = bodyPart.getEntityAs(String.class);
                    break;
                default:
                    LOGGER.log(Level.INFO, "property not use");
            }
        }


        //prepare save data
        final String dataName = fileName.substring(0, extensionPoint);
        final String uploadedFileLocation = ConfigDirectory.getDataDirectory().getAbsolutePath() + "/" + dataName;
        final String uploadedFileName = uploadedFileLocation + "/" + fileName;

        //prepare save metadata
        final String uploadMetadataLocation = ConfigDirectory.getMetadataDirectory().getAbsolutePath();
        final String uploadedMetadataFileName = uploadMetadataLocation + "/" + metadataFileName;


        DataInformation information;

        // save it
        try {
            File file = UploadUtilities.writeToFile(uploadedInputStream, uploadedFileLocation, uploadedFileName);
            File metadataFile = UploadUtilities.writeToFile(uploadedMetadataInputStream, uploadMetadataLocation, uploadedMetadataFileName);
            information = MetadataUtilities.generateMetadatasInformation(file, metadataFile, dataType);
            information.setName(dataName);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error when saving file", e);
            return Response.status(500).entity("upload file " + uploadedFileLocation + " is not saved").build();
        }

        return Response.status(200).entity(information).build();
    }

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
        final FileListBean list = new FileListBean();
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
                        final FileBean bean = new FileBean(child.getName(), child.isDirectory(), path + "/" + child.getName());
                        listBean.add(bean);
                    }

                } else {
                    final FileBean bean = new FileBean(child.getName(), child.isDirectory(), path + "/" + child.getName());
                    listBean.add(bean);
                }
            }
        }
        list.setList(listBean);
        return Response.status(200).entity(list).build();
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
        String filePath = values.getValues().get("filePath");
        String dataType = values.getValues().get("dataType");

        final File root = ConfigDirectory.getDataDirectory();
        final File choosingFile = new File(root, filePath);
        if (choosingFile.exists()) {
            DataInformation information = MetadataUtilities.generateMetadatasInformation(choosingFile, null, dataType);
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
        //Recover metadata
        DefaultMetadata dm = null;
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
        final File dataFile = new File(metadataToSave.getDataPath());
        int extensionStart = dataFile.getName().lastIndexOf(".");
        String dataName = dataFile.getName().substring(0,extensionStart);
        MetadataUtilities.saveMetaData(dm, dataName);
        return Response.status(200).build();
    }

    @POST
    @Path("pyramid/{id}/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response pyramidData(@PathParam("id") final String providerId, final SimpleValue path) {

        if (path.getValue() != null) {
            final File dataFile = new File(path.getValue());

            // create folder to save pyramid
            File dataDirectory = ConfigDirectory.getDataDirectory();
            File pyramidFolder = new File(dataDirectory, "pyramid");
            if (!pyramidFolder.exists()) {
                pyramidFolder.mkdir();
            }

            final File dataPyramidFolder = new File(pyramidFolder, providerId);
            final String pyramidPath = dataPyramidFolder.getAbsolutePath();

            //create listener which save information on Database
            final ProcessListener listener = new PyramidCoverageProcessListener();

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

    @GET
    @Path("pyramid/{id}/folder/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getPyramidFolder(@PathParam("id") final String providerId) {
        // create folder to save pyramid
        File dataDirectory = ConfigDirectory.getDataDirectory();
        File pyramidFolder = new File(dataDirectory, "pyramid");
        if (!pyramidFolder.exists()) {
            pyramidFolder.mkdir();
        }
        final File dataPyramidFolder = new File(pyramidFolder, providerId);
        final SimpleValue value = new SimpleValue(dataPyramidFolder.toURI().toString());

        return Response.ok(value).build();
    }
}


