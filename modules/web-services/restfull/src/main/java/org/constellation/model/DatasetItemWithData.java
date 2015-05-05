package org.constellation.model;

import org.constellation.engine.register.pojo.DataItem;
import org.constellation.engine.register.pojo.DatasetItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabien Bernard (Geomatys).
 */
public class DatasetItemWithData extends DatasetItem {

    private static final long serialVersionUID = 3987093140303794382L;


    protected List<DataItem> data = new ArrayList<>();


    public List<DataItem> getData() {
        return data;
    }

    public DatasetItemWithData setData(List<DataItem> data) {
        this.data = data;
        return this;
    }
}
