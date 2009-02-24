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

package org.constellation.metadata.fra;

import java.io.InputStream;
import java.io.StringWriter;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.geotools.metadata.iso.MetaDataImpl;
import org.geotools.metadata.iso.citation.CitationImpl;
import org.geotools.metadata.iso.citation.ResponsiblePartyImpl;
import org.geotools.referencing.NamedIdentifier;
import org.opengis.referencing.ReferenceIdentifier;

//Junit dependencies
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class metadataXMLBindingTest {

    private Logger       logger = Logger.getLogger("org.constellation.metadata.fra");
    private Unmarshaller unmarshaller;
    private Marshaller   marshaller;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        JAXBContext jbcontext  = JAXBContext.newInstance(MetaDataImpl.class, org.constellation.metadata.fra.ObjectFactory.class);
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
    public void marshallingTest() throws Exception {

        MetaDataImpl metadata = new MetaDataImpl();
        FRADirectReferenceSystemType refSys = new FRADirectReferenceSystemType();
        ReferenceIdentifier name = new NamedIdentifier(new CitationImpl(ResponsiblePartyImpl.EPSG), "4326");
        refSys.setName(name);
        metadata.setReferenceSystemInfo(Arrays.asList(refSys));

        StringWriter sw = new StringWriter();
        marshaller.marshal(metadata, sw);

        InputStream in = getResourceAsStream("org/constellation/metadata/fra/DirectReferenceSystem.xml");
        StringWriter out = new StringWriter();
        byte[] buffer = new byte[1024];
        int size;
        while ((size = in.read(buffer, 0, 1024)) > 0) {
            out.write(new String(buffer, 0, size));
        }

        String expResult = out.toString();
        String result    = sw.toString();

        //we remove the 2 first line because the xlmns are not always in the same order.
        expResult = expResult.substring(expResult.indexOf('\n') + 1);
        expResult = expResult.substring(expResult.indexOf('\n') + 1);

        result = result.substring(result.indexOf('\n') + 1);
        result = result.substring(result.indexOf('\n') + 1);

        assertEquals(expResult, result);
    }

    /**
     * Test simple Record Marshalling.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void UnmarshallingTest() throws Exception {
        InputStream in = getResourceAsStream("org/constellation/metadata/fra/DirectReferenceSystem.xml");
        MetaDataImpl result = (MetaDataImpl) unmarshaller.unmarshal(in);

        MetaDataImpl expResult = new MetaDataImpl();
        FRADirectReferenceSystemType refSys = new FRADirectReferenceSystemType();
        ReferenceIdentifier name = new NamedIdentifier(new CitationImpl(ResponsiblePartyImpl.EPSG), "4326");
        refSys.setName(name);
        expResult.setReferenceSystemInfo(Arrays.asList(refSys));

        //assertEquals(expResult, result);
    }


    /**
     * Return an input stream of the specified resource.
     */
    public static InputStream getResourceAsStream(final String url) {
        ClassLoader cl = getContextClassLoader();
        return cl.getResourceAsStream(url);
    }

    /**
     * Obtain the Thread Context ClassLoader.
     */
    public static ClassLoader getContextClassLoader() {
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            public ClassLoader run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
    }


}
