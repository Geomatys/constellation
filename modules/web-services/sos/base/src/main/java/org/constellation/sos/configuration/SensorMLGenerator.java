/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
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

package org.constellation.sos.configuration;

import com.sun.istack.logging.Logger;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Properties;
import java.util.logging.Level;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.constellation.engine.template.TemplateEngine;
import org.constellation.engine.template.TemplateEngineException;
import org.constellation.engine.template.TemplateEngineFactory;
import org.constellation.util.Util;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.geotoolkit.sml.xml.SensorMLMarshallerPool;
import org.geotoolkit.util.FileUtilities;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class SensorMLGenerator {
    
    private static final Logger LOGGER = Logger.getLogger(SensorMLGenerator.class);
    
    public static AbstractSensorML getTemplateSensorML(final Properties prop, final String type) {
        try {
            final TemplateEngine templateEngine = TemplateEngineFactory.getInstance(TemplateEngineFactory.GROOVY_TEMPLATE_ENGINE);
            final InputStream stream;
            if ("Component".equals(type)) {
                stream = Util.getResourceAsStream("org/constellation/engine/template/smlComponentTemplate.xml");
            } else if ("System".equals(type)) {
                stream = Util.getResourceAsStream("org/constellation/engine/template/smlSystemTemplate.xml");
            } else {
                throw new IllegalArgumentException("unexpected sml type");
            }
            
            final File templateFile = File.createTempFile("smlTemplate", ".xml");
            FileUtilities.buildFileFromStream(stream, templateFile);
            final String templateApplied = templateEngine.apply(templateFile, prop);
            
            //unmarshall the template
            final Unmarshaller um = SensorMLMarshallerPool.getInstance().acquireUnmarshaller();
            final AbstractSensorML meta = (AbstractSensorML) um.unmarshal(new StringReader(templateApplied));
            SensorMLMarshallerPool.getInstance().recycle(um);
            return meta;
        } catch (TemplateEngineException | IOException | JAXBException ex) {
           LOGGER.log(Level.WARNING, null, ex);
        }
        return null;
    }
}
