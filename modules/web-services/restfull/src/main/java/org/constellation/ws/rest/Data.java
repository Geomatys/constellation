package org.constellation.ws.rest;

import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.BodyPartEntity;
import com.sun.jersey.multipart.MultiPart;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.util.collection.TreeTable;
import org.apache.sis.util.logging.Logging;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.utils.CoverageMetadataBean;
import org.constellation.utils.MetadataMapBuilder;
import org.constellation.utils.SimplyMetadataTreeNode;
import org.constellation.dto.DataInformation;
import org.geotoolkit.coverage.io.CoverageIO;
import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.image.io.metadata.SpatialMetadata;
import org.opengis.referencing.crs.ImageCRS;
import org.opengis.util.GenericName;
import org.w3c.dom.Node;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manage data sending
 *
 * @author Benjamin Garcia (Geomatys)
 */
@Path("/1/data")
public class Data {


    private static final Logger LOGGER = Logging.getLogger(Data.class);

    /**
     * Receive a {@link MultiPart} which contain a file need to be save on server to create data on provider
     *
     * @param multi {@link MultiPart} with the file
     * @return A {@link Response} with 200 code if upload work, 500 if not work.
     */
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(MultiPart multi) {


        String dataName = "";
        String dataType = "";
        String extension = "";
        InputStream uploadedInputStream = null;

        for (BodyPart bodyPart : multi.getBodyParts()) {

            Map<String, String> cdParameter = bodyPart.getContentDisposition().getParameters();
            String name = cdParameter.get("name");
            switch (name) {
                case "file":
                    BodyPartEntity bpe = (BodyPartEntity) bodyPart.getEntity();
                    uploadedInputStream = bpe.getInputStream();
                    String fileName = bodyPart.getContentDisposition().getFileName();
                    int extensionPoint = fileName.lastIndexOf('.');
                    extension = fileName.substring(extensionPoint);
                    break;
                case "name":
                    dataName = bodyPart.getEntityAs(String.class);
                    break;
                case "type":
                    //TODO : using it when generate data provider
                    dataType = bodyPart.getEntityAs(String.class);
                    break;
                default:
                    LOGGER.log(Level.INFO, "property not use");
            }
        }


        String uploadedFileLocation = ConfigDirectory.getDataDirectory().getAbsolutePath() + "/" + dataName;
        String uploadedFileName = uploadedFileLocation + "/" + dataName + extension;
        DataInformation information;

        // save it
        try {
            File file = writeToFile(uploadedInputStream, uploadedFileLocation, uploadedFileName);
            information = generateMetadatasInformation(file, dataType);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error when saving file", e);
            return Response.status(500).entity("upload file " + uploadedFileLocation + " is not saved").build();
        }

        return Response.status(200).entity(information).build();
    }

    /**
     * Write file with http input stream reveived
     *
     * @param uploadedInputStream  stream from client
     * @param uploadedFileLocation folder path to save file
     * @param uploadedFileName     file name
     * @return {@link File} saved
     * @throws IOException
     */
    private File writeToFile(final InputStream uploadedInputStream,
                             final String uploadedFileLocation, String uploadedFileName) throws IOException {

        if (uploadedInputStream != null) {

            // create spécific directory for data
            File folder = new File(uploadedFileLocation);
            if (!folder.exists()) {
                folder.mkdir();
            }

            int read;
            byte[] bytes = new byte[4096];

            File file = new File(uploadedFileName);
            final OutputStream out = new FileOutputStream(file);
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();

            return file;
        }
        return null;
    }

    /**
     * Generate {@link DataInformation} for require file data
     *
     * @param file     data {@link File}
     * @param dataType data type (raster, sensor or vector)
     * @return a {@link DataInformation}
     */
    private DataInformation generateMetadatasInformation(final File file, final String dataType) {
        try {
            GridCoverageReader coverageReader = CoverageIO.createSimpleReader(file);
            if (!(coverageReader.getGridGeometry(0).getCoordinateReferenceSystem() instanceof ImageCRS)) {

                // get Metadata as a List
                DefaultMetadata fileMetadata = (DefaultMetadata) coverageReader.getMetadata();
                TreeTable.Node rootNode = fileMetadata.asTreeTable().getRoot();

                MetadataMapBuilder.setCounter(0);
                ArrayList<SimplyMetadataTreeNode> metadataList = MetadataMapBuilder.createMetadataList(rootNode, null, 11);

                DataInformation information = new DataInformation(file.getPath(), dataType, metadataList);

                //coverage data
                HashMap<String, CoverageMetadataBean> nameSpatialMetadataMap = new HashMap<>(0);
                for (int i = 0; i < coverageReader.getCoverageNames().size(); i++) {
                    GenericName name = coverageReader.getCoverageNames().get(i);
                    SpatialMetadata sm = coverageReader.getCoverageMetadata(i);
                    String rootNodeName = sm.getNativeMetadataFormatName();
                    Node coverateRootNode = sm.getAsTree(rootNodeName);

                    MetadataMapBuilder.setCounter(0);
                    List<SimplyMetadataTreeNode> coverageMetadataList = MetadataMapBuilder.createSpatialMetadataList(coverateRootNode, null, 11);

                    CoverageMetadataBean coverageMetadataBean = new CoverageMetadataBean(coverageMetadataList);
                    nameSpatialMetadataMap.put(name.toString(), coverageMetadataBean);
                }
                information.setCoveragesMetadata(nameSpatialMetadataMap);
                return information;
            }
        } catch (CoverageStoreException e) {
            LOGGER.log(Level.WARNING, "", e);
        }
        return null;
    }
}
