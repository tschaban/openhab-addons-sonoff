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

import java.util.Set;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.net.http.WebSocketFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * Integration tests for {@link SonoffHandlerFactory}.
 * 
 * Tests cover:
 * - Integration scenarios with real ThingTypeUID objects
 * - Comprehensive coverage of all supported device types
 * - Edge cases and boundary conditions
 * - Factory behavior with various mock configurations
 *
 * @author tschaban/SmartnyDom - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
class SonoffHandlerFactoryIntegrationTest {

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
        when(webSocketFactory.getCommonWebSocketClient()).thenReturn(webSocketClient);
        when(httpClientFactory.getCommonHttpClient()).thenReturn(httpClient);
        factory = new SonoffHandlerFactory(webSocketFactory, httpClientFactory);
    }

    @Test
    @DisplayName("Should support all thing types defined in SUPPORTED_THING_TYPE_UIDS")
    void testSupportsAllDefinedThingTypes() {
        Set<ThingTypeUID> supportedTypes = SonoffBindingConstants.SUPPORTED_THING_TYPE_UIDS;

        // Verify that the factory supports all defined thing types
        for (ThingTypeUID thingType : supportedTypes) {
            assertTrue(factory.supportsThingType(thingType), "Factory should support thing type: " + thingType.getId());
        }

        // Verify we have a reasonable number of supported types
        assertTrue(supportedTypes.size() > 30, "Should support more than 30 device types");
    }

    @Test
    @DisplayName("Should create handlers for all supported bridge types")
    void testCreateHandlersForBridgeTypes() {
        // Test account bridge
        testBridgeHandlerCreation("account", "SonoffAccountHandler");

        // Test RF bridge
        testBridgeHandlerCreation("28", "SonoffRfBridgeHandler");

        // Test Zigbee bridges
        testBridgeHandlerCreation("66", "SonoffZigbeeBridgeHandler");
        testBridgeHandlerCreation("168", "SonoffZigbeeBridgeHandler");
        testBridgeHandlerCreation("243", "SonoffZigbeeBridgeHandler");
    }

    private void testBridgeHandlerCreation(String deviceId, String expectedHandlerClass) {
        ThingTypeUID thingType = new ThingTypeUID("sonoff", deviceId);
        when(mockBridge.getThingTypeUID()).thenReturn(thingType);

        ThingHandler handler = factory.createHandler(mockBridge);

        assertNotNull(handler, "Handler should be created for bridge type: " + deviceId);
        assertEquals(expectedHandlerClass, handler.getClass().getSimpleName(),
                "Wrong handler type for bridge: " + deviceId);
    }

    @Test
    @DisplayName("Should handle factory dependency injection correctly")
    void testFactoryDependencyInjection() {
        // Verify that dependencies are properly injected
        verify(webSocketFactory, times(1)).getCommonWebSocketClient();
        verify(httpClientFactory, times(1)).getCommonHttpClient();

        // Test that factory can be created multiple times
        SonoffHandlerFactory secondFactory = new SonoffHandlerFactory(webSocketFactory, httpClientFactory);
        assertNotNull(secondFactory);

        // Verify dependencies are called again
        verify(webSocketFactory, times(2)).getCommonWebSocketClient();
        verify(httpClientFactory, times(2)).getCommonHttpClient();
    }

    @Test
    @DisplayName("Should handle null dependencies gracefully")
    void testNullDependencies() {
        // Test with null WebSocketFactory
        assertThrows(NullPointerException.class, () -> {
            new SonoffHandlerFactory(null, httpClientFactory);
        });

        // Test with null HttpClientFactory
        assertThrows(NullPointerException.class, () -> {
            new SonoffHandlerFactory(webSocketFactory, null);
        });

        // Test with both null
        assertThrows(NullPointerException.class, () -> {
            new SonoffHandlerFactory(null, null);
        });
    }

    @Test
    @DisplayName("Should handle factory methods returning null clients")
    void testNullClients() {
        // Setup factories to return null
        when(webSocketFactory.getCommonWebSocketClient()).thenReturn(null);
        when(httpClientFactory.getCommonHttpClient()).thenReturn(null);

        // Factory creation should still work (null handling is up to handlers)
        SonoffHandlerFactory factoryWithNullClients = new SonoffHandlerFactory(webSocketFactory, httpClientFactory);
        assertNotNull(factoryWithNullClients);

        // Test that it can still create non-bridge handlers
        ThingTypeUID thingType = new ThingTypeUID("sonoff", "1");
        when(mockThing.getThingTypeUID()).thenReturn(thingType);

        ThingHandler handler = factoryWithNullClients.createHandler(mockThing);
        assertNotNull(handler);
    }

    @Test
    @DisplayName("Should create correct handler types for device categories")
    void testHandlerCreationByCategory() {
        // Test single switch devices
        String[] singleSwitchIds = { "1", "6", "14", "27", "81", "107", "160", "209", "256", "260" };
        for (String id : singleSwitchIds) {
            testDeviceHandlerCreation(id, "SonoffSwitchSingleHandler");
        }

        // Test multi switch devices
        String[] multiSwitchIds = { "2", "3", "4", "7", "8", "9", "29", "30", "31", "77", "78", "82", "83", "84", "126",
                "161", "162", "210", "211", "212", "268" };
        for (String id : multiSwitchIds) {
            testDeviceHandlerCreation(id, "SonoffSwitchMultiHandler");
        }

        // Test sensor devices
        String[] sensorIds = { "1770", "7014" };
        for (String id : sensorIds) {
            testDeviceHandlerCreation(id, "SonoffZigbeeDeviceTemperatureHumiditySensorHandler");
        }

        // Test motion sensor devices
        testDeviceHandlerCreation("2026", "SonoffZigbeeDeviceMotionSensorHandler");
        testDeviceHandlerCreation("7002", "SonoffZigbeeDeviceMotionSensorV2Handler");

        // Test button devices
        testDeviceHandlerCreation("7000", "SonoffZigbeeButtonHandler");

        // Test RF devices
        String[] rfIds = { "rfremote1", "rfremote2", "rfremote3", "rfremote4", "rfsensor" };
        for (String id : rfIds) {
            testDeviceHandlerCreation(id, "SonoffRfDeviceHandler");
        }
    }

    private void testDeviceHandlerCreation(String deviceId, String expectedHandlerClass) {
        ThingTypeUID thingType = new ThingTypeUID("sonoff", deviceId);
        when(mockThing.getThingTypeUID()).thenReturn(thingType);

        ThingHandler handler = factory.createHandler(mockThing);

        assertNotNull(handler, "Handler should be created for device type: " + deviceId);
        assertEquals(expectedHandlerClass, handler.getClass().getSimpleName(),
                "Wrong handler type for device: " + deviceId);
    }

    @Test
    @DisplayName("Should handle case sensitivity in device IDs")
    void testCaseSensitivity() {
        // Test that device IDs are case sensitive
        ThingTypeUID upperCaseType = new ThingTypeUID("sonoff", "ACCOUNT");
        when(mockThing.getThingTypeUID()).thenReturn(upperCaseType);

        ThingHandler handler = factory.createHandler(mockThing);
        assertNull(handler, "Should not create handler for uppercase device ID");

        // Test mixed case
        ThingTypeUID mixedCaseType = new ThingTypeUID("sonoff", "Account");
        when(mockThing.getThingTypeUID()).thenReturn(mixedCaseType);

        handler = factory.createHandler(mockThing);
        assertNull(handler, "Should not create handler for mixed case device ID");
    }

    @Test
    @DisplayName("Should handle special characters in device IDs")
    void testSpecialCharacters() {
        // Test device IDs with valid characters but non-existent device types
        // Note: ThingTypeUID validates format, so we test valid formats that don't exist
        String[] nonExistentIds = { "1a", "a1", "999", "unknown", "test-device" };

        for (String nonExistentId : nonExistentIds) {
            try {
                ThingTypeUID thingType = new ThingTypeUID("sonoff", nonExistentId);
                when(mockThing.getThingTypeUID()).thenReturn(thingType);

                ThingHandler handler = factory.createHandler(mockThing);
                assertNull(handler, "Should not create handler for non-existent device ID: " + nonExistentId);
            } catch (IllegalArgumentException e) {
                // Some IDs may be invalid according to ThingTypeUID validation rules
                // This is expected behavior - the validation happens before our factory
                assertTrue(true, "ThingTypeUID validation rejected invalid ID: " + nonExistentId);
            }
        }
    }

    @Test
    @DisplayName("Should handle invalid device IDs through ThingTypeUID validation")
    void testInvalidDeviceIds() {
        // Test that ThingTypeUID validation prevents invalid IDs from reaching our factory

        // Test empty string - should throw IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            new ThingTypeUID("sonoff", "");
        }, "ThingTypeUID should reject empty device ID");

        // Test whitespace - should throw IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            new ThingTypeUID("sonoff", " ");
        }, "ThingTypeUID should reject whitespace device ID");

        // Test invalid characters - should throw IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            new ThingTypeUID("sonoff", "1.");
        }, "ThingTypeUID should reject device ID with invalid characters");

        // Test that our factory handles valid but non-existent device IDs
        ThingTypeUID nonExistentType = new ThingTypeUID("sonoff", "999");
        when(mockThing.getThingTypeUID()).thenReturn(nonExistentType);

        ThingHandler handler = factory.createHandler(mockThing);
        assertNull(handler, "Should not create handler for non-existent device ID");
    }

    @Test
    @DisplayName("Should maintain consistent behavior across multiple calls")
    void testConsistentBehavior() {
        ThingTypeUID thingType = new ThingTypeUID("sonoff", "1");
        when(mockThing.getThingTypeUID()).thenReturn(thingType);

        // Create multiple handlers and verify they're all the same type
        for (int i = 0; i < 5; i++) {
            ThingHandler handler = factory.createHandler(mockThing);
            assertNotNull(handler);
            assertEquals("SonoffSwitchSingleHandler", handler.getClass().getSimpleName());
        }

        // Test with unsupported type multiple times
        ThingTypeUID unsupportedType = new ThingTypeUID("sonoff", "999");
        when(mockThing.getThingTypeUID()).thenReturn(unsupportedType);

        for (int i = 0; i < 5; i++) {
            ThingHandler handler = factory.createHandler(mockThing);
            assertNull(handler);
        }
    }
}
