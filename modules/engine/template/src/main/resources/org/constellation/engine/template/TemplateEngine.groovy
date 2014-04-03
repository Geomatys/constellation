package org.constellation.engine.template

import groovy.text.GStringTemplateEngine

/** GroovyTemplateEngine using groovy.text.GStringTemplateEngine
 * Created by christophe mourette on 02/04/14 for Geomatys.
 */
public class GroovyTemplateEngine implements TemplateEngine {

    /**
     * apply values from TemplateFile
     */
    public String apply(File templateFile, Properties param){
        def gstring = new GStringTemplateEngine()
        def gbinding = [param: param]
        def goutput = gstring.createTemplate(templateFile.text).make(gbinding).toString()
		return goutput
    }
}