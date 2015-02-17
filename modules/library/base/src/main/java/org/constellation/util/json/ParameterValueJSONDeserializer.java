package org.constellation.util.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.geotoolkit.parameter.ParametersExt;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Deserialize a {@link GeneralParameterValue} from a JSON matching specific {@link GeneralParameterDescriptor}.
 * This deserializer work with array representation of parameters multi-occurrences.
 * For example this JSON : {@code {"group":{"paramName":[5,6,55]}}} can be translate to a
 * {@link ParameterValueGroup} containing three {@code paramName} (5,6,55).
 * In case of "mono-occurrence", JSON can wrap or not the value in an array.
 * For example :  {@code {"paramName":[5]}} and  {@code {"paramName":5}} are supported and deserialized in the same way.
 *
 * @author Quentin Boileau (Geomatys)
 * @see org.constellation.util.json.ParameterValueJSONSerializer
 */
public class ParameterValueJSONDeserializer extends JsonDeserializer<GeneralParameterValue> {

    private GeneralParameterDescriptor descriptor;

    public ParameterValueJSONDeserializer(GeneralParameterDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public GeneralParameterValue deserialize(JsonParser parser, DeserializationContext deserializationContext)
            throws IOException, JsonProcessingException {

        final JsonNode rootNode = parser.getCodec().readTree(parser);
        if (!rootNode.isObject()) {
            throw new IOException("Invalid JSON : Expecting JSON object as root node");
        }

        // special case when JSON doesn't contain root descriptor node.
        if (descriptor instanceof ParameterDescriptorGroup) {
            String rootParamName = descriptor.getName().getCode();

            if (!rootNode.has(rootParamName)) {
                // JSON exclude root
                return nodeToParameterValueGroup((ParameterDescriptorGroup) descriptor, rootNode);
            }
        }

        //standard case
        List<GeneralParameterValue> generalParameterValues = readGeneralParameterValue(descriptor, rootNode);
        if (generalParameterValues != null && !generalParameterValues.isEmpty()) {
            return generalParameterValues.get(0);
        }

        // error ?
        return null;
    }

    private List<GeneralParameterValue> readGeneralParameterValue(GeneralParameterDescriptor descriptor, JsonNode node) throws IOException  {
        if (descriptor instanceof ParameterDescriptorGroup) {
            ParameterDescriptorGroup descriptorGroup = (ParameterDescriptorGroup) descriptor;
            return readParameterValueGroup(descriptorGroup, node);
        } else {
            ParameterDescriptor parameterDesc = (ParameterDescriptor) descriptor;
            return readParameterValue(parameterDesc, node);
        }
    }

    private List<GeneralParameterValue> readParameterValueGroup(ParameterDescriptorGroup descriptor, JsonNode rootNode) throws IOException {

        String paramName = descriptor.getName().getCode();
        JsonNode paramNode = rootNode.get(paramName);

        if (paramNode == null) {
            throw new IOException("Parameter group "+paramName+" not found.");
        }

        final List<GeneralParameterValue> valueGroups = new ArrayList<>();
        if (paramNode.isArray()) {
            //multi-occurrences
            final ArrayNode arrayNode = (ArrayNode) paramNode;
            for (JsonNode occurrence : arrayNode) {
                valueGroups.add(nodeToParameterValueGroup(descriptor, occurrence));
            }

        } else {
            //mono-occurrences
            valueGroups.add(nodeToParameterValueGroup(descriptor, paramNode));
        }
        return valueGroups;
    }

    private ParameterValueGroup nodeToParameterValueGroup(ParameterDescriptorGroup descriptor, JsonNode node) throws IOException {

        final ParameterValueGroup valueGroup = descriptor.createValue();

        if (!node.isObject()) {
            throw new IOException("Invalid JSON node type. Expecting Object to build a ParameterValueGroup.");
        }

        List<GeneralParameterDescriptor> subDescriptors = descriptor.descriptors();
        for (GeneralParameterDescriptor subDescriptor : subDescriptors) {
            List<GeneralParameterValue> values = readGeneralParameterValue(subDescriptor, node);
            setOrCreate(valueGroup, values, subDescriptor);
        }

        return valueGroup;
    }

    /**
     * Re-use and update default created Parameters contained in {@code valueGroup} before append new GeneralParameterValue
     * to parameters list.
     *
     * For example if a {@link ParameterDescriptorGroup} define a parameter with cardinality at (2-5), the created
     * {@link ParameterValueGroup} will be initialized with two instances of this parameter.
     * And if we read three instances of this parameter that we want to used in {@link ParameterValueGroup},
     * we have to update the two default instances and add the last one. This is the job of this utility method.
     *
     * @param valueGroup
     * @param values
     * @param subDescriptor
     */
    private void setOrCreate(ParameterValueGroup valueGroup, List<GeneralParameterValue> values, GeneralParameterDescriptor subDescriptor) {
        final String paramName = subDescriptor.getName().getCode();
        final List<GeneralParameterValue> defaultParameters = ParametersExt.getParameters(valueGroup, paramName);

        for (int i = 0; i < values.size(); i++) {
            if (i < defaultParameters.size()) {
                //reuse parameters created by default
                if (subDescriptor instanceof ParameterDescriptorGroup) {
                    final ParameterValueGroup defaultParamGroup = (ParameterValueGroup) defaultParameters.get(i);
                    final ParameterValueGroup newParamGroup = (ParameterValueGroup) values.get(i);
                    ParametersExt.deepCopy(newParamGroup, defaultParamGroup);
                } else {
                    final ParameterValue defaultParamValue = (ParameterValue) defaultParameters.get(i);
                    final ParameterValue newParamValue = (ParameterValue) values.get(i);
                    defaultParamValue.setValue(newParamValue.getValue());
                }
            } else {
                //add new value
                valueGroup.values().add(values.get(i));
            }
        }

    }

    private List<GeneralParameterValue> readParameterValue(ParameterDescriptor descriptor, JsonNode rootNode) throws IOException {

        final boolean mandatory = descriptor.getMinimumOccurs() > 0;
        final String paramName = descriptor.getName().getCode();
        final JsonNode paramNode = rootNode.get(paramName);

        if (paramNode == null) {
            if (mandatory) throw new IOException("Mandatory parameter "+paramName+" not found.");

            // parameter optional and not found OK
            return null;
        }

        final List<GeneralParameterValue> parameterValues = new ArrayList<>();
        if (paramNode.isArray()) {
            //multi-occurrences
            final ArrayNode arrayNode = (ArrayNode) paramNode;
            for (JsonNode occurrence : arrayNode) {
                parameterValues.add(nodeToParameterValue(descriptor, occurrence));
            }

        } else {
            //mono-occurrences
            parameterValues.add(nodeToParameterValue(descriptor, paramNode));
        }
        return parameterValues;
    }

    private ParameterValue nodeToParameterValue(ParameterDescriptor descriptor, JsonNode paramNode) throws IOException {
        final ParameterValue value = descriptor.createValue();
        final String paramName = value.getDescriptor().getName().getCode();
        value.setValue(JsonUtils.readValue(paramNode, descriptor.getValueClass(), paramName));
        return value;
    }

}
