package org.constellation.ws.rest.post;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
@XmlRootElement
public class PortrayalContext implements Serializable {

    private String providerId;
    private String dataName;
    private String projection;
    private double[] bbox;
    private int[] size;
    private String format;
    private String sldBody;

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(final String providerId) {
        this.providerId = providerId;
    }

    public String getDataName() {
        return dataName;
    }

    public void setDataName(final String dataName) {
        this.dataName = dataName;
    }

    public String getProjection() {
        return projection;
    }

    public void setProjection(final String projection) {
        this.projection = projection;
    }

    public double[] getBbox() {
        return bbox;
    }

    public void setBbox(final double[] bbox) {
        this.bbox = bbox;
    }

    public int[] getSize() {
        return size;
    }

    public void setSize(final int[] size) {
        this.size = size;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(final String format) {
        this.format = format;
    }

    public String getSldBody() {
        return sldBody;
    }

    public void setSldBody(final String sldBody) {
        this.sldBody = sldBody;
    }
}
