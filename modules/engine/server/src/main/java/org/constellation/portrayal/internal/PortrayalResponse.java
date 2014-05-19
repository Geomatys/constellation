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

package org.constellation.portrayal.internal;

import java.awt.image.BufferedImage;
import org.geotoolkit.display.PortrayalException;
import org.geotoolkit.display2d.service.CanvasDef;
import org.geotoolkit.display2d.service.OutputDef;
import org.geotoolkit.display2d.service.SceneDef;
import org.geotoolkit.display2d.service.ViewDef;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Contain image informations for the response.
 *
 * @author Johann Sorel (Geomatys)
 */
@XmlRootElement
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
