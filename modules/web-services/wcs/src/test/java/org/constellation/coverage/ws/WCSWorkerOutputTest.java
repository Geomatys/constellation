/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
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
package org.constellation.coverage.ws;

import java.awt.image.RenderedImage;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.MimeType;
import org.geotoolkit.gml.xml.v311.DirectPositionType;
import org.geotoolkit.gml.xml.v311.EnvelopeEntry;
import org.geotoolkit.gml.xml.v311.GridLimitsType;
import org.geotoolkit.gml.xml.v311.GridType;
import org.geotoolkit.gml.xml.v311.TimePositionType;
import org.geotoolkit.wcs.xml.DescribeCoverage;
import org.geotoolkit.wcs.xml.DescribeCoverageResponse;
import org.geotoolkit.wcs.xml.GetCapabilities;
import org.geotoolkit.wcs.xml.GetCapabilitiesResponse;
import org.geotoolkit.wcs.xml.GetCoverage;
import org.geotoolkit.wcs.xml.v100.CoverageDescription;
import org.geotoolkit.wcs.xml.v100.CoverageOfferingBriefType;
import org.geotoolkit.wcs.xml.v100.CoverageOfferingType;
import org.geotoolkit.wcs.xml.v100.DescribeCoverageType;
import org.geotoolkit.wcs.xml.v100.DomainSubsetType;
import org.geotoolkit.wcs.xml.v100.GetCapabilitiesType;
import org.geotoolkit.wcs.xml.v100.GetCoverageType;
import org.geotoolkit.wcs.xml.v100.OutputType;
import org.geotoolkit.wcs.xml.v100.SpatialDomainType;
import org.geotoolkit.wcs.xml.v100.SpatialSubsetType;
import org.geotoolkit.wcs.xml.v100.TimeSequenceType;
import org.geotoolkit.wcs.xml.v100.WCSCapabilitiesType;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assume.*;


/**
 * Testing class for WCS requests.
 *
 * @version $Id$
 * @author Cédric Briançon (Geomatys)
 *
 * @since 0.5
 */
public class WCSWorkerOutputTest extends WCSWorkerInit {
    /**
     * Ensures that a PostGRID layer preconfigured is found in the GetCapabilities document
     * returned by the {@link WCSWorker}.
     *
     * @throws JAXBException
     * @throws CstlServiceException
     */
    @Test
    public void testGetCapabilities() throws JAXBException, CstlServiceException {
        final GetCapabilities request = new GetCapabilitiesType(null, null);
        final GetCapabilitiesResponse response = WORKER.getCapabilities(request);

        assertNotNull(response);
        assertTrue(response instanceof WCSCapabilitiesType);
        final WCSCapabilitiesType getCaps = (WCSCapabilitiesType) response;

        // Ensures that the test layer is really present.
        assumeTrue(containsTestLayer());
        // Verifies that the test layer is present into the GetCapabilities response.
        final List<CoverageOfferingBriefType> offerings = getCaps.getContentMetadata().getCoverageOfferingBrief();
        assertFalse(offerings.isEmpty());
        for (CoverageOfferingBriefType offering : offerings) {
            for (JAXBElement<String> string : offering.getRest()) {
                if (string.getName().getLocalPart().equalsIgnoreCase("name") &&
                    string.getValue().equals(LAYER_TEST))
                {
                    return;
                }
            }
        }
        // Not found in the list of coverage offerings, there is a mistake here.
        fail("Unable to find the layer "+ LAYER_TEST +" in the GetCapabilities document.");
    }

    /**
     * Ensures that a PostGRID layer preconfigured can be requested with a DescribeCoverage request,
     * and that the output document contains all data information.
     *
     * @throws JAXBException
     * @throws CstlServiceException
     */
    @Test
    public void testDescribeCoverage() throws JAXBException, CstlServiceException {
        // Ensures that the test layer is really present.
        assumeTrue(containsTestLayer());

        final DescribeCoverage request = new DescribeCoverageType(LAYER_TEST);
        final DescribeCoverageResponse response = WORKER.describeCoverage(request);
        assertNotNull(response);
        assertTrue(response instanceof CoverageDescription);

//        final Marshaller marshaller = POOL.acquireMarshaller();
//        marshaller.marshal(response, System.out);
//        POOL.release(marshaller);

        final CoverageDescription descCov = (CoverageDescription) response;
        // Verifies that the test layer is present into the DescribeCoverage response.
        for (CoverageOfferingType offering : descCov.getCoverageOffering()) {
            for (JAXBElement<String> string : offering.getRest()) {
                if (string.getName().getLocalPart().equalsIgnoreCase("name") &&
                    string.getValue().equals(LAYER_TEST))
                {
                    final SpatialDomainType spatialDomain = (SpatialDomainType) offering.getDomainSet()
                            .getContent().get(0).getValue();
                    final TimeSequenceType temporalDomain = (TimeSequenceType) offering.getDomainSet()
                            .getContent().get(1).getValue();
                    // Builds expected spatial domain
                    final List<DirectPositionType> pos = new ArrayList<DirectPositionType>();
                    pos.add(new DirectPositionType(-180.0, -90.0));
                    pos.add(new DirectPositionType(180.0, 90.0));
                    final EnvelopeEntry expectedEnvelope = new EnvelopeEntry(pos, "urn:ogc:def:crs:OGC:1.3:CRS84");
                    // Builds expected temporal domain
                    final List<TimePositionType> expectedTimes =
                            Collections.singletonList(new TimePositionType("2003-05-16T00:00:00Z"));
                    // Do assertions
                    assertEquals(expectedEnvelope, spatialDomain.getEnvelope());
                    assertEquals(expectedTimes, temporalDomain.getTimePositionOrTimePeriod());
                    /*
                     * All tests have succeed on that specific layer, we can now stop this test.
                     */
                    return;
                }
            }
        }
        fail("Unable to find the layer "+ LAYER_TEST +" in the DescribeCoverage document.");
    }

    /**
     * Ensures that a PostGRID layer preconfigured can be requested with a GetCoverage request.
     *
     * TODO: do a checksum on the output image.
     *
     * @throws JAXBException
     * @throws CstlServiceException
     */
    @Test
    public void testGetCoverage() throws JAXBException, CstlServiceException {
        // Ensures that the test layer is really present.
        assumeTrue(containsTestLayer());

        // Builds the GetCoverage request
        final List<String> axis = new ArrayList<String>();
        axis.add("width");
        axis.add("height");
        final List<BigInteger> low  = new ArrayList<BigInteger>();
        low.add(BigInteger.ZERO);
        low.add(BigInteger.ZERO);
        final List<BigInteger> high = new ArrayList<BigInteger>();
        high.add(new BigInteger("1024"));
        high.add(new BigInteger("512"));
        final GridLimitsType limits = new GridLimitsType(low, high);
        final GridType grid = new GridType(limits, axis);
        final List<DirectPositionType> pos = new ArrayList<DirectPositionType>();
        pos.add(new DirectPositionType(-180.0, -90.0));
        pos.add(new DirectPositionType(180.0, 90.0));
        final EnvelopeEntry envelope = new EnvelopeEntry(pos, "CRS:84");
        final DomainSubsetType domain = new DomainSubsetType(null, new SpatialSubsetType(envelope, grid));
        final GetCoverage request = new GetCoverageType(
                LAYER_TEST, domain, null, null, new OutputType(MimeType.IMAGE_PNG, "CRS:84"));

        // Finally execute the request on the worker.
        final RenderedImage image = WORKER.getCoverage(request);
        // Test on the returned image.
        assertEquals(image.getWidth(), 1024);
        assertEquals(image.getHeight(), 512);
        // Test the checksum of the image, if the image is indexed (and its values of type byte).
        // TODO: the image should have indexed colors. Find the origin of the conversion from
        //       indexed color to RGB (int values).
//        assertEquals(Commons.checksum(image), 3183786073L);
    }
}
