package org.constellation.admin.listener;

import org.constellation.engine.register.jooq.tables.pojos.Data;

/**
 * Empty implementation of IDataBusinessListener.
 *
 * @author Quentin Boileau (Geomatys)
 */
public class DefaultDataBusinessListener implements IDataBusinessListener {


    @Override
    public void postDataCreate(Data newData) {
        //empty or default
    }

    @Override
    public void preDataDelete(Data removedData) {
        //empty or default
    }

    @Override
    public void postDataDelete(Data removedData) {
        //empty or default
    }
}
