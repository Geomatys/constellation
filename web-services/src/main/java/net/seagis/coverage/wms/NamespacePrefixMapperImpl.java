package net.seagis.coverage.wms;
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;


public class NamespacePrefixMapperImpl extends NamespacePrefixMapper {
    
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
        
        if (rootNamespace!=null && rootNamespace.equals(namespaceUri))
            prefix = "";
        
        else if( "http://www.opengis.net/gml".equals(namespaceUri) )
            prefix = "gml";
        
        else if( "http://www.opengis.net/ogc".equals(namespaceUri) )
            prefix = "ogc";
        
        else if( "http://www.opengis.net/ows/1.1".equals(namespaceUri) )
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
            prefix = "sml";
        
        else if( "http://www.opengis.net/swe/1.0.1".equals(namespaceUri) )
            prefix = "swe";
        
        else if( "http://www.opengis.net/sa/1.0".equals(namespaceUri) )
            prefix = "sa";
        
        else if( "http://www.opengis.net/cat/csw/2.0.2".equals(namespaceUri) )
            prefix = "csw";
        
        //System.out.println("namespace received:" + namespaceUri + "prefix mapped:" + prefix);
        return prefix;
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
