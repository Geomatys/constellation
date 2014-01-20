package org.constellation.configuration;

import org.constellation.util.NodeUtilities;
import org.w3c.dom.Node;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.List;

/**
 * Extract some information from a node representing a ISO-19115 metadata.
 *
 * @author Cédric Briançon (Geomatys)
 */
@XmlRootElement(name="BriefNode")
@XmlAccessorType(XmlAccessType.FIELD)
public class BriefNode {
    @XmlTransient
    private Node node;

    private String title;

    private String createDate;

    private String creator;

    public BriefNode() {

    }

    public BriefNode(final Node node) {
        this.node = node;
        final List<String> titles = NodeUtilities.getValuesFromPath(node, "/gmd:MD_Metadata/gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString");
        if (!titles.isEmpty()) {
            title = titles.get(0);
        }
        List<String> dates = NodeUtilities.getValuesFromPath(node, "/gmd:MD_Metadata/gmd:dateStamp/gco:DateTime");
        if (dates.isEmpty()) {
            dates = NodeUtilities.getValuesFromPath(node, "/gmd:MD_Metadata/gmd:dateStamp/gco:Date");
            if (!dates.isEmpty()) {
                createDate = dates.get(0);
            }
        } else {
            createDate = dates.get(0);
        }
        final List<String> creators = NodeUtilities.getValuesFromPath(node, "/gmd:MD_Metadata/gmd:contact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString");
        if (!creators.isEmpty()) {
            creator = creators.get(0);
        }
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }
}
