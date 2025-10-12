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
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.State;

/**
 * Unit tests for {@link SonoffZigbeeButtonHandler}.
 * 
 * Tests cover:
 * - Handler initialization with configuration
 * - Button press event handling for all three buttons
 * - Scheduled reset tasks for button states
 * - Task cancellation on disposal
 * - Device state updates (battery, RSSI, cloud status)
 * - Edge cases and error conditions
 *
 * @author tschaban/SmartnyDom - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class SonoffZigbeeButtonHandlerTest {

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
    private TestSonoffZigbeeButtonHandler handler;

    /**
     * Test implementation that exposes protected methods for testing
     */
    private class TestSonoffZigbeeButtonHandler extends SonoffZigbeeButtonHandler {
        public TestSonoffZigbeeButtonHandler(Thing thing) {
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
    void setUp() {
        thingUID = new ThingUID("sonoff", "7000", "test-button");
        bridgeUID = new ThingUID("sonoff", "66", "test-bridge");

        // Setup configuration
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("deviceid", "test-device-id");
        configMap.put("buttonResetTimeout", 500);
        configuration = new Configuration(configMap);

        // Setup mock thing
        when(mockThing.getUID()).thenReturn(thingUID);
        when(mockThing.getConfiguration()).thenReturn(configuration);
        when(mockThing.getBridgeUID()).thenReturn(bridgeUID);

        // Setup mock bridge
        when(mockBridge.getUID()).thenReturn(bridgeUID);
        when(mockBridge.getHandler()).thenReturn(mockZigbeeBridge);

        // Setup mock scheduler
        @SuppressWarnings("unchecked")
        ScheduledFuture<?> typedFuture = mockScheduledFuture;
        lenient().when(mockScheduler.schedule(any(Runnable.class), anyLong(), any(TimeUnit.class)))
                .thenReturn(typedFuture);

        // Setup mock device state
        when(mockDeviceState.getParameters()).thenReturn(mockParameters);
        when(mockDeviceState.getCloud()).thenReturn(true);

        // Setup mock parameters - default all buttons to CLOSED
        when(mockParameters.getButton(anyInt())).thenReturn(OpenClosedType.CLOSED);
        when(mockParameters.getButtonTrigTime(anyInt())).thenReturn(new DateTimeType("2024-01-01T12:00:00Z"));
        when(mockParameters.getRssi()).thenReturn(
                new QuantityType<>(Double.valueOf(-50), org.openhab.core.library.unit.Units.DECIBEL_MILLIWATTS));
        when(mockParameters.getBatteryLevel())
                .thenReturn(new QuantityType<>(Double.valueOf(85), org.openhab.core.library.unit.Units.PERCENT));

        // Setup mock account
        when(mockZigbeeBridge.account).thenReturn(mockAccount);
        when(mockAccount.getState("test-device-id")).thenReturn(mockDeviceState);
        when(mockAccount.getMode()).thenReturn("cloud");

        // Create handler
        handler = new TestSonoffZigbeeButtonHandler(mockThing);
        handler.setCallback(mockCallback);
        handler.setTestScheduler(mockScheduler);
    }

    @Test
    @DisplayName("Should initialize with default button reset timeout")
    void testInitializeWithDefaultTimeout() {
        // Execute
        handler.initialize();

        // Verify initialization
        verify(mockAccount).addDeviceListener(eq("test-device-id"), eq(handler));
    }

    @Test
    @DisplayName("Should initialize with custom button reset timeout")
    void testInitializeWithCustomTimeout() {
        // Setup custom timeout
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("deviceid", "test-device-id");
        configMap.put("buttonResetTimeout", 1000);
        configuration = new Configuration(configMap);
        when(mockThing.getConfiguration()).thenReturn(configuration);

        // Execute
        handler.initialize();

        // Verify initialization
        verify(mockAccount).addDeviceListener(eq("test-device-id"), eq(handler));
    }

    @Test
    @DisplayName("Should handle button 0 press event")
    void testButton0PressEvent() {
        // Setup - button 0 pressed
        when(mockParameters.getButton(0)).thenReturn(OpenClosedType.OPEN);
        when(mockParameters.getButton(1)).thenReturn(OpenClosedType.CLOSED);
        when(mockParameters.getButton(2)).thenReturn(OpenClosedType.CLOSED);
        when(mockParameters.getButtonTrigTime(0)).thenReturn(new DateTimeType("2024-01-01T12:00:00Z"));

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify button state updated to OPEN
        verify(mockCallback).stateUpdated(eq(new ChannelUID(thingUID, "button0")), eq(OpenClosedType.OPEN));

        // Verify trigger time updated
        verify(mockCallback).stateUpdated(eq(new ChannelUID(thingUID, "button0TrigTime")),
                eq(new DateTimeType("2024-01-01T12:00:00Z")));

        // Verify reset task scheduled
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(mockScheduler).schedule(runnableCaptor.capture(), eq(500L), eq(TimeUnit.MILLISECONDS));

        // Execute the scheduled reset task
        runnableCaptor.getValue().run();

        // Verify button state reset to CLOSED
        verify(mockCallback).stateUpdated(eq(new ChannelUID(thingUID, "button0")), eq(OpenClosedType.CLOSED));

        // Verify button state was reset in parameters
        verify(mockParameters).setButton(0, OpenClosedType.CLOSED);
    }

    @Test
    @DisplayName("Should handle button 1 press event")
    void testButton1PressEvent() {
        // Setup - button 1 pressed
        when(mockParameters.getButton(0)).thenReturn(OpenClosedType.CLOSED);
        when(mockParameters.getButton(1)).thenReturn(OpenClosedType.OPEN);
        when(mockParameters.getButton(2)).thenReturn(OpenClosedType.CLOSED);
        when(mockParameters.getButtonTrigTime(1)).thenReturn(new DateTimeType("2024-01-01T12:00:01Z"));

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify button state updated to OPEN
        verify(mockCallback).stateUpdated(eq(new ChannelUID(thingUID, "button1")), eq(OpenClosedType.OPEN));

        // Verify trigger time updated
        verify(mockCallback).stateUpdated(eq(new ChannelUID(thingUID, "button1TrigTime")),
                eq(new DateTimeType("2024-01-01T12:00:01Z")));

        // Verify reset task scheduled
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(mockScheduler).schedule(runnableCaptor.capture(), eq(500L), eq(TimeUnit.MILLISECONDS));

        // Execute the scheduled reset task
        runnableCaptor.getValue().run();

        // Verify button state reset to CLOSED
        verify(mockCallback).stateUpdated(eq(new ChannelUID(thingUID, "button1")), eq(OpenClosedType.CLOSED));

        // Verify button state was reset in parameters
        verify(mockParameters).setButton(1, OpenClosedType.CLOSED);
    }

    @Test
    @DisplayName("Should handle button 2 press event")
    void testButton2PressEvent() {
        // Setup - button 2 pressed
        when(mockParameters.getButton(0)).thenReturn(OpenClosedType.CLOSED);
        when(mockParameters.getButton(1)).thenReturn(OpenClosedType.CLOSED);
        when(mockParameters.getButton(2)).thenReturn(OpenClosedType.OPEN);
        when(mockParameters.getButtonTrigTime(2)).thenReturn(new DateTimeType("2024-01-01T12:00:02Z"));

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify button state updated to OPEN
        verify(mockCallback).stateUpdated(eq(new ChannelUID(thingUID, "button2")), eq(OpenClosedType.OPEN));

        // Verify trigger time updated
        verify(mockCallback).stateUpdated(eq(new ChannelUID(thingUID, "button2TrigTime")),
                eq(new DateTimeType("2024-01-01T12:00:02Z")));

        // Verify reset task scheduled
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(mockScheduler).schedule(runnableCaptor.capture(), eq(500L), eq(TimeUnit.MILLISECONDS));

        // Execute the scheduled reset task
        runnableCaptor.getValue().run();

        // Verify button state reset to CLOSED
        verify(mockCallback).stateUpdated(eq(new ChannelUID(thingUID, "button2")), eq(OpenClosedType.CLOSED));

        // Verify button state was reset in parameters
        verify(mockParameters).setButton(2, OpenClosedType.CLOSED);
    }

    @Test
    @DisplayName("Should handle multiple button presses simultaneously")
    void testMultipleButtonPresses() {
        // Setup - all buttons pressed
        when(mockParameters.getButton(0)).thenReturn(OpenClosedType.OPEN);
        when(mockParameters.getButton(1)).thenReturn(OpenClosedType.OPEN);
        when(mockParameters.getButton(2)).thenReturn(OpenClosedType.OPEN);
        when(mockParameters.getButtonTrigTime(0)).thenReturn(new DateTimeType("2024-01-01T12:00:00Z"));
        when(mockParameters.getButtonTrigTime(1)).thenReturn(new DateTimeType("2024-01-01T12:00:01Z"));
        when(mockParameters.getButtonTrigTime(2)).thenReturn(new DateTimeType("2024-01-01T12:00:02Z"));

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify all button states updated to OPEN
        verify(mockCallback).stateUpdated(eq(new ChannelUID(thingUID, "button0")), eq(OpenClosedType.OPEN));
        verify(mockCallback).stateUpdated(eq(new ChannelUID(thingUID, "button1")), eq(OpenClosedType.OPEN));
        verify(mockCallback).stateUpdated(eq(new ChannelUID(thingUID, "button2")), eq(OpenClosedType.OPEN));

        // Verify all trigger times updated
        verify(mockCallback).stateUpdated(eq(new ChannelUID(thingUID, "button0TrigTime")),
                eq(new DateTimeType("2024-01-01T12:00:00Z")));
        verify(mockCallback).stateUpdated(eq(new ChannelUID(thingUID, "button1TrigTime")),
                eq(new DateTimeType("2024-01-01T12:00:01Z")));
        verify(mockCallback).stateUpdated(eq(new ChannelUID(thingUID, "button2TrigTime")),
                eq(new DateTimeType("2024-01-01T12:00:02Z")));

        // Verify three reset tasks scheduled
        verify(mockScheduler, times(3)).schedule(any(Runnable.class), eq(500L), eq(TimeUnit.MILLISECONDS));

        // Verify all button states were reset in parameters
        verify(mockParameters).setButton(0, OpenClosedType.CLOSED);
        verify(mockParameters).setButton(1, OpenClosedType.CLOSED);
        verify(mockParameters).setButton(2, OpenClosedType.CLOSED);
    }

    @Test
    @DisplayName("Should cancel existing reset task when button pressed again")
    @SuppressWarnings("unchecked")
    void testCancelExistingResetTask() {
        // Setup - button 0 pressed first time
        when(mockParameters.getButton(0)).thenReturn(OpenClosedType.OPEN);
        when(mockParameters.getButton(1)).thenReturn(OpenClosedType.CLOSED);
        when(mockParameters.getButton(2)).thenReturn(OpenClosedType.CLOSED);

        @SuppressWarnings("rawtypes")
        ScheduledFuture firstTask = mock(ScheduledFuture.class);
        @SuppressWarnings("rawtypes")
        ScheduledFuture secondTask = mock(ScheduledFuture.class);

        when(mockScheduler.schedule(any(Runnable.class), anyLong(), any(TimeUnit.class))).thenReturn(firstTask)
                .thenReturn(secondTask);

        // Execute first press
        handler.testUpdateDevice(mockDeviceState);

        // Verify first task scheduled
        verify(mockScheduler, times(1)).schedule(any(Runnable.class), eq(500L), eq(TimeUnit.MILLISECONDS));

        // Execute second press (button pressed again before reset)
        handler.testUpdateDevice(mockDeviceState);

        // Verify first task was cancelled
        verify(firstTask).cancel(false);

        // Verify second task scheduled
        verify(mockScheduler, times(2)).schedule(any(Runnable.class), eq(500L), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    @DisplayName("Should update RSSI state")
    void testUpdateRssi() {
        // Setup
        QuantityType<javax.measure.quantity.Power> rssi = new QuantityType<>(Double.valueOf(-65),
                org.openhab.core.library.unit.Units.DECIBEL_MILLIWATTS);
        when(mockParameters.getRssi()).thenReturn(rssi);

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify
        verify(mockCallback).stateUpdated(eq(new ChannelUID(thingUID, "rssi")), eq(rssi));
    }

    @Test
    @DisplayName("Should update battery level state")
    void testUpdateBatteryLevel() {
        // Setup
        QuantityType<javax.measure.quantity.Dimensionless> battery = new QuantityType<>(Double.valueOf(75),
                org.openhab.core.library.unit.Units.PERCENT);
        when(mockParameters.getBatteryLevel()).thenReturn(battery);

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify
        verify(mockCallback).stateUpdated(eq(new ChannelUID(thingUID, "battery")), eq(battery));
    }

    @Test
    @DisplayName("Should update cloud online status when connected")
    void testUpdateCloudOnlineConnected() {
        // Setup
        when(mockDeviceState.getCloud()).thenReturn(true);

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify
        verify(mockCallback).stateUpdated(eq(new ChannelUID(thingUID, "cloudOnline")), eq(new StringType("Connected")));
    }

    @Test
    @DisplayName("Should update cloud online status when disconnected")
    void testUpdateCloudOnlineDisconnected() {
        // Setup
        when(mockDeviceState.getCloud()).thenReturn(false);

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify
        verify(mockCallback).stateUpdated(eq(new ChannelUID(thingUID, "cloudOnline")),
                eq(new StringType("Disconnected")));
    }

    @Test
    @DisplayName("Should cancel all reset tasks on disposal")
    @SuppressWarnings("unchecked")
    void testCancelTasksOnDisposal() {
        // Setup - create tasks for all buttons
        @SuppressWarnings("rawtypes")
        ScheduledFuture task0 = mock(ScheduledFuture.class);
        @SuppressWarnings("rawtypes")
        ScheduledFuture task1 = mock(ScheduledFuture.class);
        @SuppressWarnings("rawtypes")
        ScheduledFuture task2 = mock(ScheduledFuture.class);

        when(mockScheduler.schedule(any(Runnable.class), anyLong(), any(TimeUnit.class))).thenReturn(task0)
                .thenReturn(task1).thenReturn(task2);

        // Press all buttons to create tasks
        when(mockParameters.getButton(0)).thenReturn(OpenClosedType.OPEN);
        when(mockParameters.getButton(1)).thenReturn(OpenClosedType.OPEN);
        when(mockParameters.getButton(2)).thenReturn(OpenClosedType.OPEN);
        handler.testUpdateDevice(mockDeviceState);

        // Execute
        handler.testCancelTasks();

        // Verify all tasks cancelled
        verify(task0).cancel(true);
        verify(task1).cancel(true);
        verify(task2).cancel(true);
    }

    @Test
    @DisplayName("Should handle cancel tasks when no tasks exist")
    void testCancelTasksWhenNoTasks() {
        // Execute - should not throw exception
        assertDoesNotThrow(() -> handler.testCancelTasks());
    }

    @Test
    @DisplayName("Should handle command without errors")
    void testHandleCommand() {
        // Setup
        ChannelUID channelUID = new ChannelUID(thingUID, "button0");
        org.openhab.core.types.Command command = mock(org.openhab.core.types.Command.class);

        // Execute - should not throw exception
        assertDoesNotThrow(() -> handler.handleCommand(channelUID, command));
    }

    @Test
    @DisplayName("Should not update button state when button is CLOSED")
    void testNoUpdateWhenButtonClosed() {
        // Setup - all buttons closed
        when(mockParameters.getButton(0)).thenReturn(OpenClosedType.CLOSED);
        when(mockParameters.getButton(1)).thenReturn(OpenClosedType.CLOSED);
        when(mockParameters.getButton(2)).thenReturn(OpenClosedType.CLOSED);

        // Execute
        handler.testUpdateDevice(mockDeviceState);

        // Verify no button state updates (only RSSI, battery, cloud status)
        verify(mockCallback, never()).stateUpdated(eq(new ChannelUID(thingUID, "button0")), any());
        verify(mockCallback, never()).stateUpdated(eq(new ChannelUID(thingUID, "button1")), any());
        verify(mockCallback, never()).stateUpdated(eq(new ChannelUID(thingUID, "button2")), any());

        // Verify no reset tasks scheduled
        verify(mockScheduler, never()).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
    }

    @Test
    @DisplayName("Should handle rapid button presses")
    @SuppressWarnings("unchecked")
    void testRapidButtonPresses() {
        // Setup
        when(mockParameters.getButton(0)).thenReturn(OpenClosedType.OPEN);
        when(mockParameters.getButton(1)).thenReturn(OpenClosedType.CLOSED);
        when(mockParameters.getButton(2)).thenReturn(OpenClosedType.CLOSED);

        @SuppressWarnings("rawtypes")
        ScheduledFuture task1 = mock(ScheduledFuture.class);
        @SuppressWarnings("rawtypes")
        ScheduledFuture task2 = mock(ScheduledFuture.class);
        @SuppressWarnings("rawtypes")
        ScheduledFuture task3 = mock(ScheduledFuture.class);

        when(mockScheduler.schedule(any(Runnable.class), anyLong(), any(TimeUnit.class))).thenReturn(task1)
                .thenReturn(task2).thenReturn(task3);

        // Execute - press button three times rapidly
        handler.testUpdateDevice(mockDeviceState);
        handler.testUpdateDevice(mockDeviceState);
        handler.testUpdateDevice(mockDeviceState);

        // Verify first two tasks were cancelled
        verify(task1).cancel(false);
        verify(task2).cancel(false);

        // Verify third task was not cancelled
        verify(task3, never()).cancel(anyBoolean());

        // Verify three tasks were scheduled
        verify(mockScheduler, times(3)).schedule(any(Runnable.class), eq(500L), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    @DisplayName("Should use custom timeout from configuration")
    void testCustomTimeoutConfiguration() {
        // Setup custom timeout
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("deviceid", "test-device-id");
        configMap.put("buttonResetTimeout", 2000);
        configuration = new Configuration(configMap);
        when(mockThing.getConfiguration()).thenReturn(configuration);

        // Create new handler with custom timeout
        TestSonoffZigbeeButtonHandler customHandler = new TestSonoffZigbeeButtonHandler(mockThing);
        customHandler.setCallback(mockCallback);
        customHandler.setTestScheduler(mockScheduler);
        customHandler.initialize();

        // Setup button press
        when(mockParameters.getButton(0)).thenReturn(OpenClosedType.OPEN);
        when(mockParameters.getButton(1)).thenReturn(OpenClosedType.CLOSED);
        when(mockParameters.getButton(2)).thenReturn(OpenClosedType.CLOSED);

        // Execute
        customHandler.testUpdateDevice(mockDeviceState);

        // Verify custom timeout used
        verify(mockScheduler).schedule(any(Runnable.class), eq(2000L), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    @DisplayName("Should handle startTasks without errors")
    void testStartTasks() {
        // Execute - should not throw exception
        assertDoesNotThrow(() -> handler.startTasks());
    }
}
