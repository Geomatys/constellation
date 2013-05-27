/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.xml;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class PrefixMappingInvocationHandler implements InvocationHandler {

    private final XMLEventReader rootEventReader;
    private final Map<String, String> prefixMapping;
    
    
    
    public PrefixMappingInvocationHandler(final XMLEventReader rootEventReader, final Map<String, String> prefixMapping) {
        this.rootEventReader = rootEventReader;
        this.prefixMapping   = prefixMapping;
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object returnVal = null;
        try {
            returnVal = method.invoke(rootEventReader, args);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
        if (method.getName().equals("nextEvent")) {
            final XMLEvent evt = (XMLEvent) returnVal;
            if (evt.isStartElement()) {
                final StartElement startElem = evt.asStartElement();
                final Iterator<Namespace> t = startElem.getNamespaces();
                while (t.hasNext()) {
                    final Namespace n = t.next();
                    prefixMapping.put(n.getPrefix(), n.getNamespaceURI());
                }
            }
        }
        return returnVal;
    }

}
