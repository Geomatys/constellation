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

package net.seagis.gml;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

/**
 *
 * @version $Id:
 * @author Guilhem Legal
 */
@XmlRegistry
public class ObjectFactory {
    
    private final static QName _MethodFormula_QNAME = new QName("http://www.opengis.net/gml/3.2", "methodFormula");
    private final static QName _Status_QNAME = new QName("http://www.opengis.net/gml/3.2", "status");
    private final static QName _MappingRule_QNAME = new QName("http://www.opengis.net/gml/3.2", "MappingRule");
    private final static QName _Formula_QNAME = new QName("http://www.opengis.net/gml/3.2", "formula");
    private final static QName _LocationKeyWord_QNAME = new QName("http://www.opengis.net/gml/3.2", "LocationKeyWord");
    private final static QName _Name_QNAME = new QName("http://www.opengis.net/gml/3.2", "name");
    private final static QName _LocationName_QNAME = new QName("http://www.opengis.net/gml/3.2", "locationName");
    private final static QName _DataSource_QNAME = new QName("http://www.opengis.net/gml/3.2", "dataSource");
    private final static QName _AxisAbbrev_QNAME = new QName("http://www.opengis.net/gml/3.2", "axisAbbrev");
    private final static QName _AnchorPoint_QNAME = new QName("http://www.opengis.net/gml/3.2", "anchorPoint");
    private final static QName _QuantityType_QNAME = new QName("http://www.opengis.net/gml/3.2", "quantityType");
    private final static QName _LocationString_QNAME = new QName("http://www.opengis.net/gml/3.2", "LocationString");
    private final static QName _Description_QNAME = new QName("http://www.opengis.net/gml/3.2", "description");
    private final static QName _CatalogSymbol_QNAME = new QName("http://www.opengis.net/gml/3.2", "catalogSymbol");
    private final static QName _AnchorDefinition_QNAME = new QName("http://www.opengis.net/gml/3.2", "anchorDefinition");
    private final static QName _AbstractObject_QNAME = new QName("http://www.opengis.net/gml/3.2", "AbstractObject");
    private final static QName _Coordinates_QNAME = new QName("http://www.opengis.net/gml/3.2", "coordinates");
    private final static QName _TupleList_QNAME = new QName("http://www.opengis.net/gml/3.2", "tupleList");
    private final static QName _Pos_QNAME = new QName("http://www.opengis.net/gml/3.2", "pos");
    private final static QName _Point_QNAME = new QName("http://www.opengis.net/gml/3.2", "Point");
    private final static QName _AbstractFeature_QNAME = new QName("http://www.opengis.net/gml/3.2", "AbstractFeature");
    private final static QName _AbstractGML_QNAME = new QName("http://www.opengis.net/gml/3.2", "AbstractGML");
    private final static QName _BoundedBy_QNAME = new QName("http://www.opengis.net/gml/3.2", "boundedBy");
    private final static QName _Envelope_QNAME = new QName("http://www.opengis.net/gml/3.2", "Envelope");
    private final static QName _AbstractGeometricPrimitive_QNAME = new QName("http://www.opengis.net/gml/3.2", "AbstractGeometricPrimitive");
    private final static QName _AbstractGeometry_QNAME = new QName("http://www.opengis.net/gml/3.2", "AbstractGeometry");
    private final static QName _Location_QNAME = new QName("http://www.opengis.net/gml/3.2", "location");
    private final static QName _AbstractImplicitGeometry_QNAME = new QName("http://www.opengis.net/gml/3.2", "AbstractImplicitGeometry");
    private final static QName _AbstractValue_QNAME = new QName("http://www.opengis.net/gml/3.2", "AbstractValue");
    private final static QName _BaseUnit_QNAME = new QName("http://www.opengis.net/gml/3.2", "BaseUnit");
    private final static QName _UnitDefinition_QNAME = new QName("http://www.opengis.net/gml/3.2", "UnitDefinition");
    private final static QName _UnitOfMeasure_QNAME = new QName("http://www.opengis.net/gml/3.2", "unitOfMeasure");
    private final static QName _Definition_QNAME = new QName("http://www.opengis.net/gml/3.2", "Definition");
    private final static QName _AbstractScalarValueList_QNAME = new QName("http://www.opengis.net/gml/3.2", "AbstractScalarValueList");
    private final static QName _AbstractScalarValue_QNAME = new QName("http://www.opengis.net/gml/3.2", "AbstractScalarValue");
    private final static QName _CountExtent_QNAME = new QName("http://www.opengis.net/gml/3.2", "CountExtent");
    private final static QName _EnvelopeWithTimePeriod_QNAME = new QName("http://www.opengis.net/gml/3.2", "EnvelopeWithTimePeriod");
    private final static QName _TimePosition_QNAME = new QName("http://www.opengis.net/gml/3.2", "timePosition");
     private final static QName _QuantityTypeReference_QNAME = new QName("http://www.opengis.net/gml/3.2", "quantityTypeReference");
    private final static QName _ModifiedCoordinate_QNAME = new QName("http://www.opengis.net/gml/3.2", "modifiedCoordinate");
    private final static QName _AbstractReference_QNAME = new QName("http://www.opengis.net/gml/3.2", "abstractReference");
    private final static QName _TargetElement_QNAME = new QName("http://www.opengis.net/gml/3.2", "targetElement");
    private final static QName _DataSourceReference_QNAME = new QName("http://www.opengis.net/gml/3.2", "dataSourceReference");
    private final static QName _Origin_QNAME = new QName("http://www.opengis.net/gml/3.2", "origin");
    private final static QName _Minutes_QNAME = new QName("http://www.opengis.net/gml/3.2", "minutes");
    private final static QName _IntegerValue_QNAME = new QName("http://www.opengis.net/gml/3.2", "integerValue");
    private final static QName _OperationVersion_QNAME = new QName("http://www.opengis.net/gml/3.2", "operationVersion");
    private final static QName _LocationReference_QNAME = new QName("http://www.opengis.net/gml/3.2", "locationReference");
    private final static QName _DescriptionReference_QNAME = new QName("http://www.opengis.net/gml/3.2", "descriptionReference");
    private final static QName _StatusReference_QNAME = new QName("http://www.opengis.net/gml/3.2", "statusReference");
    private final static QName _SourceDimensions_QNAME = new QName("http://www.opengis.net/gml/3.2", "sourceDimensions");
    private final static QName _DefinitionRef_QNAME = new QName("http://www.opengis.net/gml/3.2", "definitionRef");
    private final static QName _AssociationName_QNAME = new QName("http://www.opengis.net/gml/3.2", "associationName");
    private final static QName _AbstractTimePrimitive_QNAME = new QName("http://www.opengis.net/gml/3.2", "AbstractTimePrimitive");
    private final static QName _TimeInterval_QNAME = new QName("http://www.opengis.net/gml/3.2", "timeInterval");
    private final static QName _DoubleOrNilReasonTupleList_QNAME = new QName("http://www.opengis.net/gml/3.2", "doubleOrNilReasonTupleList");
    private final static QName _DefaultCodeSpace_QNAME = new QName("http://www.opengis.net/gml/3.2", "defaultCodeSpace");
    private final static QName _Null_QNAME = new QName("http://www.opengis.net/gml/3.2", "Null");
    private final static QName _TimePeriod_QNAME = new QName("http://www.opengis.net/gml/3.2", "TimePeriod");
    private final static QName _ReversePropertyName_QNAME = new QName("http://www.opengis.net/gml/3.2", "reversePropertyName");
    private final static QName _Seconds_QNAME = new QName("http://www.opengis.net/gml/3.2", "seconds");
    private final static QName _MinimumOccurs_QNAME = new QName("http://www.opengis.net/gml/3.2", "minimumOccurs");
    private final static QName _DecimalMinutes_QNAME = new QName("http://www.opengis.net/gml/3.2", "decimalMinutes");
    private final static QName _TargetDimensions_QNAME = new QName("http://www.opengis.net/gml/3.2", "targetDimensions");
    private final static QName _AbstractTimeObject_QNAME = new QName("http://www.opengis.net/gml/3.2", "AbstractTimeObject");
    private final static QName _BooleanValue_QNAME = new QName("http://www.opengis.net/gml/3.2", "booleanValue");
    private final static QName _MinimumValue_QNAME = new QName("http://www.opengis.net/gml/3.2", "minimumValue");
    private final static QName _Scope_QNAME = new QName("http://www.opengis.net/gml/3.2", "scope");
    private final static QName _Duration_QNAME = new QName("http://www.opengis.net/gml/3.2", "duration");
    private final static QName _GmlProfileSchema_QNAME = new QName("http://www.opengis.net/gml/3.2", "gmlProfileSchema");
    private final static QName _TimeInstant_QNAME = new QName("http://www.opengis.net/gml/3.2", "TimeInstant");
    private final static QName _AbstractTimeGeometricPrimitive_QNAME = new QName("http://www.opengis.net/gml/3.2", "AbstractTimeGeometricPrimitive");
    private final static QName _IntegerValueList_QNAME = new QName("http://www.opengis.net/gml/3.2", "integerValueList");
    private final static QName _StringValue_QNAME = new QName("http://www.opengis.net/gml/3.2", "stringValue");
    private final static QName _ValidTime_QNAME = new QName("http://www.opengis.net/gml/3.2", "validTime");
    private final static QName _Remarks_QNAME = new QName("http://www.opengis.net/gml/3.2", "remarks");
    private final static QName _RealizationEpoch_QNAME = new QName("http://www.opengis.net/gml/3.2", "realizationEpoch");
    private final static QName _CountList_QNAME = new QName("http://www.opengis.net/gml/3.2", "CountList");
    private final static QName _MaximumOccurs_QNAME = new QName("http://www.opengis.net/gml/3.2", "maximumOccurs");
    private final static QName _ValueFile_QNAME = new QName("http://www.opengis.net/gml/3.2", "valueFile");
    private final static QName _MaximumValue_QNAME = new QName("http://www.opengis.net/gml/3.2", "maximumValue");
    private final static QName _BooleanList_QNAME = new QName("http://www.opengis.net/gml/3.2", "BooleanList");
    
    
    /**
     *
     */
    public ObjectFactory() {
    }
    
    /**
     * Create an instance of {@link EnvelopeWithTimePeriodType }
     * 
     */
    public EnvelopeWithTimePeriodType createEnvelopeWithTimePeriodType() {
        return new EnvelopeWithTimePeriodType();
    }

    /**
     * Create an instance of {@link TimePositionType }
     * 
     */
    public TimePositionType createTimePositionType() {
        return new TimePositionType();
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TimePositionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "timePosition")
    public JAXBElement<TimePositionType> createTimePosition(TimePositionType value) {
        return new JAXBElement<TimePositionType>(_TimePosition_QNAME, TimePositionType.class, null, value);
    }

    
    /**
     * Create an instance of {@link BaseUnitType }
     * 
     */
    public BaseUnitType createBaseUnitType() {
        return new BaseUnitType();
    }

    /**
     * Create an instance of {@link UnitOfMeasureType }
     * 
     */
    public UnitOfMeasureType createUnitOfMeasureType() {
        return new UnitOfMeasureType();
    }

   /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UnitOfMeasureType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "unitOfMeasure")
    public JAXBElement<UnitOfMeasureType> createUnitOfMeasure(UnitOfMeasureType value) {
        return new JAXBElement<UnitOfMeasureType>(_UnitOfMeasure_QNAME, UnitOfMeasureType.class, null, value);
    }

    /**
     * Create an instance of {@link CodeType }
     * 
     */
    public CodeType createCodeType() {
        return new CodeType();
    }

    /**
     * Create an instance of {@link StringOrRefType }
     * 
     */
    public StringOrRefType createStringOrRefType() {
        return new StringOrRefType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "methodFormula", substitutionHeadNamespace = "http://www.opengis.net/gml/3.2", substitutionHeadName = "formula")
    public JAXBElement<CodeType> createMethodFormula(CodeType value) {
        return new JAXBElement<CodeType>(_MethodFormula_QNAME, CodeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Object }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "AbstractScalarValueList", substitutionHeadNamespace = "http://www.opengis.net/gml/3.2", substitutionHeadName = "AbstractValue")
    public JAXBElement<Object> createAbstractScalarValueList(Object value) {
        return new JAXBElement<Object>(_AbstractScalarValueList_QNAME, Object.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Object }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "AbstractScalarValue", substitutionHeadNamespace = "http://www.opengis.net/gml/3.2", substitutionHeadName = "AbstractValue")
    public JAXBElement<Object> createAbstractScalarValue(Object value) {
        return new JAXBElement<Object>(_AbstractScalarValue_QNAME, Object.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link List }{@code <}{@link String }{@code >}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "CountExtent", substitutionHeadNamespace = "http://www.opengis.net/gml/3.2", substitutionHeadName = "AbstractValue")
    public JAXBElement<List<String>> createCountExtent(List<String> value) {
        return new JAXBElement<List<String>>(_CountExtent_QNAME, ((Class) List.class), null, ((List<String> ) value));
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnvelopeWithTimePeriodType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "EnvelopeWithTimePeriod", substitutionHeadNamespace = "http://www.opengis.net/gml/3.2", substitutionHeadName = "Envelope")
    public JAXBElement<EnvelopeWithTimePeriodType> createEnvelopeWithTimePeriod(EnvelopeWithTimePeriodType value) {
        return new JAXBElement<EnvelopeWithTimePeriodType>(_EnvelopeWithTimePeriod_QNAME, EnvelopeWithTimePeriodType.class, null, value);
    }
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringOrRefType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "status")
    public JAXBElement<StringOrRefType> createStatus(StringOrRefType value) {
        return new JAXBElement<StringOrRefType>(_Status_QNAME, StringOrRefType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringOrRefType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "MappingRule")
    public JAXBElement<StringOrRefType> createMappingRule(StringOrRefType value) {
        return new JAXBElement<StringOrRefType>(_MappingRule_QNAME, StringOrRefType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "formula")
    public JAXBElement<CodeType> createFormula(CodeType value) {
        return new JAXBElement<CodeType>(_Formula_QNAME, CodeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringOrRefType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "dataSource")
    public JAXBElement<StringOrRefType> createDataSource(StringOrRefType value) {
        return new JAXBElement<StringOrRefType>(_DataSource_QNAME, StringOrRefType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "locationName")
    public JAXBElement<CodeType> createLocationName(CodeType value) {
        return new JAXBElement<CodeType>(_LocationName_QNAME, CodeType.class, null, value);
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "axisAbbrev")
    public JAXBElement<CodeType> createAxisAbbrev(CodeType value) {
        return new JAXBElement<CodeType>(_AxisAbbrev_QNAME, CodeType.class, null, value);
    }

     /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "anchorPoint", substitutionHeadNamespace = "http://www.opengis.net/gml/3.2", substitutionHeadName = "anchorDefinition")
    public JAXBElement<CodeType> createAnchorPoint(CodeType value) {
        return new JAXBElement<CodeType>(_AnchorPoint_QNAME, CodeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringOrRefType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "quantityType")
    public JAXBElement<StringOrRefType> createQuantityType(StringOrRefType value) {
        return new JAXBElement<StringOrRefType>(_QuantityType_QNAME, StringOrRefType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringOrRefType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "LocationString")
    public JAXBElement<StringOrRefType> createLocationString(StringOrRefType value) {
        return new JAXBElement<StringOrRefType>(_LocationString_QNAME, StringOrRefType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringOrRefType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "description")
    public JAXBElement<StringOrRefType> createDescription(StringOrRefType value) {
        return new JAXBElement<StringOrRefType>(_Description_QNAME, StringOrRefType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "name")
    public JAXBElement<CodeType> createName(CodeType value) {
        return new JAXBElement<CodeType>(_Name_QNAME, CodeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "catalogSymbol")
    public JAXBElement<CodeType> createCatalogSymbol(CodeType value) {
        return new JAXBElement<CodeType>(_CatalogSymbol_QNAME, CodeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "anchorDefinition")
    public JAXBElement<CodeType> createAnchorDefinition(CodeType value) {
        return new JAXBElement<CodeType>(_AnchorDefinition_QNAME, CodeType.class, null, value);
    }

    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "LocationKeyWord")
    public JAXBElement<CodeType> createLocationKeyWord(CodeType value) {
        return new JAXBElement<CodeType>(_LocationKeyWord_QNAME, CodeType.class, null, value);
    }

    
            
    /**
     * Create an instance of {@link ObservationEntry }
     * 
     */
    public ReferenceEntry createReferenceEntry() {
        return new ReferenceEntry();
    }
    
    /**
     * Create an instance of {@link LocationPropertyType }
     * 
     */
    public LocationPropertyType createLocationPropertyType() {
        return new LocationPropertyType();
    }
    
    
    /**
     * Create an instance of {@link CoordinatesType }
     * 
     */
    public CoordinatesType createCoordinatesType() {
        return new CoordinatesType();
    }
    
    /**
     * Create an instance of {@link DefinitionType }
     * 
     */
    public DefinitionType createDefinitionType() {
        return new DefinitionType();
    }


    /**
     * Create an instance of {@link DefinitionBaseType }
     * 
     */
    public DefinitionBaseType createDefinitionBaseType() {
        return new DefinitionBaseType();
    }

    
    /**
     * Create an instance of {@link UnitDefinitionType }
     * 
     */
    public UnitDefinitionType createUnitDefinitionType() {
        return new UnitDefinitionType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BaseUnitType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "BaseUnit", substitutionHeadNamespace = "http://www.opengis.net/gml/3.2", substitutionHeadName = "UnitDefinition")
    public JAXBElement<BaseUnitType> createBaseUnit(BaseUnitType value) {
        return new JAXBElement<BaseUnitType>(_BaseUnit_QNAME, BaseUnitType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DefinitionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "Definition", substitutionHeadNamespace = "http://www.opengis.net/gml/3.2", substitutionHeadName = "AbstractGML")
    public JAXBElement<DefinitionType> createDefinition(DefinitionType value) {
        return new JAXBElement<DefinitionType>(_Definition_QNAME, DefinitionType.class, null, value);
    }

    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UnitDefinitionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "UnitDefinition", substitutionHeadNamespace = "http://www.opengis.net/gml/3.2", substitutionHeadName = "Definition")
    public JAXBElement<UnitDefinitionType> createUnitDefinition(UnitDefinitionType value) {
        return new JAXBElement<UnitDefinitionType>(_UnitDefinition_QNAME, UnitDefinitionType.class, null, value);
    }

   /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CoordinatesType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "tupleList")
    public JAXBElement<CoordinatesType> createTupleList(CoordinatesType value) {
        return new JAXBElement<CoordinatesType>(_TupleList_QNAME, CoordinatesType.class, null, value);
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CoordinatesType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "coordinates")
    public JAXBElement<CoordinatesType> createCoordinates(CoordinatesType value) {
        return new JAXBElement<CoordinatesType>(_Coordinates_QNAME, CoordinatesType.class, null, value);
    }

    /**
     * Create an instance of {@link DirectPositionType }
     * 
     */
    public DirectPositionType createDirectPositionType() {
        return new DirectPositionType();
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractGeometricPrimitiveType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "AbstractGeometricPrimitive", substitutionHeadNamespace = "http://www.opengis.net/gml/3.2", substitutionHeadName = "AbstractGeometry")
    public JAXBElement<AbstractGeometricPrimitiveType> createAbstractGeometricPrimitive(AbstractGeometricPrimitiveType value) {
        return new JAXBElement<AbstractGeometricPrimitiveType>(_AbstractGeometricPrimitive_QNAME, AbstractGeometricPrimitiveType.class, null, value);
    }

     /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Object }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "AbstractValue", substitutionHeadNamespace = "http://www.opengis.net/gml/3.2", substitutionHeadName = "AbstractObject")
    public JAXBElement<Object> createAbstractValue(Object value) {
        return new JAXBElement<Object>(_AbstractValue_QNAME, Object.class, null, value);
    }

    
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DirectPositionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "pos")
    public JAXBElement<DirectPositionType> createPos(DirectPositionType value) {
        return new JAXBElement<DirectPositionType>(_Pos_QNAME, DirectPositionType.class, null, value);
    } 
    
     /**
     * Create an instance of {@link PointType }
     * 
     */
    public PointType createPointType() {
        return new PointType();
    }
    
    /**
     * 
     * Create an instance of {@link JAXBElement }{@code <}{@link PointType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "Point", substitutionHeadNamespace = "http://www.opengis.net/gml/3.2", substitutionHeadName = "AbstractGeometricPrimitive")
    public JAXBElement<PointType> createPoint(PointType value) {
        return new JAXBElement<PointType>(_Point_QNAME, PointType.class, null, value);
    }
     
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Object }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "AbstractObject")
    public JAXBElement<Object> createAbstractObject(Object value) {
        return new JAXBElement<Object>(_AbstractObject_QNAME, Object.class, null, value);
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractFeatureType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "AbstractFeature", substitutionHeadNamespace = "http://www.opengis.net/gml/3.2", substitutionHeadName = "AbstractGML")
    public JAXBElement<AbstractFeatureEntry> createAbstractFeature(AbstractFeatureEntry value) {
        return new JAXBElement<AbstractFeatureEntry>(_AbstractFeature_QNAME, AbstractFeatureEntry.class, null, value);
    }
    
     /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractGMLType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "AbstractGML", substitutionHeadNamespace = "http://www.opengis.net/gml/3.2", substitutionHeadName = "AbstractObject")
    public JAXBElement<AbstractGMLEntry> createAbstractGML(AbstractGMLEntry value) {
        return new JAXBElement<AbstractGMLEntry>(_AbstractGML_QNAME, AbstractGMLEntry.class, null, value);
    }
    
    /**
     * Create an instance of {@link BoundingShapeType }
     * 
     */
    public BoundingShapeEntry createBoundingShapeType() {
        return new BoundingShapeEntry();
    }
    
     /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BoundingShapeEntry }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "boundedBy")
    public JAXBElement<BoundingShapeEntry> createBoundedBy(BoundingShapeEntry value) {
        return new JAXBElement<BoundingShapeEntry>(_BoundedBy_QNAME, BoundingShapeEntry.class, null, value);
    }
    
    /**
     * Create an instance of {@link EnvelopeType }
     * 
     */
    public EnvelopeEntry createEnvelopeType() {
        return new EnvelopeEntry();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractGeometryType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "AbstractGeometry", substitutionHeadNamespace = "http://www.opengis.net/gml/3.2", substitutionHeadName = "AbstractGML")
    public JAXBElement<AbstractGeometryType> createAbstractGeometry(AbstractGeometryType value) {
        return new JAXBElement<AbstractGeometryType>(_AbstractGeometry_QNAME, AbstractGeometryType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractGeometryType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "AbstractImplicitGeometry", substitutionHeadNamespace = "http://www.opengis.net/gml/3.2", substitutionHeadName = "AbstractGeometry")
    public JAXBElement<AbstractGeometryType> createAbstractImplicitGeometry(AbstractGeometryType value) {
        return new JAXBElement<AbstractGeometryType>(_AbstractImplicitGeometry_QNAME, AbstractGeometryType.class, null, value);
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnvelopeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "Envelope", substitutionHeadNamespace = "http://www.opengis.net/gml/3.2", substitutionHeadName = "AbstractObject")
    public JAXBElement<EnvelopeEntry> createEnvelope(EnvelopeEntry value) {
        return new JAXBElement<EnvelopeEntry>(_Envelope_QNAME, EnvelopeEntry.class, null, value);
    }
  
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LocationPropertyType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "location")
    public JAXBElement<LocationPropertyType> createLocation(LocationPropertyType value) {
        return new JAXBElement<LocationPropertyType>(_Location_QNAME, LocationPropertyType.class, null, value);
    }
/**
     * Create an instance of {@link TimeInstantPropertyType }
     * 
     */
    public TimeInstantPropertyType createTimeInstantPropertyType() {
        return new TimeInstantPropertyType();
    }

   /**
     * Create an instance of {@link TimePrimitivePropertyType }
     * 
     */
    public TimePrimitivePropertyType createTimePrimitivePropertyType() {
        return new TimePrimitivePropertyType();
    }

    /**
     * Create an instance of {@link TimePeriodType }
     * 
     */
    public TimePeriodType createTimePeriodType() {
        return new TimePeriodType();
    }

    /**
     * Create an instance of {@link RelatedTimeType }
     * 
     */
    public RelatedTimeType createRelatedTimeType() {
        return new RelatedTimeType();
    }

    /**
     * Create an instance of {@link TimePeriodPropertyType }
     * 
     */
    public TimePeriodPropertyType createTimePeriodPropertyType() {
        return new TimePeriodPropertyType();
    }

    /**
     * Create an instance of {@link TimeIntervalLengthType }
     * 
     */
    public TimeIntervalLengthType createTimeIntervalLengthType() {
        return new TimeIntervalLengthType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "quantityTypeReference")
    public JAXBElement<ReferenceEntry> createQuantityTypeReference(ReferenceEntry value) {
        return new JAXBElement<ReferenceEntry>(_QuantityTypeReference_QNAME, ReferenceEntry.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "modifiedCoordinate")
    public JAXBElement<BigInteger> createModifiedCoordinate(BigInteger value) {
        return new JAXBElement<BigInteger>(_ModifiedCoordinate_QNAME, BigInteger.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "abstractReference")
    public JAXBElement<ReferenceEntry> createAbstractReference(ReferenceEntry value) {
        return new JAXBElement<ReferenceEntry>(_AbstractReference_QNAME, ReferenceEntry.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "targetElement")
    public JAXBElement<String> createTargetElement(String value) {
        return new JAXBElement<String>(_TargetElement_QNAME, String.class, null, value);
    }


    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "dataSourceReference")
    public JAXBElement<ReferenceEntry> createDataSourceReference(ReferenceEntry value) {
        return new JAXBElement<ReferenceEntry>(_DataSourceReference_QNAME, ReferenceEntry.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "origin")
    public JAXBElement<XMLGregorianCalendar> createOrigin(XMLGregorianCalendar value) {
        return new JAXBElement<XMLGregorianCalendar>(_Origin_QNAME, XMLGregorianCalendar.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "minutes")
    public JAXBElement<Integer> createMinutes(Integer value) {
        return new JAXBElement<Integer>(_Minutes_QNAME, Integer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "integerValue")
    public JAXBElement<BigInteger> createIntegerValue(BigInteger value) {
        return new JAXBElement<BigInteger>(_IntegerValue_QNAME, BigInteger.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "operationVersion")
    public JAXBElement<String> createOperationVersion(String value) {
        return new JAXBElement<String>(_OperationVersion_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "locationReference")
    public JAXBElement<ReferenceEntry> createLocationReference(ReferenceEntry value) {
        return new JAXBElement<ReferenceEntry>(_LocationReference_QNAME, ReferenceEntry.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "descriptionReference")
    public JAXBElement<ReferenceEntry> createDescriptionReference(ReferenceEntry value) {
        return new JAXBElement<ReferenceEntry>(_DescriptionReference_QNAME, ReferenceEntry.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "statusReference")
    public JAXBElement<ReferenceEntry> createStatusReference(ReferenceEntry value) {
        return new JAXBElement<ReferenceEntry>(_StatusReference_QNAME, ReferenceEntry.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "sourceDimensions")
    public JAXBElement<BigInteger> createSourceDimensions(BigInteger value) {
        return new JAXBElement<BigInteger>(_SourceDimensions_QNAME, BigInteger.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReferenceType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "definitionRef")
    public JAXBElement<ReferenceEntry> createDefinitionRef(ReferenceEntry value) {
        return new JAXBElement<ReferenceEntry>(_DefinitionRef_QNAME, ReferenceEntry.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "associationName")
    public JAXBElement<String> createAssociationName(String value) {
        return new JAXBElement<String>(_AssociationName_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractTimePrimitiveType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "AbstractTimePrimitive", substitutionHeadNamespace = "http://www.opengis.net/gml/3.2", substitutionHeadName = "AbstractTimeObject")
    public JAXBElement<AbstractTimePrimitiveType> createAbstractTimePrimitive(AbstractTimePrimitiveType value) {
        return new JAXBElement<AbstractTimePrimitiveType>(_AbstractTimePrimitive_QNAME, AbstractTimePrimitiveType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TimeIntervalLengthType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "timeInterval")
    public JAXBElement<TimeIntervalLengthType> createTimeInterval(TimeIntervalLengthType value) {
        return new JAXBElement<TimeIntervalLengthType>(_TimeInterval_QNAME, TimeIntervalLengthType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link List }{@code <}{@link String }{@code >}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "doubleOrNilReasonTupleList")
    public JAXBElement<List<String>> createDoubleOrNilReasonTupleList(List<String> value) {
        return new JAXBElement<List<String>>(_DoubleOrNilReasonTupleList_QNAME, ((Class) List.class), null, ((List<String> ) value));
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "defaultCodeSpace")
    public JAXBElement<String> createDefaultCodeSpace(String value) {
        return new JAXBElement<String>(_DefaultCodeSpace_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link List }{@code <}{@link String }{@code >}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "Null")
    public JAXBElement<List<String>> createNull(List<String> value) {
        return new JAXBElement<List<String>>(_Null_QNAME, ((Class) List.class), null, ((List<String> ) value));
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TimePeriodType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "TimePeriod", substitutionHeadNamespace = "http://www.opengis.net/gml/3.2", substitutionHeadName = "AbstractTimeGeometricPrimitive")
    public JAXBElement<TimePeriodType> createTimePeriod(TimePeriodType value) {
        return new JAXBElement<TimePeriodType>(_TimePeriod_QNAME, TimePeriodType.class, null, value);
    }

   
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "reversePropertyName")
    public JAXBElement<String> createReversePropertyName(String value) {
        return new JAXBElement<String>(_ReversePropertyName_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigDecimal }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "seconds")
    public JAXBElement<BigDecimal> createSeconds(BigDecimal value) {
        return new JAXBElement<BigDecimal>(_Seconds_QNAME, BigDecimal.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "minimumOccurs")
    public JAXBElement<BigInteger> createMinimumOccurs(BigInteger value) {
        return new JAXBElement<BigInteger>(_MinimumOccurs_QNAME, BigInteger.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigDecimal }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "decimalMinutes")
    public JAXBElement<BigDecimal> createDecimalMinutes(BigDecimal value) {
        return new JAXBElement<BigDecimal>(_DecimalMinutes_QNAME, BigDecimal.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "targetDimensions")
    public JAXBElement<BigInteger> createTargetDimensions(BigInteger value) {
        return new JAXBElement<BigInteger>(_TargetDimensions_QNAME, BigInteger.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractTimeObjectType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "AbstractTimeObject", substitutionHeadNamespace = "http://www.opengis.net/gml/3.2", substitutionHeadName = "AbstractGML")
    public JAXBElement<AbstractTimeObjectType> createAbstractTimeObject(AbstractTimeObjectType value) {
        return new JAXBElement<AbstractTimeObjectType>(_AbstractTimeObject_QNAME, AbstractTimeObjectType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link java.lang.Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "booleanValue")
    public JAXBElement<java.lang.Boolean> createBooleanValue(java.lang.Boolean value) {
        return new JAXBElement<java.lang.Boolean>(_BooleanValue_QNAME, java.lang.Boolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Double }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "minimumValue")
    public JAXBElement<Double> createMinimumValue(Double value) {
        return new JAXBElement<Double>(_MinimumValue_QNAME, Double.class, null, value);
    }

   /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "scope")
    public JAXBElement<String> createScope(String value) {
        return new JAXBElement<String>(_Scope_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Duration }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "duration")
    public JAXBElement<Duration> createDuration(Duration value) {
        return new JAXBElement<Duration>(_Duration_QNAME, Duration.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "gmlProfileSchema")
    public JAXBElement<String> createGmlProfileSchema(String value) {
        return new JAXBElement<String>(_GmlProfileSchema_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TimeInstantType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "TimeInstant", substitutionHeadNamespace = "http://www.opengis.net/gml/3.2", substitutionHeadName = "AbstractTimeGeometricPrimitive")
    public JAXBElement<TimeInstantType> createTimeInstant(TimeInstantType value) {
        return new JAXBElement<TimeInstantType>(_TimeInstant_QNAME, TimeInstantType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractTimeGeometricPrimitiveType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "AbstractTimeGeometricPrimitive", substitutionHeadNamespace = "http://www.opengis.net/gml/3.2", substitutionHeadName = "AbstractTimePrimitive")
    public JAXBElement<AbstractTimeGeometricPrimitiveType> createAbstractTimeGeometricPrimitive(AbstractTimeGeometricPrimitiveType value) {
        return new JAXBElement<AbstractTimeGeometricPrimitiveType>(_AbstractTimeGeometricPrimitive_QNAME, AbstractTimeGeometricPrimitiveType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link List }{@code <}{@link BigInteger }{@code >}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "integerValueList")
    public JAXBElement<List<BigInteger>> createIntegerValueList(List<BigInteger> value) {
        return new JAXBElement<List<BigInteger>>(_IntegerValueList_QNAME, ((Class) List.class), null, ((List<BigInteger> ) value));
    }

   /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "stringValue")
    public JAXBElement<String> createStringValue(String value) {
        return new JAXBElement<String>(_StringValue_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TimePrimitivePropertyType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "validTime")
    public JAXBElement<TimePrimitivePropertyType> createValidTime(TimePrimitivePropertyType value) {
        return new JAXBElement<TimePrimitivePropertyType>(_ValidTime_QNAME, TimePrimitivePropertyType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "remarks")
    public JAXBElement<String> createRemarks(String value) {
        return new JAXBElement<String>(_Remarks_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "realizationEpoch")
    public JAXBElement<XMLGregorianCalendar> createRealizationEpoch(XMLGregorianCalendar value) {
        return new JAXBElement<XMLGregorianCalendar>(_RealizationEpoch_QNAME, XMLGregorianCalendar.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link List }{@code <}{@link String }{@code >}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "CountList", substitutionHeadNamespace = "http://www.opengis.net/gml/3.2", substitutionHeadName = "AbstractScalarValueList")
    public JAXBElement<List<String>> createCountList(List<String> value) {
        return new JAXBElement<List<String>>(_CountList_QNAME, ((Class) List.class), null, ((List<String> ) value));
    }

   /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "maximumOccurs")
    public JAXBElement<BigInteger> createMaximumOccurs(BigInteger value) {
        return new JAXBElement<BigInteger>(_MaximumOccurs_QNAME, BigInteger.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "valueFile")
    public JAXBElement<String> createValueFile(String value) {
        return new JAXBElement<String>(_ValueFile_QNAME, String.class, null, value);
    }

    
   /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Double }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "maximumValue")
    public JAXBElement<Double> createMaximumValue(Double value) {
        return new JAXBElement<Double>(_MaximumValue_QNAME, Double.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link List }{@code <}{@link String }{@code >}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml/3.2", name = "BooleanList", substitutionHeadNamespace = "http://www.opengis.net/gml/3.2", substitutionHeadName = "AbstractScalarValueList")
    public JAXBElement<List<String>> createBooleanList(List<String> value) {
        return new JAXBElement<List<String>>(_BooleanList_QNAME, ((Class) List.class), null, ((List<String> ) value));
    }
}

