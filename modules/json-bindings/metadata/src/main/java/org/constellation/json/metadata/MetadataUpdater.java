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
import java.util.List;
import java.util.Locale;
import java.util.Date;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opengis.metadata.extent.BoundingPolygon;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.metadata.extent.GeographicDescription;
import org.opengis.referencing.ReferenceSystem;
import org.opengis.temporal.Period;
import org.opengis.util.CodeList;
import org.opengis.util.FactoryException;
import org.apache.sis.util.iso.Types;
import org.apache.sis.metadata.KeyNamePolicy;
import org.apache.sis.metadata.TypeValuePolicy;
import org.apache.sis.metadata.ValueExistencePolicy;
import org.geotoolkit.metadata.MetadataFactory;
import org.apache.sis.metadata.MetadataStandard;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.metadata.iso.extent.DefaultTemporalExtent;
import org.apache.sis.internal.jaxb.metadata.replace.ReferenceSystemMetadata;
import org.apache.sis.metadata.iso.ImmutableIdentifier;
import org.apache.sis.util.logging.Logging;
import org.apache.sis.xml.NilReason;
import org.geotoolkit.sml.xml.v101.ValidTime;
import org.geotoolkit.sml.xml.v101.SensorMLStandard;
import org.geotoolkit.gml.xml.v311.TimePeriodType;
import org.geotoolkit.gts.xml.PeriodDurationType;
import org.opengis.metadata.Metadata;
import org.opengis.temporal.PeriodDuration;


/**
 * Updates a metadata object with the information provided in a {@link SortedMap}.
 * They keys in the sorted map shall be sorted according the {@link NumerotedPath}
 * natural order. This map is provided by {@link FormReader}.
 *
 * @author Martin Desruisseaux (Geomatys)
 */
final class MetadataUpdater {
    /**
     * @deprecated "Log and continue" is not appropriate since the user can not know that his data is lost
     * (especially when the client is a web browser and the logging occurs on the server). This field needs
     * to be deleted and the logging replaced by an exception or any other mechanism reporting error to the
     * user.
     */
    private static final Logger LOGGER = Logging.getLogger(MetadataUpdater.class);

    /**
     * The metadata factory to use for creating new instances of ISO 19115 objects.
     */
    private static final MetadataFactory
            DEFAULT   = new MetadataFactory(),
            SYSTEM    = new MetadataFactory(SensorMLStandard.SYSTEM),
            COMPONENT = new MetadataFactory(SensorMLStandard.COMPONENT);

    /**
     * The metadata standard.
     */
    private final MetadataStandard standard;

    /**
     * The metadata factory to use for creating new instances.
     */
    private final MetadataFactory factory;

    /**
     * The iterator over the (path, value) pairs.
     */
    private final Iterator<Map.Entry<NumerotedPath,Object>> it;

    /**
     * The current path.
     */
    NumerotedPath np;

    /**
     * The current value. Can only be one of the instances documented in {@link FormReader#values}:
     * {@link String}, {@link Number} or {@code List<Object>}.
     */
    private Object value;

    /**
     * The GeoAPI interfaces substitution.
     * For example {@code Identification} is typically interpreted as {@code DataIdentification}.
     */
    private final Map<Class<?>, Class<?>> specialized;

    /**
     * Creates a new updater which will use the entries from the given values map.
     * The given {@code values} map shall not be empty.
     */
    MetadataUpdater(final MetadataStandard standard, final SortedMap<NumerotedPath,Object> values,
            Map<Class<?>, Class<?>> specialized)
    {
        this.standard = standard;
        this.specialized = specialized;
        if (standard == SensorMLStandard.SYSTEM) {
            factory = SYSTEM;
        } else if (standard == SensorMLStandard.COMPONENT) {
            factory = COMPONENT;
        } else if (standard == MetadataStandard.ISO_19115){
            factory = DEFAULT;
        } else {
            factory = new MetadataFactory(standard, MetadataStandard.ISO_19115);
        }
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
    final void update(final NumerotedPath parent, final Object metadata)
            throws ClassCastException, FactoryException, ParseException
    {
        String previousIdentifier = null;
        Iterator<?> existingChildren = null;
        final int childBase = (parent != null) ? parent.path.length : 0;
        do {
            final String identifier = np.path[childBase];
            if (!identifier.equals(previousIdentifier)) {
                // special case for removal
                if (existingChildren != null && existingChildren.hasNext()) {
                    specialMetadataCaseRemove(metadata, previousIdentifier, existingChildren);
                }
                existingChildren = null;
                previousIdentifier = identifier;
            }
            /*
             * If the entry is a simple value (not an other metadata object),
             * stores the value now and check for the next entry.
             */
            if (np.path.length - childBase == 1) {
                put(metadata, identifier);
                if (!next()) break;
            } else {
                /*
                 * The entry is the first element of an other metadata object.
                 */
                if (existingChildren == null) {
                    final Object child = standard.asValueMap(metadata, KeyNamePolicy.UML_IDENTIFIER,
                            ValueExistencePolicy.NON_EMPTY).get(identifier);
                    if (child != null) {
                        if (child instanceof Collection<?> && TemplateApplicator.isCollection(standard, metadata, identifier)) {
                            existingChildren = ((Collection<?>) child).iterator();
                        } else {
                            existingChildren = Collections.singleton(child).iterator();
                        }
                    }
                }
                final Object child;
                if (existingChildren != null && existingChildren.hasNext()) {
                    child = existingChildren.next();
                    if (specialMetadataCases(child.getClass(), metadata, identifier, true, child)) {
                        continue;
                    }
                } else {
                    existingChildren = Collections.emptyIterator();
                    final Class<?> type = specialize(getType(metadata, identifier), np.path[childBase + 1]);
                    if (type == null) {
                        throw new ParseException("Can not find " +
                                metadata.getClass().getSimpleName() + '.' + identifier + " property.");
                    }
                    if (specialMetadataCases(type, metadata, identifier, false, null)) {
                        continue;
                    }
                    child = factory.create(type, Collections.<String,Object>emptyMap());
                    asMap(metadata).put(identifier, child);
                }
                update(np.head(childBase + 1), child);
            }
        } while (np != null && np.isChildOf(parent));
    }

    /**
     * Puts the current value for the given property in the given metadata object.
     */
    private void put(final Object metadata, final String identifier) throws ParseException {
        Object value = this.value; // Protect the field value from change.
        final Map<String,Object> values = asMap(metadata);
        if (value instanceof CharSequence && ((CharSequence) value).length() == 0) {
            value = null;
        } else if (value != null) {
            Class<?> type = getType(metadata, identifier);
            /*
             * Note: if (type == null), then the call to 'values.put(identifier, value)' at the end of this
             * method is likely to fail. However that 'put' method provides a more accurate error message.
             */
            if (type != null) {
                if (value instanceof List<?>) {
                    @SuppressWarnings("unchecked") // See 'this.value' javadoc.
                    final List<Object> list = (List<Object>) value;
                    for (int i=list.size(); --i >= 0;) {
                        list.set(i, convert(identifier, type, list.get(i)));
                    }
                } else {
                    value = convert(identifier, type, value);
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
        values.put(identifier, value);
    }

    /**
     * Converts the given value to an instance of the given class before to store in the metadata object.
     */
    private static Object convert(final String identifier, final Class<?> type, Object value) throws ParseException {
        if (type == Date.class) {
            return toDate(value, identifier);
        }
        if (type == PeriodDuration.class && value instanceof String) {
            try {
                return new PeriodDurationType((String) value);
            } catch (IllegalArgumentException ex) {
                // TODO: "log and continue" is not appropriate here, since the user can not know that his data is lost.
                LOGGER.log(Level.WARNING, "Bad period duration value: {0} (property: {1})",
                        new Object[] {value, identifier});
            }
        }
        if (!CharSequence.class.isAssignableFrom(type) && (value instanceof CharSequence)) {
            String text = value.toString();
            if (text.startsWith(Keywords.NIL_REASON)) {
                try {
                    value = NilReason.valueOf(text.substring(Keywords.NIL_REASON.length())).createNilObject(type);
                } catch (URISyntaxException | IllegalArgumentException e) {
                    throw new ParseException("Illegal value: \"" + text + "\".(property:" + identifier + ")", e);
                }
            } else {
                final boolean isCodeList = CodeList.class.isAssignableFrom(type);
                if (isCodeList || type == Locale.class || type == Charset.class || type.isEnum()) {
                    text = text.substring(text.indexOf('.') + 1).trim();
                    if (isCodeList) {
                        value = Types.forCodeName(type.asSubclass(CodeList.class), text, false);
                    } else {
                        value = text;
                    }
                }
            }
        }
        return value;
    }

    /**
     * Returns the given value as a date.
     */
    private static Date toDate(final Object value, final String identifier) throws ParseException {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return new Date(((Number) value).longValue());
        }
        final String t = (String) value;
        if (t.indexOf('-') < 0) try {
            return new Date(Long.valueOf(t));
        } catch (NumberFormatException e) {
            throw new ParseException("Illegal date: " + value + " (property:" + identifier +")", e);
        }
        try {
            synchronized (ValueNode.DATE_FORMAT) {
                return ValueNode.DATE_FORMAT.parse((String) value);
            }
        } catch (java.text.ParseException e) {
            throw new ParseException("Illegal date: " + value + " (property:" + identifier +")", e);
        }
    }

    /**
     * Returns a view over the given metadata as a map of values.
     */
    private Map<String,Object> asMap(final Object metadata) {
        return standard.asValueMap(metadata, KeyNamePolicy.UML_IDENTIFIER, ValueExistencePolicy.NON_EMPTY);
    }

    /**
     * Returns the type of values for the given property in the given metadata.
     */
    private Class<?> getType(final Object metadata, final String identifier) {
        return standard.asTypeMap(metadata.getClass(),
                KeyNamePolicy.UML_IDENTIFIER, TypeValuePolicy.ELEMENT_TYPE).get(identifier);
    }

    /**
     * HACK - for some abstract types returned by {@link #getType(Object, String)},
     * returns a hard-coded subtype to use instead.
     *
     * @param child The identifier of the main child element.
     *
     * @todo We need a more generic mechanism.
     */
    private Class<?> specialize(Class<?> type, final String child) {
        final Class<?> c = specialized.get(type);
        if (c != null) {
            assert type.isAssignableFrom(c) : c;
            if (c == GeographicBoundingBox.class) {
                /*
                 * HACK - the 'specialized' map mechanism allows to specify only one sub-type,
                 *        while a metadata form could contain children of different types.
                 *        For now we use hard-coded checks, but we will need a more generic
                 *        mechanism here too.
                 */
                switch (child) {
                    case "polygon":              return BoundingPolygon.class;
                    case "geographicIdentifier": return GeographicDescription.class;
                }
            }
            type = c;
        }
        return type;
    }

    /**
     * Hard-coded handling of some special cases.
     *
     * @todo We need a more generic mechanism.
     *
     * @return {@code true} if we applied a special case.
     */
    private boolean specialMetadataCases(final Class<?> type, final Object metadata, final String identifier, boolean modify, final Object child) throws ParseException, FactoryException {
        if (metadata instanceof DefaultTemporalExtent && identifier.equals("extent")) {
            /*
             * Properties:
             *   - identificationInfo.extent.temporalElement.extent.beginPosition
             *   - identificationInfo.extent.temporalElement.extent.endPosition
             *
             * Reason: "extent" is a TemporalPrimitive, which is defined outside of ISO 19115.
             */
            boolean moved = false;
            Date beginPosition = null, endPosition = null;
            while (np.path.length >= 2 && np.path[np.path.length - 2].equals("extent")) {
                final Date t = toDate(value, identifier);
                switch (np.path[np.path.length - 1]) {
                    case "beginPosition": beginPosition = t; break;
                    case "endPosition":   endPosition   = t; break;
                    default: throw new ParseException("Unsupported property: \"" + np + "\".");
                }
                moved = true;
                if (!next()) break;
            }
            if (moved) {
                ((DefaultTemporalExtent) metadata).setBounds(beginPosition, endPosition);
                return true;
            }
        } else if (metadata instanceof ValidTime && identifier.equals("timePeriod")) {
            /*
             * Properties:
             *   - member.realProcess.validTime.timePeriod.beginPosition
             *   - member.realProcess.validTime.timePeriod.endPosition
             */
            boolean moved = false;
            Date beginPosition = null, endPosition = null;
            while (np.path.length >= 2 && np.path[np.path.length - 2].equals("timePeriod")) {
                final Date t = toDate(value, identifier);
                switch (np.path[np.path.length - 1]) {
                    case "beginPosition": beginPosition = t; break;
                    case "endPosition":   endPosition   = t; break;
                    default: throw new ParseException("Unsupported property: \"" + np + "\".");
                }
                moved = true;
                if (!next()) break;
            }
            if (moved) {
                final TimePeriodType period;
                if (beginPosition != null || endPosition != null) {
                    period = new TimePeriodType((Period) null);
                    period.setBeginPosition(beginPosition);
                    period.setEndPosition(endPosition);
                } else {
                    period = null;
                }
                ((ValidTime) metadata).setTimePeriod(period);
                return true;
            }
        } else if (ReferenceSystem.class.isAssignableFrom(type) && identifier.equals("referenceSystemInfo")) {
            /*
             * Properties:
             *   - referenceSystemInfo.referenceSystemIdentifier.code
             *
             * Reason: "referenceSystemInfo" is a ReferenceSystem, which is defined outside of ISO 19115.
             */
            boolean moved    = false;
            String code      = null;
            String codeSpace = null;
            String version   = null;
            int indice       =  np.indices[0];
            final Map<String, Object> extraParameters = new HashMap<>();
            
            while (np.path.length >= 2 && np.path[0].equals("referenceSystemInfo") && np.indices[0] == indice) {
                if (np.path[np.path.length - 2].equals("referenceSystemIdentifier")) {
                    switch (np.path[np.path.length - 1]) {
                        case "code": code = (String) value; break;
                        case "codeSpace": codeSpace = (String) value; break;
                        case "version": version = (String) value; break;
                        default: throw new ParseException("Unsupported property: \"" + np + "\".");
                    }
                } else {
                    extraParameters.put(np.path[np.path.length - 1], value);
                }
                moved = true;
                if (!next()) break;
            }
            if (moved) {
                DefaultMetadata meta = ((DefaultMetadata) metadata);
                if (modify) {
                    ReferenceSystemMetadata rs = (ReferenceSystemMetadata) child;
                    if (code == null) {
                        meta.getReferenceSystemInfo().remove(rs);
                    } else {
                        rs.setName(new ImmutableIdentifier(null, codeSpace, code, version, null));
                    }
                    for (Entry<String,Object> entry : extraParameters.entrySet()) {
                        final String propName = entry.getKey();
                        Class subType = getType(rs, propName);
                        asMap(rs).put(propName, convert(propName, subType, entry.getValue()));
                    }
                } else {
                    ReferenceSystemMetadata rs;
                    if (ReferenceSystemMetadata.class.isAssignableFrom(type)) {
                        rs = (ReferenceSystemMetadata) factory.create(type, Collections.<String,Object>emptyMap());
                    } else {
                        rs = new ReferenceSystemMetadata();
                    }
                    if (code != null) {
                        rs.setName(new ImmutableIdentifier(null, codeSpace, code, version, null));
                    }
                    for (Entry<String,Object> entry : extraParameters.entrySet()) {
                        final String propName = entry.getKey();
                        Class subType = getType(rs, propName);
                        asMap(rs).put(propName, convert(propName, subType, entry.getValue()));
                    }
                    meta.getReferenceSystemInfo().add(rs);
                }
                return true;
            }
        }
        return false;
    }
    
    private void specialMetadataCaseRemove(final Object metadata, final String identifier, final Iterator existingChildren) throws ParseException, FactoryException {
        if (identifier.equals("referenceSystemInfo") && (metadata instanceof Metadata)) {
            final List<ReferenceSystem> toRemove = new ArrayList<>();
            while (existingChildren.hasNext()) {
                final ReferenceSystem rs = (ReferenceSystem) existingChildren.next();
                toRemove.add(rs);
            }
            ((Metadata)metadata).getReferenceSystemInfo().removeAll(toRemove);
        }
    }
}
