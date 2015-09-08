package org.constellation.database.api.domain;

/**
 * @author Fabien Bernard (Geomatys).
 */
public interface Pageable extends Sortable {

    int getPageNumber();

    int getPageSize();

    int getOffset();

    Pageable next();

    Pageable previousOrFirst();

    Pageable first();

    boolean hasPrevious();
}
