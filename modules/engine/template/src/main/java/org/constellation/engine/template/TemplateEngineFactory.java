package org.constellation.engine.template;

import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.net.URL;

/**
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
