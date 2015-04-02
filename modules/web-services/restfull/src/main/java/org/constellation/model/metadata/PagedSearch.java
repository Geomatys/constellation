package org.constellation.model.metadata;

import javax.validation.constraints.NotNull;

/**
 * @author Mehdi Sidhoum (Geomatys).
 */
public class PagedSearch extends Search {

    @NotNull
    private int page;

    @NotNull
    private int size;


    public PagedSearch() {
        super();
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
