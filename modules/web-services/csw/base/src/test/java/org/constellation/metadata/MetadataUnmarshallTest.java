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

package org.constellation.metadata;

import org.apache.sis.internal.jaxb.gmx.Anchor;
import org.apache.sis.internal.jaxb.metadata.replace.ReferenceSystemMetadata;
import org.apache.sis.metadata.iso.DefaultExtendedElementInformation;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.metadata.iso.DefaultMetadataExtensionInformation;
import org.apache.sis.metadata.iso.ImmutableIdentifier;
import org.apache.sis.metadata.iso.citation.DefaultAddress;
import org.apache.sis.metadata.iso.citation.DefaultCitation;
import org.apache.sis.metadata.iso.citation.DefaultCitationDate;
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
import org.apache.sis.test.integration.DefaultMetadataTest;
import org.apache.sis.test.XMLComparator;
import org.apache.sis.util.iso.SimpleInternationalString;
import org.apache.sis.xml.MarshallerPool;
import org.apache.sis.xml.XML;
import org.constellation.util.Util;
import org.geotoolkit.csw.xml.CSWMarshallerPool;
import org.geotoolkit.gml.xml.v311.TimePeriodType;
import org.geotoolkit.temporal.object.TemporalUtilities;
import org.geotoolkit.xml.AnchoredMarshallerPool;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
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

import javax.measure.unit.Unit;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import static java.util.Collections.singleton;
import static org.junit.Assume.assumeTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 *
 * @author Guilhem Legal
 */
public class MetadataUnmarshallTest extends DefaultMetadataTest {
    private static MarshallerPool testPool;
    private static Unmarshaller unmarshaller;
    private static Marshaller marshaller;
    private static TimeZone localTimezone;

    @BeforeClass
    public static void setUp() throws JAXBException, URISyntaxException {
        testPool = CSWMarshallerPool.getInstance();
        CSWworkerTest.fillPoolAnchor((AnchoredMarshallerPool) testPool);
        localTimezone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("CET"));
    }

    @After
    public void releaseMarshallers() {
        if (localTimezone != null) {
            TimeZone.setDefault(localTimezone);
            localTimezone = null;
        }
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
     * Invoked by {@link DefaultMetadataTest} for setting the temporal extent of the metadata to be tested.
     *
     * @param extent    The extent to set.
     * @param startTime The start time in the {@code "yyy-mm-dd"} format.
     * @param endTime   The end time in the {@code "yyy-mm-dd"} format.
     */
    @Override
    protected void setTemporalBounds(final DefaultTemporalExtent extent, final String startTime, final String endTime) {
        assumeTrue(TimeZone.getDefault().equals(TimeZone.getTimeZone("CET")));
        extent.setExtent(new TimePeriodType(null, startTime, endTime));
    }

    /**
     * Tests the unmarshall of a metadata.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void unmarshallTest() throws Exception {
        unmarshaller = testPool.acquireUnmarshaller();

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
        Object obj = unmarshaller.unmarshal(sr);

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
    }

    protected ExtendedElementInformation createExtensionInfo(String name) {
        DefaultExtendedElementInformation element = new DefaultExtendedElementInformation();
        element.setName(name);
        element.setDefinition(new SimpleInternationalString("http://www.seadatanet.org/urnurl/"));
        element.setDataType(Datatype.CODE_LIST);
        element.setParentEntity(singleton("SeaDataNet"));
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
        citation.setAlternateTitles(singleton(new SimpleInternationalString(altTitle)));
        DefaultCitationDate revisionDate = new DefaultCitationDate();
        revisionDate.setDateType(DateType.REVISION);
        Date d = TemporalUtilities.parseDate(date);
        revisionDate.setDate(d);

        citation.setDates(singleton(revisionDate));
        citation.setEdition(new Anchor(URI.create("SDN:C371:1:35"), version));
        citation.setIdentifiers(singleton(new ImmutableIdentifier(null, null, "http://www.seadatanet.org/urnurl/")));
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
      *
      * @todo Remove (together with the XML file) after we enabled {@link DefaultMetadataTest#testMarshalling()}.
      */
    @Test
    public void marshallTest() throws Exception {

        DefaultMetadata metadata     = new DefaultMetadata();

        /*
         * static part
         */
        metadata.setFileIdentifier("42292_5p_19900609195600");
        metadata.setLanguage(Locale.ENGLISH);
        metadata.setCharacterSets(singleton(StandardCharsets.UTF_8));
        metadata.setHierarchyLevels(singleton(ScopeCode.DATASET));
        metadata.setHierarchyLevelNames(singleton("Common Data Index record"));
        /*
         * contact parts
         */
        DefaultResponsibleParty author = new DefaultResponsibleParty(Role.AUTHOR);
        author.setOrganisationName(new SimpleInternationalString("IFREMER / IDM/SISMER"));
        DefaultContact contact = new DefaultContact();
        DefaultTelephone t = new DefaultTelephone();
        t.setVoices(singleton("+33 (0)2 98.22.49.16"));
        t.setFacsimiles(singleton("+33 (0)2 98.22.46.44"));
        contact.setPhone(t);
        DefaultAddress add = new DefaultAddress();
        add.setDeliveryPoints(singleton(new SimpleInternationalString("Centre IFREMER de Brest BP 70")));
        add.setCity(new SimpleInternationalString("PLOUZANE"));
        add.setPostalCode("29280");
        add.setCountry(new Anchor(URI.create("SDN:C320:2:FR"), "France"));
        add.setElectronicMailAddresses(singleton("sismer@ifremer.fr"));
        contact.setAddress(add);
        DefaultOnlineResource o = new DefaultOnlineResource(new URI("http://www.ifremer.fr/sismer/"));
        o.setProtocol("http");
        contact.setOnlineResource(o);
        author.setContactInfo(contact);
        metadata.setContacts(singleton(author));

        /*
         * creation date
         */
        metadata.setDateStamp(TemporalUtilities.parseDate("2009-01-01T06:00:00+0200"));

        /*
         * Spatial representation info
         */
        DefaultVectorSpatialRepresentation spatialRep = new DefaultVectorSpatialRepresentation();
        DefaultGeometricObjects geoObj = new DefaultGeometricObjects(GeometricObjectType.POINT);
        spatialRep.setGeometricObjects(singleton(geoObj));
        metadata.setSpatialRepresentationInfo(singleton(spatialRep));

        /*
         * Reference system info
         */
        String code = "EPSG:4326";

        DefaultCitation RScitation = new DefaultCitation();

        RScitation.setTitle(new SimpleInternationalString("SeaDataNet geographic co-ordinate reference frames"));
        RScitation.setAlternateTitles(singleton(new SimpleInternationalString("L101")));
        RScitation.setIdentifiers(singleton(new ImmutableIdentifier(null, null, "http://www.seadatanet.org/urnurl/")));
        RScitation.setEdition(new Anchor(new URI("SDN:C371:1:2"),"2"));

        ImmutableIdentifier Nidentifier = new ImmutableIdentifier(RScitation, "L101", code);
        ReferenceSystemMetadata rs = new ReferenceSystemMetadata(Nidentifier);
        metadata.setReferenceSystemInfo(singleton(rs));

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

        metadata.setMetadataExtensionInfo(singleton(extensionInfo));

        /*
         * Data indentification
         */
        DefaultDataIdentification dataIdentification = new DefaultDataIdentification();

        DefaultCitation citation = new DefaultCitation();
        citation.setTitle(new SimpleInternationalString("90008411.ctd"));
        citation.setAlternateTitles(singleton(new SimpleInternationalString("42292_5p_19900609195600")));

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
        t.setVoices(singleton("+33(0)4 91 82 91 15"));
        t.setFacsimiles(singleton("+33(0)4 91.82.65.48"));
        contact.setPhone(t);
        add = new DefaultAddress();
        add.setDeliveryPoints(singleton(new SimpleInternationalString("UFR Centre Oceanologique de Marseille Campus de Luminy Case 901")));
        add.setCity(new SimpleInternationalString("Marseille cedex 9"));
        add.setPostalCode("13288");
        add.setCountry(new Anchor(URI.create("SDN:C320:2:FR"), "France"));
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
        t.setVoices(singleton("+33 (0)2 98.22.49.16"));
        t.setFacsimiles(singleton("+33 (0)2 98.22.46.44"));
        contact.setPhone(t);
        add = new DefaultAddress();
        add.setDeliveryPoints(singleton(new SimpleInternationalString("Centre IFREMER de Brest BP 70")));
        add.setCity(new SimpleInternationalString("PLOUZANE"));
        add.setPostalCode("29280");
        add.setCountry(new Anchor(URI.create("SDN:C320:2:FR"), "France"));
        add.setElectronicMailAddresses(singleton("sismer@ifremer.fr"));
        contact.setAddress(add);
        o = new DefaultOnlineResource(new URI("http://www.ifremer.fr/sismer/"));
        o.setProtocol("http");
        contact.setOnlineResource(o);
        custodian.setContactInfo(contact);
        dataIdentification.setPointOfContacts(singleton(custodian));

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
        DefaultLegalConstraints constraint = new DefaultLegalConstraints();
        Set<Restriction> restrictions  = new HashSet<>();
        restrictions.add(Restriction.LICENSE);

        constraint.setAccessConstraints(restrictions);
        dataIdentification.setResourceConstraints(singleton(constraint));

        /*
         * Aggregate info
         */
        Set<DefaultAggregateInformation> aggregateInfos = new HashSet<>();

        //cruise
        DefaultAggregateInformation aggregateInfo = new DefaultAggregateInformation();
        citation = new DefaultCitation();
        citation.setTitle(new SimpleInternationalString("MEDIPROD VI"));
        citation.setAlternateTitles(singleton(new SimpleInternationalString("90008411")));
        revisionDate = new DefaultCitationDate();
        revisionDate.setDateType(DateType.REVISION);
        d = TemporalUtilities.parseDate("1990-06-05T00:00:00+0200");
        revisionDate.setDate(d);
        citation.setDates(singleton(revisionDate));
        aggregateInfo.setAggregateDataSetName(citation);
        aggregateInfo.setInitiativeType(InitiativeType.CAMPAIGN);
        aggregateInfo.setAssociationType(AssociationType.LARGER_WORD_CITATION);
        aggregateInfos.add(aggregateInfo);

        dataIdentification.setAggregationInfo(aggregateInfos);

        //static part
        dataIdentification.setLanguages(singleton(Locale.ENGLISH));
        dataIdentification.setTopicCategories(singleton(TopicCategory.OCEANS));

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

        extent.setTemporalElements(singleton(tempExtent));



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
        extent.setVerticalElements(singleton(vertExtent));
        dataIdentification.setExtents(singleton(extent));
        metadata.setIdentificationInfo(singleton(dataIdentification));

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
        t.setVoices(singleton("+33 (0)2 98.22.49.16"));
        t.setFacsimiles(singleton("+33 (0)2 98.22.46.44"));
        contact.setPhone(t);
        add = new DefaultAddress();
        add.setDeliveryPoints(singleton(new SimpleInternationalString("Centre IFREMER de Brest BP 70")));
        add.setCity(new SimpleInternationalString("PLOUZANE"));
        add.setPostalCode("29280");
        add.setCountry(new Anchor(URI.create("SDN:C320:2:FR"), "France"));
        add.setElectronicMailAddresses(singleton("sismer@ifremer.fr"));
        contact.setAddress(add);
        o = new DefaultOnlineResource(new URI("http://www.ifremer.fr/sismer/"));
        o.setProtocol("http");
        contact.setOnlineResource(o);
        distributorContact.setContactInfo(contact);

        distributor.setDistributorContact(distributorContact);
        distributionInfo.setDistributors(singleton(distributor));

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
        digiTrans.setOnLines(singleton(onlines));

        distributionInfo.setTransferOptions(singleton(digiTrans));

        metadata.setDistributionInfo(Collections.singleton(distributionInfo));

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
