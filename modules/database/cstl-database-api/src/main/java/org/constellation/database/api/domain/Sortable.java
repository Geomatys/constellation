package org.constellation.database.api.domain;

import java.util.Collection;

/**
 * @author Fabien Bernard (Geomatys).
 */
public interface Sortable {

    Collection<Order> getSortOrders();
}
