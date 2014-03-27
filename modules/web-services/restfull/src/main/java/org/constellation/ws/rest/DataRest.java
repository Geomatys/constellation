package org.constellation.ws.rest;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
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
import org.apache.sis.geometry.GeneralDirectPosition;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.iso.Types;
import org.apache.sis.util.logging.Logging;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.admin.dao.DataRecord;
import org.constellation.admin.dao.ProviderRecord;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.configuration.DataBrief;
import org.constellation.configuration.StringList;
import org.constellation.coverage.PyramidCoverageHelper;
import org.constellation.coverage.PyramidCoverageProcessListener;
import org.constellation.dto.*;
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
import org.constellation.provider.coveragestore.CoverageStoreProvider;
import org.constellation.security.SecurityManagerHolder;
import org.constellation.util.SimplyMetadataTreeNode;
import org.constellation.utils.GeotoolkitFileExtensionAvailable;
import org.constellation.utils.MetadataFeeder;
import org.constellation.utils.MetadataUtilities;
import org.geotoolkit.coverage.AbstractGridMosaic;
import org.geotoolkit.coverage.CoverageReference;
import org.geotoolkit.coverage.CoverageReferenceRenderedImage;
import org.geotoolkit.coverage.CoverageStore;
import org.geotoolkit.coverage.CoverageUtilities;
import org.geotoolkit.coverage.GridMosaic;
import org.geotoolkit.coverage.GridMosaicCoverage2D;
import org.geotoolkit.coverage.GridSampleDimension;
import org.geotoolkit.coverage.Pyramid;
import org.geotoolkit.coverage.PyramidalCoverageReference;
import org.geotoolkit.coverage.TileReference;
import org.geotoolkit.coverage.grid.GeneralGridGeometry;
import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.coverage.grid.ViewType;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.coverage.xmlstore.XMLCoverageReference;
import org.geotoolkit.coverage.xmlstore.XMLCoverageStoreFactory;
import org.geotoolkit.csw.xml.CSWMarshallerPool;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.display.PortrayalException;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.image.interpolation.InterpolationCase;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.parameter.ParametersExt;
import org.geotoolkit.process.AbstractProcess;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessEvent;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.process.ProcessListener;
import org.geotoolkit.process.ProcessListenerAdapter;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.ReferencingUtilities;
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


    private static final Logger LOGGER = Logging.getLogger(DataRest.class);


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
            if (fileIs != null && fileDetail != null) {
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
        String result = ","+ mdFile.getAbsolutePath();
        return Response.ok(result).build();
    }


    /**
     * Import data from upload Directory to integrated directory 
     * - change file location from upload to integrated
     *
     * @param values {@link org.constellation.dto.ParameterValues} containing file path & data type
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
        		Files.move(Paths.get(filePath), Paths.get(new File(dataIntegratedDirectory.getAbsolutePath() + File.separator + new File(filePath).getName()).getAbsolutePath()),StandardCopyOption.REPLACE_EXISTING);
        	}
        	if (metadataFilePath!= null){
        		Files.move(Paths.get(metadataFilePath), Paths.get(new File(dataIntegratedDirectory.getAbsolutePath() + File.separator + new File(metadataFilePath).getName()).getAbsolutePath()),StandardCopyOption.REPLACE_EXISTING);
        	}

        	ImportedData importedData = new ImportedData();
        	if (filePath!= null){
        		importedData.setDataFile(new File(dataIntegratedDirectory.getAbsolutePath() + File.separator + new File(filePath).getName()).getAbsolutePath());
        	}
            if (metadataFilePath!= null){
            	importedData.setMetadataFile(new File(dataIntegratedDirectory.getAbsolutePath() + File.separator + new File(metadataFilePath).getName()).getAbsolutePath());
            }
            
            
            
        	return Response.ok(importedData).build();
        } catch (IOException e) {
        	LOGGER.log(Level.SEVERE, "Bad configuration for data Integrated directory", e);
        	return Response.status(500).entity("failed").build();
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
            choosingMetadataFile = new File(dataIntegratedDirectory.getAbsolutePath() + "/metadata/" + metadataFilePath);
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
        String dataPath;
        DataProvider dataProvider = DataProviders.getInstance().getProvider(metadataToSave.getDataName());
        
        
        switch (metadataToSave.getType()) {
            case "raster":
                try {
                    dm = MetadataUtilities.getRasterMetadata(dataProvider);
                } catch (DataStoreException e) {
                    LOGGER.log(Level.WARNING, "Error when trying to get coverage metadata", e);
                }
                break;
            case "vector":
                try {                
                    dm = MetadataUtilities.getVectorMetadata(dataProvider);
                } catch (DataStoreException e) {
                    LOGGER.log(Level.WARNING, "Error when trying to get metadata for a shape file", e);
                }
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
        dm.prune();
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
            
        }catch(DataStoreException ex){
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
        double[] fillValue = null;
        try{
            final GridCoverageReader reader = inRef.acquireReader();
            final List<GridSampleDimension> sampleDimensions = reader.getSampleDimensions(inRef.getImageIndex());
            if(sampleDimensions!=null){
                final int nbBand = sampleDimensions.size();
                fillValue = new double[nbBand];
                Arrays.fill(fillValue,Double.NaN);
                for(int i=0;i<nbBand;i++){
                    final double[] nodata = sampleDimensions.get(i).geophysics(true).getNoDataValues();
                    if(nodata!=null && nodata.length>0){
                        fillValue[i] = nodata[0];
                    }
                }
            }
        }catch(DataStoreException ex){
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
            public void run() {
                try {
                    p.call();
                } catch (ProcessException ex) {
                    Logger.getLogger(DataRest.class.getName()).log(Level.SEVERE, null, ex);
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

        new Thread(){
            public void run() {
                try {
                    p.call();
                } catch (ProcessException ex) {
                    Logger.getLogger(DataRest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }.start();
                        
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

            }catch(DataStoreException ex){
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
        final CoverageStoreProvider provider = (CoverageStoreProvider) DataProviders.getInstance().getProvider(value.getValue());
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

        final Collection<DataProvider> providers = DataProviders.getInstance().getProviders();
        for (final DataProvider p : providers) {
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

        final List<String> providerIds = ConfigurationEngine.getProviderIds();
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

                final QName name = new QName(data.getNamespace(), data.getName());
                final DataBrief db = ConfigurationEngine.getData(name, providerId);
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


