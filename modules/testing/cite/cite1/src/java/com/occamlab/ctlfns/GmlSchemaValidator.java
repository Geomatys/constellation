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
package com.occamlab.ctlfns;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import javax.xml.xpath.*;
import javax.xml.namespace.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.*;

import org.w3c.dom.*;

public class GmlSchemaValidator {
  Hashtable namespaceHash = new Hashtable();
  ClassLoader GSV_CL;
  final String config_property = "org.apache.xerces.xni.parser.XMLParserConfiguration";
  
  public GmlSchemaValidator(Document namespaces) {
    NodeList nl = namespaces.getElementsByTagName("namespace");
    for (int i = 0; i < nl.getLength(); i++) {
      Element e = (Element)nl.item(i);
      namespaceHash.put(e.getAttribute("prefix"), e.getAttribute("uri"));
    }

    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    URL urls[] = {
        cl.getResource("com/occamlab/ctlfns/GmlSchemaValidatorLib/gmlObjectModel.jar"),
        cl.getResource("com/occamlab/ctlfns/GmlSchemaValidatorLib/gmlValidator.jar"),
        cl.getResource("com/occamlab/ctlfns/GmlSchemaValidatorLib/jaxen-full.jar"),
        cl.getResource("com/occamlab/ctlfns/GmlSchemaValidatorLib/saxpath.jar"),
        cl.getResource("com/occamlab/ctlfns/GmlSchemaValidatorLib/xercesImpl.jar")
    };
    GSV_CL = new URLClassLoader(urls, null);
  }

  private class SvrContext implements NamespaceContext {
    static final String SVR = "http://www.galdosinc.com/xml/schema/validation/report"; 
    public String getNamespaceURI(String prefix) {
      return prefix.equals("svr") ? SVR : XMLConstants.NULL_NS_URI;
    }
    public String getPrefix(String namespace) {
      return namespace.equals(SVR) ? "svr" : null;
    }
    public Iterator getPrefixes(String namespace) {
      return null;
    }
  }  

  private Object execute(String baseUri, Node schema, List featureTypeNames) throws Exception {
    Element report = null;
    Transformer t = TransformerFactory.newInstance().newTransformer();
    ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
    t.transform(new DOMSource(schema), new StreamResult(baos));

    ClassLoader old_cl = Thread.currentThread().getContextClassLoader();
      
    Class qname_class = GSV_CL.loadClass("com.galdosinc.glib.xml.QName");
    Method setNamespaceUri = qname_class.getMethod("setNamespaceUri", new Class[]{String.class});
    Method setLocalName = qname_class.getMethod("setLocalName", new Class[]{String.class});
    List QNames = new ArrayList();
    Iterator it = featureTypeNames.listIterator();
    while (it.hasNext()) {
      String name = (String)it.next();
      int colon = name.indexOf(":");
      String prefix = name.substring(0, colon);
      Object qname_instance = qname_class.newInstance();
      setNamespaceUri.invoke(qname_instance, new Object[]{namespaceHash.get(prefix)});
      setLocalName.invoke(qname_instance, new Object[]{name.substring(colon + 1)});
      QNames.add(qname_instance);
    }

    Class validator_class = GSV_CL.loadClass("com.galdosinc.glib.gml.validator.XPathGmlSchemaValidator");
    Method execute = validator_class.getMethod("execute", new Class[]{String.class, String.class, List.class});
    Object validator_instance = validator_class.newInstance();

    String old_config_property = System.getProperty(config_property);
    System.clearProperty(config_property);

    try { 
      Thread.currentThread().setContextClassLoader(GSV_CL);
      report = (Element)execute.invoke(validator_instance, new Object[]{baseUri, baos.toString(), QNames});
    } catch (Exception e) {
//e.printStackTrace();
    } finally { 
      Thread.currentThread().setContextClassLoader(old_cl); 
    }

    if (old_config_property != null) {
      System.setProperty(config_property, old_config_property);
    }

    if (report == null) {
      return null;
    }

//t.transform(new DOMSource(report), new StreamResult(System.out));

    XPath xpath = XPathFactory.newInstance().newXPath();
    NamespaceContext namespaces = new SvrContext();
    xpath.setNamespaceContext(namespaces);
    XPathExpression expression = xpath.compile("count(svr:ElementReport) = 0 and count(svr:Error) = 0");
    Boolean result = (Boolean)expression.evaluate(report, XPathConstants.BOOLEAN);
    return result.booleanValue() ? result : null;
  }

  public Object validate(String baseUri, Node schema) throws Exception {
    return execute(baseUri, schema, new ArrayList());
  }

  public Object validate(String baseUri, Node schema, String featureTypeName1) throws Exception {
    List featureTypeNames = new ArrayList();
    featureTypeNames.add(featureTypeName1);
    return execute(baseUri, schema, featureTypeNames);
  }

  public Object validate(String baseUri, Node schema, String featureTypeName1, String featureTypeName2) throws Exception {
    List featureTypeNames = new ArrayList();
    featureTypeNames.add(featureTypeName1);
    featureTypeNames.add(featureTypeName2);
    return execute(baseUri, schema, featureTypeNames);
  }

  public Object validate(String baseUri, Node schema, String featureTypeName1, String featureTypeName2, String featureTypeName3) throws Exception {
    List featureTypeNames = new ArrayList();
    featureTypeNames.add(featureTypeName1);
    featureTypeNames.add(featureTypeName2);
    featureTypeNames.add(featureTypeName3);
    return execute(baseUri, schema, featureTypeNames);
  }

  public Object validate(String baseUri, Node schema, String featureTypeName1, String featureTypeName2, String featureTypeName3, String featureTypeName4) throws Exception {
    List featureTypeNames = new ArrayList();
    featureTypeNames.add(featureTypeName1);
    featureTypeNames.add(featureTypeName2);
    featureTypeNames.add(featureTypeName3);
    featureTypeNames.add(featureTypeName4);
    return execute(baseUri, schema, featureTypeNames);
  }

  public Object validate(String baseUri, Node schema, String featureTypeName1, String featureTypeName2, String featureTypeName3, String featureTypeName4, String featureTypeName5) throws Exception {
    List featureTypeNames = new ArrayList();
    featureTypeNames.add(featureTypeName1);
    featureTypeNames.add(featureTypeName2);
    featureTypeNames.add(featureTypeName3);
    featureTypeNames.add(featureTypeName4);
    featureTypeNames.add(featureTypeName5);
    return execute(baseUri, schema, featureTypeNames);
  }

/*
  public static void main(String[] args) throws Exception {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    Document namespaces_doc = db.parse(args[0]);
    Document schema_doc = db.parse(args[1]);
    GmlSchemaValidator gsv = new GmlSchemaValidator(namespaces_doc);
    Object o = gsv.validate(args[1], schema_doc.getDocumentElement(), "cdf:Deletes");
  }
*/
}

