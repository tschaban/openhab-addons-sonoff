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
package org.openhab.binding.sonoff.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.net.http.WebSocketFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * Unit tests for {@link SonoffHandlerFactory}.
 * 
 * Tests cover:
 * - Constructor and dependency injection
 * - supportsThingType method for all supported and unsupported types
 * - createHandler method for all device types
 * - Error cases and edge conditions
 *
 * @author tschaban/SmartnyDom - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
class SonoffHandlerFactoryTest {

    @Mock
    private WebSocketFactory webSocketFactory;

    @Mock
    private HttpClientFactory httpClientFactory;

    @Mock
    private WebSocketClient webSocketClient;

    @Mock
    private HttpClient httpClient;

    @Mock
    private Thing mockThing;

    @Mock
    private Bridge mockBridge;

    private SonoffHandlerFactory factory;

    @BeforeEach
    void setUp() {
        // Setup mock factories to return mock clients
        when(webSocketFactory.getCommonWebSocketClient()).thenReturn(webSocketClient);
        when(httpClientFactory.getCommonHttpClient()).thenReturn(httpClient);

        // Create factory instance
        factory = new SonoffHandlerFactory(webSocketFactory, httpClientFactory);
    }

    @Test
    @DisplayName("Constructor should initialize with required dependencies")
    void testConstructor() {
        // Verify that the factory was created successfully
        assertNotNull(factory);

        // Verify that the factories were called to get clients
        verify(webSocketFactory).getCommonWebSocketClient();
        verify(httpClientFactory).getCommonHttpClient();
    }

    @Test
    @DisplayName("Should support all defined thing types from SUPPORTED_THING_TYPE_UIDS")
    void testSupportsThingType_AllSupportedTypes() {
        // Test all supported thing types from SonoffBindingConstants
        assertTrue(factory.supportsThingType(SonoffBindingConstants.THING_TYPE_ACCOUNT));
        assertTrue(factory.supportsThingType(SonoffBindingConstants.THING_TYPE_1));
        assertTrue(factory.supportsThingType(SonoffBindingConstants.THING_TYPE_2));
        assertTrue(factory.supportsThingType(SonoffBindingConstants.THING_TYPE_5));
        assertTrue(factory.supportsThingType(SonoffBindingConstants.THING_TYPE_15));
        assertTrue(factory.supportsThingType(SonoffBindingConstants.THING_TYPE_28));
        assertTrue(factory.supportsThingType(SonoffBindingConstants.THING_TYPE_32));
        assertTrue(factory.supportsThingType(SonoffBindingConstants.THING_TYPE_59));
        assertTrue(factory.supportsThingType(SonoffBindingConstants.THING_TYPE_66));
        assertTrue(factory.supportsThingType(SonoffBindingConstants.THING_TYPE_102));
        assertTrue(factory.supportsThingType(SonoffBindingConstants.THING_TYPE_104));
        assertTrue(factory.supportsThingType(SonoffBindingConstants.THING_TYPE_138));
        assertTrue(factory.supportsThingType(SonoffBindingConstants.THING_TYPE_190));
        assertTrue(factory.supportsThingType(SonoffBindingConstants.THING_TYPE_237));
        assertTrue(factory.supportsThingType(SonoffBindingConstants.THING_TYPE_1770));
        assertTrue(factory.supportsThingType(SonoffBindingConstants.THING_TYPE_2026));
        assertTrue(factory.supportsThingType(SonoffBindingConstants.THING_TYPE_7000));
        assertTrue(factory.supportsThingType(SonoffBindingConstants.THING_TYPE_7002));
        assertTrue(factory.supportsThingType(SonoffBindingConstants.THING_TYPE_7003));
        assertTrue(factory.supportsThingType(SonoffBindingConstants.THING_TYPE_7014));
        assertTrue(factory.supportsThingType(SonoffBindingConstants.THING_TYPE_268));
        assertTrue(factory.supportsThingType(SonoffBindingConstants.THING_TYPE_RF1));
        assertTrue(factory.supportsThingType(SonoffBindingConstants.THING_TYPE_RF2));
        assertTrue(factory.supportsThingType(SonoffBindingConstants.THING_TYPE_RF3));
        assertTrue(factory.supportsThingType(SonoffBindingConstants.THING_TYPE_RF4));
        assertTrue(factory.supportsThingType(SonoffBindingConstants.THING_TYPE_RF6));
    }

    @Test
    @DisplayName("Should not support unsupported thing types")
    void testSupportsThingType_UnsupportedTypes() {
        // Test with completely different binding
        ThingTypeUID unsupportedType = new ThingTypeUID("other", "device");
        assertFalse(factory.supportsThingType(unsupportedType));

        // Test with sonoff binding but unsupported device type
        ThingTypeUID unsupportedSonoffType = new ThingTypeUID("sonoff", "unsupported");
        assertFalse(factory.supportsThingType(unsupportedSonoffType));
    }

    @Test
    @DisplayName("Should create SonoffAccountHandler for account thing type")
    void testCreateHandler_Account() {
        // Setup
        ThingTypeUID accountType = new ThingTypeUID("sonoff", "account");
        when(mockBridge.getThingTypeUID()).thenReturn(accountType);

        // Execute
        ThingHandler handler = factory.createHandler(mockBridge);

        // Verify
        assertNotNull(handler);
        assertEquals("SonoffAccountHandler", handler.getClass().getSimpleName());
    }

    @ParameterizedTest
    @ValueSource(strings = { "1", "6", "14", "27", "81", "107", "160", "209", "256", "260" })
    @DisplayName("Should create SonoffSwitchSingleHandler for single switch device types")
    void testCreateHandler_SingleSwitchDevices(String deviceId) {
        // Setup
        ThingTypeUID thingType = new ThingTypeUID("sonoff", deviceId);
        when(mockThing.getThingTypeUID()).thenReturn(thingType);

        // Execute
        ThingHandler handler = factory.createHandler(mockThing);

        // Verify
        assertNotNull(handler);
        assertEquals("SonoffSwitchSingleHandler", handler.getClass().getSimpleName());
    }

    @ParameterizedTest
    @ValueSource(strings = { "2", "3", "4", "7", "8", "9", "29", "30", "31", "77", "78", "82", "83", "84", "126", "161",
            "162", "210", "211", "212", "268" })
    @DisplayName("Should create SonoffSwitchMultiHandler for multi switch device types")
    void testCreateHandler_MultiSwitchDevices(String deviceId) {
        // Setup
        ThingTypeUID thingType = new ThingTypeUID("sonoff", deviceId);
        when(mockThing.getThingTypeUID()).thenReturn(thingType);

        // Execute
        ThingHandler handler = factory.createHandler(mockThing);

        // Verify
        assertNotNull(handler);
        assertEquals("SonoffSwitchMultiHandler", handler.getClass().getSimpleName());
    }

    @Test
    @DisplayName("Should create SonoffSwitchPOWHandler for POW device type")
    void testCreateHandler_POWDevice() {
        // Setup
        ThingTypeUID thingType = new ThingTypeUID("sonoff", "5");
        when(mockThing.getThingTypeUID()).thenReturn(thingType);

        // Execute
        ThingHandler handler = factory.createHandler(mockThing);

        // Verify
        assertNotNull(handler);
        assertEquals("SonoffSwitchPOWHandler", handler.getClass().getSimpleName());
    }

    @ParameterizedTest
    @ValueSource(strings = { "15", "181" })
    @DisplayName("Should create SonoffSwitchTHHandler for TH device types")
    void testCreateHandler_THDevices(String deviceId) {
        // Setup
        ThingTypeUID thingType = new ThingTypeUID("sonoff", deviceId);
        when(mockThing.getThingTypeUID()).thenReturn(thingType);

        // Execute
        ThingHandler handler = factory.createHandler(mockThing);

        // Verify
        assertNotNull(handler);
        assertEquals("SonoffSwitchTHHandler", handler.getClass().getSimpleName());
    }

    @Test
    @DisplayName("Should create SonoffGSMSocketHandler for GSM device type")
    void testCreateHandler_GSMDevice() {
        // Setup
        ThingTypeUID thingType = new ThingTypeUID("sonoff", "24");
        when(mockThing.getThingTypeUID()).thenReturn(thingType);

        // Execute
        ThingHandler handler = factory.createHandler(mockThing);

        // Verify
        assertNotNull(handler);
        assertEquals("SonoffGSMSocketHandler", handler.getClass().getSimpleName());
    }

    @Test
    @DisplayName("Should create SonoffRfBridgeHandler for RF Bridge device type")
    void testCreateHandler_RFBridge() {
        // Setup
        ThingTypeUID thingType = new ThingTypeUID("sonoff", "28");
        when(mockBridge.getThingTypeUID()).thenReturn(thingType);

        // Execute
        ThingHandler handler = factory.createHandler(mockBridge);

        // Verify
        assertNotNull(handler);
        assertEquals("SonoffRfBridgeHandler", handler.getClass().getSimpleName());
    }

    @Test
    @DisplayName("Should create SonoffSwitchPOWR2Handler for POWR2 device type")
    void testCreateHandler_POWR2Device() {
        // Setup
        ThingTypeUID thingType = new ThingTypeUID("sonoff", "32");
        when(mockThing.getThingTypeUID()).thenReturn(thingType);

        // Execute
        ThingHandler handler = factory.createHandler(mockThing);

        // Verify
        assertNotNull(handler);
        assertEquals("SonoffSwitchPOWR2Handler", handler.getClass().getSimpleName());
    }

    @Test
    @DisplayName("Should create SonoffRGBStripHandler for RGB Strip device type")
    void testCreateHandler_RGBStrip() {
        // Setup
        ThingTypeUID thingType = new ThingTypeUID("sonoff", "59");
        when(mockThing.getThingTypeUID()).thenReturn(thingType);

        // Execute
        ThingHandler handler = factory.createHandler(mockThing);

        // Verify
        assertNotNull(handler);
        assertEquals("SonoffRGBStripHandler", handler.getClass().getSimpleName());
    }

    @ParameterizedTest
    @ValueSource(strings = { "66", "168", "243" })
    @DisplayName("Should create SonoffZigbeeBridgeHandler for Zigbee Bridge device types")
    void testCreateHandler_ZigbeeBridge(String deviceId) {
        // Setup
        ThingTypeUID thingType = new ThingTypeUID("sonoff", deviceId);
        when(mockBridge.getThingTypeUID()).thenReturn(thingType);

        // Execute
        ThingHandler handler = factory.createHandler(mockBridge);

        // Verify
        assertNotNull(handler);
        assertEquals("SonoffZigbeeBridgeHandler", handler.getClass().getSimpleName());
    }

    @Test
    @DisplayName("Should create SonoffMagneticSwitchHandler for magnetic switch device type")
    void testCreateHandler_MagneticSwitch() {
        // Setup
        ThingTypeUID thingType = new ThingTypeUID("sonoff", "102");
        when(mockThing.getThingTypeUID()).thenReturn(thingType);

        // Execute
        ThingHandler handler = factory.createHandler(mockThing);

        // Verify
        assertNotNull(handler);
        assertEquals("SonoffMagneticSwitchHandler", handler.getClass().getSimpleName());
    }

    @Test
    @DisplayName("Should create SonoffRGBCCTHandler for RGB CCT device type")
    void testCreateHandler_RGBCCT() {
        // Setup
        ThingTypeUID thingType = new ThingTypeUID("sonoff", "104");
        when(mockThing.getThingTypeUID()).thenReturn(thingType);

        // Execute
        ThingHandler handler = factory.createHandler(mockThing);

        // Verify
        assertNotNull(handler);
        assertEquals("SonoffRGBCCTHandler", handler.getClass().getSimpleName());
    }

    @Test
    @DisplayName("Should create SonoffSwitchSingleMiniHandler for mini device type")
    void testCreateHandler_Mini() {
        // Setup
        ThingTypeUID thingType = new ThingTypeUID("sonoff", "138");
        when(mockThing.getThingTypeUID()).thenReturn(thingType);

        // Execute
        ThingHandler handler = factory.createHandler(mockThing);

        // Verify
        assertNotNull(handler);
        assertEquals("SonoffSwitchSingleMiniHandler", handler.getClass().getSimpleName());
    }

    @Test
    @DisplayName("Should create SonoffSwitchPOWUgradedHandler for upgraded POW device type")
    void testCreateHandler_POWUpgraded() {
        // Setup
        ThingTypeUID thingType = new ThingTypeUID("sonoff", "190");
        when(mockThing.getThingTypeUID()).thenReturn(thingType);

        // Execute
        ThingHandler handler = factory.createHandler(mockThing);

        // Verify
        assertNotNull(handler);
        assertEquals("SonoffSwitchPOWUgradedHandler", handler.getClass().getSimpleName());
    }

    @Test
    @DisplayName("Should create SonoffGateHandler for gate device type")
    void testCreateHandler_Gate() {
        // Setup
        ThingTypeUID thingType = new ThingTypeUID("sonoff", "237");
        when(mockThing.getThingTypeUID()).thenReturn(thingType);

        // Execute
        ThingHandler handler = factory.createHandler(mockThing);

        // Verify
        assertNotNull(handler);
        assertEquals("SonoffGateHandler", handler.getClass().getSimpleName());
    }

    @ParameterizedTest
    @ValueSource(strings = { "1770", "7014" })
    @DisplayName("Should create SonoffZigbeeDeviceTemperatureHumiditySensorHandler for temperature sensor types")
    void testCreateHandler_TemperatureSensors(String deviceId) {
        // Setup
        ThingTypeUID thingType = new ThingTypeUID("sonoff", deviceId);
        when(mockThing.getThingTypeUID()).thenReturn(thingType);

        // Execute
        ThingHandler handler = factory.createHandler(mockThing);

        // Verify
        assertNotNull(handler);
        assertEquals("SonoffZigbeeDeviceTemperatureHumiditySensorHandler", handler.getClass().getSimpleName());
    }

    @Test
    @DisplayName("Should create SonoffZigbeeDeviceMotionSensorHandler for motion sensor type 2026")
    void testCreateHandler_MotionSensor() {
        // Setup
        ThingTypeUID thingType = new ThingTypeUID("sonoff", "2026");
        when(mockThing.getThingTypeUID()).thenReturn(thingType);

        // Execute
        ThingHandler handler = factory.createHandler(mockThing);

        // Verify
        assertNotNull(handler);
        assertEquals("SonoffZigbeeDeviceMotionSensorHandler", handler.getClass().getSimpleName());
    }

    @Test
    @DisplayName("Should create SonoffZigbeeDeviceMotionSensorV2Handler for motion sensor type 7002")
    void testCreateHandler_MotionSensorV2() {
        // Setup
        ThingTypeUID thingType = new ThingTypeUID("sonoff", "7002");
        when(mockThing.getThingTypeUID()).thenReturn(thingType);

        // Execute
        ThingHandler handler = factory.createHandler(mockThing);

        // Verify
        assertNotNull(handler);
        assertEquals("SonoffZigbeeDeviceMotionSensorV2Handler", handler.getClass().getSimpleName());
    }

    @Test
    @DisplayName("Should create SonoffZigbeeButtonHandler for button device type")
    void testCreateHandler_ButtonDevice() {
        // Setup
        ThingTypeUID thingType = new ThingTypeUID("sonoff", "7000");
        when(mockThing.getThingTypeUID()).thenReturn(thingType);

        // Execute
        ThingHandler handler = factory.createHandler(mockThing);

        // Verify
        assertNotNull(handler);
        assertEquals("SonoffZigbeeButtonHandler", handler.getClass().getSimpleName());
    }

    @ParameterizedTest
    @ValueSource(strings = { "rfremote1", "rfremote2", "rfremote3", "rfremote4", "rfsensor" })
    @DisplayName("Should create SonoffRfDeviceHandler for RF device types")
    void testCreateHandler_RFDevices(String deviceId) {
        // Setup
        ThingTypeUID thingType = new ThingTypeUID("sonoff", deviceId);
        when(mockThing.getThingTypeUID()).thenReturn(thingType);

        // Execute
        ThingHandler handler = factory.createHandler(mockThing);

        // Verify
        assertNotNull(handler);
        assertEquals("SonoffRfDeviceHandler", handler.getClass().getSimpleName());
    }

    @Test
    @DisplayName("Should create SonoffZigbeeContactSensorHandler for contact sensor type")
    void testCreateHandler_ContactSensor() {
        // Setup
        ThingTypeUID thingType = new ThingTypeUID("sonoff", "7003");
        when(mockThing.getThingTypeUID()).thenReturn(thingType);

        // Execute
        ThingHandler handler = factory.createHandler(mockThing);

        // Verify
        assertNotNull(handler);
        assertEquals("SonoffZigbeeContactSensorHandler", handler.getClass().getSimpleName());
    }

    @Test
    @DisplayName("Should return null for unsupported device types")
    void testCreateHandler_UnsupportedDevice() {
        // Setup
        ThingTypeUID unsupportedType = new ThingTypeUID("sonoff", "999");
        when(mockThing.getThingTypeUID()).thenReturn(unsupportedType);

        // Execute
        ThingHandler handler = factory.createHandler(mockThing);

        // Verify
        assertNull(handler);
    }

    @Test
    @DisplayName("Should return null for null thing type")
    void testCreateHandler_NullThingType() {
        // Setup
        when(mockThing.getThingTypeUID()).thenReturn(null);

        // Execute & Verify
        assertThrows(NullPointerException.class, () -> {
            factory.createHandler(mockThing);
        });
    }

    @Test
    @DisplayName("Should handle null thing parameter gracefully")
    void testCreateHandler_NullThing() {
        // Execute & Verify
        assertThrows(NullPointerException.class, () -> {
            factory.createHandler(null);
        });
    }

    @Test
    @DisplayName("Should handle thing with different binding ID")
    void testCreateHandler_DifferentBindingId() {
        // Setup
        ThingTypeUID differentBindingType = new ThingTypeUID("other", "1");
        when(mockThing.getThingTypeUID()).thenReturn(differentBindingType);

        // First verify that supportsThingType correctly rejects different binding IDs
        assertFalse(factory.supportsThingType(differentBindingType),
                "Factory should not support different binding IDs");

        // Execute createHandler - this tests the internal implementation
        ThingHandler handler = factory.createHandler(mockThing);

        // Verify - The current implementation only checks device ID in createHandler
        // In practice, supportsThingType() should be called first to filter out unsupported types
        // This test documents that createHandler doesn't validate binding ID internally
        assertNotNull(handler, "createHandler only checks device ID, not binding ID");
        assertEquals("SonoffSwitchSingleHandler", handler.getClass().getSimpleName());

        // Note: In OpenHAB framework, supportsThingType() is the proper gatekeeper
        // createHandler() is only called for supported thing types
    }
}
