/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2007, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package net.seagis.coverage.web;

import java.awt.Color;
import java.io.*;
import java.util.*;
import java.text.ParseException;
import javax.imageio.ImageIO;
import javax.media.jai.Interpolation;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.util.NumberRange;
import org.geotools.util.logging.Logging;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;

import net.seagis.catalog.Database;
import static net.seagis.coverage.wms.WMSExceptionCode.*;


/**
 * Produces {@linkplain RenderedImage rendered images} from Web Service parameters.
 * This class assigns parameters to {@link ImageProducer} from {@link String} compliants
 * to WCS or WMS standards.
 * <p>
 * <strong>This class is not thread-safe</strong>. Multi-threads application shall
 * use one instance per thread. The first instance shall be created using the
 * {@linkplain #WebServiceWorker(Database) constructor expecting a database connection},
 * and every additional instance connected to the same database shall be created using
 * the {@linkplain #WebServiceWorker(WebServiceWorker) copy constructor}. This approach
 * enables sharing of some common structures for efficienty.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Guilhem Legal
 * @author Sam Hiatt
 *
 * @todo Some table-related fields in this class, together with some caches, should move in a
 *       more global class and be shared for every instances connected to the same database.
 */
public class WebServiceWorker extends ImageProducer {
    /**
     * WMS before this version needs longitude before latitude. WMS after this version don't
     * perform axis switch. WMS at this exact version switch axis only for EPSG:4326.
     */
    private static final ServiceVersion AXIS_SWITCH_THRESHOLD = new ServiceVersion(Service.WMS, "1.1");

    /**
     * The EPSG code for the CRS for which to switch axis in the version
     * given by {@link #AXIS_SWITCH_THRESHOLD}.
     */
    private static final Integer AXIS_SWITCH_EXCEPTION = 4326;

    /**
     * The suffix for format that allow 16 bits indexed color model. This is used
     * mostly for debugging purpose and doesn't need to be mentioned in public API.
     * {@code "nosd"} is for <cite>no scale down</cite> from 16 to 8 bits.
     */
    private static final String NO_SCALE_DOWN = "-nosd";

    /**
     * List of valid formats. Will be created only when first needed.
     */
    private transient Set<String> formats;

    /**
     * Creates a new image producer connected to the specified database.
     *
     * @param database The connection to the database.
     * @param jmx {@code true} for enabling JMX management, or {@code false} otherwise.
     */
    public WebServiceWorker(final Database database, final boolean jmx) {
        super(database, jmx);
    }

    /**
     * Creates a new image producer connected to the same database than the specified worker.
     * This constructor is used for creating many worker instance to be used in multi-threads
     * application.
     */
    public WebServiceWorker(final WebServiceWorker worker) {
        super(worker);
    }

    /**
     * Parses a value as an integer.
     *
     * @param  name  The parameter name.
     * @param  value The value to be parsed as a string.
     * @return The value as an integer.
     * @throws WebServiceException if the value can't be parsed.
     */
    private int parseInt(final String name, String value) throws WebServiceException {
        if (value == null) {
            throw new WMSWebServiceException(Errors.format(ErrorKeys.MISSING_PARAMETER_VALUE_$1, name),
                    MISSING_PARAMETER_VALUE, version);
        }
        value = value.trim();
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new WMSWebServiceException(Errors.format(ErrorKeys.NOT_AN_INTEGER_$1, value),
                    exception, INVALID_PARAMETER_VALUE, version);
        }
    }

    /**
     * Parses a value as a floating point.
     *
     * @throws WebServiceException if the value can't be parsed.
     */
    private double parseDouble(String value) throws WebServiceException {
        value = value.trim();
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            throw new WMSWebServiceException(Errors.format(ErrorKeys.NOT_A_NUMBER_$1, value),
                    exception, INVALID_PARAMETER_VALUE, version);
        }
    }

    /**
     * Sets the version of the web service. This method should be invoked before any other
     * setters in this class, since it may affect the parsing of strings.
     *
     * @param  service The kind of service.
     * @param  version The WMS version, or {@code null} if unknown.
     *         If null, latest version is assumed.
     * @throws WebServiceException if the version string can't be parsed
     */
    public void setService(final String service, final String version) throws WebServiceException {
        Service serv = (service != null) ? Service.valueOf(service.trim().toUpperCase()) : null;
        this.version = (version != null) ? new ServiceVersion(serv, version) : null;
    }

   /**
     * Sets the layer of interest.
     *
     * @param  layer The layer, or {@code null} if unknown.
     * @throws WebServiceException if the layer is not recognize.
     */
    public void setLayer(String layer) throws WebServiceException {
        if (layer == null) {
            this.layer = null;
        } else {
            layer = layer.trim();
            if (!layer.equals(this.layer)) {
                this.layer = layer;
                /*
                 * Changing the layer do not change the format, so it should not be strictly
                 * necessary to dispose the previous image writer (if any). However we clear
                 * the writer as a safety in order to get more determinist behavior. See the
                 * writer field javadoc for details.
                 */
                disposeWriter();
            }
        }
    }

    /**
     * Parses a coordinate reference system from a code.
     *
     * @param  code The coordinate reference system code. Should never be {@code null}.
     * @return The parsed coordinate reference system.
     * @throws WebServiceException if no CRS object can be built from the given code.
     */
    private CoordinateReferenceSystem decodeCRS(final String code) throws WebServiceException {
        final int versionThreshold;
        if (version != null && Service.WMS.equals(version.getService())) {
            versionThreshold = version.compareTo(AXIS_SWITCH_THRESHOLD, 2);
        } else {
            versionThreshold = 1;
        }
        CoordinateReferenceSystem crs;
        try {
            crs = CRS.decode(code, versionThreshold < 0);
            if (versionThreshold == 0 && AXIS_SWITCH_EXCEPTION.equals(CRS.lookupEpsgCode(crs, false))) {
                crs = DefaultGeographicCRS.WGS84;
            }
        } catch (FactoryException exception) {
            try {
                /*
                 * Wasn't a normal EPSG code...
                 * Try creating a CRS from a definition in the "spatial_ref_sys" table.
                 */
                crs = getSpatialReferenceSystem(code);
            } catch (FactoryException ex) {
                /*
                 * Logs the exception at the FINE level rather than the WARNING one because we are
                 * already throwing an exception and we don't want to duplicate it with logs. This
                 * exception may be normal - we do not control what the user provides. This is
                 * different than WARNING which is more often for unexpected errors that are not
                 * user's fault.
                 */
                Logging.recoverableException(LOGGER, WebServiceWorker.class, "decodeCRS", ex);
                throw new WMSWebServiceException(Errors.format(
                        ErrorKeys.ILLEGAL_COORDINATE_REFERENCE_SYSTEM), exception, INVALID_CRS, version);
            }
        }
        return crs;
    }

    /**
     * Sets the coordinate reference system from a code. Invoking this method will erase
     * any bounding box that may have been previously set.
     *
     * @param  code The coordinate reference system code, or {@code null} if unknown.
     * @throws WebServiceException if no CRS object can be built from the given code.
     */
    public void setCoordinateReferenceSystem(final String code) throws WebServiceException {
        clearGridGeometry();
        if (code == null) {
            envelope = null;
            return;
        }
        final CoordinateReferenceSystem crs = decodeCRS(code);
        envelope = new GeneralEnvelope(crs);
        envelope.setToInfinite();
    }

    /**
     * Sets the response coordinate reference system from a code.
     *
     * @param  code The coordinate reference system code, or {@code null} if none.
     * @throws WebServiceException if no CRS object can be built from the given code.
     */
    public void setResponseCRS(final String code) throws WebServiceException {
        responseCRS = (code != null) ? decodeCRS(code) : null;
    }

    /**
     * Sets the bounding box. The expected order is
     * (<var>x</var><sub>min</sub>, <var>y</var><sub>min</sub>,
     *  <var>x</var><sub>max</sub>, <var>y</var><sub>max</sub>,
     *  <var>z</var><sub>min</sub>, <var>z</var><sub>max</sub>)
     *
     * @param  The bounding box, or {@code null} if unknown.
     * @throws WebServiceException if the given bounding box can't be parsed.
     */
    @SuppressWarnings("fallthrough")
    public void setBoundingBox(final String bbox) throws WebServiceException {
        clearGridGeometry();
        if (bbox == null) {
            if (envelope != null) {
                envelope.setToInfinite();
            }
            return;
        }
        final StringTokenizer tokens = new StringTokenizer(bbox, ",;");
        if (envelope == null) {
            envelope = new GeneralEnvelope((tokens.countTokens() + 1) >> 1);
            envelope.setToInfinite();
        }
        final double[] coordinates = new double[envelope.getDimension() * 2];
        int index = 0;
        while (tokens.hasMoreTokens()) {
            final double value = parseDouble(tokens.nextToken());
            if (index >= coordinates.length) {
                throw new WMSWebServiceException(Errors.format(ErrorKeys.MISMATCHED_DIMENSION_$3, "envelope",
                        ((index + tokens.countTokens()) >> 1) + 1, envelope.getDimension()),
                        INVALID_DIMENSION_VALUE, version);
            }
            coordinates[index++] = value;
        }
        if ((index & 1) != 0) {
            throw new WMSWebServiceException(Errors.format(ErrorKeys.ODD_ARRAY_LENGTH_$1, index),
                    INVALID_DIMENSION_VALUE, version);
        }
        // Fallthrough in every cases.
        switch (index) {
            default: {
                while (index >= 6) {
                    final double maximum = coordinates[--index];
                    final double minimum = coordinates[--index];
                    envelope.setRange(index >> 1, minimum, maximum);
                }
            }
            case 4: envelope.setRange(1, coordinates[1], coordinates[3]);
            case 3:
            case 2: envelope.setRange(0, coordinates[0], coordinates[2]);
            case 1:
            case 0: break;
        }
        /*
         * Checks the envelope validity. Given that the parameter order in the bounding box
         * is a little-bit counter-intuitive, it is worth to perform this check in order to
         * avoid a NonInvertibleTransformException at some later stage.
         */
        final int dimension = envelope.getDimension();
        for (index=0; index<dimension; index++) {
            final double minimum = envelope.getMinimum(index);
            final double maximum = envelope.getMaximum(index);
            if (!(minimum < maximum)) {
                throw new WMSWebServiceException(Errors.format(ErrorKeys.BAD_RANGE_$2, minimum, maximum),
                        INVALID_PARAMETER_VALUE, version);
            }
        }
    }

    /**
     * Sets the resolution. This method is exclusive with {@link #setDimension setDimension};
     * only one of those methods should be invoked.
     *
     * @param  resx  Spatial resolution along axis X of the reply CRS.
     * @param  resy  Spatial resolution along axis Y of the reply CRS.
     * @param  resz  Spatial resolution along axis Z of the reply CRS (not yet used).
     *
     * @throws WebServiceException if the resolution can't be parsed from the given strings.
     */
    public void setResolution(final String resx, final String resy, final String resz) throws WebServiceException {
        clearGridGeometry();
        if (resx == null && resy == null) {
            gridRange = null;
            return;
        }
        double resolutionX = parseDouble(resx);
        double resolutionY = parseDouble(resy);
        final int[] upper = new int[] {
            (int) Math.round(envelope.getLength(0) / resolutionX),
            (int) Math.round(envelope.getLength(1) / resolutionY)
        };
        gridRange = new GeneralGridRange(new int[upper.length], upper);
    }

    /**
     * Sets the dimension, or {@code null} if unknown. If a value is null, the other
     * one must be null as well otherwise a {@link WebServiceException} is thrown.
     *
     * @param  width  The image width.
     * @param  height The image height.
     * @param  depth  The image depth (not yet used).
     *
     * @throws WebServiceException if the dimension can't be parsed from the given strings.
     */
    public void setDimension(final String width, final String height, final String depth) throws WebServiceException {
        clearGridGeometry();
        if (width == null && height == null) {
            gridRange = null;
            return;
        }
        final int[] upper = new int[] {
            parseInt("width",  width),
            parseInt("height", height)
        };
        gridRange = new GeneralGridRange(new int[upper.length], upper);
    }

    /**
     * Sets the <cite>grid to CRS</cite> transform from a grid origin and offset vectors.
     *
     * @param gridOrigin  The origin in "real world" coordinates.
     * @param gridOffsets The offset vectors in "real world" units.
     * @throws WebServiceException if the dimension can't be parsed from the given strings.
     */
    public void setGridCRS(String gridOrigin, String gridOffsets) throws WebServiceException {
        clearGridGeometry();
        if (gridOffsets == null || gridOrigin == null) {
            gridToCRS = null;
        } else {
            gridOrigin  = gridOrigin.trim();
            gridOffsets = gridOffsets.trim();
            // Extracts the origin parameters
            int i = 0;
            StringTokenizer tokens = new StringTokenizer(gridOrigin, ",;");
            final double[] origin = new double[tokens.countTokens()];
            while (tokens.hasMoreTokens()) {
                origin[i++] = parseDouble(tokens.nextToken());
            }
            // Extracts the offsets parameters
            final double[] offsets = new double[origin.length * 2];
            i = 0;
            tokens = new StringTokenizer(gridOffsets, ",;");
            while (tokens.hasMoreTokens()) {
                if (i >= offsets.length) {
                    throw new WMSWebServiceException(Errors.format(ErrorKeys.MISMATCHED_DIMENSION_$3,
                            "gridOffsets", (int) Math.ceil(Math.sqrt((i + tokens.countTokens()) + 1)),
                            origin.length), INVALID_DIMENSION_VALUE, version);
                }
                offsets[i++] = parseDouble(tokens.nextToken());
            }
            /*
             * builds an AffineTransform object like this (origin=T, offsets=G):
             * | G(0) G(1) T(0)|
             * | G(2) G(3) T(1)|
             * | 0    0    1   |
             */
            gridToCRS = new AffineTransform2D(offsets[0], offsets[2], offsets[1], offsets[3], origin[0], origin[1]);
        }
    }

    /**
     * Sets the time, or {@code null} if unknown.
     *
     * @param  elevation The elevation.
     * @throws WebServiceException if the elevation can't be parsed from the given string.
     *
     * @todo Needs to take the whole list in account.
     */
    public void setTime(String date) throws WebServiceException {
        if (date == null) {
            time = null;
            return;
        }
        final List<Date> dates;
        try {
            dates = TimeParser.parse(date.trim(), TimeParser.MILLIS_IN_DAY);
        } catch (ParseException exception) {
            throw new WMSWebServiceException(exception, INVALID_PARAMETER_VALUE, version);
        }
        if (dates.isEmpty()) {
            time = null;
        } else {
            time = dates.get(0);
        }
    }

    /**
     * Sets the elevation, or {@code null} if unknown.
     *
     * @param  elevation The elevation.
     * @throws WebServiceException if the elevation can't be parsed from the given string.
     */
    public void setElevation(final String elevation) throws WebServiceException {
        if (elevation == null) {
            this.elevation = null;
        } else {
            this.elevation = parseDouble(elevation);
        }
    }

    /**
     * Sets the interpolation method to use for resampling.
     * If not set the default value is {@link Interpolation#INTERP_BILINEAR}.
     *
     * @param interpolation The name of the requested interpolation method.
     */
    public void setInterpolation(String interpolation) throws WebServiceException {
        final int code;
        if (interpolation != null) {
            interpolation = interpolation.trim();
            if (interpolation.equalsIgnoreCase("bicubic") || interpolation.equalsIgnoreCase("cubic")) {
                code = Interpolation.INTERP_BICUBIC;
            } else if (interpolation.equalsIgnoreCase("nearest neighbor") || interpolation.equalsIgnoreCase("nearest")) {
                code = Interpolation.INTERP_NEAREST;
            } else if (interpolation.equalsIgnoreCase("bilinear") || interpolation.equalsIgnoreCase("linear"))  {
                code = Interpolation.INTERP_BILINEAR;
            } else {
                // Unsupported interpolations include "barycentric" and "lost area".
                throw new WMSWebServiceException("The service does not handle the \"" +
                        interpolation.toLowerCase() + "\" interpolation method.",
                        INVALID_PARAMETER_VALUE, version);
            }
        } else {
            code = DEFAULT_INTERPOLATION;
        }
        this.interpolation = Interpolation.getInstance(code);
    }

    /**
     * Sets the range on value on which to apply a color ramp.
     */
    public void setColormapRange(String range) throws WebServiceException {
       if (range == null) {
           colormapRange = null;
       } else {
           range = range.trim();
           final int split = range.indexOf(',');
           if (split < 0 || range.indexOf(',', split+1) >= 0) {
               throw new WMSWebServiceException(Errors.format(ErrorKeys.ILLEGAL_ARGUMENT_$2,
                       "range", range), INVALID_PARAMETER_VALUE, version);
           }
           final double min = parseDouble(range.substring(0,  split));
           final double max = parseDouble(range.substring(split + 1));
           colormapRange = NumberRange.create(min, max);
       }
    }

    /**
     * Sets the background Color of the requested image.
     * if not set the default value is {@code 0xFFFFFF}.
     *
     * @param background an hexadecimal description of a color.
     */
    public void setBackgroundColor(String background) throws WebServiceException {
        if (background == null) {
            this.background = Color.WHITE;
        } else {
            background = background.trim();
            try {
                this.background = Color.decode(background);
            } catch (NumberFormatException exception) {
                throw new WMSWebServiceException(Errors.format(ErrorKeys.ILLEGAL_ARGUMENT_$2,
                        "background", background), exception, INVALID_PARAMETER_VALUE, version);
            }
        }
    }

    /**
     * Sets the background transparency of the requested image.
     * If not set the default value {@code false}.
     *
     * @param a string representing a boolean.
     */
    public void setTransparency(String transparent) {
        if (transparent != null) {
            transparent = transparent.trim();
        }
        // Reminder: 'parseBoolean' accepts null.
        this.transparent = Boolean.parseBoolean(transparent);
    }

    /**
     * Sets the output format as a MIME type.
     *
     * @param  format The output format.
     * @throws WebServiceException if the format is invalid.
     */
    public void setFormat(String format) throws WebServiceException {
        indexedShortAllowed = false;
        if (format == null) {
            this.format = null;
        } else {
            format = format.trim();
            final String formatLC = format.toLowerCase();
            if (formatLC.endsWith(NO_SCALE_DOWN)) {
                format = format.substring(0, format.length() - NO_SCALE_DOWN.length());
                indexedShortAllowed = true;
            } else if (!formatLC.endsWith("png")) {
                // Formats like jpeg can work with 16-bits.
                indexedShortAllowed = true;
            }
            if (!format.equals(this.format)) {
                if (formats == null) {
                    formats = new HashSet<String>(Arrays.asList(ImageIO.getWriterMIMETypes()));
                    formats.addAll(Arrays.asList(ImageIO.getWriterFormatNames()));
                }
                if (!formats.contains(format)) {
                    throw new WMSWebServiceException(Errors.format(ErrorKeys.ILLEGAL_ARGUMENT_$2,
                            "format", format), LAYER_NOT_QUERYABLE, version);
                }
                this.format = format;
                /*
                 * Changing the format will change the image writer, so
                 * we need to dispose the previous one if there is any.
                 */
                disposeWriter();
            }
        }
    }

    /**
     * Sets the output format for exception as a MIME type.
     *
     * @param format the output exception format.
     * @throws WebServiceException if the format is invalid.
     */
    public void setExceptionFormat(String format) throws WebServiceException {
        if (format == null) {
            exceptionFormat = "application/vnd.ogc.se_xml";
            return;
        }
        format = format.trim();
        if (format.equalsIgnoreCase("text/xml") || format.equalsIgnoreCase("application/vnd.ogc.se_xml")) {
            exceptionFormat = format;
        } else {
            throw new WMSWebServiceException(Errors.format(ErrorKeys.ILLEGAL_ARGUMENT_$2,
                            "Exception", format), INVALID_PARAMETER_VALUE, version);
        }
    }

    /**
     * Clears the cache. This method should be invoked when the database content changed.
     * This {@code WebServiceWorker} instance can still be used, but the first next invocation
     * may be a little bit slower until the cache is rebuild.
     *
     * @throws WebServiceException if an error occured while clearing the cache.
     */
    @Override
    public void flush() throws WebServiceException {
        formats = null;
        super.flush();
    }
}
