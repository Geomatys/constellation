/**
 * @author Hyacinthe MENIET
 * Created on 15 juil. 07
 */
package net.sicade.observation.test;

import java.io.FileReader;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
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
              
        String fileName = "generated-ObservationEntry.xml";
        // Unmarshalles the given XML file to objects
        JAXBContext context;
        context = JAXBContext.newInstance(ObservationEntry.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        Object request =  unmarshaller.unmarshal(new FileReader(fileName));
        
        if(request instanceof ObservationEntry ){
            ObservationEntry o = (ObservationEntry) request;
            
           
                System.out.println(o.toString());
        }
        
        System.out.println();
    }
    
}
