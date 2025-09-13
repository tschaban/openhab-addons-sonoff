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
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.sonoff.internal.SonoffBindingConstants;
import org.openhab.binding.sonoff.internal.communication.SonoffCommandMessage;
import org.openhab.binding.sonoff.internal.config.DeviceConfig;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.Command;

/**
 * Integration tests for {@link SonoffBaseBridgeHandler}
 * Tests the interaction between components and full lifecycle scenarios
 *
 * @author OpenHAB Sonoff Binding - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class SonoffBaseBridgeHandlerIntegrationTest {

    @Mock
    private Bridge mockBridge;

    @Mock
    private SonoffAccountHandler mockAccountHandler;

    @Mock
    private SonoffDeviceState mockDeviceState;

    @Mock
    private ThingStatusInfo mockThingStatusInfo;

    private DeviceConfig deviceConfig;
    private ThingUID thingUID;
    private TestSonoffBaseBridgeHandler handler;

    @BeforeEach
    void setUp() {
        thingUID = new ThingUID("sonoff", "bridge", "integration-test");
        when(mockBridge.getUID()).thenReturn(thingUID);
        when(mockBridge.getHandler()).thenReturn(mockAccountHandler);
        when(mockBridge.getStatusInfo()).thenReturn(mockThingStatusInfo);

        // Setup device config
        deviceConfig = new DeviceConfig();
        deviceConfig.deviceid = "integration-device-id";
        deviceConfig.local = true;
        deviceConfig.consumptionPoll = 30;
        deviceConfig.localPoll = 10;
        deviceConfig.consumption = true;

        handler = new TestSonoffBaseBridgeHandler(mockBridge);
        handler.setTestConfig(deviceConfig);
    }

    @Test
    void testFullLifecycle_InitializeToDispose() {
        // Arrange
        setupValidEnvironment();

        // Act - Initialize
        handler.initialize();

        // Assert - Initialization
        assertEquals("integration-device-id", handler.getDeviceid());
        verify(mockAccountHandler).addDeviceListener("integration-device-id", handler);
        assertTrue(handler.startTasksCalled);

        // Act - Bridge status change to online
        ThingStatusInfo onlineStatus = new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);
        handler.bridgeStatusChanged(onlineStatus);

        // Assert - Online status handling
        verify(mockAccountHandler).queueMessage(any(SonoffCommandMessage.class));
        assertEquals(ThingStatus.ONLINE, handler.lastStatus);

        // Act - Dispose
        handler.dispose();

        // Assert - Cleanup
        verify(mockAccountHandler).removeDeviceListener("integration-device-id");
        assertTrue(handler.cancelTasksCalled);
        assertFalse(handler.taskStarted);
        assertFalse(handler.cloud);
        assertFalse(handler.local);
        assertNull(handler.account);
    }

    @Test
    void testBridgeStatusTransitions_OnlineToOfflineToOnline() {
        // Arrange
        setupValidEnvironment();
        handler.initialize();

        // Act 1 - Bridge goes online
        ThingStatusInfo onlineStatus = new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);
        handler.bridgeStatusChanged(onlineStatus);

        // Assert 1
        assertTrue(handler.taskStarted);
        assertEquals(ThingStatus.ONLINE, handler.lastStatus);

        // Act 2 - Bridge goes offline
        ThingStatusInfo offlineStatus = new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "Connection lost");
        handler.bridgeStatusChanged(offlineStatus);

        // Assert 2
        assertFalse(handler.taskStarted);
        assertEquals(ThingStatus.OFFLINE, handler.lastStatus);
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, handler.lastStatusDetail);
        assertEquals("Bridge Offline", handler.lastStatusDescription);

        // Act 3 - Bridge comes back online
        handler.bridgeStatusChanged(onlineStatus);

        // Assert 3
        assertTrue(handler.taskStarted);
        assertEquals(ThingStatus.ONLINE, handler.lastStatus);
    }

    @Test
    void testMultiModeOperations_CloudAndLocalTransitions() {
        // Arrange
        setupValidEnvironment();
        try (MockedStatic<SonoffBindingConstants> mockedConstants = mockStatic(SonoffBindingConstants.class)) {
            mockedConstants.when(() -> SonoffBindingConstants.LAN_IN.contains(1)).thenReturn(true);

            when(mockAccountHandler.getMode()).thenReturn("mixed");
            handler.initialize();

            // Test cloud mode
            handler.cloud = true;
            handler.local = false;
            handler.updateStatus();
            assertEquals(ThingStatus.ONLINE, handler.lastStatus);
            assertEquals("LAN Offline", handler.lastStatusDescription);

            // Test local mode
            handler.cloud = false;
            handler.local = true;
            handler.updateStatus();
            assertEquals(ThingStatus.ONLINE, handler.lastStatus);
            assertEquals("Cloud Offline", handler.lastStatusDescription);

            // Test both online
            handler.cloud = true;
            handler.local = true;
            handler.updateStatus();
            assertEquals(ThingStatus.ONLINE, handler.lastStatus);
            assertEquals("", handler.lastStatusDescription);
        }
    }

    @Test
    void testErrorRecovery_FromConfigurationError() {
        // Arrange - Start with invalid configuration
        when(mockBridge.getHandler()).thenReturn(null);

        // Act 1 - Initialize with invalid config
        handler.initialize();

        // Assert 1 - Should be offline
        assertEquals(ThingStatus.OFFLINE, handler.lastStatus);
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, handler.lastStatusDetail);

        // Act 2 - Fix configuration
        when(mockBridge.getHandler()).thenReturn(mockAccountHandler);
        setupValidEnvironment();
        handler.initialize();

        // Assert 2 - Should recover
        assertEquals("integration-device-id", handler.getDeviceid());
        verify(mockAccountHandler).addDeviceListener("integration-device-id", handler);
    }

    @Test
    void testConcurrentOperations_StatusUpdatesAndCommands() throws InterruptedException {
        // Arrange
        setupValidEnvironment();
        handler.initialize();

        CountDownLatch latch = new CountDownLatch(2);

        // Act - Simulate concurrent operations
        Thread statusThread = new Thread(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    handler.updateStatus();
                    Thread.sleep(10);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                latch.countDown();
            }
        });

        Thread commandThread = new Thread(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    ChannelUID channelUID = new ChannelUID(thingUID, "sled");
                    Command command = mock(Command.class);
                    when(command.toString()).thenReturn("on");
                    handler.handleCommand(channelUID, command);
                    Thread.sleep(10);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                latch.countDown();
            }
        });

        statusThread.start();
        commandThread.start();

        // Assert - Should complete without deadlock
        assertTrue(latch.await(5, TimeUnit.SECONDS));

        // Verify no exceptions occurred and operations completed
        verify(mockAccountHandler, atLeast(5)).queueMessage(any(SonoffCommandMessage.class));
    }

    private void setupValidEnvironment() {
        when(mockAccountHandler.getState("integration-device-id")).thenReturn(mockDeviceState);
        when(mockAccountHandler.getMode()).thenReturn("cloud");
        when(mockDeviceState.getUiid()).thenReturn(1);
        when(mockDeviceState.getProperties()).thenReturn(new HashMap<>());
        when(mockThingStatusInfo.getStatus()).thenReturn(ThingStatus.ONLINE);
    }

    /**
     * Test implementation of SonoffBaseBridgeHandler for integration testing
     */
    private static class TestSonoffBaseBridgeHandler extends SonoffBaseBridgeHandler {

        // Test tracking fields
        boolean startTasksCalled = false;
        boolean cancelTasksCalled = false;
        boolean updateDeviceCalled = false;

        ThingStatus lastStatus = ThingStatus.UNKNOWN;
        ThingStatusDetail lastStatusDetail = ThingStatusDetail.NONE;
        String lastStatusDescription = "";

        SonoffDeviceState lastDeviceUpdate;
        private DeviceConfig testConfig;

        public TestSonoffBaseBridgeHandler(Bridge thing) {
            super(thing);
        }

        @Override
        public void startTasks() {
            startTasksCalled = true;
        }

        @Override
        public void cancelTasks() {
            cancelTasksCalled = true;
        }

        @Override
        public void updateDevice(SonoffDeviceState newDevice) {
            updateDeviceCalled = true;
            lastDeviceUpdate = newDevice;
        }

        @Override
        protected void updateStatus(ThingStatus status) {
            lastStatus = status;
            lastStatusDetail = ThingStatusDetail.NONE;
            lastStatusDescription = "";
        }

        @Override
        protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, String description) {
            lastStatus = status;
            lastStatusDetail = statusDetail;
            lastStatusDescription = description != null ? description : "";
        }

        @Override
        protected void updateProperties(Map<String, String> properties) {
            // Track property updates if needed
        }

        @Override
        public <T> T getConfigAs(Class<T> configurationClass) {
            if (testConfig == null) {
                throw new IllegalStateException("Test config not set");
            }
            return configurationClass.cast(testConfig);
        }

        public void setTestConfig(DeviceConfig config) {
            this.testConfig = config;
        }

        @Override
        public Bridge getBridge() {
            return (Bridge) getThing();
        }

        // Make protected fields accessible for testing
        public void setAccount(SonoffAccountHandler account) {
            this.account = account;
        }
    }
}
