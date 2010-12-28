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

        public static final String META_17 =
        "<gmd:MD_Metadata xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:gco=\"http://www.isotc211.org/2005/gco\">" + '\n' +
        "<gmd:spatialRepresentationInfo>" + '\n' +
        "<gmd:MD_Georectified>" + '\n' +
        "    <gmd:numberOfDimensions>" + '\n' +
        "        <gco:Integer>2</gco:Integer>" + '\n' +
        "    </gmd:numberOfDimensions>" + '\n' +
        "    <gmd:axisDimensionProperties>" + '\n' +
        "        <gmd:MD_Dimension>" + '\n' +
        "            <gmd:dimensionName>" + '\n' +
        "                <gmd:MD_DimensionNameTypeCode codeList=\"#xpointer(//*[@gml:id='MD_DimensionNameTypeCode'])\" codeListValue=\"row\"/>" + '\n' +
        "            </gmd:dimensionName>" + '\n' +
        "            <gmd:dimensionSize>" + '\n' +
        "                <gco:Integer>1</gco:Integer>" + '\n' +
        "            </gmd:dimensionSize>" + '\n' +
        "        </gmd:MD_Dimension>" + '\n' +
        "    </gmd:axisDimensionProperties>" + '\n' +
        "    <gmd:cellGeometry>" + '\n' +
        "        <gmd:MD_CellGeometryCode codeList=\"#xpointer(//*[@gml:id='MD_CellGeometryCode'])\" codeListValue=\"point\"/>" + '\n' +
        "    </gmd:cellGeometry>" + '\n' +
        "    <gmd:transformationParameterAvailability>" + '\n' +
        "        <gco:Boolean>false</gco:Boolean>" + '\n' +
        "    </gmd:transformationParameterAvailability>" + '\n' +
        "    <gmd:checkPointAvailability>" + '\n' +
        "        <gco:Boolean>false</gco:Boolean>" + '\n' +
        "    </gmd:checkPointAvailability>" + '\n' +
        "    <gmd:cornerPoints>" + '\n' +
        "        <gml:Point gml:id=\"cornerPoints.1\" srsName=\"#epsg:2154\">" + '\n' +
        "            <gml:pos>857000 6526000</gml:pos>" + '\n' +
        "        </gml:Point>" + '\n' +
        "    </gmd:cornerPoints>" + '\n' +
        "    <gmd:cornerPoints>" + '\n' +
        "        <gml:Point gml:id=\"cornerPoints.2\" srsName=\"#epsg:2154\">" + '\n' +
        "            <gml:pos>858000 6526000</gml:pos>" + '\n' +
        "        </gml:Point>" + '\n' +
        "    </gmd:cornerPoints>" + '\n' +
       // "    <gmd:pointInPixel>" + '\n' +
       // "        <gmd:MD_PixelOrientationCode>center</gmd:MD_PixelOrientationCode>" + '\n' +
       // "    </gmd:pointInPixel>" + '\n' +
        "</gmd:MD_Georectified>" + '\n' +
        "</gmd:spatialRepresentationInfo>" + '\n' +
        "</gmd:MD_Metadata>";


        public static final String META_18 =
        "<gmd:MD_Metadata xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gml=\"http://www.opengis.net/gml\">" + '\n' +
        "   <gmd:identificationInfo>" + '\n' +
        "       <gmd:MD_DataIdentification>" + '\n' +
        "           <gmd:extent>" + '\n' +
        "               <gmd:EX_Extent>" + '\n' +
        "                   <gmd:geographicElement>" + '\n' +
        "                       <gmd:EX_BoundingPolygon>" + '\n' +
        "                           <gmd:polygon>" + '\n' +
        "                               <gml:Surface gml:id=\"id3142226\" srsName=\"http://eden.ign.fr/catalogue/cargene/20050620/crs/crs.xml#WGS84G\">" + '\n' +
        "                                  <gml:patches>" + '\n' +
        "                                    <gml:PolygonPatch>" + '\n' +
        "                                      <gml:exterior>" + '\n' +
        "                                        <gml:Ring>" + '\n' +
        "                                          <gml:curveMember>" + '\n' +
        "                                            <gml:Curve gml:id=\"curveid3142225\">" + '\n' +
        "                                              <gml:segments>" + '\n' +
        "                                                <gml:LineStringSegment>" + '\n' +
        "                                                  <gml:pos>1.0 1.0</gml:pos>" + '\n' +
        "                                                  <gml:pos>1.0 2.0</gml:pos>" + '\n' +
        "                                                </gml:LineStringSegment>" + '\n' +
        "                                               <gml:LineStringSegment>" + '\n' +
        "                                                  <gml:pos>1.0 2.0</gml:pos>" + '\n' +
        "                                                  <gml:pos>2.0 2.0</gml:pos>" + '\n' +
        "                                                </gml:LineStringSegment>" + '\n' +
        "                                                <gml:LineStringSegment>" + '\n' +
        "                                                  <gml:pos>2.0 2.0</gml:pos>" + '\n' +
        "                                                  <gml:pos>2.0 1.0</gml:pos>" + '\n' +
        "                                                </gml:LineStringSegment>" + '\n' +
        "                                                <gml:LineStringSegment>" + '\n' +
        "                                                  <gml:pos>2.0 1.0</gml:pos>" + '\n' +
        "                                                  <gml:pos>1.0 1.0</gml:pos>" + '\n' +
        "                                                </gml:LineStringSegment>" + '\n' +
        "                                              </gml:segments>" + '\n' +
        "                                            </gml:Curve>" + '\n' +
        "                                          </gml:curveMember>" + '\n' +
        "                                        </gml:Ring>" + '\n' +
        "                                      </gml:exterior>" + '\n' +
        "                                    </gml:PolygonPatch>" + '\n' +
        "                                  </gml:patches>" + '\n' +
        "                                </gml:Surface>" + '\n' +
        "                           </gmd:polygon>" + '\n' +
        "                       </gmd:EX_BoundingPolygon>" + '\n' +
        "                   </gmd:geographicElement>" + '\n' +
        "               </gmd:EX_Extent>" + '\n' +
        "           </gmd:extent>" + '\n' +
        "       </gmd:MD_DataIdentification>" + '\n' +
        "   </gmd:identificationInfo>" + '\n' +
        "</gmd:MD_Metadata>";

}
