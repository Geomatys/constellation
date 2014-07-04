package org.constellation.engine.register.i18n;

import com.google.common.collect.ImmutableMap;
import org.constellation.engine.register.Data;
import org.constellation.engine.register.DataI18n;

import java.util.Map;

/**
 * Wrap {@link Data} and {@link DataI18n}.
 * @author Olivier NOUGUIER.
 *
 */
public class DataWithI18N extends Data {

    private Map<String, DataI18n> dataI18ns;
    
    public DataWithI18N(Data data, ImmutableMap<String, DataI18n> dataI18ns) {
        copyFrom(data);
        this.dataI18ns = dataI18ns;
    }
    
    public void setDataI18n(Map<String, DataI18n> dataI18ns) {
        this.dataI18ns = dataI18ns;
    }
    
    public Map<String, DataI18n> getDataI18n() {
        return dataI18ns;
    }
    
}
