package org.constellation.admin.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.geotoolkit.process.coverage.statistics.ImageStatistics;

import java.io.IOException;

/**
 * @author Quentin Boileau (Geomatys)
 */
public class ImageStatisticSerializer extends JsonSerializer<ImageStatistics> {
    @Override
    public void serialize(ImageStatistics imageStatistics, JsonGenerator jsonGen, SerializerProvider serializerProvider)
            throws IOException, JsonProcessingException {
        jsonGen.writeStartObject();
        jsonGen.writeArrayFieldStart("bands");

        ImageStatistics.Band[] bands = imageStatistics.getBands();
        for (int i = 0; i < bands.length; i++) {
            final ImageStatistics.Band band = bands[i];

            jsonGen.writeStartObject();
            jsonGen.writeNumberField("index", band.getBandIndex());
            if (band.getName() != null) {
                jsonGen.writeStringField("name", band.getName());
            }
            jsonGen.writeNumberField("min", band.getMin());
            jsonGen.writeNumberField("max", band.getMax());

            if (band.getNoData() != null) {
                jsonGen.writeArrayFieldStart("noData");
                for (int j = 0; j < band.getNoData().length; j++) {
                    jsonGen.writeNumber(band.getNoData()[j]);
                }
                jsonGen.writeEndArray();//noData
            }

            final Long[] distrib = band.tightenDistribution(255);
            jsonGen.writeArrayFieldStart("histogram");
            for (int j = 0; j < distrib.length; j++) {
                jsonGen.writeNumber(distrib[j]);
            }
            jsonGen.writeEndArray();//histogram

            jsonGen.writeEndObject();//end band
        }

        jsonGen.writeEndArray();
        jsonGen.writeEndObject();
    }
}
