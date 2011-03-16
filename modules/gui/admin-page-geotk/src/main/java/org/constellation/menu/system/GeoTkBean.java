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

package org.constellation.menu.system;

import java.util.ArrayList;
import java.util.List;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.lang.Setup;
import org.geotoolkit.referencing.CRS;

import org.geotoolkit.referencing.factory.FactoryDependencies;

/**
 * Returns several information from the used GeotoolKit.
 *
 * @author Johann Sorel (Geomatys)
 */
public class GeoTkBean {

    private final TreeModel factoryModel;

    public GeoTkBean(){
        final FactoryDependencies depends = new FactoryDependencies(CRS.getAuthorityFactory(null));
        depends.setAbridged(true);
        final TreeNode root = depends.asTree();
        factoryModel = new DefaultTreeModel(root);
    }

    public TreeModel getFactoryModel(){
        return factoryModel;
    }

    public Hints getDefaultHints(){
        return new Hints();
    }

    public List<Object> getHintKeys(){
        return new ArrayList<Object>(getDefaultHints().keySet());
    }

}
