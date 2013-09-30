package org.constellation.gui.ajax;

import org.constellation.gui.ajax.model.SelectedExtension;
import org.constellation.utils.GeotoolkitFileExtensionAvailable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * @author Benjamin Garcia (Geomatys)
 */
@Controller
public class AjaxController {

    /**
     * WS to test extension
     * @param extension : file extension parsed by javascript client side
     * @return a {@link SelectedExtension} with defined data type
     */
    @RequestMapping(value = "/testExtension/", method = RequestMethod.POST, produces = "application/json", params = {"extension"})
    public @ResponseBody
    SelectedExtension testExtension(final @RequestParam String extension) {
        final Map<String, String> extensions = GeotoolkitFileExtensionAvailable.getAvailableFileExtension();
        final String type = extensions.get(extension.toLowerCase());

        final SelectedExtension validate = new SelectedExtension();
        validate.setExtension(extension);

        if (type != null) {
            validate.setDataType(type);
        } else {
            validate.setDataType("");
        }
        return validate;
    }
}
