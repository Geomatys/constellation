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

package net.seagis.gml.v311;

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
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the net.opengis.gml package. 
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

    private final static QName _AbstractGML_QNAME = new QName("http://www.opengis.net/gml", "AbstractGML");
    private final static QName _DatumID_QNAME = new QName("http://www.opengis.net/gml", "datumID");
    private final static QName _SourceDimensions_QNAME = new QName("http://www.opengis.net/gml", "sourceDimensions");
    private final static QName _SourceCRS_QNAME = new QName("http://www.opengis.net/gml", "sourceCRS");
    private final static QName _UsesCartesianCS_QNAME = new QName("http://www.opengis.net/gml", "usesCartesianCS");
    private final static QName _DoubleOrNullTupleList_QNAME = new QName("http://www.opengis.net/gml", "doubleOrNullTupleList");
    private final static QName _InnerBoundaryIs_QNAME = new QName("http://www.opengis.net/gml", "innerBoundaryIs");
    private final static QName _TimePrimitive_QNAME = new QName("http://www.opengis.net/gml", "_TimePrimitive");
    private final static QName _ValidArea_QNAME = new QName("http://www.opengis.net/gml", "validArea");
    private final static QName _EllipsoidID_QNAME = new QName("http://www.opengis.net/gml", "ellipsoidID");
    private final static QName _TimeInterval_QNAME = new QName("http://www.opengis.net/gml", "timeInterval");
    private final static QName _Category_QNAME = new QName("http://www.opengis.net/gml", "Category");
    private final static QName _DataSource_QNAME = new QName("http://www.opengis.net/gml", "dataSource");
    private final static QName _TimePeriod_QNAME = new QName("http://www.opengis.net/gml", "TimePeriod");
    private final static QName _Null_QNAME = new QName("http://www.opengis.net/gml", "Null");
    private final static QName _Pos_QNAME = new QName("http://www.opengis.net/gml", "pos");
    private final static QName _CartesianCS_QNAME = new QName("http://www.opengis.net/gml", "CartesianCS");
    private final static QName _Exterior_QNAME = new QName("http://www.opengis.net/gml", "exterior");
    private final static QName _MethodName_QNAME = new QName("http://www.opengis.net/gml", "methodName");
    private final static QName _MinimumOccurs_QNAME = new QName("http://www.opengis.net/gml", "minimumOccurs");
    private final static QName _AxisAbbrev_QNAME = new QName("http://www.opengis.net/gml", "axisAbbrev");
    private final static QName _DecimalMinutes_QNAME = new QName("http://www.opengis.net/gml", "decimalMinutes");
    private final static QName _Seconds_QNAME = new QName("http://www.opengis.net/gml", "seconds");
    private final static QName _TargetCRS_QNAME = new QName("http://www.opengis.net/gml", "targetCRS");
    private final static QName _UsesObliqueCartesianCS_QNAME = new QName("http://www.opengis.net/gml", "usesObliqueCartesianCS");
    private final static QName _MethodFormula_QNAME = new QName("http://www.opengis.net/gml", "methodFormula");
    private final static QName _Status_QNAME = new QName("http://www.opengis.net/gml", "status");
    private final static QName _ModifiedCoordinate_QNAME = new QName("http://www.opengis.net/gml", "modifiedCoordinate");
    private final static QName _CoordinateOperationName_QNAME = new QName("http://www.opengis.net/gml", "coordinateOperationName");
    private final static QName _CartesianCSRef_QNAME = new QName("http://www.opengis.net/gml", "cartesianCSRef");
    private final static QName _Covariance_QNAME = new QName("http://www.opengis.net/gml", "covariance");
    private final static QName _Origin_QNAME = new QName("http://www.opengis.net/gml", "origin");
    private final static QName _Minutes_QNAME = new QName("http://www.opengis.net/gml", "minutes");
    private final static QName _ImageDatumRef_QNAME = new QName("http://www.opengis.net/gml", "imageDatumRef");
    private final static QName _EnvelopeWithTimePeriod_QNAME = new QName("http://www.opengis.net/gml", "EnvelopeWithTimePeriod");
    private final static QName _MappingRule_QNAME = new QName("http://www.opengis.net/gml", "MappingRule");
    private final static QName _CsName_QNAME = new QName("http://www.opengis.net/gml", "csName");
    private final static QName _Count_QNAME = new QName("http://www.opengis.net/gml", "Count");
    private final static QName _ReferenceSystem_QNAME = new QName("http://www.opengis.net/gml", "_ReferenceSystem");
    private final static QName _AxisDirection_QNAME = new QName("http://www.opengis.net/gml", "axisDirection");
    private final static QName _AxisID_QNAME = new QName("http://www.opengis.net/gml", "axisID");
    private final static QName _ParameterName_QNAME = new QName("http://www.opengis.net/gml", "parameterName");
    private final static QName _DatumName_QNAME = new QName("http://www.opengis.net/gml", "datumName");
    private final static QName _PixelInCell_QNAME = new QName("http://www.opengis.net/gml", "pixelInCell");
    private final static QName _ImageDatum_QNAME = new QName("http://www.opengis.net/gml", "ImageDatum");
    private final static QName _MeridianID_QNAME = new QName("http://www.opengis.net/gml", "meridianID");
    private final static QName _AbstractRing_QNAME = new QName("http://www.opengis.net/gml", "AbstractRing");
    private final static QName _TimePosition_QNAME = new QName("http://www.opengis.net/gml", "timePosition");
    private final static QName _OperationVersion_QNAME = new QName("http://www.opengis.net/gml", "operationVersion");
    private final static QName _IntegerValue_QNAME = new QName("http://www.opengis.net/gml", "integerValue");
    private final static QName _UsesImageDatum_QNAME = new QName("http://www.opengis.net/gml", "usesImageDatum");
    private final static QName _AbstractSurface_QNAME = new QName("http://www.opengis.net/gml", "AbstractSurface");
    private final static QName _ColumnIndex_QNAME = new QName("http://www.opengis.net/gml", "columnIndex");
    private final static QName _MethodID_QNAME = new QName("http://www.opengis.net/gml", "methodID");
    private final static QName _TimeGeometricPrimitive_QNAME = new QName("http://www.opengis.net/gml", "_TimeGeometricPrimitive");
    private final static QName _Version_QNAME = new QName("http://www.opengis.net/gml", "version");
    private final static QName _Operation_QNAME = new QName("http://www.opengis.net/gml", "_Operation");
    private final static QName _OuterBoundaryIs_QNAME = new QName("http://www.opengis.net/gml", "outerBoundaryIs");
    private final static QName _CoordinateOperationID_QNAME = new QName("http://www.opengis.net/gml", "coordinateOperationID");
    private final static QName _ValidTime_QNAME = new QName("http://www.opengis.net/gml", "validTime");
    private final static QName _CoordinateSystemAxisRef_QNAME = new QName("http://www.opengis.net/gml", "coordinateSystemAxisRef");
    private final static QName _StringValue_QNAME = new QName("http://www.opengis.net/gml", "stringValue");
    private final static QName _Datum_QNAME = new QName("http://www.opengis.net/gml", "_Datum");
    private final static QName _TemporalExtent_QNAME = new QName("http://www.opengis.net/gml", "temporalExtent");
    private final static QName _Description_QNAME = new QName("http://www.opengis.net/gml", "description");
    private final static QName _Boolean_QNAME = new QName("http://www.opengis.net/gml", "Boolean");
    private final static QName _RealizationEpoch_QNAME = new QName("http://www.opengis.net/gml", "realizationEpoch");
    private final static QName _CountList_QNAME = new QName("http://www.opengis.net/gml", "CountList");
    private final static QName _Remarks_QNAME = new QName("http://www.opengis.net/gml", "remarks");
    private final static QName _CoordinateOperation_QNAME = new QName("http://www.opengis.net/gml", "_CoordinateOperation");
    private final static QName _MeasureDescription_QNAME = new QName("http://www.opengis.net/gml", "measureDescription");
    private final static QName _SingleOperation_QNAME = new QName("http://www.opengis.net/gml", "_SingleOperation");
    private final static QName _Name_QNAME = new QName("http://www.opengis.net/gml", "name");
    private final static QName _CRS_QNAME = new QName("http://www.opengis.net/gml", "_CRS");
    private final static QName _ValueFile_QNAME = new QName("http://www.opengis.net/gml", "valueFile");
    private final static QName _GeometricPrimitive_QNAME = new QName("http://www.opengis.net/gml", "_GeometricPrimitive");
    private final static QName _RowIndex_QNAME = new QName("http://www.opengis.net/gml", "rowIndex");
    private final static QName _EllipsoidName_QNAME = new QName("http://www.opengis.net/gml", "ellipsoidName");
    private final static QName _MaximumOccurs_QNAME = new QName("http://www.opengis.net/gml", "maximumOccurs");
    private final static QName _Point_QNAME = new QName("http://www.opengis.net/gml", "Point");
    private final static QName _Object_QNAME = new QName("http://www.opengis.net/gml", "_Object");
    private final static QName _BooleanList_QNAME = new QName("http://www.opengis.net/gml", "BooleanList");
    private final static QName _ObliqueCartesianCS_QNAME = new QName("http://www.opengis.net/gml", "ObliqueCartesianCS");
    private final static QName _Polygon_QNAME = new QName("http://www.opengis.net/gml", "Polygon");
    private final static QName _MeridianName_QNAME = new QName("http://www.opengis.net/gml", "meridianName");
    private final static QName _CoordinateSystemAxis_QNAME = new QName("http://www.opengis.net/gml", "CoordinateSystemAxis");
    private final static QName _GroupName_QNAME = new QName("http://www.opengis.net/gml", "groupName");
    private final static QName _CatalogSymbol_QNAME = new QName("http://www.opengis.net/gml", "catalogSymbol");
    private final static QName _Envelope_QNAME = new QName("http://www.opengis.net/gml", "Envelope");
    private final static QName _CountExtent_QNAME = new QName("http://www.opengis.net/gml", "CountExtent");
    private final static QName _CsID_QNAME = new QName("http://www.opengis.net/gml", "csID");
    private final static QName _TupleList_QNAME = new QName("http://www.opengis.net/gml", "tupleList");
    private final static QName _BoundingPolygon_QNAME = new QName("http://www.opengis.net/gml", "boundingPolygon");
    private final static QName _CrsRef_QNAME = new QName("http://www.opengis.net/gml", "crsRef");
    private final static QName _IsSphere_QNAME = new QName("http://www.opengis.net/gml", "isSphere");
    private final static QName _BooleanValue_QNAME = new QName("http://www.opengis.net/gml", "booleanValue");
    private final static QName _Interior_QNAME = new QName("http://www.opengis.net/gml", "interior");
    private final static QName _ObliqueCartesianCSRef_QNAME = new QName("http://www.opengis.net/gml", "obliqueCartesianCSRef");
    private final static QName _TargetDimensions_QNAME = new QName("http://www.opengis.net/gml", "targetDimensions");
    private final static QName _CoordinateSystem_QNAME = new QName("http://www.opengis.net/gml", "_CoordinateSystem");
    private final static QName _ParameterID_QNAME = new QName("http://www.opengis.net/gml", "parameterID");
    private final static QName _Duration_QNAME = new QName("http://www.opengis.net/gml", "duration");
    private final static QName _TimeObject_QNAME = new QName("http://www.opengis.net/gml", "_TimeObject");
    private final static QName _Geometry_QNAME = new QName("http://www.opengis.net/gml", "_Geometry");
    private final static QName _SrsName_QNAME = new QName("http://www.opengis.net/gml", "srsName");
    private final static QName _Scope_QNAME = new QName("http://www.opengis.net/gml", "scope");
    private final static QName _CoordinateReferenceSystem_QNAME = new QName("http://www.opengis.net/gml", "_CoordinateReferenceSystem");
    private final static QName _Coordinates_QNAME = new QName("http://www.opengis.net/gml", "coordinates");
    private final static QName _LocationKeyWord_QNAME = new QName("http://www.opengis.net/gml", "LocationKeyWord");
    private final static QName _ImageCRS_QNAME = new QName("http://www.opengis.net/gml", "ImageCRS");
    private final static QName _SrsID_QNAME = new QName("http://www.opengis.net/gml", "srsID");
    private final static QName _ImplicitGeometry_QNAME = new QName("http://www.opengis.net/gml", "_ImplicitGeometry");
    private final static QName _AbstractObject_QNAME = new QName("http://www.opengis.net/gml", "AbstractObject");
    private final static QName _QuantityType_QNAME = new QName("http://www.opengis.net/gml", "quantityType");
    private final static QName _LocationString_QNAME = new QName("http://www.opengis.net/gml", "LocationString");
    private final static QName _UsesAxis_QNAME = new QName("http://www.opengis.net/gml", "usesAxis");
    private final static QName _AnchorPoint_QNAME = new QName("http://www.opengis.net/gml", "anchorPoint");
    private final static QName _IntegerValueList_QNAME = new QName("http://www.opengis.net/gml", "integerValueList");
    private final static QName _Definition_QNAME = new QName("http://www.opengis.net/gml", "Definition");
    private final static QName _PositionalAccuracy_QNAME = new QName("http://www.opengis.net/gml", "_positionalAccuracy");
    private final static QName _GroupID_QNAME = new QName("http://www.opengis.net/gml", "groupID");
    private final static QName _TimeInstant_QNAME = new QName("http://www.opengis.net/gml", "TimeInstant");
    private final static QName _AbstractGeometry_QNAME = new QName("http://www.opengis.net/gml", "AbstractGeometry");
    private final static QName _MetaDataProperty_QNAME = new QName("http://www.opengis.net/gml", "metaDataProperty");
    private final static QName _RectifiedGrid_QNAME = new QName("http://www.opengis.net/gml", "RectifiedGrid");
    private final static QName _Grid_QNAME = new QName("http://www.opengis.net/gml", "Grid");
    private final static QName _AbstractFeature_QNAME = new QName("http://www.opengis.net/gml", "AbstractFeature");
    private final static QName _BaseUnit_QNAME = new QName("http://www.opengis.net/gml", "BaseUnit");
    private final static QName _BoundedBy_QNAME = new QName("http://www.opengis.net/gml", "boundedBy");
    private final static QName _Location_QNAME = new QName("http://www.opengis.net/gml", "location");
    private final static QName _UnitDefinition_QNAME = new QName("http://www.opengis.net/gml", "UnitDefinition");
    private final static QName _UnitOfMeasure_QNAME = new QName("http://www.opengis.net/gml", "unitOfMeasure");
     
    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: net.opengis.gml
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link MetaDataPropertyType }
     * 
     */
    public MetaDataPropertyType createMetaDataPropertyType() {
        return new MetaDataPropertyType();
    }
    
    /**
     * Create an instance of {@link CRSRefType }
     * 
     */
    public CRSRefType createCRSRefType() {
        return new CRSRefType();
    }

    /**
     * Create an instance of {@link ExtentType }
     * 
     */
    public ExtentType createExtentType() {
        return new ExtentType();
    }

    /**
     * Create an instance of {@link ImageCRSType }
     * 
     */
    public ImageCRSType createImageCRSType() {
        return new ImageCRSType();
    }

   
    /**
     * Create an instance of {@link TimeIntervalLengthType }
     * 
     */
    public TimeIntervalLengthType createTimeIntervalLengthType() {
        return new TimeIntervalLengthType();
    }

    
    /**
     * Create an instance of {@link TimePositionType }
     * 
     */
    public TimePositionType createTimePositionType() {
        return new TimePositionType();
    }

    /**
     * Create an instance of {@link CartesianCSRefType }
     * 
     */
    public CartesianCSRefType createCartesianCSRefType() {
        return new CartesianCSRefType();
    }

    /**
     * Create an instance of {@link PolygonType }
     * 
     */
    public PolygonType createPolygonType() {
        return new PolygonType();
    }


    /**
     * Create an instance of {@link TimePeriodType }
     * 
     */
    public TimePeriodType createTimePeriodType() {
        return new TimePeriodType();
    }

    /**
     * Create an instance of {@link ImageDatumType }
     * 
     */
    public ImageDatumType createImageDatumType() {
        return new ImageDatumType();
    }

    /**
     * Create an instance of {@link ImageDatumRefType }
     * 
     */
    public ImageDatumRefType createImageDatumRefType() {
        return new ImageDatumRefType();
    }

    /**
     * Create an instance of {@link IdentifierType }
     * 
     */
    public IdentifierType createIdentifierType() {
        return new IdentifierType();
    }

    /**
     * Create an instance of {@link CoordinatesType }
     * 
     */
    public CoordinatesType createCoordinatesType() {
        return new CoordinatesType();
    }

    /**
     * Create an instance of {@link RelatedTimeType }
     * 
     */
    public RelatedTimeType createRelatedTimeType() {
        return new RelatedTimeType();
    }


    /**
     * Create an instance of {@link DirectPositionType }
     * 
     */
    public DirectPositionType createDirectPositionType() {
        return new DirectPositionType();
    }

    /**
     * Create an instance of {@link TimePrimitivePropertyType }
     * 
     */
    public TimePrimitivePropertyType createTimePrimitivePropertyType() {
        return new TimePrimitivePropertyType();
    }


    /**
     * Create an instance of {@link CodeType }
     * 
     */
    public CodeType createCodeType() {
        return new CodeType();
    }

    /*
     * Create an instance of {@link AbstractRingPropertyType }
     * 
     */
    public AbstractRingPropertyType createAbstractRingPropertyType() {
        return new AbstractRingPropertyType();
    }
    
    /**
     * Create an instance of {@link PointType }
     * 
     */
    public PointType createPointType() {
        return new PointType();
    }


    /**
     * Create an instance of {@link TimeInstantPropertyType }
     * 
     */
    public TimeInstantPropertyType createTimeInstantPropertyType() {
        return new TimeInstantPropertyType();
    }

    /**
     * Create an instance of {@link CoordinateSystemAxisType }
     * 
     */
    public CoordinateSystemAxisType createCoordinateSystemAxisType() {
        return new CoordinateSystemAxisType();
    }

    /**
     * Create an instance of {@link DefinitionType }
     * 
     */
    public DefinitionType createDefinitionType() {
        return new DefinitionType();
    }


    /**
     * Create an instance of {@link TimeInstantType }
     * 
     */
    public TimeInstantType createTimeInstantType() {
        return new TimeInstantType();
    }
    
    /**
     * Create an instance of {@link AbstractSurfaceType }
     * 
     */
    public AbstractSurfaceType createAbstractSurfaceType() {
        return new AbstractSurfaceType();
    }

   
    /**
     * Create an instance of {@link StringOrRefType }
     * 
     */
    public StringOrRefType createStringOrRefType() {
        return new StringOrRefType();
    }
    
    /**
     * Create an instance of {@link CartesianCSType }
     * 
     */
    public CartesianCSType createCartesianCSType() {
        return new CartesianCSType();
    }

    /**
     * Create an instance of {@link ObliqueCartesianCSRefType }
     * 
     */
    public ObliqueCartesianCSRefType createObliqueCartesianCSRefType() {
        return new ObliqueCartesianCSRefType();
    }

    
    /**
     * Create an instance of {@link PixelInCellType }
     * 
     */
    public PixelInCellType createPixelInCellType() {
        return new PixelInCellType();
    }

    
    /**
     * Create an instance of {@link EnvelopeWithTimePeriodType }
     * 
     */
    public EnvelopeWithTimePeriodType createEnvelopeWithTimePeriodType() {
        return new EnvelopeWithTimePeriodType();
    }


    /**
     * Create an instance of {@link CoordinateSystemAxisRefType }
     * 
     */
    public CoordinateSystemAxisRefType createCoordinateSystemAxisRefType() {
        return new CoordinateSystemAxisRefType();
    }

    /**
     * Create an instance of {@link ObliqueCartesianCSType }
     * 
     */
    public ObliqueCartesianCSType createObliqueCartesianCSType() {
        return new ObliqueCartesianCSType();
    }
    
    /**
     * Create an instance of {@link CodeListType }
     * 
     */
    public CodeListType createCodeListType() {
        return new CodeListType();
    }
    
    /**
     * Create an instance of {@link GridType }
     * 
     */
    public GridType createGridType() {
        return new GridType();
    }
    
    /**
     * Create an instance of {@link RectifiedGridType }
     * 
     */
    public RectifiedGridType createRectifiedGridType() {
        return new RectifiedGridType();
    }
    
     /**
     * Create an instance of {@link GridLimitsType }
     * 
     */
    public GridLimitsType createGridLimitsType() {
        return new GridLimitsType();
    }
    
    /**
     * Create an instance of {@link GridEnvelopeType }
     * 
     */
    public GridEnvelopeType createGridEnvelopeType() {
        return new GridEnvelopeType();
    }

    /**
     * Create an instance of {@link BaseUnitType }
     * 
     */
    public BaseUnitType createBaseUnitType() {
        return new BaseUnitType();
    }
    
    /**
     * Create an instance of {@link BoundingShapeType }
     * 
     */
    public BoundingShapeEntry createBoundingShapeType() {
        return new BoundingShapeEntry();
    }
    
    /**
     * Create an instance of {@link LocationPropertyType }
     * 
     */
    public LocationPropertyType createLocationPropertyType() {
        return new LocationPropertyType();
    }
    
    /**
     * Create an instance of {@link TimePeriodPropertyType }
     * 
     */
    public TimePeriodPropertyType createTimePeriodPropertyType() {
        return new TimePeriodPropertyType();
    }

    /**
     * Create an instance of {@link UnitDefinitionType }
     * 
     */
    public UnitDefinitionType createUnitDefinitionType() {
        return new UnitDefinitionType();
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
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "unitOfMeasure")
    public JAXBElement<UnitOfMeasureType> createUnitOfMeasure(UnitOfMeasureType value) {
        return new JAXBElement<UnitOfMeasureType>(_UnitOfMeasure_QNAME, UnitOfMeasureType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UnitDefinitionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "UnitDefinition", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "Definition")
    public JAXBElement<UnitDefinitionType> createUnitDefinition(UnitDefinitionType value) {
        return new JAXBElement<UnitDefinitionType>(_UnitDefinition_QNAME, UnitDefinitionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LocationPropertyType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "location")
    public JAXBElement<LocationPropertyType> createLocation(LocationPropertyType value) {
        return new JAXBElement<LocationPropertyType>(_Location_QNAME, LocationPropertyType.class, null, value);
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BoundingShapeEntry }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "boundedBy")
    public JAXBElement<BoundingShapeEntry> createBoundedBy(BoundingShapeEntry value) {
        return new JAXBElement<BoundingShapeEntry>(_BoundedBy_QNAME, BoundingShapeEntry.class, null, value);
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BaseUnitType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "BaseUnit", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "UnitDefinition")
    public JAXBElement<BaseUnitType> createBaseUnit(BaseUnitType value) {
        return new JAXBElement<BaseUnitType>(_BaseUnit_QNAME, BaseUnitType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractFeatureEntry }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "AbstractFeature", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "AbstractGML")
    public JAXBElement<AbstractFeatureEntry> createAbstractFeature(AbstractFeatureEntry value) {
        return new JAXBElement<AbstractFeatureEntry>(_AbstractFeature_QNAME, AbstractFeatureEntry.class, null, value);
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RectifiedGridType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "RectifiedGrid", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "Grid")
    public JAXBElement<RectifiedGridType> createRectifiedGrid(RectifiedGridType value) {
        return new JAXBElement<RectifiedGridType>(_RectifiedGrid_QNAME, RectifiedGridType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GridType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "Grid", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "_Geometry")
    public JAXBElement<GridType> createGrid(GridType value) {
        return new JAXBElement<GridType>(_Grid_QNAME, GridType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Object }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "AbstractObject")
    public JAXBElement<Object> createAbstractObject(Object value) {
        return new JAXBElement<Object>(_AbstractObject_QNAME, Object.class, null, value);
    }

     /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractGeometryType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "AbstractGeometry", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "AbstractGML")
    public JAXBElement<AbstractGeometryType> createAbstractGeometry(AbstractGeometryType value) {
        return new JAXBElement<AbstractGeometryType>(_AbstractGeometry_QNAME, AbstractGeometryType.class, null, value);
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnvelopeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "Envelope", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "AbstractObject")
    public JAXBElement<EnvelopeEntry> createEnvelope(EnvelopeEntry value) {
        return new JAXBElement<EnvelopeEntry>(_Envelope_QNAME, EnvelopeEntry.class, null, value);
    }
    
     /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MetaDataPropertyType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "metaDataProperty")
    public JAXBElement<MetaDataPropertyType> createMetaDataProperty(MetaDataPropertyType value) {
        return new JAXBElement<MetaDataPropertyType>(_MetaDataProperty_QNAME, MetaDataPropertyType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractGMLType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "AbstractGML", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "AbstractObject")
    public JAXBElement<AbstractGMLEntry> createAbstractGML(AbstractGMLEntry value) {
        return new JAXBElement<AbstractGMLEntry>(_AbstractGML_QNAME, AbstractGMLEntry.class, null, value);
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IdentifierType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "datumID")
    public JAXBElement<IdentifierType> createDatumID(IdentifierType value) {
        return new JAXBElement<IdentifierType>(_DatumID_QNAME, IdentifierType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "sourceDimensions")
    public JAXBElement<BigInteger> createSourceDimensions(BigInteger value) {
        return new JAXBElement<BigInteger>(_SourceDimensions_QNAME, BigInteger.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CRSRefType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "sourceCRS")
    public JAXBElement<CRSRefType> createSourceCRS(CRSRefType value) {
        return new JAXBElement<CRSRefType>(_SourceCRS_QNAME, CRSRefType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CartesianCSRefType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "usesCartesianCS")
    public JAXBElement<CartesianCSRefType> createUsesCartesianCS(CartesianCSRefType value) {
        return new JAXBElement<CartesianCSRefType>(_UsesCartesianCS_QNAME, CartesianCSRefType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link List }{@code <}{@link String }{@code >}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "doubleOrNullTupleList")
    public JAXBElement<List<String>> createDoubleOrNullTupleList(List<String> value) {
        return new JAXBElement<List<String>>(_DoubleOrNullTupleList_QNAME, ((Class) List.class), null, ((List<String> ) value));
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractRingPropertyType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "innerBoundaryIs", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "interior")
    public JAXBElement<AbstractRingPropertyType> createInnerBoundaryIs(AbstractRingPropertyType value) {
        return new JAXBElement<AbstractRingPropertyType>(_InnerBoundaryIs_QNAME, AbstractRingPropertyType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractTimePrimitiveType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "_TimePrimitive", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "_TimeObject")
    public JAXBElement<AbstractTimePrimitiveType> createTimePrimitive(AbstractTimePrimitiveType value) {
        return new JAXBElement<AbstractTimePrimitiveType>(_TimePrimitive_QNAME, AbstractTimePrimitiveType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ExtentType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "validArea")
    public JAXBElement<ExtentType> createValidArea(ExtentType value) {
        return new JAXBElement<ExtentType>(_ValidArea_QNAME, ExtentType.class, null, value);
    }

    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IdentifierType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "ellipsoidID")
    public JAXBElement<IdentifierType> createEllipsoidID(IdentifierType value) {
        return new JAXBElement<IdentifierType>(_EllipsoidID_QNAME, IdentifierType.class, null, value);
    }


    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TimeIntervalLengthType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "timeInterval")
    public JAXBElement<TimeIntervalLengthType> createTimeInterval(TimeIntervalLengthType value) {
        return new JAXBElement<TimeIntervalLengthType>(_TimeInterval_QNAME, TimeIntervalLengthType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "Category")
    public JAXBElement<CodeType> createCategory(CodeType value) {
        return new JAXBElement<CodeType>(_Category_QNAME, CodeType.class, null, value);
    }


    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringOrRefType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "dataSource")
    public JAXBElement<StringOrRefType> createDataSource(StringOrRefType value) {
        return new JAXBElement<StringOrRefType>(_DataSource_QNAME, StringOrRefType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TimePeriodType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "TimePeriod", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "_TimeGeometricPrimitive")
    public JAXBElement<TimePeriodType> createTimePeriod(TimePeriodType value) {
        return new JAXBElement<TimePeriodType>(_TimePeriod_QNAME, TimePeriodType.class, null, value);
    }

    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link List }{@code <}{@link String }{@code >}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "Null")
    public JAXBElement<List<String>> createNull(List<String> value) {
        return new JAXBElement<List<String>>(_Null_QNAME, ((Class) List.class), null, ((List<String> ) value));
    }

    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DirectPositionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "pos")
    public JAXBElement<DirectPositionType> createPos(DirectPositionType value) {
        return new JAXBElement<DirectPositionType>(_Pos_QNAME, DirectPositionType.class, null, value);
    }


    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CartesianCSType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "CartesianCS", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "_CoordinateSystem")
    public JAXBElement<CartesianCSType> createCartesianCS(CartesianCSType value) {
        return new JAXBElement<CartesianCSType>(_CartesianCS_QNAME, CartesianCSType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractRingPropertyType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "exterior")
    public JAXBElement<AbstractRingPropertyType> createExterior(AbstractRingPropertyType value) {
        return new JAXBElement<AbstractRingPropertyType>(_Exterior_QNAME, AbstractRingPropertyType.class, null, value);
    }


    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "methodName", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "name")
    public JAXBElement<CodeType> createMethodName(CodeType value) {
        return new JAXBElement<CodeType>(_MethodName_QNAME, CodeType.class, null, value);
    }


    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "minimumOccurs")
    public JAXBElement<BigInteger> createMinimumOccurs(BigInteger value) {
        return new JAXBElement<BigInteger>(_MinimumOccurs_QNAME, BigInteger.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "axisAbbrev")
    public JAXBElement<CodeType> createAxisAbbrev(CodeType value) {
        return new JAXBElement<CodeType>(_AxisAbbrev_QNAME, CodeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigDecimal }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "decimalMinutes")
    public JAXBElement<BigDecimal> createDecimalMinutes(BigDecimal value) {
        return new JAXBElement<BigDecimal>(_DecimalMinutes_QNAME, BigDecimal.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigDecimal }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "seconds")
    public JAXBElement<BigDecimal> createSeconds(BigDecimal value) {
        return new JAXBElement<BigDecimal>(_Seconds_QNAME, BigDecimal.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CRSRefType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "targetCRS")
    public JAXBElement<CRSRefType> createTargetCRS(CRSRefType value) {
        return new JAXBElement<CRSRefType>(_TargetCRS_QNAME, CRSRefType.class, null, value);
    }

   

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ObliqueCartesianCSRefType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "usesObliqueCartesianCS")
    public JAXBElement<ObliqueCartesianCSRefType> createUsesObliqueCartesianCS(ObliqueCartesianCSRefType value) {
        return new JAXBElement<ObliqueCartesianCSRefType>(_UsesObliqueCartesianCS_QNAME, ObliqueCartesianCSRefType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "methodFormula")
    public JAXBElement<CodeType> createMethodFormula(CodeType value) {
        return new JAXBElement<CodeType>(_MethodFormula_QNAME, CodeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringOrRefType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "status")
    public JAXBElement<StringOrRefType> createStatus(StringOrRefType value) {
        return new JAXBElement<StringOrRefType>(_Status_QNAME, StringOrRefType.class, null, value);
    }



    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "modifiedCoordinate")
    public JAXBElement<BigInteger> createModifiedCoordinate(BigInteger value) {
        return new JAXBElement<BigInteger>(_ModifiedCoordinate_QNAME, BigInteger.class, null, value);
    }


    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "coordinateOperationName", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "name")
    public JAXBElement<CodeType> createCoordinateOperationName(CodeType value) {
        return new JAXBElement<CodeType>(_CoordinateOperationName_QNAME, CodeType.class, null, value);
    }


    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CartesianCSRefType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "cartesianCSRef")
    public JAXBElement<CartesianCSRefType> createCartesianCSRef(CartesianCSRefType value) {
        return new JAXBElement<CartesianCSRefType>(_CartesianCSRef_QNAME, CartesianCSRefType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Double }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "covariance")
    public JAXBElement<Double> createCovariance(Double value) {
        return new JAXBElement<Double>(_Covariance_QNAME, Double.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "origin")
    public JAXBElement<XMLGregorianCalendar> createOrigin(XMLGregorianCalendar value) {
        return new JAXBElement<XMLGregorianCalendar>(_Origin_QNAME, XMLGregorianCalendar.class, null, value);
    }


    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "minutes")
    public JAXBElement<Integer> createMinutes(Integer value) {
        return new JAXBElement<Integer>(_Minutes_QNAME, Integer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ImageDatumRefType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "imageDatumRef")
    public JAXBElement<ImageDatumRefType> createImageDatumRef(ImageDatumRefType value) {
        return new JAXBElement<ImageDatumRefType>(_ImageDatumRef_QNAME, ImageDatumRefType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EnvelopeWithTimePeriodType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "EnvelopeWithTimePeriod", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "Envelope")
    public JAXBElement<EnvelopeWithTimePeriodType> createEnvelopeWithTimePeriod(EnvelopeWithTimePeriodType value) {
        return new JAXBElement<EnvelopeWithTimePeriodType>(_EnvelopeWithTimePeriod_QNAME, EnvelopeWithTimePeriodType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringOrRefType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "MappingRule")
    public JAXBElement<StringOrRefType> createMappingRule(StringOrRefType value) {
        return new JAXBElement<StringOrRefType>(_MappingRule_QNAME, StringOrRefType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "csName", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "name")
    public JAXBElement<CodeType> createCsName(CodeType value) {
        return new JAXBElement<CodeType>(_CsName_QNAME, CodeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "Count")
    public JAXBElement<BigInteger> createCount(BigInteger value) {
        return new JAXBElement<BigInteger>(_Count_QNAME, BigInteger.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractReferenceSystemType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "_ReferenceSystem", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "Definition")
    public JAXBElement<AbstractReferenceSystemType> createReferenceSystem(AbstractReferenceSystemType value) {
        return new JAXBElement<AbstractReferenceSystemType>(_ReferenceSystem_QNAME, AbstractReferenceSystemType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "axisDirection")
    public JAXBElement<CodeType> createAxisDirection(CodeType value) {
        return new JAXBElement<CodeType>(_AxisDirection_QNAME, CodeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IdentifierType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "axisID")
    public JAXBElement<IdentifierType> createAxisID(IdentifierType value) {
        return new JAXBElement<IdentifierType>(_AxisID_QNAME, IdentifierType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "parameterName", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "name")
    public JAXBElement<CodeType> createParameterName(CodeType value) {
        return new JAXBElement<CodeType>(_ParameterName_QNAME, CodeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "datumName", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "name")
    public JAXBElement<CodeType> createDatumName(CodeType value) {
        return new JAXBElement<CodeType>(_DatumName_QNAME, CodeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PixelInCellType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "pixelInCell")
    public JAXBElement<PixelInCellType> createPixelInCell(PixelInCellType value) {
        return new JAXBElement<PixelInCellType>(_PixelInCell_QNAME, PixelInCellType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ImageDatumType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "ImageDatum", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "_Datum")
    public JAXBElement<ImageDatumType> createImageDatum(ImageDatumType value) {
        return new JAXBElement<ImageDatumType>(_ImageDatum_QNAME, ImageDatumType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IdentifierType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "meridianID")
    public JAXBElement<IdentifierType> createMeridianID(IdentifierType value) {
        return new JAXBElement<IdentifierType>(_MeridianID_QNAME, IdentifierType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractRingType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "AbstractRing", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "_Geometry")
    public JAXBElement<AbstractRingType> createAbstractRing(AbstractRingType value) {
        return new JAXBElement<AbstractRingType>(_AbstractRing_QNAME, AbstractRingType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TimePositionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "timePosition")
    public JAXBElement<TimePositionType> createTimePosition(TimePositionType value) {
        return new JAXBElement<TimePositionType>(_TimePosition_QNAME, TimePositionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "operationVersion")
    public JAXBElement<String> createOperationVersion(String value) {
        return new JAXBElement<String>(_OperationVersion_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "integerValue")
    public JAXBElement<BigInteger> createIntegerValue(BigInteger value) {
        return new JAXBElement<BigInteger>(_IntegerValue_QNAME, BigInteger.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ImageDatumRefType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "usesImageDatum")
    public JAXBElement<ImageDatumRefType> createUsesImageDatum(ImageDatumRefType value) {
        return new JAXBElement<ImageDatumRefType>(_UsesImageDatum_QNAME, ImageDatumRefType.class, null, value);
    }


    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractSurfaceType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "AbstractSurface", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "_GeometricPrimitive")
    public JAXBElement<AbstractSurfaceType> createAbstractSurface(AbstractSurfaceType value) {
        return new JAXBElement<AbstractSurfaceType>(_AbstractSurface_QNAME, AbstractSurfaceType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "columnIndex")
    public JAXBElement<BigInteger> createColumnIndex(BigInteger value) {
        return new JAXBElement<BigInteger>(_ColumnIndex_QNAME, BigInteger.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IdentifierType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "methodID")
    public JAXBElement<IdentifierType> createMethodID(IdentifierType value) {
        return new JAXBElement<IdentifierType>(_MethodID_QNAME, IdentifierType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractTimeGeometricPrimitiveType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "_TimeGeometricPrimitive", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "_TimePrimitive")
    public JAXBElement<AbstractTimeGeometricPrimitiveType> createTimeGeometricPrimitive(AbstractTimeGeometricPrimitiveType value) {
        return new JAXBElement<AbstractTimeGeometricPrimitiveType>(_TimeGeometricPrimitive_QNAME, AbstractTimeGeometricPrimitiveType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "version")
    public JAXBElement<String> createVersion(String value) {
        return new JAXBElement<String>(_Version_QNAME, String.class, null, value);
    }

     /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractCoordinateOperationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "_Operation", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "_SingleOperation")
    public JAXBElement<AbstractCoordinateOperationType> createOperation(AbstractCoordinateOperationType value) {
        return new JAXBElement<AbstractCoordinateOperationType>(_Operation_QNAME, AbstractCoordinateOperationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractRingPropertyType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "outerBoundaryIs", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "exterior")
    public JAXBElement<AbstractRingPropertyType> createOuterBoundaryIs(AbstractRingPropertyType value) {
        return new JAXBElement<AbstractRingPropertyType>(_OuterBoundaryIs_QNAME, AbstractRingPropertyType.class, null, value);
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IdentifierType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "coordinateOperationID")
    public JAXBElement<IdentifierType> createCoordinateOperationID(IdentifierType value) {
        return new JAXBElement<IdentifierType>(_CoordinateOperationID_QNAME, IdentifierType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TimePrimitivePropertyType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "validTime")
    public JAXBElement<TimePrimitivePropertyType> createValidTime(TimePrimitivePropertyType value) {
        return new JAXBElement<TimePrimitivePropertyType>(_ValidTime_QNAME, TimePrimitivePropertyType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CoordinateSystemAxisRefType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "coordinateSystemAxisRef")
    public JAXBElement<CoordinateSystemAxisRefType> createCoordinateSystemAxisRef(CoordinateSystemAxisRefType value) {
        return new JAXBElement<CoordinateSystemAxisRefType>(_CoordinateSystemAxisRef_QNAME, CoordinateSystemAxisRefType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "stringValue")
    public JAXBElement<String> createStringValue(String value) {
        return new JAXBElement<String>(_StringValue_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractDatumType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "_Datum", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "Definition")
    public JAXBElement<AbstractDatumType> createDatum(AbstractDatumType value) {
        return new JAXBElement<AbstractDatumType>(_Datum_QNAME, AbstractDatumType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TimePeriodType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "temporalExtent")
    public JAXBElement<TimePeriodType> createTemporalExtent(TimePeriodType value) {
        return new JAXBElement<TimePeriodType>(_TemporalExtent_QNAME, TimePeriodType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringOrRefType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "description")
    public JAXBElement<StringOrRefType> createDescription(StringOrRefType value) {
        return new JAXBElement<StringOrRefType>(_Description_QNAME, StringOrRefType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "Boolean")
    public JAXBElement<Boolean> createBoolean(Boolean value) {
        return new JAXBElement<Boolean>(_Boolean_QNAME, Boolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "realizationEpoch")
    public JAXBElement<XMLGregorianCalendar> createRealizationEpoch(XMLGregorianCalendar value) {
        return new JAXBElement<XMLGregorianCalendar>(_RealizationEpoch_QNAME, XMLGregorianCalendar.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link List }{@code <}{@link String }{@code >}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "CountList")
    public JAXBElement<List<String>> createCountList(List<String> value) {
        return new JAXBElement<List<String>>(_CountList_QNAME, ((Class) List.class), null, ((List<String> ) value));
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringOrRefType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "remarks")
    public JAXBElement<StringOrRefType> createRemarks(StringOrRefType value) {
        return new JAXBElement<StringOrRefType>(_Remarks_QNAME, StringOrRefType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractCoordinateOperationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "_CoordinateOperation", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "Definition")
    public JAXBElement<AbstractCoordinateOperationType> createCoordinateOperation(AbstractCoordinateOperationType value) {
        return new JAXBElement<AbstractCoordinateOperationType>(_CoordinateOperation_QNAME, AbstractCoordinateOperationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "measureDescription")
    public JAXBElement<CodeType> createMeasureDescription(CodeType value) {
        return new JAXBElement<CodeType>(_MeasureDescription_QNAME, CodeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractCoordinateOperationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "_SingleOperation", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "_CoordinateOperation")
    public JAXBElement<AbstractCoordinateOperationType> createSingleOperation(AbstractCoordinateOperationType value) {
        return new JAXBElement<AbstractCoordinateOperationType>(_SingleOperation_QNAME, AbstractCoordinateOperationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "name")
    public JAXBElement<CodeType> createName(CodeType value) {
        return new JAXBElement<CodeType>(_Name_QNAME, CodeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractReferenceSystemType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "_CRS", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "_ReferenceSystem")
    public JAXBElement<AbstractReferenceSystemType> createCRS(AbstractReferenceSystemType value) {
        return new JAXBElement<AbstractReferenceSystemType>(_CRS_QNAME, AbstractReferenceSystemType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "valueFile")
    public JAXBElement<String> createValueFile(String value) {
        return new JAXBElement<String>(_ValueFile_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractGeometricPrimitiveType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "_GeometricPrimitive", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "_Geometry")
    public JAXBElement<AbstractGeometricPrimitiveType> createGeometricPrimitive(AbstractGeometricPrimitiveType value) {
        return new JAXBElement<AbstractGeometricPrimitiveType>(_GeometricPrimitive_QNAME, AbstractGeometricPrimitiveType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "rowIndex")
    public JAXBElement<BigInteger> createRowIndex(BigInteger value) {
        return new JAXBElement<BigInteger>(_RowIndex_QNAME, BigInteger.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "ellipsoidName", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "name")
    public JAXBElement<CodeType> createEllipsoidName(CodeType value) {
        return new JAXBElement<CodeType>(_EllipsoidName_QNAME, CodeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "maximumOccurs")
    public JAXBElement<BigInteger> createMaximumOccurs(BigInteger value) {
        return new JAXBElement<BigInteger>(_MaximumOccurs_QNAME, BigInteger.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PointType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "Point", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "_GeometricPrimitive")
    public JAXBElement<PointType> createPoint(PointType value) {
        return new JAXBElement<PointType>(_Point_QNAME, PointType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Object }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "_Object")
    public JAXBElement<Object> createObject(Object value) {
        return new JAXBElement<Object>(_Object_QNAME, Object.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link List }{@code <}{@link String }{@code >}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "BooleanList")
    public JAXBElement<List<String>> createBooleanList(List<String> value) {
        return new JAXBElement<List<String>>(_BooleanList_QNAME, ((Class) List.class), null, ((List<String> ) value));
    }

   /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ObliqueCartesianCSType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "ObliqueCartesianCS", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "_CoordinateSystem")
    public JAXBElement<ObliqueCartesianCSType> createObliqueCartesianCS(ObliqueCartesianCSType value) {
        return new JAXBElement<ObliqueCartesianCSType>(_ObliqueCartesianCS_QNAME, ObliqueCartesianCSType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PolygonType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "Polygon", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "AbstractSurface")
    public JAXBElement<PolygonType> createPolygon(PolygonType value) {
        return new JAXBElement<PolygonType>(_Polygon_QNAME, PolygonType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "meridianName", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "name")
    public JAXBElement<CodeType> createMeridianName(CodeType value) {
        return new JAXBElement<CodeType>(_MeridianName_QNAME, CodeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CoordinateSystemAxisType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "CoordinateSystemAxis", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "Definition")
    public JAXBElement<CoordinateSystemAxisType> createCoordinateSystemAxis(CoordinateSystemAxisType value) {
        return new JAXBElement<CoordinateSystemAxisType>(_CoordinateSystemAxis_QNAME, CoordinateSystemAxisType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "groupName", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "name")
    public JAXBElement<CodeType> createGroupName(CodeType value) {
        return new JAXBElement<CodeType>(_GroupName_QNAME, CodeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "catalogSymbol")
    public JAXBElement<CodeType> createCatalogSymbol(CodeType value) {
        return new JAXBElement<CodeType>(_CatalogSymbol_QNAME, CodeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link List }{@code <}{@link String }{@code >}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "CountExtent")
    public JAXBElement<List<String>> createCountExtent(List<String> value) {
        return new JAXBElement<List<String>>(_CountExtent_QNAME, ((Class) List.class), null, ((List<String> ) value));
    }

   /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IdentifierType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "csID")
    public JAXBElement<IdentifierType> createCsID(IdentifierType value) {
        return new JAXBElement<IdentifierType>(_CsID_QNAME, IdentifierType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CoordinatesType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "tupleList")
    public JAXBElement<CoordinatesType> createTupleList(CoordinatesType value) {
        return new JAXBElement<CoordinatesType>(_TupleList_QNAME, CoordinatesType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PolygonType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "boundingPolygon")
    public JAXBElement<PolygonType> createBoundingPolygon(PolygonType value) {
        return new JAXBElement<PolygonType>(_BoundingPolygon_QNAME, PolygonType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CRSRefType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "crsRef")
    public JAXBElement<CRSRefType> createCrsRef(CRSRefType value) {
        return new JAXBElement<CRSRefType>(_CrsRef_QNAME, CRSRefType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "isSphere")
    public JAXBElement<String> createIsSphere(String value) {
        return new JAXBElement<String>(_IsSphere_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "booleanValue")
    public JAXBElement<Boolean> createBooleanValue(Boolean value) {
        return new JAXBElement<Boolean>(_BooleanValue_QNAME, Boolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractRingPropertyType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "interior")
    public JAXBElement<AbstractRingPropertyType> createInterior(AbstractRingPropertyType value) {
        return new JAXBElement<AbstractRingPropertyType>(_Interior_QNAME, AbstractRingPropertyType.class, null, value);
    }

   /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ObliqueCartesianCSRefType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "obliqueCartesianCSRef")
    public JAXBElement<ObliqueCartesianCSRefType> createObliqueCartesianCSRef(ObliqueCartesianCSRefType value) {
        return new JAXBElement<ObliqueCartesianCSRefType>(_ObliqueCartesianCSRef_QNAME, ObliqueCartesianCSRefType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "targetDimensions")
    public JAXBElement<BigInteger> createTargetDimensions(BigInteger value) {
        return new JAXBElement<BigInteger>(_TargetDimensions_QNAME, BigInteger.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractCoordinateSystemType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "_CoordinateSystem", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "Definition")
    public JAXBElement<AbstractCoordinateSystemType> createCoordinateSystem(AbstractCoordinateSystemType value) {
        return new JAXBElement<AbstractCoordinateSystemType>(_CoordinateSystem_QNAME, AbstractCoordinateSystemType.class, null, value);
    }

   /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IdentifierType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "parameterID")
    public JAXBElement<IdentifierType> createParameterID(IdentifierType value) {
        return new JAXBElement<IdentifierType>(_ParameterID_QNAME, IdentifierType.class, null, value);
    }

   /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Duration }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "duration")
    public JAXBElement<Duration> createDuration(Duration value) {
        return new JAXBElement<Duration>(_Duration_QNAME, Duration.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractTimeObjectType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "_TimeObject", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "AbstractGML")
    public JAXBElement<AbstractTimeObjectType> createTimeObject(AbstractTimeObjectType value) {
        return new JAXBElement<AbstractTimeObjectType>(_TimeObject_QNAME, AbstractTimeObjectType.class, null, value);
    }

   /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractGeometryType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "_Geometry", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "AbstractGML")
    public JAXBElement<AbstractGeometryType> createGeometry(AbstractGeometryType value) {
        return new JAXBElement<AbstractGeometryType>(_Geometry_QNAME, AbstractGeometryType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "srsName", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "name")
    public JAXBElement<CodeType> createSrsName(CodeType value) {
        return new JAXBElement<CodeType>(_SrsName_QNAME, CodeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "scope")
    public JAXBElement<String> createScope(String value) {
        return new JAXBElement<String>(_Scope_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractReferenceSystemType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "_CoordinateReferenceSystem", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "_CRS")
    public JAXBElement<AbstractReferenceSystemType> createCoordinateReferenceSystem(AbstractReferenceSystemType value) {
        return new JAXBElement<AbstractReferenceSystemType>(_CoordinateReferenceSystem_QNAME, AbstractReferenceSystemType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CoordinatesType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "coordinates")
    public JAXBElement<CoordinatesType> createCoordinates(CoordinatesType value) {
        return new JAXBElement<CoordinatesType>(_Coordinates_QNAME, CoordinatesType.class, null, value);
    }

    

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "LocationKeyWord")
    public JAXBElement<CodeType> createLocationKeyWord(CodeType value) {
        return new JAXBElement<CodeType>(_LocationKeyWord_QNAME, CodeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ImageCRSType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "ImageCRS", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "_CoordinateReferenceSystem")
    public JAXBElement<ImageCRSType> createImageCRS(ImageCRSType value) {
        return new JAXBElement<ImageCRSType>(_ImageCRS_QNAME, ImageCRSType.class, null, value);
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IdentifierType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "srsID")
    public JAXBElement<IdentifierType> createSrsID(IdentifierType value) {
        return new JAXBElement<IdentifierType>(_SrsID_QNAME, IdentifierType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractGeometryType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "_ImplicitGeometry", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "_Geometry")
    public JAXBElement<AbstractGeometryType> createImplicitGeometry(AbstractGeometryType value) {
        return new JAXBElement<AbstractGeometryType>(_ImplicitGeometry_QNAME, AbstractGeometryType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringOrRefType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "quantityType")
    public JAXBElement<StringOrRefType> createQuantityType(StringOrRefType value) {
        return new JAXBElement<StringOrRefType>(_QuantityType_QNAME, StringOrRefType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringOrRefType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "LocationString")
    public JAXBElement<StringOrRefType> createLocationString(StringOrRefType value) {
        return new JAXBElement<StringOrRefType>(_LocationString_QNAME, StringOrRefType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CoordinateSystemAxisRefType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "usesAxis")
    public JAXBElement<CoordinateSystemAxisRefType> createUsesAxis(CoordinateSystemAxisRefType value) {
        return new JAXBElement<CoordinateSystemAxisRefType>(_UsesAxis_QNAME, CoordinateSystemAxisRefType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CodeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "anchorPoint")
    public JAXBElement<CodeType> createAnchorPoint(CodeType value) {
        return new JAXBElement<CodeType>(_AnchorPoint_QNAME, CodeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link List }{@code <}{@link BigInteger }{@code >}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "integerValueList")
    public JAXBElement<List<BigInteger>> createIntegerValueList(List<BigInteger> value) {
        return new JAXBElement<List<BigInteger>>(_IntegerValueList_QNAME, ((Class) List.class), null, ((List<BigInteger> ) value));
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DefinitionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "Definition", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "AbstractGML")
    public JAXBElement<DefinitionType> createDefinition(DefinitionType value) {
        return new JAXBElement<DefinitionType>(_Definition_QNAME, DefinitionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractPositionalAccuracyType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "_positionalAccuracy")
    public JAXBElement<AbstractPositionalAccuracyType> createPositionalAccuracy(AbstractPositionalAccuracyType value) {
        return new JAXBElement<AbstractPositionalAccuracyType>(_PositionalAccuracy_QNAME, AbstractPositionalAccuracyType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IdentifierType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "groupID")
    public JAXBElement<IdentifierType> createGroupID(IdentifierType value) {
        return new JAXBElement<IdentifierType>(_GroupID_QNAME, IdentifierType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TimeInstantType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/gml", name = "TimeInstant", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "_TimeGeometricPrimitive")
    public JAXBElement<TimeInstantType> createTimeInstant(TimeInstantType value) {
        return new JAXBElement<TimeInstantType>(_TimeInstant_QNAME, TimeInstantType.class, null, value);
    }
}
