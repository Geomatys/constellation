package org.constellation.json;

import java.io.Serializable;

/**
 * @author Fabien Bernard (Geomatys).
 * @author Mehdi Sidhoum (Geomatys).
 */
public class Filter implements Serializable {

    private static final long serialVersionUID = -1224746509528265809L;


    private String field;

    private String value;


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
