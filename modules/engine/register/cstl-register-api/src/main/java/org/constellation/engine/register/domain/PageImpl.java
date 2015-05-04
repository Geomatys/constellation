package org.constellation.engine.register.domain;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import static org.apache.sis.util.ArgumentChecks.ensurePositive;

/**
 * @author Fabien Bernard (Geomatys).
 */
public class PageImpl<T> implements Page<T>, Serializable {

    private static final long serialVersionUID = -6536523769502359793L;

    // -------------------------------------------------------------------------
    //  Properties
    // -------------------------------------------------------------------------

    private final List<T> content;

    private final Pageable pageable;

    private final long total;

    // -------------------------------------------------------------------------
    //  Constructors
    // -------------------------------------------------------------------------

    public PageImpl(Pageable pageable, List<T> content, long total) {
        ensureNonNull("pageable", pageable);
        ensureNonNull("content", content);
        ensurePositive("total", total);

        this.pageable = pageable;
        this.content = content;
        this.total = total;
    }

    // -------------------------------------------------------------------------
    //  Page implementation
    // -------------------------------------------------------------------------

    @Override
    public int getNumber() {
        return pageable.getPageNumber();
    }

    @Override
    public int getSize() {
        return pageable.getPageSize();
    }

    @Override
    public int getTotalPages() {
        return getSize() == 0 ? 1 : (int) Math.ceil((double) total / (double) getSize());
    }

    @Override
    public int getNumberOfElements() {
        return content.size();
    }

    @Override
    public long getTotalElements() {
        return total;
    }

    @Override
    public boolean hasPreviousPage() {
        return getNumber() > 1;
    }

    @Override
    public boolean isFirstPage() {
        return !hasPreviousPage();
    }

    @Override
    public boolean hasNextPage() {
        return getNumber() < getTotalPages();
    }

    @Override
    public boolean isLastPage() {
        return !hasNextPage();
    }

    @Override
    public Pageable nextPageable() {
        return pageable.next();
    }

    @Override
    public Pageable previousPageable() { return pageable.previousOrFirst(); }

    @Override
    public List<T> getContent() {
        return content;
    }

    // -------------------------------------------------------------------------
    //  Iterable implementation
    // -------------------------------------------------------------------------

    @Override
    public Iterator<T> iterator() {
        return content.iterator();
    }
}
