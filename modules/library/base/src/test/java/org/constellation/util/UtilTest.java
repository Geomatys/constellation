/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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

package org.constellation.util;

// Junit dependencies
import java.util.ArrayList;
import java.util.List;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Legal Guilhem (Geomatys)
 */
public class UtilTest {

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
     * @throws java.lang.Exception
     */
    @Test
    public void cleanSpecialCharacterTest() throws Exception {

        String dirty = "lé oiseaux chantè à l'aube OLÉÉÉÉÉÉÉ";
        String result = Util.cleanSpecialCharacter(dirty);
        String expresult = "le oiseaux chante a l'aube OLEEEEEEE";
        assertEquals(expresult, result);
    }

    /**
     * @throws java.lang.Exception
     */
    @Test
    public void transformCodeNameTest() throws Exception {

        String dirty = "MISSING_PARAMETER_VALUE";
        String result = Util.transformCodeName(dirty);
        String expresult = "MissingParameterValue";
        assertEquals(expresult, result);

        dirty = "INVALID_PARAMETER_VALUE";
        result = Util.transformCodeName(dirty);
        expresult = "InvalidParameterValue";
        assertEquals(expresult, result);
    }

    /**
     * @throws java.lang.Exception
     */
    @Test
    public void removePrefixTest() throws Exception {

        String dirty = "ns2:what_ever";
        String result = Util.removePrefix(dirty);
        String expresult = "what_ever";
        assertEquals(expresult, result);

        dirty = "csw:GetRecord";
        result = Util.removePrefix(dirty);
        expresult = "GetRecord";
        assertEquals(expresult, result);
    }

    /**
     * @throws java.lang.Exception
     */
    @Test
    public void cleanStringsTest() throws Exception {

        List<String> dirtys = new ArrayList<String>();
        dirtys.add("\t blabla              truc machin");
        dirtys.add("  boouu         \n   tc   \n mach");
        dirtys.add("                                 bcbcbcbcbcbcbcbcbc\n");

        List<String> expResults = new ArrayList<String>();
        expResults.add("blablatrucmachin");
        expResults.add("boouutcmach");
        expResults.add("bcbcbcbcbcbcbcbcbc");

        List<String> results = Util.cleanStrings(dirtys);
        assertEquals(expResults, results);

    }

    /**
     * @throws java.lang.Exception
     */
    @Test
    public void matchesStringfromListTest() throws Exception {
        List<String> dirtys = new ArrayList<String>();
        dirtys.add("whatever");
        dirtys.add("SOMeTHING");
        dirtys.add("oTher");

        assertTrue(Util.matchesStringfromList(dirtys, "something"));

        dirtys = new ArrayList<String>();
        dirtys.add("whatever");
        dirtys.add("oTher");
        dirtys.add("SOMeTHING and other things");

        assertTrue(Util.matchesStringfromList(dirtys, "something"));

        dirtys = new ArrayList<String>();
        dirtys.add("whatever");
        dirtys.add("oTher");
        dirtys.add("SOMeTHING and other things");

        assertTrue(Util.matchesStringfromList(dirtys, "othe"));

        dirtys = new ArrayList<String>();
        dirtys.add("whatever");
        dirtys.add("oTher");
        dirtys.add("SOMeTHING and other things");

        assertFalse(Util.matchesStringfromList(dirtys, "whateveri"));

    }

    /**
     * @throws java.lang.Exception
     */
    @Test
    public void firstToUpperTest() throws Exception {

        String tmp = "hola";
        assertEquals("Hola", Util.firstToUpper(tmp));

        tmp = "Bonjour";
        assertEquals("Bonjour", Util.firstToUpper(tmp));

        tmp = "STUFF";
        assertEquals("STUFF", Util.firstToUpper(tmp));

        tmp = "sTUFF";
        assertEquals("STUFF", Util.firstToUpper(tmp));

    }

     /**
     * @throws java.lang.Exception
     */
    @Test
    public void getOccurenceFrequencyTest() throws Exception {

        String tmp = "this test is not useless";

        assertEquals(2, Util.getOccurenceFrequency(tmp, "i"));
        assertEquals(3, Util.getOccurenceFrequency(tmp, "e"));
        assertEquals(2, Util.getOccurenceFrequency(tmp, "is"));
        assertEquals(2, Util.getOccurenceFrequency(tmp, "es"));
        assertEquals(1, Util.getOccurenceFrequency(tmp, "not"));
        assertEquals(1, Util.getOccurenceFrequency(tmp, "this test"));

        tmp = "this test is not useless, i repeat: this test is not useless";

        assertEquals(2, Util.getOccurenceFrequency(tmp, "this test is not useless"));
    }

     /**
     * @throws java.lang.Exception
     */
    @Test
    public void replacePrefixTest() throws Exception {

        String tmp = "<ns2:Mark1>something<ns2:Mark1>" + '\n' +
                     "<ns2:Mark2>otherthing<ns2:Mark2>";

        String result = Util.replacePrefix(tmp, "Mark1", "csw");

        String expResult = "<csw:Mark1>something<csw:Mark1>" + '\n' +
                           "<ns2:Mark2>otherthing<ns2:Mark2>";

        assertEquals(expResult, result);

        result = Util.replacePrefix(tmp, "Mark3", "csw");

        assertEquals(tmp, result);

        tmp = "<ns2:Mark1>something<ns2:Mark1>" + '\n' +
              "<ns2:Mark2>otherthing<ns2:Mark2>"+ '\n' +
              "<ns2:Mark1>stuff<ns2:Mark1>";

        expResult = "<csw:Mark1>something<csw:Mark1>" + '\n' +
                    "<ns2:Mark2>otherthing<ns2:Mark2>" + '\n' +
                    "<csw:Mark1>stuff<csw:Mark1>";

        result = Util.replacePrefix(tmp, "Mark1", "csw");
        assertEquals(expResult, result);
    }

}
