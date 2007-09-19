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
        
        String fileName = "generated-request-getCapabilities.xml";
        
        // Instanciates an ObjectFactory
                /*ObjectFactory objFactory = new ObjectFactory();
                // Creates first rental
                GetCapabilities request = objFactory.createGetCapabilities();*/
        
        SamplingFeatureEntry sf  = new SamplingFeatureEntry("idTest", "nomSampl", "une station", "sampled feature");
        PhenomenonEntry p        = new PhenomenonEntry("phenomene test", "un phenomene");
        ProcessEntry proc        = new ProcessEntry("un capteur");
        TemporalObjectEntry t    = new TemporalObjectEntry(new Date(2002,02,12),null);
        ObservationEntry request = new ObservationEntry("obsTest", 
                                                        "une observation test",
                                                        sf,
                                                        p,
                                                        proc,
                                                        null,
                                                        "un resultat",
                                                        t,
                                                        "definition du result");
        
       
        
        // Marshalles objects to the specified file
        JAXBContext context = JAXBContext.newInstance(ObservationEntry.class);
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
