/**
 * @author Hyacinthe MENIET
 * Created on 15 juil. 07
 */
package net.sicade.observation;

import java.io.FileWriter;
import java.math.BigInteger;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import net.opengis.gml.DirectPositionType;
import net.opengis.gml.PointType;
import net.sicade.swe.AnyResultEntry;
import net.sicade.swe.DataBlockDefinitionEntry;
import net.sicade.swe.DataRecordFieldEntry;
import net.sicade.swe.SimpleDataRecordEntry;
import net.sicade.swe.TextBlockEntry;

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
        
              
        
        DirectPositionType pos           = new DirectPositionType("urn:ogc:crs:EPSG:27582", new BigInteger("2"), 163000.2345192);
        PointType p                      = new PointType("STATION_LOCALISATION", pos);
        SamplingPointEntry sf            = new SamplingPointEntry("station1", "02442X0111/F", "Point d'eau BSSS", "urn:-sandre:object:bdrhf:123X", p);
        //SamplingFeatureEntry sf        = new SamplingFeatureEntry("station1", "02442X0111/F", "Point d'eau BSSS", "urn:-sandre:object:bdrhf:123X");
        PhenomenonEntry ph               = new PhenomenonEntry("level","urn:x-ogc:phenomenon:BRGM:level","Niveau d'eau dans une source" );
        ProcessEntry proc                = new ProcessEntry("un capteur");
        TemporalObjectEntry t            = new TemporalObjectEntry(new Date(2002,02,12),null);
        AnyResultEntry result            = new AnyResultEntry("idresultat", null, "un bloc de donné");
        List<SimpleDataRecordEntry> comp = new ArrayList<SimpleDataRecordEntry>();
        List<DataRecordFieldEntry> fds   = new ArrayList<DataRecordFieldEntry>();
        SimpleDataRecordEntry dr         = new SimpleDataRecordEntry("idDef", "idDataRecord", null, false, fds);
        comp.add(dr);
        TextBlockEntry tbe               = new TextBlockEntry("enc1", ",", "@@", '.');
        DataBlockDefinitionEntry dbd     = new DataBlockDefinitionEntry("idDef", comp, tbe);
        ObservationEntry request         = new ObservationEntry("obsTest", 
                                                            "une observation test",
                                                            sf,
                                                            ph,
                                                            proc,
                                                            null,
                                                            result,
                                                            t,
                                                            dbd);
        
       
        
        // Marshalles objects to the specified file
        JAXBContext context = JAXBContext.newInstance("net.sicade.observation:net.opengis.gml:net.sicade.swe");//ObservationEntry.class,SamplingPointEntry.class,PointType.class);//
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
