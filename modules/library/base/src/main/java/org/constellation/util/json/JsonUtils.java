package org.constellation.util.json;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.sis.util.ObjectConverters;
import org.apache.sis.util.Static;
import org.apache.sis.util.UnconvertibleObjectException;
import org.geotoolkit.feature.IllegalAttributeException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;

/**
 * @author Quentin Boileau (Geomatys)
 */
class JsonUtils extends Static {

    /**
     * Jackson JsonFactory used to create temporary JsonGenerators.
     */
    private static final JsonFactory JSON_FACTORY = new JsonFactory();

    private JsonUtils(){}

    /**
     * Write value object depending of object type.
     *
     * @param value
     * @param writer
     * @throws java.io.IOException
     * @throws org.geotoolkit.feature.IllegalAttributeException
     */
    static void writeValue(Object value, JsonGenerator writer) throws IOException, IllegalAttributeException {

        if (value == null) {
            writer.writeNull();
            return;
        }

        Class binding = value.getClass();

        if (binding.isArray()) {
            if (byte.class.isAssignableFrom(binding.getComponentType())) {
                writer.writeBinary((byte[])value);
            } else {
                writer.writeStartArray();
                final int size = Array.getLength(value);
                for (int i = 0; i < size; i++) {
                    writeValue(Array.get(value, i), writer);
                }
                writer.writeEndArray();
            }

        } else if (Collection.class.isAssignableFrom(binding)) {
            writer.writeStartArray();
            Collection coll = (Collection) value;
            for (Object obj : coll) {
                writeValue(obj, writer);
            }
            writer.writeEndArray();

        } else if (Double.class.isAssignableFrom(binding)) {
            writer.writeNumber((Double) value);
        } else if (Float.class.isAssignableFrom(binding)) {
            writer.writeNumber((Float) value);
        } else if (Short.class.isAssignableFrom(binding)) {
            writer.writeNumber((Short) value);
        } else if (Byte.class.isAssignableFrom(binding)) {
            writer.writeNumber((Byte) value);
        } else if (BigInteger.class.isAssignableFrom(binding)) {
            writer.writeNumber((BigInteger) value);
        } else if (BigDecimal.class.isAssignableFrom(binding)) {
            writer.writeNumber((BigDecimal) value);
        } else if (Integer.class.isAssignableFrom(binding)) {
            writer.writeNumber((Integer) value);
        } else if (Long.class.isAssignableFrom(binding)) {
            writer.writeNumber((Long) value);

        } else if (Boolean.class.isAssignableFrom(binding)) {
            writer.writeBoolean((Boolean) value);
        } else if (String.class.isAssignableFrom(binding)) {
            writer.writeString(String.valueOf(value));
        } else {
            //fallback
            try {
                //HACK : create a temporary writer to write object.
                //In case of writeObject(value) fail input writer will not be in illegal state.
                final JsonGenerator tempGenerator = JSON_FACTORY.createGenerator(new ByteArrayOutputStream(), JsonEncoding.UTF8);
                tempGenerator.setCodec(new ObjectMapper());
                tempGenerator.writeObject(value);

                //using jackson auto mapping
                writer.writeObject(value);
            } catch (Throwable ex) {
                // last chance with converter and toString()
                writer.writeString(ObjectConverters.convert(value, String.class));
            }
        }
    }

    /**
     * Try to convert a JsonNode into an Object.
     * This method use Jackson ObjectMapper and ApacheSIS ObjectConverters (if JsonNode is a text).
     *
     * @param node JsonNode that contain value
     * @param binding expected java Class
     * @param parameterName parameter name for exception message purpose
     * @return Object instance of {@code binding}.
     * @throws IOException if node can't be converted in {@code binding}
     */
    static Object readValue(JsonNode node, Class binding, String parameterName) throws IOException {

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.treeToValue(node, binding);
        } catch (JsonProcessingException ex) {
            //mapper doesn't work
        }

        if (node.isTextual()) {
            try {
                return ObjectConverters.convert(node.textValue(), binding);
            } catch (UnconvertibleObjectException ex) {
                //ObjectConverters doesn't work
            }
        }

        throw new IOException("Can't convert JSON node ("+node.getNodeType().name()+") for parameter "+parameterName+" in Java type "+binding.getName());
    }

}
