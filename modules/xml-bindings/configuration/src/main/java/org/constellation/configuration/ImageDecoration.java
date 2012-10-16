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

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import org.constellation.configuration.utils.WMSPortrayalUtils;
import org.geotoolkit.display2d.ext.image.DefaultImageTemplate;
import org.geotoolkit.display2d.ext.image.ImageTemplate;
import org.geotoolkit.util.logging.Logging;

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
