/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
package org.constellation.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.geotoolkit.display2d.canvas.J2DCanvas;
import org.geotoolkit.display2d.ext.PositionedGraphic2D;
import org.geotoolkit.display2d.ext.grid.GraphicGridJ2D;
import org.geotoolkit.display2d.ext.grid.GridTemplate;
import org.geotoolkit.display2d.ext.image.GraphicImageJ2D;
import org.geotoolkit.display2d.ext.image.ImageTemplate;
import org.geotoolkit.display2d.ext.legend.GraphicLegendJ2D;
import org.geotoolkit.display2d.ext.northarrow.GraphicNorthArrowJ2D;
import org.geotoolkit.display2d.ext.northarrow.NorthArrowTemplate;
import org.geotoolkit.display2d.ext.scalebar.GraphicScaleBarJ2D;
import org.geotoolkit.display2d.ext.scalebar.ScaleBarTemplate;
import org.geotoolkit.display2d.ext.text.GraphicTextJ2D;
import org.geotoolkit.display2d.ext.text.TextTemplate;
import org.geotoolkit.display2d.service.PortrayalExtension;
import static org.constellation.configuration.Decorations.*;
import org.geotoolkit.display.exception.PortrayalException;

/**
 *
 * @author Quentin Boileau (Geomatys)
 */
public final class DecorationExtension implements PortrayalExtension {

    private final List<Map<String, Object>> decorations = new ArrayList<Map<String, Object>>();

    @Override
    public void completeCanvas(final J2DCanvas canvas) throws PortrayalException {

        for (final Map<String, Object> params : decorations) {

            final Class type = (Class) params.get(DECORATION_TYPE);

            if (type.equals(CompasDecoration.class)) {
                final NorthArrowTemplate arrowTemplate = (NorthArrowTemplate) params.get(DECORATION_VALUE);
                final PositionedGraphic2D compasDeco = new GraphicNorthArrowJ2D(canvas, arrowTemplate);
                compasDeco.setPosition((Integer) params.get(DECORATION_POSITION));
                compasDeco.setOffset((Integer) params.get(DECORATION_OFFSET_X), (Integer) params.get(DECORATION_OFFSET_Y));
                canvas.getContainer().add(compasDeco);

            } else if (type.equals(GridDecoration.class)) {
                final GridTemplate gridTemplate = (GridTemplate) params.get(DECORATION_VALUE);
                final GraphicGridJ2D girdDeco = new GraphicGridJ2D(canvas, gridTemplate);
                canvas.getContainer().add(girdDeco);

            } else if (type.equals(ImageDecoration.class)) {
                final ImageTemplate imgTemplate = (ImageTemplate) params.get(DECORATION_VALUE);
                final PositionedGraphic2D imageDeco = new GraphicImageJ2D(canvas, imgTemplate);
                imageDeco.setPosition((Integer) params.get(DECORATION_POSITION));
                imageDeco.setOffset((Integer) params.get(DECORATION_OFFSET_X), (Integer) params.get(DECORATION_OFFSET_Y));
                canvas.getContainer().add(imageDeco);

            } else if (type.equals(LegendDecoration.class)) {
                final org.geotoolkit.display2d.ext.legend.LegendTemplate legendTemplate =
                        (org.geotoolkit.display2d.ext.legend.LegendTemplate) params.get(DECORATION_VALUE);
                final GraphicLegendJ2D legendDeco = new GraphicLegendJ2D(canvas, legendTemplate);
                legendDeco.setPosition((Integer) params.get(DECORATION_POSITION));
                legendDeco.setOffset((Integer) params.get(DECORATION_OFFSET_X), (Integer) params.get(DECORATION_OFFSET_Y));
                canvas.getContainer().add(legendDeco);

            } else if (type.equals(ScaleBarDecoration.class)) {
                final ScaleBarTemplate template = (ScaleBarTemplate) params.get(DECORATION_VALUE);
                final GraphicScaleBarJ2D scaleDeco = new GraphicScaleBarJ2D(canvas);
                scaleDeco.setTemplate(template);
                scaleDeco.setPosition((Integer) params.get(DECORATION_POSITION));
                scaleDeco.setOffset((Integer) params.get(DECORATION_OFFSET_X), (Integer) params.get(DECORATION_OFFSET_Y));
                canvas.getContainer().add(scaleDeco);

            } else if (type.equals(TextDecoration.class)) {
                final TextTemplate textTemplate = (TextTemplate) params.get(DECORATION_VALUE);
                final PositionedGraphic2D textDeco = new GraphicTextJ2D(canvas, textTemplate);
                textDeco.setPosition((Integer) params.get(DECORATION_POSITION));
                textDeco.setOffset((Integer) params.get(DECORATION_OFFSET_X), (Integer) params.get(DECORATION_OFFSET_Y));
                canvas.getContainer().add(textDeco);
            }
        }
    }

    public List<Map<String, Object>> getDecorations() {
        return decorations;
    }
}
