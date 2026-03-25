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
 * Unit tests for {@link GeneralRequest}.
 *
 * @author tschaban/SmartnyDom - Initial contribution
 */
@NonNullByDefault
class GeneralRequestTest {

    @Test
    @DisplayName("GeneralRequest: Should create with appId")
    void testConstructor() {
        GeneralRequest request = new GeneralRequest("my-app-id");

        assertEquals("my-app-id", request.getAppid());
    }

    @Test
    @DisplayName("GeneralRequest: Should initialize nonce, ts, and version")
    void testDefaultValues() {
        GeneralRequest request = new GeneralRequest("app-id");

        assertNotNull(request.getNonce(), "Nonce should be initialized");
        assertNotNull(request.getTs(), "Timestamp should be initialized");
        assertTrue(request.getVersion() > 0, "Version should be initialized");
    }

    @Test
    @DisplayName("GeneralRequest: Should set and get accept")
    void testAccept() {
        GeneralRequest request = new GeneralRequest("app-id");
        request.setAccept("application/json");

        assertEquals("application/json", request.getAccept());
    }

    @Test
    @DisplayName("GeneralRequest: Should handle null accept")
    void testNullAccept() {
        GeneralRequest request = new GeneralRequest("app-id");

        assertNull(request.getAccept(), "Accept should be null by default");
    }

    @Test
    @DisplayName("GeneralRequest: Should create multiple instances with different values")
    void testMultipleInstances() {
        GeneralRequest request1 = new GeneralRequest("app-1");
        GeneralRequest request2 = new GeneralRequest("app-2");

        assertEquals("app-1", request1.getAppid());
        assertEquals("app-2", request2.getAppid());
        assertNotEquals(request1.getNonce(), request2.getNonce(), "Different instances should have different nonces");
    }
}
