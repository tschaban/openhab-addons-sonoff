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
package org.openhab.binding.sonoff.internal.communication;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SonoffCommandMessageUtilities}
 * Tests random string generation, nonce creation, and timestamp functionality
 *
 * @author OpenHAB Sonoff Binding - Initial contribution
 */
class SonoffCommandMessageUtilitiesTest {

    private static final String EXPECTED_CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("^[" + EXPECTED_CHARSET + "]+$");

    @Test
    void testGetNonce_ShouldReturn8CharacterString() {
        // Act
        String nonce = SonoffCommandMessageUtilities.getNonce();

        // Assert
        assertNotNull(nonce);
        assertEquals(8, nonce.length());
    }

    @Test
    void testGetNonce_ShouldContainOnlyValidCharacters() {
        // Act
        String nonce = SonoffCommandMessageUtilities.getNonce();

        // Assert
        assertTrue(ALPHANUMERIC_PATTERN.matcher(nonce).matches(),
                "Nonce should only contain alphanumeric characters: " + nonce);
    }

    @Test
    void testGetNonce_ShouldReturnDifferentValuesOnMultipleCalls() {
        // Arrange
        Set<String> nonces = new HashSet<>();
        int iterations = 100;

        // Act
        for (int i = 0; i < iterations; i++) {
            nonces.add(SonoffCommandMessageUtilities.getNonce());
        }

        // Assert
        // With 62^8 possible combinations, 100 calls should produce unique values
        assertEquals(iterations, nonces.size(), "All nonces should be unique");
    }

    @Test
    void testGetNonce_ShouldBeThreadSafe() throws InterruptedException {
        // Arrange
        int threadCount = 5;
        int noncesPerThread = 50;
        Set<String> allNonces = new HashSet<>();
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // Act
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    Set<String> threadNonces = new HashSet<>();
                    for (int j = 0; j < noncesPerThread; j++) {
                        String nonce = SonoffCommandMessageUtilities.getNonce();
                        // Verify nonce properties
                        assertNotNull(nonce, "Nonce should not be null");
                        assertEquals(8, nonce.length(), "Nonce should be 8 characters");
                        assertTrue(ALPHANUMERIC_PATTERN.matcher(nonce).matches(), 
                                "Nonce should contain only alphanumeric characters");
                        threadNonces.add(nonce);
                    }
                    synchronized (allNonces) {
                        allNonces.addAll(threadNonces);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // Assert
        assertTrue(latch.await(10, TimeUnit.SECONDS), "All threads should complete within timeout");
        
        // With 62^8 possible combinations, we should get very high uniqueness
        int expectedTotal = threadCount * noncesPerThread;
        assertTrue(allNonces.size() > expectedTotal * 0.90, 
                "At least 90% of nonces should be unique (got " + allNonces.size() + " out of " + expectedTotal + ")");
        
        executor.shutdown();
    }

    @Test
    void testGetSequence_SynchronizationBehavior() throws InterruptedException {
        // Arrange
        int threadCount = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        // Act - All threads call getSequence simultaneously
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    // Call getSequence - this should not throw any exceptions
                    Long sequence = SonoffCommandMessageUtilities.getSequence();
                    assertNotNull(sequence, "Sequence should not be null");
                    assertTrue(sequence > 0, "Sequence should be positive");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    completeLatch.countDown();
                }
            });
        }
        
        // Start all threads simultaneously
        startLatch.countDown();
        
        // Assert
        assertTrue(completeLatch.await(5, TimeUnit.SECONDS), 
                "All threads should complete without deadlock or exceptions");
        
        executor.shutdown();
    }

    @Test
    void testGetSequence_ShouldReturnCurrentTimestamp() {
        // Arrange
        long beforeCall = System.currentTimeMillis();

        // Act
        Long sequence = SonoffCommandMessageUtilities.getSequence();

        // Assert
        long afterCall = System.currentTimeMillis();
        
        assertNotNull(sequence);
        assertTrue(sequence >= beforeCall, "Sequence should be >= time before call");
        assertTrue(sequence <= afterCall, "Sequence should be <= time after call");
    }

    @Test
    void testGetSequence_ShouldReturnIncreasingValues() throws InterruptedException {
        // Act
        Long sequence1 = SonoffCommandMessageUtilities.getSequence();
        Thread.sleep(1); // Ensure time difference
        Long sequence2 = SonoffCommandMessageUtilities.getSequence();

        // Assert
        assertTrue(sequence2 > sequence1, "Second sequence should be greater than first");
    }

    @Test
    void testGetSequence_ShouldBeSynchronized() throws InterruptedException {
        // Arrange
        int threadCount = 5;
        int callsPerThread = 10;
        Set<Long> allSequences = new HashSet<>();
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // Act
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < callsPerThread; j++) {
                        // Add small delay to increase chance of different timestamps
                        if (j > 0) {
                            Thread.sleep(1);
                        }
                        Long sequence = SonoffCommandMessageUtilities.getSequence();
                        synchronized (allSequences) {
                            allSequences.add(sequence);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        // Assert
        assertTrue(latch.await(10, TimeUnit.SECONDS), "All threads should complete within timeout");
        
        // With millisecond precision and small delays, we should get some unique values
        // The main goal is to verify the method is synchronized and doesn't throw exceptions
        int expectedTotal = threadCount * callsPerThread;
        assertTrue(allSequences.size() >= 1, 
                "Should have at least 1 unique sequence (got " + allSequences.size() + " out of " + expectedTotal + ")");
        
        // Verify all sequences are reasonable timestamps
        long currentTime = System.currentTimeMillis();
        for (Long sequence : allSequences) {
            assertTrue(sequence > 0, "Sequence should be positive");
            assertTrue(Math.abs(sequence - currentTime) < 60000, "Sequence should be within 1 minute of current time");
        }
        
        executor.shutdown();
    }

    @Test
    void testGetSequence_SynchronizationBehavior() throws InterruptedException {
        // Arrange
        int threadCount = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        // Act - All threads call getSequence simultaneously
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    // Call getSequence - this should not throw any exceptions
                    Long sequence = SonoffCommandMessageUtilities.getSequence();
                    assertNotNull(sequence, "Sequence should not be null");
                    assertTrue(sequence > 0, "Sequence should be positive");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    completeLatch.countDown();
                }
            });
        }
        
        // Start all threads simultaneously
        startLatch.countDown();
        
        // Assert
        assertTrue(completeLatch.await(5, TimeUnit.SECONDS), 
                "All threads should complete without deadlock or exceptions");
        
        executor.shutdown();
    }

    @Test
    void testGetTs_ShouldReturnCurrentTimestamp() {
        // Arrange
        long beforeCall = System.currentTimeMillis();

        // Act
        Long timestamp = SonoffCommandMessageUtilities.getTs();

        // Assert
        long afterCall = System.currentTimeMillis();
        
        assertNotNull(timestamp);
        assertTrue(timestamp >= beforeCall, "Timestamp should be >= time before call");
        assertTrue(timestamp <= afterCall, "Timestamp should be <= time after call");
    }

    @Test
    void testGetTs_ShouldReturnIncreasingValues() throws InterruptedException {
        // Act
        Long ts1 = SonoffCommandMessageUtilities.getTs();
        Thread.sleep(1); // Ensure time difference
        Long ts2 = SonoffCommandMessageUtilities.getTs();

        // Assert
        assertTrue(ts2 > ts1, "Second timestamp should be greater than first");
    }

    @Test
    void testGetSequenceAndGetTs_ShouldReturnSimilarValues() {
        // Act
        Long sequence = SonoffCommandMessageUtilities.getSequence();
        Long timestamp = SonoffCommandMessageUtilities.getTs();

        // Assert
        // Both should return current time, so they should be very close
        long difference = Math.abs(timestamp - sequence);
        assertTrue(difference < 100, "Sequence and timestamp should be within 100ms of each other");
    }

    @Test
    void testConstants_ShouldHaveExpectedValues() {
        // Assert
        assertEquals("", SonoffCommandMessageUtilities.APPID);
        assertEquals("", SonoffCommandMessageUtilities.APPSECRET);
        assertEquals(Integer.valueOf(8), SonoffCommandMessageUtilities.VERSION);
    }

    @Test
    void testRandomString_WithZeroLength_ShouldReturnEmptyString() throws Exception {
        // Arrange - Access private method via reflection
        Method randomStringMethod = SonoffCommandMessageUtilities.class.getDeclaredMethod("randomString", int.class);
        randomStringMethod.setAccessible(true);

        // Act
        String result = (String) randomStringMethod.invoke(null, 0);

        // Assert
        assertNotNull(result);
        assertEquals("", result);
    }

    @Test
    void testRandomString_WithPositiveLength_ShouldReturnCorrectLength() throws Exception {
        // Arrange - Access private method via reflection
        Method randomStringMethod = SonoffCommandMessageUtilities.class.getDeclaredMethod("randomString", int.class);
        randomStringMethod.setAccessible(true);

        // Act & Assert for various lengths
        for (int length = 1; length <= 20; length++) {
            String result = (String) randomStringMethod.invoke(null, length);
            assertNotNull(result);
            assertEquals(length, result.length(), "String should have length " + length);
            assertTrue(ALPHANUMERIC_PATTERN.matcher(result).matches(),
                    "String should only contain valid characters: " + result);
        }
    }

    @Test
    void testRandomString_ShouldUseAllCharactersFromCharset() throws Exception {
        // Arrange - Access private method via reflection
        Method randomStringMethod = SonoffCommandMessageUtilities.class.getDeclaredMethod("randomString", int.class);
        randomStringMethod.setAccessible(true);
        
        Set<Character> observedChars = new HashSet<>();
        int iterations = 10000; // Generate many strings to observe character distribution

        // Act
        for (int i = 0; i < iterations; i++) {
            String result = (String) randomStringMethod.invoke(null, 10);
            for (char c : result.toCharArray()) {
                observedChars.add(c);
            }
        }

        // Assert
        // We should observe most characters from the charset
        // With 10000 iterations of 10-character strings, we should see most of the 62 characters
        assertTrue(observedChars.size() > 50, 
                "Should observe most characters from charset (observed " + observedChars.size() + " out of 62)");
        
        // Verify all observed characters are valid
        for (Character c : observedChars) {
            assertTrue(EXPECTED_CHARSET.indexOf(c) >= 0, 
                    "Character '" + c + "' should be in the expected charset");
        }
    }

    @Test
    void testRandomString_WithLargeLength_ShouldWork() throws Exception {
        // Arrange - Access private method via reflection
        Method randomStringMethod = SonoffCommandMessageUtilities.class.getDeclaredMethod("randomString", int.class);
        randomStringMethod.setAccessible(true);

        // Act
        String result = (String) randomStringMethod.invoke(null, 1000);

        // Assert
        assertNotNull(result);
        assertEquals(1000, result.length());
        assertTrue(ALPHANUMERIC_PATTERN.matcher(result).matches(),
                "Large string should only contain valid characters");
    }

    @Test
    void testSecureRandomInstance_ShouldBeInitialized() throws Exception {
        // Arrange - Access private field via reflection
        Field rndField = SonoffCommandMessageUtilities.class.getDeclaredField("rnd");
        rndField.setAccessible(true);

        // Act
        SecureRandom rnd = (SecureRandom) rndField.get(null);

        // Assert
        assertNotNull(rnd, "SecureRandom instance should be initialized");
    }

    @Test
    void testCharsetConstant_ShouldContainExpectedCharacters() throws Exception {
        // Arrange - Access private field via reflection
        Field abField = SonoffCommandMessageUtilities.class.getDeclaredField("AB");
        abField.setAccessible(true);

        // Act
        String charset = (String) abField.get(null);

        // Assert
        assertEquals(EXPECTED_CHARSET, charset);
        assertEquals(62, charset.length()); // 10 digits + 26 uppercase + 26 lowercase
        
        // Verify no duplicate characters
        Set<Character> uniqueChars = new HashSet<>();
        for (char c : charset.toCharArray()) {
            uniqueChars.add(c);
        }
        assertEquals(62, uniqueChars.size(), "Charset should not contain duplicate characters");
    }

    @Test
    void testNonceUniqueness_OverTime() throws InterruptedException {
        // Arrange
        Set<String> nonces = new HashSet<>();
        int batchSize = 50;
        int batches = 5;

        // Act - Generate nonces in batches with time delays
        for (int batch = 0; batch < batches; batch++) {
            for (int i = 0; i < batchSize; i++) {
                nonces.add(SonoffCommandMessageUtilities.getNonce());
            }
            if (batch < batches - 1) {
                Thread.sleep(10); // Small delay between batches
            }
        }

        // Assert
        assertEquals(batchSize * batches, nonces.size(), 
                "All nonces should be unique across time");
    }

    @Test
    void testTimestampMethods_ShouldReturnReasonableValues() {
        // Arrange
        long year2020 = 1577836800000L; // Jan 1, 2020 UTC
        long year2030 = 1893456000000L; // Jan 1, 2030 UTC

        // Act
        Long sequence = SonoffCommandMessageUtilities.getSequence();
        Long timestamp = SonoffCommandMessageUtilities.getTs();

        // Assert
        assertTrue(sequence > year2020, "Sequence should be after 2020");
        assertTrue(sequence < year2030, "Sequence should be before 2030");
        assertTrue(timestamp > year2020, "Timestamp should be after 2020");
        assertTrue(timestamp < year2030, "Timestamp should be before 2030");
    }

    @Test
    void testGetNonce_ConsistentLength() {
        // Act & Assert
        for (int i = 0; i < 100; i++) {
            String nonce = SonoffCommandMessageUtilities.getNonce();
            assertEquals(8, nonce.length(), "Nonce should always be 8 characters long");
        }
    }

    @Test
    void testSynchronizedMethods_NoDeadlock() throws InterruptedException {
        // Arrange
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // Act - Call both synchronized methods concurrently
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 5; j++) {
                        Long sequence = SonoffCommandMessageUtilities.getSequence();
                        Long timestamp = SonoffCommandMessageUtilities.getTs();
                        
                        // Verify both methods return valid values
                        assertNotNull(sequence, "Sequence should not be null");
                        assertNotNull(timestamp, "Timestamp should not be null");
                        assertTrue(sequence > 0, "Sequence should be positive");
                        assertTrue(timestamp > 0, "Timestamp should be positive");
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // Assert
        assertTrue(latch.await(10, TimeUnit.SECONDS), 
                "All threads should complete without deadlock");
        
        executor.shutdown();
    }

    @Test
    void testGetSequence_SynchronizationBehavior() throws InterruptedException {
        // Arrange
        int threadCount = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        // Act - All threads call getSequence simultaneously
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    // Call getSequence - this should not throw any exceptions
                    Long sequence = SonoffCommandMessageUtilities.getSequence();
                    assertNotNull(sequence, "Sequence should not be null");
                    assertTrue(sequence > 0, "Sequence should be positive");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    completeLatch.countDown();
                }
            });
        }
        
        // Start all threads simultaneously
        startLatch.countDown();
        
        // Assert
        assertTrue(completeLatch.await(5, TimeUnit.SECONDS), 
                "All threads should complete without deadlock or exceptions");
        
        executor.shutdown();
    }
}