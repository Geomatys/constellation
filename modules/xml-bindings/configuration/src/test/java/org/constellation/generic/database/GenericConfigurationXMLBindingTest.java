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
        pool = new MarshallerPool(Automatic.class, SOSConfiguration.class);
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
    public void marshalingTest() throws Exception {

        BDD bdd = new BDD("org.driver.test", "http://somehost/blablabla", "bobby", "juanito");
        
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("staticVar01", "something");
        parameters.put("staticVar02", "blavl, bloub");

        Query query = new Query("singleQuery1", new Select("var01", "pp.label"), new From("physical_parameter pp"));
        QueryList single = new QueryList(query);

        Query mquery = new Query("multiQuery1", new Select(Arrays.asList(new Column("var02", "pp.name"), new Column("var03", "tr.id"))),
                                                new From("physical_parameter pp, transduction tr"),
                                                new Where("tr.parameter=pp.id"));

        QueryList multi = new QueryList(mquery);

        Queries queries = new Queries(null, single, multi, parameters);
        Automatic config = new Automatic("MDWEB", bdd, queries);

        StringWriter sw = new StringWriter();
        marshaller.marshal(config, sw);

        String result = sw.toString();
        String expResult =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"                + '\n' +
        "<automatic format=\"MDWEB\" xmlns:ns2=\"http://www.constellation.org/config\">" + '\n' +
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
        "            </query>"                                                         + '\n' +
        "        </multiFixed>"                                                        + '\n' +
        "    </queries>"                                                               + '\n' +
        "</automatic>" + '\n';

        assertEquals(expResult, result);


        bdd = new BDD("org.driver.test", "http://somehost/blablabla", "bobby", "juanito");


        config = new Automatic("MDWEB", bdd, null);

        SOSConfiguration sosConfig = new SOSConfiguration(config, config);

        Automatic config2 = new Automatic("MDWEB", bdd, null);
        config2.setName("coriolis");
        sosConfig.getExtensions().add(config2);
        
        sw = new StringWriter();
        marshaller.marshal(sosConfig, sw);

        result = sw.toString();
        //System.out.println("result:" + result);
        expResult =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"            + '\n' +
        "<ns2:SOSConfiguration xmlns:ns2=\"http://www.constellation.org/config\">" + '\n' +
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
    public void unmarshalingTest() throws Exception {


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

        QueryList multi = new QueryList(mquery);

        Queries queries     = new Queries(null, single, multi, parameters);
        Automatic expResult = new Automatic("MDWEB", bdd, queries);

        assertEquals(expResult, result);
    }
}
