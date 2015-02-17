package org.constellation.util.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.sis.util.ObjectConverters;
import org.geotoolkit.parameter.ExtendedParameterDescriptor;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Quentin Boileau (Geomatys)
 */
public class ParameterDescriptorJSONSerializer extends JsonSerializer<GeneralParameterDescriptor> {

    @Override
    public void serialize(GeneralParameterDescriptor generalParameterDescriptor, JsonGenerator writer, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        writeGeneralDesc(generalParameterDescriptor, writer);
    }

    private void writeGeneralDesc(GeneralParameterDescriptor generalDesc, JsonGenerator writer) throws IOException {
        writer.writeStartObject();

        final String name = generalDesc.getName().getCode();
        writer.writeStringField("name", name);
        writer.writeNumberField("minOccurs", generalDesc.getMinimumOccurs());
        writer.writeNumberField("maxOccurs", generalDesc.getMaximumOccurs());
        if (generalDesc.getDescription() != null) {
            writer.writeStringField("description", generalDesc.getDescription().toString());
        }

        if (generalDesc instanceof ParameterDescriptor) {
            ParameterDescriptor descParam = (ParameterDescriptor) generalDesc;
            writeParamDesc(descParam, writer);

        } else if (generalDesc instanceof ParameterDescriptorGroup) {
            ParameterDescriptorGroup descGroup = (ParameterDescriptorGroup) generalDesc;
            writeGroupDesc(descGroup, writer);
        }
        writer.writeEndObject();
    }

    private void writeGroupDesc(ParameterDescriptorGroup descGroup, JsonGenerator writer) throws IOException {

        List<GeneralParameterDescriptor> descriptors = descGroup.descriptors();
        writer.writeArrayFieldStart("descriptors");
        for (GeneralParameterDescriptor descriptor : descriptors) {
            writeGeneralDesc(descriptor, writer);
        }
        writer.writeEndArray();
    }

    private void writeParamDesc(ParameterDescriptor descParam, JsonGenerator writer) throws IOException {

        writer.writeStringField("class", descParam.getValueClass().getCanonicalName());

        if (descParam.getUnit() != null) {
            String unit = ObjectConverters.convert(descParam.getUnit(), String.class);
            writer.writeStringField("unit", unit);
        }

        final Object defaultValue = descParam.getDefaultValue();
        if (defaultValue != null) {
            writer.writeFieldName("defaultValue");
            JsonUtils.writeValue(defaultValue, writer);
        }

        final Set validValues = descParam.getValidValues();
        final Comparable minValue = descParam.getMinimumValue();
        final Comparable maxValue = descParam.getMaximumValue();
        if (validValues != null || minValue != null || maxValue != null) {
            writer.writeObjectFieldStart("restriction");

            if (minValue != null) {
                writer.writeFieldName("minValue");
                JsonUtils.writeValue(minValue, writer);
            }

            if (maxValue != null) {
                writer.writeFieldName("maxValue");
                JsonUtils.writeValue(maxValue, writer);
            }

            if (validValues != null) {
                writer.writeArrayFieldStart("validValues");
                for (Object validValue : validValues) {
                    JsonUtils.writeValue(validValue, writer);
                }
                writer.writeEndArray();
            }
            writer.writeEndObject();
        }

        // write user map entries if exist
        if (descParam instanceof ExtendedParameterDescriptor) {
            ExtendedParameterDescriptor extDesc = (ExtendedParameterDescriptor) descParam;
            Map<String, Object> userObject = extDesc.getUserObject();
            if (userObject != null) {
                writer.writeObjectFieldStart("ext");
                for (Map.Entry<String, Object> entry : userObject.entrySet()) {
                    writer.writeFieldName(entry.getKey());
                    JsonUtils.writeValue(entry.getValue(), writer);
                }
                writer.writeEndObject();
            }
        }
    }
}
