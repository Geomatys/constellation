package net.seagis.se;

import java.math.BigInteger;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the net.opengis.se package. 
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

    private final static QName _WellKnownName_QNAME = new QName("http://www.opengis.net/se", "WellKnownName");
    private final static QName _Format_QNAME = new QName("http://www.opengis.net/se", "Format");
    private final static QName _SourceChannelName_QNAME = new QName("http://www.opengis.net/se", "SourceChannelName");
    private final static QName _GammaValue_QNAME = new QName("http://www.opengis.net/se", "GammaValue");
    private final static QName _NegativePattern_QNAME = new QName("http://www.opengis.net/se", "NegativePattern");
    private final static QName _OverlapBehavior_QNAME = new QName("http://www.opengis.net/se", "OverlapBehavior");
    private final static QName _SemanticTypeIdentifier_QNAME = new QName("http://www.opengis.net/se", "SemanticTypeIdentifier");
    private final static QName _Pattern_QNAME = new QName("http://www.opengis.net/se", "Pattern");
    private final static QName _IsAligned_QNAME = new QName("http://www.opengis.net/se", "IsAligned");
    private final static QName _MaxScaleDenominator_QNAME = new QName("http://www.opengis.net/se", "MaxScaleDenominator");
    private final static QName _GeneralizeLine_QNAME = new QName("http://www.opengis.net/se", "GeneralizeLine");
    private final static QName _Data_QNAME = new QName("http://www.opengis.net/se", "Data");
    private final static QName _ReliefFactor_QNAME = new QName("http://www.opengis.net/se", "ReliefFactor");
    private final static QName _OnlineResource_QNAME = new QName("http://www.opengis.net/se", "OnlineResource");
    private final static QName _BrightnessOnly_QNAME = new QName("http://www.opengis.net/se", "BrightnessOnly");
    private final static QName _MinScaleDenominator_QNAME = new QName("http://www.opengis.net/se", "MinScaleDenominator");
    private final static QName _MarkIndex_QNAME = new QName("http://www.opengis.net/se", "MarkIndex");
    private final static QName _IsRepeated_QNAME = new QName("http://www.opengis.net/se", "IsRepeated");
    private final static QName _FeatureTypeName_QNAME = new QName("http://www.opengis.net/se", "FeatureTypeName");
    private final static QName _CoverageName_QNAME = new QName("http://www.opengis.net/se", "CoverageName");
    private final static QName _Name_QNAME = new QName("http://www.opengis.net/se", "Name");
    private final static QName _Description_QNAME = new QName("http://www.opengis.net/se", "Description");
    private final static QName _Rule_QNAME = new QName("http://www.opengis.net/se", "Rule");
    private final static QName _CoverageStyle_QNAME = new QName("http://www.opengis.net/se", "CoverageStyle");
    
    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: net.opengis.se
     * 
     */
    public ObjectFactory() {
    }
    
    /**
     * Create an instance of {@link CoverageStyleType }
     * 
     */
    public CoverageStyleType createCoverageStyleType() {
        return new CoverageStyleType();
    }
    
    /**
     * Create an instance of {@link RuleType }
     * 
     */
    public RuleType createRuleType() {
        return new RuleType();
    }
    
    /**
     * Create an instance of {@link OnlineResourceType }
     * 
     */
    public OnlineResourceType createOnlineResourceType() {
        return new OnlineResourceType();
    }

    /**
     * Create an instance of {@link DescriptionType }
     * 
     */
    public DescriptionType createDescriptionType() {
        return new DescriptionType();
    }
    
     /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RuleType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/se", name = "Rule")
    public JAXBElement<RuleType> createRule(RuleType value) {
        return new JAXBElement<RuleType>(_Rule_QNAME, RuleType.class, null, value);
    }
    
     /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CoverageStyleType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/se", name = "CoverageStyle")
    public JAXBElement<CoverageStyleType> createCoverageStyle(CoverageStyleType value) {
        return new JAXBElement<CoverageStyleType>(_CoverageStyle_QNAME, CoverageStyleType.class, null, value);
    }
    
     /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DescriptionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/se", name = "Description")
    public JAXBElement<DescriptionType> createDescription(DescriptionType value) {
        return new JAXBElement<DescriptionType>(_Description_QNAME, DescriptionType.class, null, value);
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/se", name = "WellKnownName")
    public JAXBElement<String> createWellKnownName(String value) {
        return new JAXBElement<String>(_WellKnownName_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/se", name = "Format")
    public JAXBElement<String> createFormat(String value) {
        return new JAXBElement<String>(_Format_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/se", name = "SourceChannelName")
    public JAXBElement<String> createSourceChannelName(String value) {
        return new JAXBElement<String>(_SourceChannelName_QNAME, String.class, null, value);
    }

    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Double }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/se", name = "GammaValue")
    public JAXBElement<Double> createGammaValue(Double value) {
        return new JAXBElement<Double>(_GammaValue_QNAME, Double.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/se", name = "NegativePattern")
    public JAXBElement<String> createNegativePattern(String value) {
        return new JAXBElement<String>(_NegativePattern_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/se", name = "OverlapBehavior")
    public JAXBElement<String> createOverlapBehavior(String value) {
        return new JAXBElement<String>(_OverlapBehavior_QNAME, String.class, null, value);
    }

     /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/se", name = "SemanticTypeIdentifier")
    public JAXBElement<String> createSemanticTypeIdentifier(String value) {
        return new JAXBElement<String>(_SemanticTypeIdentifier_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/se", name = "Pattern")
    public JAXBElement<String> createPattern(String value) {
        return new JAXBElement<String>(_Pattern_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/se", name = "IsAligned")
    public JAXBElement<Boolean> createIsAligned(Boolean value) {
        return new JAXBElement<Boolean>(_IsAligned_QNAME, Boolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Double }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/se", name = "MaxScaleDenominator")
    public JAXBElement<Double> createMaxScaleDenominator(Double value) {
        return new JAXBElement<Double>(_MaxScaleDenominator_QNAME, Double.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/se", name = "GeneralizeLine")
    public JAXBElement<Boolean> createGeneralizeLine(Boolean value) {
        return new JAXBElement<Boolean>(_GeneralizeLine_QNAME, Boolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Double }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/se", name = "Data")
    public JAXBElement<Double> createData(Double value) {
        return new JAXBElement<Double>(_Data_QNAME, Double.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Double }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/se", name = "ReliefFactor")
    public JAXBElement<Double> createReliefFactor(Double value) {
        return new JAXBElement<Double>(_ReliefFactor_QNAME, Double.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link OnlineResourceType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/se", name = "OnlineResource")
    public JAXBElement<OnlineResourceType> createOnlineResource(OnlineResourceType value) {
        return new JAXBElement<OnlineResourceType>(_OnlineResource_QNAME, OnlineResourceType.class, null, value);
    }


    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/se", name = "BrightnessOnly")
    public JAXBElement<Boolean> createBrightnessOnly(Boolean value) {
        return new JAXBElement<Boolean>(_BrightnessOnly_QNAME, Boolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Double }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/se", name = "MinScaleDenominator")
    public JAXBElement<Double> createMinScaleDenominator(Double value) {
        return new JAXBElement<Double>(_MinScaleDenominator_QNAME, Double.class, null, value);
    }


    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/se", name = "MarkIndex")
    public JAXBElement<BigInteger> createMarkIndex(BigInteger value) {
        return new JAXBElement<BigInteger>(_MarkIndex_QNAME, BigInteger.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/se", name = "IsRepeated")
    public JAXBElement<Boolean> createIsRepeated(Boolean value) {
        return new JAXBElement<Boolean>(_IsRepeated_QNAME, Boolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link QName }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/se", name = "FeatureTypeName")
    public JAXBElement<QName> createFeatureTypeName(QName value) {
        return new JAXBElement<QName>(_FeatureTypeName_QNAME, QName.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/se", name = "CoverageName")
    public JAXBElement<String> createCoverageName(String value) {
        return new JAXBElement<String>(_CoverageName_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/se", name = "Name")
    public JAXBElement<String> createName(String value) {
        return new JAXBElement<String>(_Name_QNAME, String.class, null, value);
    }


}
