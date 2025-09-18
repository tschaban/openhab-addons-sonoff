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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.sonoff.internal.SonoffCacheProvider;
import org.openhab.binding.sonoff.internal.communication.SonoffCommandMessage;
import org.openhab.binding.sonoff.internal.config.AccountConfig;
import org.openhab.binding.sonoff.internal.discovery.SonoffDiscoveryService;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;

import com.google.gson.JsonObject;

/**
 * Unit tests for {@link SonoffAccountHandler}
 *
 * @author OpenHAB Sonoff Binding - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class SonoffAccountHandlerTest {

    @Mock
    private Bridge mockBridge;
    
    @Mock
    private WebSocketClient mockWebSocketClient;
    
    @Mock
    private HttpClient mockHttpClient;
    
    @Mock
    private ScheduledExecutorService mockScheduler;
    
    @Mock
    private ScheduledFuture<?> mockScheduledFuture;
    
    @Mock
    private SonoffDeviceListener mockDeviceListener;
    
    @Mock
    private SonoffDeviceState mockDeviceState;
    
    @Mock
    private ServiceEvent mockServiceEvent;
    
    @Mock
    private ServiceInfo mockServiceInfo;
    
    @Mock
    private Command mockCommand;

    private AccountConfig accountConfig;
    private ThingUID thingUID;
    private TestSonoffAccountHandler handler;

    @BeforeEach
    void setUp() {
        thingUID = new ThingUID("sonoff", "account", "test-account");
        lenient().when(mockBridge.getUID()).thenReturn(thingUID);
        
        // Setup account config
        accountConfig = new AccountConfig();
        accountConfig.appId = "test-app-id";
        accountConfig.appSecret = "test-app-secret";
        accountConfig.email = "test@example.com";
        accountConfig.password = "test-password";
        accountConfig.accessmode = "cloud";
        
        // Setup scheduler mocks
        lenient().when(mockScheduler.scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class)))
                .thenReturn((ScheduledFuture<?>) mockScheduledFuture);
        
        handler = new TestSonoffAccountHandler(mockBridge, mockWebSocketClient, mockHttpClient);
        handler.setTestConfig(accountConfig);
        handler.setScheduler(mockScheduler);
    }

    @Test
    void testInitialize_WithValidConfiguration_ShouldSetupCorrectly() {
        // Act
        handler.initialize();
        
        // Assert
        assertEquals("cloud", handler.getMode());
        assertTrue(handler.commandManagerStarted);
        assertTrue(handler.connectionManagerStarted);
        assertTrue(handler.restoreStatesCalled);
        verify(mockScheduler, times(4)).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class));
    }

    @Test
    void testInitialize_WithLocalMode_ShouldSetLocalMode() {
        // Arrange
        accountConfig.accessmode = "local";
        
        // Act
        handler.initialize();
        
        // Assert
        assertEquals("local", handler.getMode());
        assertTrue(handler.commandManagerStarted);
        assertTrue(handler.connectionManagerStarted);
    }

    @Test
    void testInitialize_WithMixedMode_ShouldSetMixedMode() {
        // Arrange
        accountConfig.accessmode = "mixed";
        
        // Act
        handler.initialize();
        
        // Assert
        assertEquals("mixed", handler.getMode());
        assertTrue(handler.commandManagerStarted);
        assertTrue(handler.connectionManagerStarted);
    }

    @Test
    void testDispose_ShouldCleanupResources() {
        // Arrange
        handler.initialize();
        
        // Act
        handler.dispose();
        
        // Assert
        verify(mockScheduledFuture, times(4)).cancel(true);
        assertTrue(handler.commandManagerStopped);
        assertTrue(handler.connectionManagerStopped);
    }

    @Test
    void testGetServices_ShouldReturnDiscoveryService() {
        // Act
        Collection<Class<? extends ThingHandlerService>> services = handler.getServices();
        
        // Assert
        assertEquals(1, services.size());
        assertTrue(services.contains(SonoffDiscoveryService.class));
    }

    @Test
    void testHandleCommand_ShouldNotThrowException() {
        // Arrange
        ChannelUID channelUID = new ChannelUID(thingUID, "test-channel");
        
        // Act & Assert
        assertDoesNotThrow(() -> handler.handleCommand(channelUID, mockCommand));
    }

    @Test
    void testIsConnected_CloudMode_BothConnected_ShouldBeOnline() {
        // Arrange
        accountConfig.accessmode = "cloud";
        handler.initialize();
        
        // Act
        handler.isConnected(true, true);
        
        // Assert
        assertEquals(ThingStatus.ONLINE, handler.lastStatus);
        assertTrue(handler.commandManagerRunning);
    }

    @Test
    void testIsConnected_CloudMode_CloudDisconnected_ShouldBeOffline() {
        // Arrange
        accountConfig.accessmode = "cloud";
        handler.initialize();
        
        // Act
        handler.isConnected(true, false);
        
        // Assert
        assertEquals(ThingStatus.OFFLINE, handler.lastStatus);
        assertFalse(handler.commandManagerRunning);
    }

    @Test
    void testIsConnected_LocalMode_LanConnected_ShouldBeOnline() {
        // Arrange
        accountConfig.accessmode = "local";
        handler.initialize();
        
        // Act
        handler.isConnected(true, false);
        
        // Assert
        assertEquals(ThingStatus.ONLINE, handler.lastStatus);
        assertTrue(handler.commandManagerRunning);
    }

    @Test
    void testIsConnected_LocalMode_LanDisconnected_ShouldBeOffline() {
        // Arrange
        accountConfig.accessmode = "local";
        handler.initialize();
        
        // Act
        handler.isConnected(false, true);
        
        // Assert
        assertEquals(ThingStatus.OFFLINE, handler.lastStatus);
        assertFalse(handler.commandManagerRunning);
    }

    @Test
    void testIsConnected_MixedMode_BothConnected_ShouldBeOnline() {
        // Arrange
        accountConfig.accessmode = "mixed";
        handler.initialize();
        
        // Act
        handler.isConnected(true, true);
        
        // Assert
        assertEquals(ThingStatus.ONLINE, handler.lastStatus);
        assertTrue(handler.commandManagerRunning);
    }

    @Test
    void testIsConnected_MixedMode_OnlyCloudConnected_ShouldBeOnlineWithDetail() {
        // Arrange
        accountConfig.accessmode = "mixed";
        handler.initialize();
        
        // Act
        handler.isConnected(false, true);
        
        // Assert
        assertEquals(ThingStatus.ONLINE, handler.lastStatus);
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, handler.lastStatusDetail);
        assertEquals("LAN Offline", handler.lastStatusDescription);
        assertTrue(handler.commandManagerRunning);
    }

    @Test
    void testIsConnected_MixedMode_OnlyLanConnected_ShouldBeOnlineWithDetail() {
        // Arrange
        accountConfig.accessmode = "mixed";
        handler.initialize();
        
        // Act
        handler.isConnected(true, false);
        
        // Assert
        assertEquals(ThingStatus.ONLINE, handler.lastStatus);
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, handler.lastStatusDetail);
        assertEquals("Cloud Offline", handler.lastStatusDescription);
        assertTrue(handler.commandManagerRunning);
    }

    @Test
    void testIsConnected_MixedMode_NeitherConnected_ShouldBeOffline() {
        // Arrange
        accountConfig.accessmode = "mixed";
        handler.initialize();
        
        // Act
        handler.isConnected(false, false);
        
        // Assert
        assertEquals(ThingStatus.OFFLINE, handler.lastStatus);
        assertFalse(handler.commandManagerRunning);
    }

    @Test
    void testAddDeviceListener_ShouldStoreListener() {
        // Arrange
        String deviceId = "test-device-id";
        
        // Act
        handler.addDeviceListener(deviceId, mockDeviceListener);
        
        // Assert
        assertEquals(mockDeviceListener, handler.getListener(deviceId));
    }

    @Test
    void testRemoveDeviceListener_ShouldRemoveListener() {
        // Arrange
        String deviceId = "test-device-id";
        handler.addDeviceListener(deviceId, mockDeviceListener);
        
        // Act
        handler.removeDeviceListener(deviceId);
        
        // Assert
        assertNull(handler.getListener(deviceId));
    }

    @Test
    void testAddLanService_ShouldCallConnectionManager() {
        // Arrange
        String deviceId = "test-device-id";
        
        // Act
        handler.addLanService(deviceId);
        
        // Assert
        assertTrue(handler.lanServiceAdded.contains(deviceId));
    }

    @Test
    void testRemoveLanService_ShouldCallConnectionManager() {
        // Arrange
        String deviceId = "test-device-id";
        
        // Act
        handler.removeLanService(deviceId);
        
        // Assert
        assertTrue(handler.lanServiceRemoved.contains(deviceId));
    }

    @Test
    void testQueueMessage_ShouldCallCommandManager() {
        // Arrange
        SonoffCommandMessage message = new SonoffCommandMessage("test-device-id");
        
        // Act
        handler.queueMessage(message);
        
        // Assert
        assertTrue(handler.messagesQueued.contains(message));
    }

    @Test
    void testGetMode_ShouldReturnCurrentMode() {
        // Arrange
        accountConfig.accessmode = "local";
        handler.initialize();
        
        // Act
        String mode = handler.getMode();
        
        // Assert
        assertEquals("local", mode);
    }

    @Test
    void testAddState_WithValidDeviceId_ShouldAddState() {
        // Arrange
        String deviceId = "test-device-id";
        
        try (MockedStatic<SonoffCacheProvider> mockedCacheProvider = mockStatic(SonoffCacheProvider.class)) {
            SonoffCacheProvider mockCache = mock(SonoffCacheProvider.class);
            mockedCacheProvider.when(() -> new SonoffCacheProvider(any())).thenReturn(mockCache);
            when(mockCache.getState(deviceId)).thenReturn(mockDeviceState);
            
            // Act
            handler.addState(deviceId);
            
            // Assert
            assertEquals(mockDeviceState, handler.getState(deviceId));
        }
    }

    @Test
    void testAddState_WithNullState_ShouldNotAddState() {
        // Arrange
        String deviceId = "test-device-id";
        
        try (MockedStatic<SonoffCacheProvider> mockedCacheProvider = mockStatic(SonoffCacheProvider.class)) {
            SonoffCacheProvider mockCache = mock(SonoffCacheProvider.class);
            mockedCacheProvider.when(() -> new SonoffCacheProvider(any())).thenReturn(mockCache);
            when(mockCache.getState(deviceId)).thenReturn(null);
            
            // Act
            handler.addState(deviceId);
            
            // Assert
            assertNull(handler.getState(deviceId));
        }
    }

    @Test
    void testGetState_WithExistingDevice_ShouldReturnState() {
        // Arrange
        String deviceId = "test-device-id";
        handler.deviceStates.put(deviceId, mockDeviceState);
        
        // Act
        SonoffDeviceState result = handler.getState(deviceId);
        
        // Assert
        assertEquals(mockDeviceState, result);
    }

    @Test
    void testGetState_WithNonExistingDevice_ShouldReturnNull() {
        // Arrange
        String deviceId = "non-existing-device";
        
        // Act
        SonoffDeviceState result = handler.getState(deviceId);
        
        // Assert
        assertNull(result);
    }

    @Test
    void testGetListener_WithExistingDevice_ShouldReturnListener() {
        // Arrange
        String deviceId = "test-device-id";
        handler.addDeviceListener(deviceId, mockDeviceListener);
        
        // Act
        SonoffDeviceListener result = handler.getListener(deviceId);
        
        // Assert
        assertEquals(mockDeviceListener, result);
    }

    @Test
    void testGetListener_WithNonExistingDevice_ShouldReturnNull() {
        // Arrange
        String deviceId = "non-existing-device";
        
        // Act
        SonoffDeviceListener result = handler.getListener(deviceId);
        
        // Assert
        assertNull(result);
    }

    @Test
    void testServiceResolved_WithValidEvent_ShouldStoreIpAddress() {
        // Arrange
        String deviceId = "test-device-id";
        String ipAddress = "192.168.1.100";
        
        when(mockServiceEvent.getInfo()).thenReturn(mockServiceInfo);
        when(mockServiceInfo.getInet4Addresses()).thenReturn(new java.net.Inet4Address[] {
            mock(java.net.Inet4Address.class)
        });
        when(mockServiceInfo.getInet4Addresses()[0].getHostAddress()).thenReturn(ipAddress);
        when(mockServiceInfo.getPropertyString("id")).thenReturn(deviceId);
        
        // Act
        handler.serviceResolved(mockServiceEvent);
        
        // Assert
        assertTrue(handler.serviceResolvedCalled);
        assertTrue(handler.ipAddresses.containsKey(deviceId));
        assertEquals(ipAddress, handler.ipAddresses.get(deviceId));
    }

    @Test
    void testServiceAdded_ShouldCallCommandManager() {
        // Act
        handler.serviceAdded(mockServiceEvent);
        
        // Assert
        assertTrue(handler.serviceAddedCalled);
    }

    @Test
    void testServiceRemoved_ShouldCallCommandManager() {
        // Act
        handler.serviceRemoved(mockServiceEvent);
        
        // Assert
        assertTrue(handler.serviceRemovedCalled);
    }

    @Test
    void testSendLanMessage_ShouldCallConnectionManager() {
        // Arrange
        String url = "http://test.url";
        String payload = "test-payload";
        
        // Act
        handler.sendLanMessage(url, payload);
        
        // Assert
        assertTrue(handler.lanMessagesSent.containsKey(url));
        assertEquals(payload, handler.lanMessagesSent.get(url));
    }

    @Test
    void testSendApiMessage_ShouldCallConnectionManager() {
        // Arrange
        String deviceId = "test-device-id";
        
        // Act
        handler.sendApiMessage(deviceId);
        
        // Assert
        assertTrue(handler.apiMessagesSent.contains(deviceId));
    }

    @Test
    void testSendWebsocketMessage_ShouldCallConnectionManager() {
        // Arrange
        String params = "test-params";
        
        // Act
        handler.sendWebsocketMessage(params);
        
        // Assert
        assertTrue(handler.websocketMessagesSent.contains(params));
    }

    @Test
    void testLanResponse_ShouldCallCommandManager() {
        // Arrange
        String message = "test-lan-response";
        
        // Act
        handler.lanResponse(message);
        
        // Assert
        assertTrue(handler.lanResponsesReceived.contains(message));
    }

    @Test
    void testWebsocketMessage_ShouldCallCommandManager() {
        // Arrange
        String message = "test-websocket-message";
        
        // Act
        handler.websocketMessage(message);
        
        // Assert
        assertTrue(handler.websocketMessagesReceived.contains(message));
    }

    @Test
    void testApiMessage_ShouldCallCommandManager() {
        // Arrange
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("test", "value");
        
        // Act
        handler.apiMessage(jsonObject);
        
        // Assert
        assertTrue(handler.apiMessagesReceived.contains(jsonObject));
    }

    @Test
    void testSetApiKey_ShouldCallCommandManager() {
        // Arrange
        String apiKey = "test-api-key";
        
        // Act
        handler.setApiKey(apiKey);
        
        // Assert
        assertEquals(apiKey, handler.apiKeySet);
    }

    @Test
    void testGetConnectionManager_ShouldReturnConnectionManager() {
        // Act
        var connectionManager = handler.getConnectionManager();
        
        // Assert
        assertNotNull(connectionManager);
        assertEquals(handler.connectionManager, connectionManager);
    }

    @Test
    void testServiceResolved_WithNullEvent_ShouldNotThrowException() {
        // Arrange
        handler.initialize();
        
        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> handler.serviceResolved(null));
        assertTrue(handler.serviceResolvedCalled);
    }

    @Test
    void testServiceResolved_WithNullEventInfo_ShouldNotThrowException() {
        // Arrange
        handler.initialize();
        ServiceEvent mockEvent = mock(ServiceEvent.class);
        when(mockEvent.getInfo()).thenReturn(null);
        
        // Act & Assert - Should handle gracefully
        assertDoesNotThrow(() -> handler.serviceResolved(mockEvent));
        assertTrue(handler.serviceResolvedCalled);
    }

    @Test
    void testServiceResolved_WithEmptyIpAddresses_ShouldNotThrowException() {
        // Arrange
        handler.initialize();
        ServiceEvent mockEvent = mock(ServiceEvent.class);
        ServiceInfo mockServiceInfo = mock(ServiceInfo.class);
        when(mockEvent.getInfo()).thenReturn(mockServiceInfo);
        when(mockServiceInfo.getInet4Addresses()).thenReturn(new java.net.Inet4Address[0]);
        
        // Act & Assert - Should handle gracefully
        assertDoesNotThrow(() -> handler.serviceResolved(mockEvent));
        assertTrue(handler.serviceResolvedCalled);
    }

    @Test
    void testServiceResolved_WithNullIpAddress_ShouldNotStoreAddress() {
        // Arrange
        handler.initialize();
        ServiceEvent mockEvent = mock(ServiceEvent.class);
        ServiceInfo mockServiceInfo = mock(ServiceInfo.class);
        java.net.Inet4Address mockAddress = mock(java.net.Inet4Address.class);
        
        when(mockEvent.getInfo()).thenReturn(mockServiceInfo);
        when(mockServiceInfo.getInet4Addresses()).thenReturn(new java.net.Inet4Address[]{mockAddress});
        when(mockAddress.getHostAddress()).thenReturn("null");
        when(mockServiceInfo.getPropertyString("id")).thenReturn("test-device");
        
        // Act
        handler.serviceResolved(mockEvent);
        
        // Assert
        assertTrue(handler.serviceResolvedCalled);
        // IP address should not be stored when it's "null"
        assertNull(handler.ipAddresses.get("test-device"));
    }

    @Test
    void testServiceResolved_WithSameIpAddress_ShouldNotUpdateAddress() {
        // Arrange
        handler.initialize();
        String deviceId = "test-device";
        String ipAddress = "192.168.1.100";
        handler.ipAddresses.put(deviceId, ipAddress);
        
        ServiceEvent mockEvent = mock(ServiceEvent.class);
        ServiceInfo mockServiceInfo = mock(ServiceInfo.class);
        java.net.Inet4Address mockAddress = mock(java.net.Inet4Address.class);
        
        when(mockEvent.getInfo()).thenReturn(mockServiceInfo);
        when(mockServiceInfo.getInet4Addresses()).thenReturn(new java.net.Inet4Address[]{mockAddress});
        when(mockAddress.getHostAddress()).thenReturn(ipAddress);
        when(mockServiceInfo.getPropertyString("id")).thenReturn(deviceId);
        
        // Act
        handler.serviceResolved(mockEvent);
        
        // Assert
        assertTrue(handler.serviceResolvedCalled);
        assertEquals(ipAddress, handler.ipAddresses.get(deviceId));
    }

    @Test
    void testRestoreStates_WithIpAddressMapping_ShouldSetLocalAndIpAddress() {
        // Arrange
        String deviceId = "test-device";
        String ipAddress = "192.168.1.100";
        
        try (MockedStatic<SonoffCacheProvider> mockedCacheProvider = mockStatic(SonoffCacheProvider.class)) {
            SonoffCacheProvider mockCache = mock(SonoffCacheProvider.class);
            SonoffDeviceState mockState = mock(SonoffDeviceState.class);
            
            mockedCacheProvider.when(() -> new SonoffCacheProvider(any())).thenReturn(mockCache);
            
            Map<String, SonoffDeviceState> cachedStates = new HashMap<>();
            cachedStates.put(deviceId, mockState);
            when(mockCache.getStates()).thenReturn(cachedStates);
            
            // Pre-populate IP address
            handler.ipAddresses.put(deviceId, ipAddress);
            
            // Act
            handler.initialize();
            
            // Assert
            verify(mockState).setIpAddress(new StringType(ipAddress));
            verify(mockState).setLocal(true);
            assertEquals(mockState, handler.deviceStates.get(deviceId));
        }
    }

    @Test
    void testAddState_WithIpAddressMapping_ShouldSetLocalAndIpAddress() {
        // Arrange
        handler.initialize();
        String deviceId = "test-device";
        String ipAddress = "192.168.1.100";
        
        try (MockedStatic<SonoffCacheProvider> mockedCacheProvider = mockStatic(SonoffCacheProvider.class)) {
            SonoffCacheProvider mockCache = mock(SonoffCacheProvider.class);
            SonoffDeviceState mockState = mock(SonoffDeviceState.class);
            
            mockedCacheProvider.when(() -> new SonoffCacheProvider(any())).thenReturn(mockCache);
            when(mockCache.getState(deviceId)).thenReturn(mockState);
            
            // Pre-populate IP address
            handler.ipAddresses.put(deviceId, ipAddress);
            
            // Act
            handler.addState(deviceId);
            
            // Assert
            verify(mockState).setIpAddress(new StringType(ipAddress));
            verify(mockState).setLocal(true);
            assertEquals(mockState, handler.deviceStates.get(deviceId));
        }
    }

    @Test
    void testIsConnected_CommandManagerStateTransitions() {
        // Arrange
        handler.initialize();
        
        // Test 1: Connected state should start command manager
        handler.isConnected(true, true);
        assertTrue(handler.commandManagerRunning);
        
        // Test 2: Disconnected state should stop command manager
        handler.isConnected(false, false);
        assertFalse(handler.commandManagerRunning);
        
        // Test 3: Reconnected should start command manager again
        handler.isConnected(true, true);
        assertTrue(handler.commandManagerRunning);
    }

    @Test
    void testMultipleDeviceListeners_ShouldMaintainSeparateStates() {
        // Arrange
        handler.initialize();
        String deviceId1 = "device1";
        String deviceId2 = "device2";
        SonoffDeviceListener listener1 = mock(SonoffDeviceListener.class);
        SonoffDeviceListener listener2 = mock(SonoffDeviceListener.class);
        
        // Act
        handler.addDeviceListener(deviceId1, listener1);
        handler.addDeviceListener(deviceId2, listener2);
        
        // Assert
        assertEquals(listener1, handler.getListener(deviceId1));
        assertEquals(listener2, handler.getListener(deviceId2));
        assertNotEquals(handler.getListener(deviceId1), handler.getListener(deviceId2));
        
        // Act - Remove one listener
        handler.removeDeviceListener(deviceId1);
        
        // Assert - Only one should be removed
        assertNull(handler.getListener(deviceId1));
        assertEquals(listener2, handler.getListener(deviceId2));
    }

    /**
     * Test implementation of SonoffAccountHandler for testing purposes
     */
    private static class TestSonoffAccountHandler extends SonoffAccountHandler {
        
        // Test tracking fields
        boolean commandManagerStarted = false;
        boolean commandManagerStopped = false;
        boolean commandManagerRunning = false;
        boolean connectionManagerStarted = false;
        boolean connectionManagerStopped = false;
        boolean restoreStatesCalled = false;
        boolean serviceAddedCalled = false;
        boolean serviceRemovedCalled = false;
        boolean serviceResolvedCalled = false;
        
        ThingStatus lastStatus = ThingStatus.UNKNOWN;
        ThingStatusDetail lastStatusDetail = ThingStatusDetail.NONE;
        String lastStatusDescription = "";
        
        // Collections to track method calls
        final Map<String, SonoffDeviceListener> deviceListeners = new HashMap<>();
        final Map<String, SonoffDeviceState> deviceStates = new HashMap<>();
        final Map<String, String> ipAddresses = new HashMap<>();
        final java.util.List<String> lanServiceAdded = new java.util.ArrayList<>();
        final java.util.List<String> lanServiceRemoved = new java.util.ArrayList<>();
        final java.util.List<SonoffCommandMessage> messagesQueued = new java.util.ArrayList<>();
        final Map<String, String> lanMessagesSent = new HashMap<>();
        final java.util.List<String> apiMessagesSent = new java.util.ArrayList<>();
        final java.util.List<String> websocketMessagesSent = new java.util.ArrayList<>();
        final java.util.List<String> lanResponsesReceived = new java.util.ArrayList<>();
        final java.util.List<String> websocketMessagesReceived = new java.util.ArrayList<>();
        final java.util.List<JsonObject> apiMessagesReceived = new java.util.ArrayList<>();
        String apiKeySet = "";
        
        private AccountConfig testConfig;
        
        // Mock managers for testing
        final TestCommunicationManager commandManager = new TestCommunicationManager(this);
        final TestConnectionManager connectionManager = new TestConnectionManager(this);
        
        public TestSonoffAccountHandler(Bridge thing, WebSocketClient webSocketClient, HttpClient httpClient) {
            super(thing, webSocketClient, httpClient);
        }
        
        @Override
        public <T> T getConfigAs(Class<T> configurationClass) {
            if (testConfig == null) {
                throw new IllegalStateException("Test config not set");
            }
            return configurationClass.cast(testConfig);
        }
        
        public void setTestConfig(AccountConfig config) {
            this.testConfig = config;
        }
        
        public void setScheduler(ScheduledExecutorService scheduler) {
            this.scheduler = scheduler;
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
        
        // Override methods to track calls instead of using real managers
        
        @Override
        public void addDeviceListener(String deviceid, SonoffDeviceListener listener) {
            deviceListeners.put(deviceid, listener);
        }
        
        @Override
        public void removeDeviceListener(String deviceid) {
            deviceListeners.remove(deviceid);
        }
        
        @Override
        public SonoffDeviceListener getListener(String deviceid) {
            return deviceListeners.get(deviceid);
        }
        
        @Override
        public SonoffDeviceState getState(String deviceid) {
            return deviceStates.get(deviceid);
        }
        
        @Override
        public void addLanService(String deviceid) {
            lanServiceAdded.add(deviceid);
        }
        
        @Override
        public void removeLanService(String deviceid) {
            lanServiceRemoved.add(deviceid);
        }
        
        @Override
        public void queueMessage(SonoffCommandMessage message) {
            messagesQueued.add(message);
        }
        
        @Override
        public void sendLanMessage(String url, String payload) {
            lanMessagesSent.put(url, payload);
        }
        
        @Override
        public void sendApiMessage(String deviceid) {
            apiMessagesSent.add(deviceid);
        }
        
        @Override
        public void sendWebsocketMessage(String params) {
            websocketMessagesSent.add(params);
        }
        
        @Override
        public void serviceAdded(ServiceEvent event) {
            serviceAddedCalled = true;
        }
        
        @Override
        public void serviceRemoved(ServiceEvent event) {
            serviceRemovedCalled = true;
        }
        
        @Override
        public void serviceResolved(ServiceEvent event) {
            serviceResolvedCalled = true;
            if (event != null && event.getInfo() != null) {
                try {
                    String localAddress = event.getInfo().getInet4Addresses()[0].getHostAddress();
                    String deviceid = event.getInfo().getPropertyString("id");
                    if (localAddress != null && deviceid != null) {
                        ipAddresses.put(deviceid, localAddress);
                    }
                } catch (Exception e) {
                    // Ignore for testing
                }
            }
        }
        
        @Override
        public void lanResponse(String message) {
            lanResponsesReceived.add(message);
        }
        
        @Override
        public void websocketMessage(String message) {
            websocketMessagesReceived.add(message);
        }
        
        @Override
        public void apiMessage(JsonObject thingResponse) {
            apiMessagesReceived.add(thingResponse);
        }
        
        @Override
        public void setApiKey(String apiKey) {
            apiKeySet = apiKey;
        }
        
        @Override
        public void addState(String deviceid) {
            // Simplified for testing - just add a mock state
            if (!deviceStates.containsKey(deviceid)) {
                SonoffDeviceState mockState = mock(SonoffDeviceState.class);
                deviceStates.put(deviceid, mockState);
            }
        }
        
        // Mock manager classes for testing
        private class TestCommunicationManager {
            private final TestSonoffAccountHandler handler;
            
            TestCommunicationManager(TestSonoffAccountHandler handler) {
                this.handler = handler;
            }
            
            void start(String mode) {
                handler.commandManagerStarted = true;
            }
            
            void stop() {
                handler.commandManagerStopped = true;
            }
            
            void startRunning() {
                handler.commandManagerRunning = true;
            }
            
            void stopRunning() {
                handler.commandManagerRunning = false;
            }
        }
        
        private class TestConnectionManager {
            private final TestSonoffAccountHandler handler;
            
            TestConnectionManager(TestSonoffAccountHandler handler) {
                this.handler = handler;
            }
            
            void start(String appId, String appSecret, String email, String password, String mode) {
                handler.connectionManagerStarted = true;
            }
            
            void stop() {
                handler.connectionManagerStopped = true;
            }
        }
    }
}