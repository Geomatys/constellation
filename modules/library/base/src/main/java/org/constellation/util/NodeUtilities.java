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

package org.constellation.util;

import org.apache.sis.util.logging.Logging;
import org.geotoolkit.temporal.object.TemporalUtilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class NodeUtilities {

    private static final String NULL_VALUE = "null";

    private static final Logger LOGGER = Logging.getLogger(NodeUtilities.class);

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
            if (nodes.isEmpty()) {
                return nodes;
            }

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
            if (!pathType.equals("*") && !pathType.equals(parent.getLocalName())) {
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

    /**
     * Extract the String values denoted by the specified paths
     * and return the values as a String values1,values2,....
     * if there is no values corresponding to the paths the method return "null" (the string)
     *
     * @param metadata
     * @param paths
     * @return
     */
    public static List<Object> extractValues(final Node metadata, final List<String> paths) {
        final List<Object> response  = new ArrayList<>();

        if (paths != null) {
            for (String fullPathID: paths) {

               // remove Standard
               final String pathPrefix = fullPathID.substring(1, fullPathID.indexOf(':'));
               fullPathID = fullPathID.substring(fullPathID.indexOf(':') + 1);
               final String pathType =  fullPathID.substring(0, fullPathID.indexOf('/'));
               if (!matchType(metadata, pathType, pathPrefix)) {
                   continue;
               }
                String pathID;
                String conditionalPath  = null;
                String conditionalValue = null;

                // if the path ID contains a # we have a conditional value next to the searched value.
                final int separator = fullPathID.indexOf('#');
                if (separator != -1) {
                    pathID               = fullPathID.substring(0, separator);
                    conditionalPath      = pathID + '/' + fullPathID.substring(separator + 1, fullPathID.indexOf('='));
                    conditionalValue     = fullPathID.substring(fullPathID.indexOf('=') + 1);
                    int nextSeparator    = conditionalValue.indexOf('/');
                    if (nextSeparator == -1) {
                        throw new IllegalArgumentException("A conditionnal path must be in the form ...start_path#conditional_path=value/endPath");
                    } else {
                        pathID = pathID + conditionalValue.substring(nextSeparator);
                        conditionalValue = conditionalValue.substring(0, nextSeparator);
                    }
                } else {
                    pathID = fullPathID;
                }

                int ordinal = -1;
                if (pathID.endsWith("]") && pathID.indexOf('[') != -1) {
                    try {
                        ordinal = Integer.parseInt(pathID.substring(pathID.lastIndexOf('[') + 1, pathID.length() - 1));
                    } catch (NumberFormatException ex) {
                        LOGGER.warning("Unable to parse last path ordinal");
                    }
                }
                final List<Node> nodes;
                if (conditionalPath == null) {
                    nodes = getNodeFromPath(metadata, pathID);
                } else {
                    nodes  = getNodeFromConditionalPath(pathID, conditionalPath, conditionalValue, metadata);
                }
                final List<Object> value = getStringValue(nodes, ordinal);
                if (!value.isEmpty() && !value.equals(Arrays.asList(NULL_VALUE))) {
                    response.addAll(value);
                }
            }
        }
        if (response.isEmpty()) {
            response.add(NULL_VALUE);
        }
        return response;
    }

    /**
     * Return a String value from the specified Object.
     * Let the number object as Number
     *
     * @param obj
     * @return
     */
    private static List<Object> getStringValue(final List<Node> nodes, final int ordinal) {
        final List<Object> result = new ArrayList<>();
        if (nodes != null && !nodes.isEmpty()) {
            for (Node n : nodes) {
                final String s = n.getTextContent();
                final String typeName = n.getLocalName();
                if (typeName == null) {
                    result.add(s);
                } else if (typeName.equals("Real") || typeName.equals("Decimal")) {
                    try {
                        result.add(Double.parseDouble(s));
                    } catch (NumberFormatException ex) {
                        LOGGER.log(Level.WARNING, "Unable to parse the real value:{0}", s);
                    }
                } else if (typeName.equals("Integer")) {
                    try {
                        result.add(Integer.parseInt(s));
                    } catch (NumberFormatException ex) {
                        LOGGER.log(Level.WARNING, "Unable to parse the integer value:{0}", s);
                    }
                } else if (typeName.equals("Date") || typeName.equals("DateTime") ||
                           typeName.equals("position") || typeName.equals("beginPosition") ||
                           typeName.equals("endPosition")) {
                    try {
                        final Date d = TemporalUtilities.getDateFromString(s);
                        synchronized (Util.LUCENE_DATE_FORMAT) {
                            result.add(Util.LUCENE_DATE_FORMAT.format(d));
                        }
                    } catch (ParseException ex) {
                        LOGGER.log(Level.WARNING, "Unable to parse the date value:{0}", s);
                    }
                } else if (typeName.endsWith("Corner")) {
                    if (ordinal != -1) {
                        final String[] parts = s.split(" ");
                        if (ordinal < parts.length) {
                            result.add(parts[ordinal]);
                        }
                    } else {
                        result.add(s);
                    }
                } else if (s != null) {
                    result.add(s);
                }
            }
        }
        if (result.isEmpty()) {
            result.add(NULL_VALUE);
        }

        /*if (obj instanceof Position) {
            final Position pos = (Position) obj;
            final Date d = pos.getDate();
            if (d != null) {
                synchronized(LUCENE_DATE_FORMAT) {
                    result.add(LUCENE_DATE_FORMAT.format(d));
                }
            } else {
               result.add(NULL_VALUE);
            }

        } else if (obj instanceof Instant) {
            final Instant inst = (Instant)obj;
            if (inst.getPosition() != null && inst.getPosition().getDate() != null) {
                synchronized(LUCENE_DATE_FORMAT) {
                    result.add( LUCENE_DATE_FORMAT.format(inst.getPosition().getDate()));
                }
            } else {
                result.add(NULL_VALUE);
            }
        } else if (obj instanceof Date) {
            synchronized (LUCENE_DATE_FORMAT){
                result.add(LUCENE_DATE_FORMAT.format((Date)obj));
            }

        } else {
            throw new IllegalArgumentException("this type is unexpected: " + obj.getClass().getSimpleName());
        }*/
        return result;
    }

    private static boolean matchType(final Node n, final String type, final String prefix) {
        final String namespace = XpathUtils.getNamespaceFromPrefix(prefix);
        return (type.equals(n.getLocalName()) || type.equals("*")) && namespace.equals(n.getNamespaceURI());
    }
}
