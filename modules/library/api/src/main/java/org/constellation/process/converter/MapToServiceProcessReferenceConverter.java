package org.constellation.process.converter;

import org.apache.sis.util.UnconvertibleObjectException;
import org.constellation.process.ServiceProcessReference;
import org.geotoolkit.util.converter.SimpleConverter;

import java.util.LinkedHashMap;

/**
 * @author Fabien Bernard (Geomatys).
 */
public class MapToServiceProcessReferenceConverter extends SimpleConverter<LinkedHashMap, ServiceProcessReference> {

    @Override
    public Class<LinkedHashMap> getSourceClass() {
        return LinkedHashMap.class;
    }

    @Override
    public Class<ServiceProcessReference> getTargetClass() {
        return ServiceProcessReference.class;
    }

    @Override
    public ServiceProcessReference apply(LinkedHashMap map) throws UnconvertibleObjectException {
        ServiceProcessReference reference = new ServiceProcessReference();
        reference.setId((Integer) map.get("id"));
        reference.setName((String) map.get("name"));
        reference.setType((String) map.get("type"));
        return reference;
    }
}
