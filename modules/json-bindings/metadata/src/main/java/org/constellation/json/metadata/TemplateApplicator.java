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
package org.constellation.json.metadata;

import java.util.Date;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import org.opengis.metadata.Identifier;
import org.opengis.metadata.Metadata;
import org.opengis.temporal.Position;
import org.opengis.temporal.Instant;
import org.opengis.temporal.Period;
import org.opengis.temporal.TemporalPrimitive;
import org.opengis.referencing.ReferenceSystem;
import org.apache.sis.metadata.MetadataStandard;
import org.apache.sis.metadata.KeyNamePolicy;
import org.apache.sis.metadata.TypeValuePolicy;
import org.apache.sis.metadata.ValueExistencePolicy;


/**
 * Creates the tree of value to writes as a JSON file.
 *
 * <h3>Design problem:</h3>
 * Templates may have two or more nodes with the same path. For example we may have:
 *
 * <blockquote><pre>
 * {
 *     "block":{
 *         "multiplicity":1,
 *         "path":null
 *         "children":[{
 *             "field":{
 *                  "multiplicity":1,
 *                 "path":"identificationInfo.descriptiveKeywords.keyword"
 *             }
 *         },{
 *             ...etc...
 *         },{
 *             "field":{
 *                 "multiplicity":1,
 *                 "path":"identificationInfo.topicCategory"
 *             }
 *         }
 *     ]}
 * },{
 *     "block":{
 *         "multiplicity":60,
 *         "path":"identificationInfo.descriptiveKeywords",
 *         "children":[{
 *             "field":{
 *                 "multiplicity":60,
 *                 "path":"identificationInfo.descriptiveKeywords.keyword"
 *             }
 *         },{
 *             ...etc...
 *         }
 *     ]}
 * }
 * </pre></blockquote>
 *
 * In the above example, the same path ("identificationInfo.descriptiveKeywords.keyword") is repeated twice.
 * The first block will show only the first occurrence (because of "multiplicity":1) while the second block
 * will show all occurrences.  It may be desirable to omit from the second block all elements already shown
 * in the first block. However this objective raises some tricky issues:
 *
 * 1) Shall we omit only the first "keyword", or the first "descriptiveKeywords" (thus loosing any keywords
 *    after the first one in the first "descriptiveKeywords"), or the first "identificationInfo" instance?
 *    Omitting only the first "keyword" would probably be confusing for the user. Omitting the first block
 *    "descriptiveKeyword" may be closer to our intend, but there is nothing in the above template telling
 *    us that. This is because the first block contains an element ("topicCategory") which is normally not
 *    part of descriptive keywords, so that block is a mix of information from different places.
 *
 * 2) Omitting elements requires that we take trace of remaining elements after we have show some of them.
 *    We can do that with the TemplateApplication.remainingValues hash map. This map requires distinct keys
 *    for the same path applied on different instances of a metadata value. For example the two following
 *    paths are distinct:
 *
 *      - identificationInfo[0].descriptiveKeywords[0].keyword
 *      - identificationInfo[0].descriptiveKeywords[1].keyword
 *
 *    This is handled by the NumerotedPath class. When an element has been shown, we need to check if the
 *    parent element became empty. This can be handled by a 'prune' operation applied after we created the
 *    tree (in comparison, in the simpler version it is possible to prune on-the-fly, without a need for a
 *    post-operation). Consequently handling of such special cases make the code much more complex.
 *
 *
 * For avoiding the complexity of the above, we currently do not try to auto-detect the properties to omit.
 * instead, we provide an explicit {@link TemplateNode#ignore} attribute. However this is not a satisfying
 * solution.
 *
 * @author Martin Desruisseaux (Geomatys)
 */
final class TemplateApplicator {
    /**
     * A path to be handled in a special way.
     */
    private static final String[] REFERENCE_SYSTEM_CODE = {"referenceSystemIdentifier", "code"};
    
    private static final String[] REFERENCE_SYSTEM_CODESPACE = {"referenceSystemIdentifier", "codeSpace"};
    
    private static final String[] REFERENCE_SYSTEM_VERSION = {"referenceSystemIdentifier", "version"};

    /**
     * {@code true} for omitting empty nodes.
     */
    private final boolean prune;

    /**
     * The 1-based index of each elements in the path, or 0 for values that are not collections.
     * Those indices will be incremented as we iterate in the metadata tree.
     */
    private final int[] indices;

    /**
     * A temporary list used when building a list of nodes for all values at a path.
     */
    private final List<ValueNode> nodes;

    /**
     * Creates a new writer.
     *
     * @param prune    {@code true} for omitting empty nodes.
     * @param maxDepth The maximal length of {@link TemplateNode#path}.
     */
    TemplateApplicator(final boolean prune, final int maxDepth) {
        this.prune   = prune;
        this.indices = new int[maxDepth];
        this.nodes   = new ArrayList<>();
    }

    /**
     * Builds a tree of values for the given node and all its children nodes.
     *
     * @param  metadata   The metadata from which to get the values.
     * @return The roots of tree nodes created by this method (not necessarily the root of the whole tree to be
     *         written), or {@code null} if none. The array may contain null elements, which shall be ignored.
     */
    final ValueNode[] createValueTree(final TemplateNode template, final Object metadata) throws ParseException {
        return createValueTree(template, null, metadata, 0);
    }

    /**
     * Builds a tree of values for the given node and all its children nodes.
     * The given {@code metadata} argument shall be one of the following:
     *
     * <ul>
     *   <li>The metadata instance.</li>
     *   <li>If the metadata instance is unknown, then the base {@link Class} of expected metadata instances.
     *       There is not risk of confusion between {@code Class} instances and metadata instances because
     *       {@code Class} can not implement a GeoAPI interface.</li>
     *   <li>If even the base {@code Class} is unknown, then {@code null}.</li>
     * </ul>
     *
     * <p>This method invokes itself recursively.</p>
     *
     * @param  sibling    The node just before this one, or {@code null} if none.
     * @param  metadata   The metadata from which to get the values, or the expected base {@code Class}, or {@code null}.
     * @param  pathOffset Index of the first {@link #path} element to use.
     * @return The roots of tree nodes created by this method (not necessarily the root of the whole tree to be
     *         written), or {@code null} if none. The array may contain null elements, which shall be ignored.
     */
    private ValueNode[] createValueTree(final TemplateNode template, ValueNode sibling,
            final Object metadata, int pathOffset) throws ParseException
    {
        if (template.path == null) {
            /*
             * If this node does not declare any path, we can not get a metadata value for this node.
             * However maybe some chidren may have a path allowing them to fetch metadata values.
             */
            sibling = null;
            ValueNode node = null;
            for (final TemplateNode child : template.children) {
                final ValueNode[] tree = createValueTree(child, sibling, metadata, pathOffset);
                node = addTo(template, node, tree);
                sibling = last(tree);
            }
            return (node != null) ? new ValueNode[] {node} : null;
        }
        /*
         * If this node declares a path, then get the values for this node. The values may be other
         * metadata objects, in which case we will need to invoke this method recursively for them.
         */
        nodes.clear();
        final boolean isMetadataInstance = (metadata != null) && !(metadata instanceof Class<?>);
        if (isMetadataInstance) {
            try {
                getValues(template, metadata, pathOffset);
            } catch (ClassCastException e) {
                final StringBuilder buffer = new StringBuilder("Illegal path: ");
                template.appendPath(pathOffset, buffer);
                throw new ParseException(buffer.append('.').toString(), e);
            }
            filterIgnoredValues(template);
        }
        /*
         * If there is no value and the user asked us to prune empty nodes,
         * returns 'null' immediately (avoid the creation of an array).
         */
        if (nodes.isEmpty()) {
            if (prune) {
                return null;
            }
            /*
             * If there is no value, write anyway if the user asked us to not prune empty nodes.
             * We will format the default values, which may be {@code null}. But before to create
             * new node for default values, we will need to add the "[1]" indice in the JSON path
             * when the expected type is a collection.
             */
            final int incrementFrom;
            final int pathEnd = template.path.length;
            if (sibling != null && Arrays.equals(template.path, sibling.template.path)) {
                System.arraycopy(sibling.indices, pathOffset, indices, pathOffset, pathEnd);
                incrementFrom = pathEnd - 1; // 'do … while' below will increment only the last indice.
            } else {
                Arrays.fill(indices, pathOffset, pathEnd, 0); // Initialize to "not a collection".
                incrementFrom = pathOffset;
            }
            Class<?> type = null;
            if (metadata != null) {
                type = isMetadataInstance ? metadata.getClass() : (Class<?>) metadata; // See method javadoc.
                do {
                    final String identifier = template.path[pathOffset];
                    final Class<?> propertyType;
                    try {
                        propertyType = getType(template, type, TypeValuePolicy.PROPERTY_TYPE, identifier);
                    } catch (ClassCastException e) {
                        break; // Same than for unknown properties (see following block).
                    }
                    if (propertyType == null) {
                        /*
                         * May happen with non-standard or unsupported properties. We can not continue further.
                         * The 'Arrays.fill(…)' above had set remaining indices to 0, which means that we treat
                         * unknown properties as singletons.
                         */
                        break;
                    }
                    if (Collection.class.isAssignableFrom(propertyType)) {
                        if (pathOffset >= incrementFrom) {
                            indices[pathOffset]++;
                        }
                        type = getType(template, type, TypeValuePolicy.ELEMENT_TYPE, identifier);
                    } else {
                        type = propertyType; // Should be equivalent to a call to 'getType(…)', but much cheaper.
                    }
                } while (++pathOffset < pathEnd);
            }
            if (template.isField()) {
                nodes.add(new ValueNode(template, indices, template.defaultValue));
                // To be formatted below like ordinary values.
            } else {
                sibling = null;
                ValueNode node = null;
                for (final TemplateNode child : template.children) {
                    final ValueNode[] tree = createValueTree(child, sibling, type, pathEnd);
                    node = addTo(template, node, tree);
                    sibling = last(tree);
                }
                return new ValueNode[] {node}; // Unlikely to be null, but still allowed.
            }
        }
        /*
         * If we have a value and this node is a field, returns the value.
         * Otherwise delegate to the child nodes for creating sub-trees.
         * Note that we need to copy the nodes in an array before to invoke
         * the 'createValueTree' method recursively.
         */
        final ValueNode[] na = nodes.toArray(new ValueNode[nodes.size()]);
        if (!template.isField()) {
            pathOffset = template.path.length;
            for (int i=0; i<na.length; i++) {
                sibling = null;
                ValueNode node = na[i];
                System.arraycopy(node.indices, 0, indices, 0, node.indices.length);
                for (final TemplateNode child : template.children) {
                    final ValueNode[] tree = createValueTree(child, sibling, node.value, pathOffset);
                    node = addTo(template, node, tree);
                    sibling = last(tree);
                }
                na[i] = node; // As a matter of principle, but the reference should be the same.
            }
        }
        return na;
    }

    /**
     * Returns the last element of the given tree, or {@code null} if none.
     */
    private static ValueNode last(final ValueNode[] tree) {
        if (tree != null) {
            for (int i=tree.length; --i>=0;) {
                final ValueNode node = tree[i];
                if (node != null) return node;
            }
        }
        return null;
    }

    /**
     * Fetches all occurrences of metadata values at the path given by {@link TemplateNode#path}.
     * This method searches only the metadata values for the given {@code TemplateNode} - it does
     * not perform any search for children {@code TemplateNode}s.
     * The values are added to the {@link #nodes} list.
     *
     * <p>This method invokes itself recursively.</p>
     *
     * @param  metadata   The metadata from where to get the values.
     * @param  pathOffset Index of the first {@code path} element to use.
     * @throws ClassCastException if {@code metadata} is not an instance of the expected standard.
     */
    private void getValues(final TemplateNode template, Object metadata, int pathOffset) throws ParseException, ClassCastException {
        Objects.requireNonNull(template.path);
        Objects.requireNonNull(metadata);
        Object value;
        do {
            if (pathOffset >= template.path.length) {
                final StringBuilder paths = new StringBuilder();
                for (String p : template.path) {
                    paths.append('\n').append(p);
                }
                throw new ParseException("Path offset out of band :" + paths);
            }
            final String identifier = template.path[pathOffset];
            if (identifier.equals("referenceSystemInfo") && template.endsWith(REFERENCE_SYSTEM_CODE) && metadata instanceof Metadata) {
                value = referenceSystemCode((Metadata) metadata); // Special case.
            } else if (identifier.equals("referenceSystemInfo") && template.endsWith(REFERENCE_SYSTEM_CODESPACE) && metadata instanceof Metadata) {
                value = referenceSystemCodeSpace((Metadata) metadata); // Special case.
            } else if (identifier.equals("referenceSystemInfo") && template.endsWith(REFERENCE_SYSTEM_VERSION) && metadata instanceof Metadata) {
                value = referenceSystemVersion((Metadata) metadata); // Special case.
            } else if (metadata instanceof TemporalPrimitive) {
                value = extent((TemporalPrimitive) metadata, identifier); // Special case.
            } else {
                value = template.standard.asValueMap(metadata, KeyNamePolicy.UML_IDENTIFIER, ValueExistencePolicy.NON_EMPTY).get(identifier);
            }
            if (value == null) {
                return;
            }
            /*
             * Verify if the value is a collection. We do not rely on (value instanceof Collection)
             * only because it may not be reliable if the value implements more than one interface.
             * Instead, we rely on the method contract.
             */
            if (value instanceof Collection<?> && isCollection(template.standard, metadata, identifier)) {
                final boolean isField = (++pathOffset >= template.path.length);
                final Object[] values = ((Collection<?>) value).toArray();
                for (int i=0; i<values.length; i++) {
                    indices[pathOffset - 1] = i + 1;
                    value = values[i];
                    if (isField) {
                        nodes.add(new ValueNode(template, indices, value));
                    } else if (value != null) {
                        getValues(template, value, pathOffset);
                    }
                }
                return;
            }
            /*
             * The value is not a collection. Continue the loop for each components in the path. For example
             * if the path is "identificationInfo.extent.geographicElement.southBoundLatitude", then the loop
             * would be executed for "identificationInfo", then "extent", etc. if all components were singleton.
             */
            indices[pathOffset] = 0; // 0 means "not a collection".
            metadata = value;
        } while (++pathOffset < template.path.length);
        nodes.add(new ValueNode(template, indices, value));
    }

    /**
     * Given a newly computed set of {@link #nodes}, separates the value to use for the given {@code TemplateNode}.
     *
     * <p>The current version only ensures that the number of elements is not greater than {@link TemplateNode#maxOccurs}.
     * However if we want to apply a more sophisticated filter in a future version, it could be applied here.</p>
     */
    private void filterIgnoredValues(final TemplateNode template) {
        final int size      = nodes.size();
        final int maxOccurs = template.maxOccurs;
        if (size > maxOccurs) {
            nodes.subList(maxOccurs, size).clear();
        }
        final NumerotedPath ignore = template.ignore;
        if (ignore != null) {
            final Iterator<ValueNode> it = nodes.iterator();
            while (it.hasNext()) {
                final ValueNode node = it.next();
                if (node.pathEquals(ignore)) {
                    it.remove();
                }
            }
        }
    }

    /**
     * Adds the non-null children to the given parent.
     * The parent will be created when first needed.
     *
     * @param  parent   The parent, or {@code null} if not yet created.
     * @param  children The children to add to the parent, or {@code null}.
     * @return The parent, newly created if the given {@code parent} was null.
     */
    private ValueNode addTo(final TemplateNode template, ValueNode parent, final ValueNode[] children) {
        if (children != null) {
            for (final ValueNode child : children) {
                if (child != null) {
                    if (parent == null) {
                        parent = new ValueNode(template, indices, null);
                    }
                    parent.add(child);
                }
            }
        }
        return parent;
    }

    /**
     * Returns the type of a metadata property.
     */
    private static Class<?> getType(final TemplateNode template, final Class<?> type, final TypeValuePolicy policy, final String identifier) {
        return template.standard.asTypeMap(type, KeyNamePolicy.UML_IDENTIFIER, policy).get(identifier);
    }

    /**
     * Returns {@code true} if the given property of the given metadata is a collection according the method contract.
     * We do not rely only on {@code (value instanceof Collection)} because it may not be reliable if the value
     * implements more than one interface.
     */
    static boolean isCollection(final MetadataStandard standard, final Object metadata, final CharSequence identifier) {
        return Collection.class.isAssignableFrom(standard.asTypeMap(metadata.getClass(),
                KeyNamePolicy.UML_IDENTIFIER, TypeValuePolicy.PROPERTY_TYPE).get(identifier));
    }

    /**
     * Special case for {@link #REFERENCE_SYSTEM_CODE}.
     */
    private static String referenceSystemCode(final Metadata metadata) {
        for (final ReferenceSystem r : metadata.getReferenceSystemInfo()) {
            for (final Identifier id : r.getIdentifiers()) {
                final String code = id.getCode();
                if (code != null) return code;
            }
        }
        return null;
    }
    
    /**
     * Special case for {@link #REFERENCE_SYSTEM_CODESPACE}.
     */
    private static String referenceSystemCodeSpace(final Metadata metadata) {
        for (final ReferenceSystem r : metadata.getReferenceSystemInfo()) {
            for (final Identifier id : r.getIdentifiers()) {
                final String code = id.getCodeSpace();
                if (code != null) return code;
            }
        }
        return null;
    }
    
    /**
     * Special case for {@link #REFERENCE_SYSTEM_VERSION}.
     */
    private static String referenceSystemVersion(final Metadata metadata) {
        for (final ReferenceSystem r : metadata.getReferenceSystemInfo()) {
            for (final Identifier id : r.getIdentifiers()) {
                final String code = id.getVersion();
                if (code != null) return code;
            }
        }
        return null;
    }

    /**
     * Special case for extent information.
     */
    private static Date extent(final TemporalPrimitive metadata, final String identifier) throws ParseException {
        final Instant instant;
        if (metadata instanceof Period) {
            switch (identifier) {
                case "beginPosition": instant = ((Period) metadata).getBeginning(); break;
                case "endPosition":   instant = ((Period) metadata).getEnding();    break;
                default: throw new ParseException("Unsupported extent property: " + identifier);
            }
        } else if (metadata instanceof Instant) {
            instant = (Instant) metadata;
        } else {
            throw new ParseException("Unsupported extent: " + metadata);
        }
        final Position position = instant.getPosition();
        return (position != null) ? position.getDate() : null;
    }
}
