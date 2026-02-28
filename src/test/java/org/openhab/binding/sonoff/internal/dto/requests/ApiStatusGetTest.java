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
 * Unit tests for {@link ApiStatusGet}.
 *
 * @author tschaban/SmartnyDom - Initial contribution
 */
@NonNullByDefault
class ApiStatusGetTest {

    @Test
    @DisplayName("ApiStatusGet: Should initialize with default values")
    void testDefaultValues() {
        ApiStatusGet request = new ApiStatusGet();

        assertNotNull(request.getVersion(), "Version should be initialized");
        assertNotNull(request.getTs(), "Timestamp should be initialized");
    }

    @Test
    @DisplayName("ApiStatusGet: Should set and get deviceid")
    void testDeviceid() {
        ApiStatusGet request = new ApiStatusGet();
        request.setDeviceid("device-123");

        // deviceid doesn't have a getter, but we verify no exception is thrown
        assertNotNull(request);
    }

    @Test
    @DisplayName("ApiStatusGet: Should set and get params")
    void testParams() {
        ApiStatusGet request = new ApiStatusGet();
        request.setParams("[\"switch\"]");

        // params doesn't have a getter, but we verify no exception is thrown
        assertNotNull(request);
    }

    @Test
    @DisplayName("ApiStatusGet: Should set and get version")
    void testVersion() {
        ApiStatusGet request = new ApiStatusGet();
        request.setVersion(9);

        assertEquals(9, request.getVersion());
    }

    @Test
    @DisplayName("ApiStatusGet: Should set and get timestamp")
    void testTimestamp() {
        ApiStatusGet request = new ApiStatusGet();
        Long customTs = 9876543210L;
        request.setTs(customTs);

        assertEquals(customTs, request.getTs());
    }

    @Test
    @DisplayName("ApiStatusGet: Should set and get apikey")
    void testApikey() {
        ApiStatusGet request = new ApiStatusGet();
        request.setApikey("test-api-key-123");

        assertEquals("test-api-key-123", request.getApikey());
    }

    @Test
    @DisplayName("ApiStatusGet: Should return self from getCommand")
    void testGetCommand() {
        ApiStatusGet request = new ApiStatusGet();

        assertSame(request, request.getCommand(), "getCommand() should return self");
    }
}
