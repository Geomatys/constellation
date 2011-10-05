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

//Junit dependencies
import java.util.Arrays;
import java.util.HashMap;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class GenericConfigurationTest {



    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
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
    public void queryBuilderTest() throws Exception {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("staticVar01", "something");
        parameters.put("staticVar02", "blavl, bloub");

        Query query = new Query("singleQuery1", new Select("var01", "pp.label"), new From("physical_parameter pp"));

        String result = query.buildSQLQuery(parameters);

        String expResult = "SELECT pp.label AS var01 FROM physical_parameter pp";

        assertEquals(expResult, result);


        Query mquery = new Query("multiQuery1", new Select(Arrays.asList(new Column("var02", "pp.name"), new Column("var03", "tr.id"))),
                                                new From("physical_parameter pp, transduction tr"),
                                                new Where("tr.parameter=pp.id AND pp.id=:${staticVar01} AND tr.parameter IN (:${staticVar02})"));

        result = mquery.buildSQLQuery(parameters);

        expResult = "SELECT pp.name AS var02,tr.id AS var03 FROM physical_parameter pp, transduction tr\n WHERE (tr.parameter=pp.id AND pp.id=something AND tr.parameter IN (blavl, bloub))";
        
        assertEquals(expResult, result);
    }
}
