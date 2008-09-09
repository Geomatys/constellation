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
package net.seagis.coverage.web;

import java.awt.Color;
import java.util.Date;
import javax.media.jai.Interpolation;
import static java.lang.Float.floatToIntBits;

import org.geotools.util.NumberRange;
import org.geotools.resources.Utilities;
import org.opengis.coverage.grid.GridGeometry;
import org.geotools.coverage.grid.GeneralGridGeometry;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


/**
 * A request for an image, which may be for a coverage or a legend.
 * To be used as key in {@link java.util.HashMap}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class ImageRequest {
    /**
     * The image type.
     */
    public final ImageType type;

    /**
     * The layer name.
     */
    private final String layer;

    /**
     * The grid geometry (including envelope and CRS) or the grid range (for legends only).
     */
    private final Object geometry;

    /**
     * The CRS of the response.
     */
    private final CoordinateReferenceSystem responseCRS;

    /**
     * The range on value on which to apply a color ramp.
     */
    private final float minimum, maximum;

    /**
     * The requested time.
     */
    private final long time;

    /**
     * The requested elevation.
     */
    private final float elevation;

    /**
     * The interpolation to use for resampling.
     */
    private final Interpolation interpolation;

    /**
     * The output format as a MIME type.
     */
    private final String format;

    /**
     * The background color of the current image (default {@code 0xFFFFFF}).
     */
    private final int background;

    /**
     * A flag specifying if the image have to handle transparency.
     */
    private final boolean transparent;

    /**
     * Creates a new request.
     */
    public ImageRequest(final ImageType                 type,
                        final String                    layer,
                        final GridGeometry              geometry,
                        final CoordinateReferenceSystem responseCRS,
                        final NumberRange               colormapRange,
                        final Date                      time,
                        final Number                    elevation,
                        final Interpolation             interpolation,
                        final String                    format,
                        final Color                     background,
                        final boolean                   transparent)
    {
        this.type      = type;
        this.layer     = layer;
        this.time      = (time != null) ? time.getTime() : Long.MIN_VALUE;
        this.elevation = (elevation != null) ? elevation.floatValue() : Float.NaN;
        this.format    = format;
        if (colormapRange != null) {
            minimum = (float) colormapRange.getMinimum(true);
            maximum = (float) colormapRange.getMaximum(true);
        } else {
            minimum = Float.NEGATIVE_INFINITY;
            maximum = Float.POSITIVE_INFINITY;
        }
        switch (type) {
            case LEGEND: {
                this.geometry      = (geometry != null) ? geometry.getGridRange() : null;
                this.responseCRS   = null;
                this.interpolation = null;
                this.background    = 0;
                this.transparent   = false;
                break;
            }
            default: {
                this.geometry      = geometry;
                this.responseCRS   = responseCRS;
                this.interpolation = interpolation;
                this.background    = (background != null) ? background.getRGB() : 0;
                this.transparent   = transparent;
                break;
            }
        }
    }

    /**
     * Compares this object with the specified one for equality.
     */
    @Override
    public boolean equals(final Object object) {
        if (!(object instanceof ImageRequest)) {
            return false;
        }
        final ImageRequest that = (ImageRequest) object;
        return this.time == that.time &&
               floatToIntBits(this.elevation) == floatToIntBits(that.elevation) &&
               floatToIntBits(this.minimum)   == floatToIntBits(that.minimum)   &&
               floatToIntBits(this.maximum)   == floatToIntBits(that.maximum)   &&
               Utilities.equals(this.layer,         that.layer        ) &&
               Utilities.equals(this.geometry,      that.geometry     ) &&
               Utilities.equals(this.responseCRS,   that.responseCRS  ) &&
               Utilities.equals(this.interpolation, that.interpolation) &&
               Utilities.equals(this.format,        that.format       ) &&
               Utilities.equals(this.type,          that.type         ) &&
               this.background  == that.background &&
               this.transparent == that.transparent;
    }

    /**
     * Returns a hash code value for this request.
     */
    @Override
    public int hashCode() {
        int code = (int) (time) + (int) (time >>> 32);
        code += floatToIntBits(elevation);
        if (layer != null) {
            code += layer.hashCode();
        }
        if (geometry != null) {
            code += geometry.hashCode();
        }
        if (type != null) {
            code += 37 * type.hashCode();
        }
        return code;
    }
}
