package org.constellation.engine.register.domain;

import org.constellation.engine.register.domain.Order.Direction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.apache.sis.util.ArgumentChecks.ensureBetween;

/**
 * @author Fabien Bernard (Geomatys).
 */
public class PageRequest implements Pageable, Serializable {

    private static final long serialVersionUID = 267788788516611715L;

    // -------------------------------------------------------------------------
    //  Properties
    // -------------------------------------------------------------------------

    private final int page;

    private final int size;

    private final Set<Order> orders;

    // -------------------------------------------------------------------------
    //  Constructors
    // -------------------------------------------------------------------------

    public PageRequest(int page, int size) {
        ensureBetween("page", 1, Integer.MAX_VALUE, page);
        ensureBetween("size", 1, Integer.MAX_VALUE, size);

        this.page = page;
        this.size = size;
        this.orders = new HashSet<>();
    }

    // -------------------------------------------------------------------------
    //  Public methods
    // -------------------------------------------------------------------------

    public Pageable sort(Direction direction, String... properties) {
        if (properties != null && properties.length > 0) {
            for (String property : properties) {
                sort(new Order(direction, property));
            }
        }
        return this;
    }

    public Pageable sort(Order... orders) {
        return sort(orders == null ? new ArrayList<Order>() : Arrays.asList(orders));
    }

    public Pageable sort(Collection<Order> orders) {
        if (orders != null && !orders.isEmpty()) {
            this.orders.addAll(orders);
        }
        return this;
    }

    public Pageable asc(String... properties) {
        return sort(Direction.ASC, properties);
    }

    public Pageable desc(String... properties) {
        return sort(Direction.DESC, properties);
    }

    // -------------------------------------------------------------------------
    //  Pageable implementation
    // -------------------------------------------------------------------------

    @Override
    public int getPageNumber() {
        return page;
    }

    @Override
    public int getPageSize() {
        return size;
    }

    @Override
    public int getOffset() {
        return (page - 1) * size;
    }

    @Override
    public Pageable next() {
        return new PageRequest(page + 1, size).sort(orders);
    }

    @Override
    public Pageable previousOrFirst() {
        return hasPrevious() ? new PageRequest(page - 1, size).sort(orders) : this;
    }

    @Override
    public Pageable first() {
        return new PageRequest(1, size).sort(orders);
    }

    @Override
    public boolean hasPrevious() {
        return page > 1;
    }

    // -------------------------------------------------------------------------
    //  Sortable implementation
    // -------------------------------------------------------------------------

    @Override
    public Set<Order> getSortOrders() {
        return orders;
    }
}
