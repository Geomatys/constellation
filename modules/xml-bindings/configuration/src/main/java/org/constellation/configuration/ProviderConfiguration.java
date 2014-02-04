package org.constellation.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cédric Briançon (Geomatys)
 */
@XmlRootElement(name = "ProviderConfiguration")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProviderConfiguration implements Serializable {
    private String type;

    private String subType;

    private Map<String, String> parameters = new HashMap<>();

    public ProviderConfiguration() {

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
}
