/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.sonoff.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SonoffBindingConstants}.
 *
 * @author Test Author - Initial contribution
 */
class SonoffBindingConstantsTest {

    @Test
    void testBindingIdValue() {
        // Test that the binding ID has the expected value
        assertEquals("sonoff", SonoffBindingConstants.BINDING_ID);
    }

    @Test
    void testLanInSetNotEmpty() {
        // Test that LAN_IN set is not empty
        assertNotNull(SonoffBindingConstants.LAN_IN);
        assertFalse(SonoffBindingConstants.LAN_IN.isEmpty());
        assertTrue(SonoffBindingConstants.LAN_IN.contains(1));
    }

    @Test
    void testLanOutSetNotEmpty() {
        // Test that LAN_OUT set is not empty
        assertNotNull(SonoffBindingConstants.LAN_OUT);
        assertFalse(SonoffBindingConstants.LAN_OUT.isEmpty());
        assertTrue(SonoffBindingConstants.LAN_OUT.contains(1));
    }

    @Test
    void testBasicAssertion() {
        // Basic test to verify test framework functionality
        assertTrue(true);
        assertEquals(2, 1 + 1);
        assertNotEquals("hello", "world");
    }
}