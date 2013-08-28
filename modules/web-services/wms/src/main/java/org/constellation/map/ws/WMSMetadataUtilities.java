/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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

import org.apache.sis.util.Static;
import org.constellation.dto.Contact;
import org.constellation.dto.Service;
import org.geotoolkit.wms.xml.AbstractWMSCapabilities;
import org.geotoolkit.wms.xml.WMSVersion;
import org.geotoolkit.wms.xml.AbstractContactAddress;
import org.geotoolkit.wms.xml.AbstractContactInformation;
import org.geotoolkit.wms.xml.AbstractContactPersonPrimary;
import org.geotoolkit.wms.xml.AbstractKeywordList;
import org.geotoolkit.wms.xml.AbstractService;
import org.geotoolkit.wms.xml.WmsXmlFactory;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import org.geotoolkit.wms.xml.AbstractCapability;

/**
 * Utility class for WMS services management/configuration.
 *
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class WMSMetadataUtilities extends Static {

    /**
     * Generates the base capabilities for a WMS from the service metadata.
     *
     * @param metadata the service metadata
     * @return the service base capabilities
     */
    public static AbstractWMSCapabilities createCapabilities(final WMSVersion wversion, final Service metadata) {
        ensureNonNull("metadata", metadata);
        ensureNonNull("version",  wversion);

        final String version = wversion.getCode();
        final Contact currentContact = metadata.getServiceContact();

        // Create keywords part.
        final AbstractKeywordList keywordList = WmsXmlFactory.createKeyword(version, metadata.getKeywords());

        // Create address part.
        final AbstractContactAddress address = WmsXmlFactory.createContactAddress(version,"POSTAL",
                currentContact.getAddress(), currentContact.getCity(), currentContact.getState(),
                currentContact.getZipCode(), currentContact.getCountry());

        // Create contact part.
        final AbstractContactPersonPrimary personPrimary = WmsXmlFactory.createContactPersonPrimary(version,
                currentContact.getFullname(), currentContact.getOrganisation());
        final AbstractContactInformation contact = WmsXmlFactory.createContactInformation(version,
                personPrimary, currentContact.getPosition(), address, currentContact.getPhone(), currentContact.getFax(),
                currentContact.getEmail());

        // Create service part.
        final AbstractService newService = WmsXmlFactory.createService(version, metadata.getName(),
                metadata.getIdentifier(), metadata.getDescription(), keywordList, null, contact,
                metadata.getServiceConstraints().getFees(), metadata.getServiceConstraints().getAccessConstraint(), 
                metadata.getServiceConstraints().getLayerLimit(), metadata.getServiceConstraints().getMaxWidth(),
                metadata.getServiceConstraints().getMaxHeight());

        // Create capabilities base.
        final AbstractCapability capability =  WmsXmlFactory.createCapability(version);
        return WmsXmlFactory.createCapabilities(version, newService, capability, null);
    }
}
