//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.3 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.06.10 at 06:06:35 PM CEST 
//


package net.seagis.gml.v212;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *         A MultiLineString is defined by one or more LineStrings, referenced 
 *         through lineStringMember elements.
 *       
 * 
 * <p>Java class for MultiLineStringType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MultiLineStringType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.opengis.net/gml}GeometryCollectionType">
 *       &lt;sequence maxOccurs="unbounded">
 *         &lt;element ref="{http://www.opengis.net/gml}lineStringMember"/>
 *       &lt;/sequence>
 *       &lt;attribute name="gid" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *       &lt;attribute name="srsName" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MultiLineStringType")
public class MultiLineStringType
    extends GeometryCollectionType
{


}
