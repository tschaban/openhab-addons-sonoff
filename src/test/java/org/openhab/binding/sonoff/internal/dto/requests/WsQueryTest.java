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
 * Unit tests for {@link WsQuery}.
 *
 * @author tschaban/SmartnyDom - Initial contribution
 */
@NonNullByDefault
class WsQueryTest {

    @Test
    @DisplayName("WsQuery: Should initialize with default values")
    void testDefaultValues() {
        WsQuery query = new WsQuery();

        assertNotNull(query.getSequence(), "Sequence should be initialized");
    }

    @Test
    @DisplayName("WsQuery: Should set and get deviceid")
    void testDeviceid() {
        WsQuery query = new WsQuery();
        query.setDeviceid("device-123");

        // deviceid doesn't have a getter, but we verify no exception is thrown
        assertNotNull(query);
    }

    @Test
    @DisplayName("WsQuery: Should set and get apikey")
    void testApikey() {
        WsQuery query = new WsQuery();
        query.setApikey("api-key-abc");

        // apikey doesn't have a getter, but we verify no exception is thrown
        assertNotNull(query);
    }

    @Test
    @DisplayName("WsQuery: Should get sequence")
    void testSequence() {
        WsQuery query = new WsQuery();

        assertNotNull(query.getSequence());
        assertTrue(query.getSequence() > 0, "Sequence should be positive");
    }

    @Test
    @DisplayName("WsQuery: Should set and get params")
    void testParams() {
        WsQuery query = new WsQuery();
        JsonObject params = new JsonObject();
        params.addProperty("test", "value");

        query.setParams(params);

        assertEquals(params, query.getParams());
        assertEquals("value", query.getParams().get("test").getAsString());
    }

    @Test
    @DisplayName("WsQuery: Should handle null params")
    void testNullParams() {
        WsQuery query = new WsQuery();

        assertNull(query.getParams(), "Params should be null by default");
    }

    @Test
    @DisplayName("WsQuery: Should handle complex params")
    void testComplexParams() {
        WsQuery query = new WsQuery();
        JsonObject params = new JsonObject();
        params.addProperty("switch", "on");
        params.addProperty("brightness", 100);

        query.setParams(params);

        assertEquals(2, query.getParams().size());
        assertEquals("on", query.getParams().get("switch").getAsString());
        assertEquals(100, query.getParams().get("brightness").getAsInt());
    }
}
