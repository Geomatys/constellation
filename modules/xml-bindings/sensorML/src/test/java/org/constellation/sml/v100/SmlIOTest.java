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

package org.constellation.sml.v100;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

//constellation
import org.constellation.gml.v311.TimePeriodType;
import org.constellation.gml.v311.TimePositionType;
import org.constellation.swe.v100.AbstractDataRecordType;
import org.constellation.swe.v100.CodeSpacePropertyType;
import org.constellation.swe.v100.DataComponentPropertyType;
import org.constellation.swe.v100.DataRecordType;
import org.constellation.swe.v100.ObservableProperty;
import org.constellation.swe.v100.QuantityRange;
import org.constellation.swe.v100.QuantityType;
import org.constellation.swe.v100.TimeRange;
import org.constellation.swe.v100.UomPropertyType;
import org.constellation.util.Util;

// JAXB dependencies
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

// Constellation dependencies
import org.constellation.gml.v311.CodeType;
import org.constellation.gml.v311.CoordinateSystemRefType;
import org.constellation.gml.v311.DirectPositionType;
import org.constellation.gml.v311.EngineeringCRSType;
import org.constellation.gml.v311.EngineeringDatumRefType;
import org.constellation.gml.v311.EngineeringDatumType;
import org.constellation.gml.v311.PointType;
import org.constellation.gml.v311.TemporalCRSType;
import org.constellation.gml.v311.TemporalCSRefType;
import org.constellation.gml.v311.TemporalDatumRefType;
import org.constellation.swe.v100.BooleanType;
import org.constellation.swe.v100.Category;
import org.constellation.swe.v100.CoordinateType;
import org.constellation.swe.v100.PositionType;
import org.constellation.swe.v100.TimeType;
import org.constellation.swe.v100.VectorPropertyType;
import org.constellation.swe.v100.VectorType;

//Junit dependencies
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class SmlIOTest {

    private Logger       logger = Logger.getLogger("org.constellation.filter");
    private Unmarshaller unmarshaller;
    private Marshaller   marshaller;
    private ObjectFactory sml100Factory = new ObjectFactory();
    private org.constellation.swe.v100.ObjectFactory swe100Factory = new org.constellation.swe.v100.ObjectFactory();


    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        JAXBContext jbcontext  = JAXBContext.newInstance("org.constellation.sml.v100");
        unmarshaller           = jbcontext.createUnmarshaller();
        marshaller             = jbcontext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//        marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapperImpl(""));

    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test simple Record Marshalling.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void ComponentMarshalingTest() throws Exception {

        ComponentType compo = new ComponentType();

    }

    /**
     * Test simple Record Marshalling.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void ComponentUnmarshallMarshalingTest() throws Exception {

        InputStream is = Util.getResourceAsStream("org/constellation/sml/component.xml");
        Object unmarshalled = unmarshaller.unmarshal(is);
        if (unmarshalled instanceof JAXBElement) {
            unmarshalled = ((JAXBElement)unmarshalled).getValue();
        }

        assertTrue(unmarshalled instanceof SensorML);

        SensorML result = (SensorML) unmarshalled;

        SensorML.Member member = new SensorML.Member();
        member.setRole("urn:x-ogx:def:sensor:OGC:detector");

        ComponentType component = new ComponentType();

        List<JAXBElement<String>> kw = new ArrayList<JAXBElement<String>>();
        kw.add(sml100Factory.createKeywordsKeywordListKeyword("piezometer"));
        kw.add(sml100Factory.createKeywordsKeywordListKeyword("geosciences"));
        kw.add(sml100Factory.createKeywordsKeywordListKeyword("point d'eau"));
        Keywords keywords = new Keywords(new KeywordList("urn:x-brgm:def:gcmd:keywords", kw));
        component.setKeywords(keywords);

        Classifier cl1 = new Classifier("intendedApplication", new Term("eaux souterraines", "urn:x-ogc:def:classifier:OGC:application"));
        CodeSpacePropertyType cs = new CodeSpacePropertyType("urn:x-brgm:def:GeoPoint:bss");
        Classifier cl2 = new Classifier("sensorType", new Term(cs, "Profondeur", "urn:sensor:classifier:sensorType"));
        List<Classifier> cls = new ArrayList<Classifier>();
        cls.add(cl1);
        cls.add(cl2);
        ClassifierList claList = new ClassifierList(null, cls);
        Classification classification = new Classification(claList);
        component.setClassification(classification);

        List<Identifier> identifiers = new ArrayList<Identifier>();
        cs = new CodeSpacePropertyType("urn:x-brgm:def:sensorSystem:hydras");
        Identifier id1 = new Identifier("supervisorCode", new Term(cs, "00ARGLELES_2000", "urn:x-ogc:def:identifier:OGC:modelNumber"));
        Identifier id2 = new Identifier("longName", new Term("Madofil II", "urn:x-ogc:def:identifier:OGC:longname"));
        identifiers.add(id1);
        identifiers.add(id2);
        IdentifierList identifierList = new IdentifierList(null, identifiers);
        Identification identification = new Identification(identifierList);
        component.setIdentification(identification);

        TimePeriodType period = new TimePeriodType(new TimePositionType("2004-06-01"));
        ValidTime vTime = new ValidTime(period);
        component.setValidTime(vTime);

        CapabilitiesSML capabilities = new CapabilitiesSML();
        TimeRange timeRange = new TimeRange(Arrays.asList("1987-04-23", "now"));
        DataComponentPropertyType field = new DataComponentPropertyType("periodOfData", "urn:x-brgm:def:property:periodOfData", timeRange);
        DataRecordType record = new DataRecordType("urn:x-brgm:def:property:periodOfData", Arrays.asList(field));
        JAXBElement<? extends AbstractDataRecordType> jbRecord = swe100Factory.createDataRecord(record);
        capabilities.setAbstractDataRecord(jbRecord);
        component.setCapabilities(capabilities);

        Contact contact = new Contact("urn:x-ogc:def:role:manufacturer", new ResponsibleParty("IRIS"));
        component.setContact(contact);

        Position position = new Position("conductivitePosition", "piezometer#piezoPosition");
        component.setPosition(position);

        IoComponentPropertyType io = new IoComponentPropertyType("level", new ObservableProperty("urn:x-ogc:def:phenomenon:OGC:level"));
        InputList inputList = new InputList(Arrays.asList(io));
        Inputs inputs = new Inputs(inputList);
        component.setInputs(inputs);

        IoComponentPropertyType io2 = new IoComponentPropertyType("depth", new ObservableProperty("urn:x-ogc:def:phenomenon:OGC:depth"));
        OutputList outputList = new OutputList(Arrays.asList(io2));
        Outputs outputs = new Outputs(outputList);
        component.setOutputs(outputs);

        List<DataComponentPropertyType> params = new ArrayList<DataComponentPropertyType>();
        UomPropertyType uom = new UomPropertyType(null, "urn:ogc:unit:minuts");
        QuantityType quantity1 = new QuantityType("urn:x-ogc:def:property:frequency", uom, 60.0);
        DataComponentPropertyType p1 = new DataComponentPropertyType("frequency", "urn:x-ogc:def:property:frequency", quantity1);
        params.add(p1);
        UomPropertyType uom2 = new UomPropertyType("m", null);
        QuantityType quantity2 = new QuantityType("urn:x-ogc:def:property:precision", uom2, 0.05);
        DataComponentPropertyType p2 = new DataComponentPropertyType("precision", "urn:x-ogc:def:property:precision", quantity2);
        params.add(p2);
        QuantityRange quantityRange = new QuantityRange(uom2, Arrays.asList(0.0, 10.0));
        DataComponentPropertyType p3 = new DataComponentPropertyType("validity", "urn:x-ogc:def:property:validity", quantityRange);
        params.add(p3);
        ParameterList paramList = new ParameterList(params);
        Parameters parameters = new Parameters(paramList);
        component.setParameters(parameters);

        component.setPosition(new Position("conductivitePosition", "piezometer#piezoPosition"));

        member.setProcess(sml100Factory.createComponent(component));
        SensorML expectedResult = new SensorML("1.0", Arrays.asList(member));

        assertEquals(result.getMember().size(), 1);
        assertTrue(result.getMember().get(0).getProcess() != null);
        assertTrue(result.getMember().get(0).getProcess().getValue() instanceof ComponentType);

        ComponentType resultProcess = (ComponentType) result.getMember().get(0).getProcess().getValue();

        assertEquals(resultProcess.getCapabilities(), component.getCapabilities());
        
        assertTrue(resultProcess.getContact().size() == 1);
        assertEquals(resultProcess.getContact().get(0).getContactList(), component.getContact().get(0).getContactList());
        assertEquals(resultProcess.getContact().get(0).getResponsibleParty().getContactInfo(), component.getContact().get(0).getResponsibleParty().getContactInfo());
        assertEquals(resultProcess.getContact().get(0).getResponsibleParty().getOrganizationName(), component.getContact().get(0).getResponsibleParty().getOrganizationName());
        assertEquals(resultProcess.getContact().get(0).getResponsibleParty(), component.getContact().get(0).getResponsibleParty());
        assertEquals(resultProcess.getContact().get(0), component.getContact().get(0));
        assertEquals(resultProcess.getContact(), component.getContact());

        assertTrue(resultProcess.getClassification().size() == 1);
        assertTrue(resultProcess.getClassification().get(0).getClassifierList().getClassifier().size() == 2);
        assertEquals(resultProcess.getClassification().get(0).getClassifierList().getClassifier().get(0).getTerm(), component.getClassification().get(0).getClassifierList().getClassifier().get(0).getTerm());
        assertEquals(resultProcess.getClassification().get(0).getClassifierList().getClassifier().get(0), component.getClassification().get(0).getClassifierList().getClassifier().get(0));
        assertEquals(resultProcess.getClassification().get(0).getClassifierList().getClassifier(), component.getClassification().get(0).getClassifierList().getClassifier());
        assertEquals(resultProcess.getClassification().get(0).getClassifierList(), component.getClassification().get(0).getClassifierList());
        assertEquals(resultProcess.getClassification().get(0), component.getClassification().get(0));
        assertEquals(resultProcess.getClassification(), component.getClassification());

        assertEquals(resultProcess.getIdentification(), component.getIdentification());

        assertEquals(resultProcess.getValidTime(), component.getValidTime());

        assertEquals(resultProcess.getParameters(), component.getParameters());

        assertEquals(resultProcess.getInputs().getInputList().getInput(), component.getInputs().getInputList().getInput());
        assertEquals(resultProcess.getInputs().getInputList(), component.getInputs().getInputList());
        assertEquals(resultProcess.getInputs(), component.getInputs());

        assertEquals(resultProcess.getOutputs(), component.getOutputs());

        assertEquals(resultProcess.getSMLLocation(), component.getSMLLocation());

        assertEquals(resultProcess.getPosition(), component.getPosition());

        assertEquals(resultProcess.getSpatialReferenceFrame(), component.getSpatialReferenceFrame());

        assertEquals(resultProcess.getDocumentation(), component.getDocumentation());

        

        assertEquals(resultProcess, component);

        assertEquals(expectedResult.getMember().get(0), result.getMember().get(0));
        assertEquals(expectedResult.getMember(), result.getMember());
        assertEquals(expectedResult, result);
    }

    /**
     * Test simple Record Marshalling.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void SystemUnmarshallMarshalingTest() throws Exception {

        InputStream is = Util.getResourceAsStream("org/constellation/sml/system.xml");
        Object unmarshalled = unmarshaller.unmarshal(is);
        if (unmarshalled instanceof JAXBElement) {
            unmarshalled = ((JAXBElement)unmarshalled).getValue();
        }

        assertTrue(unmarshalled instanceof SensorML);

        SensorML result = (SensorML) unmarshalled;

        SensorML.Member member = new SensorML.Member();
        member.setRole("urn:x-ogx:def:sensor:OGC:detector");

        SystemType system = new SystemType();

        List<JAXBElement<String>> kw = new ArrayList<JAXBElement<String>>();
        kw.add(sml100Factory.createKeywordsKeywordListKeyword("piezometer"));
        kw.add(sml100Factory.createKeywordsKeywordListKeyword("geosciences"));
        kw.add(sml100Factory.createKeywordsKeywordListKeyword("point d'eau"));
        Keywords keywords = new Keywords(new KeywordList("urn:x-brgm:def:gcmd:keywords", kw));
        system.setKeywords(keywords);

        Classifier cl1 = new Classifier("intendedApplication", new Term("eaux souterraines", "urn:x-ogc:def:classifier:OGC:application"));

        CodeSpacePropertyType cs = new CodeSpacePropertyType("urn:x-brgm:def:GeoPoint:bss");
        Classifier cl2 = new Classifier("sensorType", new Term(cs, "piezometer", "urn:sensor:classifier:sensorType"));
        
        CodeSpacePropertyType cs3 = new CodeSpacePropertyType("urn:x-sandre:def:mdo:identifier");
        Classifier cl3 = new Classifier("waterBody", new Term(cs3, "FR6221", "urn:sensor:classifier:references"));

        CodeSpacePropertyType cs4 = new CodeSpacePropertyType("urn:x-sandre:def:mdo:name");
        Classifier cl4 = new Classifier("waterBody", new Term(cs4, "Multicouche pliocene et alluvions quaternaires du Roussillon", "urn:sensor:classifier:references"));

        CodeSpacePropertyType cs5 = new CodeSpacePropertyType("urn:x-sandre:def:bdhrf:identifier");
        Classifier cl5 = new Classifier("hydrologicalEntity", new Term(cs5, "225", "urn:sensor:classifier:references"));

        CodeSpacePropertyType cs6 = new CodeSpacePropertyType("urn:x-sandre:def:bdhrf:name");
        Classifier cl6 = new Classifier("hydrologicalEntity", new Term(cs6, "Pliocene du Roussillon", "urn:sensor:classifier:references"));

        CodeSpacePropertyType cs7 = new CodeSpacePropertyType("urn:x-insee:def:commune:identifier");
        Classifier cl7 = new Classifier("commune", new Term(cs7, "66008", "urn:sensor:classifier:references"));

        CodeSpacePropertyType cs8 = new CodeSpacePropertyType("urn:x-insee:def:commune:name");
        Classifier cl8 = new Classifier("commune", new Term(cs8, "ARGELES SUR MER", "urn:sensor:classifier:references"));

        CodeSpacePropertyType cs9 = new CodeSpacePropertyType("urn:x-sandre:def:network:identifier");
        Classifier cl9 = new Classifier("network", new Term(cs9, "600000221", "urn:sensor:classifier:references"));

        CodeSpacePropertyType cs10 = new CodeSpacePropertyType("urn:x-sandre:def:network:identifier");
        Classifier cl10 = new Classifier("network", new Term(cs10, "600000025", "urn:sensor:classifier:references"));

        List<Classifier> cls = new ArrayList<Classifier>();
        cls.add(cl1);cls.add(cl2);cls.add(cl3);cls.add(cl4);cls.add(cl5);
        cls.add(cl6);cls.add(cl7);cls.add(cl8);cls.add(cl9);cls.add(cl10);

        ClassifierList claList = new ClassifierList(null, cls);
        Classification classification = new Classification(claList);
        system.setClassification(classification);

        List<Identifier> identifiers = new ArrayList<Identifier>();


        cs = new CodeSpacePropertyType("urn:x-brgm:def:samplingStation:bss");
        Identifier id1 = new Identifier("bssCode", new Term(cs, "10972X0137/PONT", "urn:x-ogc:def:identifier:OGC:modelNumber"));
        
        cs = new CodeSpacePropertyType("urn:x-brgm:def:sensorSystem:hydras");
        Identifier id2 = new Identifier("supervisorCode", new Term(cs, "00ARGLELES", "urn:x-ogc:def:identifier:OGC:modelNumber"));
        Identifier id3 = new Identifier("longName", new Term("ARGELES", "urn:x-ogc:def:identifier:OGC:longname"));

        identifiers.add(id1);
        identifiers.add(id2);
        identifiers.add(id3);
        IdentifierList identifierList = new IdentifierList(null, identifiers);
        Identification identification = new Identification(identifierList);
        system.setIdentification(identification);

        CapabilitiesSML capabilities = new CapabilitiesSML();
        List<DataComponentPropertyType> fields = new ArrayList<DataComponentPropertyType>();
        QuantityType quantity = new QuantityType("urn:x-ogc:def:property:temperature", new UomPropertyType(null, "urn:ogc:unit:degree:celsius"), 0.1);
        DataComponentPropertyType field1 = new DataComponentPropertyType("resolution", "urn:x-ogc:def:property:resolution", quantity);
        fields.add(field1);

        QuantityRange quantityR = new QuantityRange(new UomPropertyType(null, "urn:ogc:unit:percent"), Arrays.asList(-0.5, 0.5));
        quantityR.setDefinition("urn:x-ogc:def:property:absoluteAccuracy");
        DataComponentPropertyType field2 = new DataComponentPropertyType("accuracy", "urn:x-ogc:def:property:accuracy", quantityR);
        fields.add(field2);

        DataRecordType record = new DataRecordType("urn:x-ogc:def:property:measurementProperties", fields);
        record.setDescription("Toutes les informations sur les  contraintes sur les donnees");
        JAXBElement<? extends AbstractDataRecordType> jbRecord = swe100Factory.createDataRecord(record);
        capabilities.setAbstractDataRecord(jbRecord);
        system.setCapabilities(capabilities);

        AddressType address1 = new AddressType("SGR LRO-1039 Rue de Pinville-34000 Montpellier", "MONTPELLIER", null, null, null, "m.blaise@brgm.fr");
        ContactInfo contactInfo1 = new ContactInfo(null, address1);
        ResponsibleParty resp1 = new ResponsibleParty("BLAISE Marion (BRGM)", "BRGM", null, contactInfo1);
        Contact contact1 = new Contact("urn:x-ogc:def:role:producer", resp1);

        AddressType address2 = new AddressType("Hotel du Departement, B.P. 906, 66 906 Perpignan Cedex", "PERPIGNAN", null, null, null, null);
        ContactInfo contactInfo2 = new ContactInfo(null, address2);
        ResponsibleParty resp2 = new ResponsibleParty("ASSENS Martine (CG66)", "CONSEIL GENERAL DES PYRENEES ORIENTALES", null, contactInfo2);
        Contact contact2 = new Contact("urn:x-ogc:def:role:owner", resp2);

        system.setContact(Arrays.asList(contact1, contact2));

        IoComponentPropertyType io = new IoComponentPropertyType("level", new ObservableProperty("urn:x-ogc:def:phenomenon:OGC:level"));
        InputList inputList = new InputList(Arrays.asList(io));
        Inputs inputs = new Inputs(inputList);
        system.setInputs(inputs);


        fields = new ArrayList<DataComponentPropertyType>();
        TimeType time = new TimeType("urn:x-ogc:def:phenomenon:observationTime", new UomPropertyType(null, "urn:x-ogc:def:unit:ISO8601"));
        fields.add(new DataComponentPropertyType("time", null, time));

        QuantityType q = new QuantityType("urn:x-ogc:def:phenomenon:OGC:depth", new UomPropertyType("m", null), null);
        fields.add(new DataComponentPropertyType("depth", null, q));

        BooleanType b = new BooleanType("urn:x-ogc:def:phenomenon:BRGM:validity");
        fields.add(new DataComponentPropertyType("validity", null, b));

        DataRecordType outRecord = new DataRecordType(null, fields);

        IoComponentPropertyType io2 = new IoComponentPropertyType("piezoMeasurements", swe100Factory.createDataRecord(outRecord));
        OutputList outputList = new OutputList(Arrays.asList(io2));
        Outputs outputs = new Outputs(outputList);
        system.setOutputs(outputs);


        fields = new ArrayList<DataComponentPropertyType>();
        q = new QuantityType("urn:x-ogc:def:property:depth", new UomPropertyType(null, "urn:ogc:unit:m"), 166.0);
        fields.add(new DataComponentPropertyType("MaxDepth", null, q));
        DataRecordType charRecord = new DataRecordType(null, fields);

        List<DataComponentPropertyType> fields2 = new ArrayList<DataComponentPropertyType>();
        fields2.add(new DataComponentPropertyType("physicalProperties", null, swe100Factory.createDataRecord(charRecord)));

        DataRecordType ccharRecord = new DataRecordType("urn:x-ogc:def:property:physicalProperties", fields2);

        Characteristics characteristics = new Characteristics();
        characteristics.setAbstractDataRecord(swe100Factory.createDataRecord(ccharRecord));
        system.setCharacteristics(characteristics);
        


        DirectPositionType pos = new DirectPositionType("urn:ogc:crs:EPSG:27582", 2, Arrays.asList(65400.0, 1731368.0));
        PointType point = new PointType("STATION_LOCALISATION", pos);
        Location location = new Location(point);
        system.setSMLLocation(location);

        EngineeringDatumType engineeringDatum = new EngineeringDatumType("datum", "Sensor Datum", new CodeType("X, Y et Z sont orthogonal au regard d'un point de reference."));
        EngineeringDatumRefType usesEngineeringDatum = new EngineeringDatumRefType(engineeringDatum);
        EngineeringCRSType engineeringCRS = new EngineeringCRSType("STATION_FRAME", "Position absolue du capteur", 
                new CoordinateSystemRefType("urn:ogc:def:crs:ogc:1.0:xyzFrame"), usesEngineeringDatum);
        SpatialReferenceFrame spatialReferenceFrame = new SpatialReferenceFrame(engineeringCRS);
        system.setSpatialReferenceFrame(spatialReferenceFrame);

        Document doc = new Document("Fiche descriptive de la station", "text/html", Arrays.asList(new OnlineResource("http://ades.eaufrance.fr/FichePtEau.aspx?code=10972X0137/PONT")));
        Documentation documentation = new Documentation(doc);
        documentation.setRole("urn:ogc:role:description");
        system.setDocumentation(Arrays.asList(documentation));

        List<ComponentPropertyType> compos = new ArrayList<ComponentPropertyType>();
        compos.add(new ComponentPropertyType("Profondeur", "urn:x-ogc:def:sensor:detector", "capteur_"));
        ComponentList componentList = new ComponentList(compos);
        Components components = new Components(componentList);
        system.setComponents(components);

        List<CoordinateType> coordinates = new ArrayList<CoordinateType>();
        QuantityType xQuant = new QuantityType("urn:ogc:def:phenomenon:distance", new UomPropertyType("m", null), 0.0);
        xQuant.setAxisID("X");
        CoordinateType xcoord = new CoordinateType("x", xQuant);

        QuantityType yQuant = new QuantityType("urn:ogc:def:phenomenon:distance", new UomPropertyType("m", null), 0.0);
        yQuant.setAxisID("Y");
        CoordinateType ycoord = new CoordinateType("y", yQuant);

        QuantityType zQuant = new QuantityType("urn:ogc:def:phenomenon:distance", new UomPropertyType("m", null), 0.0);
        zQuant.setAxisID("Z");
        CoordinateType zcoord = new CoordinateType("z", zQuant);

        coordinates.add(xcoord);
        coordinates.add(ycoord);
        coordinates.add(zcoord);

        VectorType vect = new VectorType("urn:ogc:def:phenomenon:location", coordinates);
        VectorPropertyType vectP = new VectorPropertyType(vect);
        PositionType Sposition = new PositionType("#REFERENCE_POINT", "#PIEZOMETER_FRAME", vectP, null);
        Position position = new Position("piezoPosition", Sposition);
        PositionList positionList = new PositionList(null, Arrays.asList(position));
        Positions positions = new Positions(positionList);
        system.setPositions(positions);

        TemporalReferenceFrame temporalReferenceFrame = new TemporalReferenceFrame(new TemporalCRSType("temporalReference", 
                                                                                                        null, null,
                                                                                                        "calendrier gregorien en heure d'ete",
                                                                                                        new TemporalCSRefType("urn:x-brgm:temporalCS:gregorian"),
                                                                                                        new TemporalDatumRefType("urn:x-brgm:temporalDatum:UniversalTime")));
        system.setTemporalReferenceFrame(temporalReferenceFrame);

        List<Connection> connecL = new ArrayList<Connection>();
        connecL.add(new Connection("inputTolevel", new Link(null, new Source("this/inputs/level"), new Destination("piezometer/inputs/level"))));
        connecL.add(new Connection("depthToOutput", new Link(null, new Source("piezometer/outputs/depth"), new Destination("this/outputs/piezoMeasurements/depth"))));
        ConnectionList connectionList = new ConnectionList(connecL);
        Connections connections = new Connections(connectionList);
        system.setConnections(connections);

        
        LayerPropertyType applicationLayer = new LayerPropertyType(new Category("urn:ogc:def:protocol:applicationLink", "urn:x-brgm:def:protocol:hydrasIRIS"));
        LayerPropertyType dataLinkLayer    = new LayerPropertyType(new Category("urn:ogc:def:protocol:dataLink", "urn:x-brgm:def:dataLink:RTC"));
        InterfaceDefinition definition = new InterfaceDefinition(null, applicationLayer, dataLinkLayer);
        Interface i1 = new Interface("RS-232", definition);
        List<Interface> interfaceL = new ArrayList<Interface>();
        interfaceL.add(i1);
        InterfaceList interfaceList = new InterfaceList(null, interfaceL);
        Interfaces interfaces = new Interfaces(interfaceList);
        system.setInterfaces(interfaces);

        Rights rights = new Rights(true, true, new Documentation(new Document("Donnees sous copyright du BRGM")));
        LegalConstraint legalConstraint = new LegalConstraint(rights);
        system.setLegalConstraint(legalConstraint);

        system.setDescription("information about the piezometer");

        member.setProcess(sml100Factory.createSystem(system));
        SensorML expectedResult = new SensorML("1.0", Arrays.asList(member));

        assertEquals(result.getMember().size(), 1);
        assertTrue(result.getMember().get(0).getProcess() != null);
        assertTrue(result.getMember().get(0).getProcess().getValue() instanceof SystemType);

        SystemType resultProcess = (SystemType) result.getMember().get(0).getProcess().getValue();

        assertEquals(resultProcess.getCapabilities().size(), system.getCapabilities().size());
        assertTrue(resultProcess.getCapabilities().get(0).getAbstractDataRecord().getValue() instanceof DataRecordType);
        DataRecordType resultRecord = (DataRecordType) resultProcess.getCapabilities().get(0).getAbstractDataRecord().getValue();
        DataRecordType expRecord    = (DataRecordType) system.getCapabilities().get(0).getAbstractDataRecord().getValue();

        assertEquals(resultRecord.getField().get(0), expRecord.getField().get(0));
        assertEquals(resultRecord.getField().get(1), expRecord.getField().get(1));
        assertEquals(resultRecord.getField(), expRecord.getField());

        assertEquals(resultProcess.getCapabilities().get(0).getAbstractDataRecord().getValue(), system.getCapabilities().get(0).getAbstractDataRecord().getValue());
        assertEquals(resultProcess.getCapabilities().get(0), system.getCapabilities().get(0));
        assertEquals(resultProcess.getCapabilities(), system.getCapabilities());

        assertTrue(resultProcess.getContact().size() == 2);
        assertEquals(resultProcess.getContact().get(0).getContactList(), system.getContact().get(0).getContactList());
        assertEquals(resultProcess.getContact().get(0).getResponsibleParty().getContactInfo(), system.getContact().get(0).getResponsibleParty().getContactInfo());
        assertEquals(resultProcess.getContact().get(0).getResponsibleParty().getOrganizationName(), system.getContact().get(0).getResponsibleParty().getOrganizationName());
        assertEquals(resultProcess.getContact().get(0).getResponsibleParty(), system.getContact().get(0).getResponsibleParty());
        assertEquals(resultProcess.getContact().get(0), system.getContact().get(0));
        assertEquals(resultProcess.getContact(), system.getContact());

        assertTrue(resultProcess.getClassification().size() == 1);
        assertTrue(resultProcess.getClassification().get(0).getClassifierList().getClassifier().size() == 10);
        assertEquals(resultProcess.getClassification().get(0).getClassifierList().getClassifier().get(0).getTerm(), system.getClassification().get(0).getClassifierList().getClassifier().get(0).getTerm());
        assertEquals(resultProcess.getClassification().get(0).getClassifierList().getClassifier().get(0), system.getClassification().get(0).getClassifierList().getClassifier().get(0));
        assertEquals(resultProcess.getClassification().get(0).getClassifierList().getClassifier(), system.getClassification().get(0).getClassifierList().getClassifier());
        assertEquals(resultProcess.getClassification().get(0).getClassifierList(), system.getClassification().get(0).getClassifierList());
        assertEquals(resultProcess.getClassification().get(0), system.getClassification().get(0));
        assertEquals(resultProcess.getClassification(), system.getClassification());

        assertEquals(resultProcess.getIdentification(), system.getIdentification());

        assertEquals(resultProcess.getValidTime(), system.getValidTime());

        assertEquals(resultProcess.getParameters(), system.getParameters());

        assertEquals(resultProcess.getInputs().getInputList().getInput(), system.getInputs().getInputList().getInput());
        assertEquals(resultProcess.getInputs().getInputList(), system.getInputs().getInputList());
        assertEquals(resultProcess.getInputs(), system.getInputs());

        assertEquals(resultProcess.getOutputs(), system.getOutputs());

        assertEquals(resultProcess.getSMLLocation(), system.getSMLLocation());

        assertEquals(resultProcess.getPosition(), system.getPosition());

        assertEquals(resultProcess.getSpatialReferenceFrame(), system.getSpatialReferenceFrame());

        assertEquals(resultProcess.getDocumentation(), system.getDocumentation());

        assertEquals(resultProcess.getCharacteristics(), system.getCharacteristics());

        assertEquals(resultProcess.getComponents(), system.getComponents());

        assertEquals(resultProcess.getPositions(), system.getPositions());

        assertEquals(resultProcess.getTemporalReferenceFrame(), system.getTemporalReferenceFrame());

        assertEquals(resultProcess.getConnections(), system.getConnections());

        assertEquals(resultProcess.getInterfaces(), system.getInterfaces());

        assertEquals(resultProcess.getLegalConstraint(), system.getLegalConstraint());

        assertEquals(resultProcess, system);
        assertEquals(expectedResult.getMember().get(0), result.getMember().get(0));
        assertEquals(expectedResult.getMember(), result.getMember());
        assertEquals(expectedResult, result);
    }

}
