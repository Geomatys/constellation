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

package org.constellation.util.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.sis.util.ArgumentChecks;
import org.geotoolkit.parameter.ParametersExt;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Serialize a {@link GeneralParameterValue} in a JSON.
 * Multi-occurrences parameters are represented in a JSON array.
 * For example, if we serialize a ParameterValueGroup containing three instance of parameter {@code paramName}.
 * resulting JSON will be : {@code {"group":{"paramName":[5,6,55]}}}
 *
 * @author Quentin Boileau (Geomatys)
 * @see org.constellation.util.json.ParameterValueJSONDeserializer
 */
public class ParameterValueJSONSerializer extends JsonSerializer<GeneralParameterValue> {

    /**
     * Flag that indicate if root ParameterValueGroup is omitted or included in
     * output JSON.
     */
    private boolean excludeRootGroup = true;

    public ParameterValueJSONSerializer() {
    }

    /**
     *
     * @param excludeRootGroup Flag that indicate if root ParameterValueGroup is omitted or included in
     * output JSON.
     */
    public ParameterValueJSONSerializer(boolean excludeRootGroup) {
        this.excludeRootGroup = excludeRootGroup;
    }

    @Override
    public void serialize(GeneralParameterValue parameterValue, JsonGenerator writer, SerializerProvider serializerProvider)
            throws IOException, JsonProcessingException {

        ArgumentChecks.ensureNonNull("parameterValue", parameterValue);
        GeneralParameterDescriptor descriptor = parameterValue.getDescriptor();
        if (excludeRootGroup) {

            if (descriptor instanceof ParameterDescriptorGroup) {
                ParameterDescriptorGroup rootDesc = (ParameterDescriptorGroup) descriptor;
                ParameterValueGroup rootGroup = (ParameterValueGroup) parameterValue;
                writeGroupParameters(rootDesc, rootGroup, writer);

            } else {
                writer.writeStartObject();
                writeGeneralParameterValue(descriptor, Collections.singletonList(parameterValue), writer);
                writer.writeEndObject();
            }

        } else {
            writer.writeStartObject();
            writeGeneralParameterValue(descriptor, Collections.singletonList(parameterValue), writer);
            writer.writeEndObject();
        }
    }

    /**
     * Write GeneralParameterValue list matching input GeneralParameterDescriptor
     * @param descriptor
     * @param parameterValue
     * @param writer
     * @throws IOException
     * @throws JsonProcessingException
     */
    private void writeGeneralParameterValue(final GeneralParameterDescriptor descriptor, final List<GeneralParameterValue> parameterValue,
                                            JsonGenerator writer) throws IOException, JsonProcessingException {
        if (descriptor instanceof ParameterDescriptorGroup) {
            writeParameterValueGroup((ParameterDescriptorGroup)descriptor, parameterValue, writer);
        } else {
            writeParameterValue((ParameterDescriptor) descriptor, parameterValue, writer);
        }
    }

    /**
     * Write a list of ParameterValue matching input ParameterDescriptor.
     * @param descriptor
     * @param parameterValue
     * @param writer
     * @throws IOException
     */
    private void writeParameterValue(ParameterDescriptor descriptor, List<GeneralParameterValue> parameterValue,
                                     JsonGenerator writer) throws IOException {
        final String paramName = descriptor.getName().getCode().replace(' ', '_');

        writer.writeArrayFieldStart(paramName);
        for (GeneralParameterValue generalParameterValue : parameterValue) {
            final Object value = ((ParameterValue)generalParameterValue).getValue();
            JsonUtils.writeValue(value, writer);
        }
        writer.writeEndArray();
    }

    /**
     * Write a list of ParameterValueGroup matching input ParameterDescriptorGroup.
     * @param descriptor
     * @param parameterValue
     * @param writer
     * @throws IOException
     */
    private void writeParameterValueGroup(ParameterDescriptorGroup descriptor, List<GeneralParameterValue> parameterValue,
                                          JsonGenerator writer) throws IOException {

        final String paramName = descriptor.getName().getCode().replace(' ', '_');
        writer.writeArrayFieldStart(paramName);
        for (GeneralParameterValue generalParameterValue : parameterValue) {
            ParameterValueGroup valueGroup = (ParameterValueGroup) generalParameterValue;
            writeGroupParameters(descriptor, valueGroup, writer);
        }
        writer.writeEndArray();
    }

    /**
     * Write a ParameterValueGroup matching input ParameterDescriptorGroup.
     * This method will handle multi-occurrences of GeneralParameterValue contained in input ParameterValueGroup.
     * @param descriptor
     * @param valueGroup
     * @param writer
     * @throws IOException
     */
    private void writeGroupParameters(ParameterDescriptorGroup descriptor, ParameterValueGroup valueGroup, JsonGenerator writer)
            throws IOException {
        List<GeneralParameterDescriptor> subDescriptors = descriptor.descriptors();

        writer.writeStartObject();
        for (GeneralParameterDescriptor subDescriptor : subDescriptors) {
            //extract all GeneralParameterValue matching subDescriptor name
            List<GeneralParameterValue> values = ParametersExt.getParameters(valueGroup, subDescriptor.getName().getCode());
            writeGeneralParameterValue(subDescriptor, values, writer);
        }
        writer.writeEndObject();
    }

}