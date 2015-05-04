package org.constellation.engine.register.jooq.util;

import org.apache.sis.util.Static;
import org.constellation.engine.register.domain.Order;
import org.constellation.engine.register.domain.Sortable;
import org.jooq.Field;
import org.jooq.SortField;

import java.util.ArrayList;
import java.util.List;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;

/**
 * @author Fabien Bernard (Geomatys).
 */
public final class JooqUtils extends Static {

    /**
     * Transforms a {@link Sortable} instance into jOOQ {@link SortField} instances.
     *
     * @param sortable the sort criteria.
     * @param candidates the sortable fields.
     * @return the {@link SortField} list.
     * @throws IllegalArgumentException if a sorting property is ambiguous or unsupported.
     */
    public static List<SortField<?>> sortFields(Sortable sortable, Field... candidates) {
        ensureNonNull("sortable", sortable);
        ensureNonNull("candidates", candidates);

        // Iterate over sort orders.
        List<SortField<?>> sortFields = new ArrayList<>();
        for (Order order : sortable.getSortOrders()) {

            // Extract jOOQ fields matching with the ordering property.
            List<Field> fields = fields(order.getProperty(), candidates);

            // Detect unsupported or ambiguous ordering property.
            if (fields.isEmpty()) {
                throw new IllegalArgumentException("Ordering property \"" + order.getProperty() + "\" is not supported.");
            }
            if (fields.size() > 1) {
                throw new IllegalArgumentException("Ordering property \"" + order.getProperty() + "\" is ambiguous.");
            }

            // Create jOOQ sort field.
            SortField sortField;
            switch (order.getDirection()) {
                case DESC:
                    sortField = fields.get(0).desc();
                    break;
                case ASC:
                default:
                    sortField = fields.get(0).asc();
            }
            sortFields.add(sortField);
        }
        return sortFields;
    }

    /**
     * Returns the jOOQ {@link Field} candidate(s) matching with the specified {@code name}.
     *
     * @param name the field name.
     * @param candidates the fields candidates.
     * @return the {@link Field} array.
     */
    public static List<Field> fields(String name, Field... candidates) {
        ensureNonNull("name", name);
        ensureNonNull("candidates", candidates);

        List<Field> fields = new ArrayList<>();
        for (Field field : candidates) {
            if (field.getName().equalsIgnoreCase(name)) {
                fields.add(field);
            }
        }
        return fields;
    }
}
