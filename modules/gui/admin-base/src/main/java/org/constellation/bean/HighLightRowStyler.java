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
package org.constellation.bean;

import javax.swing.tree.TreeNode;
import org.mapfaces.renderkit.html.outline.OutlineRowStyler;

/**
 * Simple row styler that highlight lines on mouse over.
 * 
 * @author Johann Sorel (Geomatys)
 */
public class HighLightRowStyler implements OutlineRowStyler{

    public static final HighLightRowStyler INSTANCE = new HighLightRowStyler();
    
    protected HighLightRowStyler(){}
    
    @Override
    public String getRowStyle(final TreeNode node) {
        return "";
    }

    @Override
    public String getRowClass(final TreeNode node) {
        final TreeNode parent = node.getParent();
        if (node.getParent() != null) {
            final int index = parent.getIndex(node);
            if ((index % 2) == 0) {
                return "mouseHighLight";
            } else {
                return "mouseHighLight_alt";
            }
        }
        return "mouseHighLight";
    }
    
}
