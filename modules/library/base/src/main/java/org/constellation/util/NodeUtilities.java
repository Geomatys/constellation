/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class NodeUtilities {

    public static List<Node> getNodes(final String propertyName, final List<Node> nodes, final int ordinal, final boolean create) {
        final List<Node> result = new ArrayList<>();
        for (Node e : nodes) {
            final List<Node> nl = getChilds(e, propertyName);
            // add new node
            if (nl.isEmpty() && create) {
                final Element newNode = e.getOwnerDocument().createElementNS("TODO", propertyName);
                e.appendChild(newNode);
                result.add(newNode);

            // Select the node to update
            } else {
                for (int i = 0 ; i < nl.size(); i++) {
                    if (ordinal == -1) {
                        result.add(nl.get(i));
                    } else if (i == ordinal) {
                        result.add(nl.get(i));
                    }
                }
            }
        }
        return result;
    }

    public static List<Node> getChilds(final Node n, final String propertyName) {
        final List<Node> results = new ArrayList<>();
        if (propertyName.startsWith("@")) {
            final Node att = n.getAttributes().getNamedItem(propertyName.substring(1));
            if (att != null) {
                results.add(att);
            }
        } else {
            final NodeList nl = n.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                final Node child = nl.item(i);
                if (propertyName.equals("*") || propertyName.equals(child.getLocalName())) {
                    results.add(child);
                }
            }
        }
        return results;
    }

    public static List<Node> getNodeFromConditionalPath(final String xpath, final String conditionalPath, final String conditionalValue, final Node metadata) {
        final List<Node> results = new ArrayList<>();
        final String[] xpart = xpath.split("/");
        final String[] cpart = conditionalPath.split("/");
        final int min = Math.min(xpart.length, cpart.length);
        int i = 0;
        String commonPath = "";
        while (xpart[i].equals(cpart[i]) && i < min) {
            commonPath += "/" + xpart[i];
           i++;
        }
        commonPath = commonPath.substring(1);
        final List<Node> nodes = getNodeFromPath(metadata, commonPath);

        for (Node n : nodes) {
            final List<Node> conditionalNode = getNodeFromPath(n, conditionalPath.substring(commonPath.length()));
            boolean match = false;
            for (Node cNode : conditionalNode) {
                if (conditionalValue.equalsIgnoreCase(cNode.getTextContent())) {
                    match = true;
                }
            }
            if (match) {
                final List<Node> matchingNodes = getNodeFromPath(n, xpath.substring(commonPath.length()));
                results.addAll(matchingNodes);
            }
        }
        return results;
    }

    public static List<Node> getNodeFromPath(final Node parent, String xpath) {
        //we remove the type name from the xpath
        xpath = xpath.substring(xpath.indexOf('/') + 1);

        List<Node> nodes = Arrays.asList(parent);
        while (!xpath.isEmpty()) {

            //Then we get the next Property name
            int separator = xpath.indexOf('/');
            String propertyName;
            if (separator != -1) {
                propertyName = xpath.substring(0, separator);
            } else {
                propertyName = xpath;
            }
            final int ordinal = extractOrdinal(propertyName);
            final int braceIndex = propertyName.indexOf('[');
            if (braceIndex != -1) {
                propertyName = propertyName.substring(0, braceIndex);
            }

            //remove namespace on propertyName
            final int separatorIndex = propertyName.indexOf(':');
            if (separatorIndex != -1) {
                propertyName = propertyName.substring(separatorIndex + 1);
            }

            nodes = getNodes(propertyName, nodes, ordinal, false);

            separator = xpath.indexOf('/');
            if (separator != -1) {
                xpath = xpath.substring(separator + 1);
            } else {
                xpath = "";
            }
        }
        return nodes;
    }

    public static List<String> getValuesFromPath(final Node parent, final String xpath) {
        return getValuesFromPaths(parent, Arrays.asList(xpath));
    }

    public static List<String> getValuesFromPaths(final Node parent, final List<String> xpaths) {
        final List<String> results = new ArrayList<>();

        for (String xpath : xpaths) {
            // verify type
            xpath = xpath.substring(xpath.indexOf(':') + 1);
            final String pathType = xpath.substring(0, xpath.indexOf('/'));
            if (!pathType.equals(parent.getLocalName())) {
                continue;
            }
            
            final List<Node> nodes = getNodeFromPath(parent, xpath);
            for (Node n : nodes) {
                results.add(n.getTextContent());
            }
        }
        return results;
    }

    public static void appendChilds(final Node parent, final List<Node> children) {
        for (Node child : children) {
            parent.appendChild(child);
        }
    }

    /**
     * Return an ordinal if there is one in the propertyName specified else return -1.
     * example : name[1] return  1
     *           name    return -1
     * @param propertyName A property name extract from an Xpath
     * @return an ordinal if there is one, -1 else.
     */
    public static int extractOrdinal(final String propertyName) {
        int ordinal = -1;

        //we extract the ordinal if there is one
        if (propertyName.indexOf('[') != -1) {
            if (propertyName.indexOf(']') != -1) {
                try {
                    final String ordinalValue = propertyName.substring(propertyName.indexOf('[') + 1, propertyName.indexOf(']'));
                    ordinal = Integer.parseInt(ordinalValue) - 1;
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("The xpath is malformed, the brackets value is not an integer");
                }
            } else {
                throw new IllegalArgumentException("The xpath is malformed, unclosed bracket");
            }
        }
        return ordinal;
    }

    public static List<Node> buildNodes(final Document doc, final String namespace, final String localName, final List<String> values, final boolean mandatory) {
        final List<Node> nodes = new ArrayList<>();
        if (mandatory && values.isEmpty()) {
            final Node n = doc.createElementNS(namespace, localName);
            nodes.add(n);
        }
        for (String value : values) {
            final Node n = doc.createElementNS(namespace, localName);
            n.setTextContent(value);
            nodes.add(n);
        }
        return nodes;
    }
}
