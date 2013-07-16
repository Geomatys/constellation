/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2012, Geomatys
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

package org.constellation.gui.mapped;

import juzu.Mapped;
import org.apache.sis.util.iso.DefaultInternationalString;
import org.geotoolkit.style.DefaultDescription;
import org.geotoolkit.style.DefaultMutableRule;
import org.geotoolkit.style.MutableRule;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
@Mapped
public class Rule implements Serializable {

    private String name;

    private String title;

    private String description;


    public Rule() {
    }

    public Rule(final MutableRule rule) {
        this.name = rule.getName();
        if (rule.getDescription() != null) {
            this.title       = rule.getDescription().getTitle().toString();
            this.description = rule.getDescription().getAbstract().toString();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        try {
            this.name = URLDecoder.decode(name, "UTF-8");
        } catch (UnsupportedEncodingException ignore) {
            // should never happen
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        try {
            this.title = URLDecoder.decode(title, "UTF-8");
        } catch (UnsupportedEncodingException ignore) {
            // should never happen
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        try {
            this.description = URLDecoder.decode(description, "UTF-8");
        } catch (UnsupportedEncodingException ignore) {
            // should never happen
        }
    }

    public MutableRule getBinding() {
        final MutableRule rule = new DefaultMutableRule();
        rule.setName(this.name);
        rule.setDescription(new DefaultDescription(
            this.title != null ? new DefaultInternationalString(this.title) : null,
            this.description != null ? new DefaultInternationalString(this.description) : null
        ));
        return rule;
    }
}
