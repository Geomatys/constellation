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

package org.constellation.citygml.v100.building;

import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

//Junit dependencies
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class CityGMLXMLBindingTest {

    private Logger       logger = Logger.getLogger("org.constellation.filter");
    private Unmarshaller Unmarshaller;
    private Marshaller   Marshaller;


    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        JAXBContext jbcontext = JAXBContext.newInstance("org.constellation.citygml.v100:org.constellation.gml.v311:org.constellation.citygml.v100.building");
        Unmarshaller          = jbcontext.createUnmarshaller();
        Marshaller            = jbcontext.createMarshaller();
        Marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        //Marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapperImpl(""));
        
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
    public void marshalingTest() throws Exception {
    }



}
