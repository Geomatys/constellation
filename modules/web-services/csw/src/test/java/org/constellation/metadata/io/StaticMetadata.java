/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.constellation.metadata.io;

/**
 *
 * @author guilhem
 */
public class StaticMetadata {

    public static final String META_11 =
        "<gmd:MD_Metadata xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gml=\"http://www.opengis.net/gml\">" + '\n' +
        "   <gmd:identificationInfo>" + '\n' +
        "       <gmd:MD_DataIdentification>" + '\n' +
        "           <gmd:extent>" + '\n' +
        "               <gmd:EX_Extent>" + '\n' +
        "                   <gmd:geographicElement>" + '\n' +
        "                       <gmd:EX_BoundingPolygon>" + '\n' +
        "                           <gmd:polygon>" + '\n' +
        "                               <gml:Point srsName=\"EPSG:3395\">" + '\n' +
        "                                   <gml:coordinates>12.3 14.5</gml:coordinates>" + '\n' +
        "                               </gml:Point>" + '\n' +
        "                           </gmd:polygon>" + '\n' +
        "                       </gmd:EX_BoundingPolygon>" + '\n' +
        "                   </gmd:geographicElement>" + '\n' +
        "               </gmd:EX_Extent>" + '\n' +
        "           </gmd:extent>" + '\n' +
        "       </gmd:MD_DataIdentification>" + '\n' +
        "   </gmd:identificationInfo>" + '\n' +
        "</gmd:MD_Metadata>";

     public static final String META_12 =
        "<gmd:MD_Metadata xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gml=\"http://www.opengis.net/gml\">" + '\n' +
        "   <gmd:identificationInfo>" + '\n' +
        "       <gmd:MD_DataIdentification>" + '\n' +
        "           <gmd:extent>" + '\n' +
        "               <gmd:EX_Extent>" + '\n' +
        "                   <gmd:geographicElement>" + '\n' +
        "                       <gmd:EX_BoundingPolygon>" + '\n' +
        "                           <gmd:polygon>" + '\n' +
        "                               <gml:Point srsName=\"EPSG:3395\" gml:id=\"pt1\">" + '\n' +
        "                                   <gml:pos srsName=\"EPSG:3395\" srsDimension=\"2\">2.0 2.3 2.4</gml:pos>" + '\n' +
        "                               </gml:Point>" + '\n' +
        "                           </gmd:polygon>" + '\n' +
        "                       </gmd:EX_BoundingPolygon>" + '\n' +
        "                   </gmd:geographicElement>" + '\n' +
        "               </gmd:EX_Extent>" + '\n' +
        "           </gmd:extent>" + '\n' +
        "       </gmd:MD_DataIdentification>" + '\n' +
        "   </gmd:identificationInfo>" + '\n' +
        "</gmd:MD_Metadata>";

      public static final String META_13 =
        "<gmd:MD_Metadata xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gml=\"http://www.opengis.net/gml\">" + '\n' +
        "   <gmd:identificationInfo>" + '\n' +
        "       <gmd:MD_DataIdentification>" + '\n' +
        "           <gmd:extent>" + '\n' +
        "               <gmd:EX_Extent>" + '\n' +
        "                   <gmd:geographicElement>" + '\n' +
        "                       <gmd:EX_BoundingPolygon>" + '\n' +
        "                           <gmd:polygon>" + '\n' +
        "                               <gml:Curve srsName=\"EPSG:3395\" gml:id=\"c1\">" + '\n' +
        "                                   <gml:segments>" + '\n' +
        "                                       <gml:LineStringSegment>" + '\n' +
        "                                            <gml:posList>135510 2338590 2338670</gml:posList>" + '\n' +
        "                                       </gml:LineStringSegment>" + '\n' +
        "                                   </gml:segments>" + '\n' +
        "                              </gml:Curve>" + '\n' +
        "                           </gmd:polygon>" + '\n' +
        "                       </gmd:EX_BoundingPolygon>" + '\n' +
        "                   </gmd:geographicElement>" + '\n' +
        "               </gmd:EX_Extent>" + '\n' +
        "           </gmd:extent>" + '\n' +
        "       </gmd:MD_DataIdentification>" + '\n' +
        "   </gmd:identificationInfo>" + '\n' +
        "</gmd:MD_Metadata>";

      public static final String META_14 =
        "<gmd:MD_Metadata xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gml=\"http://www.opengis.net/gml\">" + '\n' +
        "   <gmd:identificationInfo>" + '\n' +
        "       <gmd:MD_DataIdentification>" + '\n' +
        "           <gmd:extent>" + '\n' +
        "               <gmd:EX_Extent>" + '\n' +
        "                   <gmd:geographicElement>" + '\n' +
        "                       <gmd:EX_BoundingPolygon>" + '\n' +
        "                           <gmd:polygon>" + '\n' +
        "                               <gml:LineString>" + '\n' +
        "                                   <gml:coordinates>12.3 14.5</gml:coordinates>" + '\n' +
        "                               </gml:LineString>" + '\n' +
        "                           </gmd:polygon>" + '\n' +
        "                       </gmd:EX_BoundingPolygon>" + '\n' +
        "                   </gmd:geographicElement>" + '\n' +
        "               </gmd:EX_Extent>" + '\n' +
        "           </gmd:extent>" + '\n' +
        "       </gmd:MD_DataIdentification>" + '\n' +
        "   </gmd:identificationInfo>" + '\n' +
        "</gmd:MD_Metadata>";
      
      public static final String META_15 =
        "<gmd:MD_Metadata xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gml=\"http://www.opengis.net/gml\">" + '\n' +
        "   <gmd:identificationInfo>" + '\n' +
        "       <gmd:MD_DataIdentification>" + '\n' +
        "           <gmd:extent>" + '\n' +
        "               <gmd:EX_Extent>" + '\n' +
        "                   <gmd:geographicElement>" + '\n' +
        "                       <gmd:EX_BoundingPolygon>" + '\n' +
        "                           <gmd:polygon>" + '\n' +
        "                               <gml:MultiLineString>" + '\n' +
        "                                   <gml:lineStringMember>" + '\n' +
        "                                       <gml:LineString>" + '\n' +
        "                                           <gml:coordinates>12.3 14.5</gml:coordinates>" + '\n' +
        "                                       </gml:LineString>" + '\n' +
        "                                   </gml:lineStringMember>" + '\n' +
        "                                   <gml:lineStringMember>" + '\n' +
        "                                       <gml:LineString>" + '\n' +
        "                                           <gml:coordinates>45.6 69.3</gml:coordinates>" + '\n' +
        "                                       </gml:LineString>" + '\n' +
        "                                   </gml:lineStringMember>" + '\n' +
        "                               </gml:MultiLineString>" + '\n' +
        "                           </gmd:polygon>" + '\n' +
        "                       </gmd:EX_BoundingPolygon>" + '\n' +
        "                   </gmd:geographicElement>" + '\n' +
        "               </gmd:EX_Extent>" + '\n' +
        "           </gmd:extent>" + '\n' +
        "       </gmd:MD_DataIdentification>" + '\n' +
        "   </gmd:identificationInfo>" + '\n' +
        "</gmd:MD_Metadata>";

       public static final String META_16 =
        "<gmd:MD_Metadata xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gml=\"http://www.opengis.net/gml\">" + '\n' +
        "   <gmd:identificationInfo>" + '\n' +
        "       <gmd:MD_DataIdentification>" + '\n' +
        "           <gmd:extent>" + '\n' +
        "               <gmd:EX_Extent>" + '\n' +
        "                   <gmd:geographicElement>" + '\n' +
        "                       <gmd:EX_BoundingPolygon>" + '\n' +
        "                           <gmd:polygon>" + '\n' +
        "                               <gml:MultiPoint srsName=\"EPSG:3395\">" + '\n' +
        "                                   <gml:pointMember>" + '\n' +
        "                                       <gml:Point srsName=\"EPSG:3395\" gml:id=\"pt1\">" + '\n' +
        "                                           <gml:pos srsName=\"EPSG:3395\" srsDimension=\"2\">2.0 2.3 2.4</gml:pos>" + '\n' +
        "                                       </gml:Point>" + '\n' +
        "                                   </gml:pointMember>" + '\n' +
        "                                   <gml:pointMember>" + '\n' +
        "                                       <gml:Point srsName=\"EPSG:3395\" gml:id=\"pt2\">" + '\n' +
        "                                           <gml:pos srsName=\"EPSG:3395\" srsDimension=\"2\">6.0 6.3 6.4</gml:pos>" + '\n' +
        "                                       </gml:Point>" + '\n' +
        "                                   </gml:pointMember>" + '\n' +
        "                               </gml:MultiPoint>" + '\n' +
        "                           </gmd:polygon>" + '\n' +
        "                       </gmd:EX_BoundingPolygon>" + '\n' +
        "                   </gmd:geographicElement>" + '\n' +
        "               </gmd:EX_Extent>" + '\n' +
        "           </gmd:extent>" + '\n' +
        "       </gmd:MD_DataIdentification>" + '\n' +
        "   </gmd:identificationInfo>" + '\n' +
        "</gmd:MD_Metadata>";


}
