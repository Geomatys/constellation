package org.constellation.admin.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import org.geotoolkit.process.coverage.statistics.ImageStatistics;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

            ImageStatistics stats = new ImageStatistics(nbBands);
            Iterator<JsonNode> bandIte = bandArray.iterator();

            int b = 0;// band index
            while (bandIte.hasNext()) {
                JsonNode band = bandIte.next();
                if (band.isObject()) {
                    int idx = ((IntNode) band.get("index")).intValue();

                    JsonNode name = band.get("name");
                    if (name != null) {
                        stats.getBand(b).setName(name.textValue());
                    }

                    double min = ((DoubleNode) band.get("min")).doubleValue();
                    double max = ((DoubleNode) band.get("max")).doubleValue();

                    JsonNode noDataNode = band.get("noData");
                    if (noDataNode != null && noDataNode.isArray()) {
                        ArrayNode noDataArray = (ArrayNode) noDataNode;
                        double[] noData = new double[noDataArray.size()];
                        Iterator<JsonNode> noDataIte = noDataArray.iterator();
                        int i = 0;
                        while (noDataIte.hasNext()) {
                            noData[i++] = noDataIte.next().doubleValue();
                        }
                        stats.getBand(b).setNoData(noData);
                    }

                    JsonNode histogramNode = band.get("histogram");
                    if (histogramNode != null && histogramNode.isArray()) {
                        ArrayNode histogramArray = (ArrayNode) histogramNode;
                        int size = histogramArray.size();

                        long[] histogram = new long[size];
                        Iterator<JsonNode> histogramIte = histogramArray.iterator();
                        int i = 0;
                        while (histogramIte.hasNext()) {
                            histogram[i++] = histogramIte.next().longValue();
                        }

                        if (size > 0) {
                            double span = max - min;
                            double step = span / size;

                            Map<Double, Long> distribution = new HashMap<>(histogram.length);
                            for (int j = 0; j < size; j++) {
                                distribution.put(min+j*step, histogram[j]);
                            }
                            stats.getBand(b).setFullDistribution(distribution);
                        }

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
