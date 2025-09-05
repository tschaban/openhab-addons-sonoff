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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Edge case and error handling tests for {@link SonoffDiscoveryService}.
 * 
 * Tests cover:
 * - Null and invalid input handling
 * - Malformed JSON responses
 * - Missing or corrupted cache files
 * - Bridge handlers returning null/empty data
 * - Concurrent access scenarios
 * - Memory and resource constraints
 *
 * @author Test Author - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
class SonoffDiscoveryServiceEdgeCaseTest {

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
    private Thing mockChildThing;

    @Mock
    private SonoffRfBridgeHandler mockRfBridgeHandler;

    @Mock
    private SonoffZigbeeBridgeHandler mockZigbeeBridgeHandler;

    @Mock
    private DiscoveryListener mockDiscoveryListener;

    private SonoffDiscoveryService discoveryService;
    private ThingUID accountThingUID;
    private String testCacheDir;

    @BeforeEach
    void setUp() {
        discoveryService = new SonoffDiscoveryService();
        testCacheDir = tempDir.resolve("sonoff").toString();
        accountThingUID = new ThingUID(SonoffBindingConstants.THING_TYPE_ACCOUNT, "edge-test");

        // Setup basic mocks
        when(mockAccountHandler.getThing()).thenReturn(mockAccountThing);
        when(mockAccountThing.getUID()).thenReturn(accountThingUID);
        when(mockAccountHandler.getConnectionManager()).thenReturn(mockConnectionManager);
        when(mockConnectionManager.getApi()).thenReturn(mockApiConnection);
        when(mockConnectionManager.getMode()).thenReturn("cloud");

        discoveryService.setThingHandler(mockAccountHandler);
        discoveryService.addDiscoveryListener(mockDiscoveryListener);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(Paths.get(testCacheDir))) {
            Files.walk(Paths.get(testCacheDir)).sorted((a, b) -> b.compareTo(a)).map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    @Test
    @DisplayName("Should handle null API response gracefully")
    void testNullApiResponse() throws Exception {
        // Setup
        when(mockApiConnection.createCache()).thenReturn(null);

        try (MockedStatic<OpenHAB> mockedOpenHAB = mockStatic(OpenHAB.class)) {
            mockedOpenHAB.when(OpenHAB::getUserDataFolder).thenReturn(tempDir.toString());

            // Execute
            List<JsonObject> devices = discoveryService.createCache(new ArrayList<>());

            // Verify
            assertTrue(devices.isEmpty(), "Should return empty list for null API response");
            verify(mockAccountHandler, never()).addState(anyString());
        }
    }

    @Test
    @DisplayName("Should handle empty JSON object response")
    void testEmptyJsonResponse() throws Exception {
        // Setup
        when(mockApiConnection.createCache()).thenReturn("{}");

        try (MockedStatic<OpenHAB> mockedOpenHAB = mockStatic(OpenHAB.class)) {
            mockedOpenHAB.when(OpenHAB::getUserDataFolder).thenReturn(tempDir.toString());

            // Execute
            List<JsonObject> devices = discoveryService.createCache(new ArrayList<>());

            // Verify
            assertTrue(devices.isEmpty(), "Should return empty list for empty JSON");
        }
    }

    @Test
    @DisplayName("Should handle JSON with missing data field")
    void testJsonMissingDataField() throws Exception {
        // Setup
        when(mockApiConnection.createCache()).thenReturn("{\"status\":\"ok\"}");

        try (MockedStatic<OpenHAB> mockedOpenHAB = mockStatic(OpenHAB.class)) {
            mockedOpenHAB.when(OpenHAB::getUserDataFolder).thenReturn(tempDir.toString());

            // Execute
            assertDoesNotThrow(() -> {
                List<JsonObject> devices = discoveryService.createCache(new ArrayList<>());
                assertTrue(devices.isEmpty(), "Should return empty list when data field is missing");
            }, "Should handle missing data field gracefully");
        }
    }

    @Test
    @DisplayName("Should handle JSON with missing thingList field")
    void testJsonMissingThingListField() throws Exception {
        // Setup
        when(mockApiConnection.createCache()).thenReturn("{\"data\":{\"status\":\"ok\"}}");

        try (MockedStatic<OpenHAB> mockedOpenHAB = mockStatic(OpenHAB.class)) {
            mockedOpenHAB.when(OpenHAB::getUserDataFolder).thenReturn(tempDir.toString());

            // Execute
            assertDoesNotThrow(() -> {
                List<JsonObject> devices = discoveryService.createCache(new ArrayList<>());
                assertTrue(devices.isEmpty(), "Should return empty list when thingList field is missing");
            }, "Should handle missing thingList field gracefully");
        }
    }

    @Test
    @DisplayName("Should handle devices with missing required fields")
    void testDevicesWithMissingFields() throws Exception {
        // Setup - device missing deviceid
        String incompleteResponse = "{" + "\"data\": {" + "\"thingList\": [" + "{" + "\"itemType\": 1,"
                + "\"itemData\": {" + "\"name\": \"Incomplete Device\"," + "\"extra\": {\"uiid\": 1}" + "}" + "}"
                + "]}}";

        when(mockApiConnection.createCache()).thenReturn(incompleteResponse);

        try (MockedStatic<OpenHAB> mockedOpenHAB = mockStatic(OpenHAB.class)) {
            mockedOpenHAB.when(OpenHAB::getUserDataFolder).thenReturn(tempDir.toString());

            // Execute
            assertDoesNotThrow(() -> {
                discoveryService.createCache(new ArrayList<>());
                // Should handle gracefully, may or may not include the device
            }, "Should handle devices with missing required fields gracefully");
        }
    }

    @Test
    @DisplayName("Should handle devices with null itemType")
    void testDevicesWithNullItemType() throws Exception {
        // Setup
        String responseWithNullItemType = "{" + "\"data\": {" + "\"thingList\": [" + "{" + "\"itemType\": null,"
                + "\"itemData\": {" + "\"deviceid\": \"null-type-device\"," + "\"name\": \"Null Type Device\","
                + "\"extra\": {\"uiid\": 1}" + "}" + "}" + "]}}";

        when(mockApiConnection.createCache()).thenReturn(responseWithNullItemType);

        try (MockedStatic<OpenHAB> mockedOpenHAB = mockStatic(OpenHAB.class)) {
            mockedOpenHAB.when(OpenHAB::getUserDataFolder).thenReturn(tempDir.toString());

            // Execute
            assertDoesNotThrow(() -> {
                List<JsonObject> devices = discoveryService.createCache(new ArrayList<>());
                assertTrue(devices.isEmpty(), "Should skip devices with null itemType");
            }, "Should handle null itemType gracefully");
        }
    }

    @Test
    @DisplayName("Should handle devices with non-type-1 itemType")
    void testDevicesWithWrongItemType() throws Exception {
        // Setup
        String responseWithWrongItemType = "{" + "\"data\": {" + "\"thingList\": [" + "{" + "\"itemType\": 2,"
                + "\"itemData\": {" + "\"deviceid\": \"wrong-type-device\"," + "\"name\": \"Wrong Type Device\","
                + "\"extra\": {\"uiid\": 1}" + "}" + "}" + "]}}";

        when(mockApiConnection.createCache()).thenReturn(responseWithWrongItemType);

        try (MockedStatic<OpenHAB> mockedOpenHAB = mockStatic(OpenHAB.class)) {
            mockedOpenHAB.when(OpenHAB::getUserDataFolder).thenReturn(tempDir.toString());

            // Execute
            List<JsonObject> devices = discoveryService.createCache(new ArrayList<>());

            // Verify
            assertTrue(devices.isEmpty(), "Should skip devices with itemType != 1");
        }
    }

    @Test
    @DisplayName("Should handle RF bridge with null sub-devices")
    void testRfBridgeWithNullSubDevices() {
        // Setup
        setupRfBridgeWithNullSubDevices();

        // Execute
        assertDoesNotThrow(() -> {
            runDiscoveryMethod();
        }, "Should handle RF bridge with null sub-devices gracefully");
    }

    @Test
    @DisplayName("Should handle RF bridge with empty sub-devices array")
    void testRfBridgeWithEmptySubDevices() {
        // Setup
        setupRfBridgeWithEmptySubDevices();

        // Execute
        assertDoesNotThrow(() -> {
            runDiscoveryMethod();
        }, "Should handle RF bridge with empty sub-devices gracefully");
    }

    @Test
    @DisplayName("Should handle Zigbee bridge with null sub-devices")
    void testZigbeeBridgeWithNullSubDevices() {
        // Setup
        setupZigbeeBridgeWithNullSubDevices();

        // Execute
        assertDoesNotThrow(() -> {
            runDiscoveryMethod();
        }, "Should handle Zigbee bridge with null sub-devices gracefully");
    }

    @Test
    @DisplayName("Should handle Zigbee bridge with malformed sub-devices")
    void testZigbeeBridgeWithMalformedSubDevices() {
        // Setup
        setupZigbeeBridgeWithMalformedSubDevices();

        // Execute
        assertDoesNotThrow(() -> {
            runDiscoveryMethod();
        }, "Should handle Zigbee bridge with malformed sub-devices gracefully");
    }

    @Test
    @DisplayName("Should handle bridge handlers returning null")
    void testBridgeHandlersReturningNull() {
        // Setup
        List<Thing> things = new ArrayList<>();

        Thing rfBridge = mock(Thing.class);
        ThingTypeUID rfBridgeTypeUID = new ThingTypeUID(SonoffBindingConstants.BINDING_ID, "28");
        when(rfBridge.getThingTypeUID()).thenReturn(rfBridgeTypeUID);
        when(rfBridge.getHandler()).thenReturn(null); // Null handler
        things.add(rfBridge);

        when(mockAccountThing.getThings()).thenReturn(things);

        // Execute
        assertDoesNotThrow(() -> {
            runDiscoveryMethod();
        }, "Should handle null bridge handlers gracefully");
    }

    @Test
    @DisplayName("Should handle things with null configuration")
    void testThingsWithNullConfiguration() throws Exception {
        // Setup
        Thing thingWithNullConfig = mock(Thing.class);
        when(thingWithNullConfig.getConfiguration()).thenReturn(null);

        List<Thing> things = List.of(thingWithNullConfig);

        when(mockApiConnection.createCache()).thenReturn(createSimpleDeviceResponse());

        try (MockedStatic<OpenHAB> mockedOpenHAB = mockStatic(OpenHAB.class)) {
            mockedOpenHAB.when(OpenHAB::getUserDataFolder).thenReturn(tempDir.toString());

            // Execute
            assertDoesNotThrow(() -> {
                discoveryService.createCache(things);
            }, "Should handle things with null configuration gracefully");
        }
    }

    @Test
    @DisplayName("Should handle things with missing deviceid in configuration")
    void testThingsWithMissingDeviceId() throws Exception {
        // Setup
        Thing thingWithoutDeviceId = mock(Thing.class);
        org.openhab.core.config.core.Configuration config = new org.openhab.core.config.core.Configuration(
                Map.of("name", "Test Thing"));
        when(thingWithoutDeviceId.getConfiguration()).thenReturn(config);

        List<Thing> things = List.of(thingWithoutDeviceId);

        when(mockApiConnection.createCache()).thenReturn(createSimpleDeviceResponse());

        try (MockedStatic<OpenHAB> mockedOpenHAB = mockStatic(OpenHAB.class)) {
            mockedOpenHAB.when(OpenHAB::getUserDataFolder).thenReturn(tempDir.toString());

            // Execute
            assertDoesNotThrow(() -> {
                discoveryService.createCache(things);
            }, "Should handle things with missing deviceid gracefully");
        }
    }

    @Test
    @DisplayName("Should handle cache directory creation failure")
    void testCacheDirectoryCreationFailure() throws Exception {
        // Setup - use a path that cannot be created (e.g., under a file)
        Path invalidPath = tempDir.resolve("file.txt");
        Files.write(invalidPath, "content".getBytes());

        when(mockApiConnection.createCache()).thenReturn(createSimpleDeviceResponse());

        try (MockedStatic<OpenHAB> mockedOpenHAB = mockStatic(OpenHAB.class)) {
            mockedOpenHAB.when(OpenHAB::getUserDataFolder).thenReturn(invalidPath.toString());

            // Execute
            assertDoesNotThrow(() -> {
                discoveryService.createCache(new ArrayList<>());
                // Should handle gracefully even if cache directory cannot be created
            }, "Should handle cache directory creation failure gracefully");
        }
    }

    @Test
    @DisplayName("Should handle extremely large JSON responses")
    void testExtremelyLargeJsonResponse() throws Exception {
        // Setup - create a very large JSON response
        StringBuilder largeResponse = new StringBuilder("{\"data\":{\"thingList\":[");
        for (int i = 0; i < 1000; i++) {
            if (i > 0)
                largeResponse.append(",");
            largeResponse.append(createDeviceJson("device" + i, "Device " + i, 1));
        }
        largeResponse.append("]}}");

        when(mockApiConnection.createCache()).thenReturn(largeResponse.toString());

        try (MockedStatic<OpenHAB> mockedOpenHAB = mockStatic(OpenHAB.class)) {
            mockedOpenHAB.when(OpenHAB::getUserDataFolder).thenReturn(tempDir.toString());

            // Execute
            List<JsonObject> devices = discoveryService.createCache(new ArrayList<>());

            // Verify
            assertEquals(1000, devices.size(), "Should handle large JSON responses");
            verify(mockAccountHandler, times(1000)).addState(anyString());
        }
    }

    @Test
    @DisplayName("Should handle JSON with deeply nested structures")
    void testDeeplyNestedJson() throws Exception {
        // Setup
        String deeplyNestedResponse = "{" + "\"data\": {" + "\"thingList\": [" + "{" + "\"itemType\": 1,"
                + "\"itemData\": {" + "\"deviceid\": \"nested-device\"," + "\"name\": \"Nested Device\","
                + "\"extra\": {" + "\"uiid\": 1," + "\"nested\": {" + "\"level1\": {" + "\"level2\": {"
                + "\"level3\": \"deep value\"" + "}" + "}" + "}" + "}," + "\"params\": {" + "\"complex\": {"
                + "\"array\": [1, 2, 3]," + "\"object\": {\"key\": \"value\"}" + "}" + "}" + "}" + "}" + "]}}";

        when(mockApiConnection.createCache()).thenReturn(deeplyNestedResponse);

        try (MockedStatic<OpenHAB> mockedOpenHAB = mockStatic(OpenHAB.class)) {
            mockedOpenHAB.when(OpenHAB::getUserDataFolder).thenReturn(tempDir.toString());

            // Execute
            assertDoesNotThrow(() -> {
                List<JsonObject> devices = discoveryService.createCache(new ArrayList<>());
                assertEquals(1, devices.size(), "Should handle deeply nested JSON");
            }, "Should handle deeply nested JSON gracefully");
        }
    }

    @Test
    @DisplayName("Should handle concurrent discovery calls")
    void testConcurrentDiscoveryCalls() throws Exception {
        // Setup
        when(mockApiConnection.createCache()).thenReturn(createSimpleDeviceResponse());

        try (MockedStatic<OpenHAB> mockedOpenHAB = mockStatic(OpenHAB.class)) {
            mockedOpenHAB.when(OpenHAB::getUserDataFolder).thenReturn(tempDir.toString());

            // Execute multiple concurrent calls
            List<Thread> threads = new ArrayList<>();
            List<Exception> exceptions = new ArrayList<>();

            for (int i = 0; i < 5; i++) {
                Thread thread = new Thread(() -> {
                    try {
                        discoveryService.createCache(new ArrayList<>());
                    } catch (Exception e) {
                        synchronized (exceptions) {
                            exceptions.add(e);
                        }
                    }
                });
                threads.add(thread);
                thread.start();
            }

            // Wait for all threads to complete
            for (Thread thread : threads) {
                thread.join(5000); // 5 second timeout
            }

            // Verify
            assertTrue(exceptions.isEmpty(), "Should handle concurrent calls without exceptions");
        }
    }

    // Helper methods

    private void setupRfBridgeWithNullSubDevices() {
        List<Thing> things = new ArrayList<>();
        Bridge rfBridge = createMockRfBridge();
        things.add(rfBridge);

        when(mockRfBridgeHandler.getSubDevices()).thenReturn(null);
        when(mockAccountThing.getThings()).thenReturn(things);
    }

    private void setupRfBridgeWithEmptySubDevices() {
        List<Thing> things = new ArrayList<>();
        Bridge rfBridge = createMockRfBridge();
        things.add(rfBridge);

        when(mockRfBridgeHandler.getSubDevices()).thenReturn(new JsonArray());
        when(mockAccountThing.getThings()).thenReturn(things);
    }

    private void setupZigbeeBridgeWithNullSubDevices() {
        List<Thing> things = new ArrayList<>();
        Bridge zigbeeBridge = createMockZigbeeBridge();
        things.add(zigbeeBridge);

        when(mockZigbeeBridgeHandler.getSubDevices()).thenReturn(null);
        when(mockAccountThing.getThings()).thenReturn(things);
    }

    private void setupZigbeeBridgeWithMalformedSubDevices() {
        List<Thing> things = new ArrayList<>();
        Bridge zigbeeBridge = createMockZigbeeBridge();
        things.add(zigbeeBridge);

        // Create malformed sub-device (missing required fields)
        JsonArray malformedSubDevices = new JsonArray();
        JsonObject malformedDevice = new JsonObject();
        malformedDevice.addProperty("name", "Malformed Device");
        // Missing deviceid and uiid
        malformedSubDevices.add(malformedDevice);

        when(mockZigbeeBridgeHandler.getSubDevices()).thenReturn(malformedSubDevices);
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

    private void runDiscoveryMethod() {
        try {
            java.lang.reflect.Method discoverMethod = discoveryService.getClass().getDeclaredMethod("discover");
            discoverMethod.setAccessible(true);
            discoverMethod.invoke(discoveryService);
        } catch (Exception e) {
            fail("Failed to run discovery method: " + e.getMessage());
        }
    }

    private String createSimpleDeviceResponse() {
        return "{" + "\"data\": {" + "\"thingList\": [" + createDeviceJson("simple-device", "Simple Device", 1) + "]}}";
    }

    private String createDeviceJson(String deviceId, String name, int uiid) {
        return "{" + "\"itemType\": 1," + "\"itemData\": {" + "\"deviceid\": \"" + deviceId + "\"," + "\"name\": \""
                + name + "\"," + "\"brandName\": \"Sonoff\"," + "\"productModel\": \"TEST\"," + "\"devicekey\": \"key-"
                + deviceId + "\"," + "\"apikey\": \"api-" + deviceId + "\"," + "\"extra\": {\"uiid\": " + uiid + "},"
                + "\"params\": {\"fwVersion\": \"1.0.0\"}" + "}" + "}";
    }
}
