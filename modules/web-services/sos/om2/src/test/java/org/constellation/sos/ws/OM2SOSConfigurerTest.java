/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.constellation.sos.ws;

import java.io.File;
import java.sql.Connection;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.xml.bind.Marshaller;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.admin.SpringHelper;
import org.constellation.business.IServiceBusiness;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.configuration.DataSourceType;
import org.constellation.configuration.SOSConfiguration;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.test.utils.Order;
import org.constellation.test.utils.SpringTestRunner;
import org.constellation.util.Util;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.util.sql.DerbySqlScriptRunner;
import org.apache.sis.util.logging.Logging;
import org.geotoolkit.util.FileUtilities;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@RunWith(SpringTestRunner.class)
public class OM2SOSConfigurerTest extends SOSConfigurerTest {

    private static DefaultDataSource ds = null;

    @Inject
    private IServiceBusiness serviceBusiness;

    private static String url;

    private static boolean initialized = false;

    @BeforeClass
    public static void setUpClass() throws Exception {
        url = "jdbc:derby:memory:OM2ConfigTest2;create=true";
        ds = new DefaultDataSource(url);

        Connection con = ds.getConnection();

        DerbySqlScriptRunner sr = new DerbySqlScriptRunner(con);
        sr.setEncoding("UTF-8");
        String sql = FileUtilities.getStringFromStream(Util.getResourceAsStream("org/constellation/om2/structure_observations.sql"));
        sql = sql.replace("$SCHEMA", "");
        sr.run(sql);
        sr.run(Util.getResourceAsStream("org/constellation/sql/sos-data-om2.sql"));


        MarshallerPool pool   = GenericDatabaseMarshallerPool.getInstance();
        Marshaller marshaller =  pool.acquireMarshaller();

        ConfigDirectory.setupTestEnvironement("OM2SOSConfigurerTest");
        pool.recycle(marshaller);
    }

    @PostConstruct
    public void setUp() {
        SpringHelper.setApplicationContext(applicationContext);
        SpringHelper.injectDependencies(configurer);
        try {

            if (!initialized) {
                serviceBusiness.deleteAll();

                //we write the configuration file
                Automatic SMLConfiguration = new Automatic();

                Automatic OMConfiguration  = new Automatic();
                BDD bdd = new BDD("org.apache.derby.jdbc.EmbeddedDriver", url, "", "");
                OMConfiguration.setBdd(bdd);
                SOSConfiguration configuration = new SOSConfiguration(SMLConfiguration, OMConfiguration);
                configuration.setObservationReaderType(DataSourceType.OM2);
                configuration.setObservationWriterType(DataSourceType.OM2);
                configuration.setObservationFilterType(DataSourceType.OM2);
                configuration.setSMLType(DataSourceType.NONE);
                configuration.setPhenomenonIdBase("urn:ogc:def:phenomenon:GEOM:");
                configuration.setProfile("transactional");
                configuration.setObservationTemplateIdBase("urn:ogc:object:observation:template:GEOM:");
                configuration.setObservationIdBase("urn:ogc:object:observation:GEOM:");
                configuration.setSensorIdBase("urn:ogc:object:sensor:GEOM:");
                configuration.getParameters().put("transactionSecurized", "false");

                serviceBusiness.create("sos", "default", configuration, null);
                initialized = true;
            }
        } catch (Exception ex) {
            Logging.getLogger("org.constellation.sos.ws").log(Level.SEVERE, null, ex);
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        File derbyLog = new File("derby.log");
        if (derbyLog.exists()) {
            derbyLog.delete();
        }
        File mappingFile = new File("mapping.properties");
        if (mappingFile.exists()) {
            mappingFile.delete();
        }
        if (ds != null) {
            ds.shutdown();
        }
        ConfigDirectory.shutdownTestEnvironement("OM2SOSConfigurerTest");
    }

    @Test
    @Override
    @Order(order=1)
    public void getObservationsCsvTest() throws Exception {
        super.getObservationsCsvTest();

    }

    @Test
    @Override
    @Order(order=2)
    public void getObservationsCsvProfileTest() throws Exception {
        super.getObservationsCsvProfileTest();

    }
}
