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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Map;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import org.apache.sis.util.ArgumentChecks;
import org.geotoolkit.util.converter.NonconvertibleObjectException;
import org.geotoolkit.util.converter.SimpleConverter;

/**
 * Outline adaptor that change Collection, Array or Map objects in TreeModel.
 * 
 * @author Johann Sorel (Geomatys)
 */
public class DefaultAdaptor extends SimpleConverter<Object, TreeModel> {

    public static final DefaultAdaptor INSTANCE = new DefaultAdaptor();
    
    protected DefaultAdaptor(){}
    
    @Override
    public Class<? super Object> getSourceClass() {
        return Object.class;
    }

    @Override
    public Class<? extends TreeModel> getTargetClass() {
        return TreeModel.class;
    }

    @Override
    public TreeModel convert(Object source) throws NonconvertibleObjectException {
        ArgumentChecks.ensureNonNull("source", source);
        
        if(source instanceof TreeModel){
            return (TreeModel) source;
        }
        
        if(source instanceof Map){
            source = ((Map)source).entrySet();
        }else if(source instanceof Array){
            source = Arrays.asList((Array)source);
        }
        
        if(source instanceof Iterable){
            final DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
            for(Object obj : (Iterable)source){
                root.add(new DefaultMutableTreeNode(obj));
            }
            return new DefaultTreeModel(root);
        }
        
        throw new IllegalArgumentException("Was expecting a TreeModel, Map, Array or Iterable but was a : "+source.getClass());
    }
    
}
