package org.constellation.services.web.controller;

import org.constellation.configuration.ConfigDirectory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;

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

    @RequestMapping(value = "/{mdIdentifierSHA1}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity get(@PathVariable("mdIdentifierSHA1") String mdIdentifierSHA1) {
        if(mdIdentifierSHA1 != null) {
            final File metadataCfgDir = ConfigDirectory.getMetadataDirectory();
            final File metadataFolder = new File(metadataCfgDir, mdIdentifierSHA1);
            final File quickLook = new File(metadataFolder, mdIdentifierSHA1);
            if (metadataFolder.exists()) {
                return ok(new FileSystemResource(quickLook));
            }
        }
        return null;
    }

    private <T> ResponseEntity<T> entity(T entity, HttpStatus httpStatus) {
        return new ResponseEntity<T>(entity, httpStatus);
    }

    public <T> ResponseEntity<T> ok(T entity) {
        return entity(entity, HttpStatus.OK);
    }

}
