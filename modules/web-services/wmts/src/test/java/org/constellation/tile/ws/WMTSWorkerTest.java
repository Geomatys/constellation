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
package org.constellation.tile.ws;



import org.junit.*;
import static org.junit.Assert.*;


/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class WMTSWorkerTest {

    

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

    @Test
    public void getLettersFromIntTest() throws Exception {

        String result = DefaultWMTSWorker.getLettersFromInt(0, 25);
        String expResult = "A";

        assertEquals(expResult, result);

        result = DefaultWMTSWorker.getLettersFromInt(0, 26);
        expResult = "AA";

        assertEquals(expResult, result);

        result = DefaultWMTSWorker.getLettersFromInt(26, 120);
        expResult = "BA";

        assertEquals(expResult, result);

        result = DefaultWMTSWorker.getLettersFromInt(51, 120);
        expResult = "BZ";

        assertEquals(expResult, result);

        result = DefaultWMTSWorker.getLettersFromInt(52, 120);
        expResult = "CA";

        result = DefaultWMTSWorker.getLettersFromInt(52, 800);
        expResult = "ACA";

        assertEquals(expResult, result);

        result = DefaultWMTSWorker.getLettersFromInt(1000, 1275);
        expResult = "BMM";

        assertEquals(expResult, result);
    }

     @Test
    public void getNumbersFromIntTest() throws Exception {

        String result = DefaultWMTSWorker.getNumbersFromInt(0, 9);
        String expResult = "1";

        assertEquals(expResult, result);

        result = DefaultWMTSWorker.getNumbersFromInt(0, 11);
        expResult = "01";

        assertEquals(expResult, result);

        result = DefaultWMTSWorker.getNumbersFromInt(12, 120);
        expResult = "013";

        assertEquals(expResult, result);

        result = DefaultWMTSWorker.getNumbersFromInt(121, 1300);
        expResult = "0122";

        assertEquals(expResult, result);

        result = DefaultWMTSWorker.getNumbersFromInt(121, 11300);
        expResult = "00122";

        assertEquals(expResult, result);

        result = DefaultWMTSWorker.getNumbersFromInt(121, 121300);
        expResult = "000122";

        assertEquals(expResult, result);
     }
}
