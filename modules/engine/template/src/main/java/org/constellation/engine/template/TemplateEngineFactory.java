
/*
 * Constellation - An open source and standard compliant SDI
 * http://www.constellation-sdi.org
 *
 * (C) 2011, Geomatys
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package org.constellation.engine.template;

import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.net.URL;

/** Factory for TemplateEngine
 * Created by christophem on 02/04/14.
 */
public class TemplateEngineFactory {
    public static final String GROOVY_TEMPLATE_ENGINE = "groovy";

    public static TemplateEngine getInstance(String templateEngineType) throws TemplateEngineException {
        try {
            switch (templateEngineType) {
                case GROOVY_TEMPLATE_ENGINE :
                    URL url = TemplateEngineFactory.class.getResource("/org/constellation/engine/template/TemplateEngine.groovy");
                    GroovyClassLoader gcl = new GroovyClassLoader();
                    Class clazz = gcl.parseClass(new File(url.toURI()));
                    Object aScript = clazz.newInstance();
                    TemplateEngine templateEngine = (TemplateEngine) aScript;
                    return templateEngine;
                default:
                    throw new IllegalArgumentException( "templateEngineType "+ templateEngineType + " undefined." );
            }
        } catch (Exception e){
            throw new TemplateEngineException("unable to load template engine",e);
        }
    }
}
