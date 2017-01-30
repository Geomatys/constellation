package org.geotoolkit.data.geojson.utils;

import org.opengis.util.GenericName;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import org.apache.sis.util.logging.Logging;
import org.geotoolkit.cql.CQL;
import org.geotoolkit.cql.CQLException;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.factory.HintsPending;
import org.geotoolkit.feature.FeatureTypeUtilities;
import org.opengis.feature.AttributeType;
import org.opengis.feature.PropertyType;
import org.geotoolkit.feature.type.*;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.FactoryException;
import org.opengis.util.InternationalString;

import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.ArgumentChecks;
import org.apache.sis.util.iso.SimpleInternationalString;

import org.geotoolkit.feature.AttributeDescriptorBuilder;
import org.geotoolkit.feature.type.FeatureTypeFactory;
import org.geotoolkit.util.NamesExt;
import org.geotoolkit.feature.FeatureTypeBuilder;
import org.geotoolkit.lang.Static;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An utility class to handle read/write of FeatureType into a JSON schema file.
 *
 * Theses schema are inspired from <a href="http://json-schema.org/">JSON-Schema</a> specification.
 * Changes are :
 *  - introducing of a {@code javatype} that define Java class used.
 *  - introducing of a {@code nillable} property for nullable attributes
 *  - introducing of a {@code userdata} Map property that contain previous user data information.
 *  - introducing of a {@code geometry} property to describe a geometry
 *  - introducing of a {@code crs} property to describe a geometry crs
 *
 * @author Quentin Boileau (Geomatys)
 */
public final class FeatureTypeUtils extends Static {

    private static final Logger LOGGER = Logging.getLogger("org.geotoolkit.data.geojson.utils");
    private static final FeatureTypeFactory FT_FACTORY = FeatureTypeFactory.INSTANCE;
    private static final FilterFactory2 FF = (FilterFactory2) FactoryFinder.getFilterFactory(
            new Hints(Hints.FILTER_FACTORY, FilterFactory2.class));

    private static final String TITLE = "title";
    private static final String TYPE = "type";
    private static final String JAVA_TYPE = "javatype";
    private static final String DESCRIPTION = "description";
    private static final String PROPERTIES = "properties";
    private static final String PRIMARY_KEY = "primaryKey";
    private static final String RESTRICTION = "restriction";
    private static final String REQUIRED = "required";
    private static final String NILLABLE = "nillable";
    private static final String MIN_ITEMS = "minItems";
    private static final String MAX_ITEMS = "maxItems";
    private static final String USER_DATA = "userdata";
    private static final String GEOMETRY = "geometry";
    private static final String GEOMETRY_ATT_NAME = "geometryName";
    private static final String CRS = "crs";

    private static final String OBJECT = "object";
    private static final String ARRAY = "array";
    private static final String INTEGER = "integer";
    private static final String NUMBER = "number";
    private static final String STRING = "string";
    private static final String BOOLEAN = "boolean";

    /**
     * Write a FeatureType in output File.
     * @param ft
     * @param ouptut
     * @throws IOException
     */
    public static void writeFeatureType(FeatureType ft, File ouptut) throws IOException, DataStoreException {
        ArgumentChecks.ensureNonNull("FeatureType", ft);
        ArgumentChecks.ensureNonNull("outputFile", ouptut);

        if (ft.getGeometryDescriptor() == null) {
            throw new DataStoreException("No default Geometry in given FeatureType : "+ft);
        }

        JsonGenerator writer = GeoJSONParser.FACTORY.createGenerator(ouptut,
                JsonEncoding.UTF8).useDefaultPrettyPrinter();

        //start write feature collection.
        writer.writeStartObject();
        writer.writeStringField(TITLE, ft.getName().tip().toString());
        writer.writeStringField(TYPE, OBJECT);
        writer.writeStringField(JAVA_TYPE, "FeatureType");
        if (ft.getDescription() != null) {
            writer.writeStringField(DESCRIPTION, ft.getDescription().toString());
        }

        writeGeometryType(ft.getGeometryDescriptor(), writer);
        writeProperties(ft, writer);

        writer.writeEndObject();
        writer.flush();
        writer.close();
    }

    public static void writeFeatureTypes(List<FeatureType> fts, OutputStream output) throws IOException, DataStoreException {
        ArgumentChecks.ensureNonNull("FeatureType", fts);
        ArgumentChecks.ensureNonNull("outputStream", output);

        if (fts.isEmpty()) return;

        if (fts.size() > 1) {
            JsonGenerator writer = GeoJSONParser.FACTORY.createGenerator(output, JsonEncoding.UTF8).useDefaultPrettyPrinter();
            writer.writeStartArray();
            for (FeatureType ft : fts) {
                writeFeatureType(ft, output, writer);
            }
            writer.writeEndArray();
            writer.flush();
            writer.close();
        } else {
            writeFeatureType(fts.get(0), output);
        }
    }
    /**
     * Write a FeatureType in output File.
     * @param ft
     * @param output
     * @throws IOException
     */
    public static void writeFeatureType(FeatureType ft, OutputStream output) throws IOException, DataStoreException {
        JsonGenerator writer = GeoJSONParser.FACTORY.createGenerator(output,JsonEncoding.UTF8).useDefaultPrettyPrinter();
        writeFeatureType(ft, output, writer);
        writer.flush();
        writer.close();
    }


    private static void writeFeatureType(FeatureType ft, OutputStream output, JsonGenerator writer) throws IOException, DataStoreException {
        ArgumentChecks.ensureNonNull("FeatureType", ft);
        ArgumentChecks.ensureNonNull("outputStream", output);

        if (ft.getGeometryDescriptor() == null) {
            throw new DataStoreException("No default Geometry in given FeatureType : "+ft);
        }

        //start write feature collection.
        writer.writeStartObject();
        writer.writeStringField(TITLE, ft.getName().tip().toString());
        writer.writeStringField(TYPE, OBJECT);
        writer.writeStringField(JAVA_TYPE, "FeatureType");
        if (ft.getDescription() != null) {
            writer.writeStringField(DESCRIPTION, ft.getDescription().toString());
        }

        writeGeometryType(ft.getGeometryDescriptor(), writer);
        writeProperties(ft, writer);

        writer.writeEndObject();
    }

    private static void writeProperties(ComplexType ft, JsonGenerator writer) throws IOException {
        writer.writeObjectFieldStart(PROPERTIES);

        Collection<PropertyDescriptor> descriptors = ft.getDescriptors();
        List<String> required = new ArrayList<>();

        for (PropertyDescriptor descriptor : descriptors) {
            PropertyType type = descriptor.getType();
            boolean isRequired = false;

            if (type instanceof ComplexType) {
                isRequired = writeComplexType(descriptor, (ComplexType)type, writer);
            } else if (type instanceof org.geotoolkit.feature.type.AttributeType) {
                if (type instanceof GeometryType) {
//                    GeometryType geometryType = (GeometryType) type;
//                    isRequired = writeGeometryType(descriptor, geometryType, writer);
                } else {
                    org.geotoolkit.feature.type.AttributeType att = (org.geotoolkit.feature.type.AttributeType) type;
                    isRequired = writeAttributeType(descriptor, att, writer);
                }
            }
            if (isRequired) {
                required.add(descriptor.getName().tip().toString());
            }
        }

        if (!required.isEmpty()) {
            writer.writeArrayFieldStart(REQUIRED);
            for (String req : required) {
                writer.writeString(req);
            }
            writer.writeEndArray();
        }
        writer.writeEndObject();
    }

    private static boolean writeComplexType(PropertyDescriptor descriptor, ComplexType complex, JsonGenerator writer)
            throws IOException {

        writer.writeObjectFieldStart(descriptor.getName().tip().toString());
        writer.writeStringField(TYPE, OBJECT);
        writer.writeStringField(JAVA_TYPE, "ComplexType");
        if (complex.getDescription() != null) {
            writer.writeStringField(DESCRIPTION, complex.getDescription().toString());
        }
        writeDescriptorAttributes(descriptor, complex, writer);
        writeProperties(complex, writer);

        writer.writeEndObject();

        return complex.getMinimumOccurs() > 0;
    }

    private static boolean writeAttributeType(PropertyDescriptor descriptor, org.geotoolkit.feature.type.AttributeType att,
                                              JsonGenerator writer)
            throws IOException {

        writer.writeObjectFieldStart(att.getName().tip().toString());
        Class binding = att.getValueClass();

        writer.writeStringField(TYPE, findType(binding));
        writer.writeStringField(JAVA_TYPE, binding.getName());
        if (att.getDescription() != null) {
            writer.writeStringField(DESCRIPTION, att.getDescription().toString());
        }
        writeDescriptorAttributes(descriptor, att, writer);
        List<Filter> restrictions = att.getRestrictions();
        if (restrictions != null && !restrictions.isEmpty()) {
            final Filter merged = FF.and(restrictions);
            writer.writeStringField(RESTRICTION, CQL.write(merged));
        }
        writer.writeEndObject();

        return att.getMinimumOccurs() > 0;
    }

    private static void writeDescriptorAttributes(PropertyDescriptor descriptor, AttributeType att, JsonGenerator writer)
            throws IOException {
        if (FeatureTypeUtilities.isPartOfPrimaryKey(descriptor)) {
            writer.writeBooleanField(PRIMARY_KEY, true);
        }

        writer.writeBooleanField(NILLABLE, descriptor.isNillable());
        writer.writeNumberField(MIN_ITEMS, descriptor.getMinOccurs());
        writer.writeNumberField(MAX_ITEMS, descriptor.getMaxOccurs());
        Map<Object, Object> userData = descriptor.getUserData();
        if (!userData.isEmpty()) {
            writeUserData(userData, writer);
        }
    }

    private static void writeUserData(Map<Object, Object> userData, JsonGenerator writer) throws IOException {
        writer.writeObjectFieldStart(USER_DATA);
        for (Map.Entry<Object, Object> entry : userData.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();

            if (!(key instanceof Serializable) || !(value instanceof Serializable)) {
                LOGGER.log(Level.WARNING, "User map entry not serializable "+entry);
            } else {
                writer.writeFieldName(key.toString());
                GeoJSONUtils.writeValue(value, writer);
            }
        }
        writer.writeEndObject();
    }

    private static String findType(Class binding) {

        if (Integer.class.isAssignableFrom(binding)) {
            return INTEGER;
        } else if (Number.class.isAssignableFrom(binding)) {
            return NUMBER;
        } else if(Boolean.class.isAssignableFrom(binding)) {
            return BOOLEAN;
        } else if (binding.isArray()) {
            return ARRAY;
        } else  {
            //fallback
            return STRING;
        }
    }

    private static boolean writeGeometryType(GeometryDescriptor descriptor, JsonGenerator writer)
            throws IOException {
        GeometryType geometryType = descriptor.getType();
        writer.writeObjectFieldStart(GEOMETRY);
        writer.writeStringField(TYPE, OBJECT);
        if (geometryType.getDescription() != null) {
            writer.writeStringField(DESCRIPTION, geometryType.getDescription().toString());
        }
        writer.writeStringField(JAVA_TYPE, geometryType.getBinding().getCanonicalName());
        CoordinateReferenceSystem crs = geometryType.getCoordinateReferenceSystem();
        if (crs != null) {
            String crsCode = GeoJSONUtils.toURN(crs);
            writer.writeStringField(CRS, crsCode);
        }
        writer.writeStringField(GEOMETRY_ATT_NAME, descriptor.getLocalName());
        writer.writeEndObject();
        return true;
    }


    /**
     * Read a FeatureType from an input File.
     * @param input file to read
     * @return FeatureType
     * @throws IOException
     */
    public static FeatureType readFeatureType(File input) throws IOException, DataStoreException {
        JsonParser parser = GeoJSONParser.FACTORY.createParser(input);

        final FeatureTypeBuilder ftb = new FeatureTypeBuilder();
        final AttributeDescriptorBuilder adb = new AttributeDescriptorBuilder();

        parser.nextToken(); // {

        String ftName = null;
        InternationalString description = null;
        List<PropertyDescriptor> propertyDescriptors = new ArrayList<>();
        GeometryDescriptor geometryDescriptor = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {

            final String currName = parser.getCurrentName();
            switch (currName) {
                case TITLE:
                    ftName = parser.nextTextValue();
                    break;
                case JAVA_TYPE:
                    String type = parser.nextTextValue();
                    if (!"FeatureType".equals(type)) {
                        throw new DataStoreException("Invalid JSON schema : " + input.getName());
                    }
                    break;
                case PROPERTIES:
                    propertyDescriptors = readProperties(parser, ftb, adb);
                    break;
                case GEOMETRY:
                    geometryDescriptor = readGeometry(parser, adb);
                    break;
                case DESCRIPTION:
                    description = new SimpleInternationalString(parser.nextTextValue());
                    break;
            }
        }

        if (ftName != null && geometryDescriptor != null) {
            propertyDescriptors.add(geometryDescriptor);
            ftb.reset();
            ftb.setName(ftName);
            ftb.setDescription(description);
            ftb.setProperties(propertyDescriptors);
            ftb.setDefaultGeometry(geometryDescriptor.getName());
        } else {
            throw new DataStoreException("FeatureType name or default geometry not found in JSON schema");
        }

        parser.close();
        return ftb.buildFeatureType();
    }

    private static GeometryDescriptor readGeometry(JsonParser parser, AttributeDescriptorBuilder adb)
            throws IOException, DataStoreException {

        Class binding = null;
        CoordinateReferenceSystem crs = null;
        InternationalString description = null;
        String geometryName = null;

        parser.nextToken(); // {
        while (parser.nextToken() != JsonToken.END_OBJECT) { // -> }
            final String currName = parser.getCurrentName();
            switch (currName) {
                case JAVA_TYPE:
                    String javaTypeValue = parser.nextTextValue();
                    if (!"ComplexType".equals(javaTypeValue)) {
                        try {
                            binding = Class.forName(javaTypeValue);
                        } catch (ClassNotFoundException e) {
                            throw new DataStoreException("Geometry javatype " + javaTypeValue + " invalid : " + e.getMessage(), e);
                        }
                    }
                    break;

                case CRS:
                    String crsCode = parser.nextTextValue();
                    try {
                        crs = org.geotoolkit.referencing.CRS.decode(crsCode);
                    } catch (FactoryException e) {
                        throw new DataStoreException("Geometry crs " + crsCode + " invalid : " + e.getMessage(), e);
                    }
                    break;

                case DESCRIPTION:
                    description = new SimpleInternationalString(parser.nextTextValue());
                    break;
                case GEOMETRY_ATT_NAME:
                    geometryName = parser.nextTextValue();
            }
        }

        if (binding == null || crs == null) {
            throw new DataStoreException("Geometry crs or binding not found.");
        }

        GenericName name = geometryName != null ? NamesExt.create(geometryName) : NamesExt.create(BasicFeatureTypes.GEOMETRY_ATTRIBUTE_NAME);
        PropertyType prop = FT_FACTORY.createGeometryType(name, binding, crs, false, false, null, null, description);
        return (GeometryDescriptor) adb.create((org.geotoolkit.feature.type.PropertyType) prop, name, crs, 1, 1, false, null);
    }

    private static List<PropertyDescriptor> readProperties(JsonParser parser, FeatureTypeBuilder ftb,
                                                           AttributeDescriptorBuilder adb)
            throws IOException, DataStoreException {
        List<PropertyDescriptor> propertyDescriptors = new ArrayList<>();
        parser.nextToken(); // {

        List<String> requiredList = null;
        while (parser.nextToken() != JsonToken.END_OBJECT) { // -> }
            final JsonToken currToken = parser.getCurrentToken();

            if (currToken == JsonToken.FIELD_NAME) {
                final String currName = parser.getCurrentName();

                if (REQUIRED.equals(currName)) {
                    requiredList = parseRequiredArray(parser);
                } else {
                    propertyDescriptors.add(parseProperty(parser, ftb, adb));
                }
            }
        }
        return propertyDescriptors;
    }

    private static PropertyDescriptor parseProperty(JsonParser parser, FeatureTypeBuilder ftb, AttributeDescriptorBuilder adb)
            throws IOException, DataStoreException {

        final String attributeName = parser.getCurrentName();
        Class binding = String.class;
        boolean nillable = true;
        boolean primaryKey = false;
        int minOccurs = 0;
        int maxOccurs = 1;
        String description = null;
        String restrictionCQL = null;
        Map<Object, Object> userData = null;
        List<PropertyDescriptor> descs = new ArrayList<>();

        parser.nextToken(); // {
        while (parser.nextToken() != JsonToken.END_OBJECT) { // -> }

            final String currName = parser.getCurrentName();
            switch (currName) {
                case JAVA_TYPE:
                    String javaTypeValue = parser.nextTextValue();
                    if (!"ComplexType".equals(javaTypeValue)) {
                        try {
                            binding = Class.forName(javaTypeValue);
                        } catch (ClassNotFoundException e) {
                            throw new DataStoreException("Attribute " + attributeName + " invalid : " + e.getMessage(), e);
                        }
                    }
                    break;
                case NILLABLE:
                    nillable = parser.nextBooleanValue();
                    break;
                case MIN_ITEMS:
                    minOccurs = parser.nextIntValue(0);
                    break;
                case MAX_ITEMS:
                    maxOccurs = parser.nextIntValue(1);
                    break;
                case PRIMARY_KEY:
                    primaryKey = parser.nextBooleanValue();
                    break;
                case RESTRICTION:
                    restrictionCQL = parser.nextTextValue();
                    break;
                case USER_DATA:
                    userData = parseUserDataMap(parser);
                    break;
                case PROPERTIES:
                    descs = readProperties(parser, ftb, adb);
                    break;
                case DESCRIPTION:
                    description = parser.nextTextValue();
                    break;
            }
        }

        if (primaryKey) {
            if (userData == null) userData = new HashMap<>();
            userData.put(HintsPending.PROPERTY_IS_IDENTIFIER, Boolean.TRUE);
        }

        GenericName name = NamesExt.valueOf(attributeName);
        InternationalString desc = description != null ? new SimpleInternationalString(description) : null;
        if (descs.isEmpty()) {
            //build AttributeDescriptor
            if (binding == null) {
                throw new DataStoreException("Empty javatype for attribute "+attributeName);
            }

            List<Filter> restrictions = null;
            if (restrictionCQL != null) {
                try {
                   restrictions = new ArrayList<>();
                   restrictions.add(CQL.parseFilter(restrictionCQL, FF));
                } catch (CQLException e) {
                    LOGGER.log(Level.WARNING, "Can't parse restriction filter : "+restrictionCQL, e);
                }
            }

            PropertyType prop = FT_FACTORY.createAttributeType(name, binding, false, false, restrictions, null, desc);
            return adb.create((org.geotoolkit.feature.type.PropertyType) prop, name, minOccurs, maxOccurs, nillable, userData);

        } else {
            //build ComplexType
            ftb.reset();
            ftb.setName(name);
            for (PropertyDescriptor property : descs) {
                ftb.add(property);
            }
            ftb.setDescription(desc);
            final ComplexType complexType = ftb.buildType();
            return adb.create(complexType, name, minOccurs, maxOccurs, nillable, userData);
        }
    }

    private static Map<Object, Object> parseUserDataMap(JsonParser parser) throws IOException {

        Map<Object, Object> map = new HashMap<>();
        parser.nextToken(); // {
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            Object key = parser.getCurrentName();
            JsonToken next = parser.nextToken();
            map.put(key, GeoJSONParser.getValue(next, parser));
        }
        return map;

    }

    private static List<String> parseRequiredArray(JsonParser parser) throws IOException {
        List<String> requiredList = new ArrayList<>();
        parser.nextToken(); // [

        while (parser.nextToken() != JsonToken.END_ARRAY) { // -> ]
            requiredList.add(parser.getValueAsString());
        }

        return requiredList;
    }
}
