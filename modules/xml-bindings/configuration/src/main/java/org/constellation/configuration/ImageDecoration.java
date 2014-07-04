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

import org.apache.sis.util.logging.Logging;
import org.constellation.configuration.utils.WMSPortrayalUtils;
import org.geotoolkit.display2d.ext.image.DefaultImageTemplate;
import org.geotoolkit.display2d.ext.image.ImageTemplate;

import javax.imageio.ImageIO;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ImageDecoration extends PositionableDecoration {

    private static final Logger LOGGER = Logging.getLogger(ImageDecoration.class);
    
    @XmlElement(name = "Source")
    private String source;

    public ImageDecoration() {
        super();
        this.source = null;
    }

    public ImageDecoration(final String source, final Background background, final Integer offsetX,
            final Integer offsetY, final String position) {
        super(background, offsetX, offsetY, position);
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public ImageTemplate toImageTemplate() {
        final URL url = WMSPortrayalUtils.parseURL(source, null);
        BufferedImage buffer;
        try {
            buffer = ImageIO.read(url);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, null, ex);
            buffer = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        }

        return new DefaultImageTemplate(getBackground().toBackgroundTemplate(), buffer);
    }
}
