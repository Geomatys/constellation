/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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

package org.constellation.menu.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import org.constellation.bean.I18NBean;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.LayerProviderService;
import org.constellation.provider.StyleProviderProxy;
import org.geotoolkit.util.logging.Logging;
import org.opengis.feature.type.Name;

/**
 * Returns several information from the used GeotoolKit.
 *
 * @author Johann Sorel (Geomatys)
 */
public class ProviderBean extends I18NBean{

    private static final Logger LOGGER = Logging.getLogger(ProviderBean.class);

    public ProviderBean(){
        addBundle("org.constellation.menu.provider.overview");
    }

    public void reloadLayerProviders() {
        LayerProviderProxy.getInstance().reload();
    }

    public void reloadStyleProviders() {
        StyleProviderProxy.getInstance().reload();
    }

    /**
     * Build a tree model representation of all available layers.
     */
    public TreeModel getLayerModel(){
        final DefaultMutableTreeNode node = new DefaultMutableTreeNode("root");

        final Collection<LayerProviderService> services = LayerProviderProxy.getInstance().getServices();
        for(LayerProviderService service : services){
        }

        return new DefaultTreeModel(node);
    }

    public List<String> getLayerProviders() {
        final List<String> names = new ArrayList<String>();
        try {
            for (Name n : LayerProviderProxy.getInstance().getKeys()) {
                if (n.getNamespaceURI() != null) {
                    names.add("{" + n.getNamespaceURI() + "}" + n.getLocalPart());
                } else {
                    names.add("{}" + n.getLocalPart());
                }
            }
            Collections.sort(names);
        } catch (IllegalStateException ex) {
            LOGGER.log(Level.SEVERE, "An error occurs while trying to load the layer providers.");
        }
        return names;
    }

    public List<String> getStyleProviders() {
        final List<String> names = new ArrayList<String>();
        try {
            names.addAll(StyleProviderProxy.getInstance().getKeys());
            Collections.sort(names);
        } catch (IllegalStateException ex) {
            LOGGER.log(Level.SEVERE, "An error occurs while trying to load the style providers.");
        }
        return names;
    }

}
