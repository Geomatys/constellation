/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009-2010, Geomatys
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

import org.constellation.ws.CstlServiceException;
import org.constellation.ws.Worker;
import org.geotoolkit.wcs.xml.DescribeCoverage;
import org.geotoolkit.wcs.xml.DescribeCoverageResponse;
import org.geotoolkit.wcs.xml.GetCapabilities;
import org.geotoolkit.wcs.xml.GetCapabilitiesResponse;
import org.geotoolkit.wcs.xml.GetCoverage;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public interface WCSWorker extends Worker{

    GetCapabilitiesResponse getCapabilities(final GetCapabilities request) throws CstlServiceException;

    DescribeCoverageResponse describeCoverage(final DescribeCoverage request) throws CstlServiceException;

    Object getCoverage(final GetCoverage request) throws CstlServiceException;
}
