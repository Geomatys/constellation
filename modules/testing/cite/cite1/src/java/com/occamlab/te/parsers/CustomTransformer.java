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

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.opengroup.ts.handlers.extensions.ogc.ResponseTransformer;

public class CustomTransformer {
  public static Document parse(URLConnection uc, Element instruction, PrintWriter logger) throws Throwable {
    Object content = uc.getContent();
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    Document doc = db.newDocument();
    Element response = doc.createElement("response");
    addHeaders(response, uc);
    String custom_class = instruction.getAttribute("class");
    if (custom_class != null) {
      Element custom = doc.createElement("custom");
      String argstring = instruction.getTextContent();
      addCustom(custom, content, custom_class, argstring, logger);
      response.appendChild(custom);
    }
    doc.appendChild(response);
//    TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(System.out));
    return doc;
  }
  
  private static void addHeaders(Element parent, URLConnection uc) { 
    String headerKey, headerValue;
    Document doc = parent.getOwnerDocument();
    Element headers = doc.createElement("headers");
    Element header, element;
    Node textNode;
    parent.appendChild(headers);
    
    for (int i = 0; ; i++) {
      headerKey = uc.getHeaderFieldKey(i);
      headerValue = uc.getHeaderField(i);
      if (headerKey == null && headerValue == null) break;

      header=doc.createElement("header");
      headers.appendChild(header);

      element = doc.createElement("name");
      if (headerKey != null) {
        header.appendChild(element);
        textNode = doc.createTextNode(headerKey);
        element.appendChild(textNode);
      }

      element = doc.createElement("value");
      header.appendChild(element);
      textNode = doc.createTextNode(headerValue);
      element.appendChild(textNode);
    }
  }

  private static void addCustom(Element parent, Object content, String custom_class, String argstring, PrintWriter logger) throws Throwable {
    Class c = Class.forName(custom_class);
    Class[] types = new Class[2];
    types[0] = Object.class;
    types[1] = String.class;
    Object[] args = new Object[2];
    args[0] = content;
    args[1] = argstring;
    ResponseTransformer rt = null;
    try {
      rt = (ResponseTransformer)c.getConstructor(types).newInstance(args);
    } catch (java.lang.reflect.InvocationTargetException e) {
      throw e.getTargetException();
    }
    rt.setLogger(logger);
    String response = rt.getTransformedDocument();
    if (response == null) {
      return;
    }

    ByteArrayInputStream bais = new ByteArrayInputStream(response.getBytes());
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer t = tf.newTransformer();
    t.transform(new StreamSource(bais), new DOMResult(parent));
  }
}
