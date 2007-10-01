/**
 * @author Hyacinthe MENIET
 * Created on 15 juil. 07
 */
package net.sicade.observation.test;

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
import net.sicade.coverage.model.Distribution;
import net.sicade.observation.*;
import net.sicade.swe.AnyResultEntry;
import net.sicade.swe.DataBlockDefinitionEntry;
import net.sicade.swe.AnyScalarEntry;
import net.sicade.gml.ReferenceEntry;
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
        
              
        // La station
        //SamplingFeatureEntry sf        = new SamplingFeatureEntry("station1", "02442X0111/F", "Point d'eau BSSS", "urn:-sandre:object:bdrhf:123X");
        DirectPositionType pos           = new DirectPositionType("urn:ogc:crs:EPSG:27582", new BigInteger("2"), 163000.2345192);
        PointType p                      = new PointType("STATION_LOCALISATION", pos);
        SamplingPointEntry sf            = new SamplingPointEntry("station1", "02442X0111/F", "Point d'eau BSSS", "urn:-sandre:object:bdrhf:123X", p);
        
        // le phenomene observé
        //PhenomenonEntry ph               = new PhenomenonEntry("level","urn:x-ogc:phenomenon:BRGM:level","Niveau d'eau dans une source" );
        List<PhenomenonEntry> compPheno  = new ArrayList<PhenomenonEntry>();
        PhenomenonEntry ph1              = new PhenomenonEntry("phe1","urn:x-ogc:phenomenon:BRGM:phe1","un phenomene" );
        PhenomenonEntry ph2              = new PhenomenonEntry("phe2","urn:x-ogc:phenomenon:BRGM:phe2","un phenomene" );
        PhenomenonEntry ph3              = new PhenomenonEntry("phe3","urn:x-ogc:phenomenon:BRGM:phe3","un phenomene" );
        compPheno.add(ph1);compPheno.add(ph2);compPheno.add(ph3);
        CompositePhenomenonEntry ph      = new CompositePhenomenonEntry("aggregatePhenomenon", "urn:x-ogc:phenomenon:BRGM:aggregate", null, null, compPheno);
        
        // le capteur
        ProcessEntry proc                = new ProcessEntry("un capteur");
        
        // le samping time
        TemporalObjectEntry t            = new TemporalObjectEntry(new Date(2002,02,12),null);
        
        //le resultat
        //AnyResultEntry result            = new AnyResultEntry("idresultat", null, "un bloc de donné");
        //UnitOfMeasureEntry uom           = new UnitOfMeasureEntry("unit1", "centimetre", "longueur", "refSyS");
        //MeasureEntry result              = new MeasureEntry("mesure1", uom, 35 );
        ReferenceEntry ref               = new ReferenceEntry("ref1", "blablabla");   
        AnyResultEntry result            = new AnyResultEntry("idresultat", ref, null);
        
        // la description du resultat
        List<SimpleDataRecordEntry> comp = new ArrayList<SimpleDataRecordEntry>();
        List<AnyScalarEntry> fds   = new ArrayList<AnyScalarEntry>();
        SimpleDataRecordEntry dr         = new SimpleDataRecordEntry("idDef", "idDataRecord", null, false, fds);
        comp.add(dr);
        TextBlockEntry tbe               = new TextBlockEntry("enc1", ",", "@@", '.');
        DataBlockDefinitionEntry dbd     = new DataBlockDefinitionEntry("idDef", comp, tbe);
        
        // l'observation
        ObservationEntry request         = new ObservationEntry("obsTest", 
                                                            "une observation test",
                                                            sf,
                                                            ph,
                                                            proc,
                                                            Distribution.NORMAL,
                                                            result,
                                                            t,
                                                            dbd);
        /*MeasurementEntry request         = new MeasurementEntry("obsTest", 
                                                                "une observation test",
                                                                sf,
                                                                ph,
                                                                proc,
                                                                Distribution.NORMAL,
                                                                result,
                                                                t,
                                                                null);*/
        
       
        
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
