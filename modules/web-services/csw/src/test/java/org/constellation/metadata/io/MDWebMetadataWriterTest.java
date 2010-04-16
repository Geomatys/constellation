/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2010, Geomatys
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
import java.util.logging.Logger;
import javax.xml.bind.Unmarshaller;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.metadata.AnchorPool;
import org.constellation.util.Util;
import org.geotoolkit.ebrim.xml.EBRIMClassesContext;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.geotoolkit.sml.xml.v100.ComponentType;
import org.geotoolkit.sml.xml.v100.IoComponentPropertyType;
import org.geotoolkit.sml.xml.v100.SensorML;
import org.geotoolkit.sml.xml.v100.SystemType;
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
        List<Class> classes = EBRIMClassesContext.getAllClassesList();
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

        Util.executeSQLScript("org/constellation/sql/sml-schema_v2.sql", con, true);
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
        writer = new MDWebMetadataWriter(configuration);
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

        Object absResult = reader.getMetadata("14:SMLC", AbstractMetadataReader.SENSORML,  null);

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

        assertEquals(expProcess.getBoundedBy().getEnvelope().getLowerCorner(), resProcess.getBoundedBy().getEnvelope().getLowerCorner());
        assertEquals(expProcess.getBoundedBy().getEnvelope(), resProcess.getBoundedBy().getEnvelope());
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

        pool.release(unmarshaller);
    }
    /**
     * Tests the storeMetadata method for SML data
     *
     * @throws java.lang.Exception
     */
    @Test
    public void writeMetadataSystemSMLTest() throws Exception {
        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        AbstractSensorML absExpResult = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/system.xml"));

        writer.storeMetadata(absExpResult);

        AbstractSensorML absResult = (AbstractSensorML) reader.getMetadata("15:SMLC", AbstractMetadataReader.SENSORML, null);

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
        SystemType expSysProcess = (SystemType) expResult.getMember().iterator().next().getProcess().getValue();
        assertTrue(result.getMember().iterator().next().getProcess().getValue() instanceof SystemType);
        SystemType resSysProcess = (SystemType) result.getMember().iterator().next().getProcess().getValue();

        assertEquals(expSysProcess.getOutputs().getOutputList().getOutput().size(), resSysProcess.getOutputs().getOutputList().getOutput().size());
        Iterator<IoComponentPropertyType> expIt = expSysProcess.getOutputs().getOutputList().getOutput().iterator();
        Iterator<IoComponentPropertyType> resIt = resSysProcess.getOutputs().getOutputList().getOutput().iterator();
        for (int i = 0; i < expSysProcess.getOutputs().getOutputList().getOutput().size(); i++) {
            IoComponentPropertyType resio = resIt.next();
            IoComponentPropertyType expio = expIt.next();
            DataRecordType resIoRec       = (DataRecordType) resio.getAbstractDataRecord().getValue();
            DataRecordType expIoRec       = (DataRecordType) expio.getAbstractDataRecord().getValue();
            assertEquals(expIoRec.getId(), resIoRec.getId());
            assertEquals(expIoRec.getField().size(), resIoRec.getField().size());
            assertEquals(expIoRec.getField().get(0), resIoRec.getField().get(0));
            assertEquals(expIoRec.getField().get(1), resIoRec.getField().get(1));
            assertEquals(expIoRec.getField().get(2), resIoRec.getField().get(2));
            assertEquals(expIoRec.getField(), resIoRec.getField());
            assertEquals(expIoRec, resIoRec);
            assertEquals(expio, resio);
        }

        assertEquals(expSysProcess.getOutputs().getOutputList().getOutput(), resSysProcess.getOutputs().getOutputList().getOutput());
        assertEquals(expSysProcess.getOutputs().getOutputList(), resSysProcess.getOutputs().getOutputList());
        assertEquals(expSysProcess.getOutputs(), resSysProcess.getOutputs());

        assertEquals(expSysProcess.getBoundedBy(), resSysProcess.getBoundedBy());

        if (expSysProcess.getCapabilities().size() > 0 && resSysProcess.getCapabilities().size() > 0) {
            assertTrue(resSysProcess.getCapabilities().get(0).getAbstractDataRecord().getValue() instanceof DataRecordType);
            DataRecordType expRecord = (DataRecordType) expSysProcess.getCapabilities().get(0).getAbstractDataRecord().getValue();
            DataRecordType resRecord = (DataRecordType) resSysProcess.getCapabilities().get(0).getAbstractDataRecord().getValue();
            assertEquals(expRecord.getField(), resRecord.getField());
            assertEquals(expSysProcess.getCapabilities().get(0).getAbstractDataRecord().getValue(), resSysProcess.getCapabilities().get(0).getAbstractDataRecord().getValue());
            assertEquals(expSysProcess.getCapabilities().get(0), resSysProcess.getCapabilities().get(0));
        }
        assertEquals(expSysProcess.getCapabilities(), resSysProcess.getCapabilities());

        assertEquals(expSysProcess.getCharacteristics().iterator().next(), resSysProcess.getCharacteristics().iterator().next());
        assertEquals(expSysProcess.getCharacteristics(), resSysProcess.getCharacteristics());

        assertEquals(expSysProcess.getClassification().size(), resSysProcess.getClassification().size());
        assertEquals(resSysProcess.getClassification().size(), 1);
        assertEquals(expSysProcess.getClassification().get(0).getClassifierList().getClassifier().size(), resSysProcess.getClassification().get(0).getClassifierList().getClassifier().size());
        for (int i = 0; i < 10; i++) {
            assertEquals(expSysProcess.getClassification().get(0).getClassifierList().getClassifier().get(i), resSysProcess.getClassification().get(0).getClassifierList().getClassifier().get(i));
        }
        assertEquals(expSysProcess.getClassification().get(0).getClassifierList().getClassifier(), resSysProcess.getClassification().get(0).getClassifierList().getClassifier());
        assertEquals(expSysProcess.getClassification().get(0).getClassifierList(), resSysProcess.getClassification().get(0).getClassifierList());
        assertEquals(expSysProcess.getClassification().get(0), resSysProcess.getClassification().get(0));
        assertEquals(expSysProcess.getClassification(), resSysProcess.getClassification());
        assertEquals(expSysProcess.getConnections(), resSysProcess.getConnections());

        assertEquals(expSysProcess.getContact().iterator().next().getResponsibleParty().getContactInfo().getAddress(), resSysProcess.getContact().iterator().next().getResponsibleParty().getContactInfo().getAddress());
        assertEquals(expSysProcess.getContact().iterator().next().getResponsibleParty().getContactInfo(), resSysProcess.getContact().iterator().next().getResponsibleParty().getContactInfo());
        assertEquals(expSysProcess.getContact().iterator().next().getResponsibleParty(), resSysProcess.getContact().iterator().next().getResponsibleParty());
        assertEquals(expSysProcess.getContact().iterator().next(), resSysProcess.getContact().iterator().next());
        assertEquals(expSysProcess.getContact(), resSysProcess.getContact());
        assertEquals(expSysProcess.getDescription(), resSysProcess.getDescription());
        assertEquals(expSysProcess.getDescriptionReference(), resSysProcess.getDescriptionReference());
        assertEquals(expSysProcess.getDocumentation().size(), resSysProcess.getDocumentation().size());
        assertEquals(expSysProcess.getDocumentation().get(0).getDocument().getOnlineResource(), resSysProcess.getDocumentation().get(0).getDocument().getOnlineResource());
        assertEquals(expSysProcess.getDocumentation().get(0).getDocument().getDescription(), resSysProcess.getDocumentation().get(0).getDocument().getDescription());
        assertEquals(expSysProcess.getDocumentation().get(0).getDocument(), resSysProcess.getDocumentation().get(0).getDocument());
        assertEquals(expSysProcess.getDocumentation().get(0).getDocumentList(), resSysProcess.getDocumentation().get(0).getDocumentList());
        assertEquals(expSysProcess.getDocumentation().get(0), resSysProcess.getDocumentation().get(0));
        assertEquals(expSysProcess.getDocumentation(), resSysProcess.getDocumentation());
        assertEquals(expSysProcess.getHistory(), resSysProcess.getHistory());
        assertEquals(expSysProcess.getId(), resSysProcess.getId());
        assertEquals(expSysProcess.getIdentification(), resSysProcess.getIdentification());
        assertEquals(expSysProcess.getInputs(), resSysProcess.getInputs());
        assertEquals(expSysProcess.getInterfaces(), resSysProcess.getInterfaces());
        assertEquals(expSysProcess.getKeywords(), resSysProcess.getKeywords());
        assertEquals(expSysProcess.getLegalConstraint().get(0).getRights().getDocumentation().getDocument().getDescription(), resSysProcess.getLegalConstraint().get(0).getRights().getDocumentation().getDocument().getDescription());
        assertEquals(expSysProcess.getLegalConstraint().get(0).getRights().getDocumentation().getDocument(), resSysProcess.getLegalConstraint().get(0).getRights().getDocumentation().getDocument());
        assertEquals(expSysProcess.getLegalConstraint().get(0).getRights().getDocumentation(), resSysProcess.getLegalConstraint().get(0).getRights().getDocumentation());
        assertEquals(expSysProcess.getLegalConstraint().get(0).getRights(), resSysProcess.getLegalConstraint().get(0).getRights());
        assertEquals(expSysProcess.getLegalConstraint().get(0), resSysProcess.getLegalConstraint().get(0));
        assertEquals(expSysProcess.getLegalConstraint(), resSysProcess.getLegalConstraint());
        assertEquals(expSysProcess.getLocation(), resSysProcess.getLocation());
        assertEquals(expSysProcess.getName(), resSysProcess.getName());
        assertEquals(expSysProcess.getComponents(), resSysProcess.getComponents());

        assertEquals(expSysProcess.getParameters(), resSysProcess.getParameters());
        assertEquals(expSysProcess.getPosition(), resSysProcess.getPosition());
        assertEquals(expSysProcess.getPositions().getPositionList().getPosition().get(0).getPosition().getLocation().getVector().getDefinition(), resSysProcess.getPositions().getPositionList().getPosition().get(0).getPosition().getLocation().getVector().getDefinition());
        assertEquals(expSysProcess.getPositions().getPositionList().getPosition().get(0).getPosition().getLocation().getVector().getCoordinate(), resSysProcess.getPositions().getPositionList().getPosition().get(0).getPosition().getLocation().getVector().getCoordinate());
        assertEquals(expSysProcess.getPositions().getPositionList().getPosition().get(0).getPosition().getLocation().getVector(), resSysProcess.getPositions().getPositionList().getPosition().get(0).getPosition().getLocation().getVector());
        assertEquals(expSysProcess.getPositions().getPositionList().getPosition().get(0).getPosition().getLocation(), resSysProcess.getPositions().getPositionList().getPosition().get(0).getPosition().getLocation());
        assertEquals(expSysProcess.getPositions().getPositionList().getPosition().get(0).getPosition(), resSysProcess.getPositions().getPositionList().getPosition().get(0).getPosition());
        assertEquals(expSysProcess.getPositions().getPositionList().getPosition().get(0).getVector(), resSysProcess.getPositions().getPositionList().getPosition().get(0).getVector());
        assertEquals(expSysProcess.getPositions().getPositionList().getPosition().get(0).getName(), resSysProcess.getPositions().getPositionList().getPosition().get(0).getName());
        assertEquals(expSysProcess.getPositions().getPositionList().getPosition().get(0), resSysProcess.getPositions().getPositionList().getPosition().get(0));
        assertEquals(expSysProcess.getPositions().getPositionList().getPosition(), resSysProcess.getPositions().getPositionList().getPosition());
        assertEquals(expSysProcess.getPositions().getPositionList(), resSysProcess.getPositions().getPositionList());
        assertEquals(expSysProcess.getPositions(), resSysProcess.getPositions());
        assertEquals(expSysProcess.getSMLLocation().getPoint().getPos(), resSysProcess.getSMLLocation().getPoint().getPos());
        assertEquals(expSysProcess.getSMLLocation().getPoint(), resSysProcess.getSMLLocation().getPoint());
        assertEquals(expSysProcess.getSMLLocation(), resSysProcess.getSMLLocation());
        assertEquals(expSysProcess.getSpatialReferenceFrame().getEngineeringCRS().getSrsName(), resSysProcess.getSpatialReferenceFrame().getEngineeringCRS().getSrsName());
        assertEquals(expSysProcess.getSpatialReferenceFrame().getEngineeringCRS().getUsesCS(), resSysProcess.getSpatialReferenceFrame().getEngineeringCRS().getUsesCS());
        assertEquals(expSysProcess.getSpatialReferenceFrame().getEngineeringCRS().getUsesEngineeringDatum(), resSysProcess.getSpatialReferenceFrame().getEngineeringCRS().getUsesEngineeringDatum());
        assertEquals(expSysProcess.getSpatialReferenceFrame().getEngineeringCRS(), resSysProcess.getSpatialReferenceFrame().getEngineeringCRS());
        assertEquals(expSysProcess.getSpatialReferenceFrame(), resSysProcess.getSpatialReferenceFrame());
        assertEquals(expSysProcess.getSrsName(), resSysProcess.getSrsName());
        assertEquals(expSysProcess.getTemporalReferenceFrame(), resSysProcess.getTemporalReferenceFrame());
        assertEquals(expSysProcess.getTimePosition(), resSysProcess.getTimePosition());
        assertEquals(expSysProcess.getValidTime(), resSysProcess.getValidTime());




        assertEquals(expResult.getMember().iterator().next().getArcrole(), result.getMember().iterator().next().getArcrole());
        assertEquals(expResult.getMember().iterator().next(), result.getMember().iterator().next());
        assertEquals(expResult.getMember(), result.getMember());


        assertEquals(expResult, result);

        absExpResult = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/system2.xml"));

        writer.storeMetadata(absExpResult);

        absExpResult = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/sml/system2.xml"));

        absResult = (AbstractSensorML) reader.getMetadata("16:SMLC", AbstractMetadataReader.SENSORML, null);

        assertTrue(absResult != null);
        assertTrue(absResult instanceof SensorML);
        assertTrue(absExpResult instanceof SensorML);
        result = (SensorML) absResult;
        expResult = (SensorML) absExpResult;


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
        expSysProcess = (SystemType) expResult.getMember().iterator().next().getProcess().getValue();
        assertTrue(result.getMember().iterator().next().getProcess().getValue() instanceof SystemType);
        resSysProcess = (SystemType) result.getMember().iterator().next().getProcess().getValue();

        assertEquals(expSysProcess.getOutputs().getOutputList().getOutput().size(), resSysProcess.getOutputs().getOutputList().getOutput().size());
        expIt = expSysProcess.getOutputs().getOutputList().getOutput().iterator();
        resIt = resSysProcess.getOutputs().getOutputList().getOutput().iterator();
        for (int i = 0; i < expSysProcess.getOutputs().getOutputList().getOutput().size(); i++) {
            IoComponentPropertyType resio = resIt.next();
            IoComponentPropertyType expio = expIt.next();
            DataRecordType resIoRec       = (DataRecordType) resio.getAbstractDataRecord().getValue();
            DataRecordType expIoRec       = (DataRecordType) expio.getAbstractDataRecord().getValue();
            assertEquals(expIoRec.getId(), resIoRec.getId());
            assertEquals(expIoRec.getField().size(), resIoRec.getField().size());
            assertEquals(expIoRec.getField().get(0), resIoRec.getField().get(0));
            assertEquals(expIoRec.getField().get(1), resIoRec.getField().get(1));
            assertEquals(expIoRec.getField().get(2), resIoRec.getField().get(2));
            assertEquals(expIoRec.getField(), resIoRec.getField());
            assertEquals(expIoRec, resIoRec);
            assertEquals(expio, resio);
        }

        assertEquals(expSysProcess.getOutputs().getOutputList().getOutput(), resSysProcess.getOutputs().getOutputList().getOutput());
        assertEquals(expSysProcess.getOutputs().getOutputList(), resSysProcess.getOutputs().getOutputList());
        assertEquals(expSysProcess.getOutputs(), resSysProcess.getOutputs());

        assertEquals(expSysProcess.getBoundedBy(), resSysProcess.getBoundedBy());

        if (expSysProcess.getCapabilities().size() > 0 && resSysProcess.getCapabilities().size() > 0) {
            assertTrue(resSysProcess.getCapabilities().get(0).getAbstractDataRecord().getValue() instanceof DataRecordType);
            DataRecordType expRecord = (DataRecordType) expSysProcess.getCapabilities().get(0).getAbstractDataRecord().getValue();
            DataRecordType resRecord = (DataRecordType) resSysProcess.getCapabilities().get(0).getAbstractDataRecord().getValue();
            assertEquals(expRecord.getField(), resRecord.getField());
            assertEquals(expSysProcess.getCapabilities().get(0).getAbstractDataRecord().getValue(), resSysProcess.getCapabilities().get(0).getAbstractDataRecord().getValue());
            assertEquals(expSysProcess.getCapabilities().get(0), resSysProcess.getCapabilities().get(0));
        }
        assertEquals(expSysProcess.getCapabilities(), resSysProcess.getCapabilities());

        assertEquals(expSysProcess.getCharacteristics().iterator().next(), resSysProcess.getCharacteristics().iterator().next());
        assertEquals(expSysProcess.getCharacteristics(), resSysProcess.getCharacteristics());

        assertEquals(expSysProcess.getClassification().size(), resSysProcess.getClassification().size());
        assertEquals(resSysProcess.getClassification().size(), 1);
        assertEquals(expSysProcess.getClassification().get(0).getClassifierList().getClassifier().size(), resSysProcess.getClassification().get(0).getClassifierList().getClassifier().size());
        for (int i = 0; i < expSysProcess.getClassification().get(0).getClassifierList().getClassifier().size(); i++) {
            assertEquals(expSysProcess.getClassification().get(0).getClassifierList().getClassifier().get(i), resSysProcess.getClassification().get(0).getClassifierList().getClassifier().get(i));
        }
        assertEquals(expSysProcess.getClassification().get(0).getClassifierList().getClassifier(), resSysProcess.getClassification().get(0).getClassifierList().getClassifier());
        assertEquals(expSysProcess.getClassification().get(0).getClassifierList(), resSysProcess.getClassification().get(0).getClassifierList());
        assertEquals(expSysProcess.getClassification().get(0), resSysProcess.getClassification().get(0));
        assertEquals(expSysProcess.getClassification(), resSysProcess.getClassification());
        assertEquals(expSysProcess.getConnections(), resSysProcess.getConnections());

        assertEquals(expSysProcess.getContact().iterator().next().getResponsibleParty().getContactInfo().getAddress(), resSysProcess.getContact().iterator().next().getResponsibleParty().getContactInfo().getAddress());
        assertEquals(expSysProcess.getContact().iterator().next().getResponsibleParty().getContactInfo(), resSysProcess.getContact().iterator().next().getResponsibleParty().getContactInfo());
        assertEquals(expSysProcess.getContact().iterator().next().getResponsibleParty(), resSysProcess.getContact().iterator().next().getResponsibleParty());
        assertEquals(expSysProcess.getContact().iterator().next(), resSysProcess.getContact().iterator().next());
        assertEquals(expSysProcess.getContact(), resSysProcess.getContact());
        assertEquals(expSysProcess.getDescription(), resSysProcess.getDescription());
        assertEquals(expSysProcess.getDescriptionReference(), resSysProcess.getDescriptionReference());
        assertEquals(expSysProcess.getDocumentation().size(), resSysProcess.getDocumentation().size());
        assertEquals(expSysProcess.getDocumentation().get(0).getDocument().getOnlineResource(), resSysProcess.getDocumentation().get(0).getDocument().getOnlineResource());
        assertEquals(expSysProcess.getDocumentation().get(0).getDocument().getDescription(), resSysProcess.getDocumentation().get(0).getDocument().getDescription());
        assertEquals(expSysProcess.getDocumentation().get(0).getDocument(), resSysProcess.getDocumentation().get(0).getDocument());
        assertEquals(expSysProcess.getDocumentation().get(0).getDocumentList(), resSysProcess.getDocumentation().get(0).getDocumentList());
        assertEquals(expSysProcess.getDocumentation().get(0), resSysProcess.getDocumentation().get(0));
        assertEquals(expSysProcess.getDocumentation(), resSysProcess.getDocumentation());
        assertEquals(expSysProcess.getHistory().get(0).getEventList().getMember().get(0).getEvent().getContact(), resSysProcess.getHistory().get(0).getEventList().getMember().get(0).getEvent().getContact());
        assertEquals(expSysProcess.getHistory().get(0).getEventList().getMember().get(0).getEvent().getDocumentation(), resSysProcess.getHistory().get(0).getEventList().getMember().get(0).getEvent().getDocumentation());
        assertEquals(expSysProcess.getHistory().get(0).getEventList().getMember().get(0).getEvent().getIdentification(), resSysProcess.getHistory().get(0).getEventList().getMember().get(0).getEvent().getIdentification());
        assertEquals(expSysProcess.getHistory().get(0).getEventList().getMember().get(0).getEvent().getKeywords(), resSysProcess.getHistory().get(0).getEventList().getMember().get(0).getEvent().getKeywords());
        assertEquals(expSysProcess.getHistory().get(0).getEventList().getMember().get(0).getEvent().getProperty(), resSysProcess.getHistory().get(0).getEventList().getMember().get(0).getEvent().getProperty());
        assertEquals(expSysProcess.getHistory().get(0).getEventList().getMember().get(0).getEvent().getClassification(), resSysProcess.getHistory().get(0).getEventList().getMember().get(0).getEvent().getClassification());
        assertEquals(expSysProcess.getHistory().get(0).getEventList().getMember().get(0).getEvent(), resSysProcess.getHistory().get(0).getEventList().getMember().get(0).getEvent());
        assertEquals(expSysProcess.getHistory().get(0).getEventList(), resSysProcess.getHistory().get(0).getEventList());
        assertEquals(expSysProcess.getHistory(), resSysProcess.getHistory());
        assertEquals(expSysProcess.getId(), resSysProcess.getId());
        assertEquals(expSysProcess.getIdentification(), resSysProcess.getIdentification());
        assertEquals(expSysProcess.getInputs(), resSysProcess.getInputs());
        assertEquals(expSysProcess.getInterfaces(), resSysProcess.getInterfaces());
        assertEquals(expSysProcess.getKeywords(), resSysProcess.getKeywords());
        assertEquals(expSysProcess.getLegalConstraint().get(0).getRights().getDocumentation().getDocument().getDescription(), resSysProcess.getLegalConstraint().get(0).getRights().getDocumentation().getDocument().getDescription());
        assertEquals(expSysProcess.getLegalConstraint().get(0).getRights().getDocumentation().getDocument(), resSysProcess.getLegalConstraint().get(0).getRights().getDocumentation().getDocument());
        assertEquals(expSysProcess.getLegalConstraint().get(0).getRights().getDocumentation(), resSysProcess.getLegalConstraint().get(0).getRights().getDocumentation());
        assertEquals(expSysProcess.getLegalConstraint().get(0).getRights(), resSysProcess.getLegalConstraint().get(0).getRights());
        assertEquals(expSysProcess.getLegalConstraint().get(0), resSysProcess.getLegalConstraint().get(0));
        assertEquals(expSysProcess.getLegalConstraint(), resSysProcess.getLegalConstraint());
        assertEquals(expSysProcess.getLocation(), resSysProcess.getLocation());
        assertEquals(expSysProcess.getName(), resSysProcess.getName());
        assertEquals(expSysProcess.getComponents(), resSysProcess.getComponents());

        assertEquals(expSysProcess.getParameters(), resSysProcess.getParameters());
        assertEquals(expSysProcess.getPosition(), resSysProcess.getPosition());
        assertEquals(expSysProcess.getPositions().getPositionList().getPosition().get(0).getPosition().getLocation().getVector().getDefinition(), resSysProcess.getPositions().getPositionList().getPosition().get(0).getPosition().getLocation().getVector().getDefinition());
        assertEquals(expSysProcess.getPositions().getPositionList().getPosition().get(0).getPosition().getLocation().getVector().getCoordinate(), resSysProcess.getPositions().getPositionList().getPosition().get(0).getPosition().getLocation().getVector().getCoordinate());
        assertEquals(expSysProcess.getPositions().getPositionList().getPosition().get(0).getPosition().getLocation().getVector(), resSysProcess.getPositions().getPositionList().getPosition().get(0).getPosition().getLocation().getVector());
        assertEquals(expSysProcess.getPositions().getPositionList().getPosition().get(0).getPosition().getLocation(), resSysProcess.getPositions().getPositionList().getPosition().get(0).getPosition().getLocation());
        assertEquals(expSysProcess.getPositions().getPositionList().getPosition().get(0).getPosition(), resSysProcess.getPositions().getPositionList().getPosition().get(0).getPosition());
        assertEquals(expSysProcess.getPositions().getPositionList().getPosition().get(0).getVector(), resSysProcess.getPositions().getPositionList().getPosition().get(0).getVector());
        assertEquals(expSysProcess.getPositions().getPositionList().getPosition().get(0).getName(), resSysProcess.getPositions().getPositionList().getPosition().get(0).getName());
        assertEquals(expSysProcess.getPositions().getPositionList().getPosition().get(0), resSysProcess.getPositions().getPositionList().getPosition().get(0));
        assertEquals(expSysProcess.getPositions().getPositionList().getPosition(), resSysProcess.getPositions().getPositionList().getPosition());
        assertEquals(expSysProcess.getPositions().getPositionList(), resSysProcess.getPositions().getPositionList());
        assertEquals(expSysProcess.getPositions(), resSysProcess.getPositions());
        assertEquals(expSysProcess.getSMLLocation().getPoint().getPos(), resSysProcess.getSMLLocation().getPoint().getPos());
        assertEquals(expSysProcess.getSMLLocation().getPoint(), resSysProcess.getSMLLocation().getPoint());
        assertEquals(expSysProcess.getSMLLocation(), resSysProcess.getSMLLocation());
        assertEquals(expSysProcess.getSpatialReferenceFrame().getEngineeringCRS().getSrsName(), resSysProcess.getSpatialReferenceFrame().getEngineeringCRS().getSrsName());
        assertEquals(expSysProcess.getSpatialReferenceFrame().getEngineeringCRS().getUsesCS(), resSysProcess.getSpatialReferenceFrame().getEngineeringCRS().getUsesCS());
        assertEquals(expSysProcess.getSpatialReferenceFrame().getEngineeringCRS().getUsesEngineeringDatum(), resSysProcess.getSpatialReferenceFrame().getEngineeringCRS().getUsesEngineeringDatum());
        assertEquals(expSysProcess.getSpatialReferenceFrame().getEngineeringCRS(), resSysProcess.getSpatialReferenceFrame().getEngineeringCRS());
        assertEquals(expSysProcess.getSpatialReferenceFrame(), resSysProcess.getSpatialReferenceFrame());
        assertEquals(expSysProcess.getSrsName(), resSysProcess.getSrsName());
        assertEquals(expSysProcess.getTemporalReferenceFrame(), resSysProcess.getTemporalReferenceFrame());
        assertEquals(expSysProcess.getTimePosition(), resSysProcess.getTimePosition());
        assertEquals(expSysProcess.getValidTime(), resSysProcess.getValidTime());




        assertEquals(expResult.getMember().iterator().next().getArcrole(), result.getMember().iterator().next().getArcrole());
        assertEquals(expResult.getMember().iterator().next(), result.getMember().iterator().next());
        assertEquals(expResult.getMember(), result.getMember());


        assertEquals(expResult, result);

        pool.release(unmarshaller);
    }

}
