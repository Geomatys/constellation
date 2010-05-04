

package org.constellation.sos.profile;

import java.io.File;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.sos.io.mdweb.MDWebSensorReader;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.geotoolkit.xml.MarshallerPool;
import org.mdweb.io.sql.v21.Writer21;
import org.mdweb.model.schemas.Classe;
import org.mdweb.model.schemas.Property;
import org.mdweb.model.schemas.Standard;
import org.mdweb.model.storage.RecordSet;

/**
 *
 * @author guilhem
 */
public class MDWSensorReaderProfile {

    private static final Logger LOGGER = Logger.getLogger("org.constellation.sos.profile");

    private static MarshallerPool pool;

    private static boolean write = false;

    private static boolean cacheStorage = false;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        BDD bdd = new BDD("org.postgresql.Driver","jdbc:postgresql://localhost:5432/mdweb21-full-testing", "guilhem", "brehan");
        Automatic configuration = new Automatic(null, bdd);

        Writer21 w21 = new Writer21(bdd.getDataSource(), true);

        

        Classe fc = w21.getClasse("FRA_DirectReferenceSystem", Standard.ISO_19115_FRA);

        Property fp = fc.getPropertyByName("referenceSystemIdentifier");
        
        Classe fc2 = fp.getType();

        System.out.println(fc2);


        Classe c = w21.getClasse("MD_ReferenceSystem", Standard.ISO_19115);

        Property p = c.getPropertyByName("referenceSystemIdentifier");

        Classe c2 = p.getType();

        System.out.println(c2);

        /*
        configuration.setEnablecache("" + cacheStorage);

        configuration.setConfigurationDirectory(new File("/home/guilhem/test-maping"));
        MDWebSensorReader reader = new MDWebSensorReader(configuration, new Properties());

        pool = new MarshallerPool("org.geotoolkit.sml.xml.v100:" +
                                 "org.geotoolkit.sml.xml.v101");

        LOGGER.info("start reading");*/


        // not threaded
        //toprofile(reader, "61279");

        //inRowSameForm(reader);
        

        
       //multiThreadDifferentForm(reader);

       //multiThreadSameForm(reader);
        
    }

     /**
     *  5 times the same form in row
     *
     * @param reader
     */
    public static void inRowSameForm(MDWebSensorReader reader) throws CstlServiceException {
        long start = System.currentTimeMillis();
        toprofile(reader, "61279");
        System.out.println("1st getMetadata in:" + (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();
        toprofile(reader, "61279");
        System.out.println("2st getMetadata in:" + (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();
        toprofile(reader, "61279");
        System.out.println("3st getMetadata in:" + (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();
        toprofile(reader, "61279");
        System.out.println("4st getMetadata in:" + (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();
        toprofile(reader, "61279");
        System.out.println("5st getMetadata in:" + (System.currentTimeMillis() - start));
    }

    /**
     *  5 thread different form
     *
     * @param reader
     */
    public static void multiThreadDifferentForm(MDWebSensorReader reader) {

        SensorReadThread srt1 = new SensorReadThread(reader, "61277");

        SensorReadThread srt2 = new SensorReadThread(reader, "61279");

        SensorReadThread srt3 = new SensorReadThread(reader, "13471");

        SensorReadThread srt4 = new SensorReadThread(reader, "13471-1017-5.0");

        SensorReadThread srt5 = new SensorReadThread(reader, "61277-1021-8.0");

        srt1.start();
        srt2.start();
        srt3.start();
        srt4.start();
        srt5.start();
    }

    /**
     *  5 thread same form
     *
     * @param reader
     */
    public static void multiThreadSameForm(MDWebSensorReader reader) {


        SensorReadThread srt1 = new SensorReadThread(reader, "61277");

        SensorReadThread srt2 = new SensorReadThread(reader, "61277");

        SensorReadThread srt3 = new SensorReadThread(reader, "61277");

        SensorReadThread srt4 = new SensorReadThread(reader, "61277");

        SensorReadThread srt5 = new SensorReadThread(reader, "61277");


        srt1.start();
        srt2.start();
        srt3.start();
        srt4.start();
        srt5.start();
    }

    public static AbstractSensorML toprofile(MDWebSensorReader reader, String platformCode) throws CstlServiceException {
        return reader.getSensor("urn:ogc:object:feature:Sensor:IFREMER:" + platformCode);
    }

    public static class SensorReadThread extends Thread {

         private String pltaformCode;

         private MDWebSensorReader reader;

         public SensorReadThread(MDWebSensorReader reader, String platformCode) {
             this.pltaformCode = platformCode;
             this.reader       = reader;
         }

        @Override
         public void run() {
            try {
                AbstractSensorML sensor = toprofile(reader, pltaformCode);
                
                if (write){
                    StringWriter sw = new StringWriter();
                    Marshaller m;
                    try {
                        m = pool.acquireMarshaller();
                        m.marshal(sensor, sw);
                        LOGGER.info("\n\n\n" + sw.toString() + "\n\n\n");
                    } catch (JAXBException ex) {
                        Logger.getLogger(MDWSensorReaderProfile.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
               
            } catch (CstlServiceException ex) {
                Logger.getLogger(MDWSensorReaderProfile.class.getName()).log(Level.SEVERE, null, ex);
            }
         }

    }
}
