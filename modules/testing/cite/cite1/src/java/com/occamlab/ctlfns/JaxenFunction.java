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

import org.jaxen.*;
import java.lang.reflect.*;
import java.util.*;

public class JaxenFunction {
  Function JaxenFnClass = null;
  Method JaxenFnCall = null;

  Object prepare_object(Object o) {
    if (o instanceof List) {
      List l = (List)o;
      for (int i=0; i<l.size(); i++) {
        l.set(i, prepare_object(l.get(i)));
      }
    } else if (o instanceof net.sf.saxon.om.NodeInfo) {
      return net.sf.saxon.dom.NodeOverNodeInfo.wrap((net.sf.saxon.om.NodeInfo)o);
    }
    return o;
  }

  Context build_context() throws Exception {
    Navigator nav = new org.jaxen.dom.DocumentNavigator();
//    SimpleFunctionContext fc = new SimpleFunctionContext();
//    SimpleNamespaceContext nc = new SimpleNamespaceContext();
//    nc.addElementNamespaces(nav, context);
    ContextSupport cs = new ContextSupport(null, null, null, nav);
    return new Context(cs);
  }

  public JaxenFunction(String class_name) throws Exception {
    Class c = Class.forName(class_name);
    JaxenFnClass = (Function)c.newInstance();
    Method methods[] = c.getMethods();
    for (int i=0; i<methods.length; i++) {
      if (methods[i].getName().equals("call")) {
        JaxenFnCall = methods[i];
        break;
      }
    }
  }

  public Object call() throws Throwable {
    Object[] params = new Object[2];
    params[0] = build_context();
    List list = new ArrayList();
    params[1] = list;
    try {
      return JaxenFnCall.invoke(JaxenFnClass, params);
    } catch (java.lang.reflect.InvocationTargetException e) {
      throw e.getTargetException();
    }
  }

  public Object call(Object o1) throws Throwable {
    Object[] params = new Object[2];
    params[0] = build_context();
    List list = new ArrayList();
    list.add(prepare_object(o1));
    params[1] = list;
    try {
      return JaxenFnCall.invoke(JaxenFnClass, params);
    } catch (java.lang.reflect.InvocationTargetException e) {
      throw e.getTargetException();
    }
  }

  public Object call(Object o1, Object o2) throws Throwable {
    Object[] params = new Object[2];
    params[0] = build_context();
    List list = new ArrayList();
    list.add(prepare_object(o1));
    list.add(prepare_object(o2));
    params[1] = list;
    try {
      return JaxenFnCall.invoke(JaxenFnClass, params);
    } catch (java.lang.reflect.InvocationTargetException e) {
      throw e.getTargetException();
    }
  }

  public Object call(Object o1, Object o2, Object o3) throws Throwable {
    Object[] params = new Object[2];
    params[0] = build_context();
    List list = new ArrayList();
    list.add(prepare_object(o1));
    list.add(prepare_object(o2));
    list.add(prepare_object(o3));
    params[1] = list;
    try {
      return JaxenFnCall.invoke(JaxenFnClass, params);
    } catch (java.lang.reflect.InvocationTargetException e) {
      throw e.getTargetException();
    }
  }

  public Object call(Object o1, Object o2, Object o3, Object o4) throws Throwable {
    Object[] params = new Object[2];
    params[0] = build_context();
    List list = new ArrayList();
    list.add(prepare_object(o1));
    list.add(prepare_object(o2));
    list.add(prepare_object(o3));
    list.add(prepare_object(o4));
    params[1] = list;
    try {
      return JaxenFnCall.invoke(JaxenFnClass, params);
    } catch (java.lang.reflect.InvocationTargetException e) {
      throw e.getTargetException();
    }
  }

  public Object call(Object o1, Object o2, Object o3, Object o4, Object o5) throws Throwable {
    Object[] params = new Object[2];
    params[0] = build_context();
    List list = new ArrayList();
    list.add(prepare_object(o1));
    list.add(prepare_object(o2));
    list.add(prepare_object(o3));
    list.add(prepare_object(o4));
    list.add(prepare_object(o5));
    params[1] = list;
    try {
      return JaxenFnCall.invoke(JaxenFnClass, params);
    } catch (java.lang.reflect.InvocationTargetException e) {
      throw e.getTargetException();
    }
  }
}
