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
import org.geotoolkit.wms.xml.v111.WMT_MS_Capabilities;
import org.geotoolkit.wms.xml.v130.WMSCapabilities;

import java.util.ArrayList;
import java.util.List;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;

/**
 * Utility class for WMS services management/configuration.
 *
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class WMSMetadataUtilities extends Static {

    /**
     * Generates the base capabilities for a WMS from the service metadata and the service version.
     *
     * @param metadata the service metadata
     * @param version  the service version
     * @return the service base capabilities
     */
    public static AbstractWMSCapabilities createCapabilities(final Service metadata, final WMSVersion version) {
        ensureNonNull("metadata", metadata);
        ensureNonNull("version",  version);

        switch (version) {
            case v111: return createCapabilitiesV111(metadata);
            case v130: return createCapabilitiesV130(metadata);
            default:   throw new IllegalArgumentException("Unknown WMS version \"" + version.getCode() + "\".");
        }
    }

    /**
     * Generates the base capabilities for a WMS from the service metadata (in version {@code 1.1.1}).
     *
     * @param metadata the service metadata
     * @return the service base capabilities
     */
    public static WMT_MS_Capabilities createCapabilitiesV111(final Service metadata) {
        ensureNonNull("metadata", metadata);

        final Contact currentContact = metadata.getServiceContact();

        // Create keywords part.
        final List<org.geotoolkit.wms.xml.v111.Keyword> keywords = new ArrayList<>(0);
        for (String keywordString : metadata.getKeywords()) {
            org.geotoolkit.wms.xml.v111.Keyword keyword = new org.geotoolkit.wms.xml.v111.Keyword(keywordString);
            keywords.add(keyword);
        }
        final org.geotoolkit.wms.xml.v111.KeywordList keywordList = new org.geotoolkit.wms.xml.v111.KeywordList(keywords);

        // Create address part.
        final org.geotoolkit.wms.xml.v111.ContactAddress address = new org.geotoolkit.wms.xml.v111.ContactAddress(
                "POSTAL", currentContact.getAddress(), currentContact.getCity(), currentContact.getState(),
                currentContact.getZipCode(), currentContact.getCountry());

        // Create contact part.
        final org.geotoolkit.wms.xml.v111.ContactPersonPrimary personPrimary = new org.geotoolkit.wms.xml.v111.ContactPersonPrimary(
                currentContact.getFullname(), currentContact.getOrganisation());
        final org.geotoolkit.wms.xml.v111.ContactInformation contact = new org.geotoolkit.wms.xml.v111.ContactInformation(
                personPrimary, currentContact.getPosition(), address, currentContact.getPhone(), currentContact.getFax(),
                currentContact.getEmail());

        // Create service part.
        final org.geotoolkit.wms.xml.v111.Service newService = new org.geotoolkit.wms.xml.v111.Service(metadata.getName(),
                metadata.getIdentifier(), metadata.getDescription(), keywordList, null, contact,
                metadata.getServiceConstraints().getFees(), metadata.getServiceConstraints().getAccessConstraint());

        // Create capabilities base.
        final org.geotoolkit.wms.xml.v111.Capability capability = new org.geotoolkit.wms.xml.v111.Capability(null, null, null, null);
        return new WMT_MS_Capabilities(newService, capability, "1.1.1", null);
    }

    /**
     * Generates the base capabilities for a WMS from the service metadata (in version {@code 1.3.0}).
     *
     * @param metadata the service metadata
     * @return the service base capabilities
     */
    public static WMSCapabilities createCapabilitiesV130(final Service metadata) {
        ensureNonNull("metadata", metadata);

        final Contact currentContact = metadata.getServiceContact();

        // Create keywords part.
        final List<org.geotoolkit.wms.xml.v130.Keyword> keywords = new ArrayList<>(0);
        for (String keywordString : metadata.getKeywords()) {
            org.geotoolkit.wms.xml.v130.Keyword keyword = new org.geotoolkit.wms.xml.v130.Keyword(keywordString);
            keywords.add(keyword);
        }
        final org.geotoolkit.wms.xml.v130.KeywordList keywordsList = new org.geotoolkit.wms.xml.v130.KeywordList(keywords);

        // Create address part.
        final org.geotoolkit.wms.xml.v130.ContactAddress address = new org.geotoolkit.wms.xml.v130.ContactAddress(
                "POSTAL", currentContact.getAddress(), currentContact.getCity(), currentContact.getState(),
                currentContact.getZipCode(), currentContact.getCountry());

        // Create contact part.
        final org.geotoolkit.wms.xml.v130.ContactPersonPrimary personPrimary = new org.geotoolkit.wms.xml.v130.ContactPersonPrimary(
                currentContact.getFullname(), currentContact.getOrganisation());
        final org.geotoolkit.wms.xml.v130.ContactInformation contact = new org.geotoolkit.wms.xml.v130.ContactInformation(
                personPrimary, currentContact.getPosition(), address, currentContact.getPhone(), currentContact.getFax(),
                currentContact.getEmail());

        // Create service part.
        final org.geotoolkit.wms.xml.v130.Service newService = new org.geotoolkit.wms.xml.v130.Service(metadata.getName(),
                metadata.getIdentifier(), metadata.getDescription(), keywordsList, null, contact,
                metadata.getServiceConstraints().getFees(), metadata.getServiceConstraints().getAccessConstraint(),
                metadata.getServiceConstraints().getLayerLimit(), metadata.getServiceConstraints().getMaxWidth(),
                metadata.getServiceConstraints().getMaxHeight());

        // Create capabilities base.
        final org.geotoolkit.wms.xml.v130.Capability capability = new org.geotoolkit.wms.xml.v130.Capability(null, null, null, null);
        return new WMSCapabilities(newService, capability, "1.3.0", null);
    }
}
