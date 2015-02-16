package org.constellation.process.converter;

import org.apache.sis.util.UnconvertibleObjectException;
import org.constellation.process.StyleProcessReference;
import org.geotoolkit.util.converter.SimpleConverter;

import java.util.LinkedHashMap;

/**
 * @author Fabien Bernard (Geomatys).
 */
public class MapToStyleProcessReferenceConverter extends SimpleConverter<LinkedHashMap, StyleProcessReference> {

    @Override
    public Class<LinkedHashMap> getSourceClass() {
        return LinkedHashMap.class;
    }

    @Override
    public Class<StyleProcessReference> getTargetClass() {
        return StyleProcessReference.class;
    }

    @Override
    public StyleProcessReference apply(LinkedHashMap map) throws UnconvertibleObjectException {
        StyleProcessReference reference = new StyleProcessReference();
        reference.setId((Integer) map.get("id"));
        reference.setName((String) map.get("name"));
        reference.setProvider((Integer) map.get("provider"));
        reference.setType((String) map.get("type"));
        return reference;
    }
}
