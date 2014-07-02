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
import org.geotoolkit.ows.xml.AbstractHTTP;
import org.geotoolkit.ows.xml.AbstractOnlineResourceType;
import org.geotoolkit.ows.xml.AbstractOperation;
import org.geotoolkit.ows.xml.AbstractOperationsMetadata;
import org.geotoolkit.ows.xml.AbstractRequestMethod;
import org.geotoolkit.ows.xml.AbstractResponsiblePartySubset;
import org.geotoolkit.ows.xml.AbstractServiceIdentification;
import org.geotoolkit.ows.xml.AbstractServiceProvider;
import org.geotoolkit.ows.xml.OWSXmlFactory;
import org.geotoolkit.ows.xml.v110.CodeType;
import org.geotoolkit.wmts.xml.WMTSResponse;
import org.geotoolkit.wmts.xml.WMTSXmlFactory;
import org.geotoolkit.wmts.xml.v100.Style;

/**
 *  WMTS Constants
 *
 * @author Guilhem Legal (Geomatys)
 */
public final class WMTSConstant {

    private WMTSConstant() {}

    public static final AbstractOperationsMetadata OPERATIONS_METADATA;
    static {
        final AbstractDomain restFullConstraint = OWSXmlFactory.buildDomain("1.1.0", "GetEncoding", Arrays.asList("RESTful"));
        final AbstractDomain kvpConstraint      = OWSXmlFactory.buildDomain("1.1.0", "GetEncoding", Arrays.asList("KVP"));

        final AbstractRequestMethod getKVP      = OWSXmlFactory.buildRrequestMethod("1.1.0", "someURL", Arrays.asList(kvpConstraint));
        final AbstractRequestMethod getRSF      = OWSXmlFactory.buildRrequestMethod("1.1.0", "someURL", Arrays.asList(restFullConstraint));
        final AbstractRequestMethod post        = OWSXmlFactory.buildRrequestMethod("1.1.0", "someURL", null);

        final AbstractHTTP httpFull             = OWSXmlFactory.buildHttp("1.1.0", Arrays.asList(getKVP, getRSF), Arrays.asList(post));
        final AbstractHTTP httpGFI              = OWSXmlFactory.buildHttp("1.1.0", Arrays.asList(getKVP), Arrays.asList(post));

        final List<AbstractDCP> getAndPostKR    = new ArrayList<>();
        getAndPostKR.add(OWSXmlFactory.buildDCP("1.1.0",httpFull));

        final List<AbstractDCP> getAndPost    = new ArrayList<>();
        getAndPost.add(OWSXmlFactory.buildDCP("1.1.0", httpGFI));

        final List<AbstractOperation> operations = new ArrayList<>();

        final AbstractOperation getCapabilities = OWSXmlFactory.buildOperation("1.1.0", getAndPostKR, null, null, "GetCapabilities");
        operations.add(getCapabilities);

        final AbstractOperation getTile = OWSXmlFactory.buildOperation("1.1.0", getAndPostKR, null, null, "GetTile");
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

        final AbstractServiceIdentification servIdent;
        if (constraint != null) {
            servIdent = OWSXmlFactory.buildServiceIdentification("1.1.0", metadata.getName(), metadata.getDescription(),
                        metadata.getKeywords(), "WMTS", metadata.getVersions(), constraint.getFees(),
                        Arrays.asList(constraint.getAccessConstraint()));
        } else {
            servIdent = OWSXmlFactory.buildServiceIdentification("1.1.0", metadata.getName(), metadata.getDescription(),
                    metadata.getKeywords(), "WMTS", metadata.getVersions(), null, new ArrayList<String>());
        }

        // Create provider part.
        final AbstractServiceProvider servProv;
        if (currentContact != null) {
            final AbstractContact contact = OWSXmlFactory.buildContact("1.1.0", currentContact.getPhone(), currentContact.getFax(),
                    currentContact.getEmail(), currentContact.getAddress(), currentContact.getCity(), currentContact.getState(),
                    currentContact.getZipCode(), currentContact.getCountry(), currentContact.getHoursOfService(), currentContact.getContactInstructions());

            final AbstractResponsiblePartySubset responsible = OWSXmlFactory.buildResponsiblePartySubset("1.1.0", currentContact.getFullname(), currentContact.getPosition(), contact, null);

            AbstractOnlineResourceType orgUrl = null;
            if (currentContact.getUrl() != null) {
                orgUrl = OWSXmlFactory.buildOnlineResource("1.1.0", currentContact.getUrl());
            }
            servProv = OWSXmlFactory.buildServiceProvider("1.1.0", currentContact.getOrganisation(), orgUrl, responsible);
        } else {
            servProv = OWSXmlFactory.buildServiceProvider("1.1.0", "", null, null);
        }

        // Create capabilities base.
        return WMTSXmlFactory.buildCapabilities(version, servIdent, servProv, null, null, null, null);
    }

    public static List<Style> DEFAULT_STYLES = new ArrayList<>();

    static {
        final Style defaultStyle = new Style();
        defaultStyle.setIsDefault(Boolean.TRUE);
        defaultStyle.setTitle("default");
        defaultStyle.setIdentifier(new CodeType("default"));
        DEFAULT_STYLES.add(defaultStyle);
    }
}
