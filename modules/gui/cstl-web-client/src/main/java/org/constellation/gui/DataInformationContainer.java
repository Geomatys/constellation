package org.constellation.gui;

import juzu.SessionScoped;
import org.constellation.ws.rest.post.DataInformation;

import javax.inject.Named;
import java.util.logging.Logger;

/**
 * @author Benjamin Garcia (Geomatys)
 */
@Named("informationContainer")
@SessionScoped
public class DataInformationContainer {

    private static DataInformation information;


    public DataInformation getInformation() {
        return information;
    }

    public void setInformation(DataInformation information) {
        this.information = information;
    }
}
