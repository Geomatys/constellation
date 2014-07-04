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
package org.constellation.configuration;

import org.geotoolkit.display.PortrayalException;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.constellation.configuration.Decorations.DECORATION_OFFSET_X;
import static org.constellation.configuration.Decorations.DECORATION_OFFSET_Y;
import static org.constellation.configuration.Decorations.DECORATION_POSITION;
import static org.constellation.configuration.Decorations.DECORATION_TYPE;
import static org.constellation.configuration.Decorations.DECORATION_VALUE;

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
                canvas.getContainer().getRoot().getChildren().add(compasDeco);

            } else if (type.equals(GridDecoration.class)) {
                final GridTemplate gridTemplate = (GridTemplate) params.get(DECORATION_VALUE);
                final GraphicGridJ2D girdDeco = new GraphicGridJ2D(canvas, gridTemplate);
                canvas.getContainer().getRoot().getChildren().add(girdDeco);

            } else if (type.equals(ImageDecoration.class)) {
                final ImageTemplate imgTemplate = (ImageTemplate) params.get(DECORATION_VALUE);
                final PositionedGraphic2D imageDeco = new GraphicImageJ2D(canvas, imgTemplate);
                imageDeco.setPosition((Integer) params.get(DECORATION_POSITION));
                imageDeco.setOffset((Integer) params.get(DECORATION_OFFSET_X), (Integer) params.get(DECORATION_OFFSET_Y));
                canvas.getContainer().getRoot().getChildren().add(imageDeco);

            } else if (type.equals(LegendDecoration.class)) {
                final org.geotoolkit.display2d.ext.legend.LegendTemplate legendTemplate =
                        (org.geotoolkit.display2d.ext.legend.LegendTemplate) params.get(DECORATION_VALUE);
                final GraphicLegendJ2D legendDeco = new GraphicLegendJ2D(canvas, legendTemplate);
                legendDeco.setPosition((Integer) params.get(DECORATION_POSITION));
                legendDeco.setOffset((Integer) params.get(DECORATION_OFFSET_X), (Integer) params.get(DECORATION_OFFSET_Y));
                canvas.getContainer().getRoot().getChildren().add(legendDeco);

            } else if (type.equals(ScaleBarDecoration.class)) {
                final ScaleBarTemplate template = (ScaleBarTemplate) params.get(DECORATION_VALUE);
                final GraphicScaleBarJ2D scaleDeco = new GraphicScaleBarJ2D(canvas);
                scaleDeco.setTemplate(template);
                scaleDeco.setPosition((Integer) params.get(DECORATION_POSITION));
                scaleDeco.setOffset((Integer) params.get(DECORATION_OFFSET_X), (Integer) params.get(DECORATION_OFFSET_Y));
                canvas.getContainer().getRoot().getChildren().add(scaleDeco);

            } else if (type.equals(TextDecoration.class)) {
                final TextTemplate textTemplate = (TextTemplate) params.get(DECORATION_VALUE);
                final PositionedGraphic2D textDeco = new GraphicTextJ2D(canvas, textTemplate);
                textDeco.setPosition((Integer) params.get(DECORATION_POSITION));
                textDeco.setOffset((Integer) params.get(DECORATION_OFFSET_X), (Integer) params.get(DECORATION_OFFSET_Y));
                canvas.getContainer().getRoot().getChildren().add(textDeco);
            }
        }
    }

    public List<Map<String, Object>> getDecorations() {
        return decorations;
    }
}
