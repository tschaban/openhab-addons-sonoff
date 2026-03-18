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
@DisplayName("User Response DTO Tests")
class UserTest {

    @Test
    @DisplayName("User: Should set and get countryCode")
    void testCountryCodeGetterSetter() {
        User user = new User();

        user.setCountryCode("+1");

        assertEquals("+1", user.getCountryCode());
    }

    @Test
    @DisplayName("User: Should set and get email")
    void testEmailGetterSetter() {
        User user = new User();

        user.setEmail("user@example.com");

        assertEquals("user@example.com", user.getEmail());
    }

    @Test
    @DisplayName("User: Should set and get apikey")
    void testApikeyGetterSetter() {
        User user = new User();

        user.setApikey("apikey-12345");

        assertEquals("apikey-12345", user.getApikey());
    }

    @Test
    @DisplayName("User: Should set and get accountLevel")
    void testAccountLevelGetterSetter() {
        User user = new User();

        user.setAccountLevel(1L);

        assertEquals(1L, user.getAccountLevel());
    }

    @Test
    @DisplayName("User: Should set and get accountConsult")
    void testAccountConsultGetterSetter() {
        User user = new User();

        user.setAccountConsult(true);

        assertTrue(user.getAccountConsult());
    }

    @Test
    @DisplayName("User: Should set and get denyRecharge")
    void testDenyRechargeGetterSetter() {
        User user = new User();

        user.setDenyRecharge(false);

        assertFalse(user.getDenyRecharge());
    }

    @Test
    @DisplayName("User: Should set and get ipCountry")
    void testIpCountryGetterSetter() {
        User user = new User();

        user.setIpCountry("US");

        assertEquals("US", user.getIpCountry());
    }

    @Test
    @DisplayName("User: Should handle null values for strings")
    void testNullStringValues() {
        User user = new User();

        user.setCountryCode(null);
        user.setEmail(null);
        user.setApikey(null);
        user.setIpCountry(null);

        assertNull(user.getCountryCode());
        assertNull(user.getEmail());
        assertNull(user.getApikey());
        assertNull(user.getIpCountry());
    }

    @Test
    @DisplayName("User: Should handle null values for Long and Boolean")
    void testNullObjectValues() {
        User user = new User();

        user.setAccountLevel(null);
        user.setAccountConsult(null);
        user.setDenyRecharge(null);

        assertNull(user.getAccountLevel());
        assertNull(user.getAccountConsult());
        assertNull(user.getDenyRecharge());
    }

    @Test
    @DisplayName("User: Should set all properties together")
    void testAllPropertiesTogether() {
        User user = new User();

        user.setCountryCode("+44");
        user.setEmail("test@test.com");
        user.setApikey("key-abc-123");
        user.setAccountLevel(5L);
        user.setAccountConsult(true);
        user.setDenyRecharge(false);
        user.setIpCountry("GB");

        assertEquals("+44", user.getCountryCode());
        assertEquals("test@test.com", user.getEmail());
        assertEquals("key-abc-123", user.getApikey());
        assertEquals(5L, user.getAccountLevel());
        assertTrue(user.getAccountConsult());
        assertFalse(user.getDenyRecharge());
        assertEquals("GB", user.getIpCountry());
    }

    @Test
    @DisplayName("User: Should handle different country codes")
    void testDifferentCountryCodes() {
        User user = new User();

        user.setCountryCode("+1");
        assertEquals("+1", user.getCountryCode());

        user.setCountryCode("+44");
        assertEquals("+44", user.getCountryCode());

        user.setCountryCode("+86");
        assertEquals("+86", user.getCountryCode());
    }

    @Test
    @DisplayName("User: Should handle different account levels")
    void testDifferentAccountLevels() {
        User user = new User();

        user.setAccountLevel(0L);
        assertEquals(0L, user.getAccountLevel());

        user.setAccountLevel(10L);
        assertEquals(10L, user.getAccountLevel());

        user.setAccountLevel(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, user.getAccountLevel());
    }

    @Test
    @DisplayName("User: Should handle boolean toggles")
    void testBooleanToggles() {
        User user = new User();

        user.setAccountConsult(false);
        assertFalse(user.getAccountConsult());

        user.setAccountConsult(true);
        assertTrue(user.getAccountConsult());

        user.setDenyRecharge(true);
        assertTrue(user.getDenyRecharge());

        user.setDenyRecharge(false);
        assertFalse(user.getDenyRecharge());
    }

    @Test
    @DisplayName("User: Should handle empty email string")
    void testEmptyEmail() {
        User user = new User();

        user.setEmail("");

        assertEquals("", user.getEmail());
    }

    @Test
    @DisplayName("User: Should handle complex email addresses")
    void testComplexEmailAddresses() {
        User user = new User();

        user.setEmail("user+tag@sub.domain.com");
        assertEquals("user+tag@sub.domain.com", user.getEmail());

        user.setEmail("user.name@example.co.uk");
        assertEquals("user.name@example.co.uk", user.getEmail());
    }
}
