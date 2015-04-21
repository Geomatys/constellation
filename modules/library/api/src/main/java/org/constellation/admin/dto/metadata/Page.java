package org.constellation.admin.dto.metadata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mehdi Sidhoum (Geomatys).
 */
public class Page<T> implements Serializable {

    private int number;

    private int size;

    private long total;

    private List<T> content;


    public Page() {
        this(1, 0, 0, new ArrayList<T>());
    }

    public Page(int number, int size, long total, List<T> content) {
        this.number = number;
        this.size = size;
        this.total = total;
        this.content = content;
    }

    public int getNumber() {
        return number;
    }

    public Page<T> setNumber(int number) {
        this.number = number;
        return this;
    }

    public int getSize() {
        return size;
    }

    public Page<T> setSize(int size) {
        this.size = size;
        return this;
    }

    public long getTotal() {
        return total;
    }

    public Page<T> setTotal(long total) {
        this.total = total;
        return this;
    }

    public List<T> getContent() {
        return content;
    }

    public Page<T> setContent(List<T> content) {
        this.content = content;
        return this;
    }
}
