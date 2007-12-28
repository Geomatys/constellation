/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2007, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */


package net.seagis.wcs;

import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import net.seagis.ows.ReferenceGroupType;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the net.opengis.wcs._1_1 package. 
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

    private final static QName _CoverageSummaryTypeSupportedCRS_QNAME = new QName("http://www.opengis.net/wcs/1.1.1", "SupportedCRS");
    private final static QName _CoverageSummaryTypeSupportedFormat_QNAME = new QName("http://www.opengis.net/wcs/1.1.1", "SupportedFormat");
    private final static QName _GridCRS_QNAME = new QName("http://www.opengis.net/wcs/1.1.1", "GridCRS");
    private final static QName _GridBaseCRS_QNAME = new QName("http://www.opengis.net/wcs/1.1.1", "GridBaseCRS");
    private final static QName _TemporalSubset_QNAME = new QName("http://www.opengis.net/wcs/1.1.1", "TemporalSubset");
    private final static QName _GridCS_QNAME = new QName("http://www.opengis.net/wcs/1.1.1", "GridCS");
    private final static QName _Coverages_QNAME = new QName("http://www.opengis.net/wcs/1.1.1", "Coverages");
    private final static QName _GridOffsets_QNAME = new QName("http://www.opengis.net/wcs/1.1.1", "GridOffsets");
    private final static QName _GridType_QNAME = new QName("http://www.opengis.net/wcs/1.1.1", "GridType");
    private final static QName _CoverageSummary_QNAME = new QName("http://www.opengis.net/wcs/1.1.1", "CoverageSummary");
    private final static QName _TemporalDomain_QNAME = new QName("http://www.opengis.net/wcs/1.1.1", "TemporalDomain");
    private final static QName _Coverage_QNAME = new QName("http://www.opengis.net/wcs/1.1.1", "Coverage");
    private final static QName _Identifier_QNAME = new QName("http://www.opengis.net/wcs/1.1.1", "Identifier");
    private final static QName _GridOrigin_QNAME = new QName("http://www.opengis.net/wcs/1.1.1", "GridOrigin");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: net.opengis.wcs._1_1
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link TimeSequenceType }
     * 
     */
    public TimeSequenceType createTimeSequenceType() {
        return new TimeSequenceType();
    }

   /**
     * Create an instance of {@link GridCrsType }
     * 
     */
    public GridCrsType createGridCrsType() {
        return new GridCrsType();
    }

    /**
     * Create an instance of {@link CoverageDomainType }
     * 
     */
    public CoverageDomainType createCoverageDomainType() {
        return new CoverageDomainType();
    }

    /**
     * Create an instance of {@link InterpolationMethodType }
     * 
     */
    public InterpolationMethodType createInterpolationMethodType() {
        return new InterpolationMethodType();
    }

    /**
     * Create an instance of {@link TimePeriodType }
     * 
     */
    public TimePeriodType createTimePeriodType() {
        return new TimePeriodType();
    }

    /**
     * Create an instance of {@link CoverageDescriptionType }
     * 
     */
    public CoverageDescriptionType createCoverageDescriptionType() {
        return new CoverageDescriptionType();
    }

    /**
     * Create an instance of {@link CoverageSummaryType }
     * 
     */
    public CoverageSummaryType createCoverageSummaryType() {
        return new CoverageSummaryType();
    }

    /**
     * Create an instance of {@link ImageCRSRefType }
     * 
     */
    public ImageCRSRefType createImageCRSRefType() {
        return new ImageCRSRefType();
    }

    /**
     * Create an instance of {@link InterpolationMethodBaseType }
     * 
     */
    public InterpolationMethodBaseType createInterpolationMethodBaseType() {
        return new InterpolationMethodBaseType();
    }

    /**
     * Create an instance of {@link Contents }
     * 
     */
    public Contents createContents() {
        return new Contents();
    }

    /**
     * Create an instance of {@link AvailableKeys }
     * 
     */
    public AvailableKeys createAvailableKeys() {
        return new AvailableKeys();
    }

    /**
     * Create an instance of {@link AxisType }
     * 
     */
    public AxisType createAxisType() {
        return new AxisType();
    }

    /**
     * Create an instance of {@link RangeType }
     * 
     */
    public RangeType createRangeType() {
        return new RangeType();
    }

    /**
     * Create an instance of {@link SpatialDomainType }
     * 
     */
    public SpatialDomainType createSpatialDomainType() {
        return new SpatialDomainType();
    }

    /**
     * Create an instance of {@link CoverageDescriptions }
     * 
     */
    public CoverageDescriptions createCoverageDescriptions() {
        return new CoverageDescriptions();
    }

    /**
     * Create an instance of {@link Capabilities }
     * 
     */
    public Capabilities createCapabilities() {
        return new Capabilities();
    }

    /**
     * Create an instance of {@link FieldType }
     * 
     */
    public FieldType createFieldType() {
        return new FieldType();
    }

    /**
     * Create an instance of {@link InterpolationMethods }
     * 
     */
    public InterpolationMethods createInterpolationMethods() {
        return new InterpolationMethods();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/wcs/1.1.1", name = "SupportedCRS", scope = CoverageSummaryType.class)
    public JAXBElement<String> createCoverageSummaryTypeSupportedCRS(String value) {
        return new JAXBElement<String>(_CoverageSummaryTypeSupportedCRS_QNAME, String.class, CoverageSummaryType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/wcs/1.1.1", name = "SupportedFormat", scope = CoverageSummaryType.class)
    public JAXBElement<String> createCoverageSummaryTypeSupportedFormat(String value) {
        return new JAXBElement<String>(_CoverageSummaryTypeSupportedFormat_QNAME, String.class, CoverageSummaryType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GridCrsType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/wcs/1.1.1", name = "GridCRS")
    public JAXBElement<GridCrsType> createGridCRS(GridCrsType value) {
        return new JAXBElement<GridCrsType>(_GridCRS_QNAME, GridCrsType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/wcs/1.1.1", name = "GridBaseCRS")
    public JAXBElement<String> createGridBaseCRS(String value) {
        return new JAXBElement<String>(_GridBaseCRS_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TimeSequenceType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/wcs/1.1.1", name = "TemporalSubset")
    public JAXBElement<TimeSequenceType> createTemporalSubset(TimeSequenceType value) {
        return new JAXBElement<TimeSequenceType>(_TemporalSubset_QNAME, TimeSequenceType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/wcs/1.1.1", name = "GridCS", defaultValue = "urn:ogc:def:cs:OGC:0.0:Grid2dSquareCS")
    public JAXBElement<String> createGridCS(String value) {
        return new JAXBElement<String>(_GridCS_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link List }{@code <}{@link Double }{@code >}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/wcs/1.1.1", name = "GridOffsets")
    public JAXBElement<List<Double>> createGridOffsets(List<Double> value) {
        return new JAXBElement<List<Double>>(_GridOffsets_QNAME, ((Class) List.class), null, ((List<Double> ) value));
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/wcs/1.1.1", name = "GridType", defaultValue = "urn:ogc:def:method:WCS:1.1:2dSimpleGrid")
    public JAXBElement<String> createGridType(String value) {
        return new JAXBElement<String>(_GridType_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CoverageSummaryType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/wcs/1.1.1", name = "CoverageSummary")
    public JAXBElement<CoverageSummaryType> createCoverageSummary(CoverageSummaryType value) {
        return new JAXBElement<CoverageSummaryType>(_CoverageSummary_QNAME, CoverageSummaryType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TimeSequenceType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/wcs/1.1.1", name = "TemporalDomain")
    public JAXBElement<TimeSequenceType> createTemporalDomain(TimeSequenceType value) {
        return new JAXBElement<TimeSequenceType>(_TemporalDomain_QNAME, TimeSequenceType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReferenceGroupType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/wcs/1.1.1", name = "Coverage", substitutionHeadNamespace = "http://www.opengis.net/ows/1.1", substitutionHeadName = "ReferenceGroup")
    public JAXBElement<ReferenceGroupType> createCoverage(ReferenceGroupType value) {
        return new JAXBElement<ReferenceGroupType>(_Coverage_QNAME, ReferenceGroupType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/wcs/1.1.1", name = "Identifier")
    public JAXBElement<String> createIdentifier(String value) {
        return new JAXBElement<String>(_Identifier_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link List }{@code <}{@link Double }{@code >}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/wcs/1.1.1", name = "GridOrigin", defaultValue = "0 0")
    public JAXBElement<List<Double>> createGridOrigin(List<Double> value) {
        return new JAXBElement<List<Double>>(_GridOrigin_QNAME, ((Class) List.class), null, ((List<Double> ) value));
    }

}
