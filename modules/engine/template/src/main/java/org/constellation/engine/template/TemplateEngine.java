package org.constellation.engine.template;

import java.io.File;
import java.util.Properties;

/**
 * Created by christophem on 02/04/14.
 */
public interface TemplateEngine {
    public void printTest(String pattern);
    public String apply(File templateFile, Properties values);
}
