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

package org.constellation.configuration;

import org.apache.sis.test.XMLComparator;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.dto.SimpleValue;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.glassfish.jersey.jettison.JettisonConfig;
import org.glassfish.jersey.jettison.JettisonJaxbContext;
import org.glassfish.jersey.jettison.JettisonMarshaller;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @author Cédric Briançon (Geomatys)
 * @author Quentin Boileau (Geomatys)
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
            pool.recycle(unmarshaller);
        }
        if (marshaller != null) {
            pool.recycle(marshaller);
        }
    }

    /**
     * Test ServiceReport Marshalling.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void serviceReportMarshalingTest() throws Exception {
        Map<String, List<String>> instances = new HashMap<>();
        ArrayList<String> prot1 = new ArrayList<>();
        prot1.add("REST");
        instances.put("WMS", prot1);
        ArrayList<String> prot2 = new ArrayList<>();
        prot2.add("REST");
        prot2.add("SOAP");
        instances.put("WPS", prot2);
        ServiceReport report = new ServiceReport(instances);

        StringWriter sw = new StringWriter();
        marshaller.marshal(report, sw);

        String expresult =
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + '\n' +
                "<ns2:ServiceReport xmlns:ns2=\"http://www.constellation.org/config\">" + '\n' +
                "    <ns2:availableServices>" + '\n' +
                "        <ns2:name>WPS</ns2:name>" + '\n' +
                "        <ns2:protocol>REST</ns2:protocol>" + '\n' +
                "        <ns2:protocol>SOAP</ns2:protocol>" + '\n' +
                "    </ns2:availableServices>" + '\n' +
                "    <ns2:availableServices>" + '\n' +
                "        <ns2:name>WMS</ns2:name>" + '\n' +
                "        <ns2:protocol>REST</ns2:protocol>" + '\n' +
                "    </ns2:availableServices>" + '\n' +
                "</ns2:ServiceReport>\n";

        final String result = sw.toString();
        final XMLComparator comparator = new XMLComparator(expresult, result);
        comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
        comparator.compare();
    }

    /**
     * Test InstanceReport Marshalling.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void instanceReportMarshalingTest() throws Exception {
        List<Instance> instances = new ArrayList<>();
        instances.add(new Instance(1, "default", "WMS", ServiceStatus.STARTED));
        instances.add(new Instance(2, "test1", "WMS", ServiceStatus.STOPPED));
        InstanceReport report = new InstanceReport(instances);

        StringWriter sw = new StringWriter();
        marshaller.marshal(report, sw);

        String expresult =
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + '\n'
                + "<ns2:InstanceReport xmlns:ns2=\"http://www.constellation.org/config\">" + '\n'
                + "    <ns2:instance identifier=\"default\" type=\"WMS\" status=\"STARTED\"/>" + '\n'
                + "    <ns2:instance identifier=\"test1\" type=\"WMS\" status=\"STOPPED\"/>" + '\n'
                + "</ns2:InstanceReport>\n";

        final String result = sw.toString();
        final XMLComparator comparator = new XMLComparator(expresult, result);
        comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
        comparator.compare();
    }

    @Test
    public void instanceReportUnMarshalingTest() throws Exception {
        List<Instance> instances = new ArrayList<>();
        instances.add(new Instance(1, "default", "WMS", ServiceStatus.STARTED));
        instances.add(new Instance(2, "test1", "WMS", ServiceStatus.STOPPED));
        InstanceReport expResult = new InstanceReport(instances);


        String xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + '\n'
                + "<ns2:InstanceReport xmlns:ns2=\"http://www.constellation.org/config\">" + '\n'
                + "    <ns2:instance status=\"STARTED\" type=\"WMS\" identifier=\"default\"/>" + '\n'
                + "    <ns2:instance status=\"STOPPED\" type=\"WMS\" identifier=\"test1\"/>" + '\n'
                + "</ns2:InstanceReport>\n";

        Object result =  unmarshaller.unmarshal(new StringReader(xml));

        assertEquals(expResult, result);
    }

    /**
     * Test layerContext Marshalling.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void layerContextMarshalingTest() throws Exception {

        /////////////////////////////////////////
        // Test MapContext with customParameters
        /////////////////////////////////////////
        LayerContext context = new LayerContext();

        context.getCustomParameters().put("multipleVersion", "false");
        StringWriter sw = new StringWriter();
        marshaller.marshal(context, sw);

        String expresult = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + '\n'
                + "<ns2:LayerContext xmlns:ns2=\"http://www.constellation.org/config\">" + '\n'
                + "    <ns2:customParameters>" + '\n'
                + "        <entry>" + '\n'
                + "            <key>multipleVersion</key>" + '\n'
                + "            <value>false</value>" + '\n'
                + "        </entry>" + '\n'
                + "    </ns2:customParameters>" + '\n'
                + "</ns2:LayerContext>\n";

        String result = sw.toString();
        XMLComparator comparator = new XMLComparator(expresult, result);
        comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
        comparator.compare();

        /////////////////////////////////////////
        // Test MapContext with custom GetFeatureInfo
        /////////////////////////////////////////
        context = new LayerContext();

        List<GFIParam> params = new ArrayList<>();
        params.add(new GFIParam("paramKey1", "paramValue1"));
        params.add(new GFIParam("paramKey2", "paramValue2"));

        GetFeatureInfoCfg gfiParam = new GetFeatureInfoCfg("image/png","org.some.package.ClassImage");
        gfiParam.setGfiParameter(params);

        List<GetFeatureInfoCfg> gfiList = new ArrayList<>();
        gfiList.add(new GetFeatureInfoCfg("text/xml","org.some.package.ClassXML"));
        gfiList.add(gfiParam);
        context.setGetFeatureInfoCfgs(gfiList);

        sw = new StringWriter();
        marshaller.marshal(context, sw);

        expresult = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + '\n'
                + "<ns2:LayerContext xmlns:ns2=\"http://www.constellation.org/config\">" + '\n'
                + "    <ns2:customParameters/>" + '\n'
                + "    <ns2:featureInfos>" + '\n'
                + "        <ns2:FeatureInfo mimeType=\"text/xml\" binding=\"org.some.package.ClassXML\"/>" + '\n'
                + "        <ns2:FeatureInfo mimeType=\"image/png\" binding=\"org.some.package.ClassImage\">" + '\n'
                + "            <ns2:parameters>" + '\n'
                + "                <ns2:GFIParam key=\"paramKey1\" value=\"paramValue1\"/>" + '\n'
                + "                <ns2:GFIParam key=\"paramKey2\" value=\"paramValue2\"/>" + '\n'
                + "            </ns2:parameters>" + '\n'
                + "        </ns2:FeatureInfo>" + '\n'
                + "    </ns2:featureInfos>" + '\n'
                + "</ns2:LayerContext>\n";

        result = sw.toString();
        comparator = new XMLComparator(expresult, result);
        comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
        comparator.compare();

        
        /////////////////////////////////////////
        // Test MainLayer and Layer with
        // title, abstract, keyword, MDUrl, DataUrl,
        // authUrl, identifier, attribution, opaque and CRS
        /////////////////////////////////////////
        Layer mainLayer = new Layer(null, null, "mainTitle", null, null, null, null, null, null, null, null, Arrays.asList("CRS-custo1", "CRS-custo2"));
        context = new LayerContext();
        context.setMainLayer(mainLayer);
        sw = new StringWriter();
        marshaller.marshal(context, sw);

        expresult = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + '\n'
                + "<ns2:LayerContext xmlns:ns2=\"http://www.constellation.org/config\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">" + '\n'
                + "    <ns2:mainLayer>" + '\n'
                + "        <ns2:Title>mainTitle</ns2:Title>" + '\n'
                + "        <ns2:CRS>CRS-custo1</ns2:CRS>" + '\n'
                + "        <ns2:CRS>CRS-custo2</ns2:CRS>" + '\n'
                + "    </ns2:mainLayer>" + '\n'
                + "    <ns2:customParameters/>" + '\n'
                + "</ns2:LayerContext>\n";

        result = sw.toString();
        comparator = new XMLComparator(expresult, result);
        comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
        comparator.compare();

       

        /////////////////////////////////////////
        // Test Layer with custom GetFeatureInfo
        ////////////////////////////////////////
        /*

        params = new ArrayList<>();
        params.add(new GFIParam("paramKey1", "paramValue1"));
        params.add(new GFIParam("paramKey2", "paramValue2"));

        gfiParam = new GetFeatureInfoCfg("image/png","org.some.package.ClassImage");
        gfiParam.setGfiParameter(params);

        gfiList = new ArrayList<>();
        gfiList.add(new GetFeatureInfoCfg("text/xml","org.some.package.ClassXML"));
        gfiList.add(gfiParam);

        l1 = new Layer(new QName("layer1"));
        l1.setGetFeatureInfoCfgs(gfiList);
        include.add(l1);
        s1 = new Source("source1", false, include, null);
        sources.add(s1);
        context = new LayerContext(new Layers(sources));
        sw = new StringWriter();
        marshaller.marshal(context, sw);

        expresult = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + '\n'
                + "<ns2:LayerContext xmlns:ns2=\"http://www.constellation.org/config\">" + '\n'
                + "    <ns2:layers>" + '\n'
                + "        <ns2:Source id=\"source1\" load_all=\"false\">" + '\n'
                + "            <ns2:include>" + '\n'
                + "                <ns2:Layer name=\"layer1\">" + '\n'
                + "                    <ns2:featureInfos>" + '\n'
                + "                        <ns2:FeatureInfo mimeType=\"text/xml\" binding=\"org.some.package.ClassXML\"/>" + '\n'
                + "                        <ns2:FeatureInfo mimeType=\"image/png\" binding=\"org.some.package.ClassImage\">" + '\n'
                + "                            <ns2:parameters>" + '\n'
                + "                                <ns2:GFIParam key=\"paramKey1\" value=\"paramValue1\"/>" + '\n'
                + "                                <ns2:GFIParam key=\"paramKey2\" value=\"paramValue2\"/>" + '\n'
                + "                            </ns2:parameters>" + '\n'
                + "                        </ns2:FeatureInfo>" + '\n'
                + "                    </ns2:featureInfos>" + '\n'
                + "                </ns2:Layer>" + '\n'
                + "            </ns2:include>" + '\n'
                + "        </ns2:Source>" + '\n'
                + "    </ns2:layers>" + '\n'
                + "    <ns2:customParameters/>" + '\n'
                + "</ns2:LayerContext>\n";

        result = sw.toString();
        comparator = new XMLComparator(expresult, result);
        comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
        comparator.compare();*/
    }

    /**
     * Test processContext Marshalling.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void processContextMarshalingTest() throws Exception {
        /////////////////////////////////////////
        // Test ProcessContext load all process
        /////////////////////////////////////////
        List<ProcessFactory> factories = new ArrayList<>();
        ProcessFactory s1 = new ProcessFactory();
        s1.setAutorityCode("source1");
        s1.setLoadAll(true);
        ProcessFactory s2 = new ProcessFactory();
        s2.setAutorityCode("source2");
        s2.setLoadAll(true);
        factories.add(s1);
        factories.add(s2);
        ProcessContext context = new ProcessContext(new Processes(factories));
        StringWriter sw = new StringWriter();
        marshaller.marshal(context, sw);

        String expresult = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + '\n'
                + "<ns2:ProcessContext xmlns:ns2=\"http://www.constellation.org/config\">" + '\n'
                + "    <ns2:processes>" + '\n'
                + "        <ns2:ProcessFactory autorityCode=\"source1\" load_all=\"true\"/>" + '\n'
                + "        <ns2:ProcessFactory autorityCode=\"source2\" load_all=\"true\"/>" + '\n'
                + "    </ns2:processes>" + '\n'
                + "    <ns2:customParameters/>" + '\n'
                + "</ns2:ProcessContext>\n";

        String result = sw.toString();
        XMLComparator comparator = new XMLComparator(expresult, result);
        comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
        comparator.compare();

        /////////////////////////////////////////
        // Test ProcessContext include process
        /////////////////////////////////////////
        factories = new ArrayList<>();
        List<Process> include = new ArrayList<>();
        Process l1 = new Process();
        l1.setId("process1");
        Process l2 = new Process();
        l2.setId("process2");
        include.add(l1);
        include.add(l2);
        s1 = new ProcessFactory();
        s1.setInclude(new ProcessList(include));
        s1.setAutorityCode("source1");
        s2 = new ProcessFactory();
        s2.setLoadAll(true);
        s2.setAutorityCode("source2");
        factories.add(s1);
        factories.add(s2);
        context = new ProcessContext(new Processes(factories));
        sw = new StringWriter();
        marshaller.marshal(context, sw);

        expresult = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + '\n'
                + "<ns2:ProcessContext xmlns:ns2=\"http://www.constellation.org/config\">" + '\n'
                + "    <ns2:processes>" + '\n'
                + "        <ns2:ProcessFactory autorityCode=\"source1\">" + '\n'
                + "            <ns2:include>" + '\n'
                + "                <ns2:Process id=\"process1\"/>" + '\n'
                + "                <ns2:Process id=\"process2\"/>" + '\n'
                + "            </ns2:include>" + '\n'
                + "        </ns2:ProcessFactory>" + '\n'
                + "        <ns2:ProcessFactory autorityCode=\"source2\" load_all=\"true\"/>" + '\n'
                + "    </ns2:processes>" + '\n'
                + "    <ns2:customParameters/>" + '\n'
                + "</ns2:ProcessContext>\n";

        result = sw.toString();
        comparator = new XMLComparator(expresult, result);
        comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
        comparator.compare();
    }

    /**
     * Test processContext Marshalling.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void webdavContextMarshalingTest() throws Exception {
        WebdavContext context = new WebdavContext("/home/guilhem");
        StringWriter sw = new StringWriter();
        marshaller.marshal(context, sw);

        String expresult = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + '\n'
                + "<ns2:WebdavContext xmlns:ns2=\"http://www.constellation.org/config\">" + '\n'
                + "    <ns2:rootFile>/home/guilhem</ns2:rootFile>" + '\n'
                + "    <ns2:digestAllowed>true</ns2:digestAllowed>" + '\n'
                + "    <ns2:hideDotFile>true</ns2:hideDotFile>" + '\n'
                + "    <ns2:contextPath>webdav</ns2:contextPath>" + '\n'
                + "</ns2:WebdavContext>\n";

        final String result = sw.toString();
        final XMLComparator comparator = new XMLComparator(expresult, result);
        comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
        comparator.compare();
    }

    /**
     * Test layerContext Marshalling.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void layerContextUnmarshalingTest() throws Exception {
        /////////////////////////////////////////
        // Test LayerContext source load all
        /////////////////////////////////////////
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + '\n'
                + "<ns2:LayerContext xmlns:ns2=\"http://www.constellation.org/config\">" + '\n'
                + "    <ns2:layers/>" + '\n'
                + "</ns2:LayerContext>\n";

        
        LayerContext expresult = new LayerContext();

        LayerContext result = (LayerContext) unmarshaller.unmarshal(new StringReader(xml));

        
        assertEquals(expresult, result);

        /////////////////////////////////////////
        // Test LayerContext complete layer
        /////////////////////////////////////////
        /*
        xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + '\n'
                + "<ns2:LayerContext xmlns:ns2=\"http://www.constellation.org/config\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">" + '\n'
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

        sources = new ArrayList<>();
        include = new ArrayList<>();
        l1 = new Layer(new QName("layer1"),
                       "some title human readeable",
                       " a resume about the layer",
                       Arrays.asList("key1", "key2"),
                       new FormatURL(null, "ISO19115:2003", "text/xml", new Reference("someurl", null, "")),
                       new FormatURL("application/zip", new Reference("http://.../download/06B42F5-9971", null, "")),
                       new FormatURL("AGIVId",  null, null, new Reference("http://www.agiv.be/index.html", null, "")),
                       new Reference("AGIVId", "0245A84E-15B8-4228-B11E-334C91ABA34F"),
                       new AttributionType("State College University",
                                           new Reference("http://www.university.edu/", null, ""),
                                           new FormatURL(100, 100, "image/gif", new Reference("http://www.university.edu/icons/logo.gif", null, ""))),
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
        expresult = new LayerContext(new Layers(mainLayer, sources));

        result = (LayerContext) unmarshaller.unmarshal(new StringReader(xml));

        assertEquals(expresult.getLayers().size(), result.getLayers().size());
        assertEquals(expresult.getLayers().get(0).getInclude().get(0).getAttribution().getOnlineResource(), result.getLayers().get(0).getInclude().get(0).getAttribution().getOnlineResource());
        assertEquals(expresult.getLayers().get(0).getInclude().get(0).getAttribution().getLogoURL(), result.getLayers().get(0).getInclude().get(0).getAttribution().getLogoURL());
        assertEquals(expresult.getLayers().get(0).getInclude().get(0).getAttribution(), result.getLayers().get(0).getInclude().get(0).getAttribution());
        assertEquals(expresult.getLayers().get(0).getInclude().get(0).getDataURL(), result.getLayers().get(0).getInclude().get(0).getDataURL());
        assertEquals(expresult.getLayers().get(0).getInclude().get(0).getMetadataURL(), result.getLayers().get(0).getInclude().get(0).getMetadataURL());
        assertEquals(expresult.getLayers().get(0).getInclude().get(0).getAuthorityURL(), result.getLayers().get(0).getInclude().get(0).getAuthorityURL());
        assertEquals(expresult.getLayers().get(0).getInclude().get(0), result.getLayers().get(0).getInclude().get(0));
        assertEquals(expresult.getLayers().get(0).getInclude(), result.getLayers().get(0).getInclude());
        assertEquals(expresult.getLayers().get(0), result.getLayers().get(0));
        assertEquals(expresult.getLayers().get(1), result.getLayers().get(1));
        
        assertEquals(expresult.getMainLayer(), result.getMainLayer());
        assertEquals(expresult, result);*/


        /////////////////////////////////////////
        // Test LayerContext custom parameters
        /////////////////////////////////////////
        xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + '\n'
                + "<c:LayerContext xmlns:c=\"http://www.constellation.org/config\">" + '\n'
                + "    <c:layers/>" + '\n'
                + "    <c:customParameters>" + '\n'
                + "        <entry>" + '\n'
                + "            <key>transactionSecurized</key>" + '\n'
                + "            <value>false</value>" + '\n'
                + "        </entry>" + '\n'
                + "    </c:customParameters>" + '\n'
                + " </c:LayerContext>\n";

        result = (LayerContext) unmarshaller.unmarshal(new StringReader(xml));

        expresult = new LayerContext();
        expresult.getCustomParameters().put("transactionSecurized", "false");
        
        assertEquals(expresult, result);

        /////////////////////////////////////////
        // Test MapContext with custom GetFeatureInfo
        /////////////////////////////////////////
        xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + '\n'
                + "<ns2:LayerContext xmlns:ns2=\"http://www.constellation.org/config\">" + '\n'
                + "    <ns2:layers>" + '\n'
                + "    </ns2:layers>" + '\n'
                + "    <ns2:customParameters/>" + '\n'
                + "    <ns2:featureInfos>" + '\n'
                + "        <ns2:FeatureInfo mimeType=\"image/png\" binding=\"org.some.package.ClassImage\">" + '\n'
                + "            <ns2:parameters>" + '\n'
                + "                <ns2:GFIParam key=\"paramKey1\" value=\"paramValue1\"/>" + '\n'
                + "            </ns2:parameters>" + '\n'
                + "        </ns2:FeatureInfo>" + '\n'
                + "    </ns2:featureInfos>" + '\n'
                + "</ns2:LayerContext>\n";

        result = (LayerContext) unmarshaller.unmarshal(new StringReader(xml));

        List<GFIParam> params = new ArrayList<>();
        params.add(new GFIParam("paramKey1", "paramValue1"));

        GetFeatureInfoCfg gfiParam = new GetFeatureInfoCfg("image/png","org.some.package.ClassImage");
        gfiParam.setGfiParameter(params);

        List<GetFeatureInfoCfg> gfiList = new ArrayList<>();
        gfiList.add(gfiParam);

        expresult = new LayerContext();
        expresult.setGetFeatureInfoCfgs(gfiList);

        
        assertEquals(expresult.getGetFeatureInfoCfgs(), result.getGetFeatureInfoCfgs());
        assertEquals(expresult.getGetFeatureInfoCfgs().get(0).getMimeType(), result.getGetFeatureInfoCfgs().get(0).getMimeType());
        assertEquals(expresult.getGetFeatureInfoCfgs().get(0).getBinding(), result.getGetFeatureInfoCfgs().get(0).getBinding());
        assertEquals(expresult.getGetFeatureInfoCfgs().get(0).getGfiParameter().get(0).getKey(), result.getGetFeatureInfoCfgs().get(0).getGfiParameter().get(0).getKey());
        assertEquals(expresult.getGetFeatureInfoCfgs().get(0).getGfiParameter().get(0).getValue(), result.getGetFeatureInfoCfgs().get(0).getGfiParameter().get(0).getValue());
        assertEquals(expresult, result);
        

        /////////////////////////////////////////
        // Test Layer with StyleReference
        /////////////////////////////////////////
       /* xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + '\n'
                + "<ns2:LayerContext xmlns:ns2=\"http://www.constellation.org/config\">" + '\n'
                + "    <ns2:layers>" + '\n'
                + "        <ns2:Source load_all=\"false\" id=\"source1\">" + '\n'
                + "            <ns2:include>" + '\n'
                + "                <ns2:Layer name=\"layer1\">" + '\n'
                + "                    <ns2:Style>${providerStyleType|sldProviderId|styleName}</ns2:Style>" + '\n'
                + "                </ns2:Layer>" + '\n'
                + "            </ns2:include>" + '\n'
                + "        </ns2:Source>" + '\n'
                + "    </ns2:layers>" + '\n'
                + "    <ns2:customParameters/>" + '\n'
                + "</ns2:LayerContext>\n";

        sources = new ArrayList<>();
        include = new ArrayList<>();
        l1 = new Layer(new QName("layer1"), Collections.singletonList(new DataReference("${providerStyleType|sldProviderId|styleName}")));
        include.add(l1);
        s1 = new Source("source1", false, include, null);
        sources.add(s1);
        expresult = new LayerContext(new Layers(sources));

        result = (LayerContext) unmarshaller.unmarshal(new StringReader(xml));

        assertEquals(expresult, result);*/

        /////////////////////////////////////////
        // Test Layer with BBOX
        /////////////////////////////////////////
        /*
        xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + '\n'
                + "<ns2:LayerContext xmlns:ns2=\"http://www.constellation.org/config\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:gml=\"http://www.opengis.net/gml\">" + '\n'
                + "    <ns2:layers>" + '\n'
                + "        <ns2:Source load_all=\"false\" id=\"source1\">" + '\n'
                + "            <ns2:include>" + '\n'
                + "                <ns2:Layer name=\"layer1\">" + '\n'
                + "                    <ns2:Style>${providerStyleType|sldProviderId|styleName}</ns2:Style>" + '\n'
                + "                    <ns2:Filter>" + '\n'
                + "                        <ogc:BBOX>"+ '\n'
                + "                            <ogc:PropertyName>property</ogc:PropertyName>"+ '\n'
                + "                            <gml:Envelope srsName=\"CRS:84\">" + '\n'
                + "                                <gml:lowerCorner>-180.0 -90.0</gml:lowerCorner>" + '\n'
                + "                                <gml:upperCorner>180.0 90.0</gml:upperCorner>" + '\n'
                + "                            </gml:Envelope>" + '\n'
                + "                        </ogc:BBOX>" + '\n'
                + "                    </ns2:Filter>"+ '\n'
                + "                </ns2:Layer>" + '\n'
                + "            </ns2:include>" + '\n'
                + "        </ns2:Source>" + '\n'
                + "    </ns2:layers>" + '\n'
                + "    <ns2:customParameters/>" + '\n'
                + "</ns2:LayerContext>\n";

        sources = new ArrayList<>();
        include = new ArrayList<>();
        final FilterType filter = new FilterType();

        final BBOXType bbox = new BBOXType("property", -180, -90, 180, 90, "CRS:84");
        filter.setSpatialOps(bbox);
        l1 = new Layer(new QName("layer1"), Collections.singletonList(new DataReference("${providerStyleType|sldProviderId|styleName}")),
                       filter, null, null, null, null, null, null, null, null, null, null, null);
        include.add(l1);
        s1 = new Source("source1", false, include, null);
        sources.add(s1);

        expresult = new LayerContext(new Layers(sources));

        result = (LayerContext) unmarshaller.unmarshal(new StringReader(xml));
        assertEquals(expresult.getMainLayer(), result.getMainLayer());
        assertEquals(expresult.getSecurity(), result.getSecurity());
        assertEquals(expresult.getCustomParameters(), result.getCustomParameters());
        assertEquals(expresult.getLayers().size(), result.getLayers().size());
        assertEquals(expresult.getLayers().get(0).getId(), result.getLayers().get(0).getId());
        assertEquals(expresult.getLayers().get(0).getExclude(), result.getLayers().get(0).getExclude());
        assertEquals(expresult.getLayers().get(0).getLoadAll(), result.getLayers().get(0).getLoadAll());
        assertEquals(expresult.getLayers().get(0).getInclude().get(0).getStyles(), result.getLayers().get(0).getInclude().get(0).getStyles());

        assertEquals(expresult.getLayers().get(0).getInclude().get(0).getFilter(), result.getLayers().get(0).getInclude().get(0).getFilter());

        assertEquals(expresult.getLayers().get(0).getInclude().get(0).getFilter().getId(), result.getLayers().get(0).getInclude().get(0).getFilter().getId());
        assertEquals(expresult.getLayers().get(0).getInclude().get(0).getFilter().getSpatialOps().getDeclaredType(), result.getLayers().get(0).getInclude().get(0).getFilter().getSpatialOps().getDeclaredType());
        assertEquals(expresult.getLayers().get(0).getInclude().get(0).getFilter().getSpatialOps().getName(), result.getLayers().get(0).getInclude().get(0).getFilter().getSpatialOps().getName());
        assertEquals(expresult.getLayers().get(0).getInclude().get(0).getFilter().getSpatialOps().getValue(), result.getLayers().get(0).getInclude().get(0).getFilter().getSpatialOps().getValue());
        assertEquals(expresult.getLayers().get(0).getInclude(), result.getLayers().get(0).getInclude());
        
        assertEquals(expresult, result);

        /////////////////////////////////////////
        // Test Layer with Custom FeatureInfo
        /////////////////////////////////////////
        xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + '\n'
                + "<ns2:LayerContext xmlns:ns2=\"http://www.constellation.org/config\">" + '\n'
                + "    <ns2:layers>" + '\n'
                + "        <ns2:Source id=\"source1\" load_all=\"false\">" + '\n'
                + "            <ns2:include>" + '\n'
                + "                <ns2:Layer name=\"layer1\">" + '\n'
                + "                    <ns2:featureInfos>" + '\n'
                + "                        <ns2:FeatureInfo mimeType=\"image/png\" binding=\"org.some.package.ClassImage\">" + '\n'
                + "                            <ns2:parameters>" + '\n'
                + "                                <ns2:GFIParam key=\"paramKey1\" value=\"paramValue1\"/>" + '\n'
                + "                            </ns2:parameters>" + '\n'
                + "                        </ns2:FeatureInfo>" + '\n'
                + "                    </ns2:featureInfos>" + '\n'
                + "                </ns2:Layer>" + '\n'
                + "            </ns2:include>" + '\n'
                + "        </ns2:Source>" + '\n'
                + "    </ns2:layers>" + '\n'
                + "    <ns2:customParameters/>" + '\n'
                + "</ns2:LayerContext>\n";

        result = (LayerContext) unmarshaller.unmarshal(new StringReader(xml));

        sources = new ArrayList<>();
        include = new ArrayList<>();

        params = new ArrayList<>();
        params.add(new GFIParam("paramKey1", "paramValue1"));

        gfiParam = new GetFeatureInfoCfg("image/png","org.some.package.ClassImage");
        gfiParam.setGfiParameter(params);

        gfiList = new ArrayList<>();
        gfiList.add(gfiParam);

        l1 = new Layer(new QName("layer1"));
        l1.setGetFeatureInfoCfgs(gfiList);
        include.add(l1);
        s1 = new Source("source1", false, include, null);
        sources.add(s1);
        expresult = new LayerContext(new Layers(sources));

        assertEquals(expresult.getMainLayer(), result.getMainLayer());
        assertEquals(expresult.getSecurity(), result.getSecurity());
        assertEquals(expresult.getCustomParameters(), result.getCustomParameters());
        assertEquals(expresult.getLayers().size(), result.getLayers().size());
        assertEquals(expresult.getLayers().get(0).getId(), result.getLayers().get(0).getId());
        assertEquals(expresult.getLayers().get(0).getExclude(), result.getLayers().get(0).getExclude());
        assertEquals(expresult.getLayers().get(0).getLoadAll(), result.getLayers().get(0).getLoadAll());

        Layer resultLayer = result.getLayers().get(0).getInclude().get(0);
        Layer expLayer = l1;
        assertEquals(expLayer.getGetFeatureInfoCfgs(), resultLayer.getGetFeatureInfoCfgs());
        assertEquals(expLayer.getGetFeatureInfoCfgs().get(0).getMimeType(), resultLayer.getGetFeatureInfoCfgs().get(0).getMimeType());
        assertEquals(expLayer.getGetFeatureInfoCfgs().get(0).getBinding(), resultLayer.getGetFeatureInfoCfgs().get(0).getBinding());
        assertEquals(expLayer.getGetFeatureInfoCfgs().get(0).getGfiParameter().get(0).getKey(), resultLayer.getGetFeatureInfoCfgs().get(0).getGfiParameter().get(0).getKey());
        assertEquals(expLayer.getGetFeatureInfoCfgs().get(0).getGfiParameter().get(0).getValue(), resultLayer.getGetFeatureInfoCfgs().get(0).getGfiParameter().get(0).getValue());

        
        assertEquals(expresult, result);*/
    }

    @Test
    public void stringListMarshalingTest() throws Exception {
        final List<String> list = new ArrayList<>();
        list.add("value1");
        list.add("value2");
        final StringList sl = new StringList(list);
        StringWriter sw = new StringWriter();
        marshaller.marshal(sl, sw);

        String expresult = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + '\n'
                + "<ns2:StringList xmlns:ns2=\"http://www.constellation.org/config\">" + '\n'
                + "    <ns2:Entry>value1</ns2:Entry>" + '\n'
                + "    <ns2:Entry>value2</ns2:Entry>" + '\n'
                + "</ns2:StringList>\n";

        String result = expresult;
        XMLComparator comparator = new XMLComparator(result, result);
        comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
        comparator.compare();

        final Set<String> set = new HashSet<>();
        set.add("value1");
        set.add("value2");
        final StringList slSet = new StringList(set);
        sw = new StringWriter();
        marshaller.marshal(slSet, sw);

        result = sw.toString();
        comparator = new XMLComparator(expresult, result);
        comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
        comparator.compare();
    }

    @Test
    public void stringListUnMarshalingTest() throws Exception {
        final List<String> list = new ArrayList<>();
        list.add("value1");
        list.add("value2");
        final StringList expResult = new StringList(list);


        String xml =
                  "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + '\n'
                + "<ns2:StringList xmlns:ns2=\"http://www.constellation.org/config\">" + '\n'
                + "    <ns2:Entry>value1</ns2:Entry>" + '\n'
                + "    <ns2:Entry>value2</ns2:Entry>" + '\n'
                + "</ns2:StringList>\n";


        Object result =  unmarshaller.unmarshal(new StringReader(xml));
        assertEquals(expResult, result);
    }
    
    @Test
    public void simpleValueMarshallingTest() throws Exception {
        final SimpleValue value = new SimpleValue("test");
        
        Map<String, String> nSMap = new HashMap<>(0);
        nSMap.put("http://www.constellation.org/config", "constellation-config");

        // create json marshaller configuration and context
        JettisonConfig config = JettisonConfig.mappedJettison().xml2JsonNs(nSMap).build();
        JettisonJaxbContext cxtx = new JettisonJaxbContext(config, SimpleValue.class);

        // create marshaller
        JettisonMarshaller jsonMarshaller = cxtx.createJsonMarshaller();

        // Marshall object
        jsonMarshaller.marshallToJSON(value, System.out);

        
    }
}
