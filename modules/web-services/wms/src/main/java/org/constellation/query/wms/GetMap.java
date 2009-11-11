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
package org.constellation.query.wms;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.constellation.query.QueryRequest;
import org.constellation.util.StringUtilities;
import org.constellation.ws.MimeType;
import org.geotoolkit.geometry.ImmutableEnvelope;
import org.geotoolkit.util.MeasurementRange;
import org.geotoolkit.util.Version;
import org.geotoolkit.util.collection.UnmodifiableArrayList;
import org.opengis.geometry.Envelope;
import org.opengis.sld.StyledLayerDescriptor;


/**
 * Representation of a {@code WMS GetMap} request, with its parameters.
 * This class is nearly immutable exept the StyleLayerDescriptor which might be mutable.
 *
 * @version $Id$
 * @author Cédric Briançon (Geomatys)
 * @author Johann Sorel (Geomatys)
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
     * Time to request in a nD layer. It can be a period. Optional.
     */
    private final Date time;

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
    private final StyledLayerDescriptor sld;

    /**
     * Azimuth, map orientation.
     */
    private final double azimuth;

    /**
     * Exceptions format. Optional.
     */
    private final String exceptions;

    /**
     * Default minimal constructor to generate a {@code GetMap} request.
     */
    public GetMap(final Envelope envelope, final Version version, final String format,
                  final List<String> layers, final Dimension size)
    {
        this(envelope, version, format, layers, new ArrayList<String>(), size);
    }

    /**
     * GetMap with a list of styles defined.
     */
    public GetMap(final Envelope envelope, final Version version, final String format,
                  final List<String> layers, final List<String> styles, final Dimension size)
    {
        this(envelope, version, format, layers, styles, null, null, size);
    }

    /**
     * GetMap with a list of styles, an elevation and a time value.
     */
    public GetMap(final Envelope envelope, final Version version, final String format,
                  final List<String> layers, final List<String> styles, final Double elevation,
                  final Date date, final Dimension size)
    {
        this(envelope, version, format, layers, styles, elevation, date, null, size);
    }

    /**
     * GetMap with a list of styles, an elevation, a time value and a {@code dim_range}.
     */
    public GetMap(final Envelope envelope, final Version version, final String format,
                  final List<String> layers, final List<String> styles, final Double elevation,
                  final Date date, final MeasurementRange dimRange, final Dimension size)
    {
        this(envelope, version, format, layers, styles, null, elevation, date, dimRange, size, null, null, 0, null);
    }

    /**
     * Constructor which contains all possible parameters in a {@code GetMap} request.
     */
    public GetMap(final Envelope envelope, final Version version, final String format,
                  final List<String> layers, final List<String> styles,
                  final StyledLayerDescriptor sld, final Double elevation, final Date date,
                  final MeasurementRange dimRange, final Dimension size, final Color background,
                  final Boolean transparent, double azimuth, final String exceptions)
    {
        super(version);
        this.envelope = new ImmutableEnvelope(envelope);
        this.format = format;
        this.layers = UnmodifiableArrayList.wrap(layers.toArray(new String[layers.size()]));
        this.styles = UnmodifiableArrayList.wrap(styles.toArray(new String[styles.size()]));
        this.sld = sld;
        this.elevation = elevation;
        this.time = date;
        this.dimRange = dimRange;
        this.size = size;
        this.background = background;
        this.transparent = transparent;
        this.exceptions = exceptions;
        this.azimuth = azimuth % 360 ;
    }

    public GetMap(final GetMap getMap, final Boolean transparent) {
        this(   getMap.envelope,
                getMap.getVersion(),
                getMap.format,
                getMap.layers,
                getMap.styles,
                getMap.sld,
                getMap.elevation,
                getMap.time,
                getMap.dimRange,
                getMap.size,
                getMap.background,
                transparent,
                getMap.azimuth,
                getMap.exceptions);
    }

    /**
     * Build a {@link GetMap} request using the parameter values found in the {@code getMap}
     * given, and replacing the {@code layers} value by an immutable singleton list containing
     * the layer specified.
     *
     * @param getMap A {@link GetMap} request.
     * @param layer  The only layer we want to keep for the {@code WMS GetMap} request.
     */
    public GetMap(final GetMap getMap, final String layer) {
        this(   getMap.envelope,
                getMap.getVersion(),
                getMap.format,
                Collections.singletonList(layer),
                getMap.styles,
                getMap.sld,
                getMap.elevation,
                getMap.time,
                getMap.dimRange,
                getMap.size,
                getMap.background,
                getMap.transparent,
                getMap.azimuth,
                getMap.exceptions);
    }

    /**
     * Build a {@link GetMap} request using the parameter values found in the {@code getMap}
     * given, and replacing the {@code layers} value by an immutable singleton list containing
     * the layer specified.
     *
     * @param getMap A {@link GetMap} request.
     * @param layers A list of layers that will be requested, instead of the ones present in the
     *               GetMap request given.
     */
    public GetMap(final GetMap getMap, final List<String> layers) {
        this(   getMap.envelope,
                getMap.getVersion(),
                getMap.format,
                layers,
                getMap.styles,
                getMap.sld,
                getMap.elevation,
                getMap.time,
                getMap.dimRange,
                getMap.size,
                getMap.background,
                getMap.transparent,
                getMap.azimuth,
                getMap.exceptions);
    }

    /**
     * Copy constructor for subclasses.
     */
    protected GetMap(final GetMap getMap) {
        this(   getMap.envelope,
                getMap.getVersion(),
                getMap.format,
                getMap.layers,
                getMap.styles,
                getMap.sld,
                getMap.elevation,
                getMap.time,
                getMap.dimRange,
                getMap.size,
                getMap.background,
                getMap.transparent,
                getMap.azimuth,
                getMap.exceptions);
    }

    /**
     * Returns the background color, or {@code null} if not defined.
     */
    public Color getBackground() {
        return background;
    }

    /**
     * Returns the time to request in a nD layer, or {@code null} if not defined.
     */
    public Date getTime() {
        return time;
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
     * Returns the map orientation in degree, azimuth.
     */
    public double getAzimuth(){
        return azimuth;
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
     * Returns the list of layers to request. This list may be immutable, depending on the
     * constructor chosen.
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
    public StyledLayerDescriptor getSld() {
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
     * Returns the exception format specified, or {@code MimeType.APP_SE_XML}
     * if {@code null}.
     */
    @Override
    public String getExceptionFormat() {
        if (exceptions != null) {
            return exceptions;
        }
        return (super.getVersion().toString().equals("1.1.1")) ?
            MimeType.APP_SE_XML : MimeType.TEXT_XML;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueryRequest getRequest() {
        return GET_MAP;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toKvp() {
        final StringBuilder kvp = new StringBuilder();
        final Version version = getVersion();
        //Obligatory Parameters
        kvp            .append(KEY_REQUEST ).append('=').append(GETMAP)
           .append('&').append(KEY_BBOX    ).append('=').append(StringUtilities.toBboxValue(envelope))
           .append('&').append((version.toString().equals("1.1.1")) ?
                               KEY_CRS_V111 :
                               KEY_CRS_V130).append('=').append(StringUtilities.toCrsCode(envelope))
           .append('&').append(KEY_VERSION ).append('=').append(version)
           .append('&').append(KEY_FORMAT  ).append('=').append(format)
           .append('&').append(KEY_LAYERS  ).append('=').append(StringUtilities.toCommaSeparatedValues(layers))
           .append('&').append(KEY_WIDTH   ).append('=').append(size.width)
           .append('&').append(KEY_HEIGHT  ).append('=').append(size.height)
           .append('&').append(KEY_STYLES  ).append('=').append(StringUtilities.toCommaSeparatedValues(styles));

        //Optional Parameters
        if (sld != null) {
            kvp.append('&').append(KEY_SLD).append('=').append(sld);
        }
        if (elevation != null) {
            kvp.append('&').append(KEY_ELEVATION).append('=').append(elevation);
        }
        if (time != null) {
            kvp.append('&').append(KEY_TIME).append('=').append(time);
        }
        if (dimRange != null) {
            kvp.append('&').append(KEY_DIM_RANGE).append('=').append(dimRange);
        }
        if (background != null) {
            kvp.append('&').append(KEY_BGCOLOR).append('=').append(background);
        }
        if (transparent != null) {
            kvp.append('&').append(KEY_TRANSPARENT).append('=').append(transparent);
        }
        if (azimuth != 0d) {
            kvp.append('&').append(KEY_AZIMUTH).append('=').append(azimuth);
        }
        if (exceptions != null) {
            kvp.append('&').append(KEY_EXCEPTIONS).append('=').append(exceptions);
        }
        return kvp.toString();
    }
}
