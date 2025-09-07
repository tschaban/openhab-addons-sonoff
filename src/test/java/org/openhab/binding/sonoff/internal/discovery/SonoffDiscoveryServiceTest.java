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
package org.openhab.binding.sonoff.internal.discovery;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.sonoff.internal.SonoffBindingConstants;
import org.openhab.binding.sonoff.internal.connection.SonoffApiConnection;
import org.openhab.binding.sonoff.internal.connection.SonoffConnectionManager;
import org.openhab.binding.sonoff.internal.handler.SonoffAccountHandler;
import org.openhab.binding.sonoff.internal.handler.SonoffRfBridgeHandler;
import org.openhab.binding.sonoff.internal.handler.SonoffZigbeeBridgeHandler;
import org.openhab.core.config.discovery.DiscoveryListener;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;

import com.google.gson.JsonObject;

/**
 * Unit tests for {@link SonoffDiscoveryService}
 *
 * @author Ona - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
class SonoffDiscoveryServiceTest {

    @Mock
    private SonoffAccountHandler mockAccountHandler;

    @Mock
    private SonoffConnectionManager mockConnectionManager;

    @Mock
    private SonoffApiConnection mockApiConnection;

    @Mock
    private Bridge mockAccountThing;

    @Mock
    private Bridge mockRfBridge;

    @Mock
    private Bridge mockZigbeeBridge;

    @Mock
    private SonoffRfBridgeHandler mockRfBridgeHandler;

    @Mock
    private SonoffZigbeeBridgeHandler mockZigbeeBridgeHandler;

    @Mock
    private ScheduledExecutorService mockScheduler;

    @Mock
    private ScheduledFuture<Object> mockScheduledFuture;

    @Mock
    private DiscoveryListener mockDiscoveryListener;

    private SonoffDiscoveryService discoveryService;
    private ThingUID accountThingUID;
    private List<DiscoveryResult> discoveredResults;

    @BeforeEach
    void setUp() {
        discoveryService = new SonoffDiscoveryService();
        discoveredResults = new ArrayList<>();

        // Setup account thing UID
        accountThingUID = new ThingUID(SonoffBindingConstants.THING_TYPE_ACCOUNT, "test-account");

        // Setup mock account handler
        lenient().when(mockAccountHandler.getThing()).thenReturn(mockAccountThing);
        lenient().when(mockAccountThing.getUID()).thenReturn(accountThingUID);
        lenient().when(mockAccountThing.getThings()).thenReturn(new ArrayList<>());
        lenient().when(mockAccountHandler.getConnectionManager()).thenReturn(mockConnectionManager);

        // Setup mock connection manager
        lenient().when(mockConnectionManager.getApi()).thenReturn(mockApiConnection);
        lenient().when(mockConnectionManager.getMode()).thenReturn("cloud");

        // Setup mock API connection
        lenient().when(mockApiConnection.createCache()).thenReturn(createSimpleApiResponse());

        // Setup mock scheduler
        lenient().when(mockScheduler.schedule(any(Runnable.class), anyLong(), any(TimeUnit.class)))
                .thenReturn((ScheduledFuture<Object>) mockScheduledFuture);

        // Setup addState method
        lenient().doNothing().when(mockAccountHandler).addState(anyString());

        // Set the account handler
        discoveryService.setThingHandler(mockAccountHandler);

        // Add discovery listener to capture results
        doAnswer(invocation -> {
            DiscoveryResult result = invocation.getArgument(1);
            discoveredResults.add(result);
            return null;
        }).when(mockDiscoveryListener).thingDiscovered(any(), any());

        // Try to set up scheduler mock using reflection
        try {
            java.lang.reflect.Field schedulerField = discoveryService.getClass().getSuperclass()
                    .getDeclaredField("scheduler");
            schedulerField.setAccessible(true);
            schedulerField.set(discoveryService, mockScheduler);
        } catch (Exception e) {
            // Fallback - tests may still work without scheduler mock
        }

        // Add discovery listener
        discoveryService.addDiscoveryListener(mockDiscoveryListener);
    }

    @Test
    @DisplayName("Should initialize with correct supported thing types")
    void testInitialization() {
        // Verify the discovery service is properly initialized
        assertNotNull(discoveryService);
        assertEquals(SonoffBindingConstants.DISCOVERABLE_THING_TYPE_UIDS, discoveryService.getSupportedThingTypes());
    }

    @Test
    @DisplayName("Should set and get thing handler correctly")
    void testThingHandlerManagement() {
        // Test setting a valid SonoffAccountHandler
        SonoffAccountHandler handler = mock(SonoffAccountHandler.class);
        discoveryService.setThingHandler(handler);
        assertEquals(handler, discoveryService.getThingHandler());

        // Test setting null handler
        discoveryService.setThingHandler(null);
        assertNull(discoveryService.getThingHandler());

        // Test setting invalid handler type
        ThingHandler invalidHandler = mock(ThingHandler.class);
        discoveryService.setThingHandler(invalidHandler);
        assertNull(discoveryService.getThingHandler());
    }

    @Test
    @DisplayName("Should handle activate and deactivate methods")
    void testLifecycleMethods() {
        // Test activate with null config
        assertDoesNotThrow(() -> discoveryService.activate(null));

        // Test activate with empty config
        Map<String, Object> config = new HashMap<>();
        assertDoesNotThrow(() -> discoveryService.activate(config));

        // Test deactivate
        assertDoesNotThrow(() -> discoveryService.deactivate());
    }

    // Helper method to create a simple API response
    private String createSimpleApiResponse() {
        return "{" + "\"data\": {" + "\"thingList\": [" + createDeviceJson("device1", "Test Device", 1) + "]" + "}"
                + "}";
    }

    // Helper method to create device JSON
    private String createDeviceJson(String deviceId, String name, int uiid) {
        return "{" + "\"itemType\": 1," + "\"itemData\": {" + "\"deviceid\": \"" + deviceId + "\"," + "\"name\": \""
                + name + "\"," + "\"brandName\": \"Sonoff\"," + "\"productModel\": \"Test Model\","
                + "\"devicekey\": \"test-key\"," + "\"apikey\": \"test-api-key\"," + "\"extra\": {" + "\"uiid\": "
                + uiid + "}," + "\"params\": {" + "\"fwVersion\": \"1.0.0\"," + "\"ssid\": \"TestWiFi\"" + "}" + "}"
                + "}";
    }

    // Helper method to create empty API response
    private String createEmptyApiResponse() {
        return "{\"data\": {\"thingList\": []}}";
    }

    // Helper method to create malformed API response
    private String createMalformedApiResponse() {
        return "{\"invalid\": \"json\"}";
    }

    @Test
    @DisplayName("Should create cache with valid API response")
    void testCreateCacheWithValidResponse() throws Exception {
        // Setup
        when(mockApiConnection.createCache()).thenReturn(createSimpleApiResponse());

        List<Thing> things = new ArrayList<>();

        // Execute
        List<JsonObject> devices = discoveryService.createCache(things);

        // Verify
        assertNotNull(devices);
        assertEquals(1, devices.size());

        JsonObject device = devices.get(0);
        assertEquals("device1", device.get("deviceid").getAsString());
        assertEquals("Test Device", device.get("name").getAsString());

        // Verify account handler was called
        verify(mockAccountHandler).addState("device1");
    }

    @Test
    @DisplayName("Should handle empty API response")
    void testCreateCacheWithEmptyResponse() throws Exception {
        // Setup
        when(mockApiConnection.createCache()).thenReturn(createEmptyApiResponse());

        List<Thing> things = new ArrayList<>();

        // Execute
        List<JsonObject> devices = discoveryService.createCache(things);

        // Verify
        assertNotNull(devices);
        assertTrue(devices.isEmpty());
    }

    @Test
    @DisplayName("Should handle malformed API response")
    void testCreateCacheWithMalformedResponse() throws Exception {
        // Setup
        when(mockApiConnection.createCache()).thenReturn(createMalformedApiResponse());

        List<Thing> things = new ArrayList<>();

        // Execute
        List<JsonObject> devices = discoveryService.createCache(things);

        // Verify
        assertNotNull(devices);
        assertTrue(devices.isEmpty());
    }

    @Test
    @DisplayName("Should handle API connection exception")
    void testCreateCacheWithApiException() throws Exception {
        // Setup
        when(mockApiConnection.createCache()).thenThrow(new RuntimeException("API Error"));

        List<Thing> things = new ArrayList<>();

        // Execute
        List<JsonObject> devices = discoveryService.createCache(things);

        // Verify
        assertNotNull(devices);
        assertTrue(devices.isEmpty());
    }

    @Test
    @DisplayName("Should handle local mode with login")
    void testCreateCacheInLocalMode() throws Exception {
        // Setup
        when(mockConnectionManager.getMode()).thenReturn("local");
        when(mockApiConnection.createCache()).thenReturn(createSimpleApiResponse());

        List<Thing> things = new ArrayList<>();

        // Execute
        List<JsonObject> devices = discoveryService.createCache(things);

        // Verify
        verify(mockApiConnection).login();
        assertNotNull(devices);
        assertEquals(1, devices.size());
    }

    @Test
    @DisplayName("Should handle scan task lifecycle correctly")
    void testScanTaskLifecycle() throws Exception {
        // Test startScan
        discoveryService.startScan();

        // Verify scheduler was called
        verify(mockScheduler).schedule(any(Runnable.class), eq(0L), eq(TimeUnit.SECONDS));

        // Test stopScan
        discoveryService.stopScan();

        // Verify task cancellation
        verify(mockScheduledFuture).cancel(true);
    }

    @Test
    @DisplayName("Should handle null account handler gracefully")
    void testNullAccountHandler() throws Exception {
        // Set null account handler
        discoveryService.setThingHandler(null);

        // Try to start scan - should not throw exception
        assertDoesNotThrow(() -> discoveryService.startScan());
    }
}
