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

package org.constellation.wmts.ws;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import org.constellation.dto.AccessConstraint;
import org.constellation.dto.Contact;
import org.constellation.dto.Service;
import org.geotoolkit.ows.xml.AbstractContact;
import org.geotoolkit.ows.xml.AbstractDCP;
import org.geotoolkit.ows.xml.AbstractDomain;
import org.geotoolkit.ows.xml.AbstractOnlineResourceType;
import org.geotoolkit.ows.xml.AbstractOperation;
import org.geotoolkit.ows.xml.AbstractOperationsMetadata;
import org.geotoolkit.ows.xml.AbstractResponsiblePartySubset;
import org.geotoolkit.ows.xml.AbstractServiceIdentification;
import org.geotoolkit.ows.xml.AbstractServiceProvider;
import org.geotoolkit.ows.xml.OWSXmlFactory;
import org.geotoolkit.wmts.xml.WMTSResponse;
import org.geotoolkit.wmts.xml.WMTSXmlFactory;

/**
 *  WMTS Constants
 *
 * @author Guilhem Legal (Geomatys)
 */
public final class WMTSConstant {

    private WMTSConstant() {}
    
    public static final AbstractOperationsMetadata OPERATIONS_METADATA;
    static {
        final List<AbstractDCP> getAndPost = new ArrayList<>();
        getAndPost.add(OWSXmlFactory.buildDCP("1.1.0", "someURL", "someURL"));

        final List<AbstractDCP> onlyPost = new ArrayList<>();
        onlyPost.add(OWSXmlFactory.buildDCP("1.1.0", null, "someURL"));

        final List<AbstractOperation> operations = new ArrayList<>();

        final AbstractOperation getCapabilities = OWSXmlFactory.buildOperation("1.1.0", getAndPost, null, null, "GetCapabilities");
        operations.add(getCapabilities);

        final AbstractOperation getTile = OWSXmlFactory.buildOperation("1.1.0", getAndPost, null, null, "GetTile");
        operations.add(getTile);

        final AbstractOperation getFeatureInfo = OWSXmlFactory.buildOperation("1.1.0", getAndPost, null, null, "GetFeatureInfo");
        operations.add(getFeatureInfo);

        final List<AbstractDomain> constraints = new ArrayList<>();
        constraints.add(OWSXmlFactory.buildDomain("1.1.0", "PostEncoding", Arrays.asList("XML")));

        OPERATIONS_METADATA = OWSXmlFactory.buildOperationsMetadata("1.1.0", operations, null, constraints, null);
    }

    /**
     * Generates the base capabilities for a WMS from the service metadata.
     *
     * @param metadata the service metadata
     * @return the service base capabilities
     */
    public static WMTSResponse createCapabilities(final String version, final Service metadata) {
        ensureNonNull("metadata", metadata);
        ensureNonNull("version",  version);

        final Contact currentContact = metadata.getServiceContact();
        final AccessConstraint constraint = metadata.getServiceConstraints();

        final AbstractServiceIdentification servIdent = OWSXmlFactory.buildServiceIdentification("1.1.0",
                                                                                                 metadata.getName(),
                                                                                                 metadata.getDescription(),
                                                                                                 metadata.getKeywords(),
                                                                                                 "WMTS",
                                                                                                 metadata.getVersions(),
                                                                                                 constraint.getFees(),
                                                                                                 Arrays.asList(constraint.getAccessConstraint()));

        // Create provider part.
        final AbstractContact contact = OWSXmlFactory.buildContact("1.1.0", currentContact.getPhone(), currentContact.getFax(),
                currentContact.getEmail(), currentContact.getAddress(), currentContact.getCity(), currentContact.getState(),
                currentContact.getZipCode(), currentContact.getCountry(), currentContact.getHoursOfService(), currentContact.getContactInstructions());

        final AbstractResponsiblePartySubset responsible = OWSXmlFactory.buildResponsiblePartySubset("1.1.0", currentContact.getFullname(), currentContact.getPosition(), contact, null);

        AbstractOnlineResourceType orgUrl = null;
        if (currentContact.getUrl() != null) {
            orgUrl = OWSXmlFactory.buildOnlineResource("1.1.0", currentContact.getUrl());
        }
        final AbstractServiceProvider servProv = OWSXmlFactory.buildServiceProvider("1.1.0", currentContact.getOrganisation(), orgUrl, responsible);


        // Create capabilities base.
        return WMTSXmlFactory.buildCapabilities(version, servIdent, servProv, null, null, null, null);
    }
}
