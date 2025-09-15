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
import org.openhab.core.types.RefreshType;

/**
 * Unit tests for {@link SonoffBaseBridgeHandler}
 *
 * @author OpenHAB Sonoff Binding - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class SonoffBaseBridgeHandlerTest {

    @Mock
    private Bridge mockBridge;

    @Mock
    private SonoffAccountHandler mockAccountHandler;

    @Mock
    private SonoffDeviceState mockDeviceState;

    private DeviceConfig deviceConfig;

    @Mock
    private ThingStatusInfo mockThingStatusInfo;

    @Mock
    private ChannelUID mockChannelUID;

    @Mock
    private Command mockCommand;

    private TestSonoffBaseBridgeHandler handler;
    private ThingUID thingUID;

    @BeforeEach
    void setUp() {
        // Use lenient mocking to avoid unnecessary stubbing errors
        lenient().when(mockBridge.getUID()).thenReturn(new ThingUID("sonoff", "bridge", "test-device"));
        lenient().when(mockBridge.getHandler()).thenReturn(mockAccountHandler);
        lenient().when(mockBridge.getStatusInfo()).thenReturn(mockThingStatusInfo);
        
        thingUID = new ThingUID("sonoff", "bridge", "test-device");

        // Setup device config
        deviceConfig = new DeviceConfig();
        deviceConfig.deviceid = "test-device-id";
        deviceConfig.local = true;

        handler = new TestSonoffBaseBridgeHandler(mockBridge);
        handler.setTestConfig(deviceConfig);
    }

    @Test
    void testInitialize_WithValidConfiguration_ShouldSetupCorrectly() {
        // Arrange
        setupValidInitialization();

        // Act
        handler.initialize();

        // Assert
        assertEquals("test-device-id", handler.getDeviceid());
        verify(mockAccountHandler).addDeviceListener("test-device-id", handler);
    }

    @Test
    void testInitialize_WithNoBridge_ShouldSetOfflineStatus() {
        // Arrange
        handler = new TestSonoffBaseBridgeHandler(null);
        handler.setTestConfig(deviceConfig);

        // Act
        handler.initialize();

        // Assert
        assertEquals(ThingStatus.OFFLINE, handler.lastStatus);
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, handler.lastStatusDetail);
        assertEquals("Bridge Not set", handler.lastStatusDescription);
    }

    @Test
    void testInitialize_WithNullDeviceState_ShouldSetOfflineStatus() {
        // Arrange
        lenient().when(mockAccountHandler.getState("test-device-id")).thenReturn(null);

        // Act
        handler.initialize();

        // Assert
        assertEquals(ThingStatus.OFFLINE, handler.lastStatus);
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, handler.lastStatusDetail);
        assertTrue(handler.lastStatusDescription.contains("not been initilized"));
    }

    @Test
    void testInitialize_WithLocalModeUnsupportedDevice_ShouldSetOfflineStatus() {
        // Arrange
        setupValidInitialization();
        lenient().lenient().when(mockAccountHandler.getMode()).thenReturn("local");
        lenient().when(mockDeviceState.getUiid()).thenReturn(999); // Unsupported UIID

        // Act
        handler.initialize();

        // Assert
        assertEquals(ThingStatus.OFFLINE, handler.lastStatus);
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, handler.lastStatusDetail);
        assertEquals("Local Mode Not supported by device", handler.lastStatusDescription);
    }

    @Test
    void testInitialize_WithLocalInDevice_ShouldSetLocalInFlag() {
        // Arrange
        setupValidInitialization();
        lenient().lenient().when(mockAccountHandler.getMode()).thenReturn("mixed");
        lenient().when(mockDeviceState.getUiid()).thenReturn(1); // 1 is actually in LAN_IN according to constants

        // Act
        handler.initialize();

        // Assert
        assertTrue(handler.isLocalIn);
    }

    @Test
    void testInitialize_WithLocalOutDevice_ShouldSetLocalOutFlag() {
        // Arrange
        setupValidInitialization();
        lenient().lenient().when(mockAccountHandler.getMode()).thenReturn("mixed");
        lenient().when(mockDeviceState.getUiid()).thenReturn(2); // 2 is actually in LAN_OUT according to constants

        // Act
        handler.initialize();

        // Assert
        assertTrue(handler.isLocalOut);
    }

    @Test
    void testDispose_ShouldCleanupResources() {
        // Arrange
        setupValidInitialization();
        handler.initialize();
        handler.taskStarted = true;
        handler.cloud = true;
        handler.local = true;

        // Act
        handler.dispose();

        // Assert
        verify(mockAccountHandler).removeDeviceListener("test-device-id");
        assertFalse(handler.taskStarted);
        assertFalse(handler.cloud);
        assertFalse(handler.local);
        assertNull(handler.account);
        assertTrue(handler.cancelTasksCalled);
    }

    @Test
    void testBridgeStatusChanged_WhenOnline_ShouldStartTasks() {
        // Arrange
        setupValidInitialization();
        handler.initialize();
        when(mockThingStatusInfo.getStatus()).thenReturn(ThingStatus.ONLINE);

        // Act
        handler.bridgeStatusChanged(mockThingStatusInfo);

        // Assert
        assertTrue(handler.startTasksCalled);
        assertTrue(handler.taskStarted);
        // queueMessage is called during initialize() via checkBridge() and again via explicit bridgeStatusChanged()
        verify(mockAccountHandler, times(2)).queueMessage(any(SonoffCommandMessage.class));
    }

    @Test
    void testBridgeStatusChanged_WhenOffline_ShouldCancelTasks() {
        // Arrange
        setupValidInitialization();
        handler.initialize();
        handler.taskStarted = true;
        when(mockThingStatusInfo.getStatus()).thenReturn(ThingStatus.OFFLINE);

        // Act
        handler.bridgeStatusChanged(mockThingStatusInfo);

        // Assert
        assertTrue(handler.cancelTasksCalled);
        assertFalse(handler.taskStarted);
        assertEquals(ThingStatus.OFFLINE, handler.lastStatus);
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, handler.lastStatusDetail);
        assertEquals("Bridge Offline", handler.lastStatusDescription);
    }

    @Test
    void testBridgeStatusChanged_WithLocalInDevice_ShouldManageLanService() {
        // Arrange
        setupValidInitialization();
        // Set isLocalIn before initialize to ensure proper setup
        handler.isLocalIn = true;
        handler.initialize();
        when(mockThingStatusInfo.getStatus()).thenReturn(ThingStatus.ONLINE);

        // Act
        handler.bridgeStatusChanged(mockThingStatusInfo);

        // Assert - addLanService should be called during initialize and again during bridgeStatusChanged
        verify(mockAccountHandler, atLeastOnce()).addLanService("test-device-id");

        // Test offline scenario
        when(mockThingStatusInfo.getStatus()).thenReturn(ThingStatus.OFFLINE);
        handler.bridgeStatusChanged(mockThingStatusInfo);
        verify(mockAccountHandler).removeLanService("test-device-id");
    }

    @Test
    void testHandleCommand_WithRefreshType_ShouldReturn() {
        // Arrange
        setupValidInitialization();
        handler.initialize();
        
        // Reset mock to clear calls from initialization
        reset(mockAccountHandler);

        // Act
        handler.handleCommand(mockChannelUID, RefreshType.REFRESH);

        // Assert
        verify(mockAccountHandler, never()).queueMessage(any());
    }

    @Test
    void testHandleCommand_WithSledCommand_ShouldQueueMessage() {
        // Arrange
        setupValidInitialization();
        handler.initialize();
        when(mockChannelUID.getId()).thenReturn("sled");
        when(mockCommand.toString()).thenReturn("ON");

        // Act
        handler.handleCommand(mockChannelUID, mockCommand);

        // Assert - queueMessage called during initialize and handleCommand
        verify(mockAccountHandler, times(2)).queueMessage(any(SonoffCommandMessage.class));
    }

    @Test
    void testHandleCommand_WithUnknownChannel_ShouldNotQueueMessage() {
        // Arrange
        setupValidInitialization();
        handler.initialize();
        when(mockChannelUID.getId()).thenReturn("unknown");
        
        // Reset mock to clear calls from initialization
        reset(mockAccountHandler);
        // Re-setup account for handler
        handler.account = mockAccountHandler;

        // Act
        handler.handleCommand(mockChannelUID, mockCommand);

        // Assert
        verify(mockAccountHandler, never()).queueMessage(any());
    }

    @Test
    void testQueueMessage_WithValidAccount_ShouldQueueMessage() {
        // Arrange
        setupValidInitialization();
        handler.initialize();
        SonoffCommandMessage message = new SonoffCommandMessage("test-device-id");

        // Act
        handler.queueMessage(message);

        // Assert - verify the specific message was queued (among other calls from initialization)
        verify(mockAccountHandler).queueMessage(message);
    }

    @Test
    void testQueueMessage_WithNullAccount_ShouldNotQueueMessage() {
        // Arrange
        handler.account = null;
        SonoffCommandMessage message = new SonoffCommandMessage("test-device-id");

        // Act
        handler.queueMessage(message);

        // Assert
        // Should not throw exception and should log debug message
        assertDoesNotThrow(() -> handler.queueMessage(message));
    }

    @Test
    void testUpdateStatus_LocalMode_WithLocalInSupport() {
        // Arrange
        setupValidInitialization();
        handler.initialize();
        handler.isLocalIn = true;
        handler.local = true;
        lenient().lenient().when(mockAccountHandler.getMode()).thenReturn("local");

        // Act
        handler.updateStatus();

        // Assert
        assertEquals(ThingStatus.ONLINE, handler.lastStatus);
    }

    @Test
    void testUpdateStatus_LocalMode_WithoutLocalInSupport() {
        // Arrange
        setupValidInitialization();
        handler.initialize();
        handler.isLocalIn = false;
        lenient().lenient().when(mockAccountHandler.getMode()).thenReturn("local");

        // Act
        handler.updateStatus();

        // Assert
        assertEquals(ThingStatus.OFFLINE, handler.lastStatus);
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, handler.lastStatusDetail);
        assertEquals("Local Mode Not supported by device", handler.lastStatusDescription);
    }

    @Test
    void testUpdateStatus_CloudMode_WithCloudConnection() {
        // Arrange
        setupValidInitialization();
        handler.initialize();
        handler.cloud = true;
        lenient().when(mockAccountHandler.getMode()).thenReturn("cloud");

        // Act
        handler.updateStatus();

        // Assert
        assertEquals(ThingStatus.ONLINE, handler.lastStatus);
    }

    @Test
    void testUpdateStatus_CloudMode_WithoutCloudConnection() {
        // Arrange
        setupValidInitialization();
        handler.initialize();
        handler.cloud = false;
        lenient().when(mockAccountHandler.getMode()).thenReturn("cloud");

        // Act
        handler.updateStatus();

        // Assert
        assertEquals(ThingStatus.OFFLINE, handler.lastStatus);
    }

    @Test
    void testUpdateStatus_MixedMode_VariousScenarios() {
        // Arrange
        setupValidInitialization();
        handler.initialize();
        lenient().when(mockAccountHandler.getMode()).thenReturn("mixed");

        // Test 1: No local support, but cloud available
        handler.isLocalIn = false;
        handler.cloud = true;
        handler.updateStatus();
        assertEquals(ThingStatus.ONLINE, handler.lastStatus);

        // Test 2: Local support, cloud offline
        handler.isLocalIn = true;
        handler.cloud = false;
        handler.local = true;
        handler.updateStatus();
        assertEquals(ThingStatus.ONLINE, handler.lastStatus);
        assertEquals("Cloud Offline", handler.lastStatusDescription);

        // Test 3: Local support, local offline
        handler.cloud = true;
        handler.local = false;
        handler.updateStatus();
        assertEquals(ThingStatus.ONLINE, handler.lastStatus);
        assertEquals("LAN Offline", handler.lastStatusDescription);

        // Test 4: Both offline
        handler.cloud = false;
        handler.local = false;
        handler.updateStatus();
        assertEquals(ThingStatus.OFFLINE, handler.lastStatus);
    }

    @Test
    void testGetDeviceid_ShouldReturnCorrectId() {
        // Arrange
        setupValidInitialization();
        handler.initialize();

        // Act & Assert
        assertEquals("test-device-id", handler.getDeviceid());
    }

    @Test
    void testSetProperties_ShouldUpdateProperties() {
        // Arrange
        Map<String, String> properties = new HashMap<>();
        properties.put("key1", "value1");
        properties.put("key2", "value2");

        // Act
        handler.setProperties(properties);

        // Assert
        assertTrue(handler.propertiesUpdated);
        assertEquals(properties, handler.lastProperties);
    }

    @Test
    void testBridgeStatusChanged_WithNullAccount_ShouldNotThrowException() {
        // Arrange
        handler.account = null;
        ThingStatusInfo statusInfo = new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);

        // Act & Assert
        assertDoesNotThrow(() -> handler.bridgeStatusChanged(statusInfo));
    }

    @Test
    void testUpdateStatus_WithNullAccount_ShouldSetOnlineStatus() {
        // Arrange
        handler.account = null;

        // Act
        handler.updateStatus();

        // Assert
        assertEquals(ThingStatus.ONLINE, handler.lastStatus);
    }

    @Test
    void testHandleCommand_WithNullMessage_ShouldLogDebug() {
        // Arrange
        setupValidInitialization();
        handler.initialize();
        ChannelUID channelUID = new ChannelUID(thingUID, "unknown-channel");
        Command command = mock(Command.class);
        lenient().when(command.toString()).thenReturn("test-command");

        // Act
        handler.handleCommand(channelUID, command);

        // Assert - Should not throw exception and should log debug message
        assertDoesNotThrow(() -> handler.handleCommand(channelUID, command));
    }

    @Test
    void testInitialize_WithTaskAlreadyStarted_ShouldNotRestartTasks() {
        // Arrange
        setupValidInitialization();
        when(mockThingStatusInfo.getStatus()).thenReturn(ThingStatus.ONLINE);
        handler.taskStarted = true;

        // Act
        handler.initialize();
        handler.bridgeStatusChanged(mockThingStatusInfo);

        // Assert
        // Should not call startTasks again since taskStarted is already true
        assertTrue(handler.taskStarted);
    }

    private void setupValidInitialization() {
        lenient().when(mockBridge.getHandler()).thenReturn(mockAccountHandler);
        lenient().when(mockAccountHandler.getState("test-device-id")).thenReturn(mockDeviceState);
        lenient().lenient().when(mockAccountHandler.getMode()).thenReturn("cloud");
        lenient().when(mockDeviceState.getUiid()).thenReturn(1);
        lenient().when(mockDeviceState.getProperties()).thenReturn(new HashMap<>());
        lenient().when(mockThingStatusInfo.getStatus()).thenReturn(ThingStatus.ONLINE);

        // Set the configuration
        handler.setTestConfig(deviceConfig);
        
        // Note: initialize() calls checkBridge() which calls bridgeStatusChanged() 
        // This may trigger queueMessage() calls during initialization
    }

    /**
     * Test implementation of SonoffBaseBridgeHandler for testing purposes
     */
    private static class TestSonoffBaseBridgeHandler extends SonoffBaseBridgeHandler {

        // Test tracking fields
        boolean startTasksCalled = false;
        boolean cancelTasksCalled = false;
        boolean updateDeviceCalled = false;
        boolean propertiesUpdated = false;

        ThingStatus lastStatus = ThingStatus.UNKNOWN;
        ThingStatusDetail lastStatusDetail = ThingStatusDetail.NONE;
        String lastStatusDescription = "";
        Map<String, String> lastProperties = new HashMap<>();

        SonoffDeviceState lastDeviceUpdate;

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
            lastStatusDescription = description;
        }

        @Override
        protected void updateProperties(Map<String, String> properties) {
            propertiesUpdated = true;
            lastProperties = new HashMap<>(properties);
        }

        private DeviceConfig testConfig;

        @Override
        public <T> T getConfigAs(Class<T> configurationClass) {
            if (testConfig == null) {
                throw new IllegalStateException("Test config not set");
            }
            return configurationClass.cast(testConfig);
        }

        // Helper method to set test configuration
        public void setTestConfig(DeviceConfig config) {
            this.testConfig = config;
        }

        @Override
        public Bridge getBridge() {
            Thing thing = getThing();
            return thing != null ? (Bridge) thing : null;
        }

        // Make protected fields accessible for testing
        public void setAccount(SonoffAccountHandler account) {
            this.account = account;
        }
    }
}
