package org.constellation.admin.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import org.geotoolkit.image.internal.SampleType;
import org.geotoolkit.metadata.ImageStatistics;

import java.io.IOException;
import java.util.Iterator;

/**
 * @author Quentin Boileau (Geomatys)
 */
public class ImageStatisticDeserializer extends JsonDeserializer<ImageStatistics> {

    @Override
    public ImageStatistics deserialize(JsonParser parser, DeserializationContext deserializationContext)
            throws IOException, JsonProcessingException {
        JsonNode node = parser.getCodec().readTree(parser);
        JsonNode bandsNode = node.get("bands");
        if (bandsNode.isArray()) {
            ArrayNode bandArray = (ArrayNode) bandsNode;
            int nbBands = bandArray.size();

            final ImageStatistics stats = new ImageStatistics(nbBands);
            Iterator<JsonNode> bandIte = bandArray.iterator();

            int b = 0;// band index
            while (bandIte.hasNext()) {
                JsonNode bandNode = bandIte.next();
                if (bandNode.isObject()) {

                    int idx = ((IntNode) bandNode.get("index")).intValue();

                    JsonNode nameNode = bandNode.get("name");
                    if (nameNode != null) {
                        stats.getBand(b).setName(nameNode.textValue());
                    }

                    JsonNode dataTypeNode = bandNode.get("dataType");
                    if (dataTypeNode != null) {
                        stats.getBand(b).setDataType(SampleType.valueOf(dataTypeNode.textValue()));
                    }

                    String minStr = bandNode.get("min").asText();
                    String maxStr = bandNode.get("max").asText();
                    stats.getBand(b).setMin(Double.valueOf(minStr));
                    stats.getBand(b).setMax(Double.valueOf(maxStr));

                    JsonNode meanNode = bandNode.get("mean");
                    if (meanNode != null) {
                        stats.getBand(b).setMean(meanNode.doubleValue());
                    }

                    JsonNode stdNode = bandNode.get("std");
                    if (stdNode != null) {
                        stats.getBand(b).setStd(stdNode.doubleValue());
                    }

                    JsonNode noDataNode = bandNode.get("noData");
                    if (noDataNode != null && noDataNode.isArray()) {
                        ArrayNode noDataArray = (ArrayNode) noDataNode;
                        double[] noData = new double[noDataArray.size()];
                        Iterator<JsonNode> noDataIte = noDataArray.iterator();
                        int i = 0;
                        while (noDataIte.hasNext()) {
                            noData[i++] = Double.valueOf(noDataIte.next().asText());
                        }
                        stats.getBand(b).setNoData(noData);
                    }

                    JsonNode histogramNode = bandNode.get("histogram");
                    if (histogramNode != null && histogramNode.isArray()) {
                        ArrayNode histogramArray = (ArrayNode) histogramNode;
                        int size = histogramArray.size();

                        long[] histogram = new long[size];
                        Iterator<JsonNode> histogramIte = histogramArray.iterator();
                        int i = 0;
                        while (histogramIte.hasNext()) {
                            histogram[i++] = histogramIte.next().longValue();
                        }
                        stats.getBand(b).setHistogram(histogram);
                    } else {
                        throw new IOException("Invalid JSON");
                    }
                }
                b++;
            }

            return stats;
        }
        throw new IOException("Invalid JSON");
    }
}
