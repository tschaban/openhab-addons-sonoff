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
import static org.mockito.Mockito.lenient;

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
import org.mockito.ArgumentCaptor;
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
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Unit tests for {@link SonoffDiscoveryService}.
 * 
 * Tests cover:
 * - Service initialization and lifecycle
 * - Discovery scan operations
 * - Device cache creation
 * - Thing discovery for different device types
 * - RF and Zigbee sub-device discovery
 * - Error handling and edge cases
 *
 * @author Test Author - Initial contribution
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
    private Thing mockChildThing;

    @Mock
    private SonoffRfBridgeHandler mockRfBridgeHandler;

    @Mock
    private SonoffZigbeeBridgeHandler mockZigbeeBridgeHandler;

    @Mock
    private ScheduledExecutorService mockScheduler;

    @Mock
    @SuppressWarnings("rawtypes")
    private ScheduledFuture mockScheduledFuture;

    @Mock
    private DiscoveryListener mockDiscoveryListener;

    private SonoffDiscoveryService discoveryService;
    private ThingUID accountThingUID;
    private List<Thing> childThings;

    @BeforeEach
    void setUp() {
        discoveryService = new SonoffDiscoveryService();

        // Setup account thing UID
        accountThingUID = new ThingUID(SonoffBindingConstants.THING_TYPE_ACCOUNT, "test-account");

        // Setup child things list
        childThings = new ArrayList<>();

        // Setup mock account handler (lenient to avoid unnecessary stubbing warnings)
        lenient().when(mockAccountHandler.getThing()).thenReturn(mockAccountThing);
        lenient().when(mockAccountThing.getUID()).thenReturn(accountThingUID);
        lenient().when(mockAccountThing.getThings()).thenReturn(childThings);
        lenient().when(mockAccountHandler.getConnectionManager()).thenReturn(mockConnectionManager);

        // Setup mock connection manager (lenient to avoid unnecessary stubbing warnings)
        lenient().when(mockConnectionManager.getApi()).thenReturn(mockApiConnection);
        lenient().when(mockConnectionManager.getMode()).thenReturn("cloud");

        // Setup addState method (lenient to avoid unnecessary stubbing warnings)
        lenient().doNothing().when(mockAccountHandler).addState(anyString());

        // Set the account handler
        discoveryService.setThingHandler(mockAccountHandler);

        // Setup scheduler mock (lenient to avoid unnecessary stubbing warnings)
        lenient().doReturn(mockScheduledFuture).when(mockScheduler).schedule(any(Runnable.class), anyLong(),
                any(TimeUnit.class));

        // Use reflection to set the scheduler
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
        // Verify
        assertEquals(SonoffBindingConstants.DISCOVERABLE_THING_TYPE_UIDS, discoveryService.getSupportedThingTypes(),
                "Should support all discoverable thing types");

        assertEquals(10, discoveryService.getScanTimeout(), "Should have 10 second scan timeout");

        assertFalse(discoveryService.isBackgroundDiscoveryEnabled(),
                "Background discovery should be disabled by default");
    }

    @Test
    @DisplayName("Should set and get thing handler correctly")
    void testThingHandlerManagement() {
        // Test setting account handler
        SonoffAccountHandler newHandler = mock(SonoffAccountHandler.class);
        discoveryService.setThingHandler(newHandler);
        assertEquals(newHandler, discoveryService.getThingHandler(), "Should return the set account handler");

        // Test setting non-account handler
        ThingHandler otherHandler = mock(ThingHandler.class);
        discoveryService.setThingHandler(otherHandler);
        assertEquals(newHandler, discoveryService.getThingHandler(),
                "Should not change handler when non-account handler is set");

        // Test setting null handler
        discoveryService.setThingHandler(null);
        assertEquals(newHandler, discoveryService.getThingHandler(), "Should not change handler when null is set");
    }

    @Test
    @DisplayName("Should start scan and schedule discovery task")
    void testStartScan() {
        // Execute
        discoveryService.startScan();

        // Verify
        verify(mockScheduler).schedule(any(Runnable.class), eq(0L), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Should stop scan and cancel scheduled task")
    void testStopScan() {
        // Setup - start a scan first
        discoveryService.startScan();

        // Execute
        discoveryService.stopScan();

        // Verify
        verify(mockScheduledFuture).cancel(true);
    }

    @Test
    @DisplayName("Should cancel existing scan when starting new scan")
    void testStartScanCancelsExisting() {
        // Setup - start first scan
        discoveryService.startScan();

        // Execute - start second scan
        discoveryService.startScan();

        // Verify - first scan should be cancelled
        verify(mockScheduledFuture).cancel(true);
        verify(mockScheduler, times(2)).schedule(any(Runnable.class), eq(0L), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Should handle null account handler gracefully")
    void testDiscoveryWithNullAccount() {
        // Setup
        discoveryService.setThingHandler(null);

        // Execute
        List<JsonObject> devices = discoveryService.createCache(new ArrayList<>());

        // Verify
        assertTrue(devices.isEmpty(), "Should return empty list when account handler is null");
    }

    @Test
    @DisplayName("Should create cache and discover devices successfully")
    void testCreateCacheSuccess() throws Exception {
        // Setup
        String mockApiResponse = createMockApiResponse();
        when(mockApiConnection.createCache()).thenReturn(mockApiResponse);

        List<Thing> things = new ArrayList<>();

        // Execute
        List<JsonObject> devices = discoveryService.createCache(things);

        // Verify
        assertFalse(devices.isEmpty(), "Should discover devices");
        assertEquals(2, devices.size(), "Should discover 2 devices");

        // Verify API was called
        verify(mockApiConnection).createCache();
        verify(mockAccountHandler, times(2)).addState(anyString());
    }

    @Test
    @DisplayName("Should handle API connection errors gracefully")
    void testCreateCacheApiError() throws Exception {
        // Setup
        when(mockApiConnection.createCache()).thenThrow(new RuntimeException("API Error"));

        List<Thing> things = new ArrayList<>();

        // Execute
        List<JsonObject> devices = discoveryService.createCache(things);

        // Verify
        assertTrue(devices.isEmpty(), "Should return empty list when API fails");
        verify(mockApiConnection).createCache();
    }

    @Test
    @DisplayName("Should login when in local mode")
    void testCreateCacheLocalMode() throws Exception {
        // Setup
        when(mockConnectionManager.getMode()).thenReturn("local");
        when(mockApiConnection.createCache()).thenReturn(createMockApiResponse());

        List<Thing> things = new ArrayList<>();

        // Execute
        discoveryService.createCache(things);

        // Verify
        verify(mockApiConnection).login();
        verify(mockApiConnection).createCache();
    }

    @Test
    @DisplayName("Should not login when in cloud mode")
    void testCreateCacheCloudMode() throws Exception {
        // Setup
        when(mockConnectionManager.getMode()).thenReturn("cloud");
        when(mockApiConnection.createCache()).thenReturn(createMockApiResponse());

        List<Thing> things = new ArrayList<>();

        // Execute
        discoveryService.createCache(things);

        // Verify
        verify(mockApiConnection, never()).login();
        verify(mockApiConnection).createCache();
    }

    @Test
    @DisplayName("Should reinitialize existing things when device found")
    void testReinitializeExistingThings() throws Exception {
        // Setup
        String deviceId = "device123";
        Thing existingThing = mock(Thing.class);
        ThingHandler existingHandler = mock(ThingHandler.class);

        when(existingThing.getConfiguration())
                .thenReturn(new org.openhab.core.config.core.Configuration(Map.of("deviceid", deviceId)));
        when(existingThing.getHandler()).thenReturn(existingHandler);

        List<Thing> things = List.of(existingThing);

        String mockApiResponse = createMockApiResponseWithDevice(deviceId);
        when(mockApiConnection.createCache()).thenReturn(mockApiResponse);

        // Execute
        discoveryService.createCache(things);

        // Verify
        verify(existingHandler).thingUpdated(existingThing);
    }

    @Test
    @DisplayName("Should handle malformed JSON response gracefully")
    void testCreateCacheMalformedJson() throws Exception {
        // Setup
        when(mockApiConnection.createCache()).thenReturn("invalid json");

        List<Thing> things = new ArrayList<>();

        // Execute
        List<JsonObject> devices = discoveryService.createCache(things);

        // Verify
        assertTrue(devices.isEmpty(), "Should return empty list for malformed JSON");
    }

    @Test
    @DisplayName("Should handle empty API response gracefully")
    void testCreateCacheEmptyResponse() throws Exception {
        // Setup
        when(mockApiConnection.createCache()).thenReturn("");

        List<Thing> things = new ArrayList<>();

        // Execute
        List<JsonObject> devices = discoveryService.createCache(things);

        // Verify
        assertTrue(devices.isEmpty(), "Should return empty list for empty response");
    }

    @Test
    @DisplayName("Should discover RF bridge sub-devices")
    void testDiscoverRfSubDevices() {
        // Setup RF bridge
        setupRfBridge();

        // Execute discovery
        runDiscoveryWithMockData();

        // Verify RF sub-devices were discovered
        ArgumentCaptor<DiscoveryResult> resultCaptor = ArgumentCaptor.forClass(DiscoveryResult.class);
        verify(mockDiscoveryListener, atLeastOnce()).thingDiscovered(eq(discoveryService), resultCaptor.capture());

        List<DiscoveryResult> results = resultCaptor.getAllValues();
        boolean foundRfDevice = results.stream()
                .anyMatch(result -> result.getThingTypeUID().getId().equals("rfremote1"));

        assertTrue(foundRfDevice, "Should discover RF sub-devices");
    }

    @Test
    @DisplayName("Should discover Zigbee bridge sub-devices")
    void testDiscoverZigbeeSubDevices() {
        // Setup Zigbee bridge
        setupZigbeeBridge();

        // Execute discovery with Zigbee device in API response
        try {
            when(mockApiConnection.createCache()).thenReturn(createMockApiResponseWithZigbeeDevice());

            // Use reflection to call the private discover method
            java.lang.reflect.Method discoverMethod = discoveryService.getClass().getDeclaredMethod("discover");
            discoverMethod.setAccessible(true);
            discoverMethod.invoke(discoveryService);
        } catch (Exception e) {
            fail("Failed to run discovery: " + e.getMessage());
        }

        // Verify Zigbee sub-devices were discovered
        ArgumentCaptor<DiscoveryResult> resultCaptor = ArgumentCaptor.forClass(DiscoveryResult.class);
        verify(mockDiscoveryListener, atLeastOnce()).thingDiscovered(eq(discoveryService), resultCaptor.capture());

        List<DiscoveryResult> results = resultCaptor.getAllValues();
        boolean foundZigbeeDevice = results.stream()
                .anyMatch(result -> result.getThingTypeUID().getId().equals("zswitch1"));

        assertTrue(foundZigbeeDevice, "Should discover Zigbee sub-devices");
    }

    @Test
    @DisplayName("Should handle unsupported device types gracefully")
    void testUnsupportedDeviceTypes() throws Exception {
        // Setup
        String mockApiResponse = createMockApiResponseWithUnsupportedDevice();
        when(mockApiConnection.createCache()).thenReturn(mockApiResponse);

        List<Thing> things = new ArrayList<>();

        // Execute
        List<JsonObject> devices = discoveryService.createCache(things);

        // Verify
        assertFalse(devices.isEmpty(), "Should still return devices even if some are unsupported");

        // Run discovery to test unsupported device handling
        runDiscoveryWithMockData();

        // Should not crash and should log error for unsupported device
    }

    @Test
    @DisplayName("Should activate and deactivate without errors")
    void testActivateDeactivate() {
        // Execute
        assertDoesNotThrow(() -> {
            discoveryService.activate(new HashMap<>());
            discoveryService.deactivate();
        }, "Should activate and deactivate without errors");
    }

    // Helper methods

    private String createMockApiResponse() {
        return "{" + "\"data\": {" + "\"thingList\": [" + "{" + "\"itemType\": 1," + "\"itemData\": {"
                + "\"deviceid\": \"device123\"," + "\"name\": \"Test Device 1\"," + "\"brandName\": \"Sonoff\","
                + "\"productModel\": \"BASIC\"," + "\"devicekey\": \"key123\"," + "\"apikey\": \"api123\","
                + "\"extra\": {\"uiid\": 1}," + "\"params\": {\"fwVersion\": \"1.0.0\", \"ssid\": \"TestWiFi\"}" + "}"
                + "}," + "{" + "\"itemType\": 1," + "\"itemData\": {" + "\"deviceid\": \"device456\","
                + "\"name\": \"Test Device 2\"," + "\"brandName\": \"Sonoff\"," + "\"productModel\": \"S20\","
                + "\"devicekey\": \"key456\"," + "\"apikey\": \"api456\"," + "\"extra\": {\"uiid\": 1},"
                + "\"params\": {\"fwVersion\": \"1.1.0\"}" + "}" + "}" + "]" + "}" + "}";
    }

    private String createMockApiResponseWithDevice(String deviceId) {
        return "{" + "\"data\": {" + "\"thingList\": [" + "{" + "\"itemType\": 1," + "\"itemData\": {"
                + "\"deviceid\": \"" + deviceId + "\"," + "\"name\": \"Test Device\"," + "\"brandName\": \"Sonoff\","
                + "\"productModel\": \"BASIC\"," + "\"devicekey\": \"key123\"," + "\"apikey\": \"api123\","
                + "\"extra\": {\"uiid\": 1}," + "\"params\": {\"fwVersion\": \"1.0.0\"}" + "}" + "}" + "]" + "}" + "}";
    }

    private String createMockApiResponseWithUnsupportedDevice() {
        return "{" + "\"data\": {" + "\"thingList\": [" + "{" + "\"itemType\": 1," + "\"itemData\": {"
                + "\"deviceid\": \"unsupported999\"," + "\"name\": \"Unsupported Device\","
                + "\"brandName\": \"Unknown\"," + "\"productModel\": \"UNKNOWN\"," + "\"devicekey\": \"key999\","
                + "\"apikey\": \"api999\"," + "\"extra\": {\"uiid\": 999}," + "\"params\": {\"fwVersion\": \"1.0.0\"}"
                + "}" + "}" + "]" + "}" + "}";
    }

    private String createMockApiResponseWithZigbeeDevice() {
        return "{" + "\"data\": {" + "\"thingList\": [" + "{" + "\"itemType\": 1," + "\"itemData\": {"
                + "\"deviceid\": \"device123\"," + "\"name\": \"Test Device 1\"," + "\"brandName\": \"Sonoff\","
                + "\"productModel\": \"BASIC\"," + "\"devicekey\": \"key123\"," + "\"apikey\": \"api123\","
                + "\"extra\": {\"uiid\": 1}," + "\"params\": {\"fwVersion\": \"1.0.0\", \"ssid\": \"TestWiFi\"}" + "}"
                + "}," + "{" + "\"itemType\": 1," + "\"itemData\": {" + "\"deviceid\": \"zigbee123\","
                + "\"name\": \"Zigbee Device\"," + "\"brandName\": \"Sonoff\"," + "\"productModel\": \"ZIGBEE\","
                + "\"devicekey\": \"zigbeekey\"," + "\"apikey\": \"zigbeeapi\"," + "\"extra\": {\"uiid\": 1000},"
                + "\"params\": {\"fwVersion\": \"1.0.0\"}" + "}" + "}" + "]" + "}" + "}";
    }

    private void setupRfBridge() {
        // Create RF bridge thing
        Bridge rfBridgeThing = mock(Bridge.class);
        ThingTypeUID rfBridgeTypeUID = new ThingTypeUID(SonoffBindingConstants.BINDING_ID, "28");
        when(rfBridgeThing.getThingTypeUID()).thenReturn(rfBridgeTypeUID);
        when(rfBridgeThing.getHandler()).thenReturn(mockRfBridgeHandler);
        when(rfBridgeThing.getUID()).thenReturn(new ThingUID(rfBridgeTypeUID, accountThingUID, "rfbridge"));

        // Setup RF sub-devices
        JsonArray rfSubDevices = new JsonArray();
        JsonObject rfDevice = new JsonObject();
        rfDevice.addProperty("name", "RF Sensor 1");
        rfDevice.addProperty("remote_type", "4");  // Use type 4 which maps to THING_TYPE_RF1
        rfSubDevices.add(rfDevice);

        when(mockRfBridgeHandler.getSubDevices()).thenReturn(rfSubDevices);
        when(mockRfBridgeHandler.getThing()).thenReturn(rfBridgeThing);

        childThings.add(rfBridgeThing);
    }

    private void setupZigbeeBridge() {
        // Create Zigbee bridge thing
        Bridge zigbeeBridgeThing = mock(Bridge.class);
        ThingTypeUID zigbeeBridgeTypeUID = new ThingTypeUID(SonoffBindingConstants.BINDING_ID, "66");
        when(zigbeeBridgeThing.getThingTypeUID()).thenReturn(zigbeeBridgeTypeUID);
        when(zigbeeBridgeThing.getHandler()).thenReturn(mockZigbeeBridgeHandler);
        when(zigbeeBridgeThing.getUID()).thenReturn(new ThingUID(zigbeeBridgeTypeUID, accountThingUID, "zigbeebridge"));

        // Setup Zigbee sub-devices
        JsonArray zigbeeSubDevices = new JsonArray();
        JsonObject zigbeeDevice = new JsonObject();
        zigbeeDevice.addProperty("deviceid", "zigbee123");
        zigbeeDevice.addProperty("uiid", 1000);
        zigbeeSubDevices.add(zigbeeDevice);

        when(mockZigbeeBridgeHandler.getSubDevices()).thenReturn(zigbeeSubDevices);
        when(mockZigbeeBridgeHandler.getThing()).thenReturn(zigbeeBridgeThing);

        childThings.add(zigbeeBridgeThing);
    }

    private void runDiscoveryWithMockData() {
        try {
            when(mockApiConnection.createCache()).thenReturn(createMockApiResponse());

            // Use reflection to call the private discover method
            java.lang.reflect.Method discoverMethod = discoveryService.getClass().getDeclaredMethod("discover");
            discoverMethod.setAccessible(true);
            discoverMethod.invoke(discoveryService);
        } catch (Exception e) {
            fail("Failed to run discovery: " + e.getMessage());
        }
    }
}
