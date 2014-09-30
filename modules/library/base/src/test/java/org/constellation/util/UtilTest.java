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

package org.constellation.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.sis.util.CharSequences;
import org.geotoolkit.util.StringUtilities;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

// Junit dependencies

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
    public void md5EncoderTest() throws Exception {

        String unencoded = "adminadmin";
        String result = StringUtilities.MD5encode(unencoded);
        String expresult = "f6fdffe48c908deb0f4c3bd36c032e72";
        assertEquals(expresult, result);
    }

    /**
     * @throws java.lang.Exception
     *//*
    @Test
    public void cleanSpecialCharacterTest() throws Exception {

        String dirty = "lé oiseaux chantè à l'aube OLÉÉÉÉÉÉÉ";
        String result = CharSequences.toASCII(dirty).toString();
        String expresult = "le oiseaux chante a l'aube OLEEEEEEE";
        assertEquals(expresult, result);
    }*/

    /**
     * @throws java.lang.Exception
     */
    @Test
    public void transformCodeNameTest() throws Exception {

        String dirty = "MISSING_PARAMETER_VALUE";
        String result = StringUtilities.transformCodeName(dirty);
        String expresult = "MissingParameterValue";
        assertEquals(expresult, result);

        dirty = "INVALID_PARAMETER_VALUE";
        result = StringUtilities.transformCodeName(dirty);
        expresult = "InvalidParameterValue";
        assertEquals(expresult, result);
    }

    /**
     * @throws java.lang.Exception
     */
    @Test
    public void removePrefixTest() throws Exception {

        String dirty = "ns2:what_ever";
        String result = StringUtilities.removePrefix(dirty);
        String expresult = "what_ever";
        assertEquals(expresult, result);

        dirty = "csw:GetRecord";
        result = StringUtilities.removePrefix(dirty);
        expresult = "GetRecord";
        assertEquals(expresult, result);
    }

    /**
     * @throws java.lang.Exception
     */
    @Test
    public void cleanCharSequencesTest() throws Exception {

        List<String> dirtys = new ArrayList<>();
        dirtys.add("\t blabla              truc machin");
        dirtys.add("  boouu         \n   tc   \n mach");
        dirtys.add("                                 bcbcbcbcbcbcbcbcbc\n");

        List<String> expResults = new ArrayList<>();
        expResults.add("blablatrucmachin");
        expResults.add("boouutcmach");
        expResults.add("bcbcbcbcbcbcbcbcbc");

        List<String> results = StringUtilities.cleanCharSequences(dirtys);
        assertEquals(expResults, results);

    }

    /**
     * @throws java.lang.Exception
     */
    @Test
    public void matchesStringfromListTest() throws Exception {
        List<String> dirtys = new ArrayList<>();
        dirtys.add("whatever");
        dirtys.add("SOMeTHING");
        dirtys.add("oTher");

        assertTrue(StringUtilities.matchesStringfromList(dirtys, "something"));

        dirtys = new ArrayList<>();
        dirtys.add("whatever");
        dirtys.add("oTher");
        dirtys.add("SOMeTHING and other things");

        assertTrue(StringUtilities.matchesStringfromList(dirtys, "something"));

        dirtys = new ArrayList<>();
        dirtys.add("whatever");
        dirtys.add("oTher");
        dirtys.add("SOMeTHING and other things");

        assertTrue(StringUtilities.matchesStringfromList(dirtys, "othe"));

        dirtys = new ArrayList<>();
        dirtys.add("whatever");
        dirtys.add("oTher");
        dirtys.add("SOMeTHING and other things");

        assertFalse(StringUtilities.matchesStringfromList(dirtys, "whateveri"));

    }

    /**
     * @throws java.lang.Exception
     */
    @Test
    public void firstToUpperTest() throws Exception {

        String tmp = "hola";
        assertEquals("Hola", StringUtilities.firstToUpper(tmp));

        tmp = "Bonjour";
        assertEquals("Bonjour", StringUtilities.firstToUpper(tmp));

        tmp = "STUFF";
        assertEquals("STUFF", StringUtilities.firstToUpper(tmp));

        tmp = "sTUFF";
        assertEquals("STUFF", StringUtilities.firstToUpper(tmp));

    }

     /**
     * @throws java.lang.Exception
     */
    @Test
    public void replacePrefixTest() throws Exception {

        String tmp = "<ns2:Mark1>something<ns2:Mark1>" + '\n' +
                     "<ns2:Mark2>otherthing<ns2:Mark2>";

        String result = StringUtilities.replacePrefix(tmp, "Mark1", "csw");

        String expResult = "<csw:Mark1>something<csw:Mark1>" + '\n' +
                           "<ns2:Mark2>otherthing<ns2:Mark2>";

        assertEquals(expResult, result);

        result = StringUtilities.replacePrefix(tmp, "Mark3", "csw");

        assertEquals(tmp, result);

        tmp = "<ns2:Mark1>something<ns2:Mark1>" + '\n' +
              "<ns2:Mark2>otherthing<ns2:Mark2>"+ '\n' +
              "<ns2:Mark1>stuff<ns2:Mark1>";

        expResult = "<csw:Mark1>something<csw:Mark1>" + '\n' +
                    "<ns2:Mark2>otherthing<ns2:Mark2>" + '\n' +
                    "<csw:Mark1>stuff<csw:Mark1>";

        result = StringUtilities.replacePrefix(tmp, "Mark1", "csw");
        assertEquals(expResult, result);
    }

    /**
     * @throws java.lang.Exception
     */
    @Test
    public void sortStringListTest() throws Exception {
        String s1 = "bonjour";
        String s2 = "banjo";
        String s3 = "zebre";
        String s4 = "alabama";
        String s5 = "horrible";
        List<String> toSort = new ArrayList<>();
        toSort.add(s1);
        toSort.add(s2);
        toSort.add(s3);
        toSort.add(s4);
        toSort.add(s5);

        Collections.sort(toSort);

        List<String> expResult = new ArrayList<>();
        expResult.add(s4);
        expResult.add(s2);
        expResult.add(s1);
        expResult.add(s5);
        expResult.add(s3);

        assertEquals(expResult, toSort);
    }

    /**
     * @throws java.lang.Exception
     */
    @Test
    public void toCommaSeparatedValuesTest() throws Exception {
        List<String> l = new ArrayList<>();
        l.add("par");
        l.add("le");
        l.add("pouvoir");
        l.add("de");
        l.add("la");
        l.add("lune");

        String result    = StringUtilities.toCommaSeparatedValues(l);
        String expResult = "par,le,pouvoir,de,la,lune";
        assertEquals(expResult, result);
    }

    /**
     * @throws java.lang.Exception
     */
    @Test
    public void toStringListTest() throws Exception {
        List<String> result    = StringUtilities.toStringList("par,le,pouvoir,de,la,lune");
        List<String> expResult = new ArrayList<>();
        expResult.add("par");
        expResult.add("le");
        expResult.add("pouvoir");
        expResult.add("de");
        expResult.add("la");
        expResult.add("lune");
        assertEquals(expResult, result);
    }

    /**
     * @throws java.lang.Exception
     */
    @Test
    public void ContainsMatchTest() throws Exception {
        List<String> list = new ArrayList<>();
        list.add("par");
        list.add("le tres grand ");
        list.add("pouvoir magique ");
        list.add("de");
        list.add("la");
        list.add("super lune");

        assertTrue(StringUtilities.matchesStringfromList(list, "magique"));
        assertTrue(StringUtilities.matchesStringfromList(list, "super"));
        assertTrue(StringUtilities.matchesStringfromList(list, "tres grand"));
        assertFalse(StringUtilities.matchesStringfromList(list, "boulette"));
        assertFalse(StringUtilities.matchesStringfromList(list, "petit"));
    }

    /**
     * @throws java.lang.Exception
     */
    @Test
    public void ContainsIgnoreCaseTest() throws Exception {
        List<String> list = new ArrayList<>();
        list.add("par");
        list.add("le tres grand ");
        list.add("pouvoir magique ");
        list.add("de");
        list.add("la");
        list.add("super lune");

        assertTrue(StringUtilities.containsIgnoreCase(list, "PAR"));
        assertTrue(StringUtilities.containsIgnoreCase(list, "Le TrEs GrAnD "));
        assertTrue(StringUtilities.containsIgnoreCase(list, "super lune"));
        assertFalse(StringUtilities.containsIgnoreCase(list, "pouvoir"));
        assertFalse(StringUtilities.containsIgnoreCase(list, "petit"));
        assertFalse(StringUtilities.containsIgnoreCase(list, "GRAND"));
    }


}
