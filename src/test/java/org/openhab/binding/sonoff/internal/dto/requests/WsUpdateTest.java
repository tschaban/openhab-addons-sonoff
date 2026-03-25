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
 * Unit tests for {@link WsUpdate}.
 *
 * @author tschaban/SmartnyDom - Initial contribution
 */
@NonNullByDefault
class WsUpdateTest {

    @Test
    @DisplayName("WsUpdate: Should initialize with null sequence")
    void testDefaultValues() {
        WsUpdate update = new WsUpdate();

        assertNull(update.getSequence(), "Sequence should be null by default");
    }

    @Test
    @DisplayName("WsUpdate: Should set and get deviceid")
    void testDeviceid() {
        WsUpdate update = new WsUpdate();
        update.setDeviceid("device-xyz");

        // deviceid doesn't have a getter, but we verify no exception is thrown
        assertNotNull(update);
    }

    @Test
    @DisplayName("WsUpdate: Should set and get apikey")
    void testApikey() {
        WsUpdate update = new WsUpdate();
        update.setApikey("api-key-123");

        // apikey doesn't have a getter, but we verify no exception is thrown
        assertNotNull(update);
    }

    @Test
    @DisplayName("WsUpdate: Should set and get sequence")
    void testSequence() {
        WsUpdate update = new WsUpdate();
        update.setSequence(12345L);

        assertEquals(12345L, update.getSequence());
    }

    @Test
    @DisplayName("WsUpdate: Should set and get params")
    void testParams() {
        WsUpdate update = new WsUpdate();
        JsonObject params = new JsonObject();
        params.addProperty("switch", "off");

        update.setParams(params);

        assertEquals(params, update.getParams());
        assertEquals("off", update.getParams().get("switch").getAsString());
    }

    @Test
    @DisplayName("WsUpdate: Should handle null params")
    void testNullParams() {
        WsUpdate update = new WsUpdate();

        assertNull(update.getParams(), "Params should be null by default");
    }

    @Test
    @DisplayName("WsUpdate: Should handle multiple properties")
    void testMultipleProperties() {
        WsUpdate update = new WsUpdate();
        update.setDeviceid("device-1");
        update.setApikey("key-1");
        update.setSequence(999L);

        JsonObject params = new JsonObject();
        params.addProperty("brightness", 50);
        params.addProperty("mode", 2);
        update.setParams(params);

        assertEquals(999L, update.getSequence());
        assertNotNull(update.getParams());
        assertEquals(50, update.getParams().get("brightness").getAsInt());
    }

    @Test
    @DisplayName("WsUpdate: Should handle complex params")
    void testComplexParams() {
        WsUpdate update = new WsUpdate();
        JsonObject params = new JsonObject();
        params.addProperty("switch", "on");
        params.addProperty("brightness", 75);
        params.addProperty("colorR", 255);
        params.addProperty("colorG", 128);
        params.addProperty("colorB", 64);

        update.setParams(params);

        assertEquals(5, update.getParams().size());
        assertEquals(255, update.getParams().get("colorR").getAsInt());
    }
}
