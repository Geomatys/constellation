/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.constellation.sos.factory;

import java.util.Properties;
import org.constellation.configuration.ObservationFilterType;
import org.constellation.configuration.ObservationReaderType;
import org.constellation.configuration.ObservationWriterType;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.sos.io.ObservationFilter;
import org.constellation.sos.io.ObservationReader;
import org.constellation.sos.io.ObservationWriter;
import org.constellation.sos.io.SensorReader;
import org.constellation.sos.io.SensorWriter;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.factory.FactoryRegistry;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class SOSFactoryTest {

    private static FactoryRegistry factory = new FactoryRegistry(AbstractSOSFactory.class);

    private AbstractSOSFactory sosFactory;

    @BeforeClass
    public static void setUpClass() throws Exception {

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
         sosFactory = factory.getServiceProvider(AbstractSOSFactory.class, null, null, null);
    }

    @After
    public void tearDown() throws Exception {
    }


    /**
     * Tests the initialisation of the SOS worker with different configuration mistake
     *
     * @throws java.lang.Exception
     */
    @Test
    public void nullTypeTest() throws Exception {
    
        ObservationFilter of = sosFactory.getObservationFilter(null, null, null, null, null);
        assertTrue(of == null);

        ObservationReader or = sosFactory.getObservationReader(null, null, null);
        assertTrue(or == null);

        ObservationWriter ow = sosFactory.getObservationWriter(null, null);
        assertTrue(ow == null);

        SensorWriter sw = sosFactory.getSensorWriter(null, null, null);
        assertTrue(sw == null);

        SensorReader sr = sosFactory.getSensorReader(null, null, null, null);
        assertTrue(sr == null);
    }

     /**
     * Tests the initialisation of the SOS worker with different configuration mistake
     *
     * @throws java.lang.Exception
     */
    @Test
    public void defaultTypeTest() throws Exception {

        BDD bdd            = new BDD("org.postgresql.driver", "SomeUrl", "boby", "gary");
        Automatic config   = new Automatic("postgrid", bdd);
        boolean exLaunched = false;
        try  {
            ObservationFilter of = sosFactory.getObservationFilter(ObservationFilterType.DEFAULT, "idbase", "templateIdBase", new Properties(), config);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertTrue(ex.getMessage().contains("No suitable driver found for SomeUrl"));
        }
        assertTrue(exLaunched);

        exLaunched = false;
        try  {
            ObservationFilter of = sosFactory.getObservationFilter(ObservationFilterType.GENERIC, "idbase", "templateIdBase", new Properties(), config);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getMessage(), "Unable to find affinage.xml");
        }
        assertTrue(exLaunched);

        exLaunched = false;
        try  {
            ObservationReader or = sosFactory.getObservationReader(ObservationReaderType.DEFAULT, config, "idbase");
        } catch (CstlServiceException ex) {
            exLaunched = true;
            
        }
        assertTrue(exLaunched);

        exLaunched = false;
        try  {
            ObservationReader or = sosFactory.getObservationReader(ObservationReaderType.GENERIC, config, "idbase");
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertTrue(ex.getMessage().contains("No suitable driver found for SomeUrl"));

        }
        assertTrue(exLaunched);

        
        // TODO WHY no error ?? ObservationWriter ow = sosFactory.getObservationWriter(ObservationWriterType.DEFAULT, config);
        

    }

}
