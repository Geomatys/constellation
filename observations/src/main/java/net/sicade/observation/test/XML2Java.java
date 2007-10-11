/**
 * @author Hyacinthe MENIET
 * Created on 15 juil. 07
 */
package net.sicade.observation.test;

import java.io.FileReader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import net.opengeospatial.sos.InsertObservation;
import net.opengeospatial.sos.RegisterSensor;
import net.sicade.observation.*;

/**
 * Unmarshalles the given XML Document.
 */
public class XML2Java {

    /**
     * The main method.
     * @param args the path to file to read.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        String fileName = "generated-Measurement.xml";
        // Unmarshalles the given XML file to objects
        JAXBContext context;
        context = JAXBContext.newInstance("net.opengeospatial.sos:net.opengis.ogc:net.opengis.ows:net.opengis.gml:net.sicade.observation:net.sicade.swe");
        Unmarshaller unmarshaller = context.createUnmarshaller();
        Object request = unmarshaller.unmarshal(new FileReader(fileName));

        if (request instanceof MeasurementEntry) {
            MeasurementEntry meas = (MeasurementEntry)request;
            if(meas.getResult() == null) System.out.println("result null");
            else System.out.println("result non null");
        } else if (request instanceof ObservationEntry) {
            ObservationEntry o = (ObservationEntry) request;


            System.out.println(o.toString());
        }
        
        
        if (request instanceof RegisterSensor) {
            RegisterSensor o = (RegisterSensor) request;
           
            if (o.getSensorDescription() != null) {
                System.out.println("sensor desc non null");
            } else {
                System.out.println("sensor desc null");
            }
            if (o.getObservationTemplate() != null) {
                System.out.println("observation non null");
                System.out.println(o.getObservationTemplate().toString());
            } else {
                System.out.println("observation null");
            }
        }
        
        
    }
}