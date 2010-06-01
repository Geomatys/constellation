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

package org.constellation.generic.database;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

//Junit dependencies
import org.constellation.configuration.SOSConfiguration;
import org.geotoolkit.xml.MarshallerPool;
import org.junit.*;
import static org.junit.Assert.*;

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
        pool = new MarshallerPool(Automatic.class, SOSConfiguration.class, org.constellation.generic.filter.Query.class);
        unmarshaller = pool.acquireUnmarshaller();
        marshaller   = pool.acquireMarshaller();
    }

    @After
    public void tearDown() throws JAXBException {
        if (unmarshaller != null) {
            pool.release(unmarshaller);
        }
        if (marshaller != null) {
            pool.release(marshaller);
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
        
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("staticVar01", "something");
        parameters.put("staticVar02", "blavl, bloub");

        Query query = new Query("singleQuery1", new Select("var01", "pp.label"), new From("physical_parameter pp"));
        QueryList single = new QueryList(query);

        Query mquery = new Query("multiQuery1", new Select(Arrays.asList(new Column("var02", "pp.name"), new Column("var03", "tr.id"))),
                                                new From("physical_parameter pp, transduction tr"),
                                                new Where("tr.parameter=pp.id"));

        Orderby order = new Orderby();
        order.setSens("ASC");
        order.setvalue("blav");
        mquery.getOrderby().add(order);

        QueryList multi = new QueryList(mquery);

        Queries queries = new Queries(null, single, multi, parameters);
        Automatic config = new Automatic("MDWEB", bdd, queries);

        StringWriter sw = new StringWriter();
        marshaller.marshal(config, sw);

        String result = sw.toString();
        //System.out.println(result);
        String expResult =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"                + '\n' +
        "<automatic format=\"MDWEB\" xmlns:ns2=\"http://www.constellation.org/config\" xmlns:ns3=\"http://constellation.generic.filter.org\">" + '\n' +
        "    <bdd>"                                                                    + '\n' +
        "        <className>org.driver.test</className>"                               + '\n' +
        "        <connectURL>http://somehost/blablabla</connectURL>"                   + '\n' +
        "        <user>bobby</user>"                                                   + '\n' +
        "        <password>juanito</password>"                                         + '\n' +
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
        "        <single>"                                                             + '\n' +
	"            <query name=\"singleQuery1\">"                                    + '\n' +
        "                <select>"                                                     + '\n' +
        "                    <col>"                                                    + '\n' +
        "                        <var>var01</var>"                                     + '\n' +
        "                        <sql>pp.label</sql>"                                  + '\n' +
        "                    </col>"                                                   + '\n' +
        "                </select>"                                                    + '\n' +
        "                <from>physical_parameter pp</from>"                           + '\n' +
        "            </query>"                                                         + '\n' +
        "        </single>"                                                            + '\n' +
        "        <multiFixed>"                                                         + '\n' +
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
        "        </multiFixed>"                                                        + '\n' +
        "    </queries>"                                                               + '\n' +
        "</automatic>" + '\n';

        assertEquals(expResult, result);

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

        String result = sw.toString();
        //System.out.println("result:" + result);
        String expResult =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"            + '\n' +
        "<ns2:SOSConfiguration xmlns:ns2=\"http://www.constellation.org/config\" xmlns:ns3=\"http://constellation.generic.filter.org\">" + '\n' +
        "    <ns2:SMLConfiguration format=\"MDWEB\">"                              + '\n' +
        "        <bdd>"                                                            + '\n' +
        "            <className>org.driver.test</className>"                       + '\n' +
        "            <connectURL>http://somehost/blablabla</connectURL>"           + '\n' +
        "            <user>bobby</user>"                                           + '\n' +
        "            <password>juanito</password>"                                 + '\n' +
        "        </bdd>"                                                           + '\n' +
        "    </ns2:SMLConfiguration>"                                              + '\n' +
        "    <ns2:OMConfiguration format=\"MDWEB\">"                               + '\n' +
        "        <bdd>"                                                            + '\n' +
        "            <className>org.driver.test</className>"                       + '\n' +
        "            <connectURL>http://somehost/blablabla</connectURL>"           + '\n' +
        "            <user>bobby</user>"                                           + '\n' +
        "            <password>juanito</password>"                                 + '\n' +
        "        </bdd>"                                                           + '\n' +
        "    </ns2:OMConfiguration>"                                               + '\n' +
        "    <ns2:extensions name=\"coriolis\" format=\"MDWEB\">"                  + '\n' +
        "        <bdd>"                                                            + '\n' +
        "            <className>org.driver.test</className>"                       + '\n' +
        "            <connectURL>http://somehost/blablabla</connectURL>"           + '\n' +
        "            <user>bobby</user>"                                           + '\n' +
        "            <password>juanito</password>"                                 + '\n' +
        "        </bdd>"                                                            + '\n' +
        "    </ns2:extensions>"                                                    + '\n' +
        "    <ns2:maxObservationByRequest>0</ns2:maxObservationByRequest>"         + '\n' +
        "    <ns2:debugMode>false</ns2:debugMode>"                                 + '\n' +
        "    <ns2:verifySynchronization>false</ns2:verifySynchronization>"         + '\n' +
        "</ns2:SOSConfiguration>" + '\n';

        assertEquals(expResult, result);
    
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
        "        <single>"                                                             + '\n' +
	"            <query name=\"singleQuery1\">"                                    + '\n' +
        "                <select>"                                                     + '\n' +
        "                    <col>"                                                    + '\n' +
        "                        <var>var01</var>"                                     + '\n' +
        "                        <sql>pp.label</sql>"                                  + '\n' +
        "                    </col>"                                                   + '\n' +
        "                </select>"                                                    + '\n' +
        "                <from>physical_parameter pp</from>"                           + '\n' +
        "            </query>"                                                         + '\n' +
        "        </single>"                                                            + '\n' +
        "        <multiFixed>"                                                         + '\n' +
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
        "        </multiFixed>"                                                        + '\n' +
        "    </queries>"                                                               + '\n' +
        "</automatic>" + '\n';

        StringReader sr = new StringReader(xml);

        Automatic result = (Automatic) unmarshaller.unmarshal(sr);


        BDD bdd = new BDD("org.driver.test", "http://somehost/blablabla", "bobby", "juanito");

        HashMap<String, String> parameters = new HashMap<String, String>();
        ArrayList<String> sp1 = new ArrayList<String>();
        parameters.put("staticVar01", "something");
        ArrayList<String> sp2 = new ArrayList<String>();
        sp2.add("value1");
        sp2.add("value2");
        parameters.put("staticVar02", "blavl, bloub");

        Query query = new Query("singleQuery1", new Select("var01", "pp.label"), new From("physical_parameter pp"));
        QueryList single = new QueryList(query);

        Query mquery = new Query("multiQuery1", new Select(Arrays.asList(new Column("var02", "pp.name"), new Column("var03", "tr.id"))),
                                                new From("physical_parameter pp, transduction tr"),
                                                new Where("tr.parameter=pp.id"));

        Orderby order = new Orderby();
        order.setSens("ASC");
        order.setvalue("blav");
        mquery.getOrderby().add(order);
        
        QueryList multi = new QueryList(mquery);

        Queries queries     = new Queries(null, single, multi, parameters);
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
        "<filter:query name=\"ObservationAffinage\" xmlns:filter=\"http://constellation.generic.filter.org\">"  + '\n' +
        " <parameters>"                                                                                         + '\n' +
        "     <entry>"                                                                                          + '\n' +
        "       <key>st1</key>"                                                                                 + '\n' +
        "       <value>plouf</value>"                                                                           + '\n' +
        "     </entry>"                                                                                         + '\n' +
        " </parameters>"                                                                                        + '\n' +
        " <statique>"                                                                                           + '\n' +
        "  	   <query name=\"platformList\">"                                                                   + '\n' +
        "        <select>"                                                                                      + '\n' +
        "           <col>"                                                                                      + '\n' +
        "              <var>platformList</var>"                                                                 + '\n' +
        "              <sql>platf</sql>"                                                                        + '\n' +
        "           </col>"                                                                                     + '\n' +
        "        </select>"                                                                                     + '\n' +
        "        <from>(select '13471' from dual)</from>"                                                       + '\n' +
        "        <orderBy>name</orderBy>"                                                                           + '\n' +
        "     </query>"                                                                                         + '\n' +
        "</statique>"                                                                                           + '\n' +
        "<select group=\"filterObservation\" type=\"text\">loc.location_date</select>"                          + '\n' +
        "<from group=\"observations\">location loc, physical_parameter pp</from>"                               + '\n' +
        "<where group=\"observations\">loc.location_id = lm.location_id</where>"                                + '\n' +
        "<orderby group=\"observations\" sens=\"ASC\">loc.platform_code, loc.instrument_code</orderby>"         + '\n' +
        "</filter:query>";

        StringReader sr = new StringReader(xml);

        org.constellation.generic.filter.Query result = (org.constellation.generic.filter.Query) unmarshaller.unmarshal(sr);


        org.constellation.generic.filter.Select select = new org.constellation.generic.filter.Select();
        select.setGroup("filterObservation");
        select.setType("text");
        select.setvalue("loc.location_date");

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

        org.constellation.generic.filter.Query expResult = new org.constellation.generic.filter.Query();
        expResult.setName("ObservationAffinage");
        expResult.addSelect(select);
        expResult.addFrom(from);
        expResult.addWhere(where);
        expResult.addOrderby(order);

        HashMap<String, String> parameters = new HashMap<String, String>();
       
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
        "<ns3:query name=\"ObservationAffinage\" xmlns:ns2=\"http://www.constellation.org/config\" xmlns:ns3=\"http://constellation.generic.filter.org\">"  + '\n' +
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
        "    <select group=\"filterObservation\" type=\"text\">loc.location_date</select>"                          + '\n' +
        "    <from group=\"observations\">location loc, physical_parameter pp</from>"                               + '\n' +
        "    <where group=\"observations\">loc.location_id = lm.location_id</where>"                                + '\n' +
        "    <orderby group=\"observations\" sens=\"ASC\">loc.platform_code, loc.instrument_code</orderby>"         + '\n' +
        "</ns3:query>" + '\n';

        org.constellation.generic.filter.Select select = new org.constellation.generic.filter.Select();
        select.setGroup("filterObservation");
        select.setType("text");
        select.setvalue("loc.location_date");

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

        org.constellation.generic.filter.Query query = new org.constellation.generic.filter.Query();
        query.setName("ObservationAffinage");
        query.addSelect(select);
        query.addFrom(from);
        query.addWhere(where);
        query.addOrderby(order);

        HashMap<String, String> parameters = new HashMap<String, String>();

        parameters.put("st1", "plouf");

        query.setParameters(parameters);

        Query querys = new Query("platformList", new Select("platformList", "platf"), new From("(select '13471' from dual)"));
        Orderby order2 = new Orderby();
        order2.setvalue("name");
        querys.getOrderby().add(order2);

        query.setStatique(new QueryList(querys));

        StringWriter sw = new StringWriter();

        marshaller.marshal(query, sw);

        String result = sw.toString();
        
        assertEquals(expResult, result);
    }
}
