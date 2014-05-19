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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each Java content interface and Java element interface generated
 * in the org.constellation.configuration package.
 * An ObjectFactory allows you to programatically construct new instances of the Java representation for XML content.
 * The Java representation of XML content can consist of schema derived interfaces and classes representing
 * the binding of schema type definitions, element declarations and model groups.
 * Factory methods for each of these are provided in this class.
 *
 */
@XmlRegistry
public class ObjectFactory {

    public static final QName SOURCE_QNAME = new QName("http://www.geotoolkit.org/parameter", "source");
    public static final QName LAYER_QNAME  = new QName("http://www.geotoolkit.org/parameter", "Layer");
    public static final QName INPUT_QNAME  = new QName("http://www.geotoolkit.org/parameter", "input");


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.constellation.configuration
     *
     */
    public ObjectFactory() {
    }

    public MailingProperties createMailingProperties() {
        return new MailingProperties();
    }

    /**
     * Create an instance of {@link AcknowlegementType }
     *
     */
    public AcknowlegementType createAcknowlegementType() {
        return new AcknowlegementType();
    }

    /**
     * Create an instance of {@link SOSConfiguration }
     *
     */
    public SOSConfiguration createSOSConfiguration() {
        return new SOSConfiguration();
    }

    /**
     * Create an instance of {@link HarvestTasks }
     *
     */
    public HarvestTasks createHarvestTasks() {
        return new HarvestTasks();
    }

    /**
     * Create an instance of {@link HarvestTask }
     *
     */
    public HarvestTask createHarvestTask() {
        return new HarvestTask();
    }

    public LayerContext createLayerContext() {
        return new LayerContext();
    }

    public ProcessContext createProcessContext() {
        return new ProcessContext();
    }

    public LayerList createLayerList() {
        return new LayerList();
    }

    public Layers createLayers() {
        return new Layers();
    }

    public Layer createLayer() {
        return new Layer();
    }

    public Source createSource() {
        return new Source();
    }

    public InstanceReport createInstanceReport() {
        return new InstanceReport();
    }

    public ServiceReport createServiceReport() {
        return new ServiceReport();
    }

    public ProviderReport createProviderReport() {
        return new ProviderReport();
    }

    public ProvidersReport createProvidersReport() {
        return new ProvidersReport();
    }

    public Instance createInstance() {
        return new Instance();
    }

    public ExceptionReport createExceptionReport() {
        return new ExceptionReport();
    }

    public StringList createSimpleList(){
        return new StringList();
    }

    public BriefNode createSimpleBriefNode(){
        return new BriefNode();
    }

    public BriefNodeList createSimpleBriefNodeList(){
        return new BriefNodeList();
    }

    public StringMap createSimpleMap(){
        return new StringMap();
    }

    public StringTreeNode createStringTreeNode(){
        return new StringTreeNode();
    }

    public WebdavContext createWebdavContext(){
        return new WebdavContext();
    }

    public TextDecoration createTextDecoration() {
        return new TextDecoration();
    }

    public GridDecoration createGridDecoration() {
        return new GridDecoration();
    }

    public WMSPortrayal createWMSMapPortrayal() {
        return new WMSPortrayal();
    }

    public StyleBrief createStyleBrief() {
        return new StyleBrief();
    }

    public StyleReport createStyleReport() {
        return new StyleReport();
    }

    public DataBrief createDataBrief() {
        return new DataBrief();
    }

    @XmlElementDecl(namespace = "http://www.geotoolkit.org/parameter", name = "source")
    public JAXBElement<Object> createSource(Object value) {
        return new JAXBElement<>(SOURCE_QNAME, Object.class, null, value);
    }

    @XmlElementDecl(namespace = "http://www.geotoolkit.org/parameter", name = "Layer")
    public JAXBElement<Object> createLayer(Object value) {
        return new JAXBElement<>(LAYER_QNAME, Object.class, null, value);
    }

    @XmlElementDecl(namespace = "http://www.geotoolkit.org/parameter", name = "input")
    public JAXBElement<Object> createInput(Object value) {
        return new JAXBElement<>(LAYER_QNAME, Object.class, null, value);
    }

}
