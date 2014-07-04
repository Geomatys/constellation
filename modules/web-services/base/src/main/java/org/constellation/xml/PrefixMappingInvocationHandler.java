/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.xml;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;

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
