package org.constellation.gui;

import org.constellation.ws.rest.post.DataInformation;

import java.util.logging.Logger;

/**
 * @author Benjamin Garcia (Geomatys)
 */
public class DataInformationContainer {

    private static final Logger LOGGER = Logger.getLogger(DataInformationContainer.class.getName());

    private static DataInformation information;


    public static DataInformation getInformation() {
        return information;
    }

    public static void setInformation(DataInformation information) {
        DataInformationContainer.information = information;
    }
}
