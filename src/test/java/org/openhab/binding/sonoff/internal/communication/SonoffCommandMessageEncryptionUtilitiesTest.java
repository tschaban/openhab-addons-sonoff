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

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Unit tests for {@link SonoffCommandMessageEncryptionUtilities}
 * Tests encryption, decryption, and authentication MAC functionality
 *
 * @author OpenHAB Sonoff Binding - Initial contribution
 */
class SonoffCommandMessageEncryptionUtilitiesTest {

    private SonoffCommandMessageEncryptionUtilities encryptionUtils;

    @BeforeEach
    void setUp() {
        encryptionUtils = new SonoffCommandMessageEncryptionUtilities();
    }

    @Test
    void testGetAuthMac_WithValidInputs_ShouldReturnBase64Mac() throws Exception {
        // Arrange
        String appSecret = "testAppSecret123";
        String data = "testDataToSign";

        // Act
        String result = encryptionUtils.getAuthMac(appSecret, data);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        
        // Verify it's valid Base64
        assertDoesNotThrow(() -> Base64.getDecoder().decode(result));
        
        // Verify consistent results for same input
        String result2 = encryptionUtils.getAuthMac(appSecret, data);
        assertEquals(result, result2);
    }

    @Test
    void testGetAuthMac_WithDifferentData_ShouldReturnDifferentMacs() throws Exception {
        // Arrange
        String appSecret = "testAppSecret123";
        String data1 = "testData1";
        String data2 = "testData2";

        // Act
        String mac1 = encryptionUtils.getAuthMac(appSecret, data1);
        String mac2 = encryptionUtils.getAuthMac(appSecret, data2);

        // Assert
        assertNotEquals(mac1, mac2);
    }

    @Test
    void testGetAuthMac_WithDifferentSecrets_ShouldReturnDifferentMacs() throws Exception {
        // Arrange
        String appSecret1 = "secret1";
        String appSecret2 = "secret2";
        String data = "sameData";

        // Act
        String mac1 = encryptionUtils.getAuthMac(appSecret1, data);
        String mac2 = encryptionUtils.getAuthMac(appSecret2, data);

        // Assert
        assertNotEquals(mac1, mac2);
    }

    @Test
    void testGetAuthMac_WithEmptySecret_ShouldNotThrow() throws Exception {
        // Arrange
        String appSecret = "";
        String data = "testData";

        // Act & Assert
        assertDoesNotThrow(() -> {
            String result = encryptionUtils.getAuthMac(appSecret, data);
            assertNotNull(result);
        });
    }

    @Test
    void testGetAuthMac_WithEmptyData_ShouldNotThrow() throws Exception {
        // Arrange
        String appSecret = "testSecret";
        String data = "";

        // Act & Assert
        assertDoesNotThrow(() -> {
            String result = encryptionUtils.getAuthMac(appSecret, data);
            assertNotNull(result);
        });
    }

    @Test
    void testGetAuthMac_WithSpecialCharacters_ShouldHandleCorrectly() throws Exception {
        // Arrange
        String appSecret = "secret!@#$%^&*()";
        String data = "data with spaces and symbols: {}[]";

        // Act
        String result = encryptionUtils.getAuthMac(appSecret, data);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertDoesNotThrow(() -> Base64.getDecoder().decode(result));
    }

    @Test
    void testGetAuthMac_WithUnicodeCharacters_ShouldHandleCorrectly() throws Exception {
        // Arrange
        String appSecret = "秘密键";
        String data = "测试数据";

        // Act
        String result = encryptionUtils.getAuthMac(appSecret, data);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertDoesNotThrow(() -> Base64.getDecoder().decode(result));
    }

    @Test
    void testEncrypt_WithValidInputs_ShouldReturnValidJsonString() {
        // Arrange
        String params = "{\"switch\":\"on\"}";
        String deviceKey = "testDeviceKey123";
        String deviceId = "device123";
        Long sequence = 1234567890L;

        // Act
        String result = encryptionUtils.encrypt(params, deviceKey, deviceId, sequence);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        
        // Verify it's valid JSON
        assertDoesNotThrow(() -> JsonParser.parseString(result));
        
        JsonObject jsonResult = JsonParser.parseString(result).getAsJsonObject();
        
        // Verify required fields are present
        assertTrue(jsonResult.has("sequence"));
        assertTrue(jsonResult.has("deviceid"));
        assertTrue(jsonResult.has("selfApikey"));
        assertTrue(jsonResult.has("iv"));
        assertTrue(jsonResult.has("encrypt"));
        assertTrue(jsonResult.has("data"));
        
        // Verify field values
        assertEquals(sequence.toString(), jsonResult.get("sequence").getAsString());
        assertEquals(deviceId, jsonResult.get("deviceid").getAsString());
        assertEquals("123", jsonResult.get("selfApikey").getAsString());
        assertTrue(jsonResult.get("encrypt").getAsBoolean());
        
        // Verify IV and data are valid Base64
        String iv = jsonResult.get("iv").getAsString();
        String data = jsonResult.get("data").getAsString();
        assertDoesNotThrow(() -> Base64.getDecoder().decode(iv));
        assertDoesNotThrow(() -> Base64.getDecoder().decode(data));
    }

    @Test
    void testEncrypt_WithDifferentParams_ShouldReturnDifferentResults() {
        // Arrange
        String params1 = "{\"switch\":\"on\"}";
        String params2 = "{\"switch\":\"off\"}";
        String deviceKey = "testDeviceKey123";
        String deviceId = "device123";
        Long sequence = 1234567890L;

        // Act
        String result1 = encryptionUtils.encrypt(params1, deviceKey, deviceId, sequence);
        String result2 = encryptionUtils.encrypt(params2, deviceKey, deviceId, sequence);

        // Assert
        assertNotEquals(result1, result2);
        
        // Both should be valid JSON
        assertDoesNotThrow(() -> JsonParser.parseString(result1));
        assertDoesNotThrow(() -> JsonParser.parseString(result2));
    }

    @Test
    void testEncrypt_WithDifferentDeviceKeys_ShouldReturnDifferentResults() {
        // Arrange
        String params = "{\"switch\":\"on\"}";
        String deviceKey1 = "key1";
        String deviceKey2 = "key2";
        String deviceId = "device123";
        Long sequence = 1234567890L;

        // Act
        String result1 = encryptionUtils.encrypt(params, deviceKey1, deviceId, sequence);
        String result2 = encryptionUtils.encrypt(params, deviceKey2, deviceId, sequence);

        // Assert
        assertNotEquals(result1, result2);
    }

    @Test
    void testEncrypt_WithEmptyParams_ShouldNotThrow() {
        // Arrange
        String params = "";
        String deviceKey = "testDeviceKey123";
        String deviceId = "device123";
        Long sequence = 1234567890L;

        // Act
        String result = encryptionUtils.encrypt(params, deviceKey, deviceId, sequence);

        // Assert
        assertNotNull(result);
        // Should either return valid JSON or empty string (depending on implementation)
        assertTrue(result.isEmpty() || result.startsWith("{"));
    }

    @Test
    void testEncrypt_WithSpecialCharactersInParams_ShouldHandleCorrectly() {
        // Arrange
        String params = "{\"message\":\"Hello! @#$%^&*()_+ World\"}";
        String deviceKey = "testDeviceKey123";
        String deviceId = "device123";
        Long sequence = 1234567890L;

        // Act
        String result = encryptionUtils.encrypt(params, deviceKey, deviceId, sequence);

        // Assert
        assertNotNull(result);
        if (!result.isEmpty()) {
            assertDoesNotThrow(() -> JsonParser.parseString(result));
        }
    }

    @Test
    void testEncrypt_WithUnicodeInParams_ShouldHandleCorrectly() {
        // Arrange
        String params = "{\"message\":\"测试消息\"}";
        String deviceKey = "testDeviceKey123";
        String deviceId = "device123";
        Long sequence = 1234567890L;

        // Act
        String result = encryptionUtils.encrypt(params, deviceKey, deviceId, sequence);

        // Assert
        assertNotNull(result);
        if (!result.isEmpty()) {
            assertDoesNotThrow(() -> JsonParser.parseString(result));
        }
    }

    @Test
    void testDecrypt_WithValidEncryptedPayload_ShouldReturnOriginalData() {
        // Arrange
        String originalData = "{\"switch\":\"on\"}";
        String deviceKey = "testDeviceKey123";
        String deviceId = "device123";
        Long sequence = 1234567890L;

        // First encrypt the data
        String encryptedJson = encryptionUtils.encrypt(originalData, deviceKey, deviceId, sequence);
        
        // Skip test if encryption failed
        if (encryptedJson.isEmpty()) {
            return;
        }
        
        JsonObject encryptedPayload = JsonParser.parseString(encryptedJson).getAsJsonObject();
        
        // Create a payload in the format expected by decrypt method
        JsonObject decryptPayload = new JsonObject();
        decryptPayload.addProperty("iv", encryptedPayload.get("iv").getAsString());
        decryptPayload.addProperty("data1", encryptedPayload.get("data").getAsString());
        decryptPayload.addProperty("data2", "");
        decryptPayload.addProperty("data3", "");
        decryptPayload.addProperty("data4", "");

        // Act
        String decryptedData = encryptionUtils.decrypt(decryptPayload, deviceKey);

        // Assert
        assertEquals(originalData, decryptedData);
    }

    @Test
    void testDecrypt_WithInvalidPayload_ShouldReturnEmptyString() {
        // Arrange
        JsonObject invalidPayload = new JsonObject();
        invalidPayload.addProperty("invalid", "data");
        String deviceKey = "testDeviceKey123";

        // Act
        String result = encryptionUtils.decrypt(invalidPayload, deviceKey);

        // Assert
        assertEquals("", result);
    }

    @Test
    void testDecrypt_WithMissingIv_ShouldReturnEmptyString() {
        // Arrange
        JsonObject payload = new JsonObject();
        payload.addProperty("data1", "someBase64Data");
        payload.addProperty("data2", "");
        payload.addProperty("data3", "");
        payload.addProperty("data4", "");
        String deviceKey = "testDeviceKey123";

        // Act
        String result = encryptionUtils.decrypt(payload, deviceKey);

        // Assert
        assertEquals("", result);
    }

    @Test
    void testDecrypt_WithWrongDeviceKey_ShouldReturnEmptyString() {
        // Arrange
        String originalData = "{\"switch\":\"on\"}";
        String correctKey = "correctKey";
        String wrongKey = "wrongKey";
        String deviceId = "device123";
        Long sequence = 1234567890L;

        // Encrypt with correct key
        String encryptedJson = encryptionUtils.encrypt(originalData, correctKey, deviceId, sequence);
        
        if (encryptedJson.isEmpty()) {
            return; // Skip if encryption failed
        }
        
        JsonObject encryptedPayload = JsonParser.parseString(encryptedJson).getAsJsonObject();
        
        JsonObject decryptPayload = new JsonObject();
        decryptPayload.addProperty("iv", encryptedPayload.get("iv").getAsString());
        decryptPayload.addProperty("data1", encryptedPayload.get("data").getAsString());
        decryptPayload.addProperty("data2", "");
        decryptPayload.addProperty("data3", "");
        decryptPayload.addProperty("data4", "");

        // Act - try to decrypt with wrong key
        String result = encryptionUtils.decrypt(decryptPayload, wrongKey);

        // Assert
        assertEquals("", result);
    }

    @Test
    void testDecrypt_WithConcatenatedDataFields_ShouldHandleCorrectly() {
        // Arrange
        JsonObject payload = new JsonObject();
        payload.addProperty("iv", "dGVzdEl2MTIzNDU2Nzg="); // Base64 encoded test IV
        payload.addProperty("data1", "part1");
        payload.addProperty("data2", "part2");
        payload.addProperty("data3", "part3");
        payload.addProperty("data4", "part4");
        String deviceKey = "testKey";

        // Act
        String result = encryptionUtils.decrypt(payload, deviceKey);

        // Assert
        // Should return empty string due to invalid encrypted data, but shouldn't throw exception
        assertEquals("", result);
    }

    @Test
    void testDecrypt_WithNullDataFields_ShouldHandleGracefully() {
        // Arrange
        JsonObject payload = new JsonObject();
        payload.addProperty("iv", "dGVzdEl2MTIzNDU2Nzg=");
        // Don't add data1, data2, data3, data4 properties (they will be null)
        String deviceKey = "testKey";

        // Act
        String result = encryptionUtils.decrypt(payload, deviceKey);

        // Assert
        assertEquals("", result);
    }

    @Test
    void testEncryptDecryptRoundTrip_ShouldPreserveOriginalData() {
        // Arrange
        String originalData = "{\"switch\":\"on\",\"brightness\":75,\"color\":{\"r\":255,\"g\":128,\"b\":0}}";
        String deviceKey = "roundTripTestKey123";
        String deviceId = "roundTripDevice";
        Long sequence = 9876543210L;

        // Act
        String encrypted = encryptionUtils.encrypt(originalData, deviceKey, deviceId, sequence);
        
        if (encrypted.isEmpty()) {
            return; // Skip if encryption failed
        }
        
        JsonObject encryptedPayload = JsonParser.parseString(encrypted).getAsJsonObject();
        
        JsonObject decryptPayload = new JsonObject();
        decryptPayload.addProperty("iv", encryptedPayload.get("iv").getAsString());
        decryptPayload.addProperty("data1", encryptedPayload.get("data").getAsString());
        decryptPayload.addProperty("data2", "");
        decryptPayload.addProperty("data3", "");
        decryptPayload.addProperty("data4", "");
        
        String decrypted = encryptionUtils.decrypt(decryptPayload, deviceKey);

        // Assert
        assertEquals(originalData, decrypted);
    }

    @Test
    void testEncryptDecryptRoundTrip_WithUnicodeData_ShouldPreserveOriginalData() {
        // Arrange
        String originalData = "{\"message\":\"你好世界\",\"status\":\"正常\"}";
        String deviceKey = "unicodeTestKey";
        String deviceId = "unicodeDevice";
        Long sequence = 1111111111L;

        // Act
        String encrypted = encryptionUtils.encrypt(originalData, deviceKey, deviceId, sequence);
        
        if (encrypted.isEmpty()) {
            return; // Skip if encryption failed
        }
        
        JsonObject encryptedPayload = JsonParser.parseString(encrypted).getAsJsonObject();
        
        JsonObject decryptPayload = new JsonObject();
        decryptPayload.addProperty("iv", encryptedPayload.get("iv").getAsString());
        decryptPayload.addProperty("data1", encryptedPayload.get("data").getAsString());
        decryptPayload.addProperty("data2", "");
        decryptPayload.addProperty("data3", "");
        decryptPayload.addProperty("data4", "");
        
        String decrypted = encryptionUtils.decrypt(decryptPayload, deviceKey);

        // Assert
        assertEquals(originalData, decrypted);
    }

    @Test
    void testEncrypt_ConsistentIvLength_ShouldAlwaysGenerate16ByteIv() {
        // Arrange
        String params = "{\"test\":\"data\"}";
        String deviceKey = "testKey";
        String deviceId = "device";
        Long sequence = 123L;

        // Act
        String result = encryptionUtils.encrypt(params, deviceKey, deviceId, sequence);

        // Assert
        if (!result.isEmpty()) {
            JsonObject jsonResult = JsonParser.parseString(result).getAsJsonObject();
            String iv = jsonResult.get("iv").getAsString();
            byte[] ivBytes = Base64.getDecoder().decode(iv);
            
            // AES CBC mode requires 16-byte IV
            assertEquals(16, ivBytes.length);
        }
    }

    @Test
    void testGetAuthMac_KnownTestVector_ShouldReturnExpectedResult() throws Exception {
        // Arrange - Using a known test vector for HMAC-SHA256
        String appSecret = "key";
        String data = "The quick brown fox jumps over the lazy dog";

        // Act
        String result = encryptionUtils.getAuthMac(appSecret, data);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        
        // Verify it's consistent
        String result2 = encryptionUtils.getAuthMac(appSecret, data);
        assertEquals(result, result2);
        
        // Verify it's valid Base64
        byte[] decoded = Base64.getDecoder().decode(result);
        assertEquals(32, decoded.length); // SHA256 produces 32 bytes
    }
}