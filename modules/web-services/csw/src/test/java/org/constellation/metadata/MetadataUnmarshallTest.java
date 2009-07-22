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
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
import org.geotoolkit.internal.jaxb.metadata.ReferenceIdentifierMetadata;
import org.geotoolkit.internal.jaxb.metadata.ReferenceSystemMetadata;
import org.geotoolkit.metadata.iso.DefaultExtendedElementInformation;
import org.geotoolkit.metadata.iso.DefaultMetaData;
import org.geotoolkit.metadata.iso.DefaultMetadataExtensionInformation;
import org.geotoolkit.metadata.iso.citation.DefaultAddress;
import org.geotoolkit.metadata.iso.citation.DefaultCitationDate;
import org.geotoolkit.metadata.iso.citation.DefaultCitation;
import org.geotoolkit.metadata.iso.citation.DefaultContact;
import org.geotoolkit.metadata.iso.citation.DefaultOnLineResource;
import org.geotoolkit.metadata.iso.citation.DefaultResponsibleParty;
import org.geotoolkit.metadata.iso.citation.DefaultTelephone;
import org.geotoolkit.metadata.iso.constraint.DefaultLegalConstraints;
import org.geotoolkit.metadata.iso.distribution.DefaultDigitalTransferOptions;
import org.geotoolkit.metadata.iso.distribution.DefaultDistribution;
import org.geotoolkit.metadata.iso.distribution.DefaultDistributor;
import org.geotoolkit.metadata.iso.distribution.DefaultFormat;
import org.geotoolkit.metadata.iso.extent.DefaultExtent;
import org.geotoolkit.metadata.iso.extent.DefaultGeographicBoundingBox;
import org.geotoolkit.metadata.iso.extent.DefaultTemporalExtent;
import org.geotoolkit.metadata.iso.extent.DefaultVerticalExtent;
import org.geotoolkit.metadata.iso.identification.DefaultAggregateInformation;
import org.geotoolkit.metadata.iso.identification.DefaultDataIdentification;
import org.geotoolkit.metadata.iso.identification.DefaultKeywords;
import org.geotoolkit.metadata.iso.spatial.DefaultGeometricObjects;
import org.geotoolkit.metadata.iso.spatial.DefaultVectorSpatialRepresentation;
import org.geotoolkit.referencing.NamedIdentifier;
import org.geotoolkit.referencing.crs.DefaultVerticalCRS;
import org.geotoolkit.referencing.cs.DefaultCoordinateSystemAxis;
import org.geotoolkit.referencing.cs.DefaultVerticalCS;
import org.geotoolkit.referencing.datum.DefaultVerticalDatum;
import org.geotoolkit.temporal.object.DefaultInstant;
import org.geotoolkit.temporal.object.DefaultPeriod;
import org.geotoolkit.temporal.object.DefaultPosition;
import org.geotoolkit.util.SimpleInternationalString;

// GeoAPI dependencies
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

/**
 *
 * @author Guilhem Legal
 */
public class MetadataUnmarshallTest {
    private static AnchorPool testPool;
    private static Unmarshaller unmarshaller;
    private static Marshaller marshaller;

    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    @BeforeClass
    public static void setUp() throws JAXBException, URISyntaxException {
        final List<Class> classes = CSWClassesContext.FRA_CLASSES;
        classes.add(DefaultMetaData.class);
        testPool = new AnchorPool(classes);
    }

    @After
    public void releaseMarshallers() {
        if (marshaller != null) {
            testPool.release(marshaller);
            marshaller = null;
        }
        if (unmarshaller != null) {
            testPool.release(unmarshaller);
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
        Object obj = unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/metadata/meta1.xml"));

        assertTrue(obj instanceof DefaultMetaData);
        DefaultMetaData result = (DefaultMetaData) obj;

        DefaultMetaData expResult = new DefaultMetaData();

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
        add.setCountry(new SimpleInternationalString("France"));
        set = new HashSet();
        set.add("sismer@ifremer.fr");
        add.setElectronicMailAddresses(set);
        contact.setAddress(add);
        DefaultOnLineResource o = new DefaultOnLineResource(new URI("http://www.ifremer.fr/sismer/"));
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
        DefaultVectorSpatialRepresentation spatialRep = new DefaultVectorSpatialRepresentation();
        DefaultGeometricObjects geoObj = new DefaultGeometricObjects(GeometricObjectType.valueOf("point"));
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

        DefaultCitation RScitation = new DefaultCitation();

        RScitation.setTitle(new SimpleInternationalString("SeaDataNet geographic co-ordinate reference frames"));
        set = new HashSet();
        set.add(new SimpleInternationalString("L101"));
        RScitation.setAlternateTitles(set);
        set = new HashSet();
        set.add(new ReferenceIdentifierMetadata(null, null, "http://www.seadatanet.org/urnurl/"));
        RScitation.setIdentifiers(set);
        RScitation.setEdition(new SimpleInternationalString("2"));

        ReferenceIdentifierMetadata Nidentifier = new ReferenceIdentifierMetadata(RScitation, "L101", code);
        ReferenceSystemMetadata rs = new ReferenceSystemMetadata(Nidentifier);
        set = new HashSet();
        set.add(rs);
        expResult.setReferenceSystemInfo(set);


        /*
         * extension information
         */
        DefaultMetadataExtensionInformation extensionInfo = new DefaultMetadataExtensionInformation();
        Set<ExtendedElementInformation> elements = new HashSet<ExtendedElementInformation>();

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
        Date d = df.parse("1990-06-05T00:00:00");
        revisionDate.setDate(d);
        set = new HashSet();
        set.add(revisionDate);
        citation.setDates(set);


        Set<ResponsibleParty> originators = new HashSet<ResponsibleParty>();
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
        add.setCountry(new SimpleInternationalString("France"));
        set = new HashSet();
        add.setElectronicMailAddresses(set);
        contact.setAddress(add);
        o = new DefaultOnLineResource(new URI("http://www.com.univ-mrs.fr/LOB/"));
        contact.setOnLineResource(o);
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
        add.setCountry(new SimpleInternationalString("France"));
        set = new HashSet();
        set.add("sismer@ifremer.fr");
        add.setElectronicMailAddresses(set);
        contact.setAddress(add);
        o = new DefaultOnLineResource(new URI("http://www.ifremer.fr/sismer/"));
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
        /*keys.add("Electrical conductivity of the water column");
        keys.add("Dissolved oxygen parameters in the water column");
        keys.add("Light extinction and diffusion coefficients");
        keys.add("Dissolved noble gas concentration parameters in the water column");
        keys.add("Optical backscatter");
        keys.add("Salinity of the water column");
        keys.add("Dissolved concentration parameters for 'other' gases in the water column");
        keys.add("Temperature of the water column");
        keys.add("Visible waveband radiance and irradiance measurements in the atmosphere");
        keys.add("Visible waveband radiance and irradiance measurements in the water column");*/
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
        DefaultLegalConstraints constraint = new DefaultLegalConstraints();
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
        Set<DefaultAggregateInformation> aggregateInfos = new HashSet<DefaultAggregateInformation>();

        //cruise
        DefaultAggregateInformation aggregateInfo = new DefaultAggregateInformation();
        citation = new DefaultCitation();
        citation.setTitle(new SimpleInternationalString("MEDIPROD VI"));
        set = new HashSet();
        set.add(new SimpleInternationalString("90008411"));
        citation.setAlternateTitles(set);
        revisionDate = new DefaultCitationDate();
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
        citation = new DefaultCitation();
        citation.setTitle(new SimpleInternationalString("5p"));
        set = new HashSet();
        set.add(new SimpleInternationalString("5p"));
        citation.setAlternateTitles(set);
        revisionDate = new DefaultCitationDate();
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
        NamedIdentifier datumID = new NamedIdentifier(new DefaultCitation(""), "D28");
        DefaultVerticalCRS vcrs = null;

        Map<String, Object> prop = new HashMap<String, Object>();
        prop.put(DefaultVerticalDatum.NAME_KEY, datumID);
        prop.put(DefaultVerticalDatum.SCOPE_KEY, null);
        DefaultVerticalDatum datum = new DefaultVerticalDatum(prop, VerticalDatumType.ELLIPSOIDAL);


        // vertical coordinate system  TODO var 32 uom?
        HashMap<String, Object> propCoo = new HashMap<String, Object>();
        HashMap<String,String> idProp = new HashMap<String, String>();
        idProp.put(NamedIdentifier.CODESPACE_KEY, "");
        idProp.put(NamedIdentifier.CODE_KEY, "meters");
        idProp.put(NamedIdentifier.AUTHORITY_KEY, "");
        propCoo.put(DefaultCoordinateSystemAxis.NAME_KEY, new NamedIdentifier(idProp));
        propCoo.put(DefaultCoordinateSystemAxis.ALIAS_KEY, DefaultCoordinateSystemAxis.UNDEFINED.getAlias());
        DefaultCoordinateSystemAxis axis = new DefaultCoordinateSystemAxis(propCoo, "meters", AxisDirection.DOWN, Unit.valueOf("m"));

        HashMap<String,Object> csProp = new HashMap<String, Object>();
        NamedIdentifier i = new NamedIdentifier(new DefaultCitation(""), "meters");
        csProp.put(DefaultVerticalCRS.NAME_KEY, i);
        DefaultVerticalCS cs = new DefaultVerticalCS(csProp, axis);

        prop = new HashMap<String, Object>();
        NamedIdentifier idVert = new NamedIdentifier(new DefaultCitation(""), "idvertCRS");
        prop.put(DefaultVerticalCRS.NAME_KEY, idVert);
        prop.put(DefaultVerticalCRS.SCOPE_KEY, null);
        //prop.put(DefaultVerticalCRS.ALIAS_KEY, DefaultCoordinateSystemAxis.UNDEFINED.getAlias());
        vcrs = new DefaultVerticalCRS(prop, datum, cs);


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
        add.setCountry(new SimpleInternationalString("France"));
        set = new HashSet();
        set.add("sismer@ifremer.fr");
        add.setElectronicMailAddresses(set);
        contact.setAddress(add);
        o = new DefaultOnLineResource(new URI("http://www.ifremer.fr/sismer/"));
        contact.setOnLineResource(o);
        distributorContact.setContactInfo(contact);

        distributor.setDistributorContact(distributorContact);
        set = new HashSet();
        set.add(distributor);
        distributionInfo.setDistributors(set);

        //format
        Set<Format> formats  = new HashSet<Format>();

        DefaultFormat format = new DefaultFormat();
        String name = "MEDATLAS ASCII";
        format.setName(new SimpleInternationalString(name));
        format.setVersion(new SimpleInternationalString("1.0"));
        formats.add(format);

        distributionInfo.setDistributionFormats(formats);

        //transfert options
        DefaultDigitalTransferOptions digiTrans = new DefaultDigitalTransferOptions();

        digiTrans.setTransferSize(2.431640625);

        DefaultOnLineResource onlines = new DefaultOnLineResource();

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
        assertEquals(expResult.getReferenceSystemInfo().iterator().next().getName().getVersion(), result.getReferenceSystemInfo().iterator().next().getName().getVersion());
        assertEquals(expResult.getReferenceSystemInfo().iterator().next().getName().getCodeSpace(), result.getReferenceSystemInfo().iterator().next().getName().getCodeSpace());
        assertEquals(expResult.getReferenceSystemInfo().iterator().next().getName(), result.getReferenceSystemInfo().iterator().next().getName());
        assertEquals(expResult.getReferenceSystemInfo().iterator().next().getDomainOfValidity(), result.getReferenceSystemInfo().iterator().next().getDomainOfValidity());
        assertEquals(expResult.getReferenceSystemInfo().iterator().next().getScope(), result.getReferenceSystemInfo().iterator().next().getScope());
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
        assertEquals(expResult.getIdentificationInfo().iterator().next().getDescriptiveKeywords().iterator().next(), result.getIdentificationInfo().iterator().next().getDescriptiveKeywords().iterator().next());
        assertEquals(expResult.getIdentificationInfo().iterator().next().getDescriptiveKeywords(), result.getIdentificationInfo().iterator().next().getDescriptiveKeywords());
        assertEquals(expResult.getIdentificationInfo().iterator().next().getGraphicOverviews(), result.getIdentificationInfo().iterator().next().getGraphicOverviews());
        assertEquals(expResult.getIdentificationInfo().iterator().next().getPointOfContacts(), result.getIdentificationInfo().iterator().next().getPointOfContacts());
        assertEquals(expResult.getIdentificationInfo().iterator().next().getPurpose(), result.getIdentificationInfo().iterator().next().getPurpose());
        assertEquals(expResult.getIdentificationInfo().iterator().next().getResourceConstraints(), result.getIdentificationInfo().iterator().next().getResourceConstraints());
        assertEquals(expResult.getIdentificationInfo().iterator().next().getResourceSpecificUsages(), result.getIdentificationInfo().iterator().next().getResourceSpecificUsages());
        assertEquals(expResult.getIdentificationInfo().iterator().next().getResourceFormats(), result.getIdentificationInfo().iterator().next().getResourceFormats());
        assertEquals(expResult.getIdentificationInfo().iterator().next().getStatus(), result.getIdentificationInfo().iterator().next().getStatus());
        DefaultDataIdentification expDataIdent = (DefaultDataIdentification) expResult.getIdentificationInfo().iterator().next();
        DefaultDataIdentification resDataIdent = (DefaultDataIdentification) result.getIdentificationInfo().iterator().next();
        assertEquals(expDataIdent.getCharacterSets(), resDataIdent.getCharacterSets());
        assertEquals(expDataIdent.getEnvironmentDescription(), resDataIdent.getEnvironmentDescription());
        assertEquals(expDataIdent.getExtents().iterator().next().getDescription(), resDataIdent.getExtents().iterator().next().getDescription());
        assertEquals(expDataIdent.getExtents().iterator().next().getGeographicElements(), resDataIdent.getExtents().iterator().next().getGeographicElements());
        assertEquals(expDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getMaximumValue(), resDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getMaximumValue());
        assertEquals(expDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getMinimumValue(), resDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getMinimumValue());
        assertEquals(expDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAxis(0).getAbbreviation(), resDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAxis(0).getAbbreviation());
        assertEquals(expDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAxis(0).getDirection(), resDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAxis(0).getDirection());
        assertEquals(expDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAxis(0).getRangeMeaning(), resDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAxis(0).getRangeMeaning());
        assertEquals(expDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAxis(0).getUnit(), resDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAxis(0).getUnit());
        assertEquals(expDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAxis(0).getMaximumValue(), resDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAxis(0).getMaximumValue(), 0.0);
        assertEquals(expDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAxis(0).getName().getCode(), resDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAxis(0).getName().getCode());
        assertEquals(expDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAxis(0).getName().getCodeSpace(), resDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAxis(0).getName().getCodeSpace());
        assertEquals(expDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAxis(0).getName().getAuthority(), resDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAxis(0).getName().getAuthority());
        assertEquals(expDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAxis(0).getName(), resDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAxis(0).getName());
        assertEquals(expDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAxis(0).getMinimumValue(), resDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAxis(0).getMinimumValue(), 0.0);
        assertEquals(expDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAxis(0).getIdentifiers(), resDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAxis(0).getIdentifiers());
        assertEquals(expDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAxis(0).getRemarks(), resDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAxis(0).getRemarks());
        assertEquals(expDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAxis(0).getAlias(), resDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAxis(0).getAlias());
        assertEquals(expDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAxis(0), resDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAxis(0));
        assertEquals(expDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAlias(), resDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getAlias());
        assertEquals(expDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getDimension(), resDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getDimension());
        assertEquals(expDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getIdentifiers(), resDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getIdentifiers());
        assertEquals(expDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getName().getAuthority(), resDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getName().getAuthority());
        assertEquals(expDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getName().getCodeSpace(), resDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getName().getCodeSpace());
        assertEquals(expDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getName(), resDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem().getName());
        assertEquals(expDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem(), resDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getCoordinateSystem());
        assertEquals(expDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getDatum().getVerticalDatumType(), resDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getDatum().getVerticalDatumType());
        assertEquals(expDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getDatum().getName(), resDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getDatum().getName());
        assertEquals(expDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getDatum().getScope(), resDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getDatum().getScope());
        assertEquals(expDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getDatum().getAlias(), resDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getDatum().getAlias());
        assertEquals(expDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getDatum().getAnchorPoint(), resDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getDatum().getAnchorPoint());
        assertEquals(expDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getDatum().getDomainOfValidity(), resDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getDatum().getDomainOfValidity());
        assertEquals(expDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getDatum().getIdentifiers(), resDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getDatum().getIdentifiers());
        assertEquals(expDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getDatum().getRealizationEpoch(), resDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getDatum().getRealizationEpoch());
        assertEquals(expDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getDatum(), resDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getDatum());
        assertEquals(expDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getDatum(), resDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getDatum());
        assertEquals(expDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getName(), resDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getName());
        assertEquals(expDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getScope(), resDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getScope());
        assertEquals(expDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getAlias(), resDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS().getAlias());
        assertEquals(expDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS(), resDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next().getVerticalCRS());
        assertEquals(expDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next(), resDataIdent.getExtents().iterator().next().getVerticalElements().iterator().next());
        assertEquals(expDataIdent.getExtents().iterator().next().getVerticalElements(), resDataIdent.getExtents().iterator().next().getVerticalElements());
        assertEquals(expDataIdent.getExtents().iterator().next().getTemporalElements(), resDataIdent.getExtents().iterator().next().getTemporalElements());
        assertEquals(expDataIdent.getExtents().iterator().next(), resDataIdent.getExtents().iterator().next());
        assertEquals(expDataIdent.getExtents(), resDataIdent.getExtents());
        assertEquals(expDataIdent.getLanguages(), resDataIdent.getLanguages());
        assertEquals(expDataIdent.getSpatialRepresentationTypes(), resDataIdent.getSpatialRepresentationTypes());
        assertEquals(expDataIdent.getSpatialResolutions(), resDataIdent.getSpatialResolutions());
        assertEquals(expDataIdent.getSupplementalInformation(), resDataIdent.getSupplementalInformation());
        assertEquals(expDataIdent.getTopicCategories(), resDataIdent.getTopicCategories());
        assertEquals(expDataIdent, resDataIdent);
        assertEquals(expResult.getIdentificationInfo(), result.getIdentificationInfo());

        assertEquals(expResult, result);

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

        DefaultCitation citation = new DefaultCitation();
        citation.setTitle(new SimpleInternationalString(title));
        Set set = new HashSet();
        set.add(new SimpleInternationalString(altTitle));
        citation.setAlternateTitles(set);
        DefaultCitationDate revisionDate = new DefaultCitationDate();
        revisionDate.setDateType(DateType.REVISION);
        Date d = df.parse(date);
        revisionDate.setDate(d);

        set = new HashSet();
        set.add(revisionDate);
        citation.setDates(set);
        citation.setEdition(new SimpleInternationalString(version));
        set = new HashSet();
        set.add(new ReferenceIdentifierMetadata(null, null, "http://www.seadatanet.org/urnurl/"));
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

        DefaultMetaData metadata     = new DefaultMetaData();

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
        add.setCountry(new SimpleInternationalString("France"));
        set = new HashSet();
        set.add("sismer@ifremer.fr");
        add.setElectronicMailAddresses(set);
        contact.setAddress(add);
        DefaultOnLineResource o = new DefaultOnLineResource(new URI("http://www.ifremer.fr/sismer/"));
        contact.setOnLineResource(o);
        author.setContactInfo(contact);
        set = new HashSet();
        set.add(author);
        metadata.setContacts(set);

        /*
         * creation date
         */
        metadata.setDateStamp(df.parse("2009-01-01T00:00:00"));

        /*
         * Spatial representation info
         */
        DefaultVectorSpatialRepresentation spatialRep = new DefaultVectorSpatialRepresentation();
        DefaultGeometricObjects geoObj = new DefaultGeometricObjects(GeometricObjectType.valueOf("point"));
        set = new HashSet();
        set.add(geoObj);
        spatialRep.setGeometricObjects(set);

        set = new HashSet();
        set.add(spatialRep);
        metadata.setSpatialRepresentationInfo(set);

        /*
         * Reference system info
         */
        String code = "World Geodetic System 84";

        DefaultCitation RScitation = new DefaultCitation();

        RScitation.setTitle(new SimpleInternationalString("SeaDataNet geographic co-ordinate reference frames"));
        set = new HashSet();
        set.add(new SimpleInternationalString("L101"));
        RScitation.setAlternateTitles(set);
        set = new HashSet();
        set.add(new ReferenceIdentifierMetadata(null, null, "http://www.seadatanet.org/urnurl/"));
        RScitation.setIdentifiers(set);
        RScitation.setEdition(new SimpleInternationalString("2"));

        ReferenceIdentifierMetadata Nidentifier = new ReferenceIdentifierMetadata(RScitation, "L101", code);
        ReferenceSystemMetadata rs = new ReferenceSystemMetadata(Nidentifier);
        set = new HashSet();
        set.add(rs);
        metadata.setReferenceSystemInfo(set);

        /*
         * extension information
         */
        DefaultMetadataExtensionInformation extensionInfo = new DefaultMetadataExtensionInformation();
        Set<ExtendedElementInformation> elements = new HashSet<ExtendedElementInformation>();

        //we only keep one element for test purpose (unordered list)
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
        Date d = df.parse("1990-06-05T00:00:00");
        revisionDate.setDate(d);
        set = new HashSet();
        set.add(revisionDate);
        citation.setDates(set);


        Set<ResponsibleParty> originators = new HashSet<ResponsibleParty>();
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
        add.setCountry(new SimpleInternationalString("France"));
        set = new HashSet();
        add.setElectronicMailAddresses(set);
        contact.setAddress(add);
        o = new DefaultOnLineResource(new URI("http://www.com.univ-mrs.fr/LOB/"));
        contact.setOnLineResource(o);
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
        add.setCountry(new SimpleInternationalString("France"));
        set = new HashSet();
        set.add("sismer@ifremer.fr");
        add.setElectronicMailAddresses(set);
        contact.setAddress(add);
        o = new DefaultOnLineResource(new URI("http://www.ifremer.fr/sismer/"));
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
        /*keys.add("Electrical conductivity of the water column");
        keys.add("Dissolved oxygen parameters in the water column");
        keys.add("Light extinction and diffusion coefficients");
        keys.add("Dissolved noble gas concentration parameters in the water column");
        keys.add("Optical backscatter");
        keys.add("Salinity of the water column");
        keys.add("Dissolved concentration parameters for 'other' gases in the water column");
        keys.add("Temperature of the water column");
        keys.add("Visible waveband radiance and irradiance measurements in the atmosphere");
        keys.add("Visible waveband radiance and irradiance measurements in the water column");*/
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
        DefaultLegalConstraints constraint = new DefaultLegalConstraints();
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
        Set<DefaultAggregateInformation> aggregateInfos = new HashSet<DefaultAggregateInformation>();

        //cruise
        DefaultAggregateInformation aggregateInfo = new DefaultAggregateInformation();
        citation = new DefaultCitation();
        citation.setTitle(new SimpleInternationalString("MEDIPROD VI"));
        set = new HashSet();
        set.add(new SimpleInternationalString("90008411"));
        citation.setAlternateTitles(set);
        revisionDate = new DefaultCitationDate();
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
        citation = new DefaultCitation();
        citation.setTitle(new SimpleInternationalString("5p"));
        set = new HashSet();
        set.add(new SimpleInternationalString("5p"));
        citation.setAlternateTitles(set);
        revisionDate = new DefaultCitationDate();
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
        dataIdentification.setExtents(set);

        set = new HashSet();
        set.add(dataIdentification);
        metadata.setIdentificationInfo(set);

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
        add.setCountry(new SimpleInternationalString("France"));
        set = new HashSet();
        set.add("sismer@ifremer.fr");
        add.setElectronicMailAddresses(set);
        contact.setAddress(add);
        o = new DefaultOnLineResource(new URI("http://www.ifremer.fr/sismer/"));
        contact.setOnLineResource(o);
        distributorContact.setContactInfo(contact);

        distributor.setDistributorContact(distributorContact);
        set = new HashSet();
        set.add(distributor);
        distributionInfo.setDistributors(set);

        //format
        Set<Format> formats  = new HashSet<Format>();

        DefaultFormat format = new DefaultFormat();
        String name = "MEDATLAS ASCII";
        format.setName(new SimpleInternationalString(name));
        format.setVersion(new SimpleInternationalString("1.0"));
        formats.add(format);

        distributionInfo.setDistributionFormats(formats);

        //transfert options
        DefaultDigitalTransferOptions digiTrans = new DefaultDigitalTransferOptions();

        digiTrans.setTransferSize(2.431640625);

        DefaultOnLineResource onlines = new DefaultOnLineResource();

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

        metadata.setDistributionInfo(distributionInfo);

        StringWriter sw = new StringWriter();
        marshaller = testPool.acquireMarshaller();
        marshaller.marshal(metadata, sw);
        String result = sw.toString();

        InputStream in = Util.getResourceAsStream("org/constellation/metadata/meta1.xml");
        StringWriter out = new StringWriter();
        byte[] buffer = new byte[1024];
        int size;

        while ((size = in.read(buffer, 0, 1024)) > 0) {
            out.write(new String(buffer, 0, size));
        }

        String expResult = out.toString();

        //we remove the header (variable)
        expResult = expResult.substring(expResult.indexOf("<gmd:fileIdentifier>"));
        result    = result.substring(result.indexOf("<gmd:fileIdentifier>"));

        //we split the result into several piece to easier debug
        int startPosition = 0;
        String expResult1 = expResult.substring(startPosition, expResult.indexOf("<gmd:contact>"));
        String result1    =    result.substring(startPosition, result.indexOf("<gmd:contact>"));

        assertEquals(expResult1, result1);

        startPosition     += expResult1.length();
        String expResult2 = expResult.substring(startPosition, expResult.indexOf("</gmd:dateStamp>"));
        String result2    =    result.substring(startPosition, result.indexOf("</gmd:dateStamp>"));

        assertEquals(expResult2, result2);

        startPosition     += expResult2.length();
        String expResult3 = expResult.substring(startPosition, expResult.indexOf("</gmd:referenceSystemInfo>"));
        String result3    =    result.substring(startPosition, result.indexOf("</gmd:referenceSystemInfo>"));

        assertEquals(expResult3, result3);

        startPosition     = startPosition + expResult3.length();
        String expResult4 = expResult.substring(startPosition, expResult.indexOf("<gmd:identificationInfo>"));
        String result4    =    result.substring(startPosition, result.indexOf("<gmd:identificationInfo>"));

        assertEquals(expResult4, result4);

        startPosition     = startPosition + expResult4.length();
        String expResult5 = expResult.substring(startPosition, expResult.indexOf("</gmd:abstract>"));
        String result5    =    result.substring(startPosition, result.indexOf("</gmd:abstract>"));

        assertEquals(expResult5, result5);

        startPosition     = startPosition + expResult5.length();
        String expResult6 = expResult.substring(startPosition, expResult.indexOf("<gmd:descriptiveKeywords>"));
        String result6    =    result.substring(startPosition, result.indexOf("<gmd:descriptiveKeywords>"));

        assertEquals(expResult6, result6);

        startPosition     = startPosition + expResult6.length();
        String expResult7 = expResult.substring(startPosition, expResult.indexOf("</gmd:descriptiveKeywords>"));
        String result7    =    result.substring(startPosition, result.indexOf("</gmd:descriptiveKeywords>"));

        assertEquals(expResult7, result7);

        startPosition     = startPosition + expResult7.length();
        String expResult8 = expResult.substring(startPosition, expResult.indexOf("</gmd:aggregationInfo>"));
        String result8    =    result.substring(startPosition, result.indexOf("</gmd:aggregationInfo>"));

        assertEquals(expResult8, result8);

        startPosition     = startPosition + expResult8.length();
        String expResult9 = expResult.substring(startPosition, expResult.indexOf("</gmd:temporalElement>"));
        String result9    =    result.substring(startPosition, result.indexOf("</gmd:temporalElement>"));

        assertEquals(expResult9, result9);

        startPosition     = startPosition + expResult9.length();
        String expResult10 = expResult.substring(startPosition, expResult.indexOf("</gmd:identificationInfo>"));
        String result10    =    result.substring(startPosition, result.indexOf("</gmd:identificationInfo>"));

        assertEquals(expResult10, result10);

        startPosition     = startPosition + expResult10.length();
        String expResult11 = expResult.substring(startPosition, expResult.indexOf("</gmd:distributor>"));
        String result11    =    result.substring(startPosition, result.indexOf("</gmd:distributor>"));

        assertEquals(expResult11, result11);

        assertEquals(expResult, result);

    }

}
