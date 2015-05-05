package org.constellation.engine.register.domain;

import com.google.common.base.Function;

import java.util.List;

/**
 * @author Fabien Bernard (Geomatys).
 */
public interface Page<T> extends Iterable<T> {

    int getNumber();

    int getSize();

    int getTotalPages();

    int getNumberOfElements();

    long getTotalElements();

    boolean hasPreviousPage();

    boolean isFirstPage();

    boolean hasNextPage();

    boolean isLastPage();

    Pageable nextPageable();

    Pageable previousPageable();

    List<T> getContent();

    <O> Page<O> transform(Function<T, O> function);
}
