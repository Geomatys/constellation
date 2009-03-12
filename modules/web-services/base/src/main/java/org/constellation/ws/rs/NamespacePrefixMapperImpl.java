/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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
package org.constellation.ws.rs;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import java.util.HashMap;
import java.util.Map;


public class NamespacePrefixMapperImpl extends NamespacePrefixMapper {

    private Map<String, String> namespaceExtensions = new HashMap<String, String>();

    /**
     * if set this namespace will be the root of the document with no prefix.
     */
    private String rootNamespace;
    
    public NamespacePrefixMapperImpl(String rootNamespace) {
        super();
        this.rootNamespace = rootNamespace;
        
    }
    /**
     * Returns a preferred prefix for the given namespace URI.
     *
     * This method is intended to be overrided by a derived class.
     *
     * @param namespaceUri
     *      The namespace URI for which the prefix needs to be found.
     *      Never be null. "" is used to denote the default namespace.
     * @param suggestion
     *      When the content tree has a suggestion for the prefix
     *      to the given namespaceUri, that suggestion is passed as a
     *      parameter. Typicall this value comes from the QName.getPrefix
     *      to show the preference of the content tree. This parameter
     *      may be null, and this parameter may represent an already
     *      occupied prefix.
     * @param requirePrefix
     *      If this method is expected to return non-empty prefix.
     *      When this flag is true, it means that the given namespace URI
     *      cannot be set as the default namespace.
     *
     * @return
     *      null if there's no prefered prefix for the namespace URI.
     *      In this case, the system will generate a prefix for you.
     *
     *      Otherwise the system will try to use the returned prefix,
     *      but generally there's no guarantee if the prefix will be
     *      actually used or not.
     *
     *      return "" to map this namespace URI to the default namespace.
     *      Again, there's no guarantee that this preference will be
     *      honored.
     *
     *      If this method returns "" when requirePrefix=true, the return
     *      value will be ignored and the system will generate one.
     */
    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
        String prefix = null;

        prefix = namespaceExtensions.get(namespaceUri);
        if (prefix != null) {
            return prefix;
        }

        if (rootNamespace!=null && rootNamespace.equals(namespaceUri))
            prefix = "";
        
        else if( "http://www.opengis.net/gml".equals(namespaceUri) )
            prefix = "gml";
        
        else if( "http://www.opengis.net/ogc".equals(namespaceUri) )
            prefix = "ogc";
        
        else if( "http://www.opengis.net/ows/1.1".equals(namespaceUri) )
            prefix = "ows";
        
        else if( "http://www.opengis.net/ows".equals(namespaceUri) )
            prefix = "ows";
        
        else if( "http://www.opengis.net/wms".equals(namespaceUri) )
            prefix = "wms";
        
        else if( "http://www.w3.org/1999/xlink".equals(namespaceUri) )
            prefix = "xlink";
        
        else if( "http://www.opengis.net/sld".equals(namespaceUri) )
            prefix = "sld";
        
        else if( "http://www.opengis.net/wcs".equals(namespaceUri) )
            prefix = "wcs";
        
        else if( "http://www.opengis.net/wcs/1.1.1".equals(namespaceUri) )
            prefix = "wcs";
        
        else if( "http://www.opengis.net/se".equals(namespaceUri) )
            prefix = "se";
        
        else if( "http://www.opengis.net/sos/1.0".equals(namespaceUri) )
            prefix = "sos";
        
        else if( "http://www.opengis.net/om/1.0".equals(namespaceUri) )
            prefix = "om";
        
        else if( "http://www.opengis.net/sensorML/1.0".equals(namespaceUri) )
            prefix = "sml1";

        else if( "http://www.opengis.net/sensorML/1.0.1".equals(namespaceUri) )
            prefix = "sml";

        else if( "http://www.opengis.net/swe/1.0".equals(namespaceUri) )
            prefix = "swe1";

        else if( "http://www.opengis.net/swe/1.0.1".equals(namespaceUri) )
            prefix = "swe";
        
        else if( "http://www.opengis.net/sa/1.0".equals(namespaceUri) )
            prefix = "sa";
        
        else if( "http://www.opengis.net/cat/csw/2.0.2".equals(namespaceUri) )
            prefix = "csw";
        
        else if( "http://purl.org/dc/elements/1.1/".equals(namespaceUri) )
            prefix = "dc";
        
        else if( "http://www.purl.org/dc/elements/1.1/".equals(namespaceUri) )
            prefix = "dc2";
        
        else if( "http://purl.org/dc/terms/".equals(namespaceUri) )
            prefix = "dct";
        
        else if( "http://www.purl.org/dc/terms/".equals(namespaceUri) )
            prefix = "dct2";
        
        else if( "http://www.isotc211.org/2005/gmd".equals(namespaceUri) )
            prefix = "gmd";
        
        else if( "http://www.isotc211.org/2005/gmx".equals(namespaceUri) )
            prefix = "gmx";
        
        else if( "http://www.isotc211.org/2005/gco".equals(namespaceUri) )
            prefix = "gco";
        
        else if( "http://www.isotc211.org/2005/srv".equals(namespaceUri) )
            prefix = "srv";
        
        else if( "http://www.isotc211.org/2005/gfc".equals(namespaceUri) )
            prefix = "gfc";
        
        else if( "http://www.w3.org/2001/XMLSchema-instance".equals(namespaceUri) )
            prefix = "xsi";
        
        else if( "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0".equals(namespaceUri) )
            prefix = "rim";
        
        else if( "urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5".equals(namespaceUri) )
            prefix = "rim25";
       
        else if( "http://www.opengis.net/cat/wrs/1.0".equals(namespaceUri) )
            prefix = "wrs";
        
        else if( "http://www.opengis.net/cat/wrs".equals(namespaceUri) )
            prefix = "wrs09";
        
        else if( "http://www.cnig.gouv.fr/2005/fra".equals(namespaceUri) )
            prefix = "fra";
        
        else if( "http://www.w3.org/2004/02/skos/core#".equals(namespaceUri) )
            prefix = "skos";
        
        else if( "http://www.w3.org/1999/02/22-rdf-syntax-ns#".equals(namespaceUri) )
            prefix = "rdf";

        else if( "http://www.opengis.net/wfs".equals(namespaceUri) )
            prefix = "wfs";

        else if( "http://www.opengis.net/ows-6/utds/0.3".equals(namespaceUri) )
            prefix = "utds";

        else if( "http://www.opengis.net/citygml/1.0".equals(namespaceUri) )
            prefix = "core";

        else if( "http://www.opengis.net/citygml/building/1.0".equals(namespaceUri) )
            prefix = "build";

        else if( "http://www.opengis.net/citygml/cityfurniture/1.0".equals(namespaceUri) )
            prefix = "furniture";

        else if( "http://www.opengis.net/citygml/transportation/1.0".equals(namespaceUri) )
            prefix = "tr";

        else if("urn:oasis:names:tc:ciq:xsdschema:xAL:2.0".equals(namespaceUri) )
            prefix = "xal";

        else if("http://www.w3.org/2001/XMLSchema".equals(namespaceUri) )
            prefix = "xsd";

        else if("urn:us:gov:ic:ism:v2".equals(namespaceUri) )
            prefix = "icism";

        else if("http://www.opengis.net/wmts/1.0.0".equals(namespaceUri) )
            prefix = "wmts";

        
        return prefix;
    }
    

    /**
     * Add a new mapping between a namespace and a prefix.
     *
     * @param prefix
     * @param namespace
     */
    public void addMappedNamespace(String prefix, String namespace) {
        namespaceExtensions.put(namespace, prefix);
    }

    /**
     * clear the extension mapping added.
     */
    public void clearExtensionMapping() {
        namespaceExtensions = new HashMap<String, String>();
    }
    
    /**
     * Returns a list of namespace URIs that should be declared
     * at the root element.
     * <p>
     * By default, the JAXB RI produces namespace declarations only when
     * they are necessary, only at where they are used. Because of this
     * lack of look-ahead, sometimes the marshaller produces a lot of
     * namespace declarations that look redundant to human eyes. For example,
     * <pre><xmp>
     * <?xml version="1.0"?>
     * <root>
     *   <ns1:child xmlns:ns1="urn:foo"> ... </ns1:child>
     *   <ns2:child xmlns:ns2="urn:foo"> ... </ns2:child>
     *   <ns3:child xmlns:ns3="urn:foo"> ... </ns3:child>
     *   ...
     * </root>
     * <xmp></pre>
     * <p>
     * If you know in advance that you are going to use a certain set of
     * namespace URIs, you can override this method and have the marshaller
     * declare those namespace URIs at the root element.
     * <p>
     * For example, by returning <code>new String[]{"urn:foo"}</code>,
     * the marshaller will produce:
     * <pre><xmp>
     * <?xml version="1.0"?>
     * <root xmlns:ns1="urn:foo">
     *   <ns1:child> ... </ns1:child>
     *   <ns1:child> ... </ns1:child>
     *   <ns1:child> ... </ns1:child>
     *   ...
     * </root>
     * <xmp></pre>
     * <p>
     * To control prefixes assigned to those namespace URIs, use the
     * {@link #getPreferredPrefix} method.
     *
     * @return
     *      A list of namespace URIs as an array of {@link String}s.
     *      This method can return a length-zero array but not null.
     *      None of the array component can be null. To represent
     *      the empty namespace, use the empty string <code>""</code>.
     *
     * @since
     *      JAXB RI 1.0.2
     */
    @Override
    public String[] getPreDeclaredNamespaceUris() {
        return new String[] {};
    }
}
