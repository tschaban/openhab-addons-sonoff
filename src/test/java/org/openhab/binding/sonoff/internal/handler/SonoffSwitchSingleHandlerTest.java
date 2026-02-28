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
 * Unit tests for {@link SonoffSwitchSingleHandler}.
 *
 * Tests cover:
 * - Handler initialization with cloud/mixed/local modes
 * - Local polling task scheduling for single-switch devices
 * - Task cancellation on disposal
 * - Device state updates for switch
 * - Single-switch command handling
 * - SLed command handling
 * - Connection status updates (cloud/local)
 * - Refresh command handling
 *
 * @author David Murton - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
class SonoffSwitchSingleHandlerTest {

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
    private Channel mockSwitchChannel;

    private ThingUID thingUID;
    private ThingUID bridgeUID;
    private Configuration configuration;
    private TestSonoffSwitchSingleHandler handler;

    /**
     * Test implementation that exposes protected methods for testing
     */
    private class TestSonoffSwitchSingleHandler extends SonoffSwitchSingleHandler {
        public TestSonoffSwitchSingleHandler(Thing thing) {
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

        @Override
        protected Bridge getBridge() {
            return mockBridge;
        }

        public void setTestAccount(SonoffAccountHandler account) {
            this.account = account;
        }

        public void setIsLocalIn(boolean isLocalIn) {
            this.isLocalIn = isLocalIn;
        }

        public void setIsLocalOut(boolean isLocalOut) {
            this.isLocalOut = isLocalOut;
        }

        public void setDeviceId(String deviceId) {
            this.deviceid = deviceId;
        }

        public void setCloudState(boolean cloud) {
            this.cloud = cloud;
        }

        public void setLocalState(boolean local) {
            this.local = local;
        }

        @Override
        public void handleCommand(ChannelUID channelUID, org.openhab.core.types.Command command) {
            super.handleCommand(channelUID, command);
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
        thingUID = new ThingUID("sonoff", "1", "test-single");
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

        // Setup mock switch channel
        lenient().when(mockSwitchChannel.getUID()).thenReturn(new ChannelUID(thingUID, "switch"));
        lenient().when(mockThing.getChannel("switch")).thenReturn(mockSwitchChannel);

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

        // Setup mock parameters with switch state
        lenient().when(mockParameters.getSwitch0()).thenReturn(OnOffType.ON);
        lenient().when(mockParameters.getNetworkLED()).thenReturn(OnOffType.ON);
        @SuppressWarnings("unchecked")
        QuantityType<javax.measure.quantity.Power> rssi = new QuantityType<>(Double.valueOf(-65),
                org.openhab.core.library.unit.Units.DECIBEL_MILLIWATTS);
        lenient().when(mockParameters.getRssi()).thenReturn(rssi);

        // Setup mock account
        lenient().when(mockAccount.getState("test-device-id")).thenReturn(mockDeviceState);
        lenient().when(mockAccount.getMode()).thenReturn("mixed"); // Mixed mode (cloud + local)
        lenient().when(mockDeviceState.getProperties()).thenReturn(new HashMap<>());
        lenient().when(mockDeviceState.getState()).thenReturn(mockDeviceState);
        lenient().when(mockDeviceState.getIpAddress()).thenReturn(new StringType("192.168.1.100"));

        // Create handler instance
        handler = new TestSonoffSwitchSingleHandler(mockThing);
        handler.setCallback(mockCallback);
        handler.setTestScheduler(mockScheduler);
        handler.setTestAccount(mockAccount);
        handler.setDeviceId("test-device-id");
        handler.setIsLocalIn(true);
        handler.setIsLocalOut(false);
        handler.setCloudState(true);
        handler.setLocalState(true);
    }

    @Test
    @DisplayName("Should start tasks in local mode")
    void testStartTasksInLocalMode() {
        // Setup
        when(mockAccount.getMode()).thenReturn("local");
        handler.setIsLocalIn(true);

        // Execute
        handler.testStartTasks();

        // Verify task scheduled with correct parameters
        verify(mockScheduler).scheduleWithFixedDelay(any(Runnable.class), eq(10L), eq(60L), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Should start tasks in mixed mode when cloud is unavailable")
    void testStartTasksInMixedModeWithNoCloud() {
        // Setup
        when(mockAccount.getMode()).thenReturn("mixed");
        handler.setCloudState(false); // Cloud unavailable
        handler.setIsLocalIn(true);

        // Execute
        handler.testStartTasks();

        // Verify task scheduled
        verify(mockScheduler).scheduleWithFixedDelay(any(Runnable.class), eq(10L), eq(60L), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Should not start tasks in mixed mode when cloud is available")
    void testNoTasksInMixedModeWithCloud() {
        // Setup
        when(mockAccount.getMode()).thenReturn("mixed");
        handler.setCloudState(true); // Cloud available
        handler.setIsLocalIn(true);

        // Execute
        handler.testStartTasks();

        // Verify no tasks scheduled
        verify(mockScheduler, never()).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(),
                any(TimeUnit.class));
    }

    @Test
    @DisplayName("Should not start tasks when local config is disabled")
    void testNoTasksWhenLocalDisabled() {
        // Setup
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("deviceid", "test-device-id");
        configMap.put("local", false); // Disable local polling
        configMap.put("localPoll", 60);
        configuration = new Configuration(configMap);
        when(mockThing.getConfiguration()).thenReturn(configuration);

        handler = new TestSonoffSwitchSingleHandler(mockThing);
        handler.setCallback(mockCallback);
        handler.setTestScheduler(mockScheduler);
        handler.setTestAccount(mockAccount);
        handler.setDeviceId("test-device-id");
        handler.setIsLocalIn(true);
        handler.setCloudState(false);

        when(mockAccount.getMode()).thenReturn("local");

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
    @DisplayName("Should cancel scheduled tasks")
    void testCancelTasks() {
        // Setup - start tasks first
        when(mockAccount.getMode()).thenReturn("local");
        handler.setIsLocalIn(true);
        handler.testStartTasks();

        // Execute - cancel tasks
        handler.testCancelTasks();

        // Verify task was cancelled
        verify(mockScheduledFuture).cancel(true);
    }

    @Test
    @DisplayName("Should handle switch ON command")
    void testHandleSwitchOnCommand() {
        // Setup
        ChannelUID channelUID = new ChannelUID(thingUID, "switch");
        OnOffType command = OnOffType.ON;

        // Execute
        handler.handleCommand(channelUID, command);

        // Verify command was queued
        verify(mockAccount, atLeastOnce()).queueMessage(any());
    }

    @Test
    @DisplayName("Should handle switch OFF command")
    void testHandleSwitchOffCommand() {
        // Setup
        ChannelUID channelUID = new ChannelUID(thingUID, "switch");
        OnOffType command = OnOffType.OFF;

        // Execute
        handler.handleCommand(channelUID, command);

        // Verify command was queued
        verify(mockAccount, atLeastOnce()).queueMessage(any());
    }

    @Test
    @DisplayName("Should handle switch ON command with isLocalOut enabled")
    void testHandleSwitchOnCommandWithLocalOut() {
        // Setup
        handler.setIsLocalOut(true);
        ChannelUID channelUID = new ChannelUID(thingUID, "switch");
        OnOffType command = OnOffType.ON;

        // Execute
        handler.handleCommand(channelUID, command);

        // Verify command was queued
        verify(mockAccount, atLeastOnce()).queueMessage(any());
    }

    @Test
    @DisplayName("Should handle sled command")
    void testHandleSledCommand() {
        // Setup
        ChannelUID channelUID = new ChannelUID(thingUID, "sled");
        OnOffType command = OnOffType.ON;

        // Execute
        handler.handleCommand(channelUID, command);

        // Verify command was queued
        verify(mockAccount, atLeastOnce()).queueMessage(any());
    }

    @Test
    @DisplayName("Should ignore refresh commands")
    void testHandleRefreshCommand() {
        // Setup
        ChannelUID channelUID = new ChannelUID(thingUID, "switch");

        // Execute
        handler.handleCommand(channelUID, RefreshType.REFRESH);

        // Verify no state updates occurred for refresh
        verify(mockCallback, never()).stateUpdated(any(ChannelUID.class), any(State.class));
    }

    @Test
    @DisplayName("Should update device with switch state")
    void testUpdateDeviceWithSwitch() {
        // Setup
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        ArgumentCaptor<String> channelCaptor = ArgumentCaptor.forClass(String.class);

        when(mockParameters.getSwitch0()).thenReturn(OnOffType.ON);

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify switch state updated
        verify(mockCallback, atLeastOnce()).stateUpdated(any(ChannelUID.class), stateCaptor.capture());
        assertTrue(stateCaptor.getAllValues().stream().anyMatch(state -> state.equals(OnOffType.ON)),
                "Switch state should be ON");
    }

    @Test
    @DisplayName("Should update device with rssi and sled")
    void testUpdateDeviceWithRssiAndSled() {
        // Setup
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        @SuppressWarnings("unchecked")
        QuantityType<javax.measure.quantity.Power> expectedRssi = new QuantityType<>(Double.valueOf(-65),
                org.openhab.core.library.unit.Units.DECIBEL_MILLIWATTS);
        when(mockParameters.getRssi()).thenReturn(expectedRssi);
        when(mockParameters.getNetworkLED()).thenReturn(OnOffType.ON);

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify states updated
        verify(mockCallback, atLeastOnce()).stateUpdated(any(ChannelUID.class), stateCaptor.capture());
        assertTrue(stateCaptor.getAllValues().stream().anyMatch(state -> state.equals(expectedRssi)),
                "RSSI should be updated");
        assertTrue(stateCaptor.getAllValues().stream().anyMatch(state -> state.equals(OnOffType.ON)),
                "SLED state should be ON");
    }

    @Test
    @DisplayName("Should update device with IP address")
    void testUpdateDeviceWithIpAddress() {
        // Setup
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        StringType expectedIp = new StringType("192.168.1.100");
        when(mockDeviceState.getIpAddress()).thenReturn(expectedIp);

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify IP address updated
        verify(mockCallback, atLeastOnce()).stateUpdated(any(ChannelUID.class), stateCaptor.capture());
        assertTrue(stateCaptor.getAllValues().stream().anyMatch(state -> state.equals(expectedIp)),
                "IP address should be updated");
    }

    @Test
    @DisplayName("Should update connection status")
    void testUpdateConnectionStatus() {
        // Setup
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        when(mockDeviceState.getCloud()).thenReturn(true);
        when(mockDeviceState.getLocal()).thenReturn(true);

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify connection states updated
        verify(mockCallback, atLeastOnce()).stateUpdated(any(ChannelUID.class), stateCaptor.capture());
        assertTrue(
                stateCaptor.getAllValues().stream().anyMatch(
                        state -> state.equals(new StringType("Connected")) || state.toString().equals("Connected")),
                "Connection status should be 'Connected'");
    }

    @Test
    @DisplayName("Should handle missing switch channel gracefully")
    void testHandleMissingSwitchChannel() {
        // Setup - remove switch channel
        when(mockThing.getChannel("switch")).thenReturn(null);

        // Execute - should not throw exception
        assertDoesNotThrow(() -> handler.testUpdateDevice(mockDeviceState));
    }

    @Test
    @DisplayName("Should use configurable polling interval")
    void testConfigurablePollingInterval() {
        // Setup with custom polling interval
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("deviceid", "test-device-id");
        configMap.put("local", true);
        configMap.put("localPoll", 120); // Custom 120 second interval
        configuration = new Configuration(configMap);
        when(mockThing.getConfiguration()).thenReturn(configuration);

        handler = new TestSonoffSwitchSingleHandler(mockThing);
        handler.setCallback(mockCallback);
        handler.setTestScheduler(mockScheduler);
        handler.setTestAccount(mockAccount);
        handler.setDeviceId("test-device-id");
        handler.setIsLocalIn(true);
        handler.setCloudState(false);

        when(mockAccount.getMode()).thenReturn("local");

        // Execute
        handler.testStartTasks();

        // Verify task scheduled with custom interval
        verify(mockScheduler).scheduleWithFixedDelay(any(Runnable.class), eq(10L), eq(120L), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Should handle unknown channel gracefully")
    void testHandleNullCommand() {
        // Setup
        ChannelUID channelUID = new ChannelUID(thingUID, "unknown-channel");
        OnOffType command = OnOffType.ON;

        // Execute - should not throw exception
        assertDoesNotThrow(() -> handler.handleCommand(channelUID, command));
    }

    @Test
    @DisplayName("Should update cloud and local state correctly")
    void testUpdateCloudAndLocalState() {
        // Setup - initial state both online
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        when(mockDeviceState.getCloud()).thenReturn(true);
        when(mockDeviceState.getLocal()).thenReturn(true);

        // Execute first update
        handler.testUpdateDevice(mockDeviceState);

        // Change to cloud only
        when(mockDeviceState.getCloud()).thenReturn(true);
        when(mockDeviceState.getLocal()).thenReturn(false);
        handler.testUpdateDevice(mockDeviceState);

        // Change to local only
        when(mockDeviceState.getCloud()).thenReturn(false);
        when(mockDeviceState.getLocal()).thenReturn(true);
        handler.testUpdateDevice(mockDeviceState);

        // Verify state changes were captured
        verify(mockCallback, atLeastOnce()).stateUpdated(any(ChannelUID.class), stateCaptor.capture());
        assertTrue(stateCaptor.getAllValues().stream().anyMatch(state -> state instanceof StringType),
                "Should have StringType states for connection status");
    }

    @Test
    @DisplayName("Should handle switch state OFF correctly")
    void testUpdateDeviceWithSwitchOff() {
        // Setup
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        when(mockParameters.getSwitch0()).thenReturn(OnOffType.OFF);

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify switch state updated to OFF
        verify(mockCallback, atLeastOnce()).stateUpdated(any(ChannelUID.class), stateCaptor.capture());
        assertTrue(stateCaptor.getAllValues().stream().anyMatch(state -> state.equals(OnOffType.OFF)),
                "Switch state should be OFF");
    }

    @Test
    @DisplayName("Should handle sled OFF command")
    void testHandleSledOffCommand() {
        // Setup
        ChannelUID channelUID = new ChannelUID(thingUID, "sled");
        OnOffType command = OnOffType.OFF;

        // Execute
        handler.handleCommand(channelUID, command);

        // Verify command was queued
        verify(mockAccount, atLeastOnce()).queueMessage(any());
    }

    @Test
    @DisplayName("Should update network LED state correctly")
    void testUpdateNetworkLedState() {
        // Setup
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        when(mockParameters.getNetworkLED()).thenReturn(OnOffType.OFF);

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify network LED state updated
        verify(mockCallback, atLeastOnce()).stateUpdated(any(ChannelUID.class), stateCaptor.capture());
        assertTrue(stateCaptor.getAllValues().stream().anyMatch(state -> state.equals(OnOffType.OFF)),
                "Network LED state should be OFF");
    }

    @Test
    @DisplayName("Should handle disconnected status correctly")
    void testUpdateConnectionStatusDisconnected() {
        // Setup
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        when(mockDeviceState.getCloud()).thenReturn(false);
        when(mockDeviceState.getLocal()).thenReturn(false);

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify disconnected states
        verify(mockCallback, atLeastOnce()).stateUpdated(any(ChannelUID.class), stateCaptor.capture());
        assertTrue(
                stateCaptor.getAllValues().stream()
                        .anyMatch(state -> state.equals(new StringType("Disconnected"))
                                || state.toString().equals("Disconnected")),
                "Connection status should be 'Disconnected'");
    }
}
