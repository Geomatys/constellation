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

import java.util.Map;
import java.util.SortedMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.nio.charset.Charset;
import org.opengis.metadata.citation.Responsibility;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.constraint.Constraints;
import org.opengis.metadata.constraint.LegalConstraints;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.metadata.extent.GeographicExtent;
import org.opengis.metadata.identification.DataIdentification;
import org.opengis.metadata.identification.Identification;
import org.opengis.util.CodeList;
import org.opengis.util.FactoryException;
import org.apache.sis.util.iso.Types;
import org.apache.sis.metadata.KeyNamePolicy;
import org.apache.sis.metadata.TypeValuePolicy;
import org.apache.sis.metadata.ValueExistencePolicy;
import org.apache.sis.metadata.AbstractMetadata;
import org.geotoolkit.metadata.MetadataFactory;
import org.apache.sis.metadata.MetadataStandard;


/**
 * Updates a metadata object with the information provided in a {@link SortedMap}.
 * They keys in the sorted map shall be sorted according the {@link NumerotedPath}
 * natural order. This map is provided by {@link FormReader}.
 *
 * @author Martin Desruisseaux (Geomatys)
 */
final class MetadataUpdater {
    /**
     * The metadata factory to use for creating new instances.
     */
    private static final MetadataFactory FACTORY = new MetadataFactory();

    /**
     * The iterator over the (path, value) pairs.
     */
    private final Iterator<Map.Entry<NumerotedPath,Object>> it;

    /**
     * The current path.
     */
    NumerotedPath np;

    /**
     * The current value.
     */
    private Object value;

    /**
     * Creates a new updater which will use the entries from the given map.
     * The given map shall not be empty.
     */
    MetadataUpdater(final SortedMap<NumerotedPath,Object> values) {
        it = values.entrySet().iterator();
        next();
    }

    /**
     * If there is an other entry, moves to that next entry and returns {@code true}.
     * Otherwise returns {@code false}.
     */
    private boolean next() {
        if (it.hasNext()) {
            final Map.Entry<NumerotedPath,Object> entry = it.next();
            np    = entry.getKey();
            value = entry.getValue();
            return true;
        } else {
            np    = null;
            value = null;
            return false;
        }
    }

    /**
     * Updates the given metadata with the content of the map given at construction time.
     */
    final void update(final NumerotedPath parent, final AbstractMetadata metadata)
            throws ClassCastException, FactoryException, ParseException
    {
        String previousIdentifier = null;
        Iterator<?> existingChildren = null;
        final int childBase = (parent != null) ? parent.path.length : 0;
        do {
            final String identifier = np.path[childBase];
            if (!identifier.equals(previousIdentifier)) {
                existingChildren = null;
                previousIdentifier = identifier;
            }
            /*
             * If the entry is a simple value (not an other metadata object),
             * stores the value now and check for the next entry.
             */
            if (np.path.length - childBase == 1) {
                put(metadata, identifier, value);
                if (!next()) break;
            } else {
                /*
                 * The entry is the first element of an other metadata object.
                 */
                if (existingChildren == null) {
                    final MetadataStandard standard = metadata.getStandard();
                    final Object child = standard.asValueMap(metadata, KeyNamePolicy.UML_IDENTIFIER,
                            ValueExistencePolicy.NON_EMPTY).get(identifier);
                    if (child != null) {
                        if (child instanceof Collection<?> && TemplateApplicator.isCollection(standard, metadata, identifier)) {
                            existingChildren = ((Collection<?>) child).iterator();
                        } else {
                            /*
                             * Equivalent to: existingChildren = Collections.singleton(child).iterator();
                             * but we inline the result for avoiding the creation of a temporary collection.
                             */
                            update(np.head(childBase + 1), (AbstractMetadata) child); // TODO: avoid cast.
                            continue;
                        }
                    }
                }
                if (existingChildren != null && existingChildren.hasNext()) {
                    update(np.head(childBase + 1), (AbstractMetadata) existingChildren.next()); // TODO: avoid cast.
                } else {
                    existingChildren = Collections.emptyIterator();
                    final Class<?> type = specialize(getType(metadata, identifier));
                    final Object child = FACTORY.create(type, Collections.<String,Object>emptyMap());
                    update(np.head(childBase + 1), (AbstractMetadata) child); // TODO: avoid cast.
                    metadata.asMap().put(identifier, child);
                }
            }
        } while (np != null && np.isChildOf(parent));
    }

    /**
     * Puts the given value for the given property in the given metadata object.
     */
    private static void put(final AbstractMetadata metadata, final String identifier, Object value) {
        final Map<String,Object> values = metadata.asMap();
        if (value instanceof CharSequence && ((CharSequence) value).length() == 0) {
            value = null;
        } else if (value != null) {
            final Class<?> type = getType(metadata, identifier);
            /*
             * Note: if (type == null), then the call to 'values.put(identifier, value)' at the end of this
             * method is likely to fail. However that 'put' method provides a more accurate error message.
             */
            if (type != null) {
                final boolean isCodeList = CodeList.class.isAssignableFrom(type);
                if (isCodeList || type == Locale.class || type == Charset.class) {
                    String text = value.toString();
                    text = text.substring(text.indexOf('.') + 1).trim();
                    if (isCodeList) {
                        value = Types.forCodeName(type.asSubclass(CodeList.class), text, false);
                    } else {
                        value = text;
                    }
                }
            } else {
                /*
                 * TODO: HACK!!!!!! The current "profile_inspire_raster.json" file defines properties which
                 * are both in LegalConstraints and SecurityConstraints, but Apache SIS can not implement both
                 * in same time. Arbitrarily discard this SecurityConstraints for now.
                 */
                if (identifier.equals("classification")) {
                    return;
                }
            }
        }
        values.put(identifier, value); // See "multi-occurrences" in AbstractMetadata.asMap() javadoc.
    }

    /**
     * Returns the type of values for the given property in the given metadata.
     */
    private static Class<?> getType(final AbstractMetadata metadata, final String identifier) {
        return metadata.getStandard().asTypeMap(metadata.getClass(),
                KeyNamePolicy.UML_IDENTIFIER, TypeValuePolicy.ELEMENT_TYPE).get(identifier);
    }

    /**
     * HACK - for some abstract types returned by {@link #getType(AbstractMetadata, String)},
     * returns a hard-coded subtype to use instead.
     *
     * @todo We need a more generic mechanism.
     */
    @SuppressWarnings("deprecation")
    private static Class<?> specialize(Class<?> type) {
        if (type == Responsibility.class)   type = ResponsibleParty.class;
        if (type == Identification.class)   type = DataIdentification.class;
        if (type == GeographicExtent.class) type = GeographicBoundingBox.class;
        if (type == Constraints.class)      type = LegalConstraints.class;
        return type;
    }
}
