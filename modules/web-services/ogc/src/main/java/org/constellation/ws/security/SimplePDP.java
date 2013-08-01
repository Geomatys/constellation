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
package org.constellation.ws.security;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.apache.sis.util.logging.Logging;


/**
 * Temporary implementation of a simple Policy Desicion Point. The full OASIS 
 * implementation is already part of the engine/xacml module
 * (org.constellation.xacml.CstlPDP). It works with a simple constraint stored
 * in a string locally.
 * 
 * TODO: remove it when the OASIS PDP will be used.
 *
 * @author Cédric Briançon (Geomatys)
 * @since 0.7
 *
 * @see org.constellation.xacml.CstlPDP
 */
public final class SimplePDP {
    /**
     * The logger for the PDP.
     */
    private static final Logger LOGGER = Logging.getLogger(SimplePDP.class);

    /**
     * Engine that will launch the evaluation of the rule.
     */
    private static final ScriptEngine SCRIPT_ENGINE =
            new ScriptEngineManager().getEngineByName("JavaScript");

    /**
     * The rule to decide whether a request should be accepted or not.
     */
    private final String rule;

    /**
     * A simple Policy Decision Point (PDP), with a unique given rule.
     *
     * @param rule The rule to test.
     */
    public SimplePDP(final String rule) {
        this.rule = rule;
    }

    /**
     * Verifies whether the request, done with a given IP address and a given referrer identifier,
     * should be allowed or not.
     *
     * @param ip The IP address from which the request was emitted.
     * @param referer The referrer identifier for the request.
     * @return {@code True} if the request is allowed according to the rules, {@code false} otherwise.
     */
    public boolean isAuthorized(final String ip, final String referer) {
        final String toEvaluate = replaceVariablesInRule(ip, referer);
        boolean result = false;
        try {
            result = (Boolean)SCRIPT_ENGINE.eval(toEvaluate);
        } catch (ScriptException ex) {
            LOGGER.log(Level.INFO, "Error trying to evaluate the expression :"+ result, ex);
        }
        return result;
    }

    /**
     * Format and return a string that can be evaluated by the
     * {@linkplain ScriptEngine javascript engine}.
     *
     * @param ip The IP address to test.
     * @param referer The referrer identifier to test.
     * @return A string that can be evaluated by the engine.
     */
    private String replaceVariablesInRule(final String ip, final String referer) {
        if (rule == null || rule.isEmpty()) {
            return "";
        }

        final StringBuilder sb = new StringBuilder("eval(");
        String predicate = rule;
        if (ip != null && !ip.isEmpty()) {
            predicate = predicate.replaceAll("\\$ip", ip);
        }
        if (referer != null && !referer.isEmpty()) {
            predicate = predicate.replaceAll("\\$referer", referer);
        }
        sb.append(predicate).append(')');
        return sb.toString();
    }
}
