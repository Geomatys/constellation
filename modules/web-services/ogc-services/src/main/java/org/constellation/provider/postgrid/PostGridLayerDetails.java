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
package org.constellation.provider.postgrid;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;
import org.constellation.catalog.CatalogException;
import org.constellation.catalog.Database;
import org.constellation.coverage.catalog.GridCoverageTable;
import org.constellation.coverage.catalog.Layer;
import org.constellation.coverage.catalog.Series;
import org.constellation.ws.Service;
import org.constellation.map.PostGridMapLayer;
import org.constellation.map.PostGridMapLayer2;
import org.constellation.map.PostGridReader;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.NamedStyleDP;
import org.constellation.query.wms.GetFeatureInfo;
import org.constellation.query.wms.WMSQuery;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.GraphicBuilder;
import org.geotools.map.MapLayer;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.style.MutableStyle;
import org.geotools.style.StyleConstants;
import org.geotools.style.function.InterpolationPoint;
import org.geotools.style.function.Method;
import org.geotools.style.function.Mode;
import org.geotools.util.MeasurementRange;

import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.operation.TransformException;
import org.opengis.style.ChannelSelection;
import org.opengis.style.ColorMap;
import org.opengis.style.ContrastEnhancement;
import org.opengis.style.Description;
import org.opengis.style.OverlapBehavior;
import org.opengis.style.RasterSymbolizer;
import org.opengis.style.ShadedRelief;
import org.opengis.style.Symbolizer;


/**
 * Regroups information about a {@linkplain Layer layer}.
 *
 * @version $Id$
 * @author Cédric Briançon
 */
class PostGridLayerDetails implements LayerDetails {
    private final PostGridReader reader;

    /**
     * Favorites styles associated with this layer.
     */
    private final List<String> favorites;

    /**
     * Stores information about a {@linkplain Layer layer} in a {@code PostGRID}
     * {@linkplain Database database}.
     *
     * @param database The database connection.
     * @param layer The layer to consider in the database.
     */
    PostGridLayerDetails(final PostGridReader reader, final List<String> favorites) {
        this.reader = reader;

        if (favorites == null) {
            this.favorites = Collections.emptyList();
        } else {
            this.favorites = favorites;
        }
    }

    /**
     * {@inheritDoc}
     */
    public GridCoverage2D getCoverage(final Envelope envelope, final Dimension dimension,
            final Double elevation, final Date time) throws CatalogException, IOException
    {
        final ReferencedEnvelope objEnv = new ReferencedEnvelope(envelope);
        final int width = dimension.width;
        final int height = dimension.height;
        final Envelope genv;
        try {
            genv = CRS.transform(objEnv, DefaultGeographicCRS.WGS84);
        } catch (TransformException ex) {
            throw new CatalogException(ex);
        }
        final GeneralEnvelope renv = new GeneralEnvelope(genv);

        //Create BBOX-----------------------------------------------------------
        final GeographicBoundingBox bbox;
        try {
            bbox = new GeographicBoundingBoxImpl(renv);
        } catch (TransformException ex) {
            throw new CatalogException(ex);
        }

        //Create resolution-----------------------------------------------------
        final double w = renv.toRectangle2D().getWidth()  / width;
        final double h = renv.toRectangle2D().getHeight() / height;
        final Dimension2D resolution = new org.geotools.resources.geometry.XDimension2D.Double(w, h);

        GridCoverageTable table = reader.getTable();
        table = new GridCoverageTable(table);

        table.setGeographicBoundingBox(bbox);
        table.setPreferredResolution(resolution);
        table.setTimeRange(time, time);
        if (elevation != null) {
            table.setVerticalRange(elevation, elevation);
        } else {
            table.setVerticalRange(null);
        }
        GridCoverage2D coverage = null;
        try {
            coverage = table.getEntry().getCoverage(null);
        } catch (SQLException ex) {
            throw new CatalogException(ex);
        }
        return coverage;
    }

    /**
     * {@inheritDoc}
     */
    public Object getInformationAt(final GetFeatureInfo gfi) throws CatalogException, IOException {
        final ReferencedEnvelope objEnv = new ReferencedEnvelope(gfi.getEnvelope());
        final int width  = gfi.getSize().width;
        final int height = gfi.getSize().height;
        final GridCoverage2D coverage = getCoverage(objEnv, new Dimension(width, height),
                gfi.getElevation(), gfi.getTime());
        // Pixel coordinates in the request.
        final int pixelUpX        = gfi.getX();
        final int pixelUpY        = gfi.getY();
        final double widthEnv     = objEnv.getSpan(0);
        final double heightEnv    = objEnv.getSpan(1);
        final double resX         =      widthEnv  / width;
        final double resY         = -1 * heightEnv / height;
        // Coordinates of the lower corner and upper corner of the objective envelope.
        final double lowerCornerX = (pixelUpX + 0.5) * resX + objEnv.getMinimum(0);
        final double lowerCornerY = (pixelUpY + 0.5) * resY + objEnv.getMaximum(1);

        final GeneralDirectPosition position = new GeneralDirectPosition(lowerCornerX, lowerCornerY);
        position.setCoordinateReferenceSystem(objEnv.getCoordinateReferenceSystem());

        double[] result = null;
        result = coverage.evaluate(position, result);
        return (result == null) ? null : result[0];
    }

    /**
     * {@inheritDoc}
     */
    public MapLayer getMapLayer(Object style, final Map<String, Object> params) {
        return createMapLayer(style, params);
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return reader.getTable().getLayer().getName();
    }

    /**
     * {@inheritDoc}
     */
    public GeneralDirectPosition getPixelCoordinates(final GetFeatureInfo gfi) {
        final ReferencedEnvelope objEnv = new ReferencedEnvelope(gfi.getEnvelope());
        final int width = gfi.getSize().width;
        final int height = gfi.getSize().height;
        final int pixelX = gfi.getX();
        final int pixelY = gfi.getY();
        final double widthEnv     = objEnv.getSpan(0);
        final double heightEnv    = objEnv.getSpan(1);
        final double resX         =      widthEnv  / width;
        final double resY         = -1 * heightEnv / height;
        final double geoX = (pixelX + 0.5) * resX + objEnv.getMinimum(0);
        final double geoY = (pixelY + 0.5) * resY + objEnv.getMaximum(1);
        final GeneralDirectPosition position = new GeneralDirectPosition(geoX, geoY);
        position.setCoordinateReferenceSystem(objEnv.getCoordinateReferenceSystem());
        return position;
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getFavoriteStyles(){
        return favorites;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isQueryable(Service service) {
        return reader.getTable().getLayer().isQueryable(service);
    }

    /**
     * {@inheritDoc}
     */
    public GeographicBoundingBox getGeographicBoundingBox() throws CatalogException {
        return reader.getTable().getLayer().getGeographicBoundingBox();
    }

    /**
     * {@inheritDoc}
     */
    public SortedSet<Date> getAvailableTimes() throws CatalogException {
        return reader.getTable().getLayer().getAvailableTimes();
    }

    /**
     * {@inheritDoc}
     */
    public SortedSet<Number> getAvailableElevations() throws CatalogException {
        return reader.getTable().getLayer().getAvailableElevations();
    }

    /**
     * {@inheritDoc}
     */
    public BufferedImage getLegendGraphic(final Dimension dimension) {
        return reader.getTable().getLayer().getLegend((dimension != null) ? dimension : LEGEND_SIZE);
    }

    /**
     * {@inheritDoc}
     */
    public MeasurementRange<?>[] getSampleValueRanges() {
        return reader.getTable().getLayer().getSampleValueRanges();
    }

    /**
     * {@inheritDoc}
     */
    public Set<Series> getSeries() {
        return reader.getTable().getLayer().getSeries();
    }

    /**
     * {@inheritDoc}
     */
    public String getRemarks() {
        return reader.getTable().getLayer().getRemarks();
    }

    /**
     * {@inheritDoc}
     */
    public String getThematic() {
        return reader.getTable().getLayer().getThematic();
    }

    private MapLayer createMapLayer(Object style, final Map<String, Object> params) {
        final PostGridMapLayer mapLayer = new PostGridMapLayer(reader);

        mapLayer.setName(getName());
        
        if (style == null) {
            //no style provided, try to get the favorite one
            if (favorites.size() > 0) {
                //there are some favorites styles
                style = favorites.get(0);
            } else {
                //no favorites defined, create a default one
                style = RANDOM_FACTORY.createRasterStyle();
            }
        }

        if (style instanceof String) {
            //the given style is a named style
            style = NamedStyleDP.getInstance().get((String)style);
            if (style == null) {
                //something is wrong, the named style doesnt exist, create a default one
                style = RANDOM_FACTORY.createRasterStyle();
            }
        }

        if (style instanceof MutableStyle) {
            //style is a commun SLD style
            mapLayer.setStyle((MutableStyle) style);
        } else if (style instanceof GraphicBuilder) {
            //special graphic builder
            mapLayer.setStyle(RANDOM_FACTORY.createRasterStyle());
            mapLayer.graphicBuilders().add((GraphicBuilder) style);
        } else {
            //style is unknowed type, use a random style
            mapLayer.setStyle(RANDOM_FACTORY.createRasterStyle());
        }

        if (params != null) {
            mapLayer.setDimRange((MeasurementRange) params.get(WMSQuery.KEY_DIM_RANGE));
            final Double elevation = (Double) params.get(WMSQuery.KEY_ELEVATION);
            if (elevation != null) {
                mapLayer.setElevation(elevation);
            }
            final Date time = (Date) params.get(WMSQuery.KEY_TIME);
            if (time != null) {
                mapLayer.times().add(time);
            }
        }


        return mapLayer;
    }
    
    private MutableStyle toStyle(final MeasurementRange dimRange) {
        final List<InterpolationPoint> values = new ArrayList<InterpolationPoint>();
        values.add(STYLE_FACTORY.createInterpolationPoint(
                        STYLE_FACTORY.colorExpression(Color.WHITE), dimRange.getMinimum()));
        values.add(STYLE_FACTORY.createInterpolationPoint(
                        STYLE_FACTORY.colorExpression(Color.BLUE), dimRange.getMaximum()));
        final Literal lookup = StyleConstants.DEFAULT_CATEGORIZE_LOOKUP;
        final Literal fallback = StyleConstants.DEFAULT_FALLBACK;
        final Function interpolateFunction = STYLE_FACTORY.createInterpolateFunction(
                lookup, values, Method.COLOR, Mode.LINEAR, fallback);

        final ChannelSelection selection = STYLE_FACTORY.createChannelSelection(
                STYLE_FACTORY.createSelectedChannelType("0", STYLE_FACTORY.literalExpression(1)));

        final Expression opacity = STYLE_FACTORY.literalExpression(1f);
        final OverlapBehavior overlap = OverlapBehavior.LATEST_ON_TOP;
        final ColorMap colorMap = STYLE_FACTORY.createColorMap(interpolateFunction);
        final ContrastEnhancement enhanced = StyleConstants.DEFAULT_CONTRAST_ENHANCEMENT;
        final ShadedRelief relief = StyleConstants.DEFAULT_SHADED_RELIEF;
        final Symbolizer outline = null; //createRealWorldLineSymbolizer();
        final Unit uom = NonSI.FOOT;
        final String geom = StyleConstants.DEFAULT_GEOM;
        final String name = "raster symbol name";
        final Description desc = StyleConstants.DEFAULT_DESCRIPTION;

        final RasterSymbolizer symbol = STYLE_FACTORY.createRasterSymbolizer(opacity, selection,
                overlap, colorMap, enhanced, relief, outline, uom, geom, name, desc);
        
        return STYLE_FACTORY.createStyle(symbol);
    }
}
