/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2015 Geomatys.
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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.sis.util.ObjectConverters;
import org.geotoolkit.feature.IllegalAttributeException;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

/**
 * @author Quentin Boileau (Geomatys)
 */
public class ParameterValueJSONSerializer extends JsonSerializer<GeneralParameterValue> {

    @Override
    public void serialize(GeneralParameterValue parameterValue, JsonGenerator writer, SerializerProvider serializerProvider)
            throws IOException, JsonProcessingException {
        writeGeneralParameterValue(parameterValue, writer);
    }


    private void writeGeneralParameterValue(final GeneralParameterValue generalParameterValue, JsonGenerator writer)
            throws IOException, JsonProcessingException {

        writer.writeStartObject();
        if (generalParameterValue instanceof ParameterValueGroup) {
            writeParameterValueGroup((ParameterValueGroup) generalParameterValue, writer);
        } else {
            writeParameterValue((ParameterValue) generalParameterValue, writer);
        }
        writer.writeEndObject();
    }

    /**
     * This method writes a ParameterValue.
     *
     * @param parameter
     * @throws XMLStreamException
     */
    private void writeParameterValue(final ParameterValue parameter, JsonGenerator writer)
            throws IOException, JsonProcessingException {

        final String paramName = parameter.getDescriptor().getName().getCode().replace(' ', '_');
        final Object value = parameter.getValue();

        writer.writeFieldName(paramName);
        writeValue(value, writer);
    }

    /**
     * This method writes a ParameterValueGroup.
     *
     * @param parameterGroup
     * @throws XMLStreamException
     */
    private void writeParameterValueGroup(final ParameterValueGroup parameterGroup, JsonGenerator writer)
            throws IOException, JsonProcessingException {

        final String paramName = parameterGroup.getDescriptor().getName().getCode().replace(' ', '_');
        List<GeneralParameterValue> values = parameterGroup.values();

        if (values != null && !values.isEmpty()) {
            writer.writeArrayFieldStart(paramName);
            for (GeneralParameterValue value : values) {
                writeGeneralParameterValue(value, writer);
            }
            writer.writeEndArray();
        }
    }

    /**
     * Write value object depending of object type.
     *
     * @param value
     * @param writer
     * @throws IOException
     * @throws IllegalAttributeException
     */
    private void writeValue(Object value, JsonGenerator writer) throws IOException, IllegalAttributeException {

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
            writer.writeString(ObjectConverters.convert(value, String.class));
        }
    }

}