
package webresources;

import com.sun.ws.rest.api.core.DefaultResourceConfig;


/**
 * This class was generated.
 * 
 * It is used to declare the set of root resource classes.
 * 
 */
public class WebResources
    extends DefaultResourceConfig
{


    /**
     * Declare the set of root resource classes.
     * 
     */
    public WebResources() {
        getResourceClasses().add(net.seagis.coverage.wms.WMService.class);
        getResourceClasses().add(com.sun.ws.rest.wadl.resource.WadlResource.class);
        getFeatures().put("com.sun.ws.rest.config.feature.IgnoreMatrixParams", true);
        getFeatures().put("com.sun.ws.rest.config.feature.CanonicalizeURIPath", true);
        getFeatures().put("com.sun.ws.rest.config.feature.Redirect", true);
        getFeatures().put("com.sun.ws.rest.config.feature.NormalizeURI", true);
    }

}
