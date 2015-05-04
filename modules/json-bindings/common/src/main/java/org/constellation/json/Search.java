package org.constellation.json;

import java.io.Serializable;
import java.util.List;

/**
 * @author Fabien Bernard (Geomatys).
 * @author Mehdi Sidhoum (Geomatys).
 */
public class Search implements Serializable {

    private static final long serialVersionUID = -7232172879137209804L;


    private String text;

    private Sort sort;

    private List<Filter> filters;


    public Search() {}

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }
}

