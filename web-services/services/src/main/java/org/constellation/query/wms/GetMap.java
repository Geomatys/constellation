/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
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
package org.constellation.query.wms;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Date;
import java.util.List;
import org.constellation.query.QueryRequest;
import org.geotools.sld.MutableStyledLayerDescriptor;
import org.geotools.util.MeasurementRange;
import org.opengis.geometry.Envelope;


/**
 * Representation of a {@code WMS GetMap} request, with its parameters.
 *
 * @version $Id$
 * @author Cédric Briançon
 */
public class GetMap extends WMSQuery {
    /**
     * Envelope which contains the bounds and the crs for the request.
     */
    private final Envelope envelope;

    /**
     * Format of the request, equivalent to the mime-type of the output file.
     */
    private final String format;

    /**
     * List of layers to request.
     */
    private final List<String> layers;

    /**
     * List of style names to apply.
     */
    private final List<String> styles;

    /**
     * Elevation to request in a nD layer. Optional.
     */
    private final Double elevation;

    /**
     * Date to request in a nD layer. It can be a period. Optional.
     */
    private final Date date;

    /**
     * Range value to define a color pal.
     */
    private final MeasurementRange dimRange;

    /**
     * Dimension of the output file, which matches with the {@code Width} and {@code Height} parameters.
     */
    private final Dimension size;

    /**
     * Background color. Optional.
     */
    private final Color background;

    /**
     * Transparent attribute. Optional.
     */
    private final Boolean transparent;

    /**
     * SLD definition to apply as a style for this layer.
     */
    private final MutableStyledLayerDescriptor sld;

    /**
     * Exceptions format. Optional.
     */
    private final String exceptions;

    /**
     * Default minimal constructor to generate a {@code GetMap} request.
     */
    public GetMap(final Envelope envelope, final WMSQueryVersion version, final String format,
                  final List<String> layers, final Dimension size)
    {
        this(envelope, version, format, layers, null, size);
    }

    /**
     * GetMap with a list of styles defined.
     */
    public GetMap(final Envelope envelope, final WMSQueryVersion version, final String format,
                  final List<String> layers, final List<String> styles, final Dimension size)
    {
        this(envelope, version, format, layers, styles, null, null, size);
    }

    /**
     * GetMap with a list of styles, an elevation and a time value.
     */
    public GetMap(final Envelope envelope, final WMSQueryVersion version, final String format,
                  final List<String> layers, final List<String> styles, final Double elevation,
                  final Date date, final Dimension size)
    {
        this(envelope, version, format, layers, styles, elevation, date, null, size);
    }

    /**
     * GetMap with a list of styles, an elevation, a time value and a {@code dim_range}.
     */
    public GetMap(final Envelope envelope, final WMSQueryVersion version, final String format,
                  final List<String> layers, final List<String> styles, final Double elevation,
                  final Date date, final MeasurementRange dimRange, final Dimension size)
    {
        this(envelope, version, format, layers, styles, null, elevation, date, dimRange, size, null, null, null);
    }

    /**
     * Constructor which contains all possible parameters in a {@code GetMap} request.
     */
    public GetMap(final Envelope envelope, final WMSQueryVersion version, final String format,
                  final List<String> layers, final List<String> styles,
                  final MutableStyledLayerDescriptor sld, final Double elevation, final Date date,
                  final MeasurementRange dimRange, final Dimension size, final Color background,
                  final Boolean transparent, final String exceptions)
    {
        super(version);
        this.envelope = envelope;
        this.format = format;
        this.layers = layers;
        this.styles = styles;
        this.sld = sld;
        this.elevation = elevation;
        this.date = date;
        this.dimRange = dimRange;
        this.size = size;
        this.background = background;
        this.transparent = transparent;
        this.exceptions = exceptions;
    }

    /**
     * Copy constructor for subclasses.
     */
    protected GetMap(final GetMap getMap) {
        super(getMap.version);
        this.envelope   = getMap.envelope;
        this.format     = getMap.format;      this.layers      = getMap.layers;
        this.styles     = getMap.styles;      this.sld         = getMap.sld;
        this.elevation  = getMap.elevation;   this.date        = getMap.date;
        this.dimRange   = getMap.dimRange;    this.size        = getMap.size;
        this.background = getMap.background;  this.transparent = getMap.transparent;
        this.exceptions = getMap.exceptions;
    }

    /**
     * Returns the background color, or {@code null} if not defined.
     */
    public Color getBackground() {
        return background;
    }

    /**
     * Returns the date to request in a nD layer, or {@code null} if not defined.
     */
    public Date getDate() {
        return date;
    }

    /**
     * Returns the range value to define a color pal, or {@code null} if not defined.
     */
    public MeasurementRange getDimRange() {
        return dimRange;
    }

    /**
     * Returns the elevation to request in a nD layer, or {@code null} if not defined.
     */
    public Double getElevation() {
        return elevation;
    }

    /**
     * Returns the envelope which contains the bounds and the crs for the request.
     */
    public Envelope getEnvelope() {
        return envelope;
    }

    /**
     * Returns the format of the request, equivalent to the mime-type of the output file.
     */
    public String getFormat() {
        return format;
    }

    /**
     * Returns the list of layers to request.
     */
    public List<String> getLayers() {
        return layers;
    }

    /**
     * Returns the dimension of the output file, which matches with the {@code Width}
     * and {@code Height} parameters.
     */
    public Dimension getSize() {
        return size;
    }

    /**
     * Returns the SLD definition to apply as a style for this layer, or {@code null} if not defined.
     */
    public MutableStyledLayerDescriptor getSld() {
        return sld;
    }

    /**
     * Returns the list of style names to apply, or {@code null} if not defined.
     */
    public List<String> getStyles(){
        return styles;
    }

    /**
     * Transparent attribute, or {@code null} if not defined.
     */
    public Boolean getTransparent() {
        return transparent;
    }

    /**
     * Returns the exception format specified, or {@code "application/vnd.ogc.se_xml"}
     * if {@code null}.
     */
    public String getExceptionFormat() {
        return (exceptions == null) ? "application/vnd.ogc.se_xml" : exceptions;
    }

    /**
     * {@inheritDoc}
     */
    public QueryRequest getRequest() {
        return WMSQueryRequest.GET_MAP;
    }
}
