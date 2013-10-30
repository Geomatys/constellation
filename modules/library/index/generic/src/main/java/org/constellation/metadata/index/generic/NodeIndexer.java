/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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

package org.constellation.metadata.index.generic;

// J2SE dependencies
import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

// Apache Lucene dependencies
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

// constellation dependencies
import org.constellation.metadata.index.AbstractCSWIndexer;
import org.constellation.metadata.index.XpathUtils;
import org.constellation.metadata.io.MetadataReader;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.metadata.io.MetadataType;
import org.constellation.metadata.utils.Utils;
import org.constellation.util.NodeUtilities;

// geotoolkit dependencies
import org.geotoolkit.lucene.IndexingException;
import org.geotoolkit.temporal.object.TemporalUtilities;

// GeoAPI dependencies
import org.w3c.dom.Node;


/**
 * A Lucene Index Handler for a generic Database.
 * @author Guilhem Legal
 */
public class NodeIndexer extends AbstractCSWIndexer<Node> {

    /**
     * The Reader of this lucene index (generic DB mode).
     */
    private final MetadataReader reader;

    /**
     * Creates a new Lucene Index into the specified directory with the specified generic database reader.
     *
     * @param reader A generic reader to request the metadata dataSource.
     * @param configurationDirectory The directory where the index can write indexation file.
     * @param serviceID The identifier, if there is one, of the index/service.
     * @param additionalQueryable A map of additional queryable element.
     */
    public NodeIndexer(final MetadataReader reader, final File configurationDirectory, final String serviceID,
            final Map<String, List<String>> additionalQueryable, final boolean create) throws IndexingException {
        super(serviceID, configurationDirectory, additionalQueryable);
        this.reader = reader;
        if (create && needCreation()) {
            createIndex();
        }
    }

    /**
     * Creates a new Lucene Index into the specified directory with the specified list of object to index.
     *
     * @param configDirectory A directory where the index can write indexation file.
     */
    public NodeIndexer(final List<Node> toIndex, final Map<String, List<String>> additionalQueryable, final File configDirectory,
            final String serviceID, final Analyzer analyzer, final Level logLevel, final boolean create) throws IndexingException {
        super(serviceID, configDirectory, analyzer, additionalQueryable);
        this.logLevel            = logLevel;
        this.reader              = null;
        if (create && needCreation()) {
            createIndex(toIndex);
        }
    }

    /**
     * Creates a new Lucene Index into the specified directory with the specified list of object to index.
     *
     * @param configDirectory A directory where the index can write indexation file.
     */
    public NodeIndexer(final List<Node> toIndex, final Map<String, List<String>> additionalQueryable, final File configDirectory,
            final String serviceID, final boolean create) throws IndexingException {
        super(serviceID, configDirectory, additionalQueryable);
        this.reader = null;
        if (create && needCreation()) {
            createIndex(toIndex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> getAllIdentifiers() throws IndexingException {
        try {
            return reader.getAllIdentifiers();
        } catch (MetadataIoException ex) {
            throw new IndexingException("Metadata_IOException while reading all identifiers", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Node getEntry(final String identifier) throws IndexingException {
        try {
            return (Node) reader.getMetadata(identifier, MetadataType.NATIVE);
        } catch (MetadataIoException ex) {
            throw new IndexingException("Metadata_IOException while reading entry for:" + identifier, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void indexSpecialField(final Node metadata, final Document doc) throws IndexingException {
        final String identifier = getIdentifier(metadata);
        if ("unknow".equals(identifier)) {
            throw new IndexingException("unexpected metadata type.");
        }
        doc.add(new Field("id", identifier,  ftna));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getType(final Node metadata) {
        return metadata.getLocalName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isISO19139(final Node meta) {
        return "MD_Metadata".equals(meta.getLocalName()) ||
               "MI_Metadata".equals(meta.getLocalName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isDublinCore(final Node meta) {
        return "Record".equals(meta.getLocalName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isEbrim25(final Node meta) {
        // TODO list rootElement
        return "RegistryObject".equals(meta.getLocalName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isEbrim30(final Node meta) {
        // TODO list rootElement
        return "Identifiable".equals(meta.getLocalName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isFeatureCatalogue(Node meta) {
        return "FC_FeatureCatalogue".equals(meta.getLocalName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void indexQueryableSet(final Document doc, final Node metadata, final  Map<String, List<String>> queryableSet, final StringBuilder anyText) throws IndexingException {
        for (final String term : queryableSet.keySet()) {
            final TermValue tm = new TermValue(term, extractValues(metadata, queryableSet.get(term)));

            final NodeIndexer.TermValue values = formatStringValue(tm);
            indexFields(values.value, values.term, anyText, doc);
        }
    }

    /**
     * Format the value part in case of a "date" term.
     * @param values
     * @return
     */
    private TermValue formatStringValue(final TermValue values) {
         if ("date".equals(values.term)) {
             final List<Object> newValues = new ArrayList<>();
             for (Object value : values.value) {
                 if (value instanceof String) {
                     String stringValue = (String) value;
                     if (stringValue.endsWith("z") || stringValue.endsWith("Z")) {
                         stringValue = stringValue.substring(0, stringValue.length() - 1);
                     }
                     if (stringValue != null) {
                        stringValue = stringValue.replace("-", "");
                        //add time if there is no
                        if (stringValue.length() == 8) {
                            stringValue = stringValue + "000000";
                        }
                        value = stringValue;
                     }
                 }
                newValues.add(value);
             }
             values.value = newValues;
         }
         return values;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getIdentifier(final Node obj) {
        return Utils.findIdentifier(obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    protected String getValues(final Node metadata, final List<String> paths) {
        final List<Object> values =  extractValues(metadata, paths);
        final StringBuilder sb = new StringBuilder();
        for (Object value : values) {
            sb.append(value).append(',');
        }
        if (!sb.toString().isEmpty()) {
            // we remove the last ','
            sb.delete(sb.length() - 1, sb.length());
        }
        return sb.toString();
    }

    private static boolean matchType(final Node n, final String type, final String prefix) {
        final String namespace = XpathUtils.getNamespaceFromPrefix(prefix);
        return (type.equals(n.getLocalName()) || type.equals("*")) && namespace.equals(n.getNamespaceURI());
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
                    nodes = NodeUtilities.getNodeFromPath(metadata, pathID);
                } else {
                    nodes  = NodeUtilities.getNodeFromConditionalPath(pathID, conditionalPath, conditionalValue, metadata);
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
                        final Date d = TemporalUtilities.parseDate(s, true);
                        synchronized (LUCENE_DATE_FORMAT) {
                            result.add(LUCENE_DATE_FORMAT.format(d));
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

    @Override
    public void destroy() {
        LOGGER.info("shutting down Node indexer");
        super.destroy();
    }

    private static class TermValue {
        public String term;

        public List<Object> value;

        public TermValue(String term, List<Object> value) {
            this.term  = term;
            this.value = value;
        }
    }
}
