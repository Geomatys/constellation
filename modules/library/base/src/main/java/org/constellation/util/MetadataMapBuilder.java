/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.util;

import org.apache.sis.util.collection.TableColumn;
import org.apache.sis.util.collection.TreeTable;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to create some {@link org.constellation.util.SimplyMetadataTreeNode} {@link java.util.List} with required metadata.
 * Can work with {@link TreeTable.Node} and {@link Node} with own method.
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 */
public class MetadataMapBuilder {

    /**
     * static counter used only to generate html div id (to know have generation problem when some node have same name)
     */
    private static int counter;

    /**
     * {@link org.constellation.util.SimplyMetadataTreeNode} {@link ArrayList} generator with a {@link TreeTable.Node}.
     *
     *
     * @param rootNode           {@link org.apache.sis.util.collection.TreeTable.Node} metadata tree root
     * @param parentTreeNodeName parent div id name
     * @param parentDepth        parent div depth
     * @return a {@link org.constellation.util.SimplyMetadataTreeNode} {@link ArrayList}
     */
    public static ArrayList<SimplyMetadataTreeNode> createMetadataList(final TreeTable.Node rootNode, final String parentTreeNodeName, final int parentDepth) {
        ArrayList<SimplyMetadataTreeNode> metadataList = new ArrayList<>(0);
        SimplyMetadataTreeNode smtn = new SimplyMetadataTreeNode();

        //remove all non alphanumeric character from real name for html generation
        String nameAlphaNumeric = rootNode.getValue(TableColumn.NAME).toString().replaceAll("[^A-Za-z0-9]", "") + counter;

        //set main attributs
        smtn.setName(rootNode.getValue(TableColumn.NAME).toString());
        smtn.setNameNoWhiteSpace(nameAlphaNumeric);
        smtn.setChildrenExist(!rootNode.isLeaf());

        //only leaf have value.
        Object value = rootNode.getValue(TableColumn.VALUE);
        if (value != null) {
            smtn.setValue(value.toString());
        }

        int depthSpan = 0;

        if (parentTreeNodeName != null) {
            //all children
            depthSpan = parentDepth - 1;
            smtn.setDepthSpan(depthSpan);
            smtn.setParentName(parentTreeNodeName);
            metadataList.add(smtn);
        } else {
            //root
            depthSpan = 11;
            smtn.setDepthSpan(depthSpan);
            metadataList.add(smtn);
        }

        //recursive loop on children
        for (TreeTable.Node node : rootNode.getChildren()) {
            counter++;
            metadataList.addAll(createMetadataList(node, nameAlphaNumeric, depthSpan));
        }
        return metadataList;
    }

    /**
     * {@link org.constellation.util.SimplyMetadataTreeNode} {@link List} generator with a {@link Node}.
     *
     *
     * @param rootNode           {@link org.w3c.dom.Node} metadata tree root
     * @param parentTreeNodeName parent div id name
     * @param parentDepth        parent div depth
     * @param listCounter
     * @return a {@link org.constellation.util.SimplyMetadataTreeNode} {@link List}
     */
    public static List<SimplyMetadataTreeNode> createSpatialMetadataList(final Node rootNode, final String parentTreeNodeName, final int parentDepth, final int listCounter) {
        List<SimplyMetadataTreeNode> metadataList = new ArrayList<>(0);
        SimplyMetadataTreeNode smtn = new SimplyMetadataTreeNode();

        //remove all non alphanumeric character from real name for html generation
        String nameAlphaNumeric = rootNode.getNodeName().replaceAll("[^A-Za-z0-9]", "") + listCounter + MetadataMapBuilder.counter;

        //set main attributs
        smtn.setName(rootNode.getNodeName());
        smtn.setNameNoWhiteSpace(nameAlphaNumeric);
        smtn.setChildrenExist(rootNode.hasChildNodes() || rootNode.hasAttributes());

        //only leaf have value.
        String value = rootNode.getNodeValue();
        if (value != null) {
            smtn.setValue(value);
        }
        int depthSpan = 0;

        if (parentTreeNodeName != null) {
            //all children
            depthSpan = parentDepth - 1;
            smtn.setDepthSpan(depthSpan);
            smtn.setParentName(parentTreeNodeName);
            metadataList.add(smtn);
        } else {
            //root
            depthSpan = 11;
            smtn.setDepthSpan(depthSpan);
            metadataList.add(smtn);
        }

        //recursive loop on attributes
        NamedNodeMap attrMap = rootNode.getAttributes();
        for (int i = 0; i < attrMap.getLength(); i++) {
            Node node = attrMap.item(i);
            MetadataMapBuilder.counter++;
            metadataList.addAll(createSpatialMetadataList(node, nameAlphaNumeric, depthSpan, listCounter));
        }

        //recursive loop on children
        NodeList nodeList = rootNode.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            MetadataMapBuilder.counter++;
            metadataList.addAll(createSpatialMetadataList(node, nameAlphaNumeric, depthSpan, listCounter));
        }
        return metadataList;
    }

    public static int getCounter() {
        return counter;
    }

    public static void setCounter(final int counter) {
        MetadataMapBuilder.counter = counter;
    }
}
