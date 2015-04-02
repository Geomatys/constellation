package org.constellation.model.metadata;

import java.io.Serializable;

/**
 * @author Mehdi Sidhoum (Geomatys).
 */
public class Profile implements Serializable {

    private String name;

    private int count;

    public Profile(){}

    public Profile(final String name, final int count) {
        this.name = name;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
