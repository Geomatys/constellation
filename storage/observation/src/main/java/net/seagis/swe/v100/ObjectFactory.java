/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
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

package net.seagis.swe.v100;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 *
 * @version $Id:
 * @author Guilhem Legal
 */
@XmlRegistry
public class ObjectFactory {
    
    private final static QName _AbstractDataRecord_QNAME    = new QName("http://www.opengis.net/swe/1.0.1", "AbstractDataRecord");
    private final static QName _SimpleDataRecord_QNAME      = new QName("http://www.opengis.net/swe/1.0.1", "SimpleDataRecord");
    private final static QName _Item_QNAME                  = new QName("http://www.opengis.net/swe/1.0.1", "Item");
    private final static QName _CompositePhenomenon_QNAME   = new QName("http://www.opengis.net/swe/1.0.1", "CompositePhenomenon");
    private final static QName _Phenomenon_QNAME            = new QName("http://www.opengis.net/swe/1.0.1", "Phenomenon");
    private final static QName _CompoundPhenomenon_QNAME    = new QName("http://www.opengis.net/swe/1.0.1", "CompoundPhenomenon");
    private final static QName _TextBlock_QNAME             = new QName("http://www.opengis.net/swe/1.0.1", "TextBlock");
    private final static QName _Encoding_QNAME              = new QName("http://www.opengis.net/swe/1.0.1", "Encoding");
    private final static QName _AbstractDataComponent_QNAME = new QName("http://www.opengis.net/swe/1.0.1", "AbstractDataComponent");
    private final static QName _Time_QNAME                  = new QName("http://www.opengis.net/swe/1.0.1", "Time");
    private final static QName _Quantity_QNAME              = new QName("http://www.opengis.net/swe/1.0.1", "Quantity");
    private final static QName _Boolean_QNAME               = new QName("http://www.opengis.net/swe/1.0.1", "Boolean");
    private final static QName _DataArray_QNAME             = new QName("http://www.opengis.net/swe/1.0.1", "DataArray");
    
    /**
     *
     */
    public ObjectFactory() {
    }
    
    /**
     * Create an instance of {@link Boolean }
     * 
     */
    public BooleanType createBooleanType() {
        return new BooleanType();
    }

    
    /**
     * Create an instance of {@link Time }
     * 
     */
    public TimeType createTimeType() {
        return new TimeType();
    }

    /**
     * Create an instance of {@link Quantity }
     * 
     */
    public QuantityType createQuantityType() {
        return new QuantityType();
    }
    
    /**
     * Create an instance of {@link UomPropertyType }
     * 
     */
    public UomPropertyType createUomPropertyType() {
        return new UomPropertyType();
    }

    /**
     * Create an instance of {@link AbstractDataComponentEntry }
     * 
     */
    public AbstractDataComponentEntry createAbstractDataComponentEntry() {
        return new AbstractDataComponentEntry();
    }
    
    /**
     * Create an instance of {@link AbstractDataComponentEntry }
     * 
     */
    public AbstractDataRecordEntry createAbstractDataRecordEntry() {
        return new AbstractDataRecordEntry();
    }
    
    /**
     * Create an instance of {@link AbstractEncodingEntry }
     * 
     */
    public AbstractEncodingEntry createAbstractEncodingEntry() {
        return new AbstractEncodingEntry();
    }
    
    /**
     * Create an instance of {@link AnyResultEntry }
     * 
     */
    public AnyResultEntry createAnyResultEntry() {
        return new AnyResultEntry();
    }
    
    /**
     * Create an instance of {@link DataBlockDefinitionEntry }
     * 
     */
    public DataBlockDefinitionEntry createDataBlockDefinitionEntry() {
        return new DataBlockDefinitionEntry();
    }
    
    /**
     * Create an instance of {@link TextBlockEntry }
     * 
     */
    public TextBlockEntry createTextBlockEntry() {
        return new TextBlockEntry();
    }
    
     /**
     * Create an instance of {@link SimpleDataRecordEntry }
     * 
     */
    public SimpleDataRecordEntry createSimpleDataRecordEntry() {
        return new SimpleDataRecordEntry();
    }
    
    /**
     * Create an instance of {@link AnyScalarEntry }
     * 
     */
    public AnyScalarPropertyType createAnyScalarEntry() {
        return new AnyScalarPropertyType();
    }
    
    /**
     * Create an instance of {@link TimeGeometricPrimitivePropertyType }
     * 
     */
    public TimeGeometricPrimitivePropertyType createTimeGeometricPrimitivePropertyType() {
        return new TimeGeometricPrimitivePropertyType();
    }
     
    /**
     * Create an instance of {@link CompositePhenomenonEntry }
     * 
     */
    public CompositePhenomenonEntry createCompositePhenomenonEntry() {
        return new CompositePhenomenonEntry();
    }
    
     /**
     * Create an instance of {@link CompositePhenomenonEntry }
     * 
     */
    public PhenomenonEntry createPhenomenonEntry() {
        return new PhenomenonEntry();
    }
    
    /**
     * Create an instance of {@link DataArrayPropertyType }
     * 
     */
    public DataArrayPropertyType createDataArrayPropertyType() {
        return new DataArrayPropertyType();
    }
    
    /**
     * Create an instance of {@link DataArrayPropertyType }
     * 
     */
    public DataArrayEntry createDataArrayEntry() {
        return new DataArrayEntry();
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CompoundPhenomenonType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/swe/1.0.1", name = "CompoundPhenomenon", substitutionHeadNamespace = "http://www.opengis.net/swe/1.0.1", substitutionHeadName = "Phenomenon")
    public JAXBElement<CompoundPhenomenonEntry> createCompoundPhenomenon(CompoundPhenomenonEntry value) {
        return new JAXBElement<CompoundPhenomenonEntry>(_CompoundPhenomenon_QNAME, CompoundPhenomenonEntry.class, null, value);
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PhenomenonType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/swe/1.0.1", name = "Phenomenon", substitutionHeadNamespace = "http://www.opengis.net/gml", substitutionHeadName = "Definition")
    public JAXBElement<PhenomenonEntry> createPhenomenon(PhenomenonEntry value) {
        return new JAXBElement<PhenomenonEntry>(_Phenomenon_QNAME, PhenomenonEntry.class, null, value);
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CompositePhenomenonType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/swe/1.0.1", name = "CompositePhenomenon", substitutionHeadNamespace = "http://www.opengis.net/swe/1.0.1", substitutionHeadName = "CompoundPhenomenon")
    public JAXBElement<CompositePhenomenonEntry> createCompositePhenomenon(CompositePhenomenonEntry value) {
        return new JAXBElement<CompositePhenomenonEntry>(_CompositePhenomenon_QNAME, CompositePhenomenonEntry.class, null, value);
    }
    
    /**
     * 
     * 
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleDataRecordType }{@code >}}
     */
     
    @XmlElementDecl(namespace = "http://www.opengis.net/swe/1.0.1", name = "SimpleDataRecord", substitutionHeadNamespace = "http://www.opengis.net/swe/1.0.1", substitutionHeadName = "AbstractDataRecord")
    public JAXBElement<SimpleDataRecordEntry> createSimpleDataRecord(SimpleDataRecordEntry value) {
        return new JAXBElement<SimpleDataRecordEntry>(_SimpleDataRecord_QNAME, SimpleDataRecordEntry.class, null, value);
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AbstractDataRecordType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/swe/1.0.1", name = "AbstractDataRecord")
    public JAXBElement<AbstractDataRecordEntry> createAbstractDataRecord(AbstractDataRecordEntry value) {
        return new JAXBElement<AbstractDataRecordEntry>(_AbstractDataRecord_QNAME, AbstractDataRecordEntry.class, null, value);
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Object }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/swe/1.0.1", name = "Item")
    public JAXBElement<Object> createItem(Object value) {
        return new JAXBElement<Object>(_Item_QNAME, Object.class, null, value);
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Object }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/swe/1.0.1", name = "TextBlock", substitutionHeadNamespace = "http://www.opengis.net/swe/1.0.1", substitutionHeadName = "Encoding")
    public JAXBElement<TextBlockEntry> createTextBlock(TextBlockEntry value) {
        return new JAXBElement<TextBlockEntry>(_TextBlock_QNAME, TextBlockEntry.class, null, value);
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Object }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/swe/1.0.1", name = "Encoding")
    public JAXBElement<AbstractEncodingEntry> createAbstractEncoding(AbstractEncodingEntry value) {
        return new JAXBElement<AbstractEncodingEntry>(_Encoding_QNAME, AbstractEncodingEntry.class, null, value);
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Object }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/swe/1.0.1", name = "AbstractDataComponent")
    public JAXBElement<AbstractDataComponentEntry> createAbstractDataComponent(AbstractDataComponentEntry value) {
        return new JAXBElement<AbstractDataComponentEntry>(_AbstractDataComponent_QNAME, AbstractDataComponentEntry.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Object }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/swe/1.0.1", name = "Time", substitutionHeadNamespace = "http://www.opengis.net/swe/1.0.1", substitutionHeadName = "AbstractDataComponent")
    public JAXBElement<TimeType> createTime(TimeType value) {
        return new JAXBElement<TimeType>(_Time_QNAME, TimeType.class, null, value);
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Object }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/swe/1.0.1", name = "Quantity", substitutionHeadNamespace = "http://www.opengis.net/swe/1.0.1", substitutionHeadName = "AbstractDataComponent")
    public JAXBElement<QuantityType> createQuantity(QuantityType value) {
        return new JAXBElement<QuantityType>(_Quantity_QNAME, QuantityType.class, null, value);
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Object }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/swe/1.0.1", name = "Boolean", substitutionHeadNamespace = "http://www.opengis.net/swe/1.0.1", substitutionHeadName = "AbstractDataComponent")
    public JAXBElement<BooleanType> createBoolean(BooleanType value) {
        return new JAXBElement<BooleanType>(_Boolean_QNAME, BooleanType.class, null, value);
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Object }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.opengis.net/swe/1.0.1", name = "DataArray")
    public JAXBElement<DataArrayEntry> createDataArray(DataArrayEntry value) {
        return new JAXBElement<DataArrayEntry>(_DataArray_QNAME, DataArrayEntry.class, null, value);
    }
    


}
