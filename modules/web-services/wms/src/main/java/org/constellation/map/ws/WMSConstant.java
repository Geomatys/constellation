/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2010, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
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
import org.constellation.dto.Service;
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
    public static AbstractWMSCapabilities createCapabilities(final String version, final Service metadata) {
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
