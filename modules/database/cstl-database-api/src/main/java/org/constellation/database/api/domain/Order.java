package org.constellation.database.api.domain;

import java.io.Serializable;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;

/**
 * @author Fabien Bernard (Geomatys).
 */
public class Order implements Serializable {

    private static final long serialVersionUID = -2014434636272903866L;

    public static final Direction DEFAULT_DIRECTION = Direction.ASC;

    // -------------------------------------------------------------------------
    //  Properties
    // -------------------------------------------------------------------------

    private final Direction direction;

    private final String property;

    // -------------------------------------------------------------------------
    //  Constructors
    // -------------------------------------------------------------------------

    public Order(Direction direction, String property) {
        ensureNonNull("direction", direction);
        ensureNonNull("property", property);

        this.direction = direction;
        this.property = property;
    }

    public Order(String property) {
        this(DEFAULT_DIRECTION, property);
    }

    // -------------------------------------------------------------------------
    //  Getters / Setters
    // -------------------------------------------------------------------------

    public Direction getDirection() {
        return direction;
    }

    public String getProperty() {
        return property;
    }

    // -------------------------------------------------------------------------
    //  Equals & HashCode
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Order that = (Order) obj;

        return direction.equals(that.direction)
                && property.equals(that.property);
    }

    @Override
    public int hashCode() {
        int result = direction.hashCode();
        result = 31 * result + property.hashCode();
        return result;
    }

    // -------------------------------------------------------------------------
    //  Inner classes / enums
    // -------------------------------------------------------------------------

    public static enum Direction {
        ASC, DESC
    }
}
