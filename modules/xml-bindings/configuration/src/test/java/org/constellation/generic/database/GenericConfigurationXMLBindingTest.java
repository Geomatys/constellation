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

package org.constellation.generic.database;

import org.apache.sis.test.XMLComparator;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.configuration.SOSConfiguration;
import org.constellation.dto.AccessConstraint;
import org.constellation.dto.Contact;
import org.constellation.dto.Details;
import org.constellation.dto.ObservationFilter;
import org.constellation.dto.ParameterValues;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class GenericConfigurationXMLBindingTest {

    private MarshallerPool pool;
    private Unmarshaller unmarshaller;
    private Marshaller   marshaller;

    @Before
    public void setUp() throws JAXBException {
        pool = GenericDatabaseMarshallerPool.getInstance();
        unmarshaller = pool.acquireUnmarshaller();
        marshaller   = pool.acquireMarshaller();
    }

    @After
    public void tearDown() throws JAXBException {
        if (unmarshaller != null) {
            pool.recycle(unmarshaller);
        }
        if (marshaller != null) {
            pool.recycle(marshaller);
        }
    }

    /**
     * Test simple Record Marshalling.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void genericMarshalingTest() throws Exception {

        BDD bdd = new BDD("org.driver.test", "http://somehost/blablabla", "bobby", "juanito");

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("staticVar01", "something");
        parameters.put("staticVar02", "blavl, bloub");

        Query query = new Query("singleQuery1", new Select("var01", "pp.label"), new From("physical_parameter pp"));

        Query mquery = new Query("multiQuery1", new Select(Arrays.asList(new Column("var02", "pp.name"), new Column("var03", "tr.id"))),
                                                new From("physical_parameter pp, transduction tr"),
                                                new Where("tr.parameter=pp.id"));

        Query mainQuery = new Query("mainQuery", new Select("varx", "p.main"), new From("physical_test pt"));

        Orderby order = new Orderby();
        order.setSens("ASC");
        order.setvalue("blav");
        mquery.getOrderby().add(order);

        QueryList multi = new QueryList(Arrays.asList(query, mquery));

        Queries queries = new Queries(mainQuery, multi, parameters);
        Automatic config = new Automatic("MDWEB", bdd, queries);
        config.putParameter("testParam", "paramValue");
        StringWriter sw = new StringWriter();
        marshaller.marshal(config, sw);

        String result =  sw.toString();
        String expResult =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"                + '\n' +
        "<automatic format=\"MDWEB\">"                                                 + '\n' +
        "    <bdd>"                                                                    + '\n' +
        "        <className>org.driver.test</className>"                               + '\n' +
        "        <connectURL>http://somehost/blablabla</connectURL>"                   + '\n' +
        "        <user>bobby</user>"                                                   + '\n' +
        "        <password>juanito</password>"                                         + '\n' +
        "        <sharedConnection>false</sharedConnection>"                           + '\n' +
        "    </bdd>"                                                                   + '\n' +
        "    <customparameters>"                                                       + '\n' +
        "        <entry>"                                                              + '\n' +
        "            <key>testParam</key>"                                             + '\n' +
        "            <value>paramValue</value>"                                        + '\n' +
        "        </entry>"                                                             + '\n' +
        "    </customparameters>"                                                      + '\n' +
        "    <queries>"                                                                + '\n' +
        "        <parameters>"                                                         + '\n' +
        "            <entry>"                                                          + '\n' +
        "                <key>staticVar01</key>"                                       + '\n' +
        "                <value>something</value>"                                     + '\n' +
        "            </entry>"                                                         + '\n' +
        "            <entry>"                                                          + '\n' +
        "                <key>staticVar02</key>"                                       + '\n' +
        "                <value>blavl, bloub</value>"                                  + '\n' +
        "            </entry>"                                                         + '\n' +
        "        </parameters>"                                                        + '\n' +
        "        <main name=\"mainQuery\">"                                            + '\n' +
        "            <select>"                                                         + '\n' +
        "                <col>"                                                        + '\n' +
        "                    <var>varx</var>"                                          + '\n' +
        "                    <sql>p.main</sql>"                                        + '\n' +
        "                </col>"                                                       + '\n' +
        "            </select>"                                                        + '\n' +
        "            <from>physical_test pt</from>"                                    + '\n' +
        "        </main>"                                                              + '\n' +
        "        <queryList>"                                                          + '\n' +
	"            <query name=\"singleQuery1\">"                                    + '\n' +
        "                <select>"                                                     + '\n' +
        "                    <col>"                                                    + '\n' +
        "                        <var>var01</var>"                                     + '\n' +
        "                        <sql>pp.label</sql>"                                  + '\n' +
        "                    </col>"                                                   + '\n' +
        "                </select>"                                                    + '\n' +
        "                <from>physical_parameter pp</from>"                           + '\n' +
        "            </query>"                                                         + '\n' +
        "            <query name=\"multiQuery1\">"                                     + '\n' +
        "                <select>"                                                     + '\n' +
        "                    <col>"                                                    + '\n' +
        "                        <var>var02</var>"                                     + '\n' +
        "                        <sql>pp.name</sql>"                                   + '\n' +
        "                    </col>"                                                   + '\n' +
        "                    <col>"                                                    + '\n' +
        "                        <var>var03</var>"                                     + '\n' +
        "                        <sql>tr.id</sql>"                                     + '\n' +
        "                    </col>"                                                   + '\n' +
        "                </select>"                                                    + '\n' +
        "                <from>physical_parameter pp, transduction tr</from>"          + '\n' +
        "                <where>tr.parameter=pp.id</where>"                            + '\n' +
        "                <orderBy sens=\"ASC\">blav</orderBy>"                         + '\n' +
        "            </query>"                                                         + '\n' +
        "        </queryList>"                                                         + '\n' +
        "    </queries>"                                                               + '\n' +
        "</automatic>" + '\n';

        final XMLComparator comparator = new XMLComparator(expResult, result);
        comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
        comparator.compare();
    }

    @Test
    public void sosConfigMarshalingTest() throws Exception {
        BDD bdd = new BDD("org.driver.test", "http://somehost/blablabla", "bobby", "juanito");


        Automatic config = new Automatic("MDWEB", bdd, null);

        SOSConfiguration sosConfig = new SOSConfiguration(config, config);

        Automatic config2 = new Automatic("MDWEB", bdd, null);
        config2.setName("coriolis");
        sosConfig.getExtensions().add(config2);

        StringWriter sw = new StringWriter();
        marshaller.marshal(sosConfig, sw);

        String result =  sw.toString();
        String expResult =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"            + '\n' +
        "<ns2:SOSConfiguration xmlns:ns2=\"http://www.constellation.org/config\">"                                                   + '\n' +
        "    <ns2:SMLConfiguration format=\"MDWEB\">"                              + '\n' +
        "        <bdd>"                                                            + '\n' +
        "            <className>org.driver.test</className>"                       + '\n' +
        "            <connectURL>http://somehost/blablabla</connectURL>"           + '\n' +
        "            <user>bobby</user>"                                           + '\n' +
        "            <password>juanito</password>"                                 + '\n' +
        "            <sharedConnection>false</sharedConnection>"                   + '\n' +
        "        </bdd>"                                                           + '\n' +
        "        <customparameters/>"                                              + '\n' +
        "    </ns2:SMLConfiguration>"                                              + '\n' +
        "    <ns2:OMConfiguration format=\"MDWEB\">"                               + '\n' +
        "        <bdd>"                                                            + '\n' +
        "            <className>org.driver.test</className>"                       + '\n' +
        "            <connectURL>http://somehost/blablabla</connectURL>"           + '\n' +
        "            <user>bobby</user>"                                           + '\n' +
        "            <password>juanito</password>"                                 + '\n' +
        "            <sharedConnection>false</sharedConnection>"                   + '\n' +
        "        </bdd>"                                                           + '\n' +
        "        <customparameters/>"                                              + '\n' +
        "    </ns2:OMConfiguration>"                                               + '\n' +
        "    <ns2:extensions format=\"MDWEB\" name=\"coriolis\">"                  + '\n' +
        "        <bdd>"                                                            + '\n' +
        "            <className>org.driver.test</className>"                       + '\n' +
        "            <connectURL>http://somehost/blablabla</connectURL>"           + '\n' +
        "            <user>bobby</user>"                                           + '\n' +
        "            <password>juanito</password>"                                 + '\n' +
        "            <sharedConnection>false</sharedConnection>"                   + '\n' +
        "        </bdd>"                                                           + '\n' +
        "        <customparameters/>"                                              + '\n' +
        "    </ns2:extensions>"                                                    + '\n' +
        "    <ns2:parameters/>"                                                    + '\n' +
        "    <ns2:maxObservationByRequest>0</ns2:maxObservationByRequest>"         + '\n' +
        "    <ns2:debugMode>false</ns2:debugMode>"                                 + '\n' +
        "    <ns2:verifySynchronization>false</ns2:verifySynchronization>"         + '\n' +
        "    <ns2:keepCapabilities>false</ns2:keepCapabilities>"                   + '\n' +
        "</ns2:SOSConfiguration>" + '\n';

        final XMLComparator comparator = new XMLComparator(expResult, result);
        comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
        comparator.compare();
    }

    /**
     *
     * @throws java.lang.Exception
     */
    @Test
    public void genericUnmarshalingTest() throws Exception {


        String xml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"                + '\n' +
        "<automatic format=\"MDWEB\">"                                                 + '\n' +
        "    <bdd>"                                                                    + '\n' +
        "        <className>org.driver.test</className>"                               + '\n' +
        "        <connectURL>http://somehost/blablabla</connectURL>"                   + '\n' +
        "        <user>bobby</user>"                                                   + '\n' +
        "        <password>juanito</password>"                                         + '\n' +
        "         <sharedConnection>false</sharedConnection>"                          + '\n' +
        "    </bdd>"                                                                   + '\n' +
        "    <queries>"                                                                + '\n' +
        "        <parameters>"                                                         + '\n' +
        "            <entry>"                                                          + '\n' +
        "                <key>staticVar01</key>"                                       + '\n' +
        "                <value>something</value>"                                     + '\n' +
        "            </entry>"                                                         + '\n' +
        "            <entry>"                                                          + '\n' +
        "                <key>staticVar02</key>"                                       + '\n' +
        "                <value>blavl, bloub</value>"                                  + '\n' +
        "            </entry>"                                                         + '\n' +
        "        </parameters>"                                                        + '\n' +
        "        <main name=\"mainQuery\">"                                            + '\n' +
        "            <select>"                                                         + '\n' +
        "                <col>"                                                        + '\n' +
        "                    <var>varx</var>"                                          + '\n' +
        "                    <sql>p.main</sql>"                                        + '\n' +
        "                </col>"                                                       + '\n' +
        "            </select>"                                                        + '\n' +
        "            <from>physical_test pt</from>"                                    + '\n' +
        "        </main>"                                                              + '\n' +
        "        <queryList>"                                                          + '\n' +
	"            <query name=\"singleQuery1\">"                                    + '\n' +
        "                <select>"                                                     + '\n' +
        "                    <col>"                                                    + '\n' +
        "                        <var>var01</var>"                                     + '\n' +
        "                        <sql>pp.label</sql>"                                  + '\n' +
        "                    </col>"                                                   + '\n' +
        "                </select>"                                                    + '\n' +
        "                <from>physical_parameter pp</from>"                           + '\n' +
        "            </query>"                                                         + '\n' +
        "            <query name=\"multiQuery1\">"                                     + '\n' +
        "                <select>"                                                     + '\n' +
        "                    <col>"                                                    + '\n' +
        "                        <var>var02</var>"                                     + '\n' +
        "                        <sql>pp.name</sql>"                                   + '\n' +
        "                    </col>"                                                   + '\n' +
        "                    <col>"                                                    + '\n' +
        "                        <var>var03</var>"                                     + '\n' +
        "                        <sql>tr.id</sql>"                                     + '\n' +
        "                    </col>"                                                   + '\n' +
        "                </select>"                                                    + '\n' +
        "                <from>physical_parameter pp, transduction tr</from>"          + '\n' +
        "                <where>tr.parameter=pp.id</where>"                            + '\n' +
        "                <orderBy sens=\"ASC\">blav</orderBy>"                         + '\n' +
        "            </query>"                                                         + '\n' +
        "        </queryList>"                                                        + '\n' +
        "    </queries>"                                                               + '\n' +
        "</automatic>" + '\n';

        StringReader sr = new StringReader(xml);

        Automatic result = (Automatic) unmarshaller.unmarshal(sr);


        BDD bdd = new BDD("org.driver.test", "http://somehost/blablabla", "bobby", "juanito");

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("staticVar01", "something");
        ArrayList<String> sp2 = new ArrayList<>();
        sp2.add("value1");
        sp2.add("value2");
        parameters.put("staticVar02", "blavl, bloub");

        Query query = new Query("singleQuery1", new Select("var01", "pp.label"), new From("physical_parameter pp"));

        Query mquery = new Query("multiQuery1", new Select(Arrays.asList(new Column("var02", "pp.name"), new Column("var03", "tr.id"))),
                                                new From("physical_parameter pp, transduction tr"),
                                                new Where("tr.parameter=pp.id"));

        Query mainQuery = new Query("mainQuery", new Select("varx", "p.main"), new From("physical_test pt"));

        Orderby order = new Orderby();
        order.setSens("ASC");
        order.setvalue("blav");
        mquery.getOrderby().add(order);

        QueryList multi = new QueryList(Arrays.asList(query, mquery));

        Queries queries     = new Queries(mainQuery, multi, parameters);
        Automatic expResult = new Automatic("MDWEB", bdd, queries);

        assertEquals(expResult, result);
    }

    /**
     *
     * @throws java.lang.Exception
     */
    @Test
    public void filterUnmarshalingTest() throws Exception {

        String xml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"                                                            + '\n' +
        "<query name=\"ObservationAffinage\" xmlns:filter=\"http://constellation.generic.filter.org\">"  + '\n' +
        " <parameters>"                                                                                         + '\n' +
        "     <entry>"                                                                                          + '\n' +
        "       <key>st1</key>"                                                                                 + '\n' +
        "       <value>plouf</value>"                                                                           + '\n' +
        "     </entry>"                                                                                         + '\n' +
        " </parameters>"                                                                                        + '\n' +
        " <statique>"                                                                                           + '\n' +
        "  	   <query name=\"platformList\">"                                                               + '\n' +
        "        <select>"                                                                                      + '\n' +
        "           <col>"                                                                                      + '\n' +
        "              <var>platformList</var>"                                                                 + '\n' +
        "              <sql>platf</sql>"                                                                        + '\n' +
        "           </col>"                                                                                     + '\n' +
        "        </select>"                                                                                     + '\n' +
        "        <from>(select '13471' from dual)</from>"                                                       + '\n' +
        "        <orderBy>name</orderBy>"                                                                       + '\n' +
        "     </query>"                                                                                         + '\n' +
        "</statique>"                                                                                           + '\n' +
        "<select group=\"filterObservation\">"                                                                  + '\n' +
        "   <col>"                                                                                              + '\n' +
        "       <var>locationDate</var>"                                                                        + '\n' +
        "       <sql>loc.location_date</sql>"                                                               + '\n' +
        "   </col>"                                                                                             + '\n' +
        "</select>"                                                                                             + '\n' +
        "<from group=\"observations\">location loc, physical_parameter pp</from>"                               + '\n' +
        "<where group=\"observations\">loc.location_id = lm.location_id</where>"                                + '\n' +
        "<orderBy group=\"observations\" sens=\"ASC\">loc.platform_code, loc.instrument_code</orderBy>"         + '\n' +
        "</query>";

        StringReader sr = new StringReader(xml);

        Query result = (Query) unmarshaller.unmarshal(sr);


        Select select = new Select();
        select.setGroup("filterObservation");
        select.addCol("locationDate", "loc.location_date");

        From from = new From();
        from.setGroup("observations");
        from.setvalue("location loc, physical_parameter pp");

        Where where = new Where();
        where.setGroup("observations");
        where.setvalue("loc.location_id = lm.location_id");

        Orderby order = new Orderby();
        order.setGroup("observations");
        order.setvalue("loc.platform_code, loc.instrument_code");
        order.setSens("ASC");

        Query expResult = new Query();
        expResult.setName("ObservationAffinage");
        expResult.addSelect(select);
        expResult.addFrom(from);
        expResult.addWhere(where);
        expResult.addOrderby(order);

        HashMap<String, String> parameters = new HashMap<>();

        parameters.put("st1", "plouf");

        expResult.setParameters(parameters);

        Query query = new Query("platformList", new Select("platformList", "platf"), new From("(select '13471' from dual)"));
        Orderby order2 = new Orderby();
        order2.setvalue("name");
        query.getOrderby().add(order2);

        expResult.setStatique(new QueryList(query));

        assertEquals(expResult.getStatique(), result.getStatique());
        assertEquals(expResult.getParameters(), result.getParameters());
        assertEquals(expResult.getFrom(), result.getFrom());
        assertEquals(expResult.getOrderby(), result.getOrderby());
        assertEquals(expResult.getSelect(), result.getSelect());
        assertEquals(expResult.getGroupby(), result.getGroupby());
        assertEquals(expResult.getName(), result.getName());
        assertEquals(expResult.getWhere(), result.getWhere());
        assertEquals(expResult, result);
    }

    /**
     *
     * @throws java.lang.Exception
     */
    @Test
    public void filterMarshalingTest() throws Exception {

        String expResult =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"                                                            + '\n' +
        "<query name=\"ObservationAffinage\" >"  + '\n' +
        "    <parameters>"                                                                                         + '\n' +
        "        <entry>"                                                                                          + '\n' +
        "            <key>st1</key>"                                                                                 + '\n' +
        "            <value>plouf</value>"                                                                           + '\n' +
        "        </entry>"                                                                                         + '\n' +
        "    </parameters>"                                                                                        + '\n' +
        "    <statique>"                                                                                           + '\n' +
        "        <query name=\"platformList\">"                                                                   + '\n' +
        "            <select>"                                                                                      + '\n' +
        "                <col>"                                                                                      + '\n' +
        "                    <var>platformList</var>"                                                                 + '\n' +
        "                    <sql>platf</sql>"                                                                        + '\n' +
        "                </col>"                                                                                     + '\n' +
        "            </select>"                                                                                     + '\n' +
        "            <from>(select '13471' from dual)</from>"                                                       + '\n' +
        "            <orderBy>name</orderBy>"                                                                           + '\n' +
        "        </query>"                                                                                         + '\n' +
        "    </statique>"                                                                                           + '\n' +
        "    <select group=\"filterObservation\">"                                                                 + '\n' +
        "        <col>"                                                                 + '\n' +
        "            <var>locationDate</var>"                                                                 + '\n' +
        "            <sql>loc.location_date</sql>"                                                                 + '\n' +
        "        </col>"                                                                 + '\n' +
        "    </select>"                          + '\n' +
        "    <from group=\"observations\">location loc, physical_parameter pp</from>"                               + '\n' +
        "    <where group=\"observations\">loc.location_id = lm.location_id</where>"                                + '\n' +
        "    <orderBy sens=\"ASC\" group=\"observations\">loc.platform_code, loc.instrument_code</orderBy>"         + '\n' +
        "</query>" + '\n';

        Select select = new Select();
        select.setGroup("filterObservation");
        select.addCol("locationDate","loc.location_date");

        From from = new From();
        from.setGroup("observations");
        from.setvalue("location loc, physical_parameter pp");

        Where where = new Where();
        where.setGroup("observations");
        where.setvalue("loc.location_id = lm.location_id");

        Orderby order = new Orderby();
        order.setGroup("observations");
        order.setvalue("loc.platform_code, loc.instrument_code");
        order.setSens("ASC");

        Query query = new Query();
        query.setName("ObservationAffinage");
        query.addSelect(select);
        query.addFrom(from);
        query.addWhere(where);
        query.addOrderby(order);

        HashMap<String, String> parameters = new HashMap<>();

        parameters.put("st1", "plouf");

        query.setParameters(parameters);

        Query querys = new Query("platformList", new Select("platformList", "platf"), new From("(select '13471' from dual)"));
        Orderby order2 = new Orderby();
        order2.setvalue("name");
        querys.getOrderby().add(order2);

        query.setStatique(new QueryList(querys));

        StringWriter sw = new StringWriter();

        marshaller.marshal(query, sw);

        final String result = sw.toString();
        final XMLComparator comparator = new XMLComparator(expResult, result);
        comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
        comparator.compare();
    }

    /*
     *
     */
    @Test
    public void providerSourceUnMarshalingTest() throws Exception {
        String xml = "<?xml version='1.0' encoding='UTF-8'?>" + '\n'
                + "<source xmlns=\"http://www.geotoolkit.org/parameter\">" + '\n'
                + "   <id>shp-tasmania</id>" + '\n'
                + "   <shapefileFolder>" + '\n'
                + "     <path>/home/guilhem/shapefile/Tasmania_shp</path>" + '\n'
                + "     <namespace>shp</namespace>" + '\n'
                + "   </shapefileFolder>" + '\n'
                + "   <load_all>false</load_all>" + '\n'
                + "  <Layer>" + '\n'
                + "    <name>tasmania_cities</name>" + '\n'
                + "    <style>PointCircleBlack12</style>" + '\n'
                + "   </Layer>" + '\n'
                + "   <Layer>" + '\n'
                + "     <name>tasmania_roads</name>" + '\n'
                + "     <style>LineRed2</style>" + '\n'
                + "   </Layer>" + '\n'
                + " </source>";

        Object obj = unmarshaller.unmarshal(new StringReader(xml));

        assertTrue(obj instanceof JAXBElement);
        obj = ((JAXBElement)obj).getValue();
        System.out.println(obj);

        assertTrue(obj instanceof Node);
    }

    @Test
    public void serviceMarshalingTest() throws Exception {

        final Contact ctc = new Contact("firstname", "lastname", "org1", "pos1", "0600", "0800", "test@jj.com", "adr1", "city1", "state1", "34000", "france", "url1", null, null);
        final AccessConstraint cstr = new AccessConstraint("fees1", "constraint1", 5, 200, 300);
        final Details service = new Details("name1", "id1", Arrays.asList("kw1", "kw2"), "desc1", Arrays.asList("1.0.0", "2.0.0"), ctc, cstr, false, "FR");

        StringWriter sw = new StringWriter();
        marshaller.marshal(service, sw);

        String result =  sw.toString();
        String expResult =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
        "<ns2:details xmlns:ns2=\"http://www.constellation.org/config\">\n" +
        "  <ns2:description>desc1</ns2:description>\n" +
        "  <ns2:identifier>id1</ns2:identifier>\n" +
        "  <ns2:keywords>kw1</ns2:keywords>\n" +
        "  <ns2:keywords>kw2</ns2:keywords>\n" +
        "  <ns2:lang>FR</ns2:lang>\n" +
        "  <ns2:name>name1</ns2:name>\n" +
        "  <ns2:serviceConstraints>\n" +
        "    <ns2:accessConstraint>constraint1</ns2:accessConstraint>\n" +
        "    <ns2:fees>fees1</ns2:fees>\n" +
        "    <ns2:layerLimit>5</ns2:layerLimit>\n" +
        "    <ns2:maxHeight>300</ns2:maxHeight>\n" +
        "    <ns2:maxWidth>200</ns2:maxWidth>\n" +
        "  </ns2:serviceConstraints>\n" +
        "  <ns2:serviceContact>\n" +
        "    <ns2:address>adr1</ns2:address>\n" +
        "    <ns2:city>city1</ns2:city>\n" +
        "    <ns2:country>france</ns2:country>\n" +
        "    <ns2:email>test@jj.com</ns2:email>\n" +
        "    <ns2:fax>0800</ns2:fax>\n" +
        "    <ns2:firstname>firstname</ns2:firstname>\n" +
        "    <ns2:fullname>firstname lastname</ns2:fullname>\n" +
        "    <ns2:lastname>lastname</ns2:lastname>\n" +
        "    <ns2:organisation>org1</ns2:organisation>\n" +
        "    <ns2:phone>0600</ns2:phone>\n" +
        "    <ns2:position>pos1</ns2:position>\n" +
        "    <ns2:state>state1</ns2:state>\n" +
        "    <ns2:url>url1</ns2:url>\n" +
        "    <ns2:zipCode>34000</ns2:zipCode>\n" +
        "  </ns2:serviceContact>\n" +
        "  <ns2:transactional>false</ns2:transactional>\n" +
        "  <ns2:versions>1.0.0</ns2:versions>\n" +
        "  <ns2:versions>2.0.0</ns2:versions>\n" +
        "</ns2:details>" + '\n';

        final XMLComparator comparator = new XMLComparator(expResult, result);
        comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
        comparator.compare();
    }
    
    @Test
    public void parameterValuesMarshalingTest() throws Exception {
        final ParameterValues values = new ParameterValues();
        values.getValues().put("providerId", "test");
        StringWriter sw = new StringWriter();
        marshaller.marshal(values, System.out);
    }
    
    @Test
    public void ObservationFilterMarshalingTest() throws Exception {
        final ObservationFilter values = new ObservationFilter();
        values.setSensorID("senord:1");
        values.setObservedProperty(Arrays.asList("phen1", "phen2"));
        values.setStart(new Date(System.currentTimeMillis()));
        values.setEnd(new Date(System.currentTimeMillis() + 10000));
        StringWriter sw = new StringWriter();
        marshaller.marshal(values, System.out);
    }
}
