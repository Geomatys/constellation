/**
 * @author Hyacinthe MENIET
 * Created on 15 juil. 07
 */
package net.sicade.observation;

import java.io.FileWriter;
import java.sql.Date;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import net.sicade.swe.Point;
import net.sicade.swe.Position;

/**
 * Marshalles the created java Objects to the specified XML Document.
 */
public class TestJava2XML {
    
    /**
     * The main method.
     * @param args the path to file to generate.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        
        String fileName = "generated-ObservationEntry.xml";
        
        // Instanciates an ObjectFactory
                /*ObjectFactory objFactory = new ObjectFactory();
                // Creates first rental
                GetCapabilities request = objFactory.createGetCapabilities();*/
        Position pos = new Position("urn:ogc:crs:EPSG:27582", 2, "1630002345192");
        Point p = new Point("STATION_LOCALISATION", pos);
        SamplingPointEntry sf    = new SamplingPointEntry("station1", "02442X0111/F", "Point d'eau BSSS", "urn:-sandre:object:bdrhf:123X", p, "blavlabvlvlvl");
        PhenomenonEntry ph       = new PhenomenonEntry("level","urn:x-ogc:phenomenon:BRGM:level","Niveau d'eau dans une source" );
        ProcessEntry proc        = new ProcessEntry("un capteur");
        TemporalObjectEntry t    = new TemporalObjectEntry(new Date(2002,02,12),null);
        ObservationEntry request = new ObservationEntry("obsTest", 
                                                        "une observation test",
                                                        sf,
                                                        ph,
                                                        proc,
                                                        null,
                                                        "un resultat",
                                                        t,
                                                        "definition du result");
        
       
        
        // Marshalles objects to the specified file
        JAXBContext context = JAXBContext.newInstance(ObservationEntry.class,SamplingPointEntry.class);
        Marshaller marshaller = context.createMarshaller();
        try {
            marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper",new NamespacePrefixMapperImpl());
        } catch( PropertyException e ) {
            System.out.println("prefix non trouv");
        }
        
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(request, new FileWriter(fileName));
    }
    
}
