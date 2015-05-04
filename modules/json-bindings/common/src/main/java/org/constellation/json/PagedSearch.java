package org.constellation.json;

/**
 * @author Fabien Bernard (Geomatys).
 * @author Mehdi Sidhoum (Geomatys).
 */
public class PagedSearch extends Search {

    private static final long serialVersionUID = 2237083291876689787L;


    private int page;

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
