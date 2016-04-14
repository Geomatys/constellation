package org.constellation.json.binding;

import org.apache.sis.util.ArgumentChecks;
import org.opengis.metadata.citation.OnlineResource;

import static org.constellation.json.util.StyleFactories.SF;

import java.net.URI;

/**
 * @author Mehdi Sidhoum (Geomatys).
 */
public class ExternalGraphic implements StyleElement<org.opengis.style.ExternalGraphic> {

    private String onlineResourceHref;
    private String format;

    public ExternalGraphic() {
    }

    public ExternalGraphic(org.opengis.style.ExternalGraphic externalGraphic) {
        ArgumentChecks.ensureNonNull("externalGraphic", externalGraphic);
        this.format = externalGraphic.getFormat();
        final OnlineResource onlineResc = externalGraphic.getOnlineResource();
        if(onlineResc!=null){
            final URI uri = onlineResc.getLinkage();
            if(uri != null) {
                this.onlineResourceHref = uri.toString();
            }
        }
    }

    public String getOnlineResourceHref() {
        return onlineResourceHref;
    }

    public void setOnlineResourceHref(String onlineResourceHref) {
        this.onlineResourceHref = onlineResourceHref;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }


    @Override
    public org.opengis.style.ExternalGraphic toType() {
        return SF.externalGraphic(onlineResourceHref,format);
    }
}
