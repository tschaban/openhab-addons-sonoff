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
package org.openhab.binding.sonoff.internal.dto.requests;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ApiLoginRequest}.
 *
 * @author tschaban/SmartnyDom - Initial contribution
 */
@NonNullByDefault
class ApiLoginRequestTest {

    @Test
    @DisplayName("ApiLoginRequest: Should create with all parameters")
    void testConstructor() {
        ApiLoginRequest request = new ApiLoginRequest("test@example.com", "password123", "+1");

        assertEquals("test@example.com", request.getEmail());
        assertEquals("password123", request.getPassword());
        assertEquals("+1", request.getCountryCode());
    }

    @Test
    @DisplayName("ApiLoginRequest: Should return self from getCommand")
    void testGetCommand() {
        ApiLoginRequest request = new ApiLoginRequest("user@test.com", "pass", "+44");

        assertSame(request, request.getCommand(), "getCommand() should return self");
    }

    @Test
    @DisplayName("ApiLoginRequest: Should handle different country codes")
    void testDifferentCountryCodes() {
        ApiLoginRequest request1 = new ApiLoginRequest("test@example.com", "pass", "+48");
        ApiLoginRequest request2 = new ApiLoginRequest("test@example.com", "pass", "+49");

        assertEquals("+48", request1.getCountryCode());
        assertEquals("+49", request2.getCountryCode());
    }

    @Test
    @DisplayName("ApiLoginRequest: Should handle empty values")
    void testEmptyValues() {
        ApiLoginRequest request = new ApiLoginRequest("", "", "");

        assertEquals("", request.getEmail());
        assertEquals("", request.getPassword());
        assertEquals("", request.getCountryCode());
    }
}
