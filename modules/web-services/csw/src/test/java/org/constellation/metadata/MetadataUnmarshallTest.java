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

import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.measure.unit.Unit;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

//Junit dependencies
import org.constellation.util.Util;
import org.geotools.metadata.iso.ExtendedElementInformationImpl;
import org.geotools.metadata.iso.IdentifierImpl;
import org.geotools.metadata.iso.MetaDataImpl;
import org.geotools.metadata.iso.MetadataExtensionInformationImpl;
import org.geotools.metadata.iso.citation.AddressImpl;
import org.geotools.metadata.iso.citation.CitationDateImpl;
import org.geotools.metadata.iso.citation.CitationImpl;
import org.geotools.metadata.iso.citation.ContactImpl;
import org.geotools.metadata.iso.citation.OnLineResourceImpl;
import org.geotools.metadata.iso.citation.ResponsiblePartyImpl;
import org.geotools.metadata.iso.citation.TelephoneImpl;
import org.geotools.metadata.iso.constraint.LegalConstraintsImpl;
import org.geotools.metadata.iso.distribution.DigitalTransferOptionsImpl;
import org.geotools.metadata.iso.distribution.DistributionImpl;
import org.geotools.metadata.iso.distribution.DistributorImpl;
import org.geotools.metadata.iso.distribution.FormatImpl;
import org.geotools.metadata.iso.extent.ExtentImpl;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;
import org.geotools.metadata.iso.extent.TemporalExtentImpl;
import org.geotools.metadata.iso.extent.VerticalExtentImpl;
import org.geotools.metadata.iso.identification.AggregateInformationImpl;
import org.geotools.metadata.iso.identification.DataIdentificationImpl;
import org.geotools.metadata.iso.identification.KeywordsImpl;
import org.geotools.metadata.iso.spatial.GeometricObjectsImpl;
import org.geotools.metadata.iso.spatial.VectorSpatialRepresentationImpl;
import org.geotools.referencing.DefaultReferenceSystem;
import org.geotools.referencing.NamedIdentifier;
import org.geotools.referencing.crs.DefaultVerticalCRS;
import org.geotools.referencing.cs.DefaultCoordinateSystemAxis;
import org.geotools.referencing.cs.DefaultVerticalCS;
import org.geotools.referencing.datum.DefaultVerticalDatum;
import org.geotools.temporal.object.DefaultInstant;
import org.geotools.temporal.object.DefaultPeriod;
import org.geotools.temporal.object.DefaultPosition;
import org.geotools.util.SimpleInternationalString;
import org.junit.*;
import org.opengis.metadata.Datatype;
import org.opengis.metadata.ExtendedElementInformation;
import org.opengis.metadata.citation.DateType;
import org.opengis.metadata.citation.OnLineFunction;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.citation.Role;
import org.opengis.metadata.constraint.Restriction;
import org.opengis.metadata.distribution.Format;
import org.opengis.metadata.extent.GeographicExtent;
import org.opengis.metadata.identification.AssociationType;
import org.opengis.metadata.identification.CharacterSet;
import org.opengis.metadata.identification.InitiativeType;
import org.opengis.metadata.identification.KeywordType;
import org.opengis.metadata.identification.Keywords;
import org.opengis.metadata.identification.TopicCategory;
import org.opengis.metadata.maintenance.ScopeCode;
import org.opengis.metadata.spatial.GeometricObjectType;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.datum.VerticalDatumType;
import org.opengis.util.InternationalString;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal
 */
public class MetadataUnmarshallTest {

    private Logger logger = Logger.getLogger("org.constellation.metadata");

    private Unmarshaller unmarshaller;

    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {

    }

    @Before
    public void setUp() throws Exception {
        JAXBContext context = JAXBContext.newInstance(MetaDataImpl.class, org.constellation.metadata.fra.ObjectFactory.class);
        unmarshaller        = context.createUnmarshaller();
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * Tests the unmarshall of a metadata.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void unmarshallTest() throws Exception {

        Object obj = unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta1.xml"));

        assertTrue(obj instanceof MetaDataImpl);
        MetaDataImpl result = (MetaDataImpl) obj;

         MetaDataImpl expResult     = new MetaDataImpl();

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
        ResponsiblePartyImpl author = new ResponsiblePartyImpl(Role.AUTHOR);
        author.setOrganisationName(new SimpleInternationalString("IFREMER / IDM/SISMER"));
        ContactImpl contact = new ContactImpl();
        TelephoneImpl t = new TelephoneImpl();
        set = new HashSet();
        set.add("+33 (0)2 98.22.49.16");
        t.setVoices(set);
        set = new HashSet();
        set.add("+33 (0)2 98.22.46.44");
        t.setFacsimiles(set);
        contact.setPhone(t);
        AddressImpl add = new AddressImpl();
        set = new HashSet();
        set.add("Centre IFREMER de Brest BP 70");
        add.setDeliveryPoints(set);
        add.setCity(new SimpleInternationalString("PLOUZANE"));
        add.setPostalCode("29280");
        add.setCountry(new SimpleInternationalString("France"));
        set = new HashSet();
        set.add("sismer@ifremer.fr");
        add.setElectronicMailAddresses(set);
        contact.setAddress(add);
        OnLineResourceImpl o = new OnLineResourceImpl(new URI("http://www.ifremer.fr/sismer/"));
        contact.setOnLineResource(o);
        author.setContactInfo(contact);
        set = new HashSet();
        set.add(author);
        expResult.setContacts(set);

        /*
         * creation date
         */
        expResult.setDateStamp(df.parse("2009-01-01T00:00:00"));

        /*
         * Spatial representation info
         */
        VectorSpatialRepresentationImpl spatialRep = new VectorSpatialRepresentationImpl();
        GeometricObjectsImpl geoObj = new GeometricObjectsImpl(GeometricObjectType.valueOf("point"));
        set = new HashSet();
        set.add(geoObj);
        spatialRep.setGeometricObjects(set);

        set = new HashSet();
        set.add(spatialRep);
        expResult.setSpatialRepresentationInfo(set);

        /*
         * Reference system info
         */
        String code = "World Geodetic System 84";

        CitationImpl RScitation = new CitationImpl();

        RScitation.setTitle(new SimpleInternationalString("SeaDataNet geographic co-ordinate reference frames"));
        set = new HashSet();
        set.add(new SimpleInternationalString("L101"));
        RScitation.setAlternateTitles(set);
        set = new HashSet();
        set.add(new IdentifierImpl("http://www.seadatanet.org/urnurl/"));
        RScitation.setIdentifiers(set);
        RScitation.setEdition(new SimpleInternationalString("2"));

        NamedIdentifier Nidentifier = new NamedIdentifier(RScitation, code);
        DefaultReferenceSystem rs = new DefaultReferenceSystem(Nidentifier);
        set = new HashSet();
        set.add(rs);
        expResult.setReferenceSystemInfo(set);


        /*
         * extension information
         */
        MetadataExtensionInformationImpl extensionInfo = new MetadataExtensionInformationImpl();
        Set<ExtendedElementInformation> elements = new HashSet<ExtendedElementInformation>();

        //EDMO
        ExtendedElementInformation edmo =  createExtensionInfo("SDN:EDMO::");
        elements.add(edmo);

        //L021
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
        elements.add(L241);

        extensionInfo.setExtendedElementInformation(elements);

        set = new HashSet();
        set.add(extensionInfo);
        expResult.setMetadataExtensionInfo(set);

        /*
         * Data indentification
         */
        DataIdentificationImpl dataIdentification = new DataIdentificationImpl();

        CitationImpl citation = new CitationImpl();
        citation.setTitle(new SimpleInternationalString("90008411.ctd"));
        set = new HashSet();
        set.add(new SimpleInternationalString("42292_5p_19900609195600"));
        citation.setAlternateTitles(set);

        CitationDateImpl revisionDate = new CitationDateImpl();
        revisionDate.setDateType(DateType.REVISION);
        Date d = df.parse("1990-06-05T00:00:00");
        revisionDate.setDate(d);
        set = new HashSet();
        set.add(revisionDate);
        citation.setDates(set);


        Set<ResponsibleParty> originators = new HashSet<ResponsibleParty>();
        ResponsiblePartyImpl originator = new ResponsiblePartyImpl(Role.ORIGINATOR);
        originator.setOrganisationName(new SimpleInternationalString("UNIVERSITE DE LA MEDITERRANNEE (U2) / COM - LAB. OCEANOG. BIOGEOCHIMIE - LUMINY"));
        contact = new ContactImpl();
        t = new TelephoneImpl();
        set = new HashSet();
        set.add("+33(0)4 91 82 91 15");
        t.setVoices(set);
        set = new HashSet();
        set.add("+33(0)4 91.82.65.48");
        t.setFacsimiles(set);
        contact.setPhone(t);
        add = new AddressImpl();
        set = new HashSet();
        set.add("UFR Centre Oceanologique de Marseille Campus de Luminy Case 901");
        add.setDeliveryPoints(set);
        add.setCity(new SimpleInternationalString("Marseille cedex 9"));
        add.setPostalCode("13288");
        add.setCountry(new SimpleInternationalString("France"));
        set = new HashSet();
        add.setElectronicMailAddresses(set);
        contact.setAddress(add);
        o = new OnLineResourceImpl(new URI("http://www.com.univ-mrs.fr/LOB/"));
        contact.setOnLineResource(o);
        originator.setContactInfo(contact);
        originators.add(originator);
        citation.setCitedResponsibleParties(originators);

        dataIdentification.setCitation(citation);


        dataIdentification.setAbstract(new SimpleInternationalString("Donnees CTD NEDIPROD VI 120"));

        ResponsiblePartyImpl custodian = new ResponsiblePartyImpl(Role.CUSTODIAN);
        custodian.setOrganisationName(new SimpleInternationalString("IFREMER / IDM/SISMER"));
        contact = new ContactImpl();
        t = new TelephoneImpl();
        set = new HashSet();
        set.add("+33 (0)2 98.22.49.16");
        t.setVoices(set);
        set = new HashSet();
        set.add("+33 (0)2 98.22.46.44");
        t.setFacsimiles(set);
        contact.setPhone(t);
        add = new AddressImpl();
        set = new HashSet();
        set.add("Centre IFREMER de Brest BP 70");
        add.setDeliveryPoints(set);
        add.setCity(new SimpleInternationalString("PLOUZANE"));
        add.setPostalCode("29280");
        add.setCountry(new SimpleInternationalString("France"));
        set = new HashSet();
        set.add("sismer@ifremer.fr");
        add.setElectronicMailAddresses(set);
        contact.setAddress(add);
        o = new OnLineResourceImpl(new URI("http://www.ifremer.fr/sismer/"));
        contact.setOnLineResource(o);
        custodian.setContactInfo(contact);
        set = new HashSet();
        set.add(custodian);
        dataIdentification.setPointOfContacts(set);

        /*
         * keywords
         */
        Set<Keywords> keywords = new HashSet<Keywords>();

        //parameter
        Set<String> keys = new HashSet<String>();
        keys.add("Transmittance and attenuance of the water column");
        keys.add("Electrical conductivity of the water column");
        keys.add("Dissolved oxygen parameters in the water column");
        keys.add("Light extinction and diffusion coefficients");
        keys.add("Dissolved noble gas concentration parameters in the water column");
        keys.add("Optical backscatter");
        keys.add("Salinity of the water column");
        keys.add("Dissolved concentration parameters for 'other' gases in the water column");
        keys.add("Temperature of the water column");
        keys.add("Visible waveband radiance and irradiance measurements in the atmosphere");
        keys.add("Visible waveband radiance and irradiance measurements in the water column");
        Keywords keyword = createKeyword(keys, "parameter", "BODC Parameter Discovery Vocabulary", "P021", "2008-11-26T02:00:04", "35");
        keywords.add(keyword);

        /*
        set = new HashSet();
        set.add("CTD profilers");
        keyword = createKeyword(set, "instrument", "SeaDataNet device categories", "L05", "2008-01-11T02:00:04", "4");
        keywords.add(keyword);

        //platform
        set = new HashSet();
        set.add("research vessel");
        keyword = createKeyword(set, "platform_class", "SeaDataNet Platform Classes", "L061", "2008-02-21T10:55:40", "6");
        keywords.add(keyword);*/

        //projects


        dataIdentification.setDescriptiveKeywords(keywords);

        /*
         * resource constraint
         */
        Set<String> resConsts = new HashSet<String>();
        resConsts.add("licence");
        LegalConstraintsImpl constraint = new LegalConstraintsImpl();
        Set<Restriction> restrictions  = new HashSet<Restriction>();
        for (String resConst : resConsts) {
            restrictions.add(Restriction.valueOf(resConst));
        }
        constraint.setAccessConstraints(restrictions);
        set = new HashSet();
        set.add(constraint);
        dataIdentification.setResourceConstraints(set);

        /*
         * Aggregate info
         */
        Set<AggregateInformationImpl> aggregateInfos = new HashSet<AggregateInformationImpl>();

        //cruise
        AggregateInformationImpl aggregateInfo = new AggregateInformationImpl();
        citation = new CitationImpl();
        citation.setTitle(new SimpleInternationalString("MEDIPROD VI"));
        set = new HashSet();
        set.add(new SimpleInternationalString("90008411"));
        citation.setAlternateTitles(set);
        revisionDate = new CitationDateImpl();
        revisionDate.setDateType(DateType.REVISION);
        d = df.parse("1990-06-05T00:00:00");
        revisionDate.setDate(d);
        set = new HashSet();
        set.add(revisionDate);
        citation.setDates(set);
        aggregateInfo.setAggregateDataSetName(citation);
        aggregateInfo.setInitiativeType(InitiativeType.CAMPAIGN);
        aggregateInfo.setAssociationType(AssociationType.LARGER_WORD_CITATION);
        aggregateInfos.add(aggregateInfo);

        /* station
        aggregateInfo = new AggregateInformationImpl();
        citation = new CitationImpl();
        citation.setTitle(new SimpleInternationalString("5p"));
        set = new HashSet();
        set.add(new SimpleInternationalString("5p"));
        citation.setAlternateTitles(set);
        revisionDate = new CitationDateImpl();
        revisionDate.setDateType(DateType.REVISION);
        d = df.parse("1990-06-09T00:00:00");
        revisionDate.setDate(d);
        set = new HashSet();
        set.add(revisionDate);
        citation.setDates(set);
        aggregateInfo.setAggregateDataSetName(citation);
        aggregateInfo.setInitiativeType(InitiativeType.CAMPAIGN);
        aggregateInfo.setAssociationType(AssociationType.LARGER_WORD_CITATION);
        aggregateInfos.add(aggregateInfo);*/

        dataIdentification.setAggregationInfo(aggregateInfos);

        /*
         * data scale TODO UOM

        String scale = getVariable("var21");
        String uom   = getVariable("var22");
        if (scale != null) {
            try {
                ResolutionImpl resolution = new ResolutionImpl();
                resolution.setDistance(Double.parseDouble(scale));
                resolution.setUnitOfMeasure(uom);
                dataIdentification.setSpatialResolutions(Arrays.asList(resolution));
            }  catch (NumberFormatException ex) {
                logger.severe("parse exception while parsing scale => scale:" + scale + " for record: " + identifier);
            }
        }*/

        //static part
        set = new HashSet();
        set.add(Locale.ENGLISH);
        dataIdentification.setLanguage(set);
        set = new HashSet();
        set.add(TopicCategory.OCEANS);
        dataIdentification.setTopicCategories(set);

        /*
         * Extent
         */
        ExtentImpl extent = new ExtentImpl();

        // geographic extent
        extent.setGeographicElements(createGeographicExtent("1.1667", "1.1667", "36.6", "36.6"));

        //temporal extent
        TemporalExtentImpl tempExtent = new TemporalExtentImpl();

        Date start = df.parse("1990-06-05T00:00:00");
        Date stop  = df.parse("1990-07-02T00:00:00");

        DefaultInstant begin = new DefaultInstant(new DefaultPosition(start));
        DefaultInstant end = new DefaultInstant(new DefaultPosition(stop));
        DefaultPeriod period = new DefaultPeriod(begin, end);
        tempExtent.setExtent(period);

        set = new HashSet();
        set.add(tempExtent);
        extent.setTemporalElements(set);



        //vertical extent
        VerticalExtentImpl vertExtent = new VerticalExtentImpl();
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
        DefaultVerticalCRS vcrs = null;

        Map<String, String> prop = new HashMap<String, String>();
        prop.put(DefaultVerticalDatum.NAME_KEY, datumID);
        prop.put(DefaultVerticalDatum.SCOPE_KEY, null);
        DefaultVerticalDatum datum = new DefaultVerticalDatum(prop, VerticalDatumType.ELLIPSOIDAL);


        // vertical coordinate system  TODO var 32 uom?
        DefaultCoordinateSystemAxis axis = new DefaultCoordinateSystemAxis("meters", AxisDirection.DOWN, Unit.valueOf("m"));
        DefaultVerticalCS cs = new DefaultVerticalCS(axis);

        prop = new HashMap<String, String>();
        prop.put(DefaultVerticalCRS.NAME_KEY, "idvertCRS");
        prop.put(DefaultVerticalCRS.SCOPE_KEY, null);
        vcrs = new DefaultVerticalCRS(prop, datum, cs);


        // TODO vertical limit? var 35
        vertExtent.setVerticalCRS(vcrs);

        set = new HashSet();
        set.add(vertExtent);
        extent.setVerticalElements(set);

        set = new HashSet();
        set.add(extent);
        dataIdentification.setExtent(set);

        set = new HashSet();
        set.add(dataIdentification);
        expResult.setIdentificationInfo(set);

        /*
         * Distribution info
         */
        DistributionImpl distributionInfo = new DistributionImpl();

        //distributor
        DistributorImpl distributor       = new DistributorImpl();

        ResponsiblePartyImpl distributorContact = new ResponsiblePartyImpl(Role.DISTRIBUTOR);
        distributorContact.setOrganisationName(new SimpleInternationalString("IFREMER / IDM/SISMER"));
        contact = new ContactImpl();
        t = new TelephoneImpl();
        set = new HashSet();
        set.add("+33 (0)2 98.22.49.16");
        t.setVoices(set);
        set = new HashSet();
        set.add("+33 (0)2 98.22.46.44");
        t.setFacsimiles(set);
        contact.setPhone(t);
        add = new AddressImpl();
        set = new HashSet();
        set.add("Centre IFREMER de Brest BP 70");
        add.setDeliveryPoints(set);
        add.setCity(new SimpleInternationalString("PLOUZANE"));
        add.setPostalCode("29280");
        add.setCountry(new SimpleInternationalString("France"));
        set = new HashSet();
        set.add("sismer@ifremer.fr");
        add.setElectronicMailAddresses(set);
        contact.setAddress(add);
        o = new OnLineResourceImpl(new URI("http://www.ifremer.fr/sismer/"));
        contact.setOnLineResource(o);
        distributorContact.setContactInfo(contact);

        distributor.setDistributorContact(distributorContact);
        set = new HashSet();
        set.add(distributor);
        distributionInfo.setDistributors(set);

        //format
        Set<Format> formats  = new HashSet<Format>();

        FormatImpl format = new FormatImpl();
        String name = "MEDATLAS ASCII";
        format.setName(new SimpleInternationalString(name));
        format.setVersion(new SimpleInternationalString("1.0"));
        formats.add(format);

        distributionInfo.setDistributionFormats(formats);

        //transfert options
        DigitalTransferOptionsImpl digiTrans = new DigitalTransferOptionsImpl();

        digiTrans.setTransferSize(2.431640625);

        OnLineResourceImpl onlines = new OnLineResourceImpl();

        String uri = "http://www.ifremer.fr/sismerData/jsp/visualisationMetadata3.jsp?langue=EN&pageOrigine=CS&cle1=42292_1&cle2=CTDF02";
        if (uri != null) {
            onlines.setLinkage(new URI(uri));
        }

        onlines.setDescription(new SimpleInternationalString("CTDF02"));
        onlines.setFunction(OnLineFunction.DOWNLOAD);
        set = new HashSet();
        set.add(onlines);
        digiTrans.setOnLines(set);

        set = new HashSet();
        set.add(digiTrans);
        distributionInfo.setTransferOptions(set);

        expResult.setDistributionInfo(distributionInfo);

        assertEquals(expResult.getApplicationSchemaInfo(), result.getApplicationSchemaInfo());
        assertEquals(expResult.getCharacterSet(), result.getCharacterSet());
        assertEquals(expResult.getContacts().iterator().next().getContactInfo().getAddress().getAdministrativeArea(), result.getContacts().iterator().next().getContactInfo().getAddress().getAdministrativeArea());
        assertEquals(expResult.getContacts().iterator().next().getContactInfo().getAddress().getCity(), result.getContacts().iterator().next().getContactInfo().getAddress().getCity());
        assertEquals(expResult.getContacts().iterator().next().getContactInfo().getAddress().getCountry(), result.getContacts().iterator().next().getContactInfo().getAddress().getCountry());
        assertEquals(expResult.getContacts().iterator().next().getContactInfo().getAddress().getDeliveryPoints(), result.getContacts().iterator().next().getContactInfo().getAddress().getDeliveryPoints());
        assertEquals(expResult.getContacts().iterator().next().getContactInfo().getAddress().getElectronicMailAddresses(), result.getContacts().iterator().next().getContactInfo().getAddress().getElectronicMailAddresses());
        assertEquals(expResult.getContacts().iterator().next().getContactInfo().getAddress().getPostalCode(), result.getContacts().iterator().next().getContactInfo().getAddress().getPostalCode());
        assertEquals(expResult.getContacts().iterator().next().getContactInfo().getAddress(), result.getContacts().iterator().next().getContactInfo().getAddress());
        assertEquals(expResult.getContacts().iterator().next().getContactInfo(), result.getContacts().iterator().next().getContactInfo());
        assertEquals(expResult.getContacts().iterator().next(), result.getContacts().iterator().next());
        assertEquals(expResult.getContentInfo(), result.getContentInfo());
        assertEquals(expResult.getDataQualityInfo(), result.getDataQualityInfo());
        assertEquals(expResult.getDataSetUri(), result.getDataSetUri());
        assertEquals(expResult.getDateStamp(), result.getDateStamp());
        assertEquals(expResult.getDistributionInfo().getDistributionFormats(), result.getDistributionInfo().getDistributionFormats());
        assertEquals(expResult.getDistributionInfo().getDistributors(), result.getDistributionInfo().getDistributors());
        assertEquals(expResult.getDistributionInfo().getTransferOptions(), result.getDistributionInfo().getTransferOptions());
        assertEquals(expResult.getDistributionInfo(), result.getDistributionInfo());
        assertEquals(expResult.getFileIdentifier(), result.getFileIdentifier());
        assertEquals(expResult.getHierarchyLevelNames(), result.getHierarchyLevelNames());
        assertEquals(expResult.getHierarchyLevels(), result.getHierarchyLevels());
        assertEquals(expResult.getLanguage(), result.getLanguage());
        assertEquals(expResult.getLocales(), result.getLocales());
        assertEquals(expResult.getMetadataExtensionInfo(), result.getMetadataExtensionInfo());
        assertEquals(expResult.getMetadataConstraints(), result.getMetadataConstraints());
        assertEquals(expResult.getMetadataMaintenance(), result.getMetadataMaintenance());
        assertEquals(expResult.getMetadataStandardName(), result.getMetadataStandardName());
        assertEquals(expResult.getMetadataStandardVersion(), result.getMetadataStandardVersion());
        assertEquals(expResult.getParentIdentifier(), result.getParentIdentifier());
        assertEquals(expResult.getPortrayalCatalogueInfo(), result.getPortrayalCatalogueInfo());
        assertEquals(expResult.getReferenceSystemInfo().iterator().next().getName().getAuthority().getAlternateTitles(), result.getReferenceSystemInfo().iterator().next().getName().getAuthority().getAlternateTitles());
        assertEquals(expResult.getReferenceSystemInfo().iterator().next().getName().getAuthority().getCitedResponsibleParties(), result.getReferenceSystemInfo().iterator().next().getName().getAuthority().getCitedResponsibleParties());
        assertEquals(expResult.getReferenceSystemInfo().iterator().next().getName().getAuthority(), result.getReferenceSystemInfo().iterator().next().getName().getAuthority());
        assertEquals(expResult.getReferenceSystemInfo().iterator().next().getName(), result.getReferenceSystemInfo().iterator().next().getName());
        assertEquals(expResult.getReferenceSystemInfo().iterator().next(), result.getReferenceSystemInfo().iterator().next());
        assertEquals(expResult.getReferenceSystemInfo(), result.getReferenceSystemInfo());
        assertEquals(expResult.getSpatialRepresentationInfo(), result.getSpatialRepresentationInfo());

        assertEquals(expResult.getIdentificationInfo().iterator().next().getAbstract(), result.getIdentificationInfo().iterator().next().getAbstract());
        assertEquals(expResult.getIdentificationInfo().iterator().next().getAggregationInfo().iterator().next().getAggregateDataSetIdentifier(), result.getIdentificationInfo().iterator().next().getAggregationInfo().iterator().next().getAggregateDataSetIdentifier());
        assertEquals(expResult.getIdentificationInfo().iterator().next().getAggregationInfo().iterator().next().getAggregateDataSetName(), result.getIdentificationInfo().iterator().next().getAggregationInfo().iterator().next().getAggregateDataSetName());
        assertEquals(expResult.getIdentificationInfo().iterator().next().getAggregationInfo().iterator().next().getAssociationType(), result.getIdentificationInfo().iterator().next().getAggregationInfo().iterator().next().getAssociationType());
        assertEquals(expResult.getIdentificationInfo().iterator().next().getAggregationInfo().iterator().next().getInitiativeType(), result.getIdentificationInfo().iterator().next().getAggregationInfo().iterator().next().getInitiativeType());
        assertEquals(expResult.getIdentificationInfo().iterator().next().getAggregationInfo().iterator().next(), result.getIdentificationInfo().iterator().next().getAggregationInfo().iterator().next());
        assertEquals(expResult.getIdentificationInfo().iterator().next().getAggregationInfo(), result.getIdentificationInfo().iterator().next().getAggregationInfo());
        assertEquals(expResult.getIdentificationInfo().iterator().next().getCitation().getCitedResponsibleParties().size(), result.getIdentificationInfo().iterator().next().getCitation().getCitedResponsibleParties().size());
        assertEquals(expResult.getIdentificationInfo().iterator().next().getCitation().getCitedResponsibleParties().iterator().next().getContactInfo().getAddress().getAdministrativeArea(), result.getIdentificationInfo().iterator().next().getCitation().getCitedResponsibleParties().iterator().next().getContactInfo().getAddress().getAdministrativeArea());
        assertEquals(expResult.getIdentificationInfo().iterator().next().getCitation().getCitedResponsibleParties().iterator().next().getContactInfo().getAddress().getCity(), result.getIdentificationInfo().iterator().next().getCitation().getCitedResponsibleParties().iterator().next().getContactInfo().getAddress().getCity());
        assertEquals(expResult.getIdentificationInfo().iterator().next().getCitation().getCitedResponsibleParties().iterator().next().getContactInfo().getAddress().getCountry(), result.getIdentificationInfo().iterator().next().getCitation().getCitedResponsibleParties().iterator().next().getContactInfo().getAddress().getCountry());
        assertEquals(expResult.getIdentificationInfo().iterator().next().getCitation().getCitedResponsibleParties().iterator().next().getContactInfo().getAddress().getDeliveryPoints(), result.getIdentificationInfo().iterator().next().getCitation().getCitedResponsibleParties().iterator().next().getContactInfo().getAddress().getDeliveryPoints());
        assertEquals(expResult.getIdentificationInfo().iterator().next().getCitation().getCitedResponsibleParties().iterator().next().getContactInfo().getAddress().getElectronicMailAddresses(), result.getIdentificationInfo().iterator().next().getCitation().getCitedResponsibleParties().iterator().next().getContactInfo().getAddress().getElectronicMailAddresses());
        assertEquals(expResult.getIdentificationInfo().iterator().next().getCitation().getCitedResponsibleParties().iterator().next().getContactInfo().getAddress().getPostalCode(), result.getIdentificationInfo().iterator().next().getCitation().getCitedResponsibleParties().iterator().next().getContactInfo().getAddress().getPostalCode());
        assertEquals(expResult.getIdentificationInfo().iterator().next().getCitation().getCitedResponsibleParties().iterator().next().getContactInfo().getAddress(), result.getIdentificationInfo().iterator().next().getCitation().getCitedResponsibleParties().iterator().next().getContactInfo().getAddress());
        assertEquals(expResult.getIdentificationInfo().iterator().next().getCitation().getCitedResponsibleParties().iterator().next().getContactInfo().getPhone(), result.getIdentificationInfo().iterator().next().getCitation().getCitedResponsibleParties().iterator().next().getContactInfo().getPhone());
        assertEquals(expResult.getIdentificationInfo().iterator().next().getCitation().getCitedResponsibleParties().iterator().next().getContactInfo(), result.getIdentificationInfo().iterator().next().getCitation().getCitedResponsibleParties().iterator().next().getContactInfo());
        assertEquals(expResult.getIdentificationInfo().iterator().next().getCitation().getCitedResponsibleParties().iterator().next().getIndividualName(), result.getIdentificationInfo().iterator().next().getCitation().getCitedResponsibleParties().iterator().next().getIndividualName());
        assertEquals(expResult.getIdentificationInfo().iterator().next().getCitation().getCitedResponsibleParties().iterator().next(), result.getIdentificationInfo().iterator().next().getCitation().getCitedResponsibleParties().iterator().next());
        assertEquals(expResult.getIdentificationInfo().iterator().next().getCitation().getCitedResponsibleParties(), result.getIdentificationInfo().iterator().next().getCitation().getCitedResponsibleParties());
        assertEquals(expResult.getIdentificationInfo().iterator().next().getCitation(), result.getIdentificationInfo().iterator().next().getCitation());
        assertEquals(expResult.getIdentificationInfo().iterator().next().getCredits(), result.getIdentificationInfo().iterator().next().getCredits());
        assertEquals(expResult.getIdentificationInfo().iterator().next().getDescriptiveKeywords(), result.getIdentificationInfo().iterator().next().getDescriptiveKeywords());
        assertEquals(expResult.getIdentificationInfo().iterator().next().getGraphicOverviews(), result.getIdentificationInfo().iterator().next().getGraphicOverviews());
        assertEquals(expResult.getIdentificationInfo().iterator().next().getPointOfContacts(), result.getIdentificationInfo().iterator().next().getPointOfContacts());
        assertEquals(expResult.getIdentificationInfo().iterator().next().getPurpose(), result.getIdentificationInfo().iterator().next().getPurpose());
        assertEquals(expResult.getIdentificationInfo().iterator().next().getResourceConstraints(), result.getIdentificationInfo().iterator().next().getResourceConstraints());
        assertEquals(expResult.getIdentificationInfo().iterator().next().getResourceSpecificUsages(), result.getIdentificationInfo().iterator().next().getResourceSpecificUsages());
        assertEquals(expResult.getIdentificationInfo().iterator().next().getResourceFormat(), result.getIdentificationInfo().iterator().next().getResourceFormat());
        assertEquals(expResult.getIdentificationInfo().iterator().next().getStatus(), result.getIdentificationInfo().iterator().next().getStatus());
        DataIdentificationImpl expDataIdent = (DataIdentificationImpl) expResult.getIdentificationInfo().iterator().next();
        DataIdentificationImpl resDataIdent = (DataIdentificationImpl) result.getIdentificationInfo().iterator().next();
        assertEquals(expDataIdent.getCharacterSets(), resDataIdent.getCharacterSets());
        assertEquals(expDataIdent.getEnvironmentDescription(), resDataIdent.getEnvironmentDescription());
        assertEquals(expDataIdent.getExtent().iterator().next().getDescription(), resDataIdent.getExtent().iterator().next().getDescription());
        assertEquals(expDataIdent.getExtent().iterator().next().getGeographicElements(), resDataIdent.getExtent().iterator().next().getGeographicElements());
        assertEquals(expDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getMaximumValue(), resDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getMaximumValue());
        assertEquals(expDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getMinimumValue(), resDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getMinimumValue());
        assertEquals(expDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAxis(0).getAbbreviation(), resDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAxis(0).getAbbreviation());
        assertEquals(expDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAxis(0).getDirection(), resDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAxis(0).getDirection());
        assertEquals(expDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAxis(0).getRangeMeaning(), resDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAxis(0).getRangeMeaning());
        assertEquals(expDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAxis(0).getUnit(), resDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAxis(0).getUnit());
        assertEquals(expDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAxis(0).getMaximumValue(), resDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAxis(0).getMaximumValue(), 0.0);
        assertEquals(expDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAxis(0), resDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAxis(0));
        assertEquals(expDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem(), resDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem());
        assertEquals(expDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getDatum().getVerticalDatumType(), resDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getDatum().getVerticalDatumType());
        assertEquals(expDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getDatum().getName(), resDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getDatum().getName());
        assertEquals(expDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getDatum().getScope(), resDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getDatum().getScope());
        assertEquals(expDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getDatum().getAlias(), resDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getDatum().getAlias());
        assertEquals(expDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getDatum().getAnchorPoint(), resDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getDatum().getAnchorPoint());
        assertEquals(expDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getDatum().getDomainOfValidity(), resDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getDatum().getDomainOfValidity());
        assertEquals(expDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getDatum().getIdentifiers(), resDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getDatum().getIdentifiers());
        assertEquals(expDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getDatum().getRealizationEpoch(), resDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getDatum().getRealizationEpoch());
        assertEquals(expDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getDatum(), resDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getDatum());
        assertEquals(expDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getDatum(), resDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getDatum());
        assertEquals(expDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getName(), resDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getName());
        assertEquals(expDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getScope(), resDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getScope());
        assertEquals(expDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getVerticalCRS(), resDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next().getVerticalCRS());
        assertEquals(expDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next(), resDataIdent.getExtent().iterator().next().getVerticalElements().iterator().next());
        assertEquals(expDataIdent.getExtent().iterator().next().getVerticalElements(), resDataIdent.getExtent().iterator().next().getVerticalElements());
        assertEquals(expDataIdent.getExtent().iterator().next().getTemporalElements(), resDataIdent.getExtent().iterator().next().getTemporalElements());
        assertEquals(expDataIdent.getExtent().iterator().next(), resDataIdent.getExtent().iterator().next());
        assertEquals(expDataIdent.getExtent(), resDataIdent.getExtent());
        assertEquals(expDataIdent.getLanguage(), resDataIdent.getLanguage());
        assertEquals(expDataIdent.getSpatialRepresentationTypes(), resDataIdent.getSpatialRepresentationTypes());
        assertEquals(expDataIdent.getSpatialResolutions(), resDataIdent.getSpatialResolutions());
        assertEquals(expDataIdent.getSupplementalInformation(), resDataIdent.getSupplementalInformation());
        assertEquals(expDataIdent.getTopicCategories(), resDataIdent.getTopicCategories());
        assertEquals(expDataIdent, resDataIdent);
        assertEquals(expResult.getIdentificationInfo(), result.getIdentificationInfo());

        assertEquals(expResult, result);

    }

    protected ExtendedElementInformation createExtensionInfo(String name) {
        ExtendedElementInformationImpl element = new ExtendedElementInformationImpl();
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

        KeywordsImpl keyword = new KeywordsImpl();
        Set<InternationalString> kws = new HashSet<InternationalString>();
        if (values != null) {
            for (String value: values) {
                if (value != null) {
                    kws.add(new SimpleInternationalString(value));
                }
            }
        }
        keyword.setKeywords(kws);
        keyword.setType(KeywordType.valueOf(keywordType));

        //we create the citation describing the vocabulary used

        CitationImpl citation = new CitationImpl();
        citation.setTitle(new SimpleInternationalString(title));
        Set set = new HashSet();
        set.add(new SimpleInternationalString(altTitle));
        citation.setAlternateTitles(set);
        CitationDateImpl revisionDate = new CitationDateImpl();
        revisionDate.setDateType(DateType.REVISION);
        Date d = df.parse(date);
        revisionDate.setDate(d);

        set = new HashSet();
        set.add(revisionDate);
        citation.setDates(set);
        citation.setEdition(new SimpleInternationalString(version));
        set = new HashSet();
        set.add(new IdentifierImpl("http://www.seadatanet.org/urnurl/"));
        citation.setIdentifiers(set);
        keyword.setThesaurusName(citation);


        return keyword;
    }

     protected Set<GeographicExtent> createGeographicExtent(String westVar, String eastVar, String southVar, String northVar) {
         Set<GeographicExtent> result = new HashSet<GeographicExtent>();
         double west = 0;
         double east = 0;
         double south = 0;
         double north = 0;

         west  = Double.parseDouble(westVar);
         east  = Double.parseDouble(eastVar);
         south = Double.parseDouble(southVar);
         north = Double.parseDouble(northVar);

         // for point BBOX we replace the westValue equals to 0 by the eastValue (respectively for  north/south)
         if (east == 0) {
             east = west;
         }
         if (north == 0) {
             north = south;
         }

         GeographicExtent geo = new GeographicBoundingBoxImpl(west, east, south, north);
         result.add(geo);
         return result;
    }

}
