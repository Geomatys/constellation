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
