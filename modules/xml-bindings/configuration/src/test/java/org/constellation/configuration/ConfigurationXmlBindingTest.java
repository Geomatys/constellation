/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.constellation.configuration;

import java.util.Arrays;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.geotoolkit.xml.MarshallerPool;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author guilhem
 */
public class ConfigurationXmlBindingTest {

    private MarshallerPool pool;
    private Unmarshaller unmarshaller;
    private Marshaller   marshaller;

    @Before
    public void setUp() throws JAXBException {
        pool = GenericDatabaseMarshallerPool.getInstance();
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
    public void layerContextMarshalingTest() throws Exception {
        List<Source> sources = new ArrayList<Source>();
        Source s1 = new Source("source1", true, null, null);
        Source s2 = new Source("source2", true, null, null);
        sources.add(s1);
        sources.add(s2);
        LayerContext context = new LayerContext(new Layers(sources));
        StringWriter sw = new StringWriter();
        marshaller.marshal(context, sw);

        String expresult = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + '\n'
                + "<ns2:LayerContext >" + '\n'
                + "    <ns2:layers>" + '\n'
                + "        <ns2:Source load_all=\"true\" id=\"source1\"/>" + '\n'
                + "        <ns2:Source load_all=\"true\" id=\"source2\"/>" + '\n'
                + "    </ns2:layers>" + '\n'
                + "</ns2:LayerContext>\n";

        String result = removeXmlns(sw.toString());
        assertEquals(expresult, result);

        sources = new ArrayList<Source>();
        List<Layer> exclude = new ArrayList<Layer>();
        Layer l1 = new Layer(new QName("layer1"));
        Layer l2 = new Layer(new QName("layer2"));
        exclude.add(l1);
        exclude.add(l2);
        s1 = new Source("source1", true, null, exclude);
        s2 = new Source("source2", true, null, null);
        sources.add(s1);
        sources.add(s2);
        context = new LayerContext(new Layers(sources));
        sw = new StringWriter();
        marshaller.marshal(context, sw);

        expresult = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + '\n'
                + "<ns2:LayerContext >" + '\n'
                + "    <ns2:layers>" + '\n'
                + "        <ns2:Source load_all=\"true\" id=\"source1\">" + '\n'
                + "            <ns2:exclude>" + '\n'
                + "                <ns2:Layer name=\"layer1\"/>" + '\n'
                + "                <ns2:Layer name=\"layer2\"/>" + '\n'
                + "            </ns2:exclude>" + '\n'
                + "        </ns2:Source>" + '\n'
                + "        <ns2:Source load_all=\"true\" id=\"source2\"/>" + '\n'
                + "    </ns2:layers>" + '\n'
                + "</ns2:LayerContext>\n";

        result =  removeXmlns(sw.toString());
        assertEquals(expresult, result);

        sources = new ArrayList<Source>();
        List<Layer> include = new ArrayList<Layer>();
        l1 = new Layer(new QName("layer1"));
        l2 = new Layer(new QName("layer2"));
        include.add(l1);
        include.add(l2);
        s1 = new Source("source1", null, include, null);
        s2 = new Source("source2", true, null, null);
        sources.add(s1);
        sources.add(s2);
        context = new LayerContext(new Layers(sources));
        sw = new StringWriter();
        marshaller.marshal(context, sw);

        expresult = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + '\n'
                + "<ns2:LayerContext >" + '\n'
                + "    <ns2:layers>" + '\n'
                + "        <ns2:Source id=\"source1\">" + '\n'
                + "            <ns2:include>" + '\n'
                + "                <ns2:Layer name=\"layer1\"/>" + '\n'
                + "                <ns2:Layer name=\"layer2\"/>" + '\n'
                + "            </ns2:include>" + '\n'
                + "        </ns2:Source>" + '\n'
                + "        <ns2:Source load_all=\"true\" id=\"source2\"/>" + '\n'
                + "    </ns2:layers>" + '\n'
                + "</ns2:LayerContext>\n";

        result =  removeXmlns(sw.toString());
        assertEquals(expresult, result);

        sources = new ArrayList<Source>();
        include = new ArrayList<Layer>();
        l1 = new Layer(new QName("layer1"), 
                       "some title human readeable",
                       " a resume about the layer",
                       Arrays.asList("key1", "key2"),
                       new FormatURL(null, "ISO19115:2003", "text/xml", "someurl"),
                       new FormatURL("application/zip", "http://.../download/06B42F5-9971"),
                       new FormatURL("AGIVId", null, null, "http://www.agiv.be/index.html"),
                       new Reference("AGIVId", "0245A84E-15B8-4228-B11E-334C91ABA34F"),
                       new AttributionType("State College University",
                                           new Reference("http://www.university.edu/"),
                                           new FormatURL(100, 100, "image/gif", "http://www.university.edu/icons/logo.gif")),
                       true,
                       Arrays.asList("EPSG:666", "EPSG:999"));
        l2 = new Layer(new QName("layer2"));
        include.add(l1);
        include.add(l2);
        s1 = new Source("source1", null, include, null);
        s2 = new Source("source2", true, null, null);
        sources.add(s1);
        sources.add(s2);
        Layer mainLayer = new Layer(null, "mainTitle", null, null, null, null, null, null, null, null, Arrays.asList("CRS-custo1", "CRS-custo2"));
        context = new LayerContext(new Layers(mainLayer, sources));
        sw = new StringWriter();
        marshaller.marshal(context, sw);

        expresult = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + '\n'
                + "<ns2:LayerContext >" + '\n'
                + "    <ns2:layers>" + '\n'
                + "        <ns2:MainLayer>" + '\n'
                + "            <ns2:Title>mainTitle</ns2:Title>" + '\n'
                + "            <ns2:CRS>CRS-custo1</ns2:CRS>" + '\n'
                + "            <ns2:CRS>CRS-custo2</ns2:CRS>" + '\n'
                + "        </ns2:MainLayer>" + '\n'
                + "        <ns2:Source id=\"source1\">" + '\n'
                + "            <ns2:include>" + '\n'
                + "                <ns2:Layer name=\"layer1\">" + '\n'
                + "                    <ns2:Title>some title human readeable</ns2:Title>" + '\n'
                + "                    <ns2:Abstract> a resume about the layer</ns2:Abstract>" + '\n'
                + "                    <ns2:Keyword>key1</ns2:Keyword>" + '\n'
                + "                    <ns2:Keyword>key2</ns2:Keyword>" + '\n'
                + "                    <ns2:MetadataURL type=\"ISO19115:2003\">" + '\n'
                + "                        <ns2:Format>text/xml</ns2:Format>" + '\n'
                + "                        <ns2:OnlineResource xlink:href=\"someurl\"/>" + '\n'
                + "                    </ns2:MetadataURL>" + '\n'
                + "                    <ns2:DataURL>" + '\n'
                + "                        <ns2:Format>application/zip</ns2:Format>" + '\n'
                + "                        <ns2:OnlineResource xlink:href=\"http://.../download/06B42F5-9971\"/>" + '\n'
                + "                    </ns2:DataURL>" + '\n'
                + "                    <ns2:AuthorityURL name=\"AGIVId\">" + '\n'
                + "                        <ns2:OnlineResource xlink:href=\"http://www.agiv.be/index.html\"/>" + '\n'
                + "                    </ns2:AuthorityURL>" + '\n'
                + "                    <ns2:Identifier authority=\"AGIVId\">0245A84E-15B8-4228-B11E-334C91ABA34F</ns2:Identifier>" + '\n'
                + "                    <ns2:Attribution>" + '\n'
                + "                        <ns2:Title>State College University</ns2:Title>" + '\n'
                + "                        <ns2:OnlineResource xlink:href=\"http://www.university.edu/\"/>" + '\n'
                + "                        <ns2:LogoURL height=\"100\" width=\"100\">" + '\n'
                + "                            <ns2:Format>image/gif</ns2:Format>" + '\n'
                + "                            <ns2:OnlineResource xlink:href=\"http://www.university.edu/icons/logo.gif\"/>" + '\n'
                + "                        </ns2:LogoURL>" + '\n'
                + "                    </ns2:Attribution>" + '\n'
                + "                    <ns2:Opaque>true</ns2:Opaque>" + '\n'
                + "                    <ns2:CRS>EPSG:666</ns2:CRS>" + '\n'
                + "                    <ns2:CRS>EPSG:999</ns2:CRS>" + '\n'
                + "                </ns2:Layer>" + '\n'
                + "                <ns2:Layer name=\"layer2\"/>" + '\n'
                + "            </ns2:include>" + '\n'
                + "        </ns2:Source>" + '\n'
                + "        <ns2:Source load_all=\"true\" id=\"source2\"/>" + '\n'
                + "    </ns2:layers>" + '\n'
                + "</ns2:LayerContext>\n";

        result =  removeXmlns(sw.toString());
        assertEquals(expresult, result);

    }

    public static String removeXmlns(String xml) {
        String s = xml;
        s = s.replaceAll("xmlns=\"[^\"]*\" ", "");
        s = s.replaceAll("xmlns=\"[^\"]*\"", "");
        s = s.replaceAll("xmlns:[^=]*=\"[^\"]*\" ", "");
        s = s.replaceAll("xmlns:[^=]*=\"[^\"]*\"", "");
        return s;
    }
}
