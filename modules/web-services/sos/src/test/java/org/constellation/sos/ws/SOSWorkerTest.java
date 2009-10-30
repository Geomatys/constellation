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

package org.constellation.sos.ws;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.constellation.util.Util;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.MimeType;

import org.geotoolkit.gml.xml.v311.EnvelopeEntry;
import org.geotoolkit.ows.xml.v110.AcceptFormatsType;
import org.geotoolkit.ows.xml.v110.AcceptVersionsType;
import org.geotoolkit.ows.xml.v110.SectionsType;
import org.geotoolkit.sos.xml.v100.Capabilities;
import org.geotoolkit.sos.xml.v100.DescribeSensor;
import org.geotoolkit.sos.xml.v100.GetCapabilities;
import org.geotoolkit.gml.xml.v311.TimeIndeterminateValueType;
import org.geotoolkit.gml.xml.v311.TimeInstantType;
import org.geotoolkit.gml.xml.v311.TimePeriodType;
import org.geotoolkit.gml.xml.v311.TimePositionType;
import org.geotoolkit.observation.xml.v100.MeasurementEntry;
import org.geotoolkit.observation.xml.v100.ObservationCollectionEntry;
import org.geotoolkit.observation.xml.v100.ObservationEntry;
import org.geotoolkit.ogc.xml.v110.BinaryTemporalOpType;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.geotoolkit.sml.xml.v100.ComponentPropertyType;
import org.geotoolkit.sml.xml.v100.ComponentType;
import org.geotoolkit.sml.xml.v100.SensorML;
import org.geotoolkit.sml.xml.v100.SystemType;
import org.geotoolkit.sos.xml.v100.EventTime;
import org.geotoolkit.sos.xml.v100.GetObservation;
import org.geotoolkit.sos.xml.v100.GetResult;
import org.geotoolkit.sos.xml.v100.GetResultResponse;
import org.geotoolkit.sos.xml.v100.InsertObservation;
import org.geotoolkit.sos.xml.v100.ObservationTemplate;
import org.geotoolkit.sos.xml.v100.RegisterSensor;
import org.geotoolkit.sos.xml.v100.RegisterSensorResponse;
import org.geotoolkit.sos.xml.v100.ResponseModeType;
import org.geotoolkit.swe.xml.v101.AnyScalarPropertyType;
import org.geotoolkit.swe.xml.v101.DataArrayEntry;
import org.geotoolkit.swe.xml.v101.DataArrayPropertyType;
import org.geotoolkit.swe.xml.v101.SimpleDataRecordEntry;
import org.geotoolkit.swe.xml.v101.TimeType;
import org.geotoolkit.xml.MarshallerPool;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

// JUnit dependencies
import org.junit.Ignore;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@Ignore
public class SOSWorkerTest {

    protected SOSworker worker;

    protected MarshallerPool marshallerPool;

    protected static final String URL = "http://pulsar.geomatys.fr/SOServer/SOService";

    /**
     * Tests the getcapabilities method
     *
     * @throws java.lang.Exception
     */
    public void getCapabilitiesTest() throws Exception {

        /*
         *  TEST 1 : minimal getCapabilities
         */
        GetCapabilities request = new GetCapabilities();
        Capabilities result = worker.getCapabilities(request);

        assertTrue(result != null);
        assertTrue(result.getVersion().equals("1.0.0"));
        assertTrue(result.getFilterCapabilities() != null);
        assertTrue(result.getOperationsMetadata() != null);
        assertTrue(result.getServiceIdentification() != null);
        assertTrue(result.getServiceProvider() != null);

        assertTrue(result.getContents() != null);
        assertTrue(result.getContents().getObservationOfferingList() != null);
        assertTrue(result.getContents().getObservationOfferingList().getObservationOffering() != null);
        assertTrue(result.getContents().getObservationOfferingList().getObservationOffering().size() == 1);

        /*
         *  TEST 2 : full get capabilities
         */
        AcceptVersionsType acceptVersions = new AcceptVersionsType("1.0.0");
        SectionsType sections             = new SectionsType("All");
        AcceptFormatsType acceptFormats   = new AcceptFormatsType(MimeType.APP_XML);
        request = new GetCapabilities(acceptVersions, sections, acceptFormats, "", "SOS");

        result = worker.getCapabilities(request);

        assertTrue(result.getVersion().equals("1.0.0"));
        assertTrue(result.getFilterCapabilities() != null);
        assertTrue(result.getOperationsMetadata() != null);
        assertTrue(result.getServiceIdentification() != null);
        assertTrue(result.getServiceProvider() != null);
        assertTrue(result.getContents() != null);
        assertTrue(result.getContents().getObservationOfferingList() != null);
        assertTrue(result.getContents().getObservationOfferingList().getObservationOffering() != null);
        assertTrue(result.getContents().getObservationOfferingList().getObservationOffering().size() == 1);
        assertTrue(result != null);

        /*
         *  TEST 3 : get capabilities section Operation metadata
         */
        acceptVersions = new AcceptVersionsType("1.0.0");
        sections       = new SectionsType("OperationsMetadata");
        acceptFormats  = new AcceptFormatsType(MimeType.APP_XML);
        request = new GetCapabilities(acceptVersions, sections, acceptFormats, "", "SOS");

        result = worker.getCapabilities(request);

        assertTrue(result.getVersion().equals("1.0.0"));
        assertTrue(result.getFilterCapabilities() == null);
        assertTrue(result.getOperationsMetadata() != null);
        assertTrue(result.getServiceIdentification() == null);
        assertTrue(result.getServiceProvider() == null);
        assertTrue(result.getContents() == null);
        assertTrue(result != null);

        /*
         *  TEST 4 : get capabilities section Service provider
         */
        acceptVersions = new AcceptVersionsType("1.0.0");
        sections       = new SectionsType("ServiceProvider");
        acceptFormats  = new AcceptFormatsType(MimeType.APP_XML);
        request = new GetCapabilities(acceptVersions, sections, acceptFormats, "", "SOS");

        result = worker.getCapabilities(request);

        assertTrue(result.getVersion().equals("1.0.0"));
        assertTrue(result.getFilterCapabilities() == null);
        assertTrue(result.getOperationsMetadata() == null);
        assertTrue(result.getServiceIdentification() == null);
        assertTrue(result.getServiceProvider() != null);
        assertTrue(result.getContents() == null);
        assertTrue(result != null);

        /*
         *  TEST 5 : get capabilities section Service Identification
         */
        acceptVersions = new AcceptVersionsType("1.0.0");
        sections       = new SectionsType("ServiceIdentification");
        acceptFormats  = new AcceptFormatsType(MimeType.APP_XML);
        request = new GetCapabilities(acceptVersions, sections, acceptFormats, "", "SOS");

        result = worker.getCapabilities(request);

        assertTrue(result.getVersion().equals("1.0.0"));
        assertTrue(result.getFilterCapabilities() == null);
        assertTrue(result.getOperationsMetadata() == null);
        assertTrue(result.getServiceIdentification() != null);
        assertTrue(result.getServiceProvider() == null);
        assertTrue(result.getContents() == null);
        assertTrue(result != null);

        /*
         *  TEST 6 : get capabilities section Contents
         */
        acceptVersions = new AcceptVersionsType("1.0.0");
        sections       = new SectionsType("Contents");
        acceptFormats  = new AcceptFormatsType(MimeType.APP_XML);
        request = new GetCapabilities(acceptVersions, sections, acceptFormats, "", "SOS");

        result = worker.getCapabilities(request);

        assertTrue(result.getVersion().equals("1.0.0"));
        assertTrue(result.getFilterCapabilities() == null);
        assertTrue(result.getOperationsMetadata() == null);
        assertTrue(result.getServiceIdentification() == null);
        assertTrue(result.getServiceProvider() == null);
        assertTrue(result.getContents() != null);
        assertTrue(result.getContents().getObservationOfferingList() != null);
        assertTrue(result.getContents().getObservationOfferingList().getObservationOffering() != null);
        assertTrue(result.getContents().getObservationOfferingList().getObservationOffering().size() == 1);
        assertTrue(result != null);

        /*
         *  TEST 7 : get capabilities with wrong version (waiting for an exception)
         */
        acceptVersions = new AcceptVersionsType("2.0.0");
        sections       = new SectionsType("All");
        acceptFormats  = new AcceptFormatsType(MimeType.TEXT_XML);
        request = new GetCapabilities(acceptVersions, sections, acceptFormats, "", "SOS");

        boolean exLaunched = false;
        try {
            worker.getCapabilities(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), VERSION_NEGOTIATION_FAILED);
            assertEquals(ex.getLocator(), "acceptVersion");
        }

        assertTrue(exLaunched);

         /*
         *  TEST 8 : : get capabilities with wrong formats (waiting for an exception)
         */
        request = new GetCapabilities("1.0.0", "ploup/xml");

        exLaunched = false;
        try {
            worker.getCapabilities(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "acceptFormats");
        }

        /*
         *  LAST TEST : get capabilities no skeleton capabilities
         */

        worker.setSkeletonCapabilities(null);
        request = new GetCapabilities();

        exLaunched = false;
        try {
            worker.getCapabilities(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), NO_APPLICABLE_CODE);
        }

        assertTrue(exLaunched);
    
    }

    
    
    /**
     * Tests the DescribeSensor method
     *
     * @throws java.lang.Exception
     */
    public void DescribeSensorTest() throws Exception {
        Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();

        /**
         * Test 1 bad outputFormat
         */
        boolean exLaunched = false;
        DescribeSensor request  = new DescribeSensor("urn:ogc:object:sensor:GEOM:1", "text/xml; subtype=\"SensorML/1.0.0\"");
        try {
            worker.describeSensor(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "outputFormat");
        }
        assertTrue(exLaunched);

        /**
         * Test 2 missing outputFormat
         */
        exLaunched = false;
        request  = new DescribeSensor("urn:ogc:object:sensor:GEOM:1", null);
        try {
            worker.describeSensor(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), MISSING_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "outputFormat");
        }
        assertTrue(exLaunched);

        /**
         * Test 3 missing sensorID
         */
        exLaunched = false;
        request  = new DescribeSensor(null, "text/xml;subtype=\"SensorML/1.0.0\"");
        try {
            worker.describeSensor(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), MISSING_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), Parameters.PROCEDURE);
        }
        assertTrue(exLaunched);

        /**
         * Test 4 system sensor
         */
        request  = new DescribeSensor("urn:ogc:object:sensor:GEOM:1", "text/xml;subtype=\"SensorML/1.0.0\"");
        AbstractSensorML absResult = (AbstractSensorML) worker.describeSensor(request);

        AbstractSensorML absExpResult = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/system.xml"));

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
        SystemType expProcess = (SystemType) expResult.getMember().iterator().next().getProcess().getValue();
        assertTrue(result.getMember().iterator().next().getProcess().getValue() instanceof SystemType);
        SystemType resProcess = (SystemType) result.getMember().iterator().next().getProcess().getValue();


        assertEquals(expProcess.getBoundedBy(), resProcess.getBoundedBy());

        assertEquals(expProcess.getCapabilities(), resProcess.getCapabilities());

        assertEquals(expProcess.getClassification().size(), resProcess.getClassification().size());
        assertEquals(resProcess.getClassification().size(), 1);
        assertEquals(expProcess.getClassification().get(0).getClassifierList().getClassifier().size(), resProcess.getClassification().get(0).getClassifierList().getClassifier().size());
        for (int i = 0; i < 10; i++) {
            assertEquals(expProcess.getClassification().get(0).getClassifierList().getClassifier().get(i), resProcess.getClassification().get(0).getClassifierList().getClassifier().get(i));
        }
        assertEquals(expProcess.getClassification().get(0).getClassifierList().getClassifier(), resProcess.getClassification().get(0).getClassifierList().getClassifier());
        assertEquals(expProcess.getClassification().get(0).getClassifierList(), resProcess.getClassification().get(0).getClassifierList());
        assertEquals(expProcess.getClassification().get(0), resProcess.getClassification().get(0));
        assertEquals(expProcess.getClassification(), resProcess.getClassification());
        assertEquals(expProcess.getConnections(), resProcess.getConnections());

        /*assertEquals(expProcess.getContact().iterator().next().getResponsibleParty().getContactInfo().getAddress().getAdministrativeArea(), resProcess.getContact().iterator().next().getResponsibleParty().getContactInfo().getAddress().getAdministrativeArea());
        assertEquals(expProcess.getContact().iterator().next().getResponsibleParty().getContactInfo().getAddress().getCity(), resProcess.getContact().iterator().next().getResponsibleParty().getContactInfo().getAddress().getCity());
        assertEquals(expProcess.getContact().iterator().next().getResponsibleParty().getContactInfo().getAddress().getCountry(), resProcess.getContact().iterator().next().getResponsibleParty().getContactInfo().getAddress().getCountry());
        assertEquals(expProcess.getContact().iterator().next().getResponsibleParty().getContactInfo().getAddress().getDeliveryPoint(), resProcess.getContact().iterator().next().getResponsibleParty().getContactInfo().getAddress().getDeliveryPoint());
        assertEquals(expProcess.getContact().iterator().next().getResponsibleParty().getContactInfo().getAddress().getElectronicMailAddress(), resProcess.getContact().iterator().next().getResponsibleParty().getContactInfo().getAddress().getElectronicMailAddress());
        assertEquals(expProcess.getContact().iterator().next().getResponsibleParty().getContactInfo().getAddress().getPostalCode(), resProcess.getContact().iterator().next().getResponsibleParty().getContactInfo().getAddress().getPostalCode());
        assertEquals(expProcess.getContact().iterator().next().getResponsibleParty().getContactInfo().getAddress(), resProcess.getContact().iterator().next().getResponsibleParty().getContactInfo().getAddress());
        assertEquals(expProcess.getContact().iterator().next().getResponsibleParty().getContactInfo(), resProcess.getContact().iterator().next().getResponsibleParty().getContactInfo());*/
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
        assertEquals(expProcess.getComponents(), resProcess.getComponents());
        assertEquals(expProcess.getOutputs(), resProcess.getOutputs());
        assertEquals(expProcess.getParameters(), resProcess.getParameters());
        /*assertEquals(expProcess.getPosition().getVector(), resProcess.getPosition().getVector());
        assertEquals(expProcess.getPosition().getPosition().getLocation().getVector().getCoordinate().get(0), resProcess.getPosition().getPosition().getLocation().getVector().getCoordinate().get(0));
        assertEquals(expProcess.getPosition().getPosition().getLocation().getVector().getCoordinate(), resProcess.getPosition().getPosition().getLocation().getVector().getCoordinate());
        assertEquals(expProcess.getPosition().getPosition().getLocation().getVector(), resProcess.getPosition().getPosition().getLocation().getVector());
        assertEquals(expProcess.getPosition().getPosition().getLocation(), resProcess.getPosition().getPosition().getLocation());
        assertEquals(expProcess.getPosition().getPosition().getOrientation(), resProcess.getPosition().getPosition().getOrientation());
        assertEquals(expProcess.getPosition().getPosition(), resProcess.getPosition().getPosition());*/
        assertEquals(expProcess.getPosition(), resProcess.getPosition());
        assertEquals(expProcess.getPositions().getPositionList().getPosition().get(0).getPosition(), resProcess.getPositions().getPositionList().getPosition().get(0).getPosition());
        assertEquals(expProcess.getPositions().getPositionList().getPosition().get(0).getVector(), resProcess.getPositions().getPositionList().getPosition().get(0).getVector());
        assertEquals(expProcess.getPositions().getPositionList().getPosition().get(0).getName(), resProcess.getPositions().getPositionList().getPosition().get(0).getName());
        assertEquals(expProcess.getPositions().getPositionList().getPosition().get(0), resProcess.getPositions().getPositionList().getPosition().get(0));
        assertEquals(expProcess.getPositions().getPositionList().getPosition(), resProcess.getPositions().getPositionList().getPosition());
        assertEquals(expProcess.getPositions().getPositionList(), resProcess.getPositions().getPositionList());
        assertEquals(expProcess.getPositions(), resProcess.getPositions());
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

        /**
         * Test 5 component sensor
         */
        request  = new DescribeSensor("urn:ogc:object:sensor:GEOM:2", "text/xml;subtype=\"SensorML/1.0.0\"");
        absResult = (AbstractSensorML) worker.describeSensor(request);

        absExpResult = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/component.xml"));

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
        ComponentType expCompo = (ComponentType) expResult.getMember().iterator().next().getProcess().getValue();
        assertTrue(result.getMember().iterator().next().getProcess().getValue() instanceof ComponentType);
        ComponentType resCompo = (ComponentType) result.getMember().iterator().next().getProcess().getValue();


        assertEquals(expCompo.getBoundedBy(), resCompo.getBoundedBy());

        assertEquals(expCompo.getCapabilities(), resCompo.getCapabilities());

        assertEquals(expCompo.getClassification().size(), resCompo.getClassification().size());
        assertEquals(resCompo.getClassification().size(), 1);
        assertEquals(expCompo.getClassification().get(0).getClassifierList().getClassifier().size(), resCompo.getClassification().get(0).getClassifierList().getClassifier().size());
        for (int i = 0; i < 2; i++) {
            assertEquals(expCompo.getClassification().get(0).getClassifierList().getClassifier().get(i), resCompo.getClassification().get(0).getClassifierList().getClassifier().get(i));
        }
        assertEquals(expCompo.getClassification().get(0).getClassifierList().getClassifier(), resCompo.getClassification().get(0).getClassifierList().getClassifier());
        assertEquals(expCompo.getClassification().get(0).getClassifierList(), resCompo.getClassification().get(0).getClassifierList());
        assertEquals(expCompo.getClassification().get(0), resCompo.getClassification().get(0));
        assertEquals(expCompo.getClassification(), resCompo.getClassification());

        /*assertEquals(expCompo.getContact().iterator().next().getResponsibleParty().getContactInfo().getAddress().getAdministrativeArea(), resCompo.getContact().iterator().next().getResponsibleParty().getContactInfo().getAddress().getAdministrativeArea());
        assertEquals(expCompo.getContact().iterator().next().getResponsibleParty().getContactInfo().getAddress().getCity(), resCompo.getContact().iterator().next().getResponsibleParty().getContactInfo().getAddress().getCity());
        assertEquals(expCompo.getContact().iterator().next().getResponsibleParty().getContactInfo().getAddress().getCountry(), resCompo.getContact().iterator().next().getResponsibleParty().getContactInfo().getAddress().getCountry());
        assertEquals(expCompo.getContact().iterator().next().getResponsibleParty().getContactInfo().getAddress().getDeliveryPoint(), resCompo.getContact().iterator().next().getResponsibleParty().getContactInfo().getAddress().getDeliveryPoint());
        assertEquals(expCompo.getContact().iterator().next().getResponsibleParty().getContactInfo().getAddress().getElectronicMailAddress(), resCompo.getContact().iterator().next().getResponsibleParty().getContactInfo().getAddress().getElectronicMailAddress());
        assertEquals(expCompo.getContact().iterator().next().getResponsibleParty().getContactInfo().getAddress().getPostalCode(), resCompo.getContact().iterator().next().getResponsibleParty().getContactInfo().getAddress().getPostalCode());
        assertEquals(expCompo.getContact().iterator().next().getResponsibleParty().getContactInfo().getAddress(), resCompo.getContact().iterator().next().getResponsibleParty().getContactInfo().getAddress());
        assertEquals(expCompo.getContact().iterator().next().getResponsibleParty().getContactInfo(), resCompo.getContact().iterator().next().getResponsibleParty().getContactInfo());*/
        assertEquals(expCompo.getContact().iterator().next().getResponsibleParty(), resCompo.getContact().iterator().next().getResponsibleParty());
        assertEquals(expCompo.getContact().iterator().next(), resCompo.getContact().iterator().next());
        assertEquals(expCompo.getContact(), resCompo.getContact());
        assertEquals(expCompo.getDescription(), resCompo.getDescription());
        assertEquals(expCompo.getDescriptionReference(), resCompo.getDescriptionReference());
        assertEquals(expCompo.getDocumentation(), resCompo.getDocumentation());
        assertEquals(expCompo.getHistory(), resCompo.getHistory());
        assertEquals(expCompo.getId(), resCompo.getId());
        assertEquals(expCompo.getIdentification(), resCompo.getIdentification());
        assertEquals(expCompo.getInputs(), resCompo.getInputs());
        assertEquals(expCompo.getInterfaces(), resCompo.getInterfaces());
        assertEquals(expCompo.getKeywords(), resCompo.getKeywords());
        assertEquals(expCompo.getLegalConstraint(), resCompo.getLegalConstraint());
        assertEquals(expCompo.getLocation(), resCompo.getLocation());
        assertEquals(expCompo.getName(), resCompo.getName());
        assertEquals(expCompo.getOutputs(), resCompo.getOutputs());
        assertEquals(expCompo.getParameters(), resCompo.getParameters());
        /*assertEquals(expCompo.getPosition().getVector(), resCompo.getPosition().getVector());
        assertEquals(expCompo.getPosition().getPosition().getLocation().getVector().getCoordinate().get(0), resCompo.getPosition().getPosition().getLocation().getVector().getCoordinate().get(0));
        assertEquals(expCompo.getPosition().getPosition().getLocation().getVector().getCoordinate(), resCompo.getPosition().getPosition().getLocation().getVector().getCoordinate());
        assertEquals(expCompo.getPosition().getPosition().getLocation().getVector(), resCompo.getPosition().getPosition().getLocation().getVector());
        assertEquals(expCompo.getPosition().getPosition().getLocation(), resCompo.getPosition().getPosition().getLocation());
        assertEquals(expCompo.getPosition().getPosition().getOrientation(), resCompo.getPosition().getPosition().getOrientation());
        assertEquals(expCompo.getPosition().getPosition(), resCompo.getPosition().getPosition());*/
        assertEquals(expCompo.getPosition(), resCompo.getPosition());
        assertEquals(expCompo.getSMLLocation(), resCompo.getSMLLocation());
        assertEquals(expCompo.getSpatialReferenceFrame(), resCompo.getSpatialReferenceFrame());
        assertEquals(expCompo.getSrsName(), resCompo.getSrsName());
        assertEquals(expCompo.getTemporalReferenceFrame(), resCompo.getTemporalReferenceFrame());
        assertEquals(expCompo.getTimePosition(), resCompo.getTimePosition());
        assertEquals(expCompo.getValidTime(), resCompo.getValidTime());




        assertEquals(expResult.getMember().iterator().next().getArcrole(), result.getMember().iterator().next().getArcrole());
        assertEquals(expResult.getMember().iterator().next(), result.getMember().iterator().next());
        assertEquals(expResult.getMember(), result.getMember());
        assertEquals(expResult, result);

        marshallerPool.release(unmarshaller);
    }

    /**
     * Tests the GetObservation method
     *
     * @throws java.lang.Exception
     */
    public void GetObservationTest() throws Exception {
        Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();

         /**
         *  Test 1: getObservation with bad response format
         */
        GetObservation request  = new GetObservation("1.0.0",
                                                     "offering-allSensor",
                                                     null,
                                                     Arrays.asList("urn:ogc:object:sensor:GEOM:4"),
                                                     null,
                                                     null,
                                                     null,
                                                     "text/xml;subtype=\"om/2.0.0\"",
                                                     Parameters.OBSERVATION_QNAME,
                                                     ResponseModeType.INLINE,
                                                     null);
        boolean exLaunched = false;
        try {
            worker.getObservation(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), Parameters.RESPONSE_FORMAT);
        }
        assertTrue(exLaunched);

        /**
         *  Test 2: getObservation with bad response format
         */
        request  = new GetObservation("1.0.0",
                                      "offering-allSensor",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:4"),
                                      null,
                                      null,
                                      null,
                                      null,
                                      Parameters.OBSERVATION_QNAME,
                                      ResponseModeType.INLINE,
                                      null);
        exLaunched = false;
        try {
            worker.getObservation(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), MISSING_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), Parameters.RESPONSE_FORMAT);
        }
        assertTrue(exLaunched);


        /**
         *  Test 3: getObservation with procedure urn:ogc:object:sensor:GEOM:4 and no resultModel
         */
        request  = new GetObservation("1.0.0",
                                      "offering-allSensor",
                                      null,           
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:4"),
                                      null,
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      null,
                                      ResponseModeType.INLINE,
                                      null);
        ObservationCollectionEntry result = (ObservationCollectionEntry) worker.getObservation(request);

        JAXBElement obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/observation3.xml"));

        ObservationEntry expResult = (ObservationEntry)obj.getValue();

        assertEquals(result.getMember().size(), 1);

        ObservationEntry obsResult = (ObservationEntry) result.getMember().iterator().next();


        assertTrue(obsResult != null);
        assertEquals(expResult.getName(), obsResult.getName());
        assertEquals(expResult.getFeatureOfInterest(), obsResult.getFeatureOfInterest());
        assertEquals(expResult.getObservedProperty(), obsResult.getObservedProperty());
        assertEquals(expResult.getProcedure(), obsResult.getProcedure());
        assertTrue(obsResult.getResult() instanceof DataArrayPropertyType);
        assertTrue(expResult.getResult() instanceof DataArrayPropertyType);

        DataArrayPropertyType expR = (DataArrayPropertyType) expResult.getResult();
        DataArrayPropertyType obsR = (DataArrayPropertyType) obsResult.getResult();

        assertTrue(obsR.getDataArray().getElementType() instanceof SimpleDataRecordEntry);
        SimpleDataRecordEntry expSdr = (SimpleDataRecordEntry) expR.getDataArray().getElementType();
        SimpleDataRecordEntry obsSdr = (SimpleDataRecordEntry) obsR.getDataArray().getElementType();
        obsSdr.setBlockId(null);

        Iterator<AnyScalarPropertyType> i1 = expSdr.getField().iterator();
        Iterator<AnyScalarPropertyType> i2 = obsSdr.getField().iterator();
        TimeType expT = (TimeType) i1.next().getComponent();
        TimeType obsT = (TimeType) i2.next().getComponent();

        assertEquals(expT.getUom(), obsT.getUom());
        assertEquals(expT, obsT);
        assertEquals(i1.next(), i2.next());
        //assertEquals(expSdr.getField(), obsSdr.getField());
        assertEquals(expSdr, obsSdr);
        assertEquals(expR.getDataArray().getElementType(),     obsR.getDataArray().getElementType());
        assertEquals(expR.getDataArray().getEncoding(),        obsR.getDataArray().getEncoding());
        assertEquals(expR.getDataArray().getValues(),          obsR.getDataArray().getValues());
        assertEquals(expR.getDataArray().getId(),              obsR.getDataArray().getId());
        assertEquals(expR.getDataArray().getElementCount(),    obsR.getDataArray().getElementCount());
        assertEquals(expR.getDataArray().getName(),            obsR.getDataArray().getName());
        assertEquals(expR.getDataArray().getPropertyElementType(), obsR.getDataArray().getPropertyElementType());
        assertEquals(expR.getDataArray(),                      obsR.getDataArray());

        assertEquals(expResult.getResult(), obsResult.getResult());
        assertEquals(expResult.getSamplingTime(), obsResult.getSamplingTime());
        assertEquals(expResult, obsResult);

        /**
         *  Test 4: getObservation with procedure urn:ogc:object:sensor:GEOM:4 avec responseMode null
         */
        request  = new GetObservation("1.0.0",
                                      "offering-allSensor",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:4"),
                                      null,
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      Parameters.OBSERVATION_QNAME,
                                      null,
                                      null);
        result = (ObservationCollectionEntry) worker.getObservation(request);

        obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/observation3.xml"));

        expResult = (ObservationEntry)obj.getValue();

        assertEquals(result.getMember().size(), 1);

        obsResult = (ObservationEntry) result.getMember().iterator().next();

        assertTrue(obsResult != null);
        assertEquals(expResult.getName(), obsResult.getName());
        assertEquals(expResult.getFeatureOfInterest(), obsResult.getFeatureOfInterest());
        assertEquals(expResult.getObservedProperty(), obsResult.getObservedProperty());
        assertEquals(expResult.getProcedure(), obsResult.getProcedure());
        assertEquals(expResult.getResult(), obsResult.getResult());
        assertEquals(expResult.getSamplingTime(), obsResult.getSamplingTime());
        assertEquals(expResult, obsResult);


        /**
         *  Test 5: getObservation with procedure urn:ogc:object:sensor:GEOM:4
         */
        request  = new GetObservation("1.0.0",
                                      "offering-allSensor",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:4"),
                                      null,
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      Parameters.OBSERVATION_QNAME,
                                      ResponseModeType.INLINE,
                                      null);
        result = (ObservationCollectionEntry) worker.getObservation(request);

        obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/observation3.xml"));

        expResult = (ObservationEntry)obj.getValue();

        assertEquals(result.getMember().size(), 1);

        obsResult = (ObservationEntry) result.getMember().iterator().next();


        assertTrue(obsResult != null);
        assertEquals(expResult.getName(), obsResult.getName());
        assertEquals(expResult.getFeatureOfInterest(), obsResult.getFeatureOfInterest());
        assertEquals(expResult.getObservedProperty(), obsResult.getObservedProperty());
        assertEquals(expResult.getProcedure(), obsResult.getProcedure());
        assertEquals(expResult.getResult(), obsResult.getResult());
        assertEquals(expResult.getSamplingTime(), obsResult.getSamplingTime());
        assertEquals(expResult, obsResult);

        /**
         *  Test 6: getObservation with procedure urn:ogc:object:sensor:GEOM:3
         */
        request  = new GetObservation("1.0.0",
                                      "offering-allSensor",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                      null,
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      Parameters.OBSERVATION_QNAME,
                                      ResponseModeType.INLINE,
                                      null);
        result = (ObservationCollectionEntry) worker.getObservation(request);

        assertEquals(result.getMember().size(), 1);
        obsResult =  (ObservationEntry) result.getMember().iterator().next();
        assertTrue(obsResult.getResult() instanceof DataArrayPropertyType);
        obsR      = (DataArrayPropertyType) obsResult.getResult();
        assertEquals(obsR.getDataArray().getElementCount().getCount().getValue(), 15);

        /**
         *  Test 7: getObservation with procedure urn:ogc:object:sensor:GEOM:3
         *          + Time filter TBefore
         */
        List<EventTime> times = new ArrayList<EventTime>();
        TimeInstantType instant = new TimeInstantType(new TimePositionType("2007-05-01T03:00:00.0"));
        BinaryTemporalOpType filter = new BinaryTemporalOpType(instant);
        EventTime before            = new EventTime(null, filter, null);
        times.add(before);
        request  = new GetObservation("1.0.0",
                                      "offering-allSensor",
                                      times,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                      null,
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      Parameters.OBSERVATION_QNAME,
                                      ResponseModeType.INLINE,
                                      null);
        result = (ObservationCollectionEntry) worker.getObservation(request);

        assertEquals(result.getMember().size(), 1);

        assertEquals(result.getMember().iterator().next().getName(), "urn:ogc:object:observation:GEOM:304");

        /**
         *  Test 8: getObservation with procedure urn:ogc:object:sensor:GEOM:3
         *          + Time filter TAFter
         */
        times = new ArrayList<EventTime>();
        EventTime after            = new EventTime(filter,null, null);
        times.add(after);
        request  = new GetObservation("1.0.0",
                                      "offering-allSensor",
                                      times,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                      null,
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      Parameters.OBSERVATION_QNAME,
                                      ResponseModeType.INLINE,
                                      null);
        result = (ObservationCollectionEntry) worker.getObservation(request);

        assertEquals(result.getMember().size(), 1);

        obsResult =  (ObservationEntry) result.getMember().iterator().next();
        assertTrue(obsResult.getResult() instanceof DataArrayPropertyType);
        obsR      = (DataArrayPropertyType) obsResult.getResult();
        assertEquals(obsR.getDataArray().getElementCount().getCount().getValue(), 15);

        /**
         *  Test 9: getObservation with procedure urn:ogc:object:sensor:GEOM:3
         *          + Time filter TDuring
         */
        times = new ArrayList<EventTime>();
        TimePeriodType period = new TimePeriodType(new TimePositionType("2007-05-01T03:00:00.0"), new TimePositionType("2007-05-01T08:00:00.0"));
        filter = new BinaryTemporalOpType(period);
        EventTime during = new EventTime(null, null, filter);
        times.add(during);
        request  = new GetObservation("1.0.0",
                                      "offering-allSensor",
                                      times,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                      null,
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      Parameters.OBSERVATION_QNAME,
                                      ResponseModeType.INLINE,
                                      null);
        result = (ObservationCollectionEntry) worker.getObservation(request);

        assertEquals(result.getMember().size(), 1);

        obsResult =  (ObservationEntry) result.getMember().iterator().next();
        assertTrue(obsResult.getResult() instanceof DataArrayPropertyType);
        obsR      = (DataArrayPropertyType) obsResult.getResult();
        assertEquals(obsR.getDataArray().getElementCount().getCount().getValue(), 10);

        /**
         *  Test 10: getObservation with procedure urn:ogc:object:sensor:GEOM:3
         *          + Time filter TEquals
         */
        times = new ArrayList<EventTime>();
        period = new TimePeriodType(new TimePositionType("2007-05-01T02:59:00.0"), new TimePositionType("2007-05-01T06:59:00.0"));
        filter = new BinaryTemporalOpType(period);
        EventTime equals = new EventTime(filter);
        times.add(equals);
        request  = new GetObservation("1.0.0",
                                      "offering-allSensor",
                                      times,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                      null,
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      Parameters.OBSERVATION_QNAME,
                                      ResponseModeType.INLINE,
                                      null);
        result = (ObservationCollectionEntry) worker.getObservation(request);

        assertEquals(result.getMember().size(), 1);

        obsResult =  (ObservationEntry) result.getMember().iterator().next();
        assertTrue(obsResult.getResult() instanceof DataArrayPropertyType);
        obsR      = (DataArrayPropertyType) obsResult.getResult();
        assertEquals(obsR.getDataArray().getElementCount().getCount().getValue(), 5);

        /**
         *  Test 11: getObservation with procedure urn:ogc:object:sensor:GEOM:3
         *          + Time filter TEquals
         *
         * with unsupported Response mode
         */
        times = new ArrayList<EventTime>();
        period = new TimePeriodType(new TimePositionType("2007-05-01T02:59:00.0"), new TimePositionType("2007-05-01T06:59:00.0"));
        filter = new BinaryTemporalOpType(period);
        equals = new EventTime(filter);
        times.add(equals);
        request  = new GetObservation("1.0.0",
                                      "offering-allSensor",
                                      times,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                      null,
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      Parameters.OBSERVATION_QNAME,
                                      ResponseModeType.OUT_OF_BAND,
                                      null);
        exLaunched = false;
        try {
            worker.getObservation(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), NO_APPLICABLE_CODE);
            assertEquals(ex.getLocator(), Parameters.RESPONSE_MODE);
        }
        assertTrue(exLaunched);

        /**
         *  Test 12: getObservation with procedure urn:ogc:object:sensor:GEOM:3
         *          + Time filter TEquals
         *
         * with unsupported Response mode
         */
        times = new ArrayList<EventTime>();
        period = new TimePeriodType(new TimePositionType("2007-05-01T02:59:00.0"), new TimePositionType("2007-05-01T06:59:00.0"));
        filter = new BinaryTemporalOpType(period);
        equals = new EventTime(filter);
        times.add(equals);
        request  = new GetObservation("1.0.0",
                                      "offering-allSensor",
                                      times,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                      null,
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      Parameters.OBSERVATION_QNAME,
                                      ResponseModeType.ATTACHED,
                                      null);
        exLaunched = false;
        try {
            worker.getObservation(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), OPERATION_NOT_SUPPORTED);
            assertEquals(ex.getLocator(), Parameters.RESPONSE_MODE);
        }
        assertTrue(exLaunched);

        /**
         *  Test 13: getObservation with procedure urn:ogc:object:sensor:GEOM:3
         *          + Time filter TEquals
         *
         * with no offering
         */
        times = new ArrayList<EventTime>();
        period = new TimePeriodType(new TimePositionType("2007-05-01T02:59:00.0"), new TimePositionType("2007-05-01T06:59:00.0"));
        filter = new BinaryTemporalOpType(period);
        equals = new EventTime(filter);
        times.add(equals);
        request  = new GetObservation("1.0.0",
                                      null,
                                      times,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                      null,
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      Parameters.OBSERVATION_QNAME,
                                      ResponseModeType.INLINE,
                                      null);
        exLaunched = false;
        try {
            worker.getObservation(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), MISSING_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), Parameters.OFFERING);
        }
        assertTrue(exLaunched);

        /**
         *  Test 14: getObservation with procedure urn:ogc:object:sensor:GEOM:3
         *          + Time filter TEquals
         *
         * with wrong offering
         */
        times = new ArrayList<EventTime>();
        period = new TimePeriodType(new TimePositionType("2007-05-01T02:59:00.0"), new TimePositionType("2007-05-01T06:59:00.0"));
        filter = new BinaryTemporalOpType(period);
        equals = new EventTime(filter);
        times.add(equals);
        request  = new GetObservation("1.0.0",
                                      "inexistant-offering",
                                      times,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                      null,
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      Parameters.OBSERVATION_QNAME,
                                      ResponseModeType.INLINE,
                                      null);
        exLaunched = false;
        try {
            worker.getObservation(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), Parameters.OFFERING);
        }
        assertTrue(exLaunched);

        /**
         *  Test 15: getObservation with procedure urn:ogc:object:sensor:GEOM:3
         *          + Time filter TEquals
         *
         * with wrong srsName
         */
        times = new ArrayList<EventTime>();
        period = new TimePeriodType(new TimePositionType("2007-05-01T02:59:00.0"), new TimePositionType("2007-05-01T06:59:00.0"));
        filter = new BinaryTemporalOpType(period);
        equals = new EventTime(filter);
        times.add(equals);
        request  = new GetObservation("1.0.0",
                                      "offering-allSensor",
                                      times,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                      null,
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      Parameters.OBSERVATION_QNAME,
                                      ResponseModeType.INLINE,
                                      "EPSG:3333");
        exLaunched = false;
        try {
            worker.getObservation(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "srsName");
        }
        assertTrue(exLaunched);


        /**
         *  Test 16: getObservation with procedure urn:ogc:object:sensor:GEOM:3
         *          + Time filter TEquals
         *
         * with wrong resultModel
         */
        times = new ArrayList<EventTime>();
        period = new TimePeriodType(new TimePositionType("2007-05-01T02:59:00.0"), new TimePositionType("2007-05-01T06:59:00.0"));
        filter = new BinaryTemporalOpType(period);
        equals = new EventTime(filter);
        times.add(equals);
        request  = new GetObservation("1.0.0",
                                      "offering-allSensor",
                                      times,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                      null,
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      new QName("some_namespace", "some_localPart"),
                                      ResponseModeType.INLINE,
                                      null);
        exLaunched = false;
        try {
            worker.getObservation(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "resultModel");
        }
        assertTrue(exLaunched);

        /**
         *  Test 17: getObservation with unexisting procedure
         *          + Time filter TEquals
         *
         */
        times = new ArrayList<EventTime>();
        period = new TimePeriodType(new TimePositionType("2007-05-01T02:59:00.0"), new TimePositionType("2007-05-01T06:59:00.0"));
        filter = new BinaryTemporalOpType(period);
        equals = new EventTime(filter);
        times.add(equals);
        request  = new GetObservation("1.0.0",
                                      "offering-allSensor",
                                      times,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:36"),
                                      null,
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      Parameters.OBSERVATION_QNAME,
                                      ResponseModeType.INLINE,
                                      null);
        exLaunched = false;
        try {
            worker.getObservation(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), Parameters.PROCEDURE);
        }
        assertTrue(exLaunched);

        /**
         *  Test 18: getObservation with procedure urn:ogc:object:sensor:GEOM:4
         *           with resultTemplate mode
         */
        request  = new GetObservation("1.0.0",
                                      "offering-allSensor",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:4"),
                                      null,
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      Parameters.OBSERVATION_QNAME,
                                      ResponseModeType.RESULT_TEMPLATE,
                                      null);
        result = (ObservationCollectionEntry) worker.getObservation(request);

        obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/observationTemplate-4.xml"));

        expResult = (ObservationEntry)obj.getValue();

        //for template the sampling time is 1970 to now
        period = new TimePeriodType(new TimePositionType("1900-01-01T00:00:00"));
        expResult.setSamplingTime(period);

        // and we empty the result object
        DataArrayPropertyType arrayP = (DataArrayPropertyType) expResult.getResult();
        DataArrayEntry array = arrayP.getDataArray();
        array.setElementCount(0);
        array.setValues("");

        expResult.setName("urn:ogc:object:observation:template:GEOM:4-0");

        assertEquals(result.getMember().size(), 1);

        obsResult = (ObservationEntry) result.getMember().iterator().next();

        assertTrue(obsResult != null);
        assertEquals(expResult.getName(), obsResult.getName());
        assertEquals(expResult.getFeatureOfInterest(), obsResult.getFeatureOfInterest());
        assertEquals(expResult.getObservedProperty(), obsResult.getObservedProperty());
        assertEquals(expResult.getProcedure(), obsResult.getProcedure());
        assertEquals(expResult.getResult(), obsResult.getResult());
        assertEquals(expResult.getSamplingTime(), obsResult.getSamplingTime());
        assertEquals(expResult, obsResult);

        /**
         *  Test 19: getObservation with procedure urn:ogc:object:sensor:GEOM:4
         *           with resultTemplate mode
         *           with timeFilter TEquals
         */
        times = new ArrayList<EventTime>();
        period = new TimePeriodType(new TimePositionType("2007-05-01T02:59:00.0"), new TimePositionType("2007-05-01T06:59:00.0"));
        filter = new BinaryTemporalOpType(period);
        equals = new EventTime(filter);
        times.add(equals);
        request  = new GetObservation("1.0.0",
                                      "offering-allSensor",
                                      times,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:4"),
                                      null,
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      Parameters.OBSERVATION_QNAME,
                                      ResponseModeType.RESULT_TEMPLATE,
                                      null);
        result = (ObservationCollectionEntry) worker.getObservation(request);

        obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/observationTemplate-4.xml"));

        expResult = (ObservationEntry)obj.getValue();

        //for template the sampling time is 1970 to now
        expResult.setSamplingTime(period);

        // and we empty the result object
        arrayP = (DataArrayPropertyType) expResult.getResult();
        array = arrayP.getDataArray();
        array.setElementCount(0);
        array.setValues("");

        expResult.setName("urn:ogc:object:observation:template:GEOM:4-1");

        assertEquals(result.getMember().size(), 1);

        obsResult = (ObservationEntry) result.getMember().iterator().next();


        assertEquals(expResult.getName(), obsResult.getName());
        assertEquals(expResult.getFeatureOfInterest(), obsResult.getFeatureOfInterest());
        assertEquals(expResult.getObservedProperty(), obsResult.getObservedProperty());
        assertEquals(expResult.getProcedure(), obsResult.getProcedure());
        assertEquals(expResult.getResult(), obsResult.getResult());
        assertEquals(expResult.getSamplingTime(), obsResult.getSamplingTime());
        assertEquals(expResult, obsResult);

        /**
         *  Test 20: getObservation with procedure urn:ogc:object:sensor:GEOM:4
         *           with resultTemplate mode
         *           with timeFilter Tafter
         */
        times = new ArrayList<EventTime>();
        instant = new TimeInstantType(new TimePositionType("2007-05-01T17:58:00.0"));
        filter = new BinaryTemporalOpType(instant);
        after = new EventTime(filter,null, null);
        times.add(after);
        request  = new GetObservation("1.0.0",
                                      "offering-allSensor",
                                      times,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:4"),
                                      null,
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      Parameters.OBSERVATION_QNAME,
                                      ResponseModeType.RESULT_TEMPLATE,
                                      null);
        result = (ObservationCollectionEntry) worker.getObservation(request);

        obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/observationTemplate-4.xml"));

        expResult = (ObservationEntry)obj.getValue();

        //for template the sampling time is 1970 to now
        period = new TimePeriodType(instant.getTimePosition());
        expResult.setSamplingTime(period);

        expResult.setName("urn:ogc:object:observation:template:GEOM:4-2");

        // and we empty the result object
        arrayP = (DataArrayPropertyType) expResult.getResult();
        array = arrayP.getDataArray();
        array.setElementCount(0);
        array.setValues("");

        assertEquals(result.getMember().size(), 1);

        obsResult = (ObservationEntry) result.getMember().iterator().next();


        assertEquals(expResult.getName(), obsResult.getName());
        assertEquals(expResult.getFeatureOfInterest(), obsResult.getFeatureOfInterest());
        assertEquals(expResult.getObservedProperty(), obsResult.getObservedProperty());
        assertEquals(expResult.getProcedure(), obsResult.getProcedure());
        assertEquals(expResult.getResult(), obsResult.getResult());
        assertEquals(expResult.getSamplingTime(), obsResult.getSamplingTime());
        assertEquals(expResult, obsResult);

        /**
         *  Test 21: getObservation with procedure urn:ogc:object:sensor:GEOM:4
         *           with resultTemplate mode
         *           with timeFilter Tbefore
         */
        times = new ArrayList<EventTime>();
        instant = new TimeInstantType(new TimePositionType("2007-05-01T17:58:00.0"));
        filter = new BinaryTemporalOpType(instant);
        before = new EventTime(null, filter, null);
        times.add(before);
        request  = new GetObservation("1.0.0",
                                      "offering-allSensor",
                                      times,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:4"),
                                      null,
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      Parameters.OBSERVATION_QNAME,
                                      ResponseModeType.RESULT_TEMPLATE,
                                      null);
        result = (ObservationCollectionEntry) worker.getObservation(request);

        obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/observationTemplate-4.xml"));

        expResult = (ObservationEntry)obj.getValue();

        //for template the sampling time is 1970 to now
        period = new TimePeriodType(TimeIndeterminateValueType.BEFORE, instant.getTimePosition());
        expResult.setSamplingTime(period);

        expResult.setName("urn:ogc:object:observation:template:GEOM:4-3");

        // and we empty the result object
        arrayP = (DataArrayPropertyType) expResult.getResult();
        array = arrayP.getDataArray();
        array.setElementCount(0);
        array.setValues("");

        assertEquals(result.getMember().size(), 1);

        obsResult = (ObservationEntry) result.getMember().iterator().next();


        assertEquals(expResult.getName(), obsResult.getName());
        assertEquals(expResult.getFeatureOfInterest(), obsResult.getFeatureOfInterest());
        assertEquals(expResult.getObservedProperty(), obsResult.getObservedProperty());
        assertEquals(expResult.getProcedure(), obsResult.getProcedure());
        assertEquals(expResult.getResult(), obsResult.getResult());
        assertEquals(expResult.getSamplingTime(), obsResult.getSamplingTime());
        assertEquals(expResult, obsResult);

        /**
         *  Test 22: getObservation with procedure urn:ogc:object:sensor:GEOM:4
         *           with observedproperties = urn:ogc:def:phenomenon:GEOM:depth
         */
        request  = new GetObservation("1.0.0",
                                      "offering-allSensor",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:4"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:depth"),
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      Parameters.OBSERVATION_QNAME,
                                      ResponseModeType.INLINE,
                                      null);
        result = (ObservationCollectionEntry) worker.getObservation(request);

        obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/observation3.xml"));

        expResult = (ObservationEntry)obj.getValue();
        assertEquals(result.getMember().size(), 1);


        obsResult = (ObservationEntry) result.getMember().iterator().next();
        assertTrue(obsResult != null);

        assertEquals(expResult.getName(), obsResult.getName());
        assertEquals(expResult.getFeatureOfInterest(), obsResult.getFeatureOfInterest());
        assertEquals(expResult.getObservedProperty(), obsResult.getObservedProperty());
        assertEquals(expResult.getProcedure(), obsResult.getProcedure());
        assertEquals(expResult.getResult(), obsResult.getResult());
        assertEquals(expResult.getSamplingTime(), obsResult.getSamplingTime());
        assertEquals(expResult, obsResult);

        /**
         *  Test 23: getObservation with procedure urn:ogc:object:sensor:GEOM:4
         *          and with wrong observed prop
         */
        request  = new GetObservation("1.0.0",
                                      "offering-allSensor",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:4"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:hotness"),
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      Parameters.OBSERVATION_QNAME,
                                      ResponseModeType.INLINE,
                                      null);

        exLaunched = false;
        try {
            worker.getObservation(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "observedProperty");
        }
        assertTrue(exLaunched);

        /**
         *  Test 24: getObservation with procedure urn:ogc:object:sensor:GEOM:5
         *           with observedproperties = urn:ogc:def:phenomenon:GEOM:aggreagtePhenomenon
         */
        request  = new GetObservation("1.0.0",
                                      "offering-allSensor",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:5"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:aggregatePhenomenon"),
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      Parameters.OBSERVATION_QNAME,
                                      ResponseModeType.INLINE,
                                      null);
        result = (ObservationCollectionEntry) worker.getObservation(request);

        obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/observation5.xml"));

        expResult = (ObservationEntry)obj.getValue();
        assertEquals(result.getMember().size(), 1);


        obsResult = (ObservationEntry) result.getMember().iterator().next();
        assertTrue(obsResult != null);

        assertEquals(expResult.getName(), obsResult.getName());
        assertEquals(expResult.getFeatureOfInterest(), obsResult.getFeatureOfInterest());
        assertEquals(expResult.getObservedProperty(), obsResult.getObservedProperty());
        assertEquals(expResult.getProcedure(), obsResult.getProcedure());
        assertEquals(expResult.getResult(), obsResult.getResult());
        assertEquals(expResult.getSamplingTime(), obsResult.getSamplingTime());
        assertEquals(expResult, obsResult);

        /**
         *  Test 25: getObservation with procedure urn:ogc:object:sensor:GEOM:5
         *           with observedproperties = urn:ogc:def:phenomenon:GEOM:aggreagtePhenomenon
         *           with foi                =  10972X0137-PLOUF
         */
        request  = new GetObservation("1.0.0",
                                      "offering-allSensor",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:5"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:aggregatePhenomenon"),
                                      new GetObservation.FeatureOfInterest(Arrays.asList("station-002")),
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      Parameters.OBSERVATION_QNAME,
                                      ResponseModeType.INLINE,
                                      null);
        result = (ObservationCollectionEntry) worker.getObservation(request);

        obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/observation5.xml"));

        expResult = (ObservationEntry)obj.getValue();
        assertEquals(result.getMember().size(), 1);


        obsResult = (ObservationEntry) result.getMember().iterator().next();
        assertTrue(obsResult != null);

        assertEquals(expResult.getName(), obsResult.getName());
        assertEquals(expResult.getFeatureOfInterest(), obsResult.getFeatureOfInterest());
        assertEquals(expResult.getObservedProperty(), obsResult.getObservedProperty());
        assertEquals(expResult.getProcedure(), obsResult.getProcedure());
        assertEquals(expResult.getResult(), obsResult.getResult());
        assertEquals(expResult.getSamplingTime(), obsResult.getSamplingTime());
        assertEquals(expResult, obsResult);

        /**
         *  Test 26: getObservation with procedure urn:ogc:object:sensor:GEOM:5
         *          and with wrong foi
         */
        request  = new GetObservation("1.0.0",
                                      "offering-allSensor",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:4"),
                                      null,
                                      new GetObservation.FeatureOfInterest(Arrays.asList("NIMP")),
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      Parameters.OBSERVATION_QNAME,
                                      ResponseModeType.INLINE,
                                      null);

        exLaunched = false;
        try {
            worker.getObservation(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "featureOfInterest");
        }
        assertTrue(exLaunched);

        /**
         *  Test 27: getObservation with procedure urn:ogc:object:sensor:GEOM:3
         *           with observedproperties = urn:ogc:def:phenomenon:GEOM:aggregatePhenomenon
         *           => no error but no result
         */
        request  = new GetObservation("1.0.0",
                                      "offering-allSensor",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:aggregatePhenomenon"),
                                      new GetObservation.FeatureOfInterest(Arrays.asList("station-002")),
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      Parameters.OBSERVATION_QNAME,
                                      ResponseModeType.INLINE,
                                      null);
        result = (ObservationCollectionEntry) worker.getObservation(request);


        ObservationCollectionEntry collExpResult = new ObservationCollectionEntry("urn:ogc:def:nil:OGC:inapplicable");
        assertEquals(collExpResult, result);

        /**
         *  Test 28: getObservation with procedure urn:ogc:object:sensor:GEOM:7
         *           with resultTemplate mode
         *  => measurement type
         */
        request  = new GetObservation("1.0.0",
                                      "offering-allSensor",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:7"),
                                      null,
                                      new GetObservation.FeatureOfInterest(Arrays.asList("station-002")),
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      Parameters.MEASUREMENT_QNAME,
                                      ResponseModeType.RESULT_TEMPLATE,
                                      null);
        result = (ObservationCollectionEntry) worker.getObservation(request);

        assertTrue(result.getMember().iterator().next() instanceof MeasurementEntry);
        
        MeasurementEntry measResult =  (MeasurementEntry) result.getMember().iterator().next();
        assertTrue(measResult != null);

        obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/observationTemplate-7.xml"));

        expResult = (ObservationEntry)obj.getValue();

        period = new TimePeriodType(new TimePositionType("1900-01-01T00:00:00"));
        expResult.setSamplingTime(period);
        expResult.setName("urn:ogc:object:observation:template:GEOM:7-0");

        assertEquals(expResult.getName(), measResult.getName());
        assertEquals(expResult.getResult(), measResult.getResult());
        assertEquals(expResult, measResult);
        
        marshallerPool.release(unmarshaller);
    }

    /**
     * Tests the GetResult method
     *
     * @throws java.lang.Exception
     */
    public void GetResultTest() throws Exception {
        Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();
        
        /**
         * Test 1: bad version number + null template ID
         */
        String templateId = null;
        GetResult request = new GetResult(templateId, null, "2.0.0");
        boolean exLaunched = false;
        try {
            worker.getResult(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), VERSION_NEGOTIATION_FAILED);
        }
        assertTrue(exLaunched);

        /**
         * Test 2:  null template ID
         */
        templateId = null;
        request = new GetResult(templateId, null, "1.0.0");
        exLaunched = false;
        try {
            worker.getResult(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), MISSING_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "ObservationTemplateId");
        }
        assertTrue(exLaunched);

        /**
         * Test 3:  bad template ID
         */
        templateId = "some id";
        request = new GetResult(templateId, null, "1.0.0");
        exLaunched = false;
        try {
            worker.getResult(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "ObservationTemplateId");
        }
        assertTrue(exLaunched);

        // we make a getObservation request in order to get a template

        /**
         *   getObservation with procedure urn:ogc:object:sensor:GEOM:4
         *           with resultTemplate mode
         */
        GetObservation GOrequest  = new GetObservation("1.0.0",
                                      "offering-allSensor",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                      null,
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      Parameters.OBSERVATION_QNAME,
                                      ResponseModeType.RESULT_TEMPLATE,
                                      null);
        ObservationCollectionEntry obsCollResult = (ObservationCollectionEntry) worker.getObservation(GOrequest);

        JAXBElement obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/observationTemplate-3.xml"));

        ObservationEntry templateExpResult = (ObservationEntry)obj.getValue();

        //for template the sampling time is 1970 to now
        TimePeriodType period = new TimePeriodType(new TimePositionType("1900-01-01T00:00:00"));
        templateExpResult.setSamplingTime(period);

        // and we empty the result object
        DataArrayPropertyType arrayP = (DataArrayPropertyType) templateExpResult.getResult();
        DataArrayEntry array = arrayP.getDataArray();
        array.setElementCount(0);
        array.setValues("");

        templateExpResult.setName("urn:ogc:object:observation:template:GEOM:3-0");

        assertEquals(obsCollResult.getMember().size(), 1);

        ObservationEntry obsResult = (ObservationEntry) obsCollResult.getMember().iterator().next();

        assertNotNull(obsResult);

        DataArrayPropertyType obsR = (DataArrayPropertyType) obsResult.getResult();
        SimpleDataRecordEntry obsSdr = (SimpleDataRecordEntry) obsR.getDataArray().getElementType();
        obsSdr.setBlockId(null);

        assertTrue(obsResult != null);
        assertEquals(templateExpResult.getName(), obsResult.getName());
        assertEquals(templateExpResult.getFeatureOfInterest(), obsResult.getFeatureOfInterest());
        assertEquals(templateExpResult.getObservedProperty(), obsResult.getObservedProperty());
        assertEquals(templateExpResult.getProcedure(), obsResult.getProcedure());
        assertEquals(templateExpResult.getResult(), obsResult.getResult());
        assertEquals(templateExpResult.getSamplingTime(), obsResult.getSamplingTime());
        assertEquals(templateExpResult, obsResult);

        /**
         * Test 4:  getResult with no TimeFilter
         */
        templateId = "urn:ogc:object:observation:template:GEOM:3-0";
        request = new GetResult(templateId, null, "1.0.0");
        GetResultResponse result = worker.getResult(request);

        String value = "2007-05-01T02:59:00,6.560@@2007-05-01T03:59:00,6.560@@2007-05-01T04:59:00,6.560@@2007-05-01T05:59:00,6.560@@2007-05-01T06:59:00,6.560@@" + '\n' +
                       "2007-05-01T07:59:00,6.560@@2007-05-01T08:59:00,6.560@@2007-05-01T09:59:00,6.560@@2007-05-01T10:59:00,6.560@@2007-05-01T11:59:00,6.560@@" + '\n' +
                       "2007-05-01T17:59:00,6.560@@2007-05-01T18:59:00,6.550@@2007-05-01T19:59:00,6.550@@2007-05-01T20:59:00,6.550@@2007-05-01T21:59:00,6.550@@" + '\n';
        GetResultResponse expResult = new GetResultResponse(new GetResultResponse.Result(value, URL + '/' + templateId));

        assertEquals(expResult.getResult().getRS(), result.getResult().getRS());
        assertEquals(expResult.getResult().getValue(), result.getResult().getValue());
        assertEquals(expResult.getResult(), result.getResult());
        assertEquals(expResult, result);

        /**
         *   getObservation with procedure urn:ogc:object:sensor:GEOM:3
         *   with resultTemplate mode and time filter TBefore
         */
        List<EventTime> times = new ArrayList<EventTime>();
        TimeInstantType instant = new TimeInstantType(new TimePositionType("2007-05-01T05:00:00.0"));
        BinaryTemporalOpType filter = new BinaryTemporalOpType(instant);
        EventTime before = new EventTime(null, filter, null);
        times.add(before);
        GOrequest  = new GetObservation("1.0.0",
                                        "offering-allSensor",
                                        times,
                                        Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                        null,
                                        null,
                                        null,
                                        "text/xml; subtype=\"om/1.0.0\"",
                                        Parameters.OBSERVATION_QNAME,
                                        ResponseModeType.RESULT_TEMPLATE,
                                        null);
        obsCollResult = (ObservationCollectionEntry) worker.getObservation(GOrequest);

        obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/observationTemplate-3.xml"));

        templateExpResult = (ObservationEntry)obj.getValue();

        //for template the sampling time is 1970 to now
        period = new TimePeriodType(TimeIndeterminateValueType.BEFORE, new TimePositionType("2007-05-01T05:00:00.0"));
        templateExpResult.setSamplingTime(period);

        // and we empty the result object
        arrayP = (DataArrayPropertyType) templateExpResult.getResult();
        array = arrayP.getDataArray();
        array.setElementCount(0);
        array.setValues("");

        templateExpResult.setName("urn:ogc:object:observation:template:GEOM:3-1");

        assertEquals(obsCollResult.getMember().size(), 1);

        obsResult = (ObservationEntry) obsCollResult.getMember().iterator().next();

        obsR = (DataArrayPropertyType) obsResult.getResult();
        obsSdr = (SimpleDataRecordEntry) obsR.getDataArray().getElementType();
        obsSdr.setBlockId(null);

        assertTrue(obsResult != null);
        assertEquals(templateExpResult.getName(), obsResult.getName());
        assertEquals(templateExpResult.getFeatureOfInterest(), obsResult.getFeatureOfInterest());
        assertEquals(templateExpResult.getObservedProperty(), obsResult.getObservedProperty());
        assertEquals(templateExpResult.getProcedure(), obsResult.getProcedure());
        assertEquals(templateExpResult.getResult(), obsResult.getResult());
        assertEquals(templateExpResult.getSamplingTime(), obsResult.getSamplingTime());
        assertEquals(templateExpResult, obsResult);

        /**
         * Test 5:  getResult with no TimeFilter
         */
        templateId = "urn:ogc:object:observation:template:GEOM:3-1";
        request = new GetResult(templateId, null, "1.0.0");
        result = worker.getResult(request);

        value = "2007-05-01T02:59:00,6.560@@2007-05-01T03:59:00,6.560@@2007-05-01T04:59:00,6.560@@" + '\n';
        expResult = new GetResultResponse(new GetResultResponse.Result(value, URL + '/' + templateId));

        assertEquals(expResult.getResult().getRS(), result.getResult().getRS());
        assertEquals(expResult.getResult().getValue(), result.getResult().getValue());
        assertEquals(expResult.getResult(), result.getResult());
        assertEquals(expResult, result);

         /**
         * Test 6:  getResult with Tafter
         */
        times = new ArrayList<EventTime>();
        instant = new TimeInstantType(new TimePositionType("2007-05-01T03:00:00.0"));
        filter = new BinaryTemporalOpType(instant);
        EventTime after = new EventTime(filter, null, null);
        times.add(after);

        templateId = "urn:ogc:object:observation:template:GEOM:3-1";
        request = new GetResult(templateId, times, "1.0.0");
        result = worker.getResult(request);

        value = "2007-05-01T03:59:00,6.560@@2007-05-01T04:59:00,6.560@@" + '\n';
        expResult = new GetResultResponse(new GetResultResponse.Result(value, URL + '/' + templateId));

        assertEquals(expResult.getResult().getRS(), result.getResult().getRS());
        assertEquals(expResult.getResult().getValue(), result.getResult().getValue());
        assertEquals(expResult.getResult(), result.getResult());
        assertEquals(expResult, result);

        /**
         * Test 7:  getResult with Tbefore
         */
        times = new ArrayList<EventTime>();
        instant = new TimeInstantType(new TimePositionType("2007-05-01T04:00:00.0"));
        filter = new BinaryTemporalOpType(instant);
        EventTime before2 = new EventTime(null, filter, null);
        times.add(before2);

        templateId = "urn:ogc:object:observation:template:GEOM:3-1";
        request = new GetResult(templateId, times, "1.0.0");
        result = worker.getResult(request);

        value = "2007-05-01T02:59:00,6.560@@2007-05-01T03:59:00,6.560@@" + '\n';
        expResult = new GetResultResponse(new GetResultResponse.Result(value, URL + '/' + templateId));

        assertEquals(expResult.getResult().getRS(), result.getResult().getRS());
        assertEquals(expResult.getResult().getValue(), result.getResult().getValue());
        assertEquals(expResult.getResult(), result.getResult());
        assertEquals(expResult, result);

        /**
         * Test 8:  getResult with TEquals
         */
        times = new ArrayList<EventTime>();
        instant = new TimeInstantType(new TimePositionType("2007-05-01T03:59:00.0"));
        filter = new BinaryTemporalOpType(instant);
        EventTime equals = new EventTime(filter);
        times.add(equals);

        templateId = "urn:ogc:object:observation:template:GEOM:3-1";
        request = new GetResult(templateId, times, "1.0.0");
        result = worker.getResult(request);

        value = "2007-05-01T03:59:00,6.560@@" + '\n';
        expResult = new GetResultResponse(new GetResultResponse.Result(value, URL + '/' + templateId));

        assertEquals(expResult.getResult().getRS(), result.getResult().getRS());
        assertEquals(expResult.getResult().getValue(), result.getResult().getValue());
        assertEquals(expResult.getResult(), result.getResult());
        assertEquals(expResult, result);

        /**
         * Test 9:  getResult with TEquals
         */
        times = new ArrayList<EventTime>();
        period = new TimePeriodType(new TimePositionType("2007-05-01T03:00:00.0"), new TimePositionType("2007-05-01T04:00:00.0"));
        filter = new BinaryTemporalOpType(period);
        EventTime during = new EventTime(null, null, filter);
        times.add(during);

        templateId = "urn:ogc:object:observation:template:GEOM:3-1";
        request = new GetResult(templateId, times, "1.0.0");
        result = worker.getResult(request);

        value = "2007-05-01T03:59:00,6.560@@" + '\n';
        expResult = new GetResultResponse(new GetResultResponse.Result(value, URL + '/' + templateId));

        assertEquals(expResult.getResult().getRS(), result.getResult().getRS());
        assertEquals(expResult.getResult().getValue(), result.getResult().getValue());
        assertEquals(expResult.getResult(), result.getResult());
        assertEquals(expResult, result);


         /**
         *   getObservation with procedure urn:ogc:object:sensor:GEOM:3
         *   with resultTemplate mode and time filter TAfter
         */
        times = new ArrayList<EventTime>();
        instant = new TimeInstantType(new TimePositionType("2007-05-01T19:00:00.0"));
        filter = new BinaryTemporalOpType(instant);
        after = new EventTime(filter, null, null);
        times.add(after);
        GOrequest  = new GetObservation("1.0.0",
                                        "offering-allSensor",
                                        times,
                                        Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                        null,
                                        null,
                                        null,
                                        "text/xml; subtype=\"om/1.0.0\"",
                                        Parameters.OBSERVATION_QNAME,
                                        ResponseModeType.RESULT_TEMPLATE,
                                        null);
        obsCollResult = (ObservationCollectionEntry) worker.getObservation(GOrequest);

        obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/observationTemplate-3.xml"));

        templateExpResult = (ObservationEntry)obj.getValue();

        //for template the sampling time is 1970 to now
        period = new TimePeriodType(new TimePositionType("2007-05-01T19:00:00.0"));
        templateExpResult.setSamplingTime(period);

        // and we empty the result object
        arrayP = (DataArrayPropertyType) templateExpResult.getResult();
        array = arrayP.getDataArray();
        array.setElementCount(0);
        array.setValues("");

        templateExpResult.setName("urn:ogc:object:observation:template:GEOM:3-2");

        assertEquals(obsCollResult.getMember().size(), 1);

        obsResult = (ObservationEntry) obsCollResult.getMember().iterator().next();

        obsR = (DataArrayPropertyType) obsResult.getResult();
        obsSdr = (SimpleDataRecordEntry) obsR.getDataArray().getElementType();
        obsSdr.setBlockId(null);

        assertTrue(obsResult != null);
        assertEquals(templateExpResult.getName(), obsResult.getName());
        assertEquals(templateExpResult.getFeatureOfInterest(), obsResult.getFeatureOfInterest());
        assertEquals(templateExpResult.getObservedProperty(), obsResult.getObservedProperty());
        assertEquals(templateExpResult.getProcedure(), obsResult.getProcedure());
        assertEquals(templateExpResult.getResult(), obsResult.getResult());
        assertEquals(templateExpResult.getSamplingTime(), obsResult.getSamplingTime());
        assertEquals(templateExpResult, obsResult);

        
        /**
         * Test 10:  getResult with no TimeFilter
         */
        templateId = "urn:ogc:object:observation:template:GEOM:3-2";
        request = new GetResult(templateId, null, "1.0.0");
        result = worker.getResult(request);

        value = "2007-05-01T19:59:00,6.550@@2007-05-01T20:59:00,6.550@@2007-05-01T21:59:00,6.550@@" + '\n';
        expResult = new GetResultResponse(new GetResultResponse.Result(value, URL + '/' + templateId));

        assertEquals(expResult.getResult().getRS(), result.getResult().getRS());
        assertEquals(expResult.getResult().getValue(), result.getResult().getValue());
        assertEquals(expResult.getResult(), result.getResult());
        assertEquals(expResult, result);

        marshallerPool.release(unmarshaller);

         /**
         *   getObservation with procedure urn:ogc:object:sensor:GEOM:3
         *   with resultTemplate mode and time filter TEquals
         */
        times = new ArrayList<EventTime>();
        instant = new TimeInstantType(new TimePositionType("2007-05-01T20:59:00.0"));
        filter = new BinaryTemporalOpType(instant);
        equals = new EventTime(filter);
        times.add(equals);
        GOrequest  = new GetObservation("1.0.0",
                                        "offering-allSensor",
                                        times,
                                        Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                        null,
                                        null,
                                        null,
                                        "text/xml; subtype=\"om/1.0.0\"",
                                        Parameters.OBSERVATION_QNAME,
                                        ResponseModeType.RESULT_TEMPLATE,
                                        null);
        obsCollResult = (ObservationCollectionEntry) worker.getObservation(GOrequest);

        obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/observationTemplate-3.xml"));

        templateExpResult = (ObservationEntry)obj.getValue();

        instant = new TimeInstantType(new TimePositionType("2007-05-01T20:59:00.0"));
        templateExpResult.setSamplingTime(instant);

        // and we empty the result object
        arrayP = (DataArrayPropertyType) templateExpResult.getResult();
        array = arrayP.getDataArray();
        array.setElementCount(0);
        array.setValues("");

        templateExpResult.setName("urn:ogc:object:observation:template:GEOM:3-3");

        assertEquals(obsCollResult.getMember().size(), 1);

        obsResult = (ObservationEntry) obsCollResult.getMember().iterator().next();

        obsR = (DataArrayPropertyType) obsResult.getResult();
        obsSdr = (SimpleDataRecordEntry) obsR.getDataArray().getElementType();
        obsSdr.setBlockId(null);

        assertTrue(obsResult != null);
        assertEquals(templateExpResult.getName(), obsResult.getName());
        assertEquals(templateExpResult.getFeatureOfInterest(), obsResult.getFeatureOfInterest());
        assertEquals(templateExpResult.getObservedProperty(), obsResult.getObservedProperty());
        assertEquals(templateExpResult.getProcedure(), obsResult.getProcedure());
        assertEquals(templateExpResult.getResult(), obsResult.getResult());
        assertEquals(templateExpResult.getSamplingTime(), obsResult.getSamplingTime());
        assertEquals(templateExpResult, obsResult);

        /**
         * Test 11:  getResult with no TimeFilter
         */
        templateId = "urn:ogc:object:observation:template:GEOM:3-3";
        request = new GetResult(templateId, null, "1.0.0");
        result = worker.getResult(request);

        value = "2007-05-01T20:59:00,6.550@@" + '\n';
        expResult = new GetResultResponse(new GetResultResponse.Result(value, URL + '/' + templateId));

        assertEquals(expResult.getResult().getRS(), result.getResult().getRS());
        assertEquals(expResult.getResult().getValue(), result.getResult().getValue());
        assertEquals(expResult.getResult(), result.getResult());
        assertEquals(expResult, result);

        marshallerPool.release(unmarshaller);

    }

    /**
     * Tests the InsertObservation method
     *
     * @throws java.lang.Exception
     */
    public void insertObservationTest() throws Exception {
        Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();

        JAXBElement obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/observationTemplate-3.xml"));

        ObservationEntry template = (ObservationEntry)obj.getValue();

        TimePeriodType period = new TimePeriodType(new TimePositionType("2007-06-01T01:00:00.0"), new TimePositionType("2007-06-01T03:00:00.0"));
        template.setSamplingTime(period);

        // and we fill the result object
        DataArrayPropertyType arrayP = (DataArrayPropertyType) template.getResult();
        DataArrayEntry array = arrayP.getDataArray();
        array.setElementCount(3);
        array.setValues("2007-06-01T01:01:00,6.560@@2007-06-01T02:00:00,6.550@@2007-06-01T03:00:00,6.550@@");

        InsertObservation request = new InsertObservation("1.0.0", "urn:ogc:object:sensor:GEOM:3", template);
        worker.insertObservation(request);

         /**
         *   getObservation with procedure urn:ogc:object:sensor:GEOM:4
         *           with resultTemplate mode
         */
        GetObservation GOrequest  = new GetObservation("1.0.0",
                                      "offering-allSensor",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                      null,
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      Parameters.OBSERVATION_QNAME,
                                      ResponseModeType.RESULT_TEMPLATE,
                                      null);
        ObservationCollectionEntry obsColl = (ObservationCollectionEntry) worker.getObservation(GOrequest);



        String templateId = "urn:ogc:object:observation:template:GEOM:3-0";
        GetResult GRrequest = new GetResult(templateId, null, "1.0.0");
        GetResultResponse result = worker.getResult(GRrequest);

        String value = "2007-05-01T02:59:00,6.560@@2007-05-01T03:59:00,6.560@@2007-05-01T04:59:00,6.560@@2007-05-01T05:59:00,6.560@@2007-05-01T06:59:00,6.560@@" + '\n' +
                       "2007-05-01T07:59:00,6.560@@2007-05-01T08:59:00,6.560@@2007-05-01T09:59:00,6.560@@2007-05-01T10:59:00,6.560@@2007-05-01T11:59:00,6.560@@" + '\n' +
                       "2007-05-01T17:59:00,6.560@@2007-05-01T18:59:00,6.550@@2007-05-01T19:59:00,6.550@@2007-05-01T20:59:00,6.550@@2007-05-01T21:59:00,6.550@@" + '\n' +
                       "2007-06-01T01:01:00,6.560@@2007-06-01T02:00:00,6.550@@2007-06-01T03:00:00,6.550@@" + '\n';
        GetResultResponse expResult = new GetResultResponse(new GetResultResponse.Result(value, URL + '/' + templateId));

        assertEquals(expResult.getResult().getRS(), result.getResult().getRS());
        assertEquals(expResult.getResult().getValue(), result.getResult().getValue());
        assertEquals(expResult.getResult(), result.getResult());
        assertEquals(expResult, result);

        marshallerPool.release(unmarshaller);
    }

    /**
     * Tests the RegisterSensor method
     *
     * @throws java.lang.Exception
     */
    public void RegisterSensorTest() throws Exception {
        Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();

        /**
         * Test 1 we register a system sensor
         */
        AbstractSensorML sensorDescription = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/system.xml"));
        
        JAXBElement obj =  (JAXBElement) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/observationTemplate-6.xml"));

        ObservationEntry obsTemplate = (ObservationEntry)obj.getValue();

        RegisterSensor request = new RegisterSensor("1.0.0", sensorDescription, new ObservationTemplate(obsTemplate));

        RegisterSensorResponse response = worker.registerSensor(request);

        assertEquals("urn:ogc:object:sensor:GEOM:6", response.getAssignedSensorId());

        /**
         * we verify that the sensor is wel registred
         */
        DescribeSensor DSrequest  = new DescribeSensor("urn:ogc:object:sensor:GEOM:6", "text/xml;subtype=\"SensorML/1.0.0\"");
        AbstractSensorML absResult = (AbstractSensorML) worker.describeSensor(DSrequest);

        
        assertTrue(absResult instanceof SensorML);
        assertTrue(sensorDescription instanceof SensorML);
        SensorML result = (SensorML) absResult;
        SensorML expResult = (SensorML) sensorDescription;


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
        SystemType expProcess = (SystemType) expResult.getMember().iterator().next().getProcess().getValue();
        assertTrue(result.getMember().iterator().next().getProcess().getValue() instanceof SystemType);
        SystemType resProcess = (SystemType) result.getMember().iterator().next().getProcess().getValue();

        /*
         * TODO fix this issue for mdweb redaer/writer
         */
        EnvelopeEntry nullEnv = null;
        resProcess.setBoundedBy(nullEnv);
        expProcess.setBoundedBy(nullEnv);
        assertEquals(expProcess.getBoundedBy(), resProcess.getBoundedBy());

        assertEquals(expProcess.getCapabilities(), resProcess.getCapabilities());

        assertEquals(expProcess.getClassification().size(), resProcess.getClassification().size());
        assertEquals(resProcess.getClassification().size(), 1);
        assertEquals(expProcess.getClassification().get(0).getClassifierList().getClassifier().size(), resProcess.getClassification().get(0).getClassifierList().getClassifier().size());
        for (int i = 0; i < 10; i++) {
            assertEquals(expProcess.getClassification().get(0).getClassifierList().getClassifier().get(i), resProcess.getClassification().get(0).getClassifierList().getClassifier().get(i));
        }
        assertEquals(expProcess.getClassification().get(0).getClassifierList().getClassifier(), resProcess.getClassification().get(0).getClassifierList().getClassifier());
        assertEquals(expProcess.getClassification().get(0).getClassifierList(), resProcess.getClassification().get(0).getClassifierList());
        assertEquals(expProcess.getClassification().get(0), resProcess.getClassification().get(0));
        assertEquals(expProcess.getClassification(), resProcess.getClassification());
        assertEquals(expProcess.getConnections(), resProcess.getConnections());

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
        for (int i = 0; i < expProcess.getComponents().getComponentList().getComponent().size(); i++) {
            ComponentPropertyType expCompo = expProcess.getComponents().getComponentList().getComponent().get(i);
            ComponentPropertyType resCompo = resProcess.getComponents().getComponentList().getComponent().get(i);
            assertEquals(expCompo.getHref(), resCompo.getHref());
            assertEquals(expCompo.getRole(), resCompo.getRole());
            assertEquals(expCompo.getName(), resCompo.getName());
            assertEquals(expCompo.getType(), resCompo.getType());
            assertEquals(expCompo.getTitle(), resCompo.getTitle());
            assertTrue(expCompo.getProcess() == null);
            assertTrue(resCompo.getProcess() == null);
            assertEquals(expCompo, resCompo);
        }
        assertEquals(expProcess.getComponents().getComponentList().getComponent(), resProcess.getComponents().getComponentList().getComponent());
        assertEquals(expProcess.getComponents().getComponentList(), resProcess.getComponents().getComponentList());
        assertEquals(expProcess.getComponents(), resProcess.getComponents());
        assertEquals(expProcess.getOutputs(), resProcess.getOutputs());
        assertEquals(expProcess.getParameters(), resProcess.getParameters());
        assertEquals(expProcess.getPosition(), resProcess.getPosition());
        assertEquals(expProcess.getPositions().getPositionList().getPosition().get(0).getPosition(), resProcess.getPositions().getPositionList().getPosition().get(0).getPosition());
        assertEquals(expProcess.getPositions().getPositionList().getPosition().get(0).getVector(), resProcess.getPositions().getPositionList().getPosition().get(0).getVector());
        assertEquals(expProcess.getPositions().getPositionList().getPosition().get(0).getName(), resProcess.getPositions().getPositionList().getPosition().get(0).getName());
        assertEquals(expProcess.getPositions().getPositionList().getPosition().get(0), resProcess.getPositions().getPositionList().getPosition().get(0));
        assertEquals(expProcess.getPositions().getPositionList().getPosition(), resProcess.getPositions().getPositionList().getPosition());
        assertEquals(expProcess.getPositions().getPositionList(), resProcess.getPositions().getPositionList());
        assertEquals(expProcess.getPositions(), resProcess.getPositions());
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


        /**
         * Test 2 we register a system sensor with no Observation template
         */

        request = new RegisterSensor("1.0.0", sensorDescription, null);
        boolean exLaunched = false;
        try {
            worker.registerSensor(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getLocator(),       Parameters.OBSERVATION_TEMPLATE);
            assertEquals(ex.getExceptionCode(), MISSING_PARAMETER_VALUE);
        }

        assertTrue(exLaunched);

        /**
         * Test 2 we register a system sensor with an imcomplete Observation template
         */
        obsTemplate.setProcedure(null);
        request = new RegisterSensor("1.0.0", sensorDescription, new ObservationTemplate(obsTemplate));
        exLaunched = false;
        try {
            worker.registerSensor(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getLocator(),       Parameters.OBSERVATION_TEMPLATE);
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
        }

        assertTrue(exLaunched);

        marshallerPool.release(unmarshaller);
    }
    
    /**
     * Tests the destroy method
     *
     * @throws java.lang.Exception
     */
    public void destroyTest() throws Exception {
        worker.destroy();
        GetCapabilities request = new GetCapabilities();

        boolean exLaunched = false;
        try {
            worker.getCapabilities(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), NO_APPLICABLE_CODE);
            assertEquals(ex.getMessage(), "The service is not running!");
        }

        assertTrue(exLaunched);
    }
}
