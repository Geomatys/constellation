package org.constellation.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import java.util.Date;
import java.util.List;

/**
 * @author Cédric Briançon (Geomatys)
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class LayerSummary {
    @XmlElement(name = "Name")
    private String name;
    @XmlElement(name = "Alias")
    private String alias;
    @XmlElement(name = "Type")
    private String type;
    @XmlElement(name = "Date")
    private Date date;
    @XmlElement(name = "Owner")
    private String owner;
    @XmlElement(name = "Provider")
    private String provider;
    @XmlElement(name = "TargetStyle")
	private List<StyleBrief> targetStyle;

    public LayerSummary() {}

    public LayerSummary(final Layer layer, final DataBrief db) {
        this.name = layer.getName().getLocalPart();
        this.alias = layer.getAlias();
        this.type = layer.getProviderType();
        this.date = layer.getDate();
        this.owner = layer.getOwner();
        this.provider = db.getProvider();
        this.targetStyle = db.getTargetStyle();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
