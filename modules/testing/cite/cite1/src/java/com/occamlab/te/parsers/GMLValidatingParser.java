/****************************************************************************

  The contents of this file are subject to the Mozilla Public License
  Version 1.1 (the "License"); you may not use this file except in
  compliance with the License. You may obtain a copy of the License at
  http://www.mozilla.org/MPL/ 

  Software distributed under the License is distributed on an "AS IS" basis,
  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
  the specific language governing rights and limitations under the License. 

  The Original Code is TEAM Engine.

  The Initial Developer of the Original Code is Northrop Grumman Corporation
  jointly with The National Technology Alliance.  Portions created by
  Northrop Grumman Corporation are Copyright (C) 2005-2006, Northrop
  Grumman Corporation. All Rights Reserved.

  Contributor(s): No additional contributors to date

 ****************************************************************************/
package com.occamlab.te.parsers;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import org.w3c.dom.*;
import com.occamlab.te.ErrorHandlerImpl;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class GMLValidatingParser {
  DocumentBuilderFactory DBF;
  ClassLoader GVP_CL;
  final String builder_property = "javax.xml.parsers.DocumentBuilderFactory";
  final String config_property = "org.apache.xerces.xni.parser.XMLParserConfiguration";
  
  public GMLValidatingParser(Document document_locations) throws Throwable {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    URL urls[] = {
        cl.getResource("com/occamlab/ctlfns/GmlSchemaValidatorLib/gmlObjectModel.jar"),
        cl.getResource("com/occamlab/ctlfns/GmlSchemaValidatorLib/gmlValidator.jar"),
        cl.getResource("com/occamlab/ctlfns/GmlSchemaValidatorLib/jaxen-full.jar"),
        cl.getResource("com/occamlab/ctlfns/GmlSchemaValidatorLib/saxpath.jar"),
        cl.getResource("com/occamlab/ctlfns/GmlSchemaValidatorLib/xercesImpl.jar")
    };
    GVP_CL = new URLClassLoader(urls, null);

    String old_config_property = System.getProperty(config_property);
    System.clearProperty(config_property);
    
    try { 
      Thread.currentThread().setContextClassLoader(GVP_CL);
      Class dbf_class = GVP_CL.loadClass("com.galdosinc.glib.xml.jaxp.ValidatingDocumentBuilderFactory");
      DBF = (DocumentBuilderFactory)dbf_class.newInstance();
      DBF.setNamespaceAware(true);
      DBF.setValidating(true);
      NodeList parms = document_locations.getElementsByTagName("parm");
      for (int i = 0; i < parms.getLength(); i++) {
        Element parm = (Element)parms.item(i);
        NodeList parm_contents = parm.getChildNodes();
        String property_name = null;
        String property_value = null;
        for (int j = 0; j < parm_contents.getLength(); j++) {
          Node n = (Node)parm_contents.item(j);
          if (n.getNodeType() == Node.ELEMENT_NODE) {
            Element e = (Element)n;
            if (e.getNodeName().equals("name")) {
              property_name = e.getTextContent();
            }
            if (e.getNodeName().equals("value")) {
              property_value = e.getTextContent();
            }
          }
        }
//System.out.println(property_name + ": " + property_value);
        DBF.setAttribute(property_name, property_value);
      }
    } catch(Exception e) {
//e.printStackTrace(System.out);
      throw e;
    } finally { 
      Thread.currentThread().setContextClassLoader(cl); 
    } 

    if (old_config_property != null) {
      System.setProperty(config_property, old_config_property);
    }
  }

  public Document parse(URLConnection uc, Element instruction, PrintWriter logger) throws Exception {
    Document doc = null;
    ClassLoader old_cl = Thread.currentThread().getContextClassLoader(); 
    ErrorHandlerImpl eh = new ErrorHandlerImpl(null, logger);

    String old_config_property = System.getProperty(config_property);
    System.clearProperty(config_property);

    try {
      Thread.currentThread().setContextClassLoader(GVP_CL);

      DocumentBuilder db = DBF.newDocumentBuilder();
      db.setErrorHandler(eh);

      doc = db.parse(uc.getInputStream());
    } catch (Exception e) {
      logger.println(e.getMessage());
    } finally { 
      Thread.currentThread().setContextClassLoader(old_cl); 
    } 

    if (old_config_property != null) {
      System.setProperty(config_property, old_config_property);
    }

    if (eh.getErrorCount() > 0 || eh.getWarningCount() > 0) {
      logger.println(eh.getErrorCounts());
      logger.flush();
    }
    
    if (eh.getErrorCount() > 0) {
      return null;
    }

    return doc;
  }

  public static void main(String[] args) throws Throwable {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    Document doc = db.parse(args[0]);
    GMLValidatingParser gvp = new GMLValidatingParser(doc);
    URLConnection uc = new java.net.URL(args[1]).openConnection();
    gvp.parse(uc, doc.getDocumentElement(), new PrintWriter(System.out));
  }
}

