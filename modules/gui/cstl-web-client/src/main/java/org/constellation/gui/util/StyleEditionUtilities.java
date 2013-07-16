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

package org.constellation.gui.util;

import org.apache.sis.util.Static;
import org.constellation.gui.mapped.Rule;
import org.geotoolkit.style.MutableRule;
import org.geotoolkit.style.MutableStyle;
import org.opengis.style.Symbolizer;

import java.util.ArrayList;
import java.util.List;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class StyleEditionUtilities extends Static {

    public static final Rule DEFAULT_RULE = new Rule();
    static {
        DEFAULT_RULE.setName("Change me!");
        DEFAULT_RULE.setTitle("");
        DEFAULT_RULE.setDescription("");
    }

    /**
     * Extracts the {@link MutableRule} list from the given {@link MutableStyle}.
     *
     * @param style the {@link MutableRule} to visit
     * @return the {@link MutableRule} list
     */
    private static List<MutableRule> getRules(final MutableStyle style) {
        ensureNonNull("style", style);
        if (!style.featureTypeStyles().isEmpty()) {
            return style.featureTypeStyles().get(0).rules();
        }
        return new ArrayList<MutableRule>(0);
    }

    /**
     * Extracts the {@link MutableRule} names from the given {@link MutableStyle}.
     *
     * @param style the {@link MutableRule} to visit
     * @return the list of rule names
     */
    public static List<String> getRuleNames(final MutableStyle style) {
        ensureNonNull("style", style);
        final List<String> ruleNames = new ArrayList<String>(0);
        for (final MutableRule rule : getRules(style)) {
            ruleNames.add(rule.getName());
        }
        return ruleNames;
    }

    public static Rule getRule(final MutableStyle style, final int ruleIndex) {
        ensureNonNull("style", style);
        final List<MutableRule> rules = getRules(style);
        if (ruleIndex >= 0 && ruleIndex < rules.size()) {
            return new Rule(rules.get(ruleIndex));
        } else {
            return DEFAULT_RULE;
        }
    }

    public static void removeRule(final MutableStyle style, final int ruleIndex) {
        ensureNonNull("style", style);
        final List<MutableRule> rules = getRules(style);
        if (ruleIndex >= 0 && ruleIndex < rules.size()) {
            rules.remove(ruleIndex);
        }
    }

    public static void updateRule(final MutableStyle style, final int ruleIndex, final Rule rule) {
        ensureNonNull("style", style);
        ensureNonNull("rule", rule);
        final List<MutableRule> rules = getRules(style);
        if (ruleIndex >= 0 && ruleIndex < rules.size()) {
            rules.set(ruleIndex, rule.getBinding());
        }
    }

    public static void addRule(final MutableStyle style, final Rule rule) {
        ensureNonNull("style", style);
        ensureNonNull("rule", rule);
        getRules(style).add(rule.getBinding());
    }

    public static void removeAllRules(final MutableStyle style) {
        ensureNonNull("style", style);
        getRules(style).clear();
    }

    /**
     * Extracts a {@link Symbolizer} at the specified index from the given {@link MutableStyle}.
     *
     * @param style       the {@link MutableRule} to visit
     * @param ruleIndex   the rule index
     * @param symbolIndex the symbolizer index
     * @return the {@link Symbolizer} or {@code null}
     */
    public static Symbolizer getSymbolizer(final MutableStyle style, final int ruleIndex, final int symbolIndex) {
        ensureNonNull("style", style);
        final List<MutableRule> rules = getRules(style);
        if (ruleIndex < 0 && ruleIndex >= rules.size()) {
            return null;
        }
        final List<Symbolizer> symbolizers = rules.get(ruleIndex).symbolizers();
        if (symbolIndex < 0 && symbolIndex >= symbolizers.size()) {
            return null;
        }
        return symbolizers.get(symbolIndex);
    }
}
