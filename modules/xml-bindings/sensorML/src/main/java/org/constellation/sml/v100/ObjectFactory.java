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

package org.constellation.sml.v100;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the net.opengis.sensorml package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Process_QNAME = new QName("http://www.opengis.net/sensorML/1.0", "_Process");
    private final static QName _RuleLanguage_QNAME = new QName("http://www.opengis.net/sensorML/1.0", "ruleLanguage");
    private final static QName _ProcessMethod_QNAME = new QName("http://www.opengis.net/sensorML/1.0", "ProcessMethod");
    private final static QName _ComponentArray_QNAME = new QName("http://www.opengis.net/sensorML/1.0", "ComponentArray");
    private final static QName _ProcessModel_QNAME = new QName("http://www.opengis.net/sensorML/1.0", "ProcessModel");
    private final static QName _Method_QNAME = new QName("http://www.opengis.net/sensorML/1.0", "method");
    private final static QName _ProcessChain_QNAME = new QName("http://www.opengis.net/sensorML/1.0", "ProcessChain");
    private final static QName _System_QNAME = new QName("http://www.opengis.net/sensorML/1.0", "System");
    private final static QName _RelaxNG_QNAME = new QName("http://www.opengis.net/sensorML/1.0", "relaxNG");
    private final static QName _DataSource_QNAME = new QName("http://www.opengis.net/sensorML/1.0", "DataSource");
    private final static QName _Component_QNAME = new QName("http://www.opengis.net/sensorML/1.0", "Component");
    private final static QName _Schematron_QNAME = new QName("http://www.opengis.net/sensorML/1.0", "schematron");
    private final static QName _KeywordsKeywordListKeyword_QNAME = new QName("http://www.opengis.net/sensorML/1.0", "keyword");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: net.opengis.sensorml
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link DataSourceType.DataDefinition }
     * 
     */
    public DataSourceType.DataDefinition createDataSourceTypeDataDefinition() {
        return new DataSourceType.DataDefinition();
    }

    /**
     * Create an instance of {@link Rights }
     * 
     */
    public Rights createRights() {
        return new Rights();
    }

    /**
     * Create an instance of {@link ContactInfo.Phone }
     * 
     */
    public ContactInfo.Phone createContactInfoPhone() {
        return new ContactInfo.Phone();
    }

    /**
     * Create an instance of {@link Event }
     * 
     */
    public Event createEvent() {
        return new Event();
    }

    /**
     * Create an instance of {@link Interfaces.InterfaceList }
     * 
     */
    public Interfaces.InterfaceList createInterfacesInterfaceList() {
        return new Interfaces.InterfaceList();
    }

    /**
     * Create an instance of {@link Components.ComponentList }
     * 
     */
    public Components.ComponentList createComponentsComponentList() {
        return new Components.ComponentList();
    }

    /**
     * Create an instance of {@link ProcessMethodType.Rules.RulesDefinition }
     * 
     */
    public ProcessMethodType.Rules.RulesDefinition createProcessMethodTypeRulesRulesDefinition() {
        return new ProcessMethodType.Rules.RulesDefinition();
    }

    /**
     * Create an instance of {@link Connections.ConnectionList }
     * 
     */
    public Connections.ConnectionList createConnectionsConnectionList() {
        return new Connections.ConnectionList();
    }

    /**
     * Create an instance of {@link History }
     * 
     */
    public History createHistory() {
        return new History();
    }

    /**
     * Create an instance of {@link Keywords.KeywordList }
     * 
     */
    public Keywords.KeywordList createKeywordsKeywordList() {
        return new Keywords.KeywordList();
    }

    /**
     * Create an instance of {@link Inputs }
     * 
     */
    public Inputs createInputs() {
        return new Inputs();
    }

    /**
     * Create an instance of {@link ComponentType }
     * 
     */
    public ComponentType createComponentType() {
        return new ComponentType();
    }

    /**
     * Create an instance of {@link ProcessMethodType.Implementation }
     * 
     */
    public ProcessMethodType.Implementation createProcessMethodTypeImplementation() {
        return new ProcessMethodType.Implementation();
    }

    /**
     * Create an instance of {@link ProcessMethodType }
     * 
     */
    public ProcessMethodType createProcessMethodType() {
        return new ProcessMethodType();
    }

    /**
     * Create an instance of {@link PresentationLayerPropertyType }
     * 
     */
    public PresentationLayerPropertyType createPresentationLayerPropertyType() {
        return new PresentationLayerPropertyType();
    }

    /**
     * Create an instance of {@link ComponentArrayType }
     * 
     */
    public ComponentArrayType createComponentArrayType() {
        return new ComponentArrayType();
    }

    /**
     * Create an instance of {@link OnlineResource }
     * 
     */
    public OnlineResource createOnlineResource() {
        return new OnlineResource();
    }

    /**
     * Create an instance of {@link DataSourceType }
     * 
     */
    public DataSourceType createDataSourceType() {
        return new DataSourceType();
    }

    /**
     * Create an instance of {@link ProcessMethodType.Algorithm.AlgorithmDefinition }
     * 
     */
    public ProcessMethodType.Algorithm.AlgorithmDefinition createProcessMethodTypeAlgorithmAlgorithmDefinition() {
        return new ProcessMethodType.Algorithm.AlgorithmDefinition();
    }

    /**
     * Create an instance of {@link Outputs.OutputList }
     * 
     */
    public Outputs.OutputList createOutputsOutputList() {
        return new Outputs.OutputList();
    }

    /**
     * Create an instance of {@link Components.ComponentList.Component }
     * 
     */
    public Components.ComponentList.Component createComponentsComponentListComponent() {
        return new Components.ComponentList.Component();
    }

    /**
     * Create an instance of {@link Inputs.InputList }
     * 
     */
    public Inputs.InputList createInputsInputList() {
        return new Inputs.InputList();
    }

    /**
     * Create an instance of {@link Parameters }
     * 
     */
    public Parameters createParameters() {
        return new Parameters();
    }

    /**
     * Create an instance of {@link LayerPropertyType }
     * 
     */
    public LayerPropertyType createLayerPropertyType() {
        return new LayerPropertyType();
    }

    /**
     * Create an instance of {@link Classification.ClassifierList.Classifier }
     * 
     */
    public Classification.ClassifierList.Classifier createClassificationClassifierListClassifier() {
        return new Classification.ClassifierList.Classifier();
    }

    /**
     * Create an instance of {@link ArrayLink }
     * 
     */
    public ArrayLink createArrayLink() {
        return new ArrayLink();
    }

    /**
     * Create an instance of {@link MethodPropertyType }
     * 
     */
    public MethodPropertyType createMethodPropertyType() {
        return new MethodPropertyType();
    }

    /**
     * Create an instance of {@link ProcessMethodType.Implementation.ImplementationCode }
     * 
     */
    public ProcessMethodType.Implementation.ImplementationCode createProcessMethodTypeImplementationImplementationCode() {
        return new ProcessMethodType.Implementation.ImplementationCode();
    }

    /**
     * Create an instance of {@link DocumentList }
     * 
     */
    public DocumentList createDocumentList() {
        return new DocumentList();
    }

    /**
     * Create an instance of {@link ContactList.Member }
     * 
     */
    public ContactList.Member createContactListMember() {
        return new ContactList.Member();
    }

    /**
     * Create an instance of {@link RuleLanguageType }
     * 
     */
    public RuleLanguageType createRuleLanguageType() {
        return new RuleLanguageType();
    }

    /**
     * Create an instance of {@link ProcessModelType }
     * 
     */
    public ProcessModelType createProcessModelType() {
        return new ProcessModelType();
    }

    /**
     * Create an instance of {@link DataSourceType.ObservationReference }
     * 
     */
    public DataSourceType.ObservationReference createDataSourceTypeObservationReference() {
        return new DataSourceType.ObservationReference();
    }

    /**
     * Create an instance of {@link Link.Destination }
     * 
     */
    public Link.Destination createLinkDestination() {
        return new Link.Destination();
    }

    /**
     * Create an instance of {@link ProcessMethodType.Implementation.ImplementationCode.SourceRef }
     * 
     */
    public ProcessMethodType.Implementation.ImplementationCode.SourceRef createProcessMethodTypeImplementationImplementationCodeSourceRef() {
        return new ProcessMethodType.Implementation.ImplementationCode.SourceRef();
    }

    /**
     * Create an instance of {@link Link.Source }
     * 
     */
    public Link.Source createLinkSource() {
        return new Link.Source();
    }

    /**
     * Create an instance of {@link TemporalReferenceFrame }
     * 
     */
    public TemporalReferenceFrame createTemporalReferenceFrame() {
        return new TemporalReferenceFrame();
    }


    /**
     * Create an instance of {@link ValidTime }
     * 
     */
    public ValidTime createValidTime() {
        return new ValidTime();
    }

    /**
     * Create an instance of {@link Characteristics }
     * 
     */
    public Characteristics createCharacteristics() {
        return new Characteristics();
    }

    /**
     * Create an instance of {@link EventList }
     * 
     */
    public EventList createEventList() {
        return new EventList();
    }

    /**
     * Create an instance of {@link SystemType }
     * 
     */
    public SystemType createSystemType() {
        return new SystemType();
    }

    /**
     * Create an instance of {@link Connection }
     * 
     */
    public Connection createConnection() {
        return new Connection();
    }

    /**
     * Create an instance of {@link Identification }
     * 
     */
    public Identification createIdentification() {
        return new Identification();
    }


    /**
     * Create an instance of {@link ArrayLink.SourceIndex }
     * 
     */
    public ArrayLink.SourceIndex createArrayLinkSourceIndex() {
        return new ArrayLink.SourceIndex();
    }

    /**
     * Create an instance of {@link Capabilities }
     * 
     */
    public CapabilitiesSML createCapabilitiesSML() {
        return new CapabilitiesSML();
    }

    /**
     * Create an instance of {@link ResponsibleParty }
     * 
     */
    public ResponsibleParty createResponsibleParty() {
        return new ResponsibleParty();
    }

    /**
     * Create an instance of {@link ArrayLink.DestinationIndex }
     * 
     */
    public ArrayLink.DestinationIndex createArrayLinkDestinationIndex() {
        return new ArrayLink.DestinationIndex();
    }

    /**
     * Create an instance of {@link Interfaces }
     * 
     */
    public Interfaces createInterfaces() {
        return new Interfaces();
    }

    /**
     * Create an instance of {@link Person }
     * 
     */
    public Person createPerson() {
        return new Person();
    }

    /**
     * Create an instance of {@link Contact }
     * 
     */
    public Contact createContact() {
        return new Contact();
    }

    /**
     * Create an instance of {@link EventList.Member }
     * 
     */
    public EventList.Member createEventListMember() {
        return new EventList.Member();
    }

    /**
     * Create an instance of {@link LegalConstraint }
     * 
     */
    public LegalConstraint createLegalConstraint() {
        return new LegalConstraint();
    }

    /**
     * Create an instance of {@link Outputs }
     * 
     */
    public Outputs createOutputs() {
        return new Outputs();
    }

    /**
     * Create an instance of {@link Identification.IdentifierList }
     * 
     */
    public Identification.IdentifierList createIdentificationIdentifierList() {
        return new Identification.IdentifierList();
    }

    /**
     * Create an instance of {@link Components }
     * 
     */
    public Components createComponents() {
        return new Components();
    }

    /**
     * Create an instance of {@link IoComponentPropertyType }
     * 
     */
    public IoComponentPropertyType createIoComponentPropertyType() {
        return new IoComponentPropertyType();
    }

    /**
     * Create an instance of {@link ProcessMethodType.Algorithm }
     * 
     */
    public ProcessMethodType.Algorithm createProcessMethodTypeAlgorithm() {
        return new ProcessMethodType.Algorithm();
    }

    /**
     * Create an instance of {@link Location }
     * 
     */
    public Location createLocation() {
        return new Location();
    }

    /**
     * Create an instance of {@link Documentation }
     * 
     */
    public Documentation createDocumentation() {
        return new Documentation();
    }

    /**
     * Create an instance of {@link TimePosition }
     * 
     */
    public TimePosition createTimePosition() {
        return new TimePosition();
    }

    /**
     * Create an instance of {@link ProcessMethodType.Rules }
     * 
     */
    public ProcessMethodType.Rules createProcessMethodTypeRules() {
        return new ProcessMethodType.Rules();
    }

    /**
     * Create an instance of {@link ArrayLink.DestinationArray }
     * 
     */
    public ArrayLink.DestinationArray createArrayLinkDestinationArray() {
        return new ArrayLink.DestinationArray();
    }

    /**
     * Create an instance of {@link ProcessMethodType.Algorithm.AlgorithmDefinition.MathML }
     * 
     */
    public ProcessMethodType.Algorithm.AlgorithmDefinition.MathML createProcessMethodTypeAlgorithmAlgorithmDefinitionMathML() {
        return new ProcessMethodType.Algorithm.AlgorithmDefinition.MathML();
    }

    /**
     * Create an instance of {@link ContactList }
     * 
     */
    public ContactList createContactList() {
        return new ContactList();
    }

    /**
     * Create an instance of {@link SpatialReferenceFrame }
     * 
     */
    public SpatialReferenceFrame createSpatialReferenceFrame() {
        return new SpatialReferenceFrame();
    }

    /**
     * Create an instance of {@link Identification.IdentifierList.Identifier }
     * 
     */
    public Identification.IdentifierList.Identifier createIdentificationIdentifierListIdentifier() {
        return new Identification.IdentifierList.Identifier();
    }

    /**
     * Create an instance of {@link Document }
     * 
     */
    public Document createDocument() {
        return new Document();
    }

    /**
     * Create an instance of {@link Position }
     * 
     */
    public Position createPosition() {
        return new Position();
    }

    /**
     * Create an instance of {@link DocumentList.Member }
     * 
     */
    public DocumentList.Member createDocumentListMember() {
        return new DocumentList.Member();
    }

    /**
     * Create an instance of {@link ContactInfo }
     * 
     */
    public ContactInfo createContactInfo() {
        return new ContactInfo();
    }

    /**
     * Create an instance of {@link DataSourceType.Values }
     * 
     */
    public DataSourceType.Values createDataSourceTypeValues() {
        return new DataSourceType.Values();
    }

    /**
     * Create an instance of {@link Connections }
     * 
     */
    public Connections createConnections() {
        return new Connections();
    }

    /**
     * Create an instance of {@link Term }
     * 
     */
    public Term createTerm() {
        return new Term();
    }

    /**
     * Create an instance of {@link ContactInfo.Address }
     * 
     */
    public ContactInfo.Address createContactInfoAddress() {
        return new ContactInfo.Address();
    }

    /**
     * Create an instance of {@link Classification }
     * 
     */
    public Classification createClassification() {
        return new Classification();
    }

    /**
     * Create an instance of {@link Interface }
     * 
     */
    public Interface createInterface() {
        return new Interface();
    }

    /**
     * Create an instance of {@link Link }
     * 
     */
    public Link createLink() {
        return new Link();
    }

    /**
     * Create an instance of {@link ProcessChainType }
     * 
     */
    public ProcessChainType createProcessChainType() {
        return new ProcessChainType();
    }

    /**
     * Create an instance of {@link Classification.ClassifierList }
     * 
     */
    public Classification.ClassifierList createClassificationClassifierList() {
        return new Classification.ClassifierList();
    }

    /**
     * Create an instance of {@link ArrayLink.SourceArray }
     * 
     */
    public ArrayLink.SourceArray createArrayLinkSourceArray() {
        return new ArrayLink.SourceArray();
    }

    /**
     * Create an instance of {@link ProcessMethodType.Implementation.ImplementationCode.BinaryRef }
     * 
     */
    public ProcessMethodType.Implementation.ImplementationCode.BinaryRef createProcessMethodTypeImplementationImplementationCodeBinaryRef() {
        return new ProcessMethodType.Implementation.ImplementationCode.BinaryRef();
    }

    /**
     * Create an instance of {@link Keywords }
     * 
     */
    public Keywords createKeywords() {
        return new Keywords();
    }

    /**
     * Create an instance of {@link Parameters.ParameterList }
     * 
     */
    public Parameters.ParameterList createParametersParameterList() {
        return new Parameters.ParameterList();
    }

    /**
     * Create an instance of {@link InterfaceDefinition }
     * 
     */
    public InterfaceDefinition createInterfaceDefinition() {
        return new InterfaceDefinition();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractProcessType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/sensorML/1.0", name = "_Process")
    public JAXBElement<AbstractProcessType> createProcess(AbstractProcessType value) {
        return new JAXBElement<AbstractProcessType>(_Process_QNAME, AbstractProcessType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RuleLanguageType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/sensorML/1.0", name = "ruleLanguage")
    public JAXBElement<RuleLanguageType> createRuleLanguage(RuleLanguageType value) {
        return new JAXBElement<RuleLanguageType>(_RuleLanguage_QNAME, RuleLanguageType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ProcessMethodType }{@code >}}
     * 
     
    @XmlElementDecl(namespace = "http://www.opengis.net/sensorML/1.0", name = "ProcessMethod")
    public JAXBElement<ProcessMethodType> createProcessMethod(ProcessMethodType value) {
        return new JAXBElement<ProcessMethodType>(_ProcessMethod_QNAME, ProcessMethodType.class, null, value);
    }*/

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ComponentArrayType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/sensorML/1.0", name = "ComponentArray", substitutionHeadNamespace = "http://www.opengis.net/sensorML/1.0", substitutionHeadName = "_Process")
    public JAXBElement<ComponentArrayType> createComponentArray(ComponentArrayType value) {
        return new JAXBElement<ComponentArrayType>(_ComponentArray_QNAME, ComponentArrayType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ProcessModelType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/sensorML/1.0", name = "ProcessModel", substitutionHeadNamespace = "http://www.opengis.net/sensorML/1.0", substitutionHeadName = "_Process")
    public JAXBElement<ProcessModelType> createProcessModel(ProcessModelType value) {
        return new JAXBElement<ProcessModelType>(_ProcessModel_QNAME, ProcessModelType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MethodPropertyType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/sensorML/1.0", name = "method")
    public JAXBElement<MethodPropertyType> createMethod(MethodPropertyType value) {
        return new JAXBElement<MethodPropertyType>(_Method_QNAME, MethodPropertyType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ProcessChainType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/sensorML/1.0", name = "ProcessChain", substitutionHeadNamespace = "http://www.opengis.net/sensorML/1.0", substitutionHeadName = "_Process")
    public JAXBElement<ProcessChainType> createProcessChain(ProcessChainType value) {
        return new JAXBElement<ProcessChainType>(_ProcessChain_QNAME, ProcessChainType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SystemType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/sensorML/1.0", name = "System", substitutionHeadNamespace = "http://www.opengis.net/sensorML/1.0", substitutionHeadName = "_Process")
    public JAXBElement<SystemType> createSystem(SystemType value) {
        return new JAXBElement<SystemType>(_System_QNAME, SystemType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DataSourceType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/sensorML/1.0", name = "DataSource", substitutionHeadNamespace = "http://www.opengis.net/sensorML/1.0", substitutionHeadName = "_Process")
    public JAXBElement<DataSourceType> createDataSource(DataSourceType value) {
        return new JAXBElement<DataSourceType>(_DataSource_QNAME, DataSourceType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ComponentType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/sensorML/1.0", name = "Component", substitutionHeadNamespace = "http://www.opengis.net/sensorML/1.0", substitutionHeadName = "_Process")
    public JAXBElement<ComponentType> createComponent(ComponentType value) {
        return new JAXBElement<ComponentType>(_Component_QNAME, ComponentType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/sensorML/1.0", name = "keyword", scope = Keywords.KeywordList.class)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    public JAXBElement<String> createKeywordsKeywordListKeyword(String value) {
        return new JAXBElement<String>(_KeywordsKeywordListKeyword_QNAME, String.class, Keywords.KeywordList.class, value);
    }

}
