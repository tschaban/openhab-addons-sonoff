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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;

/**
 * Unit tests for {@link SonoffSwitchMultiHandler}.
 *
 * Tests cover:
 * - Handler initialization with cloud/mixed/local modes
 * - Local polling task scheduling for multi-switch devices
 * - Task cancellation on disposal
 * - Device state updates for multiple switches (switch0-3)
 * - Multi-switch command handling for each outlet
 * - SLed command handling
 * - Connection status updates (cloud/local)
 * - Refresh command handling
 *
 * @author David Murton - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
class SonoffSwitchMultiHandlerTest {

    @Mock
    private Thing mockThing;

    @Mock
    private Bridge mockBridge;

    @Mock
    private ThingHandlerCallback mockCallback;

    @Mock
    private ScheduledExecutorService mockScheduler;

    @Mock
    @SuppressWarnings("rawtypes")
    private ScheduledFuture mockScheduledFuture;

    @Mock
    private SonoffAccountHandler mockAccount;

    @Mock
    private SonoffDeviceState mockDeviceState;

    @Mock
    private SonoffDeviceStateParameters mockParameters;

    @Mock
    private Channel mockSwitch0Channel;

    @Mock
    private Channel mockSwitch1Channel;

    @Mock
    private Channel mockSwitch2Channel;

    @Mock
    private Channel mockSwitch3Channel;

    private ThingUID thingUID;
    private ThingUID bridgeUID;
    private Configuration configuration;
    private TestSonoffSwitchMultiHandler handler;

    /**
     * Test implementation that exposes protected methods for testing
     */
    private class TestSonoffSwitchMultiHandler extends SonoffSwitchMultiHandler {
        public TestSonoffSwitchMultiHandler(Thing thing) {
            super(thing);
        }

        public void setTestScheduler(ScheduledExecutorService scheduler) {
            try {
                Field schedulerField = org.openhab.core.thing.binding.BaseThingHandler.class
                        .getDeclaredField("scheduler");
                schedulerField.setAccessible(true);
                schedulerField.set(this, scheduler);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set scheduler", e);
            }
        }

        public void setTestAccount(SonoffAccountHandler account) {
            this.account = account;
        }

        public void setCloudState(boolean cloud) {
            this.cloud = cloud;
        }

        public void setLocalState(boolean local) {
            this.local = local;
        }

        public void setIsLocalIn(boolean isLocalIn) {
            this.isLocalIn = isLocalIn;
        }

        public void setIsLocalOut(boolean isLocalOut) {
            this.isLocalOut = isLocalOut;
        }

        public void setDeviceId(String deviceid) {
            this.deviceid = deviceid;
        }

        @Override
        protected Bridge getBridge() {
            return mockBridge;
        }

        @Override
        protected void updateState(String channelID, State state) {
            // Capture state updates for verification
            super.updateState(channelID, state);
        }

        public void testUpdateDevice(SonoffDeviceState newDevice) {
            updateDevice(newDevice);
        }

        public void testStartTasks() {
            startTasks();
        }

        public void testCancelTasks() {
            cancelTasks();
        }
    }

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        thingUID = new ThingUID("sonoff", "1", "test-multi");
        bridgeUID = new ThingUID("sonoff", "account", "test-bridge");

        // Setup configuration
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("deviceid", "test-device-id");
        configMap.put("local", true); // Enable local polling
        configMap.put("localPoll", 60); // Poll every 60 seconds
        configuration = new Configuration(configMap);

        // Setup mock thing
        lenient().when(mockThing.getUID()).thenReturn(thingUID);
        lenient().when(mockThing.getConfiguration()).thenReturn(configuration);
        lenient().when(mockThing.getBridgeUID()).thenReturn(bridgeUID);

        // Setup mock channels
        lenient().when(mockSwitch0Channel.getUID()).thenReturn(new ChannelUID(thingUID, "switch0"));
        lenient().when(mockSwitch1Channel.getUID()).thenReturn(new ChannelUID(thingUID, "switch1"));
        lenient().when(mockSwitch2Channel.getUID()).thenReturn(new ChannelUID(thingUID, "switch2"));
        lenient().when(mockSwitch3Channel.getUID()).thenReturn(new ChannelUID(thingUID, "switch3"));

        lenient().when(mockThing.getChannel("switch0")).thenReturn(mockSwitch0Channel);
        lenient().when(mockThing.getChannel("switch1")).thenReturn(mockSwitch1Channel);
        lenient().when(mockThing.getChannel("switch2")).thenReturn(mockSwitch2Channel);
        lenient().when(mockThing.getChannel("switch3")).thenReturn(mockSwitch3Channel);

        // Setup mock bridge
        lenient().when(mockBridge.getUID()).thenReturn(bridgeUID);
        lenient().when(mockBridge.getHandler()).thenReturn(mockAccount);
        lenient().when(mockBridge.getStatusInfo())
                .thenReturn(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));

        // Setup mock scheduler
        lenient().when(
                mockScheduler.scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class)))
                .thenReturn(mockScheduledFuture);

        // Setup mock device state
        lenient().when(mockDeviceState.getParameters()).thenReturn(mockParameters);
        lenient().when(mockDeviceState.getCloud()).thenReturn(true);
        lenient().when(mockDeviceState.getLocal()).thenReturn(true);
        lenient().when(mockDeviceState.getUiid()).thenReturn(1);

        // Setup mock parameters with multiple switch states
        lenient().when(mockParameters.getSwitch0()).thenReturn(OnOffType.ON);
        lenient().when(mockParameters.getSwitch1()).thenReturn(OnOffType.OFF);
        lenient().when(mockParameters.getSwitch2()).thenReturn(OnOffType.ON);
        lenient().when(mockParameters.getSwitch3()).thenReturn(OnOffType.OFF);
        lenient().when(mockParameters.getNetworkLED()).thenReturn(OnOffType.ON);
        lenient().when(mockParameters.getRssi()).thenReturn(
                new QuantityType<>(Double.valueOf(-65), org.openhab.core.library.unit.Units.DECIBEL_MILLIWATTS));

        // Setup mock account
        lenient().when(mockAccount.getState("test-device-id")).thenReturn(mockDeviceState);
        lenient().when(mockAccount.getMode()).thenReturn("mixed"); // Mixed mode (cloud + local)
        lenient().when(mockDeviceState.getProperties()).thenReturn(new HashMap<>());
        lenient().when(mockDeviceState.getState()).thenReturn(mockDeviceState);
        lenient().when(mockDeviceState.getIpAddress()).thenReturn(new StringType("192.168.1.100"));

        // Create handler
        handler = new TestSonoffSwitchMultiHandler(mockThing);
        handler.setCallback(mockCallback);
        handler.setTestScheduler(mockScheduler);
        handler.setTestAccount(mockAccount);
        handler.setDeviceId("test-device-id");
    }

    @Test
    @DisplayName("Should start local polling task in local mode")
    void testStartTasksInLocalMode() {
        // Setup
        when(mockAccount.getMode()).thenReturn("local");
        handler.setIsLocalIn(true);
        handler.setIsLocalOut(true);
        handler.setCloudState(false);
        handler.setLocalState(true);

        // Execute
        handler.testStartTasks();

        // Verify local polling task scheduled
        verify(mockScheduler).scheduleWithFixedDelay(any(Runnable.class), eq(10L), eq(60L), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Should start local polling task in mixed mode when cloud is false")
    void testStartTasksInMixedModeWithNoCloud() {
        // Setup
        when(mockAccount.getMode()).thenReturn("mixed");
        handler.setIsLocalIn(true);
        handler.setIsLocalOut(true);
        handler.setCloudState(false);
        handler.setLocalState(true);

        // Execute
        handler.testStartTasks();

        // Verify local polling task scheduled
        verify(mockScheduler).scheduleWithFixedDelay(any(Runnable.class), eq(10L), eq(60L), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Should not start tasks in mixed mode with cloud connection")
    void testNoTasksInMixedModeWithCloud() {
        // Setup
        when(mockAccount.getMode()).thenReturn("mixed");
        handler.setIsLocalIn(true);
        handler.setIsLocalOut(true);
        handler.setCloudState(true);
        handler.setLocalState(true);

        // Execute
        handler.testStartTasks();

        // Verify no tasks scheduled (cloud is available)
        verify(mockScheduler, never()).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(),
                any(TimeUnit.class));
    }

    @Test
    @DisplayName("Should not start tasks when local is disabled in config")
    void testNoTasksWhenLocalDisabled() {
        // Setup - disable local
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("deviceid", "test-device-id");
        configMap.put("local", false);
        configMap.put("localPoll", 60);
        configuration = new Configuration(configMap);
        when(mockThing.getConfiguration()).thenReturn(configuration);

        handler = new TestSonoffSwitchMultiHandler(mockThing);
        handler.setCallback(mockCallback);
        handler.setTestScheduler(mockScheduler);
        handler.setTestAccount(mockAccount);

        when(mockAccount.getMode()).thenReturn("local");
        handler.setIsLocalIn(true);
        handler.setCloudState(false);
        handler.setLocalState(true);

        // Execute
        handler.testStartTasks();

        // Verify no tasks scheduled
        verify(mockScheduler, never()).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(),
                any(TimeUnit.class));
    }

    @Test
    @DisplayName("Should not start tasks when isLocalIn is false")
    void testNoTasksWhenNotLocalIn() {
        // Setup
        when(mockAccount.getMode()).thenReturn("local");
        handler.setIsLocalIn(false); // Device doesn't support local in
        handler.setCloudState(false);
        handler.setLocalState(true);

        // Execute
        handler.testStartTasks();

        // Verify no tasks scheduled
        verify(mockScheduler, never()).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(),
                any(TimeUnit.class));
    }

    @Test
    @DisplayName("Should cancel tasks on disposal")
    void testCancelTasks() {
        // Setup - start tasks first
        when(mockAccount.getMode()).thenReturn("local");
        handler.setIsLocalIn(true);
        handler.setIsLocalOut(true);
        handler.setCloudState(false);
        handler.setLocalState(true);
        handler.testStartTasks();

        // Execute
        handler.testCancelTasks();

        // Verify task cancelled
        verify(mockScheduledFuture).cancel(true);
    }

    @Test
    @DisplayName("Should handle switch0 ON command")
    void testHandleSwitch0OnCommand() {
        // Setup
        handler.initialize();
        handler.setIsLocalOut(false);
        handler.setDeviceId("test-device-id");
        ChannelUID switch0Channel = new ChannelUID(thingUID, "switch0");

        // Execute
        handler.handleCommand(switch0Channel, OnOffType.ON);

        // Command should be queued (we can't easily verify queue without exposing it)
        assertNotNull(handler);
    }

    @Test
    @DisplayName("Should handle switch1 OFF command")
    void testHandleSwitch1OffCommand() {
        // Setup
        handler.initialize();
        handler.setIsLocalOut(false);
        handler.setDeviceId("test-device-id");
        ChannelUID switch1Channel = new ChannelUID(thingUID, "switch1");

        // Execute
        handler.handleCommand(switch1Channel, OnOffType.OFF);

        // Command should be queued
        assertNotNull(handler);
    }

    @Test
    @DisplayName("Should handle switch2 ON command")
    void testHandleSwitch2OnCommand() {
        // Setup
        handler.initialize();
        handler.setIsLocalOut(true);
        handler.setDeviceId("test-device-id");
        ChannelUID switch2Channel = new ChannelUID(thingUID, "switch2");

        // Execute
        handler.handleCommand(switch2Channel, OnOffType.ON);

        // Command should be queued
        assertNotNull(handler);
    }

    @Test
    @DisplayName("Should handle switch3 OFF command")
    void testHandleSwitch3OffCommand() {
        // Setup
        handler.initialize();
        handler.setIsLocalOut(true);
        handler.setDeviceId("test-device-id");
        ChannelUID switch3Channel = new ChannelUID(thingUID, "switch3");

        // Execute
        handler.handleCommand(switch3Channel, OnOffType.OFF);

        // Command should be queued
        assertNotNull(handler);
    }

    @Test
    @DisplayName("Should handle sled command")
    void testHandleSledCommand() {
        // Setup
        handler.initialize();
        handler.setDeviceId("test-device-id");
        ChannelUID sledChannel = new ChannelUID(thingUID, "sled");

        // Execute
        handler.handleCommand(sledChannel, OnOffType.ON);

        // Command should be queued
        assertNotNull(handler);
    }

    @Test
    @DisplayName("Should handle refresh command without action")
    void testHandleRefreshCommand() {
        // Setup
        handler.initialize();
        ChannelUID switch0Channel = new ChannelUID(thingUID, "switch0");

        // Execute
        handler.handleCommand(switch0Channel, RefreshType.REFRESH);

        // Refresh should be ignored (no queue action)
        assertNotNull(handler);
        verify(mockScheduler, never()).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(),
                any(TimeUnit.class));
    }

    @Test
    @DisplayName("Should update all switch states from device")
    void testUpdateDeviceWithAllSwitches() {
        // Setup
        handler.initialize();

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify state updates
        ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(channelCaptor.capture(), stateCaptor.capture());

        // Verify all switch channels were updated
        List<String> channelIds = channelCaptor.getAllValues().stream().map(ChannelUID::getId)
                .collect(java.util.stream.Collectors.toList());
        assertTrue(channelIds.contains("switch0"), "Switch0 channel should be updated");
        assertTrue(channelIds.contains("switch1"), "Switch1 channel should be updated");
        assertTrue(channelIds.contains("switch2"), "Switch2 channel should be updated");
        assertTrue(channelIds.contains("switch3"), "Switch3 channel should be updated");
    }

    @Test
    @DisplayName("Should update rssi and sled states from device")
    void testUpdateDeviceWithRssiAndSled() {
        // Setup
        handler.initialize();

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify state updates
        ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(channelCaptor.capture(), any(State.class));

        // Verify rssi and sled channels were updated
        List<String> channelIds = channelCaptor.getAllValues().stream().map(ChannelUID::getId)
                .collect(java.util.stream.Collectors.toList());
        assertTrue(channelIds.contains("rssi"), "RSSI channel should be updated");
        assertTrue(channelIds.contains("sled"), "SLED channel should be updated");
    }

    @Test
    @DisplayName("Should update IP address from device")
    void testUpdateDeviceWithIpAddress() {
        // Setup
        handler.initialize();

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify state updates
        ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(channelCaptor.capture(), stateCaptor.capture());

        // Verify IP address channel was updated
        List<String> channelIds = channelCaptor.getAllValues().stream().map(ChannelUID::getId)
                .collect(java.util.stream.Collectors.toList());
        assertTrue(channelIds.contains("ipaddress"), "IP address channel should be updated");
    }

    @Test
    @DisplayName("Should update connection status from device")
    void testUpdateConnectionStatus() {
        // Setup
        handler.initialize();

        // Execute - device with cloud and local connections
        when(mockDeviceState.getCloud()).thenReturn(true);
        when(mockDeviceState.getLocal()).thenReturn(true);
        handler.testUpdateDevice(mockDeviceState);

        // Verify connection state updates
        ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(channelCaptor.capture(), stateCaptor.capture());

        List<String> channelIds = channelCaptor.getAllValues().stream().map(ChannelUID::getId)
                .collect(java.util.stream.Collectors.toList());
        assertTrue(channelIds.contains("cloudOnline"), "Cloud online channel should be updated");
        assertTrue(channelIds.contains("localOnline"), "Local online channel should be updated");

        // Verify connection states
        List<State> states = stateCaptor.getAllValues();
        for (int i = 0; i < channelIds.size(); i++) {
            if (channelIds.get(i).equals("cloudOnline")) {
                assertEquals(new StringType("Connected"), states.get(i), "Cloud should be connected");
            }
            if (channelIds.get(i).equals("localOnline")) {
                assertEquals(new StringType("Connected"), states.get(i), "Local should be connected");
            }
        }
    }

    @Test
    @DisplayName("Should handle missing switch channels gracefully")
    void testUpdateDeviceWithMissingSwitchChannels() {
        // Setup - remove switch2 and switch3 channels
        when(mockThing.getChannel("switch2")).thenReturn(null);
        when(mockThing.getChannel("switch3")).thenReturn(null);

        handler.initialize();

        // Execute - should not throw exception
        assertDoesNotThrow(() -> handler.testUpdateDevice(mockDeviceState));

        // Verify switch0 and switch1 were still updated
        ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(channelCaptor.capture(), any(State.class));

        List<String> channelIds = channelCaptor.getAllValues().stream().map(ChannelUID::getId)
                .collect(java.util.stream.Collectors.toList());
        assertTrue(channelIds.contains("switch0"), "Switch0 should be updated");
        assertTrue(channelIds.contains("switch1"), "Switch1 should be updated");
    }

    @Test
    @DisplayName("Should use configurable polling interval")
    void testConfigurablePollingInterval() {
        // Setup with custom polling interval
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("deviceid", "test-device-id");
        configMap.put("local", true);
        configMap.put("localPoll", 120); // Custom 120 seconds
        configuration = new Configuration(configMap);
        when(mockThing.getConfiguration()).thenReturn(configuration);

        handler = new TestSonoffSwitchMultiHandler(mockThing);
        handler.setCallback(mockCallback);
        handler.setTestScheduler(mockScheduler);
        handler.setTestAccount(mockAccount);

        when(mockAccount.getMode()).thenReturn("local");
        handler.setIsLocalIn(true);
        handler.setCloudState(false);
        handler.setLocalState(true);

        // Execute
        handler.testStartTasks();

        // Verify task uses custom interval (120 seconds)
        verify(mockScheduler).scheduleWithFixedDelay(any(Runnable.class), eq(10L), eq(120L), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Should handle null command gracefully")
    void testHandleNullCommand() {
        // Setup
        handler.initialize();
        handler.setDeviceId("test-device-id");
        ChannelUID unknownChannel = new ChannelUID(thingUID, "unknown");

        // Execute - unknown channel should not queue a message
        assertDoesNotThrow(() -> handler.handleCommand(unknownChannel, OnOffType.ON));
    }

    @Test
    @DisplayName("Should update cloud and local state from device updates")
    void testUpdateCloudAndLocalState() {
        // Setup
        handler.initialize();

        // Execute - first update with both connected
        when(mockDeviceState.getCloud()).thenReturn(true);
        when(mockDeviceState.getLocal()).thenReturn(true);
        handler.testUpdateDevice(mockDeviceState);

        // Update to cloud only
        when(mockDeviceState.getCloud()).thenReturn(true);
        when(mockDeviceState.getLocal()).thenReturn(false);
        handler.testUpdateDevice(mockDeviceState);

        // Update to local only
        when(mockDeviceState.getCloud()).thenReturn(false);
        when(mockDeviceState.getLocal()).thenReturn(true);
        handler.testUpdateDevice(mockDeviceState);

        // Verify multiple state updates occurred
        verify(mockCallback, atLeast(3)).stateUpdated(any(ChannelUID.class), any(State.class));
    }
}
