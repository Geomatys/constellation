package org.constellation.json.util;

import org.modelmapper.ModelMapper;

/**
 * @author Fabien Bernard (Geomatys).
 */
public final class TransferObjects {

    private static final ModelMapper MODEL_MAPPER = new ModelMapper();


    private TransferObjects() {}


    public static <T> T mapInto(Object obj, Class<T> type) {
        return MODEL_MAPPER.map(obj, type);
    }
}
