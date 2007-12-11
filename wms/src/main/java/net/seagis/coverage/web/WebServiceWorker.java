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

import java.io.*;
import java.sql.SQLException;
import java.awt.image.RenderedImage;
import java.util.Date;
import java.util.Map;
import java.util.StringTokenizer;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.util.LRULinkedHashMap;
import org.geotools.util.Version;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;

import net.seagis.catalog.Database;
import net.seagis.catalog.CatalogException;
import net.seagis.catalog.NoSuchRecordException;
import net.seagis.coverage.catalog.CoverageReference;
import net.seagis.coverage.catalog.Layer;
import net.seagis.coverage.catalog.LayerTable;
import net.seagis.resources.i18n.ResourceKeys;
import net.seagis.resources.i18n.Resources;
import static net.seagis.coverage.wms.WMSExceptionCode.*;


/**
 * Produces {@linkplain RenderedImage rendered images} from Web Service parameters.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Guihlem Legal
 */
public class WebServiceWorker {
    /**
     * The connection to the database.
     */
    private final Database database;

    /**
     * WMS before this version needs longitude before latitude. WMS after this version don't
     * perform axis switch. WMS at this exact version switch axis only for EPSG:4326.
     */
    private static final Version AXIS_SWITCH_THRESHOLD = new Version("1.1");

    /**
     * The EPSG code for the CRS for which to switch axis in the version
     * given by {@link #AXIS_SWITCH_THRESHOLD}.
     */
    private static final Integer AXIS_SWITCH_EXCEPTION = 4326;

    /**
     * The kind of service.
     */
    private Service service;

    /**
     * The web service version. This version number need to be interpreted together
     * with {@link #service}.
     */
    private Version version;

    /**
     * The layer name.
     */
    private String layer;

    /**
     * The envelope of current layer, including its CRS.
     */
    private GeneralEnvelope envelope;

    /**
     * The requested time.
     */
    private Date time;

    /**
     * The requested elevation.
     */
    private Number elevation;

    /**
     * The layer table. Will be created when first needed.
     */
    private transient LayerTable layerTable;

    /**
     * The most recently used layers.
     */
    private final transient Map<LayerRequest,Layer> layers = LRULinkedHashMap.createForRecentAccess(12);

    /**
     * Creates a new image producer connected to the specified database.
     *
     * @param database The connection to the database.
     */
    public WebServiceWorker(final Database database) {
        this.database = database;
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
        this.service = (service != null) ? Service.valueOf(service.trim().toUpperCase()) : null;
        this.version = (version != null) ? new Version(version) : null;
    }

    /**
     * Sets the layer of interest.
     *
     * @param  layer The layer, or {@code null} if unknown.
     * @throws WebServiceException if the layer is not recognize.
     */
    public void setLayer(final String layer) throws WebServiceException {
        this.layer = (layer != null) ? layer.trim() : null;
    }

    /**
     * Sets the coordinate reference system from a code. Invoking this method will erase
     * any bounding box that may have been previously set.
     *
     * @param  code The coordinate reference system code, or {@code null} if unknown.
     * @throws WebServiceException if no CRS object can be built from the given code.
     */
    public void setCoordinateReferenceSystem(final String code) throws WebServiceException {
        if (code == null) {
            envelope = null;
            return;
        }
        final int versionThreshold;
        if (Service.WMS.equals(service) && version != null) {
            versionThreshold = version.compareTo(AXIS_SWITCH_THRESHOLD);
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
            throw new WebServiceException(Errors.format(ErrorKeys.ILLEGAL_COORDINATE_REFERENCE_SYSTEM),
                    exception, INVALID_CRS, version);
        }
        envelope = new GeneralEnvelope(crs);
        envelope.setToInfinite();
    }

    /**
     * Sets the bounding box.
     *
     * @param  The bounding box, or {@code null} if unknown.
     * @throws WebServiceException if the given bounding box can't be parsed.
     */
    public void setBoundingBox(final String bbox) throws WebServiceException {
        if (bbox == null) {
            if (envelope != null) {
                envelope.setToInfinite();
            }
            return;
        }
        final StringTokenizer tokens = new StringTokenizer(bbox, ",;");
        if (envelope == null) {
            envelope = new GeneralEnvelope((tokens.countTokens() + 1) >> 1);
        }
        int dimension = 0;
        while (tokens.hasMoreTokens()) {
            final double minimum, maximum;
            String token = tokens.nextToken().trim();
            try {
                minimum = Double.parseDouble(token);
                if (tokens.hasMoreTokens()) {
                    token = tokens.nextToken().trim();
                    maximum = Double.parseDouble(token);
                } else {
                    maximum = minimum;
                }
            } catch (NumberFormatException exception) {
                throw new WebServiceException(Errors.format(ErrorKeys.NOT_A_NUMBER_$1, token),
                        exception, INVALID_PARAMETER_VALUE, version);
            }
            try {
                envelope.setRange(dimension++, minimum, maximum);
            } catch (IndexOutOfBoundsException exception) {
                throw new WebServiceException(Errors.format(ErrorKeys.MISMATCHED_DIMENSION_$3, "envelope",
                        dimension + ((tokens.countTokens() + 1) >> 1), envelope.getDimension()),
                        exception, INVALID_DIMENSION_VALUE, version);
            }
        }
    }

    /**
     * Sets the elevation, or {@code null} if unknown.
     *
     * @param elevation The elevation.
     * @catch WebServiceException if the elevation can't be parsed from the given string.
     */
    public void setElevation(String elevation) throws WebServiceException {
        if (elevation == null) {
            this.elevation = null;
        } else try {
            elevation = elevation.trim();
            this.elevation = Double.parseDouble(elevation);
        } catch (NumberFormatException exception) {
            throw new WebServiceException(Errors.format(ErrorKeys.NOT_A_NUMBER_$1, elevation),
                    exception, INVALID_PARAMETER_VALUE, version);
        }
    }

    /**
     * Returns the layer for the current configuration.
     *
     * @throws WebServiceException if an error occured while fetching the table.
     */
    private Layer getLayer() throws WebServiceException {
        if (layer == null) {
            throw new WebServiceException(Resources.format(ResourceKeys.ERROR_NO_SERIES_SELECTION),
                    LAYER_NOT_DEFINED, version);
        }
        final LayerRequest request = new LayerRequest(layer, envelope, null);
        Layer candidate;
        synchronized (layers) {
            candidate = layers.get(request);
            if (candidate == null) try {
                if (layerTable == null) {
                    layerTable = new LayerTable(database.getTable(LayerTable.class));
                }
                candidate = layerTable.getEntry(layer);
            } catch (NoSuchRecordException exception) {
                throw new WebServiceException(exception, LAYER_NOT_DEFINED, version);
            } catch (CatalogException exception) {
                throw new WebServiceException(exception, LAYER_NOT_QUERYABLE, version);
            } catch (SQLException exception) {
                throw new WebServiceException(exception, LAYER_NOT_QUERYABLE, version);
            }
        }
        return candidate;
    }

    /**
     * Gets the grid coverage for the current layer, time, elevation, <cite>etc.</cite>
     * The image dimension may <strong>not</strong> be honored by this method.
     *
     * @throws WebServiceException if an error occured while querying the coverage.
     */
    public GridCoverage2D getGridCoverage2D() throws WebServiceException {
        final Layer layer = getLayer();
        final CoverageReference ref;
        try {
            ref = layer.getCoverageReference(time, elevation);
        } catch (CatalogException exception) {
            throw new WebServiceException(exception, LAYER_NOT_QUERYABLE, version);
        }
        try {
            return ref.getCoverage(null);
        } catch (IOException exception) {
            throw new WebServiceException(Errors.format(ErrorKeys.CANT_READ_$1, ref.getFile()),
                    exception, LAYER_NOT_QUERYABLE, version);
        }
    }

    /**
     * Gets the image for the current layer. The image is resized to the requested dimension
     * and CRS.
     *
     * @throws WebServiceException if an error occured while querying the coverage.
     */
    public RenderedImage getRenderedImage() throws WebServiceException {
        final GridCoverage2D coverage = getGridCoverage2D();
        // TODO: resample the coverage.
        RenderedImage image = coverage.geophysics(false).getRenderedImage();
        return image;
    }
    
    /**
     * Returns the rendered image as a file.
     *
     * @throws WebServiceException if an error occured while processing the image.
     */
    public File getImageFile() throws WebServiceException {
        // TODO
        return null;
    }
}
