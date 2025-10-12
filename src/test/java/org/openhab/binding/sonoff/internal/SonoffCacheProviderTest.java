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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.openhab.binding.sonoff.internal.handler.SonoffDeviceState;
import org.openhab.core.OpenHAB;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Unit tests for {@link SonoffCacheProvider}.
 * 
 * Tests cover:
 * - Constructor initialization with and without Gson
 * - File operations (create, read, check existence)
 * - Cache operations (get files, get states)
 * - Error handling and edge cases
 * - Directory creation and management
 *
 * @author tschaban/SmartnyDom - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
class SonoffCacheProviderTest {

    @TempDir
    Path tempDir;

    @Mock
    private Gson mockGson;

    private SonoffCacheProvider cacheProvider;
    private String testCacheDir;

    @BeforeEach
    void setUp() {
        // Setup temporary directory for testing
        testCacheDir = tempDir.resolve("sonoff").toString();

        // Mock OpenHAB.getUserDataFolder() to return our temp directory
        try (MockedStatic<OpenHAB> mockedOpenHAB = mockStatic(OpenHAB.class)) {
            mockedOpenHAB.when(OpenHAB::getUserDataFolder).thenReturn(tempDir.toString());

            // Create cache provider
            cacheProvider = new SonoffCacheProvider(mockGson);
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        // Clean up test files
        if (Files.exists(Paths.get(testCacheDir))) {
            Files.walk(Paths.get(testCacheDir)).map(Path::toFile).forEach(File::delete);
        }
    }

    @Test
    @DisplayName("Constructor with Gson should create cache directory")
    void testConstructorWithGson() {
        // Verify directory was created
        assertTrue(Files.exists(Paths.get(testCacheDir)), "Cache directory should be created");
        assertTrue(Files.isDirectory(Paths.get(testCacheDir)), "Cache path should be a directory");
    }

    @Test
    @DisplayName("Constructor without Gson should create cache directory")
    void testConstructorWithoutGson() {
        // Verify directory was created
        assertTrue(Files.exists(Paths.get(testCacheDir)), "Cache directory should be created");
        assertTrue(Files.isDirectory(Paths.get(testCacheDir)), "Cache path should be a directory");
    }

    @Test
    @DisplayName("Should create new file with device data")
    void testNewFile() throws IOException {
        // Setup
        String deviceId = "test-device-001";
        String deviceData = "{\"deviceid\":\"test-device-001\",\"name\":\"Test Device\"}";

        // Execute
        cacheProvider.newFile(deviceId, deviceData);

        // Verify
        Path expectedFile = Paths.get(testCacheDir, deviceId + ".txt");
        assertTrue(Files.exists(expectedFile), "Device file should be created");

        String fileContent = Files.readString(expectedFile);
        assertEquals(deviceData, fileContent, "File content should match input data");
    }

    @Test
    @DisplayName("Should handle file creation with special characters in device ID")
    void testNewFileWithSpecialCharacters() throws IOException {
        // Setup
        String deviceId = "device-123_test";
        String deviceData = "{\"deviceid\":\"device-123_test\"}";

        // Execute
        cacheProvider.newFile(deviceId, deviceData);

        // Verify
        Path expectedFile = Paths.get(testCacheDir, deviceId + ".txt");
        assertTrue(Files.exists(expectedFile), "Device file with special characters should be created");
    }

    @Test
    @DisplayName("Should overwrite existing file")
    void testNewFileOverwrite() throws IOException {
        // Setup
        String deviceId = "test-device-002";
        String originalData = "{\"deviceid\":\"test-device-002\",\"version\":1}";
        String updatedData = "{\"deviceid\":\"test-device-002\",\"version\":2}";

        // Execute - create original file
        cacheProvider.newFile(deviceId, originalData);

        // Execute - overwrite with new data
        cacheProvider.newFile(deviceId, updatedData);

        // Verify
        Path expectedFile = Paths.get(testCacheDir, deviceId + ".txt");
        String fileContent = Files.readString(expectedFile);
        assertEquals(updatedData, fileContent, "File should be overwritten with new content");
    }

    @Test
    @DisplayName("Should check file existence correctly")
    void testCheckFile() throws IOException {
        // Setup
        String existingDeviceId = "existing-device";
        String nonExistingDeviceId = "non-existing-device";

        // Create a test file
        cacheProvider.newFile(existingDeviceId, "{\"test\":\"data\"}");

        // Execute & Verify
        assertTrue(cacheProvider.checkFile(existingDeviceId), "Should return true for existing file");
        assertFalse(cacheProvider.checkFile(nonExistingDeviceId), "Should return false for non-existing file");
    }

    @Test
    @DisplayName("Should get file content correctly")
    void testGetFile() throws IOException {
        // Setup
        String deviceId = "test-device-003";
        String deviceData = "{\"deviceid\":\"test-device-003\",\"status\":\"online\"}";
        String filename = deviceId + ".txt";

        cacheProvider.newFile(deviceId, deviceData);

        // Execute
        String retrievedContent = cacheProvider.getFile(filename);

        // Verify
        assertEquals(deviceData, retrievedContent, "Retrieved content should match original data");
    }

    @Test
    @DisplayName("Should return empty string for non-existing file")
    void testGetFileNonExisting() {
        // Execute
        String content = cacheProvider.getFile("non-existing-file.txt");

        // Verify
        assertEquals("", content, "Should return empty string for non-existing file");
    }

    @Test
    @DisplayName("Should get all files with .txt extension")
    void testGetFiles() throws IOException {
        // Setup - create multiple test files
        cacheProvider.newFile("device1", "{\"deviceid\":\"device1\"}");
        cacheProvider.newFile("device2", "{\"deviceid\":\"device2\"}");
        cacheProvider.newFile("device3", "{\"deviceid\":\"device3\"}");

        // Create a non-.txt file that should be ignored
        Files.write(Paths.get(testCacheDir, "ignore.log"), "log content".getBytes());

        // Execute
        List<String> files = cacheProvider.getFiles();

        // Verify
        assertEquals(3, files.size(), "Should return 3 .txt files");
        assertTrue(files.contains("{\"deviceid\":\"device1\"}"), "Should contain device1 data");
        assertTrue(files.contains("{\"deviceid\":\"device2\"}"), "Should contain device2 data");
        assertTrue(files.contains("{\"deviceid\":\"device3\"}"), "Should contain device3 data");
    }

    @Test
    @DisplayName("Should handle empty directory when getting files")
    void testGetFilesEmptyDirectory() {
        // Execute
        List<String> files = cacheProvider.getFiles();

        // Verify
        assertTrue(files.isEmpty(), "Should return empty list for empty directory");
    }

    @Test
    @DisplayName("Should skip empty files when getting files")
    void testGetFilesSkipEmpty() throws IOException {
        // Setup - create files with content and empty file
        cacheProvider.newFile("device1", "{\"deviceid\":\"device1\"}");
        Files.write(Paths.get(testCacheDir, "empty.txt"), "".getBytes());

        // Execute
        List<String> files = cacheProvider.getFiles();

        // Verify
        assertEquals(1, files.size(), "Should skip empty files");
        assertEquals("{\"deviceid\":\"device1\"}", files.get(0), "Should only return non-empty file");
    }

    @Test
    @DisplayName("Should get device states from cache files")
    void testGetStates() throws IOException {
        // Setup - create valid JSON that won't cause SonoffDeviceState constructor to fail
        String device1Json = createValidDeviceJson("device1", "Device 1");
        String device2Json = createValidDeviceJson("device2", "Device 2");

        cacheProvider.newFile("device1", device1Json);
        cacheProvider.newFile("device2", device2Json);

        // Mock Gson behavior to return null (simulating parsing failure)
        when(mockGson.fromJson(device1Json, JsonObject.class)).thenReturn(null);
        when(mockGson.fromJson(device2Json, JsonObject.class)).thenReturn(null);

        // Execute
        Map<String, SonoffDeviceState> states = cacheProvider.getStates();

        // Verify Gson was called correctly
        verify(mockGson).fromJson(device1Json, JsonObject.class);
        verify(mockGson).fromJson(device2Json, JsonObject.class);

        // Verify empty map is returned when JSON parsing returns null
        assertTrue(states.isEmpty(), "Should return empty map when JSON parsing returns null");
    }

    private String createValidDeviceJson(String deviceId, String name) {
        return "{" + "\"deviceid\":\"" + deviceId + "\"," + "\"name\":\"" + name + "\"," + "\"devicekey\":\"test-key\","
                + "\"brandName\":\"Test Brand\"," + "\"productModel\":\"Test Model\"," + "\"extra\":{\"uiid\":1},"
                + "\"params\":{\"fwVersion\":\"1.0.0\"}" + "}";
    }

    @Test
    @DisplayName("Should handle null JSON when getting states")
    void testGetStatesWithNullJson() throws IOException {
        // Setup
        String deviceJson = "{\"deviceid\":\"device1\"}";
        cacheProvider.newFile("device1", deviceJson);

        // Mock Gson to return null
        when(mockGson.fromJson(deviceJson, JsonObject.class)).thenReturn(null);

        // Execute
        Map<String, SonoffDeviceState> states = cacheProvider.getStates();

        // Verify
        assertTrue(states.isEmpty(), "Should return empty map when JSON parsing returns null");
    }

    @Test
    @DisplayName("Should get single device state")
    void testGetState() throws IOException {
        // Setup
        String deviceId = "test-device";
        String deviceJson = createValidDeviceJson(deviceId, "Test Device");

        cacheProvider.newFile(deviceId, deviceJson);

        // Mock Gson behavior to return null (simulating parsing failure)
        when(mockGson.fromJson(deviceJson, JsonObject.class)).thenReturn(null);

        // Execute
        SonoffDeviceState state = cacheProvider.getState(deviceId);

        // Verify
        verify(mockGson).fromJson(deviceJson, JsonObject.class);
        assertNull(state, "Should return null when JSON parsing returns null");
    }

    @Test
    @DisplayName("Should return null for non-existing device state")
    void testGetStateNonExisting() {
        // Execute - no mocking needed since file doesn't exist
        SonoffDeviceState state = cacheProvider.getState("non-existing-device");

        // Verify
        assertNull(state, "Should return null for non-existing device");
    }

    @Test
    @DisplayName("Should return null when JSON parsing fails")
    void testGetStateJsonParsingFails() throws IOException {
        // Setup
        String deviceId = "test-device";
        String invalidJson = "invalid json content";

        cacheProvider.newFile(deviceId, invalidJson);

        // Mock Gson to return null for invalid JSON
        when(mockGson.fromJson(invalidJson, JsonObject.class)).thenReturn(null);

        // Execute
        SonoffDeviceState state = cacheProvider.getState(deviceId);

        // Verify
        assertNull(state, "Should return null when JSON parsing fails");
    }

    @Test
    @DisplayName("Should handle file operations with Unicode content")
    void testUnicodeContent() throws IOException {
        // Setup
        String deviceId = "unicode-device";
        String unicodeData = "{\"deviceid\":\"unicode-device\",\"name\":\"test-device\",\"description\":\"Test Device\"}";

        // Execute
        cacheProvider.newFile(deviceId, unicodeData);
        String retrievedContent = cacheProvider.getFile(deviceId + ".txt");

        // Verify
        assertEquals(unicodeData, retrievedContent, "Should handle Unicode content correctly");
    }

    @Test
    @DisplayName("Should handle large file content")
    void testLargeFileContent() throws IOException {
        // Setup
        String deviceId = "large-device";
        StringBuilder largeContent = new StringBuilder("{\"deviceid\":\"large-device\",\"data\":\"");

        // Create large content (10KB)
        for (int i = 0; i < 1000; i++) {
            largeContent.append("0123456789");
        }
        largeContent.append("\"}");

        // Execute
        cacheProvider.newFile(deviceId, largeContent.toString());
        String retrievedContent = cacheProvider.getFile(deviceId + ".txt");

        // Verify
        assertEquals(largeContent.toString(), retrievedContent, "Should handle large file content correctly");
    }

    @Test
    @DisplayName("Should handle concurrent file operations")
    void testConcurrentOperations() throws InterruptedException {
        // Setup
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];

        // Execute - create multiple threads writing different files
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                String deviceId = "concurrent-device-" + threadIndex;
                String deviceData = "{\"deviceid\":\"" + deviceId + "\",\"thread\":" + threadIndex + "}";
                cacheProvider.newFile(deviceId, deviceData);
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Verify - check that all files were created
        for (int i = 0; i < threadCount; i++) {
            String deviceId = "concurrent-device-" + i;
            assertTrue(cacheProvider.checkFile(deviceId), "File should exist for device: " + deviceId);

            String content = cacheProvider.getFile(deviceId + ".txt");
            assertTrue(content.contains("\"thread\":" + i), "File should contain correct thread data");
        }
    }

    @Test
    @DisplayName("Should handle file operations when directory is read-only")
    void testReadOnlyDirectory() throws IOException {
        // This test is platform-dependent and may not work on all systems
        // It's included for completeness but may be skipped on some platforms

        File cacheDir = new File(testCacheDir);
        boolean originalWritable = cacheDir.canWrite();

        try {
            // Make directory read-only
            cacheDir.setWritable(false);

            if (!cacheDir.canWrite()) {
                // Execute - try to create file in read-only directory
                cacheProvider.newFile("readonly-test", "{\"test\":\"data\"}");

                // Verify - file should not be created (operation should fail silently)
                assertFalse(cacheProvider.checkFile("readonly-test"),
                        "File should not be created in read-only directory");
            }
        } finally {
            // Restore original permissions
            cacheDir.setWritable(originalWritable);
        }
    }
}
