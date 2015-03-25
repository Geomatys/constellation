package org.constellation.engine.register;

import org.constellation.engine.register.jooq.tables.pojos.TaskParameter;

/**
 * @author Thomas Rouby (Geomatys))
 */
public class TaskParameterWithOwnerName extends TaskParameter {

    private String ownerName;

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
}
