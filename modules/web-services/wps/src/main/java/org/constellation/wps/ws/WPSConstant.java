/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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
package org.constellation.wps.ws;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.constellation.dto.AccessConstraint;
import org.constellation.dto.Contact;
import org.constellation.dto.Service;
import org.geotoolkit.ows.xml.AbstractContact;
import org.geotoolkit.ows.xml.AbstractDCP;
import org.geotoolkit.ows.xml.AbstractDomain;
import org.geotoolkit.ows.xml.AbstractOperation;
import org.geotoolkit.ows.xml.AbstractOperationsMetadata;
import org.geotoolkit.ows.xml.AbstractResponsiblePartySubset;
import org.geotoolkit.ows.xml.AbstractServiceIdentification;
import org.geotoolkit.ows.xml.AbstractServiceProvider;
import org.geotoolkit.ows.xml.OWSXmlFactory;
import org.geotoolkit.wps.xml.WPSCapabilities;
import org.geotoolkit.wps.xml.WPSXmlFactory;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import org.geotoolkit.ows.xml.AbstractOnlineResourceType;

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

    /* Maximum size in megabytes for a complex input */
    public static final int MAX_MB_INPUT_COMPLEX = 100;
    
   
    /** 
     * Process identifier prefix to uniquely identifies process using OGC URN code.
     */
    public static final String PROCESS_PREFIX = "urn:ogc:cstl:wps:";
    
    /**
     * Temporary directory name used for store responses.
     */
    public static final String TEMP_FOLDER = "/tmp/wps" ;
    
    public static final AbstractOperationsMetadata OPERATIONS_METADATA;
    static {
        final List<AbstractDCP> getAndPost = new ArrayList<>();
        getAndPost.add(OWSXmlFactory.buildDCP("1.1.0", "somURL", "someURL"));

        final List<AbstractDCP> onlyPost = new ArrayList<>();
        onlyPost.add(OWSXmlFactory.buildDCP("1.1.0", null, "someURL"));

        final List<AbstractOperation> operations = new ArrayList<>();

        final List<AbstractDomain> gcParameters = new ArrayList<>();
        gcParameters.add(OWSXmlFactory.buildDomain("1.1.0", "service", Arrays.asList("WPS")));
        gcParameters.add(OWSXmlFactory.buildDomain("1.1.0", "Acceptversions", Arrays.asList("1.0.0")));
        gcParameters.add(OWSXmlFactory.buildDomain("1.1.0", "AcceptFormats", Arrays.asList("text/xml")));
        final AbstractOperation getCapabilities = OWSXmlFactory.buildOperation("1.1.0", getAndPost, gcParameters, null, "GetCapabilities");
        operations.add(getCapabilities);

        final List<AbstractDomain> dpParameters = new ArrayList<>();
        dpParameters.add(OWSXmlFactory.buildDomain("1.1.0", "service", Arrays.asList("WPS")));
        dpParameters.add(OWSXmlFactory.buildDomain("1.1.0", "version", Arrays.asList("1.0.0")));
        final AbstractOperation describeProcess = OWSXmlFactory.buildOperation("1.1.0", getAndPost, dpParameters, null, "DescribeProcess");
        operations.add(describeProcess);

        final List<AbstractDomain> eParameters = new ArrayList<>();
        eParameters.add(OWSXmlFactory.buildDomain("1.1.0", "service", Arrays.asList("WPS")));
        eParameters.add(OWSXmlFactory.buildDomain("1.1.0", "version", Arrays.asList("1.0.0")));
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
    public static WPSCapabilities createCapabilities(final String version, final Service metadata) {
        ensureNonNull("metadata", metadata);
        ensureNonNull("version",  version);

        final Contact currentContact = metadata.getServiceContact();
        final AccessConstraint constraint = metadata.getServiceConstraints();

        final AbstractServiceIdentification servIdent = OWSXmlFactory.buildServiceIdentification("1.1.0",
                                                                                                 metadata.getName(),
                                                                                                 metadata.getDescription(),
                                                                                                 metadata.getKeywords(),
                                                                                                 "WFS",
                                                                                                 metadata.getVersions(),
                                                                                                 constraint.getFees(),
                                                                                                 Arrays.asList(constraint.getAccessConstraint()));

        // Create provider part.
        final AbstractContact contact = OWSXmlFactory.buildContact("1.1.0", currentContact.getPhone(), currentContact.getFax(),
                currentContact.getEmail(), currentContact.getAddress(), currentContact.getCity(), currentContact.getState(),
                currentContact.getZipCode(), currentContact.getCountry());

        final AbstractResponsiblePartySubset responsible = OWSXmlFactory.buildResponsiblePartySubset("1.1.0", currentContact.getFullname(), currentContact.getPosition(), contact, null);


        AbstractOnlineResourceType orgUrl = null;
        if (currentContact.getUrl() != null) {
            orgUrl = OWSXmlFactory.buildOnlineResource("1.1.0", currentContact.getUrl());
        }
        final AbstractServiceProvider servProv = OWSXmlFactory.buildServiceProvider("1.1.0", currentContact.getOrganisation(), orgUrl, responsible);


        // Create capabilities base.
        return WPSXmlFactory.buildWPSCapabilities(version, servIdent, servProv, null, null, null, null);
    }
}
