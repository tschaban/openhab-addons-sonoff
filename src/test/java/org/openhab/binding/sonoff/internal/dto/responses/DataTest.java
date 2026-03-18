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
package org.openhab.binding.sonoff.internal.dto.responses;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * @author David Murton - Initial contribution
 */
@DisplayName("Data Response DTO Tests")
class DataTest {

    @Test
    @DisplayName("Data: Should set and get user")
    void testUserGetterSetter() {
        Data data = new Data();
        User user = new User();
        user.setEmail("test@example.com");

        data.setUser(user);

        assertNotNull(data.getUser());
        assertEquals("test@example.com", data.getUser().getEmail());
    }

    @Test
    @DisplayName("Data: Should set and get at token")
    void testAtGetterSetter() {
        Data data = new Data();
        String atToken = "access-token-123";

        data.setAt(atToken);

        assertEquals(atToken, data.getAt());
    }

    @Test
    @DisplayName("Data: Should set and get rt token")
    void testRtGetterSetter() {
        Data data = new Data();
        String rtToken = "refresh-token-456";

        data.setRt(rtToken);

        assertEquals(rtToken, data.getRt());
    }

    @Test
    @DisplayName("Data: Should set and get region")
    void testRegionGetterSetter() {
        Data data = new Data();
        String region = "us";

        data.setRegion(region);

        assertEquals(region, data.getRegion());
    }

    @Test
    @DisplayName("Data: Should handle null user")
    void testNullUser() {
        Data data = new Data();

        data.setUser(null);

        assertNull(data.getUser());
    }

    @Test
    @DisplayName("Data: Should handle null tokens")
    void testNullTokens() {
        Data data = new Data();

        data.setAt(null);
        data.setRt(null);

        assertNull(data.getAt());
        assertNull(data.getRt());
    }

    @Test
    @DisplayName("Data: Should handle null region")
    void testNullRegion() {
        Data data = new Data();

        data.setRegion(null);

        assertNull(data.getRegion());
    }

    @Test
    @DisplayName("Data: Should set all properties together")
    void testAllPropertiesTogether() {
        Data data = new Data();
        User user = new User();
        user.setApikey("apikey-789");

        data.setUser(user);
        data.setAt("at-token");
        data.setRt("rt-token");
        data.setRegion("eu");

        assertNotNull(data.getUser());
        assertEquals("apikey-789", data.getUser().getApikey());
        assertEquals("at-token", data.getAt());
        assertEquals("rt-token", data.getRt());
        assertEquals("eu", data.getRegion());
    }

    @Test
    @DisplayName("Data: Should handle different regions")
    void testDifferentRegions() {
        Data data = new Data();

        data.setRegion("us");
        assertEquals("us", data.getRegion());

        data.setRegion("eu");
        assertEquals("eu", data.getRegion());

        data.setRegion("cn");
        assertEquals("cn", data.getRegion());

        data.setRegion("as");
        assertEquals("as", data.getRegion());
    }

    @Test
    @DisplayName("Data: Should handle empty strings")
    void testEmptyStrings() {
        Data data = new Data();

        data.setAt("");
        data.setRt("");
        data.setRegion("");

        assertEquals("", data.getAt());
        assertEquals("", data.getRt());
        assertEquals("", data.getRegion());
    }
}
