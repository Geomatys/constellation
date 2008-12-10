/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
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
package org.constellation.coverage.ws.rs;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.constellation.gml.v311.DirectPositionType;
import org.constellation.gml.v311.EnvelopeEntry;
import org.constellation.portrayal.CSTLPortrayalService;
import org.constellation.query.wcs.WCSQuery;
import org.constellation.query.wms.WMSQuery;
import org.constellation.wcs.AbstractGetCoverage;
import org.constellation.ws.Service;
import org.constellation.ws.ServiceVersion;
import org.constellation.ws.WebServiceException;

import org.geotools.display.exception.PortrayalException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import static org.constellation.ws.ExceptionCode.*;

/**
 * Adapt WCS queries to generic portrayal parameters.
 *
 * @author Johann Sorel (Geomatys)
 */
public class WCSPortrayalAdapter {

    public static BufferedImage portray(final AbstractGetCoverage query)
            throws PortrayalException, WebServiceException
    {
        if (query == null) {
            throw new NullPointerException("The GetMap query cannot be null. The portray() method" +
                    " is not well used here.");
        }
        final List<String> layers = new ArrayList<String>();
        final ServiceVersion version;
        final Map<String, Object> params = new HashMap<String, Object>();
        final ReferencedEnvelope refEnv;
        final Dimension dimension;
        if (query instanceof org.constellation.wcs.v100.GetCoverage) {
            final org.constellation.wcs.v100.GetCoverage query100 = (org.constellation.wcs.v100.GetCoverage) query;
            layers.add(query100.getSourceCoverage());
            version = new ServiceVersion(Service.WCS, query100.getVersion());
            // Decode the CRS.
            String crsCode = query100.getOutput().getCrs().getValue();
            if (crsCode == null) {
                crsCode = query100.getDomainSubset().getSpatialSubSet().getEnvelope().getSrsName();
            }
            if (!crsCode.contains(":")) {
                crsCode = "EPSG:" + crsCode;
            }
            final CoordinateReferenceSystem crs;
            try {
                crs = CRS.decode(crsCode);
            } catch (NoSuchAuthorityCodeException ex) {
                throw new WebServiceException(ex, INVALID_CRS, version);
            } catch (FactoryException ex) {
                throw new WebServiceException(ex, INVALID_CRS, version);
            }
            // Calculate the bbox.
            final EnvelopeEntry envEntry = query100.getDomainSubset().getSpatialSubSet().getEnvelope();
            final List<DirectPositionType> positions = envEntry.getPos();
            refEnv = new ReferencedEnvelope(positions.get(0).getValue().get(0), positions.get(0).getValue().get(1),
                                            positions.get(1).getValue().get(0), positions.get(1).getValue().get(1), crs);
            // Additionnal parameters.
            if (query100.getDomainSubset().getTemporalSubSet() != null &&
                query100.getDomainSubset().getTemporalSubSet().getTimePositionOrTimePeriod().size() > 0)
            {
                params.put(WCSQuery.KEY_TIME, query100.getDomainSubset().getTemporalSubSet().getTimePositionOrTimePeriod().get(0));
            }
            if (envEntry.getPos().get(0).getValue().size() > 2) {
                params.put(WMSQuery.KEY_ELEVATION, positions.get(2).getValue().get(0));
            }
            final int width =
                    query100.getDomainSubset().getSpatialSubSet().getGrid().getLimits().getGridEnvelope().getHigh().get(0).intValue() -
                    query100.getDomainSubset().getSpatialSubSet().getGrid().getLimits().getGridEnvelope().getLow().get(0).intValue();
            final int height =
                    query100.getDomainSubset().getSpatialSubSet().getGrid().getLimits().getGridEnvelope().getHigh().get(1).intValue() -
                    query100.getDomainSubset().getSpatialSubSet().getGrid().getLimits().getGridEnvelope().getLow().get(1).intValue();
            dimension = new Dimension(width, height);
        } else {
            final org.constellation.wcs.v111.GetCoverage query111 = (org.constellation.wcs.v111.GetCoverage) query;
            layers.add(query111.getIdentifier().getValue());
            version = new ServiceVersion(Service.WCS, query111.getVersion());
            // Decode the CRS.
            String crsCode = query111.getOutput().getGridCRS().getSrsName().getValue();
            if (crsCode == null) {
                crsCode = query111.getDomainSubset().getBoundingBox().getValue().getCrs();
            }
            if (!crsCode.contains(":")) {
                crsCode = "EPSG:" + crsCode;
            }
            final CoordinateReferenceSystem crs;
            try {
                crs = CRS.decode(crsCode);
            } catch (NoSuchAuthorityCodeException ex) {
                throw new WebServiceException(ex, INVALID_CRS, version);
            } catch (FactoryException ex) {
                throw new WebServiceException(ex, INVALID_CRS, version);
            }
            // Calculate the bbox.
            final List<Double> lowerCornerCoords = query111.getDomainSubset().getBoundingBox().getValue().getLowerCorner();
            final List<Double> upperCornerCoords = query111.getDomainSubset().getBoundingBox().getValue().getUpperCorner();
            refEnv = new ReferencedEnvelope(lowerCornerCoords.get(0), lowerCornerCoords.get(1),
                                            upperCornerCoords.get(0), upperCornerCoords.get(1), crs);
            // Additionnal parameters.
            if (query111.getDomainSubset().getTemporalSubset() != null &&
                query111.getDomainSubset().getTemporalSubset().getTimePositionOrTimePeriod().size() > 0)
            {
                params.put(WCSQuery.KEY_TIME, query111.getDomainSubset().getTemporalSubset().getTimePositionOrTimePeriod().get(0));
            }
            if (query111.getDomainSubset().getBoundingBox().getValue().getDimensions().intValue() > 2) {
                params.put(WMSQuery.KEY_ELEVATION, lowerCornerCoords.get(2));
            }
            // TODO: do the good calculation with grid origin, grid offset and the envelope size.
            dimension = new Dimension((int) Math.round(query111.getOutput().getGridCRS().getGridOrigin().get(0)),
                    (int) Math.round(query111.getOutput().getGridCRS().getGridOrigin().get(1)));
        }

        return CSTLPortrayalService.getInstance().portray(
                refEnv, 0, null, dimension,
                layers, null, null, params, version);

    }

}
