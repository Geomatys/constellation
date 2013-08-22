/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010, Geomatys
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

package org.constellation.portrayal.internal;

import java.awt.image.BufferedImage;
import org.geotoolkit.display.PortrayalException;
import org.geotoolkit.display2d.service.CanvasDef;
import org.geotoolkit.display2d.service.OutputDef;
import org.geotoolkit.display2d.service.SceneDef;
import org.geotoolkit.display2d.service.ViewDef;

/**
 * Contain image informations for the response.
 *
 * @author Johann Sorel (Geomatys)
 */
public final class PortrayalResponse {

    private final CanvasDef canvasDef;
    private final SceneDef sceneDef;
    private final ViewDef viewDef;
    private final OutputDef outputDef;
    private BufferedImage image;
    private byte[] buffer;

    public PortrayalResponse(CanvasDef canvasDef, SceneDef sceneDef, ViewDef viewDef, OutputDef outputDef) {
        this.canvasDef = canvasDef;
        this.sceneDef = sceneDef;
        this.viewDef = viewDef;
        this.outputDef = outputDef;
        this.image = null;
    }
        
    public PortrayalResponse(BufferedImage image) {
        this.canvasDef = null;
        this.sceneDef = null;
        this.viewDef = null;
        this.outputDef = null;
        this.image = image;
    }

    public void prepareNow() throws PortrayalException{
        image = CstlPortrayalService.getInstance().portray(sceneDef, viewDef, canvasDef);
    }


    public CanvasDef getCanvasDef() {
        return canvasDef;
    }

    public OutputDef getOutputDef() {
        return outputDef;
    }

    public SceneDef getSceneDef() {
        return sceneDef;
    }

    public BufferedImage getImage() {
        return image;
    }

    public ViewDef getViewDef() {
        return viewDef;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }
    
}
