package org.constellation.json;

import java.io.Serializable;

/**
 * @author Fabien Bernard (Geomatys).
 * @author Mehdi Sidhoum (Geomatys).
 */
public class Sort implements Serializable {

    private static final long serialVersionUID = -719011313529595115L;

    public static enum Order { ASC, DESC }


    private String field;

    private Order order;


    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}
