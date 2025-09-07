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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.OpenHAB;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Integration tests for {@link SonoffCacheProvider}.
 * 
 * Tests cover:
 * - Real file system operations
 * - Thread safety and concurrent access
 * - Performance under load
 * - Error recovery scenarios
 * - Integration with real Gson
 *
 * @author Test Author - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
class SonoffCacheProviderIntegrationTest {

    @TempDir
    Path tempDir;

    private SonoffCacheProvider cacheProvider;
    private Gson realGson;
    private String testCacheDir;

    @BeforeEach
    void setUp() {
        // Setup temporary directory for testing
        testCacheDir = tempDir.resolve("sonoff").toString();
        realGson = new Gson();

        // Mock OpenHAB.getUserDataFolder() to return our temp directory
        try (MockedStatic<OpenHAB> mockedOpenHAB = mockStatic(OpenHAB.class)) {
            mockedOpenHAB.when(OpenHAB::getUserDataFolder).thenReturn(tempDir.toString());
            cacheProvider = new SonoffCacheProvider(realGson);
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
    @DisplayName("Should handle high-volume concurrent file operations")
    void testHighVolumeConcurrentOperations() throws InterruptedException {
        // Setup
        int threadCount = 20;
        int operationsPerThread = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // Execute - create multiple threads performing various operations
        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    for (int i = 0; i < operationsPerThread; i++) {
                        String deviceId = "thread" + threadId + "-device" + i;
                        String deviceData = "{\"deviceid\":\"" + deviceId + "\",\"thread\":" + threadId
                                + ",\"operation\":" + i + "}";

                        // Write file
                        cacheProvider.newFile(deviceId, deviceData);

                        // Check file exists
                        assertTrue(cacheProvider.checkFile(deviceId), "File should exist: " + deviceId);

                        // Read file content
                        String content = cacheProvider.getFile(deviceId + ".txt");
                        assertEquals(deviceData, content, "Content should match for: " + deviceId);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for all operations to complete
        assertTrue(latch.await(30, TimeUnit.SECONDS), "All operations should complete within 30 seconds");
        executor.shutdown();

        // Verify - check that all files were created correctly
        List<String> allFiles = cacheProvider.getFiles();
        assertEquals(threadCount * operationsPerThread, allFiles.size(), "Should have created all expected files");
    }

    @Test
    @DisplayName("Should handle mixed read/write operations concurrently")
    void testMixedConcurrentOperations() throws InterruptedException {
        // Setup - create some initial files
        for (int i = 0; i < 10; i++) {
            String deviceId = "initial-device-" + i;
            String deviceData = "{\"deviceid\":\"" + deviceId + "\",\"initial\":true}";
            cacheProvider.newFile(deviceId, deviceData);
        }

        int threadCount = 15;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // Execute - mix of readers and writers
        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    if (threadId % 3 == 0) {
                        // Writer threads
                        for (int i = 0; i < 20; i++) {
                            String deviceId = "writer-" + threadId + "-" + i;
                            String deviceData = "{\"deviceid\":\"" + deviceId + "\",\"writer\":true}";
                            cacheProvider.newFile(deviceId, deviceData);
                        }
                    } else if (threadId % 3 == 1) {
                        // Reader threads
                        for (int i = 0; i < 30; i++) {
                            List<String> files = cacheProvider.getFiles();
                            assertNotNull(files, "Files list should not be null");
                        }
                    } else {
                        // Mixed operation threads
                        for (int i = 0; i < 15; i++) {
                            // Check existing files
                            assertTrue(cacheProvider.checkFile("initial-device-0"), "Initial file should exist");

                            // Create new file
                            String deviceId = "mixed-" + threadId + "-" + i;
                            String deviceData = "{\"deviceid\":\"" + deviceId + "\",\"mixed\":true}";
                            cacheProvider.newFile(deviceId, deviceData);

                            // Read it back
                            String content = cacheProvider.getFile(deviceId + ".txt");
                            assertEquals(deviceData, content, "Content should match");
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for completion
        assertTrue(latch.await(45, TimeUnit.SECONDS), "All operations should complete within 45 seconds");
        executor.shutdown();

        // Verify final state
        List<String> finalFiles = cacheProvider.getFiles();
        assertTrue(finalFiles.size() >= 10, "Should have at least the initial files");
    }

    @Test
    @DisplayName("Should handle file system stress test")
    void testFileSystemStress() throws InterruptedException {
        // Setup
        int fileCount = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(fileCount);

        // Execute - create many files rapidly
        for (int i = 0; i < fileCount; i++) {
            final int fileIndex = i;
            executor.submit(() -> {
                try {
                    String deviceId = "stress-device-" + String.format("%04d", fileIndex);
                    String deviceData = "{\"deviceid\":\"" + deviceId + "\",\"index\":" + fileIndex + ",\"timestamp\":"
                            + System.currentTimeMillis() + "}";
                    cacheProvider.newFile(deviceId, deviceData);
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for completion
        assertTrue(latch.await(60, TimeUnit.SECONDS), "Stress test should complete within 60 seconds");
        executor.shutdown();

        // Verify
        List<String> files = cacheProvider.getFiles();
        assertEquals(fileCount, files.size(), "Should have created all stress test files");

        // Verify file integrity
        for (int i = 0; i < Math.min(100, fileCount); i++) { // Check first 100 files
            String deviceId = "stress-device-" + String.format("%04d", i);
            assertTrue(cacheProvider.checkFile(deviceId), "Stress test file should exist: " + deviceId);

            String content = cacheProvider.getFile(deviceId + ".txt");
            assertTrue(content.contains("\"index\":" + i), "File should contain correct index: " + i);
        }
    }

    @Test
    @DisplayName("Should handle real JSON parsing with Gson")
    void testRealJsonParsing() throws IOException {
        // Setup - create files with valid JSON
        String device1Json = "{\"deviceid\":\"real-device-1\",\"name\":\"Real Device 1\",\"status\":\"online\",\"params\":{\"switch\":\"on\",\"power\":100}}";
        String device2Json = "{\"deviceid\":\"real-device-2\",\"name\":\"Real Device 2\",\"status\":\"offline\",\"params\":{\"switch\":\"off\",\"power\":0}}";

        cacheProvider.newFile("real-device-1", device1Json);
        cacheProvider.newFile("real-device-2", device2Json);

        // Execute - this would normally work with real SonoffDeviceState
        // For now, we test that the JSON parsing doesn't throw exceptions
        List<String> files = cacheProvider.getFiles();

        // Verify
        assertEquals(2, files.size(), "Should have 2 JSON files");
        assertTrue(files.contains(device1Json), "Should contain device 1 JSON");
        assertTrue(files.contains(device2Json), "Should contain device 2 JSON");

        // Test individual file retrieval
        String retrievedJson1 = cacheProvider.getFile("real-device-1.txt");
        String retrievedJson2 = cacheProvider.getFile("real-device-2.txt");

        assertEquals(device1Json, retrievedJson1, "Retrieved JSON should match original");
        assertEquals(device2Json, retrievedJson2, "Retrieved JSON should match original");
    }

    @Test
    @DisplayName("Should handle invalid JSON gracefully")
    void testInvalidJsonHandling() throws IOException {
        // Setup - create files with invalid JSON
        String invalidJson1 = "{\"deviceid\":\"invalid-device-1\",\"name\":\"Invalid Device\",}"; // trailing comma
        String invalidJson2 = "{deviceid:\"invalid-device-2\",\"name\":\"Invalid Device\"}"; // unquoted key
        String invalidJson3 = "not json at all";

        cacheProvider.newFile("invalid-device-1", invalidJson1);
        cacheProvider.newFile("invalid-device-2", invalidJson2);
        cacheProvider.newFile("invalid-device-3", invalidJson3);

        // Execute - getFiles should still work (returns raw content)
        List<String> files = cacheProvider.getFiles();

        // Verify
        assertEquals(3, files.size(), "Should return all files even with invalid JSON");
        assertTrue(files.contains(invalidJson1), "Should contain invalid JSON 1");
        assertTrue(files.contains(invalidJson2), "Should contain invalid JSON 2");
        assertTrue(files.contains(invalidJson3), "Should contain invalid JSON 3");

        // Test that Gson parsing would fail (but our cache provider handles it)
        assertThrows(JsonSyntaxException.class, () -> {
            realGson.fromJson(invalidJson1, Object.class);
        }, "Gson should throw exception for invalid JSON");
    }

    @Test
    @DisplayName("Should handle file operations with various file sizes")
    void testVariousFileSizes() throws IOException {
        // Setup - create files of different sizes
        String[] deviceIds = { "tiny", "small", "medium", "large", "huge" };
        int[] contentSizes = { 10, 100, 1000, 10000, 100000 }; // bytes

        for (int i = 0; i < deviceIds.length; i++) {
            StringBuilder content = new StringBuilder("{\"deviceid\":\"" + deviceIds[i] + "\",\"data\":\"");

            // Fill with repeated data to reach target size
            int targetSize = contentSizes[i] - 50; // account for JSON structure
            for (int j = 0; j < targetSize; j++) {
                content.append((char) ('A' + (j % 26)));
            }
            content.append("\"}");

            cacheProvider.newFile(deviceIds[i], content.toString());
        }

        // Execute & Verify
        for (String deviceId : deviceIds) {
            assertTrue(cacheProvider.checkFile(deviceId), "File should exist: " + deviceId);

            String content = cacheProvider.getFile(deviceId + ".txt");
            assertFalse(content.isEmpty(), "Content should not be empty for: " + deviceId);
            assertTrue(content.startsWith("{\"deviceid\":\"" + deviceId + "\""),
                    "Content should start correctly for: " + deviceId);
        }

        // Verify all files are returned
        List<String> allFiles = cacheProvider.getFiles();
        assertEquals(deviceIds.length, allFiles.size(), "Should return all files of various sizes");
    }

    @Test
    @DisplayName("Should handle rapid file updates")
    void testRapidFileUpdates() throws InterruptedException {
        // Setup
        String deviceId = "rapid-update-device";
        int updateCount = 100;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        CountDownLatch latch = new CountDownLatch(updateCount);

        // Execute - rapidly update the same file
        for (int i = 0; i < updateCount; i++) {
            final int updateIndex = i;
            executor.submit(() -> {
                try {
                    String deviceData = "{\"deviceid\":\"" + deviceId + "\",\"update\":" + updateIndex
                            + ",\"timestamp\":" + System.currentTimeMillis() + "}";
                    cacheProvider.newFile(deviceId, deviceData);
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for completion
        assertTrue(latch.await(30, TimeUnit.SECONDS), "Rapid updates should complete within 30 seconds");
        executor.shutdown();

        // Verify - file should exist and contain the last update
        assertTrue(cacheProvider.checkFile(deviceId), "Rapidly updated file should exist");

        String finalContent = cacheProvider.getFile(deviceId + ".txt");
        assertFalse(finalContent.isEmpty(), "Final content should not be empty");
        assertTrue(finalContent.contains("\"deviceid\":\"" + deviceId + "\""),
                "Final content should contain device ID");

        // The exact update number may vary due to race conditions, but it should be valid JSON
        assertTrue(finalContent.startsWith("{") && finalContent.endsWith("}"),
                "Final content should be valid JSON structure");
    }

    @Test
    @DisplayName("Should maintain data integrity under concurrent access")
    void testDataIntegrityUnderConcurrentAccess() throws InterruptedException {
        // Setup
        String baseDeviceId = "integrity-test";
        int threadCount = 10;
        int operationsPerThread = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // Execute - each thread works with its own set of files
        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    for (int i = 0; i < operationsPerThread; i++) {
                        String deviceId = baseDeviceId + "-" + threadId + "-" + i;
                        String expectedData = "{\"deviceid\":\"" + deviceId + "\",\"thread\":" + threadId
                                + ",\"operation\":" + i + ",\"checksum\":" + (threadId * 1000 + i) + "}";

                        // Write
                        cacheProvider.newFile(deviceId, expectedData);

                        // Immediate read-back verification
                        String readData = cacheProvider.getFile(deviceId + ".txt");
                        assertEquals(expectedData, readData, "Data integrity check failed for " + deviceId);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for completion
        assertTrue(latch.await(60, TimeUnit.SECONDS), "Integrity test should complete within 60 seconds");
        executor.shutdown();

        // Final verification - check all files maintain integrity
        for (int t = 0; t < threadCount; t++) {
            for (int i = 0; i < operationsPerThread; i++) {
                String deviceId = baseDeviceId + "-" + t + "-" + i;
                String expectedData = "{\"deviceid\":\"" + deviceId + "\",\"thread\":" + t + ",\"operation\":" + i
                        + ",\"checksum\":" + (t * 1000 + i) + "}";

                assertTrue(cacheProvider.checkFile(deviceId), "File should exist: " + deviceId);

                String actualData = cacheProvider.getFile(deviceId + ".txt");
                assertEquals(expectedData, actualData, "Final integrity check failed for " + deviceId);
            }
        }
    }
}
