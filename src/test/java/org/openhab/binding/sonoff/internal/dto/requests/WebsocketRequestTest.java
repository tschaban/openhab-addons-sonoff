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

import com.google.gson.JsonObject;

/**
 * Unit tests for {@link WebsocketRequest}.
 *
 * @author tschaban/SmartnyDom - Initial contribution
 */
@NonNullByDefault
class WebsocketRequestTest {

    @Test
    @DisplayName("WebsocketRequest: Should create login request with all parameters")
    void testLoginConstructor() {
        WebsocketRequest request = new WebsocketRequest("app-id-123", "api-key-456", "access-token-789");

        assertEquals("userOnline", request.getAction());
        assertEquals("api-key-456", request.getApikey());
        assertEquals("access-token-789", request.getAt());
        assertEquals("app-id-123", request.getAppid());
        assertEquals("app", request.getUserAgent());
        assertNotNull(request.getNonce(), "Nonce should be initialized");
        assertNotNull(request.getTs(), "Timestamp should be initialized");
        assertNotNull(request.getVersion(), "Version should be initialized");
        assertNotNull(request.getSequence(), "Sequence should be initialized");
        assertNull(request.getDeviceid(), "Deviceid should be null for login request");
        assertNull(request.getParams(), "Params should be null for login request");
        assertNull(request.getTempRec(), "TempRec should be null for login request");
    }

    @Test
    @DisplayName("WebsocketRequest: Should create update request with all parameters")
    void testUpdateConstructor() {
        JsonObject params = new JsonObject();
        params.addProperty("switch", "on");

        WebsocketRequest request = new WebsocketRequest(123456L, "api-key-abc", "device-xyz", params);

        assertEquals("update", request.getAction());
        assertEquals("api-key-abc", request.getApikey());
        assertEquals(123456L, request.getSequence());
        assertEquals("device-xyz", request.getDeviceid());
        assertEquals(params, request.getParams());
        assertEquals("", request.getTempRec());
        assertNull(request.getAt(), "At should be null for update request");
        assertNull(request.getAppid(), "Appid should be null for update request");
        assertNull(request.getNonce(), "Nonce should be null for update request");
        assertNull(request.getTs(), "Ts should be null for update request");
        // Note: getVersion() returns primitive int and will throw NPE if version is null, so we don't test it
    }

    @Test
    @DisplayName("WebsocketRequest: Login request should have userAgent")
    void testLoginUserAgent() {
        WebsocketRequest request = new WebsocketRequest("app", "key", "token");

        assertEquals("app", request.getUserAgent());
    }

    @Test
    @DisplayName("WebsocketRequest: Update request should have userAgent")
    void testUpdateUserAgent() {
        JsonObject params = new JsonObject();
        WebsocketRequest request = new WebsocketRequest(1L, "key", "device", params);

        assertEquals("app", request.getUserAgent());
    }

    @Test
    @DisplayName("WebsocketRequest: Should handle empty params in update request")
    void testUpdateWithEmptyParams() {
        JsonObject params = new JsonObject();
        WebsocketRequest request = new WebsocketRequest(1L, "key", "device", params);

        assertNotNull(request.getParams());
        assertTrue(request.getParams().entrySet().isEmpty());
    }

    @Test
    @DisplayName("WebsocketRequest: Should handle complex params in update request")
    void testUpdateWithComplexParams() {
        JsonObject params = new JsonObject();
        params.addProperty("switch", "on");
        params.addProperty("brightness", 75);
        params.addProperty("mode", 3);

        WebsocketRequest request = new WebsocketRequest(999L, "api-key", "device-id", params);

        assertEquals(3, request.getParams().size());
        assertEquals("on", request.getParams().get("switch").getAsString());
        assertEquals(75, request.getParams().get("brightness").getAsInt());
    }
}
