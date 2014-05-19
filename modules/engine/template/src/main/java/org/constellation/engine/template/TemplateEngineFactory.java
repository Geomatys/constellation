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

package org.constellation.engine.template;

import groovy.lang.GroovyClassLoader;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import org.constellation.util.Util;
import org.geotoolkit.util.FileUtilities;

/** Factory for TemplateEngine
 * Created by christophem on 02/04/14.
 */
public class TemplateEngineFactory {
    public static final String GROOVY_TEMPLATE_ENGINE = "groovy";

    public static TemplateEngine getInstance(String templateEngineType) throws TemplateEngineException {
        try {
            switch (templateEngineType) {
                case GROOVY_TEMPLATE_ENGINE :
                    final InputStream stream = Util.getResourceAsStream("org/constellation/engine/template/TemplateEngine.groovy");
                    final File templateFile = File.createTempFile("TemplateEngine", ".groovy");
                    FileUtilities.buildFileFromStream(stream, templateFile);
                    final GroovyClassLoader gcl = new GroovyClassLoader();
                    final Class clazz = gcl.parseClass(templateFile);
                    final Object aScript = clazz.newInstance();
                    final TemplateEngine templateEngine = (TemplateEngine) aScript;
                    return templateEngine;
                default:
                    throw new IllegalArgumentException( "templateEngineType "+ templateEngineType + " undefined." );
            }
        } catch (Exception e){
            throw new TemplateEngineException("unable to load template engine",e);
        }
    }
}
