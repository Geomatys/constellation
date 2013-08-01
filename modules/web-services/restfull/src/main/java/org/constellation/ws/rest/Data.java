package org.constellation.ws.rest;

import com.sun.jersey.multipart.BodyPartEntity;
import com.sun.jersey.multipart.MultiPart;
import org.apache.sis.util.logging.Logging;
import org.constellation.configuration.ConfigDirectory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

        String fileName = multi.getBodyParts().get(0).getContentDisposition().getFileName();
        BodyPartEntity bpe = (BodyPartEntity) multi.getBodyParts().get(0).getEntity();
        InputStream uploadedInputStream = bpe.getInputStream();
        int extensionPoint = fileName.lastIndexOf('.');
        String folderName = fileName.substring(0,extensionPoint);

        String uploadedFileLocation = ConfigDirectory.getDataDirectory().getAbsolutePath()+"/"+ folderName;
        String uploadedFileName = uploadedFileLocation+"/"+fileName;
        // save it
        try {
            writeToFile(uploadedInputStream, uploadedFileLocation, uploadedFileName);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error when saving file", e);
            return Response.status(500).entity("upload file "+ uploadedFileLocation+" is not saved").build();
        }

        String output = "File uploaded to : " + uploadedFileLocation;
        return Response.status(200).entity(output).build();
    }

    // save uploaded file to new location
    private void writeToFile(final InputStream uploadedInputStream,
                             final String uploadedFileLocation, String uploadedFileName) throws IOException {

        // create spécific directory for data
        File folder = new File(uploadedFileLocation);
        if(!folder.exists()){
            folder.mkdir();
        }

        int read = 0;
        byte[] bytes = new byte[4096];

        final OutputStream out = new FileOutputStream(new File(uploadedFileName));
        while ((read = uploadedInputStream.read(bytes)) != -1) {
            out.write(bytes, 0, read);
        }
        out.flush();
        out.close();
    }
}
