
package net.sicade.observation.test;

import java.util.ArrayList;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Logger;
import net.sicade.catalog.Database;
import net.sicade.coverage.model.Distribution;
import net.sicade.gml.DirectPositionType;
import net.sicade.gml.PointType;
import net.sicade.gml.ReferenceEntry;
import net.sicade.gml.UnitOfMeasureEntry;
import net.sicade.observation.CompositePhenomenonEntry;
import net.sicade.observation.MeasureEntry;
import net.sicade.observation.MeasurementEntry;
import net.sicade.observation.MeasurementTable;
import net.sicade.observation.ObservationEntry;
import net.sicade.observation.ObservationTable;
import net.sicade.observation.PhenomenonEntry;
import net.sicade.observation.ProcessEntry;
import net.sicade.observation.SamplingPointEntry;
import net.sicade.observation.TemporalObjectEntry;
import net.sicade.sos.ResponseMode;
import net.sicade.swe.AnyScalarEntry;
import net.sicade.swe.DataBlockDefinitionEntry;
import net.sicade.swe.SimpleDataRecordEntry;
import net.sicade.swe.TextBlockEntry;
import org.postgresql.ds.PGSimpleDataSource;

/**
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class DatabaseTest {
    
    public static Logger logger = Logger.getLogger("net.sicade.test");
    /**
     * The main method.
     * @param args the path to file to generate.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        /*// La station
        //SamplingFeatureEntry sf        = new SamplingFeatureEntry("station1", "02442X0111/F", "Point d'eau BSSS", "urn:-sandre:object:bdrhf:123X");
        List<Double> values = new ArrayList<Double>();
        values.add(163000.0);
        values.add(2345192.0);
        DirectPositionType pos           = new DirectPositionType("urn:ogc:crs:EPSG:27582", 2, values);
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
        ProcessEntry proc                = new ProcessEntry("urn:ogc:object:sensor:BRGM:12349", null);
        
        // le sampling time
        TemporalObjectEntry t            = new TemporalObjectEntry(Timestamp.valueOf("2002-02-12 10:20:14"),null);
        
        //le resultat
        //AnyResultEntry result            = new AnyResultEntry("idresultat", null, "un bloc de donnée");
        UnitOfMeasureEntry uom           = new UnitOfMeasureEntry("unit1", "centimetre", "longueur", "refSyS");
        MeasureEntry mesure              = new MeasureEntry("mesure1", uom, 35 );
        ReferenceEntry result            = new ReferenceEntry("ref2", "blablabla");
        
        // la description du resultat
        List<SimpleDataRecordEntry> comp = new ArrayList<SimpleDataRecordEntry>();
        List<AnyScalarEntry> fds         = new ArrayList<AnyScalarEntry>();
        AnyScalarEntry  f1               = new AnyScalarEntry("idDataRecord", "time", "urn:x-ogc:def:phenomenon:OGC:time", "Time", "urn:x-ogc:def:unit:ISO:8601", null);
        AnyScalarEntry  f2               = new AnyScalarEntry("idDataRecord", "depth", "urn:x-ogc:def:phenomenon:BRGM:depth", "Quantity", "cm", null);
        AnyScalarEntry  f3               = new AnyScalarEntry("idDataRecord", "validity", "urn:x-ogc:def:phenomenon:BRGM:validity", "Boolean", null, null);
        fds.add(f1);fds.add(f2);fds.add(f3);
        SimpleDataRecordEntry dr         = new SimpleDataRecordEntry("idDef", "idDataRecord", null, false, fds);
        comp.add(dr);
        TextBlockEntry tbe               = new TextBlockEntry("enc1", ",", "@@", '.');
        DataBlockDefinitionEntry dbd     = new DataBlockDefinitionEntry("idDef", comp, tbe);
        
        // l'observation
        ObservationEntry request         = new ObservationEntry("obsTest1",
                "une observation test",
                sf,
                ph,
                proc,
                Distribution.NORMAL,
                result,
                t,
                dbd);
        MeasurementEntry meas           = new MeasurementEntry("measTest",
                                                                "une mesure test",
                                                                sf,
                                                                ph,
                                                                proc,
                                                                Distribution.NORMAL,
                                                                mesure,
                                                                t,
                                                                null);*/
        
        StringBuilder s = new StringBuilder();
        s.append("551411;20000;bla");
        System.out.println(s.toString());
        ResponseMode r = ResponseMode.RESULT_TEMPLATE;
        System.out.println(r.value() +" name:" + r.name());
        /*
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setServerName("localhost");
        dataSource.setDatabaseName("seagis");
        dataSource.setUser("postgres");
        dataSource.setPassword("postgres");
        Database db = new Database(dataSource);
        
        ObservationTable obsTable = new ObservationTable(db);
        obsTable.getIdentifier(request);
        */
        
        /*MeasurementTable measTable = new MeasurementTable(db);
        MeasurementEntry m = (MeasurementEntry) measTable.getEntry("test");
        if (m == null) logger.finer("OK");
        else logger.finer (":-((((((((((((((");*/
        //measTable.getIdentifier(meas);
        /*ObservationEntry obs = (ObservationEntry) obsTable.getEntry("obsTest3");
        
        MeasurementTable obsTable = new MeasurementTable(db);
        MeasurementEntry obs = (MeasurementEntry) obsTable.getEntry("MeasurementTest1");
        
        logger.finer("obs null:" + (obs == null));
        
        logger.finer("name : "               + obs.getName());
        logger.finer("desc : "               + obs.getDefinition());
        logger.finer("process : "            + obs.getProcedure().toString());
        logger.finer("phenomenon : "         + obs.getObservedProperty().toString());
        logger.finer("distrib : "            + obs.getDistribution().toString());
        logger.finer("samplingTimeBegin : "  + obs.getSamplingTime().toString());
        logger.finer("feature of interest: " + obs.getFeatureOfInterest().toString());
       
        if(obs.getResult() != null ) {
            logger.finer("result class: "    + obs.getResult().getClass().getSimpleName());
            logger.finer("result : "         + obs.getResult());
        } else {
            logger.finer("result est null");
        }
        
        if (obs.getResultDefinition() != null) {
            logger.finer(" result definition class : " +
                    obs.getResultDefinition().getClass().getSimpleName() );
            if(obs.getResultDefinition() instanceof DataBlockDefinitionEntry ) {
                DataBlockDefinitionEntry dbde = (DataBlockDefinitionEntry)obs.getResultDefinition();
                logger.finer(dbde.toString());
            }
        }
        */
        
        
    }
    
}
