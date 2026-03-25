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
package org.openhab.binding.sonoff.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.State;

/**
 * Unit tests for {@link SonoffZigbeeContactSensorHandler}.
 *
 * Tests cover:
 * - Handler initialization with configuration
 * - Contact sensor state updates (OPEN/CLOSED)
 * - Tamper detection state updates
 * - Battery level monitoring
 * - RSSI signal strength updates
 * - Trigger time tracking
 * - Cloud connection status updates
 * - Multiple device state updates
 *
 * @author tschaban/SmartnyDom - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class SonoffZigbeeContactSensorHandlerTest {

    @Mock
    private Thing mockThing;

    @Mock
    private Bridge mockBridge;

    @Mock
    private ThingHandlerCallback mockCallback;

    @Mock
    private SonoffZigbeeBridgeHandler mockZigbeeBridge;

    @Mock
    private SonoffAccountHandler mockAccount;

    @Mock
    private SonoffDeviceState mockDeviceState;

    @Mock
    private SonoffDeviceStateParameters mockParameters;

    private ThingUID thingUID;
    private ThingUID bridgeUID;
    private Configuration configuration;
    private TestSonoffZigbeeContactSensorHandler handler;

    /**
     * Test implementation that exposes protected methods for testing
     */
    private class TestSonoffZigbeeContactSensorHandler extends SonoffZigbeeContactSensorHandler {
        public TestSonoffZigbeeContactSensorHandler(Thing thing) {
            super(thing);
        }

        @Override
        protected Bridge getBridge() {
            return mockBridge;
        }

        @Override
        protected void updateState(String channelID, State state) {
            ThingHandlerCallback callback = getCallback();
            if (callback != null) {
                callback.stateUpdated(new ChannelUID(getThing().getUID(), channelID), state);
            }
        }

        public void testUpdateDevice(SonoffDeviceState newDevice) {
            updateDevice(newDevice);
        }
    }

    @BeforeEach
    void setUp() {
        thingUID = new ThingUID("sonoff", "2026", "test-contact-sensor");
        bridgeUID = new ThingUID("sonoff", "66", "test-bridge");

        // Setup configuration
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("deviceid", "test-device-id");
        configuration = new Configuration(configMap);

        // Setup mock thing
        lenient().when(mockThing.getUID()).thenReturn(thingUID);
        lenient().when(mockThing.getConfiguration()).thenReturn(configuration);
        lenient().when(mockThing.getBridgeUID()).thenReturn(bridgeUID);
        lenient().when(mockThing.getStatus()).thenReturn(ThingStatus.ONLINE);

        // Setup mock bridge
        lenient().when(mockBridge.getUID()).thenReturn(bridgeUID);
        lenient().when(mockBridge.getHandler()).thenReturn(mockZigbeeBridge);
        lenient().when(mockBridge.getStatusInfo())
                .thenReturn(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));

        // Setup mock device state
        lenient().when(mockDeviceState.getParameters()).thenReturn(mockParameters);
        lenient().when(mockDeviceState.getCloud()).thenReturn(true);

        // Setup mock parameters - default sensor values
        lenient().when(mockParameters.getContact0()).thenReturn(OpenClosedType.CLOSED);
        lenient().when(mockParameters.getTamper()).thenReturn(OpenClosedType.CLOSED);
        lenient().when(mockParameters.getRssi()).thenReturn(new QuantityType<>(-55.0, Units.DECIBEL_MILLIWATTS));
        lenient().when(mockParameters.getBatteryLevel()).thenReturn(new QuantityType<>(90.0, Units.PERCENT));
        lenient().when(mockParameters.getTrigTime()).thenReturn(new DateTimeType("2024-01-15T10:30:00Z"));

        // Setup mock account
        lenient().when(mockAccount.getState("test-device-id")).thenReturn(mockDeviceState);
        lenient().when(mockAccount.getMode()).thenReturn("cloud");
        lenient().when(mockDeviceState.getProperties()).thenReturn(new HashMap<>());

        // Set account field on zigbee bridge mock using reflection
        try {
            Field accountField = SonoffBaseBridgeHandler.class.getDeclaredField("account");
            accountField.setAccessible(true);
            accountField.set(mockZigbeeBridge, mockAccount);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set account on zigbee bridge", e);
        }

        // Create handler
        handler = new TestSonoffZigbeeContactSensorHandler(mockThing);
        handler.setCallback(mockCallback);
    }

    @Test
    @DisplayName("Should initialize handler successfully")
    void testInitialize() {
        // Execute
        handler.initialize();

        // Verify initialization
        verify(mockAccount).addDeviceListener(eq("test-device-id"), eq(handler));
    }

    @Test
    @DisplayName("Should update contact sensor to CLOSED state")
    void testContactClosed() {
        // Setup
        when(mockParameters.getContact0()).thenReturn(OpenClosedType.CLOSED);

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify contact state updated to CLOSED
        ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(channelCaptor.capture(), stateCaptor.capture());

        // Find contact update
        List<ChannelUID> channels = channelCaptor.getAllValues();
        List<State> states = stateCaptor.getAllValues();
        boolean contactFound = false;
        for (int i = 0; i < channels.size(); i++) {
            if ("contact".equals(channels.get(i).getId())) {
                assertEquals(OpenClosedType.CLOSED, states.get(i), "Contact should be CLOSED");
                contactFound = true;
                break;
            }
        }
        assertTrue(contactFound, "Contact channel should be updated");
    }

    @Test
    @DisplayName("Should update contact sensor to OPEN state")
    void testContactOpen() {
        // Setup
        when(mockParameters.getContact0()).thenReturn(OpenClosedType.OPEN);

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify contact state updated to OPEN
        ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(channelCaptor.capture(), stateCaptor.capture());

        // Find contact update
        List<ChannelUID> channels = channelCaptor.getAllValues();
        List<State> states = stateCaptor.getAllValues();
        boolean contactFound = false;
        for (int i = 0; i < channels.size(); i++) {
            if ("contact".equals(channels.get(i).getId())) {
                assertEquals(OpenClosedType.OPEN, states.get(i), "Contact should be OPEN");
                contactFound = true;
                break;
            }
        }
        assertTrue(contactFound, "Contact channel should be updated");
    }

    @Test
    @DisplayName("Should update tamper detection to CLOSED state")
    void testTamperNotDetected() {
        // Setup
        when(mockParameters.getTamper()).thenReturn(OpenClosedType.CLOSED);

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify tamper state
        ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(channelCaptor.capture(), stateCaptor.capture());

        List<String> channelIds = channelCaptor.getAllValues().stream().map(ChannelUID::getId)
                .collect(java.util.stream.Collectors.toList());

        assertTrue(channelIds.contains("tamper"), "Tamper channel should be updated");
    }

    @Test
    @DisplayName("Should update tamper detection to OPEN state")
    void testTamperDetected() {
        // Setup
        when(mockParameters.getTamper()).thenReturn(OpenClosedType.OPEN);

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify tamper state updated to OPEN
        ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(channelCaptor.capture(), stateCaptor.capture());

        // Find tamper update
        List<ChannelUID> channels = channelCaptor.getAllValues();
        List<State> states = stateCaptor.getAllValues();
        boolean tamperFound = false;
        for (int i = 0; i < channels.size(); i++) {
            if ("tamper".equals(channels.get(i).getId())) {
                assertEquals(OpenClosedType.OPEN, states.get(i), "Tamper should be OPEN");
                tamperFound = true;
                break;
            }
        }
        assertTrue(tamperFound, "Tamper channel should be updated");
    }

    @Test
    @DisplayName("Should update battery level")
    void testBatteryLevelUpdate() {
        // Setup
        when(mockParameters.getBatteryLevel()).thenReturn(new QuantityType<>(75.0, Units.PERCENT));

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify battery level updated
        ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(channelCaptor.capture(), stateCaptor.capture());

        // Find battery update
        List<ChannelUID> channels = channelCaptor.getAllValues();
        List<State> states = stateCaptor.getAllValues();
        boolean batteryFound = false;
        for (int i = 0; i < channels.size(); i++) {
            if ("battery".equals(channels.get(i).getId())) {
                assertEquals(new QuantityType<>(75.0, Units.PERCENT), states.get(i));
                batteryFound = true;
                break;
            }
        }
        assertTrue(batteryFound, "Battery channel should be updated");
    }

    @Test
    @DisplayName("Should update RSSI signal strength")
    void testRssiUpdate() {
        // Setup
        when(mockParameters.getRssi()).thenReturn(new QuantityType<>(-70.0, Units.DECIBEL_MILLIWATTS));

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify RSSI updated
        ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(channelCaptor.capture(), stateCaptor.capture());

        List<String> channelIds = channelCaptor.getAllValues().stream().map(ChannelUID::getId)
                .collect(java.util.stream.Collectors.toList());

        assertTrue(channelIds.contains("rssi"), "RSSI channel should be updated");
    }

    @Test
    @DisplayName("Should update trigger time")
    void testTrigTimeUpdate() {
        // Setup
        DateTimeType expectedTime = new DateTimeType("2024-02-20T15:45:30Z");
        when(mockParameters.getTrigTime()).thenReturn(expectedTime);

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify trigger time updated
        ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(channelCaptor.capture(), stateCaptor.capture());

        // Find trigTime update
        List<ChannelUID> channels = channelCaptor.getAllValues();
        List<State> states = stateCaptor.getAllValues();
        boolean trigTimeFound = false;
        for (int i = 0; i < channels.size(); i++) {
            if ("trigTime".equals(channels.get(i).getId())) {
                assertEquals(expectedTime, states.get(i));
                trigTimeFound = true;
                break;
            }
        }
        assertTrue(trigTimeFound, "TrigTime channel should be updated");
    }

    @Test
    @DisplayName("Should update cloud connection status to Connected")
    void testCloudConnected() {
        // Setup
        when(mockDeviceState.getCloud()).thenReturn(true);

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify cloudOnline updated
        ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(channelCaptor.capture(), stateCaptor.capture());

        // Find cloudOnline update
        List<ChannelUID> channels = channelCaptor.getAllValues();
        List<State> states = stateCaptor.getAllValues();
        boolean cloudOnlineFound = false;
        for (int i = 0; i < channels.size(); i++) {
            if ("cloudOnline".equals(channels.get(i).getId())) {
                assertEquals(new StringType("Connected"), states.get(i));
                cloudOnlineFound = true;
                break;
            }
        }
        assertTrue(cloudOnlineFound, "CloudOnline channel should be updated");
    }

    @Test
    @DisplayName("Should update cloud connection status to Disconnected")
    void testCloudDisconnected() {
        // Setup
        when(mockDeviceState.getCloud()).thenReturn(false);

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify cloudOnline updated
        ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(channelCaptor.capture(), stateCaptor.capture());

        // Find cloudOnline update
        List<ChannelUID> channels = channelCaptor.getAllValues();
        List<State> states = stateCaptor.getAllValues();
        boolean cloudOnlineFound = false;
        for (int i = 0; i < channels.size(); i++) {
            if ("cloudOnline".equals(channels.get(i).getId())) {
                assertEquals(new StringType("Disconnected"), states.get(i));
                cloudOnlineFound = true;
                break;
            }
        }
        assertTrue(cloudOnlineFound, "CloudOnline channel should be updated");
    }

    @Test
    @DisplayName("Should update all channels simultaneously")
    void testAllChannelsUpdate() {
        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify all channels updated
        ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(channelCaptor.capture(), any(State.class));

        List<String> channelIds = channelCaptor.getAllValues().stream().map(ChannelUID::getId)
                .collect(java.util.stream.Collectors.toList());

        // Verify all expected channels are present
        assertTrue(channelIds.contains("contact"), "Contact channel should be updated");
        assertTrue(channelIds.contains("tamper"), "Tamper channel should be updated");
        assertTrue(channelIds.contains("rssi"), "RSSI channel should be updated");
        assertTrue(channelIds.contains("battery"), "Battery channel should be updated");
        assertTrue(channelIds.contains("trigTime"), "TrigTime channel should be updated");
        assertTrue(channelIds.contains("cloudOnline"), "CloudOnline channel should be updated");
    }

    @Test
    @DisplayName("Should handle multiple state updates")
    void testMultipleStateUpdates() {
        // First update - contact closed
        when(mockParameters.getContact0()).thenReturn(OpenClosedType.CLOSED);
        handler.testUpdateDevice(mockDeviceState);

        // Second update - contact open
        when(mockParameters.getContact0()).thenReturn(OpenClosedType.OPEN);
        when(mockParameters.getBatteryLevel()).thenReturn(new QuantityType<>(80.0, Units.PERCENT));
        handler.testUpdateDevice(mockDeviceState);

        // Verify multiple updates occurred
        ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(channelCaptor.capture(), any(State.class));

        List<String> channelIds = channelCaptor.getAllValues().stream().map(ChannelUID::getId)
                .collect(java.util.stream.Collectors.toList());

        // Contact should appear multiple times (at least twice)
        long contactUpdateCount = channelIds.stream().filter(id -> "contact".equals(id)).count();
        assertTrue(contactUpdateCount >= 2, "Contact should be updated at least twice");
    }

    @Test
    @DisplayName("Should update low battery condition")
    void testLowBattery() {
        // Setup
        when(mockParameters.getBatteryLevel()).thenReturn(new QuantityType<>(10.0, Units.PERCENT));

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify battery level updated
        ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(channelCaptor.capture(), stateCaptor.capture());

        // Find battery update
        List<ChannelUID> channels = channelCaptor.getAllValues();
        List<State> states = stateCaptor.getAllValues();
        boolean batteryFound = false;
        for (int i = 0; i < channels.size(); i++) {
            if ("battery".equals(channels.get(i).getId())) {
                assertEquals(new QuantityType<>(10.0, Units.PERCENT), states.get(i));
                batteryFound = true;
                break;
            }
        }
        assertTrue(batteryFound, "Battery channel should reflect low battery level");
    }

    @Test
    @DisplayName("Should handle weak RSSI signal")
    void testWeakRssi() {
        // Setup
        when(mockParameters.getRssi()).thenReturn(new QuantityType<>(-90.0, Units.DECIBEL_MILLIWATTS));

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify RSSI updated
        ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(channelCaptor.capture(), stateCaptor.capture());

        // Find RSSI update
        List<ChannelUID> channels = channelCaptor.getAllValues();
        List<State> states = stateCaptor.getAllValues();
        boolean rssiFound = false;
        for (int i = 0; i < channels.size(); i++) {
            if ("rssi".equals(channels.get(i).getId())) {
                assertEquals(new QuantityType<>(-90.0, Units.DECIBEL_MILLIWATTS), states.get(i));
                rssiFound = true;
                break;
            }
        }
        assertTrue(rssiFound, "RSSI channel should reflect weak signal");
    }

    @Test
    @DisplayName("Should handle contact open with tamper detected")
    void testContactOpenWithTamper() {
        // Setup
        when(mockParameters.getContact0()).thenReturn(OpenClosedType.OPEN);
        when(mockParameters.getTamper()).thenReturn(OpenClosedType.OPEN);

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify both contact and tamper updated
        ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(channelCaptor.capture(), stateCaptor.capture());

        List<ChannelUID> channels = channelCaptor.getAllValues();
        List<State> states = stateCaptor.getAllValues();

        boolean contactOpen = false;
        boolean tamperOpen = false;

        for (int i = 0; i < channels.size(); i++) {
            if ("contact".equals(channels.get(i).getId()) && OpenClosedType.OPEN.equals(states.get(i))) {
                contactOpen = true;
            }
            if ("tamper".equals(channels.get(i).getId()) && OpenClosedType.OPEN.equals(states.get(i))) {
                tamperOpen = true;
            }
        }

        assertTrue(contactOpen, "Contact should be OPEN");
        assertTrue(tamperOpen, "Tamper should be OPEN");
    }
}
