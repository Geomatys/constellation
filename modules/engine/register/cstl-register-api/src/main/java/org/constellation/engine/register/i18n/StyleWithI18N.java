package org.constellation.engine.register.i18n;

import java.util.Map;

import org.constellation.engine.register.Style;
import org.constellation.engine.register.StyleI18n;

public class StyleWithI18N extends Style {
    
    private Map<String, StyleI18n> styleI18ns;

    public StyleWithI18N(Style style, Map<String, StyleI18n> styleI18ns) {
        copyFrom(style);
        this.styleI18ns = styleI18ns;
    }

    
    public void setStyleI18ns(Map<String, StyleI18n> styleI18ns) {
        this.styleI18ns = styleI18ns;
    }
    
    public Map<String, StyleI18n> getStyleI18ns() {
        return styleI18ns;
    }
    
}
