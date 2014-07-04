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

package org.constellation.sos.configuration;

import com.sun.istack.logging.Logger;
import org.constellation.engine.template.TemplateEngine;
import org.constellation.engine.template.TemplateEngineException;
import org.constellation.engine.template.TemplateEngineFactory;
import org.constellation.util.Util;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.geotoolkit.sml.xml.SensorMLMarshallerPool;
import org.geotoolkit.util.FileUtilities;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Properties;
import java.util.logging.Level;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class SensorMLGenerator {
    
    private static final Logger LOGGER = Logger.getLogger(SensorMLGenerator.class);
    
    public static String getTemplateSensorMLString(final Properties prop, final String type) {
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
            
            return templateApplied;
        } catch (TemplateEngineException | IOException ex) {
           LOGGER.log(Level.WARNING, null, ex);
        }
        return null;
    }
    
    public static AbstractSensorML getTemplateSensorML(final Properties prop, final String type) {
        try {
            final String templateApplied = getTemplateSensorMLString(prop, type);
            if (templateApplied != null) {
                //unmarshall the template
                final Unmarshaller um = SensorMLMarshallerPool.getInstance().acquireUnmarshaller();
                final AbstractSensorML meta = (AbstractSensorML) um.unmarshal(new StringReader(templateApplied));
                SensorMLMarshallerPool.getInstance().recycle(um);
                return meta;
            }
        } catch (JAXBException ex) {
           LOGGER.log(Level.WARNING, null, ex);
        }
        return null;
    }
}
