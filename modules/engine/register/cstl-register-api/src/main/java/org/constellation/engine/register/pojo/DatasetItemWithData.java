package org.constellation.engine.register.pojo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabien Bernard (Geomatys).
 */
public class DatasetItemWithData extends DatasetItem {

    private static final long serialVersionUID = 3987093140303794382L;

    protected List<DataItem> data = new ArrayList<>();

    public DatasetItemWithData() {
        
    }
    
    public DatasetItemWithData(final DatasetItem item, final List<DataItem> data) {
        this.data                      = data;
        this.creationDate              = item.getCreationDate();
        this.id                        = item.getId();
        this.dataCount                 = item.getDataCount();
        this.name                      = item.getName();
        this.ownerId                   = item.getOwnerId();
        this.ownerLogin                = item.getOwnerLogin();
    }

    public List<DataItem> getData() {
        return data;
    }

    public DatasetItemWithData setData(List<DataItem> data) {
        this.data = data;
        return this;
    }
}
