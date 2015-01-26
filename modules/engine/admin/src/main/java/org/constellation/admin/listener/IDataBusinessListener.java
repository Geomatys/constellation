package org.constellation.admin.listener;

import org.constellation.engine.register.Data;

/**
 * Listener that make extension point for project based on Constellation that want to
 * add/change some behavior in Constellation DataBusiness.
 *
 * @author Quentin Boileau (Geomatys)
 */
public interface IDataBusinessListener {

    /**
     * Called after create new data entry.
     * @param newData
     */
    void postDataCreate(Data newData);

    /**
     * Called before delete a data in Data table.
     * @param removedData
     */
    void preDataDelete(Data removedData);

    /**
     * Called after delete a data in Data table.
     * @param removedData
     */
    void postDataDelete(Data removedData);
}
