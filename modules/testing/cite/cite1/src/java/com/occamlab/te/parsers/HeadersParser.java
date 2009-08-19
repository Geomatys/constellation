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
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class HeadersParser {

  public static Document parse(URLConnection uc, Element instruction, PrintWriter logger) throws Exception {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    Document doc = db.newDocument();
    String headerKey, headerValue;
    Element root = doc.createElement("headers");
		Element header, element;
    Node textNode;
    doc.appendChild(root);
    
    uc.connect();

    for (int i = 0; ; i++) {
      headerKey = uc.getHeaderFieldKey(i);
      headerValue = uc.getHeaderField(i);
      if (headerKey == null && headerValue == null) break;

      header=doc.createElement("header");
      root.appendChild(header);

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
    
    return doc;
  }
}
