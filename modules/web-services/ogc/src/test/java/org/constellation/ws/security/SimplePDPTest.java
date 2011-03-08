/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010, Geomatys
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

import org.junit.Test;
import static org.junit.Assert.*;


/**
 * Test different rules and the answer of the Simple Policy Decision Point.
 *
 * @author Cédric Briançon (Geomatys)
 * @since 0.7
 */
public class SimplePDPTest {
    /**
     * Some rules to apply.
     */
    private static final String RULE1 = "('$ip'=='127.0.0.1')";
    private static final String RULE2 = "(('$ip'=='127.0.0.1') && ('$referer'.indexOf('http://localhost:8080/en')!=-1))";
    private static final String RULE3 = "(('$ip'=='17.15.25.3') || ('$ip'=='127.0.0.1')) && "
            + "(('$referer'=='http://localhost:8080/fr/web/guest/test') || ('$referer'.indexOf('http://localhost:8080/en')!=-1))";

    /**
     * Verify the IP address fields in the rule.
     */
    @Test
    public void isAuthorizedRule1Test() {
        final SimplePDP pdp = new SimplePDP(RULE1);
        assertTrue(pdp.isAuthorized("127.0.0.1", ""));
    }

    /**
     * Ensures a bad IP address makes the PDP return {@code false}.
     */
    @Test
    public void isNotAuthorizedRule1Test() {
        final SimplePDP pdp = new SimplePDP(RULE1);
        assertFalse(pdp.isAuthorized("17.15.25.3", "test"));
    }

    /**
     * Verify both IP address and referer fields.
     */
    @Test
    public void isAuthorizedRule2Test() {
        final SimplePDP pdp = new SimplePDP(RULE2);
        assertTrue(pdp.isAuthorized("127.0.0.1", "http://localhost:8080/en/test/constellation"));
    }

    /**
     * Ensures that a referer url which is not contained in the rule makes the PDP return {@code false}.
     */
    @Test
    public void isNotAuthorizedRule2Test() {
        final SimplePDP pdp = new SimplePDP(RULE2);
        assertFalse(pdp.isAuthorized("127.0.0.1", "http://localhost:8080/fr/test"));
    }

    /**
     * Verify the IP address and that the referer url is contained in the rule.
     */
    @Test
    public void isAuthorizedRule3Test() {
        final SimplePDP pdp = new SimplePDP(RULE3);
        assertTrue(pdp.isAuthorized("17.15.25.3", "http://localhost:8080/fr/web/guest/test"));
    }

    /**
     * Ensures that a referer url which is not contained in the rule makes the PDP return {@code false}.
     */
    @Test
    public void isNotAuthorizedRule3Test() {
        final SimplePDP pdp = new SimplePDP(RULE3);
        assertFalse(pdp.isAuthorized("17.15.25.3", "http://localhost:8080/fr/web/guest/test2"));
    }
}
