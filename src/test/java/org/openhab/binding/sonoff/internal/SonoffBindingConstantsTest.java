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

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.openhab.binding.sonoff.internal.SonoffBindingConstants.ValidationResult;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Unit tests for {@link SonoffBindingConstants} validation functionality.
 *
 * @author Assistant - Test implementation
 */
public class SonoffBindingConstantsTest {

    @Test
    public void testDeviceMappingValidation() {
        // Run the validation
        ValidationResult result = SonoffBindingConstants.validateDeviceMappings();

        // Print any errors or warnings for debugging
        if (result.hasErrors()) {
            System.out.println("Validation Errors:");
            result.getErrors().forEach(System.out::println);
        }

        if (result.hasWarnings()) {
            System.out.println("Validation Warnings:");
            result.getWarnings().forEach(System.out::println);
        }

        // The validation should pass without errors
        assertFalse(result.hasErrors(), "Device mapping validation failed with errors: " + result.getErrors());
    }

    @Test
    public void testAllNumericThingTypesHaveMapping() {
        Map<Integer, ThingTypeUID> deviceMap = SonoffBindingConstants.createMap();

        // Get all numeric THING_TYPE constants using reflection
        Field[] fields = SonoffBindingConstants.class.getDeclaredFields();

        for (Field field : fields) {
            if (field.getName().startsWith("THING_TYPE_") && field.getType().equals(ThingTypeUID.class)
                    && java.lang.reflect.Modifier.isStatic(field.getModifiers())
                    && java.lang.reflect.Modifier.isFinal(field.getModifiers())) {

                String fieldName = field.getName();
                String suffix = fieldName.substring("THING_TYPE_".length());

                // Check if suffix is numeric
                try {
                    int numericId = Integer.parseInt(suffix);

                    // Verify this numeric ID is in the device map
                    assertTrue(deviceMap.containsKey(numericId),
                            String.format("THING_TYPE_%d is not included in createMap()", numericId));

                    // Verify the mapping is correct
                    try {
                        ThingTypeUID expectedType = (ThingTypeUID) field.get(null);
                        ThingTypeUID actualType = deviceMap.get(numericId);
                        assertEquals(expectedType, actualType,
                                String.format("THING_TYPE_%d maps to wrong ThingTypeUID: expected %s, got %s",
                                        numericId, expectedType, actualType));
                    } catch (IllegalAccessException e) {
                        fail("Could not access field " + fieldName + ": " + e.getMessage());
                    }

                } catch (NumberFormatException e) {
                    // Not a numeric THING_TYPE, skip (e.g., THING_TYPE_ACCOUNT, THING_TYPE_ZSWITCH1)
                }
            }
        }
    }

    @Test
    public void testSupportedThingTypesCompleteness() {
        Set<ThingTypeUID> supportedTypes = SonoffBindingConstants.SUPPORTED_THING_TYPE_UIDS;

        // Get all THING_TYPE constants using reflection
        Field[] fields = SonoffBindingConstants.class.getDeclaredFields();
        int totalThingTypes = 0;
        int excludedTypes = 0;

        for (Field field : fields) {
            if (field.getName().startsWith("THING_TYPE_") && field.getType().equals(ThingTypeUID.class)
                    && java.lang.reflect.Modifier.isStatic(field.getModifiers())
                    && java.lang.reflect.Modifier.isFinal(field.getModifiers())) {

                totalThingTypes++;

                try {
                    ThingTypeUID thingType = (ThingTypeUID) field.get(null);

                    // UNKNOWNDEVICE might be intentionally excluded
                    if (field.getName().equals("THING_TYPE_UNKNOWNDEVICE")) {
                        excludedTypes++;
                        // This is acceptable to be excluded, just log it
                        if (!supportedTypes.contains(thingType)) {
                            System.out.println(
                                    "INFO: THING_TYPE_UNKNOWNDEVICE is intentionally excluded from SUPPORTED_THING_TYPE_UIDS");
                        }
                    } else {
                        assertTrue(supportedTypes.contains(thingType), String.format(
                                "%s (%s) is not included in SUPPORTED_THING_TYPE_UIDS", field.getName(), thingType));
                    }
                } catch (IllegalAccessException e) {
                    fail("Could not access field " + field.getName() + ": " + e.getMessage());
                }
            }
        }

        System.out.println(
                String.format("Validated %d THING_TYPE constants (%d excluded) against SUPPORTED_THING_TYPE_UIDS",
                        totalThingTypes, excludedTypes));

        // Verify we found a reasonable number of constants
        assertTrue(totalThingTypes >= 64, "Expected at least 64 THING_TYPE constants, found: " + totalThingTypes);
    }

    @Test
    public void testDiscoverableTypesAreSupported() {
        Set<ThingTypeUID> supportedTypes = SonoffBindingConstants.SUPPORTED_THING_TYPE_UIDS;
        Set<ThingTypeUID> discoverableTypes = SonoffBindingConstants.DISCOVERABLE_THING_TYPE_UIDS;

        // All discoverable types must be in supported types
        for (ThingTypeUID discoverableType : discoverableTypes) {
            assertTrue(supportedTypes.contains(discoverableType),
                    String.format(
                            "ThingTypeUID %s is in DISCOVERABLE_THING_TYPE_UIDS but not in SUPPORTED_THING_TYPE_UIDS",
                            discoverableType));
        }
    }

    @Test
    public void testCreateMapNoDuplicates() {
        Map<Integer, ThingTypeUID> deviceMap = SonoffBindingConstants.createMap();

        // Verify no duplicate keys (this would be caught by Map implementation, but good to test)
        // and no null values
        for (Map.Entry<Integer, ThingTypeUID> entry : deviceMap.entrySet()) {
            assertNotNull(entry.getKey(), "Device map contains null key");
            assertNotNull(entry.getValue(), "Device map contains null value for key " + entry.getKey());
        }
    }

    @Test
    public void testZigbeeMapConsistency() {
        Map<Integer, ThingTypeUID> zigbeeMap = SonoffBindingConstants.createZigbeeMap();
        Set<ThingTypeUID> supportedTypes = SonoffBindingConstants.SUPPORTED_THING_TYPE_UIDS;

        // All Zigbee mapped types should be in supported types
        for (ThingTypeUID zigbeeType : zigbeeMap.values()) {
            assertTrue(supportedTypes.contains(zigbeeType),
                    String.format("Zigbee ThingTypeUID %s is not in SUPPORTED_THING_TYPE_UIDS", zigbeeType));
        }
    }

    @Test
    public void testSensorMapConsistency() {
        Map<Integer, ThingTypeUID> sensorMap = SonoffBindingConstants.createSensorMap();
        Set<ThingTypeUID> supportedTypes = SonoffBindingConstants.SUPPORTED_THING_TYPE_UIDS;

        // All sensor mapped types should be in supported types
        for (ThingTypeUID sensorType : sensorMap.values()) {
            assertTrue(supportedTypes.contains(sensorType),
                    String.format("Sensor ThingTypeUID %s is not in SUPPORTED_THING_TYPE_UIDS", sensorType));
        }
    }

    @Test
    public void testValidationResultFunctionality() {
        ValidationResult result = new ValidationResult();

        // Test initial state
        assertFalse(result.hasErrors());
        assertFalse(result.hasWarnings());
        assertEquals(0, result.getErrorCount());
        assertEquals(0, result.getWarningCount());
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());

        // Test adding errors and warnings
        result.addError("Test error");
        result.addWarning("Test warning");

        assertTrue(result.hasErrors());
        assertTrue(result.hasWarnings());
        assertEquals(1, result.getErrorCount());
        assertEquals(1, result.getWarningCount());
        assertTrue(result.getErrors().contains("Test error"));
        assertTrue(result.getWarnings().contains("Test warning"));

        // Test immutability of returned sets
        Set<String> errors = result.getErrors();
        assertThrows(UnsupportedOperationException.class, () -> errors.add("Should fail"));
    }

    @Test
    public void testRunValidationDoesNotThrow() {
        // This should not throw any exceptions
        assertDoesNotThrow(() -> SonoffBindingConstants.runValidation());
    }

    @Test
    public void testLanProtocolSetsConsistency() {
        Set<Integer> lanIn = SonoffBindingConstants.LAN_IN;
        Set<Integer> lanOut = SonoffBindingConstants.LAN_OUT;
        Map<Integer, ThingTypeUID> deviceMap = SonoffBindingConstants.createMap();

        // All LAN protocol device IDs should have corresponding THING_TYPE mappings
        for (Integer deviceId : lanIn) {
            assertTrue(deviceMap.containsKey(deviceId),
                    String.format("LAN_IN contains device ID %d but createMap() does not", deviceId));
        }

        for (Integer deviceId : lanOut) {
            assertTrue(deviceMap.containsKey(deviceId),
                    String.format("LAN_OUT contains device ID %d but createMap() does not", deviceId));
        }

        // LAN_OUT should be a subset of LAN_IN (devices that support outbound should also support inbound)
        for (Integer deviceId : lanOut) {
            assertTrue(lanIn.contains(deviceId),
                    String.format("Device ID %d is in LAN_OUT but not in LAN_IN", deviceId));
        }
    }

    @Test
    public void testDiscoveryCapabilityValidation() {
        Set<ThingTypeUID> discoverableTypes = SonoffBindingConstants.DISCOVERABLE_THING_TYPE_UIDS;
        Map<Integer, ThingTypeUID> deviceMap = SonoffBindingConstants.createMap();

        // All numeric discoverable types should have UUID mappings for discovery to work
        for (ThingTypeUID discoverableType : discoverableTypes) {
            String thingTypeId = discoverableType.getId();

            // Check numeric types (non-numeric types like RF devices use different discovery)
            try {
                int numericId = Integer.parseInt(thingTypeId);

                assertTrue(deviceMap.containsKey(numericId), String.format(
                        "Discoverable device type %s (ID: %d) has no UUID mapping in createMap() - discovery will fail",
                        discoverableType, numericId));

                ThingTypeUID mappedType = deviceMap.get(numericId);
                assertEquals(discoverableType, mappedType,
                        String.format(
                                "Discoverable device type %s (ID: %d) maps to wrong ThingTypeUID: expected %s, got %s",
                                discoverableType, numericId, discoverableType, mappedType));

            } catch (NumberFormatException e) {
                // Non-numeric types (RF devices, Zigbee devices) - these are valid
                assertTrue(thingTypeId.startsWith("rf") || thingTypeId.startsWith("z") || thingTypeId.equals("1770")
                        || thingTypeId.equals("2026") || thingTypeId.equals("7003") || thingTypeId.equals("7014"),
                        String.format("Unexpected non-numeric discoverable type: %s", discoverableType));
            }
        }
    }

    @Test
    public void testKnownHandlerIdsCompleteness() {
        // This test ensures that the getKnownHandlerIds() method is kept up to date
        // We can't easily test the actual handler factory without complex reflection,
        // but we can test that our known handler IDs cover all discoverable types

        Set<ThingTypeUID> discoverableTypes = SonoffBindingConstants.DISCOVERABLE_THING_TYPE_UIDS;

        // Use reflection to call the private method for testing
        try {
            java.lang.reflect.Method getKnownHandlerIdsMethod = SonoffBindingConstants.class
                    .getDeclaredMethod("getKnownHandlerIds");
            getKnownHandlerIdsMethod.setAccessible(true);

            @SuppressWarnings("unchecked")
            Set<String> knownHandlerIds = (Set<String>) getKnownHandlerIdsMethod.invoke(null);

            // Check that all discoverable types have known handlers
            for (ThingTypeUID discoverableType : discoverableTypes) {
                String thingTypeId = discoverableType.getId();
                assertTrue(knownHandlerIds.contains(thingTypeId),
                        String.format(
                                "Discoverable device type %s is not in known handler IDs - update getKnownHandlerIds()",
                                discoverableType));
            }

            // Verify we have a reasonable number of handlers
            assertTrue(knownHandlerIds.size() > 50, "Known handler IDs seems too small: " + knownHandlerIds.size());

        } catch (Exception e) {
            fail("Could not test getKnownHandlerIds method: " + e.getMessage());
        }
    }

    @Test
    public void testDiscoveryValidationIntegration() {
        // Test that the discovery validation is integrated into the main validation
        ValidationResult result = SonoffBindingConstants.validateDeviceMappings();

        // Print discovery-related warnings/errors for debugging
        result.getErrors().stream()
                .filter(error -> error.toLowerCase().contains("discovery") || error.toLowerCase().contains("handler"))
                .forEach(error -> System.out.println("Discovery Error: " + error));

        result.getWarnings().stream().filter(
                warning -> warning.toLowerCase().contains("discovery") || warning.toLowerCase().contains("handler"))
                .forEach(warning -> System.out.println("Discovery Warning: " + warning));

        // The validation should not have critical discovery errors
        long discoveryErrors = result.getErrors().stream().filter(error -> error.contains("discovery will fail"))
                .count();

        assertEquals(0, discoveryErrors,
                "Found critical discovery errors that would prevent device discovery from working");
    }

    @Test
    public void testNumericThingTypeIdDetection() {
        // Test the helper method for detecting numeric thing type IDs
        try {
            java.lang.reflect.Method isNumericMethod = SonoffBindingConstants.class
                    .getDeclaredMethod("isNumericThingTypeId", String.class);
            isNumericMethod.setAccessible(true);

            // Test numeric IDs
            assertTrue((Boolean) isNumericMethod.invoke(null, "1"));
            assertTrue((Boolean) isNumericMethod.invoke(null, "123"));
            assertTrue((Boolean) isNumericMethod.invoke(null, "7003"));

            // Test non-numeric IDs
            assertFalse((Boolean) isNumericMethod.invoke(null, "account"));
            assertFalse((Boolean) isNumericMethod.invoke(null, "rfremote1"));
            assertFalse((Boolean) isNumericMethod.invoke(null, "zswitch1"));
            assertFalse((Boolean) isNumericMethod.invoke(null, "device"));

        } catch (Exception e) {
            fail("Could not test isNumericThingTypeId method: " + e.getMessage());
        }
    }

    @Test
    public void testAllThingTypeConstantsExtraction() {
        // Test the method that extracts ALL THING_TYPE constants
        try {
            java.lang.reflect.Method extractAllMethod = SonoffBindingConstants.class
                    .getDeclaredMethod("extractAllThingTypeConstants");
            extractAllMethod.setAccessible(true);

            @SuppressWarnings("unchecked")
            Set<ThingTypeUID> allThingTypes = (Set<ThingTypeUID>) extractAllMethod.invoke(null);

            // Verify we found a reasonable number of constants
            assertTrue(allThingTypes.size() >= 64,
                    "Expected at least 64 THING_TYPE constants, found: " + allThingTypes.size());

            // Verify it includes both numeric and non-numeric types
            boolean hasNumeric = allThingTypes.stream().anyMatch(t -> t.getId().equals("1"));
            boolean hasAccount = allThingTypes.stream().anyMatch(t -> t.getId().equals("account"));
            boolean hasRF = allThingTypes.stream().anyMatch(t -> t.getId().equals("rfremote1"));
            boolean hasZigbee = allThingTypes.stream().anyMatch(t -> t.getId().equals("zswitch1"));

            assertTrue(hasNumeric, "Should include numeric THING_TYPE constants");
            assertTrue(hasAccount, "Should include THING_TYPE_ACCOUNT");
            assertTrue(hasRF, "Should include RF THING_TYPE constants");
            assertTrue(hasZigbee, "Should include Zigbee THING_TYPE constants");

            System.out.println("Successfully extracted " + allThingTypes.size() + " THING_TYPE constants");

        } catch (Exception e) {
            fail("Could not test extractAllThingTypeConstants method: " + e.getMessage());
        }
    }

    @Test
    public void testAutomatedValidationForAllConstants() {
        // This test specifically validates the TODO: "Implement automated validation to ensure all THING_TYPE_X
        // constants are included"
        ValidationResult result = SonoffBindingConstants.validateDeviceMappings();

        // Print any errors related to missing THING_TYPE constants
        result.getErrors().stream().filter(
                error -> error.contains("THING_TYPE") && error.contains("not included in SUPPORTED_THING_TYPE_UIDS"))
                .forEach(error -> System.out.println("Missing THING_TYPE Error: " + error));

        // Print warnings about UNKNOWNDEVICE (which might be intentionally excluded)
        result.getWarnings().stream().filter(warning -> warning.contains("THING_TYPE_UNKNOWNDEVICE"))
                .forEach(warning -> System.out.println("UNKNOWNDEVICE Warning: " + warning));

        // Count errors related to missing THING_TYPE constants
        long missingConstantErrors = result.getErrors().stream().filter(
                error -> error.contains("THING_TYPE") && error.contains("not included in SUPPORTED_THING_TYPE_UIDS"))
                .count();

        // The automated validation should catch any missing THING_TYPE constants
        assertEquals(0, missingConstantErrors,
                "Found THING_TYPE constants missing from SUPPORTED_THING_TYPE_UIDS - automated validation is working!");

        System.out.println("✅ Automated validation successfully verified all THING_TYPE constants are included");
    }
}
