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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.sonoff.internal.SonoffBindingConstants;
import org.openhab.binding.sonoff.internal.connection.SonoffApiConnection;
import org.openhab.binding.sonoff.internal.connection.SonoffConnectionManager;
import org.openhab.binding.sonoff.internal.handler.SonoffAccountHandler;
import org.openhab.binding.sonoff.internal.handler.SonoffRfBridgeHandler;
import org.openhab.binding.sonoff.internal.handler.SonoffZigbeeBridgeHandler;
import org.openhab.core.OpenHAB;
import org.openhab.core.config.discovery.DiscoveryListener;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Integration tests for {@link SonoffDiscoveryService}.
 * 
 * Tests cover:
 * - End-to-end discovery scenarios
 * - Complex device hierarchies (RF and Zigbee bridges with sub-devices)
 * - Cache file integration
 * - Real-world API response handling
 * - Performance and timing scenarios
 *
 * @author Test Author - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
class SonoffDiscoveryServiceIntegrationTest {

    @TempDir
    Path tempDir;

    @Mock
    private SonoffAccountHandler mockAccountHandler;

    @Mock
    private SonoffConnectionManager mockConnectionManager;

    @Mock
    private SonoffApiConnection mockApiConnection;

    @Mock
    private Bridge mockAccountThing;

    @Mock
    private SonoffRfBridgeHandler mockRfBridgeHandler;

    @Mock
    private SonoffZigbeeBridgeHandler mockZigbeeBridgeHandler;

    private SonoffDiscoveryService discoveryService;
    private ThingUID accountThingUID;
    private String testCacheDir;
    private List<DiscoveryResult> discoveredResults;
    private CountDownLatch discoveryLatch;

    @BeforeEach
    void setUp() {
        discoveryService = new SonoffDiscoveryService();
        discoveredResults = new ArrayList<>();
        discoveryLatch = new CountDownLatch(1);

        // Setup temporary directory for cache
        testCacheDir = tempDir.resolve("sonoff").toString();

        // Setup account thing UID
        accountThingUID = new ThingUID(SonoffBindingConstants.THING_TYPE_ACCOUNT, "integration-test");

        // Setup mock account handler (lenient to avoid unnecessary stubbing warnings)
        lenient().when(mockAccountHandler.getThing()).thenReturn(mockAccountThing);
        lenient().when(mockAccountThing.getUID()).thenReturn(accountThingUID);
        lenient().when(mockAccountThing.getThings()).thenReturn(new ArrayList<>());
        lenient().when(mockAccountHandler.getConnectionManager()).thenReturn(mockConnectionManager);

        // Setup mock connection manager (lenient to avoid unnecessary stubbing warnings)
        lenient().when(mockConnectionManager.getApi()).thenReturn(mockApiConnection);
        lenient().when(mockConnectionManager.getMode()).thenReturn("cloud");

        // Setup addState method (lenient to avoid unnecessary stubbing warnings)
        lenient().doNothing().when(mockAccountHandler).addState(anyString());

        // Set the account handler
        discoveryService.setThingHandler(mockAccountHandler);

        // Add discovery listener to capture results
        discoveryService.addDiscoveryListener(new DiscoveryListener() {
            @Override
            public void thingDiscovered(org.openhab.core.config.discovery.DiscoveryService source,
                    DiscoveryResult result) {
                discoveredResults.add(result);
                discoveryLatch.countDown();
            }

            @Override
            public void thingRemoved(org.openhab.core.config.discovery.DiscoveryService source, ThingUID thingUID) {
                // Not used in these tests
            }

            @Override
            public void removeOlderResults(org.openhab.core.config.discovery.DiscoveryService source, Instant timestamp,
                    java.util.Collection<ThingTypeUID> thingTypeUIDs, ThingUID bridgeUID) {
                // Not used in these tests
            }
        });
    }

    @AfterEach
    void tearDown() throws IOException {
        // Clean up test files
        if (Files.exists(Paths.get(testCacheDir))) {
            Files.walk(Paths.get(testCacheDir)).sorted((a, b) -> b.compareTo(a)).map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    @Test
    @DisplayName("Should discover multiple device types in single scan")
    void testCompleteDiscoveryScenario() throws Exception {
        // Setup
        setupComplexDeviceHierarchy();
        when(mockApiConnection.createCache()).thenReturn(createComplexApiResponse());

        try (MockedStatic<OpenHAB> mockedOpenHAB = mockStatic(OpenHAB.class)) {
            mockedOpenHAB.when(OpenHAB::getUserDataFolder).thenReturn(tempDir.toString());

            // Execute
            List<JsonObject> devices = discoveryService.createCache(createMockThingsList());

            // Verify cache creation
            assertFalse(devices.isEmpty(), "Should discover devices");
            assertEquals(4, devices.size(), "Should discover 4 main devices");

            // Verify cache files were created
            assertTrue(Files.exists(Paths.get(testCacheDir)), "Cache directory should be created");

            // Verify device states were added
            verify(mockAccountHandler, times(4)).addState(anyString());
        }
    }

    @Test
    @DisplayName("Should handle RF bridge with multiple sub-devices")
    void testRfBridgeDiscovery() throws Exception {
        // Setup
        setupRfBridgeWithMultipleDevices();
        when(mockApiConnection.createCache()).thenReturn(createRfBridgeApiResponse());

        try (MockedStatic<OpenHAB> mockedOpenHAB = mockStatic(OpenHAB.class)) {
            mockedOpenHAB.when(OpenHAB::getUserDataFolder).thenReturn(tempDir.toString());

            // Execute discovery
            runFullDiscovery();

            // Verify RF bridge and sub-devices were discovered
            assertTrue(discoveredResults.size() >= 3, "Should discover RF bridge and sub-devices");

            // Verify RF bridge device
            boolean foundRfBridge = discoveredResults.stream()
                    .anyMatch(result -> result.getThingTypeUID().getId().equals("28"));
            assertTrue(foundRfBridge, "Should discover RF bridge");

            // Verify RF sub-devices
            long rfSubDevices = discoveredResults.stream().filter(result -> result.getBridgeUID() != null)
                    .filter(result -> result.getBridgeUID().toString().contains(":28:")).count();
            assertEquals(2, rfSubDevices, "Should discover 2 RF sub-devices");
        }
    }

    @Test
    @DisplayName("Should handle Zigbee bridge with multiple sub-devices")
    void testZigbeeBridgeDiscovery() throws Exception {
        // Setup
        setupZigbeeBridgeWithMultipleDevices();
        when(mockApiConnection.createCache()).thenReturn(createZigbeeBridgeApiResponse());

        try (MockedStatic<OpenHAB> mockedOpenHAB = mockStatic(OpenHAB.class)) {
            mockedOpenHAB.when(OpenHAB::getUserDataFolder).thenReturn(tempDir.toString());

            // Execute discovery
            runFullDiscovery();

            // Verify Zigbee bridge and sub-devices were discovered
            assertTrue(discoveredResults.size() >= 3, "Should discover Zigbee bridge and sub-devices");

            // Verify Zigbee bridge device
            boolean foundZigbeeBridge = discoveredResults.stream()
                    .anyMatch(result -> result.getThingTypeUID().getId().equals("66"));
            assertTrue(foundZigbeeBridge, "Should discover Zigbee bridge");

            // Verify Zigbee sub-devices
            long zigbeeSubDevices = discoveredResults.stream().filter(result -> result.getBridgeUID() != null)
                    .filter(result -> result.getBridgeUID().toString().contains(":66:")).count();
            assertEquals(2, zigbeeSubDevices, "Should discover 2 Zigbee sub-devices");
        }
    }

    @Test
    @DisplayName("Should handle mixed bridge types in single discovery")
    void testMixedBridgeDiscovery() throws Exception {
        // Setup
        setupMixedBridgeEnvironment();
        when(mockApiConnection.createCache()).thenReturn(createMixedBridgeApiResponse());

        try (MockedStatic<OpenHAB> mockedOpenHAB = mockStatic(OpenHAB.class)) {
            mockedOpenHAB.when(OpenHAB::getUserDataFolder).thenReturn(tempDir.toString());

            // Execute discovery
            runFullDiscovery();

            // Verify both bridge types and their sub-devices
            assertTrue(discoveredResults.size() >= 6, "Should discover both bridges and all sub-devices");

            // Verify RF bridge and sub-devices
            boolean foundRfBridge = discoveredResults.stream()
                    .anyMatch(result -> result.getThingTypeUID().getId().equals("28"));
            assertTrue(foundRfBridge, "Should discover RF bridge");

            // Verify Zigbee bridge and sub-devices
            boolean foundZigbeeBridge = discoveredResults.stream()
                    .anyMatch(result -> result.getThingTypeUID().getId().equals("66"));
            assertTrue(foundZigbeeBridge, "Should discover Zigbee bridge");

            // Verify total sub-devices
            long totalSubDevices = discoveredResults.stream().filter(result -> result.getBridgeUID() != null).count();
            assertEquals(4, totalSubDevices, "Should discover 4 total sub-devices");
        }
    }

    @Test
    @DisplayName("Should handle discovery with existing cache files")
    void testDiscoveryWithExistingCache() throws Exception {
        // Setup - create existing cache files
        createExistingCacheFiles();

        when(mockApiConnection.createCache()).thenReturn(createSimpleApiResponse());

        try (MockedStatic<OpenHAB> mockedOpenHAB = mockStatic(OpenHAB.class)) {
            mockedOpenHAB.when(OpenHAB::getUserDataFolder).thenReturn(tempDir.toString());

            // Execute
            List<JsonObject> devices = discoveryService.createCache(new ArrayList<>());

            // Verify
            assertFalse(devices.isEmpty(), "Should discover devices even with existing cache");

            // Verify existing cache files are preserved
            assertTrue(Files.exists(Paths.get(testCacheDir, "existing-device.txt")),
                    "Existing cache file should be preserved");
        }
    }

    @Test
    @DisplayName("Should handle discovery performance with large device count")
    void testDiscoveryPerformance() throws Exception {
        // Setup
        when(mockApiConnection.createCache()).thenReturn(createLargeDeviceListResponse());

        try (MockedStatic<OpenHAB> mockedOpenHAB = mockStatic(OpenHAB.class)) {
            mockedOpenHAB.when(OpenHAB::getUserDataFolder).thenReturn(tempDir.toString());

            long startTime = System.currentTimeMillis();

            // Execute
            List<JsonObject> devices = discoveryService.createCache(new ArrayList<>());

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // Verify
            assertEquals(10, devices.size(), "Should discover all 10 devices");
            assertTrue(duration < 5000, "Discovery should complete within 5 seconds");

            // Verify all devices were processed
            verify(mockAccountHandler, times(10)).addState(anyString());
        }
    }

    @Test
    @DisplayName("Should handle discovery with network timeouts gracefully")
    void testDiscoveryWithNetworkTimeout() throws Exception {
        // Setup
        when(mockApiConnection.createCache()).thenThrow(new RuntimeException("Network timeout"));

        try (MockedStatic<OpenHAB> mockedOpenHAB = mockStatic(OpenHAB.class)) {
            mockedOpenHAB.when(OpenHAB::getUserDataFolder).thenReturn(tempDir.toString());

            // Execute
            List<JsonObject> devices = discoveryService.createCache(new ArrayList<>());

            // Verify
            assertTrue(devices.isEmpty(), "Should return empty list on network timeout");
            verify(mockAccountHandler, never()).addState(anyString());
        }
    }

    @Test
    @DisplayName("Should discover devices with all property types")
    void testDiscoveryWithCompleteProperties() throws Exception {
        // Setup
        when(mockApiConnection.createCache()).thenReturn(createDetailedDeviceResponse());

        try (MockedStatic<OpenHAB> mockedOpenHAB = mockStatic(OpenHAB.class)) {
            mockedOpenHAB.when(OpenHAB::getUserDataFolder).thenReturn(tempDir.toString());

            // Execute discovery
            runFullDiscovery();

            // Wait for discovery to complete
            assertTrue(discoveryLatch.await(5, TimeUnit.SECONDS), "Discovery should complete within 5 seconds");

            // Verify device properties
            assertFalse(discoveredResults.isEmpty(), "Should discover devices");

            DiscoveryResult result = discoveredResults.get(0);
            Map<String, Object> properties = result.getProperties();

            // Verify all expected properties are present
            assertTrue(properties.containsKey("deviceid"), "Should have deviceid property");
            assertTrue(properties.containsKey("Name"), "Should have Name property");
            assertTrue(properties.containsKey("Brand"), "Should have Brand property");
            assertTrue(properties.containsKey("Model"), "Should have Model property");
            assertTrue(properties.containsKey("FW Version"), "Should have FW Version property");
            assertTrue(properties.containsKey("Device Key"), "Should have Device Key property");
            assertTrue(properties.containsKey("UIID"), "Should have UIID property");
            assertTrue(properties.containsKey("API Key"), "Should have API Key property");
            assertTrue(properties.containsKey("Connected To SSID"), "Should have SSID property");

            // Verify property values
            assertEquals("detailed-device", properties.get("deviceid"));
            assertEquals("Detailed Test Device", properties.get("Name"));
            assertEquals("Sonoff", properties.get("Brand"));
        }
    }

    // Helper methods

    private void setupComplexDeviceHierarchy() {
        List<Thing> things = new ArrayList<>();

        // Add RF bridge
        Bridge rfBridge = createMockRfBridge();
        things.add(rfBridge);

        // Add Zigbee bridge
        Bridge zigbeeBridge = createMockZigbeeBridge();
        things.add(zigbeeBridge);

        when(mockAccountThing.getThings()).thenReturn(things);
    }

    private void setupRfBridgeWithMultipleDevices() {
        List<Thing> things = new ArrayList<>();
        Bridge rfBridge = createMockRfBridge();
        things.add(rfBridge);

        // Setup multiple RF sub-devices
        JsonArray rfSubDevices = new JsonArray();

        JsonObject rfDevice1 = new JsonObject();
        rfDevice1.addProperty("name", "RF Remote 1");
        rfDevice1.addProperty("remote_type", "1");
        rfSubDevices.add(rfDevice1);

        JsonObject rfDevice2 = new JsonObject();
        rfDevice2.addProperty("name", "RF Sensor 1");
        rfDevice2.addProperty("remote_type", "2");
        rfSubDevices.add(rfDevice2);

        when(mockRfBridgeHandler.getSubDevices()).thenReturn(rfSubDevices);
        when(mockAccountThing.getThings()).thenReturn(things);
    }

    private void setupZigbeeBridgeWithMultipleDevices() {
        List<Thing> things = new ArrayList<>();
        Bridge zigbeeBridge = createMockZigbeeBridge();
        things.add(zigbeeBridge);

        // Setup multiple Zigbee sub-devices
        JsonArray zigbeeSubDevices = new JsonArray();

        JsonObject zigbeeDevice1 = new JsonObject();
        zigbeeDevice1.addProperty("deviceid", "zigbee-sensor-1");
        zigbeeDevice1.addProperty("uiid", 1000);
        zigbeeSubDevices.add(zigbeeDevice1);

        JsonObject zigbeeDevice2 = new JsonObject();
        zigbeeDevice2.addProperty("deviceid", "zigbee-motion-1");
        zigbeeDevice2.addProperty("uiid", 1001);
        zigbeeSubDevices.add(zigbeeDevice2);

        when(mockZigbeeBridgeHandler.getSubDevices()).thenReturn(zigbeeSubDevices);
        when(mockAccountThing.getThings()).thenReturn(things);
    }

    private void setupMixedBridgeEnvironment() {
        List<Thing> things = new ArrayList<>();

        // Add both bridge types
        Bridge rfBridge = createMockRfBridge();
        Bridge zigbeeBridge = createMockZigbeeBridge();
        things.add(rfBridge);
        things.add(zigbeeBridge);

        // Setup RF sub-devices
        JsonArray rfSubDevices = new JsonArray();
        JsonObject rfDevice1 = new JsonObject();
        rfDevice1.addProperty("name", "RF Remote");
        rfDevice1.addProperty("remote_type", "1");
        rfSubDevices.add(rfDevice1);

        JsonObject rfDevice2 = new JsonObject();
        rfDevice2.addProperty("name", "RF Sensor");
        rfDevice2.addProperty("remote_type", "2");
        rfSubDevices.add(rfDevice2);

        when(mockRfBridgeHandler.getSubDevices()).thenReturn(rfSubDevices);

        // Setup Zigbee sub-devices
        JsonArray zigbeeSubDevices = new JsonArray();
        JsonObject zigbeeDevice1 = new JsonObject();
        zigbeeDevice1.addProperty("deviceid", "zigbee-temp-1");
        zigbeeDevice1.addProperty("uiid", 1000);
        zigbeeSubDevices.add(zigbeeDevice1);

        JsonObject zigbeeDevice2 = new JsonObject();
        zigbeeDevice2.addProperty("deviceid", "zigbee-contact-1");
        zigbeeDevice2.addProperty("uiid", 1001);
        zigbeeSubDevices.add(zigbeeDevice2);

        when(mockZigbeeBridgeHandler.getSubDevices()).thenReturn(zigbeeSubDevices);
        when(mockAccountThing.getThings()).thenReturn(things);
    }

    private Bridge createMockRfBridge() {
        Bridge rfBridge = mock(Bridge.class);
        ThingTypeUID rfBridgeTypeUID = new ThingTypeUID(SonoffBindingConstants.BINDING_ID, "28");
        ThingUID rfBridgeUID = new ThingUID(rfBridgeTypeUID, accountThingUID, "rf-bridge");

        when(rfBridge.getThingTypeUID()).thenReturn(rfBridgeTypeUID);
        when(rfBridge.getUID()).thenReturn(rfBridgeUID);
        when(rfBridge.getHandler()).thenReturn(mockRfBridgeHandler);
        when(mockRfBridgeHandler.getThing()).thenReturn(rfBridge);

        return rfBridge;
    }

    private Bridge createMockZigbeeBridge() {
        Bridge zigbeeBridge = mock(Bridge.class);
        ThingTypeUID zigbeeBridgeTypeUID = new ThingTypeUID(SonoffBindingConstants.BINDING_ID, "66");
        ThingUID zigbeeBridgeUID = new ThingUID(zigbeeBridgeTypeUID, accountThingUID, "zigbee-bridge");

        when(zigbeeBridge.getThingTypeUID()).thenReturn(zigbeeBridgeTypeUID);
        when(zigbeeBridge.getUID()).thenReturn(zigbeeBridgeUID);
        when(zigbeeBridge.getHandler()).thenReturn(mockZigbeeBridgeHandler);
        when(mockZigbeeBridgeHandler.getThing()).thenReturn(zigbeeBridge);

        return zigbeeBridge;
    }

    private List<Thing> createMockThingsList() {
        return new ArrayList<>();
    }

    private void createExistingCacheFiles() throws IOException {
        Files.createDirectories(Paths.get(testCacheDir));
        Files.write(Paths.get(testCacheDir, "existing-device.txt"),
                "{\"deviceid\":\"existing-device\",\"name\":\"Existing Device\"}".getBytes());
    }

    private void runFullDiscovery() {
        try {
            // Use reflection to call the private discover method
            java.lang.reflect.Method discoverMethod = discoveryService.getClass().getDeclaredMethod("discover");
            discoverMethod.setAccessible(true);
            discoverMethod.invoke(discoveryService);
        } catch (Exception e) {
            fail("Failed to run discovery: " + e.getMessage());
        }
    }

    // API Response builders

    private String createComplexApiResponse() {
        return "{" + "\"data\": {" + "\"thingList\": [" + createDeviceJson("device1", "Basic Switch", 1) + ","
                + createDeviceJson("device2", "Dual Switch", 2) + "," + createDeviceJson("rf-bridge", "RF Bridge", 28)
                + "," + createDeviceJson("zigbee-bridge", "Zigbee Bridge", 66) + "]}}";
    }

    private String createRfBridgeApiResponse() {
        return "{" + "\"data\": {" + "\"thingList\": [" + createDeviceJson("rf-bridge", "RF Bridge", 28) + "]}}";
    }

    private String createZigbeeBridgeApiResponse() {
        return "{" + "\"data\": {" + "\"thingList\": [" + createDeviceJson("zigbee-bridge", "Zigbee Bridge", 66) + ","
                + createDeviceJson("zigbee-sensor-1", "Zigbee Sensor", 1000) + ","
                + createDeviceJson("zigbee-motion-1", "Zigbee Motion", 1001) + "]}}";
    }

    private String createMixedBridgeApiResponse() {
        return "{" + "\"data\": {" + "\"thingList\": [" + createDeviceJson("rf-bridge", "RF Bridge", 28) + ","
                + createDeviceJson("zigbee-bridge", "Zigbee Bridge", 66) + ","
                + createDeviceJson("zigbee-temp-1", "Zigbee Temperature", 1000) + ","
                + createDeviceJson("zigbee-contact-1", "Zigbee Contact", 1001) + "]}}";
    }

    private String createSimpleApiResponse() {
        return "{" + "\"data\": {" + "\"thingList\": [" + createDeviceJson("simple-device", "Simple Device", 1) + "]}}";
    }

    private String createLargeDeviceListResponse() {
        StringBuilder response = new StringBuilder("{\"data\":{\"thingList\":[");
        for (int i = 0; i < 10; i++) {
            if (i > 0)
                response.append(",");
            response.append(createDeviceJson("device" + i, "Device " + i, 1));
        }
        response.append("]}}");
        return response.toString();
    }

    private String createDetailedDeviceResponse() {
        return "{" + "\"data\": {" + "\"thingList\": [" + "{" + "\"itemType\": 1," + "\"itemData\": {"
                + "\"deviceid\": \"detailed-device\"," + "\"name\": \"Detailed Test Device\","
                + "\"brandName\": \"Sonoff\"," + "\"productModel\": \"BASIC\"," + "\"devicekey\": \"detailed-key\","
                + "\"apikey\": \"detailed-api\"," + "\"extra\": {\"uiid\": 1}," + "\"params\": {"
                + "\"fwVersion\": \"2.0.0\"," + "\"ssid\": \"TestNetwork\"" + "}" + "}" + "}" + "]}}";
    }

    private String createDeviceJson(String deviceId, String name, int uiid) {
        return "{" + "\"itemType\": 1," + "\"itemData\": {" + "\"deviceid\": \"" + deviceId + "\"," + "\"name\": \""
                + name + "\"," + "\"brandName\": \"Sonoff\"," + "\"productModel\": \"TEST\"," + "\"devicekey\": \"key-"
                + deviceId + "\"," + "\"apikey\": \"api-" + deviceId + "\"," + "\"extra\": {\"uiid\": " + uiid + "},"
                + "\"params\": {\"fwVersion\": \"1.0.0\"}" + "}" + "}";
    }
}
