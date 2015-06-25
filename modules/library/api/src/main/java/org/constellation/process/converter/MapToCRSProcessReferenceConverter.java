package org.constellation.process.converter;

import org.apache.sis.util.UnconvertibleObjectException;
import org.constellation.process.CRSProcessReference;
import java.util.LinkedHashMap;
import org.geotoolkit.feature.util.converter.SimpleConverter;

/**
 * @author Fabien Bernard (Geomatys).
 */
public class MapToCRSProcessReferenceConverter extends SimpleConverter<LinkedHashMap, CRSProcessReference> {

    @Override
    public Class<LinkedHashMap> getSourceClass() {
        return LinkedHashMap.class;
    }

    @Override
    public Class<CRSProcessReference> getTargetClass() {
        return CRSProcessReference.class;
    }

    @Override
    public CRSProcessReference apply(LinkedHashMap map) throws UnconvertibleObjectException {
        CRSProcessReference reference = new CRSProcessReference();
        reference.setCode((String) map.get("code"));
        return reference;
    }
}
