//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.06.10 at 06:06:35 PM CEST 
//


package net.seagis.gml.v212;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *         This abstract base type just makes the boundedBy element mandatory 
 *         for a feature collection.
 *       
 * 
 * <p>Java class for AbstractFeatureCollectionBaseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AbstractFeatureCollectionBaseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.opengis.net/gml}AbstractFeatureType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/gml}description" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/gml}name" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/gml}boundedBy"/>
 *       &lt;/sequence>
 *       &lt;attribute name="fid" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractFeatureCollectionBaseType")
@XmlSeeAlso({
    AbstractFeatureCollectionType.class
})
public abstract class AbstractFeatureCollectionBaseType
    extends AbstractFeatureType
{


}
