package org.constellation.admin.dto.metadata;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author Mehdi Sidhoum (Geomatys).
 */
public class Sort implements Serializable {

    public static enum Order { ASC, DESC }


    @NotNull
    private String field;

    @NotNull
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
