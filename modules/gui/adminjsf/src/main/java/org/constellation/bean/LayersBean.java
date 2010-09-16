/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2009, Geomatys
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
package org.constellation.bean;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.tree.DefaultTreeModel;
import org.apache.xerces.parsers.DOMParser;
import org.mapfaces.utils.tree.DOMTreeWalkerTreeModel;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.geotoolkit.wms.WebMapServer;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.wms.xml.AbstractWMSCapabilities;
import org.geotoolkit.wms.xml.AbstractLayer;
import org.geotoolkit.wms.map.WMSMapLayer;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import java.util.List;
import java.util.ArrayList;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Mehdi Sidhoum
 * @since 0.6.4
 */
public class LayersBean {

    private String inputText = "";
    private DefaultTreeModel treemodel;
    private MapContext mapcontext;

    public LayersBean() {
        final Object request = FacesContext.getCurrentInstance().getExternalContext().getRequest();
        if (request instanceof HttpServletRequest) {
            final HttpServletRequest svRequest = (HttpServletRequest) request;
            inputText = svRequest.getScheme() + "://" + svRequest.getServerName() + ":" + svRequest.getServerPort() + svRequest.getContextPath() + "/WS/wms";
        }
    }

    /**
     * Build a mapcontext based on existing layers and passed them to mapfaces components.
     * @throws MalformedURLException
     * @throws IOException
     * @throws SAXException
     */
    public void performAction() throws MalformedURLException {
        if (getInputText() != null) {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>><  inputText = " + getInputText());
            final WebMapServer webMapServer = new WebMapServer(new URL(getInputText()), "1.3.0");
            mapcontext = createContext(webMapServer.getCapabilities(), webMapServer);
        }
    }

    /**
     * Do necessary process to display a treetable view of capabilities document.
     */
    public void performTreeTable() {
        if (getInputText() != null) {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>><  inputText = " + getInputText());
            final String separator = getInputText().contains("?") ? "&" : "?";
            final String capaUrl = getInputText().concat(separator + "request=Getcapabilities&service=WMS&version=1.3.0");
            try {
                buildDOMTreeModel(capaUrl);
            } catch (Exception e) {
            }
        }
    }

    /**
     * Returns a new instance of mapcontext based on existing layers from the capabilities document.
     * @param capa
     * @param webMapServer
     * @return
     */
    public MapContext createContext(final Object capa, final WebMapServer webMapServer) {
        final MapContext context = MapBuilder.createContext(DefaultGeographicCRS.WGS84);
        if (capa instanceof AbstractWMSCapabilities) {
            final AbstractWMSCapabilities capabilities = (AbstractWMSCapabilities) capa;
            final List<WMSMapLayer> wmsMapLayers = new ArrayList<WMSMapLayer>();
            for (final AbstractLayer layer : capabilities.getCapability().getLayer().getLayer()) {
                final WMSMapLayer wmsLayer = new WMSMapLayer(webMapServer, layer.getName());
                wmsLayer.setFormat("image/png");
                wmsLayer.setName(layer.getName());
                wmsLayer.setUserPropertie("group", "wms " + capabilities.getVersion());
                if ("1.1.1".equals(capabilities.getVersion())) {
                    wmsLayer.setCrs84Politic(WMSMapLayer.CRS84Politic.CONVERT_TO_EPSG4326);
                }
                if (capabilities.getCapability().getLayer().getLayer().indexOf(layer) > 5) {
                    wmsLayer.setVisible(false);
                }
                if (!wmsMapLayers.contains(wmsLayer)) {
                    wmsMapLayers.add(wmsLayer);
                }
            }
            context.layers().clear();
            context.layers().addAll(wmsMapLayers);
        }
        return context;
    }

    /**
     * Create a treemodel based on capabilities xml response and affect to treemodel backing bean property
     * @param text
     * @throws MalformedURLException
     * @throws IOException
     * @throws SAXException
     * @throws SAXParseException
     */
    public void buildDOMTreeModel(String text) throws MalformedURLException, IOException, SAXException, SAXParseException {
        final DOMParser parser = new DOMParser();
        org.xml.sax.InputSource input = null;
        if (text != null && !text.isEmpty() && text.startsWith("http://")) {

            final URL url = new URL(text);
            input = new org.xml.sax.InputSource(url.openStream());

        } else {
            return;
        }
        parser.parse(input);
        final Document document = parser.getDocument();
        final DocumentTraversal traversal = (DocumentTraversal) document;
        final NodeFilter filter =
                new NodeFilter() {

                    @Override
                    public short acceptNode(Node n) {
                        if (n.getNodeType() == Node.TEXT_NODE) {
                            // Use trim() to strip off leading and trailing space.
                            // If nothing is left, then reject the node
                            if (((Text) n).getData().trim().length() == 0) {
                                return NodeFilter.FILTER_REJECT;
                            }
                        }
                        return NodeFilter.FILTER_ACCEPT;
                    }
                };
        final int whatToShow = NodeFilter.SHOW_ALL & ~NodeFilter.SHOW_COMMENT;
        final TreeWalker walker = traversal.createTreeWalker(document, whatToShow, filter, false);
        final DOMTreeWalkerTreeModel dtwtm = new DOMTreeWalkerTreeModel(walker);
        treemodel = DOMTreeWalkerTreeModel.performTreeModel(dtwtm, null);
    }

    public String getInputText() {
        if (inputText == null || inputText.isEmpty()) {
            final Object request = FacesContext.getCurrentInstance().getExternalContext().getRequest();
            if (request instanceof HttpServletRequest) {
                final HttpServletRequest svRequest = (HttpServletRequest) request;
                inputText = svRequest.getScheme() + "://" + svRequest.getServerName() + ":" + svRequest.getServerPort() + svRequest.getContextPath() + "/WS/wms";
            }

        }
        return inputText;
    }

    public void setinputText(String text) {
        this.inputText = text;
    }

    public DefaultTreeModel getTreemodel() {
        return treemodel;
    }

    public void setTreemodel(DefaultTreeModel tree) {
        this.treemodel = tree;
    }

    public MapContext getMapcontext() {
        return mapcontext;
    }
}
