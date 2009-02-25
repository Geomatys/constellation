/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package org.constellation.sos.io.filesystem;

// J2SE dependencies
import java.io.File;

// JAXB dependencies
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

// Constellation dependencies
import org.constellation.generic.database.Automatic;
import org.constellation.sml.AbstractSensorML;
import org.constellation.sos.io.SensorReader;
import org.constellation.ws.CstlServiceException;
import static org.constellation.ows.OWSExceptionCode.*;

/**
 *
 * @author Guilhem Legal (geomatys)
 */
public class FileSensorReader implements SensorReader {

    /**
     * A JAXB unmarshaller used to unmarshall the xml generated by the XMLWriter.
     */
    private Unmarshaller unmarshaller;

    private File dataDirectory;
    
    public FileSensorReader(Automatic configuration) throws CstlServiceException  {
        try {
            //we initialize the unmarshaller
            JAXBContext context = JAXBContext.newInstance("org.constellation.sml.v100:org.constellation.sml.v101");
            unmarshaller        = context.createUnmarshaller();

            this.dataDirectory  = configuration.getdataDirectory();
        } catch (JAXBException ex) {
            ex.printStackTrace();
            throw new CstlServiceException("JAXBException while starting the file system Sensor reader", NO_APPLICABLE_CODE);
        } 
    }

    @Override
    public AbstractSensorML getSensor(String sensorId) throws CstlServiceException {
        File sensorFile = new File(dataDirectory, sensorId + ".xml");
        if (sensorFile.exists()){
            try {
                Object unmarshalled = unmarshaller.unmarshal(sensorFile);
                if (unmarshalled instanceof JAXBElement) {
                    unmarshalled = ((JAXBElement) unmarshalled).getValue();
                }
                if (unmarshalled instanceof AbstractSensorML) {
                    return (AbstractSensorML) unmarshalled;
                } else {
                    throw new CstlServiceException("The form unmarshalled is not a sensor", NO_APPLICABLE_CODE);
                }
            } catch (JAXBException ex) {
                ex.printStackTrace();
                throw new CstlServiceException("JAXBException while unmarshalling the sensor", NO_APPLICABLE_CODE);
            }
        } else {
            throw new CstlServiceException("this sensor is not registered in the database!",
                        INVALID_PARAMETER_VALUE, "procedure");
        }
    }

    @Override
    public void destroy() {
    }

    public String getInfos() {
        return "Constellation Filesystem Sensor Reader 0.3";
    }
}
