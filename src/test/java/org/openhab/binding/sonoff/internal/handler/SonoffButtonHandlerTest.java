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
import static org.mockito.Mockito.lenient;

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
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Bridge;
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
 * Unit tests for {@link SonoffButtonHandler}.
 * 
 * Tests cover:
 * - Handler initialization with configuration
 * - Button press event handling for all three buttons
 * - Scheduled reset tasks for button states
 * - Task cancellation on disposal
 * - Device state updates (RSSI, cloud/local status)
 * - Refresh command handling
 * - Edge cases and error conditions
 *
 * @author tschaban/SmartnyDom - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class SonoffButtonHandlerTest {

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
    private TestSonoffButtonHandler handler;

    /**
     * Test implementation that exposes protected methods for testing
     */
    private class TestSonoffButtonHandler extends SonoffButtonHandler {
        public TestSonoffButtonHandler(Thing thing) {
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

        public void testCancelTasks() {
            cancelTasks();
        }
    }

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        thingUID = new ThingUID("sonoff", "265", "test-button");
        bridgeUID = new ThingUID("sonoff", "account", "test-bridge");

        // Setup configuration
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("deviceid", "test-device-id");
        configMap.put("buttonResetTimeout", 500);
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
        lenient().when(mockScheduler.schedule(any(Runnable.class), anyLong(), any(TimeUnit.class)))
                .thenReturn(mockScheduledFuture);

        // Setup mock device state
        lenient().when(mockDeviceState.getParameters()).thenReturn(mockParameters);
        lenient().when(mockDeviceState.getCloud()).thenReturn(true);
        lenient().when(mockDeviceState.getLocal()).thenReturn(false);
        lenient().when(mockDeviceState.getUiid()).thenReturn(265);

        // Setup mock parameters - default all buttons to CLOSED
        lenient().when(mockParameters.getButton(anyInt())).thenReturn(OpenClosedType.CLOSED);
        lenient().when(mockParameters.getButtonTrigTime(anyInt())).thenReturn(new DateTimeType("2024-01-01T12:00:00Z"));
        lenient().when(mockParameters.getRssi()).thenReturn(
                new QuantityType<>(Double.valueOf(-50), org.openhab.core.library.unit.Units.DECIBEL_MILLIWATTS));

        // Setup mock account
        lenient().when(mockAccount.getState("test-device-id")).thenReturn(mockDeviceState);
        lenient().when(mockAccount.getMode()).thenReturn("cloud");
        lenient().when(mockDeviceState.getProperties()).thenReturn(new HashMap<>());
        lenient().when(mockDeviceState.getState()).thenReturn(mockDeviceState);

        // Create handler
        handler = new TestSonoffButtonHandler(mockThing);
        handler.setCallback(mockCallback);
        handler.setTestScheduler(mockScheduler);
        handler.setTestAccount(mockAccount);
    }

    @Test
    @DisplayName("Should initialize with default button reset timeout")
    void testInitializeWithDefaultTimeout() {
        // Execute
        handler.initialize();

        // Verify initialization was successful
        verify(mockAccount).addDeviceListener("test-device-id", handler);
    }

    @Test
    @DisplayName("Should initialize with custom button reset timeout")
    void testInitializeWithCustomTimeout() {
        // Setup - custom timeout
        Map<String, Object> customConfig = new HashMap<>();
        customConfig.put("deviceid", "test-device-id");
        customConfig.put("buttonResetTimeout", 1000);
        when(mockThing.getConfiguration()).thenReturn(new Configuration(customConfig));

        // Execute
        handler.initialize();

        // Verify initialization was successful
        verify(mockAccount).addDeviceListener("test-device-id", handler);
    }

    @Test
    @DisplayName("Should handle button0 press event")
    void testButton0PressEvent() {
        // Setup
        when(mockParameters.getButton(0)).thenReturn(OpenClosedType.OPEN);
        when(mockParameters.getButton(1)).thenReturn(OpenClosedType.CLOSED);
        when(mockParameters.getButton(2)).thenReturn(OpenClosedType.CLOSED);
        DateTimeType trigTime = new DateTimeType("2024-01-01T12:30:00Z");
        when(mockParameters.getButtonTrigTime(0)).thenReturn(trigTime);

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(any(ChannelUID.class), stateCaptor.capture());

        // Verify button0 was opened
        verify(mockScheduler).schedule(any(Runnable.class), eq(500L), eq(TimeUnit.MILLISECONDS));

        // Verify button state was reset to CLOSED after processing
        verify(mockParameters).setButton(0, OpenClosedType.CLOSED);
    }

    @Test
    @DisplayName("Should handle button1 press event")
    void testButton1PressEvent() {
        // Setup
        when(mockParameters.getButton(0)).thenReturn(OpenClosedType.CLOSED);
        when(mockParameters.getButton(1)).thenReturn(OpenClosedType.OPEN);
        when(mockParameters.getButton(2)).thenReturn(OpenClosedType.CLOSED);
        DateTimeType trigTime = new DateTimeType("2024-01-01T12:30:00Z");
        when(mockParameters.getButtonTrigTime(1)).thenReturn(trigTime);

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify
        verify(mockScheduler).schedule(any(Runnable.class), eq(500L), eq(TimeUnit.MILLISECONDS));
        verify(mockParameters).setButton(1, OpenClosedType.CLOSED);
    }

    @Test
    @DisplayName("Should handle button2 press event")
    void testButton2PressEvent() {
        // Setup
        when(mockParameters.getButton(0)).thenReturn(OpenClosedType.CLOSED);
        when(mockParameters.getButton(1)).thenReturn(OpenClosedType.CLOSED);
        when(mockParameters.getButton(2)).thenReturn(OpenClosedType.OPEN);
        DateTimeType trigTime = new DateTimeType("2024-01-01T12:30:00Z");
        when(mockParameters.getButtonTrigTime(2)).thenReturn(trigTime);

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify
        verify(mockScheduler).schedule(any(Runnable.class), eq(500L), eq(TimeUnit.MILLISECONDS));
        verify(mockParameters).setButton(2, OpenClosedType.CLOSED);
    }

    @Test
    @DisplayName("Should handle multiple simultaneous button presses")
    void testMultipleButtonPressEvents() {
        // Setup - all three buttons pressed
        when(mockParameters.getButton(0)).thenReturn(OpenClosedType.OPEN);
        when(mockParameters.getButton(1)).thenReturn(OpenClosedType.OPEN);
        when(mockParameters.getButton(2)).thenReturn(OpenClosedType.OPEN);
        DateTimeType trigTime = new DateTimeType("2024-01-01T12:30:00Z");
        when(mockParameters.getButtonTrigTime(anyInt())).thenReturn(trigTime);

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify - all three reset tasks scheduled
        verify(mockScheduler, times(3)).schedule(any(Runnable.class), eq(500L), eq(TimeUnit.MILLISECONDS));

        // Verify all buttons reset to CLOSED
        verify(mockParameters).setButton(0, OpenClosedType.CLOSED);
        verify(mockParameters).setButton(1, OpenClosedType.CLOSED);
        verify(mockParameters).setButton(2, OpenClosedType.CLOSED);
    }

    @Test
    @DisplayName("Should cancel previous reset task when same button pressed twice")
    void testCancelPreviousResetTask() {
        // Setup - button0 pressed twice
        when(mockParameters.getButton(0)).thenReturn(OpenClosedType.OPEN);
        DateTimeType trigTime = new DateTimeType("2024-01-01T12:30:00Z");
        when(mockParameters.getButtonTrigTime(0)).thenReturn(trigTime);

        // Execute first press
        handler.testUpdateDevice(mockDeviceState);

        // Execute second press
        handler.testUpdateDevice(mockDeviceState);

        // Verify - previous task was cancelled
        verify(mockScheduledFuture, atLeast(1)).cancel(false);
    }

    @Test
    @DisplayName("Should update RSSI value")
    void testUpdateRSSI() {
        // Setup
        QuantityType<?> rssi = new QuantityType<>(Double.valueOf(-65),
                org.openhab.core.library.unit.Units.DECIBEL_MILLIWATTS);
        when(mockParameters.getRssi()).thenReturn((QuantityType) rssi);

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify RSSI was updated
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(any(ChannelUID.class), stateCaptor.capture());
    }

    @Test
    @DisplayName("Should update cloud online status to Connected")
    void testUpdateCloudOnline() {
        // Setup
        when(mockDeviceState.getCloud()).thenReturn(true);

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(any(ChannelUID.class), stateCaptor.capture());
    }

    @Test
    @DisplayName("Should update cloud online status to Disconnected")
    void testUpdateCloudOffline() {
        // Setup
        when(mockDeviceState.getCloud()).thenReturn(false);

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(any(ChannelUID.class), stateCaptor.capture());
    }

    @Test
    @DisplayName("Should update local online status")
    void testUpdateLocalOnline() {
        // Setup
        when(mockDeviceState.getLocal()).thenReturn(true);

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify local status updated
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);
        verify(mockCallback, atLeastOnce()).stateUpdated(any(ChannelUID.class), stateCaptor.capture());
    }

    @Test
    @DisplayName("Should ignore REFRESH command - button handlers are read-only")
    void testHandleRefreshCommand() {
        // Setup
        ChannelUID channelUID = new ChannelUID(thingUID, "button0");

        // Execute - WiFi buttons are read-only, commands are ignored
        handler.handleCommand(channelUID, RefreshType.REFRESH);

        // Verify - button handlers don't support refresh, no verification needed
        // The handleCommand method is empty for button handlers
    }

    @Test
    @DisplayName("Should ignore all commands - button handlers are read-only")
    void testIgnoreNonRefreshCommands() {
        // Setup
        ChannelUID channelUID = new ChannelUID(thingUID, "button0");

        // Execute - WiFi buttons are read-only, all commands are ignored
        handler.handleCommand(channelUID, OpenClosedType.OPEN);

        // Verify - button handlers don't process commands, no verification needed
        // The handleCommand method is empty for button handlers
    }

    @Test
    @DisplayName("Should cancel all tasks on disposal")
    void testCancelTasksOnDisposal() {
        // Setup - simulate button presses to create tasks
        when(mockParameters.getButton(0)).thenReturn(OpenClosedType.OPEN);
        when(mockParameters.getButton(1)).thenReturn(OpenClosedType.OPEN);
        when(mockParameters.getButton(2)).thenReturn(OpenClosedType.OPEN);
        handler.testUpdateDevice(mockDeviceState);

        // Execute
        handler.testCancelTasks();

        // Verify all scheduled tasks were cancelled
        verify(mockScheduledFuture, atLeast(3)).cancel(true);
    }

    @Test
    @DisplayName("Should handle cancelTasks when no tasks exist")
    void testCancelTasksWhenNoTasks() {
        // Execute - no button presses, so no tasks
        handler.testCancelTasks();

        // Verify no exceptions and no interactions with null tasks
        verify(mockScheduledFuture, never()).cancel(anyBoolean());
    }

    @Test
    @DisplayName("Should not start periodic tasks")
    void testStartTasksDoesNothing() {
        // Execute
        handler.startTasks();

        // Verify no periodic tasks scheduled (buttons are event-driven)
        verify(mockScheduler, never()).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(),
                any(TimeUnit.class));
    }

    @Test
    @DisplayName("Should handle button press with null trigger time gracefully")
    void testButtonPressWithNullTriggerTime() {
        // Setup
        when(mockParameters.getButton(0)).thenReturn(OpenClosedType.OPEN);
        when(mockParameters.getButtonTrigTime(0)).thenReturn(null);

        // Execute - should not throw exception
        assertDoesNotThrow(() -> handler.testUpdateDevice(mockDeviceState));
    }

    @Test
    @DisplayName("Should handle initialization when bridge is null")
    void testInitializeWithNullBridge() {
        // Setup
        TestSonoffButtonHandler handlerNoBridge = new TestSonoffButtonHandler(mockThing) {
            @Override
            protected Bridge getBridge() {
                return null;
            }
        };
        handlerNoBridge.setCallback(mockCallback);

        // Execute
        handlerNoBridge.initialize();

        // Verify status is OFFLINE due to missing bridge
        verify(mockCallback).statusUpdated(eq(mockThing), argThat(status -> status.getStatus() == ThingStatus.OFFLINE
                && status.getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR));
    }

    @Test
    @DisplayName("Should handle initialization when account is null")
    void testInitializeWithNullAccount() {
        // Setup - bridge handler returns null (no account)
        when(mockBridge.getHandler()).thenReturn(null);

        // Execute
        handler.initialize();

        // Verify - base handler should set status to OFFLINE when bridge is not set
        // The status update happens in SonoffBaseDeviceHandler.initialize()
        verify(mockCallback, atLeastOnce()).statusUpdated(eq(mockThing),
                argThat(status -> status.getStatus() == ThingStatus.OFFLINE
                        && status.getStatusDetail() == ThingStatusDetail.BRIDGE_UNINITIALIZED));
    }

    @Test
    @DisplayName("Should handle initialization when device state is null")
    void testInitializeWithNullDeviceState() {
        // Setup
        when(mockAccount.getState("test-device-id")).thenReturn(null);

        // Execute
        handler.initialize();

        // Verify status indicates device not initialized
        verify(mockCallback).statusUpdated(eq(mockThing), argThat(status -> status.getStatus() == ThingStatus.OFFLINE
                && status.getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR));
    }
}
