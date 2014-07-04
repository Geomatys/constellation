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

//Junit dependencies

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

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

        HashMap<String, Object> parameters = new HashMap<String, Object>();
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
