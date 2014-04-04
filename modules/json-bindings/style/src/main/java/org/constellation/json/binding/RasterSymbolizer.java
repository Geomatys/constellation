/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2012, Geomatys
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

package org.constellation.json.binding;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import static org.constellation.json.util.StyleFactories.SF;
import static org.constellation.json.util.StyleUtilities.literal;
import static org.constellation.json.util.StyleUtilities.type;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class RasterSymbolizer implements Symbolizer {

    private String name;
    private double opacity                          = 1.0;
    private ChannelSelection channelSelection       = null;
    private ColorMap colorMap                       = null;

    public RasterSymbolizer() {
    }

    public RasterSymbolizer(final org.opengis.style.RasterSymbolizer symbolizer) {
        ensureNonNull("symbolizer", symbolizer);
        name = symbolizer.getName();
        opacity = Double.parseDouble(symbolizer.getOpacity().toString());
        if (symbolizer.getChannelSelection() != null) {
            this.channelSelection = new ChannelSelection(symbolizer.getChannelSelection());
        }
        if (symbolizer.getColorMap() != null) {
            this.colorMap = new ColorMap(symbolizer.getColorMap());
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getOpacity() {
        return opacity;
    }

    public void setOpacity(final double opacity) {
        this.opacity = opacity;
    }

    public ChannelSelection getChannelSelection() {
        return channelSelection;
    }

    public void setChannelSelection(final ChannelSelection channelSelection) {
        this.channelSelection = channelSelection;
    }

    public ColorMap getColorMap() {
        return colorMap;
    }

    public void setColorMap(final ColorMap colorMap) {
        this.colorMap = colorMap;
    }

    @Override
    public org.opengis.style.Symbolizer toType() {
        return SF.rasterSymbolizer(
                name,
                literal(opacity),
                type(channelSelection),
                null,
                type(colorMap),
                null,
                null,
                null);
    }
}
