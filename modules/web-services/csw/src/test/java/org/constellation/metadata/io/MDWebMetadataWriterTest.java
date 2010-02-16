/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2009, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */


package org.constellation.metadata.io;

import java.sql.Connection;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.Unmarshaller;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.metadata.AnchorPool;
import org.constellation.metadata.CSWClassesContext;
import org.constellation.util.Util;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.geotoolkit.sml.xml.v100.ComponentType;
import org.geotoolkit.sml.xml.v100.IoComponentPropertyType;
import org.geotoolkit.sml.xml.v100.SensorML;
import org.geotoolkit.swe.xml.v100.DataRecordType;
import org.geotoolkit.xml.MarshallerPool;

import org.junit.*;
import static org.junit.Assert.*;
/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class MDWebMetadataWriterTest {


    private static Automatic configuration;

    private MDWebMetadataReader reader;

    private MDWebMetadataWriter writer;

    private static MarshallerPool pool;

    private static DefaultDataSource ds;

    @BeforeClass
    public static void setUpClass() throws Exception {
        List<Class> classes = CSWClassesContext.getAllClassesList();
        classes.add(org.geotoolkit.sml.xml.v100.ObjectFactory.class);

        pool = new AnchorPool(classes);

        final String url = "jdbc:derby:memory:MMWTest;create=true";
        ds               = new DefaultDataSource(url);

        Connection con = ds.getConnection();

        Util.executeSQLScript("org/constellation/sql/structure-mdweb.sql", con);
        Util.executeSQLScript("org/constellation/sql/mdweb-base-data.sql", con);
        Util.executeSQLScript("org/constellation/sql/ISO19115-base-data.sql", con);
        Util.executeSQLScript("org/constellation/sql/ISO19115-data.sql", con);
        Util.executeSQLScript("org/constellation/sql/ISO19119-data.sql", con);
        Util.executeSQLScript("org/constellation/sql/DC-schema.sql", con);
        Util.executeSQLScript("org/constellation/sql/ebrim-schema.sql", con);
        Util.executeSQLScript("org/constellation/sql/mdweb-user-data.sql", con);
        Util.executeSQLScript("org/constellation/metadata/sql/csw-data.sql", con);

        Util.executeSQLScript("org/constellation/sql/sml-schema_v2.sql", con);
        Util.executeSQLScript("org/constellation/sql/sml-data_v2.sql", con);

        //we write the configuration file
        BDD bdd = new BDD("org.apache.derby.jdbc.EmbeddedDriver", url, "", "");
        configuration = new Automatic("mdweb", bdd);

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        if (ds != null) {
            ds.shutdown();
        }
    }

    @Before
    public void setUp() throws Exception {

        reader = new MDWebMetadataReader(configuration);
        writer = new MDWebMetadataWriter(configuration, null);
    }

    @After
    public void tearDown() throws Exception {
        if (reader != null) {
            reader.destroy();
        }
    }

    /**
     * Tests the storeMetadata method for SML data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void writeMetadataComponentSMLTest() throws Exception {

        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        AbstractSensorML absExpResult = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/component2.xml"));

        writer.storeMetadata(absExpResult);

        Object absResult = reader.getMetadata("14:SMLC", MetadataReader.SENSORML, null, null);

        assertTrue(absResult != null);
        assertTrue(absResult instanceof SensorML);
        assertTrue(absExpResult instanceof SensorML);
        SensorML result = (SensorML) absResult;
        SensorML expResult = (SensorML) absExpResult;

        assertEquals(expResult.getCapabilities(), result.getCapabilities());
        assertEquals(expResult.getCharacteristics(), result.getCharacteristics());
        assertEquals(expResult.getClassification(), result.getClassification());
        assertEquals(expResult.getContact(), result.getContact());
        assertEquals(expResult.getDocumentation(), result.getDocumentation());
        assertEquals(expResult.getHistory(), result.getHistory());
        assertEquals(expResult.getIdentification(), result.getIdentification());
        assertEquals(expResult.getKeywords(), result.getKeywords());
        assertEquals(expResult.getLegalConstraint(), result.getLegalConstraint());
        assertEquals(expResult.getSecurityConstraint(), result.getSecurityConstraint());
        assertEquals(expResult.getValidTime(), result.getValidTime());
        assertEquals(expResult.getVersion(), result.getVersion());

        assertEquals(expResult.getMember().size(), result.getMember().size());
        assertEquals(expResult.getMember().size(), 1);
        ComponentType expProcess = (ComponentType) expResult.getMember().iterator().next().getProcess().getValue();
        assertTrue(result.getMember().iterator().next().getProcess().getValue() instanceof ComponentType);
        ComponentType resProcess = (ComponentType) result.getMember().iterator().next().getProcess().getValue();

        assertEquals(expProcess.getOutputs().getOutputList().getOutput().size(), resProcess.getOutputs().getOutputList().getOutput().size());
        Iterator<IoComponentPropertyType> expIt = expProcess.getOutputs().getOutputList().getOutput().iterator();
        Iterator<IoComponentPropertyType> resIt = resProcess.getOutputs().getOutputList().getOutput().iterator();
        for (int i = 0; i < expProcess.getOutputs().getOutputList().getOutput().size(); i++) {
            IoComponentPropertyType resio = resIt.next();
            IoComponentPropertyType expio = expIt.next();
            assertEquals(expio, resio);
        }

        assertEquals(expProcess.getOutputs().getOutputList().getOutput(), resProcess.getOutputs().getOutputList().getOutput());
        assertEquals(expProcess.getOutputs().getOutputList(), resProcess.getOutputs().getOutputList());
        assertEquals(expProcess.getOutputs(), resProcess.getOutputs());

        assertEquals(expProcess.getBoundedBy(), resProcess.getBoundedBy());

        if (expProcess.getCapabilities().size() > 0 && resProcess.getCapabilities().size() > 0) {
            assertTrue(resProcess.getCapabilities().get(0).getAbstractDataRecord().getValue() instanceof DataRecordType);
            DataRecordType expRecord = (DataRecordType) expProcess.getCapabilities().get(0).getAbstractDataRecord().getValue();
            DataRecordType resRecord = (DataRecordType) resProcess.getCapabilities().get(0).getAbstractDataRecord().getValue();
            assertEquals(expRecord.getField(), resRecord.getField());
            assertEquals(expProcess.getCapabilities().get(0).getAbstractDataRecord().getValue(), resProcess.getCapabilities().get(0).getAbstractDataRecord().getValue());
            assertEquals(expProcess.getCapabilities().get(0), resProcess.getCapabilities().get(0));
        }
        assertEquals(expProcess.getCapabilities(), resProcess.getCapabilities());

        assertEquals(expProcess.getCharacteristics(), resProcess.getCharacteristics());

        assertEquals(expProcess.getClassification().size(), resProcess.getClassification().size());
        assertEquals(resProcess.getClassification().size(), 1);
        assertEquals(expProcess.getClassification().get(0).getClassifierList().getClassifier().size(), resProcess.getClassification().get(0).getClassifierList().getClassifier().size());

        assertEquals(expProcess.getClassification().get(0).getClassifierList().getClassifier(), resProcess.getClassification().get(0).getClassifierList().getClassifier());
        assertEquals(expProcess.getClassification().get(0).getClassifierList(), resProcess.getClassification().get(0).getClassifierList());
        assertEquals(expProcess.getClassification().get(0), resProcess.getClassification().get(0));
        assertEquals(expProcess.getClassification(), resProcess.getClassification());

        assertEquals(expProcess.getContact().iterator().next().getResponsibleParty(), resProcess.getContact().iterator().next().getResponsibleParty());
        assertEquals(expProcess.getContact().iterator().next(), resProcess.getContact().iterator().next());
        assertEquals(expProcess.getContact(), resProcess.getContact());
        assertEquals(expProcess.getDescription(), resProcess.getDescription());
        assertEquals(expProcess.getDescriptionReference(), resProcess.getDescriptionReference());
        assertEquals(expProcess.getDocumentation(), resProcess.getDocumentation());
        assertEquals(expProcess.getHistory(), resProcess.getHistory());
        assertEquals(expProcess.getId(), resProcess.getId());
        assertEquals(expProcess.getIdentification(), resProcess.getIdentification());
        assertEquals(expProcess.getInputs(), resProcess.getInputs());
        assertEquals(expProcess.getInterfaces(), resProcess.getInterfaces());
        assertEquals(expProcess.getKeywords(), resProcess.getKeywords());
        assertEquals(expProcess.getLegalConstraint(), resProcess.getLegalConstraint());
        assertEquals(expProcess.getLocation(), resProcess.getLocation());
        assertEquals(expProcess.getName(), resProcess.getName());

        assertEquals(expProcess.getParameters().getParameterList().getParameter().get(0), resProcess.getParameters().getParameterList().getParameter().get(0));
        assertEquals(expProcess.getParameters().getParameterList().getParameter().get(1), resProcess.getParameters().getParameterList().getParameter().get(1));
        assertEquals(expProcess.getParameters().getParameterList().getParameter().get(2).getQuantityRange(), resProcess.getParameters().getParameterList().getParameter().get(2).getQuantityRange());
        assertEquals(expProcess.getParameters().getParameterList().getParameter().get(2), resProcess.getParameters().getParameterList().getParameter().get(2));
        assertEquals(expProcess.getParameters().getParameterList().getParameter(), resProcess.getParameters().getParameterList().getParameter());
        assertEquals(expProcess.getParameters().getParameterList(), resProcess.getParameters().getParameterList());
        assertEquals(expProcess.getParameters(), resProcess.getParameters());
        assertEquals(expProcess.getSMLLocation(), resProcess.getSMLLocation());
        assertEquals(expProcess.getSpatialReferenceFrame(), resProcess.getSpatialReferenceFrame());
        assertEquals(expProcess.getSrsName(), resProcess.getSrsName());
        assertEquals(expProcess.getTemporalReferenceFrame(), resProcess.getTemporalReferenceFrame());
        assertEquals(expProcess.getTimePosition(), resProcess.getTimePosition());
        assertEquals(expProcess.getValidTime(), resProcess.getValidTime());




        assertEquals(expResult.getMember().iterator().next().getArcrole(), result.getMember().iterator().next().getArcrole());
        assertEquals(expResult.getMember().iterator().next(), result.getMember().iterator().next());
        assertEquals(expResult.getMember(), result.getMember());


        assertEquals(expResult, result);
    }

}
