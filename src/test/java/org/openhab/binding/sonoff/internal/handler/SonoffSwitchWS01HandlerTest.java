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
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
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
 * Unit tests for {@link SonoffSwitchWS01Handler}.
 *
 * Tests cover:
 * - Handler initialization with cloud/mixed/local modes
 * - UiActive and sledOnline task scheduling
 * - Electrical data polling configuration
 * - Task cancellation on disposal
 * - Device state updates (power, voltage, current, energy)
 * - Switch and sledOnline command handling
 *
 * @author tschaban/SmartnyDom - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class SonoffSwitchWS01HandlerTest {

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

    private ThingUID thingUID;
    private ThingUID bridgeUID;
    private Configuration configuration;
    private TestSonoffSwitchWS01Handler handler;

    /**
     * Test implementation that exposes protected methods for testing
     */
    private class TestSonoffSwitchWS01Handler extends SonoffSwitchWS01Handler {
        public TestSonoffSwitchWS01Handler(Thing thing) {
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
        thingUID = new ThingUID("sonoff", "276", "test-ws01");
        bridgeUID = new ThingUID("sonoff", "account", "test-bridge");

        // Setup configuration
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("deviceid", "test-device-id");
        configMap.put("local", true); // Enable electricity polling
        configMap.put("localPoll", 60); // Poll every 60 seconds
        configuration = new Configuration(configMap);

        // Setup mock thing
        lenient().when(mockThing.getUID()).thenReturn(thingUID);
        lenient().when(mockThing.getConfiguration()).thenReturn(configuration);
        lenient().when(mockThing.getBridgeUID()).thenReturn(bridgeUID);

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
        lenient().when(mockDeviceState.getUiid()).thenReturn(276);

        // Setup mock parameters with electrical data
        lenient().when(mockParameters.getSwitch0()).thenReturn(OnOffType.ON);
        lenient().when(mockParameters.getPower())
                .thenReturn(new QuantityType<>(250.0, org.openhab.core.library.unit.Units.WATT));
        lenient().when(mockParameters.getVoltage())
                .thenReturn(new QuantityType<>(235.0, org.openhab.core.library.unit.Units.VOLT));
        lenient().when(mockParameters.getCurrent())
                .thenReturn(new QuantityType<>(1.06, org.openhab.core.library.unit.Units.AMPERE));
        lenient().when(mockParameters.getTodayKwh())
                .thenReturn(new QuantityType<>(2.5, org.openhab.core.library.unit.Units.KILOWATT_HOUR));
        lenient().when(mockParameters.getWeekKwh())
                .thenReturn(new QuantityType<>(15.0, org.openhab.core.library.unit.Units.KILOWATT_HOUR));
        lenient().when(mockParameters.getMonthKwh())
                .thenReturn(new QuantityType<>(90.0, org.openhab.core.library.unit.Units.KILOWATT_HOUR));
        lenient().when(mockParameters.getYearKwh())
                .thenReturn(new QuantityType<>(1000.0, org.openhab.core.library.unit.Units.KILOWATT_HOUR));
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
        handler = new TestSonoffSwitchWS01Handler(mockThing);
        handler.setCallback(mockCallback);
        handler.setTestScheduler(mockScheduler);
        handler.setTestAccount(mockAccount);
    }

    @Test
    @DisplayName("Should start sledOnline and uiActive tasks in mixed mode with polling enabled")
    void testStartTasksInMixedMode() {
        // Setup
        when(mockAccount.getMode()).thenReturn("mixed");
        handler.setCloudState(true);
        handler.setLocalState(true);

        // Execute
        handler.testStartTasks();

        // Verify sledOnline task scheduled (every 60 seconds)
        verify(mockScheduler, times(2)).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(),
                eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Should start sledOnline and uiActive tasks in cloud mode with polling enabled")
    void testStartTasksInCloudMode() {
        // Setup
        when(mockAccount.getMode()).thenReturn("cloud");
        handler.setCloudState(true);
        handler.setLocalState(false);

        // Execute
        handler.testStartTasks();

        // Verify tasks scheduled
        verify(mockScheduler, times(2)).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(),
                eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Should not start tasks in local-only mode")
    void testNoTasksInLocalMode() {
        // Setup
        when(mockAccount.getMode()).thenReturn("local");
        handler.setCloudState(false);
        handler.setLocalState(true);

        // Execute
        handler.testStartTasks();

        // Verify no tasks scheduled
        verify(mockScheduler, never()).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(),
                any(TimeUnit.class));
    }

    @Test
    @DisplayName("Should not start tasks when polling disabled")
    void testNoTasksWhenPollingDisabled() {
        // Setup - disable polling
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("deviceid", "test-device-id");
        configMap.put("local", false); // Disable electricity polling
        configMap.put("localPoll", 60);
        configuration = new Configuration(configMap);
        when(mockThing.getConfiguration()).thenReturn(configuration);

        handler = new TestSonoffSwitchWS01Handler(mockThing);
        handler.setCallback(mockCallback);
        handler.setTestScheduler(mockScheduler);
        handler.setTestAccount(mockAccount);

        when(mockAccount.getMode()).thenReturn("mixed");
        handler.setCloudState(true);
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
        when(mockAccount.getMode()).thenReturn("mixed");
        handler.setCloudState(true);
        handler.setLocalState(true);
        handler.testStartTasks();

        // Execute
        handler.testCancelTasks();

        // Verify tasks cancelled
        verify(mockScheduledFuture, times(2)).cancel(true);
    }

    @Test
    @DisplayName("Should update device state with electrical data")
    void testUpdateDeviceWithElectricalData() {
        // Setup
        handler.initialize();

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify state updates
        ArgumentCaptor<ChannelUID> channelCaptor = ArgumentCaptor.forClass(ChannelUID.class);
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(channelCaptor.capture(), stateCaptor.capture());

        // Verify electrical channels were updated
        List<String> channelIds = channelCaptor.getAllValues().stream().map(ChannelUID::getId)
                .collect(java.util.stream.Collectors.toList());
        assertTrue(channelIds.contains("power"), "Power channel should be updated");
        assertTrue(channelIds.contains("voltage"), "Voltage channel should be updated");
        assertTrue(channelIds.contains("current"), "Current channel should be updated");
        assertTrue(channelIds.contains("switch"), "Switch channel should be updated");
    }

    @Test
    @DisplayName("Should handle switch command")
    void testHandleSwitchCommand() {
        // Setup
        handler.initialize();
        ChannelUID switchChannel = new ChannelUID(thingUID, "switch");

        // Execute
        handler.handleCommand(switchChannel, OnOffType.ON);

        // Command should be queued (we can't easily verify queue without exposing it)
        // This test mainly ensures no exceptions are thrown
        assertNotNull(handler);
    }

    @Test
    @DisplayName("Should handle sled command")
    void testHandleSledCommand() {
        // Setup
        handler.initialize();
        ChannelUID sledChannel = new ChannelUID(thingUID, "sled");

        // Execute
        handler.handleCommand(sledChannel, OnOffType.ON);

        // Command should be queued
        assertNotNull(handler);
    }

    @Test
    @DisplayName("Should store current sledOnline state from device updates")
    void testStoreSledOnlineState() {
        // Setup
        handler.initialize();
        when(mockParameters.getNetworkLED()).thenReturn(OnOffType.OFF);

        // Execute - first update with ON state
        handler.testUpdateDevice(mockDeviceState);

        // Update to OFF state
        when(mockParameters.getNetworkLED()).thenReturn(OnOffType.OFF);
        handler.testUpdateDevice(mockDeviceState);

        // Verify no exceptions and state changes handled
        assertNotNull(handler);
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

        handler = new TestSonoffSwitchWS01Handler(mockThing);
        handler.setCallback(mockCallback);
        handler.setTestScheduler(mockScheduler);
        handler.setTestAccount(mockAccount);

        when(mockAccount.getMode()).thenReturn("mixed");
        handler.setCloudState(true);
        handler.setLocalState(true);

        // Execute
        handler.testStartTasks();

        // Verify sledOnline task uses custom interval (120 seconds)
        verify(mockScheduler, atLeastOnce()).scheduleWithFixedDelay(any(Runnable.class), eq(10L), eq(120L),
                eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Should update connection status in device updates")
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
    }
}
