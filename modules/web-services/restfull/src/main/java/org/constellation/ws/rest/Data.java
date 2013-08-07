package org.constellation.ws.rest;

import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.BodyPartEntity;
import com.sun.jersey.multipart.MultiPart;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.util.logging.Logging;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.ws.rest.post.DataInformation;
import org.geotoolkit.coverage.grid.GeneralGridGeometry;
import org.geotoolkit.coverage.io.CoverageIO;
import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.image.io.metadata.SpatialMetadata;
import org.opengis.referencing.crs.ImageCRS;
import org.opengis.util.GenericName;

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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manage data sending
 * @author Benjamin Garcia (Geomatys)
 */
@Path("/1/data")
public class Data {


    private static final Logger LOGGER = Logging.getLogger(Data.class);

    /**
     * Receive a {@link MultiPart} which contain a file need to be save on server to create data on provider
     * @param multi {@link MultiPart} with the file
     * @return A {@link Response} with 200 code if upload work, 500 if not work.
     */
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(MultiPart multi){


        String dataName = "";
        String dataType = "";
        String extension = "";
        InputStream uploadedInputStream = null;

        for (BodyPart bodyPart : multi.getBodyParts()) {

            Map<String, String> cdParameter = bodyPart.getContentDisposition().getParameters();
            String name = cdParameter.get("name");
            switch (name){
                case "file" :
                    BodyPartEntity bpe = (BodyPartEntity) bodyPart.getEntity();
                    uploadedInputStream = bpe.getInputStream();
                    String fileName = bodyPart.getContentDisposition().getFileName();
                    int extensionPoint = fileName.lastIndexOf('.');
                    extension = fileName.substring(extensionPoint);
                    break;
                case "name" :
                    dataName = bodyPart.getEntityAs(String.class);
                    break;
                case "type" :
                    //TODO : using it when generate data provider
                    dataType = bodyPart.getEntityAs(String.class);
                    break;
                default:
                    LOGGER.log(Level.INFO, "property not use");
            }
        }


        String uploadedFileLocation = ConfigDirectory.getDataDirectory().getAbsolutePath()+"/"+ dataName;
        String uploadedFileName = uploadedFileLocation+"/"+dataName+extension;
        DataInformation information  = null;

        // save it
        try {
            information = writeToFile(uploadedInputStream, uploadedFileLocation, uploadedFileName, dataType);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error when saving file", e);
            return Response.status(500).entity("upload file "+ uploadedFileLocation+" is not saved").build();
        }

        String output = "File uploaded to : " + uploadedFileLocation;
        return Response.status(200).entity(information).build();
    }

    // save uploaded file to new location
    private DataInformation writeToFile(final InputStream uploadedInputStream,
                             final String uploadedFileLocation, String uploadedFileName, String dataType) throws IOException {

        if(uploadedInputStream!=null){

            // create spécific directory for data
            File folder = new File(uploadedFileLocation);
            if(!folder.exists()){
                folder.mkdir();
            }

            int read = 0;
            byte[] bytes = new byte[4096];

            File file = new File(uploadedFileName);
            final OutputStream out = new FileOutputStream(file);
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();

            try {
                GridCoverageReader coverageReader = CoverageIO.createSimpleReader(file);
                if(!(coverageReader.getGridGeometry(0).getCoordinateReferenceSystem() instanceof ImageCRS)){

                    DataInformation information = new DataInformation(file.getPath(), dataType, (DefaultMetadata) coverageReader.getMetadata());
                    Map<String, String> nameSpatialMetadataMap = new HashMap<>(0);
                    for (int i = 0; i < coverageReader.getCoverageNames().size(); i++) {
                        GenericName name  = coverageReader.getCoverageNames().get(i);
//                        GeneralGridGeometry geometry = coverageReader.getGridGeometry(i);

                        nameSpatialMetadataMap.put(name.toString(), coverageReader.getCoverageMetadata(i).toString());
                    }
                    information.setCoveragesMetadata(nameSpatialMetadataMap);
                    return information;
                }
            } catch (CoverageStoreException e) {
                LOGGER.log(Level.WARNING, "", e);
            }
        }
        return null;
    }
}
