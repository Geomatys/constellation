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

package org.constellation.ws;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * 
 * @author Guilhem Legal (Geomatys)
 */
public final class WebServiceUtilities {

    private WebServiceUtilities(){}

    /**
     * Extract The mapping between namespace and prefix in a namespace parameter of a GET request.
     *
     * @param namespace a String with the pattern: xmlns(ns1=http://my_ns1.com),xmlns(ns2=http://my_ns2.com),xmlns(ns3=http://my_ns3.com)
     * @return a Map of @{<prefix, namespace>}.
     * @throws CstlServiceException if the parameter namespace is malformed.
     */
    public static Map<String,String> extractNamespace(String namespace) throws CstlServiceException {
        final Map<String, String> namespaces = new HashMap<String, String>();
        if (namespace != null) {
            final StringTokenizer tokens = new StringTokenizer(namespace, ",;");
            while (tokens.hasMoreTokens()) {
                String token = tokens.nextToken().trim();
                if (token.startsWith("xmlns(") && token.endsWith(")")) {
                    token = token.substring(6, token.length() -1);
                    if (token.indexOf('=') != -1) {
                        final String prefix = token.substring(0, token.indexOf('='));
                        final String url    = token.substring(token.indexOf('=') + 1);
                        namespaces.put(prefix, url);
                    } else {
                         throw new CstlServiceException("The namespace parameter is malformed : [" + token + "] the good pattern is xmlns(ns1=http://my_ns1.com)",
                                                  ExceptionCode.INVALID_PARAMETER_VALUE, "namespace");
                    }
                } else {
                    throw new CstlServiceException("The namespace attribute is malformed: good pattern is \"xmlns(ns1=http://namespace1),xmlns(ns2=http://namespace2)\"",
                                                       ExceptionCode.INVALID_PARAMETER_VALUE, "namespace");
                }
            }
        }
        return namespaces;
    }
    
    public static String getValidationLocator(final String msg, final Map<String, String> mapping) {
        if (msg.contains("must appear on element")) {
            int pos = msg.indexOf("'");
            String temp = msg.substring(pos + 1);
            pos = temp.indexOf("'");
            final String attribute = temp.substring(0, pos);
            temp = temp.substring(pos + 1);
            pos  = temp.indexOf("'");
            temp = temp.substring(pos + 1);
            pos = temp.indexOf("'");
            final String element = temp.substring(0, pos);
            pos = element.indexOf(':');
            final String prefix = element.substring(0, pos);
            final String localPart = element.substring(pos + 1);
            final String namespace = mapping.get(prefix);
            
            return "Expected attribute: " + attribute + " in element "+ localPart + '@' + namespace;
        }
        return null;
    }
    
    /*
     * This map is temporary while we don't know how to extract the request mapping from JAX-WS
     */
    public static final Map<String, String> DUMMY_MAPPING = new HashMap<String, String>();
    static {
        DUMMY_MAPPING.put("swes", "http://www.opengis.net/swes/2.0");
        DUMMY_MAPPING.put("sos", "http://www.opengis.net/sos/2.0");
    }
}
