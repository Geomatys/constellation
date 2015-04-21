package org.constellation.admin.dto.metadata;

import java.io.Serializable;

/**
 * @author Mehdi Sidhoum (Geomatys).
 */
public class Filter implements Serializable {

    private String field;

    private String value;

    public Filter() {}

    public Filter(final String field, final String value) {
        this.field = field;
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
