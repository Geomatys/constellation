/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2009, Geomatys
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
package org.constellation.wcs;

import java.awt.Dimension;
import org.constellation.ws.AbstractRequest;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


/**
 * Super abstract type for all the different versions of GetCoverage request.
 *
 * @author Guilhem Legal
 * @author Cédric Briançon (Geomatys)
 */
public abstract class AbstractGetCoverage extends AbstractRequest {

    /**
     * Returns the {@link CoordinateReferenceSystem} of the request, or {@code null}
     * if none.
     *
     * @throws FactoryException if the generation of the {@link CoordinateReferenceSystem} fails.
     */
    public abstract CoordinateReferenceSystem getCRS() throws FactoryException;

    /**
     * Returns the coverage name of the request, or {@code null} if none.
     */
    public abstract String getCoverage();

    /**
     * Returns the {@link Envelope} of the request, or {@code null} if none.
     *
     * @throws FactoryException if the generation of the {@link Envelope} fails.
     */
    public abstract Envelope getEnvelope() throws FactoryException;

    /**
     * Returns the output format of the request, or {@code null} if none.
     */
    public abstract String getFormat();

    /**
     * Returns the {@linkplain CoordinateReferenceSystem response CRS} of the request,
     * or {@code null} if none.
     *
     * @throws FactoryException if the generation of the {@link CoordinateReferenceSystem} fails.
     */
    public abstract CoordinateReferenceSystem getResponseCRS() throws FactoryException;

    /**
     * Returns the output size of the request, or {@code null} if none.
     */
    public abstract Dimension getSize();

    /**
     * Returns the time of the request, or {@code null} if none.
     */
    public abstract String getTime();
}
