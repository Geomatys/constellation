/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    private String identifier;

    private String title;

    private String createDate;

    private String creator;

    public BriefNode() {

    }

    public BriefNode(final Node node) {
        this.node = node;

        final List<String> identifiers = NodeUtilities.getValuesFromPath(node, "/gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString");
        if (!identifiers.isEmpty()) {
            identifier = identifiers.get(0);
        }

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

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
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
