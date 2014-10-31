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

import java.util.ArrayList;
import org.constellation.util.NodeUtilities;
import org.w3c.dom.Node;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.List;
import java.util.Map;

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

    public BriefNode(final Node node, final Map<String, List<String>> fieldMapping) {
        this.node = node;

        final List<String> identifiers = new ArrayList<>();
        for (String path : fieldMapping.get("identifier")) {
            identifiers.addAll(NodeUtilities.getValuesFromPath(node, path));
        }
        if (!identifiers.isEmpty()) {
            identifier = identifiers.get(0);
        }

        final List<String> titles = new ArrayList<>();
        for (String path : fieldMapping.get("title")) {
            titles.addAll(NodeUtilities.getValuesFromPath(node, path));
        }
        if (!titles.isEmpty()) {
            title = titles.get(0);
        }
        List<String> dates = new ArrayList<>();
        for (String path : fieldMapping.get("date")) {
            dates.addAll(NodeUtilities.getValuesFromPath(node, path));
        }
        if (!dates.isEmpty()) {
            createDate = dates.get(0);
        }
        final List<String> creators = new ArrayList<>();
        for (String path : fieldMapping.get("creator")) {
            creators.addAll(NodeUtilities.getValuesFromPath(node, path));
        }
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
