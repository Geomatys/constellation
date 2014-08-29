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

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.sis.metadata.MetadataStandard;
import org.apache.sis.metadata.KeyNamePolicy;
import org.apache.sis.metadata.TypeValuePolicy;
import org.apache.sis.metadata.ValueExistencePolicy;


/**
 * Creates the tree of value to writes as a JSON file.
 *
 * @author Martin Desruisseaux (Geomatys)
 */
final class TemplateApplicator {
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
        return createValueTree(template, metadata, 0);
    }

    /**
     * Builds a tree of values for the given node and all its children nodes.
     *
     * <p>This method invokes itself recursively.</p>
     *
     * @param  metadata   The metadata from which to get the values, or {@code null}.
     * @param  pathOffset Index of the first {@link #path} element to use.
     * @return The roots of tree nodes created by this method (not necessarily the root of the whole tree to be
     *         written), or {@code null} if none. The array may contain null elements, which shall be ignored.
     */
    private ValueNode[] createValueTree(final TemplateNode template, final Object metadata, int pathOffset) throws ParseException {
        if (template.path == null) {
            /*
             * If this node does not declare any path, we can not get a metadata value for this node.
             * However maybe some chidren may have a path allowing them to fetch metadata values.
             */
            ValueNode node = null;
            for (final TemplateNode child : template.children) {
                node = addTo(template, node, createValueTree(child, metadata, pathOffset));
            }
            return (node != null) ? new ValueNode[] {node} : null;
        }
        /*
         * If this node declares a path, then get the values for this node. The values may be other
         * metadata objects, in which case we will need to invoke this method recursively for them.
         */
        nodes.clear();
        try {
            getValues(template, metadata, pathOffset);
        } catch (ClassCastException e) {
            final StringBuilder buffer = new StringBuilder("Illegal path: \"");
            template.appendPath(pathOffset, buffer);
            throw new ParseException(buffer.append("\".").toString(), e);
        }
        filterNewValues(template);
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
            final int pathEnd = template.path.length;
            Arrays.fill(indices, pathOffset, pathEnd, 0); // Initialize to "not a collection".
            if (metadata != null) {
                Class<?> type = metadata.getClass();
                while (pathOffset < pathEnd) {
                    final String   identifier   = template.path[pathOffset];
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
                        indices[pathOffset] = 1;
                        if (++pathOffset != pathEnd) { // Avoid computing the next 'type' if we are reaching the end of loop.
                            type = getType(template, type, TypeValuePolicy.ELEMENT_TYPE, identifier);
                        }
                    } else {
                        ++pathOffset;
                        type = propertyType; // Should be equivalent to a call to 'getType(…)', but much cheaper.
                    }
                }
            }
            if (template.isField()) {
                nodes.add(new ValueNode(template, indices, template.defaultValue));
                // To be formatted below like ordinary values.
            } else {
                ValueNode node = null;
                for (final TemplateNode child : template.children) {
                    node = addTo(template, node, createValueTree(child, null, pathEnd));
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
                ValueNode node = na[i];
                System.arraycopy(node.indices, 0, indices, 0, node.indices.length);
                for (final TemplateNode child : template.children) {
                    node = addTo(template, node, createValueTree(child, node.value, pathOffset));
                }
                na[i] = node; // As a matter of principle, but the reference should be the same.
            }
        }
        return na;
    }

    /**
     * Fetches all occurrences of metadata values at the path given by {@link TemplateNode#path}.
     * This method searches only the metadata values for the given {@code TemplateNode} - it does
     * not perform any search for children {@code TemplateNode}s.
     * The values are added to the {@link #nodes} list.
     *
     * <p>This method invokes itself recursively.</p>
     *
     * @param  metadata   The metadata from where to get the values, or {@code null}.
     * @param  pathOffset Index of the first {@code path} element to use.
     * @throws ClassCastException if {@code metadata} is not an instance of the expected standard.
     */
    private void getValues(final TemplateNode template, Object metadata, int pathOffset)
            throws ClassCastException
    {
        if (metadata == null || template.path == null) {
            return;
        }
        Object value;
        do {
            final String identifier = template.path[pathOffset];
            value = template.standard.asValueMap(metadata, KeyNamePolicy.UML_IDENTIFIER, ValueExistencePolicy.NON_EMPTY).get(identifier);
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
                    } else {
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
    private void filterNewValues(final TemplateNode template) {
        final int size      = nodes.size();
        final int maxOccurs = template.maxOccurs;
        if (size > maxOccurs) {
            nodes.subList(maxOccurs, size).clear();
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
    private static ValueNode addTo(final TemplateNode template, ValueNode parent, final ValueNode[] children) {
        if (children != null) {
            for (final ValueNode child : children) {
                if (child != null) {
                    if (parent == null) {
                        parent = new ValueNode(template, null, null);
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
}
