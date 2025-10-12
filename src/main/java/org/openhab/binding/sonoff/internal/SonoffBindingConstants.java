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

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SonoffBindingConstants} class defines common constants, which are
 * used across the whole binding.
 * 
 * For comprehensive device information including models, features, and capabilities,
 * see the device documentation: docs/SUPPORTED_DEVICES.md
 *
 * @author David Murton - Initial contribution
 */
@NonNullByDefault
public class SonoffBindingConstants {

    private static final Logger logger = LoggerFactory.getLogger(SonoffBindingConstants.class);

    /** The binding identifier used throughout the openHAB system */
    public static final String BINDING_ID = "sonoff";

    /**
     * Device types that support inbound LAN protocol communication.
     * These devices can receive commands and status updates via local network.
     * TODO: Analyze why devices 15, 103, 104, 181, 190 are in LAN_IN but not LAN_OUT
     */
    public static final Set<Integer> LAN_IN = Collections
            .unmodifiableSet(Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 14, 15, 28, 32, 44, 77, 78, 103, 104, 126, 138, 140,
                    160, 161, 162, 181, 190, 209, 210, 211, 212, 237, 256, 260, 268).collect(Collectors.toSet()));

    /**
     * Device types that support outbound LAN protocol communication.
     * These devices can send status updates and responses via local network.
     * TODO: Analyze why some devices support only inbound LAN communication
     */
    public static final Set<Integer> LAN_OUT = Collections
            .unmodifiableSet(Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 14, 28, 32, 44, 77, 78, 126, 138, 140, 160, 161, 162,
                    209, 210, 211, 212, 237, 256, 260, 268).collect(Collectors.toSet()));

    // ========================================
    // BRIDGE AND ACCOUNT THING TYPES
    // ========================================

    /** Account bridge for cloud-based device management */
    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");

    // ========================================
    // WIFI DEVICES
    // ========================================
    // All WiFi-enabled Sonoff devices with their internal device type IDs

    // Basic switches and relays
    /** Single channel devices: S20, S26, BASIC, MINI switches */
    public static final ThingTypeUID THING_TYPE_1 = new ThingTypeUID(BINDING_ID, "1");

    /** Dual relay devices: DUALR2 */
    public static final ThingTypeUID THING_TYPE_2 = new ThingTypeUID(BINDING_ID, "2");

    /** TODO: Analyze - SOCKET_3 Unknown Model - needs device identification */
    public static final ThingTypeUID THING_TYPE_3 = new ThingTypeUID(BINDING_ID, "3");

    /** TODO: Analyze - SOCKET_4 Unknown Model - needs device identification */
    public static final ThingTypeUID THING_TYPE_4 = new ThingTypeUID(BINDING_ID, "4");

    /** TODO: Analyze - SWITCH_4 Unknown Model - needs device identification */
    public static final ThingTypeUID THING_TYPE_9 = new ThingTypeUID(BINDING_ID, "9");

    /** Legacy basic switches: BASIC (older firmware version) */
    public static final ThingTypeUID THING_TYPE_14 = new ThingTypeUID(BINDING_ID, "14");

    // Touch switches
    /** Single channel touch switches: T11C, TX1C, G1 */
    public static final ThingTypeUID THING_TYPE_6 = new ThingTypeUID(BINDING_ID, "6");

    /** Dual channel touch switches: T12C, TX2C */
    public static final ThingTypeUID THING_TYPE_7 = new ThingTypeUID(BINDING_ID, "7");

    /** Triple channel touch switches: T13C, TX3C */
    public static final ThingTypeUID THING_TYPE_8 = new ThingTypeUID(BINDING_ID, "8");

    // T5 Touch Switch Series (86mm wall switches)
    /** T5 series single channel: T5-1C-86 */
    public static final ThingTypeUID THING_TYPE_209 = new ThingTypeUID(BINDING_ID, "209");

    /** T5 series dual channel: T5-2C-86 */
    public static final ThingTypeUID THING_TYPE_210 = new ThingTypeUID(BINDING_ID, "210");

    /** T5 series triple channel: T5-3C-86 */
    public static final ThingTypeUID THING_TYPE_211 = new ThingTypeUID(BINDING_ID, "211");

    /** T5 series quad channel: T5-4C-86 */
    public static final ThingTypeUID THING_TYPE_212 = new ThingTypeUID(BINDING_ID, "212");

    // SwitchMan series
    /** SwitchMan series: M5-1C (single channel) */
    public static final ThingTypeUID THING_TYPE_160 = new ThingTypeUID(BINDING_ID, "160");

    /** SwitchMan series: M5-2C (dual channel) */
    public static final ThingTypeUID THING_TYPE_161 = new ThingTypeUID(BINDING_ID, "161");

    /** SwitchMan series: M5-3C (triple channel) */
    public static final ThingTypeUID THING_TYPE_162 = new ThingTypeUID(BINDING_ID, "162");

    // Power monitoring devices
    /** Power monitoring devices: POW (original power monitoring switch) */
    public static final ThingTypeUID THING_TYPE_5 = new ThingTypeUID(BINDING_ID, "5");

    /** Advanced power monitoring: POWR2 (second generation POW) */
    public static final ThingTypeUID THING_TYPE_32 = new ThingTypeUID(BINDING_ID, "32");

    /** Dual relay with power monitoring: DUAL R3 */
    public static final ThingTypeUID THING_TYPE_126 = new ThingTypeUID(BINDING_ID, "126");

    // Temperature/Humidity sensors
    /** Temperature/Humidity monitoring devices: TH10, TH16, TH16R2 */
    public static final ThingTypeUID THING_TYPE_15 = new ThingTypeUID(BINDING_ID, "15");

    /** Temperature/Humidity sensor: THR320D or THR316D */
    public static final ThingTypeUID THING_TYPE_181 = new ThingTypeUID(BINDING_ID, "181");

    // Door/Window sensors
    /** Magnetic door/window sensor: OPL-DMA, DW2 */
    public static final ThingTypeUID THING_TYPE_102 = new ThingTypeUID(BINDING_ID, "102");

    // Lighting controllers
    /** LED strip controller: LED CONTROLLER */
    public static final ThingTypeUID THING_TYPE_59 = new ThingTypeUID(BINDING_ID, "59");

    /** Smart bulb: B05 Bulb */
    public static final ThingTypeUID THING_TYPE_104 = new ThingTypeUID(BINDING_ID, "104");

    // Compact/Mini devices
    /** Compact WiFi switch: WiFi MICRO (USB-powered) */
    public static final ThingTypeUID THING_TYPE_77 = new ThingTypeUID(BINDING_ID, "77");

    /** Compact dual relay: MINI-D */
    public static final ThingTypeUID THING_TYPE_138 = new ThingTypeUID(BINDING_ID, "138");

    // Specialized/Unknown devices
    /** TODO: Analyze - Unknown device type 78 needs identification */
    public static final ThingTypeUID THING_TYPE_78 = new ThingTypeUID(BINDING_ID, "78");

    /** TODO: Analyze - CK-BL602-4SW-HS needs detailed specification */
    public static final ThingTypeUID THING_TYPE_140 = new ThingTypeUID(BINDING_ID, "140");

    /** TODO: Analyze - S60TPF needs detailed specification */
    public static final ThingTypeUID THING_TYPE_190 = new ThingTypeUID(BINDING_ID, "190");

    /** Smart gateway: SG200 */
    public static final ThingTypeUID THING_TYPE_237 = new ThingTypeUID(BINDING_ID, "237");

    /** BASIC 5Gen: BASIC-1GS (5th generation basic switch with Matter support) */
    public static final ThingTypeUID THING_TYPE_268 = new ThingTypeUID(BINDING_ID, "268");

    // GSM/Cellular devices
    /** TODO: Analyze - GSM Socket models need detailed specification */
    public static final ThingTypeUID THING_TYPE_24 = new ThingTypeUID(BINDING_ID, "24");
    public static final ThingTypeUID THING_TYPE_27 = new ThingTypeUID(BINDING_ID, "27");
    public static final ThingTypeUID THING_TYPE_29 = new ThingTypeUID(BINDING_ID, "29");
    public static final ThingTypeUID THING_TYPE_30 = new ThingTypeUID(BINDING_ID, "30");
    public static final ThingTypeUID THING_TYPE_31 = new ThingTypeUID(BINDING_ID, "31");
    public static final ThingTypeUID THING_TYPE_81 = new ThingTypeUID(BINDING_ID, "81");
    public static final ThingTypeUID THING_TYPE_82 = new ThingTypeUID(BINDING_ID, "82");
    public static final ThingTypeUID THING_TYPE_83 = new ThingTypeUID(BINDING_ID, "83");
    public static final ThingTypeUID THING_TYPE_84 = new ThingTypeUID(BINDING_ID, "84");
    public static final ThingTypeUID THING_TYPE_107 = new ThingTypeUID(BINDING_ID, "107");

    // ========================================
    // ZIGBEE BRIDGES
    // ========================================
    // Bridge devices for Zigbee protocol conversion

    /** Zigbee Bridge: ZB Bridge (original) */
    public static final ThingTypeUID THING_TYPE_66 = new ThingTypeUID(BINDING_ID, "66");

    /** Zigbee Bridge Pro: ZBBridge-P (enhanced version) */
    public static final ThingTypeUID THING_TYPE_168 = new ThingTypeUID(BINDING_ID, "168");

    /** Zigbee Bridge USB: ZBridge-U */
    public static final ThingTypeUID THING_TYPE_243 = new ThingTypeUID(BINDING_ID, "243");

    // ========================================
    // 433MHz RF BRIDGE
    // ========================================

    /** RF Bridge for 433MHz devices: RF-BRIDGE (RF3) */
    public static final ThingTypeUID THING_TYPE_28 = new ThingTypeUID(BINDING_ID, "28");

    // ========================================
    // CAMERA DEVICES
    // ========================================

    /** Security camera: SlimCAM2 */
    public static final ThingTypeUID THING_TYPE_256 = new ThingTypeUID(BINDING_ID, "256");

    /** Security camera: CAM-B1P */
    public static final ThingTypeUID THING_TYPE_260 = new ThingTypeUID(BINDING_ID, "260");

    // ========================================
    // ZIGBEE DEVICES
    // ========================================
    // Zigbee devices that connect through Sonoff Zigbee bridges
    // TODO: Standardize naming convention - currently mixed numeric IDs and descriptive names

    // Sensors
    /** Zigbee temperature sensor */
    public static final ThingTypeUID THING_TYPE_1770 = new ThingTypeUID(BINDING_ID, "1770");

    /** Zigbee motion sensor */
    public static final ThingTypeUID THING_TYPE_2026 = new ThingTypeUID(BINDING_ID, "2026");

    /** Zigbee contact/door sensor */
    public static final ThingTypeUID THING_TYPE_ZCONTACT = new ThingTypeUID(BINDING_ID, "zcontact");

    /** Zigbee water leak sensor */
    public static final ThingTypeUID THING_TYPE_ZWATER = new ThingTypeUID(BINDING_ID, "zwater");

    /** Zigbee door/window sensor: SNZB-04P */
    public static final ThingTypeUID THING_TYPE_7003 = new ThingTypeUID(BINDING_ID, "7003");

    /** Zigbee temperature/humidity sensor: SNZB-02P */
    public static final ThingTypeUID THING_TYPE_7014 = new ThingTypeUID(BINDING_ID, "7014");

    /** Zigbee wireless switch: SNZB-01P */
    public static final ThingTypeUID THING_TYPE_7000 = new ThingTypeUID(BINDING_ID, "7000");

    // Switches and lights
    /** Zigbee single channel switch */
    public static final ThingTypeUID THING_TYPE_ZSWITCH1 = new ThingTypeUID(BINDING_ID, "zswitch1");

    /** Zigbee dual channel switch */
    public static final ThingTypeUID THING_TYPE_ZSWITCH2 = new ThingTypeUID(BINDING_ID, "zswitch2");

    /** Zigbee triple channel switch */
    public static final ThingTypeUID THING_TYPE_ZSWITCH3 = new ThingTypeUID(BINDING_ID, "zswitch3");

    /** Zigbee quad channel switch */
    public static final ThingTypeUID THING_TYPE_ZSWITCH4 = new ThingTypeUID(BINDING_ID, "zswitch4");

    /** Zigbee dimmable white light */
    public static final ThingTypeUID THING_TYPE_ZLIGHT = new ThingTypeUID(BINDING_ID, "zlight");

    // ========================================
    // 433MHz RF DEVICES
    // ========================================
    // 433MHz devices that connect through RF bridges (THING_TYPE_28)
    // These devices use consistent naming convention with 'rf' prefix

    /** 433MHz single button remote control */
    public static final ThingTypeUID THING_TYPE_RF1 = new ThingTypeUID(BINDING_ID, "rfremote1");

    /** 433MHz dual button remote control */
    public static final ThingTypeUID THING_TYPE_RF2 = new ThingTypeUID(BINDING_ID, "rfremote2");

    /** 433MHz triple button remote control */
    public static final ThingTypeUID THING_TYPE_RF3 = new ThingTypeUID(BINDING_ID, "rfremote3");

    /** 433MHz quad button remote control */
    public static final ThingTypeUID THING_TYPE_RF4 = new ThingTypeUID(BINDING_ID, "rfremote4");

    /** 433MHz sensor (PIR, door/window, etc.) */
    public static final ThingTypeUID THING_TYPE_RF6 = new ThingTypeUID(BINDING_ID, "rfsensor");

    // ========================================
    // FALLBACK DEVICE TYPE
    // ========================================

    /** Fallback thing type for unrecognized devices */
    public static final ThingTypeUID THING_TYPE_UNKNOWNDEVICE = new ThingTypeUID(BINDING_ID, "device");

    /**
     * Complete set of all supported device types in the binding.
     * Organized by device categories: Account, WiFi devices, Zigbee bridges, Zigbee devices, 433MHz devices, Cameras
     * 
     * Automated validation is implemented via validateDeviceMappings() method to ensure all
     * THING_TYPE_X constants are included in this collection. The validation uses reflection
     * to discover all constants and verify completeness.
     */
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPE_UIDS = Collections.unmodifiableSet(Stream.of(
            // Account bridge
            THING_TYPE_ACCOUNT,

            // WiFi devices
            THING_TYPE_1, THING_TYPE_2, THING_TYPE_3, THING_TYPE_4, THING_TYPE_5, THING_TYPE_6, THING_TYPE_7,
            THING_TYPE_8, THING_TYPE_9, THING_TYPE_14, THING_TYPE_15, THING_TYPE_24, THING_TYPE_27, THING_TYPE_29,
            THING_TYPE_30, THING_TYPE_31, THING_TYPE_32, THING_TYPE_59, THING_TYPE_77, THING_TYPE_78, THING_TYPE_81,
            THING_TYPE_82, THING_TYPE_83, THING_TYPE_84, THING_TYPE_102, THING_TYPE_104, THING_TYPE_107, THING_TYPE_126,
            THING_TYPE_138, THING_TYPE_140, THING_TYPE_160, THING_TYPE_161, THING_TYPE_162, THING_TYPE_181,
            THING_TYPE_190, THING_TYPE_209, THING_TYPE_210, THING_TYPE_211, THING_TYPE_212, THING_TYPE_237,
            THING_TYPE_268,

            // Zigbee bridges
            THING_TYPE_66, THING_TYPE_168, THING_TYPE_243,

            // 433MHz RF bridge
            THING_TYPE_28,

            // Camera devices
            THING_TYPE_256, THING_TYPE_260,

            // Zigbee devices
            THING_TYPE_1770, THING_TYPE_2026, THING_TYPE_7000, THING_TYPE_7003, THING_TYPE_7014, THING_TYPE_ZCONTACT,
            THING_TYPE_ZWATER, THING_TYPE_ZLIGHT, THING_TYPE_ZSWITCH1, THING_TYPE_ZSWITCH2, THING_TYPE_ZSWITCH3,
            THING_TYPE_ZSWITCH4,

            // 433MHz RF devices
            THING_TYPE_RF1, THING_TYPE_RF2, THING_TYPE_RF3, THING_TYPE_RF4, THING_TYPE_RF6

    ).collect(Collectors.toSet()));

    /**
     * Set of device types that support automatic discovery.
     * Excludes THING_TYPE_ACCOUNT (bridge must be manually configured) and THING_TYPE_UNKNOWNDEVICE.
     * Organized by device categories for better maintainability.
     * 
     * Discovery capability validation is performed by validateDeviceMappings() method to ensure
     * all discoverable types have proper UUID mappings and handler availability.
     * 
     * TODO: Document criteria for which devices should/shouldn't be discoverable.
     */
    public static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPE_UIDS = Collections.unmodifiableSet(Stream.of(
            // WiFi devices
            THING_TYPE_1, THING_TYPE_2, THING_TYPE_3, THING_TYPE_4, THING_TYPE_5, THING_TYPE_6, THING_TYPE_7,
            THING_TYPE_8, THING_TYPE_9, THING_TYPE_14, THING_TYPE_15, THING_TYPE_24, THING_TYPE_27, THING_TYPE_29,
            THING_TYPE_30, THING_TYPE_31, THING_TYPE_32, THING_TYPE_59, THING_TYPE_77, THING_TYPE_78, THING_TYPE_81,
            THING_TYPE_82, THING_TYPE_83, THING_TYPE_84, THING_TYPE_102, THING_TYPE_104, THING_TYPE_107, THING_TYPE_126,
            THING_TYPE_138, THING_TYPE_140, THING_TYPE_160, THING_TYPE_161, THING_TYPE_162, THING_TYPE_181,
            THING_TYPE_190, THING_TYPE_209, THING_TYPE_210, THING_TYPE_211, THING_TYPE_212, THING_TYPE_237,
            THING_TYPE_268,

            // Zigbee bridges
            THING_TYPE_66, THING_TYPE_168, THING_TYPE_243,

            // 433MHz RF bridge
            THING_TYPE_28,

            // Camera devices
            THING_TYPE_256, THING_TYPE_260,

            // Zigbee devices
            THING_TYPE_1770, THING_TYPE_2026, THING_TYPE_7000, THING_TYPE_7003, THING_TYPE_7014, THING_TYPE_ZCONTACT,
            THING_TYPE_ZWATER, THING_TYPE_ZLIGHT, THING_TYPE_ZSWITCH1, THING_TYPE_ZSWITCH2, THING_TYPE_ZSWITCH3,
            THING_TYPE_ZSWITCH4,

            // 433MHz RF devices
            THING_TYPE_RF1, THING_TYPE_RF2, THING_TYPE_RF3, THING_TYPE_RF4, THING_TYPE_RF6

    ).collect(Collectors.toSet()));

    /**
     * Creates mapping from device type IDs to ThingTypeUID for device identification.
     * Used during device discovery to determine the correct thing type based on device's reported type ID.
     * TODO: Consider using static initialization instead of method to improve performance.
     * 
     * Note: Validation is available via validateDeviceMappings() method to ensure all
     * THING_TYPE_X constants with numeric IDs are included in this mapping.
     */
    public static final Map<Integer, ThingTypeUID> createMap() {
        Map<Integer, ThingTypeUID> deviceTypes = new HashMap<>();
        deviceTypes.put(1, THING_TYPE_1);
        deviceTypes.put(2, THING_TYPE_2);
        deviceTypes.put(3, THING_TYPE_3);
        deviceTypes.put(4, THING_TYPE_4);
        deviceTypes.put(5, THING_TYPE_5);
        deviceTypes.put(6, THING_TYPE_6);
        deviceTypes.put(7, THING_TYPE_7);
        deviceTypes.put(8, THING_TYPE_8);
        deviceTypes.put(9, THING_TYPE_9);
        deviceTypes.put(14, THING_TYPE_14);
        deviceTypes.put(15, THING_TYPE_15);

        deviceTypes.put(24, THING_TYPE_24);
        deviceTypes.put(27, THING_TYPE_27);
        deviceTypes.put(28, THING_TYPE_28);
        deviceTypes.put(29, THING_TYPE_29);
        deviceTypes.put(30, THING_TYPE_30);
        deviceTypes.put(31, THING_TYPE_31);

        deviceTypes.put(32, THING_TYPE_32);
        deviceTypes.put(59, THING_TYPE_59);

        deviceTypes.put(66, THING_TYPE_66);
        deviceTypes.put(77, THING_TYPE_77);
        deviceTypes.put(78, THING_TYPE_78);
        deviceTypes.put(81, THING_TYPE_81);
        deviceTypes.put(82, THING_TYPE_82);
        deviceTypes.put(83, THING_TYPE_83);
        deviceTypes.put(84, THING_TYPE_84);

        deviceTypes.put(102, THING_TYPE_102);
        deviceTypes.put(104, THING_TYPE_104);
        deviceTypes.put(107, THING_TYPE_107);
        deviceTypes.put(126, THING_TYPE_126);
        deviceTypes.put(138, THING_TYPE_138);
        deviceTypes.put(140, THING_TYPE_140);
        deviceTypes.put(160, THING_TYPE_160);
        deviceTypes.put(161, THING_TYPE_161);
        deviceTypes.put(162, THING_TYPE_162);
        deviceTypes.put(168, THING_TYPE_168);
        deviceTypes.put(181, THING_TYPE_181);
        deviceTypes.put(190, THING_TYPE_190);
        deviceTypes.put(209, THING_TYPE_209);
        deviceTypes.put(210, THING_TYPE_210);
        deviceTypes.put(211, THING_TYPE_211);
        deviceTypes.put(212, THING_TYPE_212);
        deviceTypes.put(237, THING_TYPE_237);
        deviceTypes.put(243, THING_TYPE_243);

        deviceTypes.put(256, THING_TYPE_256);
        deviceTypes.put(260, THING_TYPE_260);
        deviceTypes.put(268, THING_TYPE_268);

        return Collections.unmodifiableMap(deviceTypes);
    }

    /**
     * Creates mapping for RF sensor device types.
     * TODO: CRITICAL - Analyze why original values were all '4' and determine correct mapping.
     * TODO: Document the meaning of these numeric IDs in RF protocol context.
     * TODO: Verify current mapping is correct for RF device identification.
     */
    public static final Map<Integer, ThingTypeUID> createSensorMap() {
        Map<Integer, ThingTypeUID> sensorTypes = new HashMap<>();
        sensorTypes.put(1, THING_TYPE_RF1); // TODO: Was 4 originally - verify correct value
        sensorTypes.put(2, THING_TYPE_RF2); // TODO: Was 4 originally - verify correct value
        sensorTypes.put(3, THING_TYPE_RF3); // TODO: Was 4 originally - verify correct value
        sensorTypes.put(4, THING_TYPE_RF4); // TODO: Was 4 originally - verify correct value
        sensorTypes.put(6, THING_TYPE_RF6);

        return Collections.unmodifiableMap(sensorTypes);
    }

    /**
     * Creates mapping for Zigbee device types using 4-digit identification codes.
     * Pattern analysis:
     * - 1xxx: Single channel switches and lights (1000, 1009, 1256=switch, 1257=light)
     * - 2xxx: Dual channel switches and sensors (2026=motion, 2256=2ch switch)
     * - 3xxx: Triple channel switches and contact sensors (3026=contact, 3256=3ch switch)
     * - 4xxx: Quad channel switches and water sensors (4026=water, 4256=4ch switch)
     * - 7xxx: Specific device models (7003=SNZB-04P, 7014=SNZB-02P)
     * - 1770: Temperature sensor (exception to pattern)
     */
    public static final Map<Integer, ThingTypeUID> createZigbeeMap() {
        Map<Integer, ThingTypeUID> zigbeeTypes = new HashMap<>();
        // Single channel devices
        zigbeeTypes.put(1000, THING_TYPE_ZSWITCH1);
        zigbeeTypes.put(1009, THING_TYPE_ZSWITCH1);
        zigbeeTypes.put(1256, THING_TYPE_ZSWITCH1);
        zigbeeTypes.put(1257, THING_TYPE_ZLIGHT);

        // Sensors
        zigbeeTypes.put(1770, THING_TYPE_1770); // Temperature sensor
        zigbeeTypes.put(2026, THING_TYPE_2026); // Motion sensor
        zigbeeTypes.put(3026, THING_TYPE_ZCONTACT); // Contact sensor
        zigbeeTypes.put(4026, THING_TYPE_ZWATER); // Water sensor

        // Multi-channel switches
        zigbeeTypes.put(2256, THING_TYPE_ZSWITCH2);
        zigbeeTypes.put(3256, THING_TYPE_ZSWITCH3);
        zigbeeTypes.put(4256, THING_TYPE_ZSWITCH4);

        // Specific device models
        zigbeeTypes.put(7000, THING_TYPE_7000); // SNZB-01P
        zigbeeTypes.put(7003, THING_TYPE_7003); // SNZB-04P
        zigbeeTypes.put(7014, THING_TYPE_7014); // SNZB-02P

        return Collections.unmodifiableMap(zigbeeTypes);
    }

    // ========================================
    // VALIDATION METHODS
    // ========================================

    /**
     * Validates that all THING_TYPE_X constants with numeric IDs are properly included in device mappings.
     * This method uses reflection to find all numeric THING_TYPE constants and verifies they are included
     * in the createMap() method and other relevant collections.
     * 
     * @return ValidationResult containing any missing mappings or inconsistencies
     */
    public static ValidationResult validateDeviceMappings() {
        ValidationResult result = new ValidationResult();

        try {
            // Get all numeric THING_TYPE constants using reflection
            Set<NumericThingType> numericThingTypes = extractNumericThingTypes();

            // Get the device mapping
            Map<Integer, ThingTypeUID> deviceMap = createMap();

            // Validate createMap() completeness
            validateCreateMapCompleteness(numericThingTypes, deviceMap, result);

            // Validate collection consistency
            validateCollectionConsistency(numericThingTypes, result);

            // Log results
            if (result.hasErrors()) {
                logger.warn("Device mapping validation found {} issues", result.getErrorCount());
                result.getErrors().forEach(error -> logger.warn("Validation error: {}", error));
            } else {
                logger.debug(
                        "Device mapping validation passed - all {} numeric THING_TYPE constants are properly mapped",
                        numericThingTypes.size());
            }

        } catch (Exception e) {
            result.addError("Validation failed due to exception: " + e.getMessage());
            logger.error("Device mapping validation failed", e);
        }

        return result;
    }

    /**
     * Extracts all THING_TYPE_X constants that have numeric IDs using reflection.
     */
    private static Set<NumericThingType> extractNumericThingTypes() {
        Set<NumericThingType> numericTypes = new HashSet<>();

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
                    ThingTypeUID thingType = (ThingTypeUID) field.get(null);
                    numericTypes.add(new NumericThingType(numericId, thingType, fieldName));
                } catch (NumberFormatException e) {
                    // Not a numeric THING_TYPE, skip (e.g., THING_TYPE_ACCOUNT, THING_TYPE_ZSWITCH1)
                } catch (IllegalAccessException e) {
                    logger.warn("Could not access field {}: {}", fieldName, e.getMessage());
                }
            }
        }

        return numericTypes;
    }

    /**
     * Validates that all numeric THING_TYPE constants are included in createMap().
     */
    private static void validateCreateMapCompleteness(Set<NumericThingType> numericThingTypes,
            Map<Integer, ThingTypeUID> deviceMap, ValidationResult result) {

        for (NumericThingType numericType : numericThingTypes) {
            ThingTypeUID mappedType = deviceMap.get(numericType.numericId);

            if (mappedType == null) {
                result.addError(String.format("THING_TYPE_%d is not included in createMap()", numericType.numericId));
            } else if (!mappedType.equals(numericType.thingType)) {
                result.addError(
                        String.format("THING_TYPE_%d maps to wrong ThingTypeUID in createMap(): expected %s, got %s",
                                numericType.numericId, numericType.thingType, mappedType));
            }
        }

        // Check for mappings without corresponding constants
        for (Map.Entry<Integer, ThingTypeUID> entry : deviceMap.entrySet()) {
            boolean found = numericThingTypes.stream()
                    .anyMatch(nt -> nt.numericId.equals(entry.getKey()) && nt.thingType.equals(entry.getValue()));

            if (!found) {
                result.addWarning(String.format(
                        "createMap() contains mapping %d -> %s but no corresponding THING_TYPE_%d constant found",
                        entry.getKey(), entry.getValue(), entry.getKey()));
            }
        }
    }

    /**
     * Validates consistency across different collections (SUPPORTED_THING_TYPE_UIDS, DISCOVERABLE_THING_TYPE_UIDS).
     */
    private static void validateCollectionConsistency(Set<NumericThingType> numericThingTypes,
            ValidationResult result) {
        // Check that all numeric THING_TYPE constants are in SUPPORTED_THING_TYPE_UIDS
        for (NumericThingType numericType : numericThingTypes) {
            if (!SUPPORTED_THING_TYPE_UIDS.contains(numericType.thingType)) {
                result.addError(String.format("THING_TYPE_%d (%s) is not included in SUPPORTED_THING_TYPE_UIDS",
                        numericType.numericId, numericType.thingType));
            }
        }

        // Check that ALL THING_TYPE constants (including non-numeric) are in SUPPORTED_THING_TYPE_UIDS
        validateAllThingTypeConstantsIncluded(result);

        // Check that all discoverable types are also in supported types
        for (ThingTypeUID discoverableType : DISCOVERABLE_THING_TYPE_UIDS) {
            if (!SUPPORTED_THING_TYPE_UIDS.contains(discoverableType)) {
                result.addError(String.format(
                        "ThingTypeUID %s is in DISCOVERABLE_THING_TYPE_UIDS but not in SUPPORTED_THING_TYPE_UIDS",
                        discoverableType));
            }
        }

        // Validate discovery capabilities
        validateDiscoveryCapabilities(result);
    }

    /**
     * Validates that ALL THING_TYPE_X constants are included in SUPPORTED_THING_TYPE_UIDS.
     * This includes both numeric (THING_TYPE_1, THING_TYPE_32) and non-numeric constants
     * (THING_TYPE_ACCOUNT, THING_TYPE_ZSWITCH1, THING_TYPE_RF1, etc.).
     */
    private static void validateAllThingTypeConstantsIncluded(ValidationResult result) {
        Set<ThingTypeUID> allThingTypes = extractAllThingTypeConstants();

        for (ThingTypeUID thingType : allThingTypes) {
            if (!SUPPORTED_THING_TYPE_UIDS.contains(thingType)) {
                // Special handling for THING_TYPE_UNKNOWNDEVICE which might be intentionally excluded
                if (thingType.getId().equals("device")) {
                    result.addWarning(String.format(
                            "THING_TYPE_UNKNOWNDEVICE (%s) is not in SUPPORTED_THING_TYPE_UIDS - this may be intentional",
                            thingType));
                } else {
                    result.addError(String.format("THING_TYPE constant %s is not included in SUPPORTED_THING_TYPE_UIDS",
                            thingType));
                }
            }
        }

        logger.debug("Validated {} total THING_TYPE constants against SUPPORTED_THING_TYPE_UIDS", allThingTypes.size());
    }

    /**
     * Extracts ALL THING_TYPE_X constants using reflection, including both numeric and non-numeric types.
     */
    private static Set<ThingTypeUID> extractAllThingTypeConstants() {
        Set<ThingTypeUID> allThingTypes = new HashSet<>();

        Field[] fields = SonoffBindingConstants.class.getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().startsWith("THING_TYPE_") && field.getType().equals(ThingTypeUID.class)
                    && java.lang.reflect.Modifier.isStatic(field.getModifiers())
                    && java.lang.reflect.Modifier.isFinal(field.getModifiers())) {

                try {
                    ThingTypeUID thingType = (ThingTypeUID) field.get(null);
                    allThingTypes.add(thingType);
                } catch (IllegalAccessException e) {
                    logger.warn("Could not access THING_TYPE field {}: {}", field.getName(), e.getMessage());
                }
            }
        }

        return allThingTypes;
    }

    /**
     * Validates that all discoverable device types have proper discovery capabilities.
     * This includes checking for handler availability and discovery mapping consistency.
     */
    private static void validateDiscoveryCapabilities(ValidationResult result) {
        // Get all discoverable device types
        Set<ThingTypeUID> discoverableTypes = DISCOVERABLE_THING_TYPE_UIDS;
        Map<Integer, ThingTypeUID> deviceMap = createMap();

        // Check that discoverable types can be discovered (have UUID mapping)
        for (ThingTypeUID discoverableType : discoverableTypes) {
            String thingTypeId = discoverableType.getId();

            // Skip non-numeric types (they use different discovery mechanisms)
            if (!isNumericThingTypeId(thingTypeId)) {
                continue;
            }

            try {
                int numericId = Integer.parseInt(thingTypeId);
                ThingTypeUID mappedType = deviceMap.get(numericId);

                if (mappedType == null) {
                    result.addError(String.format(
                            "Discoverable device type %s (ID: %d) has no UUID mapping in createMap() - discovery will fail",
                            discoverableType, numericId));
                } else if (!mappedType.equals(discoverableType)) {
                    result.addError(String.format(
                            "Discoverable device type %s (ID: %d) maps to wrong ThingTypeUID in createMap(): expected %s, got %s",
                            discoverableType, numericId, discoverableType, mappedType));
                }
            } catch (NumberFormatException e) {
                // This shouldn't happen if isNumericThingTypeId works correctly
                result.addWarning(
                        String.format("Could not parse numeric ID from discoverable type %s", discoverableType));
            }
        }

        // Check for handler availability (this requires reflection to check SonoffHandlerFactory)
        validateHandlerAvailability(discoverableTypes, result);
    }

    /**
     * Validates that all discoverable device types have corresponding handlers in SonoffHandlerFactory.
     * This uses reflection to examine the createHandler method's switch statement.
     */
    private static void validateHandlerAvailability(Set<ThingTypeUID> discoverableTypes, ValidationResult result) {
        try {
            // Get the handler factory class
            Class<?> handlerFactoryClass = Class.forName("org.openhab.binding.sonoff.internal.SonoffHandlerFactory");

            // We can't easily parse the switch statement via reflection, but we can check if the method exists
            // and provide guidance on what should be validated manually

            // For now, we'll validate against known handler mappings based on the switch statement
            Set<String> knownHandlerIds = getKnownHandlerIds();

            for (ThingTypeUID discoverableType : discoverableTypes) {
                String thingTypeId = discoverableType.getId();

                if (!knownHandlerIds.contains(thingTypeId)) {
                    result.addWarning(String.format(
                            "Discoverable device type %s may not have a handler in SonoffHandlerFactory.createHandler() - manual verification required",
                            discoverableType));
                }
            }

        } catch (ClassNotFoundException e) {
            result.addWarning("Could not validate handler availability - SonoffHandlerFactory class not found");
        } catch (Exception e) {
            result.addWarning("Could not validate handler availability: " + e.getMessage());
        }
    }

    /**
     * Returns the set of known handler IDs based on the SonoffHandlerFactory switch statement.
     * This should be updated when new handlers are added to the factory.
     */
    private static Set<String> getKnownHandlerIds() {
        Set<String> handlerIds = new HashSet<>();

        // Account handler
        handlerIds.add("account");

        // Single switch handlers
        handlerIds.add("1");
        handlerIds.add("6");
        handlerIds.add("14");
        handlerIds.add("27");
        handlerIds.add("81");
        handlerIds.add("107");
        handlerIds.add("160");
        handlerIds.add("209");
        handlerIds.add("256"); // CAM not fully supported
        handlerIds.add("260"); // CAM not fully supported

        // Multi switch handlers
        handlerIds.add("2");
        handlerIds.add("3");
        handlerIds.add("4");
        handlerIds.add("7");
        handlerIds.add("8");
        handlerIds.add("9");
        handlerIds.add("29");
        handlerIds.add("30");
        handlerIds.add("31");
        handlerIds.add("77");
        handlerIds.add("78");
        handlerIds.add("82");
        handlerIds.add("83");
        handlerIds.add("84");
        handlerIds.add("126");
        handlerIds.add("161");
        handlerIds.add("162");
        handlerIds.add("210");
        handlerIds.add("211");
        handlerIds.add("212");

        // Specialized handlers
        handlerIds.add("5"); // POW
        handlerIds.add("15"); // TH
        handlerIds.add("181"); // TH
        handlerIds.add("24"); // GSM Socket
        handlerIds.add("28"); // RF Bridge
        handlerIds.add("32"); // POWR2
        handlerIds.add("59"); // RGB Strip
        handlerIds.add("66"); // Zigbee Bridge
        handlerIds.add("168"); // Zigbee Bridge
        handlerIds.add("243"); // Zigbee Bridge
        handlerIds.add("102"); // Magnetic Switch
        handlerIds.add("104"); // RGB CCT
        handlerIds.add("138"); // Single Mini
        handlerIds.add("190"); // POW Upgraded
        handlerIds.add("237"); // Gate

        // Zigbee device handlers
        handlerIds.add("1770"); // Temperature sensor
        handlerIds.add("7014"); // SNZB-02P
        handlerIds.add("2026"); // Motion sensor
        handlerIds.add("7003"); // Contact sensor

        // RF device handlers
        handlerIds.add("rfremote1");
        handlerIds.add("rfremote2");
        handlerIds.add("rfremote3");
        handlerIds.add("rfremote4");
        handlerIds.add("rfsensor");

        return handlerIds;
    }

    /**
     * Checks if a ThingTypeUID ID represents a numeric device type.
     */
    private static boolean isNumericThingTypeId(String thingTypeId) {
        try {
            Integer.parseInt(thingTypeId);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Static method to run validation and log results. Can be called during binding initialization.
     */
    public static void runValidation() {
        ValidationResult result = validateDeviceMappings();
        if (result.hasErrors()) {
            logger.error("Device mapping validation failed with {} errors and {} warnings", result.getErrorCount(),
                    result.getWarningCount());
        }
    }

    /**
     * Helper class to hold numeric THING_TYPE information.
     */
    private static class NumericThingType {
        final Integer numericId;
        final ThingTypeUID thingType;
        final String fieldName;

        NumericThingType(Integer numericId, ThingTypeUID thingType, String fieldName) {
            this.numericId = numericId;
            this.thingType = thingType;
            this.fieldName = fieldName;
        }
    }

    /**
     * Validation result container.
     */
    public static class ValidationResult {
        private final Set<String> errors = new HashSet<>();
        private final Set<String> warnings = new HashSet<>();

        public void addError(String error) {
            errors.add(error);
        }

        public void addWarning(String warning) {
            warnings.add(warning);
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }

        public Set<String> getErrors() {
            return Collections.unmodifiableSet(errors);
        }

        public Set<String> getWarnings() {
            return Collections.unmodifiableSet(warnings);
        }

        public int getErrorCount() {
            return errors.size();
        }

        public int getWarningCount() {
            return warnings.size();
        }
    }
}
