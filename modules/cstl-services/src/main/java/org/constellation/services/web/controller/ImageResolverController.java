package org.constellation.services.web.controller;

import org.constellation.configuration.ConfigDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.nio.file.Files;

/**
 * Spring servlet to resolve resources images for metadata thumbnails.
 * this servlet should not be secured to ensure that all images path
 * declared in metadata will always be accessible.
 *
 * @author Mehdi Sidhoum (Geomatys).
 */
@Controller
@RequestMapping("/resolveImage")
public class ImageResolverController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageResolverController.class);


    @RequestMapping(value = "/{mdIdentifierSHA1}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity get(@PathVariable("mdIdentifierSHA1") String mdIdentifierSHA1,
                              final HttpServletResponse response) {
        if(mdIdentifierSHA1 != null) {
            final File metadataCfgDir = ConfigDirectory.getMetadataDirectory();
            final File metadataFolder = new File(metadataCfgDir, mdIdentifierSHA1);
            final File quickLook = new File(metadataFolder, mdIdentifierSHA1);
            if (metadataFolder.exists()) {
                response.setCharacterEncoding("UTF-8");
                final HttpHeaders responseHeaders = new HttpHeaders();
                try {
                    //try to get the content type of the file
                    final String contentType = Files.probeContentType(quickLook.toPath());
                    final String mediaType = MediaType.valueOf(contentType).toString();
                    response.setContentType(mediaType);
                    responseHeaders.set("Content-Type", mediaType);
                }catch(Exception ex) {
                    LOGGER.warn(ex.getLocalizedMessage(), ex);
                    response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
                    responseHeaders.set("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE);
                }
                final FileSystemResource fsResource = new FileSystemResource(quickLook);
                return new ResponseEntity<>(fsResource,responseHeaders,HttpStatus.OK);
            }
        }
        return null;
    }

}
