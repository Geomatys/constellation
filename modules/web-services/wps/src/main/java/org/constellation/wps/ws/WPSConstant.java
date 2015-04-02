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
package org.constellation.wps.ws;

import org.constellation.dto.AccessConstraint;
import org.constellation.dto.Contact;
import org.constellation.dto.Details;
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
import org.geotoolkit.wps.xml.WPSCapabilities;
import org.geotoolkit.wps.xml.WPSXmlFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;

/**
 *  WPS Constants
 *
 * @author Quentin Boileau (Geomatys)
 */
public final class WPSConstant {

    private WPSConstant() {}

    /**
     * WPS Query service
     */
    public static final String WPS_SERVICE = "WPS";

    /**
     * Version
     */
     public static final String WPS_1_0_0 = "1.0.0";

    /**
     * Lang
     */
     public static final String WPS_LANG = "en-EN";

    /**
     * Request parameters.
     */
    public static final String GETCAPABILITIES = "GetCapabilities";
    public static final String DESCRIBEPROCESS = "DescribeProcess";
    public static final String EXECUTE = "Execute";


    public static final String IDENTIFER_PARAMETER = "IDENTIFIER";
    public static final String LANGUAGE_PARAMETER = "LANGUAGE";

    public static final String DATA_INPUTS_PARAMETER = "DATAINPUTS";
    public static final String RESPONSE_DOCUMENT_PARAMETER = "RESPONSEDOCUMENT";
    public static final String RAW_DATA_OUTPUT_PARAMETER = "RAWDATAOUTPUT";

    // Input/output parameters
    public static final String MIME_TYPE_PARAMETER = "MIMETYPE";
    public static final String ENCODING_PARAMETER = "ENCODING";
    public static final String SCHEMA_PARAMETER = "SCHEMA";
    public static final String HREF_PARAMETER = "HREF";
    // The WPS specification seems to accept either the attribute mimeType or format to set a mime type
    public static final String FORMAT_PARAMETER = "FORMAT";
    public static final String METHOD_PARAMETER = "METHOD";
    public static final String HEADER_PARAMETER = "HEADER";
    public static final String BODY_PARAMETER = "BODY";
    public static final String BODY_REFERENCE_PARAMETER = "BODYREFERENCE";
    public static final String DATA_TYPE_PARAMETER = "DATATYPE";
    public static final String UOM_PARAMETER = "UOM";
    public static final String AS_REFERENCE_PARAMETER = "asReference";

    // CRS and BoundingBox identifier
    public static final String BOUNDING_BOX_IDENTIFIER_PARAMETER = "ows:boundingbox";
    public static final String CRS_IDENTIFIER_PARAMETER = "urn:ogc:def:crs";

    // ResponseDocument options
    public static final String LINEAGE_PARAMETER = "lineage";
    public static final String STATUS_PARAMETER = "status";
    public static final String STORE_EXECUTE_RESPONSE_PARAMETER = "storeExecuteResponse";



    /* Maximum size in megabytes for a complex input */
    public static final int MAX_MB_INPUT_COMPLEX = 100;


    /**
     * Process identifier prefix to uniquely identifies process using OGC URN code.
     */
    public static final String PROCESS_PREFIX = "urn:ogc:cstl:wps:";

    public static final AbstractOperationsMetadata OPERATIONS_METADATA;

    static {
        final List<AbstractDCP> getAndPost = new ArrayList<>();
        getAndPost.add(OWSXmlFactory.buildDCP("1.1.0", "somURL", "someURL"));

        final List<AbstractDCP> onlyPost = new ArrayList<>();
        onlyPost.add(OWSXmlFactory.buildDCP("1.1.0", null, "someURL"));

        final List<AbstractOperation> operations = new ArrayList<>();

        final List<AbstractDomain> gcParameters = new ArrayList<>();
        gcParameters.add(OWSXmlFactory.buildDomain("1.1.0", "service", Arrays.asList(WPS_SERVICE)));
        gcParameters.add(OWSXmlFactory.buildDomain("1.1.0", "Acceptversions", Arrays.asList(WPS_1_0_0)));
        gcParameters.add(OWSXmlFactory.buildDomain("1.1.0", "AcceptFormats", Arrays.asList("text/xml")));
        final AbstractOperation getCapabilities = OWSXmlFactory.buildOperation("1.1.0", getAndPost, gcParameters, null, "GetCapabilities");
        operations.add(getCapabilities);

        final List<AbstractDomain> dpParameters = new ArrayList<>();
        dpParameters.add(OWSXmlFactory.buildDomain("1.1.0", "service", Arrays.asList(WPS_SERVICE)));
        dpParameters.add(OWSXmlFactory.buildDomain("1.1.0", "version", Arrays.asList(WPS_1_0_0)));
        final AbstractOperation describeProcess = OWSXmlFactory.buildOperation("1.1.0", getAndPost, dpParameters, null, "DescribeProcess");
        operations.add(describeProcess);

        final List<AbstractDomain> eParameters = new ArrayList<>();
        eParameters.add(OWSXmlFactory.buildDomain("1.1.0", "service", Arrays.asList(WPS_SERVICE)));
        eParameters.add(OWSXmlFactory.buildDomain("1.1.0", "version", Arrays.asList(WPS_1_0_0)));
        final AbstractOperation execute = OWSXmlFactory.buildOperation("1.1.0", onlyPost, eParameters, null, "Execute");
        operations.add(execute);

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
    public static WPSCapabilities createCapabilities(final String version, final Details metadata) {
        ensureNonNull("metadata", metadata);
        ensureNonNull("version",  version);

        final Contact currentContact = metadata.getServiceContact();
        final AccessConstraint constraint = metadata.getServiceConstraints();

        final AbstractServiceIdentification servIdent;
        if (constraint == null) {
            servIdent = OWSXmlFactory.buildServiceIdentification("1.1.0",
                    metadata.getName(),
                    metadata.getDescription(),
                    metadata.getKeywords(),
                    WPS_SERVICE,
                    metadata.getVersions(),
                    null, new ArrayList<String>());
        } else {
            servIdent = OWSXmlFactory.buildServiceIdentification("1.1.0",
                    metadata.getName(),
                    metadata.getDescription(),
                    metadata.getKeywords(),
                    WPS_SERVICE,
                    metadata.getVersions(),
                    constraint.getFees(),
                    Arrays.asList(constraint.getAccessConstraint()));
        }

        // Create provider part.
        AbstractContact contact = null;
        AbstractOnlineResourceType orgUrl = null;
        final AbstractResponsiblePartySubset responsible;
        final AbstractServiceProvider servProv;
        if (currentContact != null) {
            contact = OWSXmlFactory.buildContact("1.1.0", currentContact.getPhone(), currentContact.getFax(),
                    currentContact.getEmail(), currentContact.getAddress(), currentContact.getCity(), currentContact.getState(),
                    currentContact.getZipCode(), currentContact.getCountry(), currentContact.getHoursOfService(), currentContact.getContactInstructions());
            responsible = OWSXmlFactory.buildResponsiblePartySubset("1.1.0", currentContact.getFullname(), currentContact.getPosition(), contact, null);

            if (currentContact.getUrl() != null) {
                orgUrl = OWSXmlFactory.buildOnlineResource("1.1.0", currentContact.getUrl());
            }
            servProv = OWSXmlFactory.buildServiceProvider("1.1.0", currentContact.getOrganisation(), orgUrl, responsible);
        } else {
            responsible = OWSXmlFactory.buildResponsiblePartySubset("1.1.0", null, null, null, null);
            servProv = OWSXmlFactory.buildServiceProvider("1.1.0", null, orgUrl, responsible);
        }

        // Create capabilities base.
        return WPSXmlFactory.buildWPSCapabilities(version, servIdent, servProv, null, null, null, null);
    }
}
