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
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.sonoff.internal.SonoffBindingConstants;
import org.openhab.binding.sonoff.internal.SonoffCacheProvider;
import org.openhab.binding.sonoff.internal.connection.SonoffApiConnection;
import org.openhab.binding.sonoff.internal.connection.SonoffConnectionManager;
import org.openhab.binding.sonoff.internal.handler.SonoffAccountHandler;
import org.openhab.binding.sonoff.internal.handler.SonoffRfBridgeHandler;
import org.openhab.binding.sonoff.internal.handler.SonoffZigbeeBridgeHandler;
import org.openhab.core.config.core.Configuration;
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
    private SonoffCacheProvider mockCacheProvider;

    @Mock
    private Thing mockAccountThing;

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
    private ScheduledFuture<?> mockScheduledFuture;

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

        // Setup mock cache provider
        lenient().when(mockCacheProvider.checkFile(anyString())).thenReturn(false);
        lenient().doNothing().when(mockCacheProvider).newFile(anyString(), anyString());

        // Setup mock scheduler
        lenient().when(mockScheduler.schedule(any(Runnable.class), anyLong(), any(TimeUnit.class)))
                .thenReturn(mockScheduledFuture);

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
        assertEquals(SonoffBindingConstants.DISCOVERABLE_THING_TYPE_UIDS, 
                     discoveryService.getSupportedThingTypes());
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
        return "{" +
                "\"data\": {" +
                "\"thingList\": [" +
                createDeviceJson("device1", "Test Device", 1) +
                "]" +
                "}" +
                "}";
    }

    // Helper method to create device JSON
    private String createDeviceJson(String deviceId, String name, int uiid) {
        return "{" +
                "\"itemType\": 1," +
                "\"itemData\": {" +
                "\"deviceid\": \"" + deviceId + "\"," +
                "\"name\": \"" + name + "\"," +
                "\"brandName\": \"Sonoff\"," +
                "\"productModel\": \"Test Model\"," +
                "\"devicekey\": \"test-key\"," +
                "\"apikey\": \"test-api-key\"," +
                "\"extra\": {" +
                "\"uiid\": " + uiid +
                "}," +
                "\"params\": {" +
                "\"fwVersion\": \"1.0.0\"," +
                "\"ssid\": \"TestWiFi\"" +
                "}" +
                "}" +
                "}";
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
    @DisplayName("Should handle scan task lifecycle correctly")
    void testScanTaskLifecycle() {
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
    @DisplayName("Should handle multiple startScan calls correctly")
    void testMultipleStartScanCalls() {
        // First scan
        discoveryService.startScan();
        verify(mockScheduler, times(1)).schedule(any(Runnable.class), eq(0L), eq(TimeUnit.SECONDS));

        // Second scan should cancel previous task
        discoveryService.startScan();
        verify(mockScheduler, times(2)).schedule(any(Runnable.class), eq(0L), eq(TimeUnit.SECONDS));
        verify(mockScheduledFuture).cancel(true);
    }

    @Test
    @DisplayName("Should handle stopScan without active scan")
    void testStopScanWithoutActiveScan() {
        // Call stopScan without starting scan first
        assertDoesNotThrow(() -> discoveryService.stopScan());
    }

    @Test
    @DisplayName("Should handle null account handler gracefully")
    void testNullAccountHandler() {
        // Set null account handler
        discoveryService.setThingHandler(null);
        
        // Try to start scan - should not throw exception
        assertDoesNotThrow(() -> discoveryService.startScan());
    }

    @Test
    @DisplayName("Should validate constructor parameters")
    void testConstructorValidation() {
        SonoffDiscoveryService service = new SonoffDiscoveryService();
        
        // Verify supported thing types are set correctly
        assertNotNull(service.getSupportedThingTypes());
        assertFalse(service.getSupportedThingTypes().isEmpty());
        assertEquals(SonoffBindingConstants.DISCOVERABLE_THING_TYPE_UIDS, 
                     service.getSupportedThingTypes());
    }

    @Test
    @DisplayName("Should handle configuration changes")
    void testConfigurationHandling() {
        Map<String, Object> config = new HashMap<>();
        config.put("testKey", "testValue");
        config.put("timeout", 30);
        
        // Should not throw exception with various config types
        assertDoesNotThrow(() -> discoveryService.activate(config));
        
        // Test with null values in config
        config.put("nullValue", null);
        assertDoesNotThrow(() -> discoveryService.activate(config));
    }

    @Test
    @DisplayName("Should create cache with valid API response")
    void testCreateCacheWithValidResponse() {
        // Setup
        when(mockApiConnection.createCache()).thenReturn(createSimpleApiResponse());
        when(mockCacheProvider.checkFile("device1")).thenReturn(false);
        
        List<Thing> things = new ArrayList<>();
        
        // Execute
        List<JsonObject> devices = discoveryService.createCache(things);
        
        // Verify
        assertNotNull(devices);
        assertEquals(1, devices.size());
        
        JsonObject device = devices.get(0);
        assertEquals("device1", device.get("deviceid").getAsString());
        assertEquals("Test Device", device.get("name").getAsString());
        
        // Verify cache operations
        verify(mockCacheProvider).checkFile("device1");
        verify(mockCacheProvider).newFile(eq("device1"), anyString());
        verify(mockAccountHandler).addState("device1");
    }

    @Test
    @DisplayName("Should handle empty API response")
    void testCreateCacheWithEmptyResponse() {
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
    void testCreateCacheWithMalformedResponse() {
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
    void testCreateCacheWithApiException() {
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
    void testCreateCacheInLocalMode() {
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
    @DisplayName("Should skip existing cache files")
    void testCreateCacheWithExistingFile() {
        // Setup
        when(mockApiConnection.createCache()).thenReturn(createSimpleApiResponse());
        when(mockCacheProvider.checkFile("device1")).thenReturn(true);
        
        List<Thing> things = new ArrayList<>();
        
        // Execute
        List<JsonObject> devices = discoveryService.createCache(things);
        
        // Verify
        assertNotNull(devices);
        assertEquals(1, devices.size());
        
        // Verify cache file was not created again
        verify(mockCacheProvider).checkFile("device1");
        verify(mockCacheProvider, never()).newFile(anyString(), anyString());
        verify(mockAccountHandler, never()).addState(anyString());
    }

    @Test
    @DisplayName("Should handle device with missing required fields")
    void testCreateCacheWithIncompleteDevice() {
        // Setup - device missing deviceid
        String incompleteResponse = "{" +
                "\"data\": {" +
                "\"thingList\": [" +
                "{" +
                "\"itemType\": 1," +
                "\"itemData\": {" +
                "\"name\": \"Test Device\"," +
                "\"extra\": {\"uiid\": 1}" +
                "}" +
                "}" +
                "]" +
                "}" +
                "}";
        
        when(mockApiConnection.createCache()).thenReturn(incompleteResponse);
        
        List<Thing> things = new ArrayList<>();
        
        // Execute
        List<JsonObject> devices = discoveryService.createCache(things);
        
        // Verify - device should be skipped
        assertNotNull(devices);
        assertTrue(devices.isEmpty());
    }

    @Test
    @DisplayName("Should handle multiple devices in response")
    void testCreateCacheWithMultipleDevices() {
        // Setup
        String multiDeviceResponse = "{" +
                "\"data\": {" +
                "\"thingList\": [" +
                createDeviceJson("device1", "Device 1", 1) + "," +
                createDeviceJson("device2", "Device 2", 2) + "," +
                createDeviceJson("device3", "Device 3", 3) +
                "]" +
                "}" +
                "}";
        
        when(mockApiConnection.createCache()).thenReturn(multiDeviceResponse);
        when(mockCacheProvider.checkFile(anyString())).thenReturn(false);
        
        List<Thing> things = new ArrayList<>();
        
        // Execute
        List<JsonObject> devices = discoveryService.createCache(things);
        
        // Verify
        assertNotNull(devices);
        assertEquals(3, devices.size());
        
        // Verify all devices were processed
        verify(mockCacheProvider, times(3)).checkFile(anyString());
        verify(mockCacheProvider, times(3)).newFile(anyString(), anyString());
        verify(mockAccountHandler, times(3)).addState(anyString());
    }

    @Test
    @DisplayName("Should discover RF bridge sub-devices")
    void testRfBridgeSubDeviceDiscovery() {
        // Setup RF bridge
        ThingUID rfBridgeUID = new ThingUID(new ThingTypeUID("sonoff", "28"), accountThingUID, "rf-bridge");
        when(mockRfBridge.getUID()).thenReturn(rfBridgeUID);
        when(mockRfBridge.getThingTypeUID()).thenReturn(new ThingTypeUID("sonoff", "28"));
        when(mockRfBridge.getHandler()).thenReturn(mockRfBridgeHandler);
        when(mockRfBridgeHandler.getThing()).thenReturn(mockRfBridge);

        // Setup RF sub-devices
        JsonArray rfSubDevices = new JsonArray();
        JsonObject rfDevice1 = new JsonObject();
        rfDevice1.addProperty("remote_type", "1");
        rfDevice1.addProperty("name", "RF Remote 1");
        rfSubDevices.add(rfDevice1);

        JsonObject rfDevice2 = new JsonObject();
        rfDevice2.addProperty("remote_type", "2");
        rfDevice2.addProperty("name", "RF Remote 2");
        rfSubDevices.add(rfDevice2);

        when(mockRfBridgeHandler.getSubDevices()).thenReturn(rfSubDevices);

        // Setup things list with RF bridge
        List<Thing> things = new ArrayList<>();
        things.add(mockRfBridge);
        when(mockAccountThing.getThings()).thenReturn(things);

        // Mock SonoffBindingConstants.createSensorMap() to return valid mappings
        try (MockedStatic<SonoffBindingConstants> mockedConstants = mockStatic(SonoffBindingConstants.class)) {
            Map<Integer, ThingTypeUID> sensorMap = new HashMap<>();
            sensorMap.put(1, new ThingTypeUID("sonoff", "rf-sensor"));
            sensorMap.put(2, new ThingTypeUID("sonoff", "rf-remote"));
            mockedConstants.when(SonoffBindingConstants::createSensorMap).thenReturn(sensorMap);

            // Execute discovery using reflection to call private discover method
            try {
                java.lang.reflect.Method discoverMethod = discoveryService.getClass().getDeclaredMethod("discover");
                discoverMethod.setAccessible(true);
                discoverMethod.invoke(discoveryService);
            } catch (Exception e) {
                fail("Failed to invoke discover method: " + e.getMessage());
            }

            // Verify RF sub-devices were discovered
            verify(mockDiscoveryListener, times(2)).thingDiscovered(any(), any());
        }
    }

    @Test
    @DisplayName("Should discover Zigbee bridge sub-devices")
    void testZigbeeBridgeSubDeviceDiscovery() {
        // Setup Zigbee bridge
        ThingUID zigbeeBridgeUID = new ThingUID(new ThingTypeUID("sonoff", "66"), accountThingUID, "zigbee-bridge");
        when(mockZigbeeBridge.getUID()).thenReturn(zigbeeBridgeUID);
        when(mockZigbeeBridge.getThingTypeUID()).thenReturn(new ThingTypeUID("sonoff", "66"));
        when(mockZigbeeBridge.getHandler()).thenReturn(mockZigbeeBridgeHandler);
        when(mockZigbeeBridgeHandler.getThing()).thenReturn(mockZigbeeBridge);

        // Setup Zigbee sub-devices
        JsonArray zigbeeSubDevices = new JsonArray();
        JsonObject zigbeeDevice1 = new JsonObject();
        zigbeeDevice1.addProperty("deviceid", "zigbee-device-1");
        zigbeeDevice1.addProperty("uiid", 1000);
        zigbeeSubDevices.add(zigbeeDevice1);

        when(mockZigbeeBridgeHandler.getSubDevices()).thenReturn(zigbeeSubDevices);

        // Setup main device list to include the Zigbee sub-device
        String zigbeeDeviceResponse = "{" +
                "\"data\": {" +
                "\"thingList\": [" +
                createZigbeeDeviceJson("zigbee-device-1", "Zigbee Sensor", 1000) +
                "]" +
                "}" +
                "}";
        when(mockApiConnection.createCache()).thenReturn(zigbeeDeviceResponse);

        // Setup things list with Zigbee bridge
        List<Thing> things = new ArrayList<>();
        things.add(mockZigbeeBridge);
        when(mockAccountThing.getThings()).thenReturn(things);

        // Mock SonoffBindingConstants.createZigbeeMap() to return valid mappings
        try (MockedStatic<SonoffBindingConstants> mockedConstants = mockStatic(SonoffBindingConstants.class)) {
            Map<Integer, ThingTypeUID> zigbeeMap = new HashMap<>();
            zigbeeMap.put(1000, new ThingTypeUID("sonoff", "zigbee-sensor"));
            mockedConstants.when(SonoffBindingConstants::createZigbeeMap).thenReturn(zigbeeMap);

            // Execute discovery using reflection to call private discover method
            try {
                java.lang.reflect.Method discoverMethod = discoveryService.getClass().getDeclaredMethod("discover");
                discoverMethod.setAccessible(true);
                discoverMethod.invoke(discoveryService);
            } catch (Exception e) {
                fail("Failed to invoke discover method: " + e.getMessage());
            }

            // Verify Zigbee sub-device was discovered
            verify(mockDiscoveryListener, atLeastOnce()).thingDiscovered(any(), any());
        }
    }

    @Test
    @DisplayName("Should handle RF bridge with null sub-devices")
    void testRfBridgeWithNullSubDevices() {
        // Setup RF bridge
        ThingUID rfBridgeUID = new ThingUID(new ThingTypeUID("sonoff", "28"), accountThingUID, "rf-bridge");
        when(mockRfBridge.getUID()).thenReturn(rfBridgeUID);
        when(mockRfBridge.getThingTypeUID()).thenReturn(new ThingTypeUID("sonoff", "28"));
        when(mockRfBridge.getHandler()).thenReturn(mockRfBridgeHandler);
        when(mockRfBridgeHandler.getThing()).thenReturn(mockRfBridge);
        when(mockRfBridgeHandler.getSubDevices()).thenReturn(null);

        // Setup things list with RF bridge
        List<Thing> things = new ArrayList<>();
        things.add(mockRfBridge);
        when(mockAccountThing.getThings()).thenReturn(things);

        // Execute discovery - should not throw exception
        assertDoesNotThrow(() -> {
            try {
                java.lang.reflect.Method discoverMethod = discoveryService.getClass().getDeclaredMethod("discover");
                discoverMethod.setAccessible(true);
                discoverMethod.invoke(discoveryService);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    @DisplayName("Should handle Zigbee bridge with null sub-devices")
    void testZigbeeBridgeWithNullSubDevices() {
        // Setup Zigbee bridge
        ThingUID zigbeeBridgeUID = new ThingUID(new ThingTypeUID("sonoff", "66"), accountThingUID, "zigbee-bridge");
        when(mockZigbeeBridge.getUID()).thenReturn(zigbeeBridgeUID);
        when(mockZigbeeBridge.getThingTypeUID()).thenReturn(new ThingTypeUID("sonoff", "66"));
        when(mockZigbeeBridge.getHandler()).thenReturn(mockZigbeeBridgeHandler);
        when(mockZigbeeBridgeHandler.getThing()).thenReturn(mockZigbeeBridge);
        when(mockZigbeeBridgeHandler.getSubDevices()).thenReturn(null);

        // Setup things list with Zigbee bridge
        List<Thing> things = new ArrayList<>();
        things.add(mockZigbeeBridge);
        when(mockAccountThing.getThings()).thenReturn(things);

        // Execute discovery - should not throw exception
        assertDoesNotThrow(() -> {
            try {
                java.lang.reflect.Method discoverMethod = discoveryService.getClass().getDeclaredMethod("discover");
                discoverMethod.setAccessible(true);
                discoverMethod.invoke(discoveryService);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    @DisplayName("Should handle bridge with null handler")
    void testBridgeWithNullHandler() {
        // Setup bridge with null handler
        ThingUID rfBridgeUID = new ThingUID(new ThingTypeUID("sonoff", "28"), accountThingUID, "rf-bridge");
        when(mockRfBridge.getUID()).thenReturn(rfBridgeUID);
        when(mockRfBridge.getThingTypeUID()).thenReturn(new ThingTypeUID("sonoff", "28"));
        when(mockRfBridge.getHandler()).thenReturn(null);

        // Setup things list with bridge
        List<Thing> things = new ArrayList<>();
        things.add(mockRfBridge);
        when(mockAccountThing.getThings()).thenReturn(things);

        // Execute discovery - should not throw exception
        assertDoesNotThrow(() -> {
            try {
                java.lang.reflect.Method discoverMethod = discoveryService.getClass().getDeclaredMethod("discover");
                discoverMethod.setAccessible(true);
                discoverMethod.invoke(discoveryService);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Helper method to create Zigbee device JSON
    private String createZigbeeDeviceJson(String deviceId, String name, int uiid) {
        return "{" +
                "\"itemType\": 1," +
                "\"itemData\": {" +
                "\"deviceid\": \"" + deviceId + "\"," +
                "\"name\": \"" + name + "\"," +
                "\"brandName\": \"Sonoff\"," +
                "\"productModel\": \"Zigbee Model\"," +
                "\"devicekey\": \"zigbee-key\"," +
                "\"apikey\": \"zigbee-api-key\"," +
                "\"extra\": {" +
                "\"uiid\": " + uiid +
                "}," +
                "\"params\": {" +
                "\"fwVersion\": \"2.0.0\"" +
                "}" +
                "}" +
                "}";
    }

    @Test
    @DisplayName("Should handle JSON parsing errors gracefully")
    void testJsonParsingErrors() {
        // Test with completely invalid JSON
        when(mockApiConnection.createCache()).thenReturn("invalid json");
        
        List<Thing> things = new ArrayList<>();
        List<JsonObject> devices = discoveryService.createCache(things);
        
        assertNotNull(devices);
        assertTrue(devices.isEmpty());
    }

    @Test
    @DisplayName("Should handle missing data field in response")
    void testMissingDataField() {
        String responseWithoutData = "{\"status\": \"ok\"}";
        when(mockApiConnection.createCache()).thenReturn(responseWithoutData);
        
        List<Thing> things = new ArrayList<>();
        List<JsonObject> devices = discoveryService.createCache(things);
        
        assertNotNull(devices);
        assertTrue(devices.isEmpty());
    }

    @Test
    @DisplayName("Should handle missing thingList field in data")
    void testMissingThingListField() {
        String responseWithoutThingList = "{\"data\": {\"other\": \"field\"}}";
        when(mockApiConnection.createCache()).thenReturn(responseWithoutThingList);
        
        List<Thing> things = new ArrayList<>();
        List<JsonObject> devices = discoveryService.createCache(things);
        
        assertNotNull(devices);
        assertTrue(devices.isEmpty());
    }

    @Test
    @DisplayName("Should handle null JSON elements")
    void testNullJsonElements() {
        String responseWithNulls = "{" +
                "\"data\": {" +
                "\"thingList\": [" +
                "null," +
                "{" +
                "\"itemType\": 1," +
                "\"itemData\": null" +
                "}," +
                createDeviceJson("valid-device", "Valid Device", 1) +
                "]" +
                "}" +
                "}";
        
        when(mockApiConnection.createCache()).thenReturn(responseWithNulls);
        when(mockCacheProvider.checkFile("valid-device")).thenReturn(false);
        
        List<Thing> things = new ArrayList<>();
        List<JsonObject> devices = discoveryService.createCache(things);
        
        // Should only process the valid device
        assertNotNull(devices);
        assertEquals(1, devices.size());
        assertEquals("valid-device", devices.get(0).get("deviceid").getAsString());
    }

    @Test
    @DisplayName("Should handle device with missing extra field")
    void testDeviceWithMissingExtraField() {
        String deviceWithoutExtra = "{" +
                "\"data\": {" +
                "\"thingList\": [" +
                "{" +
                "\"itemType\": 1," +
                "\"itemData\": {" +
                "\"deviceid\": \"device-no-extra\"," +
                "\"name\": \"Device Without Extra\"" +
                "}" +
                "}" +
                "]" +
                "}" +
                "}";
        
        when(mockApiConnection.createCache()).thenReturn(deviceWithoutExtra);
        
        List<Thing> things = new ArrayList<>();
        List<JsonObject> devices = discoveryService.createCache(things);
        
        // Device should be skipped due to missing extra field
        assertNotNull(devices);
        assertTrue(devices.isEmpty());
    }

    @Test
    @DisplayName("Should handle device with missing uiid field")
    void testDeviceWithMissingUiidField() {
        String deviceWithoutUiid = "{" +
                "\"data\": {" +
                "\"thingList\": [" +
                "{" +
                "\"itemType\": 1," +
                "\"itemData\": {" +
                "\"deviceid\": \"device-no-uiid\"," +
                "\"name\": \"Device Without UIID\"," +
                "\"extra\": {}" +
                "}" +
                "}" +
                "]" +
                "}" +
                "}";
        
        when(mockApiConnection.createCache()).thenReturn(deviceWithoutUiid);
        
        List<Thing> things = new ArrayList<>();
        List<JsonObject> devices = discoveryService.createCache(things);
        
        // Device should be skipped due to missing uiid field
        assertNotNull(devices);
        assertTrue(devices.isEmpty());
    }

    @Test
    @DisplayName("Should handle cache provider exceptions")
    void testCacheProviderExceptions() {
        when(mockApiConnection.createCache()).thenReturn(createSimpleApiResponse());
        when(mockCacheProvider.checkFile("device1")).thenThrow(new RuntimeException("Cache error"));
        
        List<Thing> things = new ArrayList<>();
        
        // Should not throw exception even if cache provider fails
        assertDoesNotThrow(() -> {
            List<JsonObject> devices = discoveryService.createCache(things);
            assertNotNull(devices);
        });
    }

    @Test
    @DisplayName("Should handle account handler exceptions")
    void testAccountHandlerExceptions() {
        when(mockApiConnection.createCache()).thenReturn(createSimpleApiResponse());
        when(mockCacheProvider.checkFile("device1")).thenReturn(false);
        doThrow(new RuntimeException("Handler error")).when(mockAccountHandler).addState("device1");
        
        List<Thing> things = new ArrayList<>();
        
        // Should not throw exception even if account handler fails
        assertDoesNotThrow(() -> {
            List<JsonObject> devices = discoveryService.createCache(things);
            assertNotNull(devices);
            assertEquals(1, devices.size());
        });
    }

    @Test
    @DisplayName("Should handle thing re-initialization")
    void testThingReinitialization() {
        // Setup existing thing with matching deviceid
        Thing existingThing = mock(Thing.class);
        ThingHandler existingHandler = mock(ThingHandler.class);
        Map<String, Object> config = new HashMap<>();
        config.put("deviceid", "device1");
        
        when(existingThing.getConfiguration()).thenReturn(new Configuration(config));
        when(existingThing.getHandler()).thenReturn(existingHandler);
        
        List<Thing> things = new ArrayList<>();
        things.add(existingThing);
        
        when(mockApiConnection.createCache()).thenReturn(createSimpleApiResponse());
        when(mockCacheProvider.checkFile("device1")).thenReturn(false);
        
        // Execute
        List<JsonObject> devices = discoveryService.createCache(things);
        
        // Verify thing was re-initialized
        verify(existingHandler).thingUpdated(existingThing);
        assertNotNull(devices);
        assertEquals(1, devices.size());
    }

    @Test
    @DisplayName("Should handle thing with null configuration")
    void testThingWithNullConfiguration() {
        Thing thingWithNullConfig = mock(Thing.class);
        when(thingWithNullConfig.getConfiguration()).thenReturn(null);
        
        List<Thing> things = new ArrayList<>();
        things.add(thingWithNullConfig);
        
        when(mockApiConnection.createCache()).thenReturn(createSimpleApiResponse());
        when(mockCacheProvider.checkFile("device1")).thenReturn(false);
        
        // Should not throw exception
        assertDoesNotThrow(() -> {
            List<JsonObject> devices = discoveryService.createCache(things);
            assertNotNull(devices);
        });
    }

    @Test
    @DisplayName("Should handle thing with null handler")
    void testThingWithNullHandler() {
        Thing thingWithNullHandler = mock(Thing.class);
        Map<String, Object> config = new HashMap<>();
        config.put("deviceid", "device1");
        
        when(thingWithNullHandler.getConfiguration()).thenReturn(new Configuration(config));
        when(thingWithNullHandler.getHandler()).thenReturn(null);
        
        List<Thing> things = new ArrayList<>();
        things.add(thingWithNullHandler);
        
        when(mockApiConnection.createCache()).thenReturn(createSimpleApiResponse());
        when(mockCacheProvider.checkFile("device1")).thenReturn(false);
        
        // Should not throw exception
        assertDoesNotThrow(() -> {
            List<JsonObject> devices = discoveryService.createCache(things);
            assertNotNull(devices);
        });
    }

    @Test
    @DisplayName("Should handle RF sub-device with missing remote_type")
    void testRfSubDeviceWithMissingRemoteType() {
        // Setup RF bridge
        ThingUID rfBridgeUID = new ThingUID(new ThingTypeUID("sonoff", "28"), accountThingUID, "rf-bridge");
        when(mockRfBridge.getUID()).thenReturn(rfBridgeUID);
        when(mockRfBridge.getThingTypeUID()).thenReturn(new ThingTypeUID("sonoff", "28"));
        when(mockRfBridge.getHandler()).thenReturn(mockRfBridgeHandler);
        when(mockRfBridgeHandler.getThing()).thenReturn(mockRfBridge);

        // Setup RF sub-device without remote_type
        JsonArray rfSubDevices = new JsonArray();
        JsonObject rfDeviceWithoutType = new JsonObject();
        rfDeviceWithoutType.addProperty("name", "RF Device Without Type");
        rfSubDevices.add(rfDeviceWithoutType);

        when(mockRfBridgeHandler.getSubDevices()).thenReturn(rfSubDevices);

        List<Thing> things = new ArrayList<>();
        things.add(mockRfBridge);
        when(mockAccountThing.getThings()).thenReturn(things);

        // Execute discovery - should not throw exception
        assertDoesNotThrow(() -> {
            try {
                java.lang.reflect.Method discoverMethod = discoveryService.getClass().getDeclaredMethod("discover");
                discoverMethod.setAccessible(true);
                discoverMethod.invoke(discoveryService);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    @DisplayName("Should handle unsupported device types")
    void testUnsupportedDeviceTypes() {
        // Create device with unsupported UIID
        String unsupportedDeviceResponse = "{" +
                "\"data\": {" +
                "\"thingList\": [" +
                createDeviceJson("unsupported-device", "Unsupported Device", 9999) +
                "]" +
                "}" +
                "}";
        
        when(mockApiConnection.createCache()).thenReturn(unsupportedDeviceResponse);
        when(mockCacheProvider.checkFile("unsupported-device")).thenReturn(false);
        
        // Mock SonoffBindingConstants.createMap() to return empty map (no support for UIID 9999)
        try (MockedStatic<SonoffBindingConstants> mockedConstants = mockStatic(SonoffBindingConstants.class)) {
            Map<Integer, ThingTypeUID> emptyMap = new HashMap<>();
            Map<Integer, ThingTypeUID> emptyZigbeeMap = new HashMap<>();
            Map<Integer, ThingTypeUID> emptySensorMap = new HashMap<>();
            
            mockedConstants.when(SonoffBindingConstants::createMap).thenReturn(emptyMap);
            mockedConstants.when(SonoffBindingConstants::createZigbeeMap).thenReturn(emptyZigbeeMap);
            mockedConstants.when(SonoffBindingConstants::createSensorMap).thenReturn(emptySensorMap);

            List<Thing> things = new ArrayList<>();
            
            // Execute discovery
            try {
                java.lang.reflect.Method discoverMethod = discoveryService.getClass().getDeclaredMethod("discover");
                discoverMethod.setAccessible(true);
                discoverMethod.invoke(discoveryService);
            } catch (Exception e) {
                fail("Failed to invoke discover method: " + e.getMessage());
            }

            // Verify no discovery results were created for unsupported device
            verify(mockDiscoveryListener, never()).thingDiscovered(any(), any());
        }
    }

    @Test
    @DisplayName("Should create new cache file when device not cached")
    void testCreateNewCacheFile() {
        when(mockApiConnection.createCache()).thenReturn(createSimpleApiResponse());
        when(mockCacheProvider.checkFile("device1")).thenReturn(false);
        
        List<Thing> things = new ArrayList<>();
        List<JsonObject> devices = discoveryService.createCache(things);
        
        // Verify cache file creation
        verify(mockCacheProvider).checkFile("device1");
        verify(mockCacheProvider).newFile(eq("device1"), anyString());
        verify(mockAccountHandler).addState("device1");
        
        assertNotNull(devices);
        assertEquals(1, devices.size());
    }

    @Test
    @DisplayName("Should skip cache creation when file already exists")
    void testSkipCacheCreationForExistingFile() {
        when(mockApiConnection.createCache()).thenReturn(createSimpleApiResponse());
        when(mockCacheProvider.checkFile("device1")).thenReturn(true);
        
        List<Thing> things = new ArrayList<>();
        List<JsonObject> devices = discoveryService.createCache(things);
        
        // Verify cache file was checked but not created
        verify(mockCacheProvider).checkFile("device1");
        verify(mockCacheProvider, never()).newFile(anyString(), anyString());
        verify(mockAccountHandler, never()).addState(anyString());
        
        // Device should still be included in results
        assertNotNull(devices);
        assertEquals(1, devices.size());
    }

    @Test
    @DisplayName("Should handle cache provider file check exception")
    void testCacheProviderFileCheckException() {
        when(mockApiConnection.createCache()).thenReturn(createSimpleApiResponse());
        when(mockCacheProvider.checkFile("device1")).thenThrow(new RuntimeException("File check failed"));
        
        List<Thing> things = new ArrayList<>();
        
        // Should handle exception gracefully
        assertDoesNotThrow(() -> {
            List<JsonObject> devices = discoveryService.createCache(things);
            assertNotNull(devices);
            assertEquals(1, devices.size());
        });
        
        // Verify file check was attempted
        verify(mockCacheProvider).checkFile("device1");
    }

    @Test
    @DisplayName("Should handle cache provider new file exception")
    void testCacheProviderNewFileException() {
        when(mockApiConnection.createCache()).thenReturn(createSimpleApiResponse());
        when(mockCacheProvider.checkFile("device1")).thenReturn(false);
        doThrow(new RuntimeException("File creation failed")).when(mockCacheProvider).newFile(anyString(), anyString());
        
        List<Thing> things = new ArrayList<>();
        
        // Should handle exception gracefully
        assertDoesNotThrow(() -> {
            List<JsonObject> devices = discoveryService.createCache(things);
            assertNotNull(devices);
            assertEquals(1, devices.size());
        });
        
        // Verify file creation was attempted
        verify(mockCacheProvider).newFile(eq("device1"), anyString());
    }

    @Test
    @DisplayName("Should create cache with proper JSON content")
    void testCacheContentCreation() {
        when(mockApiConnection.createCache()).thenReturn(createSimpleApiResponse());
        when(mockCacheProvider.checkFile("device1")).thenReturn(false);
        
        List<Thing> things = new ArrayList<>();
        List<JsonObject> devices = discoveryService.createCache(things);
        
        // Verify cache file was created with proper JSON content
        verify(mockCacheProvider).newFile(eq("device1"), argThat(jsonContent -> {
            // Verify the JSON content contains expected device data
            return jsonContent.contains("\"deviceid\":\"device1\"") &&
                   jsonContent.contains("\"name\":\"Test Device\"") &&
                   jsonContent.contains("\"uiid\":1");
        }));
        
        assertNotNull(devices);
        assertEquals(1, devices.size());
    }

    @Test
    @DisplayName("Should handle multiple devices with mixed cache states")
    void testMixedCacheStates() {
        String multiDeviceResponse = "{" +
                "\"data\": {" +
                "\"thingList\": [" +
                createDeviceJson("cached-device", "Cached Device", 1) + "," +
                createDeviceJson("new-device", "New Device", 2) + "," +
                createDeviceJson("another-new", "Another New", 3) +
                "]" +
                "}" +
                "}";
        
        when(mockApiConnection.createCache()).thenReturn(multiDeviceResponse);
        
        // Setup mixed cache states
        when(mockCacheProvider.checkFile("cached-device")).thenReturn(true);
        when(mockCacheProvider.checkFile("new-device")).thenReturn(false);
        when(mockCacheProvider.checkFile("another-new")).thenReturn(false);
        
        List<Thing> things = new ArrayList<>();
        List<JsonObject> devices = discoveryService.createCache(things);
        
        // Verify cache operations
        verify(mockCacheProvider).checkFile("cached-device");
        verify(mockCacheProvider).checkFile("new-device");
        verify(mockCacheProvider).checkFile("another-new");
        
        // Only new devices should have cache files created
        verify(mockCacheProvider, never()).newFile(eq("cached-device"), anyString());
        verify(mockCacheProvider).newFile(eq("new-device"), anyString());
        verify(mockCacheProvider).newFile(eq("another-new"), anyString());
        
        // Only new devices should have state added
        verify(mockAccountHandler, never()).addState("cached-device");
        verify(mockAccountHandler).addState("new-device");
        verify(mockAccountHandler).addState("another-new");
        
        assertNotNull(devices);
        assertEquals(3, devices.size());
    }

    @Test
    @DisplayName("Should handle cache operations with null device data")
    void testCacheOperationsWithNullData() {
        String responseWithNullData = "{" +
                "\"data\": {" +
                "\"thingList\": [" +
                "{" +
                "\"itemType\": 1," +
                "\"itemData\": null" +
                "}" +
                "]" +
                "}" +
                "}";
        
        when(mockApiConnection.createCache()).thenReturn(responseWithNullData);
        
        List<Thing> things = new ArrayList<>();
        List<JsonObject> devices = discoveryService.createCache(things);
        
        // No cache operations should be performed for null data
        verify(mockCacheProvider, never()).checkFile(anyString());
        verify(mockCacheProvider, never()).newFile(anyString(), anyString());
        verify(mockAccountHandler, never()).addState(anyString());
        
        assertNotNull(devices);
        assertTrue(devices.isEmpty());
    }

    @Test
    @DisplayName("Should handle cache operations in local mode")
    void testCacheOperationsInLocalMode() {
        when(mockConnectionManager.getMode()).thenReturn("local");
        when(mockApiConnection.createCache()).thenReturn(createSimpleApiResponse());
        when(mockCacheProvider.checkFile("device1")).thenReturn(false);
        
        List<Thing> things = new ArrayList<>();
        List<JsonObject> devices = discoveryService.createCache(things);
        
        // Verify login was called for local mode
        verify(mockApiConnection).login();
        
        // Verify cache operations still work in local mode
        verify(mockCacheProvider).checkFile("device1");
        verify(mockCacheProvider).newFile(eq("device1"), anyString());
        verify(mockAccountHandler).addState("device1");
        
        assertNotNull(devices);
        assertEquals(1, devices.size());
    }

    @Test
    @DisplayName("Should handle cache operations when API login fails")
    void testCacheOperationsWithLoginFailure() {
        when(mockConnectionManager.getMode()).thenReturn("local");
        doThrow(new RuntimeException("Login failed")).when(mockApiConnection).login();
        when(mockApiConnection.createCache()).thenReturn(createSimpleApiResponse());
        when(mockCacheProvider.checkFile("device1")).thenReturn(false);
        
        List<Thing> things = new ArrayList<>();
        
        // Should handle login failure gracefully
        assertDoesNotThrow(() -> {
            List<JsonObject> devices = discoveryService.createCache(things);
            assertNotNull(devices);
        });
        
        verify(mockApiConnection).login();
    }
}