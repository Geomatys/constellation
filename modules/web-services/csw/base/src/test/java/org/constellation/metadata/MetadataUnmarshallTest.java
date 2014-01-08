/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
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

package org.constellation.metadata;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Logger;
import javax.measure.unit.Unit;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

// Junit dependencies
import org.junit.*;
import static org.junit.Assert.*;

// Constellation dependencies
import org.constellation.util.Util;

// geotoolkit dependencies
import org.geotoolkit.csw.xml.CSWMarshallerPool;
import org.geotoolkit.gml.xml.v311.TimePeriodType;
import org.apache.sis.internal.jaxb.metadata.ReferenceSystemMetadata;
import org.apache.sis.internal.jaxb.gmx.Anchor;
import org.apache.sis.metadata.iso.DefaultExtendedElementInformation;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.metadata.iso.DefaultMetadataExtensionInformation;
import org.apache.sis.metadata.iso.ImmutableIdentifier;
import org.apache.sis.metadata.iso.citation.DefaultAddress;
import org.apache.sis.metadata.iso.citation.DefaultCitationDate;
import org.apache.sis.metadata.iso.citation.DefaultCitation;
import org.apache.sis.metadata.iso.citation.DefaultContact;
import org.apache.sis.metadata.iso.citation.DefaultOnlineResource;
import org.apache.sis.metadata.iso.citation.DefaultResponsibleParty;
import org.apache.sis.metadata.iso.citation.DefaultTelephone;
import org.apache.sis.metadata.iso.constraint.DefaultLegalConstraints;
import org.apache.sis.metadata.iso.content.DefaultImageDescription;
import org.apache.sis.metadata.iso.distribution.DefaultDigitalTransferOptions;
import org.apache.sis.metadata.iso.distribution.DefaultDistribution;
import org.apache.sis.metadata.iso.distribution.DefaultDistributor;
import org.apache.sis.metadata.iso.distribution.DefaultFormat;
import org.apache.sis.metadata.iso.extent.DefaultExtent;
import org.apache.sis.metadata.iso.extent.DefaultGeographicBoundingBox;
import org.apache.sis.metadata.iso.extent.DefaultTemporalExtent;
import org.apache.sis.metadata.iso.extent.DefaultVerticalExtent;
import org.apache.sis.metadata.iso.identification.DefaultAggregateInformation;
import org.apache.sis.metadata.iso.identification.DefaultBrowseGraphic;
import org.apache.sis.metadata.iso.identification.DefaultDataIdentification;
import org.apache.sis.metadata.iso.identification.DefaultKeywords;
import org.apache.sis.metadata.iso.spatial.DefaultGeometricObjects;
import org.apache.sis.metadata.iso.spatial.DefaultVectorSpatialRepresentation;
import org.apache.sis.referencing.AbstractIdentifiedObject;
import org.apache.sis.referencing.crs.DefaultVerticalCRS;
import org.apache.sis.referencing.cs.DefaultCoordinateSystemAxis;
import org.apache.sis.referencing.cs.DefaultVerticalCS;
import org.apache.sis.referencing.datum.DefaultVerticalDatum;
import org.apache.sis.test.XMLComparator;
import org.geotoolkit.temporal.object.TemporalUtilities;
import org.apache.sis.util.iso.SimpleInternationalString;

// GeoAPI dependencies
import org.apache.sis.util.logging.Logging;
import org.geotoolkit.xml.AnchoredMarshallerPool;
import org.apache.sis.xml.MarshallerPool;
import org.apache.sis.xml.XML;
import org.constellation.test.utils.MetadataUtilities;
import org.opengis.metadata.Datatype;
import org.opengis.metadata.ExtendedElementInformation;
import org.opengis.metadata.citation.CitationDate;
import org.opengis.metadata.citation.DateType;
import org.opengis.metadata.citation.OnLineFunction;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.citation.Role;
import org.opengis.metadata.constraint.Restriction;
import org.opengis.metadata.distribution.Format;
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.extent.GeographicExtent;
import org.opengis.metadata.identification.AssociationType;
import org.opengis.metadata.identification.CharacterSet;
import org.opengis.metadata.identification.DataIdentification;
import org.opengis.metadata.identification.Identification;
import org.opengis.metadata.identification.InitiativeType;
import org.opengis.metadata.identification.KeywordType;
import org.opengis.metadata.identification.Keywords;
import org.opengis.metadata.identification.TopicCategory;
import org.opengis.metadata.maintenance.ScopeCode;
import org.opengis.metadata.spatial.GeometricObjectType;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.datum.VerticalDatumType;
import org.opengis.temporal.Period;
import org.opengis.temporal.TemporalPrimitive;
import org.opengis.util.InternationalString;

/**
 *
 * @author Guilhem Legal
 */
public class MetadataUnmarshallTest {
    private static MarshallerPool testPool;
    private static Unmarshaller unmarshaller;
    private static Marshaller marshaller;
    private static final Logger LOGGER = Logging.getLogger(MetadataUnmarshallTest.class);
    
    @BeforeClass
    public static void setUp() throws JAXBException, URISyntaxException {
        testPool = CSWMarshallerPool.getInstance();
        CSWworkerTest.fillPoolAnchor((AnchoredMarshallerPool) testPool);
    }

    @After
    public void releaseMarshallers() {
        if (marshaller != null) {
            testPool.recycle(marshaller);
            marshaller = null;
        }
        if (unmarshaller != null) {
            testPool.recycle(unmarshaller);
            unmarshaller = null;
        }
    }

    /**
     * Tests the unmarshall of a metadata.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void unmarshallTest() throws Exception {

        unmarshaller = testPool.acquireUnmarshaller();
        Object obj = unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta1.xml"));
        
        assertTrue(obj instanceof DefaultMetadata);
        DefaultMetadata result = (DefaultMetadata) obj;

        DefaultMetadata expResult = new DefaultMetadata();

        /*
         * static part
         */
        expResult.setFileIdentifier("42292_5p_19900609195600");
        expResult.setLanguage(Locale.ENGLISH);
        expResult.setCharacterSet(CharacterSet.UTF_8);
        Set set = new HashSet();
        set.add(ScopeCode.DATASET);
        expResult.setHierarchyLevels(set);
        set = new HashSet();
        set.add("Common Data Index record");
        expResult.setHierarchyLevelNames(set);
        /*
         * contact parts
         */
        DefaultResponsibleParty author = new DefaultResponsibleParty(Role.AUTHOR);
        author.setOrganisationName(new SimpleInternationalString("IFREMER / IDM/SISMER"));
        DefaultContact contact = new DefaultContact();
        DefaultTelephone t = new DefaultTelephone();
        set = new HashSet();
        set.add("+33 (0)2 98.22.49.16");
        t.setVoices(set);
        set = new HashSet();
        set.add("+33 (0)2 98.22.46.44");
        t.setFacsimiles(set);
        contact.setPhone(t);
        DefaultAddress add = new DefaultAddress();
        set = new HashSet();
        set.add("Centre IFREMER de Brest BP 70");
        add.setDeliveryPoints(set);
        add.setCity(new SimpleInternationalString("PLOUZANE"));
        add.setPostalCode("29280");
        add.setCountry(new Anchor(URI.create("SDN:C320:2:FR"), "France"));
        set = new HashSet();
        set.add("sismer@ifremer.fr");
        add.setElectronicMailAddresses(set);
        contact.setAddress(add);
        DefaultOnlineResource o = new DefaultOnlineResource(new URI("http://www.ifremer.fr/sismer/"));
        o.setProtocol("http");
        contact.setOnlineResource(o);
        author.setContactInfo(contact);
        set = new HashSet();
        set.add(author);
        expResult.setContacts(set);

        /*
         * creation date
         */
        expResult.setDateStamp(TemporalUtilities.parseDate("2009-01-01T06:00:00+0200"));

        /*
         * Spatial representation info
         */
        DefaultVectorSpatialRepresentation spatialRep = new DefaultVectorSpatialRepresentation();
        DefaultGeometricObjects geoObj = new DefaultGeometricObjects(GeometricObjectType.valueOf("POINT"));
        set = new HashSet();
        set.add(geoObj);
        spatialRep.setGeometricObjects(set);

        set = new HashSet();
        set.add(spatialRep);
        expResult.setSpatialRepresentationInfo(set);

        /*
         * Reference system info
         */
        String code = "EPSG:4326";

        DefaultCitation RScitation = new DefaultCitation();

        RScitation.setTitle(new SimpleInternationalString("SeaDataNet geographic co-ordinate reference frames"));
        set = new HashSet();
        set.add(new SimpleInternationalString("L101"));
        RScitation.setAlternateTitles(set);
        set = new HashSet();
        set.add(new ImmutableIdentifier(null, null, "http://www.seadatanet.org/urnurl/"));
        RScitation.setIdentifiers(set);
        RScitation.setEdition(new Anchor(new URI("SDN:C371:1:2"),"2"));

        ImmutableIdentifier Nidentifier = new ImmutableIdentifier(RScitation, "L101", code);
        ReferenceSystemMetadata rs = new ReferenceSystemMetadata(Nidentifier);
        set = new HashSet();
        set.add(rs);
        expResult.setReferenceSystemInfo(set);


        /*
         * extension information
         */
        DefaultMetadataExtensionInformation extensionInfo = new DefaultMetadataExtensionInformation();
        Set<ExtendedElementInformation> elements = new HashSet<>();

        //EDMO
        ExtendedElementInformation edmo =  createExtensionInfo("SDN:EDMO::");
        elements.add(edmo);

        /*L021
        ExtendedElementInformation L021 = createExtensionInfo("SDN:L021:1:");
        elements.add(L021);

        //L031
        ExtendedElementInformation L031 = createExtensionInfo("SDN:L031:2:");
        elements.add(L031);

        //L071
        ExtendedElementInformation L071 = createExtensionInfo("SDN:L071:1:");
        elements.add(L071);

        //L081
        ExtendedElementInformation L081 = createExtensionInfo("SDN:L081:1:");
        elements.add(L081);

        //L231
        ExtendedElementInformation L231 = createExtensionInfo("SDN:L231:3:");
        elements.add(L231);

        //L241
        ExtendedElementInformation L241 = createExtensionInfo("SDN:L241:1:");
        elements.add(L241);*/

        extensionInfo.setExtendedElementInformation(elements);

        set = new HashSet();
        set.add(extensionInfo);
        expResult.setMetadataExtensionInfo(set);

        /*
         * Data indentification
         */
        DefaultDataIdentification dataIdentification = new DefaultDataIdentification();

        DefaultCitation citation = new DefaultCitation();
        citation.setTitle(new SimpleInternationalString("90008411.ctd"));
        set = new HashSet();
        set.add(new SimpleInternationalString("42292_5p_19900609195600"));
        citation.setAlternateTitles(set);

        DefaultCitationDate revisionDate = new DefaultCitationDate();
        revisionDate.setDateType(DateType.REVISION);
        Date d = TemporalUtilities.parseDate("1990-06-05T00:00:00+0200");
        revisionDate.setDate(d);
        DefaultCitationDate creationDate = new DefaultCitationDate();
        creationDate.setDateType(DateType.CREATION);
        Date dc = TemporalUtilities.parseDate("1979-08-03T00:00:00+0200");
        creationDate.setDate(dc);
        List<CitationDate> dates = new ArrayList<>();
        dates.add(revisionDate);
        dates.add(creationDate);
        citation.setDates(dates);


        Set<ResponsibleParty> originators = new HashSet<>();
        DefaultResponsibleParty originator = new DefaultResponsibleParty(Role.ORIGINATOR);
        originator.setOrganisationName(new SimpleInternationalString("UNIVERSITE DE LA MEDITERRANNEE (U2) / COM - LAB. OCEANOG. BIOGEOCHIMIE - LUMINY"));
        contact = new DefaultContact();
        t = new DefaultTelephone();
        set = new HashSet();
        set.add("+33(0)4 91 82 91 15");
        t.setVoices(set);
        set = new HashSet();
        set.add("+33(0)4 91.82.65.48");
        t.setFacsimiles(set);
        contact.setPhone(t);
        add = new DefaultAddress();
        set = new HashSet();
        set.add("UFR Centre Oceanologique de Marseille Campus de Luminy Case 901");
        add.setDeliveryPoints(set);
        add.setCity(new SimpleInternationalString("Marseille cedex 9"));
        add.setPostalCode("13288");
        add.setCountry(new Anchor(URI.create("SDN:C320:2:FR"), "France"));
        set = new HashSet();
        add.setElectronicMailAddresses(set);
        contact.setAddress(add);
        o = new DefaultOnlineResource(new URI("http://www.com.univ-mrs.fr/LOB/"));
        contact.setOnlineResource(o);
        o.setProtocol("http");
        originator.setContactInfo(contact);
        originators.add(originator);
        citation.setCitedResponsibleParties(originators);

        dataIdentification.setCitation(citation);


        dataIdentification.setAbstract(new SimpleInternationalString("Donnees CTD NEDIPROD VI 120"));

        DefaultResponsibleParty custodian = new DefaultResponsibleParty(Role.CUSTODIAN);
        custodian.setOrganisationName(new SimpleInternationalString("IFREMER / IDM/SISMER"));
        contact = new DefaultContact();
        t = new DefaultTelephone();
        set = new HashSet();
        set.add("+33 (0)2 98.22.49.16");
        t.setVoices(set);
        set = new HashSet();
        set.add("+33 (0)2 98.22.46.44");
        t.setFacsimiles(set);
        contact.setPhone(t);
        add = new DefaultAddress();
        set = new HashSet();
        set.add("Centre IFREMER de Brest BP 70");
        add.setDeliveryPoints(set);
        add.setCity(new SimpleInternationalString("PLOUZANE"));
        add.setPostalCode("29280");
        add.setCountry(new Anchor(URI.create("SDN:C320:2:FR"), "France"));
        set = new HashSet();
        set.add("sismer@ifremer.fr");
        add.setElectronicMailAddresses(set);
        contact.setAddress(add);
        o = new DefaultOnlineResource(new URI("http://www.ifremer.fr/sismer/"));
        o.setProtocol("http");
        contact.setOnlineResource(o);
        custodian.setContactInfo(contact);
        set = new HashSet();
        set.add(custodian);
        dataIdentification.setPointOfContacts(set);

        /*
         * Browse graphic
         */
        DefaultBrowseGraphic go = new DefaultBrowseGraphic(URI.create("http://fr.wikipedia.org/wiki/Fichier:Death_skull.svg"));
        go.setFileDescription(new SimpleInternationalString("thumbnail"));
        dataIdentification.setGraphicOverviews(Arrays.asList(go));
        /*
         * keywords
         */
        Set<Keywords> keywords = new HashSet<>();

        //parameter
        Set<String> keys = new HashSet<>();
        keys.add("Transmittance and attenuance of the water column");
        Keywords keyword = createKeyword(keys, "parameter", "BODC Parameter Discovery Vocabulary", "P021", "2008-11-26T01:00:00+0200", "35");
        keywords.add(keyword);

        dataIdentification.setDescriptiveKeywords(keywords);

        /*
         * resource constraint
         */
        DefaultLegalConstraints constraint = new DefaultLegalConstraints();
        Set<Restriction> restrictions  = new HashSet<>();
        restrictions.add(Restriction.LICENSE);

        constraint.setAccessConstraints(restrictions);
        set = new HashSet();
        set.add(constraint);
        dataIdentification.setResourceConstraints(set);

        /*
         * Aggregate info
         */
        Set<DefaultAggregateInformation> aggregateInfos = new HashSet<>();

        //cruise
        DefaultAggregateInformation aggregateInfo = new DefaultAggregateInformation();
        citation = new DefaultCitation();
        citation.setTitle(new SimpleInternationalString("MEDIPROD VI"));
        set = new HashSet();
        set.add(new SimpleInternationalString("90008411"));
        citation.setAlternateTitles(set);
        revisionDate = new DefaultCitationDate();
        revisionDate.setDateType(DateType.REVISION);
        d = TemporalUtilities.parseDate("1990-06-05T00:00:00+0200");
        revisionDate.setDate(d);
        set = new HashSet();
        set.add(revisionDate);
        citation.setDates(set);
        aggregateInfo.setAggregateDataSetName(citation);
        aggregateInfo.setInitiativeType(InitiativeType.CAMPAIGN);
        aggregateInfo.setAssociationType(AssociationType.LARGER_WORD_CITATION);
        aggregateInfos.add(aggregateInfo);

        dataIdentification.setAggregationInfo(aggregateInfos);

        //static part
        set = new HashSet();
        set.add(Locale.ENGLISH);
        dataIdentification.setLanguages(set);
        set = new HashSet();
        set.add(TopicCategory.OCEANS);
        dataIdentification.setTopicCategories(set);

        /*
         * Extent
         */
        DefaultExtent extent = new DefaultExtent();

        // geographic extent
        extent.setGeographicElements(createGeographicExtent("1.1667", "1.1667", "36.6", "36.6"));

        //temporal extent
        DefaultTemporalExtent tempExtent = new DefaultTemporalExtent();

        TimePeriodType period = new TimePeriodType(null, "1990-06-05", "1990-07-02");
        tempExtent.setExtent(period);

        set = new HashSet();
        set.add(tempExtent);
        extent.setTemporalElements(set);



        //vertical extent
        DefaultVerticalExtent vertExtent = new DefaultVerticalExtent();
        String miv = null;
        String mav = null;

        if (miv != null) {
            vertExtent.setMinimumValue(Double.parseDouble(miv));
        }
        if (mav != null) {
            vertExtent.setMaximumValue(Double.parseDouble(mav));
        }

        // vertical datum
        ImmutableIdentifier datumID = new ImmutableIdentifier(null, null, "D28");

        Map<String, Object> prop = new HashMap<>();
        prop.put(DefaultVerticalDatum.NAME_KEY, datumID);
        DefaultVerticalDatum datum = new DefaultVerticalDatum(prop, VerticalDatumType.OTHER_SURFACE);


        // vertical coordinate system  TODO var 32 uom?
        HashMap<String, Object> propCoo = new HashMap<>();


        ImmutableIdentifier m = new ImmutableIdentifier(null, null, "meters");
        propCoo.put(DefaultCoordinateSystemAxis.NAME_KEY, m);
//        propCoo.put(DefaultCoordinateSystemAxis.ALIAS_KEY, "");
        DefaultCoordinateSystemAxis axis = new DefaultCoordinateSystemAxis(propCoo, "meters", AxisDirection.DOWN, Unit.valueOf("m"));

        HashMap<String,Object> csProp = new HashMap<>();
        ImmutableIdentifier i = new ImmutableIdentifier(null, null, "meters");
        csProp.put(DefaultVerticalCRS.NAME_KEY, i);
        DefaultVerticalCS cs = new DefaultVerticalCS(csProp, axis);

        prop = new HashMap<>();
        ImmutableIdentifier idVert = new ImmutableIdentifier(null, null, "idvertCRS");
        prop.put(DefaultVerticalCRS.NAME_KEY, idVert);
        prop.put(DefaultVerticalCRS.SCOPE_KEY, null);
        //prop.put(DefaultVerticalCRS.ALIAS_KEY, DefaultCoordinateSystemAxis.UNDEFINED.getAlias());
        DefaultVerticalCRS vcrs = new DefaultVerticalCRS(prop, datum, cs);


        // TODO vertical limit? var 35
        vertExtent.setVerticalCRS(vcrs);

        set = new HashSet();
        set.add(vertExtent);
        extent.setVerticalElements(set);

        set = new HashSet();
        set.add(extent);
        dataIdentification.setExtents(set);

        set = new HashSet();
        set.add(dataIdentification);
        expResult.setIdentificationInfo(set);

        /*
         * Content info
         */
        DefaultImageDescription contentInfo = new DefaultImageDescription();
        contentInfo.setCloudCoverPercentage(50.0);
        expResult.setContentInfo(Arrays.asList(contentInfo));
        
        
        /*
         * Distribution info
         */
        DefaultDistribution distributionInfo = new DefaultDistribution();

        //distributor
        DefaultDistributor distributor       = new DefaultDistributor();

        DefaultResponsibleParty distributorContact = new DefaultResponsibleParty(Role.DISTRIBUTOR);
        distributorContact.setOrganisationName(new SimpleInternationalString("IFREMER / IDM/SISMER"));
        contact = new DefaultContact();
        t = new DefaultTelephone();
        set = new HashSet();
        set.add("+33 (0)2 98.22.49.16");
        t.setVoices(set);
        set = new HashSet();
        set.add("+33 (0)2 98.22.46.44");
        t.setFacsimiles(set);
        contact.setPhone(t);
        add = new DefaultAddress();
        set = new HashSet();
        set.add("Centre IFREMER de Brest BP 70");
        add.setDeliveryPoints(set);
        add.setCity(new SimpleInternationalString("PLOUZANE"));
        add.setPostalCode("29280");
        add.setCountry(new Anchor(URI.create("SDN:C320:2:FR"), "France"));
        set = new HashSet();
        set.add("sismer@ifremer.fr");
        add.setElectronicMailAddresses(set);
        contact.setAddress(add);
        o = new DefaultOnlineResource(new URI("http://www.ifremer.fr/sismer/"));
        o.setProtocol("http");
        contact.setOnlineResource(o);
        distributorContact.setContactInfo(contact);

        distributor.setDistributorContact(distributorContact);
        set = new HashSet();
        set.add(distributor);
        distributionInfo.setDistributors(set);

        //format
        Set<Format> formats  = new HashSet<>();

        DefaultFormat format = new DefaultFormat();
        String name = "MEDATLAS ASCII";
        format.setName(new Anchor(new URI("SDN:L241:1:MEDATLAS"),name));
        format.setVersion(new SimpleInternationalString("1.0"));
        formats.add(format);

        distributionInfo.setDistributionFormats(formats);

        //transfert options
        DefaultDigitalTransferOptions digiTrans = new DefaultDigitalTransferOptions();

        digiTrans.setTransferSize(2.431640625);

        DefaultOnlineResource onlines = new DefaultOnlineResource();

        String uri = "http://www.ifremer.fr/sismerData/jsp/visualisationMetadata3.jsp?langue=EN&pageOrigine=CS&cle1=42292_1&cle2=CTDF02";
        onlines.setLinkage(new URI(uri));

        onlines.setDescription(new SimpleInternationalString("CTDF02"));
        onlines.setFunction(OnLineFunction.DOWNLOAD);
        onlines.setProtocol("http");
        set = new HashSet();
        set.add(onlines);
        digiTrans.setOnLines(set);

        set = new HashSet();
        set.add(digiTrans);
        distributionInfo.setTransferOptions(set);

        expResult.setDistributionInfo(distributionInfo);

        MetadataUtilities.metadataEquals(expResult, result);

        String xml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + '\n' +
        "<gmd:MD_Metadata xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gco=\"http://www.isotc211.org/2005/gco\" xmlns:gml=\"http://www.opengis.net/gml\">" + '\n' +
        "   <gmd:identificationInfo>" + '\n' +
	"	<gmd:MD_DataIdentification>" + '\n' +
	"		<gmd:extent>" + '\n' +
	"			<gmd:EX_Extent>" + '\n' +
	"				<gmd:description>" + '\n' +
	"					<gco:CharacterString> vertical : Ocean surface, geographical : Global scale, temporal : Near real time, </gco:CharacterString>" + '\n' +
	"				</gmd:description>" + '\n' +
	"				<gmd:geographicElement>" + '\n' +
	"					<gmd:EX_BoundingPolygon>" + '\n' +
	"						<gmd:polygon>" + '\n' +
	"							<gml:LineString gml:id=\"ls1\"> " + '\n' +
	"								<gml:identifier codeSpace=\"#\"> MED</gml:identifier>" + '\n' +
	"								<gml:name>MED</gml:name>" + '\n' +
	"								<gml:coordinates>35.47,-5.54 36.15,-5.54 46,5 46,15 40.25,26.41 38,38 30,35 29,19,35.47,-5.54</gml:coordinates>" + '\n' +
	"							</gml:LineString>" + '\n' +
	"						</gmd:polygon>" + '\n' +
	"					</gmd:EX_BoundingPolygon>" + '\n' +
	"				</gmd:geographicElement>" + '\n' +
	"				<gmd:temporalElement>" + '\n' +
	"					<gmd:EX_TemporalExtent>" + '\n' +
	"						<gmd:extent>" + '\n' +
	"							<gml:TimePeriod gml:id=\"tp1\">" + '\n' +
	"								<gml:begin>" + '\n' +
	"									<gml:TimeInstant gml:id=\"ti1\">" + '\n' +
	"										<gml:timePosition>2009-06-01</gml:timePosition>" + '\n' +
	"									</gml:TimeInstant>" + '\n' +
	"								</gml:begin>" + '\n' +
	"								<gml:end/>" + '\n' +
	"							</gml:TimePeriod>" + '\n' +
	"						</gmd:extent>" + '\n' +
	"					</gmd:EX_TemporalExtent>" + '\n' +
	"				</gmd:temporalElement>" + '\n' +
	"			</gmd:EX_Extent>" + '\n' +
	"		</gmd:extent>" + '\n' +
	"		<gmd:extent>" + '\n' +
	"			<gmd:EX_Extent>" + '\n' +
	"				<gmd:description><gco:CharacterString>delta time</gco:CharacterString></gmd:description>" + '\n' +
	"				<gmd:temporalElement>" + '\n' +
	"					<gmd:EX_TemporalExtent>" + '\n' +
	"						<gmd:extent>" + '\n' +
	"							<gml:TimePeriod gml:id=\"tp2\">" + '\n' +
	"								<gml:begin>" + '\n' +
	"									<gml:TimeInstant gml:id=\"ti4\">" + '\n' +
	"										<gml:timePosition>2009-01-26T12:21:45.750+01:00</gml:timePosition>" + '\n' +
	"									</gml:TimeInstant>" + '\n' +
	"								</gml:begin>" + '\n' +
	"								<gml:end>" + '\n' +
	"									<gml:TimeInstant gml:id=\"ti5\">" + '\n' +
	"										<gml:timePosition>2009-01-27T12:21:45.750+01:00</gml:timePosition>" + '\n' +
	"									</gml:TimeInstant>" + '\n' +
	"								</gml:end>" + '\n' +
	"							</gml:TimePeriod>" + '\n' +
	"						</gmd:extent>" + '\n' +
	"					</gmd:EX_TemporalExtent>" + '\n' +
	"				</gmd:temporalElement>" + '\n' +
	"			</gmd:EX_Extent>" + '\n' +
	"		</gmd:extent>" + '\n' +
	"	</gmd:MD_DataIdentification>" + '\n' +
	"</gmd:identificationInfo>" + '\n' +
        "</gmd:MD_Metadata>";

        StringReader sr = new StringReader(xml);
        obj = unmarshaller.unmarshal(sr);

        assertTrue(obj instanceof DefaultMetadata);
        DefaultMetadata meta = (DefaultMetadata) obj;

        assertTrue(meta.getIdentificationInfo().size() == 1);
        Identification ident = meta.getIdentificationInfo().iterator().next();
        assertTrue(ident instanceof DataIdentification);
        
        DataIdentification dataIdent = (DataIdentification) ident;

        assertTrue(dataIdent.getExtents().size() == 2);
        Iterator<? extends Extent> it = dataIdent.getExtents().iterator();
        Extent extent1 = it.next();
        Extent extent2 = it.next();

        assertTrue(extent2.getTemporalElements().size() == 1);
        TemporalPrimitive tmpObj = extent2.getTemporalElements().iterator().next().getExtent();

        assertTrue(tmpObj instanceof Period);


        obj = unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta7.xml"));

        assertTrue(obj instanceof DefaultMetadata);
        result = (DefaultMetadata) obj;

    }

    protected ExtendedElementInformation createExtensionInfo(String name) {
        DefaultExtendedElementInformation element = new DefaultExtendedElementInformation();
        element.setName(name);
        element.setDefinition(new SimpleInternationalString("http://www.seadatanet.org/urnurl/"));
        element.setDataType(Datatype.CODE_LIST);
        Set set = new HashSet();
        set.add("SeaDataNet");
        element.setParentEntity(set);
        //TODO see for the source

        return element;
    }

    /**
     *
     * @param values
     * @param keywordType
     * @param altTitle
     * @return
     */
    protected Keywords createKeyword(Set<String> values, String keywordType, String title, String altTitle, String date, String version) throws ParseException {

        DefaultKeywords keyword = new DefaultKeywords();
        Set<InternationalString> kws = new HashSet<>();
        if (values != null) {
            for (String value: values) {
                if (value != null) {
                    kws.add(new Anchor(URI.create("SDN:P021:35:ATTN"), value));
                }
            }
        }
        keyword.setKeywords(kws);
        keyword.setType(KeywordType.valueOf(keywordType));

        //we create the citation describing the vocabulary used

        DefaultCitation citation = new DefaultCitation();
        citation.setTitle(new SimpleInternationalString(title));
        Set set = new HashSet();
        set.add(new SimpleInternationalString(altTitle));
        citation.setAlternateTitles(set);
        DefaultCitationDate revisionDate = new DefaultCitationDate();
        revisionDate.setDateType(DateType.REVISION);
        Date d = TemporalUtilities.parseDate(date);
        revisionDate.setDate(d);

        set = new HashSet();
        set.add(revisionDate);
        citation.setDates(set);
        citation.setEdition(new Anchor(URI.create("SDN:C371:1:35"), version));
        set = new HashSet();
        set.add(new ImmutableIdentifier(null, null, "http://www.seadatanet.org/urnurl/"));
        citation.setIdentifiers(set);
        keyword.setThesaurusName(citation);


        return keyword;
    }

     protected Set<GeographicExtent> createGeographicExtent(String westVar, String eastVar, String southVar, String northVar) {
         Set<GeographicExtent> result = new HashSet<>();

         double west  = Double.parseDouble(westVar);
         double east  = Double.parseDouble(eastVar);
         double south = Double.parseDouble(southVar);
         double north = Double.parseDouble(northVar);

         // for point BBOX we replace the westValue equals to 0 by the eastValue (respectively for  north/south)
         if (east == 0) {
             east = west;
         }
         if (north == 0) {
             north = south;
         }

         GeographicExtent geo = new DefaultGeographicBoundingBox(west, east, south, north);
         result.add(geo);
         return result;
    }


     /**
     * Tests the unmarshall of a metadata.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void marshallTest() throws Exception {

        DefaultMetadata metadata     = new DefaultMetadata();

        /*
         * static part
         */
        metadata.setFileIdentifier("42292_5p_19900609195600");
        metadata.setLanguage(Locale.ENGLISH);
        metadata.setCharacterSet(CharacterSet.UTF_8);
        Set set = new HashSet();
        set.add(ScopeCode.DATASET);
        metadata.setHierarchyLevels(set);
        set = new HashSet();
        set.add("Common Data Index record");
        metadata.setHierarchyLevelNames(set);
        /*
         * contact parts
         */
        DefaultResponsibleParty author = new DefaultResponsibleParty(Role.AUTHOR);
        author.setOrganisationName(new SimpleInternationalString("IFREMER / IDM/SISMER"));
        DefaultContact contact = new DefaultContact();
        DefaultTelephone t = new DefaultTelephone();
        set = new HashSet();
        set.add("+33 (0)2 98.22.49.16");
        t.setVoices(set);
        set = new HashSet();
        set.add("+33 (0)2 98.22.46.44");
        t.setFacsimiles(set);
        contact.setPhone(t);
        DefaultAddress add = new DefaultAddress();
        set = new HashSet();
        set.add("Centre IFREMER de Brest BP 70");
        add.setDeliveryPoints(set);
        add.setCity(new SimpleInternationalString("PLOUZANE"));
        add.setPostalCode("29280");
        add.setCountry(new Anchor(URI.create("SDN:C320:2:FR"), "France"));
        set = new HashSet();
        set.add("sismer@ifremer.fr");
        add.setElectronicMailAddresses(set);
        contact.setAddress(add);
        DefaultOnlineResource o = new DefaultOnlineResource(new URI("http://www.ifremer.fr/sismer/"));
        o.setProtocol("http");
        contact.setOnlineResource(o);
        author.setContactInfo(contact);
        set = new HashSet();
        set.add(author);
        metadata.setContacts(set);

        /*
         * creation date
         */
        metadata.setDateStamp(TemporalUtilities.parseDate("2009-01-01T06:00:00+0200"));

        /*
         * Spatial representation info
         */
        DefaultVectorSpatialRepresentation spatialRep = new DefaultVectorSpatialRepresentation();
        DefaultGeometricObjects geoObj = new DefaultGeometricObjects(GeometricObjectType.POINT);
        set = new HashSet();
        set.add(geoObj);
        spatialRep.setGeometricObjects(set);

        set = new HashSet();
        set.add(spatialRep);
        metadata.setSpatialRepresentationInfo(set);

        /*
         * Reference system info
         */
        String code = "EPSG:4326";

        DefaultCitation RScitation = new DefaultCitation();

        RScitation.setTitle(new SimpleInternationalString("SeaDataNet geographic co-ordinate reference frames"));
        set = new HashSet();
        set.add(new SimpleInternationalString("L101"));
        RScitation.setAlternateTitles(set);
        set = new HashSet();
        set.add(new ImmutableIdentifier(null, null, "http://www.seadatanet.org/urnurl/"));
        RScitation.setIdentifiers(set);
        RScitation.setEdition(new Anchor(new URI("SDN:C371:1:2"),"2"));

        ImmutableIdentifier Nidentifier = new ImmutableIdentifier(RScitation, "L101", code);
        ReferenceSystemMetadata rs = new ReferenceSystemMetadata(Nidentifier);
        set = new HashSet();
        set.add(rs);
        metadata.setReferenceSystemInfo(set);

        /*
         * extension information
         */
        DefaultMetadataExtensionInformation extensionInfo = new DefaultMetadataExtensionInformation();
        Set<ExtendedElementInformation> elements = new HashSet<>();

        //we only keep one element for test purpose (unordered list)
        //EDMO
        ExtendedElementInformation edmo =  createExtensionInfo("SDN:EDMO::");
        elements.add(edmo);

        extensionInfo.setExtendedElementInformation(elements);

        set = new HashSet();
        set.add(extensionInfo);
        metadata.setMetadataExtensionInfo(set);

        /*
         * Data indentification
         */
        DefaultDataIdentification dataIdentification = new DefaultDataIdentification();

        DefaultCitation citation = new DefaultCitation();
        citation.setTitle(new SimpleInternationalString("90008411.ctd"));
        set = new HashSet();
        set.add(new SimpleInternationalString("42292_5p_19900609195600"));
        citation.setAlternateTitles(set);

        DefaultCitationDate revisionDate = new DefaultCitationDate();
        revisionDate.setDateType(DateType.REVISION);
        Date d = TemporalUtilities.parseDate("1990-06-05T00:00:00+0200");
        revisionDate.setDate(d);
        DefaultCitationDate creationDate = new DefaultCitationDate();
        creationDate.setDateType(DateType.CREATION);
        Date dc = TemporalUtilities.parseDate("1979-08-03T00:00:00+0200");
        creationDate.setDate(dc);
        List<CitationDate> dates = new ArrayList<>();
        dates.add(revisionDate);
        dates.add(creationDate);
        citation.setDates(dates);


        Set<ResponsibleParty> originators = new HashSet<>();
        DefaultResponsibleParty originator = new DefaultResponsibleParty(Role.ORIGINATOR);
        originator.setOrganisationName(new SimpleInternationalString("UNIVERSITE DE LA MEDITERRANNEE (U2) / COM - LAB. OCEANOG. BIOGEOCHIMIE - LUMINY"));
        contact = new DefaultContact();
        t = new DefaultTelephone();
        set = new HashSet();
        set.add("+33(0)4 91 82 91 15");
        t.setVoices(set);
        set = new HashSet();
        set.add("+33(0)4 91.82.65.48");
        t.setFacsimiles(set);
        contact.setPhone(t);
        add = new DefaultAddress();
        set = new HashSet();
        set.add("UFR Centre Oceanologique de Marseille Campus de Luminy Case 901");
        add.setDeliveryPoints(set);
        add.setCity(new SimpleInternationalString("Marseille cedex 9"));
        add.setPostalCode("13288");
        add.setCountry(new Anchor(URI.create("SDN:C320:2:FR"), "France"));
        set = new HashSet();
        add.setElectronicMailAddresses(set);
        contact.setAddress(add);
        o = new DefaultOnlineResource(new URI("http://www.com.univ-mrs.fr/LOB/"));
        o.setProtocol("http");
        contact.setOnlineResource(o);
        originator.setContactInfo(contact);
        originators.add(originator);
        citation.setCitedResponsibleParties(originators);

        dataIdentification.setCitation(citation);


        dataIdentification.setAbstract(new SimpleInternationalString("Donnees CTD NEDIPROD VI 120"));

        DefaultResponsibleParty custodian = new DefaultResponsibleParty(Role.CUSTODIAN);
        custodian.setOrganisationName(new SimpleInternationalString("IFREMER / IDM/SISMER"));
        contact = new DefaultContact();
        t = new DefaultTelephone();
        set = new HashSet();
        set.add("+33 (0)2 98.22.49.16");
        t.setVoices(set);
        set = new HashSet();
        set.add("+33 (0)2 98.22.46.44");
        t.setFacsimiles(set);
        contact.setPhone(t);
        add = new DefaultAddress();
        set = new HashSet();
        set.add("Centre IFREMER de Brest BP 70");
        add.setDeliveryPoints(set);
        add.setCity(new SimpleInternationalString("PLOUZANE"));
        add.setPostalCode("29280");
        add.setCountry(new Anchor(URI.create("SDN:C320:2:FR"), "France"));
        set = new HashSet();
        set.add("sismer@ifremer.fr");
        add.setElectronicMailAddresses(set);
        contact.setAddress(add);
        o = new DefaultOnlineResource(new URI("http://www.ifremer.fr/sismer/"));
        o.setProtocol("http");
        contact.setOnlineResource(o);
        custodian.setContactInfo(contact);
        set = new HashSet();
        set.add(custodian);
        dataIdentification.setPointOfContacts(set);

        /*
         * Browse graphic
         */
        DefaultBrowseGraphic go = new DefaultBrowseGraphic(URI.create("http://fr.wikipedia.org/wiki/Fichier:Death_skull.svg"));
        go.setFileDescription(new SimpleInternationalString("thumbnail"));
        dataIdentification.setGraphicOverviews(Arrays.asList(go));
        
        /*
         * keywords
         */
        Set<Keywords> keywords = new HashSet<>();

        //parameter
        Set<String> keys = new HashSet<>();
        keys.add("Transmittance and attenuance of the water column");
        Keywords keyword = createKeyword(keys, "parameter", "BODC Parameter Discovery Vocabulary", "P021", "2008-11-26T00:00:00+0200", "35");
        keywords.add(keyword);


        dataIdentification.setDescriptiveKeywords(keywords);

        /*
         * resource constraint
         */
        Set<String> resConsts = new HashSet<>();
        resConsts.add("license");
        DefaultLegalConstraints constraint = new DefaultLegalConstraints();
        Set<Restriction> restrictions  = new HashSet<>();
        restrictions.add(Restriction.LICENSE);

        constraint.setAccessConstraints(restrictions);
        set = new HashSet();
        set.add(constraint);
        dataIdentification.setResourceConstraints(set);

        /*
         * Aggregate info
         */
        Set<DefaultAggregateInformation> aggregateInfos = new HashSet<>();

        //cruise
        DefaultAggregateInformation aggregateInfo = new DefaultAggregateInformation();
        citation = new DefaultCitation();
        citation.setTitle(new SimpleInternationalString("MEDIPROD VI"));
        set = new HashSet();
        set.add(new SimpleInternationalString("90008411"));
        citation.setAlternateTitles(set);
        revisionDate = new DefaultCitationDate();
        revisionDate.setDateType(DateType.REVISION);
        d = TemporalUtilities.parseDate("1990-06-05T00:00:00+0200");
        revisionDate.setDate(d);
        set = new HashSet();
        set.add(revisionDate);
        citation.setDates(set);
        aggregateInfo.setAggregateDataSetName(citation);
        aggregateInfo.setInitiativeType(InitiativeType.CAMPAIGN);
        aggregateInfo.setAssociationType(AssociationType.LARGER_WORD_CITATION);
        aggregateInfos.add(aggregateInfo);

        dataIdentification.setAggregationInfo(aggregateInfos);

        //static part
        set = new HashSet();
        set.add(Locale.ENGLISH);
        dataIdentification.setLanguages(set);
        set = new HashSet();
        set.add(TopicCategory.OCEANS);
        dataIdentification.setTopicCategories(set);

        /*
         * Extent
         */
        DefaultExtent extent = new DefaultExtent();

        // geographic extent
        extent.setGeographicElements(createGeographicExtent("1.1667", "1.1667", "36.6", "36.6"));

        //temporal extent
        DefaultTemporalExtent tempExtent = new DefaultTemporalExtent();

        
        TimePeriodType period = new TimePeriodType(null, "1990-06-05", "1990-07-02");
        period.setId("extent");
        
        tempExtent.setExtent(period);

        set = new HashSet();
        set.add(tempExtent);
        extent.setTemporalElements(set);



        //vertical extent
        DefaultVerticalExtent vertExtent = new DefaultVerticalExtent();
        String miv = null;
        String mav = null;

        if (miv != null) {
            vertExtent.setMinimumValue(Double.parseDouble(miv));
        }
        if (mav != null) {
            vertExtent.setMaximumValue(Double.parseDouble(mav));
        }

        // vertical datum
        String datumID = "D28";

        Map<String, String> prop = new HashMap<>();
        prop.put(DefaultVerticalDatum.NAME_KEY, datumID);
        prop.put(DefaultVerticalDatum.SCOPE_KEY, null);
        DefaultVerticalDatum datum = new DefaultVerticalDatum(prop, VerticalDatumType.GEOIDAL);


        // vertical coordinate system  TODO var 32 uom?
        final Map<String, Object> axisMap = new HashMap<>();
        axisMap.put(AbstractIdentifiedObject.NAME_KEY, "meters");
        DefaultCoordinateSystemAxis axis = new DefaultCoordinateSystemAxis(axisMap, "meters", AxisDirection.DOWN, Unit.valueOf("m"));

        final Map<String, Object> csMap = new HashMap<>();
        csMap.put(AbstractIdentifiedObject.NAME_KEY, "meters");
        DefaultVerticalCS cs = new DefaultVerticalCS(csMap, axis);

        prop = new HashMap<>();
        prop.put(DefaultVerticalCRS.NAME_KEY, "idvertCRS");
        prop.put(DefaultVerticalCRS.SCOPE_KEY, null);
        DefaultVerticalCRS vcrs = new DefaultVerticalCRS(prop, datum, cs);


        // TODO vertical limit? var 35
        vertExtent.setVerticalCRS(vcrs);

        set = new HashSet();
        set.add(vertExtent);
        extent.setVerticalElements(set);

        set = new HashSet();
        set.add(extent);
        dataIdentification.setExtents(set);

        set = new HashSet();
        set.add(dataIdentification);
        metadata.setIdentificationInfo(set);

        /*
         * Content info
         */
        DefaultImageDescription contentInfo = new DefaultImageDescription();
        contentInfo.setCloudCoverPercentage(50.0);
        metadata.setContentInfo(Arrays.asList(contentInfo));
        
        /*
         * Distribution info
         */
        DefaultDistribution distributionInfo = new DefaultDistribution();

        //distributor
        DefaultDistributor distributor       = new DefaultDistributor();

        DefaultResponsibleParty distributorContact = new DefaultResponsibleParty(Role.DISTRIBUTOR);
        distributorContact.setOrganisationName(new SimpleInternationalString("IFREMER / IDM/SISMER"));
        contact = new DefaultContact();
        t = new DefaultTelephone();
        set = new HashSet();
        set.add("+33 (0)2 98.22.49.16");
        t.setVoices(set);
        set = new HashSet();
        set.add("+33 (0)2 98.22.46.44");
        t.setFacsimiles(set);
        contact.setPhone(t);
        add = new DefaultAddress();
        set = new HashSet();
        set.add("Centre IFREMER de Brest BP 70");
        add.setDeliveryPoints(set);
        add.setCity(new SimpleInternationalString("PLOUZANE"));
        add.setPostalCode("29280");
        add.setCountry(new Anchor(URI.create("SDN:C320:2:FR"), "France"));
        set = new HashSet();
        set.add("sismer@ifremer.fr");
        add.setElectronicMailAddresses(set);
        contact.setAddress(add);
        o = new DefaultOnlineResource(new URI("http://www.ifremer.fr/sismer/"));
        o.setProtocol("http");
        contact.setOnlineResource(o);
        distributorContact.setContactInfo(contact);

        distributor.setDistributorContact(distributorContact);
        set = new HashSet();
        set.add(distributor);
        distributionInfo.setDistributors(set);

        //format
        Set<Format> formats  = new HashSet<>();

        DefaultFormat format = new DefaultFormat();
        String name = "MEDATLAS ASCII";
        format.setName(new Anchor(new URI("SDN:L241:1:MEDATLAS"), name));
        format.setVersion(new SimpleInternationalString("1.0"));
        formats.add(format);

        distributionInfo.setDistributionFormats(formats);

        //transfert options
        DefaultDigitalTransferOptions digiTrans = new DefaultDigitalTransferOptions();

        digiTrans.setTransferSize(2.431640625);

        DefaultOnlineResource onlines = new DefaultOnlineResource();

        String uri = "http://www.ifremer.fr/sismerData/jsp/visualisationMetadata3.jsp?langue=EN&pageOrigine=CS&cle1=42292_1&cle2=CTDF02";
        onlines.setLinkage(new URI(uri));

        onlines.setDescription(new SimpleInternationalString("CTDF02"));
        onlines.setFunction(OnLineFunction.DOWNLOAD);
        onlines.setProtocol("http");
        set = new HashSet();
        set.add(onlines);
        digiTrans.setOnLines(set);

        set = new HashSet();
        set.add(digiTrans);
        distributionInfo.setTransferOptions(set);

        metadata.setDistributionInfo(distributionInfo);

        StringWriter sw = new StringWriter();
        marshaller = testPool.acquireMarshaller();
        marshaller.setProperty(XML.TIMEZONE, TimeZone.getTimeZone("GMT+2"));
        marshaller.marshal(metadata, sw);
        String result = sw.toString();

        InputStream in = Util.getResourceAsStream("org/constellation/xml/metadata/meta1.xml");
        StringWriter out = new StringWriter();
        byte[] buffer = new byte[1024];
        int size;

        while ((size = in.read(buffer, 0, 1024)) > 0) {
            out.write(new String(buffer, 0, size));
        }
        
        String expResult = out.toString();
        XMLComparator comparator = new XMLComparator(expResult, result);
        comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
        comparator.ignoredAttributes.add("http://www.w3.org/2001/XMLSchema-instance:schemaLocation");
        comparator.compare();
    }

    @Test
    public void marshallURLTest() throws Exception {

        final URI u1 = new URI("C:%5Cdossier%20test%5CFichier%5C");

        final StringWriter sw = new StringWriter();
        marshaller = testPool.acquireMarshaller();
        final DefaultOnlineResource online = new DefaultOnlineResource();
        online.setLinkage(u1);
        marshaller.marshal(online, sw);
        String result = sw.toString();
        System.out.println(result);

        unmarshaller = testPool.acquireUnmarshaller();
        final DefaultOnlineResource expResult = (DefaultOnlineResource) unmarshaller.unmarshal(new StringReader(result));

        assertEquals(expResult, online);
    }
}
