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
package org.constellation.menu.service;

import java.util.logging.Logger;
import javax.xml.namespace.QName;
import org.constellation.configuration.Layer;
import org.constellation.configuration.Source;
import org.geotoolkit.util.logging.Logging;

/**
 *
 * @author jsorel
 */
public class SourceElement {
    private final Source parent;
    private final String name;

    protected static final Logger LOGGER = Logging.getLogger("org.constellation.bean");
    
    public SourceElement(final Source parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    private boolean isLoadAll() {
        return Boolean.TRUE.equals(parent.getLoadAll());
    }

    public Boolean getIncluded() {
        LOGGER.info("included?");
        if (!isLoadAll()) {
            
            for (Layer l : parent.getInclude()) {
                if (name.equals(l.getName().getLocalPart())) {
                    LOGGER.info("yes is included");
                    return true;
                }
            }
            LOGGER.info("noincluded");
            return false;
        }
        LOGGER.info("loadall?");
        return true;
    }

    public void setIncluded(Boolean included) {
        LOGGER.info("set included:" + included);
        if (included == null) {
            return;
        }
        if (included.equals(getIncluded())) {
            //already same state
            return;
        }
        if (included) {
            //add layer
            final Layer layer = new Layer();
            layer.setName(new QName(name));
            parent.getInclude().add(layer);
        } else {
            //remove layer
            for (int i = 0, n = parent.getInclude().size(); i < n; i++) {
                final Layer l = parent.getInclude().get(i);
                if (name.equals(l.getName().getLocalPart())) {
                    parent.getInclude().remove(i);
                    break;
                }
            }
        }
    }

    public Boolean getExcluded() {
        if (isLoadAll()) {
            for (Layer l : parent.getExclude()) {
                if (name.equals(l.getName().getLocalPart())) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    public void setExcluded(final Boolean excluded) {
        if (excluded == null) {
            return;
        }
        if (excluded.equals(getExcluded())) {
            //already same state
            return;
        }
        if (excluded) {
            //add layer
            final Layer layer = new Layer();
            layer.setName(new QName(name));
            parent.getExclude().add(layer);
        } else {
            //remove layer
            for (int i = 0, n = parent.getExclude().size(); i < n; i++) {
                final Layer l = parent.getExclude().get(i);
                if (name.equals(l.getName().getLocalPart())) {
                    parent.getExclude().remove(i);
                    break;
                }
            }
        }
    }
    
}
