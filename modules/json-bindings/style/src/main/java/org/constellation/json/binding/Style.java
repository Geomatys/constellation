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

package org.constellation.json.binding;

import org.geotoolkit.style.MutableRule;
import org.geotoolkit.style.MutableStyle;

import java.util.ArrayList;
import java.util.List;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import static org.constellation.json.util.StyleFactories.SF;
import static org.constellation.json.util.StyleUtilities.listType;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class Style implements StyleElement<MutableStyle> {

    private String name;
    private List<Rule> rules = new ArrayList<Rule>();

    public Style() {
    }

    public Style(final MutableStyle style) {
        ensureNonNull("style", style);
        final List<MutableRule> mutableRules = new ArrayList<MutableRule>(0);
        if (!style.featureTypeStyles().isEmpty()) {
            mutableRules.addAll(style.featureTypeStyles().get(0).rules());
        }

        name = style.getName();
        for (final MutableRule mutableRule : mutableRules) {
            rules.add(new Rule(mutableRule));
        }
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(final List<Rule> rules) {
        this.rules = rules;
    }

    @Override
    public MutableStyle toType() {
        final MutableStyle style = SF.style();
        style.featureTypeStyles().add(SF.featureTypeStyle());
        style.featureTypeStyles().get(0).rules().addAll(listType(rules));
        return style;
    }
}
