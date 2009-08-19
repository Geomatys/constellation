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
/* Note: This is not OpenGroup code, but a replacement class allowing code
   refering to the OpenGroup class by the same name to work */ 

package org.opengroup.ts.handlers.extensions.ogc;

import java.net.*;
import java.io.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import org.w3c.dom.*;

public class ResponseTransformer {
	Class TransformerClass = null;
	PrintWriter Logger;
  
  public ResponseTransformer(String classname) throws Exception {
   	TransformerClass = Class.forName(classname);
  }

  public ResponseTransformer(Object obj, String args) {
  	// This constructor should be overridden by child classes
  }

  public String getTransformedDocument() {
  	// This method should be overridden by child classes
  	return null;
  }
  
  public void addLogMessage(String message) throws Exception {
  	Logger.println(message);
  }
  
  public void setLogger(PrintWriter logger) {
    Logger = logger;
  }

  public Document translate(URLConnection uc, Element instruction, PrintWriter logger) throws Throwable {
  	Logger = logger;
  	
    String argstring = instruction.getTextContent();
//    String argstring = parameters.getFirstChild().getTextContent();
//    System.out.println(argstring);

    Class[] types = new Class[2];
    types[0] = Object.class;
    types[1] = String.class;
    Object[] args = new Object[2];
    Object content = uc.getContent();
    if (content instanceof InputStream) {
      byte[] buf = new byte[1024];
      String s = "";
      int i = ((InputStream)content).read(buf);
      while (i > 0) {
        s += new String(buf).substring(0, i);
        i = ((InputStream)content).read(buf);
      }
      args[0] = s;
    } else {
      args[0] = content;
    }
    args[1] = argstring;
    ResponseTransformer rt = null;
    try {
      rt = (ResponseTransformer)TransformerClass.getConstructor(types).newInstance(args);
    } catch (java.lang.reflect.InvocationTargetException e) {
      throw e.getTargetException();
    }
    String response = rt.getTransformedDocument();
    if (response == null) {
    	return null;
    }

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    ByteArrayInputStream bais = new ByteArrayInputStream(response.getBytes());
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer t = tf.newTransformer();
    Document doc = db.newDocument();
    t.transform(new StreamSource(bais), new DOMResult(doc));
    return doc;
  }
}
