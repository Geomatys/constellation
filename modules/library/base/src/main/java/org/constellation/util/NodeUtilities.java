/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.constellation.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.constellation.metadata.io.MetadataIoException;
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
                if (propertyName.equals(child.getLocalName())) {
                    results.add(child);
                }
            }
        }
        return results;
    }

    public static List<Node> getNodeFromConditionalPath(final String pathID, final String conditionalAttribute, final String conditionalValue, final Node metadata) {
        // TODO
        return new ArrayList<>();
    }

    public static List<Node> getNodeFromPath(final Node parent, String xpath) {
        //we remove the type name from the xpath
        xpath = xpath.substring(xpath.indexOf(':') + 1);

        List<Node> nodes = Arrays.asList(parent);
        while (!xpath.isEmpty()) {

            //Then we get the next Property name
            int separator = xpath.indexOf(':');
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

            nodes = getNodes(propertyName, nodes, ordinal, false);

            separator = xpath.indexOf(':');
            if (separator != -1) {
                xpath = xpath.substring(separator + 1);
            } else {
                xpath = "";
            }
        }
        return nodes;
    }

    /**
     * Return an ordinal if there is one in the propertyName specified else return -1.
     * example : name[1] return  1
     *           name    return -1
     * @param propertyName A property name extract from an Xpath
     * @return an ordinal if there is one, -1 else.
     * @throws MetadataIoException
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
}
