/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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

package org.constellation.sml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

//constellation
import org.constellation.ws.rs.NamespacePrefixMapperImpl;
import org.constellation.sml.v100.ObjectFactory;
import org.constellation.sml.v100.ComponentType;
import org.constellation.util.Utils;

// JAXB dependencies
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

//Junit dependencies
import org.constellation.sml.v100.Keywords;
import org.constellation.sml.v100.SensorML;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author guilhem
 */
public class SmlIOTest {

    private Logger       logger = Logger.getLogger("org.constellation.filter");
    private Unmarshaller unmarshaller;
    private Marshaller   marshaller;
    private ObjectFactory sml100Factory = new ObjectFactory();


    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        JAXBContext jbcontext  = JAXBContext.newInstance("org.constellation.sml.v100");
        unmarshaller           = jbcontext.createUnmarshaller();
        marshaller             = jbcontext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapperImpl(""));

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
    public void ComponentMarshalingTest() throws Exception {

        ComponentType compo = new ComponentType();

    }

    /**
     * Test simple Record Marshalling.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void ComponentUnmarshallMarshalingTest() throws Exception {

        InputStream is = Utils.getResourceAsStream("org/constellation/sml/component.xml");
        Object result = unmarshaller.unmarshal(is);
        if (result instanceof JAXBElement) {
            result = ((JAXBElement)result).getValue();
        }
        if (result != null) {
            System.out.println("unmarshalled classes: " + result.getClass().getName());
            System.out.println(result);
        } else {
            System.out.println("unmarshalled Object null ");
        }

        SensorML.Member member = new SensorML.Member();
        member.setRole("urn:x-ogx:def:sensor:OGC:detector");

        ComponentType component = new ComponentType();

        List<JAXBElement<String>> kw = new ArrayList<JAXBElement<String>>();
        kw.add(sml100Factory.createKeywordsKeywordListKeyword("piezometer"));
        kw.add(sml100Factory.createKeywordsKeywordListKeyword("geosciences"));
        kw.add(sml100Factory.createKeywordsKeywordListKeyword("point d'eau"));
        Keywords keywords = new Keywords(new Keywords.KeywordList("urn:x-brgm:def:gcmd:keywords", kw));
        component.setKeywords(keywords);

        
        member.setProcess(sml100Factory.createComponent(component));
        SensorML expectedResult = new SensorML("1.0", Arrays.asList(member));

    }

}
