/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2010, Geomatys
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

import org.constellation.ServiceDef;
import org.constellation.query.QueryRequest;
import org.constellation.ws.MimeType;

import org.geotoolkit.lang.Immutable;
import org.geotoolkit.sld.xml.Specification.StyledLayerDescriptor;

import org.opengis.feature.type.Name;


/**
 * Representation of a {@code WMS GetLegendGraphic} request, with its parameters.
 *
 * @author Cédric Briançon (Geomatys)
 * @author Johann Sorel (Geomatys)
 */
@Immutable
public final class GetLegendGraphic extends WMSQuery {
    /**
     * Layer to consider.
     */
    private final Name layer;

    /**
     * Format of the legend file returned.
     */
    private final String format;

    /**
     * Width of the generated legend image. Optional.
     */
    private final Integer width;

    /**
     * Height of the generated legend image. Optional.
     */
    private final Integer height;

    /**
     * Style to apply for the legend output. Optional.
     */
    private final String style;

    /**
     * SLD to apply on the legend output. Optional.
     */
    private final String sld;

    /**
     * Version of the SLD.
     */
    private final StyledLayerDescriptor sldVersion;

    /**
     * Rule from SLD to apply for styling the legend output.
     */
    private final String rule;

    /**
     * Builds a {@code GetLegendGraphic} request, using the layer and mime-type specified
     * and width and height for the image.
     */
    public GetLegendGraphic(final Name layer, final String format, final Integer width,
                            final Integer height, final String style, final String sld,
                            final StyledLayerDescriptor sldVersion, final String rule)
    {
        super(ServiceDef.WMS_1_1_1_SLD.version, null);
        this.layer      = layer;
        this.format     = format;
        this.width      = width;
        this.height     = height;
        this.style      = style;
        this.sld        = sld;
        this.sldVersion = sldVersion;
        this.rule       = rule;
    }

    /**
     * Returns the layer to consider for this request.
     */
    public Name getLayer() {
        return layer;
    }

    /**
     * Returns the format for the legend file.
     */
    public String getFormat() {
        return format;
    }

    /**
     * Returns the width of the legend image.
     */
    public Integer getWidth() {
        return width;
    }

    /**
     * Returns the height of the legend image.
     */
    public Integer getHeight() {
        return height;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getExceptionFormat() {
        return MimeType.APP_SE_XML;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueryRequest getRequest() {
        return GET_LEGEND_GRAPHIC;
    }

    /**
     * Returns the sld value.
     */
    public String getSld() {
        return sld;
    }

    /**
     * Returns the style for this legend.
     */
    public String getStyle() {
        return style;
    }

    /**
     * Returns the SLD version for the given SLD file. Note that this parameter is
     * mandatory when {@link #sld} is specified.
     */
    public StyledLayerDescriptor getSldVersion() {
        return sldVersion;
    }

    /**
     * Returns the rule to apply from the SLD file.
     */
    public String getRule() {
        return rule;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toKvp() {
        final StringBuilder kvp = new StringBuilder();
        // Mandatory parameters
        kvp            .append(KEY_REQUEST).append('=').append(GETLEGENDGRAPHIC)
           .append('&').append(KEY_FORMAT ).append('=').append(format)
           .append('&').append(KEY_LAYER  ).append('=').append(layer);

        // Optional parameters
        if (width != null) {
            kvp.append('&').append(KEY_WIDTH).append('=').append(width);
        }
        if (height != null) {
            kvp.append('&').append(KEY_HEIGHT).append('=').append(height);
        }
        if (style != null) {
            kvp.append('&').append(KEY_STYLE).append('=').append(style);
        }
        if (sld != null) {
            kvp.append('&').append(KEY_SLD).append('=').append(sld);
        }
        if (sldVersion != null) {
            kvp.append('&').append(KEY_SLD_VERSION).append('=').append(sldVersion);
        }
        if (rule != null) {
            kvp.append('&').append(KEY_RULE).append('=').append(rule);
        }
        return kvp.toString();
    }
}
