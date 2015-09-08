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
package org.constellation.admin;

import org.constellation.business.IMetadataBusiness;
import org.constellation.business.IServiceBusiness;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.configuration.ConfigurationException;
import org.constellation.database.api.jooq.tables.pojos.Metadata;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author guilhem
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/cstl/spring/test-context.xml")
@ActiveProfiles({"standard" })
public class MetadataBusinessTest {
    
    @Autowired
    private IMetadataBusiness metadataBusiness;

    @Autowired
    private IServiceBusiness serviceBusiness;

    @BeforeClass
    public static void initTestDir() {
        ConfigDirectory.setupTestEnvironement("MetadataBusinessTest");
    }

    @PostConstruct
    public void init() {
        clean();
    }

    @AfterClass
    public static void destroy() {
        clean();
    }

    private static void clean() {
        try {
            SpringHelper.getBean(IServiceBusiness.class).deleteAll();
            SpringHelper.getBean(IMetadataBusiness.class).deleteAllMetadata();
        } catch (ConfigurationException ex) {
            Logger.getLogger(MetadataBusinessTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        ConfigDirectory.shutdownTestEnvironement("MetadataBusinessTest");
    }

    @Test
    public void createMetadata() throws ConfigurationException {
        final Metadata metadata = metadataBusiness.updateMetadata("test", BIG_XML);
        
        final Metadata read = metadataBusiness.getMetadataById(metadata.getId());
        
        Assert.assertEquals(BIG_XML.length(), read.getMetadataIso().length());
        Assert.assertEquals(BIG_XML, read.getMetadataIso());
    }
    
    
    private static String BIG_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
"<gmd:MD_Metadata xmlns:gco=\"http://www.isotc211.org/2005/gco\"\n" +
"                 xmlns:gmd=\"http://www.isotc211.org/2005/gmd\"\n" +
"                 xmlns:fra=\"http://www.cnig.gouv.fr/2005/fra\"\n" +
"                 xmlns:gmx=\"http://www.isotc211.org/2005/gmx\"\n" +
"                 xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n" +
"                 xmlns:gml=\"http://www.opengis.net/gml\">\n" +
"    <gmd:fileIdentifier>\n" +
"        <gco:CharacterString>42292_9s_19900610041000</gco:CharacterString>\n" +
"    </gmd:fileIdentifier>\n" +
"    <gmd:language>\n" +
"        <gmd:LanguageCode codeList=\"http://schemas.opengis.net/iso/19139/20070417/resources/Codelist/ML_gmxCodelists.xml#LanguageCode\" codeListValue=\"eng\">eng</gmd:LanguageCode>\n" +
"    </gmd:language>\n" +
"    <gmd:characterSet>\n" +
"        <gmd:MD_CharacterSetCode codeList=\"http://www.tc211.org/ISO19139/resources/codeList.xml#utf8\" codeListValue=\"utf8\"/>\n" +
"    </gmd:characterSet>\n" +
"    <gmd:hierarchyLevel>\n" +
"        <gmd:MD_ScopeCode codeList=\"http://www.tc211.org/ISO19139/resources/codeList.xml#dataset\" codeListValue=\"dataset\"/>\n" +
"    </gmd:hierarchyLevel>\n" +
"    <gmd:hierarchyLevelName>\n" +
"        <gmx:Anchor xlink:href=\"SDN:L231:3:CDI\">Common Data Index record</gmx:Anchor>\n" +
"    </gmd:hierarchyLevelName>\n" +
"    <gmd:contact>\n" +
"        <gmd:CI_ResponsibleParty>\n" +
"            <gmd:organisationName>\n" +
"                <gco:CharacterString>IFREMER / IDM/SISMER</gco:CharacterString>\n" +
"            </gmd:organisationName>\n" +
"            <gmd:contactInfo>\n" +
"                <gmd:CI_Contact>\n" +
"                    <gmd:phone>\n" +
"                        <gmd:CI_Telephone>\n" +
"                            <gmd:voice>\n" +
"                                <gco:CharacterString>+33 (0)2 98.22.49.16</gco:CharacterString>\n" +
"                            </gmd:voice>\n" +
"                            <gmd:facsimile>\n" +
"                                <gco:CharacterString>+33 (0)2 98.22.46.44</gco:CharacterString>\n" +
"                            </gmd:facsimile>\n" +
"                        </gmd:CI_Telephone>\n" +
"                    </gmd:phone>\n" +
"                    <gmd:address>\n" +
"                        <gmd:CI_Address>\n" +
"                            <gmd:deliveryPoint>\n" +
"                                <gco:CharacterString>Centre IFREMER de Brest BP 70</gco:CharacterString>\n" +
"                            </gmd:deliveryPoint>\n" +
"                            <gmd:city>\n" +
"                                <gco:CharacterString>PLOUZANE</gco:CharacterString>\n" +
"                            </gmd:city>\n" +
"                            <gmd:postalCode>\n" +
"                                <gco:CharacterString>29280</gco:CharacterString>\n" +
"                            </gmd:postalCode>\n" +
"                            <gmd:country>\n" +
"                                <gmx:Anchor xlink:href=\"SDN:C320:2:FR\">France</gmx:Anchor>\n" +
"                            </gmd:country>\n" +
"                            <gmd:electronicMailAddress>\n" +
"                                <gco:CharacterString>sismer@ifremer.fr</gco:CharacterString>\n" +
"                            </gmd:electronicMailAddress>\n" +
"                        </gmd:CI_Address>\n" +
"                    </gmd:address>\n" +
"                    <gmd:onlineResource>\n" +
"                        <gmd:CI_OnlineResource>\n" +
"                            <gmd:linkage>\n" +
"                                <gmd:URL>http://www.ifremer.fr/sismer/</gmd:URL>\n" +
"                            </gmd:linkage>\n" +
"                            <gmd:protocol>\n" +
"                                <gco:CharacterString>http</gco:CharacterString>\n" +
"                            </gmd:protocol>\n" +
"                        </gmd:CI_OnlineResource>\n" +
"                    </gmd:onlineResource>\n" +
"                </gmd:CI_Contact>\n" +
"            </gmd:contactInfo>\n" +
"            <gmd:role>\n" +
"                <gmd:CI_RoleCode codeList=\"http://www.tc211.org/ISO19139/resources/codeList.xml#author\" codeListValue=\"author\"/>\n" +
"            </gmd:role>\n" +
"        </gmd:CI_ResponsibleParty>\n" +
"    </gmd:contact>\n" +
"    <gmd:dateStamp>\n" +
"        <gco:DateTime>2009-01-26T13:00:00+02:00</gco:DateTime>\n" +
"    </gmd:dateStamp>\n" +
"    <gmd:spatialRepresentationInfo>\n" +
"        <gmd:MD_VectorSpatialRepresentation>\n" +
"            <gmd:geometricObjects>\n" +
"                <gmd:MD_GeometricObjects>\n" +
"                    <gmd:geometricObjectType>\n" +
"                        <gmd:MD_GeometricObjectTypeCode codeList=\"http://www.tc211.org/ISO19139/resources/codeList.xml#point\" codeListValue=\"point\"/>\n" +
"                    </gmd:geometricObjectType>\n" +
"                </gmd:MD_GeometricObjects>\n" +
"            </gmd:geometricObjects>\n" +
"        </gmd:MD_VectorSpatialRepresentation>\n" +
"    </gmd:spatialRepresentationInfo>\n" +
"    <gmd:referenceSystemInfo>\n" +
"        <gmd:MD_ReferenceSystem>\n" +
"            <gmd:referenceSystemIdentifier>\n" +
"                <gmd:RS_Identifier>\n" +
"                    <gmd:authority>\n" +
"                        <gmd:CI_Citation>\n" +
"                            <gmd:title>\n" +
"                                <gco:CharacterString>SeaDataNet geographic co-ordinate reference frames</gco:CharacterString>\n" +
"                            </gmd:title>\n" +
"                            <gmd:alternateTitle>\n" +
"                                <gco:CharacterString>L101</gco:CharacterString>\n" +
"                            </gmd:alternateTitle>\n" +
"                            <gmd:edition>\n" +
"                                <gmx:Anchor xlink:href=\"SDN:C371:1:2\">2</gmx:Anchor>\n" +
"                            </gmd:edition>\n" +
"                            <gmd:identifier>\n" +
"                                <gmd:RS_Identifier>\n" +
"                                    <gmd:code>\n" +
"                                        <gco:CharacterString>http://www.seadatanet.org/urnurl/</gco:CharacterString>\n" +
"                                    </gmd:code>\n" +
"                                </gmd:RS_Identifier>\n" +
"                            </gmd:identifier>\n" +
"                        </gmd:CI_Citation>\n" +
"                    </gmd:authority>\n" +
"                    <gmd:code>\n" +
"                        <gmx:Anchor xlink:href=\"SDN:L101:2:4326\">World Geodetic System 84</gmx:Anchor>\n" +
"                    </gmd:code>\n" +
"                </gmd:RS_Identifier>\n" +
"            </gmd:referenceSystemIdentifier>\n" +
"        </gmd:MD_ReferenceSystem>\n" +
"    </gmd:referenceSystemInfo>\n" +
"    <gmd:metadataExtensionInfo>\n" +
"        <gmd:MD_MetadataExtensionInformation>\n" +
"            <gmd:extendedElementInformation>\n" +
"                <gmd:MD_ExtendedElementInformation>\n" +
"                    <gmd:name>\n" +
"                        <gco:CharacterString>SDN:EDMO::</gco:CharacterString>\n" +
"                    </gmd:name>\n" +
"                    <gmd:definition>\n" +
"                        <gco:CharacterString>http://www.seadatanet.org/urnurl/</gco:CharacterString>\n" +
"                    </gmd:definition>\n" +
"                    <gmd:dataType>\n" +
"                        <gmd:MD_DatatypeCode codeList=\"http://www.tc211.org/ISO19139/resources/codeList.xml#codelist\" codeListValue=\"codelist\"/>\n" +
"                    </gmd:dataType>\n" +
"                    <gmd:parentEntity>\n" +
"                        <gco:CharacterString>SeaDataNet</gco:CharacterString>\n" +
"                    </gmd:parentEntity>\n" +
"                </gmd:MD_ExtendedElementInformation>\n" +
"            </gmd:extendedElementInformation>\n" +
"            <gmd:extendedElementInformation>\n" +
"                <gmd:MD_ExtendedElementInformation>\n" +
"                    <gmd:name>\n" +
"                        <gco:CharacterString>SDN:L021:1:</gco:CharacterString>\n" +
"                    </gmd:name>\n" +
"                    <gmd:definition>\n" +
"                        <gco:CharacterString>http://www.seadatanet.org/urnurl/</gco:CharacterString>\n" +
"                    </gmd:definition>\n" +
"                    <gmd:dataType>\n" +
"                        <gmd:MD_DatatypeCode codeList=\"http://www.tc211.org/ISO19139/resources/codeList.xml#codelist\" codeListValue=\"codelist\"/>\n" +
"                    </gmd:dataType>\n" +
"                    <gmd:parentEntity>\n" +
"                        <gco:CharacterString>SeaDataNet</gco:CharacterString>\n" +
"                    </gmd:parentEntity>\n" +
"                </gmd:MD_ExtendedElementInformation>\n" +
"            </gmd:extendedElementInformation>\n" +
"            <gmd:extendedElementInformation>\n" +
"                <gmd:MD_ExtendedElementInformation>\n" +
"                    <gmd:name>\n" +
"                        <gco:CharacterString>SDN:L031:2:</gco:CharacterString>\n" +
"                    </gmd:name>\n" +
"                    <gmd:definition>\n" +
"                        <gco:CharacterString>http://www.seadatanet.org/urnurl/</gco:CharacterString>\n" +
"                    </gmd:definition>\n" +
"                    <gmd:dataType>\n" +
"                        <gmd:MD_DatatypeCode codeList=\"http://www.tc211.org/ISO19139/resources/codeList.xml#codelist\" codeListValue=\"codelist\"/>\n" +
"                    </gmd:dataType>\n" +
"                    <gmd:parentEntity>\n" +
"                        <gco:CharacterString>SeaDataNet</gco:CharacterString>\n" +
"                    </gmd:parentEntity>\n" +
"                </gmd:MD_ExtendedElementInformation>\n" +
"            </gmd:extendedElementInformation>\n" +
"            <gmd:extendedElementInformation>\n" +
"                <gmd:MD_ExtendedElementInformation>\n" +
"                    <gmd:name>\n" +
"                        <gco:CharacterString>SDN:L071:1:</gco:CharacterString>\n" +
"                    </gmd:name>\n" +
"                    <gmd:definition>\n" +
"                        <gco:CharacterString>http://www.seadatanet.org/urnurl/</gco:CharacterString>\n" +
"                    </gmd:definition>\n" +
"                    <gmd:dataType>\n" +
"                        <gmd:MD_DatatypeCode codeList=\"http://www.tc211.org/ISO19139/resources/codeList.xml#codelist\" codeListValue=\"codelist\"/>\n" +
"                    </gmd:dataType>\n" +
"                    <gmd:parentEntity>\n" +
"                        <gco:CharacterString>SeaDataNet</gco:CharacterString>\n" +
"                    </gmd:parentEntity>\n" +
"                </gmd:MD_ExtendedElementInformation>\n" +
"            </gmd:extendedElementInformation>\n" +
"            <gmd:extendedElementInformation>\n" +
"                <gmd:MD_ExtendedElementInformation>\n" +
"                    <gmd:name>\n" +
"                        <gco:CharacterString>SDN:L081:1:</gco:CharacterString>\n" +
"                    </gmd:name>\n" +
"                    <gmd:definition>\n" +
"                        <gco:CharacterString>http://www.seadatanet.org/urnurl/</gco:CharacterString>\n" +
"                    </gmd:definition>\n" +
"                    <gmd:dataType>\n" +
"                        <gmd:MD_DatatypeCode codeList=\"http://www.tc211.org/ISO19139/resources/codeList.xml#codelist\" codeListValue=\"codelist\"/>\n" +
"                    </gmd:dataType>\n" +
"                    <gmd:parentEntity>\n" +
"                        <gco:CharacterString>SeaDataNet</gco:CharacterString>\n" +
"                    </gmd:parentEntity>\n" +
"                </gmd:MD_ExtendedElementInformation>\n" +
"            </gmd:extendedElementInformation>\n" +
"            <gmd:extendedElementInformation>\n" +
"                <gmd:MD_ExtendedElementInformation>\n" +
"                    <gmd:name>\n" +
"                        <gco:CharacterString>SDN:L231:3:</gco:CharacterString>\n" +
"                    </gmd:name>\n" +
"                    <gmd:definition>\n" +
"                        <gco:CharacterString>http://www.seadatanet.org/urnurl/</gco:CharacterString>\n" +
"                    </gmd:definition>\n" +
"                    <gmd:dataType>\n" +
"                        <gmd:MD_DatatypeCode codeList=\"http://www.tc211.org/ISO19139/resources/codeList.xml#codelist\" codeListValue=\"codelist\"/>\n" +
"                    </gmd:dataType>\n" +
"                    <gmd:parentEntity>\n" +
"                        <gco:CharacterString>SeaDataNet</gco:CharacterString>\n" +
"                    </gmd:parentEntity>\n" +
"                </gmd:MD_ExtendedElementInformation>\n" +
"            </gmd:extendedElementInformation>\n" +
"            <gmd:extendedElementInformation>\n" +
"                <gmd:MD_ExtendedElementInformation>\n" +
"                    <gmd:name>\n" +
"                        <gco:CharacterString>SDN:L241:1:</gco:CharacterString>\n" +
"                    </gmd:name>\n" +
"                    <gmd:definition>\n" +
"                        <gco:CharacterString>http://www.seadatanet.org/urnurl/</gco:CharacterString>\n" +
"                    </gmd:definition>\n" +
"                    <gmd:dataType>\n" +
"                        <gmd:MD_DatatypeCode codeList=\"http://www.tc211.org/ISO19139/resources/codeList.xml#codelist\" codeListValue=\"codelist\"/>\n" +
"                    </gmd:dataType>\n" +
"                    <gmd:parentEntity>\n" +
"                        <gco:CharacterString>SeaDataNet</gco:CharacterString>\n" +
"                    </gmd:parentEntity>\n" +
"                </gmd:MD_ExtendedElementInformation>\n" +
"            </gmd:extendedElementInformation>\n" +
"        </gmd:MD_MetadataExtensionInformation>\n" +
"    </gmd:metadataExtensionInfo>\n" +
"    <gmd:identificationInfo>\n" +
"        <gmd:MD_DataIdentification>\n" +
"            <gmd:citation>\n" +
"                <gmd:CI_Citation>\n" +
"                    <gmd:title>\n" +
"                        <gco:CharacterString>90008411-2.ctd</gco:CharacterString>\n" +
"                    </gmd:title>\n" +
"                    <gmd:alternateTitle>\n" +
"                        <gco:CharacterString>42292_9s_19900610041000</gco:CharacterString>\n" +
"                    </gmd:alternateTitle>\n" +
"                    <gmd:date>\n" +
"                        <gmd:CI_Date>\n" +
"                            <gmd:date>\n" +
"                                <gco:DateTime>1990-06-05T00:00:00+02:00</gco:DateTime>\n" +
"                            </gmd:date>\n" +
"                            <gmd:dateType>\n" +
"                                <gmd:CI_DateTypeCode codeList=\"http://www.tc211.org/ISO19139/resources/codeList.xml#revision\" codeListValue=\"revision\"/>\n" +
"                            </gmd:dateType>\n" +
"                        </gmd:CI_Date>\n" +
"                    </gmd:date>\n" +
"                    <gmd:date>\n" +
"                        <gmd:CI_Date>\n" +
"                            <gmd:date>\n" +
"                                <gco:Date>1970-02-04T03:04:26+02:00</gco:Date>\n" +
"                            </gmd:date>\n" +
"                            <gmd:dateType>\n" +
"                                <gmd:CI_DateTypeCode codeList=\"http://schemas.opengis.net/iso/19139/20070417/resources/Codelist/gmxCodelists.xml#CI_DateTypeCode\" codeListValue=\"creation\" codeSpace=\"eng\">Creation</gmd:CI_DateTypeCode>\n" +
"                            </gmd:dateType>\n" +
"                        </gmd:CI_Date>\n" +
"                    </gmd:date>\n" +
"                    <gmd:citedResponsibleParty>\n" +
"                        <gmd:CI_ResponsibleParty>\n" +
"                            <gmd:organisationName>\n" +
"                                <gco:CharacterString>UNIVERSITE DE LA MEDITERRANNEE (U2) / COM - LAB. OCEANOG. &amp; BIOGEOCHIMIE - LUMINY</gco:CharacterString>\n" +
"                            </gmd:organisationName>\n" +
"                            <gmd:contactInfo>\n" +
"                                <gmd:CI_Contact>\n" +
"                                    <gmd:phone>\n" +
"                                        <gmd:CI_Telephone>\n" +
"                                            <gmd:voice>\n" +
"                                                <gco:CharacterString>+33(0)4 91 82 91 15</gco:CharacterString>\n" +
"                                            </gmd:voice>\n" +
"                                            <gmd:facsimile>\n" +
"                                                <gco:CharacterString>+33(0)4 91.82.65.48</gco:CharacterString>\n" +
"                                            </gmd:facsimile>\n" +
"                                        </gmd:CI_Telephone>\n" +
"                                    </gmd:phone>\n" +
"                                    <gmd:address>\n" +
"                                        <gmd:CI_Address>\n" +
"                                            <gmd:deliveryPoint>\n" +
"                                                <gco:CharacterString>UFR Centre Oceanologique de Marseille Campus de Luminy Case 901</gco:CharacterString>\n" +
"                                            </gmd:deliveryPoint>\n" +
"                                            <gmd:city>\n" +
"                                                <gco:CharacterString>Marseille cedex 9</gco:CharacterString>\n" +
"                                            </gmd:city>\n" +
"                                            <gmd:postalCode>\n" +
"                                                <gco:CharacterString>13288</gco:CharacterString>\n" +
"                                            </gmd:postalCode>\n" +
"                                            <gmd:country>\n" +
"                                                <gmx:Anchor xlink:href=\"SDN:C320:2:FR\">France</gmx:Anchor>\n" +
"                                            </gmd:country>\n" +
"                                            <gmd:electronicMailAddress>\n" +
"                                                <gmx:Anchor xlink:href=\"SDN:EDMERP::10680\"/>\n" +
"                                            </gmd:electronicMailAddress>\n" +
"                                        </gmd:CI_Address>\n" +
"                                    </gmd:address>\n" +
"                                    <gmd:onlineResource>\n" +
"                                        <gmd:CI_OnlineResource>\n" +
"                                            <gmd:linkage>\n" +
"                                                <gmd:URL>http://www.com.univ-mrs.fr/LOB/</gmd:URL>\n" +
"                                            </gmd:linkage>\n" +
"                                            <gmd:protocol>\n" +
"                                                <gco:CharacterString>http</gco:CharacterString>\n" +
"                                            </gmd:protocol>\n" +
"                                        </gmd:CI_OnlineResource>\n" +
"                                    </gmd:onlineResource>\n" +
"                                </gmd:CI_Contact>\n" +
"                            </gmd:contactInfo>\n" +
"                            <gmd:role>\n" +
"                                <gmd:CI_RoleCode codeList=\"http://www.tc211.org/ISO19139/resources/codeList.xml#originator\" codeListValue=\"originator\"/>\n" +
"                            </gmd:role>\n" +
"                        </gmd:CI_ResponsibleParty>\n" +
"                    </gmd:citedResponsibleParty>\n" +
"                </gmd:CI_Citation>\n" +
"            </gmd:citation>\n" +
"            <gmd:abstract>\n" +
"                <gco:CharacterString>Donnees CTD MEDIPROD VI 120</gco:CharacterString>\n" +
"            </gmd:abstract>\n" +
"            <gmd:pointOfContact>\n" +
"                <gmd:CI_ResponsibleParty>\n" +
"                    <gmd:organisationName>\n" +
"                        <gco:CharacterString>IFREMER / IDM/SISMER</gco:CharacterString>\n" +
"                    </gmd:organisationName>\n" +
"                    <gmd:contactInfo>\n" +
"                        <gmd:CI_Contact>\n" +
"                            <gmd:phone>\n" +
"                                <gmd:CI_Telephone>\n" +
"                                    <gmd:voice>\n" +
"                                        <gco:CharacterString>+33 (0)2 98.22.49.16</gco:CharacterString>\n" +
"                                    </gmd:voice>\n" +
"                                    <gmd:facsimile>\n" +
"                                        <gco:CharacterString>+33 (0)2 98.22.46.44</gco:CharacterString>\n" +
"                                    </gmd:facsimile>\n" +
"                                </gmd:CI_Telephone>\n" +
"                            </gmd:phone>\n" +
"                            <gmd:address>\n" +
"                                <gmd:CI_Address>\n" +
"                                    <gmd:deliveryPoint>\n" +
"                                        <gco:CharacterString>Centre IFREMER de Brest BP 70</gco:CharacterString>\n" +
"                                    </gmd:deliveryPoint>\n" +
"                                    <gmd:city>\n" +
"                                        <gco:CharacterString>PLOUZANE</gco:CharacterString>\n" +
"                                    </gmd:city>\n" +
"                                    <gmd:postalCode>\n" +
"                                        <gco:CharacterString>29280</gco:CharacterString>\n" +
"                                    </gmd:postalCode>\n" +
"                                    <gmd:country>\n" +
"                                        <gmx:Anchor xlink:href=\"SDN:C320:2:FR\">France</gmx:Anchor>\n" +
"                                    </gmd:country>\n" +
"                                    <gmd:electronicMailAddress>\n" +
"                                        <gco:CharacterString>sismer@ifremer.fr</gco:CharacterString>\n" +
"                                    </gmd:electronicMailAddress>\n" +
"                                </gmd:CI_Address>\n" +
"                            </gmd:address>\n" +
"                            <gmd:onlineResource>\n" +
"                                <gmd:CI_OnlineResource>\n" +
"                                    <gmd:linkage>\n" +
"                                        <gmd:URL>http://www.ifremer.fr/sismer/</gmd:URL>\n" +
"                                    </gmd:linkage>\n" +
"                                    <gmd:protocol>\n" +
"                                        <gco:CharacterString>http</gco:CharacterString>\n" +
"                                    </gmd:protocol>\n" +
"                                </gmd:CI_OnlineResource>\n" +
"                            </gmd:onlineResource>\n" +
"                        </gmd:CI_Contact>\n" +
"                    </gmd:contactInfo>\n" +
"                    <gmd:role>\n" +
"                        <gmd:CI_RoleCode codeList=\"http://www.tc211.org/ISO19139/resources/codeList.xml#custodian\" codeListValue=\"custodian\"/>\n" +
"                    </gmd:role>\n" +
"                </gmd:CI_ResponsibleParty>\n" +
"            </gmd:pointOfContact>\n" +
"            <gmd:descriptiveKeywords>\n" +
"                <gmd:MD_Keywords>\n" +
"                    <gmd:keyword>\n" +
"                        <gmx:Anchor xlink:href=\"SDN:P021:35:ATTN\">Transmittance and attenuance of the water column</gmx:Anchor>\n" +
"                    </gmd:keyword>\n" +
"                    <gmd:keyword>\n" +
"                        <gmx:Anchor xlink:href=\"SDN:P021:35:CNDC\">Electrical conductivity of the water column</gmx:Anchor>\n" +
"                    </gmd:keyword>\n" +
"                    <gmd:keyword>\n" +
"                        <gmx:Anchor xlink:href=\"SDN:P021:35:DOXY\">Dissolved oxygen parameters in the water column</gmx:Anchor>\n" +
"                    </gmd:keyword>\n" +
"                    <gmd:keyword>\n" +
"                        <gmx:Anchor xlink:href=\"SDN:P021:35:EXCO\">Light extinction and diffusion coefficients</gmx:Anchor>\n" +
"                    </gmd:keyword>\n" +
"                    <gmd:keyword>\n" +
"                        <gmx:Anchor xlink:href=\"SDN:P021:35:HEXC\">Dissolved noble gas concentration parameters in the water column</gmx:Anchor>\n" +
"                    </gmd:keyword>\n" +
"                    <gmd:keyword>\n" +
"                        <gmx:Anchor xlink:href=\"SDN:P021:35:OPBS\">Optical backscatter</gmx:Anchor>\n" +
"                    </gmd:keyword>\n" +
"                    <gmd:keyword>\n" +
"                        <gmx:Anchor xlink:href=\"SDN:P021:35:PSAL\">Salinity of the water column</gmx:Anchor>\n" +
"                    </gmd:keyword>\n" +
"                    <gmd:keyword>\n" +
"                        <gmx:Anchor xlink:href=\"SDN:P021:35:SCOX\">Dissolved concentration parameters for 'other' gases in the water column</gmx:Anchor>\n" +
"                    </gmd:keyword>\n" +
"                    <gmd:keyword>\n" +
"                        <gmx:Anchor xlink:href=\"SDN:P021:35:TEMP\">Temperature of the water column</gmx:Anchor>\n" +
"                    </gmd:keyword>\n" +
"                    <gmd:keyword>\n" +
"                        <gmx:Anchor xlink:href=\"SDN:P021:35:VSRA\">Visible waveband radiance and irradiance measurements in the atmosphere</gmx:Anchor>\n" +
"                    </gmd:keyword>\n" +
"                    <gmd:keyword>\n" +
"                        <gmx:Anchor xlink:href=\"SDN:P021:35:VSRW\">Visible waveband radiance and irradiance measurements in the water column</gmx:Anchor>\n" +
"                    </gmd:keyword>\n" +
"                    <gmd:type>\n" +
"                        <gmd:MD_KeywordTypeCode codeList=\"http://www.tc211.org/ISO19139/resources/codeList.xml#parameter\" codeListValue=\"parameter\"/>\n" +
"                    </gmd:type>\n" +
"                    <gmd:thesaurusName>\n" +
"                        <gmd:CI_Citation>\n" +
"                            <gmd:title>\n" +
"                                <gco:CharacterString>BODC Parameter Discovery Vocabulary</gco:CharacterString>\n" +
"                            </gmd:title>\n" +
"                            <gmd:alternateTitle>\n" +
"                                <gco:CharacterString>P021</gco:CharacterString>\n" +
"                            </gmd:alternateTitle>\n" +
"                            <gmd:date>\n" +
"                                <gmd:CI_Date>\n" +
"                                    <gmd:date>\n" +
"                                        <gco:DateTime>2008-11-26T02:00:04+01:00</gco:DateTime>\n" +
"                                    </gmd:date>\n" +
"                                    <gmd:dateType>\n" +
"                                        <gmd:CI_DateTypeCode codeList=\"http://www.tc211.org/ISO19139/resources/codeList.xml#revision\" codeListValue=\"revision\"/>\n" +
"                                    </gmd:dateType>\n" +
"                                </gmd:CI_Date>\n" +
"                            </gmd:date>\n" +
"                            <gmd:edition>\n" +
"                                <gmx:Anchor xlink:href=\"SDN:C371:1:35\">35</gmx:Anchor>\n" +
"                            </gmd:edition>\n" +
"                            <gmd:identifier>\n" +
"                                <gmd:RS_Identifier>\n" +
"                                    <gmd:code>\n" +
"                                        <gco:CharacterString>http://www.seadatanet.org/urnurl/</gco:CharacterString>\n" +
"                                    </gmd:code>\n" +
"                                </gmd:RS_Identifier>\n" +
"                            </gmd:identifier>\n" +
"                        </gmd:CI_Citation>\n" +
"                    </gmd:thesaurusName>\n" +
"                </gmd:MD_Keywords>\n" +
"            </gmd:descriptiveKeywords>\n" +
"            <gmd:descriptiveKeywords>\n" +
"                <gmd:MD_Keywords>\n" +
"                    <gmd:keyword>\n" +
"                        <gmx:Anchor xlink:href=\"SDN:L054:2:130\">CTD profilers</gmx:Anchor>\n" +
"                    </gmd:keyword>\n" +
"                    <gmd:type>\n" +
"                        <gmd:MD_KeywordTypeCode codeList=\"http://www.tc211.org/ISO19139/resources/codeList.xml#instrument\" codeListValue=\"instrument\"/>\n" +
"                    </gmd:type>\n" +
"                    <gmd:thesaurusName>\n" +
"                        <gmd:CI_Citation>\n" +
"                            <gmd:title>\n" +
"                                <gco:CharacterString>SeaDataNet device categories</gco:CharacterString>\n" +
"                            </gmd:title>\n" +
"                            <gmd:alternateTitle>\n" +
"                                <gco:CharacterString>L05</gco:CharacterString>\n" +
"                            </gmd:alternateTitle>\n" +
"                            <gmd:date>\n" +
"                                <gmd:CI_Date>\n" +
"                                    <gmd:date>\n" +
"                                        <gco:DateTime>2008-01-11T02:00:04+01:00</gco:DateTime>\n" +
"                                    </gmd:date>\n" +
"                                    <gmd:dateType>\n" +
"                                        <gmd:CI_DateTypeCode codeList=\"http://www.tc211.org/ISO19139/resources/codeList.xml#revision\" codeListValue=\"revision\"/>\n" +
"                                    </gmd:dateType>\n" +
"                                </gmd:CI_Date>\n" +
"                            </gmd:date>\n" +
"                            <gmd:edition>\n" +
"                                <gmx:Anchor xlink:href=\"SDN:C371:1:4\">4</gmx:Anchor>\n" +
"                            </gmd:edition>\n" +
"                            <gmd:identifier>\n" +
"                                <gmd:RS_Identifier>\n" +
"                                    <gmd:code>\n" +
"                                        <gco:CharacterString>http://www.seadatanet.org/urnurl/</gco:CharacterString>\n" +
"                                    </gmd:code>\n" +
"                                </gmd:RS_Identifier>\n" +
"                            </gmd:identifier>\n" +
"                        </gmd:CI_Citation>\n" +
"                    </gmd:thesaurusName>\n" +
"                </gmd:MD_Keywords>\n" +
"            </gmd:descriptiveKeywords>\n" +
"            <gmd:descriptiveKeywords>\n" +
"                <gmd:MD_Keywords>\n" +
"                    <gmd:keyword>\n" +
"                        <gmx:Anchor xlink:href=\"SDN:L061:6:31\">research vessel</gmx:Anchor>\n" +
"                    </gmd:keyword>\n" +
"                    <gmd:type>\n" +
"                        <gmd:MD_KeywordTypeCode codeList=\"http://www.tc211.org/ISO19139/resources/codeList.xml#platform_class\" codeListValue=\"platform_class\"/>\n" +
"                    </gmd:type>\n" +
"                    <gmd:thesaurusName>\n" +
"                        <gmd:CI_Citation>\n" +
"                            <gmd:title>\n" +
"                                <gco:CharacterString>SeaDataNet Platform Classes</gco:CharacterString>\n" +
"                            </gmd:title>\n" +
"                            <gmd:alternateTitle>\n" +
"                                <gco:CharacterString>L061</gco:CharacterString>\n" +
"                            </gmd:alternateTitle>\n" +
"                            <gmd:date>\n" +
"                                <gmd:CI_Date>\n" +
"                                    <gmd:date>\n" +
"                                        <gco:DateTime>2008-02-21T10:55:40+01:00</gco:DateTime>\n" +
"                                    </gmd:date>\n" +
"                                    <gmd:dateType>\n" +
"                                        <gmd:CI_DateTypeCode codeList=\"http://www.tc211.org/ISO19139/resources/codeList.xml#revision\" codeListValue=\"revision\"/>\n" +
"                                    </gmd:dateType>\n" +
"                                </gmd:CI_Date>\n" +
"                            </gmd:date>\n" +
"                            <gmd:edition>\n" +
"                                <gmx:Anchor xlink:href=\"SDN:C371:1:6\">6</gmx:Anchor>\n" +
"                            </gmd:edition>\n" +
"                            <gmd:identifier>\n" +
"                                <gmd:RS_Identifier>\n" +
"                                    <gmd:code>\n" +
"                                        <gco:CharacterString>http://www.seadatanet.org/urnurl/</gco:CharacterString>\n" +
"                                    </gmd:code>\n" +
"                                </gmd:RS_Identifier>\n" +
"                            </gmd:identifier>\n" +
"                        </gmd:CI_Citation>\n" +
"                    </gmd:thesaurusName>\n" +
"                </gmd:MD_Keywords>\n" +
"            </gmd:descriptiveKeywords>\n" +
"            <gmd:resourceConstraints>\n" +
"                <gmd:MD_LegalConstraints>\n" +
"                    <gmd:accessConstraints>\n" +
"                        <gmd:MD_RestrictionCode codeList=\"http://www.tc211.org/ISO19139/resources/codeList.xml#license\" codeListValue=\"license\"/>\n" +
"                    </gmd:accessConstraints>\n" +
"                </gmd:MD_LegalConstraints>\n" +
"            </gmd:resourceConstraints>\n" +
"            <gmd:aggregationInfo>\n" +
"                <gmd:MD_AggregateInformation>\n" +
"                    <gmd:aggregateDataSetName>\n" +
"                        <gmd:CI_Citation>\n" +
"                            <gmd:title>\n" +
"                                <gco:CharacterString>MEDIPROD VI</gco:CharacterString>\n" +
"                            </gmd:title>\n" +
"                            <gmd:alternateTitle>\n" +
"                                <gco:CharacterString>90008411</gco:CharacterString>\n" +
"                            </gmd:alternateTitle>\n" +
"                            <gmd:date>\n" +
"                                <gmd:CI_Date>\n" +
"                                    <gmd:date>\n" +
"                                        <gco:DateTime>1990-06-05T00:00:00+02:00</gco:DateTime>\n" +
"                                    </gmd:date>\n" +
"                                    <gmd:dateType>\n" +
"                                        <gmd:CI_DateTypeCode codeList=\"http://www.tc211.org/ISO19139/resources/codeList.xml#revision\" codeListValue=\"revision\"/>\n" +
"                                    </gmd:dateType>\n" +
"                                </gmd:CI_Date>\n" +
"                            </gmd:date>\n" +
"                        </gmd:CI_Citation>\n" +
"                    </gmd:aggregateDataSetName>\n" +
"                    <gmd:associationType>\n" +
"                        <gmd:DS_AssociationTypeCode codeList=\"http://www.tc211.org/ISO19139/resources/codeList.xml#largerWorkCitation\" codeListValue=\"largerWorkCitation\"/>\n" +
"                    </gmd:associationType>\n" +
"                    <gmd:initiativeType>\n" +
"                        <gmd:DS_InitiativeTypeCode codeList=\"http://www.tc211.org/ISO19139/resources/codeList.xml#campaign\" codeListValue=\"campaign\"/>\n" +
"                    </gmd:initiativeType>\n" +
"                </gmd:MD_AggregateInformation>\n" +
"            </gmd:aggregationInfo>\n" +
"            <gmd:aggregationInfo>\n" +
"                <gmd:MD_AggregateInformation>\n" +
"                    <gmd:aggregateDataSetName>\n" +
"                        <gmd:CI_Citation>\n" +
"                            <gmd:title>\n" +
"                                <gco:CharacterString>9s</gco:CharacterString>\n" +
"                            </gmd:title>\n" +
"                            <gmd:alternateTitle>\n" +
"                                <gco:CharacterString>9s</gco:CharacterString>\n" +
"                            </gmd:alternateTitle>\n" +
"                            <gmd:date>\n" +
"                                <gmd:CI_Date>\n" +
"                                    <gmd:date>\n" +
"                                        <gco:DateTime>1990-06-10T00:00:00+02:00</gco:DateTime>\n" +
"                                    </gmd:date>\n" +
"                                    <gmd:dateType>\n" +
"                                        <gmd:CI_DateTypeCode codeList=\"http://www.tc211.org/ISO19139/resources/codeList.xml#revision\" codeListValue=\"revision\"/>\n" +
"                                    </gmd:dateType>\n" +
"                                </gmd:CI_Date>\n" +
"                            </gmd:date>\n" +
"                        </gmd:CI_Citation>\n" +
"                    </gmd:aggregateDataSetName>\n" +
"                    <gmd:associationType>\n" +
"                        <gmd:DS_AssociationTypeCode codeList=\"http://www.tc211.org/ISO19139/resources/codeList.xml#largerWorkCitation\" codeListValue=\"largerWorkCitation\"/>\n" +
"                    </gmd:associationType>\n" +
"                    <gmd:initiativeType>\n" +
"                        <gmd:DS_InitiativeTypeCode codeList=\"http://www.tc211.org/ISO19139/resources/codeList.xml#campaign\" codeListValue=\"campaign\"/>\n" +
"                    </gmd:initiativeType>\n" +
"                </gmd:MD_AggregateInformation>\n" +
"            </gmd:aggregationInfo>\n" +
"            <gmd:language>\n" +
"                <gco:CharacterString>eng</gco:CharacterString>\n" +
"            </gmd:language>\n" +
"            <gmd:topicCategory>\n" +
"                <gmd:MD_TopicCategoryCode>oceans</gmd:MD_TopicCategoryCode>\n" +
"            </gmd:topicCategory>\n" +
"            <gmd:extent>\n" +
"                <gmd:EX_Extent>\n" +
"                    <gmd:geographicElement>\n" +
"                        <gmd:EX_GeographicBoundingBox>\n" +
"                            <gmd:extentTypeCode>\n" +
"                                <gco:Boolean>true</gco:Boolean>\n" +
"                            </gmd:extentTypeCode>\n" +
"                            <gmd:westBoundLongitude>\n" +
"                                <gco:Decimal>1.3667</gco:Decimal>\n" +
"                            </gmd:westBoundLongitude>\n" +
"                            <gmd:eastBoundLongitude>\n" +
"                                <gco:Decimal>1.3667</gco:Decimal>\n" +
"                            </gmd:eastBoundLongitude>\n" +
"                            <gmd:southBoundLatitude>\n" +
"                                <gco:Decimal>36.6</gco:Decimal>\n" +
"                            </gmd:southBoundLatitude>\n" +
"                            <gmd:northBoundLatitude>\n" +
"                                <gco:Decimal>36.6</gco:Decimal>\n" +
"                            </gmd:northBoundLatitude>\n" +
"                        </gmd:EX_GeographicBoundingBox>\n" +
"                    </gmd:geographicElement>\n" +
"                    <gmd:geographicElement>\n" +
"                        <gmd:EX_GeographicBoundingBox>\n" +
"                            <gmd:extentTypeCode>\n" +
"                                <gco:Boolean>true</gco:Boolean>\n" +
"                            </gmd:extentTypeCode>\n" +
"                            <gmd:westBoundLongitude>\n" +
"                                <gco:Decimal>12.1</gco:Decimal>\n" +
"                            </gmd:westBoundLongitude>\n" +
"                            <gmd:eastBoundLongitude>\n" +
"                                <gco:Decimal>12.1</gco:Decimal>\n" +
"                            </gmd:eastBoundLongitude>\n" +
"                            <gmd:southBoundLatitude>\n" +
"                                <gco:Decimal>31.2</gco:Decimal>\n" +
"                            </gmd:southBoundLatitude>\n" +
"                            <gmd:northBoundLatitude>\n" +
"                                <gco:Decimal>31.2</gco:Decimal>\n" +
"                            </gmd:northBoundLatitude>\n" +
"                        </gmd:EX_GeographicBoundingBox>\n" +
"                    </gmd:geographicElement>\n" +
"                    <gmd:temporalElement>\n" +
"                        <gmd:EX_TemporalExtent>\n" +
"                            <gmd:extent>\n" +
"                                <gml:TimePeriod gml:id=\"extent\">\n" +
"                                    <gml:beginPosition>1990-06-05T00:00:00+02:00</gml:beginPosition>\n" +
"                                    <gml:endPosition>1990-07-02T00:00:00+02:00</gml:endPosition>\n" +
"                                </gml:TimePeriod>\n" +
"                            </gmd:extent>\n" +
"                        </gmd:EX_TemporalExtent>\n" +
"                    </gmd:temporalElement>\n" +
"                    <gmd:verticalElement>\n" +
"                        <gmd:EX_VerticalExtent>\n" +
"                            <gmd:verticalCRS>\n" +
"                                <gml:VerticalCRS gml:id=\"coordinate-reference-system\">\n" +
"                                    <gml:identifier codeSpace=\"\">idvertCRS</gml:identifier>\n" +
"                                    <gml:scope/>\n" +
"                                    <gml:verticalCS>\n" +
"                                        <gml:VerticalCS gml:id=\"coordinate-system\">\n" +
"                                            <gml:identifier codeSpace=\"\">meters</gml:identifier>\n" +
"                                            <gml:axis>\n" +
"                                                <gml:CoordinateSystemAxis gml:uom=\"m\" gml:id=\"coordinate-system-axis\">\n" +
"                                                    <gml:identifier codeSpace=\"\">meters</gml:identifier>\n" +
"                                                    <gml:axisAbbrev>meters</gml:axisAbbrev>\n" +
"                                                    <gml:axisDirection codeSpace=\"\">down</gml:axisDirection>\n" +
"                                                </gml:CoordinateSystemAxis>\n" +
"                                            </gml:axis>\n" +
"                                        </gml:VerticalCS>\n" +
"                                    </gml:verticalCS>\n" +
"                                    <gml:verticalDatum>\n" +
"                                        <gml:VerticalDatum gml:id=\"datum\">\n" +
"                                            <gml:identifier codeSpace=\"\">D28</gml:identifier>\n" +
"                                            <gml:scope/>\n" +
"                                        </gml:VerticalDatum>\n" +
"                                    </gml:verticalDatum>\n" +
"                                </gml:VerticalCRS>\n" +
"                            </gmd:verticalCRS>\n" +
"                        </gmd:EX_VerticalExtent>\n" +
"                    </gmd:verticalElement>\n" +
"                </gmd:EX_Extent>\n" +
"            </gmd:extent>\n" +
"        </gmd:MD_DataIdentification>\n" +
"    </gmd:identificationInfo>\n" +
"    <gmd:contentInfo>\n" +
"        <gmd:MD_ImageDescription>\n" +
"            <gmd:cloudCoverPercentage>\n" +
"                <gco:Real>21.0</gco:Real>\n" +
"            </gmd:cloudCoverPercentage>\n" +
"        </gmd:MD_ImageDescription>\n" +
"    </gmd:contentInfo>\n" +
"    <gmd:distributionInfo>\n" +
"        <gmd:MD_Distribution>\n" +
"            <gmd:distributionFormat>\n" +
"                <gmd:MD_Format>\n" +
"                    <gmd:name>\n" +
"                        <gmx:Anchor xlink:href=\"SDN:L241:1:MEDATLAS\">MEDATLAS ASCII</gmx:Anchor>\n" +
"                    </gmd:name>\n" +
"                    <gmd:version>\n" +
"                        <gco:CharacterString>1.0</gco:CharacterString>\n" +
"                    </gmd:version>\n" +
"                </gmd:MD_Format>\n" +
"            </gmd:distributionFormat>\n" +
"            <gmd:distributor>\n" +
"                <gmd:MD_Distributor>\n" +
"                    <gmd:distributorContact>\n" +
"                        <gmd:CI_ResponsibleParty>\n" +
"                            <gmd:organisationName>\n" +
"                                <gco:CharacterString>IFREMER / IDM/SISMER</gco:CharacterString>\n" +
"                            </gmd:organisationName>\n" +
"                            <gmd:contactInfo>\n" +
"                                <gmd:CI_Contact>\n" +
"                                    <gmd:phone>\n" +
"                                        <gmd:CI_Telephone>\n" +
"                                            <gmd:voice>\n" +
"                                                <gco:CharacterString>+33 (0)2 98.22.49.16</gco:CharacterString>\n" +
"                                            </gmd:voice>\n" +
"                                            <gmd:facsimile>\n" +
"                                                <gco:CharacterString>+33 (0)2 98.22.46.44</gco:CharacterString>\n" +
"                                            </gmd:facsimile>\n" +
"                                        </gmd:CI_Telephone>\n" +
"                                    </gmd:phone>\n" +
"                                    <gmd:address>\n" +
"                                        <gmd:CI_Address>\n" +
"                                            <gmd:deliveryPoint>\n" +
"                                                <gco:CharacterString>Centre IFREMER de Brest BP 70</gco:CharacterString>\n" +
"                                            </gmd:deliveryPoint>\n" +
"                                            <gmd:city>\n" +
"                                                <gco:CharacterString>PLOUZANE</gco:CharacterString>\n" +
"                                            </gmd:city>\n" +
"                                            <gmd:postalCode>\n" +
"                                                <gco:CharacterString>29280</gco:CharacterString>\n" +
"                                            </gmd:postalCode>\n" +
"                                            <gmd:country>\n" +
"                                                <gmx:Anchor xlink:href=\"SDN:C320:2:FR\">France</gmx:Anchor>\n" +
"                                            </gmd:country>\n" +
"                                            <gmd:electronicMailAddress>\n" +
"                                                <gco:CharacterString>sismer@ifremer.fr</gco:CharacterString>\n" +
"                                            </gmd:electronicMailAddress>\n" +
"                                        </gmd:CI_Address>\n" +
"                                    </gmd:address>\n" +
"                                    <gmd:onlineResource>\n" +
"                                        <gmd:CI_OnlineResource>\n" +
"                                            <gmd:linkage>\n" +
"                                                <gmd:URL>http://www.ifremer.fr/sismer/</gmd:URL>\n" +
"                                            </gmd:linkage>\n" +
"                                            <gmd:protocol>\n" +
"                                                <gco:CharacterString>http</gco:CharacterString>\n" +
"                                            </gmd:protocol>\n" +
"                                        </gmd:CI_OnlineResource>\n" +
"                                    </gmd:onlineResource>\n" +
"                                </gmd:CI_Contact>\n" +
"                            </gmd:contactInfo>\n" +
"                            <gmd:role>\n" +
"                                <gmd:CI_RoleCode codeList=\"http://www.tc211.org/ISO19139/resources/codeList.xml#distributor\" codeListValue=\"distributor\"/>\n" +
"                            </gmd:role>\n" +
"                        </gmd:CI_ResponsibleParty>\n" +
"                    </gmd:distributorContact>\n" +
"                </gmd:MD_Distributor>\n" +
"            </gmd:distributor>\n" +
"            <gmd:transferOptions>\n" +
"                <gmd:MD_DigitalTransferOptions>\n" +
"                    <gmd:transferSize>\n" +
"                        <gco:Real>2.431640625</gco:Real>\n" +
"                    </gmd:transferSize>\n" +
"                    <gmd:onLine>\n" +
"                        <gmd:CI_OnlineResource>\n" +
"                            <gmd:linkage>\n" +
"                                <gmd:URL>http://www.ifremer.fr/sismerData/jsp/visualisationMetadata3.jsp?langue=EN&amp;pageOrigine=CS&amp;cle1=42292_1&amp;cle2=CTDF02</gmd:URL>\n" +
"                            </gmd:linkage>\n" +
"                            <gmd:protocol>\n" +
"                                <gco:CharacterString>http</gco:CharacterString>\n" +
"                            </gmd:protocol>\n" +
"                            <gmd:description>\n" +
"                                <gco:CharacterString>CTDF02</gco:CharacterString>\n" +
"                            </gmd:description>\n" +
"                            <gmd:function>\n" +
"                                <gmd:CI_OnLineFunctionCode codeList=\"http://www.tc211.org/ISO19139/resources/codeList.xml#download\" codeListValue=\"download\"/>\n" +
"                            </gmd:function>\n" +
"                        </gmd:CI_OnlineResource>\n" +
"                    </gmd:onLine>\n" +
"                </gmd:MD_DigitalTransferOptions>\n" +
"            </gmd:transferOptions>\n" +
"        </gmd:MD_Distribution>\n" +
"    </gmd:distributionInfo>\n" +
"</gmd:MD_Metadata>\n";

}
