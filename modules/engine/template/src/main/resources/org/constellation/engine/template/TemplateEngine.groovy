package org.constellation.engine.template

import groovy.text.GStringTemplateEngine

public class GroovyTemplateEngine implements TemplateEngine {
    public void printTest(String pattern) {
        println "this is in the $pattern test class"
    }

    public String apply(File templateFile, Properties param){
        def gstring = new GStringTemplateEngine()
//        def gsource = '''Dear <%= name %>,
//            Text is created for
//            <% if (gstring) out << 'GStringTemplateEngine'
//            else out << 'other template engine'%>.'''
        def gbinding = [param: param]
        def goutput = gstring.createTemplate(templateFile.text).make(gbinding).toString()
		return goutput
    }
}