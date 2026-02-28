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
@DisplayName("WsServerResponse DTO Tests")
class WsServerResponseTest {

    @Test
    @DisplayName("WsServerResponse: Should set and get port")
    void testPortGetterSetter() {
        WsServerResponse response = new WsServerResponse();

        response.setPort(8080);

        assertEquals(8080, response.getPort());
    }

    @Test
    @DisplayName("WsServerResponse: Should set and get IP")
    void testIPGetterSetter() {
        WsServerResponse response = new WsServerResponse();

        response.setIP("192.168.1.1");

        assertEquals("192.168.1.1", response.getIP());
    }

    @Test
    @DisplayName("WsServerResponse: Should set and get reason")
    void testReasonGetterSetter() {
        WsServerResponse response = new WsServerResponse();

        response.setReason("Connection refused");

        assertEquals("Connection refused", response.getReason());
    }

    @Test
    @DisplayName("WsServerResponse: Should set and get domain")
    void testDomainGetterSetter() {
        WsServerResponse response = new WsServerResponse();

        response.setDomain("api.sonoff.com");

        assertEquals("api.sonoff.com", response.getDomain());
    }

    @Test
    @DisplayName("WsServerResponse: Should set and get error")
    void testErrorGetterSetter() {
        WsServerResponse response = new WsServerResponse();

        response.setError(0);

        assertEquals(0, response.getError());
    }

    @Test
    @DisplayName("WsServerResponse: Should handle null port")
    void testNullPort() {
        WsServerResponse response = new WsServerResponse();

        response.setPort(null);

        assertNull(response.getPort());
    }

    @Test
    @DisplayName("WsServerResponse: Should handle null IP")
    void testNullIP() {
        WsServerResponse response = new WsServerResponse();

        response.setIP(null);

        assertNull(response.getIP());
    }

    @Test
    @DisplayName("WsServerResponse: Should handle null reason")
    void testNullReason() {
        WsServerResponse response = new WsServerResponse();

        response.setReason(null);

        assertNull(response.getReason());
    }

    @Test
    @DisplayName("WsServerResponse: Should handle null domain")
    void testNullDomain() {
        WsServerResponse response = new WsServerResponse();

        response.setDomain(null);

        assertNull(response.getDomain());
    }

    @Test
    @DisplayName("WsServerResponse: Should handle null error")
    void testNullError() {
        WsServerResponse response = new WsServerResponse();

        response.setError(null);

        assertNull(response.getError());
    }

    @Test
    @DisplayName("WsServerResponse: Should set all properties together")
    void testAllPropertiesTogether() {
        WsServerResponse response = new WsServerResponse();

        response.setPort(443);
        response.setIP("10.0.0.1");
        response.setReason("Success");
        response.setDomain("ws.example.com");
        response.setError(0);

        assertEquals(443, response.getPort());
        assertEquals("10.0.0.1", response.getIP());
        assertEquals("Success", response.getReason());
        assertEquals("ws.example.com", response.getDomain());
        assertEquals(0, response.getError());
    }

    @Test
    @DisplayName("WsServerResponse: Should handle different port numbers")
    void testDifferentPorts() {
        WsServerResponse response = new WsServerResponse();

        response.setPort(80);
        assertEquals(80, response.getPort());

        response.setPort(443);
        assertEquals(443, response.getPort());

        response.setPort(8080);
        assertEquals(8080, response.getPort());

        response.setPort(65535);
        assertEquals(65535, response.getPort());
    }

    @Test
    @DisplayName("WsServerResponse: Should handle IPv4 addresses")
    void testIPv4Addresses() {
        WsServerResponse response = new WsServerResponse();

        response.setIP("192.168.1.1");
        assertEquals("192.168.1.1", response.getIP());

        response.setIP("10.0.0.1");
        assertEquals("10.0.0.1", response.getIP());

        response.setIP("172.16.0.1");
        assertEquals("172.16.0.1", response.getIP());
    }

    @Test
    @DisplayName("WsServerResponse: Should handle IPv6 addresses")
    void testIPv6Addresses() {
        WsServerResponse response = new WsServerResponse();

        response.setIP("2001:0db8:85a3:0000:0000:8a2e:0370:7334");

        assertEquals("2001:0db8:85a3:0000:0000:8a2e:0370:7334", response.getIP());
    }

    @Test
    @DisplayName("WsServerResponse: Should handle different error codes")
    void testDifferentErrorCodes() {
        WsServerResponse response = new WsServerResponse();

        response.setError(0);
        assertEquals(0, response.getError());

        response.setError(400);
        assertEquals(400, response.getError());

        response.setError(500);
        assertEquals(500, response.getError());

        response.setError(-1);
        assertEquals(-1, response.getError());
    }

    @Test
    @DisplayName("WsServerResponse: Should handle empty strings")
    void testEmptyStrings() {
        WsServerResponse response = new WsServerResponse();

        response.setIP("");
        response.setReason("");
        response.setDomain("");

        assertEquals("", response.getIP());
        assertEquals("", response.getReason());
        assertEquals("", response.getDomain());
    }

    @Test
    @DisplayName("WsServerResponse: Should handle different domain formats")
    void testDifferentDomainFormats() {
        WsServerResponse response = new WsServerResponse();

        response.setDomain("example.com");
        assertEquals("example.com", response.getDomain());

        response.setDomain("sub.domain.example.com");
        assertEquals("sub.domain.example.com", response.getDomain());

        response.setDomain("api-v2.example.co.uk");
        assertEquals("api-v2.example.co.uk", response.getDomain());
    }
}
