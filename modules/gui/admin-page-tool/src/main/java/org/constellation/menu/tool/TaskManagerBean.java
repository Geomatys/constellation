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

package org.constellation.menu.tool;

import java.util.ArrayList;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import org.geotoolkit.factory.Hints;

import org.mapfaces.i18n.I18NBean;
import org.mapfaces.renderkit.html.outline.OutlineRowStyler;

/**
 * Returns task manager and pages to add new values.
 *
 * @author Johann Sorel (Geomatys)
 */
public class TaskManagerBean extends I18NBean{

    public static final OutlineRowStyler ROW_STYLER = new OutlineRowStyler() {

        @Override
        public String getRowStyle(final TreeNode node) {
            final DefaultMutableTreeNode mn = (DefaultMutableTreeNode) node;
            final Object obj = mn.getUserObject();
            return "";
        }

        @Override
        public String getRowClass(final TreeNode node) {
            return "";
        }
    };
    
    private final TreeModel taskModel;

    public TaskManagerBean(){
        addBundle("tasks.tasks");       
        
        taskModel = new DefaultTreeModel(new DefaultMutableTreeNode("root"));
    }

    public TreeModel getTaskModel(){
        return taskModel;
    }

    public Hints getDefaultHints(){
        return new Hints();
    }

    public List<Object> getHintKeys(){
        return new ArrayList<Object>(getDefaultHints().keySet());
    }

}
