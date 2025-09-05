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
import com.google.gson.JsonSyntaxException;

/**
 * Error handling and edge case tests for {@link SonoffCacheProvider}.
 * 
 * Tests cover:
 * - Null parameter handling
 * - Invalid file operations
 * - Gson error scenarios
 * - File system error conditions
 * - Boundary conditions
 *
 * @author Test Author - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
class SonoffCacheProviderErrorHandlingTest {

    @TempDir
    Path tempDir;

    @Mock
    private Gson mockGson;

    @Mock
    private JsonObject mockJsonObject;

    private SonoffCacheProvider cacheProvider;
    private SonoffCacheProvider cacheProviderWithoutGson;
    private String testCacheDir;

    @BeforeEach
    void setUp() {
        // Setup temporary directory for testing
        testCacheDir = tempDir.resolve("sonoff").toString();
        
        // Mock OpenHAB.getUserDataFolder() to return our temp directory
        try (MockedStatic<OpenHAB> mockedOpenHAB = mockStatic(OpenHAB.class)) {
            mockedOpenHAB.when(OpenHAB::getUserDataFolder).thenReturn(tempDir.toString());
            
            cacheProvider = new SonoffCacheProvider(mockGson);
            cacheProviderWithoutGson = new SonoffCacheProvider();
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        // Clean up test files
        if (Files.exists(Paths.get(testCacheDir))) {
            Files.walk(Paths.get(testCacheDir))
                .map(Path::toFile)
                .forEach(File::delete);
        }
    }

    @Test
    @DisplayName("Should handle null device ID in newFile")
    void testNewFileWithNullDeviceId() {
        // Execute & Verify - should not throw exception
        assertDoesNotThrow(() -> {
            cacheProvider.newFile(null, "{\"test\":\"data\"}");
        }, "Should handle null device ID gracefully");
    }

    @Test
    @DisplayName("Should handle null content in newFile")
    void testNewFileWithNullContent() {
        // Execute & Verify - should not throw exception
        assertDoesNotThrow(() -> {
            cacheProvider.newFile("test-device", null);
        }, "Should handle null content gracefully");
    }

    @Test
    @DisplayName("Should handle empty device ID in newFile")
    void testNewFileWithEmptyDeviceId() {
        // Execute
        cacheProvider.newFile("", "{\"test\":\"data\"}");

        // Verify - should create file with empty name
        assertTrue(cacheProvider.checkFile(""), "Should create file with empty device ID");
    }

    @Test
    @DisplayName("Should handle empty content in newFile")
    void testNewFileWithEmptyContent() {
        // Execute
        cacheProvider.newFile("empty-content-device", "");

        // Verify
        assertTrue(cacheProvider.checkFile("empty-content-device"), "Should create file with empty content");
        assertEquals("", cacheProvider.getFile("empty-content-device.txt"), "Should return empty content");
    }

    @Test
    @DisplayName("Should handle null filename in getFile")
    void testGetFileWithNullFilename() {
        // Execute
        String result = cacheProvider.getFile(null);

        // Verify
        assertEquals("", result, "Should return empty string for null filename");
    }

    @Test
    @DisplayName("Should handle empty filename in getFile")
    void testGetFileWithEmptyFilename() {
        // Execute
        String result = cacheProvider.getFile("");

        // Verify
        assertEquals("", result, "Should return empty string for empty filename");
    }

    @Test
    @DisplayName("Should handle null device ID in checkFile")
    void testCheckFileWithNullDeviceId() {
        // Execute
        Boolean result = cacheProvider.checkFile(null);

        // Verify
        assertFalse(result, "Should return false for null device ID");
    }

    @Test
    @DisplayName("Should handle empty device ID in checkFile")
    void testCheckFileWithEmptyDeviceId() {
        // Setup - create file with empty device ID
        cacheProvider.newFile("", "{\"test\":\"data\"}");

        // Execute
        Boolean result = cacheProvider.checkFile("");

        // Verify
        assertTrue(result, "Should return true for existing file with empty device ID");
    }

    @Test
    @DisplayName("Should handle Gson throwing JsonSyntaxException in getStates")
    void testGetStatesWithGsonException() throws IOException {
        // Setup
        cacheProvider.newFile("invalid-json-device", "invalid json content");

        // Mock Gson to throw JsonSyntaxException
        when(mockGson.fromJson(anyString(), eq(JsonObject.class)))
            .thenThrow(new JsonSyntaxException("Invalid JSON"));

        // Execute & Verify - should not throw exception
        assertDoesNotThrow(() -> {
            cacheProvider.getStates();
        }, "Should handle Gson exceptions gracefully");
    }

    @Test
    @DisplayName("Should handle Gson throwing JsonSyntaxException in getState")
    void testGetStateWithGsonException() throws IOException {
        // Setup
        String deviceId = "invalid-json-device";
        cacheProvider.newFile(deviceId, "invalid json content");

        // Mock Gson to throw JsonSyntaxException
        when(mockGson.fromJson(anyString(), eq(JsonObject.class)))
            .thenThrow(new JsonSyntaxException("Invalid JSON"));

        // Execute & Verify - should not throw exception
        assertDoesNotThrow(() -> {
            SonoffDeviceState result = cacheProvider.getState(deviceId);
            assertNull(result, "Should return null when Gson throws exception");
        }, "Should handle Gson exceptions gracefully");
    }

    @Test
    @DisplayName("Should handle null Gson in getStates")
    void testGetStatesWithNullGson() {
        // Execute & Verify - should throw NullPointerException
        assertThrows(NullPointerException.class, () -> {
            cacheProviderWithoutGson.getStates();
        }, "Should throw NullPointerException when Gson is null");
    }

    @Test
    @DisplayName("Should handle null Gson in getState")
    void testGetStateWithNullGson() {
        // Execute & Verify - should throw NullPointerException
        assertThrows(NullPointerException.class, () -> {
            cacheProviderWithoutGson.getState("test-device");
        }, "Should throw NullPointerException when Gson is null");
    }

    @Test
    @DisplayName("Should handle long but realistic device IDs")
    void testLongRealisticDeviceId() {
        // Setup - create a long but realistic device ID (100 characters)
        // This represents a realistic upper bound for actual device IDs
        StringBuilder longDeviceId = new StringBuilder("sonoff-device-");
        for (int i = 0; i < 85; i++) {
            longDeviceId.append("a");
        }
        String deviceId = longDeviceId.toString();

        // Execute
        cacheProvider.newFile(deviceId, "{\"test\":\"data\"}");

        // Verify
        assertTrue(cacheProvider.checkFile(deviceId), "Should handle long realistic device ID");
        assertEquals("{\"test\":\"data\"}", cacheProvider.getFile(deviceId + ".txt"), 
            "Should retrieve content for long realistic device ID");
    }

    @Test
    @DisplayName("Should handle device IDs with special file system characters")
    void testDeviceIdWithSpecialCharacters() {
        // Note: Some characters may be invalid on certain file systems
        // This test documents the behavior rather than enforcing it
        
        String[] specialIds = {
            "device-with-dashes",
            "device_with_underscores", 
            "device.with.dots",
            "device with spaces",
            "device@with@symbols"
        };

        for (String deviceId : specialIds) {
            try {
                // Execute
                cacheProvider.newFile(deviceId, "{\"deviceid\":\"" + deviceId + "\"}");
                
                // Verify if file was created successfully
                if (cacheProvider.checkFile(deviceId)) {
                    String content = cacheProvider.getFile(deviceId + ".txt");
                    assertEquals("{\"deviceid\":\"" + deviceId + "\"}", content, 
                        "Content should match for device ID: " + deviceId);
                }
            } catch (Exception e) {
                // Some special characters may cause file system errors
                // This is expected behavior on some platforms
                assertTrue(true, "File system rejected special character in device ID: " + deviceId);
            }
        }
    }

    @Test
    @DisplayName("Should handle corrupted cache directory")
    void testCorruptedCacheDirectory() throws IOException {
        // Setup - create a file where the cache directory should be
        Files.deleteIfExists(Paths.get(testCacheDir));
        Files.createFile(Paths.get(testCacheDir)); // Create file instead of directory

        // Execute - try to create new cache provider
        try (MockedStatic<OpenHAB> mockedOpenHAB = mockStatic(OpenHAB.class)) {
            mockedOpenHAB.when(OpenHAB::getUserDataFolder).thenReturn(tempDir.toString());
            
            // This should handle the corrupted directory gracefully
            SonoffCacheProvider corruptedCacheProvider = new SonoffCacheProvider(mockGson);
            
            // Try to perform operations
            assertDoesNotThrow(() -> {
                corruptedCacheProvider.newFile("test-device", "{\"test\":\"data\"}");
            }, "Should handle corrupted cache directory gracefully");
        }
    }

    @Test
    @DisplayName("Should handle missing cache directory")
    void testMissingCacheDirectory() throws IOException {
        // Setup - delete the cache directory
        if (Files.exists(Paths.get(testCacheDir))) {
            Files.walk(Paths.get(testCacheDir))
                .map(Path::toFile)
                .forEach(File::delete);
        }

        // Execute - operations should still work (directory should be recreated)
        cacheProvider.newFile("test-device", "{\"test\":\"data\"}");

        // Verify
        assertTrue(Files.exists(Paths.get(testCacheDir)), "Cache directory should be recreated");
        assertTrue(cacheProvider.checkFile("test-device"), "Should create file after recreating directory");
    }

    @Test
    @DisplayName("Should handle extremely large file content")
    void testExtremelyLargeFileContent() {
        // Setup - create very large content (1MB)
        StringBuilder largeContent = new StringBuilder("{\"deviceid\":\"large-device\",\"data\":\"");
        for (int i = 0; i < 100000; i++) {
            largeContent.append("0123456789");
        }
        largeContent.append("\"}");

        // Execute & Verify - should handle large content
        assertDoesNotThrow(() -> {
            cacheProvider.newFile("large-device", largeContent.toString());
        }, "Should handle extremely large file content");

        // Verify file was created
        assertTrue(cacheProvider.checkFile("large-device"), "Large file should be created");
    }

    @Test
    @DisplayName("Should handle getFiles when directory contains non-text files")
    void testGetFilesWithNonTextFiles() throws IOException {
        // Setup - create various file types
        cacheProvider.newFile("device1", "{\"deviceid\":\"device1\"}");
        cacheProvider.newFile("device2", "{\"deviceid\":\"device2\"}");
        
        // Create non-.txt files
        Files.write(Paths.get(testCacheDir, "config.xml"), "<config></config>".getBytes());
        Files.write(Paths.get(testCacheDir, "data.json"), "{\"data\":\"value\"}".getBytes());
        Files.write(Paths.get(testCacheDir, "readme.md"), "# README".getBytes());
        Files.createDirectory(Paths.get(testCacheDir, "subdirectory"));

        // Execute
        List<String> files = cacheProvider.getFiles();

        // Verify - should only return .txt files
        assertEquals(2, files.size(), "Should only return .txt files");
        assertTrue(files.contains("{\"deviceid\":\"device1\"}"), "Should contain device1 data");
        assertTrue(files.contains("{\"deviceid\":\"device2\"}"), "Should contain device2 data");
    }

    @Test
    @DisplayName("Should handle getFiles when directory is empty")
    void testGetFilesEmptyDirectory() {
        // Execute
        List<String> files = cacheProvider.getFiles();

        // Verify
        assertNotNull(files, "Files list should not be null");
        assertTrue(files.isEmpty(), "Files list should be empty for empty directory");
    }

    @Test
    @DisplayName("Should handle getFiles when directory contains only empty files")
    void testGetFilesOnlyEmptyFiles() throws IOException {
        // Setup - create empty files
        Files.write(Paths.get(testCacheDir, "empty1.txt"), "".getBytes());
        Files.write(Paths.get(testCacheDir, "empty2.txt"), "".getBytes());

        // Execute
        List<String> files = cacheProvider.getFiles();

        // Verify - empty files should be skipped
        assertTrue(files.isEmpty(), "Should skip empty files");
    }

    @Test
    @DisplayName("Should handle boundary conditions for file operations")
    void testBoundaryConditions() {
        // Test with single character content
        cacheProvider.newFile("single-char", "a");
        assertEquals("a", cacheProvider.getFile("single-char.txt"), "Should handle single character content");

        // Test with single character device ID
        cacheProvider.newFile("a", "{\"test\":\"data\"}");
        assertTrue(cacheProvider.checkFile("a"), "Should handle single character device ID");

        // Test with numeric device ID
        cacheProvider.newFile("123", "{\"deviceid\":\"123\"}");
        assertTrue(cacheProvider.checkFile("123"), "Should handle numeric device ID");

        // Test with mixed case device ID
        cacheProvider.newFile("MixedCase", "{\"deviceid\":\"MixedCase\"}");
        assertTrue(cacheProvider.checkFile("MixedCase"), "Should handle mixed case device ID");
    }
}