/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
        style.setName(name);
        style.featureTypeStyles().add(SF.featureTypeStyle());
        style.featureTypeStyles().get(0).rules().addAll(listType(rules));
        return style;
    }
}
