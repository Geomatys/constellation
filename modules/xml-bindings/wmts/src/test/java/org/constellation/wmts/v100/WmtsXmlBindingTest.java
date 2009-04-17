/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
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

package org.constellation.wmts.v100;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.constellation.gml.v311.DirectPositionType;
import org.constellation.gml.v311.PointType;
import org.constellation.ows.v110.CodeType;

import org.geotoolkit.xml.MarshallerPool;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class WmtsXmlBindingTest {

    private MarshallerPool pool;
    private Unmarshaller unmarshaller;
    private Marshaller   marshaller;

    @Before
    public void setUp() throws JAXBException {
        pool = new MarshallerPool("http://www.opengis.net/wmts/1.0", "org.constellation.wmts.v100:" +
                                                                     "org.constellation.gml.v311:" +
                                                                     "org.constellation.ows.v110");
        unmarshaller = pool.acquireUnmarshaller();
        marshaller   = pool.acquireMarshaller();
    }

    @After
    public void tearDown() {
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
    public void unmarshallingTest() throws JAXBException {

        String xml = "<TileMatrix xmlns=\"http://www.opengis.net/wmts/1.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:gml=\"http://www.opengis.net/gml\" >" + '\n' +
                     "  <ows:Identifier>16d</ows:Identifier>" + '\n' +
                     "  <ScaleDenominator>55218.001386</ScaleDenominator>" + '\n' +
                     "  <TopLeftPoint>" + '\n' +
                     "      <gml:Point>" + '\n' +
                     "          <gml:pos>-90.080000 29.982000</gml:pos>" + '\n' +
                     "      </gml:Point>" + '\n' +
                     "  </TopLeftPoint>" + '\n' +
                     "  <TileWidth>256</TileWidth>" + '\n' +
                     "  <TileHeight>256</TileHeight>" + '\n' +
                     "  <MatrixWidth>3</MatrixWidth>" + '\n' +
                     "  <MatrixHeight>3</MatrixHeight>" + '\n' +
                     " </TileMatrix>";

        StringReader sr = new StringReader(xml);
        TileMatrix result = (TileMatrix) unmarshaller.unmarshal(sr);

        PointType pt = new PointType(null, new DirectPositionType(-90.080000, 29.982000));
        TileMatrix expResult = new TileMatrix(new CodeType("16d"),
                                              55218.001386,
                                              new TopLeftPoint(pt),
                                              new BigInteger("256"),
                                              new BigInteger("256"),
                                              new BigInteger("3"),
                                              new BigInteger("3"));

        assertEquals(expResult, result);
    
    }

    /**
     * Test simple Record Marshalling.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void marshallingTest() throws Exception {

        PointType pt = new PointType(null, new DirectPositionType(-90.080000, 29.982000));
        TileMatrix matrix = new TileMatrix(new CodeType("16d"),
                                              55218.001386,
                                              new TopLeftPoint(pt),
                                              new BigInteger("256"),
                                              new BigInteger("256"),
                                              new BigInteger("3"),
                                              new BigInteger("3"));


        StringWriter sw = new StringWriter();
        marshaller.marshal(matrix, sw);
        String result = sw.toString();

        //System.out.println("RESULT:" + result);
        
        //we remove the first line
        result = result.substring(result.indexOf("?>") + 3);
        //we remove the xmlmns
        result = result.replace(" xmlns:ows=\"http://www.opengis.net/ows/1.1\"", "");
        result = result.replace(" xmlns:gml=\"http://www.opengis.net/gml\"", "");
        result = result.replace(" xmlns=\"http://www.opengis.net/wmts/1.0\"", "");
        result = result.replace(" xmlns:xlink=\"http://www.w3.org/1999/xlink\"", "");


        String expResult =
                     "<TileMatrix>" + '\n' +
                     "    <ows:Identifier>16d</ows:Identifier>" + '\n' +
                     "    <ScaleDenominator>55218.001386</ScaleDenominator>" + '\n' +
                     "    <TopLeftPoint>" + '\n' +
                     "        <gml:Point>" + '\n' +
                     "            <gml:pos>-90.08 29.982</gml:pos>" + '\n' +
                     "        </gml:Point>" + '\n' +
                     "    </TopLeftPoint>" + '\n' +
                     "    <TileWidth>256</TileWidth>" + '\n' +
                     "    <TileHeight>256</TileHeight>" + '\n' +
                     "    <MatrixWidth>3</MatrixWidth>" + '\n' +
                     "    <MatrixHeight>3</MatrixHeight>" + '\n' +
                     "</TileMatrix>" + '\n';

        assertEquals(expResult, result);

    }
}
