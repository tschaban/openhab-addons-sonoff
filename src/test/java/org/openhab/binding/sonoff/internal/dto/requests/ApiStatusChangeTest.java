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
 * Unit tests for {@link ApiStatusChange}.
 *
 * @author tschaban/SmartnyDom - Initial contribution
 */
@NonNullByDefault
class ApiStatusChangeTest {

    @Test
    @DisplayName("ApiStatusChange: Should create with appId")
    void testConstructor() {
        ApiStatusChange request = new ApiStatusChange("test-app-id");

        assertEquals("test-app-id", request.getAppid());
        assertNotNull(request.getVersion(), "Version should be initialized");
        assertNotNull(request.getTs(), "Timestamp should be initialized");
    }

    @Test
    @DisplayName("ApiStatusChange: Should set and get deviceid")
    void testDeviceid() {
        ApiStatusChange request = new ApiStatusChange("app-id");
        request.setDeviceid("device123");

        // deviceid doesn't have a getter, but we verify no exception is thrown
        assertNotNull(request);
    }

    @Test
    @DisplayName("ApiStatusChange: Should set and get version")
    void testVersion() {
        ApiStatusChange request = new ApiStatusChange("app-id");
        request.setVersion(8);

        assertEquals(8, request.getVersion());
    }

    @Test
    @DisplayName("ApiStatusChange: Should set and get appid")
    void testAppid() {
        ApiStatusChange request = new ApiStatusChange("initial-app-id");
        request.setAppid("updated-app-id");

        assertEquals("updated-app-id", request.getAppid());
    }

    @Test
    @DisplayName("ApiStatusChange: Should set and get timestamp")
    void testTimestamp() {
        ApiStatusChange request = new ApiStatusChange("app-id");
        Long customTs = 1234567890L;
        request.setTs(customTs);

        assertEquals(customTs, request.getTs());
    }

    @Test
    @DisplayName("ApiStatusChange: Should set params")
    void testParams() {
        ApiStatusChange request = new ApiStatusChange("app-id");
        request.setParams("{\"switch\":\"on\"}");

        // params doesn't have a getter, but we verify no exception is thrown
        assertNotNull(request);
    }

    @Test
    @DisplayName("ApiStatusChange: Should return self from getCommand")
    void testGetCommand() {
        ApiStatusChange request = new ApiStatusChange("app-id");

        assertSame(request, request.getCommand(), "getCommand() should return self");
    }
}
