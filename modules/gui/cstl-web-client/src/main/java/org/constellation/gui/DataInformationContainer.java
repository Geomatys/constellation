package org.constellation.gui;

import juzu.SessionScoped;
import org.constellation.dto.DataInformation;

import javax.inject.Named;
import java.util.logging.Logger;

/**
 * @author Benjamin Garcia (Geomatys)
 */
public class DataInformationContainer {

    private static DataInformation information;


    public static DataInformation getInformation() {
        return information;
    }

    public static void setInformation(DataInformation information) {
        DataInformationContainer.information = information;
    }
}
