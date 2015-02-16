package org.constellation.process.converter;

import org.apache.sis.util.UnconvertibleObjectException;
import org.constellation.process.DatasetProcessReference;
import org.geotoolkit.util.converter.SimpleConverter;

import java.util.LinkedHashMap;

/**
 * @author Fabien Bernard (Geomatys).
 */
public class MapToDatasetProcessReferenceConverter extends SimpleConverter<LinkedHashMap, DatasetProcessReference> {

    @Override
    public Class<LinkedHashMap> getSourceClass() {
        return LinkedHashMap.class;
    }

    @Override
    public Class<DatasetProcessReference> getTargetClass() {
        return DatasetProcessReference.class;
    }

    @Override
    public DatasetProcessReference apply(LinkedHashMap map) throws UnconvertibleObjectException {
        DatasetProcessReference reference = new DatasetProcessReference();
        reference.setId((Integer) map.get("id"));
        reference.setIdentifier((String) map.get("identifier"));
        return reference;
    }
}
