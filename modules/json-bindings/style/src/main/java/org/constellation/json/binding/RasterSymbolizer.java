/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.constellation.json.binding;

import org.apache.sis.util.logging.Logging;
import org.geotoolkit.cql.CQL;
import org.opengis.filter.expression.Expression;

import java.util.logging.Level;
import java.util.logging.Logger;

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

	private static final long serialVersionUID = 1L;

    /**
     * Used for debugging purposes.
     */
    private static final Logger LOGGER = Logging.getLogger(RasterSymbolizer.class);

	private String name;
    private double opacity                          = 1.0;
    private ChannelSelection channelSelection       = null;
    private ColorMap colorMap                       = null;

    public RasterSymbolizer() {
    }

    public RasterSymbolizer(final org.opengis.style.RasterSymbolizer symbolizer) {
        ensureNonNull("symbolizer", symbolizer);
        name = symbolizer.getName();
        final Expression opacityExp = symbolizer.getOpacity();
        if(opacityExp!=null){
            final String opacityStr = CQL.write(opacityExp);
            try{
                opacity = Double.parseDouble(opacityStr);
            }catch(NumberFormatException ex){
                LOGGER.log(Level.WARNING,ex.getLocalizedMessage(),ex);
            }
        }
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
