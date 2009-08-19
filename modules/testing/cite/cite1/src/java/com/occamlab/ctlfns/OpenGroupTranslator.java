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

import org.w3c.dom.*;
import java.net.*;
import javax.xml.parsers.*;
import org.opengroup.ts.handlers.extensions.ogc.ResponseTransformer;

public class OpenGroupTranslator {
  static Node translate(URLConnection uc, Node parameters) throws Exception {
    Document doc =  parameters.getOwnerDocument();
    String classname = doc.getElementsByTagName("class").item(0).getTextContent().trim();
    String argstring = doc.getElementsByTagName("argstring").item(0).getTextContent().trim();

    Class[] types = new Class[2];
    types[0] = Object.class;
    types[1] = String.class;
    Object[] args = new Object[2];
    args[0] = uc.getContent();
    args[1] = argstring;
    ResponseTransformer rt = (ResponseTransformer)Class.forName(classname).getConstructor(types).newInstance(args);
    rt.getTransformedDocument();
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    doc = db.newDocument();
    return doc;
  }
}
