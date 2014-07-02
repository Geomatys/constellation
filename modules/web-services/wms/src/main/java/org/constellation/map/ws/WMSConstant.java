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

package org.constellation.map.ws;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.JAXBElement;
import net.jcip.annotations.Immutable;
import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import org.apache.sis.util.iso.DefaultNameFactory;
import org.constellation.dto.AccessConstraint;
import org.constellation.dto.Contact;
import org.constellation.dto.Details;
import org.geotoolkit.inspire.xml.vs.ExtendedCapabilitiesType;
import org.geotoolkit.service.ServiceTypeImpl;
import org.geotoolkit.wms.xml.AbstractCapability;
import org.geotoolkit.wms.xml.AbstractContactAddress;
import org.geotoolkit.wms.xml.AbstractContactInformation;
import org.geotoolkit.wms.xml.AbstractContactPersonPrimary;
import org.geotoolkit.wms.xml.AbstractKeywordList;
import org.geotoolkit.wms.xml.AbstractOnlineResource;
import org.geotoolkit.wms.xml.AbstractService;
import org.geotoolkit.wms.xml.AbstractWMSCapabilities;
import org.geotoolkit.wms.xml.WmsXmlFactory;
import org.geotoolkit.wms.xml.v111.DescribeLayer;
import org.geotoolkit.wms.xml.v111.GetCapabilities;
import org.geotoolkit.wms.xml.v111.GetFeatureInfo;
import org.geotoolkit.wms.xml.v111.GetLegendGraphic;
import org.geotoolkit.wms.xml.v111.GetMap;
import org.geotoolkit.wms.xml.v130.DCPType;
import org.geotoolkit.wms.xml.v130.Get;
import org.geotoolkit.wms.xml.v130.HTTP;
import org.geotoolkit.wms.xml.v130.ObjectFactory;
import org.geotoolkit.wms.xml.v130.OnlineResource;
import org.geotoolkit.wms.xml.v130.OperationType;
import org.geotoolkit.wms.xml.v130.Post;
import org.geotoolkit.wms.xml.v130.Request;
import org.opengis.metadata.maintenance.ScopeCode;
import org.opengis.util.LocalName;
import org.opengis.util.NameFactory;

/**
 *  WMS Constants
 *
 * @author Guilhem Legal (Geomatys)
 */
@Immutable
public final class WMSConstant {

    /**
     * Request parameters.
     */
    public static final String GETMAP           = "GetMap";
    public static final String MAP              = "Map";
    public static final String GETFEATUREINFO   = "GetFeatureInfo";
    public static final String GETCAPABILITIES  = "GetCapabilities";
    public static final String DESCRIBELAYER    = "DescribeLayer";
    public static final String GETLEGENDGRAPHIC = "GetLegendGraphic";
    public static final String GETORIGFILE      = "GetOrigFile";
    
    /**
     * WMS Query service
     */
    public static final String WMS_SERVICE = "WMS";

    /**
     * For backward compatibility with WMS 1.0.0, the request can be done with
     * a value {@code capabilities}.
     */
    public static final String CAPABILITIES     = "Capabilities";

    /** Parameter used in getMap, getLegendGraphic, getCapabilities */
    public static final String KEY_FORMAT = "FORMAT";
    /** Parameter used in getMap, describeLayer */
    public static final String KEY_LAYERS = "LAYERS";
    /** Parameter used in getOrigFile, getLegendGraphic */
    public static final String KEY_LAYER = "LAYER";
    /** Parameter used in getFeatureInfo */
    public static final String KEY_QUERY_LAYERS = "QUERY_LAYERS";
    /** Parameter used in getMap, getFeatureInfo */
    public static final String KEY_CRS_V111 = "SRS";
    /** Parameter used in getMap, getFeatureInfo */
    public static final String KEY_CRS_V130 = "CRS";
    /** Parameter used in getMap, getFeatureInfo */
    public static final String KEY_BBOX = "BBOX";
    /** Parameter used in getMap, getFeatureInfo */
    public static final String KEY_ELEVATION = "ELEVATION";
    /** Parameter used in getMap, getOrigFile, getFeatureInfo */
    public static final String KEY_TIME = "TIME";
    /** Parameter used in getMap, getFeatureInfo, getLegendGraphic */
    public static final String KEY_WIDTH = "WIDTH";
    /** Parameter used in getMap, getFeatureInfo, getLegendGraphic */
    public static final String KEY_HEIGHT = "HEIGHT";
    /** Parameter used in getMap */
    public static final String KEY_BGCOLOR = "BGCOLOR";
    /** Parameter used in getMap */
    public static final String KEY_TRANSPARENT = "TRANSPARENT";
    /** Parameter used in getMap */
    public static final String KEY_STYLES = "STYLES";
    /** Parameter used in getLegendGraphic */
    public static final String KEY_STYLE = "STYLE";
    /** Parameter used in getMap,getLegendGraphic */
    public static final String KEY_SLD = "SLD";
    /** Parameter used in getMap, getLegendGraphic */
    public static final String KEY_SLD_VERSION = "SLD_VERSION";
    /** Parameter used in getLegendGraphic */
    public static final String KEY_FEATURETYPE = "FEATURETYPE";
    /** Parameter used in getLegendGraphic */
    public static final String KEY_COVERAGE = "COVERAGE";
    /** Parameter used in getLegendGraphic */
    public static final String KEY_RULE = "RULE";
    /** Parameter used in getLegendGraphic */
    public static final String KEY_SCALE = "SCALE";
    /** Parameter used in getLegendGraphic */
    public static final String KEY_SLD_BODY = "SLD_BODY";
    /** Parameter used in getMap,getLegendGraphic */
    public static final String KEY_REMOTE_OWS_TYPE = "REMOTE_OWS_TYPE";
    /** Parameter used in getMap,getLegendGraphic */
    public static final String KEY_REMOTE_OWS_URL = "REMOTE_OWS_URL";
    /** Parameter used in getFeatureInfo */
    public static final String KEY_I_V130 = "I";
    /** Parameter used in getFeatureInfo */
    public static final String KEY_J_V130 = "J";
    /** Parameter used in getFeatureInfo */
    public static final String KEY_I_V111 = "X";
    /** Parameter used in getFeatureInfo */
    public static final String KEY_J_V111 = "Y";
    /** Parameter used in getFeatureInfo */
    public static final String KEY_INFO_FORMAT= "INFO_FORMAT";
    /** Parameter used in getFeatureInfo */
    public static final String KEY_FEATURE_COUNT = "FEATURE_COUNT";
    /** Parameter used in getFeatureInfo */
    public static final String KEY_GETMETADATA = "GetMetadata";
    /** Parameter used in getMap */
    public static final String KEY_AZIMUTH = "AZIMUTH";
    /** Parameter used in GetCapabilities, for backward compatibility with WMS 1.0.0 */
    public static final String KEY_WMTVER = "WMTVER";
    /** Parameter used to store additional parameters from the query, the value object is a MultiValueMap */
    public static final String KEY_EXTRA_PARAMETERS = "EXTRA";
    /** Parameter INSPIRE used to choose the language of the capabilities document */
    public static final String KEY_LANGUAGE = "LANGUAGE";
    
    public static final String KEY_EXCEPTIONS = "EXCEPTIONS";
    
    public static final String EXCEPTIONS_INIMAGE = "INIMAGE";
    
    private WMSConstant() {}

    public static Request createRequest130(final List<String> gfi_mimetypes){
        final DCPType dcp = new DCPType(new HTTP(new Get(new OnlineResource("someurl")), new Post(new OnlineResource("someurl"))));

        final OperationType getCapabilities = new OperationType(Arrays.asList("text/xml", "application/vnd.ogc.wms_xml"), dcp);
        final OperationType getMap          = new OperationType(Arrays.asList("image/gif","image/png","image/jpeg","image/bmp","image/tiff","image/x-portable-pixmap"), dcp);
        final OperationType getFeatureInfo  = new OperationType(gfi_mimetypes, dcp);

        final Request REQUEST_130 = new Request(getCapabilities, getMap, getFeatureInfo);

        /*
         * Extended Operation
         */
        ObjectFactory factory = new ObjectFactory();

        final OperationType describeLayer    = new OperationType(Arrays.asList("text/xml"), dcp);
        final OperationType getLegendGraphic = new OperationType(Arrays.asList("image/png","image/jpeg","image/gif","image/tiff"), dcp);

        REQUEST_130.getExtendedOperation().add(factory.createDescribeLayer(describeLayer));

        REQUEST_130.getExtendedOperation().add(factory.createGetLegendGraphic(getLegendGraphic));
        return REQUEST_130;
    }

    public static org.geotoolkit.wms.xml.v111.Request createRequest111(final List<String> gfi_mimetypes){
        final org.geotoolkit.wms.xml.v111.Post post   = new org.geotoolkit.wms.xml.v111.Post(new org.geotoolkit.wms.xml.v111.OnlineResource("someurl"));
        final org.geotoolkit.wms.xml.v111.Get get     = new org.geotoolkit.wms.xml.v111.Get(new org.geotoolkit.wms.xml.v111.OnlineResource("someurl"));
        final org.geotoolkit.wms.xml.v111.HTTP http   = new org.geotoolkit.wms.xml.v111.HTTP(get, post);
        final org.geotoolkit.wms.xml.v111.DCPType dcp = new org.geotoolkit.wms.xml.v111.DCPType(http);

        final GetCapabilities getCapabilities = new GetCapabilities(Arrays.asList("text/xml", "application/vnd.ogc.wms_xml"), dcp);
        final GetMap getMap                   = new GetMap(Arrays.asList("image/gif","image/png","image/jpeg","image/bmp","image/tiff","image/x-portable-pixmap"), dcp);
        final GetFeatureInfo getFeatureInfo   = new GetFeatureInfo(gfi_mimetypes, dcp);

         /*
         * Extended Operation
         */
        final DescribeLayer describeLayer       = new DescribeLayer(Arrays.asList("text/xml"), dcp);
        final GetLegendGraphic getLegendGraphic = new GetLegendGraphic(Arrays.asList("image/png","image/jpeg","image/gif","image/tiff"), dcp);

        org.geotoolkit.wms.xml.v111.Request REQUEST_111 = new org.geotoolkit.wms.xml.v111.Request(getCapabilities, getMap, getFeatureInfo, describeLayer, getLegendGraphic, null, null);
        return REQUEST_111;
    }

    public static final String EXCEPTION_111_XML        = "application/vnd.ogc.se_xml";
    public static final String EXCEPTION_111_INIMAGE    = "application/vnd.ogc.se_inimage";
    public static final String EXCEPTION_111_BLANK      = "application/vnd.ogc.se_blank";
    public static final List<String> EXCEPTION_111 = new ArrayList<>();
    static {
        EXCEPTION_111.add(EXCEPTION_111_XML);
        EXCEPTION_111.add(EXCEPTION_111_INIMAGE);
        EXCEPTION_111.add(EXCEPTION_111_BLANK);
    }


    public static final String EXCEPTION_130_XML        = "XML";
    public static final String EXCEPTION_130_INIMAGE    = "INIMAGE";
    public static final String EXCEPTION_130_BLANK      = "BLANK";
    public static final List<String> EXCEPTION_130 = new ArrayList<>();
    static {
        EXCEPTION_130.add(EXCEPTION_130_XML);
        EXCEPTION_130.add(EXCEPTION_130_INIMAGE);
        EXCEPTION_130.add(EXCEPTION_130_BLANK);
    }

    /**
     * Generates the base capabilities for a WMS from the service metadata.
     *
     * @param metadata the service metadata
     * @return the service base capabilities
     */
    public static AbstractWMSCapabilities createCapabilities(final String version, final Details metadata) {
        ensureNonNull("metadata", metadata);
        ensureNonNull("version",  version);

        final Contact currentContact = metadata.getServiceContact();

        // Create keywords part.
        AbstractKeywordList keywordList = null;
        if (metadata.getKeywords() != null) {
            keywordList = WmsXmlFactory.createKeyword(version, metadata.getKeywords());
        }

        // Create address part.
        AbstractOnlineResource orgUrl = null;
        AbstractContactInformation contact = null;
        if (currentContact != null) {
            final AbstractContactAddress address = WmsXmlFactory.createContactAddress(version,"POSTAL",
                    currentContact.getAddress(), currentContact.getCity(), currentContact.getState(),
                    currentContact.getZipCode(), currentContact.getCountry());

            // Create contact part.
            final AbstractContactPersonPrimary personPrimary = WmsXmlFactory.createContactPersonPrimary(version,
                    currentContact.getFullname(), currentContact.getOrganisation());
            contact = WmsXmlFactory.createContactInformation(version,
                    personPrimary, currentContact.getPosition(), address, currentContact.getPhone(), currentContact.getFax(),
                    currentContact.getEmail());

            // url
            if (currentContact.getUrl() != null) {
                orgUrl = WmsXmlFactory.createOnlineResource(version, currentContact.getUrl());
            }
        }

        // Create service part.
        AccessConstraint serviceConstraints = metadata.getServiceConstraints();
        if (serviceConstraints == null) {
            serviceConstraints = new AccessConstraint();
        }
        final AbstractService newService = WmsXmlFactory.createService(version, metadata.getName(),
                metadata.getIdentifier(), metadata.getDescription(), keywordList, orgUrl, contact,
                serviceConstraints.getFees(), serviceConstraints.getAccessConstraint(),
                serviceConstraints.getLayerLimit(), serviceConstraints.getMaxWidth(),
                serviceConstraints.getMaxHeight());

        // extension
        final NameFactory nf = new DefaultNameFactory();
        final LocalName servType = nf.createLocalName(null, "view");
        final ExtendedCapabilitiesType ext = new ExtendedCapabilitiesType(ScopeCode.SERVICE, new ServiceTypeImpl(servType));
        final org.geotoolkit.inspire.xml.vs.ObjectFactory factory = new org.geotoolkit.inspire.xml.vs.ObjectFactory();
        final JAXBElement<?> extension = factory.createExtendedCapabilities(ext);
        // Create capabilities base.
        final AbstractCapability capability =  WmsXmlFactory.createCapability(version, extension);
        return WmsXmlFactory.createCapabilities(version, newService, capability, null);
    }
}
