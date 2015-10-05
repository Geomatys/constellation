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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.commons.io.IOUtils;
import org.apache.sis.util.ArgumentChecks;
import org.apache.sis.util.Static;
import org.apache.sis.util.logging.Logging;
import org.constellation.util.json.ParameterDescriptorJSONSerializer;
import org.constellation.util.json.ParameterValueJSONDeserializer;
import org.constellation.util.json.ParameterValueJSONSerializer;
import org.geotoolkit.xml.parameter.ParameterValueReader;
import org.geotoolkit.xml.parameter.ParameterValueWriter;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptorGroup;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;

/**
 *
 */
public final class ParamUtilities extends Static {

    private static final Logger LOGGER = Logging.getLogger("org.constellation.util");

    /**
     * Reads an {@link java.io.InputStream} to build a {@link org.opengis.parameter.GeneralParameterValue}
     * instance according the specified {@link org.opengis.parameter.ParameterDescriptorGroup}.
     *
     * @param stream
     *            the stream to read
     * @param descriptor
     *            the parameter descriptor
     * @return a {@link org.opengis.parameter.GeneralParameterValue} instance
     * @throws java.io.IOException
     *             on error while reading {@link org.opengis.parameter.GeneralParameterValue} XML
     */
    public static GeneralParameterValue readParameter(final InputStream stream,
                                                      final ParameterDescriptorGroup descriptor) throws IOException {
        ensureNonNull("stream", stream);
        ensureNonNull("descriptor", descriptor);
        try {
            final ParameterValueReader reader = new ParameterValueReader(descriptor);
            reader.setInput(stream);
            return reader.read();
        } catch (XMLStreamException ex) {
            throw new IOException("An error occurred while parsing ParameterDescriptorGroup XML.", ex);
        } finally {
            try {
                stream.close();
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING,"Error while closing stream", ex);
            }
        }
    }

    public static GeneralParameterValue readParameter(final InputStream stream,
                                                      final GeneralParameterDescriptor descriptor) throws IOException {
        ensureNonNull("stream", stream);
        ensureNonNull("descriptor", descriptor);
        try {
            final ParameterValueReader reader = new ParameterValueReader(descriptor);
            reader.setInput(stream);
            return reader.read();
        } catch (XMLStreamException ex) {
            throw new IOException("An error occurred while parsing ParameterDescriptorGroup XML.", ex);
        } finally {
            try {
                stream.close();
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Error while closing stream", ex);
            }
        }
    }

    public static GeneralParameterValue readParameter(final String xml,
                                                      final GeneralParameterDescriptor descriptor) throws IOException {
        ensureNonNull("xml", xml);
        ensureNonNull("descriptor", descriptor);
        try {
            final ParameterValueReader reader = new ParameterValueReader(descriptor);
            reader.setInput(xml);
            return reader.read();
        } catch (XMLStreamException ex) {
            throw new IOException("An error occurred while parsing ParameterDescriptorGroup XML.", ex);
        }
    }

    /**
     * Reads a {@link String} instance from the specified {@link InputStream}.
     *
     * @return a {@link String} instance
     * @throws IOException
     *             if an I/O error occurs
     */
    public static String readString(final InputStream stream) throws IOException {
        ensureNonNull("stream", stream);
        try {
            final StringWriter writer = new StringWriter();
            IOUtils.copy(stream, writer);
            return writer.toString();
        } finally {
            try {
                stream.close();
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Error while closing stream", ex);
            }
        }
    }

    /**
     * Transform a {@link GeneralParameterValue} instance into a {@link String}
     * instance.
     *
     * @param parameter
     *            the parameter to be written
     * @return a {@link String} instance
     * @throws IOException
     *             on error while writing {@link GeneralParameterValue} XML
     */
    public static String writeParameter(final GeneralParameterValue parameter) throws IOException {
        ensureNonNull("parameter", parameter);
        try {
            final StringWriter sw = new StringWriter();
            final ParameterValueWriter writer = new ParameterValueWriter();
            writer.setOutput(sw);
            writer.write(parameter);
            return sw.toString();
        } catch (XMLStreamException ex) {
            throw new IOException("An error occurred while writing ParameterDescriptorGroup XML.", ex);
        }
    }

    /**
     * Serialize a ParameterValueGroup into a JSON String.
     * @param parameter ParameterValueGroup
     * @return JSON String.
     * @throws JsonProcessingException
     * @throws org.apache.sis.util.NullArgumentException if {@code parameter} is {@code null}
     */
    public static String writeParameterJSON(GeneralParameterValue parameter) throws JsonProcessingException {
        ArgumentChecks.ensureNonNull("parameter", parameter);
        final ObjectMapper mapper = new ObjectMapper();
        final SimpleModule module = new SimpleModule();
        module.addSerializer(GeneralParameterValue.class, new ParameterValueJSONSerializer()); //custom serializer
        mapper.registerModule(module);
        return mapper.writeValueAsString(parameter);
    }

    /**
     * Deserialize a ParameterValueGroup from a JSON String.
     * @param inputJson String json
     * @param descriptor GeneralParameterDescriptor that describe ParameterValueGroup
     * @return ParameterValueGroup matching GeneralParameterDescriptor descriptor
     * @throws IOException
     * @throws org.apache.sis.util.NullArgumentException if {@code inputJson} or {@code descriptor} are {@code null}
     */
    public static GeneralParameterValue readParameterJSON(String inputJson, GeneralParameterDescriptor descriptor) throws IOException {
        ArgumentChecks.ensureNonNull("inputJson", inputJson);
        ArgumentChecks.ensureNonNull("descriptor", descriptor);
        final ObjectMapper mapper = new ObjectMapper();
        final SimpleModule module = new SimpleModule();
        module.addDeserializer(GeneralParameterValue.class, new ParameterValueJSONDeserializer(descriptor)); //custom serializer
        mapper.registerModule(module);
        return mapper.readValue(inputJson, GeneralParameterValue.class);
    }

    /**
     * Serialize a GeneralParameterDescriptor into a JSON String.
     * @param descriptor GeneralParameterDescriptor.
     * @return JSON String.
     * @throws JsonProcessingException
     * @throws org.apache.sis.util.NullArgumentException if {@code descriptor} is {@code null}
     */
    public static String writeParameterDescriptorJSON(GeneralParameterDescriptor descriptor) throws JsonProcessingException {
        ArgumentChecks.ensureNonNull("descriptor", descriptor);
        final ObjectMapper mapper = new ObjectMapper();
        final SimpleModule module = new SimpleModule();
        module.addSerializer(GeneralParameterDescriptor.class, new ParameterDescriptorJSONSerializer()); //custom serializer
        mapper.registerModule(module);
        return mapper.writeValueAsString(descriptor);
    }
}
