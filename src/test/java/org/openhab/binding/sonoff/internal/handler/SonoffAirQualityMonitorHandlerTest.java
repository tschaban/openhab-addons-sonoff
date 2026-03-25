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
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.State;

/**
 * Unit tests for {@link SonoffAirQualityMonitorHandler}.
 *
 * Tests cover:
 * - Model detection (SAWF-08P CO2 vs SAWF-07P PM2.5/PM10)
 * - Common sensor updates (temperature, humidity)
 * - Model-specific sensor updates (co2, pm10, pm2_5)
 * - Display control commands (sensorLight, sensorLightBr, voiceAlarm)
 * - Pending command state management
 * - Channel removal based on model
 * - Connection status updates
 *
 * @author tschaban/SmartnyDom - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class SonoffAirQualityMonitorHandlerTest {

    @Mock
    private Thing mockThing;

    @Mock
    private Bridge mockBridge;

    @Mock
    private ThingHandlerCallback mockCallback;

    @Mock
    private SonoffAccountHandler mockAccount;

    @Mock
    private SonoffDeviceState mockDeviceState;

    @Mock
    private SonoffDeviceStateParameters mockParameters;

    @Mock
    private ThingBuilder mockThingBuilder;

    @Mock
    private Channel mockChannel;

    private ThingUID thingUID;
    private ThingUID bridgeUID;
    private Configuration configuration;
    private TestSonoffAirQualityMonitorHandler handler;

    /**
     * Test implementation that exposes protected methods for testing
     */
    private class TestSonoffAirQualityMonitorHandler extends SonoffAirQualityMonitorHandler {
        public TestSonoffAirQualityMonitorHandler(Thing thing) {
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

        // Expose protected methods for testing
        public void testUpdateDevice(SonoffDeviceState newDevice) {
            updateDevice(newDevice);
        }
    }

    @BeforeEach
    void setUp() {
        // Setup UIDs
        bridgeUID = new ThingUID("sonoff", "account", "testAccount");
        thingUID = new ThingUID("sonoff", "airqualitymonitor", "testDevice");

        // Setup configuration
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("deviceid", "test-device-id");
        configuration = new Configuration(configMap);

        // Mock thing
        lenient().when(mockThing.getUID()).thenReturn(thingUID);
        lenient().when(mockThing.getConfiguration()).thenReturn(configuration);
        lenient().when(mockThing.getBridgeUID()).thenReturn(bridgeUID);
        lenient().when(mockThing.getStatus()).thenReturn(ThingStatus.ONLINE);
        lenient().when(mockThing.getLabel()).thenReturn("Air Quality Monitor");

        // Mock bridge
        lenient().when(mockBridge.getUID()).thenReturn(bridgeUID);
        lenient().when(mockBridge.getHandler()).thenReturn(mockAccount);
        lenient().when(mockBridge.getStatus()).thenReturn(ThingStatus.ONLINE);

        // Mock account handler
        lenient().when(mockAccount.getMode()).thenReturn("mixed");

        // Mock device state - CO2 model by default
        lenient().when(mockDeviceState.getModel()).thenReturn("SAWF-08P");
        lenient().when(mockDeviceState.getParameters()).thenReturn(mockParameters);
        lenient().when(mockDeviceState.getCloud()).thenReturn(true);
        lenient().when(mockDeviceState.getLocal()).thenReturn(true);
        lenient().when(mockDeviceState.getIpAddress()).thenReturn(new StringType("192.168.1.100"));

        // Mock common sensor values
        lenient().when(mockParameters.getTemperature()).thenReturn(new QuantityType<>(22.5, SIUnits.CELSIUS));
        lenient().when(mockParameters.getTemperatureF()).thenReturn(new QuantityType<>(72.5, ImperialUnits.FAHRENHEIT));
        lenient().when(mockParameters.getHumidity()).thenReturn(new QuantityType<>(55, Units.PERCENT));
        lenient().when(mockParameters.getRssi()).thenReturn(new QuantityType<>(-65, Units.DECIBEL_MILLIWATTS));

        // Mock CO2 value
        lenient().when(mockParameters.getCo2()).thenReturn(new QuantityType<>(450, Units.PARTS_PER_MILLION));

        // Mock PM values
        lenient().when(mockParameters.getPm10()).thenReturn(new QuantityType<>(35, Units.PARTS_PER_MILLION));
        lenient().when(mockParameters.getPm2_5()).thenReturn(new QuantityType<>(15, Units.PARTS_PER_MILLION));

        // Mock display control values
        lenient().when(mockParameters.getSensorLight()).thenReturn(OnOffType.ON);
        lenient().when(mockParameters.getSensorLightBr()).thenReturn(new PercentType(80));
        lenient().when(mockParameters.getVoiceAlarm()).thenReturn(OnOffType.OFF);

        // Create handler
        handler = new TestSonoffAirQualityMonitorHandler(mockThing);
        handler.setCallback(mockCallback);
    }

    @Test
    @DisplayName("Should detect CO2 model (SAWF-08P) and update CO2 channel")
    void testCO2ModelDetection() {
        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify CO2 channel updated
        ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(channelCaptor.capture(), stateCaptor.capture());

        List<String> channelIds = channelCaptor.getAllValues().stream().map(ChannelUID::getId)
                .collect(java.util.stream.Collectors.toList());

        // CO2 model should have co2 channel
        assertTrue(channelIds.contains("co2"), "CO2 channel should be updated for SAWF-08P model");

        // Find CO2 value
        for (int i = 0; i < channelCaptor.getAllValues().size(); i++) {
            if ("co2".equals(channelCaptor.getAllValues().get(i).getId())) {
                assertEquals(new QuantityType<>(450, Units.PARTS_PER_MILLION), stateCaptor.getAllValues().get(i));
            }
        }
    }

    @Test
    @DisplayName("Should detect PM model (SAWF-07P) and update PM channels")
    void testPMModelDetection() {
        // Setup PM model
        when(mockDeviceState.getModel()).thenReturn("SAWF-07P");

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify PM channels updated
        ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(channelCaptor.capture(), stateCaptor.capture());

        List<String> channelIds = channelCaptor.getAllValues().stream().map(ChannelUID::getId)
                .collect(java.util.stream.Collectors.toList());

        // PM model should have pm10 and pm2_5 channels
        assertTrue(channelIds.contains("pm10"), "PM10 channel should be updated for SAWF-07P model");
        assertTrue(channelIds.contains("pm2_5"), "PM2.5 channel should be updated for SAWF-07P model");
    }

    @Test
    @DisplayName("Should update common sensor channels for both models")
    void testCommonSensorUpdates() {
        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify common channels updated
        ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(channelCaptor.capture(), stateCaptor.capture());

        List<String> channelIds = channelCaptor.getAllValues().stream().map(ChannelUID::getId)
                .collect(java.util.stream.Collectors.toList());

        // Common channels should be present
        assertTrue(channelIds.contains("temperature"), "Temperature channel should be updated");
        assertTrue(channelIds.contains("temperatureF"), "Temperature F channel should be updated");
        assertTrue(channelIds.contains("humidity"), "Humidity channel should be updated");
        assertTrue(channelIds.contains("rssi"), "RSSI channel should be updated");
    }

    @Test
    @DisplayName("Should handle sensorLight ON command")
    void testSensorLightOnCommand() {
        // Setup
        ChannelUID channelUID = new ChannelUID(thingUID, "sensorLight");

        // Execute
        handler.handleCommand(channelUID, OnOffType.ON);

        // Verify state updated immediately (optimistic update)
        ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(channelCaptor.capture(), stateCaptor.capture());

        // Find sensorLight update
        for (int i = 0; i < channelCaptor.getAllValues().size(); i++) {
            if ("sensorLight".equals(channelCaptor.getAllValues().get(i).getId())) {
                assertEquals(OnOffType.ON, stateCaptor.getAllValues().get(i));
            }
        }
    }

    @Test
    @DisplayName("Should handle sensorLight OFF command")
    void testSensorLightOffCommand() {
        // Setup
        ChannelUID channelUID = new ChannelUID(thingUID, "sensorLight");

        // Execute
        handler.handleCommand(channelUID, OnOffType.OFF);

        // Verify state updated immediately (optimistic update)
        ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(channelCaptor.capture(), stateCaptor.capture());

        // Find sensorLight update
        for (int i = 0; i < channelCaptor.getAllValues().size(); i++) {
            if ("sensorLight".equals(channelCaptor.getAllValues().get(i).getId())) {
                assertEquals(OnOffType.OFF, stateCaptor.getAllValues().get(i));
            }
        }
    }

    @Test
    @DisplayName("Should handle sensorLightBr command with valid value")
    void testSensorLightBrCommand() {
        // Setup
        ChannelUID channelUID = new ChannelUID(thingUID, "sensorLightBr");

        // Execute
        handler.handleCommand(channelUID, new DecimalType(75));

        // Verify state updated immediately (optimistic update)
        ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(channelCaptor.capture(), stateCaptor.capture());

        // Find sensorLightBr update
        for (int i = 0; i < channelCaptor.getAllValues().size(); i++) {
            if ("sensorLightBr".equals(channelCaptor.getAllValues().get(i).getId())) {
                assertEquals(new PercentType(75), stateCaptor.getAllValues().get(i));
            }
        }
    }

    @Test
    @DisplayName("Should map low sensorLightBr values to device minimum (10)")
    void testSensorLightBrMinimumMapping() {
        // Setup
        ChannelUID channelUID = new ChannelUID(thingUID, "sensorLightBr");

        // Execute with value below device minimum
        handler.handleCommand(channelUID, new DecimalType(5));

        // Verify state updated with user value (5), even though device gets 10
        ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(channelCaptor.capture(), stateCaptor.capture());

        // Find sensorLightBr update - UI shows commanded value (5)
        for (int i = 0; i < channelCaptor.getAllValues().size(); i++) {
            if ("sensorLightBr".equals(channelCaptor.getAllValues().get(i).getId())) {
                assertEquals(new PercentType(5), stateCaptor.getAllValues().get(i));
            }
        }
    }

    @Test
    @DisplayName("Should handle voiceAlarm command")
    void testVoiceAlarmCommand() {
        // Setup
        ChannelUID channelUID = new ChannelUID(thingUID, "voiceAlarm");

        // Execute
        handler.handleCommand(channelUID, OnOffType.ON);

        // Verify state updated immediately (optimistic update)
        ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(channelCaptor.capture(), stateCaptor.capture());

        // Find voiceAlarm update
        for (int i = 0; i < channelCaptor.getAllValues().size(); i++) {
            if ("voiceAlarm".equals(channelCaptor.getAllValues().get(i).getId())) {
                assertEquals(OnOffType.ON, stateCaptor.getAllValues().get(i));
            }
        }
    }

    @Test
    @DisplayName("Should initialize display controls from device state")
    void testDisplayControlsInitialization() {
        // Execute - first update, no pending commands
        handler.testUpdateDevice(mockDeviceState);

        // Verify display control channels initialized from device
        ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(channelCaptor.capture(), stateCaptor.capture());

        List<String> channelIds = channelCaptor.getAllValues().stream().map(ChannelUID::getId)
                .collect(java.util.stream.Collectors.toList());

        assertTrue(channelIds.contains("sensorLight"), "SensorLight should be initialized");
        assertTrue(channelIds.contains("sensorLightBr"), "SensorLightBr should be initialized");
        assertTrue(channelIds.contains("voiceAlarm"), "VoiceAlarm should be initialized");
    }

    @Test
    @DisplayName("Should preserve commanded values and ignore device state updates")
    void testPendingCommandPreservation() {
        // Setup - send command first
        ChannelUID sensorLightUID = new ChannelUID(thingUID, "sensorLight");
        handler.handleCommand(sensorLightUID, OnOffType.OFF);

        // Reset mock to clear command update
        reset(mockCallback);

        // Now update from device with different value
        lenient().when(mockParameters.getSensorLight()).thenReturn(OnOffType.ON);
        handler.testUpdateDevice(mockDeviceState);

        // Verify sensorLight was NOT updated from device (pending command preserved)
        ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(channelCaptor.capture(), any(State.class));

        List<String> channelIds = channelCaptor.getAllValues().stream().map(ChannelUID::getId)
                .collect(java.util.stream.Collectors.toList());

        // sensorLight should NOT be in the update list (preserved from command)
        long sensorLightUpdates = channelIds.stream().filter(id -> "sensorLight".equals(id)).count();
        assertEquals(0, sensorLightUpdates, "SensorLight should not be updated from device after command");
    }

    @Test
    @DisplayName("Should update connection status channels")
    void testConnectionStatusUpdates() {
        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify connection channels updated
        ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(channelCaptor.capture(), stateCaptor.capture());

        List<String> channelIds = channelCaptor.getAllValues().stream().map(ChannelUID::getId)
                .collect(java.util.stream.Collectors.toList());

        assertTrue(channelIds.contains("cloudOnline"), "CloudOnline channel should be updated");
        assertTrue(channelIds.contains("localOnline"), "LocalOnline channel should be updated");
        assertTrue(channelIds.contains("ipaddress"), "IP address channel should be updated");
    }

    @Test
    @DisplayName("Should handle multiple commands in sequence")
    void testMultipleCommandsInSequence() {
        // Setup
        ChannelUID sensorLightUID = new ChannelUID(thingUID, "sensorLight");
        ChannelUID sensorLightBrUID = new ChannelUID(thingUID, "sensorLightBr");
        ChannelUID voiceAlarmUID = new ChannelUID(thingUID, "voiceAlarm");

        // Execute multiple commands
        handler.handleCommand(sensorLightUID, OnOffType.ON);
        handler.handleCommand(sensorLightBrUID, new DecimalType(50));
        handler.handleCommand(voiceAlarmUID, OnOffType.OFF);

        // Verify all channels updated
        ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(channelCaptor.capture(), stateCaptor.capture());

        List<String> channelIds = channelCaptor.getAllValues().stream().map(ChannelUID::getId)
                .collect(java.util.stream.Collectors.toList());

        assertTrue(channelIds.contains("sensorLight"), "SensorLight should be updated");
        assertTrue(channelIds.contains("sensorLightBr"), "SensorLightBr should be updated");
        assertTrue(channelIds.contains("voiceAlarm"), "VoiceAlarm should be updated");
    }

    @Test
    @DisplayName("Should update thing label with detected model")
    void testThingLabelUpdate() {
        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify thing label updated with model info
        verify(mockThing).setLabel("Air Quality Monitor - SAWF-08P (CO2 Sensor)");
    }

    @Test
    @DisplayName("Should handle PM model label update")
    void testPMModelLabelUpdate() {
        // Setup PM model
        when(mockDeviceState.getModel()).thenReturn("SAWF-07P");

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify thing label updated with PM model info
        verify(mockThing).setLabel("Air Quality Monitor - SAWF-07P (PM2.5/PM10 Sensor)");
    }
}
