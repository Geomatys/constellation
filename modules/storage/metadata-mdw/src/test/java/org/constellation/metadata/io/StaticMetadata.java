/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.constellation.metadata.io;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import org.apache.sis.util.logging.Logging;
import org.geotoolkit.xml.AnchoredMarshallerPool;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class StaticMetadata {

    public static final String META_11 =
        "<gmd:MD_Metadata xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:gco=\"http://www.isotc211.org/2005/gco\">" + '\n' +
        "   <gmd:fileIdentifier>" + '\n' +
        "       <gco:CharacterString>af24f70a-818c-4da1-9afb-1fc1e0058760</gco:CharacterString>" + '\n' +
        "   </gmd:fileIdentifier>" + '\n' +
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
        "<gmd:MD_Metadata xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:gco=\"http://www.isotc211.org/2005/gco\">" + '\n' +
        "   <gmd:fileIdentifier>" + '\n' +
        "       <gco:CharacterString>1c7d52ac-66c5-449b-a88b-8a0feeccb5fa</gco:CharacterString>" + '\n' +
        "   </gmd:fileIdentifier>" + '\n' +
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
        "<gmd:MD_Metadata xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:gco=\"http://www.isotc211.org/2005/gco\">" + '\n' +
        "   <gmd:fileIdentifier>" + '\n' +
        "       <gco:CharacterString>4c017cc5-3e0e-49d5-9f68-549943247e7e</gco:CharacterString>" + '\n' +
        "   </gmd:fileIdentifier>" + '\n' +
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
        "<gmd:MD_Metadata xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:gco=\"http://www.isotc211.org/2005/gco\">" + '\n' +
        "   <gmd:fileIdentifier>" + '\n' +
        "       <gco:CharacterString>4c017cc5-3e0e-49d5-9f68-549943247e89</gco:CharacterString>" + '\n' +
        "   </gmd:fileIdentifier>" + '\n' +
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
        "<gmd:MD_Metadata xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:gco=\"http://www.isotc211.org/2005/gco\">" + '\n' +
        "   <gmd:fileIdentifier>" + '\n' +
        "       <gco:CharacterString>484fc4d9-8d11-48a5-a386-65c19398f7c3</gco:CharacterString>" + '\n' +
        "   </gmd:fileIdentifier>" + '\n' +
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
        "<gmd:MD_Metadata xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:gco=\"http://www.isotc211.org/2005/gco\">" + '\n' +
        "   <gmd:fileIdentifier>" + '\n' +
        "       <gco:CharacterString>484fc4d9-8d11-48a5-a386-65c19398f7k7</gco:CharacterString>" + '\n' +
        "   </gmd:fileIdentifier>" + '\n' +
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
        "<gmd:fileIdentifier>" + '\n' +
        "    <gco:CharacterString>81a25c84-2bb0-4727-8f36-4a296e1e7b57</gco:CharacterString>" + '\n' +
        "</gmd:fileIdentifier>" + '\n' +
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
        "    <gmd:pointInPixel>" + '\n' +
        "        <gmd:MD_PixelOrientationCode>center</gmd:MD_PixelOrientationCode>" + '\n' +
       "    </gmd:pointInPixel>" + '\n' +
        "</gmd:MD_Georectified>" + '\n' +
        "</gmd:spatialRepresentationInfo>" + '\n' +
        "</gmd:MD_Metadata>";


        public static final String META_18 =
        "<gmd:MD_Metadata xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:gco=\"http://www.isotc211.org/2005/gco\">" + '\n' +
        "   <gmd:fileIdentifier>" + '\n' +
        "       <gco:CharacterString>28644bf0-5d9d-4ebd-bef0-f2b0b2067b26</gco:CharacterString>" + '\n' +
        "   </gmd:fileIdentifier>" + '\n' +
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

        public static final String META_19 =
        "<gmd:MD_Metadata xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:gco=\"http://www.isotc211.org/2005/gco\">" + '\n' +
        "   <gmd:fileIdentifier>" + '\n' +
        "       <gco:CharacterString>937491cd-4bc4-43e4-9509-f6cc606f906e</gco:CharacterString>" + '\n' +
        "   </gmd:fileIdentifier>" + '\n' +
        "   <gmd:identificationInfo>" + '\n' +
        "       <gmd:MD_DataIdentification>" + '\n' +
        "           <gmd:extent>" + '\n' +
        "               <gmd:EX_Extent>" + '\n' +
        "                   <gmd:geographicElement>" + '\n' +
        "                       <gmd:EX_BoundingPolygon>" + '\n' +
        "                           <gmd:polygon>" + '\n' +
        "                               <gml:Polygon srsName=\"EPSG:4326\">" + '\n' +
        "                                   <gml:exterior>" + '\n' +
        "                                       <gml:Ring>" + '\n' +
        "                                          <gml:curveMember>" + '\n' +
        "                                            <gml:Curve gml:id=\"curveid3142226\">" + '\n' +
        "                                              <gml:segments>" + '\n' +
        "                                                <gml:LineStringSegment>" + '\n' +
        "                                                  <gml:pos>1.0 1.0</gml:pos>" + '\n' +
        "                                                  <gml:pos>1.0 2.0</gml:pos>" + '\n' +
        "                                                </gml:LineStringSegment>" + '\n' +
        "                                               <gml:LineStringSegment>" + '\n' +
        "                                                  <gml:pos>1.0 2.0</gml:pos>" + '\n' +
        "                                                  <gml:pos>2.0 2.0</gml:pos>" + '\n' +
        "                                                </gml:LineStringSegment>" + '\n' +
        "                                              </gml:segments>" + '\n' +
        "                                            </gml:Curve>" + '\n' +
        "                                          </gml:curveMember>" + '\n' +
        "                                        </gml:Ring>" + '\n' +
        "                                     </gml:exterior>" + '\n' +
        "                                </gml:Polygon>" + '\n' +
        "                           </gmd:polygon>" + '\n' +
        "                       </gmd:EX_BoundingPolygon>" + '\n' +
        "                   </gmd:geographicElement>" + '\n' +
        "               </gmd:EX_Extent>" + '\n' +
        "           </gmd:extent>" + '\n' +
        "       </gmd:MD_DataIdentification>" + '\n' +
        "   </gmd:identificationInfo>" + '\n' +
        "</gmd:MD_Metadata>";

        public static final String META_20 =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + '\n' +
        "<gmd:MD_Metadata xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:srv=\"http://www.isotc211.org/2005/srv\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:gco=\"http://www.isotc211.org/2005/gco\">" + '\n' +
        "   <gmd:fileIdentifier>" + '\n' +
        "       <gco:CharacterString>666-999-666</gco:CharacterString>" + '\n' +
        "   </gmd:fileIdentifier>" + '\n' +
        "   <gmd:identificationInfo>" + '\n' +
        "       <srv:SV_ServiceIdentification>" + '\n' +
        "           <srv:operatesOn xlink:href=\"http://test.com\">" + '\n' +
        "               <gmd:MD_DataIdentification>" + '\n' +
        "                   <gmd:abstract>" + '\n' +
        "                       <gco:CharacterString>not empty</gco:CharacterString>" + '\n' +
        "                   </gmd:abstract>" + '\n' +
        "               </gmd:MD_DataIdentification>" + '\n' +
        "           </srv:operatesOn>" + '\n' +
        "       </srv:SV_ServiceIdentification>" + '\n' +
        "    </gmd:identificationInfo>" + '\n' +
        "</gmd:MD_Metadata>";

        public static final String META_21 =
        "<gmd:MD_Metadata xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:srv=\"http://www.isotc211.org/2005/srv\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:gco=\"http://www.isotc211.org/2005/gco\">" + '\n' +
        "   <gmd:fileIdentifier>" + '\n' +
        "       <gco:CharacterString>999-666-999</gco:CharacterString>" + '\n' +
        "   </gmd:fileIdentifier>" + '\n' +
        "   <gmd:identificationInfo>" + '\n' +
        "       <srv:SV_ServiceIdentification>" + '\n' +
        "           <srv:operatesOn xlink:href=\"http://test2.com\"/>" + '\n' +
        "       </srv:SV_ServiceIdentification>" + '\n' +
        "    </gmd:identificationInfo>" + '\n' +
        "</gmd:MD_Metadata>";

        public static final String META_22 =
        "<gmd:MD_Metadata xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:ns11=\"http://www.opengis.net/gml\" xmlns:srv=\"http://www.isotc211.org/2005/srv\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:gco=\"http://www.isotc211.org/2005/gco\">" + '\n' +
        "   <gmd:fileIdentifier>" + '\n' +
        "       <gco:CharacterString>777-444-852</gco:CharacterString>" + '\n' +
        "   </gmd:fileIdentifier>" + '\n' +
        "   <gmd:identificationInfo>" + '\n' +
        "       <gmd:MD_DataIdentification>" + '\n' +
        "           <gmd:extent>\n" +
        "                <gmd:EX_Extent>\n" +
        "                    <gmd:temporalElement>\n" +
        "                        <gmd:EX_TemporalExtent>\n" +
        "                            <gmd:extent>\n" +
        "                                <ns11:TimePeriod ns11:id=\"testID\">\n" +
        "                                    <ns11:beginPosition>2010-01-27T14:26:10+01:00</ns11:beginPosition>\n" +
        "                                    <ns11:endPosition>2010-08-27T15:26:10+02:00</ns11:endPosition>\n" +
        "                                </ns11:TimePeriod>\n" +
        "                            </gmd:extent>\n" +
        "                        </gmd:EX_TemporalExtent>\n" +
        "                    </gmd:temporalElement>\n" +
        "                </gmd:EX_Extent>\n" +
        "            </gmd:extent>" + '\n' +
        "       </gmd:MD_DataIdentification>" + '\n' +
        "    </gmd:identificationInfo>" + '\n' +
        "</gmd:MD_Metadata>";


        public static void fillPoolAnchor(AnchoredMarshallerPool pool) {
        try {
            pool.addAnchor("Common Data Index record", new URI("SDN:L231:3:CDI"));
            pool.addAnchor("France", new URI("SDN:C320:2:FR"));
            pool.addAnchor("EPSG:4326", new URI("SDN:L101:2:4326"));
            pool.addAnchor("2", new URI("SDN:C371:1:2"));
            pool.addAnchor("35", new URI("SDN:C371:1:35"));
            pool.addAnchor("Transmittance and attenuance of the water column", new URI("SDN:P021:35:ATTN"));
            pool.addAnchor("Electrical conductivity of the water column", new URI("SDN:P021:35:CNDC"));
            pool.addAnchor("Dissolved oxygen parameters in the water column", new URI("SDN:P021:35:DOXY"));
            pool.addAnchor("Light extinction and diffusion coefficients", new URI("SDN:P021:35:EXCO"));
            pool.addAnchor("Dissolved noble gas concentration parameters in the water column", new URI("SDN:P021:35:HEXC"));
            pool.addAnchor("Optical backscatter", new URI("SDN:P021:35:OPBS"));
            pool.addAnchor("Salinity of the water column", new URI("SDN:P021:35:PSAL"));
            pool.addAnchor("Dissolved concentration parameters for 'other' gases in the water column", new URI("SDN:P021:35:SCOX"));
            pool.addAnchor("Temperature of the water column", new URI("SDN:P021:35:TEMP"));
            pool.addAnchor("Visible waveband radiance and irradiance measurements in the atmosphere", new URI("SDN:P021:35:VSRA"));
            pool.addAnchor("Visible waveband radiance and irradiance measurements in the water column", new URI("SDN:P021:35:VSRW"));
            pool.addAnchor("MEDATLAS ASCII", new URI("SDN:L241:1:MEDATLAS"));
        } catch (URISyntaxException ex) {
            Logging.getLogger("StaticMetadata").log(Level.SEVERE, null, ex);
        } catch (IllegalStateException ex) {
            // this exception happen when we try to put 2 twice the same anchor.
            // for this test we call many times this method in a static instance (CSWMarshallerPool)
            // so for now we do bnothing here
            // TODO find a way to call this only one time in the CSW test
        }
    }
}
