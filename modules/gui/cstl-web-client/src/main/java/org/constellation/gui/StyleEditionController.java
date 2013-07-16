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

package org.constellation.gui;

import com.google.common.base.Strings;
import juzu.Action;
import juzu.Path;
import juzu.RequestScoped;
import juzu.Resource;
import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.plugin.ajax.Ajax;
import juzu.template.Template;
import org.apache.sis.util.logging.Logging;
import org.constellation.gui.mapped.Rule;
import org.constellation.gui.service.StyleManager;
import org.constellation.gui.util.JuzuUtilities;
import org.constellation.gui.util.StyleEditionUtilities;
import org.constellation.gui.util.StyleEditionWorkspace;
import org.geotoolkit.style.MutableStyle;
import org.opengis.style.Symbolizer;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * StyledLayerDescriptor controller to manage style edition.
 *
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
@RequestScoped
public final class StyleEditionController {

    /**
     * Use for debugging purpose.
     */
    private static final Logger LOGGER = Logging.getLogger(StyleEditionController.class);

    /**
     * Style edition template.
     */
    @Inject
    @Path("StyleEdition.gtmpl")
    Template styleEdition;

    /**
     * Rule edition template.
     */
    @Inject
    @Path("RuleEdition.gtmpl")
    Template ruleEdition;

    /**
     * Symbolizer edition template.
     */
    @Inject
    @Path("SymbolizerEdition.gtmpl")
    Template symbolizerEdition;

    /**
     * Constellation style API manager.
     */
    @Inject
    private StyleManager manager;

    /**
     * Current edited style binding.
     */
    private MutableStyle style;


    /**
     * Post injection callback.
     * <p>
     * This controller is request scoped so this method is called on each request.
     */
    @PostConstruct
    public void postConstruct() {
        final String providerId = JuzuUtilities.getRequestParameter("providerId");
        final String styleName  = JuzuUtilities.getRequestParameter("styleName");
        if (!Strings.isNullOrEmpty(providerId) && !Strings.isNullOrEmpty(styleName)) {
            try {
                /*
                 * For performance issues and to make edition not effective before the final
                 * validation/publication, we work in a temporary workspace (session scoped
                 * folder).
                 *
                 * Try to find the current edited style from workspace. If the style is not
                 * present in the workspace directory, use the constellation api to load it
                 * and save it into the workspace directory for future requests.
                 */
                style = StyleEditionWorkspace.acquire(providerId, styleName);
                if (style == null) {
                    style = manager.getStyle(providerId, styleName);
                    StyleEditionWorkspace.save(providerId, styleName, style);
                }
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "An error occurred when trying to acquire the" +
                        "style named \"" + styleName + "\" from the provider with id \"" +
                        providerId + "\".", ex);
            }
        }
    }


    /**************************************************************************
     *                                 Views                                  *
     **************************************************************************/

    /**
     * View for StyledLayerDescriptor edition.
     *
     * @param providerId the style provider id
     * @param styleName  the style name
     * @return the view {@link Response}
     */
    @View
    @Route("edition/style")
    public Response styleEdition(final String providerId, final String styleName) {
        // Ensure non null style.
        if (style == null) {
            return Controller_.index();
        }

        // Extract the rule names.
        final List<String> rules = StyleEditionUtilities.getRuleNames(style);

        // Go to view with appropriate input parameters.
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("providerId", providerId);
        parameters.put("styleName" , styleName);
        parameters.put("rules"     , rules);
        return styleEdition.ok(parameters).withMimeType("text/html");
    }

    /**
     * View for StyledLayerDescriptor rule edition.
     *
     * @param providerId the style provider id
     * @param styleName  the style name
     * @param method     the rule edition method
     * @param ruleIndex  the rule index in style
     * @return the view {@link Response}
     */
    @View
    @Route("edition/rule")
    public Response ruleEdition(final String providerId, final String styleName, final String method, final String ruleIndex) {
        // Ensure non null style.
        if (style == null) {
            return Controller_.index();
        }

        // Acquire or create the rule to edit.
        final Rule rule;
        if (ruleIndex != null) {
            rule = StyleEditionUtilities.getRule(style, Integer.parseInt(ruleIndex));
        } else {
            rule = StyleEditionUtilities.DEFAULT_RULE;
        }

        // Go to view with appropriate input parameters.
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("providerId", providerId);
        parameters.put("styleName" , styleName);
        parameters.put("ruleIndex" , ruleIndex);
        parameters.put("rule"      , rule);
        parameters.put("method"    , method);
        return ruleEdition.ok(parameters).withMimeType("text/html");
    }

    /**
     * View for StyledLayerDescriptor symbolizer edition.
     *
     * @param providerId  the style provider id
     * @param styleName   the style name
     * @param ruleIndex   the rule index in style
     * @param symbolIndex the symbolizer index in rule
     * @return the view {@link Response}
     */
    @View
    @Route("edition/symbolizer")
    public Response symbolizerEdition(final String providerId, final String styleName, final String ruleIndex, final String symbolIndex) {
        // Ensure non null style.
        if (style == null) {
            return Controller_.index();
        }

        // Acquire or create the symbolizer to edit.
        final Symbolizer symbolizer;
        if (symbolIndex != null) {
            symbolizer = StyleEditionUtilities.getSymbolizer(style, Integer.parseInt(ruleIndex), Integer.parseInt(symbolIndex));
        } else {
            symbolizer = null;
        }

        // Go to view with appropriate input parameters.
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("providerId", providerId);
        parameters.put("styleName" , styleName);
        parameters.put("ruleIndex" , ruleIndex);
        parameters.put("symbolizer", symbolizer);
        return symbolizerEdition.ok(parameters).withMimeType("text/html");
    }


    /**************************************************************************
     *                         Style update methods                           *
     **************************************************************************/

    /**
     * Removes the specified rule from the style.
     *
     * @param providerId  the style provider id
     * @param styleName   the style name
     * @param ruleIndex   the rule index in style
     * @return the status {@link Response}
     * @throws IOException on temporary style update error
     */
    @Ajax
    @Resource
    @Route("edition/style/remove")
    public Response removeRule(final String providerId, final String styleName, final String ruleIndex) throws IOException {
        // Update the temporary style.
        StyleEditionUtilities.removeRule(style, Integer.parseInt(ruleIndex));
        StyleEditionWorkspace.save(providerId, styleName, style);

        // Return operation status.
        return JuzuUtilities.success();
    }

    /**
     * Removes all the rules from the style.
     *
     * @param providerId  the style provider id
     * @param styleName   the style name
     * @return the status {@link Response}
     * @throws IOException on temporary style update error
     */
    @Ajax
    @Resource
    @Route("edition/style/removeAll")
    public Response removeAllRules(final String providerId, final String styleName) throws IOException {
        // Update the temporary style.
        StyleEditionUtilities.removeAllRules(style);
        StyleEditionWorkspace.save(providerId, styleName, style);

        // Return operation status.
        return JuzuUtilities.success();
    }

    /**
     * Updates a rule in the style.
     *
     * TODO: handle IOException
     *
     * @param providerId  the style provider id
     * @param styleName   the style name
     * @param ruleIndex   the rule index in style
     * @param rule        the rule form model
     * @return the status {@link Response}
     * @throws IOException on temporary style update error
     */
    @Action
    @Route("edition/rule/update")
    public Response updateRule(final String providerId, final String styleName, final String ruleIndex, final Rule rule) throws IOException {
        // Update the temporary style.
        if (ruleIndex != null) {
            StyleEditionUtilities.updateRule(style, Integer.parseInt(ruleIndex), rule);
        } else {
            StyleEditionUtilities.addRule(style, rule);
        }
        StyleEditionWorkspace.save(providerId, styleName, style);

        // Go to view with appropriate input parameters.
        return StyleEditionController_.styleEdition(providerId, styleName);
    }
}
